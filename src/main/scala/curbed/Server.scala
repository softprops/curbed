package curbed

import unfiltered.response.ResponseString

object Server {
  def main(args: Array[String]) {
    unfiltered.jetty.Http(8080).filter(new Throttle with DailyWindow {
      override val maxRequests = 20
    }).filter(unfiltered.filter.Planify {
     case _ => ResponseString("ah ha")
    }).run
  }
}
