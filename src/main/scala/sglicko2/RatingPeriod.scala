// SPDX-License-Identifier: ISC

package sglicko2

import scala.collection.mutable.{HashMap, ReusableBuilder}

final class RatingPeriod[A: Eq, B: ScoringRulesC[A]] private[sglicko2] (
    val games: Map[A, Vector[ScoreVsPlayer[A]]]):

  def withGames(gamesToAdd: B*): RatingPeriod[A, B] = new RatingPeriod(RatingPeriod.updated(games, gamesToAdd*)) 

object RatingPeriod:
  def empty[A: Eq, B: ScoringRulesC[A]]: RatingPeriod[A, B] = new RatingPeriod(Map.empty)

  def apply[A: Eq, B: ScoringRulesC[A]](gamesToAdd: B*): RatingPeriod[A, B] =
    new RatingPeriod(updated(Map.empty, gamesToAdd*))

  private def updated[A: Eq, B: ScoringRulesC[A]](games: Map[A, Vector[ScoreVsPlayer[A]]], gamesToAdd: B*) =
    val rules = summon[ScoringRules[A, B]]
    val mm = HashMap.empty[A, ReusableBuilder[ScoreVsPlayer[A], Vector[ScoreVsPlayer[A]]]]

    games.foreach { (id, gamesOfPlayer) => mm.put(id, {
      val builder = Vector.newBuilder[ScoreVsPlayer[A]]
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
