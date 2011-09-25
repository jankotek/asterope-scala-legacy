package org.asterope.chart

case class Point2d(x:Double, y:Double) 
	extends java.awt.geom.Point2D{
	
	def setLocation(x:Double, y:Double) = 
			throw new UnsupportedOperationException("Immutable!")
	
	def getX = x;
	def getY = y;

}