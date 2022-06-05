// SPDX-License-Identifier: ISC

package sglicko2

trait ScoringRules[A: Eq, B] extends Serializable:
  def gameScores(game: B): Iterable[(A, A, Score)]
