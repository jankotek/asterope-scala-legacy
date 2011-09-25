package org.asterope.ephem

import org.asterope.util.ScalaTestCase

class AstroDateTest extends ScalaTestCase {

  def testAstroDate{
    System.out.println("AstroDate Test")

    case class ATest(year:Int, month:Int, day:Int, frac:Double,jd:Double)

    // data from Meeus
    val at = List(
      ATest(2000, 1, 1, .5, 2451545.),
      ATest(1987, 6, 19, .5, 2446966.),
      ATest(1900, 1, 1, .0, 2415020.5),
      ATest(1600, 12, 31, .0, 2305812.5),
      ATest(837, 4, 10, .3, 2026871.8)

    //TODO      for following data test fails
//      ATest(-1000, 7, 12, .5, 1356001.),
//      ATest(-4712, 1, 1, .5, 0.)
  )

      at.foreach{a=>
        val ad = new AstroDate(a.year, a.month, a.day,
            (a.frac * EphemConstant.SECONDS_PER_DAY).toInt);
        val jdm = ad.jd();
        //double jdg = dmyToDoubleDay(ad);

        println("year: " + a.year + ", expected: " + a.jd + ", jd(m)=" + jdm  );
        assert(a.jd === jdm)
      }

  }
}
