// SPDX-License-Identifier: ISC

package sglicko2

final case class RankedPlayer[+P: Eq](rank: Rank, player: Player[P])
