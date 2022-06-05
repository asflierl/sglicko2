// SPDX-License-Identifier: ISC

package example

import sglicko2.*, WinOrDraw.Ops.*

@main def run: Unit = 
  given Glicko2 = Glicko2()

  Leaderboard.empty[String]
    .after(RatingPeriod(
      "Abby"  winsVs   "Becky",
      "Abby"  winsVs   "Chas",
      "Abby"  winsVs   "Dave",
      "Chas"  winsVs   "Becky",
      "Becky" tiesWith "Dave",
      "Dave"  winsVs   "Chas"))
    .rankedPlayers
    .foreach(p => println(
      f"${p.rank}%2d ${p.player.id}%5s: " +
      f"[${p.player.confidence95.lower.value}%4.0f, " +
      f"${p.player.confidence95.upper.value}%4.0f]"))
      