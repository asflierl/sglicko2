// SPDX-License-Identifier: ISC

package sglicko2

opaque type Tau = Double

object Tau extends Opaque[Double, Tau]:
  def apply(d: Double): Valid[Tau] = Either.cond(d > 0d && d < Double.PositiveInfinity, d, Err("System constant τ must be a number greater than 0."))

  inline def apply[A <: Double & Singleton]: Tau =
    inline val a = compiletime.constValue[A]
    inline if a > 0d && a < Double.PositiveInfinity then a else compiletime.error("System constant τ must be a number greater than 0.")

  val default = Tau[0.6d]

  given (using od: Ordering[Double]): Ordering[Tau] = od

  extension (inline t: Tau) inline def value: Double = t
