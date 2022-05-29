// SPDX-License-Identifier: ISC

package sglicko2

trait ScoringRules[G[_]] extends Serializable:
  def gameScores[A](game: G[A]): Vector[(A, A, Score)]
