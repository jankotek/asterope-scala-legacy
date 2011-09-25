package org.asterope.healpix

import junit.framework.TestCase
import junit.framework.Assert._
import org.asterope.util.Vector3d

class PixToolTest1 extends TestCase {

  /**inverse method, convert area ID to RaDePoint */
  def getRaDe(area: Long, nsides: Long): RaDePoint = {
    var tools: PixTools = new PixTools(nsides)
    var polar: Array[Double] = tools.pix2ang(area)
    var radec: Array[Double] = PixToolsUtils.PolarToRaDec(polar)
    new RaDePoint(radec(0), radec(1))
  }

  /**
   * Simple point on sky. Used as return value of functions
   * <p>
   * @See CoeliObject for data definition
   */
  case class RaDePoint(ra:Double,de:Double)



  def testCircle1 {
    var tools: PixTools = new PixTools(NSIDE1)
    var vect: Vector3d = PixTools.Ang2Vec(0.5178634297507729, 0.06421357206295804)
    System.out.println(vect)
    tools.query_disc(PixTools.Ang2Vec(0.5178634297507729, 0.06421357206295804), 5.817764173314432E-4, true)
  }

  def testCircle2 {
    var tools: PixTools = new PixTools(NSIDE1)
    tools.query_disc(PixTools.Ang2Vec(0.3127581538205727, 0.1050979097909252), 0.01454441043328608, true)
  }

  def testCircle3 {
    var tools: PixTools = new PixTools(NSIDE1)
    var vect: Vector3d = PixTools.Ang2Vec(0.011620983936195673, 0.44456444930382233)
    System.out.println(vect)
    tools.query_disc(vect, 5.526875964648709E-4, true)
  }

  def testCircle4 {
    var tools: PixTools = new PixTools(NSIDE1)
    tools.query_disc(PixTools.Ang2Vec(0.3127581538205727, 0.1050979097909252), 0.01454441043328608, true)
  }

  /*** with NSIDE 512 **/
  def testCircle5 {
    val tools: PixTools = new PixTools(NSIDE2)
    tools.query_disc(Vector3d.rade2Vector(1.0486568403767373, 0.036411931519731704), 6.399540590645875E-4, true)
  }

  /*** with NSIDE 512 **/
  def testVertexes {
    var ra: Double = 30.0
    var dec: Double = 30.0
    var radec: Array[Double] = new Array[Double](2)
    radec(0) = ra
    radec(1) = dec
    var thetphi: Array[Double] = new Array[Double](2)
    thetphi = PixToolsUtils.RaDecToPolar(radec)
    var theta: Double = thetphi(0)
    var phi: Double = thetphi(1)
    var tools: PixTools = new PixTools(NSIDE2)
    var ipix: Long = tools.ang2pix(theta, phi)
    var vertexes: Array[Vector3d] = tools.makePix2Vect(ipix).toVertex
    var ipixR: Long = tools.ang2pix(theta, phi)
    var vertexesr: Array[Vector3d] = tools.makePix2Vect(ipixR).toVertex
    for(i<-0 until 4){
          var vect: Vector3d = vertexes(i)
          var angs: Array[Double] = PixToolsUtils.Vect2Ang(vect)
          var vectR: Vector3d = vertexesr(i)
          var angsR: Array[Double] = PixToolsUtils.Vect2Ang(vectR)
          assertEquals("theta=" + angs(0), angs(0), angsR(0), 1e-10)
          assertEquals("phi=" + angs(1), angs(1), angsR(1), 1e-10)
       }

  }

  /*** with high res.**/
  def testVertexesHR {
    var nside: Long = 1 << 20
    var tools: PixTools = new PixTools(nside)
    var ra: Double = 30.0
    var dec: Double = 30.0
    var radec: Array[Double] = new Array[Double](2)
    radec(0) = ra
    radec(1) = dec
    var thetphi: Array[Double] = new Array[Double](2)
    thetphi = PixToolsUtils.RaDecToPolar(radec)
    var theta: Double = thetphi(0)
    var phi: Double = thetphi(1)
    var ipix: Long = tools.ang2pix(theta, phi)
    var vertexes: Array[Vector3d] = tools.makePix2Vect(ipix).toVertex
    var ipixR: Long = tools.ang2pix(theta, phi)
    var vertexesr: Array[Vector3d] = tools.makePix2Vect(ipix).toVertex
    for(i<-0 until 4){
          var vect: Vector3d = vertexes(i)
          var angs: Array[Double] = PixToolsUtils.Vect2Ang(vect)
          var vectR: Vector3d = vertexesr(i)
          var angsR: Array[Double] = PixToolsUtils.Vect2Ang(vectR)
          assertEquals("theta=" + angs(0), angs(0), angsR(0), 1e-10)
          assertEquals("phi=" + angs(1), angs(1), angsR(1), 1e-10)
    }
  }

  def testInverse(){
      for(ra <- Range(0,360,10); de <- Range(-85,85,10)) {
        var area: Long = getHealId(ra, de, NSIDE2)
        var p: RaDePoint = getRaDe(area, NSIDE2)
        //compare with tolerance
        assertTrue(ra + "!=" + p.ra, math.abs(ra - p.ra) < 1)
        assertTrue(de + "!=" + p.de, math.abs(de - p.de) < 1)
      }
  }

  /**
   * get position ID for ra,de
   * @param ra right ascencion in degrees
   * @param de declination in degrees
   * @param nsides - non default number of nsides
   */
  def getHealId(ra: Double, de: Double, nsides: Long): Long = {
    var radec: Array[Double] = new Array[Double](2)
    radec(0) = ra
    radec(1) = de
    var polar: Array[Double] = PixToolsUtils.RaDecToPolar(radec)
    var tools: PixTools = new PixTools(nsides)
    var ip: Long = tools.ang2pix(polar(0), polar(1))
    ip
  }

  val NSIDE1 = 64
  val NSIDE2 = 512
}