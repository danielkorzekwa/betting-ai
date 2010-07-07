package dk.bettingai.marketcollector

import org.junit._
import Assert._
import dk.bettingai.marketsimulator.betex.Market._

class MarketEventCalculatorTradedVolumeDelta {

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

}