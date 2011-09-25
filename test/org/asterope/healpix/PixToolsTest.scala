/*
 * Created on Mar 10, 2005
 * Modified on December 2007
 *
 */
package org.asterope.healpix

import junit.framework.Assert._
import math._
import org.asterope.util._
import collection.mutable.Buffer

/**
 * test suit for PixTools
 *
 * @author N. Kuropatkin
 *
 */
class PixToolsTest extends ScalaTestCase {
  /**
   *  test MODULO function
   */
  def testMODULO(){
    var res: Double = PixToolsUtils.MODULO(8.0, 5.0)
    assertEquals("modulo = " + res, 3.0, res, 1e-10)
    res = PixToolsUtils.MODULO(-8.0, 5.0)
    assertEquals("modulo = " + res, 2.0, res, 1e-10)
    res = PixToolsUtils.MODULO(8.0, -5.0)
    assertEquals("modulo = " + res, -2.0, res, 1e-10)
    res = PixToolsUtils.MODULO(-8.0, -5.0)
    assertEquals("modulo = " + res, -3.0, res, 1e-10)
  }

  /**
   * tests calculation of npixels from nside
   */
  def testNside2Npix(){
    var nside: Int = 1
    var npix = PixTools.Nside2Npix(nside).toInt
    assertEquals("Npix=" + npix, 12, npix, 1e-10)
    nside = 2
    npix = PixTools.Nside2Npix(nside).toInt
    assertEquals("Npix=" + npix, 48, npix, 1e-10)
  }

  /**
   * tests calculation of nsides from npixels
   */
  def testNpix2Nside(){
    val npix: Int = 12
    val nside: Long = PixTools.Npix2Nside(npix)
    val pixSize1: Double = PixTools.PixRes(65536)
    val nside1: Long = PixTools.GetNSide(pixSize1)
    assertEquals("Nside=" + nside1, 65536, nside1, 1e-10)
    assertEquals("Nside=" + nside, 1, nside, 1e-10)
  }

  /**
   * test of directional angles calculation
   */
  def testVec2Ang(){
    var v: Vector3d = new Vector3d(0.0, 1.0, 0.0)
    var ang_tup: Array[Double] = Array(0.0, 0.0)
    ang_tup = PixToolsUtils.Vect2Ang(v)

    assertEquals("Theta=" + ang_tup(0), 0.5, ang_tup(0) / Pi, 1e-10)
    assertEquals("Phi=" + ang_tup(1), 0.5, ang_tup(1) / Pi, 1e-10)
    v = new Vector3d(1.0, 0.0, 0.0)
    ang_tup = PixToolsUtils.Vect2Ang(v)
    assertEquals("phi=" + ang_tup(1), 0.0, ang_tup(1) / Pi, 1e-10)

  }

  /**
   * tests calculation of pixel from polar angles
   * in ring schema of pixalization
   */
  def testAng2Pix(){


    val theta: Double = Pi / 2.0- 0.2
    val phi: Double = Pi / 2.0
    val nside: Long = 4
    val pt: PixTools = new PixTools(nside)

    val pix = pt.ang2pix(theta, phi)
    val v: Vector3d = PixTools.Ang2Vec(theta, phi)
    val pix1: Long = pt.vect2pix(v)
    assertEquals("pix=" + pix, pix1, pix, 1e-10)
    assertEquals("pix=" + pix, 76, pix, 1e-10)
    val radec: Array[Double] = pt.pix2ang(76)
    assertEquals("theta=" + theta, theta, radec(0), 4e-2)
    assertEquals("phi=" + phi, radec(1), phi, 1e-2)

  }

  /**
   * tests calculation of unit vector from polar angle
   */
  def testAng2Vect(){

    val theta: Double = Pi / 2.0
    val phi: Double = Pi / 2
    val v: Vector3d = PixTools.Ang2Vec(theta, phi)
    System.out.println(v)
    assertEquals("x=" + v.x, 0.0, v.x, 1e-10)
    assertEquals("y=" + v.y, 1.0, v.y, 1e-10)
    assertEquals("z=" + v.z, 0.0, v.z, 1e-10)

  }

