# Curbed

Bottle requests with an [unfiltered](http://github.com/n8han/Unfiltered) throttle

# usage

Install as a filter in an unfiltered pipeline

basic in memory throttling

    object Server {
      def main(args: Array[String]) {
        unfiltered.server.Http(8080).filter(new curbed.Throttle).filter(unfiltered.Planify {
          case _ => ResponseString("hit me")
        }).run
      }
    }

basic in memory throttling with explicit maximum requests

    object Server {
      def main(args: Array[String]) {
        unfiltered.server.Http(8080).filter(new curbed.Throttle {
          override def maxRequests = 100
        }).filter(unfiltered.Planify {
          case _ => ResponseString("hit me")
        }).run
      }
    }

basic in memory throttling with a daily request window

    object Server {
      def main(args: Array[String]) {
        unfiltered.server.Http(8080).filter(new curbed.Throttle with DailyWindow).filter(unfiltered.Planify {
          case _ => ResponseString("hit me")
        }).run
      }
    }

## TODO

use plain unfilter.Cycle.Intent[_, _]

Doug Tangren (softprops) 2010-2011
