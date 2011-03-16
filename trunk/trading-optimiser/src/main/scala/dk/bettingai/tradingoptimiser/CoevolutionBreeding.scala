package dk.bettingai.tradingoptimiser

import dk.bettingai.marketsimulator.trader._

case class CoevolutionBreeding[T <: ITrader](mutate: (Solution[T]) => T, populationSize: Int, fitness: CoevolutionFitness) {

	  /**Search for best solution by creating a population of traders that are mutated versions of modelTrader and then by competing among them on market simulator.
     * @param modelTrader Model trader used for breeding population.
     * @returns The best solution in population.
     * */
    def breed(modelTrader: Solution[T]): Solution[T] = {

      /** Born 10 traders.*/
      val population = for (i <- 1 to populationSize) yield mutate(modelTrader)

      /**Get best of children.*/
      fitness.fitness(population)
    }
}