package utils

import play.api.Logger

/**
  * @author Yaroslav Derman <yaroslav.derman@gmail.com>.
  *         created on 28.01.2017.
  */
trait Logging {
  val log = Logger(this.getClass.getName)
}
