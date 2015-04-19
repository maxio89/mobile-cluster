package pl.edu.agh.backend.ga.example.rastrigin

import pl.edu.agh.backend.ga.strategy.Pool._
import pl.edu.agh.backend.ga.strategy._

import scala.collection.mutable.ArrayBuffer
import scala.util.Random


@SerialVersionUID(1L)
class Number(override val target: Double) extends Gene(target) {

  def score: Double = -1.0

  def crossover(that: Gene): Gene = {
    getGene(that.target)
  }

  //TODO take a look and try to increase the precision, right now is E-13
  def mutation(mu: Double): Number = {
    val mutationValue: Double = (mu * target) / 100
    if (Random.nextBoolean())
      getGene(target + mutationValue)
    else
      getGene(target - mutationValue)
  }

  def getGene(target: Double) = new Number(target)
}

object Number {

  def apply(id: String, target: Double): Number =
    new Number(target)

}

@SerialVersionUID(1L)
final class Point(override val code: List[Number]) extends Chromosome(code) {
  var unfitness: Double = 0.0

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
    val xs = code.indices.map(i => {
      code(i).mutation(mu)
    }
    ).toList
    Point(xs)
  }
}

@SerialVersionUID(1L)
class Variables(limit: Int, override val chromosomes: Pool[Number]) extends Population(limit, chromosomes) {

  def +(that: Population[Number]): Population[Number] = {
    if (that.size > 0) Variables(limit, chromosomes ++: that.chromosomes) else this
  }

  final def isEmpty: Boolean = chromosomes.isEmpty

  /**
   * <p>Selection operator for the chromosomes pool The selection relies on the
   * normalized cumulative unfitness for each of the chromosome ranked by decreasing
   * order.</p>
   * @param score Scoring function applied to all the chromosomes of this population
   */
  def select(score: Chromosome[Number] => Unit): Unit = {
     chromosomes foreach { chromosome =>
       score(chromosome)
     }
    // Sorts the chromosome by the increasing value of their unfitness
    val newChromosomes = chromosomes.sortWith(_.unfitness < _.unfitness)

    chromosomes.clear()
    // Leaves only limited number of chromosomes by taking part from the tail
    chromosomes ++= newChromosomes.take(limit)
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

  final def chromosomeSize: Int = if (chromosomes.nonEmpty) chromosomes.head.size else -1

  def mutation(mu: Double): Unit = {
    var mutants : Pool[Number] = ArrayBuffer()
    for(chromosome <- chromosomes) {
      if (Random.nextBoolean())
        mutants = mutants :+ (chromosome mutation mu)
    }
    chromosomes ++= mutants
  }

  final def fittest: Option[Chromosome[Number]] = if (size > 0) Some(chromosomes.head) else None

  final def fittest(n: Int): Option[Population[Number]] =
    if (size > 0) {
      if (n < size)
        Some(Variables(limit, chromosomes.take(n)))
      else
        Some(Variables(limit, chromosomes.take(size)))
    } else {
      None
    }

  final def size: Int = chromosomes.size

  //TODO unused
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
