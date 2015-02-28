package pl.edu.agh.api

object Work {

  /**
  //   * @param initialSize Initial population size.
//   * @param maxSize  Maximum population size.
//   * @param xover Value of the cross-over parameter, in the range [0.0, 1.0] used to compute
//   *              the index of bit string representation of the chromosome for cross-over.
//   * @param mu Value in the range [0.0, 1.0] used to compute the index of the bit or
//   *           individual to be mutate in each chromosome.
//   * @param maxCycles Maximum number of iterations allowed by the genetic solver
    * (reproduction cycles).
    */
  trait Config {

    val initialSize: Int
    val maxSize: Int
    val xover: Double
    val mu: Double
    val maxCycles: Int
    val DEFAULT_MAX_CYCLES = 2048

    //    def apply(initialSize: Int, maxSize: Int, xover: Double, mu: Double, maxCycles: Int) : Config = new Config(initialSize, maxSize, xover, mu, maxCycles)

  }

  trait Result

  case class Work(id: String, job: Config)

  case class WorkResult(id: String, result: Result)

}