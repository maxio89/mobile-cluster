package pl.edu.agh.backend.ga

import scala.util.Random

final protected class Evaluation[T <: Gene](score: Chromosome[T] => Unit) {
  private[this] val rand = new Random(System.currentTimeMillis)

  def mate(population: Population[T], config: Config, cycle: Int) {
    rand.setSeed(rand.nextInt + System.currentTimeMillis)
    population.select(score, config.softLimit(cycle))
    population crossover rand.nextDouble * config.xover
    population mutation rand.nextDouble * config.mu
  }
}


object Evaluation {

  def apply[T <: Gene](score: Chromosome[T] => Unit): Evaluation[T] = new Evaluation[T](score)
}
