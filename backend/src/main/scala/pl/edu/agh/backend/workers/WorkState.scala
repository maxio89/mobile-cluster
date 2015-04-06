package pl.edu.agh.backend.workers

import pl.edu.agh.api.Work.Work

import scala.collection.immutable.Queue

object WorkState {

  def empty: WorkState = WorkState(
    pendingWork = Queue.empty,
    workInProgress = Map.empty,
    acceptedWorkIds = Set.empty,
    doneWorkIds = Set.empty)

  sealed trait WorkDomainEvent

  final case class WorkAccepted(work: Work) extends WorkDomainEvent

  final case class WorkStarted(id: String) extends WorkDomainEvent

  final case class WorkCompleted(id: String, result: Any) extends WorkDomainEvent

  final case class WorkInProgress(id: String, result: Any, population: Any) extends WorkDomainEvent

  final case class WorkerFailed(id: String) extends WorkDomainEvent

  final case class WorkerTimedOut(id: String) extends WorkDomainEvent

}

case class WorkState private(
                              private val pendingWork: Queue[Work],
                              private val workInProgress: Map[String, Work],
                              private val acceptedWorkIds: Set[String],
                              private val doneWorkIds: Set[String]) {

  import pl.edu.agh.backend.workers.WorkState._

  def hasWork: Boolean = pendingWork.nonEmpty

  def nextWork: Work = pendingWork.head

  def isAccepted(id: String): Boolean = acceptedWorkIds.contains(id)

  def isInProgress(id: String): Boolean = workInProgress.contains(id)

  def isDone(id: String): Boolean = doneWorkIds.contains(id)

  def updated(event: WorkDomainEvent): WorkState = event match {
    case WorkAccepted(work) ⇒
      copy(
        pendingWork = pendingWork enqueue work,
        acceptedWorkIds = acceptedWorkIds + work.id)

    case WorkStarted(id) ⇒
      val (work, rest) = pendingWork.dequeue
      require(id == work.id, s"WorkStarted expected id $id == ${work.id}")
      copy(
        pendingWork = rest,
        workInProgress = workInProgress + (id -> work))

    case WorkCompleted(id, result) ⇒
      copy(
        workInProgress = workInProgress - id,
        doneWorkIds = doneWorkIds + id)

    case WorkerFailed(id) ⇒
      copy(
        pendingWork = pendingWork enqueue workInProgress(id), //java.util.NoSuchElementException: key not found: 71a046e1-c8c3-4be8-b8df-8e99d012cf21
        workInProgress = workInProgress - id)

    case WorkerTimedOut(id) ⇒
      copy(
        pendingWork = pendingWork enqueue workInProgress(id),
        workInProgress = workInProgress - id)
  }

}
