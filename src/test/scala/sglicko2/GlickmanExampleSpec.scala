// SPDX-License-Identifier: ISC

package sglicko2

import org.specs2.*

class GlickmanExampleSpec extends Specification: 
  def is = s2"""
    The implementation of the Glicko2 algorithm should calculate sufficiently similar results as the example in [Mark Glickman's paper](http://www.glicko.net/glicko/glicko2.pdf). $ex1
  """

  import WinOrDraw.Ops.*

  given Glicko2 = Glicko2(Tau[0.5d])

  def ex1 =
    val initialBoard = Leaderboard.fromPlayers(Seq(
      Player("a", Rating(1500d), Deviation(200d)), 
      Player("b", Rating(1400d), Deviation(30d)), 
      Player("c", Rating(1550d), Deviation(100d)), 
      Player("d", Rating(1700d), Deviation(300d))))

    val updatedBoard = initialBoard.after(RatingPeriod(
      "a" winsVs "b",
      "c" winsVs "a",
      "d" winsVs "a"))

    val player = updatedBoard.playersByIdInNoParticularOrder("a")

    (player.rating should be_~(Rating(1464.06d) +/- Rating.fromGlicko2(0.01d))) and
    (player.deviation should be_~(Deviation(151.52d) +/- Deviation.fromGlicko2(0.01d))) and
    (player.volatility should be_~(Volatility(0.05999d) +/- Volatility(0.00001d)))
