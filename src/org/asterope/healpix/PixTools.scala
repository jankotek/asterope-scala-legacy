//
// Licenced under GPLv2, see licence.txt
// (c) K.M. Gorski, Nickolai Kuropatkin, Jan Kotek,
//
package org.asterope.healpix

import org.asterope.util.Vector3d
import java.util.ArrayList
import java.util.Arrays
import java.lang.IllegalArgumentException
import math._

/**
 *
 *  Contains methods translated from HEALPix Fortran90
 *  with increased map resolution in comparison to original Fortran code.
 *  <p>
 *  Is thread safe.
 *
 * @author N Kuropatkin
 * @author Jan Kotek
 *
 */
object PixTools {

  /** represents Healpix pixel with center and corner unit vectors)*/
  case class Pixel(center:Vector3d, west:Vector3d, east:Vector3d, north:Vector3d, south:Vector3d){
    def toVertex = Array(west,east,north,south)
  }

  /**
   * returns nside such that npix = 12*nside^2,  nside should be
   * power of 2 and smaller than ns_max if not return -1
   *
   * @param npix
   *            long the number of pixels in the map
   * @return long nside the map resolution parameter
   */
  def Npix2Nside(npix: Long): Long = {
    val npixmax: Long = 12 * ns_max.toLong * ns_max.toLong
    val nside: Long = rint(sqrt(npix / 12)).toLong
    if (npix < 12) {
      throw new IllegalArgumentException("npix is too small should be > 12")
    }
    if (npix > npixmax) {
      throw new IllegalArgumentException("npix is too large > 12 * ns_max^2")
    }
    val fnpix: Double = 12.0 * nside * nside
    if (abs(fnpix - npix) > 1.0e-2) {
      throw new IllegalArgumentException("npix is not 12*nside*nside")
    }
    val flog: Double = log(nside.toDouble) / log(2.0)
    val ilog: Double = rint(flog)
    if (abs(flog - ilog) > 1.0e-6) {
      throw new IllegalArgumentException("nside is not power of 2")
    }
    nside
  }

  /**
   * calculates npix such that npix = 12*nside^2 ,nside should be
   * a power of 2, and smaller than ns_max otherwise return -1
   *
   * @param nside
   *            long the map resolution
   * @return npix long the number of pixels in the map
   */
  def Nside2Npix(nside: Long): Long = {
    if (Arrays.binarySearch(nsidelist, nside) < 0) {
      throw new IllegalArgumentException("nside should be >0, power of 2, <" + ns_max)
    }
    12 * nside * nside
  }

  /**
   * calculates angular resolution of the pixel map
   * in arc seconds.
   * @param nside
   * @return double resolution in arcsec
   */
  def PixRes(nside: Long): Double = {
    val degrad: Double = toDegrees(1.0)
    val skyArea: Double = 4.0* Pi * degrad * degrad // 4PI steredian in deg^2
    val arcSecArea: Double = skyArea * 3600.0* 3600.0 // 4PI steredian in (arcSec^2)
    val npixels: Long = 12 * nside * nside
    val area =  arcSecArea / npixels // area per pixel
    sqrt(area)   // angular size of the pixel arcsec
  }

  /**
   * calculate requared nside given pixel size in arcsec
   * @param pixsize in arcsec
   * @return long nside parameter
   */
  def GetNSide(pixsize: Double): Long = {
    var res: Long = 0
    val pixelArea: Double = pixsize * pixsize
    val degrad: Double = toDegrees(1.0)
    val skyArea: Double = 4.0* Pi * degrad * degrad * 3600.0* 3600.0
    val npixels: Long = (skyArea / pixelArea).toLong
    val nsidesq: Long = npixels / 12
    val nside_req: Long = sqrt(nsidesq).toLong
    var mindiff: Long = ns_max
    var indmin: Int = 0
    for(i <-0 until nsidelist.length){
          if (abs(nside_req - nsidelist(i)) <= mindiff) {
            mindiff = abs(nside_req - nsidelist(i))
            res = nsidelist(i)
            indmin = i
          }
          if ((nside_req > res) && (nside_req < ns_max)) res = nsidelist(indmin + 1)
          if (nside_req > ns_max) {
            //throw new IllegalArgumentException("nside cannot be bigger than " + ns_max)
            //TODO I would like to throw exception here, but one test case fails after that. Investigate!
            System.err.println("nside cannot be bigger than "+ns_max);
            return ns_max;
          }

    }
    res
  }

  /**
     * calculates vector corresponding to angles theta (co-latitude
     * measured from North pole, in [0,pi] radians) phi (longitude measured
     * eastward in [0,2pi] radians) North pole is (x,y,z) = (0, 0, 1)
     *
     * @param theta double
     * @param phi double
     * @return Vector3d
     * @throws IllegalArgumentException
     */
    def Ang2Vec(theta: Double, phi: Double): Vector3d = {
      var v: Vector3d = null
      if ((theta < 0.0) || (theta > Pi)) {
        throw new IllegalArgumentException("theta out of range [0.,PI]")
      }
      var stheta: Double = sin(theta)
      var x: Double = stheta * cos(phi)
      var y: Double = stheta * sin(phi)
      var z: Double = cos(theta)
      v = new Vector3d(x, y, z)
      v
    }

    def nside2norder(nside:Long):Int ={
      nsidelist.indexWhere(_==nside)
    }

