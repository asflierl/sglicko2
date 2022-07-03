// SPDX-License-Identifier: ISC

package sglicko2

trait ScoringRules[-G] extends Serializable:
  type P
  def gameScores(game: G): Iterable[(P, P, Score)]

object ScoringRules:
  type For[-P2, -G] = ScoringRules[G] { type P >: P2 }
