package org.asterope.geometry


import java.lang.Double.NaN
import java.lang.Math._

/**
 * This class implements the <a href="http://en.wikipedia.org/wiki/Sinusoidal_projection">
 *   Sanson-Flamsteed (Sinusoidal)</a>
 *  projection. This is an all-sky projection similar to the Car projection, but where the horizontal extent at each latitude is reduced by the cosine of the latitude, resulting in an equal-area projection.  The suffix GLS is also recognized in FITS files for this projection.  This projection is normally used in all sky projections.
 */
object ProjecterSfl extends Projecter {

  def getName = "Sfl"

  def getDescription = "Transform from the celestial sphere to the sinusoidal projection"


  def inverse: Deprojecter = SflDeproj

  def isInverse(t: Transformer) =  t == SflDeproj

  def transform(sphere: Array[Double], plane: Array[Double]){
    if (java.lang.Double.isNaN(sphere(2))) {
      plane(0) = NaN
      plane(1) = NaN
    }
    else {
      plane(1) = atan2(sphere(2), sqrt(sphere(0) * sphere(0) + sphere(1) * sphere(1)))
      plane(0) = atan2(sphere(1), sphere(0)) * math.cos(plane(1))
    }
  }

  override def validPosition(plane: Array[Double]): Boolean = {
    return super.validPosition(plane) && abs(plane(1)) <= PI / 2 && abs(plane(0)) <= PI * cos(plane(1))
  }

  protected object SflDeproj extends Deprojecter {

    def getName = "SflDeproj"

    def getDescription =  "Transform from a sinusoidal projection to the corresponding unit vector."

    def isInverse(t: Transformer) = t == ProjecterSfl

    def inverse = ProjecterSfl

    def transform(plane: Array[Double], sphere: Array[Double]){
      if (!validPosition(plane)) {
        sphere(0) = NaN
        sphere(1) = NaN
        sphere(2) = NaN
      } else {
        val dec: Double = plane(1)
        val sd: Double = sin(dec)
        val cd: Double = cos(dec)
        if (abs(dec) <= PI / 2 && abs(plane(0)) <= PI * cd) {
          var ra: Double = plane(0)
          if (cd > 0) {
            ra /= cd
          }
          val sr: Double = sin(ra)
          val cr: Double = cos(ra)
          sphere(0) = cr * cd
          sphere(1) = sr * cd
          sphere(2) = sd
        }
      }
    }
  }

}

