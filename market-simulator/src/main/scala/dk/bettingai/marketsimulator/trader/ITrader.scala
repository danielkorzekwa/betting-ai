package dk.bettingai.marketsimulator.trader

import dk.bettingai.marketsimulator.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import java.util.Date
import dk.bettingai.marketsimulator.risk._
import scala.collection._

/**This trait represents a trader that can place bets on a betting exchange.
 * 
 * Warning! Trader can be executed in parallel for many markets. 
 * The only one object that is thread safe is TraderContext,which is passed to 'init','execute' and 'after' methods of Trader.
 * It's a trader's responsibility to make Trader instance level's variables to be thread-safe.
 *  
 * @author korzekwad
 *
 */
trait ITrader {

  /**It is called once for every analysed market.
   * 
   * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
   * */
  def init(ctx: ITraderContext) {}

  /**Executes trader implementation so it can analyse market on a betting exchange and take appropriate bet placement decisions.
   * 
   * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
   */
  def execute(ctx: ITraderContext)

  /**It is called once for every analysed market, after market simulation is finished.
   * 
   * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market
   * */
  def after(ctx: ITraderContext) {}

}