package actors.services.ga

import java.util.UUID

import akka.actor.{Actor, ActorRef, Props, _}
import akka.contrib.pattern.{ClusterSingletonProxy, DistributedPubSubExtension, DistributedPubSubMediator}
import akka.pattern._
import akka.util.Timeout
import pl.edu.agh.api.Constants
import pl.edu.agh.api.Work.{Config, Work, WorkResult}

import scala.concurrent.duration._


class FrontendActor(out: ActorRef) extends Actor with ActorLogging {

  import context.dispatcher
  import pl.edu.agh.api.MasterService._

  val masterProxy = context.actorOf(ClusterSingletonProxy.props(
    singletonPath = Constants.SingletonPath,
    role = Some(Constants.BackendRole)),
    name = Constants.MasterProxyName)

  val mediator = DistributedPubSubExtension(context.system).mediator
  mediator ! DistributedPubSubMediator.Subscribe(Constants.ResultsTopic, self)

  def receive = {
    case config: Config =>
      implicit val timeout = Timeout(5.seconds)
      (masterProxy ? Work(nextWorkId(), config)) map {
        case Ack(_) => Ok
      } recover { case _ => NotOk } pipeTo sender()
    case _: DistributedPubSubMediator.SubscribeAck =>
      log.info("Subscribed results topic")
    case workResult: WorkResult =>
      log.info(s"Consumed result: ${workResult.result}" + s"")
      out ! workResult.result
    case _ =>
      log.info("Received something")
  }

  def nextWorkId(): String = UUID.randomUUID().toString

}

object FrontendActor extends {

  def props(out: ActorRef) = Props(classOf[FrontendActor], out)

}
