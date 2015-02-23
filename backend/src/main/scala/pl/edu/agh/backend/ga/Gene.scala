package pl.edu.agh.backend.ga


class Gene(val id: String, val target: Double) {

  var geneValue = target

  def score: Double = -1.0

  def crossover(that: Gene): Gene = {
    getGene(id, that.geneValue)
  }

  def getGene(id: String, target: Double) = new Gene(id, target)

  def mutation(): Gene = getGene(id, geneValue)
}

object Gene {

  def apply(id: String, target: Double): Gene =
    new Gene(id, target)

}
