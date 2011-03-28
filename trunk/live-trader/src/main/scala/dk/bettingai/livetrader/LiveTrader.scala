package dk.bettingai.livetrader

import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketcollector.marketservice._
import IMarketService._
import scala.actors.Actor
import Actor._

/**This class allows for running trader on a betting exchange market. Trader observes a market and places some bets.
 * 
 * @author korzekwad
 *
 * @param trader Trader, which listens on a betting exchange market.
 * @param marketId Unique identifier of a betting exchange market to listen on.
 * @param interval How often (in milliseconds) trader is triggered with a new data obtained from a betting exchange market.
 * @param marketService Adapter for a betting exchange.
 */

/**LiveTrader actor messages.*/
case object StartTrader
case object StopTrader
case object ExecuteTrader

case class LiveTrader(trader: ITrader, marketId: Long, interval: Long, marketService: IMarketService) {

  private val liveTraderActor = actor {

    var marketDetails: Option[MarketDetails] = None

    loop {
      react {
        case StartTrader => {
          marketDetails = Some(marketService.getMarketDetails(marketId))
          LiveTraderExecutor.execute(ctx => trader.init(ctx), marketId, marketDetails.get,marketService)
          self ! ExecuteTrader
          reply
        }
        case StopTrader => {
          LiveTraderExecutor.execute(ctx => trader.after(ctx), marketId, marketDetails.get,marketService)
          reply
          exit
        }
        case ExecuteTrader => {
          LiveTraderExecutor.execute(ctx => trader.execute(ctx), marketId, marketDetails.get,marketService)
          Thread.sleep(interval)
          self ! ExecuteTrader
        }
      }
    }
  }
  /**Start live trader.*/
  def start() = liveTraderActor !? StartTrader

  /**Stop live trader.*/
  def stop() = liveTraderActor !? StopTrader
}