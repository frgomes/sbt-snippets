organization := "info.rgomes"

name := "sbt-snippets"

licenses += ("BSD", url("http://opensource.org/licenses/BSD-2-Clause"))


scalaVersion       := "2.11.7"

crossScalaVersions := Seq("2.11.7", "2.10.6")

crossPaths         := true

scalacOptions ++= Seq(
  "-unchecked",
  "-deprecation",
  "-Xlint",
  "-language:_",
  "-target:jvm-1.6",
  "-encoding", "UTF-8"
)

sbtPlugin := true

publishMavenStyle := false

publishArtifact in Test := false

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
  </developers>
)