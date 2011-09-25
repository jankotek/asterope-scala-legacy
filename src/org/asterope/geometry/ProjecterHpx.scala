package org.asterope.geometry;

import math._;


/**
 *  This class treats the HealPix projection as being
 *  skewed by 45 degrees so that the squares are oriented with
 *  the coordinate axes.  We rearrange the 12 base squares into the
 *  following orientation
 *  <code>
 *     3       
 *     40
 *     851
 *      962
 *       A7
 *        B
 *  </code>
 * Thus the 12 data squares can be enclosed in a 6x4 array including another
 * 12 unused squares.
 * <p>
 * An alternative arrangement might be.
 * <code>
 *     40  
 *     851 
 *      962
 *       A73
 *        B
 * </code>
 * where the data squares can be enclosed in a 5x5 array.
 * <p>
 * The actual transformations to and from the coordinate plane
 * are carried out using the static methods proj and deproj which
 * are called by the relevant method of Hpx an HpxDeproj.  Note
 * that HpxDeproj is included as a static class.
 * <p>
 * The transformation does not depend upon the input dimension.
 * This is used in ancillary functions (notably cvtPixel) which are
 * used when individual pixels are to be considered rather than the
 * geometric transformation between sphere and plane.
 * 
 *  @param dim The power of two giving the number of pixels
 *             along an edge of a square.  The total number
 *             of pixels in the projection is 12 * Math.pow(2, 2*dim)
 */

class ProjecterHpx(val dim:Int) extends Projecter {
  
	def getName = "Healpix"
    
    private val tileNum  = Array[Int](
       -1, -1, -1, 11,
       -1, -1, 10,  7,
       -1,  9,  6,  2,
	8,  5,  1, -1,
	4,  0, -1, -1,
	3, -1, -1, -1
    )
    
//    private static final double[][] tileOffsets= {
//	  {2*Math.PI, 0}
//    };
    
    /** The coordinates of the corners of squares 0 to 11. */   
    private val botLeftX= Array[Double](-1,  0,  1, -2, -2, -1,  0,  1, -2, -1,  0,  1);
    private val botLeftY= Array[Double]( 1,  0, -1,  2,  1,  0, -1, -2,  0, -1, -2, -3);
    
    /* The offsets are chosen with respect to an origin at the corner
     *  shared by squares 5 and 6.  This is at the position (3 pi/4, 0)
     *  in the nominal HEALPIX x,y plane.
     */
    private val zp = Array(0.75*Pi, 0);
    
    // Recall sin(45)=cos(45) = 1/sqrt(2)
    private val isqrt2 = 1./Math.sqrt(2);
        
    
    // The nominal HEALPix projection has the squares being
    // of length pi*sqrt(2)/4
    private val squareLength = Pi*isqrt2/2;
    
    /** Default to the 512x512 squares */
    def this() {
    	this(9);
    }
    
    

	val nSide:Int   = Math.pow(2, dim).toInt;
	val nSq:Int     = nSide * nSide;
	val nPixel:Int  = 12*nSq;
	val sqDelta 	= 1.0/nSide;
    
        
    
    
    def isInverse(t:Transformer):Boolean =  {
    	return false;
    	// We'd like this to cancel out with HPXDeproj
    	// but then the scaler can send data outside the bounds of
    	// the underlying image.
    	//	return t instanceof HpxDeproj;
    }

    
    override def validPosition(plane:Array[Double]):Boolean =  {
    	super.validPosition(plane) && 
    	abs(plane(1)) <= Pi/2 - abs(Pi/4 - (abs(plane(0))% Pi/2));
    }
	  
    /** Get the lower left corner in the oblique projection */
    def  getOblCorner(pix:Int):Array[Double] = {
	
	if (pix < 0 || pix >= nPixel) {
	    return Array[Double](Double.NaN, Double.NaN);
	}
	
	val square = pix/nSq;
	
	var rem   = pix % nSq;
	var	delta = 0.5;
	var div   = nSide/2;
	
	var x = botLeftX(square);
	var y = botLeftY(square);
	
	while (div > 0) {
	    val nrem = rem%div;
	    
	    x += (rem%2)*delta;
	    y += (rem/2)*delta;
	    
	    delta /= 2;
	    div   /= 2;
	    rem    = nrem;
	}
	Array(x,y)
    }
	    
