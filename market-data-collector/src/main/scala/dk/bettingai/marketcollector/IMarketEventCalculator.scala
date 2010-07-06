package dk.bettingai.marketcollector

import dk.bettingai.marketsimulator.betex.api.IMarket._

/**This trait represents a function that calculates market events for the delta between the previous and the current state of the market runner.
 * 
 * @author KorzekwaD
 *
 */
trait IMarketEventCalculator {

	/**Calculates market events for the delta between the previous and the current state of the market runner.
	 * 
	 * @param userId The user Id that the bet placement events are calculated for.
	 * @param marketId The market id that the bet placement events are calculated for. 
	 * @param runnerId The market runner id that the bet placement events are calculated for. 
	 * @param runnerPricesDelta Delta between the new and the previous state of the runner prices.
	 * @param runnerTradedVolumeDelta Delta between the new and the previous state of the runner traded volume.
	 * @return List of market events in a json format (PLACE_BET, CANCEL_BET) for the market runner
	 */
	def calculateRunnerDelta(userId:Long,marketId:Long,runnerId:Long)(runnerPricesDelta:List[IRunnerPrice], runnerTradedVolumeDelta:List[IPriceTradedVolume]): List[String]

	/**Calculates delta between the new and the previous state of the runner prices.
	 * 
	 * @param newRunnerPrices
	 * @param previousRunnerPrices
	 * @return Delta between the new and the previous state of the runner prices.
	 */
	def calculateRunnerPricesDelta(newRunnerPrices:List[IRunnerPrice],previousRunnerPrices:List[IRunnerPrice]):List[IRunnerPrice]
	
	/**Calculates delta between the new and the previous state of the runner traded volume.
	 * 
	 * @param newTradedVolumes
	 * @param previousTradedVolumes
	 * @return Delta between the new and the previous state of the runner traded volume.
	 */
	def calculateTradedVolumeDelta(newTradedVolumes:List[IPriceTradedVolume],previousTradedVolumes:List[IPriceTradedVolume]):List[IPriceTradedVolume]
}