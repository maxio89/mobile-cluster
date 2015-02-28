package pl.edu.agh.backend.ga.strategy

import pl.edu.agh.api.Work._

import scala.util.Random

abstract class Evolution[T <: Gene](score: Chromosome[T] => Unit) {
  private[this] val rand = new Random(System.currentTimeMillis)

  def mate(population: Population[T], config: Config, cycle: Int) {
    rand.setSeed(rand.nextInt + System.currentTimeMillis)
    population.select(score, config.maxSize)
    population crossover rand.nextDouble * config.xover
    population mutation rand.nextDouble * config.mu
  }
}

