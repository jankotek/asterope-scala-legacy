package org.asterope.geometry

import org.asterope.util._

/**
 * Helper class to create WCS (World Coordinate System) for Skyview
 */
class WCSBuilder {
  final val projections: List[String] = List("Ait", "Arc", "Stg", "Arc", "Toa", "Csc", "Sfl", "Tan", "Xtn", "Car", "Sin", "Hpx", "Zea")

  def build: WCS = {
    val coordinateSystem: CoordinateSystem = CoordinateSystem.J2000
    var proj: Projection = createProjection(projection, refRa, refDe)


    // Find where the requested center is with respect to
    // the fixed center of this projection.
    var coords: Array[Double] = Vector3d.rade2Vector(refRa, refDe).toArray
    if (proj == null || proj.getProjecter == null) throw new IllegalArgumentException("projection was not created")
    if (proj.getProjecter.inverse == null) throw new IllegalArgumentException("projection " + projection + " does not support inverse function")
    coords = proj.getRotater.transform(coords)
    coords = proj.getProjecter.transform(coords)
    if (coords.length != 2) throw new InternalError

    //scaller
    if (width == 0 || height == 0) throw new IllegalArgumentException("Zero width or height")
    if (pixelScale == 0) throw new InternalError("zero pixelScale")
    var xs: Double = xscale * pixelScale
    var ys: Double = yscale * pixelScale
    if (xs == 0 || ys == 0) throw new InternalError
    var scaler: Scaler = new Scaler(0.5 * width + coords(0) / xs, 0.5 * height - coords(1) / ys, -1 / xs, 0, 0, 1 / ys)

    //add rotation
    val rScaler: Scaler = new Scaler(0, 0, math.cos(rotation), math.sin(rotation), -math.sin(rotation), math.cos(rotation))
    scaler = rScaler.add(scaler)
    return new WCS(coordinateSystem, proj, null,scaler)
  }

  def setPixelScaleFromFOV(fov: Angle): Unit = {
    pixelScale = fov.toRadian / math.sqrt(width * width + height * height)
  }

  /**
   * construct projection with rotation to reference point,
   * ref. point center of view
   */
  protected[geometry] def createProjection(projName: String, ra: Double, de: Double): Projection = {
    //is fixed projection? is used without rotation to reference point
   if (Projection.fixedPoint(projName) != null) {
      val proj: Projection = new Projection(projName)
      proj.setReference(ra, de)
      return proj
    } else {
     //not fixed projection, needs rotation to reference point
      return new Projection(projName, Array[Double](ra, de))
    }
  }

  /**
   * Reference RA and De, all points are rotated to this position
   */
  var refRa: Double = .0
  var refDe: Double = .0
  /**
   * Canvas width and height in Pixels
   */
  var width: Double = 800
  var height: Double = 600
  /**
   * Projection to be used
   */
  var projection: String = "Sin"
  /**
   * xscale 
   */
  var xscale: Double = 1d
  /**
   * yscale, is -1 because pixels are numbered opposite way on computer screen 
   */
  var yscale: Double = -1d
  /**
   * Angular size of pixel 
   */
  var pixelScale: Double = .0
  /**anticlockwise rotation*/
  var rotation: Double = 0
}