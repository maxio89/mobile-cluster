package pl.edu.agh.api

import pl.edu.agh.api.Work._

object RastriginWork extends {

  case class RastriginConfig(n: Int, initialSize: Int, maxSize: Int, xover: Double, mu: Double, maxCycles: Int, snapshotFreq: Int) extends Config

  case class RastriginResult(cycles: Int, value: Double, point: List[Double]) extends Result

}
