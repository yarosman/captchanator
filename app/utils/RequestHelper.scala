package utils

import play.api.mvc.RequestHeader

/**
  * @author Yaroslav Derman <yaroslav.derman@gmail.com>.
  *         created on 28.01.2017.
  */
object RequestHelper {

  private val HTTP_IP_HEADERS = Seq(
    "X-Real-IP", "X-Forwarded-For", "Proxy-Client-IP", "HTTP_CLIENT_IP", "HTTP_X_FORWARDED_FOR", "WL-Proxy-Client-IP"
  )

  implicit class RichRequest(val r: RequestHeader) extends AnyVal {
    def clientIp: String = {
      val header = HTTP_IP_HEADERS.find(name => r.headers.get(name).exists(h => h.nonEmpty && !h.equalsIgnoreCase("unknown")))

      header match {
        case Some(name) => r.headers(name)
        case None => r.remoteAddress
      }
    }

    def clientAgent: String = r.headers.get("User-Agent").getOrElse("not set")
  }

}
