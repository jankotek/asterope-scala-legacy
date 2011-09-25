package org.asterope.geometry

import java.lang.Math._

/**
 * The class defines coordinate systems in terms of the
 * operations needed to transform the standard coordinate
 * system (currently J2000) to the coordinate system
 * associated with the object.  Most coordinate systems will
 * be defined as simple rotations, but some coordinate systems
 * may involve more complext transformations.
 *  A factory method is available to generate Coordinate systems from
 *  a given string.   Typically a string is comprised
 *  of an initial and an epoch (e.g., "B1950", "J1975.5").  Any string
 *  beginning with "G" is assumed to be Galactic coordinates.
 */
abstract class CoordinateSystem{

  /**Get the rotation associated with the projection.
   */
  def getRotater: Rotater

  /**Get the distortion associated with the projection.
   *  By default there is no distortion, but subclasses,
   *  notably BesselianProjection, can override this.
   */
  def getSphereDistorter: SphereDistorter = {
    return null
  }
  
  def getName:String

}


object CoordinateSystem {
  /**Get a coordinate system by name.
   * @param name A designation of the desired coordinate
   *              system.  Normally the name is an initial
   *              designating the general frame and orientation of
   *              the coordinate system followed by an epoch of equinox,
   *              e.g., J2000, B1950 E2000.45.
   *              The initial letters are:
   *  <dl><dt>J   <dd> Julian Equatorial Coordinates.
   *      <dt>B   <dd> BesselianSphereDistorter Equatorial Coordinates.
   *      <dt>E   <dd> Julian Ecliptic Coordinates
   *      <dt>H   <dd> Helioecliptic coordinates.
   *      <dt>G   <dd> Galactic coordinates.  Only the first letter is parsed.
   *  </dl>
   *  The name is not case-sensitive.
   */
  def factory(name: String): CoordinateSystem = {
    return factory(name, null)
  }

  def factory(name2: String, equinox: String): CoordinateSystem = {
    val name = name2.toUpperCase
    if (name.equals("ICRS")) {
      return ICRS
    }
    var c = name.charAt(0)
    if (c == 'G') {
      return Galactic
    }
    var epoch: Double = -1
    try {
      epoch = name.substring(1).toDouble
    }
    catch {
      case e: NumberFormatException => {
          if (equinox != null)
            epoch = equinox.toDouble
      }
    }
    c match {
      case 'J' =>
        if (epoch < 0) {
          epoch = 2000
        }
        return new Julian(epoch)
      case 'B' =>
        if (epoch < 0) {
          epoch = 1950
        }
        return new CoordinateSystemBesselian(epoch)
      case 'E' =>
        return new Ecliptic(epoch, 0)
      case 'H' =>
        return new Helioecliptic(epoch)
      case _ =>
        return null
    }
  }

  /**Standard J2000 coordinates -- the reference frame */
  def J2000: CoordinateSystem = new Julian(2000)
  /**Standard B1950 coordinates */
  def B1950: CoordinateSystem = new CoordinateSystemBesselian(1950)


  /** Galactic coordinate system */
  object Galactic extends CoordinateSystem{
    def getName = "Galactic"
    def getDescription: String =  "Coordinate system based upon the orientation and center of the Galaxy"
    private val poles  = Array[Double](122.931918, 27.128251, 192.859481)
    val getRotater = Rotater("ZYZ", math.toRadians(poles(2)), math.toRadians(90 - poles(1)), math.toRadians(180 - poles(0)))
  }

  /**A class defining the ICRS coordinate system.
   *  This should probably be the reference coordinate system, but it
   *  differs only very slightly from the J2000 coordinates.
   *  We are assuming the the J2000 coordinate system is FK5 based.
   *  This may not be true for at the 50 mas level.
   *
   *  According to Feissel and Mignard (1998) the rotation angles for
   *  the three axes are between the ICRS coordinates and FK5
   *     (-19.9, -9.1, -22.9)
   */

  object ICRS extends CoordinateSystem{
    def getName: String = "ICRS"
    def getDescription= "Non-precessing equatorial coordinate system"
    private val angles = Array(math.toRadians(-.0199 / 3600), math.toRadians(-.0091 / 3600), math.toRadians(+.0229 / 3600))
    val getRotater = Rotater("XYZ", angles(0), angles(1), angles(2))
  }


/**The class defining Julian coordinate systems.
 *  The reference coordinate system is J2000.
 *  We use the FK5 coordinate frame as the realization
 *  of the Julian coordinate system.  At the 50 milliarcsecond
 *  level this is not valid: There is an offset of J2000 and FK5.
 *  However the details of this offset are still somewhat unclear,
 *  so we use the better defined frame.
 * @param epoch The epoch of the equinox of the coordinates in calendar years (possibly fractional).
 */
class Julian(epoch:Double) extends CoordinateSystem{

  def getName = "J" + epoch
  def getDescription = "A Julian (FK5-based) equatorial coordinate system with epoch of the equinox of " + epoch

  val getRotater: Rotater =
     if(epoch == 2000.0)  null
      else precession

