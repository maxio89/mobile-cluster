package controllers

import actors._
import models.Metrics
import play.api.Play.current
import play.api.libs.json.JsValue
import play.api.mvc._

object Cluster extends Controller {

  def clusterNodesWebsocket = WebSocket.acceptWithActor[JsValue, JsValue] { implicit request =>
    MonitorActor.props
  }

  def clusterMetricsWebsocket = WebSocket.acceptWithActor[JsValue, Metrics.NodeMetric] { implicit request =>
    MetricsActor.props
  }
}