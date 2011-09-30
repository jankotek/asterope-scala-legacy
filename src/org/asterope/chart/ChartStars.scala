package org.asterope.chart

import edu.umd.cs.piccolo.PNode
import java.awt.geom.Ellipse2D
import java.awt.BasicStroke
import edu.umd.cs.piccolo.nodes.PPath
import java.awt.geom.Path2D
import java.awt.Shape
import org.asterope.data._
import org.asterope.util._
import scala.math._

case class ChartStarsConfig(
	limitStarMagDelta: Double = -3,
	limitStarMagPower: Double = 1.3,
	limitStarMagForce:Option[Magnitude] = None, //if set, this magnitude is used instead of calculated one
	starDiscMultiply:Double = 1,
	doubleStarMinSize:Double = 1/100,
	doubleStarMagDif:Double = -3,
	showVariable:Boolean = true
)

/**
 * An class responsible for painting stars into Chart.  
 * 
 * @author Jan Kotek
 */
class ChartStars(
		dao: LiteStarDao)
					   extends ChartFeature[ChartStarsConfig] 
	                   with ChartPainter[ChartStarsConfig, LiteStar]{
	
	def defaultConfig = new ChartStarsConfig()
	
	def calculateLimitStarMag(chart:ChartBase, config:ChartStarsConfig):Magnitude = {
		config.limitStarMagForce.getOrElse{
			val pogson = chart.pixelAngularSize.toRadian;
			//convert pogson magnitude into real one
			var mag = abs(Magnitude.pogson2mag(pogson)) - 5;
			mag = max(mag,1)
			mag = 6 + pow(mag, config.limitStarMagPower) + config.limitStarMagDelta;
			//round to 0.1
			mag = round(mag*10D) / 10D
			new Magnitude(mag);
		}

	}
	
	/**
	 * Paint star into chart node. 
	 * 
	 * @param chart
	 * @param star
	 * @return node which represents star, or `None` if star disk can not be projected to map
	 */
	def paintObject(chart:ChartBase, config:ChartStarsConfig, star:LiteStar,addToLayer:Boolean):Option[PNode] = {
		val limitStarMag = calculateLimitStarMag(chart,config)
		val diameter = (limitStarMag.mag  - star.mag.mag) * config.starDiscMultiply
		val pos = chart.wcs.project(star.ra, star.de)
		val strokeWidth = math.min(1,diameter.toDouble/10)
		if(pos.isEmpty || diameter<1e-6) return None//nothing to paint
		var s1:Shape = new Ellipse2D.Double(-diameter / 2, -diameter/ 2, diameter, diameter);
		
		  //paint double star decoration if needed
	    if(star.separation.isDefined &&
	    	star.mag.mag < limitStarMag.mag + config.doubleStarMagDif){

	    	if(star.posAngle.isDefined && star.separation.isDefined &&
           star.separation.get > chart.fieldOfView * config.doubleStarMinSize){
	    		//TODO calculate position angle based on map orientation, dont just expect north points up
	    		val xa = +strokeWidth 
	    		val xb =  -strokeWidth
	    		val y = diameter	    		 
	    		
	    		//rotate by position angle
	    		val angle = star.posAngle.get.toRadian;
	    		val xa2 = xa * cos(angle) - y * sin(angle)
	    		val xb2 = xb * cos(angle) - y * sin(angle)
	    		val ya2 = y * cos(angle) + xa * sin(angle)
	    		val yb2 = y * cos(angle) + xb * sin(angle)
	    		//paint line	    		
	    		val line = new Path2D.Double();
	    		line.moveTo(xa2,ya2)
	    		line.lineTo(-xb2,-yb2)
	    		line.lineTo(-xa2,-ya2)
	    		line.lineTo(xb2,yb2)	    			    		 	
	    		line.closePath()
	    		//add line to stellar disc
	    		s1 = ChartUtils.shapeAdd(line,s1)
	    		
	    	}
	    }

		
	    
		val n = new PPath(s1,null)
		n.setGlobalTranslation(pos.get);
	    n.setPaint(chart.colors.star);
	    try{
	    	n.setStroke( new BasicStroke(strokeWidth.toFloat));
	    }catch{
	    	//Linux OpenJDK have problems painting stroke around some stars, 
	    	//this is kind of workaround, until OpenJDK is fixed or better workaround is find.
	    	//NOTE: this problem is somehow related to binary star line decoration
	    	case e:java.awt.geom.IllegalPathStateException => 
	    		Log.warning("!!Error in OpenJDK!!, not fatal, but star drawn without stroke"/*, e*/)
	    		//exception stack trace is not logged, as it makes hard to programm on linux	    	
	    }
	    n.setStrokePaint(chart.colors.bg);
	    
	    //if is variable star, add black circle decoration
	    if(config.showVariable && star.minMag.isDefined &&
	    		star.mag.mag +3< limitStarMag.mag){ //TODO hardcoded limit not to show variables by brightness

	    	val minMag = star.minMag.get
	    	val maxMag = star.maxMag.get
	    	if(minMag!=null && maxMag!=null && abs(maxMag.mag - minMag.mag)>0.5){
	    		//add dark circle to node
	    		val c1Dia = diameter -  2.6
	    		if(c1Dia>0){
	    			val c1 = new Ellipse2D.Double(-c1Dia / 2 , -c1Dia/ 2 , c1Dia, c1Dia);
 			   		val c2Dia = c1Dia  -  2.6
 			   		//cut central area of circle
 			   		val c2 = new Ellipse2D.Double(-c2Dia / 2 , -c2Dia/ 2 , c2Dia, c2Dia);
 			   		val res = if(c2Dia> 4) new PPath(ChartUtils.shapeSubtract(c1,c2));
 			   			else new PPath(c1)
 			   		res.setPaint(chart.colors.bg)
 			   		res.setStroke(null)
 			   		res.setStrokePaint(null)
 			   		n.addChild(res)
	    		}
	    	}
	    }

	    if(addToLayer)
	    	chart.addNode(Layer.star,n,star,star.mag.mag)
	    	   
	    Some(n)
	}
	
	
	def updateChart(chart: ChartBase, config:ChartStarsConfig){
		val limitStarMag = calculateLimitStarMag(chart,config)
		Log.info("Refresh with limit star mag: "+limitStarMag)
		val stars = dao.starsByAreaMag(chart.area, limitStarMag)
		paintAll(chart, config, stars)
	}
	
	def clearChart(chart: ChartBase){
		chart.getLayer(Layer.star).removeAllChildren()
	}
	

}