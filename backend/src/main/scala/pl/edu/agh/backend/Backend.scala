package pl.edu.agh.backend

import akka.actor.{ActorSystem, AddressFromURIString, RootActorPath}
import akka.japi.Util.immutableSeq
import com.typesafe.config.ConfigFactory
import pl.edu.agh.backend.factorial.FactorialBackend
import pl.edu.agh.backend.persistence.SharedJournal
import pl.edu.agh.backend.ga.{Master, Worker}

import scala.collection.JavaConversions._
/**
 * Booting a cluster backend node with all actors
 */
object Backend extends App {

  val port = args match {
    case Array() => Nil
    case Array(_port) => _port
    case args => throw new IllegalArgumentException(s"only ports. Args [ $args ] are invalid")
  }

  val properties = Map("akka.remote.netty.tcp.port" -> port)

  val system = port match {
    case Nil => ActorSystem("application")
    case _ => ActorSystem("application", (ConfigFactory parseMap properties).withFallback(ConfigFactory.load()))
  }

  // Deploy actors and services

  SharedJournal startOn system

  FactorialBackend startOn system

  Master startOn system

  val workerConf = ConfigFactory.load("worker")

  val workerSystem = ActorSystem("applicationWorker", workerConf)

  val initialContacts = immutableSeq(workerConf.getStringList("contact-points")).map {
    case AddressFromURIString(addr) ⇒ workerSystem.actorSelection(RootActorPath(addr) / "user" / "receptionist")
  }.toSet

  Worker.startOn(workerSystem, initialContacts)

}
