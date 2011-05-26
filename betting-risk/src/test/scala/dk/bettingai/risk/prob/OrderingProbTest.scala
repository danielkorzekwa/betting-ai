package dk.bettingai.risk.prob

import org.junit._
import Assert._

class OrderingProbTest {

  /**Test scenarios for calcPlaceProb.*/

  @Test(expected = classOf[IllegalArgumentException])
  def calcPlaceProb_num_of_runners_is_less_than_2 {
    val marketProb: Map[Long, Double] = Map(10l -> 1 / 2d)
    OrderingProb.calcPlaceProb(10l, marketProb)
  }
  @Test
  def calcPlaceProb_three_runners_with_different_prob {
    val marketProb: Map[Long, Double] = Map(10l -> 1 / 2d, 11l -> 1 / 3d, 12l -> 1 / 6d)

    assertEquals(0.85, OrderingProb.calcPlaceProb(10l, marketProb), 0)
    assertEquals(0.733, OrderingProb.calcPlaceProb(11l, marketProb), 0.001)
    assertEquals(0.416, OrderingProb.calcPlaceProb(12l, marketProb), 0.001)
  }

  @Test
  def calcPlaceProb_three_runners_with_the_same_prob {
    val marketProb: Map[Long, Double] = Map(10l -> 1 / 3d, 11l -> 1 / 3d, 12l -> 1 / 3d)

    assertEquals(0.666, OrderingProb.calcPlaceProb(10l, marketProb), 0.001)
    assertEquals(0.666, OrderingProb.calcPlaceProb(11l, marketProb), 0.001)
    assertEquals(0.666, OrderingProb.calcPlaceProb(12l, marketProb), 0.001)
  }

  @Test
  def calcPlaceProb_two_runners_with_different_prob {
    val marketProb: Map[Long, Double] = Map(10l -> 1 / 1.5d, 11l -> 1 / 3d)

    assertEquals(1, OrderingProb.calcPlaceProb(10l, marketProb), 0.001)
    assertEquals(1, OrderingProb.calcPlaceProb(11l, marketProb), 0.001)
  }

  @Test
  def calcPlaceProb_two_runners_with_the_same_prob {
    val marketProb: Map[Long, Double] = Map(10l -> 1 / 2d, 11l -> 1 / 2d)

    assertEquals(1, OrderingProb.calcPlaceProb(10l, marketProb), 0.001)
    assertEquals(1, OrderingProb.calcPlaceProb(11l, marketProb), 0.001)
  }

  @Test
  def calcPlaceProb_four_runners_with_different_prob {
    val marketProb: Map[Long, Double] = Map(10l -> 1 / 6d, 11l -> 1 / 2d, 12l -> 1 / 6d, 13l -> 1 / 6d)

    assertEquals(0.399, OrderingProb.calcPlaceProb(10l, marketProb), 0.001)
    assertEquals(0.8, OrderingProb.calcPlaceProb(11l, marketProb), 0.001)
    assertEquals(0.399, OrderingProb.calcPlaceProb(12l, marketProb), 0.001)
    assertEquals(0.399, OrderingProb.calcPlaceProb(13l, marketProb), 0.001)
  }

  @Test
  def calcPlaceProb_four_runners_with_the_same_prob {
    val marketProb: Map[Long, Double] = Map(10l -> 1 / 4d, 11l -> 1 / 4d, 12l -> 1 / 4d, 13l -> 1 / 4d)

    assertEquals(0.5, OrderingProb.calcPlaceProb(10l, marketProb), 0)
    assertEquals(0.5, OrderingProb.calcPlaceProb(11l, marketProb), 0)
    assertEquals(0.5, OrderingProb.calcPlaceProb(12l, marketProb), 0)
    assertEquals(0.5, OrderingProb.calcPlaceProb(13l, marketProb), 0)
  }

  /**Test scenarios for calcShowProb.*/
  
  @Test(expected = classOf[IllegalArgumentException])
  def calcShowProb_num_of_runners_is_less_than_3 {
    val marketProb: Map[Long, Double] = Map(10l -> 1 / 2d, 11l -> 1 / 2d)
    OrderingProb.calcShowProb(10l, marketProb)
  }
  @Test
  def calcShowProb_three_runners_with_different_prob {
    val marketProb: Map[Long, Double] = Map(10l -> 1 / 2d, 11l -> 1 / 3d, 12l -> 1 / 6d)

    assertEquals(1, OrderingProb.calcShowProb(10l, marketProb), 0)
    assertEquals(1, OrderingProb.calcShowProb(11l, marketProb), 0.001)
    assertEquals(1, OrderingProb.calcShowProb(12l, marketProb), 0.001)
  }

  @Test
  def calcShowProb_three_runners_with_the_same_prob {
    val marketProb: Map[Long, Double] = Map(10l -> 1 / 3d, 11l -> 1 / 3d, 12l -> 1 / 3d)

    assertEquals(1, OrderingProb.calcShowProb(10l, marketProb), 0.001)
    assertEquals(1, OrderingProb.calcShowProb(11l, marketProb), 0.001)
    assertEquals(1, OrderingProb.calcShowProb(12l, marketProb), 0.001)
  }

  @Test
  def calcShowProb_four_runners_with_different_prob {
    val marketProb: Map[Long, Double] = Map(10l -> 1 / 6d, 11l -> 1 / 2d, 12l -> 1 / 6d, 13l -> 1 / 6d)

    assertEquals(0.683, OrderingProb.calcShowProb(10l, marketProb), 0.001)
    assertEquals(0.95, OrderingProb.calcShowProb(11l, marketProb), 0.001)
    assertEquals(0.683, OrderingProb.calcShowProb(12l, marketProb), 0.001)
    assertEquals(0.683, OrderingProb.calcShowProb(13l, marketProb), 0.001)
  }

  @Test
  def calcShowProb_four_runners_with_the_same_prob {
    val marketProb: Map[Long, Double] = Map(10l -> 1 / 4d, 11l -> 1 / 4d, 12l -> 1 / 4d, 13l -> 1 / 4d)

    assertEquals(0.75, OrderingProb.calcShowProb(10l, marketProb), 0)
    assertEquals(0.75, OrderingProb.calcShowProb(11l, marketProb), 0)
    assertEquals(0.75, OrderingProb.calcShowProb(12l, marketProb), 0)
    assertEquals(0.75, OrderingProb.calcShowProb(13l, marketProb), 0)
  }

}