  /**Get the Julian Precession Matrix for a given epoch (from J2000).
   *  Approach based  on P.Wallace's SLA library.
   *  The equations are available
   *  in Kaplan, USNO Circular 163, 1981, page A2.  Here we assume
   *  in these equations we assume T=0 so that we are doing a
   *  transformation between J2000 and the specified ending epoch.
   *
   */
  private def precession: Rotater = {
    val sec2rad = 4.848136811095359935e-6

    //  Interval over which precession required (expressed in Julian
    //  centuries. In principal it should be in units of 365.24 Julian
    //  days.
    val t = (epoch - 2000) / 100

    //  Euler angles
    val tas2r = t * sec2rad
    val w = 2306.2181
    val zeta = (w + (0.30188 + 0.017998 * t) * t) * tas2r
    val z = (w + (1.09468 + 0.018203 * t) * t) * tas2r
    val theta: Double = (2004.3109 + (-0.42665 - 0.041833 * t) * t) * tas2r

    return Rotater("ZYZ", -zeta, theta, -z)
  }
}


/**An ecliptic coordinate system in a Julian frame.
 *
 * @param epoch The epoch of the equinox.
 * @param elon  The longitude in a standard coordinate system
 *               at which the prime meridian should be placed.
 */
class Ecliptic(epoch:Double, elon:Double = 0) extends CoordinateSystem{
  def getName =  "E" + epoch

  def getDescription: String = {
    return "A coordinate system with the ecliptic as the equator at epoch of equinox" + epoch
  }

  val getRotater: Rotater = {
    //TODO replace with code from ephem package

    val DAS2R = 4.84813681109535993589914102e-6
    //   Interval between basic epoch J2000.0 and current epoch (JC) */
    val t = (epoch - 2000) / 100
    //   Mean obliquity
    val eps0 = DAS2R * (84381.448 + (-46.8150 + (-0.00059 + 0.001813 * t) * t) * t)

    //   Mean obliquity
    val r1 = new Julian(epoch).getRotater
    val r2 = Rotater("XZ", eps0, elon, 0.)
    if (r1 == null) r2
    else r1.add(r2)
  }

}




/**A helioecliptic coordinate system at a given epoch.
 *  This gives a coordinate system where the Sun is at the
 *  center of the coordinate system.  We assume that the same epoch
 *  is to be used to get the position of the Sun and the basis
 *  Ecliptic coordinate system.
   @param epoch The desired epoch in years
 */
class Helioecliptic(epoch:Double) extends Ecliptic(epoch,Helioecliptic.sunlong(epoch)){

  override def getName: String = "H" + epoch
  override def getDescription = "A coordinate system with the equator along  the ecliptic and the Sun at the center. The position of the sun is inferred from the epoch."


}

protected object Helioecliptic{
  /**Find the ecliptic longitude of the Sun at a given epoch.
   *  Algorithm derived (and simplified) from the IDL Astronomy
   *  library sunpos (C.D. Pike, B. Emerson).
   * @param epoch (in years).
   * TODO replace this with alg from ephem package
   */
  def sunlong(epoch: Double): Double = {
    val dtor: Double = 3.1415926535 / 180.0
    val t: Double = ((epoch - 2000) * 365.25 + 2451544.5 - 2415020) / 36525.
    var l: Double = (279.696678 + ((36000.768925 * t) % 360)) * 3600
    val me: Double = 358.475844 + ((35999.049750 * t) % 360.0)
    var ellcor: Double = (6910.1 - 17.2 * t) * sin(me * dtor) + 72.3 * sin(2.0 * me * dtor)
    l += ellcor
    val mv: Double = 212.603219 + ((58517.803875 * t) % 360)
    var vencorr: Double = 4.8 * math.cos((299.1017 + mv - me) * dtor) + 5.5 * cos((148.3133 + 2.0 * mv - 2.0 * me) * dtor) + 2.5 * cos((315.9433 + 2.0 * mv - 3.0 * me) * dtor) + 1.6 * cos((345.2533 + 3.0 * mv - 4.0 * me) * dtor) + 1.0 * cos((318.15 + 3.0 * mv - 5.0 * me) * dtor)
    l += vencorr
    val mm: Double = 319.529425 + ((19139.858500 * t) % 360)
    var marscorr: Double = 2.0 * cos((343.8883 - 2.0 * mm + 2.0 * me) * dtor) + 1.8 * cos((200.4017 - 2.0 * mm + me) * dtor)
    l += marscorr
    val mj: Double = 225.328328 + ((3034.6920239 * t) % 360.0)
    var jupcorr: Double = 7.2 * cos((179.5317 - mj + me) * dtor) + 2.6 * cos((263.2167 - mj) * dtor) + 2.7 * cos((87.1450 - 2.0 * mj + 2.0 * me) * dtor) + 1.6 * cos((109.4933 - 2.0 * mj + me) * dtor)
    l += jupcorr
    val d: Double = 350.7376814 + ((445267.11422 * t) % 360.0)
    var mooncorr: Double = 6.5 * sin(d * dtor)
    l += mooncorr
    var longterm: Double = +6.4 * sin((231.19 + 20.20 * t) * dtor)
    l += longterm
    l = (l + 2592000.0) % 1296000.0
    val longmed: Double = l / 3600.0 * dtor
    return longmed
  }

}



}



