package org.asterope.chart

import scala.collection.JavaConversions._
import edu.umd.cs.piccolo.PNode
import javax.imageio.ImageIO
import java.io._
import edu.umd.cs.piccolo.util.PPaintContext
import org.apache.batik.dom.GenericDOMImplementation
import org.apache.batik.svggen._
import org.apache.batik.util.SVGConstants
import edu.umd.cs.piccolo.nodes.{PText, PPath}
import java.awt.font.{GlyphVector, FontRenderContext}
import scala.List
import java.awt.geom._
import java.awt._

object ChartUtils {

  val supportedImageTypes = List("svg", "png")

  /**
   * Exports chart into image file. This methods uses Batik to convert to SVG or PNG
   */
  def saveChartToImageFile(chart: Chart, file: File) {
    val ext = file.getName.substring(file.getName.lastIndexOf('.') + 1).toLowerCase;
    //first check if we have vector based transcoder
    if (ext == "svg") {
    	//repaint chart into SVG canvas
   	    // Get a DOMImplementation and create an XML document
    	val domImpl = GenericDOMImplementation.getDOMImplementation();
    	val document = domImpl.createDocument(null, "svg", null);
    	val ctx = SVGGeneratorContext.createDefault(document);
    	//Batik does not know about our custom Round gradient paint
    	ctx.setExtensionHandler(new GradientExtensionHandler());
    	// Create an instance of the SVG Generator
    	val g2 = new SVGGraphics2D(ctx,false);
    	g2.setSVGCanvasSize(new Dimension( chart.width.toInt, (chart.height + chart.legendHeight).toInt))

    	// draw the chart in the SVG generator
    	chart.camera.fullPaint(new PPaintContext(g2))

      val out = new OutputStreamWriter(new FileOutputStream(file));
    	g2.stream(out)
    	out.close

    } else if (ext == "png") { 
    	//NO, format is not vector based, try exporting to raster
      val image = chart.toBufferedImage
      ImageIO.write(image, ext, file); 
    } else {
    	//unknown format, report error
      throw new IOException("Unknown format extension: " + ext)
    }
  }
  
  /** generates shape which contains all points from both shapes (+ operation) */
  def shapeAdd(s1:Shape,s2:Shape):Shape ={
	  val a1 = new Area()
	  a1.add(new Area(s1))
	  a1.add(new Area(s2))
	  a1
  }
  
  /** generates shape which contains points from s1, but not any from s2 */
  def shapeSubtract(s1:Shape,s2:Shape):Shape ={
	  val a1 = new Area(s1)
	  a1.subtract(new Area(s2))
	  a1
  }

  
  /** iterate over node and all its children */
  def allNodes(node:PNode):List[PNode] = {
		  var ret = node::Nil
		  node.getChildrenReference.foreach{n=>
		 	  ret =  allNodes(n.asInstanceOf[PNode]) ::: ret  
		  }
		  ret
  }


  /**
   * Creates text aligned along Shape.
   *
   * Based on code from http://www.jhlabs.com/java/java2d/strokes/
   */
    def textAlongPath(shape: Shape, text: String,font: Font = PText.DEFAULT_FONT): Shape = {
      val t = new AffineTransform
      val frc = new FontRenderContext(null, true, true)
      val glyphVector = font.createGlyphVector(frc, text)
      val result = new GeneralPath
      val it = new FlatteningPathIterator(shape.getPathIterator(null), 1)
      val points = new Array[Double](6)
      var lastX = 0.0
      var lastY = 0.0
      var next = 0.0
      var nextAdvance = 0.0
      var currentChar: Int = 0
      if (glyphVector.getNumGlyphs == 0) return result
      while (currentChar < glyphVector.getNumGlyphs && !it.isDone) {
        it.currentSegment(points) match {
          case PathIterator.SEG_MOVETO =>
            lastX = points(0)
            lastY = points(1);
            result.moveTo(points(0), points(1))
            nextAdvance = glyphVector.getGlyphMetrics(currentChar).getAdvance * 0.5
            next = nextAdvance

          case PathIterator.SEG_CLOSE =>

          case PathIterator.SEG_LINETO =>
            val dx = points(0) - lastX
            val dy = points(1) - lastY
            val distance = math.sqrt(dx * dx + dy * dy)
            if (distance >= next) {
              val angle = math.atan2(dy, dx)
              while (currentChar < glyphVector.getNumGlyphs && distance >= next) {
                val glyph = glyphVector.getGlyphOutline(currentChar)
                val p = glyphVector.getGlyphPosition(currentChar)
                val x = lastX + next * dx / distance
                val y = lastY + next * dy / distance
                val advance = nextAdvance
                nextAdvance =
                  if (currentChar < glyphVector.getNumGlyphs - 1)
                    glyphVector.getGlyphMetrics(currentChar + 1).getAdvance * 0.5
                  else 0
                t.setToTranslation(x, y)
                t.rotate(angle)
                t.translate(-p.getX - advance, -p.getY)
                result.append(t.createTransformedShape(glyph), false)
                next += (advance + nextAdvance)
                currentChar += 1;
              }
            }
            next -= distance
            lastX = points(0)
            lastY = points(1)
        }
        it.next
      }
      return result
    }
}

