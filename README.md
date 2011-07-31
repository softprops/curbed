# Curbed

Bottle requests with an [unfiltered](http://github.com/n8han/Unfiltered) throttle

# usage

Install as a filter in an unfiltered pipeline

basic in memory throttling

    import unfiltered.response.ResponseString
    import curbed._

    object Server {
       def main(args: Array[String]) {
         unfiltered.jetty.Http.anylocal
          .filter(unfiltered.filter.Planify(new Throttle().intent))
          .filter(unfiltered.filter.Planify {
            case _ => ResponseString("ah ha")
          }).run
       }
    }

basic in memory throttling with explicit maximum requests

    import unfiltered.response.ResponseString
    import curbed._

    object Server {
       def main(args: Array[String]) {
         unfiltered.jetty.Http.anylocal
          .filter(unfiltered.filter.Planify(new Throttle(maxRequests = 100).intent))
          .filter(unfiltered.filter.Planify {
            case _ => ResponseString("ah ha")
          }).run
       }
    }

basic in memory throttling with a daily request window

    import unfiltered.response.ResponseString
    import curbed._

    object Server {
      def main(args: Array[String]) {
        unfiltered.jetty.Http.anylocal
          .filter(unfiltered.filter.Planify(new Throttle(window = new DailyWindow with IpKeyer).intent))
          .filter(unfiltered.filter.Planify {
            case _ => ResponseString("ah ha")
          }).run
      }
    }

## TODO

Doug Tangren (softprops) 2010-2011
