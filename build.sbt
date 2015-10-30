//
// build.sbt
//

def newModule(module: String): Project =
  Project(module, file(module))
    .settings(
      organization := "info.rgomes",
      name := s"sbt-snippets-${module}",
      version := (version in ThisBuild).value,
      description := s"Utilities for SBT build scripts and other SBT plugins: ${module} module.",
      licenses += ("BSD Simplified", url("http://opensource.org/licenses/BSD-2-Clause")),
      publishMavenStyle := false,
      publishArtifact in Test := false)
    .settings(
      name in bintray:= s"sbt-snippets-${module}",
      bintrayRepository    in bintray := "sbt-plugins",
      bintrayPackageLabels in bintray := Seq("scala", "sbt", "plugin"),
      bintrayOrganization  in bintray := None)
    .settings(
    pomExtra := (
      <scm>
        <url>git@github.com:frgomes/{name.value}.git</url>
        <connection>scm:git:git@github.com:frgomes/{name.value}.git</connection>
        </scm>
        <developers>
        <developer>
        <id>frgomes</id>
        <name>Richard Gomes</name>
        <url>https://github.com/frgomes</url>
          </developer>
        </developers> ))

lazy val scalaSettings: Seq[Setting[_]] =
  Seq(
    scalaVersion       := "2.11.7",
    crossScalaVersions := Seq("2.11.7", "2.10.6"),
    crossPaths         := true,
    scalacOptions ++= Seq(
      "-unchecked", "-deprecation",
      "-Xlint", "-language:_",
      "-target:jvm-1.6", "-encoding", "UTF-8"
    ))


//++ val testSetup = taskKey[Unit]("Setup tests for extractor")


lazy val root =
  project.in(file("."))
    .settings( publish := { }, publishLocal := { }, bintrayUnpublish := { } )
    .settings(scalaSettings:_*)
    .aggregate(core, plugin)

lazy val core =
  newModule("core")
    .settings(scalaSettings:_*)

lazy val plugin =
  newModule("plugin")
    .settings(sbtPlugin := true)
    .settings(scalaSettings:_*)
    // .settings(crossBuildingSettings:_*)
    // .settings(
    //   name := name.value + "-" + CrossBuilding.pluginSbtVersion.value,
    //   CrossBuilding.crossSbtVersions := Seq("0.12.4", "0.13.0", "0.13.7", "0.13.9")
    // .settings(
      //++ testSetup := {
      //++   System.setProperty("structure.sbtversion.full", CrossBuilding.pluginSbtVersion.value)
      //++   System.setProperty("structure.sbtversion.short", CrossBuilding.pluginSbtVersion.value.substring(0, 4))
      //++   System.setProperty("structure.scalaversion", scalaBinaryVersion.value)
      //++ },
      //++ test in Test <<= (test in Test).dependsOn(testSetup),
      //++ testOnly in Test <<= (testOnly in Test).dependsOn(testSetup))
