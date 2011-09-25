package org.asterope.geometry

import java.lang.Math.sqrt

/**This class implements Besselian coordinate systems.
 *  These systems are not simple rotations from the reference
 *  coordinate frame.  These coordinate systems are implemented
 *  such that the rotation matrix is appropriate for J2000 coordinates
 *  but the rectify and derectify function perform transformation
 *  from/to Besselian coordinates.  The transformations do
 *  not use any proper motion or distance information supplied
 *  by the user.  The methods in this class are based on P. Wallaces
 *  SLA library substantially modified for use within Java and SkyView.
 *
 * @param epoch The epoch as a calendar year (possibly fractional).
 *  From Wiki:
 *
A Besselian epoch, named after the German mathematician and astronomer
 Friedrich Bessel (1784–1846), is an epoch that is based on a Besselian y
 ear of 365.242198781 days, which is a tropical year measured at the point
 where the Sun's longitude is exactly 280°. Since 1984, Besselian
 equinoxes/epochs have been superseded by Julian equinoxes/epochs.
 The current standard equinox/epoch is J2000.0, which is a Julian equinox/epoch.

 Besselian equinoxes/epochs are calculated according to:

 B = 1900.0 + (Julian date − 2415020.31352) / 365.242198781
 The previous standard equinox/epoch was B1950.0, a Besselian equinox/epoch.

 Since the right ascension and declination of stars are constantly changing
 due to precession, astronomers always specify these with reference to
 a particular equinox. Historically used Besselian equinoxes include
 B1875.0, B1900.0, B1925.0 and B1950.0. The official constellation boundaries
 were defined in 1930 using B1875.0.
 *
 */
class CoordinateSystemBesselian(epoch:Double) extends CoordinateSystem{

  /**This coordinate system is not just a rotation away from the reference frame.*/
  def isRotation = false

  def getName = "B" + epoch

  def getDescription =  "A Beseelian (FK4 based) equatorial coordinate system.  Dynamic terms are not included."


  def getRotater: Rotater = {
    return precession(epoch)
  }

  override def getSphereDistorter: SphereDistorter =  BesselianSphereDistorter


  /**
   * Calculate the Besselian Precession between 1950 and the given epoch.
   */
  private def precession(epoch: Double): Rotater = {
    val DAS2R: Double = 4.8481368110953599358991410235794797595635330237270e-6
    val bigt: Double = (1950 - 1850) / 100.0
    val t: Double = (epoch - 1950) / 100.0
    val tas2r: Double = t * DAS2R
    val w: Double = 2303.5548 + (1.39720 + 0.000059 * bigt) * bigt
    val zeta: Double = (w + (0.30242 - 0.000269 * bigt + 0.017996 * t) * t) * tas2r
    val z: Double = (w + (1.09478 + 0.000387 * bigt + 0.018324 * t) * t) * tas2r
    val theta: Double = (2005.1125 + (-0.85294 - 0.000365 * bigt) * bigt + (-0.42647 - 0.000365 * bigt - 0.041802 * t) * t) * tas2r
    return Rotater("ZYZ", -zeta, theta, -z)
  }



/**This class implements the distortion of BesselianSphereDistorter coordinate systems.
 */
protected object BesselianSphereDistorter extends SphereDistorter {

  private final val D2PI: Double = 6.2831853071795864769252867665590057683943387987502
  private final val pmf: Double = 100.0 * 60 * 60 * 360 / D2PI

  /**Get the inverse distorter */
  def inverse: SphereDistorter = {
    return BesselianInverse
  }

  /**Is the the inverse of another transformation */
  def isInverse(t: Transformer): Boolean = {
    return t == BesselianInverse
  }

  override def getName = "BesselianSphereDistorter distorter"
  override def getDescription = "A BesselianSphereDistorter (FK4 based) distortion.  Dynamic terms are not included."

  private val a  = Array[Double](-1.62557E-6, -0.31919E-6, -0.13843E-6)
  private val emi = Array[Array[Double]](Array(0.9999256795, 0.0111814828, 0.0048590039), Array(-0.0111814828, 0.9999374849, -0.0000271771), Array(-0.0048590040, -0.0000271557, 0.9999881946))


