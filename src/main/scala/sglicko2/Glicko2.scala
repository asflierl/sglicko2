// SPDX-License-Identifier: ISC

package sglicko2

import scala.math.{abs, exp, sqrt, log as ln, Pi as π}

/* 
 * Implements the (public domain) Glicko 2 algorithm. 
 * See http://www.glicko.net/glicko.html for further details.
 * Please note that this implementation does NOT convert to and from the original Glicko scale for each calculation.
 */ 
final class Glicko2(val tau: Tau = Tau.default, 
    val defaultVolatility: Volatility = Volatility.default, val scale: Scale = Scale.Glicko) extends Serializable:

  private inline val ε = 0.000001d
  private inline def τ = tau.value

  extension [A](inline p: Player[A])
    // Step 1
    private inline def r: Double = Rating.toGlicko2(p.rating)
    private inline def rd: Double = Deviation.toGlicko2(p.deviation)
    private inline def σ: Double = p.volatility.value

    // Step 2
    private inline def µ: Double = r
    private inline def φ: Double = rd
  
  extension (n: Double)
    private def `²`: Double = n * n

  def updatedRatingAndDeviationAndVolatility[A: Eq](playerID: A, matchResults: IndexedSeq[ScoreVsPlayer[A]], lookup: A => Option[Player[A]]): Player[A] =
    val default = Player(playerID, volatility = defaultVolatility)
    val player = lookup(playerID).getOrElse(default)

    val opponents = Array.tabulate(matchResults.size)(n => lookup(matchResults(n).opponentID).getOrElse(default))

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

  def updatedDeviation[A: Eq](player: Player[A]): Player[A] =
    // Step 7
    val `φ'` = sqrt(player.φ.`²` + player.σ.`²`)

    // Step 8
    Player(player.id, player.rating, Deviation.fromGlicko2(`φ'`), player.volatility)

  private def g(φ: Double): Double = 1d / sqrt(1d + 3d * φ.`²` / π.`²`)
  private def E(µ: Double, µj: Double, φj: Double): Double = 1d / (1d + exp(-g(φj) * (µ - µj)))

  override def toString: String = s"Glicko2(τ = $tau, defaultVolatility = $defaultVolatility)"
