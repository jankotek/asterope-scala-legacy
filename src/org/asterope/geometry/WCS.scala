package org.asterope.geometry

import nom.tam.fits.Header
import org.asterope.chart.Point2d
import org.asterope.util._
import java.awt.geom.Point2D
import java.lang.IllegalArgumentException

/**A World Coordinate System defines
 *  a translation between celestial and pixel
 *  coordinates.  Note that in many cases
 *  FITS keywords describe the transformations
 *  in the other direction (from pixel to celestial)
 *  but we follow the convention that forward transformations
 *  are from celestial to pixel.
 *  Given a WCS object, wcs,  the pixel-celestial coordinates trasnformation
 *  is simply wcs.inverse();
 */
class WCS(csys: CoordinateSystem, proj: Projection, sphereDistorder:Distorter = null, scale: Scaler)
	extends Converter(List(
        csys.getSphereDistorter,csys.getRotater,proj.getRotater,proj.getProjecter,
        proj.getDistorter,sphereDistorder, scale)) {

  /**This includes a nominal 'scale' for the WCS. While this
   *  can often be calculated from the transformation, that may sometimes
   *  be difficult.
   */
  var wcsScale: Double = {
	var p: Array[Double] = scale.getParams
    var det: Double = p(2) * p(5) - p(3) * p(4)
    1 / math.sqrt(math.abs(det))
  }
  /**Which axis is the longitude */
  protected var lonAxis: Int = -1
  /**Which axis is the latitude */
  protected var latAxis: Int = -1



  /**Get the CoordinateSystem used in the WCS */
  def getCoordinateSystem: CoordinateSystem = {
    return csys
  }

  /**Get the projection used in the WCS */
  def getProjection: Projection = {
    return proj
  }

  /**Get the linear scaler used in the projection */
  def getScaler: Scaler = {
    return scale
  }


  /**Get the nominal scale of the WCS.
   */
  def getScale: Double = {
    return wcsScale
  }

  /**Write FITS WCS keywords given key values.  Only relatively simple
   *  WCSs are handled here.  We assume we are dealing with axes 1 and 2.
   * @param h  The header to be updated.
   * @param s  A Scaler giving the transformation between standard projection
   *            coordinates and pixel/device coordinates.
   * @param projString A three character string giving the projection used.
   *            Supported projections are: "Tan", "Sin", "Ait", "Car", "Zea".
   * @param coordString A string giving the coordinate system used.  The first
   *            character gives the general frame.  For most frames the remainder
   *            of the string gives the equinox of the coordinate system.
   *            E.g., J2000, B1950, Galactic, "E2000", "H2020.10375".
   */
  def updateHeader(h: Header, s: Scaler, crval: Array[Double], projString: String, coordString2: String): Unit = {
    if (proj.isFixedProjection) {
      h.addValue("CRVAL1", proj.getReferencePoint(0), "Fixed reference center")
      h.addValue("CRVAL2", proj.getReferencePoint(1), "Fixed reference center")
    }
    else {
      h.addValue("CRVAL1", crval(0), "Reference longitude")
      h.addValue("CRVAL2", crval(1), "Reference latitude")
    }
    val coordString = coordString2.toUpperCase
    var prefixes: Array[String] = new Array[String](2)
    var c: Char = coordString.charAt(0)
    if (c == 'J' || c == 'I') {
      h.addValue("RADESYS", "FK5", "Coordinate system")
      prefixes(0) = "RA--"
      prefixes(1) = "DEC-"
    }
    else if (c == 'B') {
      h.addValue("RADESYS", "FK4", "Coordinate system")
      prefixes(0) = "RA--"
      prefixes(1) = "DEC-"
    }
    else {
      prefixes(0) = c + "LON"
      prefixes(1) = c + "LAT"
    }
    if (c != 'G' && c != 'I') {
      try {
        var equinox: Double = coordString.substring(1).toDouble
        h.addValue("EQUINOX", equinox, "Epoch of the equinox")
      }
      catch {
        case e: Exception => {
          // Couldn't parse out the equinox
        }
      }
    }
    if (c == 'I') {
      h.addValue("EQUINOX", 2000, "ICRS coordinates")
    }
    var upProj: String = projString.toUpperCase
    h.addValue("CTYPE1", prefixes(0) + "-" + upProj, "Coordinates -- projection")
    h.addValue("CTYPE2", prefixes(1) + "-" + upProj, "Coordinates -- projection")





    // Note that the scaler transforms from the standard projection
    // coordinates to the pixel coordinates.
    //     P = P0 + M X  where X is the standard coordinates and P is the
    // pixel coordinates.  So the reference pixels are just the constants
    // in the scaler.
    // Remember that FITS pixels are offset by 0.5 from 0 offset pixels.

    h.addValue("CRPIX1", s.x0 + 0.5, "X reference pixel")
    h.addValue("CRPIX2", s.y0 + 0.5, "Y reference pixel")




    // Remember that the FITS values are of the form
    //    X = M(P-P0)
    // so we'll need to invert the scaler.
    //
    // Do we need a matrix?

    if (math.abs(s.a01) < 1.e-14 && math.abs(s.a10) < 1.e-14) {
      // No cross terms, so we'll just use CDELTs
      h.addValue("CDELT1", math.toDegrees(1 / s.a00), "X scale")
      h.addValue("CDELT2", math.toDegrees(1 / s.a11), "Y scale")
    }
    else {
      // We have cross terms.  It's simplest
      // just to use the CD matrix and not worry about
      // normalization.  First invert the matrix to get
      // the transformation in the direction that FITS uses.
      val rev: Scaler = s.inverse
      h.addValue("CD1_1", math.toDegrees(rev.a00), "Matrix element")
      h.addValue("CD1_2", math.toDegrees(rev.a01), "Matrix element")
      h.addValue("CD2_1", math.toDegrees(rev.a10), "Matrix element")
      h.addValue("CD2_2", math.toDegrees(rev.a11), "Matrix element")
    }
  }





  /**
   * project spherical 3d vector into canvas 2d coordinates
   * @param xyz 3d spherical vector with xyz coordinates
   * @return array with xy coordinates, or None if projection fails
   */
  def project(xyz: Array[Double]):Option[Array[Double]] = {
      if(xyz.length != 3)
        throw new IllegalArgumentException("vector array must have three items")
      val res  = transform(xyz);
      if (java.lang.Double.isNaN(res(0)) || java.lang.Double.isNaN(res(1)))
        None
      else
        Some(res)

  }

  /**
   * project spheric 3d vector to 2d canvas
   * @param vector3d normalized vector on sphere
   * @return point on canvas, or None if point can not be projected
   */
  def project(vector: Vector3d):Option[Point2d] = {
    Vector3d.assertNormalized(vector);
    val p = project(vector.toArray)
    if(p.isDefined) Some(Point2d(p.get(0),p.get(1)));
    else None
  }

  /** project spheric coordinates to 2d canvas
   * @param ra in radians
   * @param  de in radians
   * @return point on canvas, or None if point can not be projected
   */
  def project(ra: Double, de: Double):Option[Point2d] = project(Vector3d.rade2Vector(ra,de));

  def project(ra: Angle, de: Angle):Option[Point2d] = project(Vector3d.rade2Vector(ra,de));



  /**
   * deproject point from canvas to spherical vector
   * @param xy array with canvas xy coordinate
   * @return xyz array with xyz spherical vector coordinate
   */
 def deproject(xy: Array[Double]):Option[Array[Double]]={
      if(xy.length != 2)
        throw new IllegalArgumentException("xy array must have two items")
      val res = inverse.transform(xy);
      if (java.lang.Double.isNaN(res(0)) || java.lang.Double.isNaN(res(1)) || java.lang.Double.isNaN(res(0)))
        None
      else
        Some(res);
 }

 /**
  * deproject point from canvas to spherical vector
  * @param x coordinate on canvas
  * @param y coordinate on canvas
  * @return Spherical 3d vector or None, if deprojection fails
  */
  def deproject(x:Double, y:Double):Option[Vector3d] = {
    val xy = Array[Double](x,y)
    val xyz = deproject(xy);
    if(xyz.isDefined) Some(Vector3d(xyz.get));
    else None;
  }

  def deproject(p:Point2D):Option[Vector3d] = deproject(p.getX, p.getY)


}