    /** Get the corners of the pixel in the nominal orientation */
    def getCorners(pix:Int):Array[Array[Double]] = {
	
	val z:Array[Array[Double]] = Array.ofDim(4,2)
	
	z(0) = getOblCorner(pix);
	z(1)(0) = z(0)(0) + sqDelta;
	z(1)(1) = z(0)(1);
	z(2)(0) = z(1)(0);
	z(2)(1) = z(0)(1) + sqDelta;
	z(3)(0) = z(0)(0);
	z(3)(1) = z(2)(1);

        for (t <- z) {
            val u = t(0);
            val v = t(1);

            t(0) = squareLength * (isqrt2 * u - isqrt2 * v) + zp(0);
            if (t(0) > 2 * Pi) {
                t(0) -= 2 * Pi;
            } else if (t(0) < 0) {
                t(0) += 2 * Pi;
            }

            t(1) = squareLength * (isqrt2 * u + isqrt2 * v) + zp(1)
        }
	return z;
    }

    def deproj(in:Array[Double], unit:Array[Double]) {
	
	var  x = in(0);
	var y = in(1);
	if (x < 0) {
	    x += 2*Pi;
	}
	if (x > 2*Pi) {
	    x -= 2*Pi;
	}
	
	// Check that we are in the valid region.
	if (abs(y) > 0.5*Pi) {
	  unit(0) = Double.NaN
	  unit(1) = Double.NaN
	  unit(2) = Double.NaN
	  return;
	}
	if (abs(y) > 0.25*Pi) {
	    val posit = (x/Pi) % 0.5; 
	    val yt    = abs(y)/Pi;
	    
	    // Add a small delta to allow for roundoff.
	    if (yt > (0.5-Math.abs(posit-0.25))+1.e-13 ) {
	    	unit(0) = Double.NaN
	    	unit(1) = Double.NaN
	    	unit(2) = Double.NaN
	        return;
	    }
	}
	
	var ra, sdec = 0.0;
	if (abs(y) < Pi/4) {
	    ra   = x;
	    sdec = (8*y/(3*Pi));
	    
	} else {
	    
	    val yabs = Math.abs(y);
	    val xt   = x % (Pi/2);
	    ra  = x - (yabs - Pi/4)/(yabs-Pi/2) * (xt - Pi/4);
	    if (java.lang.Double.isNaN(ra)) {
	    	ra = 0;
	    }
	    sdec = (1 - (2-4*yabs/Pi)*(2-4*yabs/Pi)/3 ) * y / yabs;
	    if (sdec > 1) {
	    	sdec =  1;
	    } else if (sdec < -1) {
	    	sdec = -1;
	    }
	}
	val cdec = sqrt(1-sdec*sdec);
	unit(0) = cos(ra)*cdec;
	unit(1) = sin(ra)*cdec;
	unit(2) = sdec;
	
    }
	    

    def transform(unit:Array[Double], proj:Array[Double]) {
	
	if (abs(unit(2)) < 2./3) {
	    proj(0) = atan2(unit(1), unit(0));
	    if (proj(0) < 0) {
	    	proj(0) += 2*Pi;
	    }
	    proj(1) = 3*Pi/8 * unit(2);
	    
	} else {
	    var phi = atan2(unit(1), unit(0));
	    if (phi < 0) {
	    	phi += 2*Pi;
	    }
	    
	    val phit = phi % (Pi/2);
	    
	    var z    = unit(2);
	    var sign = 1;
	    if (z < 0) {
	    	z    = -z;
	    	sign = -1;
	    }
	    
	    val sigma = sign*(2-Math.sqrt(3*(1-z)));
	    proj(0) = phi - (abs(sigma)-1)*(phit-Pi/4);
	    proj(1) = Pi*sigma/4;
	}
	val x = proj(0)/Math.Pi;
	val y = proj(1)/Math.Pi;
	// We move the right half of tile 4 and all of tile 3 back by 2 PI
        // so that the standard region is appropriate for the 4x6 region in the
	// rotated coordinates.
	if (x > 1.5 && y > 1.75-x)  {
	    proj(0) -= 2*Math.Pi;
	}
    }
    
 
    
