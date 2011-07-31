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
  def key[T] = _ match { case RemoteAddr(addr) => addr }
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

trait Blocker {
  def block(key: String, requestCount: Int): unfiltered.response.ResponseFunction[Any]
}

object ForbiddenBlocker extends Blocker {
  def block(key: String, requestCount: Int) = PlainTextContent ~> Forbidden
}

/** Throttles requests based on windows of time limiting maxRequests requests per client */
class Throttle(
  window: Window = new HourlyWindow with IpKeyer,
  cache: Cache = new HashCache {},
  maxRequests: Int = 10,
  blocker: Blocker = ForbiddenBlocker) {

  object Throttled {
    def unapply[T](r: HttpRequest[T]) = {
      val k = window.fullkey(r)
      val cnt = cache.get(k) match {
        case Some(n) => n + 1
        case _ => 1
      }
      cache.set(k, cnt)
      if(cnt > maxRequests) Some(k, cnt)
      else None
    }
  }

  def intent[A, B]: unfiltered.Cycle.Intent[A, B] = {
    case Throttled(k, n) => blocker.block(k, n)
    case _ => Pass
  }
}
