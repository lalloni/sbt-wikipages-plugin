organization := "plalloni"

name := "sbt-wikipages"

sbtPlugin := true

scalaVersion := "2.9.1"

resolvers += "Sonatype OSS Releases" at "http://oss.sonatype.org/content/repositories/releases/"

//libraryDependencies <+= sbtVersion { "org.scala-sbt" % "sbt" % _ }

libraryDependencies ++= Seq("dispatch-core", "json4s-jackson") map ("net.databinder.dispatch" %% _ % "0.9.5")

libraryDependencies ++= Seq("slf4j-api", "slf4j-simple" ) map ("org.slf4j" % _ % "1.7.2")

libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.1.0"

libraryDependencies += "org.scalatest" %% "scalatest" % "1.9.1" % "test"

EclipseKeys.withSource := true

net.virtualvoid.sbt.graph.Plugin.graphSettings
