package org.asterope.healpix

import org.asterope.util._

class HiResTest extends ScalaTestCase {
  /**
   * test on high resolutions. If range set are working correctly, no OutOfMemory is generated
   *
   */
  def testQueryRing() {
    var rs: LongRangeSet = null
    System.out.println("ring 1d")
    rs = ps.query_disc(V, D2R * 1, false)
    System.out.println(rs.size)
    System.out.println("ring 10d")
    rs = ps.query_disc(V, D2R * 10, false)
    System.out.println(rs.size)
  }

  private[healpix] final val NSIDE: Int = 1048576   //highest res available with long ranges
  private[healpix] final val D2R: Double = math.Pi / 180d
  private[healpix] final val ps: PixTools = new PixTools(NSIDE)
  private[healpix] final val V: Vector3d = new Vector3d(1, 1, 1).normalized
}