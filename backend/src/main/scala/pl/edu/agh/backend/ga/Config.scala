/**
 * Copyright (c) 2013-2015  Patrick Nicolas - Scala for Machine Learning - All rights reserved
 *
 * The source code in this file is provided by the author for the sole purpose of illustrating the
 * concepts and algorithms presented in "Scala for Machine Learning". It should not be used to
 * build commercial applications.
 * ISBN: 978-1-783355-874-2 Packt Publishing.
 * Unless required by applicable law or agreed to in writing, software is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *
 * Version 0.98.1
 */
package pl.edu.agh.backend.ga


/**
 * @param xover Value of the cross-over parameter, in the range [0.0, 1.0] used to compute
 *              the index of bit string representation of the chromosome for cross-over
 * @param mu Value in the range [0.0, 1.0] used to compute the index of the bit or
 *           individual to be mutate in each chromosome.
 * @param maxCycles Maximum number of iterations allowed by the genetic solver
 *                  (reproduction cycles).
 * @param softLimit  Soft limit function (Linear, Square or Boltzman) used to attenuate
 *                   the impact of cross-over or mutation operation during optimization.</span></pre></p>
 */
final class Config(
                    val xover: Double,
                    val mu: Double,
                    val maxCycles: Int,
                    val softLimit: Int => Double) {


  /**
   * mutation Mutation computed using an attenuation function mutation = f(cycle)
   * <p>re-compute the mutation factor using an attenuator</p>
   */
  val mutation = (cycle: Int) => {
    softLimit(cycle)
  }

}


object Config {

  private val DEFAULT_SOFTLIMIT = (n: Int) => -0.01 * n + 1.001
  private val DEFAULT_MAX_CYCLES = 2048

  /**
   * Default constructor for the GAConfig class
   * @param xover Value of the cross-over parameter, in the range [0.0, 1.0] used to compute
   *              the index of bit string representation of the chromosome for cross-over
   * @param maxCycles Maximum number of iterations allowed by the genetic solver
   *                  (reproduction cycles).
   * @param softLimit  Soft limit function (Linear, Square or Boltzman) used to attenuate
   *                   the impact of cross-over or mutation operation during optimization.</span></pre></p>
   */
  def apply(xover: Double, mu: Double, maxCycles: Int, softLimit: Int => Double): Config =
    new Config(xover, mu, maxCycles, softLimit)

  def apply(xover: Double, mu: Double, maxCycles: Int): Config =
    new Config(xover, mu, maxCycles, DEFAULT_SOFTLIMIT)
}


