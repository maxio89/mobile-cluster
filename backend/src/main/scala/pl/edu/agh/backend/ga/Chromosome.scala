package pl.edu.agh.backend.ga

import scala.util.Random


final class Chromosome[T <: Gene](val code: List[T]) {
  var unfitness: Double = 1000 * (1.0 + Random.nextDouble)

  def crossover(that: Chromosome[T], gIdx: GeneticIndices): (Chromosome[T], Chromosome[T]) = {
    val xoverIdx = gIdx.chOpIdx
    val xGenes = spliceGene(gIdx, that.code(xoverIdx))

    val offspring1 = code.slice(0, xoverIdx) ::: xGenes._1 :: that.code.drop(xoverIdx + 1)
    val offspring2 = that.code.slice(0, xoverIdx) ::: xGenes._2 :: code.drop(xoverIdx + 1)

    (Chromosome[T](offspring1), Chromosome[T](offspring2))
  }

  private def spliceGene(gIdx: GeneticIndices, thatCode: T): (T, T) = {
    ((this.code(gIdx.chOpIdx) crossover thatCode).asInstanceOf[T],
      (thatCode crossover code(gIdx.chOpIdx)).asInstanceOf[T])
  }

  @inline
  def size: Int = code.size

  def isNull: Boolean = code.isEmpty

  def mutation(gIdx: GeneticIndices): Chromosome[T] = {
    val mutated = code(gIdx.chOpIdx) mutation()

    val xs = Range(0, code.size).map(i =>
      if (i == gIdx.chOpIdx) mutated.asInstanceOf[T] else code(i)
    ).toList
    Chromosome[T](xs)
  }

  def normalize(normalizeFactor: Double): Unit = {
    unfitness /= normalizeFactor
  }


  override def clone: Chromosome[T] = Chromosome[T](code)

}


object Chromosome {

  import scala.collection.mutable.ArrayBuffer

  type Pool[T <: Gene] = ArrayBuffer[Chromosome[T]]

  def apply[T <: Gene](code: List[T]): Chromosome[T] = new Chromosome[T](code)

}
