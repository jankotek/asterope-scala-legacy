package org.asterope.chart

import org.asterope.geometry._
import org.asterope.util._
import java.awt.BasicStroke
import math._
import org.asterope.data.{TwoPointSkyLine, RotatingSkyLine}
import edu.umd.cs.piccolo.nodes.PPath

/**
 * Provides configuration for ChartCoordinateGrid. 
 * This class is used in 'BaseChart', each system have its own class
 */
case class ChartCoordinateGridConfig2(
	showLines:Boolean,
	numberOfLines:Int = 3,
	showPoles:Boolean = true,
	showEquator:Boolean = true,
  equatorStroke:BasicStrokeConfig = new BasicStrokeConfig()
	
)

case class ChartCoordinateGridConfig(
	coordinateGridJ2000:ChartCoordinateGridConfig2 = ChartCoordinateGridConfig2(
    showLines = true, showPoles = true, showEquator = false),
	coordinateGridJ1950:ChartCoordinateGridConfig2 = ChartCoordinateGridConfig2(
    showLines = false, showPoles = false, showEquator = false),
	coordinateGridGalactic:ChartCoordinateGridConfig2 = ChartCoordinateGridConfig2(
    showLines = false, showPoles = true, showEquator = true,
    equatorStroke = new BasicStrokeConfig(dash = List(5,5))),
	coordinateGridEcliptic:ChartCoordinateGridConfig2 = ChartCoordinateGridConfig2(
    showLines = false, showPoles = true, showEquator = true,
    equatorStroke = new BasicStrokeConfig(dash = List(5,5)))
)


/**
 * Paints coordinate grids on chart
 *
 * @author Jan Kotek
 */
class ChartCoordinateGrid extends ChartFeature[ChartCoordinateGridConfig]{
	
	/**coordinate steps*/
	protected val deSteps = List[Angle](90.degree, 45.degree, 30.degree, 15.degree, 10.degree, 5.degree, 2.degree, 1.degree, 
			30.arcMinute, 15.arcMinute, 10.arcMinute, 5 .arcMinute, 2.arcMinute);

	protected val raSteps = {
		val hour = 15.degree
		val minute = hour/60
		val second = minute/60
		List[Angle](hour * 6, hour * 3, hour * 2, hour * 1, minute * 30, minute * 15, minute * 10, minute * 5,
				minute * 2, minute * 1, second * 30, second * 15)
	}
			


	protected val j1950Rotater = CoordinateSystem.factory("J1950").getRotater
	protected val j1950Derotater = j1950Rotater.inverse
	protected val galacticRotater = CoordinateSystem.factory("Galactic").getRotater
	protected val galacticDerotater = galacticRotater.inverse
	protected val eclipticRotater = CoordinateSystem.factory("Ecliptic").getRotater
	protected val eclipticDerotater = eclipticRotater.inverse
	
	def defaultConfig = new ChartCoordinateGridConfig()
	
	def updateChart(chart: ChartBase, config:ChartCoordinateGridConfig){

	   paintLines(chart,Layer.coordinateGridJ2000,config.coordinateGridJ2000,None,None,"j2000")
	   paintLines(chart,Layer.coordinateGridJ1950,config.coordinateGridJ1950,Some(j1950Rotater),Some(j1950Derotater),"j1950")
	   paintLines(chart,Layer.coordinateGridGalactic,config.coordinateGridGalactic,Some(galacticRotater),Some(galacticDerotater),"galactic")
	   paintLines(chart,Layer.coordinateGridEcliptic,config.coordinateGridEcliptic,Some(eclipticRotater),Some(eclipticDerotater),"ecliptic")
	   
	   if(config.coordinateGridJ2000.showPoles)
	  	   paintPoles(chart,Layer.coordinateGridJ2000,config.coordinateGridJ2000,None,None);
	   if(config.coordinateGridJ1950.showPoles)
	  	   paintPoles(chart,Layer.coordinateGridJ1950,config.coordinateGridJ1950,Some(j1950Rotater),Some(j1950Derotater))
	   if(config.coordinateGridGalactic.showPoles)
	  	   paintPoles(chart, Layer.coordinateGridGalactic,config.coordinateGridGalactic,Some(galacticRotater),Some(galacticDerotater))
	   if(config.coordinateGridEcliptic.showPoles)
	  	   paintPoles(chart,Layer.coordinateGridEcliptic,config.coordinateGridEcliptic,Some(eclipticRotater),Some(eclipticDerotater))
	}
	
	/**
	 * Converts position from one coordinate system to other using given rotater/derotater 
	 */
	protected def transform(pos:Vector3d, tater:Option[Rotater]):Vector3d = {
		if(tater == None)
			return pos
		val out = tater.get.transform(pos.toArray);
		new Vector3d(out);
	}

  /**In same cases all coordinate lines should be painted.
   * There are two rules: if FOV is bigger than 60 degrees.
   * Or if there are null points (outside of sphere) on chart.
   */
  protected def shouldDisplayAllLines(chart:ChartBase):Boolean = {
    if(chart.fieldOfView > 30.degree) return true
    for(x <- Range(0, chart.width, chart.width/10);
       y <-  Range(0, chart.width ,chart.width/10)){
      val projected = chart.wcs.deproject(x,y)
      //if point can not be deprojected to sphere, it is null point
      if(projected.isEmpty) return true;
    }
    false

  }

