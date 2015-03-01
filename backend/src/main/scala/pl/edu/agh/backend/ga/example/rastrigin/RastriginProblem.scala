package pl.edu.agh.backend.ga.example.rastrigin

import pl.edu.agh.backend.ga.strategy.Pool._
import pl.edu.agh.backend.ga.strategy._

import scala.util.Random


class Number(override val id: String, override val target: Double) extends Gene(id, target) {

  def score: Double = -1.0

  def crossover(that: Gene): Gene = {
    getGene(id, that.geneValue)
  }

  def getGene(id: String, target: Double) = new Number(id, target)

  def mutation(mu: Double): Number = if (Random.nextBoolean()) getGene(id, geneValue + ((mu / geneValue) * 100)) else getGene(id, geneValue - ((mu / geneValue) * 100))
}

object Number {

  def apply(id: String, target: Double): Number =
    new Number(id, target)

}

final class Point(override val code: List[Number]) extends Chromosome(code) {
  var unfitness: Double = 1000 * (1.0 + Random.nextDouble)

  /**
   * <p>Applies the cross-over operator on the population by pairing
   * the half most fit chromosomes with the half least fit chromosomes.</p>
   */
  def crossover(that: Chromosome[Number], chOpIdx: Int): (Chromosome[Number], Chromosome[Number]) = {
    val xGenes = spliceGene(chOpIdx, that.code(chOpIdx))

    val offspring1 = code.slice(0, chOpIdx) ::: xGenes._1 :: that.code.drop(chOpIdx + 1)
    val offspring2 = that.code.slice(0, chOpIdx) ::: xGenes._2 :: code.drop(chOpIdx + 1)

    (Point(offspring1), Point(offspring2))
  }

  private def spliceGene(chOpIdx: Int, thatCode: Number): (Number, Number) = {
    ((this.code(chOpIdx) crossover thatCode).asInstanceOf[Number],
      (thatCode crossover code(chOpIdx)).asInstanceOf[Number])
  }

  def mutation(mu: Double): Chromosome[Number] = {
    val xs = Range(0, code.size).map(i =>
      if (Random.nextBoolean()) code(i).mutation(mu) else code(i)
    ).toList
    Point(xs)
  }
}

class Variables(limit: Int, override val chromosomes: Pool[Number]) extends Population(limit, chromosomes) {

  def +(that: Population[Number]): Population[Number] = {
    if (that.size > 0) Variables(limit, chromosomes ++: that.chromosomes) else this
  }

  final def isNull: Boolean = chromosomes.isEmpty

  /**
   * <p>Selection operator for the chromosomes pool The selection relies on the
   * normalized cumulative unfitness for each of the chromosome ranked by decreasing
   * order.</p>
   * @param score Scoring function applied to all the chromosomes of this population
   * @param cutOff Normalized threshold value for the selection of the fittest chromosomes
   */
  def select(score: Chromosome[Number] => Unit, cutOff: Double): Unit = {
    // Compute the cumulative score for the entire population
    val cumul = chromosomes.foldLeft(0.0)((s, xy) => {
      score(xy)
      s + xy.unfitness
    }) / SCALING_FACTOR

    // Normalize each chromosome unfitness value
    chromosomes foreach (_ normalize cumul)

    // Sorts the chromosome by the increasing value of their unfitness
    val newChromosomes = chromosomes.sortWith(_.unfitness < _.unfitness)

    // Apply a cutoff value to the current size of the population
    // if the cutoff has been defined.
    val cutOffSize: Int = (cutOff * newChromosomes.size).floor.toInt
    val newPopSize = if (limit < cutOffSize) limit else cutOffSize
    chromosomes.clear()
    chromosomes ++= newChromosomes.take(newPopSize)
  }

  def crossover(xOver: Double): Unit = {

    // It makes sense to cross over all the chromosomes in this
    // population if there are more than one chromosome
    if (size > 1) {
      // Breakdown the sorted list of chromosomes into two segments
      val mid = size >> 1
      val bottom = chromosomes.slice(mid, size)

      // Pair a chromosome for one segment with a chromosome
      // from the other segment.Then add those offsprings to the
      // current population
      val chOpIdx = getChromosomeIndex(xOver)
      val offSprings = chromosomes.take(mid)
        .zip(bottom)
        .map(p => p._1 crossover(p._2, chOpIdx))
        .unzip
      chromosomes ++= offSprings._1 ++ offSprings._2
    }
  }

  /**
   * Compute the genetic index for cross-over
   * according to a probability value
   * Index of the gene in the chromosome, manipulated by a genetic operator
   * @param prob probability value [0, 1]
   */
  def getChromosomeIndex(prob: Double): Int = {
    val idx = (prob * chromosomeSize).floor.toInt
    if (idx == chromosomeSize) chromosomeSize - 1 else idx
  }

  final def chromosomeSize: Int = if (chromosomes.size > 0) chromosomes.head.size else -1

  final def size: Int = chromosomes.size

  def mutation(mu: Double): Unit = {
    chromosomes ++= chromosomes.map(_ mutation mu)
  }

  final def fittest: Option[Chromosome[Number]] = if (size > 0) Some(chromosomes.head) else None

  final def averageScore: Double = chromosomes.size / chromosomes.map(_.unfitness).sum


  protected def +=(newCode: List[Number]): Unit = {
    chromosomes += new Point(newCode)
  }
}

final protected class FunctionOptimization(score: Chromosome[Number] => Unit) extends Evolution(score)


object Point {

  def apply(code: List[Number]): Point = new Point(code)

}

object Variables {

  def apply(limit: Int, chromosomes: Pool[Number]): Variables =
    new Variables(limit, chromosomes)

  def apply(limit: Int, chromosomes: List[Point]): Variables =
    new Variables(limit, new Pool ++ chromosomes)

}

object FunctionOptimization {

  def apply(score: Chromosome[Number] => Unit): FunctionOptimization = new FunctionOptimization(score)
}
