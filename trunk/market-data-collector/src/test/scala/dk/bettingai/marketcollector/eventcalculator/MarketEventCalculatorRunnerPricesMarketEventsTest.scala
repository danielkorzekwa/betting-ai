package dk.bettingai.marketcollector.eventcalculator

import org.junit._
import Assert._
import dk.bettingai.marketsimulator.betex.Market._

class MarketEventCalculatorRunnerPricesMarketEventsTest {


	/**
	 * Tests scenarios for calculateMarketEvents 
	 * */


	@Test(expected=classOf[IllegalArgumentException]) 
	def testPriceWithBothToBackAndToLayArePositive {
		val marketRunnerDelta = new RunnerPrice(1.85,3,8):: Nil
		val marketEvents = MarketEventCalculator.calculateMarketEvents(10,1000)(marketRunnerDelta)
	}
	
	@Test(expected=classOf[IllegalArgumentException]) 
	def testPriceWithBothToBackAndToLayAreNegative {
		val marketRunnerDelta = new RunnerPrice(1.85,-3,-8):: Nil
		val marketEvents = MarketEventCalculator.calculateMarketEvents(10,1000)(marketRunnerDelta)
	}
	
	@Test def testCalculateMarketEventsDeltaIsEmpty {
		val marketEvents = MarketEventCalculator.calculateMarketEvents(10,1000)(List())
		assertEquals(0, marketEvents.size)
	}

	@Test def testCalculateMarketEventsDeltaIsZero {
		val marketRunnerDelta = new RunnerPrice(1.85,0,0) :: new RunnerPrice(1.9,0,0) :: Nil

		val marketEvents = MarketEventCalculator.calculateMarketEvents(10,1000)(marketRunnerDelta)
		assertEquals(0, marketEvents.size)
	}
	
	@Test def testCalculateMarketEventsLayBetsAreGenerated {
		val marketRunnerDelta = new RunnerPrice(1.85,20,0) :: new RunnerPrice(1.9,2,0) :: Nil

		val marketEvents = MarketEventCalculator.calculateMarketEvents(10,1000)(marketRunnerDelta)

		val expectedEvents = 
			"""{"eventType":"PLACE_BET","betSize":20.0,"betPrice":1.85,"betType":"LAY","marketId":10,"runnerId":1000}""" ::
				"""{"eventType":"PLACE_BET","betSize":2.0,"betPrice":1.9,"betType":"LAY","marketId":10,"runnerId":1000}""" :: 
					Nil

		assertEquals(2, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
	}

	@Test def testCalculateMarketEventsBackBetsAreGenerated {
		val marketRunnerDelta = new RunnerPrice(1.95,0,3) :: new RunnerPrice(2,0,30) :: Nil

		val marketEvents = MarketEventCalculator.calculateMarketEvents(10,1000)(marketRunnerDelta)

		val expectedEvents = 
			"""{"eventType":"PLACE_BET","betSize":3.0,"betPrice":1.95,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
				"""{"eventType":"PLACE_BET","betSize":30.0,"betPrice":2.0,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
					Nil

		assertEquals(2, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
	}

@Test def testCalculateMarketEventsLayBetsAreCancelled {
		val marketRunnerDelta = new RunnerPrice(1.85,-20,0) :: new RunnerPrice(1.9,-2,0) :: Nil

		val marketEvents = MarketEventCalculator.calculateMarketEvents(10,1000)(marketRunnerDelta)

		val expectedEvents = 
			"""{"eventType":"CANCEL_BETS","betsSize":20.0,"betPrice":1.85,"betType":"LAY","marketId":10,"runnerId":1000}""" ::
			"""{"eventType":"CANCEL_BETS","betsSize":2.0,"betPrice":1.9,"betType":"LAY","marketId":10,"runnerId":1000}""" :: 
			Nil

		assertEquals(2, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
	}

@Test def testCalculateMarketEventsBackBetsAreCancelled {
		val marketRunnerDelta = new RunnerPrice(1.85,0,-3) :: new RunnerPrice(1.9,0,-7) :: Nil

		val marketEvents = MarketEventCalculator.calculateMarketEvents(10,1000)(marketRunnerDelta)

		val expectedEvents = 
			"""{"eventType":"CANCEL_BETS","betsSize":3.0,"betPrice":1.85,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
			"""{"eventType":"CANCEL_BETS","betsSize":7.0,"betPrice":1.9,"betType":"BACK","marketId":10,"runnerId":1000}""" :: 
			Nil

		assertEquals(2, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
	}

@Test def testCalculateEventsForLayToBackChange {
		val marketRunnerDelta = new RunnerPrice(1.85,9,-30) :: Nil

		val marketEvents = MarketEventCalculator.calculateMarketEvents(10,1000)(marketRunnerDelta)

		val expectedEvents = 
			"""{"eventType":"CANCEL_BETS","betsSize":30.0,"betPrice":1.85,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
				"""{"eventType":"PLACE_BET","betSize":9.0,"betPrice":1.85,"betType":"LAY","marketId":10,"runnerId":1000}""" ::
		Nil

		assertEquals(2, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
}

@Test def testCalculateEventsForBackToLayChange {
		val marketRunnerDelta = new RunnerPrice(1.85,-9,30) :: Nil

		val marketEvents = MarketEventCalculator.calculateMarketEvents(10,1000)(marketRunnerDelta)

		val expectedEvents = 
			"""{"eventType":"CANCEL_BETS","betsSize":9.0,"betPrice":1.85,"betType":"LAY","marketId":10,"runnerId":1000}""" ::
				"""{"eventType":"PLACE_BET","betSize":30.0,"betPrice":1.85,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
		Nil

		assertEquals(2, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
}
}