organization := "me.lessis"

name := "curbed"

version := "0.1.0-SNAPSHOT"

crossScalaVersions := Seq("2.8.0", "2.8.1", "2.9.0", "2.9.0-1"/*, "2.9.1.RC1"*/)

libraryDependencies ++= Seq(
  "net.databinder" %% "unfiltered-jetty" % "0.4.0",
  "net.databinder" %% "unfiltered-filter" % "0.4.0"
)
