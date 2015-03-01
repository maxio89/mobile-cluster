package pl.edu.agh.backend.ga.strategy

abstract class Gene(val id: String, val target: Double) {

  var geneValue = target

  def crossover(that: Gene): Gene

  def mutation(mu: Double): Gene
}

