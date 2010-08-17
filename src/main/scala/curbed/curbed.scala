package curbed

import unfiltered.request._
import unfiltered.response._

import javax.servlet.http.{HttpServletRequest => Req}

/** cache interface for client request counter */
trait Cache {
  def get(k: String): Option[Int]
  def set(k: String, v: Int)
}

/** memory based cache */
trait HashCache extends Cache {
  private var cache = scala.collection.mutable.Map.empty[String, Int]
  def get(k: String) = cache.get(k)
  def set(k: String, v: Int) = cache += (k -> v)
}

/** responsible for making keys from requests */
trait LockSmith {
  /** @return a string key for a request */
  def key: Req => String
}

/** makes keys from client ips */
trait IpKeyer extends LockSmith {
  override def key = { _.getRemoteAddr }
}

/** responsible making a full key varying depending the size of a time window */
trait Window { self: LockSmith => 
  def fullkey = { r: Req => (self.key(r) :: fmtNow(dateFmt) :: Nil).mkString(":") }
  /** @return a date formate based on the size of the window */
  protected def dateFmt: String
  private def fmtNow(fmt: String) = new java.text.SimpleDateFormat(fmt).format(new java.util.Date)
}

/** makes keys for hourly windows */
trait HourlyWindow extends Window { self: LockSmith => 
  override protected def dateFmt = "yyyy-MM-dd-HH"
}

/** makes keys for daily windows */
trait DailyWindow extends Window { self: LockSmith => 
  override protected def dateFmt = "yyyy-MM-dd"
}

/** Throttles requests based on windows of time limiting maxRequests requests per client */
class Throttle extends IpKeyer with HashCache with HourlyWindow with unfiltered.Plan {
  object Throttled {
    def unapply(r: Req) = {
      val k = fullkey(r)
      val cnt = get(k) match {
        case Some(n) => n + 1
        case _ => 1
      }
      set(k, cnt)
      if(cnt > maxRequests) Some(k, cnt)
      else None
    }
  }
  
  def filter = {
    case Throttled(k, n) => block(k, n)
    case _ => Pass
  }
  
  def maxRequests = 10
  
  def block(k: String, n: Int) = PlainTextContent ~> Forbidden
}

object Server {
  def main(args: Array[String]) {
    unfiltered.server.Http(8080).filter(new Throttle with DailyWindow {
      override def maxRequests = 20
    }).filter(unfiltered.Planify{
     case _ => ResponseString("ah ha") 
    }).run
  }
}
