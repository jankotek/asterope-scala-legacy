package org.asterope.chart

import edu.umd.cs.piccolo.PCamera
import edu.umd.cs.piccolo.PCanvas
import edu.umd.cs.piccolo.PNode
import edu.umd.cs.piccolo.util.PBounds
import java.awt.geom.Rectangle2D
import java.awt.geom.Path2D

import org.asterope.healpix._
import org.asterope.data._
import org.asterope.util._
import org.asterope.geometry._

import java.awt.image.BufferedImage

import collection.mutable.{Map => MMap}
import org.apache.commons.math.geometry.{Rotation, Vector3D}


/**
 * Base class for Sky Charting 
 * 
 * @author Jan Kotek
 */
case class Chart(
	position:Vector3D = Vector3D_asterope,
	fieldOfView:Angle = 10.degree,
	projection:String = "Sin", //TODO change projection to enum
	rotation:Angle = 0.degree,
	xscale:Double = 1, yscale:Double = 1,
	width:Int = 800, height:Int = 600,
	legendHeight:Int = 0,
	colors:Colors = DarkBlueColors,

	/** 
	 * Executor is responsible for executing any code which may modify chart.
	 * If chart is printed on screen, it executes code in Swing Event Dispatch Thread.
	 * In headless mode it just executes code in synchronized methods
	 */
	executor:ChartExecutor = new ChartExecutor
	){
	
	lazy val camera:PCamera = {
		val c = new PCamera()
		//little hack to make sure node hierarchy is initialized
		new edu.umd.cs.piccolo.PRoot().addChild(c);
		//set size and reset view transform
		c.setWidth(width);
		c.setHeight(height + legendHeight);
		c.setViewBounds(new PBounds(0,0,width, height+ legendHeight));
		c.setPaint(colors.bg)


    //create default layers
    Layer.values.foreach{l=>
      val layer = new Layer(l);
      c.addLayer(layer)
      layers.put(l,layer)
    }

		c
	}
		
	
	
  /** World Coordinate System used to project from sphere to flat canvas */
	val  wcs:WCS = {
		val builder = new WCSBuilder
		builder.width  = width
		builder.height  = height
		builder.xscale = xscale
		builder.yscale = - yscale
		builder.rotation = rotation.toRadian
		builder.projection = projection
		builder.refRa = position.getRaRadian
		builder.refDe = position.getDeRadian
		builder.setPixelScaleFromFOV(fieldOfView);
		builder.build
	}
	
	
	/** Inversion used for deprojection */
	protected val wcsInverse = wcs.inverse
	
	lazy val area = 
	  if(fieldOfView>160.degree) LongRangeSetBuilder.EMPTY.complement
	  else Pixelization.queryDisc(position,fieldOfView)

  /** angular size of one pixel in center of map deprojected back to sphere */
	lazy val pixelAngularSize:Angle = {
		val p1 = new Point2d(width/2, height/2)
		val p2 = p1.copy(x = p1.x+1);
		Vector3D.angle(wcs.deproject(p1).get,wcs.deproject(p2).get).radian
	}

  def projectLine(line:SkyLine):Option[Path2D] = {
	  val path = new Path2D.Float();
	  val bounds = new PBounds(0,0,width,height)
	  var oldPos: Option[Point2d] = None;
	  for(vect <-line.skyLineIterator(pixelAngularSize*3); //TODO distance for line should be adaptive
	      pos = wcs.project(vect)
	      if(pos.isDefined && bounds.contains(pos.get))
	   ){
	 	  //is inside map?
          if (oldPos.isDefined && oldPos.get.distance(pos.get)<10)
        	//yes paint line
            path.lineTo(pos.get.getX, pos.get.getY);
          else
        	// no just move it here
            path.moveTo(pos.get.getX, pos.get.getY);
          oldPos = pos;

	  }

	  Some(path);
  }


   
  /**
   * Test if given vector can be projected into canvas
   */
  def isInsideCanvas(p:Vector3D):Boolean = {
    val pos = wcs.project(p);
    pos.isDefined &&
            pos.get.x >=0 && pos.get.x <= width &&
            pos.get.y >=0 && pos.get.y <= height
  }
  
  /**
   * Test if given rectangle is inside or intersects with canvas
   */
  def isInsideCanvas(p:Rectangle2D):Boolean ={
    val bounds = new PBounds(0,0,width,height)
	  bounds.contains(p) || bounds.intersects(p)
  }
	  
  /**
   * Test if given node is inside or intersects with canvas
   */
  def isInsideCanvas(p:PNode):Boolean = {
    val bounds = new PBounds(0,0,width,height)
	  bounds.contains(p.getGlobalBounds) || bounds.intersects(p.getGlobalBounds)
  }



  
  /**
   * Map of layer names and layers 
   */
  protected val layers = MMap[Layer.Value, Layer]();

  /**
   * Return layer by name. New layer is created if 
   * layer with given name is not found.
   * 
   *  @return layer with given name, existing or newly created
   */
  def getOrCreateLayer(layerName:Layer.Value):Layer = {
    //make sure default layers are initialized
    camera

	  if(!layers.contains(layerName)){
		  val layer = new Layer(layerName)
		  camera.addLayer(layer)
		  layers.put(layerName,layer)

	  }
	  layers(layerName)
  }
  
  /**
   * Get layer with given name. If layer does not exist, exception is thrown
   */
  def getLayer(layerName:Layer.Value):Layer = {
    //make sure default layers are initialized
    camera
    layers(layerName)
  }
  


  /**
   * Paint part of map camera to buffered image
   * @param x image image
   * @param y image image
   * @param w image width
   * @param h image height
   * @return BufferedImage 
   */
  def toBufferedImage(x:Int, y: Int, w: Int, h: Int):BufferedImage = synchronized {
	 executor.sync{
		 //TODO this should be possible without changing camera view bounds
		 val oldBounds = camera.getBounds
		 camera.setBounds(new PBounds(x,y,w,h));
		 val img = camera.toImage.asInstanceOf[BufferedImage];
		 camera.setBounds(oldBounds);
		 assert(img.getWidth == w && img.getHeight == h)
		 img
	 }
		 
  }
  
  /**
   * Paint content of map to `BufferedImage`
   * @return
   */
  def toBufferedImage: BufferedImage = {
	  executor.sync{
		  camera.repaint();
		  return camera.toImage.asInstanceOf[BufferedImage]
	  }
  }	

   /**
    * Save chart to image file. All Batik formats are supported (pdf, svg, png..)
    * Image format is taken from file extension.
    * @param file 
    */
  def saveToImageFile(file: java.io.File) = ChartUtils.saveChartToImageFile(this, file)

  protected val object2Node = MMap[Any,PNode]()
  
  def objects:Iterator[Any] = object2Node.keys.iterator
  
  
  /**
   * Adds an graphical node to chart.  
   * 
   * @param layerName where node will be added
   * @param node graphical node
   * @param obj object represented by node, may be `None`
   * @param zorder in which node should be added to layer
   */
  def addNode(layer: Layer.Value, node: PNode, obj: Any = None, zorder: Double = 0, async:Boolean = true) {
	  def add2{
  	 	 	if(!isInsideCanvas(node)) return
	 	  	object2Node.put(obj,node)
	 	  	getLayer(layer).addChildWithZorder(node, zorder);
	  }
	  if(async) executor.async{
	 	  add2
	  } else executor.sync{
	 	  add2
	  }
	  
  }
  
  def getNodeForObject(obj:Any):Option[PNode] = object2Node.get(obj)
  def getObjectForNode(node:PNode):Option[Any] = object2Node.find(n=>n._2 ==node).map(_._1)
  
  def angleSizeOnChart(pos:Vector3D, angle:Angle):Option[Double] = {
	  val p1 = wcs.project(pos);
	  if(p1.isEmpty) return None
	  //rotate vector by angle in direction to/from centre
	  val rotAxis = Vector3D.crossProduct(position,pos);
    val rot = new Rotation(rotAxis,angle.toRadian);
	  val pos2 = rot.applyTo(pos);
	  val p2 = wcs.project(pos2)
	  if(p2.isEmpty) return None
	  Some(p1.get.distance(p2.get))
  }
  
  /**
   * Debug method, opens new window and shows chart in it. 
   */
  def showInWindow(){
	  val canvas = new PCanvas();
	  canvas.setCamera(camera)
	  canvas.setPreferredSize(new java.awt.Dimension(width.toInt, height.toInt))
	  val dialog = new javax.swing.JDialog();
	  dialog.setContentPane(canvas)	 
	  dialog.pack()
	  dialog.setModal(true)
	  dialog.setVisible(true) 
  }
  
  def saveChartToImageFile(file:java.io.File){
    ChartUtils.saveChartToImageFile(this,file)
  }

}

/**
 * Various static data related to map
 */
object Chart{
  val resMap = new ResourceMap(classOf[Chart])

}

class ChartExecutor{
	def async(block: =>Unit){
    synchronized{
      block
    }
  }
	def sync[E](block: =>E):E = {
    synchronized{
      block
    }
  }
}
