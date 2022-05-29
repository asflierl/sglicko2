// SPDX-License-Identifier: ISC

package sglicko2

import scala.math.{abs, exp, sqrt, log as ln, Pi as π}

/* 
 * Implements the (public domain) Glicko 2 algorithm. 
 * See http://www.glicko.net/glicko.html for further details.
 * Please note that this implementation does NOT convert to and from the original Glicko scale for each calculation.
 */ 
final class Glicko2[A: Eq, B[_]: ScoringRules](tau: Tau = Tau.default, defaultVolatility: Volatility = Volatility.default) extends Serializable:
  def newRatingPeriod: RatingPeriod[A, B] = RatingPeriod[A, B](Map.empty)
  def newLeaderboard: Leaderboard[A] = Leaderboard.empty
  def newPlayer(id: A): Player[A] = Player(id)

  def updatedLeaderboard(currentLeaderboard: Leaderboard[A], ratingPeriod: RatingPeriod[A, B]): Leaderboard[A] =
    val competingPlayers = ratingPeriod.games.iterator.map { (id, matchResults) =>
      updatedRatingAndDeviationAndVolatility(id, matchResults, currentLeaderboard)
    }

    val notCompetingPlayers =
      currentLeaderboard.playersByIdInNoParticularOrder.keysIterator
                        .filterNot(ratingPeriod.games.contains)
                        .map(id => updatedDeviation(id, currentLeaderboard))

    Leaderboard.fromPlayers(competingPlayers ++ notCompetingPlayers)

  extension (currentLeaderboard: Leaderboard[A]) def after(ratingPeriod: RatingPeriod[A, B]): Leaderboard[A] = 
    updatedLeaderboard(currentLeaderboard, ratingPeriod)

  override def toString: String = s"Glicko2(τ = $tau, defaultVolatility = $defaultVolatility, rules = ${summon[ScoringRules[B]]})"

  private object Private:
    inline val ε = 0.000001d
    inline def τ = tau.value

    extension [A](inline p: Player[A])
        // Step 1
      inline def r: Double = Rating.toGlicko2(p.rating)
      inline def rd: Double = Deviation.toGlicko2(p.deviation)
      inline def σ: Double = p.volatility.value

      // Step 2
      inline def µ: Double = r
      inline def φ: Double = rd
    
    extension (n: Double)
      def `²`: Double = n * n

  import Private._

  private def updatedRatingAndDeviationAndVolatility(playerID: A, matchResults: Vector[ScoreVsPlayer[A]], leaderboard: Leaderboard[A]): Player[A] =
    val default = Player(playerID, volatility = defaultVolatility)
    val player = leaderboard.playersByIdInNoParticularOrder.getOrElse(playerID, default)

    val opponents = Array.tabulate(matchResults.size)(n => leaderboard.playersByIdInNoParticularOrder.getOrElse(matchResults(n).opponentID, default))

    inline def sumOverOpponentsAndMatchResults(inline f: (Player[A], ScoreVsPlayer[A]) => Double): Double =
      var n = 0
      var s = 0d
      while n < matchResults.size do
        s += f(opponents(n), matchResults(n))
        n += 1
      s

    // Step 3
    val ν = 1d / (sumOverOpponentsAndMatchResults { (opponent, matchResult) =>
      val Eµ = E(player.µ, opponent.µ, opponent.φ)
      g(opponent.φ).`²` * Eµ * (1d - Eµ)
    })

    // Step 4
    val ∆ = ν * (sumOverOpponentsAndMatchResults { (opponent, matchResult) =>
      g(opponent.φ) * (matchResult.score.value - E(player.µ, opponent.µ, opponent.φ))
    })

    // Step 5
    val a = ln(player.σ.`²`)

    inline def f(x: Double) = ((exp(x) * (∆.`²` - player.φ.`²` - ν - exp(x))) / (2d * (player.φ.`²` + ν + exp(x)).`²`)) - ((x - a) / τ.`²`)

    var A = a
    var k = 1d
    var B =
      if ∆.`²` > (player.φ.`²` + ν) then ln(∆.`²` - player.φ.`²` - ν)
      else
        while f(a - k * τ) < 0d do k += 1d
        a - k * τ

    var fA = f(A)
    var fB = f(B)

    while abs(B - A) > ε do
      val C = A + (A - B) * fA / (fB - fA)
      val fC = f(C)

      if fC * fB < 0 then
        A = B
        fA = fB
      else fA /= 2d

      B = C
      fB = fC

    val `σ'` = exp(A / 2d)

    // Step 6
    val `φ*` = sqrt(player.φ.`²` + `σ'`.`²`)

    // Step 7
    val `φ'` = 1d / sqrt((1d / `φ*`.`²`) + (1d / ν))
    val `µ'` = player.µ + `φ'`.`²` * (sumOverOpponentsAndMatchResults { (opponent, matchResult) =>
      g(opponent.φ) * (matchResult.score.value - E(player.µ, opponent.µ, opponent.φ))
    })

    // Step 8
    Player(playerID, Rating.fromGlicko2(`µ'`), Deviation.fromGlicko2(`φ'`), Volatility(`σ'`))

  private def updatedDeviation(playerID: A, leaderboard: Leaderboard[A]): Player[A] =
    val player = leaderboard.playersByIdInNoParticularOrder(playerID)
    // Step 7
    val `φ'` = sqrt(player.φ.`²` + player.σ.`²`)

    // Step 8
    Player(player.id, player.rating, Deviation.fromGlicko2(`φ'`), player.volatility)

  private def g(φ: Double): Double = 1d / sqrt(1d + 3d * φ.`²` / π.`²`)
  private def E(µ: Double, µj: Double, φj: Double): Double = 1d / (1d + exp(-g(φj) * (µ - µj)))
