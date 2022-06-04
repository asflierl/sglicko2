// SPDX-License-Identifier: ISC

package sglicko2.benchmark

import sglicko2.WinOrDraw
import sglicko2.WinOrDraw.*

import scala.util.hashing.MurmurHash3.mix

class Generator(numberOfPlayers: Int):
  lazy val players = names(numberOfPlayers)

  def endlessCombinations: Iterator[(String, String)] = players.combinations(2).map(v => (v(0), v(1))) ++ endlessCombinations

  val oneWins = Set(0, 3, 4)
  val twoWins = Set(1, 2, 6)

  lazy val gameStream: Iterator[WinOrDraw[String]] = endlessCombinations.zipWithIndex.map {
    case ((p1, p2), i) =>
      val d = i % 7
      if oneWins(d) then Win(p1, p2) else if twoWins(d) then Win(p2, p1) else Draw(p1, p2)
  }

  private def names(n: Int): Vector[String] = Vector.tabulate(n)(m => mix(0xabcdef, m).toHexString)
