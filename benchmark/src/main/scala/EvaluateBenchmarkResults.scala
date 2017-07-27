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

import java.nio.charset.StandardCharsets
import java.nio.file.Files.readAllBytes
import java.nio.file.{Path, Paths}

import io.circe.generic.auto._
import io.circe.parser._
import spire.implicits._
import spire.math._

object EvaluateBenchmarkResults {
  def main(args: Array[String]): Unit = {
    val referenceResults = groups(results(Paths.get(getClass.getResource("/results-v1.4.json").toURI)))
    val newResults = groups(results(Paths.get("target", "results.json")))

    println("Benchmark results:")
    newResults.valuesIterator.toVector.sortBy(_.name).foreach { group =>
      println(s" * ${group.name}")
      group.others.valuesIterator.toVector.sortBy(_.benchmark).foreach { result =>
        val normalizedScore = result.primaryMetric.score / group.baseline.primaryMetric.score
        val analysis = referenceResults.get(group.name).flatMap { referenceGroup =>
          referenceGroup.others.get(result.benchmark).map { reference =>
            val cf = reference.confidence / referenceGroup.baseline.confidence
            if (cf contains normalizedScore) "OK"
            else s"outside reference confidence interval $cf"
          }
        }.getOrElse("new benchmark")

        println(f"   + ${result.benchmark}%-40s: $normalizedScore%-10f ${analysis.toString}%s")
      }
    }
  }

  def results(p: Path): Vector[BenchmarkResult] = decode[Vector[BenchmarkResult]](new String(readAllBytes(p), StandardCharsets.UTF_8)).toTry.get

  def groups(rs: Vector[BenchmarkResult]): Map[String, BenchmarkGroup] =
    rs.groupBy(_.groupName).map { case (k, v) =>
      val (baselines, others) = v.partition(_.benchmark endsWith ".baseline")
      k -> BenchmarkGroup(k, baselines.head, others.map(r => r.benchmark -> r).toMap)
    }
}

case class BenchmarkGroup(name: String, baseline: BenchmarkResult, others: Map[String, BenchmarkResult])

case class BenchmarkResult(benchmark: String, primaryMetric: Metric, params: Option[Map[String, String]]) {
  def name = benchmark

  val groupName = {
    val baseName = benchmark.substring(0, benchmark.lastIndexOf('.'))
    val paramsPostfix = params.fold("")(_.toVector.sortBy(_._1).map { case (k, v) => f"($k%s:$v%6s)" }.mkString(" ", " ", ""))
    s"$baseName$paramsPostfix"
  }

  val confidence = Interval(primaryMetric.scoreConfidence._1, primaryMetric.scoreConfidence._2)
}
case class Metric(score: Double, scoreConfidence: (Double, Double))

