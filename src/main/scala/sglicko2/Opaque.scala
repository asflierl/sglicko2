package sglicko2

private abstract class Opaque[A, B]:
  final inline def apply(a: A): Valid[B] = Either.cond(cond(a), lift(a), Err(msg))

  final inline def apply[C <: A & Singleton]: B =
    inline if cond(compiletime.constValue[C]) then lift(compiletime.constValue[C]) else compiletime.error(msg)

  protected inline def cond(a: A): Boolean
  protected transparent inline def msg: String
  protected inline def lift(a: A): B

// private object Opaque:
//   transparent inline def apply[A, B](inline a: A, inline b: B, inline p: Boolean, inline msg: String): B | Valid[B] =
//     inline compiletime.constValueOpt[a.type] match
//       case _: Some[A] => if p then b else compiletime.error(msg)
//       case None       => Either.cond(p, b, Err(msg))