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

final case class Player[A](id: A, rating: Double = 1500d, deviation: Double = 350d, volatility: Double = 0.06d) derives CanEqual:
  require(
    rating > 0d && rating < Double.PositiveInfinity &&
    deviation > 0d && deviation < Double.PositiveInfinity &&
    volatility > 0d && volatility < Double.PositiveInfinity,
    s"rating ($rating), deviation ($deviation) and volatility ($volatility) must each be a number greater than 0")

  // Step 1
  private[sglicko2] inline def r = rating
  private[sglicko2] inline def rd = deviation
  private[sglicko2] inline def σ = volatility

  // Step 2
  private[sglicko2] lazy val µ = (r - 1500d) / glicko2Scalar
  private[sglicko2] lazy val φ = rd / glicko2Scalar