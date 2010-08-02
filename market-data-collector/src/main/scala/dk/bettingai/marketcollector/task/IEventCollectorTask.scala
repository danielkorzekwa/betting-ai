package dk.bettingai.marketcollector.task

/**This trait represents a task that writes markets events to files for given set of markets (one file per market). 
 * Those market events represent delta between two market states, where a market state is defined by runner prices and traded volume. 
 * Calling this task with a given interval allows collecting market data that can be used to replay market on a betting exchange.
 * 
 * @author korzekwad
 *
 */
trait IEventCollectorTask {

	/** Executes EventCollectorTask. For more details, read class level comments.*/ 
	def execute()
}