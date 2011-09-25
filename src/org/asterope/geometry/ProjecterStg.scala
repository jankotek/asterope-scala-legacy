package org.asterope.geometry


/**
 * This class implements the <a href="http://mathworld.wolfram.com/StereographicProjection.html">
 *  stereographic (STG) </a>  projection.
 *  This projection is similar to the Tan projection except that instead of drawing
 *  lines from the center of the sphere, we draw lines from the point opposite
 *  the tangent plane.  The entire sky projects to the full plane.
 *  Circles on the sphere project to circles in the projection plane.
 *  However the center of the circle on the sphere will not project to the
 *  center of the corresponding circle in the plane.
 *  <p>
 *  Note that the tangent point
 *  is assumed to be at the north pole.
 *  The STG projection projects circles into circles.
 */

object ProjecterStg extends Projecter {

  def getName = "Stg"
  def getDescription =  "Stereographic projection. Project from antipodes to a tanget plane touching the sphere"

  override def allValid = true

  def inverse = StgDeproj
  def isInverse(t: Transformer) = t ==StgDeproj

  def transform(sphere: Array[Double], plane: Array[Double]){
    if (java.lang.Double.isNaN(sphere(2)) || sphere(2) < 0) {
      plane(0) = Double.NaN
      plane(1) = Double.NaN
    } else {
      val fac: Double = 2 / (1 + sphere(2))
      plane(0) = fac * sphere(0)
      plane(1) = fac * sphere(1)
    }
  }


  protected object StgDeproj extends Deprojecter {

    def getName =  "StgDeproj"
    def getDescription = "Transform from the stereographic tangent plane to the sphere"

    def inverse = ProjecterStg
    def isInverse(t: Transformer) = t == ProjecterStg

    def transform(plane: Array[Double], sphere: Array[Double]){
      if (!validPosition(plane)) {
        sphere(0) = Double.NaN
        sphere(1) = Double.NaN
        sphere(2) = Double.NaN
      } else {
        val x: Double = plane(0)
        val y: Double = plane(1)
        val r: Double = math.sqrt(x * x + y * y)
        val theta: Double = 2 * math.atan2(r, 2)
        val z: Double = math.cos(theta)
        sphere(2) = z
        if (math.abs(z) != 1) {
          sphere(0) = plane(0) * (1 + z) / 2
          sphere(1) = plane(1) * (1 + z) / 2
        }
        else {
          sphere(0) = 0
          sphere(1) = 0
        }
      }
    }
  }

}

