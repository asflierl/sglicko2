name := "sglicko2"
organization := "sglicko2"
version := "1.0"

scalaVersion := "2.11.5"
crossScalaVersions := Seq("2.10.4", "2.11.5")
fork in Test := true
javaOptions in Test := Seq("-server", "-Xmx256m", "-Xss512k", "-XX:+UseG1GC")
scalacOptions := Seq("-unchecked", "-deprecation", "-language:_", "-encoding", "UTF-8", "-target:jvm-1.7")
scalacOptions in Test += "-Yrangepos"
testOptions in Test += Tests.Argument("exclude", "slow")

bintraySettings
licenses += ("ISC", url("http://opensource.org/licenses/ISC"))
bintray.Keys.packageLabels in bintray.Keys.bintray := Seq("glicko 2", "scala", "rating")

updateOptions ~= (_ withCachedResolution true)
resolvers += "bintray" at "http://dl.bintray.com/scalaz/releases"
libraryDependencies ++= Seq("core", "matcher", "matcher-extra", "scalacheck", "html") map (m => "org.specs2" %% s"specs2-$m" % "2.4.15" % Test)
libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.12.1" % Test,
  "com.jsuereth" %% "scala-arm" % "1.4" % Test)