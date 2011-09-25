package org.asterope.data

import org.asterope.healpix._
import org.asterope.util._
import scala.collection.JavaConversions._

case class MilkyWayPixel
	(pos:Vector3d, gray:Int){		
	
	def ipix = Pixelization.vector2Ipix(pos)
}

import jdbm._

class MilkyWayDao(recman:RecordManager){
	import java.lang.Long
	
	/** serializer used for more efficient space usage */ 
	protected object serializer extends Serializer[List[MilkyWayPixel]] {
	
		override def serialize(out:SerializerOutput, obj: List[MilkyWayPixel]){
			//save as much space as possible, so angles are writen in MAS as Integers
      out.write(obj.size)
      obj.foreach{pix=>
			  out.writeDouble(pix.pos.x)
			  out.writeDouble(pix.pos.y)
			  out.writeDouble(pix.pos.z)
			  out.writeInt(pix.gray)
      }
		}
	
		override def deserialize(in:SerializerInput):List[MilkyWayPixel] = {
        val size = in.read()
        val pixels = for{
          i<-0 until size;
          pos = Vector3d(in.readDouble, in.readDouble, in.readDouble);
          gray = in.readInt
        } yield new MilkyWayPixel(pos,gray)
        pixels.toList
		}
	}	
	
	protected val milkyWayPixelMap:PrimaryTreeMap[Long,List[MilkyWayPixel]] = recman.treeMap("milkyWayPixelMap",serializer)
	
	def addMilkyWayPixel(pixel:MilkyWayPixel){
		val ipix:Long = pixel.ipix
    val list:List[MilkyWayPixel] =  pixel :: milkyWayPixelMap.getOrElse(ipix,Nil)
		milkyWayPixelMap.put(ipix, list)
	}
	
	def milkyWayPixelsByArea(area:LongRangeSet):Iterator[MilkyWayPixel] = {
     Pixelization.rangeSetToSeq(area)
       .map(f=>milkyWayPixelMap.subMap(f._1, f._2+1))
       .flatMap(_.values())
       .flatMap(_.iterator)
       .iterator
	}
	
	
	def all:Iterator[MilkyWayPixel] = milkyWayPixelMap.valuesIterator.flatMap(_.iterator)
}