package dk.bettingai.marketcollector

import org.junit._
import Assert._
import dk.bettingai.marketsimulator.betex.Market._

class MarketEventCalculatorTest {

	/**
	 * Tests scenarios for calculateRunnerPricesDelta.
	 * */

	@Test def testCalculateRunnerPricesDeltaBothRunnerPricesAreEmpty {
		val runnerPricesDelta = MarketEventCalculator.calculateRunnerPricesDelta(List(),List())
		assertEquals(0, runnerPricesDelta.size)
	}

	@Test def testCalculateRunnerPricesDeltaBothRunnerPricesAreTheSame {
		val newRunnerPrices = new RunnerPrice(1.9,10,0) :: new RunnerPrice(1.95,0,20) :: Nil
		val previousRunnerPrices = new RunnerPrice(1.9,10,0) :: new RunnerPrice(1.95,0,20) :: Nil

		val runnerPricesDelta = MarketEventCalculator.calculateRunnerPricesDelta(newRunnerPrices,previousRunnerPrices)
		assertEquals(0, runnerPricesDelta.size)
	}

	@Test def testCalculateRunnerPricesDeltaNewRunnerPriceIsAvailable {
		val newRunnerPrices = new RunnerPrice(1.85,15,7) :: new RunnerPrice(1.9,10,0) :: new RunnerPrice(1.95,0,20) :: Nil
		val previousRunnerPrices = new RunnerPrice(1.9,10,0) :: new RunnerPrice(1.95,0,20) :: Nil

		val runnerPricesDelta = MarketEventCalculator.calculateRunnerPricesDelta(newRunnerPrices,previousRunnerPrices)
		assertEquals(1, runnerPricesDelta.size)

		assertEquals(1.85,runnerPricesDelta(0).price,0)
		assertEquals(15,runnerPricesDelta(0).totalToBack,0)
		assertEquals(7,runnerPricesDelta(0).totalToLay,0)
	}

	@Test def testCalculateRunnerPricesDeltaRunnerPricesAreUpdated {
		val newRunnerPrices = new RunnerPrice(1.9,8,5) :: new RunnerPrice(1.95,6,12) :: Nil
		val previousRunnerPrices = new RunnerPrice(1.9,10,0) :: new RunnerPrice(1.95,0,20) :: Nil

		val runnerPricesDelta = MarketEventCalculator.calculateRunnerPricesDelta(newRunnerPrices,previousRunnerPrices)
		assertEquals(2, runnerPricesDelta.size)

		assertEquals(1.9,runnerPricesDelta(0).price,0)
		assertEquals(-2,runnerPricesDelta(0).totalToBack,0)
		assertEquals(5,runnerPricesDelta(0).totalToLay,0)

		assertEquals(1.95,runnerPricesDelta(1).price,0)
		assertEquals(6,runnerPricesDelta(1).totalToBack,0)
		assertEquals(-8,runnerPricesDelta(1).totalToLay,0)
	}

	@Test def testCalculateRunnerPricesDeltaRunnerPriceIsNotAvailableAnymore {
		val newRunnerPrices = new RunnerPrice(1.9,10,0) ::  Nil
		val previousRunnerPrices = new RunnerPrice(1.9,10,0) :: new RunnerPrice(1.95,5,20) :: Nil

		val runnerPricesDelta = MarketEventCalculator.calculateRunnerPricesDelta(newRunnerPrices,previousRunnerPrices)
		assertEquals(1, runnerPricesDelta.size)

		assertEquals(1.95,runnerPricesDelta(0).price,0)
		assertEquals(-5,runnerPricesDelta(0).totalToBack,0)
		assertEquals(-20,runnerPricesDelta(0).totalToLay,0)
	}

	/**
	 * Tests scenarios for calculateTradedVolumeDelta.
	 * */

	@Test def testCalculateRunnerTradedVolumesDeltaBothTradedVolumesAreEmpty {
		val tradedVolumeDelta = MarketEventCalculator.calculateTradedVolumeDelta(List(),List())
		assertEquals(0, tradedVolumeDelta.size)
	}

	@Test def testCalculateRunnerTradedVolumesDeltaBothRunnerTradedVolumesAreTheSame {
		val newTradedVolume = new PriceTradedVolume(1.84,100) :: new PriceTradedVolume(1.86,200) :: Nil
		val previousTradedVolume = new PriceTradedVolume(1.84,100) :: new PriceTradedVolume(1.86,200) :: Nil

		val tradedVolumeDelta = MarketEventCalculator.calculateTradedVolumeDelta(newTradedVolume,previousTradedVolume)
		assertEquals(0, tradedVolumeDelta.size)
	}

