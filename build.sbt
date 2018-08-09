name := """play-task"""

version := "1.0-SNAPSHOT"

scalaVersion := "2.12.6"

crossScalaVersions := Seq("2.11.12", "2.12.4")

lazy val root = (project in file(".")).enablePlugins(PlayScala)

libraryDependencies += guice

//libraryDependencies ++= Seq(guice, evolutions, jdbc)

libraryDependencies ++= Seq(
  "com.typesafe.play" %% "play-slick" % "3.0.1",
  "com.typesafe.play" %% "play-slick-evolutions" % "3.0.1",
  "com.typesafe.play" %% "play-json" % "2.6.0",
  "org.scalatestplus.play" %% "scalatestplus-play" % "3.1.2" % "test",
  "org.postgresql" % "postgresql" % "9.4.1212"
)
