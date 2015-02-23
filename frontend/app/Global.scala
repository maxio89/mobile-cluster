import actors.services.factorial.FactorialService
import actors.services.ga.{Frontend, WorkProducer, WorkResultConsumer}
import play.api.GlobalSettings
import play.api.libs.concurrent.Akka
import play.api.mvc.WithFilters
import play.filters.gzip.GzipFilter

object Global extends WithFilters(new GzipFilter(shouldGzip =
  (request, response) => {
    val contentType = response.headers.get("Content-Type")
    contentType.exists(_.startsWith("text/html")) || request.path.endsWith("jsroutes.js")
  })) with GlobalSettings {

  override def onStart(app: play.api.Application)
  {
    FactorialService startOn Akka.system(app)
    Frontend startOn Akka.system(app)
//    val frontend = Frontend startOn Akka.system(app)
//    WorkProducer.startOn(Akka.system(app), frontend)
//    WorkResultConsumer startOn Akka.system(app)
  }
}
