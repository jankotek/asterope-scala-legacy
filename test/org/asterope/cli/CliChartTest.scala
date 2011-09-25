package org.asterope.cli

import java.io.File
import org.asterope.chart.{LightColors, ChartLayers}
import org.asterope.util._
import org.asterope.data.TestRecordManager

class CliChartTest extends ScalaTestCase {
	
	lazy val cli = new CliChart with TestRecordManager
	
	
	def split(s:String) = s.split(" ")

	def testCli{	
		new File("build/tmp").mkdirs
		val file = new File("build/tmp/cli-chart-1.png")
		file.delete;
		assert(!file.exists)
		cli.call(split("-f Asterope --width 800 --height 600 -fov 10d -o "+file))
		assert(file.exists && file.length>2000)
	}
	
	def testMirrorVert1{
		val (chart,star,ds,coord) = cli.call2(Array())
		assert(chart.xscale===1)
	}
	def testMirrorVert2{
		val (chart,star,ds,coord) = cli.call2(split("-mirrorVert true"))
		assert(chart.xscale=== -1)		
	}

	def testMirrorHoriz{
		val (chart,star,ds,coord) = cli.call2(split("-mirrorHoriz true"))
		assert(chart.yscale=== -1)		
	}

	def testRotate{
		val (chart,star,ds,coord) = cli.call2(split("-rotation 120"))
		assert(chart.rotation === 120.degree)		
	}

  def testCenter{
    val (chart,star,ds,coord) = cli.call2(split("-c 0d90d"))
    assert(chart.position === Vector3d.northPole)
  }

	
	def testj2000Grid{
		val (chart,star,ds,coord) = cli.call2(split("-j2000Grid false"))
		assert(coord.coordinateGridJ2000.showLines === false)		
	}

	def testj1950Grid{
		val (chart,star,ds,coord) = cli.call2(split("-j1950Grid true"))
		assert(coord.coordinateGridJ1950.showLines === true)		
	}
	
	def testEclipticGrid{
		val (chart,star,ds,coord) = cli.call2(split("-eclipticGrid true"))
		assert(coord.coordinateGridEcliptic.showLines === true)		
	}

	def testGalacticGrid{
		val (chart,star,ds,coord) = cli.call2(split("-galacticGrid true"))
		assert(coord.coordinateGridGalactic.showLines === true)		
	}

	def testEcliptic{
		val (chart,star,ds,coord) = cli.call2(split("-ecliptic false"))
		assert(coord.coordinateGridEcliptic.showEquator === false)		
	}

	def testGalacticEquator{
		val (chart,star,ds,coord) = cli.call2(split("-galacticEquator false"))
		assert(coord.coordinateGridGalactic.showEquator === false)		
	}

	def testStarDiscDiameter{
		val (chart,star,ds,coord) = cli.call2(split("-starDiscDiameter 3"))
		assert(star.starDiscMultiply === 3)		
	}

  /**
   * test if layer generated with given command line parameters is empty or not
   */
  def assertLayerEmpty(empty:Boolean,layer:ChartLayers.Value,params:String){
    val (chart,star,ds,coord) = cli.call2(split(params))
    val count = chart.getLayer(layer).getChildrenCount
    if(empty) assert(count ===0)
    else assert(count?>0)
  }

  def testShowLegend{
    assertLayerEmpty(false, ChartLayers.legend, "-fov 10")
    assertLayerEmpty(true, ChartLayers.legend, "-showLegend false -fov 20")
    assertLayerEmpty(false, ChartLayers.legend, "-showLegend true")
  }

  def testShowConsteLines{
    assertLayerEmpty(false, ChartLayers.constelLine, "-fov 20")
    assertLayerEmpty(true, ChartLayers.constelLine, "-showConstelLines false -fov 20")
    assertLayerEmpty(false, ChartLayers.constelLine, "-showConstelLines true")
  }

  def testShowConstelBounds{
    assertLayerEmpty(false, ChartLayers.constelBoundary, "-fov 20")
    assertLayerEmpty(true, ChartLayers.constelBoundary, "-showConstelBounds false -fov 20")
    assertLayerEmpty(false, ChartLayers.constelBoundary, "-showConstelBounds true")
  }

  def testInverse{
    val (chart,star,ds,coord) = cli.call2(split("-inverse true"))
    assert(chart.colors === LightColors)
  }



	//TODO test that command line fails if an unknown argument is passed (strict parsing)

}