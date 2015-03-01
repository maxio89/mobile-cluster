package pl.edu.agh.backend.workers

import akka.actor._
import akka.contrib.pattern.{ClusterReceptionistExtension, ClusterSingletonManager, DistributedPubSubExtension, DistributedPubSubMediator}
import pl.edu.agh.api.Constants
import pl.edu.agh.api.MasterService._
import pl.edu.agh.api.Work._

import scala.concurrent.duration.{Deadline, FiniteDuration}

class Master(workTimeout: FiniteDuration) extends Actor with ActorLogging {

  import pl.edu.agh.backend.workers.Master._
  import pl.edu.agh.backend.workers.WorkState._

  import scala.concurrent.ExecutionContext.Implicits.global

  val mediator = DistributedPubSubExtension(context.system).mediator
  ClusterReceptionistExtension(context.system).registerService(self)
  val cleanupTask = context.system.scheduler.schedule(workTimeout / 2, workTimeout / 2,
    self, CleanupTick)
  // workers state is not event sourced
  private var workers = Map[String, WorkerState]()

  // workState is event sourced
  private var workState = WorkState.empty

  // persistenceId must include cluster role to support multiple masters
  //  override def persistenceId: String = Cluster(context.system).selfRoles.find(_.startsWith("backend-")) match {
  //    case Some(role) ⇒ role + "-master"
  //    case None ⇒ "master"
  //  }

  override def postStop() = cleanupTask.cancel()

  //  override def receiveRecover: Receive = {
  //    case event: WorkDomainEvent =>
  //      // only update current state by applying the event, no side effects
  //      workState = workState.updated(event)
  //      log.info("Replayed {}", event.getClass.getSimpleName)
  //  }

  def receive = {
    case MasterWorkerProtocol.RegisterWorker(workerId) =>
      if (workers.contains(workerId)) {
        workers += (workerId -> workers(workerId).copy(ref = sender()))
      } else {
        log.info("Worker registered: {}", workerId)
        workers += (workerId -> WorkerState(sender(), status = Idle))
        if (workState.hasWork)
          sender() ! MasterWorkerProtocol.WorkIsReady
      }

    case MasterWorkerProtocol.WorkerRequestsWork(workerId) =>
      if (workState.hasWork) {
        workers.get(workerId) match {
          case Some(s@WorkerState(_, Idle)) =>
            val work = workState.nextWork
            //            persist(WorkStarted(work.workId)) { event =>
            //              workState = workState.updated(event)
            workState = workState.updated(WorkStarted(work.id))
            log.info("Giving worker {} some work {}", workerId, work.id)
            workers += (workerId -> s.copy(status = Busy(work.id, Deadline.now + workTimeout)))
            sender() ! work
          //            }
          case _ =>
        }
      }

    case MasterWorkerProtocol.WorkIsDone(workerId, id, result) =>
      // idempotent
      if (workState.isDone(id)) {
        // previous Ack was lost, confirm again that this is done
        sender ! MasterWorkerProtocol.Ack(id)
      } else if (!workState.isInProgress(id)) {
        log.info("Work {} not in progress, reported as done by worker {}", id, workerId)
      } else {
        log.info("Work {} is done by worker {}", id, workerId)
        changeWorkerToIdle(workerId, id)
        //        persist(WorkCompleted(id, result)) { event ⇒
        //          workState = workState.updated(event)
        workState = workState.updated(WorkCompleted(id, result))
        mediator ! DistributedPubSubMediator.Publish(Constants.ResultsTopic, WorkResult(id, result))
        // Ack back to original sender
        sender ! MasterWorkerProtocol.Ack(id)
        //        }
      }

    case MasterWorkerProtocol.WorkInProgress(workerId, id, result) =>
      log.info("Work {} partially result was sent by worker {}", id, workerId)
      mediator ! DistributedPubSubMediator.Publish(Constants.ResultsTopic, WorkResult(id, result))
      workers.get(workerId) match {
        case Some(s@WorkerState(_, Busy(workId, timeout))) =>
          log.info("Extended timeout {} for work {}", workerId, workId)
          workers += (workerId -> s.copy(status = Busy(workId, Deadline.now + workTimeout)))
        //            }
        case _ =>
      }

    case MasterWorkerProtocol.WorkFailed(workerId, id) =>
      if (workState.isInProgress(id)) {
        log.info("Work {} failed by worker {}", id, workerId)
        changeWorkerToIdle(workerId, id)
        //        persist(WorkerFailed(id)) { event ⇒
        //          workState = workState.updated(event)
        workState = workState.updated(WorkerFailed(id))
        notifyWorkers()
        //        }
      }

    case work: Work =>
      // idempotent
      if (workState.isAccepted(work.id)) {
        sender() ! Ack(work.id)
      } else {
        log.info("Accepted work: {}", work.id)
        //        persist(WorkAccepted(work)) { event ⇒
        // Ack back to original sender
        sender() ! Ack(work.id)
        //          workState = workState.updated(event)
        workState = workState.updated(WorkAccepted(work))
        notifyWorkers()
        //        }
      }

    case CleanupTick =>
      for ((workerId, s@WorkerState(_, Busy(id, timeout))) ← workers) {
        if (timeout.isOverdue()) {
          log.info("Work timed out: {}", id)
          workers -= workerId
          //          persist(WorkerTimedOut(id)) { event ⇒
          //            workState = workState.updated(event)
          workState = workState.updated(WorkerTimedOut(id))
          notifyWorkers()
          //          }
        }
      }
  }

  def notifyWorkers() =
    if (workState.hasWork) {
      // could pick a few random instead of all
      log.info("notified workers")
      workers.foreach {
        case (_, WorkerState(ref, Idle)) => ref ! MasterWorkerProtocol.WorkIsReady
        case _ => // busy
      }
    }

  def changeWorkerToIdle(workerId: String, id: String) =
    workers.get(workerId) match {
      case Some(s@WorkerState(_, Busy(`id`, _))) ⇒
        workers += (workerId -> s.copy(status = Idle))
      case _ ⇒
      // ok, might happen after standby recovery, worker state is not persisted
    }


  // TODO cleanup old workers
  // TODO cleanup old workIds, doneWorkIds

}

object Master {

  import scala.concurrent.duration._

  val ResultsTopic = "results"

  def startOn(system: ActorSystem) {
    system.actorOf(ClusterSingletonManager.props(Master.props(workTimeout), "active",
      PoisonPill, Some("backend")), "master")
  }

  def workTimeout = 10.seconds

  def props(workTimeout: FiniteDuration): Props =
    Props(classOf[Master], workTimeout)

  private sealed trait WorkerStatus

  private case class WorkerState(ref: ActorRef, status: WorkerStatus)

  private case class Busy(id: String, deadline: Deadline) extends WorkerStatus

  private case object Idle extends WorkerStatus

  private case object CleanupTick

}