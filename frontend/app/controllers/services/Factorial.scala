package controllers.services

import actors.services.FactorialActor
import actors.services.FactorialActor._
import pl.edu.agh.api.FactorialService
import play.api.Play.current
import play.api.mvc._

object Factorial extends Controller {

  def websocket() = WebSocket.acceptWithActor[FactorialService.Compute, FactorialService.Result] { implicit request =>
    FactorialActor.props
  }
}