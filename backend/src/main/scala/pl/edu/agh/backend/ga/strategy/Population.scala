package pl.edu.agh.backend.ga.strategy

import org.slf4j.LoggerFactory
import pl.edu.agh.backend.ga.strategy.Pool._

/**
 * @param limit Maximum number of chromosomes allowed in this population
 *              (constrained optimization)
 * @param chromosomes Current pool of chromosomes (type: ArrayBuffer{Chromosome[T]\])
 */
@SerialVersionUID(0L)
abstract class Population[T <: Gene](limit: Int, val chromosomes: Pool[T]) extends Serializable {

  protected final val SCALING_FACTOR = 100
  val log = LoggerFactory.getLogger(this.getClass)

  /**
   * <p>Add an array of chromosomes (or new population) to this existing population and return
   * a new combined population. The new chromosomes are appended to the existing pool
   * @param that New population to be added to the existing population
   * @return The combined population if the new population is not empty, this population otherwise
   */
  def +(that: Population[T]): Population[T]

  def size: Int

  def isEmpty: Boolean

  /**

   * @param score Scoring function applied to all the chromosomes of this population
   */
  def select(score: Chromosome[T] => Unit): Unit

  /**
   * @param xOver cross-over factor [0, 1]
   */
  def crossover(xOver: Double): Unit

  /**
   * Apply the mutation of the population. The operation produces a duplicate set of
   * chromosomes that are mutated using the mutate operator on chromosome.
   * @param mu mutation factor [0, 1]
   * @return Population with original chromosomes and mutated counter-part
   */
  def mutation(mu: Double): Unit

  /**
   * Return the number of genes in the chromosomes of this population.
   * @return Number of genes in each of the chromosomes of this population if the population
   *         is not empty, -1 otherwise
   */
  def chromosomeSize: Int

  def fittest: Option[Chromosome[T]]

  def fittest(n: Int): Option[Population[T]]

  def averageScore: Double

  override def toString: String = {
    var text = "Population{"
    for (chromosome <- chromosomes) {
      text += chromosome.toString + ","
    }
    text + "}"
  }

  protected def +=(newCode: List[T]): Unit
}

