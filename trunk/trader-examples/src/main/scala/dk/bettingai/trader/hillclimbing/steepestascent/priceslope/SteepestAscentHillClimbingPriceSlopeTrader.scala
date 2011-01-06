package dk.bettingai.trader.hillclimbing.steepestascent.priceslope

import scala.collection.JavaConversions._
import dk.bettingai.marketsimulator.betex.api._
import IMarket._
import IBet.BetTypeEnum._
import IBet.BetStatusEnum._
import dk.bettingai.marketsimulator.trader._
import ITrader._
import dk.bettingai.marketsimulator.betex._
import dk.bettingai.marketsimulator.betex.PriceUtil._
import scala.util._
import org.slf4j.LoggerFactory
import org.apache.commons.math.random._
import dk.bettingai.trader.hillclimbing.price._
import SteepestAscentHillClimbingPriceSlopeTrader._
import com.espertech.esper.client._
import com.espertech.esper.client.time._
import org.apache.commons.math.util.MathUtils._
import dk.bettingai.marketsimulator.risk._
/** Simple trading strategy based on price slope variable.
 * 
 * @author korzekwad
 *  
 */
object SteepestAscentHillClimbingPriceSlopeTrader {

  class ChildTrader(val childId: String, val backPriceSlopeSignal: Double, val layPriceSlopeSignal: Double) extends ITrader {

    val config = new Configuration()
    config.getEngineDefaults().getThreading().setInternalTimerEnabled(false)

    val probEventProps = new java.util.HashMap[String, Object]
    probEventProps.put("runnerId", "long")
    probEventProps.put("prob", "double")
    probEventProps.put("timestamp", "long")
    config.addEventType("ProbEvent", probEventProps)

    val epService: EPServiceProvider = EPServiceProviderManager.getProvider(childId, config)

    val expression = "select runnerId, slope from ProbEvent.std:groupwin(runnerId).win:time(120 sec).stat:linest(timestamp,prob, runnerId)"
    var stmt: EPStatement = null

    override def init(ctx: ITraderContext) {
      epService.initialize()
      stmt = epService.getEPAdministrator().createEPL(expression)
    }

    def execute(ctx: ITraderContext) = {

      /**Send new time event to esper.*/
      epService.getEPRuntime().sendEvent(new CurrentTimeEvent(ctx.getEventTimestamp))

      /**Sent events to esper.*/
      for (runnerId <- ctx.runners.map(_.runnerId)) {
        val bestPrices = ctx.getBestPrices(runnerId)
        val avgPrice = PriceUtil.avgPrice(bestPrices._1.price -> bestPrices._2.price)
        epService.getEPRuntime().sendEvent(Map("runnerId" -> runnerId, "prob" -> 100 / avgPrice, "timestamp" -> ctx.getEventTimestamp / 1000), "ProbEvent")
      }
      /**Pull epl statement and execute trading strategy.*/
      for (event <- stmt.iterator) {
        val runnerId = event.get("runnerId").asInstanceOf[Long]
        val priceSlope = -1 * event.get("slope").asInstanceOf[Double] //convert prob slope to price slope
        val bestPrices = ctx.getBestPrices(runnerId)

        val probs = ProbabilityCalculator.calculate(ctx.getBestPrices.mapValues(prices => prices._1.price -> prices._2.price), 1)

        val matchedBetsBack = List(new Bet(1, 1, 2, bestPrices._1.price, BACK, M, ctx.marketId, runnerId))
        val riskBack = ExpectedProfitCalculator.calculate(matchedBetsBack, probs, ctx.commission)

        val matchedBetsLay = List(new Bet(1, 1, 2, bestPrices._2.price, LAY, M, ctx.marketId, runnerId))
        val riskLay = ExpectedProfitCalculator.calculate(matchedBetsLay, probs, ctx.commission)

        if (priceSlope < backPriceSlopeSignal && riskBack.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._1.price, BACK, runnerId)
        if (priceSlope > layPriceSlopeSignal && riskLay.marketExpectedProfit > -0.2) ctx.fillBet(2, bestPrices._2.price, LAY, runnerId)

      }

    }
  }

}

class SteepestAscentHillClimbingPriceSlopeTrader extends ITrader {

  private val log = LoggerFactory.getLogger(getClass)

  val rand = new Random(System.currentTimeMillis)
  var bestBackPriceSlopeSignal = -0.01
  var bestLayPriceSlopeSignal = 0.01
  var bestExpectedProfit = Double.MinValue

  var children: Iterable[Tuple2[ChildTrader, ITraderContext]] = _

  /**It is called once for every analysed market.
   * 
   * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market
   * */
  override def init(ctx: ITraderContext) {
    log.info("Initilising trader for market=" + ctx.marketId)
  	/** Born 5 traders and search for the best solution.*/
    children = for {
      i <- 0 until 5
      val backPriceSlopeSignal = bestBackPriceSlopeSignal + ((rand.nextInt(11) - 5) * 0.001)
      val layPriceSlopeSignal = bestLayPriceSlopeSignal + ((rand.nextInt(11) - 5) * 0.001)
    } yield new ChildTrader("childTrader" + i, backPriceSlopeSignal, layPriceSlopeSignal) -> ctx.registerTrader()

    children.foreach { case (trader, context) => trader.init(context) }
  }
  /**Executes trader implementation so it can analyse market on a betting exchange and take appropriate bet placement decisions.
   * 
   * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market.
   */

  def execute(ctx: ITraderContext) = {
    /**Trigger all child traders.*/
    for ((trader, childCtx) <- children) {
      childCtx.setEventTimestamp(ctx.getEventTimestamp)
      trader.execute(childCtx)
    }

  }

  /**It is called once for every analysed market, after market simulation is finished.
   * 
   * @param ctx Provides market data and market operations that can be used by trader to place bets on a betting exchange market
   * */
  override def after(ctx: ITraderContext) {
    
    val bestChild = children.reduceLeft((c1, c2) => if (c1._2.risk.marketExpectedProfit > c2._2.risk.marketExpectedProfit) c1 else c2)

    if (bestChild._2.risk.marketExpectedProfit > bestExpectedProfit) {
      bestBackPriceSlopeSignal = bestChild._1.backPriceSlopeSignal
      bestLayPriceSlopeSignal = bestChild._1.layPriceSlopeSignal
      bestExpectedProfit = bestChild._2.risk.marketExpectedProfit

      log.info("Best found [backPriceSlopeSignal/layPriceSlopeSignal] = " + bestBackPriceSlopeSignal + "/" + bestLayPriceSlopeSignal + ", profit=" + bestExpectedProfit)
    } else log.info("Best price not found [backPriceSlopeSignal/layPriceSlopeSignal] = " + bestChild._1.backPriceSlopeSignal + "/" + bestChild._1.layPriceSlopeSignal + ", profit=" + bestChild._2.risk.marketExpectedProfit + ", current best [backPriceSlopeSignal/layPriceSlopeSignal/profit]=" + bestBackPriceSlopeSignal + "/" + bestLayPriceSlopeSignal + "/" + bestExpectedProfit)
  }
}