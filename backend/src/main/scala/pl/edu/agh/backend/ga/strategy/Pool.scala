package pl.edu.agh.backend.ga.strategy

import scala.collection.mutable.ArrayBuffer

object Pool {
  type Pool[T <: Gene] = ArrayBuffer[Chromosome[T]]
}
