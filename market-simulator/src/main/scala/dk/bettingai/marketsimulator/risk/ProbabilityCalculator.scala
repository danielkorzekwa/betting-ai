package dk.bettingai.marketsimulator.risk

import scala.collection._

/**This object represents a function that calculates market probabilities from market prices.
 * 
 * @author korzekwad
 *
 */
object ProbabilityCalculator extends IProbabilityCalculator{

	/**Calculates market probabilities from market prices. 
	 * 
	 * Negative or NaN probabilities may be returned for irrational prices. 
	 * It's a client responsibility to pass rational prices to the calculator and deal negative probabilities. 
	 * For more details on scenarios that cause negative/NaN probabilities go to @see ProbabilityCalculatorTest
	 * 
	 * @param marketPrices Key - runnerId, Value - market prices (element 1 - priceToBack, element 2 - priceToLay)
	 * @param numOfWinners Number of winners in the market
	 * @return Key - runnerId, value - runner probability
	 */
	def calculate(marketPrices: Map[Long,Tuple2[Double,Double]], numOfWinners:Int): Map[Long,Double] = {
		require(numOfWinners>0)	
		if(marketPrices.isEmpty) return Map()

			/**Convert prices to probabilities.Key - runnerId, Value - market probabilities (element 1 - probToBack, element 2 - probToLay)*/
			val marketProbs = marketPrices.mapValues(prices => toProb(prices._1,1) -> toProb(prices._2,Double.PositiveInfinity))

			/**Sums up all back and lay probabilities.Element 1 - totalToBack probability, element 2 - totalToLay probability.*/
			val totalProbs:Tuple2[Double,Double] = marketProbs.values.reduceLeft((a,b) =>a._1 + b._1 -> (a._2+ b._2))

			/**Default case that should always happen in practice.*/
			if(totalProbs._1 > totalProbs._2) {
				val factor = (numOfWinners - totalProbs._2) / (totalProbs._1 - totalProbs._2)
				marketProbs.mapValues(prob => prob._2 + factor * (prob._1 - prob._2))
			}
			/**Use only toBack probabilities.*/
			else {
				val factor = numOfWinners / totalProbs._1
				marketProbs.mapValues(prob => factor * prob._1)
			}
	}

	/**Convert price to probability.
	 * @param price
	 * @param nanValue Default probability value if price is NaN 
	 */
	private def toProb(price:Double, nanValue:Double) = if(!price.equals(Double.NaN)) 1/price else nanValue
}