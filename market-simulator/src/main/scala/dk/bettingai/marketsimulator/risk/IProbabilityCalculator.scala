package dk.bettingai.marketsimulator.risk

import scala.collection._

/**This trait represents a function that calculates market probabilities from market prices.
 * 
 * @author korzekwad
 *
 */
trait IProbabilityCalculator {

	/**Calculates market probabilities from market prices.
	 * 
	 * @param marketPrices Key - runnerId, Value - market prices (element 1 - priceToBack, element 2 - priceToLay)
	 * @param numOfWinners Number of winners in the market
	 * @return Key - runnerId, value - runner probability
	 */
	def calculate(marketPrices: Map[Long,Tuple2[Double,Double]], numOfWinners:Int): Map[Long,Double]
}