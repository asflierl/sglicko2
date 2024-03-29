// SPDX-License-Identifier: ISC

package sglicko2

final case class Player[+P: Eq](id: P, rating: Rating = Rating.default, deviation: Deviation = Deviation.default, 
    volatility: Volatility = Volatility.default) derives CanEqual:
  
  def confidence95 = Interval(
    Rating.fromGlicko2(Rating.toGlicko2(rating) - Deviation.toGlicko2(deviation) * 1.96d), 
    Rating.fromGlicko2(Rating.toGlicko2(rating) + Deviation.toGlicko2(deviation) * 1.96d))
