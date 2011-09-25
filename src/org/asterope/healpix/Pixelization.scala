package org.asterope.healpix

import scala.collection.mutable.Buffer
import org.asterope.util._
import collection.immutable.TreeSet

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
	val maxPixNumber = vector2Ipix(Vector3d.southPole)
	
	/**
	 * Set of pixels which contains the entire sky. 
	 * Is used to query for all data in database. 
	 */
	val FULL_SKY: LongRangeSet = new LongRangeSet(Array(0,Long.MaxValue -1),2);
	
	def vector2Ipix(v:Vector3d):Long = {
		v.assertNormalized
		tools.vect2pix(v);
	}
	
	def rade2Ipix(ra:Angle, de:Angle):Long = vector2Ipix(Vector3d.rade2Vector(ra,de))

	def ipix2Vector(ipix:Long):Vector3d = tools.pix2vect(ipix)
	

	
	def queryDisc(centralPoint:Vector3d, radius:Angle) = 
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

	
}