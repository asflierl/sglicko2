// SPDX-License-Identifier: ISC

package sglicko2.benchmark

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations.*
import sglicko2.{WinOrDraw, Glicko2, Leaderboard, RatingPeriod}

import scala.compiletime.uninitialized

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@Warmup(iterations = 50, time = 50, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 11, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(1)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
class LeaderboardBenchmark:

  given Glicko2 = Glicko2()

  @Param(Array("10", "1000", "10000"))
  @volatile var numberOfGames: Int = uninitialized

  @Param(Array("5", "50", "5000"))
  @volatile var numberOfPlayers: Int = uninitialized

  @volatile var ratingPeriod: RatingPeriod[String, WinOrDraw] = uninitialized

  @volatile var prefilledLeaderboard: Leaderboard[String] = uninitialized

  @Setup
  def prepare: Unit =
    val generator = new Generator(numberOfPlayers)
    ratingPeriod = RatingPeriod(generator.gameStream.take(numberOfGames).toVector*)
    prefilledLeaderboard = Leaderboard.empty.after(RatingPeriod(generator.gameStream.take(numberOfGames).toVector*))

  @Benchmark
  def updateFreshLeaderboard: Leaderboard[String] = Leaderboard.empty.after(ratingPeriod)

  @Benchmark
  def updatePrefilledLeaderboard: Leaderboard[String] = prefilledLeaderboard.after(ratingPeriod)


