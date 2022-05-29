// SPDX-License-Identifier: ISC

package sglicko2.benchmark

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files.readAllBytes
import java.nio.file.{Path, Paths}

import buildinfo.BuildInfo.crossTarget
import io.circe.*
import io.circe.generic.auto.*
import io.circe.parser.*
import io.circe.syntax.*

import scala.collection.Iterator.continually
import scala.util.chaining.*

object EvaluateBenchmarkResults:

  def main(args: Array[String]): Unit =
    val referenceGroups = groups(results(readResource("/v1.7.1-graalvm-ce-21.1-java11/results.json")))
    val referenceBaseline = referenceGroups("sglicko2.benchmark.BaselineBenchmark").results("sglicko2.benchmark.BaselineBenchmark.baseline")
    val referenceResults = referenceGroups - "sglicko2.benchmark.BaselineBenchmark"
    val newGroups = groups(results(readPath(crossTarget.toPath.resolve("results.json"))))
    val newBaseline = newGroups("sglicko2.benchmark.BaselineBenchmark").results("sglicko2.benchmark.BaselineBenchmark.baseline")
    val newResults = newGroups - "sglicko2.benchmark.BaselineBenchmark"

    println("Benchmark results:")
    newResults.valuesIterator.toVector.sortBy(_.name).foreach { group =>
      println(s" * ${group.name}")
      group.results.valuesIterator.toVector.sortBy(_.benchmark).foreach { result =>
        val normalizedScore = result.primaryMetric.score / newBaseline.primaryMetric.score
        val analysis = referenceResults.get(group.name).flatMap { referenceGroup =>
          referenceGroup.results.get(result.benchmark).map { reference =>
            val cf = reference.confidence / referenceBaseline.confidence
            if cf `contains` normalizedScore then "OK"
            else s"outside reference confidence interval $cf"
          }
        }.getOrElse("new benchmark")

        println(f"   + ${result.benchmark}%-40s: $normalizedScore%-10f ${analysis.toString}%s")
      }
    }

  def groups(rs: Vector[BenchmarkResult]): Map[String, BenchmarkGroup] =
    rs.groupBy(_.groupName).map { case (k, v) =>
      k -> BenchmarkGroup(k, v.map(r => r.benchmark -> r).toMap)
    }

  def results(s: String): Vector[BenchmarkResult] = decode[Vector[BenchmarkResult]](s).toTry.get

  def readResource(r: String): String =
    val stream = getClass.getResourceAsStream(r)
    try utf8String(continually(stream.read).takeWhile(-1 != _).map(_.toByte).toArray)
    finally stream.close

  def readPath(p: Path): String = utf8String(readAllBytes(p))

  def utf8String(a: Array[Byte]): String = new String(a, UTF_8)

final case class BenchmarkGroup(name: String, results: Map[String, BenchmarkResult])

final case class BenchmarkResult(benchmark: String, primaryMetric: Metric, params: Option[Map[String, String]]):
  def name = benchmark

  val groupName =
    val baseName = benchmark.substring(0, benchmark.lastIndexOf('.'))
    val paramsPostfix = params.fold("")(_.toVector.sortBy(_._1).map { case (k, v) => f"($k%s:$v%6s)" }.mkString(" ", " ", ""))
    s"$baseName$paramsPostfix"

  val confidence = Interval(primaryMetric.scoreConfidence(0), primaryMetric.scoreConfidence(1)).tap { i =>
    require(i.a > 0 && i.b > 0, s"invalid benchmark result `$groupName`: confidence interval $i must be positive")
  }

final case class Metric(score: Double, scoreConfidence: Vector[Double])

final case class Interval(a: Double, b: Double):
  import math.*

  def reciprocal: Interval = Interval(1d / b, 1d / a)
  def / (y: Interval): Interval = *(y.reciprocal)
  def * (y: Interval): Interval = Interval(min(min(min((a * y.a), (a * y.b)), (b * y.a)), (b * y.b)), max(max(max((a * y.a), (a * y.b)), (b * y.a)), (b * y.b)))
  def contains(c: Double): Boolean = a <= c && c <= b
  override def toString: String = f"[$a%.3f,$b%.3f]"
