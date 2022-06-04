// SPDX-License-Identifier: ISC

package sglicko2

import org.specs2.*
import org.specs2.matcher.Matcher
import GW2ExampleResources.*

class GW2ExampleSpec extends Specification: 
  def is = s2"""
    This example uses "real world" data from the world vs world game mode of Guild Wars 2 to showcase the use of the
    pluggable scoring rules.

    Information how they use Glicko 2 exactly can be found from these sources:
      - https://forum-en.guildwars2.com/forum/game/wuv/The-math-behind-WvW-ratings/first#post679386
      - https://forum-en.guildwars2.com/forum/game/wuv/The-math-behind-WvW-ratings/first#post682392
      - https://www.guildwars2.com/en/news/big-changes-coming-to-wvw-matchups
      - https://www.guildwars2.com/en/news/wvw-matchup-variance-reduction
        
    An updated leaderboard after a rating period should be calculated sufficiently exactly…
      for rating period 1 $ex1
      and rating period 2. $ex2
  """

  def ex1 = theLeaderboardCalculatedFrom("leaderboard1.txt", "ratingPeriod1.txt") should beSufficientlySimilarTo("leaderboard2.txt")
  def ex2 = theLeaderboardCalculatedFrom("leaderboard2.txt", "ratingPeriod2.txt") should beSufficientlySimilarTo("leaderboard3.txt")

  given Glicko2 = Glicko2()
  
  def theLeaderboardCalculatedFrom(leaderboardName: String, ratingPeriodName: String): Leaderboard[WorldID] =
    leaderboardFromResource(leaderboardName).after(RatingPeriod(outcomesFromResource(ratingPeriodName)*))

  def beSufficientlySimilarTo(rsc: String): Matcher[Leaderboard[WorldID]] =
    ((_: Leaderboard[WorldID]).rankedPlayers) ^^
    contain(exactly[RankedPlayer[WorldID]](leaderboardFromResource(rsc).rankedPlayers.map(sufficientlySimilar)*).inOrder)

  def sufficientlySimilar(other: RankedPlayer[WorldID]): Matcher[RankedPlayer[WorldID]] =
    (===(other.rank) ^^ ((_: RankedPlayer[WorldID]).rank aka "rank")) and
    (===(other.player.id) ^^ ((_: RankedPlayer[WorldID]).player.id aka "ID")) and
    (closeTo(other.player.rating +/- Rating.fromGlicko2(0.1d)) ^^ ((_: RankedPlayer[WorldID]).player.rating aka "rating")) and
    (closeTo(other.player.deviation +/- Deviation.fromGlicko2(0.01d)) ^^ ((_: RankedPlayer[WorldID]).player.deviation aka "deviation")) and
    (closeTo(other.player.volatility +/- Volatility(0.0001)) ^^ ((_: RankedPlayer[WorldID]).player.volatility aka "volatility"))
