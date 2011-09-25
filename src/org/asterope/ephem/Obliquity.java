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


import org.asterope.ephem.Precession.Method;


/**
 * Calculation of mean and true obliquity. Different methods can be applied, in
 * an unique system of EphemUtils. compatible with {@linkplain EphemerisElement}
 * class.
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class Obliquity
{
//
//	/**
//	 * EphemUtils.ID for selecting Capitaine formula of obliquity. Astronomy &
//	 * Astrophysics 412, 567-586, 2003.
//	 */
//	public static final int OBLIQUITY_CAPITAINE = EphemerisElement.APPLY_IAU2009;
//
//	/**
//	 * EphemUtils.ID for selecting IAU2000 formula of obliquity.
//	 */
//	public static final int OBLIQUITY_IAU2000 = EphemerisElement.APPLY_IAU2000;
//
//	/**
//	 * EphemUtils.ID for selecting Williams formula of obliquity (DE403 JPL
//	 * Ephemeris). Astron J 108 (1994).
//	 */
//	public static final int OBLIQUITY_WILLIAMS = EphemerisElement.APPLY_WILLIAMS;
//
//	/**
//	 * EphemUtils.ID for selecting JPL DE403 formula of obliquity.
//	 */
//	public static final int OBLIQUITY_JPLDE403 = EphemerisElement.APPLY_JPLDE403;
//
//	/**
//	 * EphemUtils.ID for selecting Simon formula of obliquity.
//	 */
//	public static final int OBLIQUITY_SIMON = EphemerisElement.APPLY_SIMON;
//
//	/**
//	 * EphemUtils.ID for selecting Laskar formula of obliquity.
//	 */
//	public static final int OBLIQUITY_LASKAR = EphemerisElement.APPLY_LASKAR;

	/**
	 * Calculates the mean obliquity at a given time.
	 * <P>
	 * The code for this method comes from S. L. Moshier.
	 * <P>
	 * References:
	 * <P>
	 * Capitaine et al., Astronomy and Astrophysics 412, 567-586, (2003).
	 * <P>
	 * James G. Williams, "Contributions to the Earth's obliquity rate,
	 * precession, and nutation," Astron. J. 108, 711-724 (1994).
	 * <P>
	 * J. L. Simon, P. Bretagnon, J. Chapront, M. Chapront-Touze', G. Francou,
	 * and J. Laskar, "Numerical Expressions for precession formulae and mean
	 * elements for the Moon and the planets," Astronomy and Astrophysics 282,
	 * 663-683 (1994).
	 * <P>
	 * J. H. Lieske, T. Lederle, W. Fricke, and B. Morando, "Expressions for the
	 * Precession Quantities Based upon the IAU (1976) System of Astronomical
	 * EphemUtils.," Astronomy and Astrophysics 58, 1-16 (1977).
	 * <P>
	 * J. Laskar's expansion comes from "Secular terms of classical planetary
	 * theories using the results of general theory," Astronomy and Astrophysics
	 * 157, 59070 (1986).
	 * 
	 * @param t Time in julian centuries from J2000.<BR>
	 *        Valid range is the years -8000 to +12000 (t = -100 to 100).
	 * @param type Time of formula. Can be  {@linkplain Obliquity#OBLIQUITY_CAPITAINE},
	 *        {@linkplain Obliquity#OBLIQUITY_IAU2000}, {@linkplain Obliquity#OBLIQUITY_JPLDE403},
	 *        {@linkplain Obliquity#OBLIQUITY_SIMON}, {@linkplain Obliquity#OBLIQUITY_WILLIAMS}, 
	 *        and {@linkplain Obliquity#OBLIQUITY_LASKAR}.
	 * @return The mean obliquity (epsilon sub 0) in radians.
	 */
	public static double meanObliquity(double t, Method type)
	{

		// The obliquity formula come from Meeus, Astro Algorithms, 2ed.

		double rval = 0.;
		double u, u0;
		double t0 = 30000.;

		// Capitaine et al. 2003
		final double rvalStart_CAP = 23. * EphemConstant.SECONDS_PER_DEGREE + 26. * EphemConstant.MINUTES_PER_DEGREE + 21.4059;
		final double coeffs_CAP[] =
		{ -468367.69, -183.1, 200340., -5760., -43400., 0.0, 0.0, 0.0, 0.0, 0.0 };

		// Simon et al., 1994
		final int OBLIQ_COEFFS = 10;
		final double rvalStart_SIM = 23. * EphemConstant.SECONDS_PER_DEGREE + 26. * EphemConstant.MINUTES_PER_DEGREE + 21.412;
		final double coeffs_SIM[] =
		{ -468092.7, -152., 199890., -5138., -24967., -3905., 712., 2787., 579., 245. };

		// Williams et al., DE403 Ephemeris
		final double rvalStart_WIL = 23. * EphemConstant.SECONDS_PER_DEGREE + 26. * EphemConstant.MINUTES_PER_DEGREE + 21.406173;
		final double coeffs_WIL[] =
		{ -468339.6, -175., 199890., -5138., -24967., -3905., 712., 2787., 579., 245. };

		// Laskar et al.
		/*
		 * This expansion is from Laskar, cited above. Bretagnon and Simon say,
		 * in Planetary Programs and Tables, that it is accurate to 0.1" over a
		 * span of 6000 years. Laskar estimates the precision to be 0.01" after
		 * 1000 years and a few seconds of arc after 10000 years.
		 */
		final double rvalStart_IAU = 23. * EphemConstant.SECONDS_PER_DEGREE + 26. * EphemConstant.MINUTES_PER_DEGREE + 21.448;
		final double coeffs_IAU[] =
		{ -468093., -155., 199925., -5138., -24967., -3905., 712., 2787., 579., 245. };

		double rvalStart = rvalStart_SIM;
		double coeffs[] = coeffs_SIM;

		// Select the desired formula
		switch (type)
		{
		case CAPITAINE:
			rvalStart = rvalStart_CAP;
			coeffs = coeffs_CAP;
			break;
		case IAU2000:
			rvalStart = rvalStart_CAP;
			coeffs = coeffs_CAP;
			break;
		case WILLIAMS:
			rvalStart = rvalStart_WIL;
			coeffs = coeffs_WIL;
			break;
		case JPLDE403:
			rvalStart = rvalStart_WIL;
			coeffs = coeffs_WIL;
			break;
		case SIMON:
			rvalStart = rvalStart_SIM;
			coeffs = coeffs_SIM;
			break;
		case LASKAR:
			rvalStart = rvalStart_IAU;
			coeffs = coeffs_IAU;
			break;
		}

		if (t0 != t)
		{

			t0 = t;
			u = u0 = t / 100.; // u is in julian 10000's of years
			rval = rvalStart;

			for (int i = 0; i < OBLIQ_COEFFS; i++)
			{
				rval += u * coeffs[i] / 100.;
				u *= u0;
			}

			// convert from seconds to radians
			rval = rval * EphemConstant.ARCSEC_TO_RAD;
		}

		return rval;
	}

	/**
	 * Calculate true obliquity applying the corresponding nutation theory.
	 * 
	 * @param t Julian centuries from J2000.
	 * @param type Method for mean obliquity and nutation.
	 * @return true obliquity.
	 */
	public static double trueObliquity(double t, Method type)
	{
		double meanObliquity = meanObliquity(t, type);
		Nutation.calcNutation(t, Nutation.getNutationTheory(type));
		double trueObliquity = meanObliquity + Nutation.nutationInObliquity;

		return trueObliquity;
	}
	

}
