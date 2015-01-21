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

case class RatingPeriod[A, B] private[sglicko2] (games: Map[A, List[ScoreAgainstAnotherPlayer[A]]] =
    Map.empty[A, List[ScoreAgainstAnotherPlayer[A]]].withDefaultValue(Nil))(implicit rules: ScoringRules[B]) {

  def withGame(player1: A, player2: A, outcome: B): RatingPeriod[A, B] = {
    require(player1 != player2, s"player1 ($player1) and player2 ($player2) must not be the same player")
    val score = rules.scoreForTwoPlayers(outcome)

    val outcomes1 = ScoreAgainstAnotherPlayer(player2, score.asSeenFromPlayer1) :: games(player1)
    val outcomes2 = ScoreAgainstAnotherPlayer(player1, score.asSeenFromPlayer2) :: games(player2)

    copy(games.updated(player1, outcomes1).updated(player2, outcomes2))
  }

  def withGames(games: (A, A, B)*): RatingPeriod[A, B] = games.foldLeft(this)((p, g) => p.withGame(g._1, g._2, g._3))
}
