package dk.bettingai.trader.hillclimbing.price

import dk.bettingai.marketsimulator.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.betex.PriceUtil._
import scala.util._
import org.slf4j.LoggerFactory
import org.apache.commons.math.random._

/** Simple trading strategy based on price variable only. It is searching for optimal price adopting hill climbing algorithm (http://en.wikipedia.org/wiki/Hill_climbing). 
 *  This trader is just a proof of concept for hill climbing approach and doesn't represents any bigger value.
 * 
 * @author korzekwad
 *  
 */
class HillClimbingPriceTrader extends ITrader {

  private val log = LoggerFactory.getLogger(getClass)

  val bank=1000d
  
  val marketId = 101655622
  val runnerId = 4207432

  val rand = new Random(System.currentTimeMillis)
  var bestPrice = 2.22
  var bestExpectedProfit = Double.MinValue
  var candidate = Double.NaN

  /**It is called once for every analysed market.
   * 
   * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market
   * */
  override def init(ctx: ITraderContext) { candidate = move(bestPrice,rand.nextInt(11)-5) }

  /**Executes trader implementation so it can analyse market on a betting exchange and take appropriate bet placement decisions.
   * 
   * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
   */

  def execute(ctx: ITraderContext) = {

    if (ctx.marketId == marketId) {
      val bestPrices = ctx.getBestPrices(runnerId)
      if (bestPrices._1.price > candidate) ctx.fillBet(2, bestPrices._1.price, BACK, runnerId)
    }
  }

  /**It is called once for every analysed market, after market simulation is finished.
   * 
   * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market
   * */
  override def after(ctx: ITraderContext) {
    if (ctx.risk(bank).marketExpectedProfit > bestExpectedProfit) {
      bestPrice = candidate
      bestExpectedProfit = ctx.risk(bank).marketExpectedProfit

      log.info("Best price found = " + candidate + ", profit=" + bestExpectedProfit)
    } else log.info("Best price not found = " + candidate + ", profit=" + ctx.risk(bank).marketExpectedProfit + ", current best [price/profit]=" + bestPrice + "/" + bestExpectedProfit)
  }
}