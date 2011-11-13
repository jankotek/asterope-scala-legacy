package org.asterope.ephem

import java.util.GregorianCalendar
import org.asterope.util.ScalaTestCase

class TimeElementTest extends ScalaTestCase{

  def testTimeElement{
      val time: TimeElement = new TimeElement(new GregorianCalendar, TimeElement.Scale.TERRESTRIAL_TIME)
      println(" Time scale is " + time.timeScale)
      assert(time.timeScale === TimeElement.Scale.TERRESTRIAL_TIME)

  }
}
