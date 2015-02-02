package actors.services.master

import akka.actor.{Actor, ActorRef, ActorSystem, Props}
import akka.contrib.pattern.ClusterSingletonProxy
import akka.pattern._
import akka.util.Timeout

import scala.concurrent.duration._


class Frontend extends Actor {

  import context.dispatcher
  import pl.edu.agh.api.MasterService._

  val masterProxy = context.actorOf(ClusterSingletonProxy.props(
    singletonPath = "/user/master/active",
    role = Some("backend")),
    name = "masterProxy")

  def receive = {
    case work =>
      implicit val timeout = Timeout(5.seconds)
      (masterProxy ? work) map {
        case Ack(_) => Ok
      } recover { case _ => NotOk} pipeTo sender()

  }

}

object Frontend extends {
  def startOn(system: ActorSystem): ActorRef = {
    system.actorOf(Props[Frontend], "frontend")
  }
}