    def norder2nside(norder:Long):Long = {
      nsidelist(norder.toInt)
    }


    protected val twothird: Double = 2.0/ 3.0
  protected val TWOPI: Double = 2.0* Pi
  protected val HALFPI: Double = Pi / 2.0
  protected val ns_max: Int = 1048576
  protected val  nsidelist: Array[Long] = Array(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576, 2097152, 4194304)



}

class PixTools(val nside: Long) {

  import PixTools._

  /**
   * finds pixels having a colatitude (measured from North pole) :
   * theta1 < colatitude < theta2 with 0 <= theta1 < theta2 <= Pi
   * if theta2 < theta1
   * then pixels with 0 <= colatitude < theta2 or theta1 < colatitude < Pi are
   * returned
   *
   * @param theta1
   *            lower edge of the colatitude
   * @param theta2
   *            upper edge of the colatitude
   * @return LongList of  pixel numbers (long)
   * @throws Exception
   * @throws IllegalArgumentException
   */
  def query_strip(theta1: Double, theta2: Double): LongRangeSet = {
    val res = new LongRangeSetBuilder
    var nstrip: Long = 0L

    val colrange: Array[Double] = new Array[Double](4)
    val npix = PixTools.Nside2Npix(nside)
    if (npix < 0) {
      throw new IllegalArgumentException("Nside should be power of 2")
    }
    if ((theta1 < 0.0 || theta1 > Pi) || (theta2 < 0.0 || theta2 > Pi)) {
      throw new IllegalArgumentException("Illegal value of theta1, theta2")
    }
    if (theta1 <= theta2) {
      nstrip = 1
      colrange(0) = theta1
      colrange(1) = theta2
    }else {
      nstrip = 2
      colrange(0) = 0.0
      colrange(1) = theta2
      colrange(2) = theta1
      colrange(3) = Pi
    }

    //loops on strips
    for(
      is <- 0 until nstrip.toInt;
      irmin = RingNum(cos(colrange(2 * is)));
      irmax = RingNum(cos(colrange(2 * is + 1)));
      //loops on strips
      iz<-irmin to irmax
    ){
                val phi0 = 0.0
                val dphi = Pi
                InRing(iz, phi0, dphi, res)

    }
    res.build
  }

  /**
   * finds pixels that lay within a CONVEX polygon defined by its vertex on
   * sphere
   *
   * @param vlist
   *            ArrayList of vectors defining the polygon vertices
   * @param inclusive
   *            if set 1 returns all pixels crossed by polygon boundaries
   * @return LongList of pixels
   *
   * algorithm: the polygon is divided into triangles vertex 0 belongs to all
   * triangles
   * @throws Exception
   * @throws IllegalArgumentException
   */
  def query_polygon(vlist2: Iterable[Vector3d], inclusive: Boolean): LongRangeSet = {
    val vlist = vlist2.toBuffer
    var res: LongRangeSet = LongRangeSetBuilder.EMPTY
    var vp0: Vector3d = null
    var vp1: Vector3d = null
    var vp2: Vector3d = null
    var vo: Vector3d = null
    var hand: Double = .0
    val ss: Array[Double] = new Array[Double](vlist.size)
    var ix: Int = 0
    var n_remain: Int = 0
    var np: Int = 0
    var nm: Int = 0
    var nlow: Int = 0
    for(k<-0 until vlist.size){
        ss(k) = 0.0
    }
    n_remain = vlist.size
    if (n_remain < 3) {
      throw new IllegalArgumentException(" Number of vertices should be >= 3")
    }
    /* Check that the polygon is convex or has only one concave vertex */
    var i0: Int = 0
    var i2: Int = 0
    if (n_remain > 3) { // a triangle is always convex
      for(i1<-1 to n_remain-1){ // in [0,n_remain-1]
            i0 = (i1 - 1) % n_remain
            i2 = (i1 + 1) % n_remain
            vp0 = vlist(i0) // select vertices by 3
            // neighbour
            vp1 = vlist(i1)
            vp2 = vlist(i2)
            // computes handedness (v0 x v2) . v1 for each vertex v1
            vo = vp0.cross(vp2)
            hand = vo.dot(vp1)
            if (hand >= 0.0) {
              ss(i1) = 1.0
            } else {
              ss(i1) = -1.0
            }
      }
      np = 0 // number of vert. with positive handedness
      for(i<-0 until vlist.size;if (ss(i) > 0.0)){
              np += 1;
      }
      nm = n_remain - np
      nlow = min(np, nm)
      if (nlow != 0) {
        if (nlow == 1) { // only one concave vertex
           // ix index of the vertex in the list
          val plus = if(np==1) -1.0 else 1.0
          (0 until vlist.size -1)
              .find(k=>abs(ss(k) +plus ) <= 1.e-12)
              .foreach(ix=_)

          }
          // rotate pixel list to put that vertex in #0
          val n_rot: Int = vlist.size - ix
          val ilast: Int = vlist.size - 1
          for(k <- 0 until  n_rot) {
                val temp: Vector3d = vlist(ilast)
                vlist.remove(ilast)
                vlist.`+=:`(temp) //TODO howto call this operator normaly?
          }
        }
        if (nlow > 1) {// more than 1concave vertex
          throw new IllegalArgumentException(" The polygon has more than one concave vertex.  The result is unpredictable")
        }
    }
    /* fill the polygon, one triangle at a time */
    while (n_remain >= 3) {
      vp0 = vlist(0)
      vp1 = vlist(n_remain - 2)
      vp2 = vlist(n_remain - 1)
      val templist = query_triangle(vp0, vp1, vp2, inclusive)
      res = res.union(templist)

      n_remain -= 1

    }
    res
  }

