package sglicko2

opaque type Err = String

object Err:
  def apply(message: String): Err = message

extension (e: Err)
  def message: String = e
