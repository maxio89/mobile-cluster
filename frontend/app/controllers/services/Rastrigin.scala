package controllers.services

import actors.services.ga.{WorkProducer, WorkResultConsumer}
import pl.edu.agh.api.WorkModel._
import play.api.Play.current
import play.api.mvc._

object Rastrigin extends Controller {
  //
  //  import play.api.libs.json._
  //  import play.api.mvc.WebSocket.FrameFormatter
  //
  //
  //  implicit val inFormat = Json.format[Job]
  //  implicit val outFormat = Json.format[Result]
  //
  //  implicit val inFrameFormatter = FrameFormatter.jsonFrame[Work]
  //  implicit val outFrameFormatter = FrameFormatter.jsonFrame[WorkResult]
  import actors.services.ga.JsonFormatters._

  def workProducerWebsocket() = WebSocket.acceptWithActor[Job, pl.edu.agh.api.WorkModel.Result] { implicit request =>
    WorkProducer.props
  }

  def workResultConsumerWebsocket() = WebSocket.acceptWithActor[Job, pl.edu.agh.api.WorkModel.Result] { implicit request =>
    WorkResultConsumer.props
  }
}