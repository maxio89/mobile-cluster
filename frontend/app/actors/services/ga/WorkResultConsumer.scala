package actors.services.ga

import akka.actor.{ActorSystem, Props, Actor, ActorLogging}
import akka.contrib.pattern.{DistributedPubSubExtension, DistributedPubSubMediator}
import pl.edu.agh.api.Constants
import pl.edu.agh.api.WorkModel.WorkResult

class WorkResultConsumer extends Actor with ActorLogging {

  val mediator = DistributedPubSubExtension(context.system).mediator
  mediator ! DistributedPubSubMediator.Subscribe(Constants.ResultsTopic, self)

  def receive = {
    case _: DistributedPubSubMediator.SubscribeAck =>
    case WorkResult(workId, result) =>
      log.info("Consumed result: {}", result)
  }

}

object WorkResultConsumer extends {
  def startOn(system: ActorSystem): Unit = {
    system.actorOf(Props[WorkResultConsumer], "consumer")
  }
}