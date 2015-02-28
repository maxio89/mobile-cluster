package pl.edu.agh.backend.ga.strategy

import pl.edu.agh.backend.ga.strategy.Pool._

/**
 * @param chOpIdx  Index of the gene in the chromosome, manipulated by a genetic operator
 */
case class GeneticIndices(chOpIdx: Int)

/**
 * @param limit Maximum number of chromosomes allowed in this population
 *              (constrained optimization)
 * @param chromosomes Current pool of chromosomes (type: ArrayBuffer{Chromosome[T]\])
 */
abstract class Population[T <: Gene](limit: Int, val chromosomes: Pool[T]) {

  protected final val SCALING_FACTOR = 100

  /**
   * <p>Add an array of chromosomes (or new population) to this existing population and return
   * a new combined population. The new chromosomes are appended to the existing pool
   * @param that New population to be added to the existing population
   * @return The combined population if the new population is not empty, this population otherwise
   */
  def +(that: Population[T]): Population[T]

  def size: Int

  def isNull: Boolean

  /**
   * <p>Selection operator for the chromosomes pool The selection relies on the
   * normalized cumulative unfitness for each of the chromosome ranked by decreasing
   * order.</p>
   * @param score Scoring function applied to all the chromosomes of this population
   * @param cutOff Normalized threshold value for the selection of the fittest chromosomes
   */
  def select(score: Chromosome[T] => Unit, cutOff: Double): Unit

  /**
   * <p>Applies the cross-over operator on the population by pairing
   * the half most fit chromosomes with the half least fit chromosomes.</p>
   * @param xOver cross-over factor [0, 1]
   */
  def crossover(xOver: Double): Unit

  /**
   * Apply the mutation of the population. The operation produces a duplicate set of
   * chromosomes that are mutated using the mutate operator on chromosome.
   * @param mu mutation factor
   * @return Population with original chromosomes and mutated counter-part
   */
  def mutation(mu: Double): Unit

  /**
   * Compute the genetic index for cross-over
   * according to a probability value
   * @param prob probability value [0, 1]
   */
  def geneticIndices(prob: Double): GeneticIndices

  /**
   * Return the number of genes in the chromosomes of this population.
   * @return Number of genes in each of the chromosomes of this population if the population
   *         is not empty, -1 otherwise
   */
  def chromosomeSize: Int

  def fittest(depth: Int): Option[Pool[T]]

  def fittest: Option[Chromosome[T]]

  def averageScore: Double


  protected def +=(newCode: List[T]): Unit
}

