// SPDX-License-Identifier: ISC

package sglicko2

final case class ScoreVsPlayer[A] private[sglicko2] (opponentID: A, score: Score)
