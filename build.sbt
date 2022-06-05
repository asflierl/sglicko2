import xerial.sbt.Sonatype._

inThisBuild(Seq(
  organization := "eu.flierl",
  version := "2.0.0",
  versionScheme := Some("semver-spec"),
  scalaVersion := "3.1.2",
  scalacOptions := Seq("-source:3.1", "-language:strictEquality", "-new-syntax", "-unchecked", "-deprecation", "-encoding", "UTF-8", "-java-output-version:11"),
  githubWorkflowPublishTargetBranches := Nil,
  githubWorkflowBuild := Seq(WorkflowStep.Sbt(List("clean", "test")))))

lazy val sglicko2 = project.in(file(".")).enablePlugins(AutomateHeaderPlugin).settings(licensing).settings(
  publishMavenStyle := true,
  sonatypeCredentialHost := "s01.oss.sonatype.org",
  sonatypeProjectHosting := Some(GitHubHosting("asflierl", "sglicko2", "andreas@flierl.eu")),
  publishTo := sonatypePublishToBundle.value,
  turbo := true,
  Test / testOptions += Tests.Argument(TestFrameworks.Specs2, "console", "html", "html.toc", "!pandoc", "specs2ThreadsNb", cpus.toString),
  libraryDependencies ++= Seq("core", "matcher", "matcher-extra", "scalacheck", "html") map (m => "org.specs2" %% s"specs2-$m" % "5.0.0" % Test),
  libraryDependencies ++= Seq("org.scalacheck" %% "scalacheck" % "1.16.0" % Test))

lazy val benchmark = project.dependsOn(sglicko2).enablePlugins(JmhPlugin, BuildInfoPlugin, AutomateHeaderPlugin).settings(licensing).settings(
  fork := true,
  javaOptions := Seq("-Dfile.encoding=UTF-8", "-Duser.country=US", "-Duser.language=en", "-Xms4g", "-Xmx4g", "-Xss1m", "-XX:+UseG1GC", "-XX:MaxGCPauseMillis=1", "-XX:MaxInlineLevel=20"),
  publish / skip := true,
  Jmh / bspEnabled := false,
  libraryDependencies ++= Seq("core", "generic", "parser").map(m => "io.circe" %% s"circe-$m" % "0.14.1"),
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

Global / concurrentRestrictions := Seq(Tags.limitAll(cpus * 4), Tags.limit(Tags.CPU, cpus), Tags.exclusiveGroup(Tags.Clean))
Global / sourcesInBase := false

lazy val jmh = taskKey[Unit]("Runs all benchmarks")

lazy val licensing = Seq(
  licenses += ("ISC", url("http://opensource.org/licenses/ISC")),
  headerLicense := Some(HeaderLicense.Custom("SPDX-License-Identifier: ISC".stripMargin)),
  headerMappings := Map(HeaderFileType.scala -> HeaderCommentStyle.cppStyleLineComment))

lazy val cpus = java.lang.Runtime.getRuntime.availableProcessors