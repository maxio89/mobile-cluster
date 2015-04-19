package pl.edu.agh.api

import pl.edu.agh.api.Work._

object RastriginWork extends {

  case class RastriginConfig(n: Int, initialSize: Int, maxSize: Int, xover: Double, mu: Double, maxCycles: Int, snapshotFreq: Int, migrationFreq: Int, migrationFactor: Int) extends Config

  //TODO add workId to distinguish work result
  case class RastriginResult(workerId: String, hostname: String, runtime: Long, cycles: Int, value: Double, point: List[Double]) extends Result

}
