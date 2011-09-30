package org.asterope.chart

import org.asterope.util._

class CoordinateGridTest extends ScalaTestCase{
	
	val empty = CoordinateGridConfig2(showLines = false,showPoles = false, showEquator =false);
	val lines = CoordinateGridConfig2(showLines = true,showPoles = false, showEquator =false);
	val poles = CoordinateGridConfig2(showLines = false,showPoles = true, showEquator =false);
	val equator = CoordinateGridConfig2(showLines = false,showPoles = false, showEquator =true);
	
	def testLayerNoGridJ2000{
		val c = new Chart()
		val config = CoordinateGridConfig(coordinateGridJ2000 = empty)
		assert(c.getLayer(Layer.coordinateGridJ2000).getChildrenCount === 0)
		CoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridJ2000).getChildrenCount === 0)
	}
	
	def testLayerGridLinesJ2000{
		val c = new Chart()
		val config = CoordinateGridConfig(coordinateGridJ2000 = lines)
		assert(c.getLayer(Layer.coordinateGridJ2000).getChildrenCount === 0)
		CoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridJ2000).getChildrenCount ?> 0)
	}

	def testLayerGridPolesJ2000{
		val c = new Chart(position = Vector3d.northPole)
		val config = CoordinateGridConfig(coordinateGridJ2000 = poles)
		assert(c.getLayer(Layer.coordinateGridJ2000).getChildrenCount === 0)
		CoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridJ2000).getChildrenCount ?> 0)
	}

	def testLayerGridPolesEquatorJ2000{
		val c = new Chart(position = Vector3d.zeroPoint)
		val config = CoordinateGridConfig(coordinateGridJ2000 = equator)
		assert(c.getLayer(Layer.coordinateGridJ2000).getChildrenCount === 0)
		CoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridJ2000).getChildrenCount ?> 0)
	}
	
	
	def testLayerNoGridJ1950{
		val c = new Chart()
		val config = CoordinateGridConfig(coordinateGridJ1950 = empty)
		assert(c.getLayer(Layer.coordinateGridJ1950).getChildrenCount === 0)
		CoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridJ1950).getChildrenCount === 0)
	}
	
	def testLayerGridLinesJ1950{
		val c = new Chart()
		val config = CoordinateGridConfig(coordinateGridJ1950 = lines)
		assert(c.getLayer(Layer.coordinateGridJ1950).getChildrenCount === 0)
		CoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridJ1950).getChildrenCount ?> 0)
	}

	def testLayerGridPolesJ1950{
		val c = new Chart(position = Vector3d.northPole)
		val config = CoordinateGridConfig(coordinateGridJ1950 = poles)
		assert(c.getLayer(Layer.coordinateGridJ1950).getChildrenCount === 0)
		CoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridJ1950).getChildrenCount ?> 0)
	}

	def testLayerGridPolesEquatorJ1950{
		val c = new Chart(position = Vector3d.zeroPoint)
		val config = CoordinateGridConfig(coordinateGridJ1950 = equator)
		assert(c.getLayer(Layer.coordinateGridJ1950).getChildrenCount === 0)
		CoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridJ1950).getChildrenCount ?> 0)
	}

	def testLayerNoGridGalactic{
		val c = new Chart()
		val config = CoordinateGridConfig(coordinateGridGalactic = empty)
		assert(c.getLayer(Layer.coordinateGridGalactic).getChildrenCount === 0)
		CoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridGalactic).getChildrenCount === 0)
	}
	
	def testLayerGridLinesGalactic{
		val c = new Chart()
		val config = CoordinateGridConfig(coordinateGridGalactic = lines)
		assert(c.getLayer(Layer.coordinateGridGalactic).getChildrenCount === 0)
		CoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridGalactic).getChildrenCount ?> 0)
	}

	def testLayerGridPolesGalactic{
		val c = new Chart(position = Vector3d.galaxyNorthPole)
		val config = CoordinateGridConfig(coordinateGridGalactic = poles)
		assert(c.getLayer(Layer.coordinateGridGalactic).getChildrenCount === 0)
		CoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridGalactic).getChildrenCount ?> 0)
	}

	def testLayerGridPolesEquatorGalactic{
		val c = new Chart(position = Vector3d.galaxyCentre)
		val config = CoordinateGridConfig(coordinateGridGalactic = equator)
		assert(c.getLayer(Layer.coordinateGridGalactic).getChildrenCount === 0)
		CoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridGalactic).getChildrenCount ?> 0)
	}


	def testLayerNoGridEcliptic{
		val c = new Chart()
		val config = CoordinateGridConfig(coordinateGridEcliptic = empty)
		assert(c.getLayer(Layer.coordinateGridEcliptic).getChildrenCount === 0)
		CoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridEcliptic).getChildrenCount === 0)
	}
	
	def testLayerGridLinesEcliptic{
		val c = new Chart()
		val config = CoordinateGridConfig(coordinateGridEcliptic = lines)
		assert(c.getLayer(Layer.coordinateGridEcliptic).getChildrenCount === 0)
		CoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridEcliptic).getChildrenCount ?> 0)
	}

	def testLayerGridPolesEcliptic{
		val c = new Chart(position = Vector3d.eclipticNorthPole)
		val config = CoordinateGridConfig(coordinateGridEcliptic = poles)
		assert(c.getLayer(Layer.coordinateGridEcliptic).getChildrenCount === 0)
		CoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridEcliptic).getChildrenCount ?> 0)
	}

	def testLayerGridPolesEquatorEcliptic{
		val sunPosition = Vector3d.rade2Vector(Angle.parseRa("12","55","41"), Angle.parseDe("-","5","57","38.6"));
		
		
		val c = new Chart(position = sunPosition)
		val config = CoordinateGridConfig(coordinateGridEcliptic = equator)
		assert(c.getLayer(Layer.coordinateGridEcliptic).getChildrenCount === 0)
		CoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridEcliptic).getChildrenCount ?> 0)
	}


  def test_should_display_all(){

  }


	
}