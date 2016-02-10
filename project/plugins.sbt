resolvers += Resolver.url("scoverage-bintray", url("https://dl.bintray.com/sksamuel/sbt-plugins/"))(Resolver.ivyStylePatterns)
addSbtPlugin("me.lessis" % "bintray-sbt" % "0.3.0")
//addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.3.5") // 1.3.5 does not work with cross publishing - disabled till 1.3.6 is released
