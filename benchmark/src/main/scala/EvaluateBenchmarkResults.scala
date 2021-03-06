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

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files.readAllBytes
import java.nio.file.{Path, Paths}

import org.json4s._
import org.json4s.native.Serialization
import org.json4s.native.Serialization.read
import spire.implicits._
import spire.math._

import scala.collection.Iterator.continually

object EvaluateBenchmarkResults {
  implicit val formats = Serialization.formats(NoTypeHints)

  def main(args: Array[String]): Unit = {
    val referenceGroups = groups(results(readResource("/v1.7/results.json")))
    val referenceBaseline = referenceGroups("sglicko2.benchmark.BaselineBenchmark").results("sglicko2.benchmark.BaselineBenchmark.baseline")
    val referenceResults = referenceGroups - "sglicko2.benchmark.BaselineBenchmark"
    val newGroups = groups(results(readPath(Paths.get("target", "results.json"))))
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
            if (cf contains normalizedScore) "OK"
            else s"outside reference confidence interval $cf"
          }
        }.getOrElse("new benchmark")

        println(f"   + ${result.benchmark}%-40s: $normalizedScore%-10f ${analysis.toString}%s")
      }
    }
  }

  def groups(rs: Vector[BenchmarkResult]): Map[String, BenchmarkGroup] =
    rs.groupBy(_.groupName).map { case (k, v) =>
      k -> BenchmarkGroup(k, v.map(r => r.benchmark -> r).toMap)
    }

  def results(s: String): Vector[BenchmarkResult] = read[Vector[BenchmarkResult]](s)

  def readResource(r: String): String = {
    val stream = getClass.getResourceAsStream(r)
    try utf8String(continually(stream.read).takeWhile(-1 !=).map(_.toByte).toArray)
    finally stream.close
  }

  def readPath(p: Path): String = utf8String(readAllBytes(p))

  def utf8String(a: Array[Byte]): String = new String(a, UTF_8)
}

case class BenchmarkGroup(name: String, results: Map[String, BenchmarkResult])

case class BenchmarkResult(benchmark: String, primaryMetric: Metric, params: Option[Map[String, String]]) {
  def name = benchmark

  val groupName = {
    val baseName = benchmark.substring(0, benchmark.lastIndexOf('.'))
    val paramsPostfix = params.fold("")(_.toVector.sortBy(_._1).map { case (k, v) => f"($k%s:$v%6s)" }.mkString(" ", " ", ""))
    s"$baseName$paramsPostfix"
  }

  val confidence = Interval(primaryMetric.scoreConfidence(0), primaryMetric.scoreConfidence(1))
}
case class Metric(score: Double, scoreConfidence: Vector[Double])
