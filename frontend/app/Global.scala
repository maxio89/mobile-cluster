import actors.services.FactorialService
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
  }
}