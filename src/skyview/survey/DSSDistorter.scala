package skyview.survey

import org.asterope.geometry._
import org.asterope.util._

class DSSDistorter(
    plateScale:Double, 
    x_coeff:Array[Double], // TODO use immutable structure instead of an array 
    y_coeff:Array[Double]
   )extends Distorter {
 
  
   private val  CONS2R    = math.toDegrees(1)*3600;  
      
   override def transform(x:Array[Double], y:Array[Double]) {
		  
        val max_iterations = 50;
	
        // Convert to seconds.
        val xi  = x(0)*Angle.R2S;
        val eta = x(1)*Angle.R2S;
	
        /* Convert to millimeters. */
        var xmm = xi  / this.plateScale;
        var ymm = eta / this.plateScale;

	
        /* Iterate by Newton's method */	
        var i=0;
        while(i<max_iterations){
        	
        	/** set position */
        	val xy      = xmm * ymm;
        	val x2      = xmm * xmm;
        	val y2      = ymm * ymm;
        	val x2y     = x2 * ymm;
        	val y2x     = y2 * xmm;
        	val x2y2    = x2 + y2;
        	val x4y4    = x2y2 * x2y2;
        	val x3      = x2 * xmm;
        	val y3      = y2 * ymm;
        	val x4      = x2 * x2;
        	val y4      = y2 * y2;

        	
	    
        	/** Give the corrected X coordinate for the current actual position */
        	val ft =      
    			x_coeff(0)*xmm      + x_coeff(1)*ymm +
                x_coeff(2)          + x_coeff(3)*x2 +
                x_coeff(4)*xy       + x_coeff(5)*y2 +
                x_coeff(6)*x2y2     + x_coeff(7)*x3 +
                x_coeff(8)*x2y      + x_coeff(9)*y2x +
                x_coeff(10)*y3      + x_coeff(11)*xmm*x2y2 +
                x_coeff(12)*xmm*x4y4;    
        	
            /** Give the corrected Y coordinate for the currenat actual position */
        	val gt = 
                y_coeff(0)*ymm        + y_coeff(1)*xmm +
                y_coeff(2)            + y_coeff(3)*y2 +
                y_coeff(4)*xy         + y_coeff(5)*x2 +
                y_coeff(6)*x2y2       + y_coeff(7)*y3 +
                y_coeff(8)*y2x        + y_coeff(9)*x2y +
                y_coeff(10)*x3        + y_coeff(11)*ymm*x2y2 +
                y_coeff(12)*ymm*x4y4;
        	

	    
        	/** Derivative of corrected X coordinate with respect to actual X coordinate */
            val fx =    
           		/*  Derivative of X model wrt x */
    			 x_coeff(0)           + x_coeff(3)*2.0*xmm +
                 x_coeff(4)*ymm       + x_coeff(6)*2.0*xmm +
                 x_coeff(7)*3.0*x2    + x_coeff(8)*2.0*xy +
                 x_coeff(9)*y2        + x_coeff(11)*(3.0*x2+y2) +
                 x_coeff(12)*(5.0*x4 +6.0*x2*y2+y4);
        	
            /** Derivative of corrected X coordinate with respect to actual Y coordinate */
            val fy =
            	/* Derivative of X model wrt y */
                 x_coeff(1)           + x_coeff(4)*xmm +
                 x_coeff(5)*2.0*ymm   + x_coeff(6)*2.0*ymm +
                 x_coeff(8)*x2        + x_coeff(9)*2.0*xy +
                 x_coeff(10)*3.0*y2   + x_coeff(11)*2.0*xy +
                 x_coeff(12)*4.0*xy*x2y2;
            
            /** Derivative of corrected Y coordinate with respect to actual X coordinate */
            val gx = 
                 y_coeff(1)           + y_coeff(4)*ymm +
                 y_coeff(5)*2.0*xmm   + y_coeff(6)*2.0*xmm +
                 y_coeff(8)*y2       + y_coeff(9)*2.0*xy +
                 y_coeff(10)*3.0*x2  + y_coeff(11)*2.0*xy +
                 y_coeff(12)*4.0*xy*x2y2;
            
            /** Derivative of corrected Y coordinate with respect to actual Y coordinate */
            val gy = 
                 y_coeff(0)            + y_coeff(3)*2.0*ymm +
                 y_coeff(4)*xmm        + y_coeff(6)*2.0*ymm +
                 y_coeff(7)*3.0*y2     + y_coeff(8)*2.0*xy +
                 y_coeff(9)*x2         + y_coeff(11)*(x2+3.0*y2) +
                 y_coeff(12)*(5.0*y4 + 6.0*x2*y2 + x4);
        
            val df = ft - xi;
            val dg = gt - eta;
	    
	    
            val dx = ((-df * gy) + (dg * fy)) / ((fx * gy) - (fy * gx));
            val dy = ((-dg * fx) + (df * gx)) / ((fx * gy) - (fy * gx));
	    
            xmm = xmm + dx;
            ymm = ymm + dy;

            val TOLERANCE = 0.0000005;
            if ((math.abs(dx) < TOLERANCE) && (math.abs(dy) < TOLERANCE)) {
        	   i = max_iterations+1000; //break
            }
            i+=1;
        }
	
       	// Convert to radians and return.
       	y(0) = xmm*this.plateScale/Angle.R2S;
       	y(1) = ymm*this.plateScale/Angle.R2S;
    }
   
   def isInverse(inv:Transformer):Boolean = inv == inverse
         
   
       /** This inner class is the inverse of the DSS Distorter and corrects
     *  the distortion generated there.  For the DSS projection, this
     *  direction is described analytically using a polynomial expansion,
     *  while the 'forward' distortion must be done by inverting the polynomial
     *  using Newton's method.
     */
   object inverse extends Distorter{
     def inverse = DSSDistorter.this
     def isInverse(t:Transformer):Boolean = t == DSSDistorter.this
     
 	 override def transform(x:Array[Double], y:Array[Double]) {
    	 	// Need to convert from radians to mm
            val	xmm = x(0) * Angle.R2S / plateScale;	
            val	ymm = x(1) * Angle.R2S / plateScale;
    
            //  Compute corrected coordinates XI,ETA in "

        	/** set position */
        	val xy      = xmm * ymm;
        	val x2      = xmm * xmm;
        	val y2      = ymm * ymm;
        	val x2y     = x2 * ymm;
        	val y2x     = y2 * xmm;
        	val x2y2    = x2 + y2;
        	val x4y4    = x2y2 * x2y2;
        	val x3      = x2 * xmm;
        	val y3      = y2 * ymm;
        	val x4      = x2 * x2;
        	val y4      = y2 * y2;

        	
        	/** Give the corrected X coordinate for the current actual position */
        	val xi =      
    			x_coeff(0)*xmm      + x_coeff(1)*ymm +
                x_coeff(2)          + x_coeff(3)*x2 +
                x_coeff(4)*xy       + x_coeff(5)*y2 +
                x_coeff(6)*x2y2     + x_coeff(7)*x3 +
                x_coeff(8)*x2y      + x_coeff(9)*y2x +
                x_coeff(10)*y3      + x_coeff(11)*xmm*x2y2 +
                x_coeff(12)*xmm*x4y4;    
        	
            /** Give the corrected Y coordinate for the currenat actual position */
        	val eta = 
                y_coeff(0)*ymm        + y_coeff(1)*xmm +
                y_coeff(2)            + y_coeff(3)*y2 +
                y_coeff(4)*xy         + y_coeff(5)*x2 +
                y_coeff(6)*x2y2       + y_coeff(7)*y3 +
                y_coeff(8)*y2x        + y_coeff(9)*x2y +
                y_coeff(10)*x3        + y_coeff(11)*ymm*x2y2 +
                y_coeff(12)*ymm*x4y4;
        	
	  
	  
            /* Convert from " to radians */
            y(0) = xi  / Angle.R2S;
            y(1) = eta / Angle.R2S;
	}	
     
     

     
   }

}