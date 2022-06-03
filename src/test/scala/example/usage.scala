// SPDX-License-Identifier: ISC

package example

import sglicko2.*, WinOrDraw.Ops.*

@main def run: Unit = 
  given Glicko2 = Glicko2()

  Leaderboard.empty
    .after(RatingPeriod(
      "Nilin"   :>: "Bob",
      "Bob"     :=: "Nilin"))
    .after(RatingPeriod(
      "Nilin"   :>: "Cookies",
      "Cookies" :>: "Bob",
      "Nilin"   :=: "Cookies"))
    .rankedPlayers
    .foreach(p => println(f"${p.rank}%2d ${p.player.id}%8s: [${p.player.confidence95.lower.value}%4.0f, ${p.player.confidence95.upper.value}%4.0f]"))
