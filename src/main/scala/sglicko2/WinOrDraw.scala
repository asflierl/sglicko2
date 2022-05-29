/*
 * Copyright (c) 2021, Andreas Flierl <andreas@flierl.eu>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

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
