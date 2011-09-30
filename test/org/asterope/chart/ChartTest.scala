package org.asterope.chart

import org.asterope.data.LiteStar
import scala.collection.JavaConversions._
import edu.umd.cs.piccolo.nodes.PText
import edu.umd.cs.piccolo.PNode
import org.asterope.util._

class ChartTest extends ScalaTestCase{
	
	val chart = new Chart();

	def testAddToCamera{
		val node = new PNode()
		chart.camera.addChild(node);
		assert(chart.camera.getChildrenIterator.contains(node))
	}
	
	def testPoint2d{
		val p = new org.asterope.chart.Point2d(1,1)
		new PNode().setGlobalTranslation(p)
	}
	
	def testProjectionCenter{
		val p:Point2d = chart.wcs.project(chart.position).get
		val center = Point2d(chart.width/2, chart.height/2)
		assert(p.distance(center)~==0)
	}
	
	def testProjectionInverseOverField{
		def center = chart.position 
		def axis1 = Vector3d.northPole
		def axis2 = center.cross(axis1) //90 degrees to both vectors
		//iterate 10 degrees in both direction
		for(a1 <- -10 to 10; a2 <- -10 to 10;
			v:Vector3d = center.rotateVector(axis1,a1.degree).rotateVector(axis2,a2.degree)){
			assert(v.angle(center).radian<20.degree)
			val inverse:Vector3d = chart.wcs.deproject(chart.wcs.project(v).get).get
			
			assert(v.angle(inverse).radian ?< 1.arcSec, "failed for: "+a1+" and "+a2 )
			
		}
	}
//	def testCreateLayer() {
//		intercept[Throwable]{		
//			chart.getLayer("XXX");
//			fail("should throw exception, layer does not exist");
//		}
//		val xxx = chart.getOrCreateLayer("XXX");
//		assert(chart.camera .getLayersReference.contains(xxx))
//		assert(xxx !== null);
//		assert(chart.getLayer("XXX") === xxx);
//	}

  def testZorderLayer(){    
    val layer = chart.getOrCreateLayer(Layer.star);

    //fill with random data
    for(iii <- 1 to 100){
      val rand = math.random;
      val node = new PText(rand.toString);
      chart.addNode(Layer.star, node, rand.toString, rand)
    }

    var last:Double = -1000;
    layer.getChildrenIterator.foreach{ it=>
      val p = it.asInstanceOf[PText];
      val curr = p.getText.toDouble;
      assert(curr?>last);
      last = curr;
    }
  }
  
  def testAngleSizeOnChart{
	  val chart = new Chart(position = Vector3d.zeroPoint,
	 		  fieldOfView = 10.degree, width = 10, height=10)
	  val pos1 = Vector3d.rade2Vector(3.degree, 3.degree)
	  val size = chart.angleSizeOnChart(pos1, 1.degree).get
	  assert(size?>1.2)
	  assert(size?<1.6)
  }
  
  def testLimitMag{
	  def testFovMag(fov:Angle, min:Double, max:Double){
	 	  val chart = new Chart(fieldOfView = fov, width = 800, height = 600);
	 	  val stars = new Stars(null)
	 	  val m =  stars.calculateLimitStarMag(chart,stars.defaultConfig).mag
	 	  assert(m?>= min)
	 	  assert(m?<= max)
	  } 
	   
	  testFovMag(60.degree,5,7)
	  testFovMag(30.degree,6,8)
	  testFovMag(15.degree,8,10)
	  testFovMag(7.degree,9,11)
	  testFovMag(4.degree,11,13)
	  testFovMag(2.degree,13,15)	  
	  testFovMag(1.degree,15,17)
	  
  }
  
  def testStarDiscSize{
	  val stars = new Stars(null);
	  val chart = new Chart();
	  val config1 = new StarsConfig(starDiscMultiply = 1)
	  val config2 = new StarsConfig(starDiscMultiply = 2)
	  val ra = chart.position.getRaRadian.radian
	  val de = chart.position.getDeRadian.radian
	  val star1 = new LiteStar(ra,de, Magnitude(1))
	  val star2 = new LiteStar(ra,de, Magnitude(2))
	   
	  val config = stars.defaultConfig
	  
	  val n11 = stars.paintObject(chart,config1,star1).get
	  val n12 = stars.paintObject(chart,config1,star2).get
	  val n21 = stars.paintObject(chart,config2,star1).get
	  val n22 = stars.paintObject(chart,config2,star2).get
	  
	  //chart 2 should produce bigger stars, multiply is higher
	  assert(n11.getWidth ?< n21.getWidth)
	  assert(n12.getWidth ?< n22.getWidth)
	  
	  //star 1 should be bigger, is brighter
	  assert(n11.getWidth ?> n12.getWidth)
	  assert(n21.getWidth ?> n22.getWidth)

  }

  def testResources{
    assert("Equator" === Chart.resMap.getString("j2000Equator"));
    assert("Ecliptic" === Chart.resMap.getString("eclipticEquator"));
  }


}