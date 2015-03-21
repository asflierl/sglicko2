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

import org.scalameter.api._

import EitherOnePlayerWinsOrItsADraw._

class Benchmarking extends PerformanceTest.Quickbenchmark {
  val system = new Glicko2[String, EitherOnePlayerWinsOrItsADraw]
  import system._

  performance of "RatingPeriod" in {
    measure method "withGames" in {
      using(games) in { g =>
        newRatingPeriod.withGames(g:_*)
      }
    }
  }

  performance of "Glicko2" in {
    measure method "updatedRatingPeriod" in {
      using(leaderboardAndRatingPeriod) in { case (l, r) =>
        updatedLeaderboard(l, r)
      }
    }
  }

  lazy val numPlayers: Gen[Int] = Gen.enumeration("number of players")(100, 1000, 10000, 100000)
  lazy val numGamesPerPlayer = 35
  lazy val ratingPeriod: Gen[RatingPeriod[String, EitherOnePlayerWinsOrItsADraw]] = games.map(g => newRatingPeriod.withGames(g:_*)).cached

  lazy val leaderboardAndRatingPeriod: Gen[(Leaderboard[String], RatingPeriod[String, EitherOnePlayerWinsOrItsADraw])] =
    ratingPeriod.map(r => (updatedLeaderboard(newLeaderboard, r), r)).cached

  lazy val games: Gen[Vector[(String, String, EitherOnePlayerWinsOrItsADraw)]] =
    numPlayers.map(n =>
      (1 to n).map(id).sliding(numGamesPerPlayer, numGamesPerPlayer).flatMap(_ combinations 2).zipWithIndex.map { case (p, i) =>
        val (p1, p2) = (p(0), p(1))
        val o: EitherOnePlayerWinsOrItsADraw = if (i % 3 == 0) Player2Wins else Player1Wins
        (p1, p2, o)
      }.toVector).cached

  def id(n: Int) = s"player$n"
}
