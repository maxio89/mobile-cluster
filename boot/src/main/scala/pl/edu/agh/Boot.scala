package pl.edu.agh

import akka.kernel.Bootable
import pl.edu.agh.backend.Backend

class Boot extends Bootable {

  def startup() = {
    Backend.main(Array())
  }

  def shutdown() = {
  }
}
