package actors.services.ga

import pl.edu.agh.api.WorkModel.{Job, Result}
import play.api.libs.json.Json
import play.api.mvc.WebSocket.FrameFormatter

object JsonFormatters {

  // api conversions
  implicit val resultFormat = Json.format[Result]

  implicit val resultFormatter = FrameFormatter.jsonFrame[Result]

  implicit val jobFormat = Json.format[Job]

  implicit val jobFormatter = FrameFormatter.jsonFrame[Job]
}