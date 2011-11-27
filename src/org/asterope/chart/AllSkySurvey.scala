package org.asterope.chart

import org.asterope.util._
import org.asterope.healpix._
import java.net.URL
import javax.imageio.ImageIO

import edu.umd.cs.piccolo.PNode
import edu.umd.cs.piccolo.util._
import java.awt.geom.{Area, AffineTransform, Path2D}
import org.asterope.geometry.CoordinateSystem.Galactic
import edu.umd.cs.piccolo.nodes.PText
import java.awt.{Color, Image, Graphics2D, Shape}
import org.apache.commons.math.geometry.Vector3D
import scala.collection.JavaConversions._

object AllSkySurvey extends ChartFeature[AllSkySurveyMem] {



  val defaultConfig = new AllSkySurveyMem()

  def updateChart(chart: Chart, config:AllSkySurveyMem=defaultConfig){

    //add label with copyright
    val text = new PText(config.survey.copyright)
    text.setPaint(new Color(chart.colors.bg.getRed,chart.colors.bg.getGreen,chart.colors.bg.getBlue,128))
    text.setTextPaint(chart.colors.fg)
    text.centerBoundsOnPoint(chart.width - text.getWidth/2 - 10, chart.height - text.getHeight/2 - 10)
    chart.addNode(Layer.skyview,text,zorder = 1);

    def translate(pos:Vector3D):Vector3D = {
      config.survey.coordSys match{
        case "E" => pos
        case "G" => Galactic.getRotater.transform(pos)
      }
    }

    def untranslate(pos:Vector3D):Vector3D = {
      config.survey.coordSys match{
        case "E" => pos
        case "G" => Galactic.getRotater.inverse.transform(pos)
      }
    }


    val norder = getNorder(chart.pixelAngularSize,config)
    val nside = Pixelization.norder2nside(norder)
    val tools = new PixTools(nside)


    val pixelsRing = tools.query_disc(translate(chart.position),chart.fieldOfView.toRadian,false)
    val futures = for(
      ring<-pixelsRing.iterator(); //TODO replace with 'longIterator' to reduce instance creation
      nested = PixToolsNested.ring2nest(nside,ring);
      bounds = tools.makePix2Vect(ring);
      north = chart.wcs.project(untranslate(bounds.north));
      south = chart.wcs.project(untranslate(bounds.south));
      west = chart.wcs.project(untranslate(bounds.west));
      east = chart.wcs.project(untranslate(bounds.east));
      if(north.isDefined && south.isDefined && west.isDefined && east.isDefined);
      points = Array(south.get,east.get,west.get,north.get);
      x = points.map(_.x).min;
      y = points.map(_.y).min;
      w = points.map(_.x).max - x;
      h = points.map(_.y).max - y;
      if(chart.isInsideCanvas(new PBounds(x,y,w,h)));
      file = config.survey.url+"Norder"+norder+"/Dir"+(nested-nested%10000)+"/Npix"+nested+config.survey.suffix
    )yield future[Unit]{

        val is = GetURL(new URL(file));
        checkInterrupted()
        val img = ImageIO.read(is)
        checkInterrupted()
        object node extends PNode{
          setBounds(x,y,w,h)
          override def paint(paintContext:PPaintContext) {
            val g2 = paintContext.getGraphics;
            drawTriangle(g2,img,points,0,config.survey.imgWidth)
            drawTriangle(g2,img,points,3,config.survey.imgWidth)
          }
        }
        chart.executor{
          chart.addNode(layer=Layer.skyview, node=node)
          node.repaint()
        }
        checkInterrupted()
    }


    //wait for all scheduled futures to finish
    waitOrInterrupt(futures.toBuffer)




  }

  def clearChart(chart: Chart){
    chart.executor{
      chart.getLayer(Layer.skyview).removeAllChildren()
    }
  }

