package dk.bettingai.marketsimulator.trader

import org.junit._
import  Assert._
import dk.bettingai.marketsimulator.betex._
import api._
import Market._
import IBet.BetTypeEnum._
import java.util.Date

class TraderContextTest {

	private val betex = new Betex()
	private val market:IMarket  = betex.createMarket(1l,"marketName","eventName",1,new Date(System.currentTimeMillis),new Runner(11,"runnerName1") :: new Runner(12,"runnerName2") :: Nil)

	private var lastBetId=0l
	private val nextBetId = () => {lastBetId+=1;lastBetId}

	@Test def testPlaceHedgeBetNoHedgeBetNoPricesToBet {
		val ctx = new TraderContext(nextBetId(),200,market,0)
		market.placeBet(nextBetId(),201l,2,3,LAY,11l)
		ctx.placeBet(2,3,BACK,11l)

		val hedgeBet:Option[IBet] = ctx.placeHedgeBet(11l)
		assertEquals(None,hedgeBet)
	}

	@Test def testPlaceHedgeBetLayHedgeBetIsPlaced {
		val ctx = new TraderContext(nextBetId(),200,market,0)
		market.placeBet(nextBetId(),201l,2,3,LAY,11l)
		market.placeBet(nextBetId(),201l,2,4,BACK,11l)
		ctx.placeBet(2,3,BACK,11l)

		val hedgeBet:Option[IBet] = ctx.placeHedgeBet(11l)
		assertEquals(4,hedgeBet.get.betPrice,0)
		assertEquals(1.5,hedgeBet.get.betSize,0)
		assertEquals(LAY,hedgeBet.get.betType)
		assertEquals(11,hedgeBet.get.runnerId)

		/**No hedge bet is placed this time.*/
		val nextHedgeBet:Option[IBet] = ctx.placeHedgeBet(11l)
		assertEquals(None,nextHedgeBet)
		assertEquals(-0.5,ctx.risk.ifWin(11),0)
		assertEquals(-0.5,ctx.risk.ifLose(11),0)
	}

	@Test def testPlaceHedgeBetBackHedgeBetIsPlaced {
		val ctx = new TraderContext(nextBetId(),200,market,0)
		market.placeBet(nextBetId(),201l,2,3,LAY,11l)
		market.placeBet(nextBetId(),201l,2,4,BACK,11l)
		ctx.placeBet(1.5,4,LAY,11l)

		val hedgeBet:Option[IBet] = ctx.placeHedgeBet(11l)
		assertEquals(3,hedgeBet.get.betPrice,0)
		assertEquals(2,hedgeBet.get.betSize,0)
		assertEquals(BACK,hedgeBet.get.betType)
		assertEquals(11,hedgeBet.get.runnerId)

		/**No hedge bet is placed this time.*/
		val nextHedgeBet:Option[IBet] = ctx.placeHedgeBet(11l)
		assertEquals(None,nextHedgeBet)
		assertEquals(-0.5,ctx.risk.ifWin(11),0)
		assertEquals(-0.5,ctx.risk.ifLose(11),0)
	}

}