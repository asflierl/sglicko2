import xerial.sbt.Sonatype._

inThisBuild(Seq(
  organization := "eu.flierl",
  version := "2.0.0",
  versionScheme := Some("semver-spec"),
  scalaVersion := "3.1.2",
  scalacOptions := Seq("-source:3.1", "-language:strictEquality", "-new-syntax", "-unchecked", "-deprecation", "-encoding", "UTF-8", "-java-output-version:11"),
  githubWorkflowPublishTargetBranches := Nil,
  githubWorkflowScalaVersions := Seq(scalaVersion.value),
  licenses += ("ISC", url("http://opensource.org/licenses/ISC")),
  headerLicense := Some(HeaderLicense.Custom(
    """|Copyright (c) 2022, Andreas Flierl <andreas@flierl.eu>
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

ThisBuild / turbo := true
Global / concurrentRestrictions := Seq(Tags.limitAll(32), Tags.exclusiveGroup(Tags.Clean))
Global / sourcesInBase := false

publishMavenStyle := true
sonatypeCredentialHost := "s01.oss.sonatype.org"
sonatypeProjectHosting := Some(GitHubHosting("asflierl", "sglicko2", "andreas@flierl.eu"))
publishTo := sonatypePublishToBundle.value

Test / testOptions += Tests.Argument(TestFrameworks.Specs2, "console", "html", "html.toc", "!pandoc", "specs2ThreadsNb", "31")

libraryDependencies ++= Seq("core", "matcher", "matcher-extra", "scalacheck", "html") map (m => "org.specs2" %% s"specs2-$m" % "5.0.0" % Test)
libraryDependencies ++= Seq("org.scalacheck" %% "scalacheck" % "1.16.0" % Test)

val benchmark = project.dependsOn(sglicko2).enablePlugins(JmhPlugin, BuildInfoPlugin).settings(
  fork := true,
  javaOptions := Seq("-Dfile.encoding=UTF-8", "-Duser.country=US", "-Duser.language=en", "-Xms4g", "-Xmx4g", "-Xss1m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=1", "-XX:MaxInlineLevel=20"),
  publish / skip := true,
  Jmh / bspEnabled := false,
  libraryDependencies ++= Seq("core", "generic", "parser").map(m => "io.circe" %% s"circe-$m" % "0.14.1"),
  headerLicense := (ThisBuild / headerLicense).value,
  buildInfoKeys += crossTarget,
  jmh := Def.sequential(
    clean,
    (Compile / compile),
    Def.taskDyn { 
      val dir = crossTarget.value
      (Jmh / run).toTask(s""" -rf json -rff \"$dir/results.json\" -o \"$dir/results.txt\"""")
    },
    (Compile / run).toTask("")
  ).value)

lazy val jmh = taskKey[Unit]("Runs all benchmarks")
