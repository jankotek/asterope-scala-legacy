package org.asterope.geometry

/**The Transformer class is the superclass
 *  for a variety of classes that transform positions
 *  represented in one frame to another.  The subclasses
 *  of Transformer include:
 * <ul>
 *   <li> Projecter 3/2:       Transform celestial coordinates to a projection plane.
 *   <li> Deprojecter 2/3:     Transform coordinates in a projection plane to the celestial sphere.
 *   <li> Rotater 3/3:         Rotate coordinates in the celestial sphere
 *   <li> SphereDistorter 3/3: Non-linear transformations in the celestial sphere.
 *   <li> Distorter 2/2:       Non-linear transformations in the projection plane
 *   <li> Scaler 2/2:          Affine transformations in the projection plane
 *   <li> Converter:           Apply a series of conversions in turn.
 * The numbers after the type indicate the dimensionality of the input/output.
 */
abstract class Transformer{
  /**Get the dimensionality of the output vectors.
   */
  def getOutputDimension: Int

  /**Get the dimensionality of the input vectors.
   */
  def getInputDimension: Int

  /**Convert a single point.  This method creates a new
   *  object and is not recommended when high throughput is needed.
   * @param in  An array giving the input vector.
   * @return An array giving the transformed vector.
   *          For projections and deprojections this will have
   *          a different dimension
   */
  def transform(in: Array[Double]): Array[Double] = {
    val odim: Int = getOutputDimension
    if (odim == 0) {
      return in
    }else {
      val out = new Array[Double](odim)
      transform(in, out)
      return out
    }
  }


  /**Get the inverse of the transformation. If the order
   *  matters, then the inverse is to be applied after the original
   *  transformation.  This is primarily an issue with Converters.
   */
  def inverse: Transformer

  /**Convert a single point where the output vector is supplied.
   * @param in   The input vector.
   * @param out  The output vector, it may be the same as the input
   *              vector if the dimensionalities are the same.  All
   *              transformers are expected to work with aliased inputs and output.
   */
  def transform(in: Array[Double], out: Array[Double]): Unit

  /**Are these two transformations, inverses of each other?  This
   * method is used to optimize a series of transformations where
   * transformations.
   */
  def isInverse(trans: Transformer): Boolean

  /**Convert an array of points where the output vectors are supplied.
   *  The vectors should have dimensionality [2][n] or [3][n].  The first
   *  dimension gives the index within the vector while the second gives
   *  which vector is being processed.  This means that the user needs
   *  to create only a few objects (3 or 4) rather than of order n objects
   *  for each array.  In practice this seems to speed up code by a factor
   *  of 4. (JDK1.5).
   * @param in A set of positions to be transformed.  The first dimension should
   *         be consistent with  getInputDimension, while the second is the number of
   *         points to be transferred.
   * @param out The updated positions.  The first dimension should be consistent with
   *         getOutputDimension, while the second is the number of points to be transferred.
   *         This argument may point to the same data as the input.
   */
  def transform(in: Array[Array[Double]], out: Array[Array[Double]]): Unit = {
    if (in == null || out == null || in.length == 0 || out.length == 0 || in(0).length != out(0).length) {
      throw new IllegalArgumentException("Array mismatch on vector transformation")
    }
    var xin: Array[Double] = null
    var xout: Array[Double] = null
    var idim = getInputDimension
    var odim = getOutputDimension
    if (idim == 0 && odim == 0) {
      // Identity transformations, e.g., converters that have no elements.
      var i = 0
      while (i < in.length) {
        System.arraycopy(in(i), 0, out(i), 0, in(i).length)

        i += 1
      }
      return
    }
    if (idim == 2) {
      xin = new Array[Double](2)
    }else {
      xin = new Array[Double](3)
    }
    if (odim == 2) {
      xout = new Array[Double](2)
    }else {
      xout = new Array[Double](3)
    }

    var i = 0
    while (i < in(0).length) {
      // The copying into/from the temporary array is the price we pay
      // for defining the vectors as in[2/3][n] rather than in[n][2/3].
      // It is possible new compilers or particular user circumstances
      // may make this a poor choice, but currently (12/04: JDK 1.5) it saves
      // about a factor of 4 in total program throughput.

      var j = 0
      while (j < idim) {
        xin(j) = in(j)(i)
        j += 1
      }
      transform(xin, xout)

      j = 0
      while (j < odim) {
        out(j)(i) = xout(j)
        j += 1
      }
      i += 1
    }

  }
}

	
