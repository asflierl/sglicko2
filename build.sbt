name := "sglicko2"
organization := "sglicko2"
version := "1.1"

scalaVersion := "2.11.6"
crossScalaVersions := Seq("2.10.4", "2.11.6")
fork in Test := true
parallelExecution in Test := false
javaOptions in Test := Seq("-server", "-Xmx4g", "-Xss1m")
scalacOptions := Seq("-unchecked", "-deprecation", "-language:_", "-encoding", "UTF-8", "-target:jvm-1.7")
scalacOptions in Test += "-Yrangepos"
testFrameworks += new TestFramework("org.scalameter.ScalaMeterFramework")
logBuffered := false

bintraySettings
licenses += ("ISC", url("http://opensource.org/licenses/ISC"))
bintray.Keys.packageLabels in bintray.Keys.bintray := Seq("Glicko-2", "Scala", "rating")

updateOptions ~= (_ withCachedResolution true)
resolvers += "bintray" at "http://dl.bintray.com/scalaz/releases"
resolvers += "Sonatype OSS Snapshots" at "https://oss.sonatype.org/content/repositories/snapshots"
libraryDependencies ++= Seq("core", "matcher", "matcher-extra", "scalacheck", "html") map (m => "org.specs2" %% s"specs2-$m" % "3.1" % Test)
libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.12.2" % Test,
  "com.storm-enroute" %% "scalameter" % "0.7-SNAPSHOT",
  "com.jsuereth" %% "scala-arm" % "1.4" % Test)