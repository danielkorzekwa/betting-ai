package dk.bettingai.marketsimulator.marketevent

import org.junit._
import Assert._
import org.jmock._
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
	private val betex:Betex = mockery.mock(classOf[Betex])

	private val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

	@Test def testProcessCreateMarketEvent() {

		val market = new Market(10,"Match Odds","Man Utd vs Arsenal",1,df.parse("2010-04-15 14:00:00"),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))
		mockery.checking(new SExpectations() {
			{
				one(betex).createMarket(withArg(new MarketMatcher(market)))
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
				"selections": [{"selectionId":11,
				"selectionName":"Man Utd"},
				{"selectionId":12, 
				"selectionName":"Arsenal"}]
				}
		"""))
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
				"selections": [{"selectionId":11,
				"selectionName":"Man Utd"},
				{"selectionId":12, 
				"selectionName":"Arsenal"}]
				}
		"""))
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
				"selections": [{"selectionId":11,
				"selectionName":"Man Utd"},
				{"selectionId":12, 
				"selectionName":"Arsenal"}]
				}
		"""))
	}

	@Test def testProcessPlaceBetEventLay() {
		val bet = new Bet(100,123,10,3, Bet.BetTypeEnum.LAY, 1,11)
		mockery.checking(new SExpectations(){
			{
				one(betex).placeBet(withArg(new BetMatcher(bet)))
			}
		})

		new MarketEventProcessorImpl(betex).process(new String("""
				{"eventType":"PLACE_BET",
				"userId":123,
				"betId":100,	
				"betSize":10,
				"betPrice":3,
				"betType":"LAY",
				"marketId":1,
				"selectionId":11
				}
		"""))
	}

	@Test def testProcessPlaceBetEventBack() {
		val bet = new Bet(100,345,10,3, Bet.BetTypeEnum.BACK, 1,11)
		mockery.checking(new SExpectations(){
			{
				one(betex).placeBet(withArg(new BetMatcher(bet)))
			}
		})

		new MarketEventProcessorImpl(betex).process(new String("""
				{"eventType":"PLACE_BET",
				"userId":345,
				"betId":100,	
				"betSize":10,
				"betPrice":3,
				"betType":"BACK",
				"marketId":1,
				"selectionId":11
				}
		"""))
	}

	@Test(expected=classOf[NoSuchElementException]) 
	def testProcessPlaceBetEventNotSupportedBetType() {

		new MarketEventProcessorImpl(betex).process(new String("""
				{"eventType":"PLACE_BET",
				"userId":345,
				"betSize":10,
				"betPrice":3,
				"betType":"NOT_SUPPORTED",
				"marketId":1,
				"selectionId":11
				}
		"""))
	}

	@Test(expected=classOf[IllegalArgumentException]) def testProcessEventNotInJSONFormat() {
		new MarketEventProcessorImpl(betex).process(new String(""))
	}
	@Test(expected=classOf[IllegalArgumentException]) def testProcessNoEventTypeAttribute() {
		new MarketEventProcessorImpl(betex).process(new String("{}"))
	}
	@Test(expected=classOf[IllegalArgumentException]) def testProcessNotSupportedEventType() {
		new MarketEventProcessorImpl(betex).process(new String("""
				{"eventType":"NOT_SUPPORTED_EVENT_NAME"}
		"""))
	}

	/**Check if both market objects are the same.*/
	private class MarketMatcher(market:Market) extends TypeSafeMatcher[Market] {

		def matchesSafely(s:Market):Boolean = {
				if(s.marketId!=market.marketId) return false
				if(s.marketName!=market.marketName) return false
				if(s.eventName!=market.eventName) return false
				if(s.numOfWinners!=market.numOfWinners) return  false
				if(s.marketTime.getTime!=market.marketTime.getTime) return false
				if(s.selections.length!=market.selections.length) return false

				for(i <- 0 until s.selections.length) {
					if(s.selections(i).selectionId!=market.selections(i).selectionId) return false
					if(s.selections(i).selectionName!=market.selections(i).selectionName)return false
				}
				return true
		}

		def describeTo(description:Description) = {
			description.appendText("market equals to").appendValue(market);
		}
	}

	/**Check if both bet objects are the same.*/
	private class BetMatcher(bet:Bet) extends TypeSafeMatcher[Bet] {

		def matchesSafely(s:Bet):Boolean = {
				if(s.betId!=bet.betId) return false
				if(s.userId!=bet.userId) return false
				if(s.betSize!=bet.betSize) return false
				if(s.betPrice!=bet.betPrice) return false
				if(s.betType!=bet.betType) return false
				if(s.marketId!=bet.marketId) return false
				if(s.selectionId!=bet.selectionId) return false

				return true
		}

		def describeTo(description:Description) = {
			description.appendText("bet equals to").appendValue(bet);
		}
	}

	/**The 'with' method from jmock can't be used in Scala, therefore it's changed to 'withArg' method*/
	private class SExpectations extends Expectations {
		def withArg[T](matcher: Matcher[T]): T = super.`with`(matcher)  
	} 
}