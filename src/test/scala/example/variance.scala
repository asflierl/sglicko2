// SPDX-License-Identifier: ISC

package example

import sglicko2.*, WinOrDraw.Ops.*

@main def variance: Unit =
  given Glicko2 = Glicko2()

  val l1 = Leaderboard.Empty // Leaderboard[Nothing]

  val (a1, a2) = (Apple("red"), Apple("green"))
  val (o1, o2) = (Orange(42), Orange(23))

  val r1 = RatingPeriod(a1 :>: a2) // RatingPeriod[Apple]
  val r2 = RatingPeriod(o1 :=: o2) // RatingPeriod[Orange]
  val r3 = RatingPeriod(a1 :=: o1, o2 :>: a2) // RatingPeriod[Fruit]
  val r4 = r1.withGames(o1 :=: o2)

  val l2a = l1 after r1 // Leaderboard[Apple]
  val l2b = l1 after r2 // Leaderboard[Orange]
  val l2c = l1 after r3 // Leaderboard[Fruit]

  val l3a = l2a after r2 // Leaderboard[Fruit]
  val l3b = l2b after r1 // Leaderboard[Fruit]
  val l3c = l2c after r3 // Leaderboard[Fruit]

  val l = Leaderboard.Empty
    .after(r1)
    .after(r2)
    .after(r3)

  println(l)

sealed trait Fruit derives Eq
case class Apple(color: String) extends Fruit
case class Orange(ripeness: Int) extends Fruit