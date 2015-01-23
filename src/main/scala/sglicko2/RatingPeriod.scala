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

import scala.collection.mutable.{HashMap, ListBuffer}

case class RatingPeriod[A, B] private[sglicko2] (games: Map[A, List[ScoreAgainstAnotherPlayer[A]]] =
    Map.empty[A, List[ScoreAgainstAnotherPlayer[A]]].withDefaultValue(Nil))(implicit rules: ScoringRules[B]) {

  def withGame(player1: A, player2: A, outcome: B): RatingPeriod[A, B] = {
    require(player1 != player2, s"player1 ($player1) and player2 ($player2) must not be the same player")
    val score = rules.scoreForTwoPlayers(outcome)

    val outcomes1 = ScoreAgainstAnotherPlayer(player2, score.asSeenFromPlayer1) :: games(player1)
    val outcomes2 = ScoreAgainstAnotherPlayer(player1, score.asSeenFromPlayer2) :: games(player2)

    copy(games.updated(player1, outcomes1).updated(player2, outcomes2))
  }

  def withGames(gamesToAdd: (A, A, B)*): RatingPeriod[A, B] = {
    val mm = HashMap.empty[A, ListBuffer[ScoreAgainstAnotherPlayer[A]]].withDefaultValue(ListBuffer.empty)

    games.foreach {
      case (k, v) => mm.put(k, mm(k) ++ v)
    }

    gamesToAdd.foreach {
      case (player1, player2, outcome) =>
        require(player1 != player2, s"player1 ($player1) and player2 ($player2) must not be the same player")

        val score = rules.scoreForTwoPlayers(outcome)

        val outcome1 = ScoreAgainstAnotherPlayer(player2, score.asSeenFromPlayer1)
        if (! mm.contains(player1)) mm.put(player1, ListBuffer(outcome1))
        else mm(player1).append(outcome1)

        val outcome2 = ScoreAgainstAnotherPlayer(player1, score.asSeenFromPlayer2)
        if (! mm.contains(player2)) mm.put(player2, ListBuffer(outcome2))
        else mm(player2).append(outcome2)
    }

    val newGames = mm.mapValues(_ toList).toMap

    copy(games = newGames)
  }
}
