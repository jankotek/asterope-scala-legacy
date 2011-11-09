package org.asterope.data

import java.lang.{Long => JLong}
import scala.collection.JavaConversions._
import org.asterope.util._
import xml.{XML, Node}
import org.asterope.healpix._
import org.asterope.util._
import java.io.File
import org.apache.commons.math.geometry.Vector3D

/**
 * An object which stores minimal information about deep sky object.
 * It is used to draw deep sky object on map without loading full StarlinkObject
 * which is slower to fetch.
 *  
 */
case class DeepSky(
	ra: Angle,
	de: Angle,
	mag: Option[Magnitude],
	sizeMax:Option[Angle],
	sizeMin:Option[Angle],
	posAngle:Option[Angle],
	deepSkyType:DeepSkyType.Value,
 	names:List[Nomenclature] = Nil
	) extends HasNomenclature {
	
	/** normalized vector with position calculated from Ra De */
	val vector = rade2Vector(ra,de)
	/** healpix ipix */
	val ipix = Pixelization.vector2Ipix(vector);
}


object DeepSkyType extends Enumeration{

  /** Asterism*/
  val ASTER = Value
  /**Bright Nebula*/
  val BRTNB = Value
  /**Cluster with Nebulosity*/
  val CL_NB = Value
  /**Dark Nebula*/
  val DRKNB = Value
  /** Galaxy cluster*/
  val GALCL = Value
  /**Galaxy */
  val GALXY = Value
  /** Globular Cluster*/
  val GLOCL = Value
  /** Diffuse Nebula in a Galaxy*/
  val GX_DN = Value
  /**Globular Cluster in a Galaxy*/
  val GX_GC = Value
  /**Cluster with Nebulosity in a Galaxy*/
  val G_C_N = Value
  /**Cluster with Nebulosity in the LMC*/
  val LMCCN = Value
  /**Diffuse Nebula in the LMC*/
  val LMCDN = Value
  /**Globular Cluster in the LMC*/
  val LMCGC = Value
  /**Open cluster in the LMC*/
  val LMCOC = Value
  /**Nonexistent*/
  val NONEX = Value
  /**Open Cluster*/
  val OPNCL = Value
  /**Planetary Nebula*/
  val PLNNB = Value
  /**Cluster with Nebulosity in the SMC*/
  val SMCCN = Value
  /** Diffuse Nebula in the SMC*/
  val SMCDN = Value
  /**Globular Cluster in the SMC*/
  val SMCGC = Value
  /**Open cluster in the SMC*/
  val SMCOC = Value
  /**Supernova Remnant*/
  val SNREM = Value
  /**Quasar*/
  val QUASR = Value

  val STAR1 = Value
  /**double star*/
  val STAR2 = Value
  /**triple star*/
  val STAR3 = Value
  /**quad star*/
  val STAR4 = Value
  /**8 stars*/
  val STAR8 = Value

}

case class DeepSkyOutline(id:String,  author:String, points:Vector[Vector3D]){

  points.foreach(_.assertNormalized())

  /** convert Outline to XML*/
  def toXml:Node = {
    <outline id={id}  author={author}>
      {
        points.map{v=>
          " "+(v.getRa.toDegree/15).toFloat+" "+v.getDe.toDegree.toFloat
        }
      }
    </outline>
  }
}

object DeepSky{

  lazy val resourceMap = new ResourceMap(classOf[DeepSky])

  /** create Outline from XML */
  def outlineFromXml(n:Node):DeepSkyOutline = {
    val t = n.text.trim.replaceAll("[ \\n\\r]+"," ").split(" ").iterator
    var points = new collection.immutable.VectorBuilder[Vector3D]()
    while(t.hasNext){
      val ra = t.next().toDouble
      val de = t.next().toDouble
      if(ra!= -1.0 && de!=0.0) //TODO seems like -1.0 & 0.0 starts new outline
        points += rade2Vector(
          ra * Angle.H2R, de * Angle.D2R)
    }

    new DeepSkyOutline(id=(n\"@id").text, author=(n\"@author").text, points=points.result )
  }
}



import jdbm._

/**
 * Data Acces Object bean which handless DeepSky. 
 * Most complicated task is fetching by area (healpix). 
 * 
 *
 */
class DeepSkyDao(val recman:RecordManager)  {

	/** serializer used for more efficient space usage */ 
	protected object serializer extends Serializer[DeepSky] {
	
