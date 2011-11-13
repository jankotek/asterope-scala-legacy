package org.asterope.ephem

import org.asterope.util.ScalaTestCase

/**
 * Not sure if values in this test are correct.
 * There is also lot of duplicities
 */
class NutationTest extends ScalaTestCase{


  val astro = new AstroDate(2004, AstroDate.JANUARY, 1, 0, 0, 0);
  val d = astro.jd();

  def testDate{
    println(d);
    assert(d === 2453005.5)

  }

  def testNutation1980{
	EarthRotationParameters.clearEOP(); 
    Nutation.calcNutation(EphemUtils.toCenturies(d), Nutation.Method.NUTATION_IAU1980);
    var dPhi = (Nutation.nutationInLongitude * EphemConstant.RAD_TO_ARCSEC)
    var dEpsilon = (Nutation.nutationInObliquity * EphemConstant.RAD_TO_ARCSEC)
    println("dPhi=" + dPhi + ", dEpsilon=" + dEpsilon);
    assert(dPhi === -12.169178501291626)
    assert(dEpsilon === 5.739452291994096)
  }

  def testNutation2000{
    EarthRotationParameters.obtainEOP(EphemUtils.toCenturies(d), Precession.Method.IAU2000);
    Nutation.calcNutation(EphemUtils.toCenturies(d), Nutation.Method.NUTATION_IAU1980);

    val dPhi =  (Nutation.nutationInLongitude * EphemConstant.RAD_TO_ARCSEC)
    val dEpsilon = (Nutation.nutationInObliquity * EphemConstant.RAD_TO_ARCSEC)
    println("dPhi=" + dPhi + ", dEpsilon=" + dEpsilon);
  }


  def testNutation2000B{
    EarthRotationParameters.clearEOP();
    println("IAU2000");
    Nutation.calcNutation(EphemUtils.toCenturies(d), Nutation.Method.NUTATION_IAU2000);
    val dPhi = (Nutation.nutationInLongitude * EphemConstant.RAD_TO_ARCSEC)
    val dEpsilon = (Nutation.nutationInObliquity * EphemConstant.RAD_TO_ARCSEC)
    println("dPhi=" + dPhi + ", dEpsilon=" + dEpsilon);
    assert(dPhi === -12.17157545301567)
    assert(dEpsilon === 5.7470405384358045)
  }

  def testNutation2000C{
    EarthRotationParameters.obtainEOP(EphemUtils.toCenturies(d), Precession.Method.IAU2000);
    Nutation.calcNutation(EphemUtils.toCenturies(d), Nutation.Method.NUTATION_IAU2000);
    val dPhi = (Nutation.nutationInLongitude * EphemConstant.RAD_TO_ARCSEC)
    val dEpsilon = (Nutation.nutationInObliquity * EphemConstant.RAD_TO_ARCSEC)

    println("dPhi=" + dPhi + ", dEpsilon=" + dEpsilon);
    assert(dPhi === -12.17157545301567)
    assert(dEpsilon === 5.7470405384358045)

  }
}
