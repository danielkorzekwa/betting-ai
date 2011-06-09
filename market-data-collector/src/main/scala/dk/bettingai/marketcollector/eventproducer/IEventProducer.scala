package dk.bettingai.marketcollector.eventproducer

import dk.betex.api._
import IMarket._
/**This trait represents a service that transforms state of a market on a betting exchange into the stream of events.
 * 
 * @author KorzekwaD
 *
 */
trait IEventProducer {

	/**Transforms state of market on a betting exchange into the stream of events. 
	 * 
	 * This is a stateful service that is keeping recent states for all markets that this method has been called.
	 * First call of this method for a given market returns list of events that represents delta between empty state of a market(no prices and traded volume) and market state passed to this method.
	 * Next call of this method for the same market returns list of events that represents delta between previous and new state of a market.
	 * 
	 * @param timestamp for all generated events
	 * @param marketId Market id.
	 * @param marketRunners Market state represented by runner prices and price traded volume.
	 * 
	 * @return List of market events in a json format (PLACE_BET, CANCEL_BET) for a market
	 * */
	def produce(timestamp:Long,marketId:Long,marketRunners: Map[Long,Tuple2[List[IRunnerPrice],IRunnerTradedVolume]]):List[String]
}