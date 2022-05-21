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

package sglicko2.benchmark

import sglicko2.WinOrDraw
import sglicko2.WinOrDraw.{Draw, Player1Wins, Player2Wins}

import scala.util.hashing.MurmurHash3.mix

class Generator(numberOfPlayers: Int):
  lazy val players = names(numberOfPlayers)

  def endlessCombinations: Iterator[(String, String)] = players.combinations(2).map(v => (v(0), v(1))) ++ endlessCombinations

  val oneWins = Set(0, 3, 4)
  val twoWins = Set(1, 2, 6)

  lazy val gameStream: Iterator[(String, String, WinOrDraw)] = endlessCombinations.zipWithIndex.map {
    case ((p1, p2), i) =>
      val d = i % 7
      (p1, p2, if oneWins(d) then Player1Wins else if twoWins(d) then Player2Wins else Draw)
  }

  private def names(n: Int): Vector[String] = Vector.tabulate(n)(m => mix(0xabcdef, m).toHexString)
