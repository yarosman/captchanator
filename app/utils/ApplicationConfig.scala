package utils

import java.awt.{Color, Font}
import java.nio.file.{Files, Path, Paths}
import javax.inject.Inject

import play.api.Configuration

/**
  * @author Yaroslav Derman <yaroslav.derman@gmail.com>.
  *         created on 28.01.2017.
  */
class ApplicationConfig @Inject()(conf: Configuration) {

  val imageFolder: Path = Files.createDirectories(Paths.get(conf.getString("media.folder").get))
  val prefix: String = conf.getString("captcha.prefix").getOrElse("c:")
  val width: Int = conf.getInt("captcha.width").getOrElse(200)
  val height: Int = conf.getInt("captcha.height").getOrElse(50)
  val captchaTtl: Int = conf.getMilliseconds("captcha.ttl").getOrElse(3 * 60L).toInt

  val fonts: Seq[Font] = conf.getConfigSeq("captcha.fonts").get.map { conf =>
    val f = conf.underlying
    new Font(f.getString("name"), getFontStyle(conf), f.getInt("size"))
  }

  val colors: Seq[Color] = conf.getStringSeq("captcha.colors").get.map(Color.decode)

  val redisHost: String = conf.getString("redis.host").getOrElse("mem:local")

  /**
    * Returns font style if set from config.
    * Allowed values are plain, bold, italic and bold italic or italic bold
    *
    * @param cfg Configuration
    * @return
    */
  private def getFontStyle(cfg: Configuration): Int = cfg.getString("style") match {
    case Some("plain") => Font.PLAIN
    case Some("bold") => Font.BOLD
    case Some("italic") => Font.ITALIC
    case Some("bold italic") => Font.BOLD
    case Some("italic bold") => Font.ITALIC
    case _ => Font.PLAIN
  }

}
