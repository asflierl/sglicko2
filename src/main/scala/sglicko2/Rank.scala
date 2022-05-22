package sglicko2

opaque type Rank = Int

object Rank extends Opaque[Int, Rank]:
  transparent inline def apply(r: Int): Rank | Valid[Rank] =
    macros.opaqueCons(r, liftRank(r), r > 0, "Rank must be a number greater than 0.") 

private inline def liftRank(r: Int): Rank = r
