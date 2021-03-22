# sglicko2 [![Build Status](https://github.com/asflierl/sglicko2/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/asflierl/sglicko2/actions?query=branch%3Amaster) [![Join the chat at https://gitter.im/asflierl/sglicko2](https://badges.gitter.im/asflierl/sglicko2.svg)](https://gitter.im/asflierl/sglicko2?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

A small, simple & self-contained implementation of the [Glicko-2 rating algorithm](http://www.glicko.net/glicko.html) in Scala that also helps the user with maintaining a leaderboard and allows for custom scoring rules.

### Setup

Version 1.7.1 is currently available for Scala 2.13 and targets Java 8. 


 To use this library in your [SBT](http://scala-sbt.org) project, add the following to your build definition:

```scala
libraryDependencies += "eu.flierl" %% "sglicko2" % "1.7.1"
```

### Usage

Here's a simple, runnable example on how the library can be used. You can [experiment with it right in your browser (using Scastie)](https://scastie.scala-lang.org/asflierl/Rh8aKj7aTNapEE163WYyHA/4).

```scala
import sglicko2._, EitherOnePlayerWinsOrItsADraw._

object Example extends App {
  val glicko2 = new Glicko2[String, EitherOnePlayerWinsOrItsADraw]

  val ratingPeriod = glicko2.newRatingPeriod.withGames(
    ("Abby", "Becky", Player1Wins),
    ("Abby", "Chas", Player1Wins),
    ("Abby", "Dave", Player1Wins),
    ("Becky", "Chas", Player2Wins),
    ("Becky", "Dave", Draw),
    ("Chas", "Dave", Player2Wins))

  val leaderboard = glicko2.updatedLeaderboard(glicko2.newLeaderboard, ratingPeriod)

  def pretty(r: RankedPlayer[String]) = 
    f"${r.rank}%2d ${r.player.id}%5s ${r.player.rating}%4.0f " +
    f"(± ${r.player.deviation * 2d}%4.0f)"

  leaderboard.rankedPlayers map pretty foreach println
}
```

Output:
```
 1  Abby 1800 (± 455)
 2  Dave 1500 (± 455)
 3  Chas 1400 (± 455)
 4 Becky 1300 (± 455)
```

You can find more example code in the test sources. The main sources should be very easy to understand, too, so don't hesitate to look at those if you have questions.

Also, if you use this library, I'd love to hear from you. Thanks <3

### Note on earlier versions

Earlier versions of this library are hosted on Bintray and will be available until February 1st 2022 after which Bintray/JCenter will be [retired by JFrog and completely go out of service](https://jfrog.com/blog/into-the-sunset-bintray-jcenter-gocenter-and-chartcenter/).

**If you are still depending on these versions consider migrating as soon as possible before your build breaks in 2022.**

Versions 1.6 and 1.7 are available for Scala 2.13.

The last version to support Scala 2.11 and 2.12 was 1.5. The last version to support Scala 2.10 was 1.3.

Access them by adding the JCenter repo to your build along with the actual dependency:

```scala
resolvers += Resolver.jcenterRepo
libraryDependencies += "sglicko2" %% "sglicko2" % "1.7"
```