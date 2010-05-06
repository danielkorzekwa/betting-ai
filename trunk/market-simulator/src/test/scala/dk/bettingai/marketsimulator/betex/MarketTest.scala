package dk.bettingai.marketsimulator.betex

import org.junit._
import Assert._
import java.util.Date

class MarketTest {

	@Test
	def testCreateMarket {
		new Market(10,"Match Odds","Man Utd vs Arsenal",2,new Date(2000),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))
	}

	@Test(expected=classOf[IllegalArgumentException]) 
	def testCreateMarketWrongNumOfWinners {
		new Market(10,"Match Odds","Man Utd vs Arsenal",0,new Date(2000),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))
	}

	@Test(expected=classOf[IllegalArgumentException]) 
	def testCreateMarketWrongNumOfSelections {
		new Market(10,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Selection(11,"Man Utd")))
	}
}