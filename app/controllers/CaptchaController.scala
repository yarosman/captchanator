package controllers

import java.io.File
import javax.inject.{Inject, Singleton}

import play.api.libs.json.JsValue
import play.api.mvc.{Action, Controller}
import play.utils.UriEncoding
import services.{CaptchaService, FileUploadingService}
import utils.RequestHelper._
import utils.{ApplicationConfig, Logging}

import scala.concurrent.ExecutionContext

/**
  * @author Yaroslav Derman <yaroslav.derman@gmail.com>.
  *         created on 27.01.2017.
  */
@Singleton
class CaptchaController @Inject()(appConf: ApplicationConfig,
                                  captchaService: CaptchaService)(implicit ctx: ExecutionContext)
  extends Controller with FileUploadingService with Logging {

  log.info("Images folder: " + appConf.imageFolder)

  def create: Action[JsValue] = Action.async(parse.json) { r =>
    val code = (r.body \ "challenge").as[String]
    val ttl = (r.body \ "ttl").asOpt[Int].getOrElse(appConf.captchaTtl)

    captchaService.create(code, ttl, r.clientIp).map {
      case true => Ok
      case false => BadRequest
    }
  }

  def solve(challenge: String): Action[JsValue] = Action.async(parse.json) { r =>
    val answer = (r.body \ "answer").as[String]
    val ip = r.clientIp
    captchaService.solve(challenge, answer, ip).map {
      case true =>
        log.info("Ok: " + challenge + ", ip: " + ip + ", answer: " + answer)
        Ok

      case false =>
        log.info("Wrong: " + challenge + ", ip: " + ip + ", answer: " + answer)
        PreconditionFailed
    }
  }

  def get(file: String) = Action { implicit req =>
    val fileName = UriEncoding.decodePath(file, "utf-8")
    val f = new File(appConf.imageFolder.resolve(fileName).toString)

    if (f.exists && f.canRead) {
      log.info("Get file: " + f.getCanonicalPath + ", file: " + file)
      sendFile(fileName, f.toPath)
    } else {
      if (!f.exists) log.warn("File " + f.getCanonicalPath + " not exists")
      else if (!f.canRead) log.warn("File " + f.getCanonicalPath + " is not readable")
      else {
        log.warn("File " + f.getCanonicalPath + " path don't match: " + appConf.imageFolder.toAbsolutePath)
      }
      NotFound
    }
  }

}
