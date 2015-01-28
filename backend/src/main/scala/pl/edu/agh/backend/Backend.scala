package pl.edu.agh.backend

import akka.actor._
import com.typesafe.config.ConfigFactory
import pl.edu.agh.backend.factorial.FactorialBackend

import scala.collection.JavaConversions._

/**
 * Booting a cluster backend node with all actors
 */
object Backend extends App {

  // Simple cli parsing
  val port = args match {
    case Array() => "0"
    case Array(_port) => _port
    case args => throw new IllegalArgumentException(s"only ports. Args [ $args ] are invalid")
  }

  // System initialization
  val properties = Map("akka.remote.netty.tcp.port" -> port)

  val system = ActorSystem("mobile-cluster", (ConfigFactory parseMap properties).withFallback(ConfigFactory.load()))

  // Deploy actors and services
  FactorialBackend startOn system
}
