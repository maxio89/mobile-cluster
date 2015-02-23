package pl.edu.agh.backend.ga

import akka.actor.{Actor, ActorLogging}
import pl.edu.agh.api.WorkModel.{Job, Result}

import scala.util.Random

class WorkExecutor extends Actor with ActorLogging {

  val POPULATION_LIMIT = 150
  private val score = (chr: Chromosome[Gene]) => {
    chr.unfitness = Rastrigin.value(chr.code)
  }

  def receive = {
    case job: Job =>

      log.info("Evaluation")

      val config = Config(0.5, 0.5, job.cycles)
      val evaluation = Evaluation[Gene](score)
      val population = createRandomPopulation(job.n, job.size)

      Range(0, job.cycles).foreach(i =>
        evaluation.mate(population, config, i)
      )
      population.fittest match {
        case Some(fittest) => sender() ! Worker.WorkComplete(Result(job.cycles, Rastrigin.value(fittest.code)))
        case None => log.info("No result!")
      }


    case _ =>
      log.info("haha")
  }

  def createRandomPopulation(n: Int, size: Int): Population[Gene] = {
    var chromosomes: List[Chromosome[Gene]] = List()
    Range(0, size).foreach(i => {
      val num = Random.nextDouble()
      var genes: List[Gene] = List()
      Range(0, n).foreach(i =>
        genes = genes :+ Gene(num.toString, num.toDouble)
      )
      val chromosome: Chromosome[Gene] = Chromosome[Gene](genes)
      chromosomes = chromosomes :+ chromosome
    }
    )
    Population[Gene](POPULATION_LIMIT, chromosomes)
  }

  def createPopulation(n: Int, size: Int): Population[Gene] = {
    var chromosomes: List[Chromosome[Gene]] = List()
    Range(0, size).foreach(i => {
      val num = i - (0.5 * size)
      var genes: List[Gene] = List()
      Range(0, n).foreach(i =>
        genes = genes :+ Gene(num.toString, num.toDouble)
      )
      val chromosome: Chromosome[Gene] = Chromosome[Gene](genes)
      chromosomes = chromosomes :+ chromosome
    }
    )
    Population[Gene](POPULATION_LIMIT, chromosomes)
  }

  object Rastrigin {
    def value(x: List[Gene]) = 10 * x.size + x.map(x => (x.geneValue * x.geneValue) - 10 * math.cos(2 * math.Pi * x.geneValue)).sum
  }

}