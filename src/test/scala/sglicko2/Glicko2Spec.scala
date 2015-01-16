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

import org.specs2._

class Glicko2Spec extends Specification { def is =
s2"""
The implementation of the Glicko2 algorithm should calculate sufficiently similar results as the example in Mark Glickman's paper. $ex1
"""

  lazy val system = new Glicko2(0.5d)
  import system._

  def ex1 = {
    val (a, b, c, d) = (Player(1500d, 200d), Player(1400d, 30d), Player(1550d, 100d), Player(1700d, 300d))

    val player = a.afterPlaying(Seq(Outcome(b, 1d), Outcome(c, 0d), Outcome(d, 0d)))

    (player.r should be ~(1464.06d +/- 0.01d)) and
    (player.rd should be ~(151.52d +/- 0.01d)) and
    (player.σ should be ~(0.05999d +/- 0.00001d))
  }
}
