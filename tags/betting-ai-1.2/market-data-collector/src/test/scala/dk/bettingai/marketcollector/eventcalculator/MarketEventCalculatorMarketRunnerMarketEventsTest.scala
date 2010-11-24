package dk.bettingai.marketcollector.eventcalculator

import org.junit._
import Assert._
import dk.bettingai.marketsimulator.betex.Market._
import dk.bettingai.marketsimulator.betex._
import RunnerTradedVolume._

class MarketEventCalculatorMarketRunnerMarketEventsTest {


	@Test def testEmptyData {
		val marketRunner = (List(),new RunnerTradedVolume(List()))
		val prevMarketRunner = (List(),new RunnerTradedVolume(List()))
		val result = MarketEventCalculator.produce(1234,2,22,marketRunner,prevMarketRunner)

		assertEquals(0,result.size)
	}

	@Test def testGenericCase {
		val prevRunnerPrices = new RunnerPrice(2.0,4.3,0) :: new RunnerPrice(2.1,100.02,0) :: new RunnerPrice(2.2,0,20.47) :: new RunnerPrice(2.3,0,70.12):: Nil 
		val prevTradedVolume = new PriceTradedVolume(1.9,2.1) :: new PriceTradedVolume(2.0,2.5) :: new PriceTradedVolume(2.05,6.64) :: new PriceTradedVolume(2.2,343.23) :: new PriceTradedVolume(2.3,8.23) :: new PriceTradedVolume(2.4,39.64) :: Nil

		val runnerPrices = new RunnerPrice(2.0,23.53,0) :: new RunnerPrice(2.1,23.42,0) :: new RunnerPrice(2.2,3.45,0) :: new RunnerPrice(2.25,0,78.72) :: new RunnerPrice(2.3,0,70.12):: Nil 
		val tradedVolume = new PriceTradedVolume(1.9,3.4) :: new PriceTradedVolume(1.95,56.74) :: new PriceTradedVolume(2.0,5.1) :: new PriceTradedVolume(2.05,16.0003) :: new PriceTradedVolume(2.2,344.13) :: new PriceTradedVolume(2.3,8.23) :: new PriceTradedVolume(2.4,39.64) :: new PriceTradedVolume(2.35,45.64) :: Nil
		val marketRunner = (runnerPrices,new RunnerTradedVolume(tradedVolume))
		val prevMarketRunner = (prevRunnerPrices,new RunnerTradedVolume(prevTradedVolume))
		val result = MarketEventCalculator.produce(1235,2,22,marketRunner,prevMarketRunner)

		assertEquals(19,result.size)

		assertEquals("""{"time":1235,"eventType":"CANCEL_BETS","betsSize":100.0,"betPrice":2.1,"betType":"LAY","marketId":2,"runnerId":22}""",result(0))
		assertEquals("""{"time":1235,"eventType":"PLACE_BET","betSize":10.0,"betPrice":2.05,"betType":"LAY","marketId":2,"runnerId":22}""",result(1))
		assertEquals("""{"time":1235,"eventType":"PLACE_BET","betSize":10.0,"betPrice":2.05,"betType":"BACK","marketId":2,"runnerId":22}""",result(2))
		assertEquals("""{"time":1235,"eventType":"PLACE_BET","betSize":3.0,"betPrice":2.0,"betType":"BACK","marketId":2,"runnerId":22}""",result(3))
		assertEquals("""{"time":1235,"eventType":"CANCEL_BETS","betsSize":1.0,"betPrice":2.0,"betType":"LAY","marketId":2,"runnerId":22}""",result(4))
		assertEquals("""{"time":1235,"eventType":"PLACE_BET","betSize":56.0,"betPrice":1.95,"betType":"LAY","marketId":2,"runnerId":22}""",result(5))
		assertEquals("""{"time":1235,"eventType":"PLACE_BET","betSize":56.0,"betPrice":1.95,"betType":"BACK","marketId":2,"runnerId":22}""",result(6))
		assertEquals("""{"time":1235,"eventType":"PLACE_BET","betSize":1.0,"betPrice":1.9,"betType":"LAY","marketId":2,"runnerId":22}""",result(7))
		assertEquals("""{"time":1235,"eventType":"PLACE_BET","betSize":1.0,"betPrice":1.9,"betType":"BACK","marketId":2,"runnerId":22}""",result(8))
		assertEquals("""{"time":1235,"eventType":"PLACE_BET","betSize":1.0,"betPrice":2.2,"betType":"LAY","marketId":2,"runnerId":22}""",result(9))
		assertEquals("""{"time":1235,"eventType":"CANCEL_BETS","betsSize":19.0,"betPrice":2.2,"betType":"BACK","marketId":2,"runnerId":22}""",result(10))
		assertEquals("""{"time":1235,"eventType":"CANCEL_BETS","betsSize":70.0,"betPrice":2.3,"betType":"BACK","marketId":2,"runnerId":22}""",result(11))
		assertEquals("""{"time":1235,"eventType":"PLACE_BET","betSize":45.0,"betPrice":2.35,"betType":"LAY","marketId":2,"runnerId":22}""",result(12))
		assertEquals("""{"time":1235,"eventType":"PLACE_BET","betSize":45.0,"betPrice":2.35,"betType":"BACK","marketId":2,"runnerId":22}""",result(13))
		assertEquals("""{"time":1235,"eventType":"PLACE_BET","betSize":23.0,"betPrice":2.0,"betType":"LAY","marketId":2,"runnerId":22}""",result(14))
		assertEquals("""{"time":1235,"eventType":"PLACE_BET","betSize":23.0,"betPrice":2.1,"betType":"LAY","marketId":2,"runnerId":22}""",result(15))
		assertEquals("""{"time":1235,"eventType":"PLACE_BET","betSize":3.0,"betPrice":2.2,"betType":"LAY","marketId":2,"runnerId":22}""",result(16))
		assertEquals("""{"time":1235,"eventType":"PLACE_BET","betSize":78.0,"betPrice":2.25,"betType":"BACK","marketId":2,"runnerId":22}""",result(17))
		assertEquals("""{"time":1235,"eventType":"PLACE_BET","betSize":70.0,"betPrice":2.3,"betType":"BACK","marketId":2,"runnerId":22}""",result(18))
	}
}