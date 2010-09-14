package dk.bettingai.marketcollector.eventcalculator

import dk.bettingai.marketsimulator.betex.api._
import IMarket._

/**This trait represents a function that calculates market events for the delta between the previous and the current state of the market runner.
 * 
 * @author KorzekwaD
 *
 */
trait IMarketEventCalculator {
  
	/**Transforms delta between two states of market runner into the stream of events.
	 * 	
	 * @param timestamp for all generated events
	 * @param marketId
	 * @param runnerId
	 * @param marketRunner current state of market runner
	 * @param prevMarketRunner previous state of market runner
	 * @return List of market events in a json format (PLACE_BET, CANCEL_BET) for a market
	 * */
	def produce(timestamp:Long,marketId:Long,runnerId:Long,marketRunner:Tuple2[List[IRunnerPrice],IRunnerTradedVolume],prevMarketRunner:Tuple2[List[IRunnerPrice],IRunnerTradedVolume]):List[String]
	
	/**Calculates market events for the delta between the previous and the current state of the market runner.
	 * 
	 * @param timestamp for all generated events
	 * @param marketId The market id that the bet placement events are calculated for. 
	 * @param runnerId The market runner id that the bet placement events are calculated for. 
	 * @param marketRunnerDelta Delta between the new and the previous state of the market runner (both runner prices and traded volume combined to runner prices).
	 * @return List of market events in a json format (PLACE_BET, CANCEL_BET) for the market runner
	 */
	def calculateMarketEvents(timestamp:Long,marketId:Long,runnerId:Long)(marketRunnerDelta:List[IRunnerPrice]): List[String]

	/**Combines delta for runner prices with delta for traded volume and represents it as runner prices.
	 * 
	 * @param  runnerPricesDelta
	 * @param runnerTradedVolumeDelta
	 * 
	 * Example:
	 * runner price[price,toBack,toLay] = 1.9,2,0
	 * traded volume [price,volume] = 1.9,5
	 * runner price + traded volume = [price,toBack+volume,toLay+volume] = 1.9,7,5
	 * */
	def combine(runnerPricesDelta:List[IRunnerPrice], runnerTradedVolumeDelta:List[IRunnerTradedVolume.IPriceTradedVolume]):List[IRunnerPrice]
	
	/**Calculates delta between the new and the previous state of the runner prices.
	 * 
	 * @param newRunnerPrices
	 * @param previousRunnerPrices
	 * @return Delta between the new and the previous state of the runner prices.
	 */
	def calculateRunnerPricesDelta(newRunnerPrices:List[IRunnerPrice],previousRunnerPrices:List[IRunnerPrice]):List[IRunnerPrice]
		
	/**This function transforms runner from state A to B. 
	 * State A is represented by runner prices in state A and traded volume delta between states B and A. 
	 * The state B is represented the list of market events that reflect traded volume delta between states B and A
	 *  and by runner prices in state B.
	 *  
	 *  @param timestamp for all generated events
	 *  @param marketId
	 *  @param runnerId
	 * */
	def calculateMarketEventsForTradedVolume(timestamp:Long,marketId:Long,runnerId:Long)(previousRunnerPrices:List[IRunnerPrice],runnerTradedVolumeDelta:IRunnerTradedVolume):Tuple2[List[IRunnerPrice],List[String]]
}