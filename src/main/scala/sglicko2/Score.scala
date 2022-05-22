/*
 * Copyright (c) 2021, Andreas Flierl <andreas@flierl.eu>
 *
 * Permission to use, copy, modify, and/or distribute this software for any
 * purpose with or without fee is hereby granted, provided that the above
 * copyright notice and this permission notice appear in all copies.
 *
 * THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
 * WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
 * MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
 * ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
 * WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
 * ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
 * OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.
 */

package sglicko2

opaque type Score = Double

object Score extends Opaque[Double, Score]:
  transparent inline def apply(d: Double): Score | Valid[Score] =
    macros.opaqueCons(d, liftScore(d), d >= 0d && d <= 1d, "Score must be a number between 0 and 1 (both inclusive).") 

  extension (s: Score)
    def asSeenFromPlayer1: Score = s
    def asSeenFromPlayer2: Score = 1d - asSeenFromPlayer1
    def value: Double = s

private inline def liftScore(d: Double): Score = d