	@Test def testCalculateRunnerTradedVolumesDeltaNewTradedVolumeIsAvailable {
		val newTradedVolume = new PriceTradedVolume(1.84,100) :: new PriceTradedVolume(1.85,150) :: new PriceTradedVolume(1.86,200) :: Nil
		val previousTradedVolume = new PriceTradedVolume(1.84,100) :: new PriceTradedVolume(1.86,200) :: Nil

		val tradedVolumeDelta = MarketEventCalculator.calculateTradedVolumeDelta(newTradedVolume,previousTradedVolume)
		assertEquals(1, tradedVolumeDelta.size)

		assertEquals(1.85,tradedVolumeDelta(0).price,0)
		assertEquals(150,tradedVolumeDelta(0).totalMatchedAmount,0)
	}

	@Test def testCalculateRunnerTradedVolumesDeltaRunnerTradedVolumesAreUpdated {
		val newTradedVolume = new PriceTradedVolume(1.84,120) :: new PriceTradedVolume(1.86,170) :: Nil
		val previousTradedVolume = new PriceTradedVolume(1.84,100) :: new PriceTradedVolume(1.86,200) :: Nil

		val tradedVolumeDelta = MarketEventCalculator.calculateTradedVolumeDelta(newTradedVolume,previousTradedVolume)
		assertEquals(2, tradedVolumeDelta.size)

		assertEquals(1.84,tradedVolumeDelta(0).price,0)
		assertEquals(20,tradedVolumeDelta(0).totalMatchedAmount,0)

		assertEquals(1.86,tradedVolumeDelta(1).price,0)
		assertEquals(-30,tradedVolumeDelta(1).totalMatchedAmount,0)
	}

	@Test def testCalculateRunnerTradedVolumesDeltaRunnerTradedVolumeIsNotAvailableAnymore {
		val newTradedVolume = new PriceTradedVolume(1.86,200) :: Nil
		val previousTradedVolume = new PriceTradedVolume(1.84,100) :: new PriceTradedVolume(1.86,200) :: Nil

		val tradedVolumeDelta = MarketEventCalculator.calculateTradedVolumeDelta(newTradedVolume,previousTradedVolume)
		assertEquals(1, tradedVolumeDelta.size)

		assertEquals(1.84,tradedVolumeDelta(0).price,0)
		assertEquals(-100,tradedVolumeDelta(0).totalMatchedAmount,0)
	}

	/**
	 * Tests scenarios for calculateRunnerDelta - Edge cases
	 * */
	
	@Test def testCalculateRunnerDeltaBothRunnerStatesAreEmpty {
		val marketEvents = MarketEventCalculator.calculateRunnerDelta(123,10,1000)(List(),List())
		assertEquals(0, marketEvents.size)
	}

	@Test(expected=classOf[IllegalArgumentException])  
	def testCalculateRunnerDeltaTradedVolumeForThePreviousStateIsBiggerThanForTheNewState {
		val newRunnerPricesDelta = new RunnerPrice(1.9,10,0) :: new RunnerPrice(1.95,0,20) :: Nil
		val newTradedVolumesDelta = new PriceTradedVolume(1.84,100) :: new PriceTradedVolume(1.86,-20) :: Nil

		val marketEvents = MarketEventCalculator.calculateRunnerDelta(123,10,1000)(newRunnerPricesDelta,newTradedVolumesDelta)
	}

