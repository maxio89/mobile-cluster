package actors.services.ga

import pl.edu.agh.api.RastriginWork._
import play.api.libs.json.Json
import play.api.mvc.WebSocket.FrameFormatter

object JsonFormatters {

  // api conversions
  implicit val resultFormat = Json.format[RastriginResult]

  implicit val resultFormatter = FrameFormatter.jsonFrame[RastriginResult]

  implicit val jobFormat = Json.format[RastriginConfig]

  implicit val jobFormatter = FrameFormatter.jsonFrame[RastriginConfig]
}