  def transform(x: Array[Double], y: Array[Double]): Unit = {
    var t10 = 0.0
    var t11 = 0.0
    var t12 = 0.0

    t10 = x(0) * emi(0)(0) + x(1) * emi(0)(1) + x(2) * emi(0)(2)
    t11 = x(0) * emi(1)(0) + x(1) * emi(1)(1) + x(2) * emi(1)(2)
    t12 = x(0) * emi(2)(0) + x(1) * emi(2)(1) + x(2) * emi(2)(2)

    y(0) = t10
    y(1) = t11
    y(2) = t12

    var rxyz: Double = sqrt(y(0) * y(0) + y(1) * y(1) + y(2) * y(2))
    var w: Double = 0

    var i = 0
    while (i < 3) {
      w += a(i) * y(i)
      i += 1
    }

    t10 = (1 - w) * y(0) + a(0) * rxyz
    t11 = (1 - w) * y(1) + a(1) * rxyz
    t12 = (1 - w) * y(2) + a(2) * rxyz

    rxyz = sqrt(t10 * t10 + t11 * t11 + t12 * t12)

    i = 0
    while (i < 3) {
      w += a(i) * y(i)
      i += 1
    }

    i = 0
    while (i < 3) {
      y(i) = (1 - w) * y(i) + a(i) * rxyz
      i += 1
    }
    rxyz = sqrt(y(0) * y(0) + y(1) * y(1) + y(2) * y(2))

    i =0
    while (i < 3) {
      y(i) /= rxyz
      i += 1
    }
  }


  /**This inner class defines the inverse distortion
   *  to the enclosing BesselianSphereDistorter distorter.
   */
  protected object BesselianInverse extends org.asterope.geometry.SphereDistorter {
    override def getName = "Inv. " + BesselianSphereDistorter.this.getName


    def inverse: SphereDistorter = {
      return BesselianSphereDistorter
    }

    /**Is the the inverse of another transformation */
    def isInverse(t: Transformer): Boolean = {
      return t == BesselianSphereDistorter
    }

    override def getDescription: String = {
      return BesselianSphereDistorter.this.getDescription + " (inverse)"
    }

    private val a  = Array[Double](-1.62557E-6, -0.31919E-6, -0.13843E-6)
    private val em1 = Array[Array[Double]](Array(0.9999256782, -0.0111820611, -0.0048579477), Array(0.0111820610, 0.9999374784, -0.0000271765), Array(0.0048579479, -0.0000271474, 0.9999881997))
    private val em2 = Array[Array[Double]](Array(-0.000551, -0.238565, 0.435739), Array(0.238514, -0.002667, -0.008541), Array(-0.435623, 0.012254, 0.002117))

    /**
     * Convert coordinates from B1950 to J2000 for epoch 1950.
     */
    def transform(x: Array[Double], y: Array[Double]){

      var t10 = 0.0
      var t11 = 0.0
      var t12 = 0.0


      var w: Double = 0

      var i: Int = 0
      while (i < 3) {
        w += a(i) * x(i)
        i += 1
      }

      t10 = x(0) - a(0) - w * x(0)
      t11 = x(1) - a(1) - w * x(1)
      t12 = x(2) - a(2) - w * x(2)
      y(0) = t10
      y(1) = t11
      y(2) = t12


      t10 = y(0) * em1(0)(0) + y(1) * em1(0)(1) + y(2) * em1(0)(2)
      t11 = y(0) * em1(1)(0) + y(1) * em1(1)(1) + y(2) * em1(1)(2)
      t12 = y(0) * em1(2)(0) + y(1) * em1(2)(1) + y(2) * em1(2)(2)



      y(0) = t10
      y(1) = t11
      y(2) = t12

      val tdelta: Double = -50 / pmf

      y(0) += tdelta * t10
      y(1) += tdelta * t11
      y(2) += tdelta * t12

      val rxyz: Double = math.sqrt(y(0) * y(0) + y(1) * y(1) + y(2) * y(2))
      i = 0
      while (i < 3) {
        y(i) /= rxyz
        i += 1
      }

    }
  }
}
}


