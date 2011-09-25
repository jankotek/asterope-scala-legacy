package org.asterope.data

import org.asterope.util._

import org.asterope.healpix._
import collection.immutable.TreeSet

/**
 * Represents line on sky. 
 * Line is defined by iteration over its points with given step (accuracy). 
 * Lower step means less points and faster iteration. 
 * Bigger step means more points and slower iteration.
 * 
 */
abstract class SkyLine {
	
	/** 
	 * Iteration over line points. 
	 * ZeroToOne argument can have value from 0 to 1, returned vector is at corresponding 
	 * line position from start (0) to end (1)
	 */
	def skyLineIteration(zeroToOne:Double):Vector3d
	
	/** @return first point of line, corresponds to <code>skyLineIteration(0)</code> **/
	def start:Vector3d = skyLineIteration(0d);
	/** @return last point of line, corresponds to <code>skyLineIteration(1)</code> **/
	def end:Vector3d = skyLineIteration(1d);
	/** @return center point of line, corresponds to <code>skyLineIteration(0.5)</code> **/
	def center:Vector3d = skyLineIteration(0.5d);
	
	/**
	 * Most friendly way to iterate over line points. 
	 * In iterator it returns all points contained in line. Maximal distance between two points
	 * is limited by given maximal step (accuracy). Smaller step means more points will be returned. 	 
	 */
	def skyLineIterator(accuracy:Angle):Iterator[Vector3d] = {				
		val step:Double = accuracy.toRadian/length.toRadian;				
		return new Iterator[Vector3d]{			
			var value:Double = -step;
			def hasNext:Boolean = (value <=1d - step)
			def next():Vector3d={
				value+=step
				if(value>1d)throw new NoSuchElementException();
				return skyLineIteration(value);				
			}
		}
	}
	
	/**
	 * Calculate lenght of line.  
	 * Default implementation is iterating over points and does not provide exact result.
	 * Subclasses should override this if better method exist.
	 */
	def length():Angle = {
			var ret:Double = 0d;
			val step = 1/100d;
			var pos = 0d;
			var oldvect = skyLineIteration(pos);
			while(pos<=1d-step){
				pos+=step
				val vect = skyLineIteration(pos)
				ret+=oldvect.angle(vect);
				oldvect = vect
			}			
			assert(ret>0,"line have zero length");
			return ret.radian
	}
	
	/** @return true if line is closed shape, ie if start == end */
	def isClosed() = start.angle(end)<1e-10;
		
	/** Calculate healpix area occupied by this line  */
	def calculateArea():LongRangeSet = {
		var set = new TreeSet[Long]();
		val res = Pixelization.resolution
		skyLineIterator(res).foreach{v=>
			val ipix = Pixelization.vector2Ipix(v)
			set+=ipix
		}
		return Pixelization.treeSetToRangeSet(set)
	}
		
}

/** 
 * Line defined by rotating vector around axis by given angle. 
 * It is defined by three params: starting point, rotation axis and rotation angle. 
 * 
 * This class can represend declination lines (rotationAxis is north pole) or
 * RA lines (rotationAxis is cross product of start and end)
 */
@SerialVersionUID(-1604918798770235512L)
case class RotatingSkyLine(startPoint:Vector3d, rotationAxis: Vector3d, rotationAngle:Angle) 
	extends SkyLine{
	startPoint.assertNormalized
	rotationAxis.assertNormalized
	assert(rotationAngle.toDegree>=0 && rotationAngle.toDegree<=360 , 
			"angle should be between 0 and 360 degrees: "+rotationAngle)
  assert(startPoint.angle(rotationAxis)!=0, "start point and rot axis are equal")
  assert(startPoint.angle(rotationAxis)!=math.Pi, "start point and rot axis are 180 degrees apart")
	
	override def skyLineIteration(zeroToOne:Double): Vector3d ={  
		startPoint.rotateVector(rotationAxis, rotationAngle.toRadian * zeroToOne);
}
	override def start = startPoint;
	
	override def length = rotationAngle * math.sin(startPoint.angle(rotationAxis))
}

/**
 * Line defined as great circle (shortest path on sphere) between two points. 
 * It is defined by two points.
 */
@SerialVersionUID(6841049333663254920L)
case class TwoPointSkyLine(v1:Vector3d, v2:Vector3d) extends SkyLine{
	private var angle = v1.angle(v2)
	assert(angle<math.Pi, "angle can not be 180 degrees")
	assert(angle>1e-7, "points are equal")
	v1.assertNormalized
	v2.assertNormalized
	//rotation axis which have 90degree angle with both vectors
	private val rotAxis = v1.cross(v2).normalized
	//swap rotation direction if needed
	if(skyLineIteration(0.1).angle(v2)>angle)
		angle = -angle

	override def skyLineIteration(zeroToOne:Double): Vector3d = 
		v1.rotateVector(rotAxis, angle * zeroToOne);
		
	override def start = v1
	override def end = v2
	override def length = v1.angle(v2).radian
}
