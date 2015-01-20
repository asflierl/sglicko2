/*
 * Copyright (c) 2015, Andreas Flierl
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package sglicko2

import scala.collection.breakOut

class Leaderboard[A] private (val playersByIdInNoParticularOrder: Map[A, Player[A]]) {
  lazy val idsByRank: Vector[Set[A]] = playersByIdInNoParticularOrder.values.groupBy(_ rating).toVector.sortBy(e => - e._1).map { case (_, ps) => ps.map(_ id)(breakOut): Set[A] }
  lazy val rankedPlayers: Vector[RankedPlayer[A]] = idsByRank.zipWithIndex.flatMap { case (ids, idx) => ids.map(id => RankedPlayer(idx + 1, playersByIdInNoParticularOrder(id))) }
  lazy val playersInRankOrder: Vector[Player[A]] = idsByRank.flatMap(_ map playersByIdInNoParticularOrder)

  def playerIdentifiedBy(id: A): Either[A, Player[A]] = playersByIdInNoParticularOrder.get(id).toRight(id)
  
  def rankOf(id: A): Option[Int] = idsByRank.indexWhere(_ contains id) match {
    case -1 => None
    case other => Some(other + 1)
  }

  override def equals(any: Any): Boolean = any match {
    case other: Leaderboard[_] => other.playersByIdInNoParticularOrder == playersByIdInNoParticularOrder
    case _ => false
  }

  override def hashCode: Int = playersByIdInNoParticularOrder##

  override def toString = s"Leaderboard(${rankedPlayers mkString ", "})"
}

object Leaderboard {
  def empty[A] = new Leaderboard[A](Map())
  def fromPlayers[A](players: Traversable[Player[A]]) = new Leaderboard[A](players.map(p => p.id -> p)(breakOut))
}
