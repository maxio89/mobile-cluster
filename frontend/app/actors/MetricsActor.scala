package actors

import akka.actor._
import akka.cluster.ClusterEvent._
import akka.cluster.StandardMetrics.{Cpu, HeapMemory}
import akka.cluster.{Cluster, NodeMetrics}

/**
 * Created by the Play Framework for a websocket connection.
 * Listens to ClusterMetricsChanged events and pushes them to the websocket.
 *
 * @param out - the websocket to which we can push messages
 */
class MetricsActor(out: ActorRef) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)

  // subscribe to ClusterMetricsChanged
  override def preStart() = cluster subscribe(self, classOf[ClusterMetricsChanged])

  // clean up
  override def postStop() = cluster unsubscribe self

  // handle the events
  def receive = {
    case ClusterMetricsChanged(metrics) => metrics foreach handleMetrics
    case state: CurrentClusterState => // ignore
  }

  def handleMetrics(metrics: NodeMetrics) = {
    pushHeap(metrics)
    pushCpu(metrics)
  }

  def pushHeap(nodeMetrics: NodeMetrics): Unit = nodeMetrics match {
    case HeapMemory(address, timestamp, used, committed, max) =>
      out ! models.Metrics.HeapMemory(address, timestamp, used, committed, max)
    case _ => // no heap info
  }

  def pushCpu(nodeMetrics: NodeMetrics): Unit = nodeMetrics match {
    case Cpu(address, timestamp, systemLoadAverage, cpuCombined, processors) =>
      out ! models.Metrics.Cpu(address, timestamp, systemLoadAverage, cpuCombined, processors)
    case _ => // no cpu info
  }
}

object MetricsActor {

  /**
   * actor definition
   */
  def props(out: ActorRef) = Props(new MetricsActor(out))
}