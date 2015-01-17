/*
 * Copyright (c) 2015, Andreas Flierl
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package sglicko2

import scala.math.{sin, Pi => π}
import scala.io.Codec.UTF8
import scala.io.Source
import resource.managed

case class WorldID(name: String)

case class Outcome(green: Participant, blue: Participant, red: Participant)

case class Participant(id: WorldID, points: Long)

case class Pairing(pointsOfPlayer1: Long, pointsOfPlayer2: Long)

object Pairing extends ((Long, Long) => Pairing) {
  implicit val rules: ScoringRules[Pairing] = new ScoringRules[Pairing] {
    val scoreForTwoPlayers = (pair: Pairing) => Score(rateAVersusB(pair pointsOfPlayer1, pair pointsOfPlayer2))
    def rateAVersusB(a: Double, b: Double): Double = (sin((a / (a + b) - 0.5d) * π) + 1d) * 0.5d
  }
}

object GW2ExampleResources {
  private val WorldExtractor = "\\d+\\s+([a-zA-Z ']+)(?: \\[(?:DE|FR|SP)\\])?\\s+([0-9.]+)\\s+([0-9.]+)\\s+([0-9.]+).+".r

  def leaderboardFromResource(name: String): Leaderboard[WorldID] =
    managed(Source.fromURL(getClass.getClassLoader.getResource(name))(UTF8)).acquireAndGet { file =>
      Leaderboard.fromPlayers(file.getLines.toStream.map {
        case WorldExtractor(n, r, d, v) => Player(WorldID(n trim), r toDouble, d toDouble, v toDouble)
      })
    }

  def outcomesFromResource(name: String): Vector[Outcome] =
    managed(Source.fromURL(getClass.getClassLoader.getResource(name))(UTF8)).acquireAndGet { file =>
      file.getLines.drop(3).toVector.sliding(6, 9).map { l =>
        Outcome(participant(l(0), l(3)), participant(l(1), l(4)), participant(l(2), l(5)))
      }.toVector
    }

  private def participant(name: String, score: String) = Participant(WorldID(name trim), score.replaceAll("\\s", "").toLong)
}

