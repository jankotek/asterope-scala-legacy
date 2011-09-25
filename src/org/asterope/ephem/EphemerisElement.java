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
 * Support class to perform ephemerides calculations. This class provides
 * methods and fields that can be passed to any ephemeris calculation method to
 * obtain certain kind of ephemeris results. Output ephemeris type ({@linkplain
 * EphemerisElement#ephemType}), output equinox ({@linkplain EphemerisElement#equinox}), ephem methods to apply 
 * ({@linkplain EphemerisElement#ephemMethod}), topocentric or geocentric ephemeris ({@linkplain 
 * EphemerisElement#isTopocentric}), frame of the results ({@linkplain EphemerisElement#frame}), and of
 * course the object target of the ephemeris ({@linkplain EphemerisElement#targetBody}) are some of the
 * selectable parameters. These fields are lowercase, while uppercase parameters
 * are possible values of each other.
 * <P>
 * The types of ephemerides are geometric, astrometric, and apparent.
 * <P>
 * Geometric positions refer to the true positions of the body in the Solar
 * System, without light-time or any other corrections. Sometimes used to obtain
 * mean coordinates refered to certain equinox.
 * <P>
 * Astrometric positions are those corrected for light-time, but not for
 * aberrations, nutation, or deflection. They are mean positions refered to
 * certain equinox (J2000, equinox of the date, or any other) that can be
 * compared to catalog positions refered to the same epoch.
 * <P>
 * Apparent positions are the positions of the object in the sky as seen by the
 * observer. Typically topocentric, but geocentric apparent position can be
 * obtained as well.
 * <P>
 * Reference frames available are dynamical J2000 and ICRS. ICRS is the new
 * reference frame to be officially adopted by IAU shortly, recommended in the
 * IERS Conventions 2003. The use of the ICRS frame should include the IAU2000
 * formulae for precession, nutation, and Greenwich mean sidereal time. Note
 * that the official ephemeris adopted by the IAU are JPL DE406. In this library
 * these ephemerides are available. ICRS frame is supposed to be adopted by 
 * official sources (ephemeris servers, almanacs) shortly.
 * <P>
 * Note that all these selections depends on each other, and incorrect
 * decissions could give results different from other sources. On the other
 * hand, some other sources sometimes makes a mixture of precise algoriths with
 * classical ones (IAU 1980 theory of nutation, or even the Lieske precession
 * algorithm, here substituted by the more adequate Laskar expression). Note
 * that the new precession theory by Capitaine et al. 2003 is the one
 * recommended in the report of the IAU Division I Working Group on Precession
 * and the Ecliptic (Hilton et al. 2006, Celest. Mech., 94, 351-367). The
 * recomendation is to adopt this theory replacing the inconsistent precession
 * part of the IAU2000A precession-nutation model in 2009. Here, we have
 * directly applied Capitaine et al. theory, although support to the original
 * IAU2000 model is also given.
 * <P>
 * This library provides results with and accuracy up to the milliarcsecond
 * level when comparing to JPL DE403 ephemeris, using Series96 from the IMCCE
 * and selecting the adequate ephemeris parameters. The accuracy decreases when
 * comparing to the new long time spand ephemeris JPL DE406, but it is generally
 * in the arcsecond level. Accuracy is similar in the other calculations
 * methods, respect to the corresponding fit to JPL ephemeris version (DE200 in
 * VSOP and ELP theories). Results from Moshier are well below the arcsecond
 * level, even when comparing to JPL DE406, but the discrepancy is greater (not
 * too much) for Pluto in ancient dates. The limited accuracy of the coordinates
 * obtained with each theory is the cause of the subsecuent discrepancies.
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class EphemerisElement implements Serializable
{

	/**
	 * 
	 */
	private static final long serialVersionUID = -5118870796735931257L;
	/**
	 * Default constructor. It defines an Ephemeris object by selecting Sun as
	 * target body, equinox of date for the results, topocentric coordinates,
	 * IAU2000 methods, apparent coordinates, and J2000 mean dynamical reference
	 * frame. Ephemeris algorithm is set to Moshier.
	 */
	public EphemerisElement()
	{
		targetBody = Target.Sun;
		equinox = EphemerisElement.EQUINOX_OF_DATE;
		isTopocentric = true;
		frame = EphemerisElement.Frame.J2000;
		ephemMethod = Precession.Method.IAU2000;
		ephemType = Ephem.APPARENT;
	}

	/**
	 * Explicit constructor for planets. It defines an Ephemeris object by
	 * giving the values of the different fields. Ephemeris algorithm is set to
	 * Moshier.
	 * 
	 * @param planet Planet ID constant.
	 * @param ephem_type Can be {@linkplain EphemerisElement#EPHEM_APPARENT}, {@linkplain 
	 *        EphemerisElement#EPHEM_ASTROMETRIC}, or {@linkplain EphemerisElement#EPHEM_GEOMETRIC}.
	 * @param equinox Can be {@linkplain EphemerisElement#EQUINOX_J2000}, {@linkplain 
	 *        EphemerisElement#EQUINOX_OF_DATE}, or any positive Julian day.
	 * @param topocentric True for topocentric results, false for geocentric.
	 * @param apply_method {@linkplain EphemerisElement#APPLY_JPLDE403}, {@linkplain 
	 *        EphemerisElement#APPLY_WILLIAMS}, {@linkplain EphemerisElement#APPLY_SIMON}, 
	 *        {@linkplain EphemerisElement#APPLY_LASKAR}, {@linkplain EphemerisElement#APPLY_IAU2000},
	 *        or {@linkplain EphemerisElement#APPLY_IAU2009}.
	 * @param frame Reference frame, {@linkplain EphemerisElement#FRAME_J2000} or 
	 *        {@linkplain EphemerisElement#ICRS}.
	 */
	public EphemerisElement(Target planet, Ephem ephem_type, double equinox, boolean topocentric, Precession.Method apply_method, 
			Frame frame)
	{
		targetBody = planet;
		this.equinox = equinox;
		isTopocentric = topocentric;
		ephemMethod = apply_method;
		ephemType = ephem_type;
		this.frame = frame;
	}

//	/**
//	 * Explicit constructor for any object. It defines an Ephemeris object by
//	 * giving the values of all the fields.
//	 * 
//	 * @param planet Planet ID constant.
//	 * @param ephem_type Can be {@linkplain EphemerisElement#EPHEM_APPARENT}, {@linkplain 
//	 *        EphemerisElement#EPHEM_ASTROMETRIC}, or {@linkplain EphemerisElement#EPHEM_GEOMETRIC}.
//	 * @param equinox Can be {@linkplain EphemerisElement#EQUINOX_J2000}, {@linkplain 
//	 *        EphemerisElement#EQUINOX_OF_DATE}, or any positive Julian day.
//	 * @param topocentric True for topocentric results, false for geocentric.
//	 * @param apply_method {@linkplain EphemerisElement#APPLY_JPLDE403}, {@linkplain 
//	 *        EphemerisElement#APPLY_WILLIAMS}, {@linkplain EphemerisElement#APPLY_SIMON}, 
//	 *        {@linkplain EphemerisElement#APPLY_LASKAR}, {@linkplain EphemerisElement#APPLY_IAU2000},
//	 *        or {@linkplain EphemerisElement#APPLY_IAU2009}.
//	 * @param frame Reference frame, {@linkplain EphemerisElement#FRAME_J2000} or 
//	 *        {@linkplain EphemerisElement#FRAME_ICRS}.
//	 * @param algorithm Algorithm to apply.
//	 * @param orbit Orbital Element set.
//	 */
//	public EphemerisElement(Planet planet, int ephem_type, double equinox, boolean topocentric, Precession.Method apply_method,
//			int frame)
//	{
//		targetBody = planet;
//		this.equinox = equinox;
//		isTopocentric = topocentric;
//		ephemMethod = apply_method;
//		ephemType = ephem_type;
//		this.frame = frame;
//		this.orbit = orbit;
//	}

	/**
	 * Planet ID for ephemeris, probe index value when calculation orbits or
	 * probes, or satellite index for orbits of artificial satellites.
	 */
	public Target targetBody;

	/**
	 * Set equinox of the results as a Julian day in Terrestrial Time.
	 */
	public double equinox;

	/**
	 * True for topocentric ephemeris, false for geocentric.
	 */
	public boolean isTopocentric;

	/**
	 * Methods for Greenwich Mean Sidereal Time, Precession, and Obliquity.
	 */
	public Precession.Method ephemMethod;

	/**
	 * Defines the frame reference of the results. 
	 * 
	 */
	public Frame frame;

	/**
	 * Type of ephemeris. 
	 */
	public Ephem ephemType;


//	/**
//	 * Holds the orbital elements of a given body.
//	 */
//	public OrbitalElement orbit;

	/**
	 * Constant for selecting equinox of date results.
	 */
	public static final double EQUINOX_OF_DATE = -1.0;

	/**
	 * Constant for selecting equinox of J2000 as the reference equinox for
	 * results.
	 */
	public static final double EQUINOX_J2000 = EphemConstant.J2000;

	
	public enum Ephem{
	
		/**
		 * Constant ID for astrometric ephemeris calculation.
		 * Position of an object corrected in such a way, that it can directly be plotted into a star c
		 * hart for a given epoch (usually J2000). This is the geometric position, corrected for light-time. 
		 */
		ASTROMETRIC,

		/**
		 * Constant ID for geometric ephemeris calculation.
		 * Celestial coordinates referring to the center of the Earth without correction of planetary aberration.
		 */
		GEOMETRIC,

		/**
		 * Constant ID for apparent ephemeris calculation.
		 * Celestial coordinates which are directly observable. Corrected for various effects: i.e. light-time, 
		 * light deflection due to effects of relativity, planetary aberration, and corresponding to the true
		 * equator and equinox. Topocentric apparent coordinates include effects of refraction (if not assumed airless) 
		 * and diurnal aberration (perspective displacement from the earth mass center).
		 */
		APPARENT
	}

	/**
	 * Value for topocentric results.
	 */
	public static final boolean TOPOCENTRIC = true;

	/**
	 * Value for geocentric results.
	 */
	public static final boolean GEOCENTRIC = false;

	
	public enum Frame {
		/**
		 * Value ID for J2000 mean dynamical reference frame. This is the frame
		 * reference of the results for the JPL ephemeris and other sources, but it
		 * is expected to be replaced by the ICRS shortly.
		 */
		J2000,

		/**
	 	* Value ID for ICRS reference frame. An offset is conveniently applied to
	 	* transform from J2000 mean (dynamical) frame. This correction is
	 	* convenient for apparent true positions in the sky refered to the ICRS
	 	* system.
	 	*/
		ICRS
	}


	/**
	 * Set to true for using always IAU2000A Nutation model.
	 */
//	public static boolean useAlwaysNutationIAU2000 = false;

	/**
	 * To clone the object.
	 */
	public Object clone()
	{
		// Create new EphemerisElement identical to the input
		EphemerisElement new_eph = new EphemerisElement(this.targetBody, this.ephemType, this.equinox,
				this.isTopocentric, this.ephemMethod, this.frame/*, this.orbit*/);

		return new_eph;
	}
	/**
	 * Returns if the input object is equals to this ephemeris object.
	 */
	public boolean equals(Object eph)
	{
		if (eph == null) {
			return false;
		}
		boolean equals = true;
		EphemerisElement e = (EphemerisElement) eph;
		if (e.targetBody != this.targetBody) equals = false;
		if (e.ephemType != this.ephemType) equals = false;
		if (e.ephemMethod != this.ephemMethod) equals = false;
		if (e.equinox != this.equinox) equals = false;
		if (e.frame != this.frame) equals = false;
		if (e.isTopocentric != this.isTopocentric) equals = false;
		//if (!e.orbit.equals(this.orbit)) equals = false;
		return equals;
	}
}
