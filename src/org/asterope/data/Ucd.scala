package org.asterope.data

/** contains mostly used UCD identificators*/
object Ucd {
	
		/** Identifier, name or designation*/
	val ID = "meta.id";

	/** Identifier, name or designation*/
	val ID_MAIN = "meta.id;meta.main";
	
	
	
	/**Source classification (star, galaxy, cluster...)*/
	val CLASS = "src.class";
	
	/**Right ascension in equatorial coordinates*/
	val RA = "pos.eq.ra;meta.main";
	
	/**Declination in equatorial coordinates*/
	val DE = "pos.eq.dec;meta.main";
	
	/** Magnitude in  Optical band between 300 and 400 nm*/
	val MAG_U = "phot.mag;em.opt.U";
	
	/** Magnitude in  Optical band between 400 and 500 nm*/
	val MAG_B = "phot.mag;em.opt.B";
	
	/** Magnitude in  Optical band between 500 and 600 nm*/
	val MAG_V = "phot.mag;em.opt.V";
	
	/** Magnitude in  Optical band between 600 and 750 nm*/
	val MAG_R = "phot.mag;em.opt.R";
	
	/** Magnitude in  Optical band between 750 and 1000 nm*/
	val MAG_I = "phot.mag;em.opt.I";
	
	/** 
	 * surface brightness. In Asterope definition should be used mag/arcmin^2 from 
	 * "Third Reference Cat. of Bright Galaxies (RC3)"
	 */
	val SUBR = "phot.mag.sb";
	
	/** Angular size, width, diameter, dimension, extension, major minor axis, extraction radius*/
	val SIZE = "phys.angSize";
	
	/**Angular size, extent or extension of semi-major axis*/
	val SIZE_MAX = "phys.angSize.smajAxis";
	
	/**Angular size, extent or extension of semi-minor axis*/
	val SIZE_MIN = "phys.angSize.sminAxis";

	/**Position angle of a given vector (mostly rotation angle of major axis)*/
	val POS_ANGLE = "pos.posAng";
	
	


}