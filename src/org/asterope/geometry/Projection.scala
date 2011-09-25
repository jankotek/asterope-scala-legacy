package org.asterope.geometry


/**This class implements projection algorithms to/from a projection
 * plane and the unit sphere.  Data on the unit sphere is normally
 * represented as a unit-three vector.  Data in a projection
 * plane is normally represented as a two-ple.  Note that the projection
 * is usually broken into two pieces: a transformation to a convenient
 * location on the celestial sphere (e.g., for a TAN projection,
 * the unit vectors are rotated so that the reference pixel is at the pole),
 * and a functional transformation from the sphere to the plane.
 * The project and deproject functions address
 * this later element, while the rotation needed is encoded in
 * in the eulerRotationMatrix.
 */


class Projection {

  def setReference(lon: Double, lat: Double): Unit = {
    if (lon != refProj(0) || lat != refProj(1)) {
      val r1 = Rotater("ZY", -refProj(0), refProj(1), 0)
      val r2 = Rotater("ZY", -lon, lat, 0)
      setRotater(r1.add(r2.inverse))
      refProj(0) = lon
      refProj(1) = lat
    }
  }

  /**Get the rotation that needs to be performed before the rotation. */
  def getRotater: Rotater = {
    return rotation
  }

  /**Update the Rotater...*/
  def setRotater(rot: Rotater): Unit = {
    rotation = rot
  }

  /**Get the projection algorithm associated with this rotation. */
  def getProjecter: Projecter = {
    return proj
  }

  /**Get any distortion in the plane associated with this projection. */
  def getDistorter: Distorter = {
    return dist
  }

  protected def setDistorter(dist: Distorter){
    this.dist = dist
  }

  protected def specialReference: Array[Double] = {
    return null
  }

  /**Get the correct projection */
  def this(typ : String) {
    this ()
    this.refProj = Projection.fixedPoint(typ)
    if (this.refProj == null) {
      throw new IllegalArgumentException("Invalid non-parametrized projection:" + typ)
    }
    fixedProjection = true
    this.proj = Projecter(typ)
    if (this.proj == null) {
      throw new IllegalArgumentException("Error creating non-parametrized projection:" + typ)
    }
    this.rotation = null
  }

  /**Is this a fixed point projection? */
  def isFixedProjection: Boolean = {
    return fixedProjection
  }

  /**Get the current reference position */
  def getReferencePoint: Array[Double] = {
    return refProj
  }

  /**Create the specified projection.
   * @param type	   The three character string defining
   *                     the projection.
   * @param reference  The reference point for the projection (as a coordinate pair)
   *
   * @throws ProjectionException when the requested projection
   *          cannot be found or does not have an appropriate constructor.
   */
  def this(typ : String, reference: Array[Double]) {
    this ()
    this.proj = Projecter(typ)
    if (this.proj == null) {
      throw new IllegalArgumentException("Cannot create parametrized projection:" + typ + "\n")
    }
    // We need to rotate the reference pixel to the pole.
    rotation = Rotater("ZYZ", reference(0), -reference(1) + math.Pi / 2, math.Pi / 2)
    if (specialReference != null) {
      var spec: Array[Double] = specialReference
      rotation = rotation.add(Rotater("ZYZ", spec(0), spec(1), spec(2)))
    }
  }

  /**Unit vector for standard reference point for this
   * projection.  I.e., we need to rotate to this
   * point to use the projection algorithms.
   * For most azimuthal projections this is the North pole,
   * but it can be the coordinate origin for other projections
   * and can in principl be anything...
   */
  private var refProj: Array[Double] = Array[Double](0, math.Pi / 2)
  private var fixedProjection: Boolean = false
  private var rotation: Rotater = null
  private var proj: Projecter = null
  private var dist: Distorter = null

}

object Projection{
    private val fixedPoints: Map[String, Array[Double]] =Map(
    "Car" -> Array(0D,0D),
    "Ait" -> Array(0D,0D),
    "Csc" -> Array(0D,0D),
    "Sfl" -> Array(0D,0D),
    "Toa" -> Array(0D,90D),
    "Hpx" -> Array(0D,135D),
    "Tea" -> Array(0D,90D)
  )


  /**This static method returns the location of the
 *  default projection center for fixed point projections.
 *  It returns a null if the projection is not
 *  normally used as a fixed point projection where
 *  the projection is expanded around some fixed point
 *  on the sphere regardless of the location of
 *  the image data.
 * @param proj  The three letter string denoting the projection.
 * @return The fixed point for the projection or null
 *          if not a fixed point projection.
 */
def fixedPoint(proj: String): Array[Double] = {
  if(fixedPoints.contains(proj))
     fixedPoints(proj)
  else
    null
}

}