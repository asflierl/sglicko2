// SPDX-License-Identifier: ISC

package sglicko2

final case class ScoreVsPlayer[+P] private[sglicko2] (opponentID: P, score: Score)
