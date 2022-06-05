// SPDX-License-Identifier: ISC

package sglicko2

opaque type Rank = Int

object Rank extends Opaque[Int, Rank]:
  inline def apply(inline r: Int): Rank = r

  given Ordering[Rank] = summon[Ordering[Int]]

  extension (inline r: Rank) inline def value: Int = r