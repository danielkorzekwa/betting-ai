package dk.bettingai.livetrader

import org.junit._
import Assert._
import java.util.Date
import dk.bettingai.marketcollector.marketservice._
import IMarketService._
import dk.bettingai.marketsimulator.betex.api.IBet._
import BetTypeEnum._
import BetStatusEnum._
import org.junit.runner._
import org.jmock.integration.junit4._
import org.jmock._
import org.hamcrest._
import org.hamcrest.Matchers._
import org.jmock.Expectations._
import dk.bettingai.marketsimulator.betex._

@RunWith(value = classOf[JMock])
class LiveTraderContextTest {

  private val marketId = 1
  private val numOfWinners = 3
  private val interval = 10
  private val marketTime = new Date(2000)
  private val marketRunners: List[RunnerDetails] = Nil
  private val marketDetails = new MarketDetails(marketId, "Soccer Jacpot", "Plump 28th Mar - 14:10 2m Mdn Hrd", numOfWinners, marketTime, marketRunners)

  private val mockery = new Mockery()
  private val marketService = mockery.mock(classOf[IMarketService])

  private val liveCtx = LiveTraderContext(marketDetails, marketService)

  @Test
  def marketDetailsAreCorrect {
    assertEquals(1, liveCtx.marketId)
    assertEquals("Soccer Jacpot", liveCtx.marketName)
    assertEquals("Plump 28th Mar - 14:10 2m Mdn Hrd", liveCtx.eventName)
    assertEquals(3, liveCtx.numOfWinners)
    assertEquals(new Date(2000), liveCtx.marketTime)
  }

  @Test
  def placeBet {
    val bet = Bet(100, 1000, 10, 2.2, BACK, 1, 11)
    mockery.checking(new SExpectations() {
      {
        one(marketService).placeBet(bet.betSize, bet.betPrice, bet.betType, bet.marketId, bet.runnerId); will(returnValue(bet))
      }
    })

    val placedBet = liveCtx.placeBet(bet.betSize, bet.betPrice, bet.betType, bet.runnerId)
    assertEquals(bet, placedBet)
  }

  @Test
  def fillBetFullyPlaced {
    val betToBePlaced = Bet(100, 1000, 10, 2.2, BACK, 1, 11)
    mockery.checking(new SExpectations() {
      {
        one(marketService).getUserBets(marketId, Option(U)); will(returnValue(Nil))
        one(marketService).placeBet(betToBePlaced.betSize, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.marketId, betToBePlaced.runnerId); will(returnValue(betToBePlaced))
      }
    })

    val placedBet = liveCtx.fillBet(betToBePlaced.betSize, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.runnerId)

    assertEquals(betToBePlaced, placedBet.get)
  }

  @Test
  def fillBetFullyPlacedOtherBetsAreAvailable {
    val existingBets =
      Bet(100, 1000, 10, 2.2, LAY, 1, 11) ::
        Bet(100, 1000, 10, 2.2, BACK, 1, 12) ::
        Bet(100, 1000, 10, 2.21, BACK, 1, 11) :: Nil
    val betToBePlaced = Bet(100, 1000, 10, 2.2, BACK, 1, 11)

    mockery.checking(new SExpectations() {
      {
        one(marketService).getUserBets(marketId, Option(U)); will(returnValue(existingBets))
        one(marketService).placeBet(betToBePlaced.betSize, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.marketId, betToBePlaced.runnerId); will(returnValue(betToBePlaced))
      }
    })

    val placedBet = liveCtx.fillBet(betToBePlaced.betSize, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.runnerId)

    assertEquals(betToBePlaced, placedBet.get)
  }

  @Test
  def fillBetPartiallyPlaced {
    val existingBet = Bet(100, 1000, 6, 2.2, BACK, 1, 11)
    val betToBePlaced = Bet(100, 1000, 4, 2.2, BACK, 1, 11)
    mockery.checking(new SExpectations() {
      {
        one(marketService).getUserBets(marketId, Option(U)); will(returnValue(existingBet :: Nil))
        one(marketService).placeBet(betToBePlaced.betSize, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.marketId, betToBePlaced.runnerId); will(returnValue(betToBePlaced))
      }
    })

    val placedBet = liveCtx.fillBet(10, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.runnerId)

    assertEquals(betToBePlaced, placedBet.get)
  }

  @Test
  def fillBetNothingPlaced {
    val betToBePlaced = Bet(100, 1000, 10, 2.2, BACK, 1, 11)
    mockery.checking(new SExpectations() {
      {
        one(marketService).getUserBets(marketId, Option(U)); will(returnValue(betToBePlaced :: Nil))
      }
    })

    val placedBet = liveCtx.fillBet(betToBePlaced.betSize, betToBePlaced.betPrice, betToBePlaced.betType, betToBePlaced.runnerId)

    assertTrue(placedBet.isEmpty)
  }

  @Test def getBestPricesForRunnerId {
	  liveCtx.getBestPrices(11)
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