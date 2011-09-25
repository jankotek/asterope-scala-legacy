package skyview.sampler

import org.asterope.geometry.Transformer
import skyview.Utilities
import skyview.survey.Image

/**The Sampler class is the superset of classes
 *  that extract data at a given point given the data at other
 *  points.  Note that unlike the standard literature
 *  on sampling we deal with images whose boundaries are
 *  at integers, i.e., the first pixel includes the values 0 to 1.
 *  Thus when we resample it, we treat the value as having been
 *  measured at the mean value of the pixel, i.e., at 0.5.  For
 *  many samplers this means that we subtract 0.5 from the coordinates
 *  before we being sampling. This is in addition to the 0.5 offset
 *  between the pixel coordinates used here and the pixels defined in
 *  FITS files.  I.e., the center of the first pixel is 1 in FITS coordinates,
 *  0.5 in the coordinates used in most of the SkyView Java routines, and is
 *  most conveniently treated as 0 in resampling routines.
 *  <p>
 *  Original Skyview from nasa had about 6 resamplers in many wariants.
 *  Asterope provides only two: Nearest Neighbour and Lanczos.
 *  One is very fast, second is more sophisticated and has better results.
 *  <p>
 *  All fields in this class are given protected access to allow direct
 *  use in sub-classes since this class often determines the
 *  total throughput of an operation.
 */
abstract class Sampler extends skyview.Component {
  /**Find the value in the input data to put in the output data.
   *  The output array defined in a previous setOutput call
   *  is updated.
   * @param index     The index into the output array.
   * @param tmpOut    temporary array used to transform coordinates
   */
  def sample(index: Int): Unit

  /**Set the input image for the sampling
   */
  def setInput(inImage: Image): Unit = {
    this.inImage = inImage
    this.inWidth = inImage.getWidth
    this.inHeight = inImage.getHeight
    this.inDepth = inImage.getDepth
  }

  /**Set the bounds of the output image that may be asked for. */
  def setBounds(bounds: Array[Int]): Unit = {
    this.bounds = bounds
  }

  /**Set the output image for the sampling
   */
  def setOutput(outImage: Image): Unit = {
    this.outImage = outImage
    this.outWidth = outImage.getWidth
    this.outHeight = outImage.getHeight
    this.outDepth = outImage.getDepth
  }

  /**Set the transformation information.
   * @param transform  The transformer object.
   */
  def setTransform(transform: Transformer): Unit = {
    this.trans = transform
  }

  /**The input image.  It should have a size inHeight*inWidth*inDepth */
  protected var inImage: Image = null
  protected var inHeight: Int = 0
  protected var inWidth: Int = 0
  protected var inDepth: Int = 0
  /**This gives the minX,maxX, minY,maxY pixel values for the current
   *  output image.  We can use this to limit the region of the input
   *  image we are interested in.
   */
  protected var bounds: Array[Int] = null
  /**The output image.  It should have a size outHeight*outWidth*outDepth */
  protected var outImage: Image = null
  protected var outHeight: Int = 0
  protected var outWidth: Int = 0
  protected var outDepth: Int = 0
  /**The transformation from the output image to the input image. */
  protected var trans: Transformer = null
}

