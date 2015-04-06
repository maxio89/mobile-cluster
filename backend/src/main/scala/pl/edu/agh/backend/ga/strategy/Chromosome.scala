package pl.edu.agh.backend.ga.strategy


abstract class Chromosome[T <: Gene](val code: List[T]) extends Serializable {
  var unfitness: Double

  def crossover(that: Chromosome[T], chOpIdx: Int): (Chromosome[T], Chromosome[T])

  @inline
  def size: Int = code.size

  def isEmpty: Boolean = code.isEmpty

  def mutation(gIdx: Double): Chromosome[T]

  def normalize(normalizeFactor: Double): Unit = {
    unfitness /= normalizeFactor
  }
}

