package org.asterope.ephem.moons

import java.util.Map
import org.asterope.ephem._
import collection.JavaConversions._
import org.asterope.util._

class MoonEphemTest extends ScalaTestCase {

  implicit def toVector(elem:MoonEphemElement) = Vector3d.rade2Vector(Angle.normalizeRa(elem.rightAscension),elem.declination)
  def testIoGanymedeOccult: Unit = {
    val astroi: AstroDate = new AstroDate(2009, 4, 27, 6, 41, 43)
    val time: TimeElement = new TimeElement(astroi, TimeElement.Scale.UNIVERSAL_TIME_UTC)
    val obs: ObserverElement = ObserverElement.MADRID
    val eph: EphemerisElement = new EphemerisElement(Target.Jupiter, EphemerisElement.Ephem.APPARENT, EphemerisElement.EQUINOX_OF_DATE, EphemerisElement.TOPOCENTRIC, Precession.Method.IAU2000, EphemerisElement.Frame.J2000)
    val e: Map[Target, MoonEphemElement] = MoonEphem.galileanSatellitesEphemerides_L1(time, obs, eph)
    System.out.println(e.get(Target.Io).angle(e.get(Target.Ganymede)))
    for (t <- e.keySet) {
      System.out.println(t + " + " + e.get(t).mutualPhenomena)
    }
    System.out.println(e.get(Target.Io).rightAscension - e.get(Target.Ganymede).rightAscension)
    System.out.println(e.get(Target.Io).declination - e.get(Target.Ganymede).declination)
    System.out.println(e.get(Target.Io).rightAscension - e.get(Target.Callisto).rightAscension)
  }
}

