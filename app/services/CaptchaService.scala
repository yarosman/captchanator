package services

import java.io.File
import java.nio.file.Path
import javax.imageio.ImageIO
import javax.inject.Inject

import com.fotolog.redis.RedisClient
import nl.captcha.Captcha
import nl.captcha.text.renderer.DefaultWordRenderer
import utils.{ApplicationConfig, Logging}

import scala.collection.JavaConversions._
import scala.concurrent.Future

/**
  * @author Yaroslav Derman <yaroslav.derman@gmail.com>.
  *         created on 28.01.2017.
  */
class CaptchaService @Inject()(appConf: ApplicationConfig) extends Logging {

  private val renderer = new DefaultWordRenderer(appConf.colors, appConf.fonts)

  implicit def path2string(p: Path): String = p.toString

  private val redis = RedisClient(appConf.redisHost)

  def create(code: String, ttl: Int, ip: String): Future[Boolean] = {
    val captcha = buildCaptcha

    val outFile = new File(appConf.imageFolder.resolve(code + ".png"))
    log.info("Create: " + code + ", ip: " + ip + ", ttl: " + ttl + ", im: " + outFile.getCanonicalPath)

    ImageIO.write(captcha.getImage, "png", outFile)

    redis.setAsync(appConf.prefix + ip + "-" + code, captcha.getAnswer, ttl)/*.onFailure {
      case e: Exception => log.error("Error creating captcha image: " + code, e)
    }*/
  }

  //TODO: remove ip from captcha
  def solve(challenge: String, answer: String, ip: String): Future[Boolean] = {
    val key = appConf.prefix + ip + "-" + challenge

    redis.getAsync[String](key) map {
      case Some(realAnsw) =>
        new File(appConf.imageFolder.resolve(challenge + ".png")).delete()
        redis.delAsync(key)
        realAnsw.equals(answer)

      case None =>
        log.info("Not found: " + challenge + ", ip: " + ip + ", answer: " + answer)
        //TODO: to custom exception
        throw new RuntimeException("Captcha expired or unexist")
    }
  }

  //TODO: remove expired captcha images from disk space

  private def buildCaptcha = new Captcha.Builder(appConf.width, appConf.height)
    .addText(renderer)
    // .addBackground(new GradiatedBackgroundProducer())
    .gimp()
    .build()

}
