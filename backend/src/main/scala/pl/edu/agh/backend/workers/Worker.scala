package pl.edu.agh.backend.workers

import java.util.UUID

import akka.actor.SupervisorStrategy.{Restart, Stop}
import akka.actor._
import akka.contrib.pattern.ClusterClient
import akka.contrib.pattern.ClusterClient.SendToAll
import pl.edu.agh.api.Work.Result
import pl.edu.agh.backend.workers.rastrigin.WorkExecutor

import scala.concurrent.duration._

class Worker(clusterClient: ActorRef, registerInterval: FiniteDuration)
  extends Actor with ActorLogging {

  import pl.edu.agh.api.Work._
  import pl.edu.agh.backend.workers.MasterWorkerProtocol._
  import pl.edu.agh.backend.workers.Worker._

  val workerId = UUID.randomUUID().toString

  import context.dispatcher

  val registerTask = context.system.scheduler.schedule(0.seconds, registerInterval, clusterClient,
    SendToAll("/user/master/active", RegisterWorker(workerId)))

  val workExecutor = context.watch(context.actorOf(Props(classOf[WorkExecutor], workerId), "exec"))

  var currentWorkId: Option[String] = None

  def workId: String = currentWorkId match {
    case Some(workId) => workId
    case None => throw new IllegalStateException("Not working")
  }

  override def supervisorStrategy = OneForOneStrategy() {
    case _: ActorInitializationException => Stop
    case _: DeathPactException => Stop
    case _: Exception =>
      currentWorkId foreach { workId => sendToMaster(WorkFailed(workerId, workId))}
      context.become(idle)
      Restart
  }

  override def postStop() = registerTask.cancel()

  def receive = idle

  def idle: Receive = {
    case WorkIsReady =>
      sendToMaster(WorkerRequestsWork(workerId))

    case Work(workId, config) =>
      log.info("Got work: {}", config)
      currentWorkId = Some(workId)
      workExecutor ! config
      context.become(working)
  }

  def working: Receive = {
    case WorkComplete(result) =>
      log.info("Work is complete. Result {}.", result)
      sendToMaster(WorkIsDone(workerId, workId, result))
      context.setReceiveTimeout(10.seconds)
      context.become(waitForWorkIsDoneAck(result))
    case PartiallyResult(result) =>
      log.info("Work is partially done. Result {}.", result)
      sendToMaster(WorkInProgress(workerId, workId, result))

    case _: Work =>
      log.info("Yikes. Master told me to do work, while I'm working.")
  }

  def waitForWorkIsDoneAck(result: Result): Receive = {
    case Ack(id) if id == workId =>
      sendToMaster(WorkerRequestsWork(workerId))
      context.setReceiveTimeout(Duration.Undefined)
      context.become(idle)
    case ReceiveTimeout =>
      log.info("No ack from master, retrying")
      sendToMaster(WorkIsDone(workerId, workId, result))
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
    clusterClient ! SendToAll("/user/master/active", msg)
  }

}

object Worker {

  def startOn(system: ActorSystem, initialContacts: Set[ActorSelection]) = {
    val clusterClient = system.actorOf(ClusterClient.props(initialContacts), "clusterClient")
    system.actorOf(Worker.props(clusterClient), "worker")
  }

  def props(clusterClient: ActorRef, registerInterval: FiniteDuration = 10.seconds): Props =
    Props(classOf[Worker], clusterClient, registerInterval)

  case class WorkComplete(result: Result)

  case class PartiallyResult(result: Result)

}