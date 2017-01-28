package utils

import java.awt.{Color, Font}
import java.io.File
import javax.inject.Inject

import play.api.Configuration

/**
  * @author Yaroslav Derman <yaroslav.derman@gmail.com>.
  *         created on 28.01.2017.
  */
class ApplicationConfig @Inject()(configuration: Configuration) {

  val imageFolder: String = new File(configuration.getString("media.folder").get).getCanonicalPath + File.separatorChar
  val prefix: String = configuration.getString("captcha.prefix").getOrElse("c:")
  val width: Int = configuration.getInt("captcha.width").getOrElse(200)
  val height: Int = configuration.getInt("captcha.height").getOrElse(50)
  val captchaTtl: Int = configuration.getMilliseconds("captcha.ttl").getOrElse(3 * 60L).toInt

  val fonts: Seq[Font] = configuration.getConfigSeq("captcha.fonts").get.map { conf =>
    val f = conf.underlying
    new Font(f.getString("name"), getFontStyle(conf), f.getInt("size"))
  }

  val colors: Seq[Color] = configuration.getStringSeq("captcha.colors").get.map(Color.decode)

  val redisHost: String = configuration.getString("redis.host").getOrElse("mem:local")

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
