//
// Licenced under GPLv2, see licence.txt
// (c) K.M. Gorski, Nickolai Kuropatkin, Jan Kotek,
//
package org.asterope.healpix

import org.asterope.util.Vector3d

/**various utilities not directly related to Healpix.*/
object PixToolsUtils {
  /**
   * calculates the surface of spherical triangle defined by
   * vertices v1,v2,v3 Algorithm: finds triangle sides and uses l'Huilier
   * formula to compute "spherical excess" = surface area of triangle on a
   * sphere of radius one see, eg Bronshtein, Semendyayev Eq 2.86 half
   * perimeter hp = 0.5*(side1+side2+side3) l'Huilier formula x0 = tan( hp/2.)
   * x1 = tan((hp - side1)/2.) x2 = tan((hp - side2)/2.) x3 = tan((hp -
   * side3)/2.)
   *
   * @param v1 Vector3d
   * @param v2 Vector3d
   * @param v3 Vector3d vertices of the triangle
   * @return double the triangle surface
   * @throws Exception
   *
   */
  def SurfaceTriangle(v1: Vector3d, v2: Vector3d, v3: Vector3d): Double = {
    var res: Double = 0.
    val side1: Double = v2.angle(v3) / 4.0
    val side2: Double = v3.angle(v1) / 4.0
    val side3: Double = v1.angle(v2) / 4.0
    val x0: Double = math.tan(side1 + side2 + side3)
    val x1: Double = math.tan(side2 + side3 - side1)
    val x2: Double = math.tan(side1 + side3 - side2)
    val x3: Double = math.tan(side1 + side2 - side3)
    res = 4.0 * math.atan(math.sqrt(x0 * x1 * x2 * x3))
    res
  }

  /**
   * returns polar coordinates in radians given ra, dec in degrees
   * @param radec double array containing ra,dec in degrees
   * @return res double array containing theta and phi in radians
   *             res[0] = theta res[1] = phi
   */
  def RaDecToPolar(radec: Array[Double]): Array[Double] = {
    val res: Array[Double] = Array(0.0, 0.0)
    val ra: Double = radec(0)
    val dec: Double = radec(1)
    val theta: Double = math.Pi / 2.- math.toRadians(dec)
    val phi: Double = math.toRadians(ra)
    res(0) = theta
    res(1) = phi
    res
  }

  /**
   * returns ra, dec in degrees given polar coordinates in radians
   * @param polar double array polar[0] = phi in radians
   *                           polar[1] = theta in radians
   * @return double array radec radec[0] = ra in degrees
   *                radec[1] = dec in degrees
   */
  def PolarToRaDec(polar: Array[Double]): Array[Double] = {
    val radec: Array[Double] = Array(0.0, 0.0)
    val phi: Double = polar(1)
    var theta: Double = polar(0)
    val dec: Double = math.toDegrees(math.Pi / 2.- theta)
    val ra: Double = math.toDegrees(phi)
    radec(0) = ra
    radec(1) = dec
    radec
  }

  /**
   * converts a Vector3d in a tuple of angles tup[0] = theta
   * co-latitude measured from North pole, in [0,PI] radians, tup[1] = phi
   * longitude measured eastward, in [0,2PI] radians
   *
   * @param v
   *            Vector3d
   * @return double[] out_tup out_tup[0] = theta out_tup[1] = phi
   */
  def Vect2Ang(v: Vector3d): Array[Double] = {
    val out_tup: Array[Double] = new Array[Double](2)
    val norm: Double = v.length
    val z: Double = v.z / norm
    val theta: Double = math.acos(z)
    var phi: Double = 0.
    if ((v.x != 0.) || (v.y != 0)) {
      phi = math.atan2(v.y, v.x)
    }
    if (phi < 0) phi += 2.0 * math.Pi
    out_tup(0) = theta
    out_tup(1) = phi
    out_tup
  }

