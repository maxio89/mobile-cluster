package controllers.services.ga

import actors.services.ga.FrontendActor
import akka.serialization.SerializationExtension
import pl.edu.agh.api.RastriginWork.{RastriginConfig, RastriginResult}
import play.api.Play.current
import play.api._
import play.api.libs.concurrent.Akka
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.{JsObject, _}
import play.api.mvc._

import scala.concurrent.Future

// Reactive Mongo imports

import reactivemongo.api._

// Reactive Mongo plugin, including the JSON-specialized collection

import play.modules.reactivemongo.MongoController
import play.modules.reactivemongo.json.collection.JSONCollection

object Rastrigin extends Controller with MongoController {

  import actors.services.ga.JsonFormatters._

  def frontendWebsocket() = WebSocket.acceptWithActor[RastriginConfig, RastriginResult] { implicit request =>
    FrontendActor.props
  }

  def historicalData() = Action.async {
    val cursor: Cursor[JsObject] = collection.
      find(Json.obj()).
      sort(Json.obj("sequenceNr" -> -1)).
      cursor[JsObject]
    Logger.info("Attempting risky calculation.")
    val system = Akka.system

    // Get the Serialization Extension
    val serialization = SerializationExtension(system)

    // Have something to serialize
    val original = "woohoo"

    // Find the Serializer for it
    val serializer = serialization.findSerializerFor(original)

    // Turn it into bytes
    val bytes = serializer.toBinary(original)

    // Turn it back into an object
    val back = serializer.fromBinary(bytes, manifest = None)

    val futureDataList: Future[List[JsObject]] = cursor.collect[List]()

    val futureDataJsonArray: Future[JsArray] = futureDataList.map { data =>
      Json.arr(data)
    }

    futureDataJsonArray.map { data =>
      Ok(data)
    }
  }

  def collection: JSONCollection = db.collection[JSONCollection]("messages")

}