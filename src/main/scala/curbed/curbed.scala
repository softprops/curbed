package curbed

import unfiltered.request._
import unfiltered.response._

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
  def key[T]: HttpRequest[T] => String
}

/** makes keys from client ips */
trait IpKeyer extends LockSmith {
  def key[T] = _ match { case RemoteAddr(addr, _) => addr }
}

/** responsible making a full key varying depending the size of a time window */
trait Window { self: LockSmith => 
  def fullkey[T] = { r: HttpRequest[T] => (self.key(r) :: fmtNow(dateFmt) :: Nil).mkString(":") }
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
class Throttle extends IpKeyer with HashCache with HourlyWindow with unfiltered.filter.Plan {
  object Throttled {
    def unapply[T](r: HttpRequest[T]) = {
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
  
  def intent = {
    case Throttled(k, n) => block(k, n)
    case _ => Pass
  }
  
  /** client is allowed  maxRequests per window before being blocked */
  val maxRequests: Int = 10
  
  def block(k: String, n: Int) = PlainTextContent ~> Forbidden
}

object Server {
  def main(args: Array[String]) {
    unfiltered.jetty.Http(8080).filter(new Throttle with DailyWindow {
      override def maxRequests = 20
    }).filter(unfiltered.filter.Planify {
     case _ => ResponseString("ah ha") 
    }).run
  }
}
