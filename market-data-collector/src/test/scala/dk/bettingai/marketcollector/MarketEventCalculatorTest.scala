package dk.bettingai.marketcollector

import org.junit._
import Assert._
import dk.bettingai.marketsimulator.betex.Market._

class MarketEventCalculatorTest {

	@Test def testBothRunnerStatesAreEmpty {
		val marketEvents = MarketEventCalculator.calculate(123,10,1000)(List()->List(),List()->List())
		assertEquals(0, marketEvents.size)
	}

	@Test def testBothRunnerStatesAreTheSame {
		val newRunnerPrices = new RunnerPrice(1.9,10,0) :: new RunnerPrice(1.95,0,20) :: Nil
		val newTradedVolume = new PriceTradedVolume(1.84,100) :: new PriceTradedVolume(1.86,200) :: Nil

		val previousRunnerPrices = new RunnerPrice(1.9,10,0) :: new RunnerPrice(1.95,0,20) :: Nil
		val previousTradedVolume = new PriceTradedVolume(1.84,100) :: new PriceTradedVolume(1.86,200) :: Nil

		val marketEvents = MarketEventCalculator.calculate(123,10,1000)(newRunnerPrices->newTradedVolume,previousRunnerPrices->previousTradedVolume)
		assertEquals(0, marketEvents.size)
	}

	@Test(expected=classOf[IllegalArgumentException])  
	def testTradedVolumeForThePreviousStateIsBiggerThanForTheNewState {
		val newRunnerPrices = new RunnerPrice(1.9,10,0) :: new RunnerPrice(1.95,0,20) :: Nil
		val newTradedVolume = new PriceTradedVolume(1.84,100) :: new PriceTradedVolume(1.86,200) :: Nil

		val previousRunnerPrices = new RunnerPrice(1.9,10,0) :: new RunnerPrice(1.95,0,20) :: Nil
		val previousTradedVolume = new PriceTradedVolume(1.84,100) :: new PriceTradedVolume(1.86,201) :: Nil

		val marketEvents = MarketEventCalculator.calculate(123,10,1000)(newRunnerPrices->newTradedVolume,previousRunnerPrices->previousTradedVolume)
	}

	/**
	 * Tests for the most common scenario: If both previous and current market states are not the same 
	 * then appropriate PLACE_BET/CANCEL_BET events are generated.
	 * 
	 * */
	
	@Test def testTradedVolumeAreEmptyLayBetPlacementEventsAreGenerated {
		val newRunnerPrices =  new RunnerPrice(1.85,20,0) :: new RunnerPrice(1.87,45,0) :: new RunnerPrice(1.9,12,0) :: Nil
		val newTradedVolume = Nil

		val previousRunnerPrices = new RunnerPrice(1.9,10,0) :: new RunnerPrice(1.87,45,0) :: Nil
		val previousTradedVolume = Nil

		val marketEvents = MarketEventCalculator.calculate(123,10,1000)(newRunnerPrices->newTradedVolume,previousRunnerPrices->previousTradedVolume)
		
		val expectedEvents = 
		  """{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":20.0,"betPrice":1.85,"betType":"LAY","marketId":10,"runnerId":1000}""" ::
		  """{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":2.0,"betPrice":1.9,"betType":"LAY","marketId":10,"runnerId":1000}""" :: 
		  Nil
		
		assertEquals(2, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
	}
	
	@Test def testTradedVolumeAreEmptyBackBetPlacementEventsAreGenerated {
		val newRunnerPrices = new RunnerPrice(1.93,0,56) :: new RunnerPrice(1.95,0,23) :: new RunnerPrice(2.0,0,30):: Nil
		val newTradedVolume = Nil

		val previousRunnerPrices = new RunnerPrice(1.93,0,56) :: new RunnerPrice(1.95,0,20) :: Nil
		val previousTradedVolume = Nil

		val marketEvents = MarketEventCalculator.calculate(123,10,1000)(newRunnerPrices->newTradedVolume,previousRunnerPrices->previousTradedVolume)
		
		val expectedEvents = 
		  """{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":3.0,"betPrice":1.95,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
		  """{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":30.0,"betPrice":2.0,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
		  Nil
		
		assertEquals(2, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
		assertEquals(expectedEvents(1),marketEvents(1))
	}
	
@Test def testTradedVolumeAreTheSameNewMarketPriceWasRemoved {
		val newRunnerPrices =  new RunnerPrice(1.85,20,0) :: new RunnerPrice(1.95,0,20) :: Nil
		val newTradedVolume = new PriceTradedVolume(1.84,100) :: new PriceTradedVolume(1.86,200) :: Nil

		val previousRunnerPrices = new RunnerPrice(1.85,20,0) :: new RunnerPrice(1.9,10,0) :: new RunnerPrice(1.95,0,20) :: Nil
		val previousTradedVolume = new PriceTradedVolume(1.84,100) :: new PriceTradedVolume(1.86,200) :: Nil

		val marketEvents = MarketEventCalculator.calculate(123,10,1000)(newRunnerPrices->newTradedVolume,previousRunnerPrices->previousTradedVolume)
		
		val expectedEvents = """
				CANCEL_BET
		""" :: Nil
		
		assertEquals(1, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
	}
	
	@Test def testTradedVolumeAreNotTheSameLayBetPlacementEventsAreGenerated{
		val newRunnerPrices =  new RunnerPrice(1.85,2,0) :: Nil
		val newTradedVolume = new PriceTradedVolume(1.85,13) :: Nil

		val previousRunnerPrices = new RunnerPrice(1.85,5,0) :: Nil
		val previousTradedVolume = new PriceTradedVolume(1.85,10) :: Nil

		val marketEvents = MarketEventCalculator.calculate(123,10,1000)(newRunnerPrices->newTradedVolume,previousRunnerPrices->previousTradedVolume)
		
		val expectedEvents = 
		  """{"eventType":"PLACE_BET","userId":123,"betId":100,"betSize":3.0,"betPrice":1.85,"betType":"BACK","marketId":10,"runnerId":1000}""" ::
		  Nil
		
		assertEquals(1, marketEvents.size)
		assertEquals(expectedEvents(0),marketEvents(0))
	}

}