package pl.edu.agh.backend.ga.strategy

import org.slf4j.LoggerFactory


@SerialVersionUID(0L)
abstract class Chromosome[T <: Gene](val code: List[T]) extends Serializable {

  val log = LoggerFactory.getLogger(this.getClass)

  var unfitness: Double

  def crossover(that: Chromosome[T], chOpIdx: Int): (Chromosome[T], Chromosome[T])

  @inline
  def size: Int = code.size

  def isEmpty: Boolean = code.isEmpty

  def mutation(gIdx: Double): Chromosome[T]

  override def toString: String = {
    var text = "Chromosome{"
    for (individual <- code) {
      text += individual.toString + ","
    }
    text + "}"
  }
}

