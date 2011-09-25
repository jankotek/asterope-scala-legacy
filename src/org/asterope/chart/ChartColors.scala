package org.asterope.chart

import java.awt.Color
import scala.math._

object ChartColors {

	val WHITE = new Color(255, 255, 255);
	val BLACK = new Color(0,0,0)
	val DARK_BLUE = new Color(0,0,64)

	/**
	 * Calculates color between two colors
	 * @param c1 first color
	 * @param c2 second color
	 * @param diff value from 0 to 1
	 * @return color between two colors, if diff=0 returns c1, if diff=1 returns c2
	 */
	def between(c1:Color, c2:Color, diff:Double):Color = {
		assert(diff>=0 && diff<=1);
		val red = ( c1.getRed + (c2.getRed - c1.getRed) * diff).toInt;
		val green = ( c1.getGreen + (c2.getGreen - c1.getGreen) * diff).toInt;
		val blue =  ( c1.getBlue + (c2.getBlue - c1.getBlue) * diff).toInt;
		val alpha = ( c1.getAlpha + (c2.getAlpha - c1.getAlpha) * diff).toInt;

		new Color(red, green, blue, alpha);
	}

	def setAlpha(c1:Color, alpha:Int):Color = {
		new Color(c1.getRed, c1.getGreen, c1.getBlue, alpha);
	}

}


trait ChartColors{
  def fg:Color
  def bg:Color
	def gridColor:Color
  def gridLabel:Color

  def galaxy:Color
  def brightNebula:Color
  def darkNebula:Color
  def planetaryNebula:Color
  def openCluster:Color
  def globularCluster:Color
  def unknown:Color

  def galaxyInside:Color
  def brightNebulaInside:Color
  def darkNebulaInside:Color
  def planetaryNebulaInside:Color
  def openClusterInside:Color
  def globularClusterInside:Color
  def unknownInside:Color


  def milkyWay:Color
  def constelLine:Color
  def constelBoundary:Color
  def star:Color
}

object DarkBlueColors extends ChartColors{
  val fg = ChartColors.WHITE
  val bg = ChartColors.DARK_BLUE
  val gridColor = new Color(32,32,127)
  val gridLabel = new Color(96,96,150)

  val galaxy = new Color(255,0,0)
  val brightNebula = new Color(0,255,0)
  val darkNebula= new Color(200, 200, 255,128);
  val planetaryNebula = new Color(0,255,0)
  val openCluster = new Color(255, 255, 0)
  val globularCluster = new Color(255, 255, 0)
  val unknown = new Color(0,0,255)

  private val trans = 64
  val galaxyInside = new Color(255,0,0,trans)
  val brightNebulaInside = new Color(0,255,0,trans)
  val darkNebulaInside = new Color(200, 200, 255,trans);
  val planetaryNebulaInside = new Color(0,255,0,trans)
  val openClusterInside = new Color(255, 255, 0,trans)
  val globularClusterInside = new Color(255, 255, 0,trans)
  val unknownInside = new Color(0,0,255,trans)

  val milkyWay = new Color(0, 127, 127, 255)
  val constelLine = new Color(64, 64, 160)
  val constelBoundary = new Color(32, 32, 120)
  val star = new Color(255,255,255)
}


object LightColors extends ChartColors{
  val fg = ChartColors.BLACK
  val bg = ChartColors.WHITE
  val gridColor = new Color(70,70,150)
  val gridLabel = new Color(127,127,255)

  val galaxy = fg
  val brightNebula = fg
  val darkNebula = fg
  val planetaryNebula = fg
  val openCluster = fg
  val globularCluster = fg
  val unknown = fg

  private val trans = 128
  val galaxyInside = new Color(255,127,127,trans)
  val brightNebulaInside = new Color(127,255,127,trans)
  val darkNebulaInside = new Color(200, 200, 255,trans);
  val planetaryNebulaInside = new Color(127,255,127,trans)
  val openClusterInside = new Color(255, 255, 127,trans)
  val globularClusterInside = new Color(255, 255, 127,trans)
  val unknownInside = new Color(127,127,255,trans)


  val milkyWay = new Color(127, 255, 255, 255)
  val constelLine = new Color(128, 128, 255, 255)
  val constelBoundary = new Color(64, 64, 64)
  val star = fg
}
