// SPDX-License-Identifier: ISC

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

    new RatingPeriod(newGames)

object RatingPeriod:
  def apply[A: Eq, G[_]: ScoringRules](gamesToAdd: G[A]*): RatingPeriod[A, G] =
    new RatingPeriod(Map.empty).withGames(gamesToAdd*)