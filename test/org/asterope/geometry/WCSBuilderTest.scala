package org.asterope.geometry


import org.asterope.util.Angle._
import org.asterope.util._
import org.apache.commons.math.geometry.Vector3D


class WCSBuilderTest extends ScalaTestCase {
  def testCreateProjection(){
    testProjection("Sin", D2R * 0, D2R * 0)
    testProjection("Sin", D2R * 90, D2R * 45)
    testProjection("Tan", D2R * 90, D2R * 45)
    testProjection("Car", D2R * 0, D2R * 0)
  }

  protected def testProjection(name: String, ra: Double, de: Double){
    System.out.println(name)
    val wcs: WCSBuilder = new WCSBuilder
    val p: Projection = wcs.createProjection(name, ra, de)
    testCenterPoint(ra, de, p)
  }

  private def testCenterPoint(ra: Double, de: Double, p: Projection){
    var testPoint: Array[Double] = rade2Vector(ra, de).toArray
    if (p.getRotater == null) return
    testPoint = p.getRotater.transform(testPoint)
    testPoint = p.getProjecter.transform(testPoint)
    assert(testPoint.length === 2)
    assert(math.abs(testPoint(0)) ?< 1e-8)
    assert(math.abs(testPoint(1)) ?< 1e-8)
  }

  /**
   * Test that single RA DE point is projected and deprojected to same position
   */
  private def testPoint(name: String, ra: Double, de: Double, p: Projection){
    var testPoint: Array[Double] = rade2Vector(ra, de).toArray
    if (p.getRotater != null) testPoint = p.getRotater.transform(testPoint)
    testPoint = p.getProjecter.transform(testPoint)
    assert(testPoint.length === 2)
    testPoint = p.getProjecter.inverse.transform(testPoint)
    if (p.getRotater != null) testPoint = p.getRotater.inverse.transform(testPoint)
    val testPoint2: Array[Double] = rade2Vector(ra, de).toArray
    assert(testPoint2(0) ~== testPoint(0))
    assert(testPoint2(1) ~== testPoint(1))
  }

  def testBuild() {
    val wcsBuilder: WCSBuilder = new WCSBuilder
    wcsBuilder.refRa = D2R * 10
    wcsBuilder.refDe = D2R * 10
    wcsBuilder.width = 800
    wcsBuilder.height = 600
    wcsBuilder.projection = "Sin"
    wcsBuilder.pixelScale = 1 / (D2R * 16 * 800)
    val wcs: WCS = wcsBuilder.build
    val ref: Array[Double] = rade2Vector(wcsBuilder.refRa, wcsBuilder.refDe).toArray
    val v2: Array[Double] = wcs.transform(ref)
    assert(v2(0) === 400d)
    assert(v2(1) ===  300d)
  }

}