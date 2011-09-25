package org.asterope.geometry

/** Projects a point from the celestial sphere
 *  to a projection plane.
 */
abstract class Projecter extends Transformer {
  
  def getName:String

  def inverse: Transformer

  //check that inverse is supported
  assert(
      inverse.getOutputDimension == 3 && inverse.getInputDimension == 2,
      "Projecter inverse wrong dimension: "+getName)

  def getOutputDimension = 2

  def getInputDimension = 3

  /**Some projections can tile the projection plane with repeated
   *  copies.  This method gives the vectors along which the tiles repeat.
   *  Note that for azimuthal like (e.g., Mercator) projections, the tiling may be possible in
   *  the longitudinal direction, but not in the latitudinal direction.
   *  Some projections (e.g., CAR) can tile in both directions (but note that the periodicity
   *  for tiling is 360 degrees in both directions in the CAR projection since the latitudes
   *  will run ... -90 -80 ...0 ... 80 90 80 ... 0 ... -80 -90 -80 ....
   *  It's also conceivable that the projection tiling is along but we do not accomodate
   *  this.
   * @return The period in X in radians.  A value of 0 means that there is not periodicity.
   *
   */
  def getXTiling: Double =  0

  /**The tiling period in Y
   * @return The tiling period in radians.  A value of 0 means that
   *  there is no period.
   */
  def getYTiling: Double = 0

  /**Is this a valid position in the projection plane for this image. This
   *  default is appropriate for all projections where the projection plane is infinite.
   */
  def validPosition(pos: Array[Double]): Boolean = {
    return pos != null && !(java.lang.Double.isNaN(pos(0)))
  }

  /**Are all points in the projection plane valid?
   */
  def allValid: Boolean  = false

  /**Return a shadowpoint for the input location.
   *  Shadowpoints are not defined for all projections.
   */
  def shadowPoint(x: Double, y: Double): Array[Double] = {
    throw new UnsupportedOperationException("No shadow points in requested projection")
  }

}

object Projecter{
  val projecters = Map(
    "Ait" -> ProjecterAit,
    "Arc" -> ProjecterArc,
    "Car" -> ProjecterCar,
    "Sfl" -> ProjecterSfl,
    "Sin" -> ProjecterSin,
    "Stg" -> ProjecterStg,
    "Tan" -> ProjecterTan,
    "Zea" -> ProjecterZea

  )
  def apply(name:String):Projecter = projecters(name)

}