  def getNorder(pixelSize:Angle, mem:AllSkySurveyMem):Int = {
    val ang = pixelSize.toArcSec * mem.survey.imgWidth
    val nside = PixTools.GetNSide(ang)
    val norder = Pixelization.nside2norder(nside)
    2.max(norder.min(mem.survey.maxNorder))
  }

  /** calculate AffineTransform and paint part of image in triangle
   * This method comes from Alladin*/
  def drawTriangle(g2d: Graphics2D, img: Image, b: Array[Point2d], h: Int, width: Int){
    val (d,g) =  h match {
      case 0 =>(2,1)
      case 3 => (1,2)
      case 1 => (3,0)
      case _ => (0,3)
    }
    if (b(d) == null || b(g) == null) return


    //contruct rectangular clip
    val clip: Path2D = new Path2D.Double
    clip.moveTo(b(h).x, b(h).y)
    clip.lineTo(b(d).x, b(d).y)
    clip.lineTo(b(g).x, b(g).y)
    clip.closePath()
    //and combine it with currently set clipping to remove fragments
    val oldClip: Shape = g2d.getClip
    val clipArea: Area = new Area(clip)
    if (oldClip != null) clipArea.intersect(new Area(oldClip))
    g2d.setClip(clipArea)


    // On tourne l'image pour l'aligner sur h-d
    var hdx: Double = b(h).x - b(d).x
    if (h == 0 || h == 2) hdx = -hdx
    var hdy: Double = b(h).y - b(d).y
    if (h == 0 || h == 2) hdy = -hdy
    val angle: Double = math.atan2(hdy, hdx)

    // On crase la longueur
    val hd: Double = math.sqrt(hdx * hdx + hdy * hdy)
    val mx: Double = hd / width

    // On crase la hauteur
    var hgx: Double = b(h).x - b(g).x
    if (h == 0 || h == 2) hgx = -hgx
    var hgy: Double = b(h).y - b(g).y
    if (h == 0 || h == 2) hgy = -hgy
    val dhg: Double = math.sqrt(hgx * hgx + hgy * hgy)
    val anglehg: Double = math.atan2(hgy, hgx) - angle
    val my: Double = dhg * math.sin(anglehg) / width

    // On fait glisser selon les x pour longer l'axe d-h
    val sx: Double = (dhg * math.cos(anglehg)) / hd

    //construct and set affine transform
    val tr: AffineTransform = new AffineTransform
    if (h == 3 || h == 1) tr.translate((b(d).x + b(g).x - b(h).x), (b(d).y + b(g).y - b(h).y))
    else tr.translate(b(h).x, b(h).y)
    tr.rotate(angle)
    tr.scale(mx, my)
    tr.shear(sx, 0)
    //paint image with affine transform and clipping
    g2d.drawImage(img, tr, null)

    //restore clip
    g2d.setClip(oldClip)
  }


  case class Survey(url:String, name:String, description:String,
                            copyright:String, copyrightUrl:String,
                            imgWidth:Int = 512,maxNorder:Int = 8, suffix:String = ".jpg",
                            coordSys:String = "G")


  val dssColorSurvey =
    new Survey(url= "http://alasky.u-strasbg.fr/DssColor/",
      name = "DSS colored", description = "Colorized DSS survey",
      copyright = "DSS consortium & STScI/NASA",
      copyrightUrl = "http://archive.stsci.edu/dss/copyright.html",
      maxNorder = 8, coordSys = "E")

  val mellingerSurvey =
    Survey(url=  "http://alasky.u-strasbg.fr/MellingerRGB/",
    name = "Mellinger colored", description = "Mellinger optical survey, color",
    copyright = "(c) Axel Mellinger. Permission is granted for use in research and personal,\n non-commercial use. Please contact Axel Mellinger for permission for other use",
    copyrightUrl = "http://home.arcor.de/axel.mellinger/",
    maxNorder = 4, coordSys = "G")


}

case class AllSkySurveyMem( survey:AllSkySurvey.Survey = AllSkySurvey.dssColorSurvey)

