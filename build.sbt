name := "sglicko2"
organization := "sglicko2"
version := "1.4"

logBuffered := false

scalaVersion := "2.12.1"
crossScalaVersions := Seq("2.11.8", "2.12.1")
scalacOptions := {
  val common = Seq("-unchecked", "-deprecation", "-language:_", "-encoding", "UTF-8")

  common ++ {
    if (scalaVersion.value startsWith "2.12.") Seq("-opt:l:classpath", "-target:jvm-1.8", "-Ywarn-unused-import")
    else Seq("-target:jvm-1.7", "-Ywarn-unused-import")
  }
}

fork in Test := true
javaOptions in Test := Seq("-server", "-Xmx4g", "-Xss1m")
scalacOptions in Test += "-Yrangepos"

//configs(Benchmark)
//inConfig(Benchmark)(Defaults.testTasks)
//testFrameworks in Benchmark := ((testFrameworks in Benchmark).value :+ ScalaMeter filterNot (TestFrameworks.Specs2.==))
//testOptions in Benchmark += Tests.Argument(ScalaMeter, "-CresultDir", baseDirectory.value / "benchmark" absolutePath)
//parallelExecution in Benchmark := false

licenses += ("ISC", url("http://opensource.org/licenses/ISC"))
bintrayPackageLabels := Seq("Glicko-2", "Scala", "rating")

updateOptions ~= (_ withCachedResolution true)

libraryDependencies ++= Seq("core", "matcher", "matcher-extra", "scalacheck", "html") map (m => "org.specs2" %% s"specs2-$m" % "3.8.6" % Test)
libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.13.4" % Test,
//  "com.storm-enroute" %% "scalameter" % "0.7" % Test,
  "com.jsuereth" %% "scala-arm" % "2.0" % Test)

//lazy val Benchmark = config("benchmark").extend(Test).hide
//lazy val ScalaMeter = new TestFramework("org.scalameter.ScalaMeterFramework")
