package org.asterope.chart

import org.asterope.data._
import org.asterope.util._

class ChartStarsTest extends ScalaTestCase {

	lazy val chart = new ChartBase()
	lazy val chartStars = new ChartStars(null)
	
	lazy val star = new LiteStar(mag = Magnitude(1.0),
				ra= chart.position.getRaRadian.radian, de= chart.position.getDeRadian.radian)

	
	def testStarDiameterBigger{
		val s1 = star
		val s2 = new LiteStar(mag = Magnitude(2.0),
				ra= chart.position.getRaRadian.radian, de= chart.position.getDeRadian.radian)
		val config = chartStars.defaultConfig
		val node1 = chartStars.paintObject(chart,config,s1).get
		val node2 = chartStars.paintObject(chart,config,s2).get				
		assert(node1.getWidth ?> node2.getWidth)		
	}
	
	def testCenterColor{
		val config = chartStars.defaultConfig
		//test if there is white pixel at position of star
		val node = chartStars.paintObject(chart, config, star).get
		chart.addNode(Layer.star, node, star, 0)
		val img = chart.toBufferedImage
	
		assert(chart.colors.star.getRGB() ===
			img.getRGB(chart.width.toInt/2, chart.height.toInt/2))
		assert(chart.colors.bg.getRGB() ===
			img.getRGB(chart.width.toInt/2+10, chart.height.toInt/2+10))		
	}
	
	def testNoncenterColor{
		val pos = Point2d(100,100)
		val starPos = chart.wcs.deproject(pos).get
		//test if there is white pixel at position of star
		val star2 = new LiteStar(mag = Magnitude(1.0),
				ra= starPos.getRaRadian.radian, de= starPos.getDeRadian.radian)
		val config = chartStars.defaultConfig
		val node = chartStars.paintObject(chart, config, star2).get
		chart.addNode(Layer.star, node, star2, 0)
		val img = chart.toBufferedImage
	
		assert(chart.colors.star.getRGB() ===
			img.getRGB(pos.x.toInt, pos.y.toInt))
		assert(chart.colors.bg.getRGB() ===
			img.getRGB(pos.x.toInt+10, pos.y.toInt+10))		
	}
	
	
}