package controllers.services.ga

import actors.services.ga.FrontendActor
import pl.edu.agh.api.RastriginWork.{RastriginResult, RastriginConfig}
import play.api.Play.current
import play.api.mvc._

object Rastrigin extends Controller {

  import actors.services.ga.JsonFormatters._

  def frontendWebsocket() = WebSocket.acceptWithActor[RastriginConfig, RastriginResult] { implicit request =>
    FrontendActor.props
  }

}