/*
 * Copyright (c) 2015, Andreas Flierl
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 *  Redistributions of source code must retain the above copyright notice, this
 *   list of conditions and the following disclaimer.
 *
 *  Redistributions in binary form must reproduce the above copyright notice,
 *   this list of conditions and the following disclaimer in the documentation
 *   and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS"
 * AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE
 * IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE
 * FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL
 * DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER
 * CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
 * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE
 * OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

package sglicko2

import scala.math.{abs, exp, sqrt, log => ln}

// implements the (public domain) Glicko 2 algorithm; see http://www.glicko.net/glicko.html for further details
// Step 1
class Glicko2(val τ: Double = 0.6d) {
  private final val glicko2Scalar = 173.7178d
  private final val ε = 0.000001d

  private def g(Φ: Double): Double = 1d / sqrt(1d + 3d * Φ.`²` / π.`²`)
  private def E(µ: Double, µj: Double, Φj: Double): Double = 1d / (1d + exp(-g(Φj) * (µ - µj)))

  case class Player(r: Double = 1500d, rd: Double = 350d, σ: Double = 0.06d) {
    // Step 2
    val µ = (r - 1500d) / glicko2Scalar
    val Φ = rd / glicko2Scalar

    def afterPlaying(matches: Seq[Outcome]): Player = {
      // Step 3
      val v = 1d / (matches map { m =>
        def e = E(µ, m.opponent.µ, m.opponent.Φ)
        g(m.opponent.Φ).`²` * e * (1d - e)
      } sum)

      // Step 4
      val ∆ = v * (matches map { m =>
        g(m.opponent.Φ) * (m.score - E(µ, m.opponent.µ, m.opponent.Φ))
      } sum)

      // Step 5
      val a = ln(σ.`²`)

      def f(x: Double) = ((exp(x) * (∆.`²` - Φ.`²` - v - exp(x))) / (2d * (Φ.`²` + v + exp(x)).`²`)) - ((x - a) / τ.`²`)

      var A = a
      var k = 1d
      var B =
        if (∆.`²` > (Φ.`²` + v)) ln(∆.`²` - Φ.`²` - v)
        else {
          while (f(a - k * τ) < 0d) k += 1d
          a - k * τ
        }

      var fA = f(A)
      var fB = f(B)

      while (abs(B - A) > ε) {
        val C = A + (A - B) * fA / (fB - fA)
        val fC = f(C)

        if (fC * fB < 0) {
          A = B
          fA = fB
        } else fA /= 2d

        B = C
        fB = fC
      }

      val `σ'` = exp(A / 2d)

      // Step 6
      val `Φ*` = sqrt(Φ.`²` + `σ'`.`²`)

      // Step 7
      val `Φ'` = 1d / sqrt((1d / `Φ*`.`²`) + (1d / v))
      val `µ'` = µ + `Φ'`.`²` * (matches map { m =>
        g(m.opponent.Φ) * (m.score - E(µ, m.opponent.µ, m.opponent.Φ))
      } sum)

      // Step 8
      Player(`µ'` * glicko2Scalar + 1500d, `Φ'` * glicko2Scalar, `σ'`)
    }
  }

  case class Outcome(opponent: Player, score: Double)
}
