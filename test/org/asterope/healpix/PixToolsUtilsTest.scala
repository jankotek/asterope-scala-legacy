package org.asterope.healpix

import junit.framework.TestCase
import junit.framework.Assert._
import org.asterope.util.Vector3d

class PixToolsUtilsTest extends TestCase {
  /**
   * @throws Exception
   */
  def testSurfaceTriangle {
    var v1: Vector3d = new Vector3d(1.0, 0.0, 0.0)
    var v2: Vector3d = new Vector3d(0.0, 1.0, 0.0)
    var v3: Vector3d = new Vector3d(0.0, 0.0, 1.0)
    var res: Double = PixToolsUtils.SurfaceTriangle(v1, v2, v3)
    System.out.println("Triangle surface is=" + res / math.Pi + " steredians")
    assertEquals("Triangle surface=" + res, 0.5, res / math.Pi, 1e-10)
    System.out.println(" test of SurfaceTriangle is done")
  }

  /**
   *  test conversion of Ra Dec to polar coordinates
   */
  def testRaDecToPolar {
    System.out.println(" Start test RaDecToPolar !!!!!!!!!!!!!!!!!!!!!")
    var radec: Array[Double] = new Array[Double](2)
    radec(0) = 312.115456
    radec(1) = -1.153759
    var polar: Array[Double] = PixToolsUtils.RaDecToPolar(radec)
    assertEquals("theta = " + polar(0), 1.5909332201194137, polar(0), 1e-10)
    assertEquals("phi = " + polar(1), 5.447442353563491, polar(1), 1e-10)
    System.out.println("End test RaDecToPolar__________________________")
  }

  def testFloor {
    var i1: Long = 9.999999999d.asInstanceOf[Long]
    var i2: Long = math.floor(9.999999999d).asInstanceOf[Long]
    assertEquals(i1, i2)
  }

  /**
   * tests intrs_intrv method
   * @throws Exception
   */
  def testIntrs_Intrv {
    System.out.println(" test intrs_intrv !!!!!!!!!!!!!!!!!!!!!!!!!!!")
    var d1: Array[Double] = Array(1.0, 9.0)
    var d2: Array[Double] = Array(3.0, 16.0)
    var di: Array[Double] = null
    di = PixToolsUtils.intrs_intrv(d1, d2)
    var n12: Int = di.length / 2
    assertEquals("n12 = " + n12, 1, n12, 1e-6)
    assertEquals("di[0] = " + di(0), 3.0, di(0), 1e-6)
    assertEquals("di[1] = " + di(1), 9.0, di(1), 1e-6)
    d1 = Array[Double](0.537, 4.356)
    d2 = Array[Double](3.356, 0.8)
    di = PixToolsUtils.intrs_intrv(d1, d2)
    n12 = di.length / 2
    assertEquals("n12 = " + n12, 2, n12, 1e-6)
    assertEquals("di[0] = " + di(0), 0.537, di(0), 1e-6)
    assertEquals("di[1] = " + di(1), 0.8, di(1), 1e-6)
    assertEquals("di[2] = " + di(2), 3.356, di(2), 1e-6)
    assertEquals("di[1] = " + di(3), 4.356, di(3), 1e-6)
    d1 = Array[Double](2.356194490092345, 2.356194490292345)
    d2 = Array[Double](1.251567, 4.17)
    di = PixToolsUtils.intrs_intrv(d1, d2)
    n12 = di.length / 2
    assertEquals("n12 = " + n12, 1, n12, 1e-6)
    assertEquals("di[0] = " + di(0), 2.35619449009, di(0), 1e-6)
    assertEquals("di[1] = " + di(1), 2.35619449029, di(1), 1e-6)
    System.out.println(" test intrs_intrv is done")
  }
}