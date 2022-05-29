// SPDX-License-Identifier: ISC

package sglicko2

opaque type Err = String

object Err:
  def apply(message: String): Err = message

  given Eq[Err] = CanEqual.derived

extension (e: Err)
  def message: String = e
