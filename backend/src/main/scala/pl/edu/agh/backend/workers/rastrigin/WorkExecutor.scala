package pl.edu.agh.backend.workers.rastrigin

import akka.actor.{Actor, ActorLogging}
import pl.edu.agh.api.RastriginWork._
import pl.edu.agh.backend.ga.example.rastrigin.{FunctionOptimization, Point, Variables}
import pl.edu.agh.backend.ga.strategy.{Chromosome, Population}
import pl.edu.agh.backend.workers.Worker

import scala.util.Random

class WorkExecutor extends Actor with ActorLogging {

  val POPULATION_LIMIT = 150
  private val score = (chr: Chromosome[pl.edu.agh.backend.ga.example.rastrigin.Number]) => {
    chr.unfitness = Rastrigin.value(chr.code)
  }

  def receive = {
    case config: RastriginConfig =>

      log.info("Evaluation")

      val evaluation = FunctionOptimization(score)
      val population = createRandomPopulation(config.n, config.initialSize)

      Range(0, config.maxCycles).foreach(i => {
        evaluation.mate(population, config, i)
        if (i % config.snapshotFreq == 0) {
          population.fittest match {
            case Some(fittest) => sender() ! Worker.PartiallyResult(RastriginResult(i, Rastrigin.value(fittest.code), numbersToDoubleList(fittest.code)))
            case None => log.info("No partially result!")
          }
        }
      }
      )
      population.fittest match {
        case Some(fittest) => sender() ! Worker.WorkComplete(RastriginResult(config.maxCycles, Rastrigin.value(fittest.code), numbersToDoubleList(fittest.code)))
        case None => log.info("No result!")
      }
    case _ =>
      log.info("haha")
  }

  def numbersToDoubleList(numbers: List[pl.edu.agh.backend.ga.example.rastrigin.Number]): List[Double] = {
    numbers map {
      number => number.target
    }
  }

  def createRandomPopulation(n: Int, size: Int): Population[pl.edu.agh.backend.ga.example.rastrigin.Number] = {
    var points: List[Point] = List()
    Range(0, size).foreach(i => {
      val num = Random.nextDouble()
      var numbers: List[pl.edu.agh.backend.ga.example.rastrigin.Number] = List()
      Range(0, n).foreach(i =>
        numbers = numbers :+ pl.edu.agh.backend.ga.example.rastrigin.Number(num.toString, num.toDouble)
      )
      val point: Point = Point(numbers)
      points = points :+ point
    }
    )
    Variables(POPULATION_LIMIT, points)
  }

  object Rastrigin {
    def value(x: List[pl.edu.agh.backend.ga.example.rastrigin.Number]) = 10 * x.size + x.map(x => (x.geneValue * x.geneValue) - 10 * math.cos(2 * math.Pi * x.geneValue)).sum
  }

}