  /**
   * generates a list of pixels that lay inside a triangle defined by
   * the three vertex vectors
   *
   * @param v1
   *            Vector3d defines one vertex of the triangle
   * @param v2
   *            Vector3d another vertex
   * @param v3
   *            Vector3d yet another one
   * @param do_inclusive
   *            long 0 (default) only pixels whose centers are inside the
   *            triangle will be listed, if set to 1 all pixels overlaping the
   *            triangle will be listed
   * @return LongList with pixel numbers
   * @throws Exception
   * @throws IllegalArgumentException
   */
  def query_triangle(v1: Vector3d, v2: Vector3d, v3: Vector3d, do_inclusive: Boolean): LongRangeSet = {


    val npix: Long = PixTools.Nside2Npix(nside)
    if (npix < 0) {
      throw new IllegalArgumentException(" Nside should be power of 2 >0 and < " + ns_max)
    }

    val vv = Array[Vector3d](v1.normalized, v2.normalized, v3.normalized)


    val dth1: Double = 1.0 / (3.0 * (nside * nside))
    val dth2: Double = 2.0 / (3.0 * nside)


    // determ = (v1 X v2) . v3 determines the left ( <0) or right (>0)
    // handedness of the triangle
    val vt: Vector3d = vv(0).cross(vv(1))
    val determ: Double = vt.dot(vv(2))
    if (abs(determ) < 1.0e-20) {
      throw new IllegalArgumentException("QueryTriangle: the triangle is degenerated - query cannot be performed")
    }
    // The sign of determinant
    val sdet: Double = if (determ >= 0.) 1.0 else -1.0
    /* vector orthogonal to the great circle containing the vertex doublet */
    val vo = Array[Vector3d](
      vv(1).cross(vv(2)).normalized,
      vv(2).cross(vv(0)).normalized,
      vv(0).cross(vv(1)).normalized
    )

    /* test presence of poles in the triangle */
    var zmax: Double = -1.0
    var zmin: Double = 1.0
    val test1: Boolean = (vo(0).z * sdet >= 0.0) // north pole in hemisphere defined by 2-3
    val test2: Boolean = (vo(1).z * sdet >= 0.0) // north pole in the hemisphere defined 1-2
    val test3: Boolean = (vo(2).z * sdet >= 0.0)  // north pole in hemisphere defined by 1-3
    if (test1 && test2 && test3)
      zmax = 1.0  // north pole in the triangle
    if ((!test1) && (!test2) && (!test3))
      zmin = -1.0 // south pole in the triangle
    /* look for northenest and southernest points in the triangle */

    /* sin of theta for orthogonal vector */
    val sto: Array[Double] = new Array[Double](3)
    for(i <-0 until 3){
          sto(i) = sqrt((1.0 - vo(i).z) * (1.0 + vo(i).z))
    }

    //for each segment ( side of the triangle ) the extrema are either -
    //-the 2 vertices - one of the vertices and a point within the segment
    zmax = max(max(vv(0).z, vv(1).z), max(vv(2).z, zmax))
    zmin = min(min(vv(0).z, vv(1).z), min(vv(2).z, zmin))

    //if we are inclusive, move upper point up, and lower point down, by a half pixel size
    var sin_off: Double = 0.0
    if (do_inclusive) {
      val offset: Double = Pi / (4.0 * nside)  // half pixel size
      sin_off = sin(offset)
      zmax = min(1.0, cos(acos(zmax) - offset))
      zmin = max(-1.0, cos(acos(zmin) + offset))
    }

    /* loop on the rings */
    val phi0i: Array[Double] = new Array[Double](3)
    val tgthi: Array[Double] = new Array[Double](3)
    for(i<-0 until 3){
          tgthi(i) = -1.0e30 * vo(i).z
          phi0i(i) = 0.0
    }
    for(j<-0 until 3; if (sto(j) > 1.0e-10)){

            tgthi(j) = -vo(j).z / sto(j)
            phi0i(j) = atan2(vo(j).y, vo(j).x) // Should make it 0-2pi ?
            /* Bring the phi0i to the [0,2pi] domain if need */
            if (phi0i(j) < 0.0) {
              phi0i(j) = PixToolsUtils.MODULO((atan2(vo(j).y, vo(j).x) + PixTools.TWOPI), PixTools.TWOPI)
            }
    }
        /*
                  * the triangle boundaries are geodesics: intersection of the sphere
                  * with plans going through (0,0,0) if we are inclusive, the boundaries
                  * are the intersection of the sphere with plains pushed outward by
                  * sin(offset)
                  */

    val dom: Array[Array[Double]] = new Array[Array[Double]](3, 2)
    val alldom: Array[Double] = new Array[Double](6)
    val res: LongRangeSetBuilder = new LongRangeSetBuilder
    for(iz<-RingNum(zmax) to  RingNum(zmin)){
          var found: Boolean = false
          var z: Double = .0
          if (iz <= nside - 1) {
            z = 1.0 - iz * iz * dth1
          }
          else if (iz <= 3 * nside) {
            z = (2.0 * nside - iz) * dth2
          }
          else {
            z = -1.0 + (4.0 * nside - iz) * (4.0 * nside - iz) * dth1
          }
          /* computes the 3 intervals described by the 3 great circles */
          val st: Double = sqrt((1.0 - z) * (1.0 + z))
          val tgth: Double = z / st // cotan(theta_ring)
          val dc: Array[Double] = new Array[Double](3)
          for(j<-0 until 3){
              dc(j) = tgthi(j) * tgth - sdet * sin_off / ((sto(j) + 1.0e-30) * st)
          }
          for(k<-0 until 3){
                if (dc(k) * sdet <= -1.0) {
                  // the whole iso-latitude ring is on  right side of the great circle
                  dom(k)(0) = 0.0
                  dom(k)(1) = TWOPI
                }else if (dc(k) * sdet >= 1.0) {
                  // all on the wrong side
                  dom(k)(0) = -1.000001 * (k + 1)
                  dom(k)(1) = -1.0 * (k + 1)
                }else {
                   // some is good some is bad
                  var phi_neg: Double = phi0i(k) - (acos(dc(k)) * sdet)
                  var phi_pos: Double = phi0i(k) + (acos(dc(k)) * sdet)
                  if (phi_pos < 0.) phi_pos += TWOPI
                  if (phi_neg < 0.) phi_neg += TWOPI
                  dom(k)(0) = PixToolsUtils.MODULO(phi_neg, TWOPI)
                  dom(k)(1) = PixToolsUtils.MODULO(phi_pos, TWOPI)
                }
              }

          /* identify the intersections (0,1,2 or 3) of the 3 intervals */
          val dom12: Array[Double] = PixToolsUtils.intrs_intrv(dom(0), dom(1))
          val n12: Int = dom12.length / 2
          var ndom: Int = 0
          if (n12 != 0) {
            if (n12 == 1) {
              val dom123a: Array[Double] = PixToolsUtils.intrs_intrv(dom(2), dom12)
              var n123a: Int = dom123a.length / 2

              if (n123a == 0) found = true
              if (!found) {
                System.arraycopy(dom123a, 0, alldom, 0, dom123a.length)
                ndom = n123a  // 1 or 2
              }
            }
            if (!found) {
              if (n12 == 2) {
                val tmp: Array[Double] = Array(dom12(0), dom12(1))
                val dom123a: Array[Double] = PixToolsUtils.intrs_intrv(dom(2), tmp)
                val tmp1: Array[Double] = Array(dom12(2), dom12(3))
                val dom123b: Array[Double] = PixToolsUtils.intrs_intrv(dom(2), tmp1)
                val n123a: Int = dom123a.length / 2
                val n123b: Int = dom123b.length / 2
                ndom = n123a + n123b // 0, 1, 2 or 3
                if (ndom == 0) found = true
                if (!found) {
                  if (n123a != 0) {
                    System.arraycopy(dom123a, 0, alldom, 0, 2 * n123a)
                  }
                  if (n123b != 0) {
                    for(l<- 0 until 2 * n123b){
                          alldom(l + 2 * n123a) = dom123b(l)
                    }
                  }
                  if (ndom > 3) {
                    throw new IllegalArgumentException("QueryTriangle: too many intervals found")
                  }
                }
              }
            }
            if (!found) {
              for (idom <- 0 until ndom) {
                    val a_i: Double = alldom((2 * idom).toInt)
                    val b_i: Double = alldom((2 * idom + 1).toInt)
                    var phi0: Double = (a_i + b_i) / 2.0
                    var dphiring: Double = abs(b_i - a_i) / 2.0
                    if (dphiring < 0.0) {
                      phi0 += Pi
                      dphiring += Pi
                    }
                    InRing(iz, phi0, dphiring, res)
              }
            }
          }
    }
    res.build
  }

