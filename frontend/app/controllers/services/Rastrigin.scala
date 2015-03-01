package controllers.services

import actors.services.ga.Frontend
import pl.edu.agh.api.RastriginWork.{RastriginConfig, RastriginResult}
import play.api.Play.current
import play.api.mvc._

object Rastrigin extends Controller {

  import actors.services.ga.JsonFormatters._

  def frontendWebsocket() = WebSocket.acceptWithActor[RastriginConfig, RastriginResult] { implicit request =>
    Frontend.props
  }

}