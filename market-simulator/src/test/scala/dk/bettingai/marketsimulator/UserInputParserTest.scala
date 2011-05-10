package dk.bettingai.marketsimulator

import org.junit._
import Assert._
import dk.bettingai.marketsimulator.trader.examples._

class UserInputParserTest {

  @Test(expected = classOf[IllegalArgumentException])
  def testWrongNumberOfInputParameters { UserInputParser.parse(Array("incorrect parameters")) }

  @Test(expected = classOf[IllegalArgumentException])
  def testWrongMarketDataParameter { UserInputParser.parse(Array("marketDatasss=marketData.csv", "traderImpl=dk.bettingai.trader.SimpleTraderImpl")) }

  @Test(expected = classOf[IllegalArgumentException])
  def testWrongTraderImplParameter { UserInputParser.parse(Array("marketData=marketData.csv", "traderImpldfdfd=dk.bettingai.trader.SimpleTraderImpl")) }

  @Test(expected = classOf[IllegalArgumentException])
  def testWrongFormatOfParameters { UserInputParser.parse(Array("marketDatamarketData.csv", "traderImpldfdfddk.bettingai.trader.SimpleTraderImpl")) }

  @Test(expected = classOf[IllegalArgumentException])
  def testNotExistingMarketDataFile { UserInputParser.parse(Array("marketData=/tmp/blabla/marketdata.csv", "traderImpl=dk.bettingai.trader.SimpleTraderImpl")) }

  @Test(expected = classOf[IllegalArgumentException])
  def testTraderImplNotFound { UserInputParser.parse(Array("marketData=src/test/resources/marketDataEmpty.csv", "traderImpl=com.dk.bettingai.blabla.SimpleTraderImpl")) }

  @Test(expected = classOf[IllegalArgumentException])
  def testTraderImplNoEmptyConstructor { UserInputParser.parse(Array("marketData=src/test/resources/marketDataEmpty.csv", "traderImpl=dk.bettingai.marketsimulator.mock.TraderWithoutEmptyConstructor")) }

  @Test
  def testTraderImplCorrectDataOneMarketFile {
    val inputData = UserInputParser.parse(Array("marketDataDir=src/test/resources/marketDataPlaceAndCancelLayBet", "traderImpl=dk.bettingai.marketsimulator.trader.examples.NopTrader"))
    assertEquals(1, inputData.marketData.size)
    assertEquals("10.csv", inputData.marketData(10).getName)
    assertEquals(classOf[NopTrader],inputData.traderFactory.create().getClass)
  }

  @Test
  def testTraderImplCorrectDataTwoMarketFiles {
    val inputData = UserInputParser.parse(Array("marketDataDir=src/test/resources/twomarketfiles", "traderImpl=dk.bettingai.marketsimulator.trader.examples.NopTrader"))
    assertEquals(2, inputData.marketData.size)
    assertEquals("10.csv", inputData.marketData(10).getName)
    assertEquals("20.csv", inputData.marketData(20).getName)
    assertEquals(classOf[NopTrader],inputData.traderFactory.create().getClass)
  }

  @Test
  def testHtmlReportDirIsNotDefined {
    val inputData = UserInputParser.parse(Array("marketDataDir=src/test/resources/twomarketfiles", "traderImpl=dk.bettingai.marketsimulator.trader.examples.NopTrader"))

    assertEquals(2, inputData.marketData.size)
    assertEquals("10.csv", inputData.marketData(10).getName)
    assertEquals("20.csv", inputData.marketData(20).getName)
    assertEquals(classOf[NopTrader],inputData.traderFactory.create().getClass)
    assertEquals("./", inputData.reportDir)
  }

  @Test
  def testHtmlReportDirIsDefined {
    val inputData = UserInputParser.parse(Array("htmlReportDir=./target", "marketDataDir=src/test/resources/twomarketfiles", "traderImpl=dk.bettingai.marketsimulator.trader.examples.NopTrader"))

    assertEquals(2, inputData.marketData.size)
    assertEquals("10.csv", inputData.marketData(10).getName)
    assertEquals("20.csv", inputData.marketData(20).getName)
    assertEquals(classOf[NopTrader],inputData.traderFactory.create().getClass)
    assertEquals("./target", inputData.reportDir)
  }

  @Test
  def testBankIsNotDefined {
    val inputData = UserInputParser.parse(Array("marketDataDir=src/test/resources/twomarketfiles", "traderImpl=dk.bettingai.marketsimulator.trader.examples.NopTrader"))
    assertEquals(2500, inputData.bank,0)
  }

  @Test
  def testBankIsDefined {
    val inputData = UserInputParser.parse(Array("bank=1000","marketDataDir=src/test/resources/twomarketfiles", "traderImpl=dk.bettingai.marketsimulator.trader.examples.NopTrader"))
    assertEquals(1000, inputData.bank,0)
  }
}