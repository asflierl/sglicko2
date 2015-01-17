name := "sglicko2"
version := "1.0-SNAPSHOT"

scalaVersion := "2.11.5"
crossScalaVersions := Seq("2.10.4", "2.11.5")
fork in run := true
fork in test := true
javaOptions in Compile := Seq("-server", "-Xmx2g", "-Xss1m", "-XX:+UseG1GC")
javacOptions := Seq("-encoding", "UTF-8", "-g", "-Xlint:all", "-source", "1.6", "-target", "1.6")
javaHome := Some(file(sys.props("java.home"))).map(d => if (d.name == "jre" && ! (d.getParentFile / "bin" * "javac*").get.isEmpty) d.getParentFile else d)
scalacOptions := Seq("-unchecked", "-deprecation", "-language:_", "-encoding", "UTF-8", "-target:jvm-1.6")
scalacOptions in Test += "-Yrangepos"

updateOptions ~= (_ withCachedResolution true)
resolvers += "bintray" at "http://dl.bintray.com/scalaz/releases"
libraryDependencies ++= Seq("core", "matcher", "matcher-extra", "scalacheck") map (m => "org.specs2" %% s"specs2-$m" % "2.4.15" % Test)
libraryDependencies ++= Seq(
  "org.scalacheck" %% "scalacheck" % "1.12.1" % Test,
  "com.jsuereth" %% "scala-arm" % "1.4" % Test)