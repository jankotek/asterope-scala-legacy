package org.asterope.geometry



/**
 * This class implements the <a href="http://www.astron.nl/aips++/docs/memos/107/node11.html">
 * Zenithal Equal Area (ZEA) </a>
 *  projection.
 *  This equal area projection transforms rings around a given reference point into rings in the projection varying the width of the rings in the projection plane to conserve area.  The point opposite the reference point is transformed into a bounding circle in the projection plane.  This equal area projection allows the user to select the region of minimum distortion unlike the Aitoff projection.
 *  <p>
 *  Note that the tangent point
 *  is assumed to be at the north pole.
 *  This class assumes preallocated arrays for
 *  maximum efficiency.
 */
object ProjecterZea extends Projecter {

  def getName = "Zea"
  def getDescription = "Zenithal Equal Area projecter"

  def inverse = ZeaDeproj
  def isInverse(t: Transformer) = t == ZeaDeproj

  def transform(sphere: Array[Double], plane: Array[Double]){
    if (java.lang.Double.isNaN(sphere(2))) {
      plane(0) = Double.NaN
      plane(1) = Double.NaN
    }else {
      var num: Double = 2 * (1 - sphere(2))
      if (num < 0) {
        num = 0
      }
      var denom: Double = sphere(0) * sphere(0) + sphere(1) * sphere(1)
      if (denom == 0) {
        plane(0) = 0
        plane(1) = 0
      } else {
        val ratio: Double = math.sqrt(num) / math.sqrt(sphere(0) * sphere(0) + sphere(1) * sphere(1))
        plane(0) = ratio * sphere(0)
        plane(1) = ratio * sphere(1)
      }
    }
  }

  override def validPosition(plane: Array[Double]): Boolean = {
    return super.validPosition(plane) && plane(0) * plane(0) + plane(1) * plane(1) <= 4
  }

  protected object ZeaDeproj extends Deprojecter {

    def getName = "ZeaDeproj"
    def getDescription = "Zenithal equal area deprojecter"

    def inverse: Projecter = ProjecterZea

    def isInverse(t: Transformer) = t == ProjecterZea

    def transform(plane: Array[Double], sphere: Array[Double]){
      if (!validPosition(plane)) {
        sphere(0) = Double.NaN
        sphere(1) = Double.NaN
        sphere(2) = Double.NaN
      } else {
        val r: Double = math.sqrt(plane(0) * plane(0) + plane(1) * plane(1))
        sphere(2) = 1 - r * r / 2
        var ratio: Double = (1 - sphere(2) * sphere(2))
        if (ratio > 0) {
          ratio = math.sqrt(ratio) / r
        } else {
          ratio = 0
        }
        sphere(0) = ratio * plane(0)
        sphere(1) = ratio * plane(1)
      }
    }
  }

}

