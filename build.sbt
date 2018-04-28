inThisBuild(Seq(
  organization := "sglicko2",
  scalaVersion := "2.12.6",
  licenses += ("ISC", url("http://opensource.org/licenses/ISC")),
  headerLicense := Some(HeaderLicense.Custom(
    """|Copyright (c) 2015, Andreas Flierl <andreas@flierl.eu>
       |
       |Permission to use, copy, modify, and/or distribute this software for any
       |purpose with or without fee is hereby granted, provided that the above
       |copyright notice and this permission notice appear in all copies.
       |
       |THE SOFTWARE IS PROVIDED "AS IS" AND THE AUTHOR DISCLAIMS ALL WARRANTIES
       |WITH REGARD TO THIS SOFTWARE INCLUDING ALL IMPLIED WARRANTIES OF
       |MERCHANTABILITY AND FITNESS. IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR
       |ANY SPECIAL, DIRECT, INDIRECT, OR CONSEQUENTIAL DAMAGES OR ANY DAMAGES
       |WHATSOEVER RESULTING FROM LOSS OF USE, DATA OR PROFITS, WHETHER IN AN
       |ACTION OF CONTRACT, NEGLIGENCE OR OTHER TORTIOUS ACTION, ARISING OUT OF
       |OR IN CONNECTION WITH THE USE OR PERFORMANCE OF THIS SOFTWARE.""".stripMargin))))

val sglicko2 = project.in(file("."))

version := "1.6"

bintrayPackageLabels := Seq("Glicko-2", "Scala", "rating")
headerLicense := (ThisBuild / headerLicense).value

updateOptions ~= (_ withCachedResolution true)
logBuffered := false
scalacOptions := Seq("-unchecked", "-deprecation", "-language:_", "-encoding", "UTF-8", "-Ywarn-unused-import",
  "-opt:l:inline", "-opt-inline-from:sglicko2.**", "-opt-warnings:_", "-Yopt-log-inline", "_", "-target:jvm-1.8")

Test / fork := true
Test / javaOptions := Seq("-server", "-Xmx4g", "-Xss1m")
Test / scalacOptions += "-Yrangepos"
Test / testOptions += Tests.Argument(TestFrameworks.Specs2, "console", "html", "html.toc", "!pandoc")

libraryDependencies ++= Seq("core", "matcher", "matcher-extra", "scalacheck", "html") map (m => "org.specs2" %% s"specs2-$m" % "4.1.0" % Test)
libraryDependencies ++= Seq("org.scalacheck" %% "scalacheck" % "1.13.4" % Test)

val benchmark = project.dependsOn(sglicko2).enablePlugins(JmhPlugin).settings(
  fork := true,
  scalacOptions := Seq("-unchecked", "-deprecation", "-language:_", "-encoding", "UTF-8", "-Ywarn-unused-import", "-target:jvm-1.8"),
  javaOptions := Seq("-Dfile.encoding=UTF-8", "-Duser.country=US", "-Duser.language=en", "-Xms4g", "-Xmx4g", "-Xss1m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=250"),
  libraryDependencies ++= Seq(
    "org.typelevel" %% "spire"         % "0.15.0",
    "io.circe"      %% "circe-core"    % "0.9.3",
    "io.circe"      %% "circe-generic" % "0.9.3",
    "io.circe"      %% "circe-parser"  % "0.9.3"),
  addCompilerPlugin("org.scalamacros" % "paradise" % "2.1.0" cross CrossVersion.full),
  headerLicense := (ThisBuild / headerLicense).value)

addCommandAlias("runBenchmarks", ";benchmark/jmh:run -rf json -rff target/results.json -o target/results.txt;benchmark/runMain sglicko2.benchmark.EvaluateBenchmarkResults")
