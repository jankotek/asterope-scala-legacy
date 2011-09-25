package org.asterope.geometry

/**
 *  This class implements the <a href="http://en.wikipedia.org/wiki/Orthographic_projection_(cartography)">
 *  Sine (Orthographic)</a>
 *  projection.
 *  The sine projection is commonly used for small scale images in astronomy especially for images in the radio.   This projection can be visualized in a fashion similar to the gnomonic projection with a plane tangent to the celestial sphere.  However in this case the line is drawn from the plane to the point on the sphere perpendicular to the plane.  As with the gnomonic projection only half the sphere can be represented in the projection plane, but the projection is finite.  A full sine projection looks like the sphere seen from a distance.  The NCP projection is a special case of the Sine projection where tangent point is fixed at the pole.
 *  <p>
 *  Note that the tangent point
 *  is assumed to be at the north pole.
 *  This class assumes preallocated arrays for
 *  maximum efficiency.
 */

object ProjecterSin extends Projecter {

  def getName = "Sin"

  def getDescription = "Project as if seeing the sphere from a great distance"

  def inverse: Deprojecter =  SinDeproj

  def isInverse(t: Transformer) = t == SinDeproj

  def transform(sphere: Array[Double], plane: Array[Double]) {
    if (java.lang.Double.isNaN(sphere(2)) || sphere(2) <= 0) {
      plane(0) = Double.NaN
      plane(1) = Double.NaN
    } else {
      plane(0) = sphere(0)
      plane(1) = sphere(1)
    }
  }

  override def validPosition(plane: Array[Double]): Boolean = {
    return super.validPosition(plane) && (plane(0) * plane(0) + plane(1) * plane(1) <= 1)
  }

  protected object SinDeproj extends Deprojecter {

    def getName = "SinDeproj"
    def getDescription = "Invert the sine projection"


    def inverse: Projecter = ProjecterSin

    def isInverse(t: Transformer) = t == ProjecterSin

    def transform(plane: Array[Double], sphere: Array[Double]){
      if (!validPosition(plane)) {
        sphere(0) = Double.NaN
        sphere(1) = Double.NaN
        sphere(2) = Double.NaN
      } else {
        sphere(0) = plane(0)
        sphere(1) = plane(1)
        sphere(2) = math.sqrt(1 - plane(0) * plane(0) - plane(1) * plane(1))
      }
    }

  }

}

