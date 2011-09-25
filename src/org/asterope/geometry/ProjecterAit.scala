package org.asterope.geometry


import java.lang.Math.abs
import java.lang.Math.sqrt

/**
 * This class implements the
 *  <a href="http://www.quadibloc.com/maps/meq0801.htm"> AIT (Hammer-Aitoff) </a>
 *  projection.
 *  The Aitoff projection is an equal-area projection that transforms the sky into a elliptical region.
 *  The Aitoff projection is usually used for all sky projections around the origin.
 *  Maximum distortions are much less severe than for the Cartesian projection.
 *  <p>
 *  This version uses only the Math.sqrt
 *  function without any calls to trigonometric functions.
 *
 *
 */

object ProjecterAit extends Projecter {

  def getName = "Ait"
  def getDescription = "Project to an Hammer-Aitoff projection (often used for all sky data)"
  def inverse: Deprojecter = AitDeproj
  def isInverse(trans: Transformer) = trans == AitDeproj

  def transform(sphere: Array[Double], plane: Array[Double]){
    if (java.lang.Double.isNaN(sphere(2))) {
      plane(0) = Double.NaN
      plane(1) = Double.NaN
    } else {
      forwardTransform(sphere, plane)
    }
  }



  override def validPosition(plane: Array[Double]): Boolean = {
    return super.validPosition(plane) && plane(0) * plane(0) / 8 + plane(1) * plane(1) / 2 <= 1
  }

  protected def reverseTransform(plane: Array[Double], sphere: Array[Double]){
    var z: Double = (1 - plane(0) * plane(0) / 16 - plane(1) * plane(1) / 4)
    if (z > 0) {
      z = sqrt(z)
    }
    else {
      z = 0
    }
    sphere(2) = plane(1) * z
    val cos_b: Double = sqrt(1 - sphere(2) * sphere(2))
    if (abs(cos_b) > 1.e-12) {
      val sl2: Double = z * plane(0) / (2 * cos_b)
      val cl2: Double = (2 * z * z - 1) / cos_b
      val cl: Double = 2 * cl2 * cl2 - 1
      val sl: Double = 2 * sl2 * cl2
      sphere(0) = cl * cos_b
      sphere(1) = sl * cos_b
    }
    else {
      sphere(0) = 0
      sphere(1) = 0
    }
  }



  /**Find the shadow point for the given element.
   */
  override def shadowPoint(x: Double, y: Double): Array[Double] = {
    val xx: Array[Double] = Array[Double](x, y)
    val pnt: Array[Double] = new Array[Double](3)
    reverseTransform(xx, pnt)
    val slat: Double = pnt(2)
    var clat = 1 - slat * slat
    if (clat < 0) {
      clat = 0
    }
    clat = sqrt(clat)
    var lon: Double = math.atan2(pnt(1), pnt(0))
    lon = org.asterope.util.Angle.normalizeRa(lon)
    var gamma: Double = (1 + clat * math.cos(lon / 2))
    if (gamma > 0) {
      gamma = sqrt(2 / gamma)
    } else {
      gamma = 0
    }
    val res: Array[Double] = Array[Double](2 * gamma * clat * Math.sin(lon / 2), gamma * slat)
    if ((x > 0 && res(0) > 0) || (x < 0 && res(0) < 0)) {
      res(0) = -res(0)
    }
    return res
  }



  protected object AitDeproj extends Deprojecter {

    def getName = "AitDeproj"

    def getDescription = "Deproject from a Hammer-Aitoff ellipse back to the sphere."

    def inverse = ProjecterAit

    def isInverse(trans: Transformer) = trans == ProjecterAit

    def transform(plane: Array[Double], sphere: Array[Double]){
      if (!validPosition(plane)) {
        sphere(0) = Double.NaN
        sphere(1) = Double.NaN
        sphere(2) = Double.NaN
      }
      else {
        reverseTransform(plane, sphere)
      }
    }
  }

  def forwardTransform(sphere: Array[Double], plane: Array[Double]){
    val cos_b: Double = sqrt(1 - sphere(2) * sphere(2))
    var cos_l: Double = 0
    if (1 - abs(sphere(2)) > 1.e-10) {
      cos_l = sphere(0) / cos_b
    }
    var cos_l2: Double = (0.5 * (1 + cos_l))
    if (cos_l2 > 0) {
      cos_l2 = sqrt(cos_l2)
    } else {
      cos_l2 = 0
    }
    var sin_l2: Double = (0.5 * (1 - cos_l))
    if (sin_l2 > 0) {
      sin_l2 = sqrt(sin_l2)
    }  else {
      sin_l2 = 0
    }
    if (sphere(1) < 0) {
      sin_l2 = -sin_l2
    }
    val gamma: Double = sqrt(2 / (1 + cos_b * cos_l2))
    plane(0) = 2 * gamma * cos_b * sin_l2
    plane(1) = gamma * sphere(2)
  }

}
