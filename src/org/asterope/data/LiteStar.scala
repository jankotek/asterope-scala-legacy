package org.asterope.data

import java.lang.{Long => JLong}
import scala.collection.JavaConversions._
import org.asterope.util._
import org.asterope.healpix._

/**
 * LiteStar is an data object which stores minimal information about star.
 * It is used to draw stars on map without loading full StarlinkObject
 * which is slower to fetch.
 *  
 * @author Jan Kotek
 */
case class LiteStar(
	ra: Angle,
	de: Angle,
	mag: Magnitude,
  separation: Option[Angle] = None,
  posAngle: Option[Angle] = None,
  minMag: Option[Magnitude] = None,
  maxMag: Option[Magnitude] = None,
  names:List[Nomenclature] = Nil
	) extends HasNomenclature {
	
	/** normalized vector with position calculated from Ra De */
	val vector = Vector3d.rade2Vector(ra,de)
	/** healpix ipix */
	lazy val ipix = Pixelization.vector2Ipix(vector);
}

import java.util.ArrayList
import java.lang.Long
import jdbm._


/**
 * Data Acces Object bean which handless LiteStar. 
 * Most complicated task is fetching by area (healpix) and limiting magnitude. 
 * 
 * @author Jan Kotek
 *
 */
class
LiteStarDao(val recman:RecordManager)  {

	/** serializer used for more efficient space usage */ 
	protected object serializer extends Serializer[LiteStar] {
	
		override def serialize(out:SerializerOutput, obj: LiteStar){
			//save as much space as possible, so angles are writen in MAS as Integers
			out.writeInt(obj.ra.toMas.toInt)
			out.writeInt(obj.de.toMas.toInt)
			//and magnitude is written in Short.
			out.writeShort(obj.mag.toMilimag.toShort);
      val isVariable = obj.minMag.isDefined
      val isBinary = obj.separation.isDefined && obj.posAngle.isDefined
      out.writeByte(IOUtil.bitsEncode(List(isVariable,isBinary)))

      if(isVariable){
        out.writeShort(obj.maxMag.get.toMilimag.toShort)
        out.writeShort(obj.minMag.get.toMilimag.toShort)
      }
      if(isBinary){
        out.writeInt(obj.posAngle.get.toMas.toInt)
        out.writeInt(obj.separation.get.toMas.toInt)
      }

      Nomenclature.listSerializer.serialize(out,obj.names)


		}
	
		override def deserialize(in:SerializerInput):LiteStar = {
				val ra2 = in.readInt.mas
				val de2 = in.readInt.mas
				val mag2 = Magnitude (in.readShort() / 1000)
        val List(isVariable,isBinary,_,_,_,_,_,_) = IOUtil.bitsDecode(in.readByte())
        val maxMag2:Option[Magnitude] = if(isVariable) Some(Magnitude(in.readShort/1000)) else None
        val minMag2:Option[Magnitude] = if(isVariable) Some(Magnitude(in.readShort/1000)) else None
        val posAngle2:Option[Angle] = if(isBinary) Some(in.readInt.mas) else None
        val separation2:Option[Angle] = if(isBinary) Some(in.readInt.mas) else None
        val names2 = Nomenclature.listSerializer.deserialize(in)

				return new LiteStar(ra=ra2,de=de2,mag=mag2,posAngle=posAngle2, separation=separation2, maxMag=maxMag2, minMag=minMag2, names=names2);
		}
	}
	
	
	/**
	 * Holds 'lite star' an object containing minimal information about Star. 
	 * It is used to draw map instead of heavier 'StarlinkObject'.
	 */
	val liteStars:PrimaryStoreMap[JLong,LiteStar] = recman.storeMap("liteStars", serializer)
	
	/** most brightest star for Mag/Healpix index ,brighter stars mag is converted to this */ 
	val MINMAG = 2;
	/** most fainter star in Mag/Healpix, bigger magnitudes are converted to this value */ 
	val MAXMAG = 25;
	
	/**
	 * Magnitude/Healpix index. It is little bit complicated:
	 * 1) key in first map is magnitude calculated  using mag2liteStarsByMagHealpixKey()
	 * 2) key in second map is healpix ipix. 
	 * 3) value in second map is list of recids pointing to liteStars Map. ArrayList is better persisted by JDBM. 
	 */
	val liteStarsByMagHealpix:Map[Int,PrimaryTreeMap[JLong,ArrayList[JLong]]] =
		Map() ++ (for(i <- MINMAG to MAXMAG)
			yield (i -> recman.treeMap[JLong,ArrayList[JLong]]("liteStarsByMagHealpix-"+i))
	)

  private object nameKeyExtractor extends jdbm.SecondaryKeyExtractor[java.lang.Iterable[Nomenclature],JLong,LiteStar]{
    import collection.JavaConversions._
    def extractSecondaryKey(l:JLong, star:LiteStar) = star.names
  }

  val liteStarsByName:SecondaryTreeMap[Nomenclature,JLong,LiteStar] = liteStars.secondaryTreeMapManyToOne(
    "liteStarsByName",nameKeyExtractor, Nomenclature.serializer)
	
	/**
	 * Query stars by given area and limiting magnitude. Full StarlinkObject can be obtained using 
	 * LiteStar.starlinkObjectRecid reference 
	 * 
	 * @param area limit
	 * @param limitMag limiting magnitude
	 * @return iterator over matching stars
	 */
	def starsByAreaMag(area: LongRangeSet, limitMag:Magnitude):Iterator[LiteStar] = {
      val areaSeq = Pixelization.rangeSetToSeq(area)
    val limitMag2 = mag2liteStarsByMagHealpixKey(limitMag)
    (MINMAG to limitMag2)
      .flatMap{mag=>
      val substars = liteStarsByMagHealpix(mag)
       areaSeq
       .map(f=>substars.subMap(f._1, f._2+1))
       .flatMap(_.values())
       .flatMap(_.iterator)
       .map(liteStars.get(_))
       .filter(_.mag<limitMag)
    }.iterator
	}

	
	/**
	 * Convert Magnitude into first key used in liteStarsByMagHealpix map
	 */
	protected def mag2liteStarsByMagHealpixKey(mag:Magnitude) = 
			if(mag.mag<MINMAG) MINMAG
			else if (mag.mag>MAXMAG) MAXMAG
			else mag.mag.floor.asInstanceOf[Int]
	/**
	 * Add star to database 
	 * @param star to be added
	 * @param starlinkRecid of bigger starlink object. If does not exist, use 0 
	 */
	def addStar(star:LiteStar){
		val liteStarRecid:Long = liteStars.putValue(star);
		/**
		 * add lite star into Healpix/Mag index
		 */
		val ipix:Long = new JLong(star.ipix)
		val mag:Int = mag2liteStarsByMagHealpixKey(star.mag)
		//create array list if needed
    val list: ArrayList[JLong] =
      if (!liteStarsByMagHealpix(mag).containsKey(ipix)) new ArrayList[JLong];
      else liteStarsByMagHealpix(mag).get(ipix)
		//insert ipix into array list
		list.add(liteStarRecid)
		//put array list back to index
		liteStarsByMagHealpix(mag).put(ipix,list)
	}
	
	def all:Iterator[LiteStar] = liteStars.valuesIterator

	def objectsByName(name:String):Iterator[LiteStar] = {
		recidsByName(name).map{liteStarsByName.getPrimaryValue(_)}
	}

  //TODO code duplication
	def recidsByName(name2:String):Iterator[Long] = {
    Nomenclature.recidsByMame(name2,liteStarsByName)
	}

  def addName(id1: String, id2: Nomenclature) {
		//query for all objects with this name
		val iter = recidsByName(id1).toList
		assert(iter.size>0,"Name not found: '"+id1+"'");
    assert(iter.size<2,"More then one candidate found: '"+id1+"'");
		iter.foreach{recid=>
      //fetch and make copy with new name
      val s1 = liteStars.get(recid)
      val names2 = id2 :: s1.names;
      val s2 = s1.copy(names = names2)
      //replace original with new copy
      liteStars.put(recid,s2)
    }
	}

  def byRecid(recid:Long):LiteStar = liteStars.get(recid)

	
}

