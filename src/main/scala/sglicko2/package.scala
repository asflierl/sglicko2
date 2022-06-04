// SPDX-License-Identifier: ISC

package sglicko2 

type Eq[-A] = CanEqual[A, A]

type Valid[A] = Either[Err, A]
