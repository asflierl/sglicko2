/*
 * Copyright (c) 2015, Andreas Flierl
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package sglicko2

import scala.math.{abs, exp, sqrt, log => ln, Pi => π}
import scala.collection.breakOut

// implements the (public domain) Glicko 2 algorithm; see http://www.glicko.net/glicko.html for further details
class Glicko2[A, B: ScoringRules](val tau: Double = 0.6d) {
  private final val glicko2Scalar = 173.7178d
  private final val ε = 0.000001d

  private def g(φ: Double): Double = 1d / sqrt(1d + 3d * φ.`²` / π.`²`)
  private def E(µ: Double, µj: Double, φj: Double): Double = 1d / (1d + exp(-g(φj) * (µ - µj)))

  @inline private final def τ = tau

  def newRatingPeriod: RatingPeriod[A, B] = RatingPeriod[A, B]()
  def newLeaderboard: Leaderboard[A] = Leaderboard.empty
  def newPlayer(id: A): Player[A] = Player(id)

  def updatedLeaderboard(currentLeaderboard: Leaderboard[A], ratingPeriod: RatingPeriod[A, B]): Leaderboard[A] = {
    val competingPlayers: Vector[Player[A]] = ratingPeriod.games.map {
      case (id, matchResults) =>
        updatedRatingAndDeviationAndVolatility(id, matchResults, currentLeaderboard)
    }(breakOut)

    val notCompetingPlayers: Vector[Player[A]] =
      currentLeaderboard.playersByIdInNoParticularOrder.keySet
                        .filterNot(ratingPeriod.games.contains)
                        .flatMap(id => updatedDeviation(id, currentLeaderboard))(breakOut)

    Leaderboard.fromPlayers(competingPlayers ++ notCompetingPlayers)
  }

  private def updatedRatingAndDeviationAndVolatility(playerID: A, matchResults: Traversable[ScoreAgainstAnotherPlayer[A]], leaderboard: Leaderboard[A]): Player[A] = {
    val player = leaderboard.playerIdentifiedBy(playerID).orNew
    import player._

    // Step 3
    val ν = 1d / (matchResults map { matchResult =>
      val opponent = leaderboard.playerIdentifiedBy(matchResult opponentID).orNew
      val Eµ = E(µ, opponent µ, opponent φ)
      g(opponent.φ).`²` * Eµ * (1d - Eµ)
    } sum)

    // Step 4
    val ∆ = ν * (matchResults map { matchResult =>
      val opponent = leaderboard.playerIdentifiedBy(matchResult opponentID).orNew
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
    val `µ'` = µ + `φ'`.`²` * (matchResults map { matchResult =>
      val opponent = leaderboard.playerIdentifiedBy(matchResult opponentID).orNew
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
