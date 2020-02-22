/*
 * Copyright (c) 2020, Andreas Flierl <andreas@flierl.eu>
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

import scala.collection.mutable.{HashMap, ReusableBuilder}

final case class RatingPeriod[A, B] private[sglicko2] (games: Map[A, Vector[ScoreAgainstAnotherPlayer[A]]] =
    Map.empty[A, Vector[ScoreAgainstAnotherPlayer[A]]])(implicit rules: ScoringRules[B]) {

  def withGame(player1: A, player2: A, outcome: B): RatingPeriod[A, B] = {
    require(player1 != player2, s"player1 ($player1) and player2 ($player2) must not be the same player")
    val score = rules.scoreForTwoPlayers(outcome)

    val outcomes1 = ScoreAgainstAnotherPlayer(player2, score.asSeenFromPlayer1) +: games.getOrElse(player1, Vector.empty)
    val outcomes2 = ScoreAgainstAnotherPlayer(player1, score.asSeenFromPlayer2) +: games.getOrElse(player2, Vector.empty)

    copy(games.updated(player1, outcomes1).updated(player2, outcomes2))
  }

  def withGames(gamesToAdd: (A, A, B)*): RatingPeriod[A, B] = {
    val mm = HashMap.empty[A, ReusableBuilder[ScoreAgainstAnotherPlayer[A], Vector[ScoreAgainstAnotherPlayer[A]]]]

    games.foreach { case (id, gamesOfPlayer)  => mm.put(id, {
      val builder = Vector.newBuilder[ScoreAgainstAnotherPlayer[A]]
      builder ++= gamesOfPlayer
      builder
    })}

    gamesToAdd.foreach { case (player1, player2, outcome) =>
      require(player1 != player2, s"player1 ($player1) and player2 ($player2) must not be the same player")

      val score = rules.scoreForTwoPlayers(outcome)

      mm.getOrElseUpdate(player1, Vector.newBuilder) += ScoreAgainstAnotherPlayer(player2, score.asSeenFromPlayer1)
      mm.getOrElseUpdate(player2, Vector.newBuilder) += ScoreAgainstAnotherPlayer(player1, score.asSeenFromPlayer2)
    }

    val newGames = mm.view.mapValues(_.result).toMap

    copy(games = newGames)
  }
}
