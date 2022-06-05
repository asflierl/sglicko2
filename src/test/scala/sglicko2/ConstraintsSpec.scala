// SPDX-License-Identifier: ISC

package sglicko2

import org.specs2.Specification
import org.specs2.execute.*, Typecheck.*
import org.specs2.matcher.TypecheckMatchers.*
import org.specs2.matcher.ThrownExpectations

class ConstraintsSpec extends Specification with ThrownExpectations:
  def is = s2"""
    ${typecheck("Tau[-1d]") should not(succeed)}
    ${typecheck("Tau[0d]") should failWith("moo")}
    ${typecheck("Tau[1]") should failWith("does not conform to upper bound Double & Singleton")}
    ${typecheck("Tau[0.05d]") should succeed}
    ${typecheck("Tau[Double.NaN.type]") should not(succeed)}
    ${typecheck("Tau[Double.NegativeInfinity.type]") should not(succeed)}
    ${typecheck("Tau[Double.PositiveInfinity.type]") should not(succeed)}

    ${Tau(-1d) should beLeft}
    ${Tau(0d) should beLeft}
    ${Tau(1) should beRight(Tau[1d])}
    ${Tau(0.05d) should beRight(Tau[0.05d])}
    ${Tau(Double.NaN) should beLeft}
    ${Tau(Double.PositiveInfinity) should beLeft}
    ${Tau(Double.NegativeInfinity) should beLeft}

    ${typecheck("Score[-4.9E-324]") should not(succeed)}
    ${typecheck("Score[1.00000000000001d]") should not(succeed)}
    ${typecheck("Score[0]") should not(succeed)}
    ${typecheck("Score[1]") should not(succeed)}
    ${typecheck("Score[0d]") should succeed}
    ${typecheck("Score[1d]") should succeed}
    ${typecheck("Score[Double.NaN.type]") should not(succeed)}
    ${typecheck("Score[Double.NegativeInfinity.type]") should not(succeed)}
    ${typecheck("Score[Double.PositiveInfinity.type]") should not(succeed)}

    ${Score(-4.9E-324) should ===(Score[0d])}
    ${Score(1.00000000000001d) should ===(Score[1d])}
    ${Score(0) should ===(Score[0d])}
    ${Score(1) should ===(Score[1d])}
    ${Score(Double.NaN).value.isNaN should beTrue}
    ${Score(Double.NegativeInfinity) should ===(Score[0d])}
    ${Score(Double.PositiveInfinity) should ===(Score[1d])}
  """
