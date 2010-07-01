package dk.bettingai.marketcollector

import dk.bettingai.marketsimulator.betex.api.IMarket._

/**This trait represents a function that calculates market events for the delta between previous and current state of the market runner.
 * 
 * @author KorzekwaD
 *
 */
trait IMarketEventCalculator {

	/**Calculates market events for the delta between previous and current state of the market runner.
	 * 
	 * @param userId The user Id that the bet placement events are calculated for.
	 * @param marketId The market id that the bet placement events are calculated for. 
	 * @param runnerId The market runner id that the bet placement events are calculated for. 
	 * @param newMarketData Element 1 - marketPrices for the market runner, Element 2 - total traded volume for the market runner
	 * @param previousMarketData Element 1 - marketPrices for the market runner, Element 2 - total traded volume for the market runner
	 * @return List of market events in a json format (PLACE_BET, CANCEL_BET) for the market runner
	 */
	def calculate(userId:Long,marketId:Long,runnerId:Long)(newMarketData:Tuple2[List[IRunnerPrice],List[IPriceTradedVolume]],previousMarketData:Tuple2[List[IRunnerPrice],List[IPriceTradedVolume]]): List[String]
}