// SPDX-License-Identifier: ISC

package sglicko2

import scala.collection.mutable.{HashMap, ReusableBuilder}

final class RatingPeriod[P: Eq] private (val games: Map[P, Vector[ScoreVsPlayer[P]]]):

  def withGames[G](gamesToAdd: G*)(using rules: ScoringRules.For[P, G])(using Eq[rules.P]): RatingPeriod[rules.P] = 
    new RatingPeriod(RatingPeriod.updated(games, gamesToAdd*))

object RatingPeriod:
  val Empty = new RatingPeriod[Nothing](Map.empty)
  def empty[P: Eq]: RatingPeriod[P] = Empty.asInstanceOf[RatingPeriod[P]]

  def apply[G](gamesToAdd: G*)(using rules: ScoringRules[G])(using Eq[rules.P]): RatingPeriod[rules.P] =
    new RatingPeriod(updated(Map.empty, gamesToAdd*))

  private def updated[P, G](games: Map[P, Vector[ScoreVsPlayer[P]]], gamesToAdd: G*)(using rules: ScoringRules.For[P, G])(using Eq[rules.P]): Map[rules.P, Vector[ScoreVsPlayer[rules.P]]] =
    val mm = HashMap.empty[rules.P, ReusableBuilder[ScoreVsPlayer[rules.P], Vector[ScoreVsPlayer[rules.P]]]]

    games.foreach { (id, gamesOfPlayer) => mm.put(id, {
      val builder = Vector.newBuilder[ScoreVsPlayer[rules.P]]
      builder ++= gamesOfPlayer
      builder
    })}

    gamesToAdd.iterator
      .flatMap(rules.gameScores(_).iterator)
      .filter((p1, p2, _) => p1 != p2)
      .foreach { (player1, player2, score) =>
        mm.getOrElseUpdate(player1, Vector.newBuilder) += ScoreVsPlayer(player2, score.asSeenFromPlayer1)
        mm.getOrElseUpdate(player2, Vector.newBuilder) += ScoreVsPlayer(player1, score.asSeenFromPlayer2)
      }

    mm.iterator.map(_ -> _.result()).toMap
