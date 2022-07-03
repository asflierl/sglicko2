// SPDX-License-Identifier: ISC

package sglicko2

import scala.annotation.targetName

enum WinOrDraw[+P]:
  case Win(winner: P, loser: P)
  case Draw(player1: P, player2: P)

object WinOrDraw:
  given [P2: Eq]: ScoringRules[WinOrDraw[P2]] with
    type P = P2
    override def gameScores(g: WinOrDraw[P]): Iterable[(P, P, Score)] = Some(g match
      case Win(winner, loser)     => (winner,  loser,   Score[1d])
      case Draw(player1, player2) => (player1, player2, Score[0.5d]))

    override def toString = "Either one player wins or the game is a draw."

  object Ops:
    extension [P](a: P)
      @targetName("win") def :>:(b: P) = Win(a, b)
      infix def winsVs(b: P) = Win(a, b)
      
      @targetName("draw") def :=:(b: P) = Draw(a, b)
      infix def tiesWith(b: P) = Draw(a, b)