/**
 * An case class which stores configuration for `java.awt.BasicStroke`.
 * Use 'getStroke' method to create stroke from values stored in this class
 */
case class  BasicStrokeConfig(
  width:Double = 1,
  cap:Int = java.awt.BasicStroke.CAP_SQUARE,
  join:Int = java.awt.BasicStroke.JOIN_MITER,
  miterlimit:Double = 10,
  dash:List[Double] = null,
  dash_phase:Double = 0
){
  def getStroke:Stroke = getStroke(1.0)

  private def getStroke(pixelSize:Double):Stroke = new java.awt.BasicStroke(
      (width * pixelSize).toFloat,
      cap,join,miterlimit.toFloat,
      if(dash!=null)dash.map(_ * pixelSize).map(_.toFloat).toArray else null,
      dash_phase.toFloat)
}


/**
 * Batik SVG conversion does not know about round gradient.
 * This extension will make it work.
 * Resources:
 * http://www.w3schools.com/svg/svg_grad_radial.asp
 * http://xmlgraphics.apache.org/batik/using/svg-generator.html
 *
 * @author Jan Kotek
 */
private[chart] class GradientExtensionHandler extends DefaultExtensionHandler {
  override def handlePaint(paint: Paint, generatorCtx: SVGGeneratorContext): SVGPaintDescriptor = {
    if (paint.isInstanceOf[RadialGradientPaint]) {
      val p: RadialGradientPaint = paint.asInstanceOf[RadialGradientPaint]
      val id: String = generatorCtx.getIDGenerator.generateID("gradient")
      val doc: org.w3c.dom.Document = generatorCtx.getDOMFactory
      val grad: org.w3c.dom.Element = doc.createElementNS(SVGConstants.SVG_NAMESPACE_URI, SVGConstants.SVG_RADIAL_GRADIENT_TAG)
      grad.setAttributeNS(null, SVGConstants.SVG_ID_ATTRIBUTE, id)
      grad.setAttributeNS(null, SVGConstants.SVG_GRADIENT_UNITS_ATTRIBUTE, SVGConstants.SVG_USER_SPACE_ON_USE_VALUE)
      grad.setAttributeNS(null, "r", "" + p.getRadius)
      val pt: Point2D = p.getCenterPoint
      grad.setAttributeNS(null, "cx", "" + pt.getX)
      grad.setAttributeNS(null, "cy", "" + pt.getY)
      grad.setAttributeNS(null, "fx", "" + (pt.getX + p.getRadius))
      grad.setAttributeNS(null, "fx", "" + pt.getY)
      if (p.getColors.length != 2) throw new InternalError("SVG conversion only supports 2 colors")
      val pointColor: Color = p.getColors.apply(0)
      val backgroundColor: Color = p.getColors.apply(1)
      val stop1: org.w3c.dom.Element = doc.createElement("stop")
      stop1.setAttributeNS(null, "offset", "0")
      stop1.setAttributeNS(null, "style", "stop-color:rgb(" + pointColor.getRed + "," + pointColor.getGreen + "," + pointColor.getBlue + ");stop-opacity:" + (1d / 255 * pointColor.getAlpha))
      grad.appendChild(stop1)
      val stop2: org.w3c.dom.Element = doc.createElement("stop")
      stop2.setAttributeNS(null, "offset", "" + p.getRadius)
      stop2.setAttributeNS(null, "style", "stop-color:rgb(" + backgroundColor.getRed + "," + backgroundColor.getGreen + "," + backgroundColor.getBlue + ");stop-opacity:" + (1d / 255 * backgroundColor.getAlpha))
      grad.appendChild(stop2)
      new SVGPaintDescriptor("url(#" + id + ")", SVGConstants.SVG_OPAQUE_VALUE, grad)
    }
    else super.handlePaint(paint, generatorCtx)
  }
}

