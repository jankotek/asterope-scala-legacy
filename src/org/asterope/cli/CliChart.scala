package org.asterope.cli

import org.asterope.chart._
import org.asterope.util._



/**
 * An command line feature which handles map drawing
 * 
 * @author Jan Kotek
 *
 */
class CliChart extends CliFeature with ChartBeans{

	
	override val description = "Paints sky charts";
	override val keyword ="chart";
	
	override def call(args:Array[String]):Unit = call2(args)
	
	/** class which does actuall work.
	 * Returns some config values are used for rade2Vector tests
	 */
	protected[cli] def call2(args:Array[String]) ={
		
		var chart = new ChartBase
		var output = "chart.png"
		var find = "Asterope"
    var center:Vector3d = null
		var starsConfig = stars.defaultConfig
		var deepSkyConfig = deepSky.defaultConfig
		var coordinateGridConfig = coordinateGrid.defaultConfig
    var showConstelBounds = true
    var showConstelLines = true
    var showLegend = true
    var inverse = false
		
		val parser = new OptionParser("asterope chart") {
			opt("o", "output", "<file>", "save chart to file, supported formats: png, jpg, svg, pdf", 
					{v => output = v})
			opt("f", "find", "<object>", "center chart on this object",
					{v=> find = v})
            opt("c", "center", "RaDe", "center chart on this position",
                    {v=> val pos = Angle.parseRaDe(v); center = Vector3d.rade2Vector(pos._1,pos._2)})

			doubleOpt("fov", "fieldOfView", "Field of view in degrees", 
					{v => chart = chart.copy(fieldOfView = v.degree)})
			intOpt("w", "width", "chart width in pixels [800]", 
					{v => chart=chart.copy(width=v)})
			intOpt("h", "height", "chart height in pixels [600]", 
					{v => chart=chart.copy(height=v)})
			opt("p", "projection", "set projection, default Sin ", //TODO add more values 
					{v => chart=chart.copy(projection=v)})
			doubleOpt("m", "limitStarMag", "Faintest star magnitude to be displayed, is calculated by default", 
					{v => 
						starsConfig = starsConfig.copy(limitStarMagForce = Some(Magnitude(v)))
					})
			booleanOpt("mirrorVert","mirrorVert","Flip map vertically",
					{ v=> chart = chart.copy(xscale = if(v)-1 else 1)})
			booleanOpt("mirrorHoriz","mirrorHoriz","Flip map horizontaly",
					{ v=> chart = chart.copy(yscale = if(v)-1 else 1)})
			doubleOpt("rotation", "rotation", "Rotate chart by given angle in degrees", 
					{v => chart = chart.copy(rotation = v.degree)})

			booleanOpt("j2000Grid","j2000Grid","show J2000 coordinate grid",
					{ v=> 		
						coordinateGridConfig = coordinateGridConfig
						.copy(coordinateGridJ2000 = coordinateGridConfig.coordinateGridJ2000
							.copy(showLines = v))})
			booleanOpt("j1950Grid","j1950Grid","show J1950 coordinate grid",
					{ v=> 		
						coordinateGridConfig = coordinateGridConfig
						.copy(coordinateGridJ1950 = coordinateGridConfig.coordinateGridJ1950
							.copy(showLines = v))})
			booleanOpt("eclipticGrid","eclipticGrid","show Ecliptic coordinate grid",
					{ v=> 		
						coordinateGridConfig = coordinateGridConfig
						.copy(coordinateGridEcliptic = coordinateGridConfig.coordinateGridEcliptic
							.copy(showLines = v))})
			booleanOpt("galacticGrid","galacticGrid","show Galactic coordinate grid",
					{ v=> 		
						coordinateGridConfig = coordinateGridConfig
						.copy(coordinateGridGalactic = coordinateGridConfig.coordinateGridGalactic
							.copy(showLines = v))})

			booleanOpt("ecliptic","ecliptic","show Ecliptic",
					{ v=> 		
						coordinateGridConfig = coordinateGridConfig
						.copy(coordinateGridEcliptic = coordinateGridConfig.coordinateGridEcliptic
							.copy(showEquator = v))})
			booleanOpt("galacticEquator","galacticEquator","show Galactic equator",
					{ v=> 		
						coordinateGridConfig = coordinateGridConfig
						.copy(coordinateGridGalactic = coordinateGridConfig.coordinateGridGalactic
							.copy(showEquator = v))})

			doubleOpt("starDiscDiameter", "starDiscDiameter", "Set coeficient used to calculate star disc diameter, higher means bigger star disc", 
				{v => starsConfig = starsConfig.copy(starDiscMultiply = v)})

			booleanOpt("bigObjectsTransparent","bigObjectsTransparent","Make big Deep Sky objects transparent",
				{ v=> deepSkyConfig = deepSkyConfig.copy( bigObjectsTransparent = v)})
				
			booleanOpt("showGlobularCluster","showGlobularCluster","Show Globular Clusters on chart",
				{ v=> deepSkyConfig = deepSkyConfig.copy( showGlobularCluster = v)})
			booleanOpt("showOpenCluster","showGlobularCluster","Show Open Clusters on chart",
				{ v=> deepSkyConfig = deepSkyConfig.copy( showOpenCluster = v)})
			booleanOpt("showGalaxy","showGalaxy","Show Galaxies on chart",
				{ v=> deepSkyConfig = deepSkyConfig.copy( showGalaxy = v)})
			booleanOpt("showBrightNebula","showBrightNebula","Show Bright Nebula on chart",
				{ v=> deepSkyConfig = deepSkyConfig.copy( showBrightNebula = v)})
			booleanOpt("showPlanetaryNebula","showPlanetaryNebula","Show Planetary Nebula on chart",
				{ v=> deepSkyConfig = deepSkyConfig.copy( showPlanetaryNebula = v)})
			booleanOpt("showDarkNebula","showDarkNebula","Show Dark Nebula on chart",
				{ v=> deepSkyConfig = deepSkyConfig.copy( showDarkNebula = v)})
			booleanOpt("showSupernovaRemnant","showSupernovaRemnant","Show SupernovaRemnant on chart",
				{ v=> deepSkyConfig = deepSkyConfig.copy( showSupernovaRemnant = v)})


      booleanOpt("showConstelBounds","showConstelBounds","Show or hide constellation boundary lines",
        { v=> showConstelBounds = v})
      booleanOpt("showConstelLines","showConstelLines","Show or hide lines connecting bright stars in constellation.",
        { v=> showConstelLines = v})
      booleanOpt("showLegend","showLegend","Show or hide chart legend at bottom of map.",
        { v=> showLegend = v})
      booleanOpt("inverse","inverse","Show dark colors on white background, good for printing.",
        { v=> inverse = v})


				
			
		}
		if (parser.parse(args)) {
      val pos =
            if(center!=null) center
            else{
    			    //find central object
              val ds = deepSkyDao.objectsByName(find);
              lazy val s = liteStarDao.objectsByName(find);
              if(ds.hasNext) ds.next.vector
              else if(s.hasNext) s.next.vector
              else throw new Error("Central object not found: "+find)
            }


			chart = chart.copy(position = pos,
        legendHeight = if(showLegend)legendBorder.height else 0
      )
      if(showLegend)
        chart = chart.copy(height = chart.height-chart.legendHeight)

      if(inverse)
        chart = chart.copy(colors = LightColors)

			
      stars.updateChart(chart, starsConfig)
      deepSky.updateChart(chart,deepSkyConfig)
      milkyWay.updateChart(chart)

      coordinateGrid.updateChart(chart,coordinateGridConfig)
      if(showLegend) legendBorder.updateChart(chart)
      if(showConstelLines) constelLine.updateChart(chart)
      if(showConstelBounds) constelBoundary.updateChart(chart)


      labels.updateChart(chart)
	  chart.saveToImageFile(new java.io.File(output))
			
		}else {
			// arguments are bad, usage message will have been displayed
			
		}
		(chart,starsConfig,deepSkyConfig,coordinateGridConfig)
	}
	

}