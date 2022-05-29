// SPDX-License-Identifier: ISC

package sglicko2

opaque type Volatility = Double

object Volatility extends Opaque[Double, Volatility]:
  inline def apply(v: Double): Volatility = v

  val default = Volatility(0.06d)

  extension (inline v: Volatility) inline def value: Double = v