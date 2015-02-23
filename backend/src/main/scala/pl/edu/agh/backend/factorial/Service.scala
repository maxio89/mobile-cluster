package pl.edu.agh.backend.factorial

import akka.actor._
import akka.pattern.pipe
import pl.edu.agh.api.FactorialService._

import scala.annotation.tailrec
import scala.concurrent.Future

/**
 * Doing the calculation
 */
class WorkerActor extends Actor with ActorLogging {

  import context.dispatcher

  def receive =
  {
    case Compute(n: Int) => Future(factorial(n)) map {
      Result
    } pipeTo sender()
  }

  def factorial(n: Int): BigInt =
  {
    @tailrec def factorialAcc(acc: BigInt, n: Int): BigInt =
    {
      if (n <= 1) {
        acc
      } else {
        log.info(s"I've got accc: $acc")
        factorialAcc(acc * n, n - 1)
      }
    }
    factorialAcc(BigInt(1), n)
  }
}

/**
 * Bootup the factorial service and the associated worker actors
 */
object FactorialBackend {

  def startOn(system: ActorSystem)
  {
    system.actorOf(Props[WorkerActor], name = "factorialBackend")
  }
}