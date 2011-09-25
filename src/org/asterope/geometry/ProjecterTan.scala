package org.asterope.geometry


/**
 *  This class implements the <a href="http://en.wikipedia.org/wiki/Gnomonic_projection">
 *  tangent (gnomonic)</a>
 *  projection.
 *  The gnomonic projection can be visualized by placing a plane tangent to the celestial sphere and drawing lines from the center of the sphere, through the point to be projected and extending the line till it intersects the plane.  The point of tangency is the reference point of the projection.  Normally this is at (or near) the center of the image.  The gnomonic projection is probably the most common projection used for small astronomical images.  It can only represent half the sky and has significant distortions for fields larger than a few degrees.  The circle 90 degrees from the tangent point projects to infinity in the projection plane.   Great circles in the celestial sphere (e.g., lines of constant right ascension) transform to straight lines in the projection plane.
 *  <p>
 *  Note that the tangent point
 *  is assumed to be at the north pole.
 *  This class assumes preallocated arrays for
 *  maximum efficiency.
 */

object ProjecterTan extends Projecter {

  def getName = "Tan"
  def getDescription = "Project to a tangent plane touching the sphere"

  override def allValid = true

  final def transform(sphere: Array[Double], plane: Array[Double]) {
    if (java.lang.Double.isNaN(sphere(2)) || sphere(2) < 0) {
      plane(0) = Double.NaN
      plane(1) = Double.NaN
    } else {
      val fac: Double = 1 / sphere(2)
      plane(0) = fac * sphere(0)
      plane(1) = fac * sphere(1)
    }
  }

  def inverse: Deprojecter = TanDeproj
  def isInverse(t: Transformer) = t == TanDeproj

  protected object TanDeproj extends Deprojecter {

    def getName = "TanDeproj"
    def getDescription = "Transform from the tangent plane to the sphere"


    def inverse: Projecter = ProjecterTan

    def isInverse(t: Transformer) = t == ProjecterTan

    def transform(plane: Array[Double], sphere: Array[Double]){
      if (java.lang.Double.isNaN(plane(0))) {
        sphere(0) = Double.NaN
        sphere(1) = Double.NaN
        sphere(2) = Double.NaN
      }else {
        val factor: Double = 1 / math.sqrt(plane(0) * plane(0) + plane(1) * plane(1) + 1)
        sphere(0) = factor * plane(0)
        sphere(1) = factor * plane(1)
        sphere(2) = factor
      }
    }
  }

}