  /**
   * computes the intersection di of 2 intervals d1 (= [a1,b1])
   * and d2 (= [a2,b2]) on the periodic domain (=[A,B] where A and B
   * arbitrary) ni is the resulting number of intervals (0,1, or 2) if a1 <b1
   * then d1 = {x |a1 <= x <= b1} if a1>b1 then d1 = {x | a1 <=x <= B U A <=x
   * <=b1}
   *
   * @param d1 double[] first interval
   * @param d2 double[] second interval
   * @return double[] one or two intervals intersections
   */
  def intrs_intrv(d1: Array[Double], d2: Array[Double]): Array[Double] = {
    var res: Array[Double] = null
    val epsilon: Double = 1.0e-10

    var dk: Array[Double] = null
    var di: Array[Double] = Array(0.)
    var ik: Int = 0
    val tr12: Boolean = (d1(0) < d1(1) + epsilon)
    val tr21: Boolean = !tr12 // d1[0] >= d1[1]
    val tr34: Boolean = (d2(0) < d2(1) + epsilon)
    val tr43: Boolean = !tr34 // d2[0]>d2[1]
    val tr13: Boolean = (d1(0) < d2(0) + epsilon) //  d2[0] can be in interval
    val tr31: Boolean = !tr13 // d1[0] in longerval
    val tr24: Boolean = (d1(1) < d2(1) + epsilon)// d1[1] upper limit
    val tr42: Boolean = !tr24 // d2[1] upper limit
    val tr14: Boolean = (d1(0) < d2(1) + epsilon) // d1[0] in interval
    val tr32: Boolean = (d2(0) < d1(1) + epsilon) // d2[0] in interval

    ik = 0
    dk = Array[Double](-1.0e9, -1.0e9, -1.0e9, -1.0e9)
    /* d1[0] lower limit case 1 */
    if ((tr34 && tr31 && tr14) || (tr43 && (tr31 || tr14))) {
      ik += 1
      dk(ik - 1) = d1(0)
    }
    /* d2[0] lower limit case 1 */
    if ((tr12 && tr13 && tr32) || (tr21 && (tr13 || tr32))) {
      ik += 1;
      dk(ik - 1) = d2(0)
    }
    /* d1[1] upper limit case 2 */
    if ((tr34 && tr32 && tr24) || (tr43 && (tr32 || tr24))) {
      ik += 1;
      dk(ik - 1) = d1(1)
    }
    /* d2[1] upper limit case 2 */
    if ((tr12 && tr14 && tr42) || (tr21 && (tr14 || tr42))) {
      ik += 1;
      dk(ik - 1) = d2(1)
    }
    di = new Array[Double](1)
    di(0) = 0.
    ik match {
      case 2 =>{
        di = new Array[Double](2)
        di(0) = dk(0) - epsilon
        di(1) = dk(1) + epsilon
      }
      case 4 =>{
        di = new Array[Double](4)
        di(0) = dk(0) - epsilon
        di(1) = dk(3) + epsilon
        di(2) = dk(1) - epsilon
        di(3) = dk(2) + epsilon
      }
      case _ =>{
        //TODO should default case be here?
      }
    }
    di
  }

    /**
   * simulates behaviour of fortran90 MODULO function
   * @param a  double
   * @param b  double
   * @return double MODULO
   */
  def MODULO(a: Double, b: Double): Double = {
    var res: Double = 0.0
    var k: Long = 0L
    if (a > 0.0) {
      if (b > 0.0) {
        k = (a / b).toLong
        res = a - k * b
        return res
      }
      if (b < 0.0) {
        k = math.rint(a / b).toLong
        res = a - k * b
        return res
      }
    }
    if (a <= 0.0) {
      if (b <= 0.0) {
        k = (a / b).toLong
        res = a - k * b
        return res
      }
      if (b > 0.0) {
        k = math.rint(a / b).toLong
        res = a - k * b
        return res
      }
    }
    res
  }
}