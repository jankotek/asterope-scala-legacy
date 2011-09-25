package org.asterope.ephem.examples

import org.asterope.ephem._
import org.asterope.util._

/**
 * Testing precession.
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
object PrecessionExample {
  /**
   * A program to test precession.
   * @param args Unused
   */
  def main(args: Array[String]){
    var ra: String = "10h 05m 05s"
    var dec: String = "-15" + Angle.DEGREE_SIGN + " 20' 30''"
    var rightAscension: Double = Angle.parseRa(ra).toRadian
    var declination: Double = Angle.parseDe(dec).toRadian
    val locB1950: LocationElement = new LocationElement(rightAscension, declination, 1.0)
    val eqB1950 = locB1950.getRectangularCoordinates
    val eqJ2000 = Precession.B1950ToJ2000(eqB1950)
    val locJ2000: LocationElement = LocationElement.parseRectangularCoordinates(eqJ2000)
    rightAscension = locJ2000.getLongitude
    declination = locJ2000.getLatitude
    ra = EphemUtils.formatRA(rightAscension)
    dec = EphemUtils.formatDEC(declination)
    println("RA  J2000: " + ra)
    println("DEC J2000: " + dec)
  }
}


