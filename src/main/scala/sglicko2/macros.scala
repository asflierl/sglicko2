// package sglicko2

// import scala.quoted.*

// private object macros:
//   transparent inline def opaqueCons[A <: Boolean | Byte | Short | Int | Long | Float | Double | Char | String, B](inline a: A, inline b: B, inline p: Boolean, inline msg: String): B | Valid[B] =
//     ${ opaqueConsImpl('a, 'b, 'p, 'msg) }

//   def opaqueConsImpl[A <: Boolean | Byte | Short | Int | Long | Float | Double | Char | String : Type, B : Type](a: Expr[A], b: Expr[B], p: Expr[Boolean], msg: Expr[String])(using q: Quotes): Expr[B] | Expr[Valid[B]] =
//     val isConst = a match
//       case '{ $c: Boolean } => c.value.isDefined
//       case '{ $c: Byte }    => c.value.isDefined
//       case '{ $c: Short }   => c.value.isDefined
//       case '{ $c: Int }     => c.value.isDefined
//       case '{ $c: Long }    => c.value.isDefined
//       case '{ $c: Float }   => c.value.isDefined
//       case '{ $c: Double }  => c.value.isDefined
//       case '{ $c: Char }    => c.value.isDefined
//       case '{ $c: String }  => c.value.isDefined

//     if isConst then
//       if p.valueOrAbort then b
//       else q.reflect.report.errorAndAbort(msg.valueOrAbort)
//     else '{ Either.cond($p, $b, Err($msg)) }