	/**
	 * Tests scenarios for calculateRunnerDelta - DeltaTradedVolumeIsTheSame - PLACE BET.
	 * */
	
	
	@Test def testCalculateRunnerDeltaTradedVolumeIsTheSameLayBetPlacementEventsAreGenerated {
		val newRunnerPricesDelta = new RunnerPrice(1.85,20,0) :: new RunnerPrice(1.9,2,0) :: Nil
		val newTradedVolumesDelta = Nil

		val marketEvents = MarketEventCalculator.calculateRunnerDelta(123,10,1000)(newRunnerPricesDelta,newTradedVolumesDelta)

		val expectedEvents = 
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":20.0,"betPrice":1.85,"betType":"LAY","marketId":10,"runnerId":1000}""" ::
				"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":2.0,"betPrice":1.9,"betType":"LAY","marketId":10,"runnerId":1000}""" :: 
					Nil

					assertEquals(2, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
	}

	@Test def testCalculateRunnerDeltaTradedVolumeIsTheSameBackBetPlacementEventsAreGenerated {
		val newRunnerPricesDelta = new RunnerPrice(1.95,0,3) :: new RunnerPrice(2,0,30) :: Nil
		val newTradedVolumesDelta = Nil

		val marketEvents = MarketEventCalculator.calculateRunnerDelta(123,10,1000)(newRunnerPricesDelta,newTradedVolumesDelta)

		val expectedEvents = 
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":3.0,"betPrice":1.95,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
				"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":30.0,"betPrice":2.0,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
					Nil

					assertEquals(2, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
	}

	/**
	 * Tests scenarios for calculateRunnerDelta - DeltaTradedVolumeIsNotTheSame - Matched on priceToBack - PLACE BET.
	 * */

	@Test def testCalculateRunnerDeltaTradedVolumeIsNotTheSameBackBetIsGenerated{

		val newRunnerPricesDelta = new RunnerPrice(1.85,-3,0) :: Nil
		val newTradedVolumesDelta = new PriceTradedVolume(1.85,3) :: Nil

		val marketEvents = MarketEventCalculator.calculateRunnerDelta(123,10,1000)(newRunnerPricesDelta,newTradedVolumesDelta)

		val expectedEvents = 
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":3.0,"betPrice":1.85,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
				Nil

				assertEquals(1, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
	}

	@Test def testCalculateRunnerDeltaTradedVolumeIsNotTheSameBackBetAndLayBetAreGenerated{

		val newRunnerPricesDelta = new RunnerPrice(1.85,-2,0) :: Nil
		val newTradedVolumesDelta = new PriceTradedVolume(1.85,3) :: Nil

		val marketEvents = MarketEventCalculator.calculateRunnerDelta(123,10,1000)(newRunnerPricesDelta,newTradedVolumesDelta)

		val expectedEvents = 
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":1.0,"betPrice":1.85,"betType":"LAY","marketId":10,"runnerId":1000}""" ::
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":3.0,"betPrice":1.85,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
					Nil

		assertEquals(2, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
	}
	
