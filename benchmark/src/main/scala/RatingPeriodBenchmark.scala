// SPDX-License-Identifier: ISC

package sglicko2.benchmark

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations.*
import sglicko2.{WinOrDraw, Glicko2, RatingPeriod}

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@Warmup(iterations = 50, time = 50, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 11, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(1)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
class RatingPeriodBenchmark:

  var system: Glicko2[String, WinOrDraw] = ???//new Glicko2[String, EitherOnePlayerWinsOrItsADraw]

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
  def createRatingPeriod: RatingPeriod[String, WinOrDraw] = ??? //system.newRatingPeriod.withGames(games *)