	protected def paintLines(chart:ChartBase, layer:Layer.Value, config:ChartCoordinateGridConfig2,
                           rotater:Option[Rotater],derotater:Option[Rotater],
                           labelPrefix:String){

    val showAll = shouldDisplayAllLines(chart)
    val transformedPosition = transform(chart.position,rotater)
    val deNumberOfLines = config.numberOfLines;
    val deStep = deSteps.find(_< chart.fieldOfView /deNumberOfLines).getOrElse{
      return
    }

    val raNumberOfLines = max(2.0,deNumberOfLines * chart.width/chart.height);
    val raStep = raSteps.find(_< chart.fieldOfView / ( raNumberOfLines * cos(transformedPosition.getDeRadian))).getOrElse{
      return
    }

    def roundTo(r:Angle,to:Angle) = Angle(r.uas -r.uas%to.uas)
    val lineStroke = new BasicStroke(1);

    //draw DE lines
    val deStart = {
       val v1 = (-90).degree + deStep
       val v2 = roundTo(transformedPosition.getDeRadian.radian-chart.fieldOfView,deStep)
       if(showAll || v1>v2) v1 else v2 //make sure that lines do not start to close to south pole
    }

	  val deStop = {
	 	  val v1 = 90.degree - deStep
	 	  val v2 = roundTo(transformedPosition.getDeRadian.radian+chart.fieldOfView,deStep)
	 	  if(showAll || v1<v2) v1 else v2 //make sure that lines do not start to close to north pole
	  }

    val fullRa:Boolean = showAll ||
      chart.position.getDe >80.degree - chart.fieldOfView*2 ||
      chart.position.getDe <80.degree + chart.fieldOfView*2

    val raStart =
      if(fullRa) 0.degree
      else roundTo(chart.position.getRa - chart.fieldOfView *2 / cos(chart.position.getDeRadian),raStep)
    val raStop =
      if(fullRa) 360.degree
      else roundTo(chart.position.getRa + chart.fieldOfView *2 / cos(chart.position.getDeRadian),raStep)


    for(
      de <- deStart.to(deStop,deStep);
      if(config.showLines || (config.showEquator && de == 0.degree));
      start = Vector3d.rade2Vector(0.degree,de);
      line = RotatingSkyLine(
        transform(start,derotater),
        transform(Vector3d.southPole,derotater),
        raStop - raStart
      );
      projected <- chart.projectLine(line);
      path = new PPath(projected)
    ){
      checkInterrupted()

      val label = "  " + (//special label for equator
        if(config.showEquator && de == 0.degree) " "+ChartBase.resMap.getString(labelPrefix+"Equator")
        else Angle.deToString(de.toRadian)
      )
      assert(label!=null);

      val stroke =  //on equator use stroke from config
        if(config.showEquator && de == 0.degree) config.equatorStroke.getStroke
        else lineStroke

      val text = new PPath(ChartUtils.textAlongPath(path.getPathReference,label))
      text.setPaint(chart.colors.gridLabel)
      text.setStroke(null)
      path.addChild(text)

      path.setStroke(stroke);
      path.setStrokePaint(chart.colors.gridColor)

      chart.addNode(layer,path)
    }


    if(!config.showLines) return

    //paint RA lines

    for(
      ra <-raStart.to(raStop-1.arcSec,raStep);
      point1 = Vector3d.rade2Vector(ra,deStart);
      point2 = Vector3d.rade2Vector(ra,deStop);
      line = new TwoPointSkyLine(transform(point1,derotater), transform(point2,derotater));
      projected <- chart.projectLine(line);
      path = new PPath(projected)
    ){
      checkInterrupted()
      val label = "  " + Angle.raToString(Angle.normalizeRa(ra.toRadian));
      val text = new PPath(ChartUtils.textAlongPath(path.getPathReference,label))
      text.setPaint(chart.colors.gridLabel)
      text.setStroke(null)
      path.addChild(text)

      path.setStroke(lineStroke)
      path.setStrokePaint(chart.colors.gridColor)
      chart.addNode(layer, path)

    }

	}
	
	protected def paintPoles(chart:ChartBase, layer:Layer.Value, config:ChartCoordinateGridConfig2, rotater:Option[Rotater],derotater:Option[Rotater]){
		def node = {
				val n= new PPath(new java.awt.geom.Ellipse2D.Double(-1,-1,1,1));
				n.setStroke(new BasicStroke(1));
				n.setStrokePaint(chart.colors.gridColor)
				n
		}
		val south = transform(Vector3d.southPole,derotater)
		if(chart.isInsideCanvas(south)){
			val pos = chart.wcs.project(south).get
			val n = node
			//TODO set label for south pole
			n.setGlobalTranslation(pos);
			chart.addNode(layer,n)
		}
			
		val north = transform(Vector3d.northPole,derotater)
		if(chart.isInsideCanvas(north)){
			val pos = chart.wcs.project(north).get
			val n = node
			n.setGlobalTranslation(pos);
			//TODO set label for north pole
			chart.addNode(layer,n)
		}
	}
	
	def clearChart(chart: ChartBase){
		List(Layer.coordinateGridJ2000,Layer.coordinateGridJ1950,
				Layer.coordinateGridEcliptic,Layer.coordinateGridGalactic).foreach{
			chart.getLayer(_).removeAllChildren
		}
	}


}

