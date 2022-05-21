package sglicko2

opaque type Deviation = Rating

object Deviation:
  def apply(r: Rating): Deviation = r
  