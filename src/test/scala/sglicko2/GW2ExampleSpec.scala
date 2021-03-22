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

import org.specs2._
import org.specs2.matcher.Matcher
import GW2ExampleResources._

class GW2ExampleSpec extends Specification { def is =
s2"""
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

  lazy val glicko2 = new Glicko2[WorldID, Pairing]
  import glicko2._
  
  def theLeaderboardCalculatedFrom(leaderboardName: String, ratingPeriodName: String): Leaderboard[WorldID] = {
    val currentLeaderboard = leaderboardFromResource(leaderboardName)
    val outcomes = outcomesFromResource(ratingPeriodName)
    val games = outcomes.flatMap { outcome =>
      Vector(
        (outcome.green.id, outcome.blue.id, Pairing(outcome.green.points, outcome.blue.points)),
        (outcome.green.id, outcome.red.id, Pairing(outcome.green.points, outcome.red.points)),
        (outcome.blue.id, outcome.red.id, Pairing(outcome.blue.points, outcome.red.points)))
    }

    updatedLeaderboard(currentLeaderboard, newRatingPeriod.withGames(games:_*))
  }

  def beSufficientlySimilarTo(rsc: String): Matcher[Leaderboard[WorldID]] =
    ((_: Leaderboard[WorldID]).rankedPlayers) ^^
    contain(exactly(leaderboardFromResource(rsc).rankedPlayers.map(sufficientlySimilar):_*).inOrder)

  def sufficientlySimilar(other: RankedPlayer[WorldID]): Matcher[RankedPlayer[WorldID]] =
    (typedEqualTo(other.rank) ^^ ((_: RankedPlayer[WorldID]).rank aka "rank")) and
    (typedEqualTo(other.player.id) ^^ ((_: RankedPlayer[WorldID]).player.id aka "ID")) and
    (closeTo(other.player.rating +/- 0.1) ^^ ((_: RankedPlayer[WorldID]).player.rating aka "rating")) and
    (closeTo(other.player.deviation +/- 0.01) ^^ ((_: RankedPlayer[WorldID]).player.deviation aka "deviation")) and
    (closeTo(other.player.volatility +/- 0.0001) ^^ ((_: RankedPlayer[WorldID]).player.volatility aka "volatility"))
}
