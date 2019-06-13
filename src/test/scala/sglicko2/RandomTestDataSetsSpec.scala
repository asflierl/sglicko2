/*
 * Copyright (c) 2015, Andreas Flierl <andreas@flierl.eu>
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

import org.scalacheck.Gen._
import org.scalacheck.Prop.forAll
import org.scalacheck._
import org.specs2._
import org.specs2.matcher.MatchResult
import sglicko2.EitherOnePlayerWinsOrItsADraw._
import sglicko2.{EitherOnePlayerWinsOrItsADraw => S}

class RandomTestDataSetsSpec extends Specification with ScalaCheck { def is =
s2"""
  The number of players on the leaderboard is less than or equal to the number of player IDs. $ex1
  The ranking list of a leaderboard contains no duplicates. $ex2
  Adding games to a rating period via repeating 'withGame' is equivalent to using 'withGames' once with the same but aggregated input data. $ex3
  Adding games to a rating period via repeating 'withGames' is equivalent to using 'withGames' once with the same but aggregated input data. $ex4
"""

  import Generators._

  def ex1 = leaderboardProperty { case (config, leaderboard) =>
    leaderboard.playersByIdInNoParticularOrder.size should beLessThanOrEqualTo(config.ids.size)
  }

  def ex2 = leaderboardProperty { case (config, leaderboard) =>
    leaderboard.playersInRankOrder.distinct should_=== leaderboard.playersInRankOrder
  }

  def ex3 = forAll(listOfN(1000, genGame(newIDs(1000)))) { g =>
    val glicko2 = new G
    val empty = glicko2.newRatingPeriod

    val method1 = empty.withGames(g:_*)
    val method2 = g.foldLeft(empty)((akku, el) => akku.withGame(el _1, el _2, el _3))

    method1.games.view.mapValues(_ toSet).toMap should_=== method2.games.view.mapValues(_ toSet).toMap
  }

  def ex4 = forAll(listOfN(1000, genGame(newIDs(1000)))) { g =>
    val glicko2 = new G
    val empty = glicko2.newRatingPeriod

    val method1 = empty.withGames(g:_*)
    val method2 = g.sliding(100, 100).toSeq.foldLeft(empty)((akku, el) => akku.withGames(el:_*))

    method1.games.view.mapValues(_ toSet).toMap should_=== method2.games.view.mapValues(_ toSet).toMap
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

  def newIDs(numberOfIDs: Int): Vector[ID] = (1 to numberOfIDs).view.map(n => ID(s"player #$n")).toVector

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
