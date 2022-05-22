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

final class Leaderboard[A: Eq] private (val playersByIdInNoParticularOrder: Map[A, Player[A]]) extends Serializable derives CanEqual:
  import Ordering.Double.TotalOrdering

  lazy val idsByRank: Vector[Set[A]] = playersByIdInNoParticularOrder.values.groupBy(_.rating).toVector.sortBy(e => - e._1.value).map { case (_, ps) => ps.view.map(_.id).toSet }
  lazy val rankedPlayers: Vector[RankedPlayer[A]] = idsByRank.zipWithIndex.flatMap { case (ids, idx) => ids.map(id => RankedPlayer(liftRank(idx + 1), playersByIdInNoParticularOrder(id))) }
  lazy val playersInRankOrder: Vector[Player[A]] = idsByRank.flatMap(_ map playersByIdInNoParticularOrder)

  def playerIdentifiedBy(id: A): Either[A, Player[A]] = playersByIdInNoParticularOrder.get(id).toRight(id)

  def rankOf(id: A): Option[Rank] = idsByRank.indexWhere(_ contains id) match
    case n if n < 0 => None
    case other => Some(liftRank(other + 1))

  override def equals(any: Any): Boolean =
    any match
      case other: Leaderboard[?] => other.playersByIdInNoParticularOrder equals playersByIdInNoParticularOrder
      case _ => false

  override def hashCode: Int = playersByIdInNoParticularOrder.hashCode

  override def toString = s"Leaderboard(... ${playersByIdInNoParticularOrder.size} player(s) ...)"


object Leaderboard:
  def empty[A: Eq] = new Leaderboard[A](Map())
  def fromPlayers[A: Eq](players: IterableOnce[Player[A]]) = new Leaderboard[A](players.iterator.map(p => p.id -> p).toMap)