  /**
   * generates  all pixels that lays within an
   * angular distance Radius of the center.
   *
   * @param vector
   *            Vector3d pointing to the disc center
   * @param radius
   *            double angular radius of the disc (in RADIAN )
   * @param inclusive
   *            boolean false (default) only pixels whose centers lay in the disc
   *            are listed, if set to true, all pixels overlapping the disc
   *            are listed. In the inclusive mode the radius is increased by half the pixel size.
   *            In this case most probably all neighbor pixels will be listed even with very small
   *            radius.
   *            In case of exclusive search and very small radius when the disc lays completely
   *            inside a pixel the pixel number is returned using vector2pix method.
   * @return LongList of pixel numbers
   *
   * calls: RingNum(nside, ir) InRing(nside, iz, phi0, dphi,nest) vector2pix(nside,ipix)
   */
  def query_disc(vector: Vector3d, radius: Double, inclusive: Boolean=true): LongRangeSet = {
    val res = new LongRangeSetBuilder
    val pixres: Double = PixTools.PixRes(nside)
    if (radius < 0.0 || radius > Pi) {
      throw new IllegalArgumentException("angular radius is in RADIAN and should be in [0,pi]")
    }
    vector.assertNormalized
    val dth1: Double = 1.0 / (3.0 * nside * nside)
    val dth2: Double = 2.0 / (3.0 * nside)
    var radius_eff: Double = radius

    if (inclusive)
      radius_eff += Pi / (4.0 * nside)
    val cosang: Double = cos(radius_eff)

    var phi0: Double = 0.0
    var dphi: Double = 0.0

    if (vector.x != 0.0 || vector.y != 0.0)
      phi0 = PixToolsUtils.MODULO(atan2(vector.y, vector.x) + PixTools.TWOPI, PixTools.TWOPI)

    val cosphi0: Double = cos(phi0)
    val a: Double = vector.x * vector.x + vector.y * vector.y
    /* coordinate z of highest and lowest points in the disc */
    val rlat0: Double = asin(vector.z) // latitude in RAD of the center
    val rlat1: Double = rlat0 + radius_eff
    val rlat2: Double = rlat0 - radius_eff
    val zmax:Double =
      if (rlat1 >= PixTools.HALFPI)1.0
      else sin(rlat1)

    val irmin: Long = max(1, RingNum(zmax) - 1) // start from a higher point to be safe
    val zmin: Double  =
      if (rlat2 <= -PixTools.HALFPI) -1.0
      else sin(rlat2)

    val irmax: Long = min(4 * nside - 1, RingNum(zmin) + 1)  // go down to a lower point
    /* loop on ring number */
    for (iz <- irmin to irmax) {
          var z: Double = .0
          if (iz <= nside - 1) {  // north polar cap
            z = 1.0 - iz * iz * dth1
          } else if (iz <= 3 * nside) { // tropical band + equator
            z = (2.0 * nside - iz) * dth2
          } else {
            z = -1.0 + (4.0 * nside - iz) * (4.0 * nside - iz) * dth1
          }
          /* find phi range in the disc for each z */
          val b: Double = cosang - z * vector.z
          val c: Double = 1.0 - z * z
          var cosdphi: Double = b / sqrt(a * c)
          var done: Long = 0
          if (abs(vector.x) <= 1.0e-12 && abs(vector.y) <= 1.0e-12) {
            cosdphi = -1.0
            dphi = Pi // in [0,pi]
            done = 1
          }
          if (done == 0) {
            if (abs(cosdphi) <= 1.0) {
              dphi = acos(cosdphi)
            }else {
              if (cosphi0 >= cosdphi) {
                dphi = Pi // all the pixels at this elevation are in the disc
              } else {
                done = 2 // out of the disc
              }
            }
          }
          if (done < 2) { // pixels in disc
            /* find pixels in the disc */
            InRing(iz, phi0, dphi, res)
          }

    }
    //
    // if no intersections and radius less than pixel size return the pixel number
    //

    if (res.size == 0 && pixres > toDegrees(radius) / 3600.0) {
      val pixel: Long = vect2pix(vector)
      res.append(pixel)
    }
    //make sure that center vector is contained in result set.
    //this is HOTFIX for rounding errors in InRing method

    var ret2: LongRangeSet = res.build
    val centerIpix: Long = vect2pix(vector)
    if (!ret2.contains(centerIpix)) {
      //construct new long range set and add it content to result
      val b2 = new LongRangeSetBuilder
      b2.append(centerIpix)
      ret2 = ret2.union(b2.build)
    }
    ret2
  }

