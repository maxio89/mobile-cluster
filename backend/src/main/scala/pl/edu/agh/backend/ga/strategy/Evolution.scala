package pl.edu.agh.backend.ga.strategy

import org.slf4j.LoggerFactory
import pl.edu.agh.api.Work._

import scala.util.Random

@SerialVersionUID(0L)
abstract class Evolution[T <: Gene](score: Chromosome[T] => Unit) {

  val log = LoggerFactory.getLogger(this.getClass)

  private[this] val rand = new Random(System.currentTimeMillis)

  def mate(population: Population[T], config: Config, cycle: Int) {
    rand.setSeed(rand.nextInt + System.currentTimeMillis)
    population.select(score)
    population crossover rand.nextDouble * config.xover //TODO Is random here makes sense? maybe get rid of this factor
    population mutation rand.nextDouble * config.mu    //TODO Is random here makes sense?
  }
}

