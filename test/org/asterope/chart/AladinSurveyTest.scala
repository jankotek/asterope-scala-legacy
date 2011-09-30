package org.asterope.chart

import org.asterope.util._
import org.asterope.healpix._
import java.awt.image.BufferedImage
import java.awt.Color
import javax.imageio.ImageIO
import java.io.File

class AladinSurveyTest extends ScalaTestCase{

  def createImg(width:Int, height:Int, color:Color):BufferedImage = {
    val img = new BufferedImage(width,height,BufferedImage.TYPE_INT_RGB);
    for(x<-0 until width; y<-0 until height)
      img.setRGB(x,y,color.getRGB)
    img
  }

  def saveImg(img:BufferedImage){
    ImageIO.write(img,"png",new File("drawTriangle.png"))
  }

  def testDrawTriangleFull(){

    //create image filled with blue and some corners
    val img = createImg(100,100,Color.BLUE)
    val corners = Array(
      Point2d(100,100),
      Point2d(200,100),
      Point2d(100,200),
      Point2d(200,200))

    //only one side is drawn
    val canvas = createImg(500,500,Color.BLACK);
    AladinSurvey.drawTriangle(canvas.createGraphics(),img,corners,0,100)
    assert(canvas.getRGB(99,99) == Color.black.getRGB)
    assert(canvas.getRGB(101,101) == Color.blue.getRGB)
    assert(canvas.getRGB(201,99) == Color.black.getRGB)
    assert(canvas.getRGB(199,101) == Color.black.getRGB) //here is triangle cut
    assert(canvas.getRGB(99,201) == Color.black.getRGB)
    assert(canvas.getRGB(101,199) == Color.black.getRGB) //here is triangle cut
    assert(canvas.getRGB(201,201) == Color.black.getRGB)
    assert(canvas.getRGB(199,199) == Color.black.getRGB) //here is triangle cut
  }

  def testRotateAndShard(){

    //create image filled with blue and some corners
    val img = createImg(100,100,Color.BLUE)
    val corners = Array(
      Point2d(150,50),
      Point2d(250,150),
      Point2d(50,250),
      Point2d(150,250))

    //paint triangle on first canvas without cutting
    val canvas = createImg(500,500,Color.BLACK);
    AladinSurvey.drawTriangle(canvas.createGraphics(),img,corners,0,100)
    //saveImg(canvas)

    assert(canvas.getRGB(150,49) == Color.black.getRGB)
    assert(canvas.getRGB(150,51) == Color.blue.getRGB)
    assert(canvas.getRGB(150,71) == Color.blue.getRGB)

    assert(canvas.getRGB(250+3,150) == Color.black.getRGB)
    assert(canvas.getRGB(250-3,150) == Color.blue.getRGB)

    assert(canvas.getRGB(50+15,250-20) == Color.blue.getRGB)
    assert(canvas.getRGB(150,250) == Color.black.getRGB)
  }


  def testPixelNumber(){
    //I know Asterope at 'NOrder 6' has image number 474
    val nside = math.pow(2,6).toLong
    val tools = new PixTools(nside)
    val ring = tools.vect2pix(Vector3d.asterope)
    val nested = Nested.ring2nest(nside,ring)
    assert(nested === 474)

  }

}