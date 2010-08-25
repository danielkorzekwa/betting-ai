package dk.bettingai.marketsimulator

import org.junit._
import Assert._

class UserInputParserTest {

	@Test(expected=classOf[IllegalArgumentException])
	def testWrongNumberOfInputParameters { UserInputParser.parse(Array("incorrect parameters")) }

	@Test(expected=classOf[IllegalArgumentException])
	def testWrongMarketDataParameter { UserInputParser.parse(Array("marketDatasss=marketData.csv","traderImpl=dk.bettingai.trader.SimpleTraderImpl")) }

	@Test(expected=classOf[IllegalArgumentException])
	def testWrongTraderImplParameter { UserInputParser.parse(Array("marketData=marketData.csv","traderImpldfdfd=dk.bettingai.trader.SimpleTraderImpl")) }

	@Test(expected=classOf[IllegalArgumentException])
	def testWrongFormatOfParameters { UserInputParser.parse(Array("marketDatamarketData.csv","traderImpldfdfddk.bettingai.trader.SimpleTraderImpl")) }

	@Test(expected=classOf[IllegalArgumentException])
	def testNotExistingMarketDataFile { UserInputParser.parse(Array("marketData=/tmp/blabla/marketdata.csv","traderImpl=dk.bettingai.trader.SimpleTraderImpl")) }

	@Test(expected=classOf[IllegalArgumentException])
	def testTraderImplNotFound { UserInputParser.parse(Array("marketData=src/test/resources/marketDataEmpty.csv","traderImpl=com.dk.bettingai.blabla.SimpleTraderImpl")) }

	@Test(expected=classOf[IllegalArgumentException])
	def testTraderImplNoEmptyConstructor { UserInputParser.parse(Array("marketData=src/test/resources/marketDataEmpty.csv","traderImpl=dk.bettingai.marketsimulator.mock.TraderWithoutEmptyConstructor")) }
	
	@Test
	def testTraderImplCorrectDataOneMarketFile { 
		val inputData = UserInputParser.parse(Array("marketDataDir=src/test/resources/marketDataPlaceAndCancelLayBet","traderImpl=dk.bettingai.marketsimulator.trader.NopTrader")) 
		assertEquals(1,inputData._1.size)
		assertEquals(3,inputData._1(10).getLines.size)
		assertTrue(inputData._2.isInstanceOf[dk.bettingai.marketsimulator.trader.NopTrader])
	}
	
	@Test
	def testTraderImplCorrectDataTwoMarketFiles { 
		val inputData = UserInputParser.parse(Array("marketDataDir=src/test/resources/twomarketfiles","traderImpl=dk.bettingai.marketsimulator.trader.NopTrader")) 
		assertEquals(2,inputData._1.size)
		assertEquals(3,inputData._1(10).getLines.size)
		assertEquals(3,inputData._1(20).getLines.size)
		assertTrue(inputData._2.isInstanceOf[dk.bettingai.marketsimulator.trader.NopTrader])
	}
}