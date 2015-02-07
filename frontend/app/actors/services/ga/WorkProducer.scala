package actors.services.ga

import java.util.UUID

import akka.actor._
import pl.edu.agh.api.WorkModel.Work

import scala.concurrent.duration._
import scala.concurrent.forkjoin.ThreadLocalRandom


class WorkProducer(frontend: ActorRef) extends Actor with ActorLogging {

  import context.dispatcher
  import pl.edu.agh.api.MasterService._


  var n = 0

//  override def preStart(): Unit =
//    scheduler.scheduleOnce(5.seconds, self, Tick)

  def scheduler = context.system.scheduler

  // override postRestart so we don't call preStart and schedule a new Tick
  override def postRestart(reason: Throwable): Unit = ()

  def receive = {
    case Tick =>
      n += 1
      log.info("Produced work: {}", n)
      val work = Work(nextWorkId(), n)
      frontend ! work
      context.become(waitAccepted(work), discardOld = false)

  }

  def nextWorkId(): String = UUID.randomUUID().toString

  def waitAccepted(work: Work): Actor.Receive = {
    case Ok =>
      context.unbecome()
      scheduler.scheduleOnce(rnd.nextInt(3, 10).seconds, self, Tick)
    case NotOk =>
      log.info("Work not accepted, retry after a while")
      scheduler.scheduleOnce(3.seconds, frontend, work)
  }

  def rnd = ThreadLocalRandom.current

}

object WorkProducer extends {
  def startOn(system: ActorSystem, frontend: ActorRef): Unit = {
    system.actorOf(Props(classOf[WorkProducer], frontend), "producer")
  }
}