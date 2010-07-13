package dk.bettingai.marketcollector.eventproducer

import org.junit._
import Assert._

class EventProducerTest {

	@Test
	def test {
		val eventProducer = new EventProducer()
		val marketEvents = eventProducer.produce()
	}
}