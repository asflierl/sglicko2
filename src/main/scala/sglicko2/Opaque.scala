package sglicko2

private abstract class Opaque[A: Eq, B]:
  given CanEqual[B, B] = CanEqual.derived
