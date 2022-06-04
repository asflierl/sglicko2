// SPDX-License-Identifier: ISC

package sglicko2

opaque type Score = Double

object Score extends Opaque[Double, Score]:
  def apply(d: Double): Score = if d < 0d then 0d else if d > 1d then 1d else d

  inline def apply[A <: Double & Singleton]: Score =
    inline val a = compiletime.constValue[A]
    inline if a >= 0d && a <= 1d then a else compiletime.error("Score must be a number between 0 and 1 (both inclusive).")

  given (using od: Ordering[Double]): Ordering[Score] = od

  extension (s: Score)
    def asSeenFromPlayer1: Score = s
    def asSeenFromPlayer2: Score = 1d - asSeenFromPlayer1
    def value: Double = s
