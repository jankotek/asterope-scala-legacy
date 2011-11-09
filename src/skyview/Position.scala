package skyview

import org.asterope.geometry._
import org.asterope.util._
import org.apache.commons.math.geometry.Vector3D

/**This class represents a position in the sky.  This
 *  class is used to pass a position that may be represented in different
 * frames in different parts of a program.  However since it creates
 * CoordinateSystem objects for each transformation it should not be
 * used to do coordinate transformations for large arrays of positions.
 */
class Position(l: Double, b: Double, frame: String = "J2000") {

  /**The J2000 coordinates */
  private var coords: Array[Double] = new Array[Double](2)
  /**The original system coordinates */
  private var orig: Array[Double] = null
  /**The original coordinate system */
  private var origFrame: String = null


    orig = new Array[Double](2)
    orig(0) = l
    orig(1) = b
    origFrame = frame
    if (frame == null || frame.toUpperCase.equals("J2000")) {
      coords(0) = l
      coords(1) = b
    }
    else {
      val csys: CoordinateSystem = CoordinateSystem.factory(frame)
      val unit: Array[Double] = rade2Vector(math.toRadians(l), math.toRadians(b)).toArray
            
      val conv = new Converter(List(csys.getSphereDistorter,csys.getRotater )).inverse
      val j2000Unit: Array[Double] = conv.transform(unit)
      val j2000C: Array[Double] = new Vector3D(j2000Unit(0),j2000Unit(1),j2000Unit(2)).toRaDeArray
      coords(0) = math.toDegrees(j2000C(0))
      coords(1) = math.toDegrees(j2000C(1))
    }


  /**Get the coordinates in the standard (J2000) frame.
   * Used to be called getPosition.
   */
  def getCoordinates: Array[Double] = {
    return getCoordinates("J2000")
  }

  /**Get the coordinates in a specified frame.
   * Used to be called getPosition.
   */
  def getCoordinates(frame: String): Array[Double] = {
    if (frame == null || frame.toUpperCase.equals("J2000")) {
      return coords
    }
    else {
      if (frame.equals(origFrame)) {
        return orig.clone
      }
      val csys: CoordinateSystem = CoordinateSystem.factory(frame)
      val unit: Array[Double] = rade2Vector(math.toRadians(coords(0)), math.toRadians(coords(1))).toArray
      

      val conv: Converter = new Converter(List(csys.getSphereDistorter,csys.getRotater ))
      val xUnit: Array[Double] = conv.transform(unit)
      val xCoords: Array[Double] = new Vector3D(xUnit(0),xUnit(1),xUnit(2)).toRaDeArray
      xCoords(0) = math.toDegrees(xCoords(0))
      xCoords(1) = math.toDegrees(xCoords(1))
      if (xCoords(0) < 0) {
        xCoords(0) += 360
      }
      else if (xCoords(0) >= 360) {
        xCoords(0) -= 360
      }
      return xCoords
    }
  }

}

