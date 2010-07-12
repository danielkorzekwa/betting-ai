package dk.bettingai.marketsimulator.marketevent

import org.junit._
import Assert._
import org.jmock._
import org.jmock.Expectations._
import dk.bettingai.marketsimulator.betex.api._
import dk.bettingai.marketsimulator.betex._
import org.junit.runner._
import java.util.Date
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.Description
import org.hamcrest._
import org.hamcrest.Matchers._
import java.text._
import org.jmock.integration.junit4._

@RunWith(value=classOf[JMock])
class MarketEventProcessorImplTest {

	private val mockery = new Mockery()
	private val betex:IBetex = mockery.mock(classOf[IBetex])

	private val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	/**
	 * Tests for CREATE_MARKET event.
	 */

	@Test def testProcessCreateMarketEvent() {

		val market = new Market(10,"Match Odds","Man Utd vs Arsenal",1,df.parse("2010-04-15 14:00:00"),List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))
		mockery.checking(new SExpectations() {
			{
				one(betex).createMarket(withArg(10),withArg("Match Odds"),withArg("Man Utd vs Arsenal"),withArg(1),withArg(df.parse("2010-04-15 14:00:00")),withArg(new RunnersMatcher(List(new Market.Runner(11,"Man Utd"),new Market.Runner(12,"Arsenal")))))
			}
		})

		new MarketEventProcessorImpl(betex).process(new String("""
				{"eventType":"CREATE_MARKET",
				"marketId":10, 
				"marketName":"Match Odds",
				"eventName":"Man Utd vs Arsenal", 
				"numOfWinners":1, 
				"marketTime":
				"2010-04-15 14:00:00", 
				"runners": [{"runnerId":11,
				"runnerName":"Man Utd"},
				{"runnerId":12, 
				"runnerName":"Arsenal"}]
				}
		"""),0,123)
	}

	@Test(expected=classOf[ClassCastException])
	def testProcessCreateMarketEventMarketIdNotANumber() {
		new MarketEventProcessorImpl(betex).process(new String("""
				{"eventType":"CREATE_MARKET",
				"marketId":"not_a_number", 
				"marketName":"Match Odds",
				"eventName":"Man Utd vs Arsenal", 
				"numOfWinners":1, 
				"marketTime":
				"2010-04-15 14:00:00", 
				"runners": [{"runnerId":11,
				"runnerName":"Man Utd"},
				{"runnerId":12, 
				"runnerName":"Arsenal"}]
				}
		"""),0,123)
	}

	@Test(expected=classOf[NoSuchElementException])
	def testProcessCreateMarketEventNoMarketName() {
		new MarketEventProcessorImpl(betex).process(new String("""
				{"eventType":"CREATE_MARKET",
				"marketId":1, 
				"eventName":"Man Utd vs Arsenal", 
				"numOfWinners":1, 
				"marketTime":
				"2010-04-15 14:00:00", 
				"runners": [{"runnerId":11,
				"runnerName":"Man Utd"},
				{"runnerId":12, 
				"runnerName":"Arsenal"}]
				}
		"""),0,123)
	}

	/**
	 * Tests for PLACE_BET event.
	 */

	@Test def testProcessPlaceBetEventLay() {
		val market = mockery.mock(classOf[IMarket])
		mockery.checking(new SExpectations(){
			{
				one(betex).findMarket(1);will(returnValue(market))
				one(market).placeBet(100,123,10,3, IBet.BetTypeEnum.LAY, 11)
			}
		})

		new MarketEventProcessorImpl(betex).process(new String("""
				{"eventType":"PLACE_BET",	
				"betSize":10,
				"betPrice":3,
				"betType":"LAY",
				"marketId":1,
				"runnerId":11
				}
		"""),100,123)
	}

	@Test def testProcessPlaceBetEventBack() {
		val market = mockery.mock(classOf[IMarket])
		mockery.checking(new SExpectations(){
			{
				one(betex).findMarket(1);will(returnValue(market))
				one(market).placeBet(101,345,10,3, IBet.BetTypeEnum.BACK, 11)
			}
		})

		new MarketEventProcessorImpl(betex).process(new String("""
				{"eventType":"PLACE_BET",
				"betSize":10,
				"betPrice":3,
				"betType":"BACK",
				"marketId":1,
				"runnerId":11
				}
		"""),101,345)
	}

	@Test(expected=classOf[NoSuchElementException]) 
	def testProcessPlaceBetEventNotSupportedBetType() {
		val market = mockery.mock(classOf[IMarket])
		mockery.checking(new SExpectations(){
			{
				one(betex).findMarket(1);will(returnValue(market))
			}
		})
		new MarketEventProcessorImpl(betex).process(new String("""
				{"eventType":"PLACE_BET",
				"betSize":10,
				"betPrice":3,
				"betType":"NOT_SUPPORTED",
				"marketId":1,
				"runnerId":11
				}
		"""),0,123)
	}

	/**
	 * Tests for CANCEL_BETS event.
	 */
	@Test def testProcessCancelLayBets {
		val market = mockery.mock(classOf[IMarket])
		mockery.checking(new SExpectations(){
			{
				one(betex).findMarket(10);will(returnValue(market))
				one(market).cancelBets(124,3,1.85,IBet.BetTypeEnum.LAY,1000)
			}
		})

		new MarketEventProcessorImpl(betex).process("""{"eventType":"CANCEL_BETS","betsSize":3.0,"betPrice":1.85,"betType":"LAY","marketId":10,"runnerId":1000}""",0,124)
	}

	/**
	 * Tests for wrong event data.
	 */

	@Test(expected=classOf[IllegalArgumentException]) def testProcessEventNotInJSONFormat() {
		new MarketEventProcessorImpl(betex).process(new String(""),0,123)
	}
	@Test(expected=classOf[IllegalArgumentException]) def testProcessNoEventTypeAttribute() {
		new MarketEventProcessorImpl(betex).process(new String("{}"),0,123)
	}
	@Test(expected=classOf[IllegalArgumentException]) def testProcessNotSupportedEventType() {
		new MarketEventProcessorImpl(betex).process(new String("""
				{"eventType":"NOT_SUPPORTED_EVENT_NAME"}
		"""),0,123)
	}

	/**Check if both market runners lists are the same.*/
	private class RunnersMatcher(runners:List[Market.Runner]) extends TypeSafeMatcher[List[Market.Runner]] {

		def matchesSafely(s:List[Market.Runner]):Boolean = {

				if(s.length!=runners.length) return false
				for(i <- 0 until s.length) {
					if(s(i).runnerId!=runners(i).runnerId) return false
					if(s(i).runnerName!=runners(i).runnerName)return false
				}
				return true
		}

		def describeTo(description:Description) = {
			description.appendText("market equals to").appendValue(runners);
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