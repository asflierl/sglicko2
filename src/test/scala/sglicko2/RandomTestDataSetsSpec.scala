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

import org.specs2._
import org.scalacheck._, Gen._
import org.specs2.matcher.MatchResult

import sglicko2.{EitherOnePlayerWinsOrItsADraw => S}, S._

class RandomTestDataSetsSpec extends Specification with ScalaCheck { def is =
s2"""
  The number of players on the leaderboard is less than or equal to the number of player IDs. $ex1
  The ranking list of a leaderboard contains no duplicates. $ex2
  Rating 100000 players over 100 rating periods with 10000 games each should not blow up the heap or the stack (as configured for the forked test VM). $ex3
  Rating 100000 players over 10000 rating periods with 100 games each should not blow up the heap or the stack (as configured for the forked test VM). $ex4
"""

  import Generators._

  def ex1 = leaderboardProperty { case (config, leaderboard) =>
    leaderboard.playersByIdInNoParticularOrder.size should beLessThanOrEqualTo(config.ids.size)
  }

  def ex2 = leaderboardProperty { case (config, leaderboard) =>
    leaderboard.playersInRankOrder.distinct should beEqualTo(leaderboard.playersInRankOrder)
  }

  def ex3 = {
    genGlicko2System.flatMap(g => genLeaderboard(Config(g, newIDs(100000), 100, 100, 10000, 10000))).sample.get should not(throwAn[Error])
  }

  def ex4 = {
    genGlicko2System.flatMap(g => genLeaderboard(Config(g, newIDs(100000), 10000, 10000, 100, 100))).sample.get should not(throwAn[Error])
  }

  def leaderboardProperty[A](f: ((Config, Leaderboard[ID])) => MatchResult[A]) = prop(f)
}

object Generators {
  case class ID(raw: String)

  type G = Glicko2[ID, S]
  type Game = (ID, ID, S)

  case class Config(glicko2: G, ids: Vector[ID], minNumberOfRatingPeriods: Int, maxNumberOfRatingPeriods: Int, minNumberOfGames: Int, maxNumberOfGames: Int)

  implicit lazy val arbLeaderboard: Arbitrary[(Config, Leaderboard[ID])] = Arbitrary {
    for {
      config      <- genConfig
      leaderboard <- genLeaderboard(config)
    } yield (config, leaderboard)
  }

  lazy val genConfig: Gen[Config] =
    for {
      glicko2                  <- genGlicko2System
      numberOfIDs              <- choose(2, 100)
    } yield Config(glicko2, newIDs(numberOfIDs), 0, 10, 0, numberOfIDs * 2)

  def genLeaderboard(config: Config): Gen[Leaderboard[ID]] =
    for {
      numberOfRatingPeriods <- chooseNum(config.minNumberOfRatingPeriods, config.maxNumberOfRatingPeriods)
      ratingPeriods         <- listOfN(numberOfRatingPeriods, genRatingPeriod(config))
    } yield ratingPeriods.foldLeft(config.glicko2.newLeaderboard)((board, period) => config.glicko2.updatedLeaderboard(board, period))

  lazy val genGlicko2System: Gen[G] = choose(0.3d, 1.2d) map (new Glicko2(_))

  def newIDs(numberOfIDs: Int): Vector[ID] = (1 to numberOfIDs).map(n => ID(s"player #$n"))(breakOut)

  def genRatingPeriod(config: Config): Gen[RatingPeriod[ID, S]] =
    for {
      numberOfGames <- chooseNum(config.minNumberOfRatingPeriods, config.maxNumberOfGames)
      games <- listOfN(numberOfGames, genGame(config.ids))
    } yield config.glicko2.newRatingPeriod.withGames(games:_*)

  def genGame(ids: Vector[ID]): Gen[Game] =
    for {
      index1  <- choose(0, ids.size - 1)
      id1      = ids(index1)
      index2  <- choose(1, ids.size - 1).map(n => (index1 + n) % ids.size)
      id2      = ids(index2)
      outcome <- oneOf(Player1Wins, Player2Wins, Draw)
    } yield (id1, id2, outcome)

}
