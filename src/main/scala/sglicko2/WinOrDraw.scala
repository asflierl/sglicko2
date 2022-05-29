// SPDX-License-Identifier: ISC

package sglicko2

enum WinOrDraw[A]:
  case Win(winner: A, loser: A)
  case Draw(player1: A, player2: A)

object WinOrDraw:
  given ScoringRules[WinOrDraw] = new ScoringRules[WinOrDraw]:
    override def gameScores[A](g: WinOrDraw[A]): Vector[(A, A, Score)] = Vector(g match
      case Win(winner, loser)     => (winner,  loser,   Score[1d])
      case Draw(player1, player2) => (player1, player2, Score[0.5d]))

    override def toString = "Either one player wins or the game is a draw."
