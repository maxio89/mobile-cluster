package pl.edu.agh.backend.ga.strategy

abstract class Gene(val id: String, val target: Double) extends Serializable {

  def crossover(that: Gene): Gene

  def mutation(mu: Double): Gene
}

