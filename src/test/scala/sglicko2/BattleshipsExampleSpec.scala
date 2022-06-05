// SPDX-License-Identifier: ISC

package sglicko2

import org.specs2.*
import org.specs2.collection.IsEmpty
import org.specs2.matcher.Matcher

class BattleshipsExampleSpec extends Specification: 
  def is = s2"""
    This example is about a group of 5 players competing against each other in the [Battleships game](http://en.wikipedia.org/wiki/Battleship_(game)).

    For this example, the rating system is ${glicko2.toString}$p

    Initially, there are 4 players (Abby, Becky, Chas and Dave) and an empty leaderboard. $ex1

    During the first rating period, each player plays against every other player. Dave sadly called in sick and thus cannot participate (yet). Abby wins all her games and Becky wins against Chas.

    On the resulting leaderboard…
      Abby's rank is 1 and her rating is greater than Becky's. $ex2
      Becky's rank is 2 and her rating is greater than Chas'. $ex3
      Chas' rank is 3. $ex4
      Only these 3 players are ranked. Dave is not yet on that leaderboard because he has not yet played any games. $ex5

    In the second rating period, again each player plays against every other player but now Dave has recovered and joins in. Since he's had lots of rest, he wins all of the games. Abby is distracted by her cat and has a losing streak. Becky and Chas' game ends in a draw.

    On the resulting leaderboard…
      There are now 4 ranked players. $ex6
      Abby is now at the lowest rank, she lost the most games. $ex7
      Becky's rank is now 2, she won 1 more game than Chas. $ex8
      Chas' rank is now 3. $ex9
      Dave made an impressive debut, resulting in the highest rank. $ex10
      Dave's deviation (quarter 95% confidence interval, i.e. uncertainty) is the greatest, because he only played 3 games, while the others already played 5. $ex11
      
    In the grand finale, a new player, Emma, joins the group. Again, every player plays against every other player. Abby makes a fantastic return as the undisputed queen of Battleships and wins all her games. Becky is on vacation and misses the final rating period completely. Chas and Emma perform equally. Dave loses against Emma.

    On the final leaderboard…
      There are now 5 ranked players. $ex12
      Abby is back at rank 1. $ex13
      Becky rank is still 2, her rating and volatility remain unchanged while her deviation increased because she did not compete during this rating period. $ex14
      Chas ends up at rank 4. $ex15
      Dave starts planning his revenge as he ends up on the last rank. $ex16
      Emma manages to claim rank 3 for herself but her deviation is by far the highest since she only played a few games. $ex17
  """

  import WinOrDraw.Ops.*

  given glicko2: Glicko2 = Glicko2()

  lazy val namesOfInitialPlayers = List("Abby", "Becky", "Chas", "Dave")
  lazy val initialLeaderboard = Leaderboard.empty[String]

  def ex1 =
    (initialLeaderboard.playersByIdInNoParticularOrder should beEmpty) and
    (initialLeaderboard.playersInRankOrder should beEmpty) and
    (initialLeaderboard.rankedPlayers should beEmpty) and
    (foreach(namesOfInitialPlayers)(name => initialLeaderboard.rankOf(name) should beNone)) and
    (foreach(namesOfInitialPlayers)(name => initialLeaderboard.playerIdentifiedBy(name) should beNone))

  lazy val ratingPeriod1 = RatingPeriod(
    "Abby"  winsVs "Becky",
    "Abby"  winsVs "Chas",
    "Becky" winsVs "Chas")

  lazy val leaderboard1 = initialLeaderboard.after(ratingPeriod1)

  def ex2 = (leaderboard1.rankOf("Abby") should beSome(Rank(1))) and ("Abby" hasAGreaterRatingThan "Becky" on leaderboard1)
  def ex3 = (leaderboard1.rankOf("Becky") should beSome(Rank(2))) and ("Becky" hasAGreaterRatingThan "Chas" on leaderboard1)
  def ex4 = leaderboard1.rankOf("Chas") should beSome(Rank(3))

  def ex5 =
    (leaderboard1 should knowOnlyAbout("Abby", "Chas", "Becky")) and
    (leaderboard1.rankOf("Dave") should beNone) and
    (leaderboard1.playerIdentifiedBy("Dave") should beNone)

  lazy val ratingPeriod2 = RatingPeriod(
    "Becky" winsVs   "Abby",
    "Chas"  winsVs   "Abby",
    "Dave"  winsVs   "Abby",
    "Becky" tiesWith "Chas",
    "Dave"  winsVs   "Becky",
    "Dave"  winsVs   "Chas")

  lazy val leaderboard2 = leaderboard1.after(ratingPeriod2)

  def ex6 = leaderboard2 should knowOnlyAbout("Abby", "Chas", "Becky", "Dave")

  def ex7 = leaderboard2.rankOf("Abby") should beSome(Rank(4))
  def ex8 = leaderboard2.rankOf("Becky") should beSome(Rank(2))
  def ex9 = leaderboard2.rankOf("Chas") should beSome(Rank(3))
  def ex10 = leaderboard2.rankOf("Dave") should beSome(Rank(1))

  def ex11 = {
    def deviationOf(name: String) = player(name, leaderboard2).deviation

    foreach(Seq("Abby", "Becky", "Chas")) { name =>
      deviationOf(name) aka s"deviation of $name" should beLessThan(deviationOf("Dave"))
    }
  }

  lazy val ratingPeriod3 = RatingPeriod(
    "Abby" winsVs   "Chas",
    "Abby" winsVs   "Dave",
    "Abby" winsVs   "Emma",
    "Chas" winsVs   "Dave",
    "Chas" tiesWith "Emma",
    "Emma" winsVs   "Dave")

  lazy val leaderboard3 = leaderboard2.after(ratingPeriod3)

  def ex12 = leaderboard3 should knowOnlyAbout("Abby", "Chas", "Becky", "Dave", "Emma")

  def ex13 = leaderboard3.rankOf("Abby") should beSome(Rank(1))

  def ex14 = {
    val previous = player("Becky", leaderboard2)
    val current = player("Becky", leaderboard3)

    (leaderboard3.rankOf("Becky") aka "rank" should beSome(Rank(2))) and
    (current.rating aka "rating" should beEqualTo(previous.rating)) and
    (current.volatility aka "volatility" should beEqualTo(previous.volatility)) and
    (current.deviation aka "deviation" should beGreaterThan(previous.deviation))
  }

  def ex15 = leaderboard3.rankOf("Chas") should beSome(Rank(4))

  def ex16 = leaderboard3.rankOf("Dave") should beSome(Rank(5))

  def ex17 = {
    def deviationOf(name: String) = player(name, leaderboard3).deviation

    (leaderboard3.rankOf("Emma") should beSome(Rank(3))) and
    (foreach(Seq("Abby", "Becky", "Chas", "Dave")) { name =>
      deviationOf(name) aka s"deviation of $name" should beLessThan(deviationOf("Emma"))
    })
  }

  def knowOnlyAbout(ps: String*): Matcher[Leaderboard[String]] =
    (contain(exactly[String](ps*)) ^^ ((_: Leaderboard[String]).playersByIdInNoParticularOrder.keySet aka "known player names")) and
    (ps map knowAbout reduceLeft (_ and _)) and
    (haveSize[Vector[Player[String]]](ps.size) ^^ ((_: Leaderboard[String]).playersInRankOrder aka "players in rank order")) and
    (haveSize[Vector[RankedPlayer[String]]](ps.size) ^^ ((_: Leaderboard[String]).rankedPlayers aka "ranked players"))

  def knowAbout(name: String): Matcher[Leaderboard[String]] = beSome[Player[String]] ^^ ((_: Leaderboard[String]).playerIdentifiedBy(name) aka s"the result of looking for a player named '$name' on the leaderboard")

  extension (a: String)
    def hasAGreaterRatingThan(b: String) = new scala.reflect.Selectable:
      def on(l: Leaderboard[String]) = player(a, l).rating aka "rating" should beGreaterThan(player(b, l).rating)
    
  def player(name: String, l: Leaderboard[String]) = l.playerIdentifiedBy(name).getOrElse(throw new NoSuchElementException(s"player '$name' not on the leaderboard"))

  given [A, B]: IsEmpty[Map[A, B]] with
    def isEmpty(m: Map[A, B]) = m.isEmpty
