package pl.edu.agh.backend.workers

import java.util.UUID

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import akka.contrib.pattern._
import pl.edu.agh.api.Constants
import pl.edu.agh.api.RastriginWork.RastriginConfig
import pl.edu.agh.api.Work.Result
import pl.edu.agh.backend.workers.rastrigin.WorkExecutor

import scala.concurrent.duration._

class Worker(registerInterval: FiniteDuration)
  extends Actor with ActorLogging {

  import pl.edu.agh.api.Work._
  import pl.edu.agh.backend.workers.MasterWorkerProtocol._
  import pl.edu.agh.backend.workers.Worker._

  val workerId = UUID.randomUUID().toString

  import context.dispatcher

  val masterProxy = context.actorOf(ClusterSingletonProxy.props(
    singletonPath = Constants.SingletonPath,
    role = Some(Constants.BackendRole)),
    name = Constants.MasterProxyName)

  val registerTask = context.system.scheduler.schedule(0.seconds, registerInterval, masterProxy,
    RegisterWorker(workerId))

  val workExecutor = context.watch(context.actorOf(Props(classOf[WorkExecutor], workerId), "exec"))
  val mediator = DistributedPubSubExtension(context.system).mediator
  mediator ! DistributedPubSubMediator.Put(self)

  var currentWorkId: Option[String] = None

  def workId: String = currentWorkId match {
    case Some(workId) => workId
    case None => throw new IllegalStateException("Not working")
  }

  override def supervisorStrategy = OneForOneStrategy() {
    case _: ActorInitializationException =>
      log.info("Stopped")
      Stop
    case _: DeathPactException =>
      log.info("DeathPact stopped")
      Stop
    case _: Exception =>
      currentWorkId foreach { workId => sendToMaster(WorkFailed(workerId, workId)) }
      context.become(idle)
      log.info("Restarted")
      Restart
  }

  override def postStop() =
    log.info("Register canceled")

  registerTask.cancel()

  def receive = idle

  def idle: Receive = {
    case WorkIsReady =>
      sendToMaster(WorkerRequestsWork(workerId))
    case Work(workId, config) =>
      log.info("Got work: {}", config)
      currentWorkId = Some(workId)
      workExecutor ! config
      context.become(working)
    case Migration(senderWorkerId, population) =>
      log.info(s"Ignore immigrants from $senderWorkerId, because I'm not working.")
    case _: DistributedPubSubMediator.SubscribeAck =>
      log.info("Subscribed results topic")
    case _ =>
      log.info("idle unhandled")
  }

  def working: Receive = {
    case WorkComplete(result) =>
      log.info("Work is complete. Result {}.", result)
      sendToMaster(WorkIsDone(workerId, workId, result))
      context.setReceiveTimeout(30.seconds)
      context.become(waitForWorkIsDoneAck(result))
    case PartiallyResult(result, population, config) =>
      log.info("Work is partially done. Result {}.", result)
      sendToMaster(WorkInProgress(workerId, workId, result, population))
      workExecutor ! config
    case MigrationRequest(population) =>
      mediator ! DistributedPubSubMediator.Send(Constants.MigrationPath, Migration(workerId, population), localAffinity = false)
      log.info(s"Published migration request for $workerId!")
    case Migration(senderWorkerId, population) =>
      workExecutor ! Migration(senderWorkerId, population)
      log.info(s"Migration received from $senderWorkerId!")
    case _: DistributedPubSubMediator.SubscribeAck =>
      log.info("Subscribed migration topic")
    case _: Work =>
      log.info("Yikes. Master told me to do work, while I'm working.")
    case _ =>
      log.info("working unhandled")

  }

  def waitForWorkIsDoneAck(result: Result): Receive = {
    case Ack(id) if id == workId =>
      sendToMaster(WorkerRequestsWork(workerId))
      context.setReceiveTimeout(Duration.Undefined)
      context.become(idle)
    case ReceiveTimeout =>
      log.info("No ack from master, retrying")
      sendToMaster(WorkIsDone(workerId, workId, result))
    case _ =>
      log.info("waitForWorkIsDoneAck unhandled")
  }

  override def unhandled(message: Any) = message match {
    case Terminated(`workExecutor`) =>
      log.info("stop self")
      context.stop(self)
    case WorkIsReady => log.info("WorkIsReady")
    case _ =>
      log.info("Unhandled message")
      super.unhandled(message)
  }

  def sendToMaster(msg: Any) = {
    masterProxy ! msg
  }

}

object Worker {

  def startOn(system: ActorSystem) = {
    system.actorOf(Worker.props(), "worker")
  }

  def props(registerInterval: FiniteDuration = 60.seconds): Props =
    Props(classOf[Worker], registerInterval)

  case class WorkComplete(result: Result)

  case class PartiallyResult(result: Result, population: Any, config: RastriginConfig)

  case class MigrationRequest(population: Any)

  case class Migration(workerId: String, population: Any)

}