package services

import java.io.File
import javax.imageio.ImageIO
import javax.inject.Inject

import com.fotolog.redis.RedisClient
import nl.captcha.Captcha
import nl.captcha.text.renderer.DefaultWordRenderer
import play.api.Logger
import utils.ApplicationConfig

import scala.collection.JavaConversions._
import scala.concurrent.Future

/**
  * @author Yaroslav Derman <yaroslav.derman@gmail.com>.
  *         created on 28.01.2017.
  */
class CaptchaService @Inject()(appConf: ApplicationConfig) {

  val log = Logger(this.getClass.getName)
  private val renderer = new DefaultWordRenderer(appConf.colors, appConf.fonts)

  private val redis = RedisClient(appConf.redisHost)

  def create(code: String, ttl: Int, ip: String): Future[Boolean] = {
    val captcha = buildCaptcha

    val outFile = new File(appConf.imageFolder + code + ".png")
    log.info("Create: " + code + ", ip: " + ip + ", ttl: " + ttl + ", im: " + outFile.getCanonicalPath)

    ImageIO.write(captcha.getImage, "png", outFile)

    redis.setAsync(appConf.prefix + ip + "-" + code, captcha.getAnswer, ttl)/*.onFailure {
      case e: Exception => log.error("Error creating captcha image: " + code, e)
    }*/
  }

  //TODO: validate captcha

  //TODO: remove captcha images from disk space

  private def buildCaptcha = new Captcha.Builder(appConf.width, appConf.height)
    .addText(renderer)
    // .addBackground(new GradiatedBackgroundProducer())
    .gimp()
    .build()

}
