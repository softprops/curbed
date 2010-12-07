import sbt._
class Project(info: ProjectInfo) extends DefaultProject(info) {
  val ufversion = "0.2.3"
  val ufj = "net.databinder" %% "unfiltered-jetty" % ufversion
  val ufs = "net.databinder" %% "unfiltered-filter" % ufversion
}
