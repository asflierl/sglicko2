package sglicko2

opaque type Deviation = Double
object Deviation extends OpaqueDouble[Deviation]:
  def fromGlickoValue(d: Double): Deviation = d / glicko2Scalar
  val default = fromGlickoValue(350d)
  extension (d: Deviation) def glickoValue: Double = d * glicko2Scalar


opaque type Rating = Double
object Rating extends OpaqueDouble[Rating]:
  def fromGlickoValue(r: Double): Rating = (r - 1500d) / glicko2Scalar
  val default = Rating(0d)
  extension (r: Rating) def glickoValue: Double = r * glicko2Scalar + 1500d


opaque type Tau = Double
object Tau extends Opaque[Double, Tau]:
  transparent inline def apply(d: Double): Tau | Valid[Tau] =
    macros.opaqueCons(d, liftTau(d), d > 0d && d < Double.PositiveInfinity, "System constant τ must be a number greater than 0.") 
    
  val default = Tau(0.6d)

private inline def liftTau(inline d: Double): Tau = d


opaque type Volatility = Double
object Volatility extends OpaqueDouble[Volatility]:
  val default = Volatility(0.06d)


private sealed abstract class OpaqueDouble[A >: Double] extends Opaque[Double, A]:
  inline def apply(r: Double): A = r


extension (inline v: Deviation | Rating | Tau | Volatility)
  inline def value: Double = v


private inline val glicko2Scalar = 173.7178d