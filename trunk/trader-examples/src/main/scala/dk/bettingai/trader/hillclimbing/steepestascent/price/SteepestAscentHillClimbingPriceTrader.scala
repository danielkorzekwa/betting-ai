package dk.bettingai.trader.hillclimbing.steepestascent.price

import dk.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import dk.bettingai.marketsimulator.trader._
import dk.betex._
import dk.betex.PriceUtil._
import scala.util._
import org.slf4j.LoggerFactory
import org.apache.commons.math.random._
import dk.bettingai.trader.hillclimbing.price._
import SteepestAscentHillClimbingPriceTrader._

/** Simple trading strategy based on price variable only. It is searching for optimal price adopting steepest ascent hill climbing algorithm (http://en.wikipedia.org/wiki/Hill_climbing). 
 *  This trader is just a proof of concept for hill climbing approach and doesn't represents any bigger value.
 * 
 * @author korzekwad
 *  
 */
object SteepestAscentHillClimbingPriceTrader {

  class ChildTrader(val price: Double) extends ITrader {

    val marketId = 101655622
    val runnerId = 4207432

    def execute(ctx: ITraderContext) = {
      if (ctx.marketId == marketId) {
        val bestPrices = ctx.getBestPrices(runnerId)
        if (bestPrices._1.price > price) ctx.fillBet(2, bestPrices._1.price, BACK, runnerId)
      }
    }
  }

}

class SteepestAscentHillClimbingPriceTrader extends ITrader {

  private val log = LoggerFactory.getLogger(getClass)

  val bank=1000d
  val rand = new Random(System.currentTimeMillis)
  var bestPrice = 2.22
  var bestExpectedProfit = Double.MinValue

  var children: Iterable[Tuple2[ChildTrader, ITraderContext]] = _

  /**It is called once for every analysed market.
   * 
   * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market
   * */
  override def init(ctx: ITraderContext) {
    /** Born 10 traders and search for the best solution.*/
    children = for (i <- 0 until 10; price = move(bestPrice, rand.nextInt(11) - 5)) yield new ChildTrader(price) -> ctx.registerTrader()
  }
  /**Executes trader implementation so it can analyse market on a betting exchange and take appropriate bet placement decisions.
   * 
   * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
   */

  def execute(ctx: ITraderContext) = {
    /**Trigger all child traders.*/
    children.foreach { case (trader, context) => trader.execute(context) }
  }

  /**It is called once for every analysed market, after market simulation is finished.
   * 
   * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market
   * */
  override def after(ctx: ITraderContext) {
    val bestChild = children.reduceLeft((c1, c2) => if (c1._2.risk(bank).marketExpectedProfit > c2._2.risk(bank).marketExpectedProfit) c1 else c2)

    if (bestChild._2.risk(bank).marketExpectedProfit > bestExpectedProfit) {
      bestPrice = bestChild._1.price
      bestExpectedProfit = bestChild._2.risk(bank).marketExpectedProfit

      log.info("Best price found = " + bestPrice + ", profit=" + bestExpectedProfit)
    } else log.info("Best price not found = " + bestChild._1.price + ", profit=" + bestChild._2.risk(bank).marketExpectedProfit + ", current best [price/profit]=" + bestPrice + "/" + bestExpectedProfit)
  }
}