package org.asterope.data

import org.asterope.healpix._
import org.asterope.util._
import org.apache.commons.math.geometry.Vector3D

class DatabaseTest extends BeansTestCase{


	def testEnoughtStars {
		assert(beans.liteStarDao.all.size ?> 290000)
	}
	
	def testInitializeAllStars{
		beans.liteStarDao.all.foreach{
			_.hashCode();
		}
	}
	
	def testContainsBrightStars {
		assert(beans.liteStarDao.objectsByName("Vega").hasNext)
		assert(beans.liteStarDao.objectsByName("Sirius").hasNext)
		assert(beans.liteStarDao.objectsByName("Mizar").hasNext)
		assert(beans.liteStarDao.objectsByName("Alcor").hasNext)
	}
	
	def testPrintStarMagDistribuion{
    val counts = scala.collection.mutable.Map[Int, Int]()
		beans.liteStarDao.all.foreach{s=>
			val mag = s.mag.mag.floor.asInstanceOf[Int]
			counts(mag) = counts.getOrElse(mag,0) +1
			
		}
		
		println("Star distribution by magnitude: ")
		counts.keySet.toList.sortWith(_<_).foreach{key=>
			println(" "+key+" mag ->	"+counts(key))
		}
		
	}
	
//	def testPrintNameDistributionByCatalog{
//		val counts = scala.collection.mutable.Map[String,Int]()
//		for(id<- starlinkDao.allNames){
//			val prefix = id.catalogPrefix
//      if(prefix == "NAME")
//        println(id.name)
//			counts(prefix) = counts.getOrElse(prefix,0) +1
//		}
//		println("Name distribution by prefix: ")
//		counts.keySet.
//			filter(counts(_)>30).
//			toList.sortWith(counts(_)>counts(_)).
//			foreach{key=>
//				println(" "+counts(key)+" ->	"+key)
//			}
//    assert(counts("NAME") <5000)
//	}


  def testBigHealpix = testHealpix(Vector3D.PLUS_I, 5.degree)
  def testSmallHealpix = testHealpix(Vector3D_asterope, 20.arcMinute)


  def testHealpix(point:Vector3D, angle:Angle){
    	val area = Pixelization.queryDisc(point,angle)
			val starsInDisc = beans.liteStarDao.starsByAreaMag(area, Magnitude(30)).toSet

			//iterate over all star and compare distance from point and if they are in disc
			for(star <-beans.liteStarDao.all){
				val distance = Vector3D.angle(star.vector,point)
				if(distance < angle.toRadian- Pixelization.resolution.toRadian*5) //FIXME there should not be multiplication in here
					assert(starsInDisc.contains(star), "not in set \n center: "+point+"\n radius:"+angle.toRadian
							+"\n starDistance:"+distance)
				if(distance> angle.toRadian + Pixelization.resolution.toRadian*5) //FIXME there should not be multiplication in this expression
						assert(!starsInDisc.contains(star), "in set \n center: "+point+"\n radius:"+angle.toRadian
							+"\n starDistance:"+distance)
			}
  }
	
	def testMilkyWay{
		val area = Pixelization.queryDisc(Vector3D_galaxyCentre,10.degree);
		val inArea = beans.milkyWayDao.milkyWayPixelsByArea(area).toList
		assert(inArea.size ?> 100)
		inArea.foreach{ p=>
			assert(Vector3D.angle(p.pos,Vector3D_galaxyCentre) ?< (11 * Angle.D2R))
		}
		
	}
	
	def testDeepSky{
		assert(beans.deepSkyDao.objectsByName("M13").hasNext)
		assert(beans.deepSkyDao.objectsByName("M31").hasNext)
    assert(Vector3D.angle(beans.deepSkyDao.objectsByName("M31").next.vector,Vector3D_m31).radian ?< 20.arcMinute)
		val area = Pixelization.queryDisc(Vector3D_m31,20.arcMinute)
		val galaxy = beans.deepSkyDao.deepSkyByArea(area).next
		assert(galaxy.mag.get.mag ?<5)
		assert(galaxy.sizeMax.get.toDegree?>1)
	}
	
	def testConstellationLine{
		assert(100?<beans.constelLineDao.all.size)
    val iter = beans.constelLineDao.constellationLineByArea(
      Pixelization.queryDisc(Vector3D.PLUS_K, 10.degree))
      .toList
		val count = iter.filter(_.constellation  == Constel.UMi.toString ).size
		assert(count?>=3)	
		assert(iter.size?>2)
		assert(iter.size?<10)
		
		beans.constelLineDao.all.foreach{l=>
			assert(Vector3D.angle(l.line.start,l.line.end).radian?<30.degree, l.toString)
		}
	}
	
	def testStarlink_Object_Search_Case_Insensitive{
		assert(beans.deepSkyDao.objectsByName("m13").hasNext)
		assert(beans.deepSkyDao.objectsByName("M13").hasNext)
		assert(beans.deepSkyDao.objectsByName("NgC7000").hasNext)
		assert(beans.deepSkyDao.objectsByName("ngc7000").hasNext)
	}
	
	def testIsBinaryStar{
		//test if Polaris is marked as binary star
		val polaris = beans.liteStarDao.objectsByName("HIP11767").next
		assert(polaris.posAngle.isDefined)
	}

}