    /** Find the pixel that includes the given position.
     * 
     * @param pos The position in the nominal HEALPix projection plane */
    
    def getPixel(pos:Array[Double]):Int = {
    	return getPixel(pos(0), pos(1));
    }
    
    /** Find the pixel that includes the given position.
     *  Generally if a position is exactly on a pixel border the pixel
     *  with the larger coordinate value will be returned.
     * 
     * @param x,y The coordinates of point for which the pixel number is desired.
     */
    
    def getPixel(x2:Double, y2:Double):Int =  {
	// First convert this to the oblique projection plane.
	
    var x = x2
    
	if (x < 0) {
	    x += 2*Pi;
	} else if (x > 2*Pi) {
	    x -= 2*Pi;
	}
	
	
	// Move to standard rotation center
	x = x - zp(0);
	var y = y2 - zp(1);
	
	// Now rotate to the oblique plan.
	val u = ( x*isqrt2 + y*isqrt2)/squareLength;
	val v = (-x*isqrt2 + y*isqrt2)/squareLength;
	
	getObliquePixel(u,v);
    }
    
    def getObliquePixel(u:Double, v:Double):Int = {
	
	
	var xSq  = floor(u);
	var ySq  = floor(v);
	
	
	// Find out which tile we are in.
	
	if (xSq < -2 || xSq >= 2) {
	    // The sequence of tiles repeats every
	    // four columns but ySq=5 for xSq=0
	    // corresponds to ySq=1 for xSq=4
	    val ix = ( xSq + 2).toInt;
	    
	    ySq = ySq + 4*(ix/4);
	    xSq = ix%4;
	    if (xSq < 0) {
	    	xSq += 4;
	    }
	    xSq -= 2;
	}
	    
	var td   = (xSq+2 + 4*(ySq+3)).toInt;
	if (ySq < -3 || ySq >= 3) {
	    td = -1;
	}
	
	var tile = -1;
	
	if (td >= 0 && td < tileNum.length) {
	    tile = tileNum(td);
	}
	
	if (tile == -1) {
	    return -1;
	}
	
	var pix = nSq*tile;
	
	var delta = 0.5;
	var dx    = u - xSq;
	var dy    = v - ySq;
	
	var npix = nSide;
	while (npix > 1) {
	    
	    var ipix = 0;   // Will be 0-3 at the end.
	    
	    if (dy  >= delta) {
	    	ipix = 2;
	    	dy  -= delta;
	    }	    
	    if (dx   >= delta) {
	    	ipix += 1;
	    	dx   -= delta;
	    }
	    
	    npix   /= 2;
	    delta  /= 2;
	    
	    pix += ipix*npix*npix;
	    
	}
	
	return pix;
    }
    
    /** This method converts a pixel number based on the assumption
     *  that we have a simple two-d image map, into the nested HEALPix
     *  pixel number. This routine assumes that the input pixel
     *  numbers are associated with a (4 nSide)x(6 nSide) virtual
     *  image.  Note that this is assumed to be in the oblique frame.
     */
    def cvtPixel(pixel:Int):Int = {
	
	val    ix = pixel % (4 * nSide);
	val    iy = pixel / (4 * nSide);
	
	
	val px:Double = ix.toDouble/nSide;
	val py:Double= iy.toDouble/nSide;
	
	getObliquePixel(px-2, py-3);
		
    }
	
   object inverse extends Deprojecter{
    
	def inverse:Transformer = ProjecterHpx.this;
	
	
	def isInverse(t:Transformer):Boolean = {
	    false;
//  Would like to do the following but seems to cause things
//  to run outside proper bounds
//	    return t instanceof Hpx;
	}
	
	
	def transform(plane:Array[Double], sphere:Array[Double]) {
	    deproj(plane, sphere);        
    }
	}
}
