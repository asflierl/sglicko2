// SPDX-License-Identifier: ISC

package sglicko2

import scala.reflect.TypeTest

final class Leaderboard[P: Eq] private (val playersByIdInNoParticularOrder: Map[P, Player[P]]) extends Serializable derives CanEqual:
  import Ordering.Double.TotalOrdering

  def after[P2 >: P : Eq](ratingPeriod: RatingPeriod[? <: P2])(using glicko2: Glicko2): Leaderboard[P2] =
    val competingPlayers = ratingPeriod.games.iterator.map { (id, matchResults) =>
      glicko2.updatedRatingAndDeviationAndVolatility(id, matchResults, playersByIdInNoParticularOrder.asInstanceOf[Map[P2, Player[P2]]].get)
    }

    val notCompetingPlayers = playersByIdInNoParticularOrder
      .valuesIterator
      .filterNot(p => ratingPeriod.games.asInstanceOf[Map[P2, Player[P2]]].contains(p.id))
      .map(glicko2.updatedDeviation)

    Leaderboard.fromPlayers(competingPlayers ++ notCompetingPlayers)

  lazy val idsByRank: Vector[Set[P]] = playersByIdInNoParticularOrder.values.groupBy(_.rating).toVector.sortBy(e => - Rating.toGlicko2(e._1)).map((_, ps) => ps.view.map(_.id).toSet)
  lazy val rankedPlayers: Vector[RankedPlayer[P]] = idsByRank.zipWithIndex.flatMap((ids, idx) => ids.map(id => RankedPlayer(Rank(idx + 1), playersByIdInNoParticularOrder(id))))
  lazy val playersInRankOrder: Vector[Player[P]] = idsByRank.flatMap(_ map playersByIdInNoParticularOrder)

  def playerIdentifiedBy(id: P): Option[Player[P]] = playersByIdInNoParticularOrder.get(id)

  def rankOf(id: P): Option[Rank] = idsByRank.indexWhere(_ contains id) match
    case n if n < 0 => None
    case other => Some(Rank(other + 1))

  override def equals(any: Any): Boolean = any match
    case other: Leaderboard[?] => other.playersByIdInNoParticularOrder equals playersByIdInNoParticularOrder
    case _ => false

  override def hashCode: Int = playersByIdInNoParticularOrder.hashCode

  override def toString = s"Leaderboard(... ${playersByIdInNoParticularOrder.size} player(s) ...)"


object Leaderboard:
  val Empty = new Leaderboard[Nothing](Map.empty)
  def empty[P: Eq]: Leaderboard[P] = Empty.asInstanceOf[Leaderboard[P]]
  def fromPlayers[P: Eq](players: IterableOnce[Player[P]]) = new Leaderboard[P](players.iterator.map(p => p.id -> p).toMap)
