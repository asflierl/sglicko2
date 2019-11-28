# sglicko2 [![Build Status](https://travis-ci.org/asflierl/sglicko2.svg?branch=master)](https://travis-ci.org/asflierl/sglicko2) [![Download](https://api.bintray.com/packages/asflierl/maven/sglicko2/images/download.svg)](https://bintray.com/asflierl/maven/sglicko2/_latestVersion/) [![Join the chat at https://gitter.im/asflierl/sglicko2](https://badges.gitter.im/asflierl/sglicko2.svg)](https://gitter.im/asflierl/sglicko2?utm_source=badge&utm_medium=badge&utm_campaign=pr-badge&utm_content=badge) [![Get automatic notifications about new versions of sglicko2](https://www.bintray.com/docs/images/bintray_badge_color.png)](https://bintray.com/asflierl/maven/sglicko2/view?source=watch)

A small, simple & self-contained implementation of the [Glicko-2 rating algorithm](http://www.glicko.net/glicko.html) in Scala that also helps the user with maintaining a leaderboard and allows for custom scoring rules.

### Setup

Version 1.7 is currently available for Scala 2.13. The last version to support Scala 2.11 and 2.12 was 1.5. The last version to support Scala 2.10 was 1.3. To use this library in your [SBT](http://scala-sbt.org) project, add the following to your build definition:

```scala
resolvers += "jcenter" at "http://jcenter.bintray.com"
libraryDependencies += "sglicko2" %% "sglicko2" % "1.7"
```

### Usage

Here's a simple, runnable example on how the library can be used:

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
    f"${r.rank}%2d ${r.player.id}%5s ${r.player.rating}%4.0f (± ${r.player.deviation}%4.0f)"

  leaderboard.rankedPlayers map pretty foreach println
}
```

Output:
```
 1  Abby 1800 (± 228)
 2  Dave 1500 (± 228)
 3  Chas 1400 (± 228)
 4 Becky 1300 (± 228)
```

You can find more example code in the test sources. The main sources should be very easy to understand, too, so don't hesitate to look at those if you have questions.

Also, if you use this library, I'd love to hear from you. Thanks <3
