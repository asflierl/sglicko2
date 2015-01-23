# sglicko2 [![Get automatic notifications about new "sglicko2" versions](https://www.bintray.com/docs/images/bintray_badge_color.png)](https://bintray.com/asflierl/maven/sglicko2/view?source=watch) [![Build Status](https://travis-ci.org/asflierl/sglicko2.svg?branch=master)](https://travis-ci.org/asflierl/sglicko2)

A small, simple & self-contained implementation of the [Glicko 2 rating algorithm](http://www.glicko.net/glicko) in Scala that also helps the user with maintaining a leaderboard and allows for custom scoring rules.

### Setup

Version 1.0 is currently available for Scala 2.10 and 2.11. To use it in your [SBT](http://scala-sbt.org) project, add the following to your build definition:

```scala
resolvers += "bintray/asflierl" at "http://dl.bintray.com/asflierl/maven"
libraryDependencies += "sglicko2" %% "sglicko2" % "1.0"
```

### Usage

Here's a simple, runnable example on how the library can be used:

```scala
import sglicko2._, EitherOnePlayerWinsOrItsADraw._

object Example extends App {
  val glicko2 = new Glicko2[Symbol, EitherOnePlayerWinsOrItsADraw]

  val ratingPeriod = glicko2.newRatingPeriod.withGames(
    ('Anna, 'Becky, Player1Wins),
    ('Anna, 'Chas, Player1Wins),
    ('Anna, 'Dave, Player1Wins),
    ('Becky, 'Chas, Player2Wins),
    ('Becky, 'Dave, Draw),
    ('Chas, 'Dave, Player2Wins))

  val leaderboard = glicko2.updatedLeaderboard(glicko2.newLeaderboard, ratingPeriod)

  leaderboard.rankedPlayers foreach println
}
```

You can find more example code in the test sources. The main sources should be very easy to understand, too, so don't hesitate to look at those if you have questions.

Also, if you use this library, I'd love to hear from you. Thanks <3
