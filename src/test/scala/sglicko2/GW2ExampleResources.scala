/*
 * Copyright (c) 2021, Andreas Flierl <andreas@flierl.eu>
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

// package sglicko2

// import java.nio.charset.StandardCharsets.UTF_8
// import java.nio.file.Files.readAllLines
// import java.nio.file.Paths.{get => path}

// import scala.jdk.CollectionConverters._
// import scala.math.{sin, Pi => π}

// object GW2ExampleResources {
//   def leaderboardFromResource(name: String): Leaderboard[WorldID] =
//     Leaderboard.fromPlayers(
//       readAllLines(path(getClass.getClassLoader.getResource(name).toURI), UTF_8).asScala.iterator.map {
//         case WorldExtractor(n, r, d, v) => Player(WorldID(n trim), r toDouble, d toDouble, v toDouble)
//       })

//   private val WorldExtractor = "\\d+\\s+([a-zA-Z ']+)(?: \\[(?:DE|FR|SP)\\])?\\s+([0-9.]+)\\s+([0-9.]+)\\s+([0-9.]+).+".r

//   def outcomesFromResource(name: String): Vector[Outcome] =
//     readAllLines(path(getClass.getClassLoader.getResource(name).toURI), UTF_8).asScala.iterator.drop(3).sliding(6, 9).map { l =>
//       Outcome(participant(l(0), l(3)), participant(l(1), l(4)), participant(l(2), l(5)))
//     }.toVector

//   case class WorldID(name: String)

//   case class Outcome(green: Participant, blue: Participant, red: Participant)

//   case class Participant(id: WorldID, points: Long)

//   private def participant(name: String, score: String) = Participant(WorldID(name trim), score.replaceAll("\\s", "").toLong)

//   case class Pairing(pointsOfPlayer1: Long, pointsOfPlayer2: Long)

//   object Pairing extends ((Long, Long) => Pairing) {
//     implicit val rules: ScoringRules[Pairing] = new ScoringRules[Pairing] {
//       val scoreForTwoPlayers = (pair: Pairing) => Score(rateAVersusB(pair.pointsOfPlayer1.toDouble, pair.pointsOfPlayer2.toDouble))
//       def rateAVersusB(a: Double, b: Double): Double = (sin((a / (a + b) - 0.5d) * π) + 1d) * 0.5d
//     }
//   }
// }
