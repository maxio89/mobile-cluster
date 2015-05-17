package pl.edu.agh.backend.workers.rastrigin

import akka.actor.{Actor, ActorLogging}
import com.typesafe.config.ConfigFactory
import pl.edu.agh.api.RastriginWork._
import pl.edu.agh.backend.ga.example.rastrigin.{FunctionOptimization, Point, Variables}
import pl.edu.agh.backend.ga.strategy.{Chromosome, Population}
import pl.edu.agh.backend.workers.Worker

import scala.util.Random

class WorkExecutor(workerId: String) extends Actor with ActorLogging {

  val POPULATION_LIMIT = 150
  val hostname: String = ConfigFactory.load().getString("akka.remote.netty.tcp.hostname")
  private val score = (chr: Chromosome[pl.edu.agh.backend.ga.example.rastrigin.Number]) => {
    chr.unfitness = Rastrigin.value(chr.code)
  }
  var immigrants: Population[pl.edu.agh.backend.ga.example.rastrigin.Number] = Variables(POPULATION_LIMIT, List())
  var population: Population[pl.edu.agh.backend.ga.example.rastrigin.Number] = null
  /**
   * (Start time, Maximum cycles number)
   */
  var start = (0L, 0)

  def receive = {
    case config: RastriginConfig =>
      log.info("Evaluation")

      if (start._2 == 0) {
        population = createRandomPopulation(config)
        start = (System.currentTimeMillis, config.maxCycles)
      }

      if (!immigrants.isEmpty) {
        val size = immigrants.size
        population = population + immigrants
        log.info(s"Added $size immigrants")
        immigrants = Variables(config.maxSize, List())
      }

      val evaluation = FunctionOptimization(score)
      var maxCycles = getNumberOfCycles(config.migrationFreq, config.maxCycles)
      Range(0, maxCycles).foreach(i => {
        evaluation.mate(population, config, i)
      })

      population.fittest match {
        case Some(fittest) =>

          maxCycles = config.maxCycles - maxCycles
          val cycles = start._2 - maxCycles
          //TODO think about frequency of migration and partially result sending
          if (maxCycles > 0) {
            val n = (config.migrationFactor * config.maxSize) / 100
            sender() ! Worker.PartiallyResult(RastriginResult(workerId, hostname, System.currentTimeMillis - start._1, cycles, Rastrigin.value(fittest.code), numbersToDoubleList(fittest.code)), population,
              RastriginConfig(config.n, config.initialSize, config.maxSize, config.xover, config.mu, maxCycles, config.snapshotFreq, config.migrationFreq, config.migrationFactor))
            log.info("Sent partially results!")
            if (n > 0) {
              population.fittest(n) match {
                case Some(emigrants) =>
                  sender() ! Worker.MigrationRequest(emigrants)
              }
            }
          } else {
            sender() ! Worker.WorkComplete(RastriginResult(workerId, hostname, System.currentTimeMillis - start._1, cycles, Rastrigin.value(fittest.code), numbersToDoubleList(fittest.code)))
            start = (0L, 0)
            population = null
            immigrants = Variables(POPULATION_LIMIT, List())
            log.info("Sent results!")
          }

        case None => log.info("No result!")
      }

    case Worker.Migration(arg) =>
      arg match {
        case newPopulation: Population[pl.edu.agh.backend.ga.example.rastrigin.Number] =>
          if (self.compareTo(sender()) != 0) {
            //TODO It doesn't work, immigrants are still added when they are coming from the same worker
            immigrants = newPopulation + immigrants
            val size = newPopulation.size
            val totalSize = immigrants.size
            val path = sender().path
            log.info(s"$size immigrants arrived from $path, now is $totalSize!")
          }
      }
    case _ =>
      log.info("haha")
  }

  def getNumberOfCycles(migrationFreq: Int, maxCycles: Int): Int =
    if (maxCycles > migrationFreq)
      migrationFreq
    else
      maxCycles

  def numbersToDoubleList(numbers: List[pl.edu.agh.backend.ga.example.rastrigin.Number]): List[Double] = {
    numbers map {
      number => number.target
    }
  }

  def createRandomPopulation(config: RastriginConfig): Population[pl.edu.agh.backend.ga.example.rastrigin.Number] = {
    var points: List[Point] = List()
    Range(0, config.initialSize).foreach(i => {
      var numbers: List[pl.edu.agh.backend.ga.example.rastrigin.Number] = List()
      Range(0, config.n).foreach(i => {
        var num = Random.nextDouble()
        //Rastrigin function range [-5.12, 5.12]
        if (Random.nextBoolean())
          num = num - 5.12
        else
          num = num + 4.12
        numbers = numbers :+ pl.edu.agh.backend.ga.example.rastrigin.Number(num.toString, num)
      }
      )
      val point: Point = Point(numbers)
      points = points :+ point
    }
    )
    Variables(config.maxSize, points)
  }

  object Rastrigin {
    def value(x: List[pl.edu.agh.backend.ga.example.rastrigin.Number]): Double = {
      10 * x.size + x.map(x => (x.target * x.target) - 10 * math.cos(2 * math.Pi * x.target)).sum
    }
  }

}