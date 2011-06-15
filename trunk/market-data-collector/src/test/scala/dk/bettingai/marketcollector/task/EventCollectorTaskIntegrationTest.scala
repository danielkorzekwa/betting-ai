package dk.bettingai.marketcollector.task

import org.junit._
import Assert._
import dk.bot.betfairservice._
import dk.bettingai.marketcollector.marketservice._
import org.apache.commons.io._
import java.io.File
import org.slf4j.LoggerFactory

class EventCollectorTaskIntegrationTest {

  val log = LoggerFactory.getLogger(getClass)

  val betfairServiceFactoryBean = new dk.bot.betfairservice.DefaultBetFairServiceFactoryBean();
  var betfairService: BetFairService = null

  var marketService: MarketService = null
  var eventCollectorTask: EventCollectorTask = null

  val marketDataDir = "./target/eventcollectortask"

  @Before
  def setUp {

    /**Create betfair service and login to betfair account.*/
    betfairServiceFactoryBean.setUser(System.getenv("bfUser"))
    betfairServiceFactoryBean.setPassword(System.getenv("bfPassword"))
    val productId = if (System.getenv("bfProductId") != null) System.getenv("bfProductId").toInt else 82
    betfairServiceFactoryBean.setProductId(productId)
    val loginResponse = betfairServiceFactoryBean.login
    betfairService = (betfairServiceFactoryBean.getObject.asInstanceOf[BetFairService])

    /**Create event collector task.*/
    marketService = new MarketService(betfairService)
    val marketEventListener = EventCollectorTask.FilesystemEventListener(marketDataDir)
    eventCollectorTask = new EventCollectorTask(marketService, -60, 60, Option(3), 5,marketEventListener)

    /**Delete old market files.*/
    FileUtils.forceMkdir(new File(marketDataDir))
  }

  @Test
  def test {
    for (i <- 0 to 4) {
      log.info("EventCollectorTask is started.")
      eventCollectorTask.execute()
      log.info("EventCollectorTask is finished.")
    }
  }
}