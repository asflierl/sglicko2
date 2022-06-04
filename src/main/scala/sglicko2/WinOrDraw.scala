// SPDX-License-Identifier: ISC

package sglicko2

import scala.annotation.targetName

enum WinOrDraw[A]:
  case Win(winner: A, loser: A)
  case Draw(player1: A, player2: A)

object WinOrDraw:
  given [A: Eq]: ScoringRules[A, WinOrDraw[A]] = new ScoringRules[A, WinOrDraw[A]]:
    override def gameScores(g: WinOrDraw[A]): Iterable[(A, A, Score)] = Some(g match
      case Win(winner, loser)     => (winner,  loser,   Score[1d])
      case Draw(player1, player2) => (player1, player2, Score[0.5d]))

    override def toString = "Either one player wins or the game is a draw."

  object Ops:
    extension [A](a: A)
      @targetName("win") def :>:(b: A) = Win(a, b)
      infix def winsVs(b: A) = Win(a, b)
      
      @targetName("draw") def :=:(b: A) = Draw(a, b)
      infix def tiesWith(b: A) = Draw(a, b)
