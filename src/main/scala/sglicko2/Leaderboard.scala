// SPDX-License-Identifier: ISC

package sglicko2

final class Leaderboard[A: Eq] private (val playersByIdInNoParticularOrder: Map[A, Player[A]]) extends Serializable derives CanEqual:
  import Ordering.Double.TotalOrdering

  def after[B: [b] =>> ScoringRules[A, b]](ratingPeriod: RatingPeriod[A, B])(using glicko2: Glicko2): Leaderboard[A] =
    val competingPlayers = ratingPeriod.games.iterator.map { (id, matchResults) =>
      glicko2.updatedRatingAndDeviationAndVolatility(id, matchResults, playersByIdInNoParticularOrder.get)
    }

    val notCompetingPlayers = playersByIdInNoParticularOrder
      .valuesIterator
      .filterNot(p => ratingPeriod.games.contains(p.id))
      .map(glicko2.updatedDeviation)

    Leaderboard.fromPlayers(competingPlayers ++ notCompetingPlayers)

  lazy val idsByRank: Vector[Set[A]] = playersByIdInNoParticularOrder.values.groupBy(_.rating).toVector.sortBy(e => - Rating.toGlicko2(e._1)).map((_, ps) => ps.view.map(_.id).toSet)
  lazy val rankedPlayers: Vector[RankedPlayer[A]] = idsByRank.zipWithIndex.flatMap((ids, idx) => ids.map(id => RankedPlayer(Rank(idx + 1), playersByIdInNoParticularOrder(id))))
  lazy val playersInRankOrder: Vector[Player[A]] = idsByRank.flatMap(_ map playersByIdInNoParticularOrder)

  def playerIdentifiedBy(id: A): Option[Player[A]] = playersByIdInNoParticularOrder.get(id)

  def rankOf(id: A): Option[Rank] = idsByRank.indexWhere(_ contains id) match
    case n if n < 0 => None
    case other => Some(Rank(other + 1))

  override def equals(any: Any): Boolean = any match
    case other: Leaderboard[?] => other.playersByIdInNoParticularOrder equals playersByIdInNoParticularOrder
    case _ => false

  override def hashCode: Int = playersByIdInNoParticularOrder.hashCode

  override def toString = s"Leaderboard(... ${playersByIdInNoParticularOrder.size} player(s) ...)"


object Leaderboard:
  def empty[A: Eq] = new Leaderboard[A](Map())
  def fromPlayers[A: Eq](players: IterableOnce[Player[A]]) = new Leaderboard[A](players.iterator.map(p => p.id -> p).toMap)