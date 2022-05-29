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
  headerLicense := Some(HeaderLicense.Custom("SPDX-License-Identifier: ISC".stripMargin)),
  headerMappings := Map(HeaderFileType.scala -> HeaderCommentStyle.cppStyleLineComment)))

val sglicko2 = project.in(file("."))

headerLicense := (ThisBuild / headerLicense).value
headerMappings := (ThisBuild / headerMappings).value

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
  headerMappings := (ThisBuild / headerMappings).value,
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
