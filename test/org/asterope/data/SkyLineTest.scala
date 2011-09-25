package org.asterope.data

import org.asterope.healpix._
import org.asterope.util._

class SkyLineTest extends ScalaTestCase{
	
	val equator = new RotatingSkyLine(Vector3d.zeroPoint, Vector3d.northPole, 360.degree)

	def testLength{
		assert(equator.length.toDegree ~==360)
	}
	
	def testIteration{
		val iter = equator.skyLineIterator(1.00 degree)
		var oldVect = iter.next;
		assert(oldVect === Vector3d.zeroPoint, " did not start on 0");
		var counter =0;
		while(iter.hasNext){
			counter+=1;
			val vect = iter.next
			val angle = oldVect.angle(vect)
			assert(angle>Angle.D2R * 0.5," too small angle "+angle)
			assert(angle<Angle.D2R * 2," too big angle "+angle)
			oldVect = vect
		}

		assert(counter>300 && counter<1000, "wrong counter:"+counter);	
	}
	
	def testCalculateArea{
		val rangeSet = equator.calculateArea();
		assert(rangeSet.rangeCount < 4, "equator should be one line");
		var counter = 0;
		equator.skyLineIterator(0.01 degree).foreach{ v=>
			counter+=1;
			val ipix = Pixelization.vector2Ipix(v)
			assert(rangeSet.contains(ipix))
		}
		assert(counter>30000,"wrong counter:"+counter)
	}
	
	def testTwoPointSkyLineOrientation{
		val v1 = Vector3d(1,0,0);
		val v2 = Vector3d(0,0,1);
		val maxAngle = math.Pi/4  + 0.01
		val line1 = new TwoPointSkyLine(v1,v2);		
		val middle1 = line1.skyLineIteration(0.5);
		assert(middle1.angle(v1)<maxAngle)
		assert(middle1.angle(v2)<maxAngle)
		assert(v2.angle(line1.end)<1e-3)
		
		val line2 = new TwoPointSkyLine(v2,v1);		
		val middle2 = line1.skyLineIteration(0.5);
		assert(middle2.angle(v1)<maxAngle)
		assert(middle2.angle(v2)<maxAngle)
		assert(v1~=line2.end)
	}
	
}
