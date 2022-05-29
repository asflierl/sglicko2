// SPDX-License-Identifier: ISC

package sglicko2

opaque type Rating = Double

object Rating extends Opaque[Double, Rating]:
  def apply(r: Double)(using scale: Scale): Rating = scale match
    case Scale.Glicko  => (r - 1500d) / Scale.glicko2Scalar
    case Scale.Glicko2 => r

  private[sglicko2] inline def fromGlicko2Value(r: Double): Rating = r
  private[sglicko2] inline def toGlicko2(r: Rating): Double = r

  val default: Rating = 0d

  extension (r: Rating)
     def value(using  scale: Scale): Double = scale match
      case Scale.Glicko  => (r * Scale.glicko2Scalar) + 1500d
      case Scale.Glicko2 => r
