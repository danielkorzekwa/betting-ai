package dk.bettingai.marketcollector.eventcalculator

import org.junit._
import Assert._
import dk.betex.Market._
import dk.betex.api.IMarket._
import dk.betex._
import dk.betex.RunnerTradedVolume._

class MarketEventCalculatorTradedVolumeMarketEventsTest {

	/**Test scenarios for input data validation.*/
	@Test(expected=classOf[IllegalArgumentException]) 
	def testNegativeTradedVolumeData {
		val previousRunnerPrices = Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.0,-3) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(1234,2,22)(previousRunnerPrices,tradedVolumeDelta)

	}

	@Test(expected=classOf[IllegalArgumentException]) 
	def testPriceWithBothToBackAndToLayToBeZero {
		val previousRunnerPrices = new RunnerPrice(2.0,0,0) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.0,3) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(1234,2,22)(previousRunnerPrices,tradedVolumeDelta)
	}

	@Test(expected=classOf[IllegalArgumentException]) 
	def testPriceWithBothToBackAndToLayAvailable {
		val previousRunnerPrices = new RunnerPrice(2.0,3,2) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.0,3) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(1234,2,22)(previousRunnerPrices,tradedVolumeDelta)
	}

	@Test(expected=classOf[IllegalArgumentException]) 
	def testTotalToBackOnHigherPriceThanTotalToLay {
		val previousRunnerPrices = new RunnerPrice(2.0,3,0) :: new RunnerPrice(1.9,0,3) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.0,3) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(1235,2,22)(previousRunnerPrices,tradedVolumeDelta)
	}
	
	/**Price to back is 1000, toLay is not available - it's a valid data, not exception should be thrown.*/
	@Test def testTotalToBackis1000ToLayNotAvailable {
		val previousRunnerPrices = new RunnerPrice(1000.0,20,0) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(1234,2,22)(previousRunnerPrices,tradedVolumeDelta)
	}
	/**Price to back is not available, toLay is 1.01 - it's a valid data, not exception should be thrown.*/
	@Test def testTotalToBackisNotAvailableToLayIs1_01 {
		val previousRunnerPrices = new RunnerPrice(1.01,0,20) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(1236,2,22)(previousRunnerPrices,tradedVolumeDelta)
	}

	/**Test scenarios for priceToBack available only.*/

	@Test def testOnePriceToBackNothingIsMatched {
		val previousRunnerPrices = new RunnerPrice(2.0,3,0) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(1236,2,22)(previousRunnerPrices,tradedVolumeDelta)

		val runnerPrices = result._1
		assertEquals(1,runnerPrices.size)
		assertEquals(2.0,runnerPrices(0).price,0)
		assertEquals(3,runnerPrices(0).totalToBack,0)
		assertEquals(0,runnerPrices(0).totalToLay,0)

		val marketEvents = result._2
		assertEquals(0,marketEvents.size)
	}

	@Test def testOnePriceToBackFullyMatched {
		val previousRunnerPrices = new RunnerPrice(2.0,3,0) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.0,3) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(1236,2,22)(previousRunnerPrices,tradedVolumeDelta)

		val runnerPrices = result._1
		assertEquals(0,runnerPrices.size)

		val marketEvents = result._2
		assertEquals(1,marketEvents.size)
		assertEquals("""{"time":1236,"eventType":"PLACE_BET","betSize":3.0,"betPrice":2.0,"betType":"BACK","marketId":2,"runnerId":22}""",marketEvents(0))
	}

	@Test def testOnePriceToBackPartiallyMatched {
		val previousRunnerPrices = new RunnerPrice(2.0,3,0) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume (new PriceTradedVolume(2.0,2) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(1237,2,22)(previousRunnerPrices,tradedVolumeDelta)

		val runnerPrices = result._1
		assertEquals(1,runnerPrices.size)
		assertEquals(2.0,runnerPrices(0).price,0)
		assertEquals(1,runnerPrices(0).totalToBack,0)
		assertEquals(0,runnerPrices(0).totalToLay,0)

		val marketEvents = result._2
		assertEquals(1,marketEvents.size)
		assertEquals("""{"time":1237,"eventType":"PLACE_BET","betSize":2.0,"betPrice":2.0,"betType":"BACK","marketId":2,"runnerId":22}""",marketEvents(0))
	}

	@Test def testOnePriceToBackMatchedMoreThanAvailable {
		val previousRunnerPrices = new RunnerPrice(2.0,3,0) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.0,5) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(1237,2,22)(previousRunnerPrices,tradedVolumeDelta)

		val runnerPrices = result._1
		assertEquals(0,runnerPrices.size)

		val marketEvents = result._2
		assertEquals(2,marketEvents.size)
		assertEquals("""{"time":1237,"eventType":"PLACE_BET","betSize":2.0,"betPrice":2.0,"betType":"LAY","marketId":2,"runnerId":22}""",marketEvents(0))
		assertEquals("""{"time":1237,"eventType":"PLACE_BET","betSize":5.0,"betPrice":2.0,"betType":"BACK","marketId":2,"runnerId":22}""",marketEvents(1))

	}

	@Test def testTwoPricesToBackPartiallyMatchedOnSecondPrice {
		val previousRunnerPrices = new RunnerPrice(2.0,4,0) :: new RunnerPrice(2.1,100,0) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.0,2) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(1237,2,22)(previousRunnerPrices,tradedVolumeDelta)

		val runnerPrices = result._1
		assertEquals(1,runnerPrices.size)
		assertEquals(2.0,runnerPrices(0).price,0)
		assertEquals(2,runnerPrices(0).totalToBack,0)
		assertEquals(0,runnerPrices(0).totalToLay,0)

		val marketEvents = result._2
		assertEquals(2,marketEvents.size)
		assertEquals("""{"time":1237,"eventType":"CANCEL_BETS","betsSize":100.0,"betPrice":2.1,"betType":"LAY","marketId":2,"runnerId":22}""",marketEvents(0))
		assertEquals("""{"time":1237,"eventType":"PLACE_BET","betSize":2.0,"betPrice":2.0,"betType":"BACK","marketId":2,"runnerId":22}""",marketEvents(1))
	}

	@Test def testTwoPricesToBackPartiallyMatchedOnFirstPriceThenPartiallyMatchedOnSecondPrice {
		val previousRunnerPrices = new RunnerPrice(2.0,4,0) :: new RunnerPrice(2.1,100,0) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.0,2) :: new PriceTradedVolume(2.1,40) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(1238,2,22)(previousRunnerPrices,tradedVolumeDelta)

		val runnerPrices = result._1
		assertEquals(1,runnerPrices.size)
		assertEquals(2.0,runnerPrices(0).price,0)
		assertEquals(2,runnerPrices(0).totalToBack,0)
		assertEquals(0,runnerPrices(0).totalToLay,0)

		val marketEvents = result._2
		assertEquals(3,marketEvents.size)
		assertEquals("""{"time":1238,"eventType":"PLACE_BET","betSize":40.0,"betPrice":2.1,"betType":"BACK","marketId":2,"runnerId":22}""",marketEvents(0))
		assertEquals("""{"time":1238,"eventType":"CANCEL_BETS","betsSize":60.0,"betPrice":2.1,"betType":"LAY","marketId":2,"runnerId":22}""",marketEvents(1))
		assertEquals("""{"time":1238,"eventType":"PLACE_BET","betSize":2.0,"betPrice":2.0,"betType":"BACK","marketId":2,"runnerId":22}""",marketEvents(2))
	}

	@Test def testTwoPricesToBackPartiallyMatchedOnFirstPriceThenFullyMatchedOnSecondPrice {
		val previousRunnerPrices = new RunnerPrice(2.0,4,0) :: new RunnerPrice(2.1,100,0) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.0,4) :: new PriceTradedVolume(2.1,40) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(1238,2,22)(previousRunnerPrices,tradedVolumeDelta)


		val runnerPrices = result._1
		assertEquals(0,runnerPrices.size)

		val marketEvents = result._2
		assertEquals(3,marketEvents.size)
		assertEquals("""{"time":1238,"eventType":"PLACE_BET","betSize":40.0,"betPrice":2.1,"betType":"BACK","marketId":2,"runnerId":22}""",marketEvents(0))
		assertEquals("""{"time":1238,"eventType":"CANCEL_BETS","betsSize":60.0,"betPrice":2.1,"betType":"LAY","marketId":2,"runnerId":22}""",marketEvents(1))
		assertEquals("""{"time":1238,"eventType":"PLACE_BET","betSize":4.0,"betPrice":2.0,"betType":"BACK","marketId":2,"runnerId":22}""",marketEvents(2))
	}

	@Test def testTradedVolumeZeroOnSecondToBackPrice {
		val previousRunnerPrices = new RunnerPrice(2.0,4,0) :: new RunnerPrice(2.1,100,0) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.0,0) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(1238,2,22)(previousRunnerPrices,tradedVolumeDelta)

		val runnerPrices = result._1
		assertEquals(2,runnerPrices.size)
		assertEquals(2.0,runnerPrices(0).price,0)
		assertEquals(4,runnerPrices(0).totalToBack,0)
		assertEquals(0,runnerPrices(0).totalToLay,0)

		assertEquals(2.1,runnerPrices(1).price,0)
		assertEquals(100,runnerPrices(1).totalToBack,0)
		assertEquals(0,runnerPrices(1).totalToLay,0)

		val marketEvents = result._2
		assertEquals(0,marketEvents.size)
	}

	/**Test scenarios for priceToLay available only.*/

	@Test def testOnePriceToLayNothingIsMatched {
		val previousRunnerPrices = new RunnerPrice(2.0,0,3) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(1238,2,22)(previousRunnerPrices,tradedVolumeDelta)

		val runnerPrices = result._1
		assertEquals(1,runnerPrices.size)
		assertEquals(2.0,runnerPrices(0).price,0)
		assertEquals(0,runnerPrices(0).totalToBack,0)
		assertEquals(3,runnerPrices(0).totalToLay,0)

		val marketEvents = result._2
		assertEquals(0,marketEvents.size)
	}

	@Test def testOnePriceToLayFullyMatched {
		val previousRunnerPrices = new RunnerPrice(2.0,0,3) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.0,3) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(1239,2,22)(previousRunnerPrices,tradedVolumeDelta)

		val runnerPrices = result._1
		assertEquals(0,runnerPrices.size)

		val marketEvents = result._2
		assertEquals(1,marketEvents.size)
		assertEquals("""{"time":1239,"eventType":"PLACE_BET","betSize":3.0,"betPrice":2.0,"betType":"LAY","marketId":2,"runnerId":22}""",marketEvents(0))
	}

	@Test def testOnePriceToLayPartiallyMatched {
		val previousRunnerPrices = new RunnerPrice(2.0,0,3) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.0,2) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(1239,2,22)(previousRunnerPrices,tradedVolumeDelta)

		val runnerPrices = result._1
		assertEquals(1,runnerPrices.size)
		assertEquals(2.0,runnerPrices(0).price,0)
		assertEquals(0,runnerPrices(0).totalToBack,0)
		assertEquals(1,runnerPrices(0).totalToLay,0)

		val marketEvents = result._2
		assertEquals(1,marketEvents.size)
		assertEquals("""{"time":1239,"eventType":"PLACE_BET","betSize":2.0,"betPrice":2.0,"betType":"LAY","marketId":2,"runnerId":22}""",marketEvents(0))
	}

	@Test def testOnePriceToLayMatchedMoreThanAvailable {
		val previousRunnerPrices = new RunnerPrice(2.0,0,3) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.0,5) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(134,2,22)(previousRunnerPrices,tradedVolumeDelta)

		val runnerPrices = result._1
		assertEquals(0,runnerPrices.size)

		val marketEvents = result._2
		assertEquals(2,marketEvents.size)
		assertEquals("""{"time":134,"eventType":"PLACE_BET","betSize":5.0,"betPrice":2.0,"betType":"LAY","marketId":2,"runnerId":22}""",marketEvents(0))
		assertEquals("""{"time":134,"eventType":"PLACE_BET","betSize":2.0,"betPrice":2.0,"betType":"BACK","marketId":2,"runnerId":22}""",marketEvents(1))

	}

	@Test def testTwoPricesToLayPartiallyMatchedOnSecondPrice {
		val previousRunnerPrices = new RunnerPrice(2.0,0,100) :: new RunnerPrice(2.1,0,4) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.1,2) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(134,2,22)(previousRunnerPrices,tradedVolumeDelta)

		val runnerPrices = result._1
		assertEquals(1,runnerPrices.size)
		assertEquals(2.1,runnerPrices(0).price,0)
		assertEquals(0,runnerPrices(0).totalToBack,0)
		assertEquals(2,runnerPrices(0).totalToLay,0)

		val marketEvents = result._2
		assertEquals(2,marketEvents.size)
		assertEquals("""{"time":134,"eventType":"CANCEL_BETS","betsSize":100.0,"betPrice":2.0,"betType":"BACK","marketId":2,"runnerId":22}""",marketEvents(0))
		assertEquals("""{"time":134,"eventType":"PLACE_BET","betSize":2.0,"betPrice":2.1,"betType":"LAY","marketId":2,"runnerId":22}""",marketEvents(1))
	}

	@Test def testTwoPricesToLayPartiallyMatchedOnFirstPriceThenPartiallyMatchedOnSecondPrice {
		val previousRunnerPrices = new RunnerPrice(2.0,0,100) :: new RunnerPrice(2.1,0,4) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.0,40) :: new PriceTradedVolume(2.1,2) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(134,2,22)(previousRunnerPrices,tradedVolumeDelta)

		val runnerPrices = result._1
		assertEquals(1,runnerPrices.size)
		assertEquals(2.1,runnerPrices(0).price,0)
		assertEquals(0,runnerPrices(0).totalToBack,0)
		assertEquals(2,runnerPrices(0).totalToLay,0)

		val marketEvents = result._2
		assertEquals(3,marketEvents.size)
		assertEquals("""{"time":134,"eventType":"PLACE_BET","betSize":40.0,"betPrice":2.0,"betType":"LAY","marketId":2,"runnerId":22}""",marketEvents(0))
		assertEquals("""{"time":134,"eventType":"CANCEL_BETS","betsSize":60.0,"betPrice":2.0,"betType":"BACK","marketId":2,"runnerId":22}""",marketEvents(1))
		assertEquals("""{"time":134,"eventType":"PLACE_BET","betSize":2.0,"betPrice":2.1,"betType":"LAY","marketId":2,"runnerId":22}""",marketEvents(2))
	}

	@Test def testTwoPricesToLayPartiallyMatchedOnFirstPriceThenFullyMatchedOnSecondPrice {
		val previousRunnerPrices = new RunnerPrice(2.0,0,100) :: new RunnerPrice(2.1,0,4) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.0,40) :: new PriceTradedVolume(2.1,4) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(134,2,22)(previousRunnerPrices,tradedVolumeDelta)


		val runnerPrices = result._1
		assertEquals(0,runnerPrices.size)

		val marketEvents = result._2
		assertEquals(3,marketEvents.size)
		assertEquals("""{"time":134,"eventType":"PLACE_BET","betSize":40.0,"betPrice":2.0,"betType":"LAY","marketId":2,"runnerId":22}""",marketEvents(0))
		assertEquals("""{"time":134,"eventType":"CANCEL_BETS","betsSize":60.0,"betPrice":2.0,"betType":"BACK","marketId":2,"runnerId":22}""",marketEvents(1))
		assertEquals("""{"time":134,"eventType":"PLACE_BET","betSize":4.0,"betPrice":2.1,"betType":"LAY","marketId":2,"runnerId":22}""",marketEvents(2))
	}

	@Test def testTradedVolumeZeroOnSecondToLayPrice {
		val previousRunnerPrices = new RunnerPrice(2.0,0,100) :: new RunnerPrice(2.1,0,4) :: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.1,0) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(134,2,22)(previousRunnerPrices,tradedVolumeDelta)

		val runnerPrices = result._1
		assertEquals(2,runnerPrices.size)
		assertEquals(2.0,runnerPrices(0).price,0)
		assertEquals(0,runnerPrices(0).totalToBack,0)
		assertEquals(100,runnerPrices(0).totalToLay,0)

		assertEquals(2.1,runnerPrices(1).price,0)
		assertEquals(0,runnerPrices(1).totalToBack,0)
		assertEquals(4,runnerPrices(1).totalToLay,0)

		val marketEvents = result._2
		assertEquals(0,marketEvents.size)
	}

	/**Test scenarios for traded volume on not available price.*/
	@Test def testTradedVolumeOnNotAvailablePrices {
		val previousRunnerPrices = Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.0,3) :: new PriceTradedVolume(2.1,6) :: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(134,2,22)(previousRunnerPrices,tradedVolumeDelta)

		val runnerPrices = result._1
		assertEquals(0,runnerPrices.size)

		val marketEvents = result._2
		assertEquals(4,marketEvents.size)
		assertEquals("""{"time":134,"eventType":"PLACE_BET","betSize":6.0,"betPrice":2.1,"betType":"LAY","marketId":2,"runnerId":22}""",marketEvents(0))
		assertEquals("""{"time":134,"eventType":"PLACE_BET","betSize":6.0,"betPrice":2.1,"betType":"BACK","marketId":2,"runnerId":22}""",marketEvents(1))

		assertEquals("""{"time":134,"eventType":"PLACE_BET","betSize":3.0,"betPrice":2.0,"betType":"LAY","marketId":2,"runnerId":22}""",marketEvents(2))
		assertEquals("""{"time":134,"eventType":"PLACE_BET","betSize":3.0,"betPrice":2.0,"betType":"BACK","marketId":2,"runnerId":22}""",marketEvents(3))
	}

	@Test def testBothToBackAndToLayPricesAvailable {
		val previousRunnerPrices = new RunnerPrice(2.0,4,0) :: new RunnerPrice(2.1,100,0) :: new RunnerPrice(2.2,0,50) :: new RunnerPrice(2.3,0,8):: Nil
		val tradedVolumeDelta = new RunnerTradedVolume(new PriceTradedVolume(2.0,2) :: new PriceTradedVolume(2.1,40) :: new PriceTradedVolume(2.2,20) :: new PriceTradedVolume(2.3,5):: Nil)

		val result:Tuple2[List[IRunnerPrice],List[String]] = MarketEventCalculator.calculateMarketEventsForTradedVolume(135,2,22)(previousRunnerPrices,tradedVolumeDelta)

		val runnerPrices = result._1
		assertEquals(2,runnerPrices.size)
		assertEquals(2.0,runnerPrices(0).price,0)
		assertEquals(2,runnerPrices(0).totalToBack,0)
		assertEquals(0,runnerPrices(0).totalToLay,0)

		assertEquals(2.3,runnerPrices(1).price,0)
		assertEquals(0,runnerPrices(1).totalToBack,0)
		assertEquals(3,runnerPrices(1).totalToLay,0)

		val marketEvents = result._2
		assertEquals(6,marketEvents.size)
		assertEquals("""{"time":135,"eventType":"PLACE_BET","betSize":40.0,"betPrice":2.1,"betType":"BACK","marketId":2,"runnerId":22}""",marketEvents(0))
		assertEquals("""{"time":135,"eventType":"CANCEL_BETS","betsSize":60.0,"betPrice":2.1,"betType":"LAY","marketId":2,"runnerId":22}""",marketEvents(1))
		assertEquals("""{"time":135,"eventType":"PLACE_BET","betSize":2.0,"betPrice":2.0,"betType":"BACK","marketId":2,"runnerId":22}""",marketEvents(2))
		assertEquals("""{"time":135,"eventType":"PLACE_BET","betSize":20.0,"betPrice":2.2,"betType":"LAY","marketId":2,"runnerId":22}""",marketEvents(3))
		assertEquals("""{"time":135,"eventType":"CANCEL_BETS","betsSize":30.0,"betPrice":2.2,"betType":"BACK","marketId":2,"runnerId":22}""",marketEvents(4))
		assertEquals("""{"time":135,"eventType":"PLACE_BET","betSize":5.0,"betPrice":2.3,"betType":"LAY","marketId":2,"runnerId":22}""",marketEvents(5))

	}
}