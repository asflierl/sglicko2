// SPDX-License-Identifier: ISC

package sglicko2.benchmark

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations.*
import sglicko2.{Glicko2, RatingPeriod, WinOrDraw}

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@Warmup(iterations = 250, time = 33, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 17, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(1)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
class RatingPeriodBenchmark:

  @Param(Array("10", "1000", "10000"))
  @volatile var numberOfGames: Int = _

  @Param(Array("5", "50", "5000"))
  @volatile var numberOfPlayers: Int = _

  @volatile var games: Seq[WinOrDraw[String]] = _

  @Setup
  def prepare: Unit =
    val generator = new Generator(numberOfPlayers)
    games = generator.gameStream.take(numberOfGames).toVector

  @Benchmark
  def createRatingPeriod: RatingPeriod[String] = RatingPeriod(games*)
