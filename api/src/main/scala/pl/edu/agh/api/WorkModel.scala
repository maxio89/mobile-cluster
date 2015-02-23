package pl.edu.agh.api


object WorkModel {

  case class Job(n: Int, cycles: Int, size: Int)
  case class Result(cycles: Int, value: Double)

  case class Work(workId: String, job: Job)
  case class WorkResult(workId: String, result: Result)

}