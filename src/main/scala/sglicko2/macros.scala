package sglicko2

import scala.quoted.*

private object macros:
  transparent inline def opaqueCons[A <: Int | Double, B](inline in: A, inline out: B, inline isValid: Boolean, inline errorMessage: String): B | Valid[B] =
    ${ opaqueConsImpl('in, 'out, 'isValid, 'errorMessage) }

  def opaqueConsImpl[A <: Int | Double : Type, B : Type](in: Expr[A], out: Expr[B], isValid: Expr[Boolean], errorMessage: Expr[String])(using q: Quotes): Expr[B] | Expr[Valid[B]] =
    val isConst = in match
      case '{ $c: Int }     => c.value.isDefined
      case '{ $c: Double }  => c.value.isDefined

    if isConst then
      if isValid.valueOrAbort then out
      else q.reflect.report.errorAndAbort(errorMessage.valueOrAbort)
    else '{ Either.cond($isValid, $out, Err($errorMessage)) }
