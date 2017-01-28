package services

import java.io.File
import java.nio.file.{Files, Path}

import akka.stream.scaladsl.FileIO
import play.api.http.{HeaderNames, HttpEntity, HttpProtocol, MimeTypes}
import play.api.mvc.{Request, ResponseHeader, Result, Results}

/**
  * @author Yaroslav Derman <yaroslav.derman@gmail.com>.
  *         created on 28.01.2017.
  */
trait FileUploadingService {

  def sendFile(name: String, filePath: Path)(implicit request: Request[_]): Result = {
    val file = new File(filePath.toString)

    if (file.exists()) {
      val source = FileIO.fromPath(file.toPath)
      val contentType = Option(Files.probeContentType(filePath)).getOrElse(MimeTypes.BINARY)

      val headers = Seq(
        HeaderNames.CONTENT_DISPOSITION -> s"attachment; filename=$name",
        HeaderNames.CONTENT_TYPE -> contentType
      )

      request.version match {
        case HttpProtocol.HTTP_1_0 =>
          val size = file.length()
          val httpEntity = HttpEntity.Streamed(source, Some(size), Some(contentType))

          Result(
            header = ResponseHeader(200, Map(HeaderNames.CONTENT_LENGTH -> size.toString) ++ headers.toMap),
            body = httpEntity
          )
        case HttpProtocol.HTTP_1_1 =>
          Results.Ok.chunked(source).withHeaders(headers: _*)
      }
    } else {
      Results.NotFound
    }
  }

}