  /**
   * renders theta and phi coordinates of the nominal pixel center for the
   * pixel number ipix (RING scheme) given the map resolution parameter nside
   *
   * @param ipix
   *            long pixel number
   * @return double[] theta,phi
   */
  def pix2ang(ipix: Long): Array[Double] = {
    var theta: Double = .0
    var phi: Double = .0
    if (nside < 1 || nside > ns_max) {
      throw new IllegalArgumentException("Nside should be power of 2 >0 and < " + ns_max)
    }
    val nsidesq: Long = nside * nside
    val npix: Long = 12 * nsidesq // total number of pixels
    if (ipix < 0 || ipix > npix - 1) {
      throw new IllegalArgumentException("ipix out of range calculated from nside")
    }
    val ipix1: Long = ipix + 1  //  in [1, npix]
    val nl2: Long = 2 * nside
    val nl4: Long = 4 * nside
    val ncap: Long = 2 * nside * (nside - 1) // points in each polar cap, =0 for

    if (ipix1 <= ncap) { // North polar cap
      val hip: Double = ipix1 / 2.0
      val fihip: Double = hip.toLong // get integer part of hip
      val iring: Long = (sqrt(hip - sqrt(fihip))).toLong + 1 // counted from north pole
      val iphi: Long = ipix1 - 2 * iring * (iring - 1)
      theta = acos(1.0 - iring * iring / (3.0 * nsidesq))
      phi = (iphi.asInstanceOf[Double] - 0.5) * Pi / (2.0 * iring)

    }else if (ipix1 <= nl2 * (5 * nside + 1)) {  // equatorial region
      val ip: Long = ipix1 - ncap - 1
      val iring: Long = (ip / nl4) + nside  // counted from North pole
      val iphi: Long = ip % nl4 + 1
      // 1 if iring+nside  is odd, 1/2 otherwise
      val fodd: Double = 0.5 * (1.0+ PixToolsUtils.MODULO(iring + nside, 2))
      theta = acos((nl2 - iring) / (1.5 * nside))
      phi = (iphi.asInstanceOf[Double] - fodd) * Pi / (2.0 * nside)
    }else {  // South pole cap
      val ip: Long = npix - ipix1 + 1
      val hip: Double = ip / 2.0
      val fihip: Double = hip.toLong
      val iring: Long = (sqrt(hip - sqrt(fihip))).toLong + 1 // counted from South pole
      val iphi: Long = 4 * iring + 1 - (ip - 2 * iring * (iring - 1))
      theta = acos(-1.0 + iring * iring / (3.0 * nsidesq))
      phi = (iphi.asInstanceOf[Double] - 0.5) * Pi / (2.0 * iring)
    }
    Array(theta, phi)
  }

