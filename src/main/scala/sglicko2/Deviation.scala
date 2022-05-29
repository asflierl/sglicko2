// SPDX-License-Identifier: ISC

package sglicko2

opaque type Deviation = Double

object Deviation extends Opaque[Double, Deviation]:
  inline def apply(d: Double)(using inline scale: Scale): Deviation = scale match
    case Scale.Glicko  => d / Scale.glicko2Scalar
    case Scale.Glicko2 => d

  private[sglicko2] inline def fromGlicko2(inline d: Double): Deviation = d
  private[sglicko2] inline def toGlicko2(inline d: Deviation): Double = d

  val default: Deviation = 350d / Scale.glicko2Scalar
  
  extension (d: Deviation) def value(using scale: Scale): Double = scale match
    case Scale.Glicko  => d * Scale.glicko2Scalar
    case Scale.Glicko2 => d
