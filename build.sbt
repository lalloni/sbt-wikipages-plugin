organization := "plalloni"

name := "sbt-wikipages-plugin"

sbtPlugin := true

libraryDependencies ++= Seq("dispatch-core", "json4s-jackson") map ("net.databinder.dispatch" %% _ % "0.9.5")

libraryDependencies += "org.json4s" %% "json4s-jackson" % "3.1.0"
