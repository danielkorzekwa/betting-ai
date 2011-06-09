package dk.bettingai.marketcollector.eventcalculator

import org.junit._
import Assert._
import dk.betex.Market._

class MarketEventCalculatorRunnerPriceDeltaTest {

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

	
	

}