  /**
   * tests calculation of ring number from z coordinate
   */
  def testRingNum(){

    def ringNum(z:Double, nside:Int, expect: Long){
      val nring = new PixTools(nside).RingNum(z)
      assertEquals("z=" + z + ", nside="+nside, expect, nring)
    }
    ringNum(0.25, 1, 2)
    ringNum(-0.25, 1, 2)
    ringNum(0.8, 1, 1)
    ringNum(-0.8, 1, 3)

    def ringNum2(ipix:Long, nside:Int,expect:Long) {
      val z = new PixTools(nside).pix2vect(ipix).z
      ringNum(z,nside,expect);
    }

    ringNum2(11,4,2)
    ringNum2(23,4,3)
    ringNum2(39,4,4)
    ringNum2(55,4,5)
    ringNum2(71,4,6)

    ringNum2(87,4,7)
    ringNum2(103,4,8)
    ringNum2(119,4,9)
    ringNum2(135,4,10)

    ringNum2(151,4,11)
    ringNum2(167,4,12)
    ringNum2(169,4,13)
    ringNum2(180,4,14)

  }

  /**
   * tests InRing method
   */
  def testInRing(){
    val pt: PixTools = new PixTools(2)

    var ring = pt.InRing(3, Pi, Pi).toBuffer
    assert( ring === (0 until ring.size).map(_+12).toBuffer)

    var ang_tup  = PixToolsUtils.Vect2Ang(new Vector3d(1.0, 0.0, 0.0))
    ring = pt.InRing(3, ang_tup(1) / Pi, Pi).toBuffer
    assert( ring === (0 until ring.size).map(_+12).toBuffer)


    ang_tup = PixToolsUtils.Vect2Ang(new Vector3d(-1.0, 0.0, 0.0))
    ring = pt.InRing(3, ang_tup(1) / Pi, Pi).toBuffer
    assert( ring === (0 until ring.size).map(_+12).toBuffer)

    ring = pt.InRing(3, 1.75 * Pi, 0.5 * Pi).toBuffer
    val ringHi = Buffer[Long](12, 13, 17, 18, 19)
    assert(ringHi === ring)

    ring =  pt.InRing(3,  0.25 * Pi,  0.5 * Pi).toBuffer
    val ringLow = Buffer[Long](12, 13, 14, 15,19)
    assert(ringLow === ring)


  }

  /**
   * tests conversion from pixel number to vector
   */
  def testPix2Vect_ring(){


    var pt: PixTools = new PixTools(2)
    var v1 = pt.pix2vect(0)
    assertEquals("v1.z = " + v1.z, 1.0, v1.z, 1e-1)

    val v2 = pt.pix2vect(20)
    assertEquals("v2.x = " + v2.x, 1.0, v2.x, 1e-1)
    assertEquals("v2.z = " + v2.z, 0.0, v2.z, 1e-1)

    val v3: Vector3d = pt.pix2vect(22)
    assertEquals("v3.y = " + v3.y, 1.0, v3.y, 1e-1)
    assertEquals("v3.z = " + v3.z, 0.0, v3.z, 1e-1)


    pt = new PixTools(4)
    v1 = pt.pix2vect(95)
    v1 = v1.normalized
    var phi1: Double = atan2(v1.y, v1.x)
    var tetphi = pt.pix2ang(95)
    assertEquals("phi = " + phi1, 0.0, abs(phi1 - tetphi(1)), 1e-10)


    pt = new PixTools(4)
    v1 = pt.pix2vect(26)
    v1 = v1.normalized
    phi1 = atan2(v1.y, v1.x)
    if (phi1 < 0.0) phi1 += Pi * 2
    tetphi = new Array[Double](2)
    tetphi = pt.pix2ang(26)
    assertEquals("phi = " + phi1, 0.0, abs(phi1 - tetphi(1)), 1e-10)

  }

