name := "sglicko2"
organization := "sglicko2"
version := "1.2"

logBuffered := false

scalaVersion := "2.11.7"
crossScalaVersions := Seq("2.10.5", "2.11.7")
scalacOptions := Seq("-unchecked", "-deprecation", "-optimise", "-language:_", "-encoding", "UTF-8", "-target:jvm-1.7")

fork in Test := true
javaOptions in Test := Seq("-server", "-Xmx4g", "-Xss1m")
scalacOptions in Test += "-Yrangepos"

configs(Benchmark)
inConfig(Benchmark)(Defaults.testTasks)
testFrameworks in Benchmark := ((testFrameworks in Benchmark).value :+ ScalaMeter filterNot (TestFrameworks.Specs2.==))
testOptions in Benchmark += Tests.Argument(ScalaMeter, "-CresultDir", baseDirectory.value / "benchmark" absolutePath)
parallelExecution in Benchmark := false

licenses += ("ISC", url("http://opensource.org/licenses/ISC"))
bintrayPackageLabels := Seq("Glicko-2", "Scala", "rating")

updateOptions ~= (_ withCachedResolution true)
libraryDependencies ++= Seq("core", "matcher", "matcher-extra", "scalacheck", "html") map (m => "org.specs2" %% s"specs2-$m" % "3.6.4" % Test)
libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.12.4" % Test,
  "com.storm-enroute" %% "scalameter" % "0.7" % Test,
  "com.jsuereth" %% "scala-arm" % "1.4" % Test)

lazy val Benchmark = config("benchmark").extend(Test).hide
lazy val ScalaMeter = new TestFramework("org.scalameter.ScalaMeterFramework")
