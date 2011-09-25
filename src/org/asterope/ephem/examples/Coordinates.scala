package org.asterope.ephem.examples

import org.asterope.ephem.AstroDate
import org.asterope.ephem.CoordinateSystem
import org.asterope.ephem.EphemConstant
import org.asterope.ephem.EphemUtils
import org.asterope.ephem.EphemerisElement
import org.asterope.ephem.LocationElement
import org.asterope.ephem.ObserverElement
import org.asterope.ephem.Precession
import org.asterope.ephem.Target
import org.asterope.ephem.TimeElement
import org.asterope.util._

/**
 * Testing coordinate conversion.
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
object Coordinates{
  /**
   * A program to test coordinate conversion.
   * @param args Unused
   */
  def main(args: Array[String]): Unit = {
    var ra: String = "10h 05m 05s"
    var dec: String = "-15" + Angle.DEGREE_SIGN + " 20' 30''"
    val rightAscension: Double = Angle.parseRa(ra).toRadian
    val declination: Double = Angle.parseDe(ra).toRadian
    var loc: LocationElement = new LocationElement(rightAscension, declination, 1.0)
    val astro: AstroDate = new AstroDate(1, AstroDate.JANUARY, 2000)
    val time: TimeElement = new TimeElement(astro, TimeElement.Scale.UNIVERSAL_TIME_UTC)
    val obs: ObserverElement = new ObserverElement("Madrid", -3.7100 * EphemConstant.DEG_TO_RAD, 40.420 * EphemConstant.DEG_TO_RAD, 693, 1)
    val eph: EphemerisElement = new EphemerisElement(Target.Saturn, EphemerisElement.Ephem.APPARENT, EphemerisElement.EQUINOX_OF_DATE, EphemerisElement.TOPOCENTRIC, Precession.Method.IAU2000, EphemerisElement.Frame.J2000)
    loc = CoordinateSystem.equatorialToGalactic(loc, time, obs, eph)
    ra = EphemUtils.formatAngle(loc.getLongitude, 0)
    dec = EphemUtils.formatAngle(loc.getLatitude, 0)
    println("RA  J2000: " + ra)
    println("DEC J2000: " + dec)
  }
}


