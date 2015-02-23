package actors.services.ga

import akka.actor._
import akka.contrib.pattern.{DistributedPubSubExtension, DistributedPubSubMediator}
import pl.edu.agh.api.Constants
import pl.edu.agh.api.WorkModel.WorkResult

class WorkResultConsumer(out: ActorRef) extends Actor with ActorLogging {

  val mediator = DistributedPubSubExtension(context.system).mediator
  mediator ! DistributedPubSubMediator.Subscribe(Constants.ResultsTopic, self)

  def receive = {
    case _: DistributedPubSubMediator.SubscribeAck =>
      log.info("Subscribed results topic")
    case workResult: WorkResult =>
      log.info(s"Consumed result: ${workResult.result.cycles} - ${workResult.result.value}")
      out ! workResult.result
    case _ =>
      log.info("Received something")
  }

}

object WorkResultConsumer extends {


  def startOn(system: ActorSystem): Unit = {
    system.actorOf(Props[WorkResultConsumer], "consumer")
  }

  def props(out: ActorRef) = Props(classOf[WorkResultConsumer], out)

}