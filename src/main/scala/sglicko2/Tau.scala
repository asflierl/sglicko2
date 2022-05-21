package sglicko2

import scala.compiletime._

opaque type Tau = Double

// object Tau:
//   transparent inline def apply(d: Double): Tau | Valid[Tau] =
//     Opaque(d, liftTau(d), d > 0d && d < Double.PositiveInfinity, "System constant τ must be a number greater than 0.") 
    
//   val default: Tau = 0.6d

// private inline def liftTau(inline d: Double): Tau = d

// object Tau:
//   transparent inline def apply(d: Double): Tau | Valid[Tau] =
//     macros.opaqueCons(d, liftTau(d), d > 0d && d < Double.PositiveInfinity, "System constant τ must be a number greater than 0.") 
    
//   val default: Tau = 0.6d

// private inline def liftTau(inline d: Double): Tau = d

// object Tau:
//   inline def apply(d: Double): Valid[Tau] = 
//     Either.cond(d > 0d && d < Double.PositiveInfinity, d, Err("System constant τ must be a number greater than 0."))

//   inline def apply[A <: Double & Singleton]: Tau =
//     val d = compiletime.constValue[A]
//     inline if d > 0d && d < Double.PositiveInfinity then d else compiletime.error("System constant τ must be a number greater than 0.")

//   val default: Tau = 0.6d

object Tau extends Opaque[Double, Tau]:
  protected transparent inline def cond(d: Double): Boolean = d > 0d && d < Double.PositiveInfinity
  protected transparent inline def msg: String = "System constant τ must be a number greater than 0."
  protected inline def lift(d: Double): Tau = d

  val default: Tau = 0.6d

extension (τ: Tau)
  def value: Double = τ
