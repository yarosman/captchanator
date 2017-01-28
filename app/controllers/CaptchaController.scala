package controllers

import java.io.File
import java.nio.file.Paths
import javax.inject.{Inject, Singleton}

import com.fotolog.redis.RedisClient
import play.api.Logger
import play.api.libs.json.{JsValue, Json}
import play.api.mvc.{Action, Controller}
import play.utils.UriEncoding
import services.{CaptchaService, FileUploadingService}
import utils.ApplicationConfig
import utils.RequestHelper._

/**
  * @author Yaroslav Derman <yaroslav.derman@gmail.com>.
  *         created on 27.01.2017.
  */
@Singleton
class CaptchaController @Inject()(appConf: ApplicationConfig,
                                  captchaService: CaptchaService) extends Controller with FileUploadingService {

  val log = Logger("captcha")
  log.info("Images folder: " + appConf.imageFolder)

  val redis = RedisClient(appConf.redisHost)

  def create: Action[JsValue] = Action.async(parse.json) { r =>
    val code = (r.body \ "code").asOpt[String].getOrElse((r.body \ "challenge").as[String])
    val ttl = (r.body \ "ttl").asOpt[Int].getOrElse(appConf.captchaTtl)

    captchaService.create(code, ttl, r.clientIp).map {
      case true => Ok(Json.obj("status" -> 0))
      case false => Ok(Json.obj("status" -> -1))
    }
  }

  def solve(code: String): Action[JsValue] = Action.async(parse.json) { r =>
    val answer = (r.body \ "answer").as[String]
    val ip = (r.body \ "ip").asOpt[String].getOrElse("")
    val key = appConf.prefix + ip + "-" + code

    redis.getAsync[String](key) map {
      case Some(realAnsw) =>
        new File(appConf.imageFolder + code + ".png").delete()
        redis.delAsync(key)

        val matches = realAnsw.equals(answer)

        if (matches) {
          log.info("Ok: " + code + ", ip: " + ip + ", answer: " + answer)
          Ok(Json.obj("correct" -> true))
        } else {
          log.info("Wrong: " + code + ", ip: " + ip + ", answer: " + answer)
          Ok(Json.obj("correct" -> false))
        }

      case None =>
        log.info("Not found: " + code + ", ip: " + ip + ", answer: " + answer)
        NotFound
    }
  }

  def get(file: String) = Action { implicit req =>
    val fileName = UriEncoding.decodePath(file, "utf-8")
    val f = new File(appConf.imageFolder + fileName)

    if (f.exists && f.canRead && f.getCanonicalPath.startsWith(appConf.imageFolder)) {
      log.info("Get file: " + f.getCanonicalPath + ", file: " + file)
      sendFile(fileName, Paths.get(appConf.imageFolder + fileName))
    } else {
      if (!f.exists) log.warn("File " + f.getCanonicalPath + " not exists")
      else if (!f.canRead) log.warn("File " + f.getCanonicalPath + " is not readable")
      else {
        log.warn("File " + f.getCanonicalPath + " path don't match: " + new File(appConf.imageFolder).getCanonicalPath)
      }
      NotFound
    }
  }

}
