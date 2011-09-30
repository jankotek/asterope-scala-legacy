package org.asterope.chart

import edu.umd.cs.piccolo.PNode
import java.awt.BasicStroke
import java.awt.Font
import edu.umd.cs.piccolox.nodes.PLine
import java.awt.geom.Rectangle2D
import math._

import org.asterope.data._
import org.asterope.util._
import edu.umd.cs.piccolo.nodes.PText


case class ChartLegendBorderConfig(
	starConfig : ChartStarsConfig = new ChartStarsConfig,
	deepSkyConfig:ChartDeepSkyConfig = new ChartDeepSkyConfig,
	pageName:String = null
)

/**
 * Paints border around chart with legend. 
 */
class ChartLegendBorder (stars:ChartStars, deepSky:ChartDeepSky) 
	extends ChartFeature[ChartLegendBorderConfig]{

  val height = 80
  
	def defaultConfig = new ChartLegendBorderConfig
	
	def clearChart(chart: ChartBase) {
		chart.getLayer(Layer.legend).removeAllChildren
	}
	
	/**
	 * Update chart with this feature. 
	 * If this feature provides stars, it should paint them in this method
	 * 
	 * This method must wait until update is finished.
	 *  
	 * @param chart to paint on
	 * @param config stores configuration for this action
	 */
	def updateChart(chart: ChartBase, config:ChartLegendBorderConfig){
		//paint line around canvas
		val line = new PLine();
    val ps = 1.0
		line.setStrokePaint(chart.colors.fg)
		line.setStroke(new BasicStroke(2))
		line.addPoint(0,ps,ps)
		line.addPoint(1,chart.width-ps,ps)
		line.addPoint(2,chart.width-ps,chart.height+chart.legendHeight-ps)
		line.addPoint(3,ps,chart.height+chart.legendHeight-ps)
		line.addPoint(4,ps,ps)		
		
		if(chart.legendHeight>0){
			val line2 = new PLine();
			line2.setStrokePaint(chart.colors.fg)
			line2.setStroke(new BasicStroke(ps.toFloat  * 2))
			line2.addPoint(0,0,chart.height)
			line2.addPoint(0,chart.width,chart.height)
			line.addChild(line2)
		}


		val legendNode = new PNode()
    legendNode.setBounds(new Rectangle2D.Double(0,0,chart.width,chart.legendHeight))
		legendNode.setOffset(new Point2d(0,chart.height+0))
		legendNode.setPaint(chart.colors.bg)

		//add 'created by' note
		val createdBy = new PText(" Generated with Asterope, http://github.com/jankotek/asterope")
		createdBy.setFont(new Font("SansSerif", Font.PLAIN, 9))
		createdBy.setTextPaint(ChartColors.setAlpha(chart.colors.fg,96))
    createdBy.setOffset(3, chart.legendHeight-3 - createdBy.getHeight)
		legendNode.addChild(createdBy)

    var pos = 10.0

    def append(node:PNode, l:String){
      val text = new PText(l)
      val w = math.max(node.getWidth,text.getWidth)
      node.centerFullBoundsOnPoint(pos + w/2, chart.legendHeight/3)
      legendNode.addChild(node)
      text.setTextPaint(chart.colors.fg)
      text.centerFullBoundsOnPoint(pos + w/2, chart.legendHeight * 2/3)
      legendNode.addChild(text)
      pos += w + 15
    }



		//paint star legend
		val numberOfStars = 8
		val limitStarMag = stars.calculateLimitStarMag(chart,config.starConfig)

		for(i <- 0 until numberOfStars){			
			def magRound(d:Double) = Magnitude(d -d%0.5) //fce used to round magnitude to 0.5
			val mag = magRound(0 + i * limitStarMag.mag / numberOfStars)

			val star = new LiteStar(chart.position.getRa,chart.position.getDe,mag)
			
			val node = stars.paintObject(chart,config.starConfig,star,addToLayer=false).get

      append(node,mag.mag.toString)
		}


    //put space between stars and deepsky
    pos+=30

		val angle = chart.pixelAngularSize  * 20
		val ds = new DeepSky(ra=chart.position.getRa,de=chart.position.getDe,
				mag=Some(Magnitude(8)), sizeMax=Some(angle), sizeMin=Some(angle/2),posAngle = Some(70.degree),
				deepSkyType = null)
		//galaxy legend		
		deepSky.paintGalaxy(chart,config.deepSkyConfig,ds).foreach{g=>
      append(g,"Galaxy")
		}
		deepSky.paintOpenCluster(chart,config.deepSkyConfig,ds).foreach{g=>
      append(g,"Open c.")
		}

		deepSky.paintGlobularCluster(chart,config.deepSkyConfig,ds).foreach{g=>
      append(g,"Glob. c.")
		}

		def smallerNeb = ds.copy(sizeMax = Some(angle/5), sizeMin = None)
		deepSky.paintPlanetaryNebula(chart,config.deepSkyConfig,smallerNeb).foreach{g=>
      append(g,"Planetary")
		}

		deepSky.paintBrightNebula(chart,config.deepSkyConfig,ds).foreach{g=>
      append(g,"Bright")
		}

		deepSky.paintDarkNebula(chart,config.deepSkyConfig,ds).foreach{g=>
      append(g,"Dark neb.")
		}


		//add page numbering
		if(config.pageName!=null){
		    val pageNumber = new PText(config.pageName+"  ")
		    pageNumber.setFont(new Font(Font.SANS_SERIF,Font.BOLD,40))
		    pageNumber.setTextPaint(chart.colors.fg)
        pageNumber.setOffset(chart.width - pageNumber.getWidth,0)
		    legendNode.addChild(pageNumber)
		    
		}
		



		chart.executor.async{ //can not use `addNode`, legend is outside of canvas
      val l = chart.getLayer(Layer.legend);
      l.addChild(legendNode)
      l.addChild(line)
    }
		
	}

}