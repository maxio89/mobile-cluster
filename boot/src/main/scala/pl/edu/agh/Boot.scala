package pl.edu.agh

import akka.actor.ActorSystem
import akka.kernel.Bootable
import pl.edu.agh.backend.factorial.FactorialBackend

class Boot extends Bootable {

  val system = ActorSystem("mobile-cluster")

  def startup() = {
    FactorialBackend startOn system
  }

  def shutdown() = {
    system.shutdown()
  }
}
