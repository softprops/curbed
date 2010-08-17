import sbt._
class Project(info: ProjectInfo) extends DefaultProject(info) {
  val uf = "net.databinder" %% "unfiltered-server" % "0.1.4-SNAPSHOT"
}