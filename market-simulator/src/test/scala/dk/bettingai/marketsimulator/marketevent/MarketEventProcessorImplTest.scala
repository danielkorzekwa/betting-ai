package dk.bettingai.marketsimulator.marketevent

import org.junit._
import Assert._
import org.jmock._
import dk.bettingai.marketsimulator.betex._
import org.junit.runner._
import java.util.Date
import org.hamcrest.TypeSafeMatcher
import org.hamcrest.Description
import org.hamcrest.Matcher
import java.text._

class MarketEventProcessorImplTest {

	private val mockery = new Mockery()
	private val betex:Betex = mockery.mock(classOf[Betex])

	private val df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
	
	@Test def testProcessCreateMarketEvent() {

		
		val market = new Market(1,"Match Odds","Man Utd vs Arsenal",1,df.parse("2010-04-15 14:00:00"),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))
		mockery.checking(new SExpectations() {
			{
				one(betex).createMarket(withArg(new StringStartsWithMatcher(market)))
			}
		});

		new MarketEventProcessorImpl(betex).process(new String("""
				{"eventType":"CREATE_MARKET",
				"marketId":1, 
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
		mockery.assertIsSatisfied()
	}

	/**Check if both market objects are the same.*/
	private class StringStartsWithMatcher(market:Market) extends TypeSafeMatcher[Market] {

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

	/**The 'with' method from jmock can't be used in Scala, therefore it's changed to 'withArg' method*/
	private class SExpectations extends Expectations {
		def withArg[T](matcher: Matcher[T]): T = super.`with`(matcher)  
	} 
}