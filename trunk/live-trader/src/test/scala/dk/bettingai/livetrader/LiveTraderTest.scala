package dk.bettingai.livetrader

import org.junit._
import Assert._
import dk.bettingai.marketsimulator.trader._
import dk.betex.eventcollector.marketservice._
import IMarketService._
import org.junit.runner._
import org.jmock.integration.junit4._
import org.jmock._
import org.hamcrest._
import org.hamcrest.Matchers._
import org.jmock.Expectations._
import java.util.Date

@RunWith(value = classOf[JMock])
class LiveTraderTest {

  private val trader = TestTrader()

  private val marketId = 1l
  private val numOfWinners = 3
  private val interval = 10
  private val marketTime = new Date(2000)
  private val marketRunners: List[RunnerDetails] = Nil
  private val marketDetails = new MarketDetails(marketId, "market Name", "menuPath", numOfWinners, marketTime, marketRunners)

  private val commission = 0.05
  private val bank = 100
  private val mockery = new Mockery()
  private val marketService = mockery.mock(classOf[IMarketService])

  private val menuPathFilter = "Strat 1st Apr"

  private val liveTrader = LiveTrader(trader, interval, marketService, commission, bank, -60, 12, 60, menuPathFilter)

  @Test
  def liveTraderStart {
    val marketPrices = MarketPrices(0, Map())
    mockery.checking(new SExpectations() {
      {
        atLeast(1).of(marketService).getUserBets(marketId, None); will(returnValue(Nil))
        atLeast(1).of(marketService).getUserMatchedBets(withArg(marketId), withArg(Matchers.any(classOf[Date]))); will(returnValue(Nil))
        atLeast(1).of(marketService).getMarketPrices(1l); will(returnValue(marketPrices))
        one(marketService).getMarketDetails(marketId); will(returnValue(marketDetails))
        one(marketService).getMarkets(withArg(Matchers.any(classOf[Date])), withArg(Matchers.any(classOf[Date])), withArg(Option(menuPathFilter)),withArg1(None)); will(returnValue(List(1l)))
      }
    })

    assertEquals(0, trader.executed)
    assertEquals(0, trader.initialised)
    assertEquals(0, trader.finalised)

    liveTrader.start
    Thread.sleep(200)
    assertTrue("Trader was called %s times only.".format(trader.executed), trader.executed > 3)
    liveTrader.stop
  }

  @Test
  def liveTraderStartMoreThanOneMarketAreDiscovered {
    mockery.checking(new SExpectations() {
      {
        one(marketService).getMarkets(withArg(Matchers.any(classOf[Date])), withArg(Matchers.any(classOf[Date])),withArg(Option(menuPathFilter)),withArg1(None)); will(returnValue(List(1l, 2l)))
      }
    })

    assertEquals(0, trader.executed)
    assertEquals(0, trader.initialised)
    assertEquals(0, trader.finalised)

    liveTrader.start
    Thread.sleep(200)
    assertTrue("Trader was called %s times.".format(trader.executed), trader.executed == 0)

    liveTrader.stop

  }

  @Test
  def liveTraderStartZeroMarketAreDiscovered {
    mockery.checking(new SExpectations() {
      {
        one(marketService).getMarkets(withArg(Matchers.any(classOf[Date])), withArg(Matchers.any(classOf[Date])), withArg(Option(menuPathFilter)),withArg1(None)); will(returnValue(List()))
      }
    })

    assertEquals(0, trader.executed)
    assertEquals(0, trader.initialised)
    assertEquals(0, trader.finalised)

    liveTrader.start
    Thread.sleep(200)
    assertTrue("Trader was called %s times.".format(trader.executed), trader.executed == 0)

    liveTrader.stop

  }

