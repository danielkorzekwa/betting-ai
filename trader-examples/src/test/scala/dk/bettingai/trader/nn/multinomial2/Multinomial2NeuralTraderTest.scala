package dk.bettingai.trader.nn.multinomial2

import org.junit._
import Assert._

class Multinomial2NeuralTraderTest {

  @Test
  def test {
    val traderClass = classOf[Multinomial2NeuralTrader]
    val marketDataDir = "marketDataDir=./src/test/resources/five_hr_10mins_before_inplay"
    //val marketDataDir = "marketDataDir=c:/daniel/marketdata10"
    val inputArgs = Array("htmlReportDir=./target/reports/" + traderClass.getSimpleName, "bank=100", marketDataDir, "traderImpl=" + traderClass.getName)
    dk.bettingai.marketsimulator.SimulatorApp.main(inputArgs)
  }
}