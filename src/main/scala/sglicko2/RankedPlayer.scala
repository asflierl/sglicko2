// SPDX-License-Identifier: ISC

package sglicko2

final case class RankedPlayer[A : Eq](rank: Rank, player: Player[A])
