package actors.services.ga

import java.util.UUID

import akka.actor.{Actor, ActorRef, ActorSystem, Props, _}
import akka.contrib.pattern.{ClusterSingletonProxy, DistributedPubSubExtension, DistributedPubSubMediator}
import akka.pattern._
import akka.util.Timeout
import pl.edu.agh.api.Constants
import pl.edu.agh.api.Work.{Config, Work, WorkResult}

import scala.concurrent.duration._


class Frontend(out: ActorRef) extends Actor with ActorLogging {

  import context.dispatcher
  import pl.edu.agh.api.MasterService._

  val masterProxy = context.actorOf(ClusterSingletonProxy.props(
    singletonPath = "/user/master/active",
    role = Some("backend")),
    name = "masterProxy")

  val mediator = DistributedPubSubExtension(context.system).mediator
  mediator ! DistributedPubSubMediator.Subscribe(Constants.ResultsTopic, self)

  def receive = {
    case config: Config =>
      implicit val timeout = Timeout(5.seconds)
      (masterProxy ? Work(nextWorkId(), config)) map {
        case Ack(_) => Ok
      } recover { case _ => NotOk} pipeTo sender()
    //      masterProxy ! Work(nextWorkId(), job)
    case _: DistributedPubSubMediator.SubscribeAck =>
      log.info("Subscribed results topic")
    case workResult: WorkResult =>
      log.info(s"Consumed result: ${workResult.result}" +
        s"")
      //      log.info(s"Consumed result: ${workResult.result.cycles} - ${workResult.result.value}")
      out ! workResult.result
    case _ =>
      log.info("Received something")
  }

  def nextWorkId(): String = UUID.randomUUID().toString

}

object Frontend extends {

  def startOn(system: ActorSystem): ActorRef = {
    system.actorOf(Props[Frontend], "frontend")
  }

  def props(out: ActorRef) = Props(classOf[Frontend], out)

}
