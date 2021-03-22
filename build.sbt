import xerial.sbt.Sonatype._

inThisBuild(Seq(
  organization := "eu.flierl",
  version := "1.7.0",
  versionScheme := Some("semver-spec"),
  scalaVersion := "2.13.5",
  licenses += ("ISC", url("http://opensource.org/licenses/ISC")),
  githubWorkflowPublishTargetBranches := Nil,
  githubWorkflowJavaVersions := Seq("adopt@11"),
  headerLicense := Some(HeaderLicense.Custom(
    """|Copyright (c) 2021, Andreas Flierl <andreas@flierl.eu>
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

headerLicense := (ThisBuild / headerLicense).value

updateOptions ~= (_ withCachedResolution true)
logBuffered := false
scalacOptions := Seq("-unchecked", "-deprecation", "-language:_", "-encoding", "UTF-8", "-Ybackend-parallelism", "16",
  "-opt:l:inline", "-opt-inline-from:sglicko2.**", "-opt-warnings:_", "-target:11")

ThisBuild / turbo := true
Global / concurrentRestrictions := Seq(Tags.limitAll(32), Tags.exclusiveGroup(Tags.Clean))
Global / sourcesInBase := false

publishMavenStyle := true
sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeProjectHosting := Some(GitHubHosting("asflierl", "sglicko2", "andreas@flierl.eu"))
publishTo := sonatypePublishToBundle.value

Test / scalacOptions += "-Yrangepos"
Test / testOptions += Tests.Argument(TestFrameworks.Specs2, "console", "html", "html.toc", "!pandoc", "specs2ThreadsNb", "31")

libraryDependencies ++= Seq("core", "matcher", "matcher-extra", "scalacheck", "html") map (m => "org.specs2" %% s"specs2-$m" % "4.10.6" % Test)
libraryDependencies ++= Seq("org.scalacheck" %% "scalacheck" % "1.15.3" % Test)

val benchmark = project.dependsOn(sglicko2).enablePlugins(JmhPlugin).settings(
  fork := true,
  scalacOptions := Seq("-unchecked", "-deprecation", "-language:_", "-encoding", "UTF-8", "-target:jvm-1.8"),
  javaOptions := Seq("-Dfile.encoding=UTF-8", "-Duser.country=US", "-Duser.language=en", "-Xms4g", "-Xmx4g", "-Xss1m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=1", "-XX:MaxInlineLevel=20"),
  publish / skip := true,
  Jmh / bspEnabled := false,
  libraryDependencies ++= Seq(
    "org.json4s"    %% "json4s-native" % "3.6.10",
    "org.typelevel" %% "spire" % "0.17.0"),
  headerLicense := (ThisBuild / headerLicense).value)

// benchmarks will run for about 2.5 minutes
addCommandAlias("runBenchmarks", ";benchmark/jmh:run -rf json -rff target/results.json -o target/results.txt;benchmark/runMain sglicko2.benchmark.EvaluateBenchmarkResults")
