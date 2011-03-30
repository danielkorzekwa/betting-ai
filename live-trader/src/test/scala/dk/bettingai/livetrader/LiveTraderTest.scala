package dk.bettingai.livetrader

import org.junit._
import Assert._
import dk.bettingai.marketsimulator.trader._
import dk.bettingai.marketcollector.marketservice._
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

  private val marketId = 1
  private val numOfWinners = 3
  private val interval = 10
  private val marketTime = new Date(2000)
  private val marketRunners: List[RunnerDetails] = Nil
  private val marketDetails = new MarketDetails(marketId, "market Name", "menuPath", numOfWinners, marketTime, marketRunners)

  private val commission = 0.05

  private val mockery = new Mockery()
  private val marketService = mockery.mock(classOf[IMarketService])
  mockery.checking(new SExpectations() {
    {
      one(marketService).getMarketDetails(marketId); will(returnValue(marketDetails))
    }
  })

  private val liveTrader = LiveTrader(trader, marketId, interval, marketService, commission)

  @Test
  def liveTraderStart {
    assertEquals(0, trader.executed)
    assertEquals(0, trader.initialised)
    assertEquals(0, trader.finalised)

    liveTrader.start
    Thread.sleep(50)
    assertTrue("Trader was called %s times only.".format(trader.executed), trader.executed > 3)
    liveTrader.stop
  }

  @Test
  def liveTraderStop {
    liveTrader.start
    liveTrader.stop
    val numOfCalls = trader.executed
    Thread.sleep(50)
    assertEquals(numOfCalls, trader.executed)
  }

  @Test
  def traderInit {
    liveTrader.start
    Thread.sleep(50)
    assertEquals(1, trader.initialised)
    assertEquals(0, trader.finalised)
    liveTrader.stop

  }

  @Test
  def traderAfter {
    liveTrader.start
    liveTrader.stop
    assertEquals(1, trader.initialised)
    assertEquals(1, trader.finalised)
  }

  private case class TestTrader extends ITrader {
    var executed = 0
    var initialised = 0
    var finalised = 0

    override def init(ctx: ITraderContext) = initialised += 1
    override def after(ctx: ITraderContext) = finalised += 1

    def execute(ctx: ITraderContext) = {
      require(ctx != null, "Trader Context is null")
      executed += 1
    }
  }

  /**The 'with' method from jmock can't be used in Scala, therefore it's changed to 'withArg' method*/
  private class SExpectations extends Expectations {
    def withArg[T](matcher: Matcher[T]): T = super.`with`(matcher)
    def withArg(value: Long): Long = super.`with`(value)
    def withArg(value: String): String = super.`with`(value)
    def withArg(value: Int): Int = super.`with`(value)
    def withArg[Any](matcher: Any): Any = super.`with`(matcher)
  }

}