// SPDX-License-Identifier: ISC

package sglicko2.benchmark

import java.util.concurrent.TimeUnit

import org.openjdk.jmh.annotations.*
import org.openjdk.jmh.infra.Blackhole
import sglicko2.{WinOrDraw, Glicko2, RatingPeriod}

@State(Scope.Benchmark)
@BenchmarkMode(Array(Mode.AverageTime))
@Warmup(iterations = 1000, time = 10, timeUnit = TimeUnit.MILLISECONDS)
@Measurement(iterations = 50, time = 100, timeUnit = TimeUnit.MILLISECONDS)
@Fork(1)
@Threads(1)
@OutputTimeUnit(TimeUnit.MICROSECONDS)
class BaselineBenchmark:

  @Benchmark
  def baseline: Unit = Blackhole.consumeCPU(527)

