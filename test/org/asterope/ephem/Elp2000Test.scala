package org.asterope.ephem

import org.asterope.util.ScalaTestCase

class Elp2000Test extends ScalaTestCase{

  def testElp2000{
  val astro: AstroDate = new AstroDate(-1000, AstroDate.JANUARY, 1, 0, 0, 0)
    val time: TimeElement = new TimeElement(astro, TimeElement.Scale.TERRESTRIAL_TIME)
    val observer: ObserverElement = ObserverElement.MADRID
    val eph: EphemerisElement = new EphemerisElement(Target.Moon, EphemerisElement.Ephem.APPARENT, EphemerisElement.EQUINOX_OF_DATE, EphemerisElement.GEOCENTRIC, Precession.Method.LASKAR, EphemerisElement.Frame.ICRS)
    val name: String = eph.targetBody.toString
    val ephem: EphemElement = Elp2000.elp2000Ephemeris(time, observer, eph)

    val dst = TimeScale.getDST(astro.jd, observer);
    System.out.println("jd " + astro.jd + " / dst " + dst)

    System.out.println("ELP2000")
    System.out.println("" + name + " RA: " + ephem.rightAscension)
    System.out.println("" + name + " DEC: " + ephem.declination)
    System.out.println("" + name + " dist: " + ephem.distance)
    System.out.println("" + name + " elong: " + ephem.elongation)
    System.out.println("" + name + " phaseAng: " + ephem.phaseAngle)


    //MOSHIER just to compare
    System.out.println("Moshier")

    val ephem2 = new MoshierPlanetEphem().getEphemeris(time, observer, eph, true)

    System.out.println("" + name + " RA: " + ephem2.rightAscension)
    System.out.println("" + name + " DEC: " + ephem2.declination)
    System.out.println("" + name + " dist: " + ephem2.distance)
    System.out.println("" + name + " elong: " + ephem2.elongation)
    System.out.println("" + name + " phaseAng: " + ephem2.phaseAngle)

    assert(astro.jd === 1356173.5)
    assert(dst === 0)
    assert(ephem.rightAscension === -1.2853882407853616)
    assert(ephem.declination === -0.3212567584977935)
    assert(ephem.distance === 0.0027122886053350765)
    assert(ephem.elongation === 0.28525543212890625)
    assert(ephem.phaseAngle === -2.8555612564086914 ) //TODO ELP2000 and Moshier gives oposite phase angle

  }
}

