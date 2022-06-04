# sglicko2 [![Build Status](https://github.com/asflierl/sglicko2/actions/workflows/ci.yml/badge.svg?branch=master)](https://github.com/asflierl/sglicko2/actions?query=branch%3Amaster) [![Maven Central](https://img.shields.io/maven-central/v/eu.flierl/sglicko2_2.13)](https://search.maven.org/search?q=g:eu.flierl%20AND%20a:sglicko2_3) [![Join the chat at https://gitter.im/asflierl/sglicko2](https://badges.gitter.im/asflierl/sglicko2.svg)](https://gitter.im/asflierl/sglicko2?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge)

A small, simple & self-contained implementation of the [Glicko-2 rating algorithm](http://www.glicko.net/glicko.html) in Scala that also helps the user with maintaining a leaderboard and allows for custom scoring rules.

### Setup

Version 2.0.0 is currently available for Scala 3, targetting Java 11. 


 To use this library in your [SBT](http://scala-sbt.org) project, add the following to your build definition:

```scala
libraryDependencies += "eu.flierl" %% "sglicko2" % "2.0.0"
```

### Usage

Here's a simple, runnable example on how the library can be used. You can [experiment with it right in your browser (using Scastie)](https://scastie.scala-lang.org/asflierl/Rh8aKj7aTNapEE163WYyHA/7).

```scala
import sglicko2.*, WinOrDraw.Ops.*

@main def run: Unit = 
  given Glicko2 = Glicko2()

  Leaderboard.empty
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

You can find more example code in the test sources. The main sources should be very easy to understand, too, so don't hesitate to look at those if you have questions.

Also, if you use this library, I'd love to hear from you. Thanks <3
