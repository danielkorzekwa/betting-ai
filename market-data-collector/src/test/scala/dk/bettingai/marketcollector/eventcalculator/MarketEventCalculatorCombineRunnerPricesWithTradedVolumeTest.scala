package dk.bettingai.marketcollector.eventcalculator

import org.junit._
import Assert._
import dk.betex.Market._
import dk.betex.RunnerTradedVolume._

class MarketEventCalculatorCombineRunnerPricesWithTradedVolumeTest {

	/**
	 * Tests scenarios for combine runner prices with traded volume.
	 * */

	@Test def testCombineRunnerPricesAndTradedVolumeAreEmpty {
		val combinedRunnerPrices = MarketEventCalculator.combine(List(),List())
		assertEquals(0, combinedRunnerPrices.size)
	}


	@Test def testCombineTradedVolumeIsEmpty {
		val newRunnerPricesDelta = new RunnerPrice(1.85,20,-5) :: new RunnerPrice(1.9,2,-7) :: Nil
		val newTradedVolumesDelta = Nil

		val combinedRunnerPrices = MarketEventCalculator.combine(newRunnerPricesDelta,newTradedVolumesDelta)
		assertEquals(2, combinedRunnerPrices.size)

		assertEquals(1.85, combinedRunnerPrices(0).price,0)
		assertEquals(20, combinedRunnerPrices(0).totalToBack,0)
		assertEquals(-5, combinedRunnerPrices(0).totalToLay,0)

		assertEquals(1.9, combinedRunnerPrices(1).price,0)
		assertEquals(2, combinedRunnerPrices(1).totalToBack,0)
		assertEquals(-7, combinedRunnerPrices(1).totalToLay,0)
	}

	@Test def testCombineBothRunnerPricesAndTradedVolumeAreAvailable {
		val newRunnerPricesDelta = new RunnerPrice(1.85,20,-5) :: new RunnerPrice(1.9,2,-7) :: Nil
		val newTradedVolumesDelta = new PriceTradedVolume(1.85,3) :: new PriceTradedVolume(1.95,3) :: Nil

		val combinedRunnerPrices = MarketEventCalculator.combine(newRunnerPricesDelta,newTradedVolumesDelta)
		assertEquals(3, combinedRunnerPrices.size)

		assertEquals(1.85, combinedRunnerPrices(0).price,0)
		assertEquals(23, combinedRunnerPrices(0).totalToBack,0)
		assertEquals(-2, combinedRunnerPrices(0).totalToLay,0)

		assertEquals(1.9, combinedRunnerPrices(1).price,0)
		assertEquals(2, combinedRunnerPrices(1).totalToBack,0)
		assertEquals(-7, combinedRunnerPrices(1).totalToLay,0)

		assertEquals(1.95, combinedRunnerPrices(2).price,0)
		assertEquals(3, combinedRunnerPrices(2).totalToBack,0)
		assertEquals(3, combinedRunnerPrices(2).totalToLay,0)
	}
}