		override def serialize(out:SerializerOutput, obj: DeepSky){
			//save as much space as possible, so angles are writen in MAS as Integers
			def writeOptAngle(a:Option[Angle]) = {
				val i = if(a.isEmpty) Integer.MIN_VALUE 
					else a.get.toMas.asInstanceOf[Int]
			    out.writeInt(i)
			}
			out.writeInt(obj.ra.toMas.asInstanceOf[Int])
			out.writeInt(obj.de.toMas.asInstanceOf[Int])
			//and magnitude is written in Short.
      if(obj.mag.isDefined) out.writeShort(obj.mag.get.toMilimag.toShort)
			else out.writeShort(Short.MaxValue);
			writeOptAngle(obj.sizeMax)
			writeOptAngle(obj.sizeMin)
			writeOptAngle(obj.posAngle)
			out.write(obj.deepSkyType.id)
      Nomenclature.listSerializer.serialize(out,obj.names)
		}
	
		override def deserialize(in:SerializerInput):DeepSky = {
				val ra = in.readInt.mas
				val de = in.readInt.mas
				val mag = in.readShort()
        val mag2 = if(mag==Short.MaxValue) None
                  else Some(Magnitude(mag/1000))
				def readOptAngle:Option[Angle] = {
					var i = in.readInt
					if(i == Integer.MIN_VALUE) None
					else Some(i.mas)
				}
				val maxAngle = readOptAngle
				val minAngle = readOptAngle
				val posAngle = readOptAngle
				val deepSkyType = DeepSkyType(in.read)
        val names = Nomenclature.listSerializer.deserialize(in)
				return new DeepSky(ra,de,mag2, maxAngle,minAngle,posAngle,deepSkyType,names);
		}
	}
	
	
	/**
	 * Main map where deep sky objects are stored
	 */
	val deepSkyMap:PrimaryStoreMap[JLong,DeepSky] = recman.storeMap("deepSkyMap", serializer)

	/**
	 * healpix index
	 */
	val deepSkyByArea:SecondaryTreeMap[JLong,JLong,DeepSky] = deepSkyMap.secondaryTreeMap("deepSkyByArea",
		new SecondaryKeyExtractor[JLong,JLong,DeepSky]{
			 override def  extractSecondaryKey(key:JLong, value:DeepSky) = value.ipix
		}
	)

   private object nameKeyExtractor extends jdbm.SecondaryKeyExtractor[java.lang.Iterable[Nomenclature],JLong,DeepSky]{
    import collection.JavaConversions._
    def extractSecondaryKey(l:JLong, ds:DeepSky) = ds.names
  }

  val deepSkyByName:SecondaryTreeMap[Nomenclature,JLong,DeepSky] = deepSkyMap.secondaryTreeMapManyToOne(
    "deepSkyByName",nameKeyExtractor, Nomenclature.serializer)

	
		
	
	
	def deepSkyByArea(area:LongRangeSet):Iterator[DeepSky] = {
     Pixelization.rangeSetToSeq(area)
       .map(f=>deepSkyByArea.subMap(f._1, f._2+1))
       .flatMap(_.values())
       .flatMap(_.iterator)
       .map(deepSkyByArea.getPrimaryValue(_))
       .iterator
	}
	
	def addDeepSky(deepSky:DeepSky){
		val deepSkyRecid = deepSkyMap.putValue(deepSky);
	}
	
	def all:Iterator[DeepSky] = deepSkyMap.valuesIterator


  def objectsByName(name:String):Iterator[DeepSky] = {
		recidsByName(name).map{deepSkyByName.getPrimaryValue(_)}
	}

  def recidsByName(name2:String):Iterator[JLong] = {
    Nomenclature.recidsByMame(name2,deepSkyByName)
	}

  var outlines: Map[String, DeepSkyOutline] =  loadOutlines();

  private def loadOutlines():Map[String,DeepSkyOutline] = {
    val xml = XML.loadFile(outlineFile)
    (xml\"outline")
      .map(DeepSky.outlineFromXml(_))
      .map(c=>(c.id,c))
    }.toMap


  /**
   *  Find contour for given deep sky object
   */
  def findOutline(id:String):Option[DeepSkyOutline] = outlines.get(id)

  def findOutline(ds:DeepSky):Option[DeepSkyOutline] = ds.names
      .map(_.toString)
      .flatMap(findOutline(_))
      .headOption

  private lazy val outlineFile = new File("data/nebula_outlines")

	
}