  /**
   * tests Query_Strip method
   */
  def testQuery_Strip(){

    val pixel1 = Buffer[Long](0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26, 27, 28, 29, 30, 31, 32, 33, 34, 35, 36, 37, 38, 39, 40, 41, 42, 43, 44, 45, 46, 47, 48, 49, 50, 51, 52, 53, 54, 55)

    val theta1: Double = 0.0
    val theta2: Double = Pi / 4.0 + 0.2
    val pt: PixTools = new PixTools(4)
    val pixlist = pt.query_strip(theta1, theta2).toBuffer
    assert(pixlist === pixel1)

  }

  /**
   * tests Query_Disc method
   */
  def testQuery_Disc(){


    val pt: PixTools = new PixTools(4)

    val pixel1 = Buffer[Long](45, 46, 60, 61, 62, 77, 78, 92, 93, 94, 109, 110, 124, 125, 126, 141, 142).sorted

    //TODO investigate where those two unused arrays come from
    val pixel2 = Buffer[Long](24, 19, 93, 18, 17, 87, 16, 86, 85, 106, 84, 159, 81, 158, 157, 155, 156).sorted
    val pixel3 = Buffer[Long](52, 79, 49, 78, 77, 75, 76, 74, 73, 70, 72, 67, 189, 66, 65, 183, 64).sorted

    val inclusive: Boolean = true
    val radius: Double = Pi / 8.0
    val v: Vector3d = pt.pix2vect(93)
    val pixlist = pt.query_disc(v, radius, inclusive).toBuffer

    assert(pixel1 === pixlist)
  }

  /**
   * tests Query_Triangle method
  */
  def testQuery_Triangle(){

    val pt: PixTools = new PixTools(4)

    var v11: Vector3d = pt.pix2vect(62)
    var v22: Vector3d = pt.pix2vect(57)
    var v33: Vector3d = pt.pix2vect(140)
    val pixlist = pt.query_triangle(v11, v22, v33, false).toBuffer
    val pixel1 = Buffer[Long](57, 58, 59, 60, 61, 62, 74, 75, 76, 77, 78, 90, 91, 92, 93, 107, 108, 109, 123, 124, 140)
    assert(pixlist === pixel1)

    v11 = pt.pix2vect(92)
    v22 = pt.pix2vect(88)
    v33 = pt.pix2vect(154)
    val pixlist1 = pt.query_triangle(v11, v22, v33, false).toBuffer
    val pixel2 = Buffer[Long](88, 89, 90, 91, 105, 106, 107, 108, 121, 122, 123, 138, 139, 154)
    assert(pixlist1 == pixel2)

    v11 = pt.pix2vect(49)
    v22 = pt.pix2vect(142)
    v33 = pt.pix2vect(145)
    var pixlist2 = pt.query_triangle(v11, v22, v33, false).toBuffer
    val pixel3 = Buffer[Long](49, 64, 80, 81, 95, 96, 112, 113, 127, 128, 142, 143, 144, 145)
    assert(pixlist2 === pixel3)

    v11 = pt.pix2vect(36)
    v22 = pt.pix2vect(129)
    v33 = pt.pix2vect(135)
    pixlist2 = pt.query_triangle(v11, v22, v33, false).toBuffer
    val pixel4 = Buffer[Long](36, 52, 53, 67, 68, 69, 83, 84, 85, 86, 98, 99, 100, 101, 102, 114, 115, 116, 117, 118, 119, 129, 130, 131, 132, 133, 134, 135)
    assert(pixlist2 === pixel4)

    v11 = pt.pix2vect(123)
    v22 = pt.pix2vect(156)
    v33 = pt.pix2vect(110)
    pixlist2 = pt.query_triangle(v11, v22, v33, false).toBuffer
    val pixel6 = Buffer[Long](110, 123, 124, 125, 140, 141, 156)
    assert(pixlist2 === pixel6)

    v11 = pt.pix2vect(69)
    v22 = pt.pix2vect(53)
    v33 = pt.pix2vect(68)
    pixlist2 = pt.query_triangle(v11, v22, v33, false).toBuffer
    val pixel7 = Buffer[Long](53, 68, 69)
    assert(pixlist2 === pixel7)

  }

