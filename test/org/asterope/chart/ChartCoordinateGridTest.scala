package org.asterope.chart

import org.asterope.util._

class ChartCoordinateGridTest extends ScalaTestCase{
	
	val empty = ChartCoordinateGridConfig2(showLines = false,showPoles = false, showEquator =false);
	val lines = ChartCoordinateGridConfig2(showLines = true,showPoles = false, showEquator =false);
	val poles = ChartCoordinateGridConfig2(showLines = false,showPoles = true, showEquator =false);
	val equator = ChartCoordinateGridConfig2(showLines = false,showPoles = false, showEquator =true);
	
	def testLayerNoGridJ2000{
		val c = new ChartBase()
		val config = ChartCoordinateGridConfig(coordinateGridJ2000 = empty)
		assert(c.getLayer(Layer.coordinateGridJ2000).getChildrenCount === 0)
		ChartCoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridJ2000).getChildrenCount === 0)
	}
	
	def testLayerGridLinesJ2000{
		val c = new ChartBase()
		val config = ChartCoordinateGridConfig(coordinateGridJ2000 = lines)
		assert(c.getLayer(Layer.coordinateGridJ2000).getChildrenCount === 0)
		ChartCoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridJ2000).getChildrenCount ?> 0)
	}

	def testLayerGridPolesJ2000{
		val c = new ChartBase(position = Vector3d.northPole)
		val config = ChartCoordinateGridConfig(coordinateGridJ2000 = poles)
		assert(c.getLayer(Layer.coordinateGridJ2000).getChildrenCount === 0)
		ChartCoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridJ2000).getChildrenCount ?> 0)
	}

	def testLayerGridPolesEquatorJ2000{
		val c = new ChartBase(position = Vector3d.zeroPoint)
		val config = ChartCoordinateGridConfig(coordinateGridJ2000 = equator)
		assert(c.getLayer(Layer.coordinateGridJ2000).getChildrenCount === 0)
		ChartCoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridJ2000).getChildrenCount ?> 0)
	}
	
	
	def testLayerNoGridJ1950{
		val c = new ChartBase()
		val config = ChartCoordinateGridConfig(coordinateGridJ1950 = empty)
		assert(c.getLayer(Layer.coordinateGridJ1950).getChildrenCount === 0)
		ChartCoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridJ1950).getChildrenCount === 0)
	}
	
	def testLayerGridLinesJ1950{
		val c = new ChartBase()
		val config = ChartCoordinateGridConfig(coordinateGridJ1950 = lines)
		assert(c.getLayer(Layer.coordinateGridJ1950).getChildrenCount === 0)
		ChartCoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridJ1950).getChildrenCount ?> 0)
	}

	def testLayerGridPolesJ1950{
		val c = new ChartBase(position = Vector3d.northPole)
		val config = ChartCoordinateGridConfig(coordinateGridJ1950 = poles)
		assert(c.getLayer(Layer.coordinateGridJ1950).getChildrenCount === 0)
		ChartCoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridJ1950).getChildrenCount ?> 0)
	}

	def testLayerGridPolesEquatorJ1950{
		val c = new ChartBase(position = Vector3d.zeroPoint)
		val config = ChartCoordinateGridConfig(coordinateGridJ1950 = equator)
		assert(c.getLayer(Layer.coordinateGridJ1950).getChildrenCount === 0)
		ChartCoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridJ1950).getChildrenCount ?> 0)
	}

	def testLayerNoGridGalactic{
		val c = new ChartBase()
		val config = ChartCoordinateGridConfig(coordinateGridGalactic = empty)
		assert(c.getLayer(Layer.coordinateGridGalactic).getChildrenCount === 0)
		ChartCoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridGalactic).getChildrenCount === 0)
	}
	
	def testLayerGridLinesGalactic{
		val c = new ChartBase()
		val config = ChartCoordinateGridConfig(coordinateGridGalactic = lines)
		assert(c.getLayer(Layer.coordinateGridGalactic).getChildrenCount === 0)
		ChartCoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridGalactic).getChildrenCount ?> 0)
	}

	def testLayerGridPolesGalactic{
		val c = new ChartBase(position = Vector3d.galaxyNorthPole)
		val config = ChartCoordinateGridConfig(coordinateGridGalactic = poles)
		assert(c.getLayer(Layer.coordinateGridGalactic).getChildrenCount === 0)
		ChartCoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridGalactic).getChildrenCount ?> 0)
	}

	def testLayerGridPolesEquatorGalactic{
		val c = new ChartBase(position = Vector3d.galaxyCentre)
		val config = ChartCoordinateGridConfig(coordinateGridGalactic = equator)
		assert(c.getLayer(Layer.coordinateGridGalactic).getChildrenCount === 0)
		ChartCoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridGalactic).getChildrenCount ?> 0)
	}


	def testLayerNoGridEcliptic{
		val c = new ChartBase()
		val config = ChartCoordinateGridConfig(coordinateGridEcliptic = empty)
		assert(c.getLayer(Layer.coordinateGridEcliptic).getChildrenCount === 0)
		ChartCoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridEcliptic).getChildrenCount === 0)
	}
	
	def testLayerGridLinesEcliptic{
		val c = new ChartBase()
		val config = ChartCoordinateGridConfig(coordinateGridEcliptic = lines)
		assert(c.getLayer(Layer.coordinateGridEcliptic).getChildrenCount === 0)
		ChartCoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridEcliptic).getChildrenCount ?> 0)
	}

	def testLayerGridPolesEcliptic{
		val c = new ChartBase(position = Vector3d.eclipticNorthPole)
		val config = ChartCoordinateGridConfig(coordinateGridEcliptic = poles)
		assert(c.getLayer(Layer.coordinateGridEcliptic).getChildrenCount === 0)
		ChartCoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridEcliptic).getChildrenCount ?> 0)
	}

	def testLayerGridPolesEquatorEcliptic{
		val sunPosition = Vector3d.rade2Vector(Angle.parseRa("12","55","41"), Angle.parseDe("-","5","57","38.6"));
		
		
		val c = new ChartBase(position = sunPosition)
		val config = ChartCoordinateGridConfig(coordinateGridEcliptic = equator)
		assert(c.getLayer(Layer.coordinateGridEcliptic).getChildrenCount === 0)
		ChartCoordinateGrid.updateChart(c,config)
		assert(c.getLayer(Layer.coordinateGridEcliptic).getChildrenCount ?> 0)
	}


  def test_should_display_all(){

  }


	
}