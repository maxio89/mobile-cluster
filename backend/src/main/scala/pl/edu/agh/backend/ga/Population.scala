package pl.edu.agh.backend.ga

import pl.edu.agh.backend.ga.Chromosome._


/**
 * @param chOpIdx  Index of the gene in the chromosome, manipulated by a genetic operator
 */
case class GeneticIndices(chOpIdx: Int)

/**
 * @param limit Maximum number of chromosomes allowed in this population
 *              (constrained optimization)
 * @param chromosomes Current pool of chromosomes (type: ArrayBuffer{Chromosome[T]\])
 */
class Population[T <: Gene](limit: Int, val chromosomes: Pool[T]) {

  import pl.edu.agh.backend.ga.Population._

  /**
   * <p>Add an array of chromosomes (or new population) to this existing population and return
   * a new combined population. The new chromosomes are appended to the existing pool
   * @param that New population to be added to the existing population
   * @return The combined population if the new population is not empty, this population otherwise
   */
  def +(that: Population[T]): Population[T] = {
    if (that.size > 0) Population[T](limit, chromosomes ++: that.chromosomes) else this
  }

  final def size: Int = chromosomes.size

  final def isNull: Boolean = chromosomes.isEmpty

  /**
   * <p>Selection operator for the chromosomes pool The selection relies on the
   * normalized cumulative unfitness for each of the chromosome ranked by decreasing
   * order.</p>
   * @param score Scoring function applied to all the chromosomes of this population
   * @param cutOff Normalized threshold value for the selection of the fittest chromosomes
   */
  def select(score: Chromosome[T] => Unit, cutOff: Double): Unit = {
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

  /**
   * <p>Applies the cross-over operator on the population by pairing
   * the half most fit chromosomes with the half least fit chromosomes.</p>
   * @param xOver cross-over factor [0, 1]
   */
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

  /**
   * Apply the mutation of the population. The operation produces a duplicate set of
   * chromosomes that are mutated using the mutate operator on chromosome.
   * @param mu mutation factor
   * @return Population with original chromosomes and mutated counter-part
   */
  def mutation(mu: Double): Unit = {
    chromosomes ++= chromosomes.map(_ mutation geneticIndices(mu))
  }

  /**
   * Compute the genetic index for cross-over and mutation
   * according to a probability value
   * @param prob probability value [0, 1]
   */
  private[this] def geneticIndices(prob: Double): GeneticIndices = {
    var idx = (prob * chromosomeSize).floor.toInt
    val chIdx = if (idx == chromosomeSize) chromosomeSize - 1 else idx

    idx = prob.floor.toInt

    GeneticIndices(chIdx)
  }

  /**
   * Return the number of genes in the chromosomes of this population.
   * @return Number of genes in each of the chromosomes of this population if the population
   *         is not empty, -1 otherwise
   */
  final def chromosomeSize: Int = if (chromosomes.size > 0) chromosomes.head.size else -1

  /**
   * Compute the difference between the N fittest chromosomes of two populations.
   * @param that The population to be compared to
   * @param depth Number of fittest chromosomes used in the comparison. If the depth exceeds
   *              the size the entire population is used in the comparison
   * @return The depth fittest chromosomes if there are common to both population, None otherwise
   */
  final def diff(that: Population[T], depth: Int): Option[Pool[T]] = {
    require(that.size > 1, "Population.diff Other population has no chromosome")
    require(depth > 0, s"Population.diff depth $depth should be >1")

    // Define the number of chromosomes participating
    // to the comparison of two populations 'this' and 'that'
    val fittestPoolSize = {
      if (depth >= size || depth >= that.size)
        if (size < that.size) size else that.size
      depth
    }
    // Deals with nested options. Get the 'depth' most fit
    // chromosomes for this population and 'depth' most fit
    // chromosomes for that population, then compare..
    for {
      first <- fittest(fittestPoolSize)
      second <- that.fittest(fittestPoolSize)
      if !first.zip(second).exists(x => x._1 != x._2)
    } yield first
  }

  final def fittest(depth: Int): Option[Pool[T]] = {
    if (size > 1)
      Some(chromosomes.take(if (depth > size) size else depth))
    else
      None
  }

  final def fittest: Option[Chromosome[T]] = if (size > 0) Some(chromosomes.head) else None

  final def averageScore: Double = chromosomes.size / chromosomes.map(_.unfitness).sum


  protected def +=(newCode: List[T]): Unit = {
    chromosomes += new Chromosome[T](newCode)
  }
}


object Population {
  private final val SCALING_FACTOR = 100
  private val MAX_NUM_CHROMOSOMES = 10000

  def apply[T <: Gene](limit: Int, chromosomes: Pool[T]): Population[T] =
    new Population[T](limit, chromosomes)

  def apply[T <: Gene](limit: Int, chromosomes: List[Chromosome[T]]): Population[T] =
    new Population[T](limit, new Pool[T] ++ chromosomes)


}
