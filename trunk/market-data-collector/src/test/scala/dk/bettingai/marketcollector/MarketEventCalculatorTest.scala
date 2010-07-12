package dk.bettingai.marketcollector

import org.junit._
import Assert._
import dk.bettingai.marketsimulator.betex.Market._

class MarketEventCalculatorTest {


	/**
	 * Tests scenarios for calculateMarketEvents 
	 * */

	@Test def testCalculateMarketEventsDeltaIsEmpty {
		val marketEvents = MarketEventCalculator.calculateMarketEvents(10,1000)(List())
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

	@Test def testCalculateMarketEventsBothBackAndLayBetsAreGenerated {
		val marketRunnerDelta = new RunnerPrice(1.95,7,3) :: new RunnerPrice(2,5,30) :: Nil

		val marketEvents = MarketEventCalculator.calculateMarketEvents(10,1000)(marketRunnerDelta)

		val expectedEvents = 
			"""{"eventType":"PLACE_BET","betSize":7.0,"betPrice":1.95,"betType":"LAY","marketId":10,"runnerId":1000}""" ::
			"""{"eventType":"PLACE_BET","betSize":5.0,"betPrice":2.0,"betType":"LAY","marketId":10,"runnerId":1000}""" ::
			"""{"eventType":"PLACE_BET","betSize":3.0,"betPrice":1.95,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
			"""{"eventType":"PLACE_BET","betSize":30.0,"betPrice":2.0,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
			Nil

		assertEquals(4, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
		assertEquals(expectedEvents(2),marketEvents(2))
		assertEquals(expectedEvents(3),marketEvents(3))
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
}