// SPDX-License-Identifier: ISC

package example

import sglicko2.*

object Multiple {
  case class Outcome(winner: String, second: String, last: String)

  given ScoringRules[Outcome] with
    type P = String

    def gameScores(o: Outcome): Iterable[(P, P, Score)] = Seq(
      (o.winner, o.second, Score[1d]),
      (o.winner, o.last,   Score[1d]),
      (o.second, o.last,   Score[1d]))
}

object Detailed {
  import scala.math.{sin, Pi}

  case class Outcome(name1: String, points1: Long, name2: String, points2: Long)

  given ScoringRules[Outcome] with
    type P = String

    def gameScores(o: Outcome): Iterable[(P, P, Score)] = 
      Some(o.name1, o.name2, rateAVsB(o.points1.toDouble, o.points2.toDouble))

    def rateAVsB(a: Double, b: Double) = 
      Score((sin((a / (a + b) - 0.5d) * Pi) + 1d) * 0.5d)
}
