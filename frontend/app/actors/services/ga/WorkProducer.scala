package actors.services.ga

import java.util.UUID

import akka.actor._
import pl.edu.agh.api.WorkModel.{Job, Work}

import scala.concurrent.forkjoin.ThreadLocalRandom


class WorkProducer extends Actor with ActorLogging {

  import pl.edu.agh.api.MasterService._

  val frontend: ActorSelection = {
    val path = context.system / "frontend"
    context actorSelection path
  }
  var n = 0

//  override def preStart(): Unit =
  //      self ! Tick
//    scheduler.scheduleOnce(5.seconds, self, Tick)

  //  def scheduler = context.system.scheduler

  // override postRestart so we don't call preStart and schedule a new Tick
  override def postRestart(reason: Throwable): Unit = ()

  def receive = {
    case job: Job =>
      n += 1
      log.info("Produced work: {}", n)
          val work = Work(nextWorkId(), job)
          frontend ! work
          context.become(waitAccepted(work), discardOld = false)
  }

  def nextWorkId(): String = UUID.randomUUID().toString

  def waitAccepted(work: Work): Actor.Receive = {
    case Ok =>
      context.unbecome()
    //      scheduler.scheduleOnce(rnd.nextInt(3, 10).seconds, self, Tick)
    case NotOk =>
      log.info("Work not accepted, retry after a while")
    //      scheduler.scheduleOnce(3.seconds, frontend, work)
  }

  def rnd = ThreadLocalRandom.current

}

object WorkProducer extends {

  import actors.services.ga.JsonFormatters._

  def startOn(system: ActorSystem, frontend: ActorRef): Unit = {
    system.actorOf(Props(classOf[WorkProducer], frontend))
  }

  def props(out: ActorRef) = Props(classOf[WorkProducer])

}