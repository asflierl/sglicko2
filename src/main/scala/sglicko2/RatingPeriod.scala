/*
 * Copyright (c) 2021, Andreas Flierl <andreas@flierl.eu>
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

final class RatingPeriod[A: Eq, G[_]: ScoringRules] private[sglicko2] (
    val games: Map[A, Vector[ScoreVsPlayer[A]]]):

  def withGames(gamesToAdd: G[A]*): RatingPeriod[A, G] = 
    val mm = HashMap.empty[A, ReusableBuilder[ScoreVsPlayer[A], Vector[ScoreVsPlayer[A]]]]

    games.foreach { (id, gamesOfPlayer) => mm.put(id, {
      val builder = Vector.newBuilder[ScoreVsPlayer[A]]
      builder ++= gamesOfPlayer
      builder
    })}

    gamesToAdd.iterator
      .flatMap(summon[ScoringRules[G]].gameScores(_).iterator)
      .filter((p1, p2, _) => p1 != p2)
      .foreach { (player1, player2, score) =>
        mm.getOrElseUpdate(player1, Vector.newBuilder) += ScoreVsPlayer(player2, score.asSeenFromPlayer1)
        mm.getOrElseUpdate(player2, Vector.newBuilder) += ScoreVsPlayer(player1, score.asSeenFromPlayer2)
      }

    val newGames = mm.view.mapValues(_.result()).toMap

    RatingPeriod(newGames)
