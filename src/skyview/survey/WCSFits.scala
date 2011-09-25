package skyview.survey

import nom.tam.fits.Header
import org.asterope.geometry._
import org.asterope.util._


/**
 * Factory method to constructs WCS (World Coordinate System) from FITS files
 *
 */
object WCSFits {
  
  
  
  def fromDSS(h:Header):WCSFits = {
  
    
    val wcsKeys = new java.util.LinkedHashMap[String,Any];
    
    val headerNaxis = new Array[Int](2)
    headerNaxis(0) = h.getIntValue("NAXIS1");
    headerNaxis(1) = h.getIntValue("NAXIS2");
	if (headerNaxis(0) == 0) {
		headerNaxis(0) = h.getIntValue("XPIXELS");
		headerNaxis(1) = h.getIntValue("YPIXELS");
	}

    wcsKeys.put("ORIGIN", h.getStringValue("ORIGIN"));

    var plateRA = h.getDoubleValue("PLTRAH") + h.getDoubleValue("PLTRAM")/60 + h.getDoubleValue("PLTRAS")/3600;
	plateRA        = math.toRadians(15*plateRA);
	wcsKeys.put("PLTRAH", h.getDoubleValue("PLTRAH"));
	wcsKeys.put("PLTRAM", h.getDoubleValue("PLTRAM"));
	wcsKeys.put("PLTRAS", h.getDoubleValue("PLTRAS"));

	var plateDec = h.getDoubleValue("PLTDECD") + h.getDoubleValue("PLTDECM")/60 + h.getDoubleValue("PLTDECS")/3600;
	plateDec        = math.toRadians(plateDec);

	if (h.getStringValue("PLTDECSN").substring(0,1).equals("-")) {
	    plateDec    = -plateDec;
	}
	wcsKeys.put("PLTDECD", h.getDoubleValue("PLTDECD"));
	wcsKeys.put("PLTDECM", h.getDoubleValue("PLTDECM"));
	wcsKeys.put("PLTDECS", h.getDoubleValue("PLTDECS"));
	wcsKeys.put("PLTDECSN", h.getStringValue("PLTDECSN"));

	var plateScale = h.getDoubleValue("PLTSCALE");
	var xPixelSize = h.getDoubleValue("XPIXELSZ");
	var yPixelSize = h.getDoubleValue("YPIXELSZ");
	wcsKeys.put("PLTSCALE", plateScale);
	wcsKeys.put("XPIXELSZ", xPixelSize);
	wcsKeys.put("YPIXELSZ", yPixelSize);

	var xCoeff = new Array[Double](20);
	var yCoeff = new Array[Double](20);

	for (i <- 1 to 20) {
	    xCoeff(i-1) = h.getDoubleValue("AMDX"+i);
	    yCoeff(i-1) = h.getDoubleValue("AMDY"+i);
	    wcsKeys.put("AMDX"+i, xCoeff(i-1));
	    wcsKeys.put("AMDY"+i, yCoeff(i-1));
	}

	val ppo = new Array[Double](6)
	for (i <- 1 to 6) {
	    ppo(i-1) = h.getDoubleValue("PPO"+i);
	    wcsKeys.put("PPO"+i, ppo(i-1));
	}

	val plateCenterX = ppo(2);
	val plateCenterY = ppo(5);

	var cdelt1 = - plateScale/1000 * xPixelSize/3600;
	var cdelt2 =   plateScale/1000 * yPixelSize/3600;

	val wcsScale = Math.abs(cdelt1) 	

	// This gives cdelts in degrees per pixel.

	// CNPIX pixels use a have the first pixel going from 1 - 2 so they are
	// off by 0.5 from FITS (which in turn is offset by 0.5 from the internal
	// scaling, but we handle that elsewhere).
	val crpix1 =   plateCenterX/xPixelSize - h.getDoubleValue("CNPIX1", 0) - 0.5;
	val crpix2 =   plateCenterY/yPixelSize - h.getDoubleValue("CNPIX2", 0) - 0.5;
	wcsKeys.put("CNPIX1", h.getDoubleValue("CNPIX1", 0));
	wcsKeys.put("CNPIX2", h.getDoubleValue("CNPIX2", 0));


	val proj = new Projection("Tan", Array[Double](plateRA, plateDec));
	
	val coords = CoordinateSystem.factory("J2000");
	

	cdelt1 = Math.toRadians(cdelt1);
	cdelt2 = Math.toRadians(cdelt2);

	val s = new Scaler(-cdelt1*crpix1, -cdelt2*crpix2, cdelt1, 0, 0, cdelt2);


	val distorter = new DSSDistorter(plateScale, xCoeff, yCoeff);

	
	val wcs = new WCSFits(coords,proj,distorter,s.inverse, 
	    wcsKeys,headerNaxis)
	wcs.wcsScale = wcsScale
	
	wcs
	
  }
  
  
  def fromNormal(h:Header):WCSFits = {
    val wcsKeys = new java.util.LinkedHashMap[String,Any];    
    val headerNaxis = new Array[Int](2)
    var lonAxis = -1
    var latAxis = -1

    /** Find which axes are being used for coordinates.
     *  Normally this will just be 1 and 2, but occasionally
     *  we may be surprised.
     *  
     * The first axes match are assumed to be correct.
     */
	for (i <-1 to h.getIntValue("NAXIS")) {
	    var axis = h.getStringValue("CTYPE"+i);
	    
	    if (axis != null && axis.length() >= 4) {
	    	axis = axis.substring(0,4);
	    	
	    	if (lonAxis == -1) {
	    		if (axis == "RA--" || axis == "GLON" || axis =="ELON"|| axis =="HLON") {
	    			lonAxis = i;
	    		}
	    	}
	    	if (latAxis == -1) {
	    		if (axis == "DEC-"  || axis == "GLAT" || axis == "ELAT" || axis == "HLAT") {
	    			latAxis = i;
	    		}
	    	}
	    }
	}
	if (lonAxis > -1) {
	    wcsKeys.put("CTYPE1", h.getStringValue("CTYPE"+lonAxis));
	}
	if (latAxis > -1) {
	    wcsKeys.put("CTYPE2", h.getStringValue("CTYPE"+latAxis));
	}
	headerNaxis(0) = h.getIntValue("NAXIS"+lonAxis);
	headerNaxis(1) = h.getIntValue("NAXIS"+latAxis);
	
	if (lonAxis == -1 || latAxis == -1) {
		throw new Error("Unable to find coordinate axes");
	}
	
	/**
	 * Extract coordinate system from header
	 * To get the coordinate system we look at the CTYPEn, EQUINOX, RADESYSm
	 */
	val coordinateSystem = {
	val lonType = h.getStringValue("CTYPE"+lonAxis).substring(0,4);
	val latType = h.getStringValue("CTYPE"+latAxis).substring(0,4);
	
	wcsKeys.put("EQUINOX", 2000); //TODO 2000 comes from original Skyview, but it may be fault
	val equinox:Double = 		
		h.getDoubleValue("EQUINOX", h.getDoubleValue("EPOCH", 2000));		
    	
	val frame:String = {
	  val sys = h.getStringValue("RADESYS");
      if (sys != null) {
	    wcsKeys.put("RADESYS", sys);
     	if (sys.startsWith("FK4"))"B";
     	else "J";
	  }else{
	    if (equinox >= 1984) "J"
	    else "B"	    
	  }
	}
	
	val coordSym:String = { 
	  if (lonType == "RA--" && latType =="DEC-" )
	    frame+equinox;
	  else if (lonType.charAt(0) != latType.charAt(0)) 
	        throw new Exception("Inconsistent axes definitions:"+lonType+","+latType);
	   else if (lonType == "GLON") 
	       "G";
	   else if (lonType == "ELON") 
	       "E"+equinox;
	   else if (lonType == "HLON") 
	        "H"+equinox;
	   else 
	     throw new Error("Unknown coordinate system: "+lonType + " - "+latType)
	}

	 CoordinateSystem.factory(coordSym);
    }
	
	/**
	 * Extract projection 
	 */
	var postProjectionDistorter:Distorter = null
	val projection:Projection = {

	val lonType = h.getStringValue("CTYPE"+lonAxis).substring(5,8);
	val latType = h.getStringValue("CTYPE"+latAxis).substring(5,8);
	if (!lonType.equals(latType)) {
	    throw new Error("Inconsistent projection in FITS header: "+lonType+","+latType);
	}	
    val crval1 = h.getDoubleValue("CRVAL"+lonAxis, Double.NaN);
    val crval2 = h.getDoubleValue("CRVAL"+latAxis, Double.NaN);


    if (lonType == "AIT") {
	    new Projection("Ait");
	} else if (lonType == "CAR") {
	    new Projection("Car");
	} else if (lonType == "CSC") {
	    new Projection("Csc");
	} else if (lonType == "SFL" || lonType == "GLS") {
	    new Projection("Sfl");
	} else if (lonType == "TOA") {
	    new Projection("Toa");
	} else if(lonType == "TAN" || lonType == "SIN" || lonType == "ZEA" || lonType == "ARC"  || lonType == "STG"){

	    if (java.lang.Double.isNaN(crval1+crval2)) {
	        throw new Error("Unable to find reference coordinates in FITS header");
	    }

	    wcsKeys.put("CRVAL1", crval1);
	    wcsKeys.put("CRVAL2", crval2);

	    val lonpole = h.getDoubleValue("LONPOLE", Double.NaN);
		if (!java.lang.Double.isNaN(lonpole)) {
		    wcsKeys.put("LONPOLE", lonpole);
		}

		val typ = lonType.substring(0,1)+lonType.substring(1,3).toLowerCase();
	    new Projection(typ, Array(Angle.D2R * crval1, Angle.D2R * crval2));
	} else if (lonType == "NCP") {

	        // Sin projection with projection centered at pole.
		val xproj = Array(math.toRadians(crval1), math.Pi/2);
		if (crval2 < 0) {
		    xproj(1) = - xproj(1);
		}


		val poleOffset = math.sin(xproj(1)-Angle.D2R * crval2);		

		// NCP scales the Y-axis to accommodate the distortion of the SIN projection away
        // from the pole.
		postProjectionDistorter = 
		  new Scaler(0, poleOffset, 1, 0, 0, 1).add(
		      new Scaler(0., 0., 1,0,0,1/math.sin( Angle.D2R* crval2 ) ) );

		
		// Have we handled South pole here?
	    new Projection("Sin", xproj);


	} else {
		throw new IllegalArgumentException("Unsupported projection type:"+lonType);
	}
  }

  /** 
   * Get the scaling between the projection plane and pixel coordinates 
   */
  val scaler = {
	// There are three ways that scaling information may be provided:
	//    CDELTn, CRPIXn, and CROTAn
	//    CDm_n, CRPIXn
	//    PCm_n, CDELTn, CRPIXn
	// We look for them in this sequence.
	//

	var crpix1 = h.getDoubleValue("CRPIX"+lonAxis, Double.NaN);
	var crpix2 = h.getDoubleValue("CRPIX"+latAxis, Double.NaN);
	wcsKeys.put("CRPIX1", crpix1);
	wcsKeys.put("CRPIX2", crpix2);
	if (java.lang.Double.isNaN(crpix1) || java.lang.Double.isNaN(crpix2)) {
	    throw new RuntimeException("CRPIXn not found in header");
	}

	// Note that in FITS files, the center of the first pixel is
	// assumed to be at coordinates 1,1.  Thus the corner of the image
	// is at pixels coordinates 1/2, 1/2.
	crpix1 -= 0.5;
	crpix2 -= 0.5;
	
	/**
	 * check what type of Scaler to use
	 */
	val cdelt1 = h.getDoubleValue("CDELT"+lonAxis, Double.NaN);
	val cdelt2 = h.getDoubleValue("CDELT"+latAxis, Double.NaN);
	wcsKeys.put("CDELT1", cdelt1);
	wcsKeys.put("CDELT2", cdelt2);
	if (!java.lang.Double.isNaN(cdelt1) && !java.lang.Double.isNaN(cdelt2)) {
	  /** Get the scaling when CDELT is specified */

	  var matrix = false
	  // We use 1 indexing to match better with the FITS files.
	  var m11, m12, m21, m22:Double = Double.NaN;

	  //We've got minimal information...  We might have more.
	   val crota = h.getDoubleValue("CROTA"+latAxis, Double.NaN);
	   if (!java.lang.Double.isNaN(crota) && crota != 0) {
	    wcsKeys.put("CROTA2", crota);
	    m11 =  math.cos(crota* Angle.D2R);
	    m12 =  math.sin(crota* Angle.D2R);
	    m21 = -math.sin(crota* Angle.D2R);
	    m22 =  math.cos(crota* Angle.D2R);
	    matrix = true;
	} else {
	    m11 = h.getDoubleValue("PC"+lonAxis+"_"+lonAxis, Double.NaN);
	    m12 = h.getDoubleValue("PC"+lonAxis+"_"+latAxis, Double.NaN);
	    m21 = h.getDoubleValue("PC"+latAxis+"_"+lonAxis, Double.NaN);
	    m22 = h.getDoubleValue("PC"+latAxis+"_"+latAxis, Double.NaN);
	    matrix = ! java.lang.Double.isNaN(m11+m12+m21+m22);
	    if (matrix) {
	    	wcsKeys.put("PC1_1", m11);
	    	wcsKeys.put("PC1_2", m12);
	    	wcsKeys.put("PC2_1", m21);
	    	wcsKeys.put("PC2_2", m22);
	    }
	}


	// Note that Scaler is defined with parameters t = x0 + a00 x + a01 y; u = y0 + a10 x + a11 y
	// which is different from what we have here...
	//    t = scalex (x-x0),  u = scaley (y-y0)
	//    t = scalex x - scalex x0; u = scaley y - scaley y0
	// or
	//    t = scalex [a11 (x-x0) + a12 (y-y0)], u = scaley [a21 (x-x0) + a22 (y-y0)] ->
	//       t = scalex a11 x - scalex a11 x0 + scalex a12 y + scalex a12 y0         ->
        //       t = - scalex (a11 x0 + a12 y0) + scalex a11 x + scalex a12 y (and similarly for u)

	
	val cdelt1R = Angle.D2R * cdelt1;
	val cdelt2R = Angle.D2R * cdelt2;
	var scaler = if (!matrix) {
	    new Scaler(-cdelt1R*crpix1,-cdelt2R*crpix2,
			    cdelt1R, 0, 0,  cdelt2R);
	} else {
	    new Scaler(-cdelt1R*(m11*crpix1+m12*crpix2), -cdelt2R*(m21*crpix1+m22*crpix2),
				   cdelt1R*m11, cdelt1R*m12, cdelt2R*m21,cdelt2R*m22);
	}
	// Note that this scaler transforms from pixel coordinates to standard projection
	// plane coordinates.  We want the inverse transformation as the scaler.
	scaler = scaler.inverse

	// Are lon and lat in unusual order?
	if (lonAxis > latAxis) {
	    scaler = scaler.interchangeAxes;
	}
	scaler

	}else{
		/**
	  	 * check that matrix is in use
	  	 */
		var m11 = h.getDoubleValue("CD"+lonAxis+"_"+lonAxis, Double.NaN);
		var m12 = h.getDoubleValue("CD"+lonAxis+"_"+latAxis, Double.NaN);
		var m21 = h.getDoubleValue("CD"+latAxis+"_"+lonAxis, Double.NaN);
		var m22 = h.getDoubleValue("CD"+latAxis+"_"+latAxis, Double.NaN);
	    
	    if (java.lang.Double.isNaN(m11+m12+m21+m22)) {
	      throw new RuntimeException("No scaling information found in FITS header");
	    }
	    wcsKeys.put("PC1_1", m11);
	    wcsKeys.put("PC1_2", m12);
	    wcsKeys.put("PC2_1", m21);
	    wcsKeys.put("PC2_2", m22);
	    m11 = math.toRadians(m11);
	    m12 = math.toRadians(m12);
	    m21 = math.toRadians(m21);
	    m22 = math.toRadians(m22);
	    // we have
        //   t = a11 (x-x0) + a12 (y-y0); u = a21(x-x0) + a22(y-y0)
	    //    t = a11x + a12y - a11 x0 - a12 y0;
	    //
	    var s = new Scaler(-m11*crpix1 - m12*crpix2, -m21*crpix1 - m22*crpix2,
			      m11, m12, m21, m22);

	    s = s.inverse;

	    // Are longitude and latitude in unusual order?
	    if (lonAxis > latAxis) 
	    	s = s.interchangeAxes;
	    s
	}
	  	 
  }
	
  new WCSFits(coordinateSystem,projection,postProjectionDistorter,scaler,
      wcsKeys,headerNaxis)
  }

}

class WCSFits(
    csys: CoordinateSystem, proj: Projection, sphereDistorter:Distorter = null, scale: Scaler,
    wcsKeys: java.util.Map[String,Any], val headerNaxis:Array[Int]
    ) extends WCS(csys,proj,sphereDistorter,scale){
  
    def copyToHeader(h:Header) {

    	val srt = wcsKeys.keySet.toArray();
    	java.util.Arrays.sort(srt);
    	for(key<- srt) {
	    val o = wcsKeys.get(key);
	    if (o.isInstanceOf[Int]) {
	    	h.addValue(key.toString, o.isInstanceOf[java.lang.Integer], "Copied WCS eleemnt");
	    } else if (o.isInstanceOf[Double]) {
	    	h.addValue(key.toString, o.asInstanceOf[java.lang.Double], "Copied WCS element");
	    } else if (o.isInstanceOf[String]) {
	    	h.addValue(key.toString, o.asInstanceOf[String], "Copied WCS element");
	    }
	}
    }
}
