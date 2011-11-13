package org.asterope.ephem

import org.asterope.util.ScalaTestCase

class SiderialTimeTest extends ScalaTestCase{

  def testSiderialTime{

      val astro = new AstroDate(2004, AstroDate.JANUARY, 1, 0, 0, 0);
			val time = new TimeElement(astro.toGCalendar(), TimeElement.Scale.UNIVERSAL_TIME_UT1);
			val observer = ObserverElement.MADRID;
			val eph = new EphemerisElement(Target.Moon, EphemerisElement.Ephem.APPARENT,
					EphemerisElement.EQUINOX_OF_DATE, EphemerisElement.GEOCENTRIC, Precession.Method.IAU2000,
					EphemerisElement.Frame.ICRS);

			EarthRotationParameters.applyEOPParameters = false;

			val gmst = SiderealTime.greenwichMeanSiderealTime(time, observer, eph);
			val eceq = SiderealTime.equationOfEquinoxes(time, observer, eph);
			val ast = gmst + eceq; // SiderealTime.apparentSiderealTime(time,
										// observer, eph);
			System.out.println("jd " + astro.jd() + " / dst " + TimeScale.getDST(astro.jd(), observer));
			System.out.println("GMST: " + EphemUtils.formatRA(gmst));
			System.out.println("AST: " + EphemUtils.formatRA(ast));
			System.out.println("ECEQ: " + EphemUtils.formatRA(eceq));

      //TODO not sure those values are correct, based on first rade2Vector test run
      assert(EphemUtils.formatRA(gmst) === "06h 39m 59.6636s")
      assert(EphemUtils.formatRA(ast) === "06h 39m 58.9193s")
      assert(EphemUtils.formatRA(eceq) === "23h 59m 59.2557s")
  }
}