  @Test
  def liveTraderStop {
    val marketPrices = MarketPrices(0, Map())
    mockery.checking(new SExpectations() {
      {
        atLeast(1).of(marketService).getUserBets(1l, None); will(returnValue(Nil))
        atLeast(1).of(marketService).getUserMatchedBets(withArg(marketId), withArg(Matchers.any(classOf[Date]))); will(returnValue(Nil))
        atLeast(1).of(marketService).getMarketPrices(1l); will(returnValue(marketPrices))
        one(marketService).getMarketDetails(marketId); will(returnValue(marketDetails))
        one(marketService).getMarkets(withArg(Matchers.any(classOf[Date])), withArg(Matchers.any(classOf[Date])), withArg(Option(menuPathFilter)),withArg1(None)); will(returnValue(List(1l)))
      }
    })

    liveTrader.start
    liveTrader.stop
    val numOfCalls = trader.executed
    Thread.sleep(50)
    assertEquals(numOfCalls, trader.executed)
  }

  @Test
  def traderInit {
    val marketPrices = MarketPrices(0, Map())
    mockery.checking(new SExpectations() {
      {
        atLeast(1).of(marketService).getUserBets(1l, None); will(returnValue(Nil))
        atLeast(1).of(marketService).getUserMatchedBets(withArg(marketId), withArg(Matchers.any(classOf[Date]))); will(returnValue(Nil))
        atLeast(1).of(marketService).getMarketPrices(1l); will(returnValue(marketPrices))
        one(marketService).getMarketDetails(marketId); will(returnValue(marketDetails))
        one(marketService).getMarkets(withArg(Matchers.any(classOf[Date])), withArg(Matchers.any(classOf[Date])), withArg(Option(menuPathFilter)),withArg1(None)); will(returnValue(List(1l)))
      }
    })

    liveTrader.start
    Thread.sleep(50)
    assertEquals(1, trader.initialised)
    assertEquals(0, trader.finalised)
    liveTrader.stop

  }

  @Test
  def traderAfter {
    val marketPrices = MarketPrices(0, Map())
    mockery.checking(new SExpectations() {
      {
        atLeast(1).of(marketService).getUserBets(1l, None); will(returnValue(Nil))
        atLeast(1).of(marketService).getUserMatchedBets(withArg(marketId), withArg(Matchers.any(classOf[Date]))); will(returnValue(Nil))
        atLeast(1).of(marketService).getMarketPrices(1l); will(returnValue(marketPrices))
        one(marketService).getMarketDetails(marketId); will(returnValue(marketDetails))
        one(marketService).getMarkets(withArg(Matchers.any(classOf[Date])), withArg(Matchers.any(classOf[Date])), withArg(Option(menuPathFilter)),withArg1(None)); will(returnValue(List(1l)))
      }
    })

    liveTrader.start
    liveTrader.stop
    assertEquals(1, trader.initialised)
    assertEquals(1, trader.finalised)
  }

  private case class TestTrader extends ITrader {
    var executed = 0
    var initialised = 0
    var finalised = 0

    override def init(ctx: ITraderContext) = {
      require(ctx.getEventTimestamp > 0, "Event time stamp is wrong:" + ctx.getEventTimestamp)
      initialised += 1
    }
    override def after(ctx: ITraderContext) = finalised += 1

    def execute(ctx: ITraderContext) = {
      require(ctx != null, "Trader Context is null")
      require(ctx.getEventTimestamp > 0, "Event time stamp is wrong:" + ctx.getEventTimestamp)
      executed += 1
    }
  }

  /**The 'with' method from jmock can't be used in Scala, therefore it's changed to 'withArg' method*/
  private class SExpectations extends Expectations {
    def withArg[T](matcher: Matcher[T]): T = super.`with`(matcher)
    def withArg(value: Long): Long = super.`with`(value)
    def withArg(value: String): String = super.`with`(value)
    def withArg(value: Option[String]): Option[String] = super.`with`(value)
    def withArg(value: Int): Int = super.`with`(value)
    def withArg1(value: Option[Int]): Option[Int] = super.`with`(value)
    def withArg(value: Date): Date = super.`with`(value)
    def withArg[Any](matcher: Any): Any = super.`with`(matcher)
  }

}