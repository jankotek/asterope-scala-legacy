/*
 * This file is part of JPARSEC library.
 * 
 * (C) Copyright 2006-2009 by T. Alonso Albi - OAN (Spain).
 *  
 * Project Info:  http://conga.oan.es/~alonso/jparsec/jparsec.html
 * 
 * JPARSEC library is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * JPARSEC library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 */					
package org.asterope.ephem;

import java.io.Serializable;

/**
 * Convenient class for stars data access.
 * <P>
 * This class provides access to the data contained in any star
 * object. Below there's a list of available fields to access the
 * data.
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class StarElement implements Serializable
{

	private static final long serialVersionUID = 273880902224415699L;

	/**
	 * Constructs an star object providing the values of the fields.
	 * 
	 * @param nom Name.
	 * @param ra Right Ascension in radians.
	 * @param dec Declination in radians.
	 * @param r Distance to the observer in pc.
	 * @param mag Apparent magnitude.
	 * @param pm_ra Proper motion in RA.
	 * @param pm_dec Proper motion in DEC.
	 * @param pm_rv Radial velocity.
	 * @param eq Reference equinox.
	 * @param fr Reference frame.
	 */
	public StarElement(String nom, double ra, double dec, double r, double mag, double pm_ra, double pm_dec,
			double pm_rv, double eq, int fr)
	{
		rightAscension = ra;
		declination = dec;
		distance = r;
		magnitude = mag;
		properMotionRA = pm_ra;
		properMotionDEC = pm_dec;
		properMotionRadialV = pm_rv;
		equinox = eq;
		frame = fr;
		name = nom;
		spectrum = "";
		type = "";
	}

	/**
	 * Constructs an empty star object.
	 */
	public StarElement()
	{
		rightAscension = 0.0;
		declination = 0.0;
		distance = 0.0;
		properMotionRA = 0.0f;
		properMotionDEC = 0.0f;
		properMotionRadialV = 0.0f;
		equinox = 0.0;
		frame = -1;
		name = "";
		spectrum = "";
		type = "";
	}

	/**
	 * Name of the star.
	 */
	public String name;

	/**
	 * Right Ascension in radians from the catalogue.
	 */
	public double rightAscension;

	/**
	 * Declination in radians from the catalogue.
	 */
	public double declination;

	/**
	 * Distance to the observer in parsecs.
	 */
	public double distance;

	/**
	 * Apparent visual magnitude.
	 */
	public double magnitude;

	/**
	 * Displacement in Right Ascension in radians per Julian year, refered to
	 * the Solar System Barycenter. This movement is in terms of coordinate
	 * angle on the celestial sphere. In the catalogues (specially the old
	 * ones), it is sometimes measured in arcseconds per Julian year as a true
	 * angle. If it is the case, as in the Bright Star Calalogue, you will have
	 * to divide it by COS(DECLINATION). If you are in doubts, check Alp Umi
	 * (Polaris) in the catalogue. It's annual proper motion in RA is 3
	 * [coordinate "/year] = 0.03 [apparent "/year] / COS(89.2).
	 */
	public double properMotionRA;

	/**
	 * Displacement in Declination in radians per Julian year, refered to the
	 * Solar System Barycenter.
	 */
	public double properMotionDEC;

	/**
	 * Displacement in Radial velocity in km/s, refered to the Solar System
	 * Barycenter.
	 */
	public double properMotionRadialV;

	/**
	 * Reference equinox for the catalogue coordinates as a Julian day.
	 */
	public double equinox;

	/**
	 * Reference frame for the catalogue coordinates.
	 */
	public int frame;

	/**
	 * Spectrum type. Currently available only for JPARSEC file format.
	 */
	public String spectrum;

	/**
	 * Type of star: N for Normal, D for double or multiple, V for variable, and
	 * B for both double and variable. Only available for BSC5 and JPARSEC file
	 * formats. For JPARSEC file format additional information is available as three
	 * fields separated by ;. First field is one of the previous values N, D, V, B.
	 * Second is double star data (only if it is double or multiple). Third is variability
	 * data (if it is variable). Double data includes four fields separated by a comma
	 * (separation of main components in arcseconds, magnitude difference in components A-B,
	 * orbit period in years, position angle in degrees), while variability data includes 
	 * another four fields separated by a comma (maximum magnitude, minimum magnitude,
	 * period of variability in days, variable type).
	 */
	public String type;

	/**
	 * ID constant for ICRS frame.
	 */
	public static final int FRAME_ICRS = 0;

	/**
	 * ID constant for FK5 frame.
	 */
	public static final int FRAME_FK5 = 1;

	/**
	 * ID constant for FK4 frame.
	 */
	public static final int FRAME_FK4 = 2;

	/**
	 * Constant for selecting equinox of J2000 as the reference equinox for
	 * results.
	 */
	public static final double EQUINOX_J2000 = EphemConstant.J2000;

	/**
	 * Constant for selecting equinox of B1950 as the reference equinox for
	 * results.
	 */
	public static final double EQUINOX_B1950 = EphemConstant.B1950;

	/**
	 * To clone the object.
	 */
	public Object clone()
	{
		StarElement out = new StarElement(this.name, this.rightAscension, this.declination, this.distance,
				this.magnitude, this.properMotionRA, this.properMotionDEC, this.properMotionRadialV,
				this.equinox, this.frame);
		out.spectrum = this.spectrum;
		out.type = this.type;
		return out;
	}
	/**
	 * Returns true if the input object is equals to this
	 * instance.
	 */
	public boolean equals(Object s)
	{
		if (s == null) {
			return false;
		}
		boolean equals = true;
		StarElement se = (StarElement) s;
		if (se.properMotionRA != this.properMotionRA) equals = false;
		if (!se.name.equals(this.name)) equals = false;
		if (se.declination != this.declination) equals = false;
		if (se.distance != this.distance) equals = false;
		if (se.properMotionDEC != this.properMotionDEC) equals = false;
		if (se.magnitude != this.magnitude) equals = false;
		if (se.properMotionRadialV != this.properMotionRadialV) equals = false;
		if (se.rightAscension != this.rightAscension) equals = false;
		if (se.equinox != this.equinox) equals = false;
		if (se.frame != this.frame) equals = false;
		if (!se.spectrum.equals(this.spectrum)) equals = false;
		if (!se.type.equals(this.type)) equals = false;
		return equals;
	}

//	/**
//	 * Creates a star object from a SourceElement one.
//	 * 
//	 * @param source Source object.
//	 * @return Star object.
//	 */
//	public static StarElement parseSourceElement(SourceElement source)
//	{
//		StarElement star = new StarElement(source.name, source.rightAscension, source.declination, source.distance,
//				(double) source.magnitude, (double) source.properMotionRA, (double) source.properMotionDEC, (double) source.properMotionRadialV,
//				source.equinox, source.frame);
//
//		return star;
//	}
}

// end of class StarElement
