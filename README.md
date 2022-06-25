# sglicko2 [![Maven Central](https://img.shields.io/maven-central/v/eu.flierl/sglicko2_3)](https://search.maven.org/search?q=g:eu.flierl%20AND%20a:sglicko2_3) [![Build Status](https://github.com/asflierl/sglicko2/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/asflierl/sglicko2/actions?query=branch%3Amaster)

A small & simple implementation of the [Glicko-2 rating algorithm](http://www.glicko.net/glicko.html) in Scala.

## Features

 - helps with maintaining a player leaderboard 
 - any type (with proper equality, like `String` or `UUID` etc.) can be used to identify a player
 - allows custom scoring rules (e.g. more than 2 opponents per game, point difference instead of ternary win/loss/draw)
 - rating scale can be switched between Glicko and Glicko-2
 - checks important constraints – where possible, at compile-time
 - only depends on the Scala standard library

## Setup

Version 2.0.2 is currently available for Scala 3, targetting Java 11. 

To use this library in your [SBT](http://scala-sbt.org) project, add the following to your build definition:

```scala
libraryDependencies += "eu.flierl" %% "sglicko2" % "2.0.2"
```

## Usage

### Basics
Here's a simple, runnable example on how the library can be used. You can [experiment with it right in your browser (using Scastie)](https://scastie.scala-lang.org/asflierl/e7d2vFTpTFqxq85sIx6WDQ).

```scala
import sglicko2.*, WinOrDraw.Ops.*

@main def run: Unit = 
  given Glicko2 = Glicko2()

  Leaderboard.empty[String]
    .after(RatingPeriod(
      "Abby"  winsVs   "Becky",
      "Abby"  winsVs   "Chas",
      "Abby"  winsVs   "Dave",
      "Chas"  winsVs   "Becky",
      "Becky" tiesWith "Dave",
      "Dave"  winsVs   "Chas"))
    .rankedPlayers
    .foreach(p => println(
      f"${p.rank}%2d ${p.player.id}%5s: " +
      f"[${p.player.confidence95.lower.value}%4.0f, " +
      f"${p.player.confidence95.upper.value}%4.0f]"))
```

Output:
```
 1  Abby: [1353, 2246]
 2  Dave: [1054, 1946]
 3  Chas: [ 954, 1846]
 4 Becky: [ 854, 1747]
```

 - The starting point is an empty leaderboard, since no games have been played yet and therefore no players have been recorded.
 - Games are grouped into "rating periods" of time intervals of your choice, i.e. all games that have been played in a day, a week, a season etc. — whatever makes sense for the particular kind of game being played.
 - After each rating period, the evaluation of each player changes according to the games that have been played, even for the ones who didn't play during that particular rating period (but played in previous ones): the uncertainty (deviation) of their rating increases.
 - In other words, conceptually, the rating function `glicko2` takes a leaderboard and a set of games during a rating period and produces an updated leaderboard, i.e. `glicko2: Leaderboard ⨯ RatingPeriod → Leaderboard`.
 - The papers on [Glicko-2](http://www.glicko.net/glicko/glicko.pdf) and its predecessor, [Glicko](http://www.glicko.net/glicko/glicko.pdf), contain a lot of details on the usage of the system and are a highly recommended read.
 
 *A note on the output of the code above:* as recommended in the [Glicko paper](http://www.glicko.net/glicko/glicko.pdf), the rating of the players is reported as a 95% confidence interval here instead of using the median / middle value (rating). So in this example, we're fairly confident that Abby's actual playing strength lies somewhere between 1353 and 2246, i.e. a player's rating is the median of the probability distribution and should not be treated like an accurate measure of actual playing strength.


### Changing parameters

[The Glicko-2 paper](http://www.glicko.net/glicko/glicko2.pdf) mentions two parameters that you might want to tweak to fit your particular application:

> The system constant, τ, which constrains the change in volatility over time, needs to be set prior to application of the system. Reasonable choices are between 0.3 and 1.2, though the system should be tested to decide which value results in greatest predictive accuracy. Smaller values of τ prevent the volatility measures from changing by large  amounts, which in turn prevent enormous changes in ratings based on very improbable results. If the application of Glicko-2 is expected to involve extremely improbable collections of game outcomes, then τ should be set to a small value, even as small as, say, τ = 0.2.

and

> If the player is unrated, [...] set the player's volatility to 0.06 (this value depends on the particular application).

Both values can be set when creating the `Glicko2` instance:

```scala
given Glicko2 = Glicko2(tau = Tau[0.6d], defaultVolatility = Volatility(0.06d))
```

### Notes on syntax

For the most commonly used scoring mode (win/loss/draw), there are three syntax alternatives. Choose whichever you like best:

```scala
import sglicko2.WinOrDraw.*

// (1)
Win("Abby", "Becky")
Draw("Abby", "Becky")

import sglicko2.WinOrDraw.Ops.*

// (2)
"Abby" winsVs "Becky"
"Abby" tiesWith "Becky"

// (3)
"Abby" :>: "Becky"
"Abby" :=: "Becky"
```

The types `Tau` (the Glicko-2 system constant) and `Score` have constraints that can be checked at compile- or runtime.

```scala
Tau[-2d]   // will not compile: tau must be > 0
Tau[0.6d]  // will compile and return a Tau of 0.6
Tau(-2d)   // will compile but return a Left[Err, ...] at runtime
Tau(0.6d)  // will compile and return a Right[..., Tau] at runtime

Score[2d]   // will not compile: score must be in [0d, 1d]
Score[0.5d] // will compile and return a Score of 0.5
Score(2d)   // will compile and return a Score of 1
Score(-1d)  // will compile and return a Score of 0
```

For more cases, check `src/test/scala/sglicko2/ConstraintsSpec.scala`.

### Choosing a scale

Technically, Glicko-2 uses its own scale that is centered around 0. However, it is not commonly used and even the paper uses the original Glicko scale that is centered around 1500 in its examples and converts to (and from) its internal scale at the beginning (and end) of the algorithm specification.

Nevertheless, this library works with the Glicko-2 scale throughout and only converts to an implictly given "output" scale, whenever the user converts a `Rating` or `Deviation` to a `Double`. An unfortunate side-effect of this is that relying on `.toString` to show the values will use the Glicko-2 scale, which is usually not what you want. Instead, you should always use the `.value` methods of these types as shown in the simple example at the beginning of this document.

You can select a different scale (Glicko is the default) when creating the `Glicko2` instance:

```scala
given Glicko2 = Glicko2(scale = Scale.Glicko)
```

Of course, you can always supply the `Scale` implicitly yourself, in case you don't have (or don't want to have) a `Glicko2` instance in scope.

### Identifying players

Many of this library's types are parameterized with a type parameter `A` to allow for any custom type to identify players. This can be as general as a `String` or more specific like a `java.util.UUID` or your own type, e.g. a case class. The only requirement is that its implementation provides a proper equality (i.e. `.equals(...)` method) and provides an instance of `Eq[A]` (which is just an alias for `scala.CanEqual[A, A]`) to signal that values of this type can safely be compared to each other.

The Scala standard library provides that for primitive types and some very common types. If you use your own type, you will have to provide this typeclass instance as well.

**Note:** for performance reasons, the type you use to identify players should have a performant (and of course correct) implementation of `.hashCode`. Again, this is automatically the case for primitive types, `String`, `UUID` and tuples / products / case classes of these types etc. but for your own classes, some attention to this is advised.

### Implementing custom games and scoring rules

Any type `B` can be used in a `RatingPeriod` to represent a game, as long as there is an instance of `ScoringRules[A, B]` in implicit scope, where `A` is any type you use to identify a player. The job of scoring rules is to "explain" what an outcome of a game means "in Glicko-2 terms". The Glicko-2 algorithm expects input as three values:

 1. the player that is being rated
 2. their opponent
 3. a score for the player being rated, given as any (fractional) value between 0 and 1, where 0 is a loss, 1 is a win and 0.5 is a draw

#### Provided implementation

Let's look at the most basic scoring rules as an example, where either one player wins or the game is a draw. This is implemented in `src/main/scala/sglicko2/WinOrDraw.scala`:

```scala
given [A: Eq]: ScoringRules[A, WinOrDraw[A]] with
  def gameScores(g: WinOrDraw[A]): Iterable[(A, A, Score)] = Some(g match
    case Win(winner, loser)     => (winner,  loser,   Score[1d])
    case Draw(player1, player2) => (player1, player2, Score[0.5d]))
```

So in a single game with two players, represented e.g. by `Win("Abby", "Becky")` (Abby wins against Becky), these scoring rules will "translate" that single outcome as if Abby is being rated:

 1. `"Abby"`
 2. `"Becky"`
 3. `1.0d`

This will be used to update the rating of Abby and her position on the leaderboard. But the second player also needs to be rated. Therefore the above values will be automatically "inverted" for the second player like this:

 1. `"Becky"`
 2. `"Abby"`
 3. `0.0d`

This "inversion" is not the responsibility of the scoring rules. It is done behind the scenes when a leaderboard is updated after a new rating period.

#### Games with more than 2 players

Although, not mentioned in the paper, an interesting property of Glicko-2 is that it supports breaking down games with more than two opposing players / teams into several games of exactly two players / teams. Here's a sketch of how that could look for three players / teams:

```scala
case class Outcome(winner: String, second: String, last: String)

given ScoringRules[String, Outcome] with
  def gameScores(o: Outcome): Iterable[(String, String, Score)] = Seq(
    (o.winner, o.second, Score[1d]),
    (o.winner, o.last,   Score[1d]),
    (o.second, o.last,   Score[1d]))
```

This is essentially saying that one three-player game where e.g. Abby wins, Becky takes second place and Chas is last can be represented as three two-player games: Abby wins vs Becky, Abby wins vs Chas and Becky wins vs Chas.

#### More accurate score

One detail we can glean from the paper is that we are not limited to the three values 0, 0.5 and 1 to represent the outcome of a game. Any fractional value between 0 and 1 is fine and Glicko-2 was made with that in mind. Considering e.g. soccer games, winning 7 : 0 is certainly a bigger win than winning 2 : 1 and we might want to see that reflected in how much the ratings change afterwards.

Calculating a good score for these scenarios can get involved but a neat formula that works for any positive point-based outcome was used by the Guild Wars 2 team to calculate server ratings for their "world vs world" game mode (see `src/test/scala/sglicko2/GW2ExampleSpec.scala` and `src/test/scala/sglicko2/GW2ExampleResources.scala` for more details).

Implementing the gist of that as `ScoringRules` would look like this:

```scala
import scala.math.{sin, Pi}

case class Outcome(name1: String, points1: Long, name2: String, points2: Long)

given ScoringRules[String, Outcome] with
  def gameScores(o: Outcome): Iterable[(String, String, Score)] = 
    Some(o.name1, o.name2, rateAVsB(o.points1.toDouble, o.points2.toDouble))

  def rateAVsB(a: Double, b: Double) = 
    Score((sin((a / (a + b) - 0.5d) * Pi) + 1d) * 0.5d)
```

When calculating a custom `Score` like this, be careful that your formula does not produce `NaN` values for plausible outcomes of your game. 😇

## Enjoy!

If you use this library, I'd love to hear from you. Please feel free to reach out with feature requests or if the API is unclear. 💖