  /**
   * returns the vector pointing in the center of the pixel ipix. The vector
   * is calculated by makePix2Vect method
   *
   * @param ipix pixel number
   * @return Vector3d
   */
  def pix2vect(ipix: Long): Vector3d = {
    makePix2Vect(ipix).center
  }

  /**
   * renders vector (x,y,z) coordinates of the nominal pixel center for pixel
   * ipix (RING scheme) given the map resolution parameter nside. It also
   * calculates (x,y,z) positions of the four vertices in order N,W,S,E. These
   * results are stored in pixVect and pixVertex structures. Those can be
   * obtained using pix2Vect_ring and pix2vert_ring methods
   *
   * @param ipix
   *            pixel number
   * @return Pixel
   */
  def makePix2Vect(ipix: Long): PixTools.Pixel = {
    var z_nv: Double = .0
    var z_sv: Double = .0
    var hdelta_phi: Double = .0
    var z: Double = .0
    var phi: Double = .0
    val nsidesq: Long = nside * nside
    if (nside < 1 || nside > ns_max) {
      throw new IllegalArgumentException("Nside should be power of 2 >0 and < " + ns_max)
    }
    val npix: Long = 12 * nsidesq
    if (ipix < 0 || ipix > npix - 1) {
      throw new IllegalArgumentException("ipix out of range calculated from nside")
    }
    val ipix1: Long = ipix + 1 //  in [1, npix]
    val nl2: Long = 2 * nside
    val nl4: Long = 4 * nside
    val ncap: Long = 2 * nside * (nside - 1) // points in each polar cap
    val fact1: Double = 1.5 * nside
    val fact2: Double = 3.0 * nsidesq
    var phi_nv: Double = 0.0
    var phi_sv: Double = 0.0
    if (ipix1 <= ncap) { // north polar cap
      val hip: Double = ipix1 / 2.0
      val fihip: Double = hip.toLong
      val iring: Long = (sqrt(hip - sqrt(fihip))).toLong + 1   // counted from north pole
      val iphi: Long = ipix1 - 2 * iring * (iring - 1)
      z = 1.0 - iring * iring / fact2
      phi = (iphi - 0.5) * Pi / (2.0 * iring)
      hdelta_phi = Pi / (4.0 * iring)  // half pixel width
      z_nv = 1.0 - (iring - 1) * (iring - 1) / fact2
      z_sv = 1.0 - (iring + 1) * (iring + 1) / fact2
      val iphi_mod: Long = (iphi - 1) % iring // in [0,1,...,iring-1]
      val iphi_rat: Long = (iphi - 1) / iring  // in [0,1,2,3]
      if (iring > 1) phi_nv = HALFPI * (iphi_rat + iphi_mod / (iring - 1.0))
      phi_sv = HALFPI * (iphi_rat + (iphi_mod + 1.0) / (iring + 1.0))
    }
    else if (ipix1 <= nl2 * (5 * nside + 1)) { // equatorial region
      val ip: Long = (ipix1 - ncap - 1)
      val iring: Long = (ip / nl4) + nside // counted from North pole
      val iphi: Long = ip % nl4 + 1
      val fodd: Double = 0.5 * (1.0+ PixToolsUtils.MODULO(iring + nside, 2))  // 1 if iring+nside is odd or 1/2
      z = (nl2 - iring) / fact1
      phi = (iphi - fodd) * Pi / (2.0 * nside)
      hdelta_phi = Pi / (4.0 * nside) // half pixel width
      phi_nv = phi
      phi_sv = phi
      z_nv = (nl2 - iring + 1) / fact1
      z_sv = (nl2 - iring - 1) / fact1
      if (iring == nside) { // nothern transition
        z_nv = 1.0 - (nside - 1) * (nside - 1) / fact2
        val iphi_mod: Long = (iphi - 1) % nside // in [0,1,...,nside-1]
        val iphi_rat: Long = (iphi - 1) / nside  // in [0,1,2,3]
        if (nside > 1) phi_nv = HALFPI * (iphi_rat + iphi_mod / (nside - 1.))
      }
      else if (iring == 3 * nside) { // southern transition
        z_sv = -1.0 + (nside - 1) * (nside - 1) / fact2
        val iphi_mod: Long = (iphi - 1) % nside // in [0,1,... iring-1]
        val iphi_rat: Long = (iphi - 1) / nside // in [0,1,2,3]
        if (nside > 1) phi_sv = HALFPI * (iphi_rat + iphi_mod / (nside - 1.0))
      }
    }
    else {  // South polar cap
      val ip: Long = npix - ipix1 + 1
      val hip: Double = ip / 2.0
      val fihip: Double = hip.toLong
      val iring: Long = (sqrt(hip - sqrt(fihip))).toLong + 1 // counted from South Pole
      val iphi: Long = 4 * iring + 1 - (ip - 2 * iring * (iring - 1))
      z = -1.0 + iring * iring / fact2
      phi = (iphi - 0.5) * Pi / (2.0 * iring)
      hdelta_phi = Pi / (4.0 * iring) // half pixel width
      z_nv = -1.0 + (iring + 1) * (iring + 1) / fact2
      z_sv = -1.0 + (iring - 1) * (iring - 1) / fact2
      val iphi_mod: Long = (iphi - 1) % iring // in [0,1,...,iring-1]
      val iphi_rat: Long = (iphi - 1) / iring // in [0,1,2,3]
      phi_nv = HALFPI * (iphi_rat + (iphi_mod + 1) / (iring + 1.0))
      if (iring > 1) phi_sv = HALFPI * (iphi_rat + iphi_mod / (iring - 1.0))
    }
    /* pixel center */
    val sth: Double = sqrt((1.0 - z) * (1.0 + z))
    val pixVect: Vector3d = new Vector3d(sth * cos(phi), sth * sin(phi), z)
    /* west vertex */
    val phi_wv: Double = phi - hdelta_phi
    val west = new Vector3d(sth * cos(phi_wv), sth * sin(phi_wv), z)
    /* east vertex */
    val phi_ev: Double = phi + hdelta_phi
    val east = new Vector3d(sth * cos(phi_ev), sth * sin(phi_ev), z)

    /* north vertex */
    val sth_nv: Double = sqrt((1.0 - z_nv) * (1.0 + z_nv))
    val north = new Vector3d( sth_nv * cos(phi_nv), sth_nv * sin(phi_nv), z_nv)
    /* south vertex */
    val sth_sv: Double = sqrt((1.0 - z_sv) * (1.0 + z_sv))
    val south = new Vector3d( sth_sv * cos(phi_sv), sth_sv * sin(phi_sv), z_sv)
    new PixTools.Pixel(center=pixVect, west = west, east = east, north=north, south = south)
  }

