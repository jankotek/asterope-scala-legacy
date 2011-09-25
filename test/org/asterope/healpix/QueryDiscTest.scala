package org.asterope.healpix

import junit.framework.TestCase
import junit.framework.Assert._
import org.asterope.util.Vector3d

class QueryDiscTest extends TestCase {
  def testQueryDisc(){
    val nside: Long = 32
    val inclusive: Boolean = false
    val radius: Double = math.Pi
    val radius1: Double = math.Pi / 2.0
    val pt: PixTools = new PixTools(nside)
    val npix: Int = PixTools.Nside2Npix(nside).asInstanceOf[Int]
    val res: Double = PixTools.PixRes(nside)
    System.out.println("res=" + res)
    val pixSize: Double = math.toRadians(res / 3600.0)
    System.out.println("pixSize=" + pixSize + " rad")
    var fullSky = pt.query_disc(new Vector3d(0.0, 0.0, 1.0), radius, inclusive).toBuffer
    var firstHalfSky = pt.query_disc(new Vector3d(0.0, 0.0, 1.0), radius1, inclusive).toBuffer
    var secondHalfSky = pt.query_disc(new Vector3d(0.0, 0.0, -1.0), radius1, inclusive).toBuffer
    firstHalfSky.appendAll(secondHalfSky)

    var pixHalfsList = firstHalfSky.toSet.toBuffer.sorted
    fullSky = fullSky.sorted
    var listL = math.min(fullSky.size, pixHalfsList.size)
    assertEquals(npix, fullSky.size)
    assertEquals(npix, listL)
    for(i<-0 until listL){
          assertEquals(fullSky(i), pixHalfsList(i))
    }

    firstHalfSky = pt.query_disc(new Vector3d(1.0, 0.0, 0.0), radius1, inclusive).toBuffer
    secondHalfSky = pt.query_disc(new Vector3d(-1.0, 0.0, 0.0), radius1, inclusive).toBuffer
    firstHalfSky.appendAll(secondHalfSky)

    pixHalfsList = firstHalfSky.toSet.toBuffer.sorted
    System.out.println("full size=" + fullSky.size + " half size=" + pixHalfsList.size)
    listL = math.min(fullSky.size, pixHalfsList.size)
    assertEquals(npix, fullSky.size)
    assertEquals(npix, listL)
    for(i<- 0 until listL){
          assertEquals(fullSky(i), pixHalfsList(i))
    }

    firstHalfSky = pt.query_disc(new Vector3d(0.0, 1.0, 0.0), radius1, inclusive).toBuffer
    secondHalfSky = pt.query_disc(new Vector3d(0.0, -1.0, 0.0), radius1, inclusive).toBuffer
    firstHalfSky.appendAll(secondHalfSky)
    pixHalfsList = firstHalfSky.toSet.toBuffer.sorted

    System.out.println("full size=" + fullSky.size + " half size=" + pixHalfsList.size)
    listL = math.min(fullSky.size, pixHalfsList.size)
    assertEquals(npix, fullSky.size)

    for(i<- 0 until listL){
          assertEquals(fullSky(i), pixHalfsList(i))
    }
  }
}