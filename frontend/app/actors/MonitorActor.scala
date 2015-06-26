package actors

import akka.actor._
import akka.cluster.ClusterEvent._
import akka.cluster._
import models.Member._
import play.api.libs.json.Json._
import play.api.libs.json._

import scala.collection.mutable.ArrayBuffer

/**
 * Created by the Play Framework for a websocket connection.
 * Listens to MemberEvents and pushes them to the websocket.
 *
 * @param out - the websocket to which we can push messages
 */
class MonitorActor(out: ActorRef) extends Actor with ActorLogging {

  val cluster = Cluster(context.system)
  private val members: ArrayBuffer[Member] = new ArrayBuffer()

  // subscribe to cluster changes, re-subscribe when restart
  override def preStart(): Unit = {
    cluster.subscribe(self, initialStateMode = InitialStateAsEvents,
      classOf[MemberEvent], classOf[UnreachableMember])
  }

  // clean up on shutdown
  override def postStop(): Unit = cluster unsubscribe self

  // handle the member events
  def receive = {
    case MemberUp(member) => handleMemberUp(member)
    case UnreachableMember(member) => handleUnreachable(member)
    case MemberRemoved(member, previousStatus) => handleRemoved(member, previousStatus)
    case MemberExited(member) => handleExit(member)
    case _: MemberEvent => // ignore
  }

  def handleMemberUp(member: Member) {
    if (member.hasRole("backend")) {
      members += member
      if (isOldest(member)) {
        implicit val masterMarkerWrites = Json.writes[MasterMarker]
        out ! (Json.obj("state" -> "up") ++ toJson(MasterMarker(member)).as[JsObject])
        return
      }
    }
    out ! (Json.obj("state" -> "up") ++ toJson(member).as[JsObject])
  }

  def isOldest(newMember: Member): Boolean = {
    if (members.size == 1 && members.contains(newMember)) return true
    for (member <- members) if (!newMember.isOlderThan(member)) return false
    true
  }

  def handleUnreachable(member: Member) {
    out ! (Json.obj("state" -> "unreachable") ++ toJson(member).as[JsObject])
  }

  def handleRemoved(member: Member, previousStatus: MemberStatus) {
    if (member.hasRole("master")) {
      members -= member
    }
    out ! (Json.obj("state" -> "removed") ++ toJson(member).as[JsObject])
  }

  def handleExit(member: Member) {
    if (member.hasRole("master")) {
      members -= member
    }
    out ! (Json.obj("state" -> "exit") ++ toJson(member).as[JsObject])
  }

  case class MasterMarker(member: Member)

}


object MonitorActor {

  /**
   * Definition for the controller to create the websocket
   */
  def props(out: ActorRef) = Props(new MonitorActor(out))
}