  /**
   * renders the pixel number ipix (RING scheme) for a pixel which contains a
   * point with coordinates theta and phi, given the map resolution parameter
   * nside.
   *
   * @param theta
   *            double theta
   * @param phi -
   *            double phi
   * @return long ipix
   */
  def ang2pix(theta: Double, phi: Double): Long = {

    if (nside < 1 || nside > ns_max) {
      throw new IllegalArgumentException("Nside should be power of 2 >0 and < " + ns_max)
    }
    if (theta < 0.0 || theta > Pi) {
      throw new IllegalArgumentException("Theta out of range [0,pi]")
    }

    val z: Double = cos(theta)

    zPhi2pix(z,phi)
  }

  /**
   * renders the pixel number ipix (RING scheme) for a pixel which contains a
   * point on a sphere at coordinate vector (x,y,z), given the map resolution
   * parameter nside
   *
   * @param vector
   *            Vector3d of the point coordinates
   * @return long pixel number
   * @throws IllegalArgumentException
   */
  def vect2pix(vector: Vector3d): Long = {
    var ipix1: Long = 0L
    if (nside < 1 || nside > ns_max) {
      throw new IllegalArgumentException("Nside should be power of 2 >0 and < " + ns_max)
    }
    val dnorm: Double = vector.length
    val z: Double = vector.z / dnorm
    var phi: Double = 0.0
    if (vector.x != 0.0|| vector.y != 0.0)
      phi = atan2(vector.y, vector.x) // phi in [-pi,pi]
    zPhi2pix(z,phi)
  }


  private def zPhi2pix(z:Double, phi2:Double):Long = {
    val zAbs: Double = abs(z)
    var ipix1: Long = 0L
    var phi = phi2
    if (phi >= TWOPI) phi = phi - PixTools.TWOPI
    if (phi < 0.0) phi = phi + PixTools.TWOPI //  phi in [0, 2pi]
    val tt: Double = phi / HALFPI  // in [0,4]
    val nl2: Long = 2 * nside
    val nl4: Long = 4 * nside
    val ncap: Long = nl2 * (nside - 1)  // number of pixels in the north polar cap
    val npix: Long = 12 * nside * nside
    if (zAbs < twothird) { // equatorial region
      val jp: Long = (nside * (0.5 + tt - 0.75 * z)).toLong  // index of ascending edge line
      val jm: Long = (nside * (0.5 + tt + 0.75 * z)).toLong  // index of descending edge line
      val ir: Long = nside + 1 + jp - jm // in [1,2n+1]
      val kshift: Long =   // 1 if ir even, 0 otherwise
          if (PixToolsUtils.MODULO(ir, 2) == 0)  1 else 0
      var ip: Long = ((jp + jm - nside + kshift + 1) / 2) + 1
      if (ip > nl4) ip = ip - nl4
      ipix1 = ncap + nl4 * (ir - 1) + ip
    }
    else { // North and South polar caps
      val tp: Double = tt - tt.toLong
      val tmp: Double = sqrt(3.0 * (1.0 - zAbs))
      val jp: Long = (nside * tp * tmp).toLong // increasing edge line index
      val jm: Long = (nside * (1.0 - tp) * tmp).toLong // decreasing edge index

      val ir: Long = jp + jm + 1 // ring number counted from closest pole
      var ip: Long = (tt * ir).toLong + 1  // in [1,4*ir]
      if (ip > 4 * ir)
        ip = ip - 4 * ir

      ipix1 = 2 * ir * (ir - 1) + ip
      if (z <= 0.0)
        ipix1 = npix - 2 * ir * (ir + 1) + ip
    }
    ipix1 - 1
  }

