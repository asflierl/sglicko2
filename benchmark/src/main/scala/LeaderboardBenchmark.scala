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

package sglicko2.benchmark

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations._
import sglicko2.{EitherOnePlayerWinsOrItsADraw, Glicko2, Leaderboard, RatingPeriod}

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@Warmup(iterations = 10, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 5, time = 500, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(3)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
class LeaderboardBenchmark {

  var system: Glicko2[String, EitherOnePlayerWinsOrItsADraw] = new Glicko2[String, EitherOnePlayerWinsOrItsADraw]

  @Param(Array("10", "1000", "10000"))
  @volatile var numberOfGames: Int = _

  @Param(Array("5", "50", "5000"))
  @volatile var numberOfPlayers: Int = _

  @volatile var ratingPeriod: RatingPeriod[String, EitherOnePlayerWinsOrItsADraw] = _

  @volatile var prefilledLeaderboard: Leaderboard[String] = _

  @Setup
  def prepare: Unit = {
    val generator = new Generator(numberOfPlayers)
    ratingPeriod = system.newRatingPeriod.withGames(generator.gameStream.take(numberOfGames).toVector:_*)
    prefilledLeaderboard = system.updatedLeaderboard(system.newLeaderboard, system.newRatingPeriod.withGames(generator.gameStream.take(numberOfGames).toVector:_*))
  }

  @Benchmark
  def baseline: Vector[Int] = (1 to (numberOfGames + numberOfPlayers)).toVector.zipWithIndex.map { case (x, y) => x + y }

  @Benchmark
  def updateFreshLeaderboard: Leaderboard[String] = system.updatedLeaderboard(system.newLeaderboard, ratingPeriod)

  @Benchmark
  def updatePrefilledLeaderboard: Leaderboard[String] = system.updatedLeaderboard(prefilledLeaderboard, ratingPeriod)
}