  /**
   * tests Query_Poligon method
   */
  def testQuery_Polygon(){

    val pt: PixTools = new PixTools(4)

    var vlist = Buffer(53,51,82,115,117,86).map(pt.pix2vect(_))
    var pixlist = pt.query_polygon(vlist, false).toBuffer
    var result = Buffer[Long](51, 52, 53, 66, 67, 68, 69, 82, 83, 84, 85, 86, 98, 99, 100, 101, 115, 116, 117)
    assert(pixlist === result)

    vlist = Buffer(71,55,70,87).map(pt.pix2vect(_))
    pixlist = pt.query_polygon(vlist, false).toBuffer
    result = Buffer[Long](55, 70, 71, 87)
    assert(pixlist === result)

    vlist = Buffer(153,137,152,168).map(pt.pix2vect(_))
    pixlist = pt.query_polygon(vlist, false).toBuffer
    result = Buffer[Long](137, 152, 153, 168)
    assert(pixlist === result)

    vlist = Buffer(110,27,105,154,123,156).map(pt.pix2vect(_))
    pixlist = pt.query_polygon(vlist, false).toBuffer
    result = Buffer[Long](27, 43, 44, 58, 59, 60, 74, 75, 76, 77, 89, 90, 91, 92, 93, 105, 106, 107, 108, 109, 110, 121, 122, 123, 124, 125, 138, 139, 140, 141, 154, 156)
    assert(pixlist === result)

  }

  /**
   * tests MaxResolution method
   */
  def testMaxResolution(){

    val nside: Long = 1048576
    val res: Double = PixTools.PixRes(nside)
    System.out.println("Minimum size of the pixel side is " + res + " arcsec.")
    assertEquals("res = " + res, 0.2, res, 1e-1)
    val nsideR: Long = PixTools.GetNSide(res)
    assertEquals("nside = " + nside, nside, nsideR, 1e-1)

  }

  /**
   * tests QueryDiscResolution method
   */
  def testQueryDiscRes(){

    var ipix: Long = 0
    val inclusive: Boolean = false
    val theta: Double = Pi
    val phi: Double = Pi
    val radius: Double = toRadians(0.2 / 3600.0)
    val nside: Long = PixTools.GetNSide(radius)
    val pt: PixTools = new PixTools(nside)

    val cpix: Long = pt.ang2pix(theta, phi)
    val vc: Vector3d = pt.pix2vect(cpix)
    val pixlist = pt.query_disc(vc, radius, inclusive).toBuffer
    pixlist.foreach{ipix=>
      val v: Vector3d = pt.pix2vect(ipix)
      val dist: Double = v.angle(vc)
      assertTrue(dist <= 2.0* radius)
    }

  }

  /**
   * tests GetNside method
   */
  def testGetNside(){

    val pixsize: Double = 0.3
    val nside: Long = PixTools.GetNSide(pixsize)
    assertEquals("nside = " + nside, 1048576, nside, 1e-1)

  }

  def testQueryCircle(){
    val nside: Long = 512
    val pt: PixTools = new PixTools(nside)
    val angle: Double = toRadians(0.011451621372724687)
    val v: Vector3d = new Vector3d(0.8956388362603873, -1.838600645782914E-4, 0.44478201534866)
    val ipix: Long = pt.vect2pix(v)
    val r: LongRangeSet = pt.query_disc(v, angle, true)

    assertTrue("pixel not found in disc", r.contains(ipix))
  }
}