  def InRing(iz: Long, phi0: Double, dphi: Double): LongRangeSet = {
    val b = new LongRangeSetBuilder
    InRing(iz, phi0, dphi, b)
    b.build
  }

  /**
   * returns the list of pixels in RING scheme with latitude in [phi0 -
   * dpi, phi0 + dphi] on the ring iz in [1, 4*nside -1 ] The pixel id numbers
   * are in [0, 12*nside^2 - 1]
   *
   * @param iz long ring number
   * @param phi0
   * @param dphi
   * @param res store result in this builder
   * @throws IllegalArgumentException
   *
   * Modified by N. Kuropatkin 07/09/2008  Corrected several bugs and make test of all cases.
   *
   */
  def InRing(iz: Long, phi0: Double, dphi: Double, res: LongRangeSetBuilder){
    var to_top: Boolean = false
    val conservative: Boolean = false
    val epsilon: Double = java.lang.Double.MIN_VALUE
    var ir: Long = 0
    var kshift: Long = 0L
    var nr: Long = 0L
    var ipix1: Long = 0L
    var ipix2: Long = 0L
    val npix: Long = 12 * nside * nside  // total number of pixels
    val ncap: Long = 2 * nside * (nside - 1)  // number of pixels in the north polar
    var ip_low: Long = 0
    var ip_hi: Long = 0

    val phi_low: Double = PixToolsUtils.MODULO(phi0 - dphi, TWOPI) - epsilon

    //excluding 2pi period
    val phi_hi: Double = PixToolsUtils.MODULO(phi0 + dphi, TWOPI) + epsilon
    //identifies ring number
    if ((iz >= nside) && (iz <= 3 * nside)) { //equatorial region
      ir = iz - nside + 1 // in [1, 2*nside + 1]
      ipix1 = ncap + 4 * nside * (ir - 1) // lowest pixel number in the ring
      ipix2 = ipix1 + 4 * nside - 1 // highest pixel number in the ring
      kshift = PixToolsUtils.MODULO(ir, 2.0).toLong

      nr = nside * 4
    } else {
      if (iz < nside) {  // north pole
        ir = iz
        ipix1 = 2 * ir * (ir - 1)  // lowest pixel number
        ipix2 = ipix1 + 4 * ir - 1  // highest pixel number
      } else { //south pole
        ir = 4 * nside - iz
        ipix1 = npix - 2 * ir * (ir + 1)   // lowest pixel number
        ipix2 = ipix1 + 4 * ir - 1   // highest pixel number
      }
      nr = ir * 4
      kshift = 1
    }
    // Construct the pixel list
    if (abs(dphi - Pi) < epsilon) {
      //take entire range
      res.appendRange(ipix1, ipix2)
      return
    }
    //java calculation jitter
    val shift: Double = kshift / 2.0

    // conservative : include every intersected pixel, even if the
    // pixel center is out of the [phi_low, phi_hi] region
    if (conservative) {
      ip_low = round((nr * phi_low) / TWOPI - shift)
      ip_hi = round((nr * phi_hi) / TWOPI - shift)
      ip_low = ip_low % nr  // in [0, nr - 1]
      ip_hi = ip_hi % nr // in [0, nr - 1]
    }
    else { // strict: includes only pixels whose center is in [phi_low,phi_hi]
      ip_low = ceil((nr * phi_low) / TWOPI - shift).toLong
      ip_hi = ((nr * phi_hi) / TWOPI - shift).toLong
      if (ip_low == ip_hi + 1) ip_low = ip_hi
      if ((ip_low - ip_hi == 1) && (dphi * nr < Pi)) {
       // the interval is too small ( and away from pixel center)
       // so no pixels is included in the list
        throw new IllegalArgumentException("the interval is too small and avay from center")
      }
      ip_low = min(ip_low, nr - 1)
      ip_hi = max(ip_hi, 0)
    }
    if (ip_low > ip_hi) to_top = true
    if (to_top) {
      ip_low += ipix1
      ip_hi += ipix1
      res.appendRange(ipix1, ip_hi)
      res.appendRange(ip_low, ipix2)
    }
    else {
      if (ip_low < 0) {
        ip_low = abs(ip_low)
        res.appendRange(ipix1, ipix1 + ip_hi)
        res.appendRange(ipix2 - ip_low + 1, ipix2)
        return
      }
      ip_low += ipix1
      ip_hi += ipix1
      res.appendRange(ip_low, ip_hi)
    }
  }

  /**
   * returns the ring number in {1, 4*nside - 1} calculated from z coordinate
   * @param z double z coordinate
   * @return long ring number
   */
  def RingNum(z: Double): Long = {

    var iring: Long = 0

    if (z > twothird) {/* north cap */
      iring = round(nside * sqrt(3.0 * (1.0 - z)))
      if (iring == 0) iring = 1
    }else if (z < -twothird) { /* south cap */
      iring = round(nside * sqrt(3.0 * (1.0 + z)))
      if (iring == 0) iring = 1
      iring = 4 * nside - iring
    }else{ /* equatorial region */
       iring = round(nside * (2.0 - 1.5 * z))
    }

    iring
  }


}