package org.asterope.geometry


/**
 * This class implements the
 *  <a href="http://www.astron.nl/aips++/docs/memos/107/node10.html">Zenithal Equidistant (Arc)</a>
 *  projection.  This projection has equidistant
 *  parallels of latitude.
 *  Note that the tangent point
 *  is assumed to be at the north pole.
 *  This class assumes preallocated arrays for
 *  maximum efficiency.
 */
object ProjecterArc extends Projecter {

  def getName = "Arc"

  def getDescription = "Zenithal Equidistant (ARC) projecter"


  def inverse: Deprojecter = Deproj

  def isInverse(t: Transformer) =  t == Deproj


  def transform(sphere: Array[Double], plane: Array[Double]): Unit = {
    if (java.lang.Double.isNaN(sphere(2))) {
      plane(0) = Double.NaN
      plane(1) = Double.NaN
    } else {
      var denom: Double = sphere(0) * sphere(0) + sphere(1) * sphere(1)
      if (denom == 0) {
        plane(0) = 0
        plane(1) = 0
      }else {
        val ratio: Double = (math.Pi / 2 - math.asin(sphere(2))) / math.sqrt(denom)
        plane(0) = ratio * sphere(0)
        plane(1) = ratio * sphere(1)
      }
    }
  }

  override def validPosition(plane: Array[Double]): Boolean = {
    return super.validPosition(plane) && (plane(0) * plane(0) + plane(1) * plane(1)) <= math.Pi * math.Pi
  }

  protected object Deproj extends Deprojecter {

    def getName = "ArcDeproj"

    def getDescription = "Zenithal equal area (ARC) deprojecter"

    def inverse = ProjecterArc

    def isInverse(t: Transformer) = t == ProjecterArc

    def transform(plane: Array[Double], sphere: Array[Double]) {
      val r: Double = math.sqrt(plane(0) * plane(0) + plane(1) * plane(1))
      if (!validPosition(plane)) {
        sphere(0) = Double.NaN
        sphere(1) = Double.NaN
        sphere(2) = Double.NaN
      } else {
        sphere(2) = math.cos(r)
        val pr_sinde: Double = sphere(2)
        val pr_sq_cosde: Double = 1 - pr_sinde * pr_sinde
        var ratio: Double = 0
        if (r > 0) {
          ratio = math.sqrt(pr_sq_cosde) / r
        }
        sphere(0) = ratio * plane(0)
        sphere(1) = ratio * plane(1)
      }
    }
  }

}

