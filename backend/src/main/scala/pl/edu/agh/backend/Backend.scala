package pl.edu.agh.backend

import akka.actor._
import com.typesafe.config.ConfigFactory
import pl.edu.agh.backend.factorial.FactorialBackend

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
  FactorialBackend startOn system
}