	@Test def testCalculateRunnerDeltaTradedVolumeIsNotTheSameBackBetAndLayBetAreGenerated2{

		val newRunnerPricesDelta = new RunnerPrice(1.85,1,0) :: Nil
		val newTradedVolumesDelta = new PriceTradedVolume(1.85,3) :: Nil

		val marketEvents = MarketEventCalculator.calculateRunnerDelta(123,10,1000)(newRunnerPricesDelta,newTradedVolumesDelta)

		val expectedEvents = 
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":4.0,"betPrice":1.85,"betType":"LAY","marketId":10,"runnerId":1000}""" ::
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":3.0,"betPrice":1.85,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
					Nil

		assertEquals(2, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
	}
	
	@Test def testCalculateRunnerDeltaTradedVolumeIsNotTheSameBackBetAndLayBetAreGenerated3{

		val newRunnerPricesDelta = new RunnerPrice(1.85,-3,0) :: Nil
		val newTradedVolumesDelta = new PriceTradedVolume(1.85,6) :: Nil

		val marketEvents = MarketEventCalculator.calculateRunnerDelta(123,10,1000)(newRunnerPricesDelta,newTradedVolumesDelta)

		val expectedEvents = 
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":3.0,"betPrice":1.85,"betType":"LAY","marketId":10,"runnerId":1000}""" ::
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":6.0,"betPrice":1.85,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
					Nil

		assertEquals(2, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
	}
	
	/**
	 * Tests scenarios for calculateRunnerDelta - DeltaTradedVolumeIsNotTheSame - Matched on priceToLay - PLACE BET.
	 * */
	
	@Test def testCalculateRunnerDeltaTradedVolumeIsNotTheSameLayBetIsGenerated{

		val newRunnerPricesDelta = new RunnerPrice(1.85,0,-3) :: Nil
		val newTradedVolumesDelta = new PriceTradedVolume(1.85,3) :: Nil

		val marketEvents = MarketEventCalculator.calculateRunnerDelta(123,10,1000)(newRunnerPricesDelta,newTradedVolumesDelta)

		val expectedEvents = 
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":3.0,"betPrice":1.85,"betType":"LAY","marketId":10,"runnerId":1000}""" ::
				Nil

				assertEquals(1, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
	}

	@Test def testCalculateRunnerDeltaTradedVolumeIsNotTheSameLayBetAndBackBetAreGenerated{

		val newRunnerPricesDelta = new RunnerPrice(1.85,0,-2) :: Nil
		val newTradedVolumesDelta = new PriceTradedVolume(1.85,3) :: Nil

		val marketEvents = MarketEventCalculator.calculateRunnerDelta(123,10,1000)(newRunnerPricesDelta,newTradedVolumesDelta)

		val expectedEvents = 
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":3.0,"betPrice":1.85,"betType":"LAY","marketId":10,"runnerId":1000}""" ::
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":1.0,"betPrice":1.85,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
					Nil

		assertEquals(2, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
	}
	
	@Test def testCalculateRunnerDeltaTradedVolumeIsNotTheSameLayBetAndBackBetAreGenerated2{

		val newRunnerPricesDelta = new RunnerPrice(1.85,0,1) :: Nil
		val newTradedVolumesDelta = new PriceTradedVolume(1.85,3) :: Nil

		val marketEvents = MarketEventCalculator.calculateRunnerDelta(123,10,1000)(newRunnerPricesDelta,newTradedVolumesDelta)

		val expectedEvents = 
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":3.0,"betPrice":1.85,"betType":"LAY","marketId":10,"runnerId":1000}""" ::
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":4.0,"betPrice":1.85,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
					Nil

		assertEquals(2, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
	}
	
	@Test def testCalculateRunnerDeltaTradedVolumeIsNotTheSameLayBetAndBackBetAreGenerated3{

		val newRunnerPricesDelta = new RunnerPrice(1.85,0,-3) :: Nil
		val newTradedVolumesDelta = new PriceTradedVolume(1.85,6) :: Nil

		val marketEvents = MarketEventCalculator.calculateRunnerDelta(123,10,1000)(newRunnerPricesDelta,newTradedVolumesDelta)

		val expectedEvents = 
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":6.0,"betPrice":1.85,"betType":"LAY","marketId":10,"runnerId":1000}""" ::
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":3.0,"betPrice":1.85,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
					Nil

		assertEquals(2, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
	}
	
	/**
	 * Tests scenarios for calculateRunnerDelta - DeltaTradedVolumeIsNotTheSame - Matched on emptyPrice - PLACE BET.
	 * */
	@Test def testCalculateRunnerDeltaTradedVolumeIsNotTheSameEqualBackAndLayBetsAreGenerated{

		val newRunnerPricesDelta = new RunnerPrice(1.85,0,0) :: Nil
		val newTradedVolumesDelta = new PriceTradedVolume(1.85,3) :: Nil

		val marketEvents = MarketEventCalculator.calculateRunnerDelta(123,10,1000)(newRunnerPricesDelta,newTradedVolumesDelta)

		val expectedEvents = 
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":3.0,"betPrice":1.85,"betType":"LAY","marketId":10,"runnerId":1000}""" ::
			"""{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":3.0,"betPrice":1.85,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
				Nil

				assertEquals(2, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
	}
	
	/**
	 * Tests scenarios for calculateRunnerDelta - DeltaTradedVolumeIsTheSame - CANCEL BET.
	 * */
	
	/**
	 * Tests scenarios for calculateRunnerDelta - DeltaTradedVolumeIsNotTheSame - CANCEL BET.
	 * */
	
	//
	//	@Test def testCalculateRunnerDeltaTradedVolumeAreTheSameNewMarketPriceWasRemoved {
	//		val newRunnerPrices =  new RunnerPrice(1.85,20,0) :: new RunnerPrice(1.95,0,20) :: Nil
	//		val newTradedVolume = new PriceTradedVolume(1.84,100) :: new PriceTradedVolume(1.86,200) :: Nil
	//
	//		val previousRunnerPrices = new RunnerPrice(1.85,20,0) :: new RunnerPrice(1.9,10,0) :: new RunnerPrice(1.95,0,20) :: Nil
	//		val previousTradedVolume = new PriceTradedVolume(1.84,100) :: new PriceTradedVolume(1.86,200) :: Nil
	//
	//		val marketEvents = MarketEventCalculator.calculateRunnerDelta(123,10,1000)(newRunnerPricesDelta,newTradedVolumesDelta)
	//
	//		val expectedEvents = """
	//			CANCEL_BET
	//			""" :: Nil
	//
	//			assertEquals(1, marketEvents.size)
	//		assertEquals(expectedEvents(0),marketEvents(0))
	//	}
	//
	
	

}