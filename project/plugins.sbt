resolvers ++=
  Seq(
    Resolver.url("bintray-sbt-plugin-releases", url("https://dl.bintray.com/content/sbt/sbt-plugin-releases"))
                (Resolver.ivyStylePatterns))

addSbtPlugin("me.lessis" % "bintray-sbt" % "0.1.2")

addSbtPlugin("net.virtual-void" % "sbt-cross-building" % "0.8.1")

//XXX libraryDependencies += ("org.scala-sbt" % "scripted-plugin" % sbtVersion.value)
