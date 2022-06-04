// SPDX-License-Identifier: ISC

package sglicko2

enum Scale derives Eq:
  case Glicko
  case Glicko2

object Scale:
  inline val glicko2Scalar = 173.7178d

  given (using g: Glicko2): Scale = g.scale