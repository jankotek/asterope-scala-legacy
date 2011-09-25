package org.asterope.geometry


import java.lang.Double.NaN
import java.lang.Math._
import scala.math

/**This class implements the Cartesian (rectangular)
 *  projection.
 *  The Cartesian projection transforms coordinates directly into an RA/Dec plane.  This projection is normally centered on the coordinate origin and has extreme distortions near the pole.  It is often used for all-sky maps but can be used for limited regions so long as they are not near the pole.
 *  <p>
 *  Note that the tangent point
 *  is assumed to be at the north pole.
 *  This class assumes preallocated arrays for
 *  maximum efficiency.
 */
object ProjecterCar extends Projecter {

  def getName = "Car"


  def getDescription =  "Transform from the celestial sphere to the plane described by Lon/Lat directly"

  def inverse: Deprojecter = CarDeproj
  def isInverse(t: Transformer) = t == CarDeproj

  override val getXTiling: Double = 2 * math.Pi
  override val getYTiling: Double = 2 * math.Pi


  override def shadowPoint(x: Double, y: Double): Array[Double] = {
    if (x > 0) {
      return Array[Double](x - 2 * math.Pi, y)
    }
    else if (x < 0) {
      return Array[Double](x + 2 * math.Pi, y)
    }
    else {
      return Array[Double](2 * math.Pi, y)
    }
  }

  def transform(sphere: Array[Double], plane: Array[Double]){
    if (java.lang.Double.isNaN(sphere(2))) {
      plane(0) = NaN
      plane(1) = NaN
    } else {
      plane(0) = atan2(sphere(1), sphere(0))
      plane(1) = asin(sphere(2))
    }
  }


  override def allValid: Boolean = true


  protected object CarDeproj extends Deprojecter {

    def getName = "CarDeproj"

    def getDescription = "Transform from the Lat/Lon to the corresponding unit vector."

    def isInverse(t: Transformer) = t == ProjecterCar

    def inverse = ProjecterCar

    val getXTiling: Double = 2 * math.Pi
    val getYTiling: Double = 2 * math.Pi

    def transform(plane: Array[Double], sphere: Array[Double]){
      if (java.lang.Double.isNaN(plane(0))) {
        sphere(0) = NaN
        sphere(1) = NaN
        sphere(2) = NaN
      }
      else {
        val sr: Double = sin(plane(0))
        val cr: Double = cos(plane(0))
        val sd: Double = sin(plane(1))
        val cd: Double = cos(plane(1))
        sphere(0) = cr * cd
        sphere(1) = sr * cd
        sphere(2) = sd
      }
    }
  }

}

