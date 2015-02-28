package pl.edu.agh.backend.ga.example.rastrigin

import pl.edu.agh.backend.ga.strategy.Pool._
import pl.edu.agh.backend.ga.strategy._

import scala.util.Random


final class Point(override val code: List[Number]) extends Chromosome(code) {
  var unfitness: Double = 1000 * (1.0 + Random.nextDouble)

  def crossover(that: Chromosome[Number], gIdx: GeneticIndices): (Chromosome[Number], Chromosome[Number]) = {
    val xoverIdx = gIdx.chOpIdx
    val xGenes = spliceGene(gIdx, that.code(xoverIdx))

    val offspring1 = code.slice(0, xoverIdx) ::: xGenes._1 :: that.code.drop(xoverIdx + 1)
    val offspring2 = that.code.slice(0, xoverIdx) ::: xGenes._2 :: code.drop(xoverIdx + 1)

    (Point(offspring1), Point(offspring2))
  }

  private def spliceGene(gIdx: GeneticIndices, thatCode: Number): (Number, Number) = {
    ((this.code(gIdx.chOpIdx) crossover thatCode).asInstanceOf[Number],
      (thatCode crossover code(gIdx.chOpIdx)).asInstanceOf[Number])
  }


  def mutation(gIdx: GeneticIndices): Chromosome[Number] = {
    val mutated = code(gIdx.chOpIdx) mutation()

    val xs = Range(0, code.size).map(i =>
      if (i == gIdx.chOpIdx) mutated.asInstanceOf[Number] else code(i)
    ).toList
    Point(xs)
  }
}


object Point {

  def apply(code: List[Number]): Point = new Point(code)

}

final protected class FunctionOptimization(score: Chromosome[Number] => Unit) extends Evolution(score)

object FunctionOptimization {

  def apply(score: Chromosome[Number] => Unit): FunctionOptimization = new FunctionOptimization(score)
}

class Number(override val id: String, override val target: Double) extends Gene(id, target) {

  def score: Double = -1.0

  def crossover(that: Gene): Gene = {
    getGene(id, that.geneValue)
  }

  def getGene(id: String, target: Double) = new Number(id, target)

  def mutation(): Gene = getGene(id, geneValue + 1)
}

object Number {

  def apply(id: String, target: Double): Number =
    new Number(id, target)

}

class Variables(limit: Int, override val chromosomes: Pool[Number]) extends Population(limit, chromosomes) {

  def +(that: Population[Number]): Population[Number] = {
    if (that.size > 0) Variables(limit, chromosomes ++: that.chromosomes) else this
  }

  final def isNull: Boolean = chromosomes.isEmpty

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
      val gIdx = geneticIndices(xOver)
      val offSprings = chromosomes.take(mid)
        .zip(bottom)
        .map(p => p._1 crossover(p._2, gIdx))
        .unzip
      chromosomes ++= offSprings._1 ++ offSprings._2
    }
  }

  def mutation(mu: Double): Unit = {
    chromosomes ++= chromosomes.map(_ mutation geneticIndices(mu))
  }

  def geneticIndices(prob: Double): GeneticIndices = {
    val idx = (prob * chromosomeSize).floor.toInt
    val chIdx = if (idx == chromosomeSize) chromosomeSize - 1 else idx

    GeneticIndices(chIdx)
  }

  final def chromosomeSize: Int = if (chromosomes.size > 0) chromosomes.head.size else -1

  final def fittest(depth: Int): Option[Pool[Number]] = {
    if (size > 1)
      Some(chromosomes.take(if (depth > size) size else depth))
    else
      None
  }

  final def size: Int = chromosomes.size

  final def fittest: Option[Chromosome[Number]] = if (size > 0) Some(chromosomes.head) else None

  final def averageScore: Double = chromosomes.size / chromosomes.map(_.unfitness).sum


  protected def +=(newCode: List[Number]): Unit = {
    chromosomes += new Point(newCode)
  }
}


object Variables {

  def apply(limit: Int, chromosomes: Pool[Number]): Variables =
    new Variables(limit, chromosomes)

  def apply(limit: Int, chromosomes: List[Point]): Variables =
    new Variables(limit, new Pool ++ chromosomes)

}

