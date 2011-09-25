package org.asterope.healpix

import org.asterope.util._

/**
 * Test which runs some 'brutal force' calculations to
 * test calculations produce consistent results.
 * <p>
 * This test is optional and may take very long time to finish.
 */
class CalculTest extends ScalaTestCase {
  def nsideRes(nside: Long): Double = {
    math.toRadians(PixTools.PixRes(nside) / 3600d)
  }

  def testInversionRing: Unit = {
    for (nside <- nsidelist; if(nside >= 5)) {
      val p: PixTools = new PixTools(nside)
      System.out.println(nside)
      val angle: Double = nsideRes(nside) * 2
      for(n <-0 until COUNT){
        val v: Vector3d = new Vector3d(math.random - 0.5, math.random - 0.5, math.random - 0.5).normalized
        val ipix: Long = p.vect2pix(v)
        val v2: Vector3d = p.pix2vect(ipix)
        assert(v2.angle(v) ?< angle, "Vector inversion failed. Nside:" + nside + ", \nvector:" + v)
      }
    }
  }

  def testSmallCircleRing(){
    //query small circle and check center ipix is in it
    for (nside <- nsidelist;  if (nside >= 500)) {

      val p: PixTools = new PixTools(nside)
      System.out.println(nside)
      val angle: Double = nsideRes(nside)

      for(n <-0 until COUNT){
        //get random vector and ipix
        val v: Vector3d = new Vector3d(math.random - 0.5, math.random - 0.5, math.random - 0.5).normalized
        val ipix: Long = p.vect2pix(v)
        //query circle
        val r1: LongRangeSet = p.query_disc(v, angle, true)
        assert(r1.contains(ipix), "Query disc failed. Nside:  " + nside + "\n angle: " + math.toDegrees(angle) + "\n vector : " + v + "\n ipix: " + ipix + "\n rangeSet: " + r1)
         //test non inclusive
        val r2: LongRangeSet = p.query_disc(v, angle, false)
        assert(r2.contains(ipix),"Query disc failed non inclusive. Nside:  " + nside + "\n angle: " + math.toDegrees(angle) + "\n vector : " + v + "\n ipix: " + ipix + "\n rangeSet: " + r2)
        //even smaller circle
        val r3: LongRangeSet = p.query_disc(v, angle / 10, true)
        assert(r3.contains(ipix),"Query disc failed inclusive. Nside:  " + nside + "\n angle: " + math.toDegrees(angle / 10) + "\n vector : " + v + "\n ipix: " + ipix + "\n rangeSet: " + r3)
        //bigger circle
        val r4: LongRangeSet = p.query_disc(v, angle * 10, false)
        assert(r4.contains(ipix),"Query disc failed non inclusive. Nside:  " + nside + "\n angle: " + math.toDegrees(angle * 10) + "\n vector : " + v + "\n ipix: " + ipix + "\n rangeSet: " + r4)
      }
    }
  }

  private[healpix] final val COUNT = 1000
  private[healpix] final val nsidelist: Array[Long] = Array(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576)
}