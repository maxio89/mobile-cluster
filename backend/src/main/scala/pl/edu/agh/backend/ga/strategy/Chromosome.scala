package pl.edu.agh.backend.ga.strategy


abstract class Chromosome[T <: Gene](val code: List[T]) {
  var unfitness: Double

  def crossover(that: Chromosome[T], gIdx: GeneticIndices): (Chromosome[T], Chromosome[T])

  @inline
  def size: Int = code.size

  def isNull: Boolean = code.isEmpty

  def mutation(gIdx: GeneticIndices): Chromosome[T]
  def normalize(normalizeFactor: Double): Unit = {
    unfitness /= normalizeFactor
  }
}

