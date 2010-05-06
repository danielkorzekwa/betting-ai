package dk.bettingai.marketsimulator.betex

import org.junit._
import Assert._
import java.util.Date

class BetexImplTest {

	private val betex = new BetexImpl()

	@Test def testCreateMarket() {
		val market = new Market(10,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))
		betex.createMarket(market)
		assertEquals(1,betex.getActiveMarkets.size)

		val marketFromBetex = betex.getActiveMarkets()(0)
		assertEquals(10,marketFromBetex.marketId)
		assertEquals("Match Odds",marketFromBetex.marketName)
		assertEquals("Man Utd vs Arsenal",marketFromBetex.eventName)
		assertEquals(1,marketFromBetex.numOfWinners)
		assertEquals(new Date(2000),marketFromBetex.marketTime)

		assertEquals(11,marketFromBetex.selections(0).selectionId)
		assertEquals("Man Utd",marketFromBetex.selections(0).selectionName)
		assertEquals(12,marketFromBetex.selections(1).selectionId)
		assertEquals("Arsenal",marketFromBetex.selections(1).selectionName)
	}

	@Test def testCreateTwoMarkets() {
		val market1 = new Market(10,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))
		val market2 = new Market(20,"Match Odds","Fulham vs Wigan",1,new Date(2000),List(new Market.Selection(31,"Fulham"),new Market.Selection(42,"Wigan")))

		betex.createMarket(market1)
		betex.createMarket(market2)
		assertEquals(2,betex.getActiveMarkets.size)

		val marketFromBetex1 = betex.getActiveMarkets()(0)
		assertEquals(20,marketFromBetex1.marketId)
		assertEquals("Match Odds",marketFromBetex1.marketName)
		assertEquals("Fulham vs Wigan",marketFromBetex1.eventName)
		assertEquals(1,marketFromBetex1.numOfWinners)
		assertEquals(new Date(2000),marketFromBetex1.marketTime)

		assertEquals(31,marketFromBetex1.selections(0).selectionId)
		assertEquals("Fulham",marketFromBetex1.selections(0).selectionName)
		assertEquals(42,marketFromBetex1.selections(1).selectionId)
		assertEquals("Wigan",marketFromBetex1.selections(1).selectionName)

		val marketFromBetex2 = betex.getActiveMarkets()(1)
		assertEquals(10,marketFromBetex2.marketId)
		assertEquals("Match Odds",marketFromBetex2.marketName)
		assertEquals("Man Utd vs Arsenal",marketFromBetex2.eventName)
		assertEquals(1,marketFromBetex2.numOfWinners)
		assertEquals(new Date(2000),marketFromBetex2.marketTime)

		assertEquals(11,marketFromBetex2.selections(0).selectionId)
		assertEquals("Man Utd",marketFromBetex2.selections(0).selectionName)
		assertEquals(12,marketFromBetex2.selections(1).selectionId)
		assertEquals("Arsenal",marketFromBetex2.selections(1).selectionName)
	}


	@Test(expected=classOf[IllegalArgumentException]) def testCreateMarketAlreadyExist() {
		val market1 = new Market(10,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))
		val market2 = new Market(10,"Match Odds","Man Utd vs Arsenal",1,new Date(2000),List(new Market.Selection(11,"Man Utd"),new Market.Selection(12,"Arsenal")))

		betex.createMarket(market1)
		betex.createMarket(market2)
	}

}
