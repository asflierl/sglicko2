// SPDX-License-Identifier: ISC

package sglicko2

type ScoringRulesC[A] = [b] =>> ScoringRules[A, b]

trait ScoringRules[A: Eq, B] extends Serializable:
  def gameScores(game: B): Iterable[(A, A, Score)]
