package org.asterope.chart

import java.awt.Color
import edu.umd.cs.piccolo.PNode
import java.awt.BasicStroke
import edu.umd.cs.piccolo.nodes.PPath
import org.asterope.data._
import org.asterope.util._
import java.awt.RadialGradientPaint
import edu.umd.cs.piccolo.util.PPaintContext
import java.awt.geom.{Area, Ellipse2D}

case class ChartMilkyWayConfig()

class ChartMilkyWay(dao:MilkyWayDao)
	extends ChartFeature[ChartMilkyWayConfig] with ChartPainter[ChartMilkyWayConfig, MilkyWayPixel]{
	
	def paintObject(chart:Chart, config:ChartMilkyWayConfig, pixel:MilkyWayPixel,addToLayer:Boolean):Option[PNode] = {
		val pos = chart.wcs.project(pixel.pos)
		if(pos.isEmpty) return None
		//calculate diameter size, 
		//NOTE: 1 degree is too much, but part of disc is transparent 
		val diameter = chart.angleSizeOnChart(pixel.pos, 1.0 degree)
		if(diameter.isEmpty) return None
		
		val n= new PPath(new Ellipse2D.Double(-diameter.get/2,-diameter.get/2,diameter.get,diameter.get)){
      override protected def paint(paintContext:PPaintContext) {
        //paint only when non interactive
        if(paintContext.getRenderQuality >= PPaintContext.HIGH_QUALITY_RENDERING)
          super.paint(paintContext)
      }
    };
    n.setGlobalTranslation(pos.get);

    val gray = 1.0* pixel.gray/255.0;
    val c1 = Colors.between(chart.colors.bg, chart.colors.milkyWay,gray );
    val c2 = Colors.setAlpha(c1, 0);
    n.setPaint(new RadialGradientPaint(0F,0F,diameter.get.toFloat/2F,Array[Float](0,1F),Array[Color](c1,c2)));
    n.setStroke(new BasicStroke(0));
    n.setStrokePaint(c2);
    if(addToLayer)
      	chart.addNode(Layer.milkyway, n, None, gray);
    Some(n)
	}

	def updateChart(chart: Chart, config:ChartMilkyWayConfig){
		val pixels = dao.milkyWayPixelsByArea(chart.area);
		paintAll(chart,config,pixels)
	}
	
	def defaultConfig = new ChartMilkyWayConfig()
	
	def clearChart(chart:Chart) {
		chart.getLayer(Layer.milkyway).removeAllChildren()
	}
}

case class ChartConstelLineConfig(stroke: BasicStrokeConfig = BasicStrokeConfig(width = 3 ), cutoutCircle:Int = 15)

class ChartConstelLine(dao:ConstelLineDao)
	extends ChartFeature[ChartConstelLineConfig] with ChartPainter[ChartConstelLineConfig, ConstelLine]{

	def paintObject(chart:Chart, config:ChartConstelLineConfig, line:ConstelLine,addToLayer:Boolean):Option[PNode] = {
		
		val p1 = chart.wcs.project(line.v1);
		val p2 = chart.wcs.project(line.v2);
		if(p1.isEmpty || p2.isEmpty) return None
		val projected = chart.projectLine(line.line)
		if(projected.isEmpty) return None
    val area = new Area(projected.get)
    //cutout ends so line does not connect directly to star
    def cutoutArea(x:Double, y:Double) = new Area(new Ellipse2D.Double(
          (x - config.cutoutCircle), (y - config.cutoutCircle),
          config.cutoutCircle*2, config.cutoutCircle*2))
    area.subtract(cutoutArea(p1.get.getX, p1.get.getY))
    area.subtract(cutoutArea(p2.get.getX, p2.get.getY))

		val node = new PPath(area)
	  node.setStroke(config.stroke.getStroke);
	  node.setStrokePaint(chart.colors.constelLine);
	  if(addToLayer)
	    	chart.addNode(Layer.constelLine, node)
		Some(node)
	}
	
	def updateChart(chart: Chart, config:ChartConstelLineConfig){
		val lines = dao.constellationLineByArea(chart.area);
		paintAll(chart,config,lines)
	}
	
	def defaultConfig = new ChartConstelLineConfig()
 
	
	def clearChart(chart:Chart) {
		chart.getLayer(Layer.constelLine).removeAllChildren()
	}

}

case class ChartConstelBoundaryConfig(stroke: BasicStrokeConfig = BasicStrokeConfig(width = 3,  dash = List(3,5)) )

		
class ChartConstelBoundary(dao: ConstelBoundaryDao)
	extends ChartFeature[ChartConstelBoundaryConfig] {
	

	def defaultConfig = new ChartConstelBoundaryConfig
	
	def clearChart(chart:Chart) {
		chart.getLayer(Layer.constelBoundary).removeAllChildren()
	}

	def updateChart(chart: Chart, config:ChartConstelBoundaryConfig){
		for{
      line <-dao.constelBoundsByArea(chart.area);
			projected <- chart.projectLine(line);
			node =  new PPath(projected)			
		}{
      checkInterrupted()
			node.setStroke(config.stroke.getStroke);
			node.setStrokePaint(chart.colors.constelBoundary);
	    	chart.addNode(Layer.constelBoundary, node)
		}		
	}

			
}
