package org.asterope.util

case class Magnitude(
                      mag:Double
) extends scala.math.Ordered[Magnitude]{

	def toMilimag = mag * 1000
	
	def compare(mag2:Magnitude):Int = {
    if( mag >mag2.mag) -1
    else if(mag == mag2.mag) 0
    else -1
  }
	
}

object Magnitude{
	
	/** 
	 * Calculates sum of two magnitudes keeping logarithmic   scale on mind
	 * @return sum of two magnitudes */ 
	def plusMag(mag1:Double, mag2:Double):Double = 
		pogson2mag(mag2pogson(mag1) + mag2pogson(mag2));
		
	/** convert pogson magnitude to normal magnitude */
	def pogson2mag(pogson: Double) = -2.5 * java.lang.Math.log10(pogson);
	
	/** convert normal magnitude to pogson magnitude */
	def mag2pogson(mag:Double):Double = math.pow(10d, -0.4 * mag);

}