package org.asterope.data

import org.asterope.healpix._
import scala.collection.JavaConversions._
import java.lang.{Long => JLong}
import org.apache.commons.math.geometry.Vector3D


/**
 * Represents line connecting two bright stars in constellation.
 * Stars are identified by Hipparcos id, but position is also stored.  
 * 
 * @author Jan Kotek
 */
@SerialVersionUID(6274119050738877926L)
case class ConstelLine(
		 v1:Vector3D,  v2:Vector3D,
		hipparcos1:Int, hipparcos2:Int, constellation: String,
		lineWidth:Int //TODO current data from stellarium do not contain line width
		) {
	val line = TwoPointSkyLine(v1,v2)


}

import jdbm._

/** an DAO for ConstelLine*/
class ConstelLineDao(recman: RecordManager){
	
	protected val constellationLines:PrimaryStoreMap[JLong,ConstelLine] =
		recman.storeMap("constelationLines")

	
	/** index to query lines by area */	
	protected val constellationLinesAreaIndex: PrimaryTreeMap[JLong, LongRangeSet] = 
		recman.treeMap("constelationLinesAreaIndex")
		
	/** bind two map together, so index is automaticaly updated */	
	constellationLines.addRecordListener(new RecordListener[JLong,ConstelLine]{
		override def recordInserted(key:JLong, value:ConstelLine) =
			constellationLinesAreaIndex.put(key, value.line.calculateArea)
		
		override def recordUpdated(key:JLong, oldValue:ConstelLine, newValue:ConstelLine) = {
				constellationLinesAreaIndex.remove(key)
				constellationLinesAreaIndex.put(key, newValue.line.calculateArea)
		}
		
		override def recordRemoved(key:JLong, value:ConstelLine) =
				constellationLinesAreaIndex.remove(key)
	})
	
	/** add constellation line to dao*/
	def add(line:ConstelLine) = constellationLines.putValue(line)
	
	/** @return all constellation lines */
	def all = constellationLines.valuesIterator
	
	/** query constellation lines by area */
	def constellationLineByArea(area:LongRangeSet):Iterator[ConstelLine] = {
			//iterate over all areas and find out if they overlap with area
			constellationLinesAreaIndex.filter{case(k,v)=>
				v.containsAny(area.rangeIterator)
			}.map(v=>constellationLines.get(v._1)).iterator
	}
}


/** an DAO for constelation bondary lines*/
class ConstelBoundaryDao(recman: RecordManager){
	protected val constelBounds:PrimaryStoreMap[JLong,SkyLine] =
		recman.storeMap("constelationBoundary")


	/** index to query lines by area */
	protected val constellationBoundaryAreaIndex: PrimaryTreeMap[JLong, LongRangeSet] =
		recman.treeMap("constellationBoundaryAreaIndex")

	/** bind two map together, so index is automaticaly updated */
	constelBounds.addRecordListener(new RecordListener[JLong,SkyLine]{
		override def recordInserted(key:JLong, value:SkyLine) =
			constellationBoundaryAreaIndex.put(key, value.calculateArea)

		override def recordUpdated(key:JLong, oldValue:SkyLine, newValue:SkyLine) = {
				constellationBoundaryAreaIndex.remove(key)
				constellationBoundaryAreaIndex.put(key, newValue.calculateArea)
		}

		override def recordRemoved(key:JLong, value:SkyLine) =
				constellationBoundaryAreaIndex.remove(key)
	})

	/** add constellation line to dao*/
	def add(line:SkyLine) = constelBounds.putValue(line)

	/** @return all constellation lines */
	def all = constelBounds.valuesIterator

	/** query constellation lines by area */
	def constelBoundsByArea(area:LongRangeSet):Iterator[SkyLine] = {
		//iterate over all areas and find out if they overlap with area
		constellationBoundaryAreaIndex.filter{case(k,v)=>
				v.containsAny(area.rangeIterator)
    }.map(v=>constelBounds.get(v._1)).iterator
	}
}