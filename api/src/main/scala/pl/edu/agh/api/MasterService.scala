package pl.edu.agh.api

object MasterService {

  //  FrontendActor
  case class Ack(workId: String)

  case object Ok

  case object NotOk

}