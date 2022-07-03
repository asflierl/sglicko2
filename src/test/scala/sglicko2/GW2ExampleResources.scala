// SPDX-License-Identifier: ISC

package sglicko2

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.file.Files.readAllLines
import java.nio.file.Paths.{get as path}

import scala.collection.immutable.ArraySeq
import scala.jdk.CollectionConverters.*
import scala.math.{sin, Pi as π}

object GW2ExampleResources:
  def leaderboardFromResource(name: String)(using Scale): Leaderboard[WorldID] =
    Leaderboard.fromPlayers(
      readAllLines(path(getClass.getClassLoader.getResource(name).toURI), UTF_8).asScala.iterator.map {
        case WorldExtractor(n, r, d, v) => Player(WorldID(n.trim), Rating(r.toDouble), Deviation(d.toDouble), Volatility(v.toDouble))
      })

  private val WorldExtractor = "\\d+\\s+([a-zA-Z ']+)(?: \\[(?:DE|FR|SP)\\])?\\s+([0-9.]+)\\s+([0-9.]+)\\s+([0-9.]+).+".r

  def outcomesFromResource(name: String): Vector[Outcome] =
    readAllLines(path(getClass.getClassLoader.getResource(name).toURI), UTF_8).asScala.iterator.drop(3).sliding(6, 9).map { l =>
      Outcome(participant(l(0), l(3)), participant(l(1), l(4)), participant(l(2), l(5)))
    }.toVector

  final case class WorldID(name: String) derives CanEqual

  final case class Outcome(green: Participant, blue: Participant, red: Participant)

  final case class Participant(id: WorldID, points: Long)

  private def participant(name: String, score: String) = Participant(WorldID(name.trim), score.replaceAll("\\s", "").toLong)

  given ScoringRules[Outcome] with
    type P = WorldID
    override def gameScores(o: Outcome): Iterable[(P, P, Score)] = 
      Array(pair(o.green, o.blue), pair(o.green, o.red), pair(o.blue, o.red))

    private def pair(a: Participant, b: Participant) = (a.id, b.id, rateAVsB(a.points.toDouble, b.points.toDouble))
    private def rateAVsB(a: Double, b: Double) = Score((sin((a / (a + b) - 0.5d) * π) + 1d) * 0.5d)
