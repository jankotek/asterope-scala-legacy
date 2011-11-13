package org.asterope.ephem

import org.asterope.util._

class PrecessionTest extends ScalaTestCase{

  def testPrecession{
    System.out.println("Precession Test")

    val ra = "05h 32m 59.061s"
    val dec = "-05d 11' 52.28''"
    var loc = new LocationElement(EphemUtils.parseRightAscension(05, 32, 59.061), EphemUtils.parseDeclination(-5, 11, 52.28), 1.0)

    println(loc.getLongitude * Angle.R2D)
    println(loc.getLatitude * Angle.R2D)

    val q = LocationElement.parseLocationElement(loc)
    val eq = Precession.B1950ToJ2000(q)
    loc = LocationElement.parseRectangularCoordinates(eq)

    println("ra: " + EphemUtils.formatRA(loc.getLongitude))
    println("dec: " + EphemUtils.formatDEC(loc.getLatitude))
    println("0: " + eq(0))
    println("1: " + eq(1))
    println("2: " + eq(2))

    //TODO not sure if those values are correct, it come out when test was ran first time
    assert(eq(0) === 0.10649576185724702)
    assert(eq(1) === 0.99022689519881)
    assert(eq(2) === -0.09005192241906679)



    /* //an even older code, should be propably removed
     eq = Precession.precessFromJ2000(EphemUtils.J2000 + 36525, LocationElement.parseLocationElement(new LocationElement(0.0, 0.0, 1.0)), Precession.PRECESSION_LASKAR);
     loc = LocationElement.parseRectangularCoordinates(eq);

     System.out.println("ra: " + Functions.formatRA(loc.getLongitude()));
     System.out.println("dec: " + Functions.formatDEC(loc.getLatitude()));

     double ra1 = 0.0, dec1  = 0.0;
     double t = 1.0;
     double zeta = 2306.2181 * t + 0.30188 * t * t + 0.017998 * t * t * t;
     double z = 2306.2181 * t + 1.09468 * t * t + 0.018203 * t * t * t;
     double theta = 2004.3109 * t - 0.42665 * t * t - 0.041833 * t * t * t;
     zeta = zeta * EphemUtils.ARCSEC_TO_RAD;
     z = z * EphemUtils.ARCSEC_TO_RAD;
     theta = theta * EphemUtils.ARCSEC_TO_RAD;
     double sindec = Math.cos(ra1 + zeta) * Math.sin(theta) * Math.cos(dec1) + Math.cos(theta) * Math.sin(dec1);
     double sinra = Math.sin(ra1 + zeta) * Math.cos(dec1);
     double cosra = Math.cos(ra1 + zeta) * Math.cos(theta) * Math.cos(dec1) - Math.sin(theta) * Math.sin(dec1);

     System.out.println(Functions.formatRA(z + Math.atan2(sinra, cosra)));
     System.out.println(Functions.formatDEC(Math.asin(sindec)));
     */
  }
}
