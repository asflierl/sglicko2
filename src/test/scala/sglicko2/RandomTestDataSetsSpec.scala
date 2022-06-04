// SPDX-License-Identifier: ISC

package sglicko2

import org.scalacheck.*
import org.scalacheck.Gen.*
import org.scalacheck.Prop.forAll
import org.specs2.*
import org.specs2.execute.Result
import sglicko2.WinOrDraw.Ops.*

import scala.collection.immutable.ArraySeq

class RandomTestDataSetsSpec extends Specification with ScalaCheck:
  def is = s2"""
    The number of players on the leaderboard is less than or equal to the number of player IDs. $ex1
    The ranking list of a leaderboard contains no duplicates. $ex2
    Adding games to a rating period via repeating 'withGames' is equivalent to using 'withGames' once with the same but aggregated input data. $ex3
  """

  import Generators.*

  def ex1 = leaderboardProperty { (config, leaderboard) =>
    leaderboard.playersByIdInNoParticularOrder.size should beLessThanOrEqualTo(config.ids.size)
  }

  def ex2 = leaderboardProperty { (config, leaderboard) =>
    leaderboard.playersInRankOrder.distinct should ===(leaderboard.playersInRankOrder)
  }

  def ex3 = forAll(containerOfN[ArraySeq, Game](1000, genGame(newIDs(1000)))) { g =>
    given Glicko2 = Glicko2()
    val empty = RatingPeriod.empty[ID, WinOrDraw[ID]]

    val method1 = empty.withGames(g*)
    val method2 = g.sliding(100, 100).toSeq.foldLeft(empty)((akku, el) => akku.withGames(el*))

    method1.games.view.mapValues(_.toSet).toMap should ===(method2.games.view.mapValues(_.toSet).toMap)
  }

  def leaderboardProperty[A](f: ((Config, Leaderboard[ID])) => Result) = prop(f)

object Generators:
  final case class ID(raw: String) derives Eq

  type Game = WinOrDraw[ID]

  final case class Config(glicko2: Glicko2, ids: Vector[ID], minNumberOfRatingPeriods: Int, maxNumberOfRatingPeriods: Int, minNumberOfGames: Int, maxNumberOfGames: Int)

  implicit lazy val arbLeaderboard: Arbitrary[(Config, Leaderboard[ID])] = Arbitrary {
    for
      config      <- genConfig
      leaderboard <- genLeaderboard(config)
    yield (config, leaderboard)
  }

  lazy val genConfig: Gen[Config] =
    for
      glicko2                  <- genGlicko2System
      numberOfIDs              <- choose(2, 100)
    yield Config(glicko2, newIDs(numberOfIDs), 0, 10, 0, numberOfIDs * 2)

  def genLeaderboard(config: Config): Gen[Leaderboard[ID]] =
    given Glicko2 = config.glicko2
    for
      numberOfRatingPeriods <- chooseNum(config.minNumberOfRatingPeriods, config.maxNumberOfRatingPeriods)
      ratingPeriods         <- listOfN(numberOfRatingPeriods, genRatingPeriod(config))
    yield ratingPeriods.foldLeft(Leaderboard.empty[ID])((board, period) => board after period)

  given Gen.Choose[Tau] with
    def choose(min: Tau, max: Tau): Gen[Tau] = Gen.choose(min.value, max.value) map (t => Tau(t).getOrElse(???))

  lazy val genGlicko2System: Gen[Glicko2] = choose(Tau[0.3d], Tau[1.2d]) map (Glicko2(_))

  def newIDs(numberOfIDs: Int): Vector[ID] = (1 to numberOfIDs).view.map(n => ID(s"player #$n")).toVector

  def genRatingPeriod(config: Config): Gen[RatingPeriod[ID, WinOrDraw[ID]]] =
    for
      numberOfGames <- chooseNum(config.minNumberOfRatingPeriods, config.maxNumberOfGames)
      games <- listOfN(numberOfGames, genGame(config.ids))
    yield RatingPeriod(games*)

  def genGame(ids: Vector[ID]): Gen[Game] =
    for
      index1  <- choose(0, ids.size - 1)
      id1      = ids(index1)
      index2  <- choose(1, ids.size - 1).map(n => (index1 + n) % ids.size)
      id2      = ids(index2)
      outcome <- oneOf(WinOrDraw.Win(id1, id2), WinOrDraw.Draw(id1, id2))
    yield outcome
