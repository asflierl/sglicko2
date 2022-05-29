// SPDX-License-Identifier: ISC

package example

import sglicko2.*

@main def run: Unit = 
  given glicko2: Glicko2[String, WinOrDraw] = Glicko2()

  import glicko2.*, WinOrDraw.*

  newLeaderboard
    .after(newRatingPeriod.withGames(
      Win("Nilin", "Bob"),
      Draw("Bob", "Nilin")))
    .after(newRatingPeriod.withGames(
      Win("Nilin", "Cookies"),
      Win("Cookies", "Bob"),
      Draw("Nilin", "Cookies")))
    .rankedPlayers
    .foreach(p => println(f"${p.rank}%2d ${p.player.id}%8s: [${p.player.confidence95.lower.value}%4.0f, ${p.player.confidence95.upper.value}%4.0f]"))
