package example

import sglicko2.*

@main def run: Unit = 
  val n: 0.5d = 0.5d
  val x = Tau(n)

  val m = 0.5d
  val y = Tau(m)

  val z = Tau[0.5d]

  println(x)
  println(y)
  println(z)

class Meep:
  private val meep = Tau[0.5d]
