package pl.edu.agh.backend.ga.strategy

import org.slf4j.LoggerFactory

@SerialVersionUID(0L)
abstract class Gene(val target: Double) extends Serializable {

  val log = LoggerFactory.getLogger(this.getClass)

  def crossover(that: Gene): Gene

  def mutation(mu: Double): Gene

  override def toString: String = {
    s"Gene{$target}"
  }
}

