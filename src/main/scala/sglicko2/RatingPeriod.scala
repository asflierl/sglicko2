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
    val mm = new HashMap[A, ListBuffer[ScoreAgainstAnotherPlayer[A]]]() {
      override protected def initialSize = 262144
    }.withDefaultValue(null)

    games.foreach {
      case (k, v) =>
        val g = mm(k)
        if (g eq null) mm.put(k, v.to[ListBuffer])
        else g ++= v
    }

    gamesToAdd.foreach {
      case (player1, player2, outcome) =>
        require(player1 != player2, s"player1 ($player1) and player2 ($player2) must not be the same player")

        val score = rules.scoreForTwoPlayers(outcome)
        val outcome1 = ScoreAgainstAnotherPlayer(player2, score.asSeenFromPlayer1)
        val outcome2 = ScoreAgainstAnotherPlayer(player1, score.asSeenFromPlayer2)

        val g1 = mm(player1)
        val g2 = mm(player2)

        if (g1 eq null) mm.put(player1, ListBuffer(outcome1))
        else g1 += outcome1

        if (g2 eq null) mm.put(player2, ListBuffer(outcome2))
        else g2 += outcome2
    }

    val newGames: Map[A, List[ScoreAgainstAnotherPlayer[A]]] = mm.mapValues(_ toList).toMap

    copy(games = newGames)
  }
}
