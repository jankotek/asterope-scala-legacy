package skyview.survey

import org.asterope.geometry.WCS

/**This class defines a proxy image.  The proxy has an
 *  approximate WCS which may be used to determine where the
 *  image is to used for sampling, but it must be replaced by
 *  the real image before the sampling is done.
 *  of a set of pixel values and a WCS describing the
 *  pixel coordinates.
 */
class ProxyImage(spell: String, wcs: WCS, width: Int, height: Int, depth: Int, fac: ImageFactory)
  extends Image {

  protected var currentImage = new Image(null, wcs, width, height, depth)


  override def getName = currentImage.getName

  override def setName(name: String) = currentImage.setName(name)

  override def getWCS =  currentImage.getWCS
  override def getData(npix: Int) =  currentImage.getData(npix)

  override def getDataArray: Array[Double] = currentImage.getDataArray
  override def setData(npix: Int, newData: Double) = currentImage.setData(npix, newData)

  override def clearData = currentImage.clearData


  override def setDataArray(newData: Array[Double]) = currentImage.setDataArray(newData)
  override def getTransformer =  currentImage.getTransformer

  override def getWidth =  currentImage.getWidth
  override def getHeight =  currentImage.getHeight
  override def getDepth =  currentImage.getDepth


  override def getCenter(npix: Int) =  currentImage.getCenter(npix)

  override def getCorners(npix: Int): Array[Array[Double]] = currentImage.getCorners(npix)


  /**Make sure the image is read for detailed use.
   * Replace the proxy with the real image */
  override def validate: Unit = {
    if (realImage == null) {
      realImage = fac.factory(spell)
    }
    currentImage = realImage
  }


  override def valid: Boolean = {
    return realImage != null
  }

  /**Get the current 'real' image.
   */
  override def getBaseImage =  currentImage


  /**The image that we are proxying for.
   *  If this is not null, we simple forward requests to this.
   */
  private var realImage: Image = null
}

	
