package controllers.services

import actors.services.ga.Frontend
import pl.edu.agh.api.RastriginWork.{RastriginResult, RastriginConfig}
import play.api.mvc._
import play.api.Play.current

object Rastrigin extends Controller {
  import actors.services.ga.JsonFormatters._

  def frontendWebsocket() = WebSocket.acceptWithActor[RastriginConfig, RastriginResult] { implicit request =>
    Frontend.props
  }

}