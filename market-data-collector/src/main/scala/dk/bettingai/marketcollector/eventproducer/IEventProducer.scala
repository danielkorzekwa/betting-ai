package dk.bettingai.marketcollector.eventproducer

/**This trait represents a service that transforms state of market on a betting exchange into the stream of events.
 * 
 * How it works:
 *  1)First of all, get list of all monitored markets from the betfair api. Then for each market do the following:
 *  2)Get market prices and total traded volume from the Betfair API.
 *  3)Calculate market events that represent the delta between previous and current state of the market. 
 *  If no previous market state is available then CREATE_MARKET event will be generated as the first on the list.
 *  4)Add those market events to the Betex (Implementation of a betting exchange), 
 *  then get market prices and total traded volume for the market and compare both data against market prices and total traded volume that was received from the Betfair API. 
 *  The reason for that is to check if market events have been generated correctly. If both data don't match then exception should be thrown.
 * 
 * @author KorzekwaD
 *
 */
trait IEventProducer {

	/**Transforms state of market on a betting exchange into the stream of events. First call of this method will return a CREATE MARKET event and then bet placement events. 
	 * Next call of this method will return delta of the market state between subsequent calls
	 * 
	 * @return List of market events in a json format (PLACE_BET, CANCEL_BET) for a market
	 * */
	def produce():List[String]
}