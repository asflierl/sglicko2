package example

import sglicko2.*

@main def run: Unit = 
  val glicko2 = Glicko2[String, WinOrDraw]()
  import glicko2.*, WinOrDraw.*

  val result = for {
    initial <- Right(newLeaderboard)
    period1 <- newRatingPeriod.withGames(
                 Win("Nilin", "Bob"),
                 Draw("Bob", "Nilin"))
    board    = updatedLeaderboard(initial, period1)
  } yield board

  result.foreach(_.playersInRankOrder.map(_.toGlickoScale).foreach(println))
