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

// package sglicko2

// import org.specs2._

// class GlickmanExampleSpec extends Specification { def is =
// s2"""
// The implementation of the Glicko2 algorithm should calculate sufficiently similar results as the example in [Mark Glickman's paper](http://www.glicko.net/glicko/glicko2.pdf). $ex1
// """

//   lazy val glicko2 = new Glicko2[String, EitherOnePlayerWinsOrItsADraw](0.5d)
//   import glicko2._, EitherOnePlayerWinsOrItsADraw._

//   def ex1 = {
//     val initialBoard = Leaderboard.fromPlayers(Seq(
//       Player("a", 1500d, 200d), Player("b", 1400d, 30d), Player("c", 1550d, 100d), Player("d", 1700d, 300d)
//     ))

//     val updatedBoard = updatedLeaderboard(initialBoard,
//       newRatingPeriod.withGame("a", "b", Player1Wins)
//                      .withGame("a", "c", Player2Wins)
//                      .withGame("a", "d", Player2Wins))

//     val player = updatedBoard.playerIdentifiedBy("a").left.map(Player(_)).merge

//     (player.rating should be ~(1464.06d +/- 0.01d)) and
//     (player.deviation should be ~(151.52d +/- 0.01d)) and
//     (player.volatility should be ~(0.05999d +/- 0.00001d))
//   }
// }
