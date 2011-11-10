package org.asterope.util

import scala.collection.mutable.Buffer
import collection.immutable.TreeSet
import org.apache.commons.math.geometry.Vector3D
import org.asterope.healpix._

/**
 * Utilities related to Healpix sky pixelization
 * 
 * @author Jan Kotek
 */
object Pixelization {
	
	/**
	 * Angular resolution used for Healpix. 
	 * Asterope is using single resolution for all purposes to keep things simple. 
	 */
	val resolution:Angle = 10.arcMinute;



	
	/**
	 * Healpix NSIDE used in Asterope. It corresponds to number of pixels,
	 * which corresponds to angular pixel resolution 
	 */
	val NSIDE = PixTools.GetNSide(resolution.toArcSec);


  val tools = new PixTools(NSIDE);

	/**
	 * Maximal pix number, ie pixel number of south pole
	 */
	val maxPixNumber = vector2Ipix(Vector3D.MINUS_K)
	
	/**
	 * Set of pixels which contains the entire sky. 
	 * Is used to query for all data in database. 
	 */
	val FULL_SKY: LongRangeSet = new LongRangeSet(Array(0,Long.MaxValue -1),2);
	
	def vector2Ipix(v:Vector3D):Long = {
		v.assertNormalized
		tools.vect2pix(v);
	}
	
	def rade2Ipix(ra:Angle, de:Angle):Long = vector2Ipix(rade2Vector(ra.toRadian,de.toRadian))

	def ipix2Vector(ipix:Long):Vector3D = tools.pix2vect(ipix)
	

	
	def queryDisc(centralPoint:Vector3D, radius:Angle) =
		tools.query_disc(centralPoint, radius.toRadian, true)
		
	def rangeSetToSeq(rangeSet:LongRangeSet):Seq[(Long,Long)] = {
	    val buffer = Buffer[(Long,Long)]()
	    val iter = rangeSet.rangeIterator;
	    while(iter.moveToNext)
	        buffer+=((iter.first,iter.last))
	    buffer.toSeq
	}

  def treeSetToRangeSet(r:TreeSet[Long]):LongRangeSet={
    val b = new LongRangeSetBuilder()
    r.foreach(i=>b.append(i))
    b.build
  }

  protected val  nsidelist: Array[Long] = Array(1, 2, 4, 8, 16, 32, 64, 128, 256, 512, 1024, 2048, 4096, 8192, 16384, 32768, 65536, 131072, 262144, 524288, 1048576, 2097152, 4194304)

  def nside2norder(nside:Long):Int ={
    nsidelist.indexWhere(_==nside)
  }

  def norder2nside(norder:Long):Long = {
    nsidelist(norder.toInt)
  }

	
}