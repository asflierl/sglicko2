/*
 * Copyright (c) 2015, Andreas Flierl <andreas@flierl.eu>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package sglicko2

import scala.math.{abs, exp, sqrt, log => ln, Pi => π}
import scala.collection.breakOut

// implements the (public domain) Glicko 2 algorithm; see http://www.glicko.net/glicko.html for further details
class Glicko2[A, B: ScoringRules](val tau: Double = 0.6d) {
  require(tau > 0d && tau < Double.PositiveInfinity, s"the system constant τ ($tau) must be a number greater than 0")

  private final val glicko2Scalar = 173.7178d
  private final val ε = 0.000001d

  private def g(φ: Double): Double = 1d / sqrt(1d + 3d * φ.`²` / π.`²`)
  private def E(µ: Double, µj: Double, φj: Double): Double = 1d / (1d + exp(-g(φj) * (µ - µj)))

  @inline private final def τ = tau

  def newRatingPeriod: RatingPeriod[A, B] = RatingPeriod[A, B]()
  def newLeaderboard: Leaderboard[A] = Leaderboard.empty
  def newPlayer(id: A): Player[A] = Player(id)

  def updatedLeaderboard(currentLeaderboard: Leaderboard[A], ratingPeriod: RatingPeriod[A, B]): Leaderboard[A] = {
    val competingPlayers = ratingPeriod.games.toStream.par.map {
      case (id, matchResults) =>
        updatedRatingAndDeviationAndVolatility(id, matchResults, currentLeaderboard)
    }

    val notCompetingPlayers =
      currentLeaderboard.playersByIdInNoParticularOrder.keys.toStream.par
                        .filterNot(ratingPeriod.games.contains)
                        .flatMap(id => updatedDeviation(id, currentLeaderboard))

    currentLeaderboard.updatedWith((competingPlayers ++ notCompetingPlayers)(breakOut))
  }

  private def updatedRatingAndDeviationAndVolatility(playerID: A, matchResults: List[ScoreAgainstAnotherPlayer[A]], leaderboard: Leaderboard[A]): Player[A] = {
    val player = leaderboard.playerIdentifiedBy(playerID).orNew
    import player._

    val opponentsAndMatchResults = matchResults.map(s => (leaderboard playerIdentifiedBy s.opponentID orNew, s))

    // Step 3
    val ν = 1d / (opponentsAndMatchResults map { case (opponent, matchResult) =>
      val Eµ = E(µ, opponent µ, opponent φ)
      g(opponent.φ).`²` * Eµ * (1d - Eµ)
    } sum)

    // Step 4
    val ∆ = ν * (opponentsAndMatchResults map { case (opponent, matchResult) =>
      g(opponent φ) * (matchResult.score - E(µ, opponent µ, opponent φ))
    } sum)

    // Step 5
    val a = ln(σ.`²`)

    def f(x: Double) = ((exp(x) * (∆.`²` - φ.`²` - ν - exp(x))) / (2d * (φ.`²` + ν + exp(x)).`²`)) - ((x - a) / τ.`²`)

    var A = a
    var k = 1d
    var B =
      if (∆.`²` > (φ.`²` + ν)) ln(∆.`²` - φ.`²` - ν)
      else {
        while (f(a - k * τ) < 0d) k += 1d
        a - k * τ
      }

    var fA = f(A)
    var fB = f(B)

    while (abs(B - A) > ε) {
      val C = A + (A - B) * fA / (fB - fA)
      val fC = f(C)

      if (fC * fB < 0) {
        A = B
        fA = fB
      } else fA /= 2d

      B = C
      fB = fC
    }

    val `σ'` = exp(A / 2d)

    // Step 6
    val `φ*` = sqrt(φ.`²` + `σ'`.`²`)

    // Step 7
    val `φ'` = 1d / sqrt((1d / `φ*`.`²`) + (1d / ν))
    val `µ'` = µ + `φ'`.`²` * (opponentsAndMatchResults map { case (opponent, matchResult) =>
      g(opponent φ) * (matchResult.score - E(µ, opponent µ, opponent φ))
    } sum)

    // Step 8
    Player(player id, `µ'` * glicko2Scalar + 1500d, `φ'` * glicko2Scalar, `σ'`)
  }

  private def updatedDeviation(playerID: A, leaderboard: Leaderboard[A]): Option[Player[A]] =
    leaderboard.playerIdentifiedBy(playerID).right.toOption.map { player =>
      // Step 7
      val `φ'` = sqrt(player.φ.`²` + player.σ.`²`)

      // Step 8
      Player(player id, player rating, `φ'` * glicko2Scalar, player volatility)
    }

  override def toString: String = s"Glicko2(τ = $τ, scoring rules = ${implicitly[ScoringRules[B]]})"
}
