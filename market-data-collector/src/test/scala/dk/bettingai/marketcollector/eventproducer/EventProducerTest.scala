package dk.bettingai.marketcollector.eventproducer

import org.junit._
import Assert._
import dk.bettingai.marketsimulator.betex._
import Market._

class EventProducerTest {

	val eventProducer:EventProducer = new EventProducer()

@Test
def testProduce {

	/**key - selectionId, value - runner prices + price traded volume*/
	val runnerPrices = new RunnerPrice(2.0,23.53,0) :: new RunnerPrice(2.25,0,78.72) :: Nil 
	val tradedVolume = new PriceTradedVolume(1.9,3.4) :: new PriceTradedVolume(2.3,8.23) :: Nil
	val runnerPrices2 = new RunnerPrice(2.0,4.3,0) :: new RunnerPrice(2.1,100.02,0):: Nil 
	val tradedVolume2 = new PriceTradedVolume(1.9,2.1) :: new PriceTradedVolume(2.0,2.5) :: Nil
	val marketRunnersMap:Map[Long,Tuple2[List[RunnerPrice],List[PriceTradedVolume]]] =  Map((22,(runnerPrices,tradedVolume)),(23,(runnerPrices2,tradedVolume2)))
	
	val events = eventProducer.produce(2,marketRunnersMap)
	assertEquals(12,events.size)
	assertEquals("""{"eventType":"PLACE_BET","betSize":8.0,"betPrice":2.3,"betType":"LAY","marketId":2,"runnerId":22}""",events(0))
	assertEquals("""{"eventType":"PLACE_BET","betSize":8.0,"betPrice":2.3,"betType":"BACK","marketId":2,"runnerId":22}""",events(1))
	assertEquals("""{"eventType":"PLACE_BET","betSize":3.0,"betPrice":1.9,"betType":"LAY","marketId":2,"runnerId":22}""",events(2))
	assertEquals("""{"eventType":"PLACE_BET","betSize":3.0,"betPrice":1.9,"betType":"BACK","marketId":2,"runnerId":22}""",events(3))
	assertEquals("""{"eventType":"PLACE_BET","betSize":23.0,"betPrice":2.0,"betType":"LAY","marketId":2,"runnerId":22}""",events(4))
	assertEquals("""{"eventType":"PLACE_BET","betSize":78.0,"betPrice":2.25,"betType":"BACK","marketId":2,"runnerId":22}""",events(5))
	assertEquals("""{"eventType":"PLACE_BET","betSize":2.0,"betPrice":2.0,"betType":"LAY","marketId":2,"runnerId":23}""",events(6))
	assertEquals("""{"eventType":"PLACE_BET","betSize":2.0,"betPrice":2.0,"betType":"BACK","marketId":2,"runnerId":23}""",events(7))
	assertEquals("""{"eventType":"PLACE_BET","betSize":2.0,"betPrice":1.9,"betType":"LAY","marketId":2,"runnerId":23}""",events(8))
	assertEquals("""{"eventType":"PLACE_BET","betSize":2.0,"betPrice":1.9,"betType":"BACK","marketId":2,"runnerId":23}""",events(9))
	assertEquals("""{"eventType":"PLACE_BET","betSize":4.0,"betPrice":2.0,"betType":"LAY","marketId":2,"runnerId":23}""",events(10))
	assertEquals("""{"eventType":"PLACE_BET","betSize":100.0,"betPrice":2.1,"betType":"LAY","marketId":2,"runnerId":23}""",events(11))

	val nextRunnerPrices = new RunnerPrice(2.0,23.53,0) :: new RunnerPrice(2.25,0,88.72) :: Nil 
	val nextTradedVolume = new PriceTradedVolume(1.9,13.4) :: new PriceTradedVolume(2.3,8.23) :: Nil
	val nextRunnerPrices2 = new RunnerPrice(2.0,4.3,0) :: new RunnerPrice(2.1,100.02,0):: Nil 
	val nextTradedVolume2 = new PriceTradedVolume(1.9,2.1) :: new PriceTradedVolume(2.0,2.5) :: Nil
	val nextMarketRunnersMap:Map[Long,Tuple2[List[RunnerPrice],List[PriceTradedVolume]]] =  Map((22,(nextRunnerPrices,nextTradedVolume)),(23,(nextRunnerPrices2,nextTradedVolume2)))
	
	val events2 = eventProducer.produce(2,nextMarketRunnersMap)
	assertEquals(5,events2.size)
	assertEquals("""{"eventType":"CANCEL_BETS","betsSize":23.0,"betPrice":2.0,"betType":"LAY","marketId":2,"runnerId":22}""",events2(0))
	assertEquals("""{"eventType":"PLACE_BET","betSize":10.0,"betPrice":1.9,"betType":"LAY","marketId":2,"runnerId":22}""",events2(1))
	assertEquals("""{"eventType":"PLACE_BET","betSize":10.0,"betPrice":1.9,"betType":"BACK","marketId":2,"runnerId":22}""",events2(2))
	assertEquals("""{"eventType":"PLACE_BET","betSize":23.0,"betPrice":2.0,"betType":"LAY","marketId":2,"runnerId":22}""",events2(3))
	assertEquals("""{"eventType":"PLACE_BET","betSize":10.0,"betPrice":2.25,"betType":"BACK","marketId":2,"runnerId":22}""",events2(4))
	
	val events3 = eventProducer.produce(2,nextMarketRunnersMap)
	assertEquals(0,events3.size)
	
	val events4 = eventProducer.produce(3,nextMarketRunnersMap)
	assertEquals(12,events4.size)
}
	
	@Test
def testProduce2 {
		
	val runnerPrices = new RunnerPrice(20.0,11.0,0) :: new RunnerPrice(21,19.00,0) :: Nil 
	val tradedVolume = Nil
	val marketRunnersMap:Map[Long,Tuple2[List[RunnerPrice],List[PriceTradedVolume]]] =  Map((21,(Nil,Nil)),(22,(runnerPrices,tradedVolume)))
	val events = eventProducer.produce(2,marketRunnersMap)
	assertEquals(2,events.size)
	assertEquals("""{"eventType":"PLACE_BET","betSize":11.0,"betPrice":20.0,"betType":"LAY","marketId":2,"runnerId":22}""",events(0))
	assertEquals("""{"eventType":"PLACE_BET","betSize":19.0,"betPrice":21.0,"betType":"LAY","marketId":2,"runnerId":22}""",events(1))
	
	val nextRunnerPrices = new RunnerPrice(20.0,0,8) :: Nil 
	val nextTradedVolume = Nil
	val nextMarketRunnersMap:Map[Long,Tuple2[List[RunnerPrice],List[PriceTradedVolume]]] =  Map((21,(Nil,Nil)),(22,(nextRunnerPrices,nextTradedVolume)))
	
	val nextEvents = eventProducer.produce(2,nextMarketRunnersMap)
	assertEquals(3,nextEvents.size)
	assertEquals("""{"eventType":"CANCEL_BETS","betsSize":11.0,"betPrice":20.0,"betType":"LAY","marketId":2,"runnerId":22}""",nextEvents(0))
	assertEquals("""{"eventType":"CANCEL_BETS","betsSize":19.0,"betPrice":21.0,"betType":"LAY","marketId":2,"runnerId":22}""",nextEvents(1))
	assertEquals("""{"eventType":"PLACE_BET","betSize":8.0,"betPrice":20.0,"betType":"BACK","marketId":2,"runnerId":22}""",nextEvents(2))
	
	}
}