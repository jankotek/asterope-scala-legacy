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


/**
 * Calculate mean or apparent sidereal time through different methods.
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class SiderealTime
{
	/**
	 * Calculates the Greenwich mean sidereal time.
	 * <P>
	 * This function returns mean Greenwich sidereal time for the given Julian
	 * day in UT1.
	 * <P>
	 * Note: at epoch J2000.0, the 16 decimal precision of IEEE double precision
	 * numbers limits time resolution measured by Julian date to approximately
	 * 50 microseconds. References:
	 * <P>
	 * SOFA subroutine GMST2000.f.
	 * <P>
	 * Capitaine et al., Astronomy & Astrophysics 412, 567-586, EQ. (42).
	 * <P>
	 * James G. Williams, "Contributions to the Earth's obliquity rate,
	 * precession, and nutation," Astron. J. 108, 711-724 (1994).
	 * <P>
	 * J. H. Lieske, T. Lederle, W. Fricke, and B. Morando, "Expressions for the
	 * Precession Quantities Based upon the IAU (1976) System of Astronomical
	 * EphemEphemConstant.," Astronomy and Astrophysics 58, 1-16 (1977).
	 * <P>
	 * 
	 * @param time Time object containing the date.
	 * @param obs Observer object containing the observer position.
	 * @param eph Ephemeris object.
	 * @return Greenwich mean sidereal time in radians.
	 * @ If the date is invalid.
	 */
	public static double greenwichMeanSiderealTime(TimeElement time, ObserverElement obs, EphemerisElement eph)
			
	{

		double gmst = 0.0, msday = 0.0;

		// Obtain julian day in Universal Time
		double jd = TimeScale.getJD(time, obs, eph, TimeElement.Scale.UNIVERSAL_TIME_UT1);

		/* Correct Julian day to express it refered to the previous midnight */
		double jd0 = Math.floor(jd - 0.5) + 0.5;

		/* Julian centuries from J2000 at 0h UT */
		double T0 = (jd0 - EphemConstant.J2000) / EphemConstant.JULIAN_DAYS_PER_CENTURY;

		/* Obtain seconds elapsed from midnight */
		double secs = (jd - jd0) * EphemConstant.SECONDS_PER_DAY;

		if (eph.ephemMethod == Precession.Method.LASKAR)
		{

			/* This is the 1976 IAU formula. */
			gmst = ((-6.2e-6 * T0 + 9.3104e-2) * T0 + 8640184.812866) * T0 + 24110.54841;

			/* mean solar days per sidereal day at date T0 */
			msday = 1.0 + ((-1.86e-5 * T0 + 0.186208) * T0 + 8640184.812866) / (86400. * 36525.);

		} else
		{

			if (eph.ephemMethod ==  Precession.Method.IAU2000 || eph.ephemMethod ==  Precession.Method.CAPITAINE)
			{

				// Compute Earth rotation angle
				double DT0 = jd - EphemConstant.J2000;
				gmst = 2.0 * Math.PI * (secs / EphemConstant.SECONDS_PER_DAY + 0.5 + 0.7790572732640 + (EphemConstant.SIDEREAL_DAY_LENGTH - 1.0) * DT0);

				// Obtain julian day in Dynamical Time
				double jd_tdb = TimeScale.getJD(time, obs, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
				double DT = (jd_tdb - EphemConstant.J2000) / EphemConstant.JULIAN_DAYS_PER_CENTURY;

				// Precession contributions from Capitaine et al. 2003
				gmst += (0.014506 + (4612.15739966 + (+1.39667721 + (-0.00009344 + (+0.00001882) * DT) * DT) * DT) * DT) * EphemConstant.ARCSEC_TO_RAD;

				return EphemUtils.normalizeRadians(gmst);

			} else
			{
				/*
				 * J. G. Williams, "Contributions to the Earth's obliquity rate,
				 * precession, and nutation," Astronomical Journal 108, p. 711
				 * (1994)
				 */
				gmst = (((-2.0e-6 * T0 - 3.e-7) * T0 + 9.27695e-2) * T0 + 8640184.7928613) * T0 + 24110.54841;

				/* mean solar (er, UT) days per sidereal day at date T0 */
				msday = (((-(4. * 2.0e-6) * T0 - (3. * 3.e-7)) * T0 + (2. * 9.27695e-2)) * T0 + 8640184.7928613) / (86400. * 36525.) + 1.0;
			}
		}

		/* Greenwich mean sidereal time at given UT */
		gmst = gmst + msday * secs;

		/* To radians */
		gmst = EphemUtils.normalizeRadians(gmst * (15.0 / 3600.0) * EphemConstant.DEG_TO_RAD);

		return gmst;
	}

	/**
	 * Returns apparent sidereal time of the observer.
	 * 
	 * @param time Time object.
	 * @param obs Observer object.
	 * @param eph Ephemeris object.
	 * @return Apparent Sidereal Time in radians.
	 * @ If the date is invalid.
	 */
	public static double apparentSiderealTime(TimeElement time, ObserverElement obs, EphemerisElement eph)
			
	{
		// Obtain local apparent sidereal time
		double lst = SiderealTime.greenwichMeanSiderealTime(time, obs, eph) + obs.longitude + SiderealTime
				.equationOfEquinoxes(time, obs, eph);
		return lst;
	}

	/**
	 * Returns apparent sidereal time for Greenwich.
	 * 
	 * @param time Time object.
	 * @param obs Observer object.
	 * @param eph Ephemeris object.
	 * @return Apparent Sidereal Time in radians.
	 * @ If the date is invalid.
	 */
	public static double greenwichApparentSiderealTime(TimeElement time, ObserverElement obs, EphemerisElement eph)
			
	{
		// Obtain apparent sidereal time
		double lst = SiderealTime.greenwichMeanSiderealTime(time, obs, eph) + SiderealTime
				.equationOfEquinoxes(time, obs, eph);

		return lst;
	}

//	/**
//	 * Returns the equation of time for a given instant.
//	 * @param time Time object.
//	 * @return Equation of time in radians.
//	 * @ If an error occurs.
//	 */
//	public static double equationOfTime(TimeElement time)
//	 {
//		ObserverElement obs = ObserverElement.parseCity(new CityElement("Madrid"));
//		EphemerisElement eph = new EphemerisElement(Target.SUN, EphemerisElement.EPHEM_APPARENT,
//				EphemerisElement.EQUINOX_OF_DATE, EphemerisElement.GEOCENTRIC, EphemerisElement.APPLY_IAU2009,
//				EphemerisElement.FRAME_J2000);
//
//		double jd = TimeScale.getJD(time, obs, eph, TimeScale.JD_TDB);
//		double t = (jd - EphemEphemConstant.J2000) / EphemEphemConstant.JULIAN_DAYS_PER_CENTURY;
//		double lon = EphemUtils.normalizeDegrees(280.46645 + 36000.76983 * t + .0003032 * t * t) * EphemEphemConstant.DEG_TO_RAD;
//		double incl = (23.43929111 - .013004167 * t -.00000016389 * t * t + .0000005036 * t * t * t) * EphemEphemConstant.DEG_TO_RAD;
//		double anom = (357.5291 + 35999.0503 * t - .0001559 * t * t - .00000048 * t * t * t) * EphemEphemConstant.DEG_TO_RAD;
//		double exc = .016708617 - .000042037 * t - .0000001236 * t * t;
//		double y = Math.tan(incl / 2.0) * Math.tan(incl / 2.0);
//
//		double eqTime = y * Math.sin(2.0 * lon) - 2.0 * exc * Math.sin(anom) + 4.0 * exc * y * Math.sin(anom) * Math.cos(2.0 * lon);
//		eqTime = eqTime - .5 * y * y * Math.sin(4.0 * lon) - 1.25 * exc * exc * Math.sin(2.0 * anom);
//		eqTime = EphemUtils.normalizeRadians(eqTime);
//		if (eqTime > Math.PI) eqTime -= EphemEphemConstant.TWO_PI;
//		
//		return eqTime;
//	}
//	
	/**
	 * Returns equation of equinoxes. Complementary terms are included in case
	 * of IAU 2000 method.
	 * 
	 * @param time Time object.
	 * @param obs Observer object.
	 * @param eph Ephemeris object.
	 * @return Equation of Equinoxes in radians.
	 * @ If the date is invalid.
	 */
	public static double equationOfEquinoxes(TimeElement time, ObserverElement obs, EphemerisElement eph)
			
	{
		// Obtain mean obliquity
		double t = EphemUtils.toCenturies(TimeScale.getJD(time, obs, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME));
		double epsilon = Obliquity.meanObliquity(t, eph.ephemMethod);

		// Calculate Nutation
		Nutation.calcNutation(t, Nutation.getNutationTheory(eph.ephemMethod));

		// Obtain local apparent sidereal time
		double eq_eq = Nutation.nutationInLongitude * Math.cos(epsilon);

		// Add complementary terms if necessary
		if (eph.ephemMethod ==  Precession.Method.IAU2000 ||
				eph.ephemMethod ==  Precession.Method.CAPITAINE)
		{
			eq_eq += SiderealTime.eect(t);
		}

		return eq_eq;
	}

	static double EECT_last_value = 0.0;
	static double EECT_last_calc_T = -100.0;

	/**
	 * Complementary terms of equation of equinoxes from SOFA library.
	 * 
	 * @param T Julian centuries from J2000 in dynamical time.
	 * @return Value in radians.
	 */
	static double eect(double T)
	{
		if (T == EECT_last_calc_T)
			return EECT_last_value;
		EECT_last_calc_T = T;

		double ct = 0.0;

		/*
		 * Fundamental (Delaunay) arguments (from IERS Conventions 2003)
		 */

		// * Mean anomaly of the Moon.
		double EL = EphemUtils
				.mod3600(485868.249036 + T * (715923.2178 + T * (31.8792 + T * (0.051635 + T * (-0.00024470))))) * EphemConstant.ARCSEC_TO_RAD + EphemUtils
				.module(1325.0 * T, 1.0) * 2.0 * Math.PI;

		// * Mean anomaly of the Sun.
		double ELP = EphemUtils
				.mod3600(1287104.793048 + T * (129596581.0481 + T * (-0.5532 + T * (0.000136 + T * (-0.00001149))))) * EphemConstant.ARCSEC_TO_RAD + EphemUtils
				.module(99.0 * T, 1.0) * 2.0 * Math.PI;

		// * Mean argument of the latitude of the Moon.
		double F = EphemUtils
				.mod3600(335779.526232 + T * (295262.8478 + T * (-12.7512 + T * (-0.001037 + T * (0.00000417))))) * EphemConstant.ARCSEC_TO_RAD + EphemUtils
				.module(1342.0 * T, 1.0) * 2.0 * Math.PI;

		// * Mean elongation of the Moon from the Sun.
		double D = EphemUtils
				.mod3600(1072260.70369 + T * (1105601.2090 + T * (-6.3706 + T * (0.006593 + T * (-0.00003169))))) * EphemConstant.ARCSEC_TO_RAD + EphemUtils
				.module(1236.0 * T, 1.0) * 2.0 * Math.PI;

		// * Mean longitude of the ascending node of the Moon.
		double OM = EphemUtils
				.mod3600(450160.398036 + T * (-482890.5431 + T * (7.4722 + T * (0.007702 + T * (-0.00005939))))) * EphemConstant.ARCSEC_TO_RAD - EphemUtils
				.module(5.0 * T, 1.0) * 2.0 * Math.PI;

		// * Planetary longitudes, Mercury through Pluto.
		double ALME = EphemUtils.normalizeRadians(4.402608842 + 2608.7903141574 * T);
		double ALVE = EphemUtils.normalizeRadians(3.176146697 + 1021.3285546211 * T);
		double ALEA = EphemUtils.normalizeRadians(1.753470314 + 628.3075849991 * T);
		double ALMA = EphemUtils.normalizeRadians(6.203480913 + 334.0612426700 * T);
		double ALJU = EphemUtils.normalizeRadians(0.599546497 + 52.9690962641 * T);
		double ALSA = EphemUtils.normalizeRadians(0.874016757 + 21.3299104960 * T);
		double ALUR = EphemUtils.normalizeRadians(5.481293872 + 7.4781598567 * T);
		double ALNE = EphemUtils.normalizeRadians(5.311886287 + 3.8133035638 * T);
		double ALPL = EphemUtils.normalizeRadians(0.024381750 + 0.00000538691 * T);

		double FA[] =
		{ EL, ELP, F, D, OM, ALME, ALVE, ALEA, ALMA, ALJU, ALSA, ALUR, ALNE, ALPL };

		// Evaluate the EE complementary terms.
		double S0 = 0.0;
		double S1 = 0.0;

		for (int i = 32; i >= 0; i--)
		{
			double A = 0.0;
			for (int j = 0; j <= 13; j++)
			{
				A = A + eect00.KE0[i][j] * FA[j];
			}
			S0 = S0 + (eect00.SE0[i][0] * Math.sin(A) + eect00.SE0[i][1] * Math.cos(A));
		}

		for (int i = 0; i >= 0; i--)
		{
			double A = 0.0;
			for (int j = 0; j <= 13; j++)
			{
				A = A + eect00.KE1[j] * FA[j];
			}
			S1 = S1 + (eect00.SE1[0] * Math.sin(A) + eect00.SE1[1] * Math.cos(A));
		}

		ct = (S0 + S1 * T) * EphemConstant.ARCSEC_TO_RAD;

		EECT_last_value = ct;

		return ct;
	}

}

final class eect00
{
	// Argument coefficients for t^0
	static double KE0[][] =
	{
	// DATA ( ( KE0(I,J), I=1,14), J = 1, 10 ),
			{ 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 2, -2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 2, -2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 2, -2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 1, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 1, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			// DATA ( ( KE0(I,J), I=1,14), J = 11, 20 ),
			{ 1, 0, 0, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 1, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 1, 2, -2, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 1, 2, -2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 4, -4, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 1, -1, 1, 0, -8, 12, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 2, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 1, 0, 2, 0, 3, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 1, 0, 2, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			// DATA ( ( KE0(I,J), I=1,14), J = 21, 30 ),
			{ 0, 0, 2, -2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 1, -2, 2, -3, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 1, -2, 2, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 0, 0, 0, 0, 8, -13, 0, 0, 0, 0, 0, -1 },
			{ 0, 0, 0, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 2, 0, -2, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 1, 0, 0, -2, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 1, 2, -2, 2, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 1, 0, 0, -2, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 0, 0, 4, -2, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			// DATA ( ( KE0(I,J), I=1,14), J = 31, NE0 ),
			{ 0, 0, 2, -2, 4, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 1, 0, -2, 0, -3, 0, 0, 0, 0, 0, 0, 0, 0, 0 },
			{ 1, 0, -2, 0, -1, 0, 0, 0, 0, 0, 0, 0, 0, 0 } };

	// Argument coefficients for t^1
	static double KE1[] =
	{ 0, 0, 0, 0, 1, 0, 0, 0, 0, 0, 0, 0, 0, 0 };

	// Sine and cosine coefficients for t^0
	static double SE0[][] =
	{
	// DATA ( ( SE0(I,J), I=1,2), J = 1, 10 ),
			{ +2640.96e-6, -0.39e-6 },
			{ +63.52e-6, -0.02e-6 },
			{ +11.75e-6, +0.01e-6 },
			{ +11.21e-6, +0.01e-6 },
			{ -4.55e-6, +0.00e-6 },
			{ +2.02e-6, +0.00e-6 },
			{ +1.98e-6, +0.00e-6 },
			{ -1.72e-6, +0.00e-6 },
			{ -1.41e-6, -0.01e-6 },
			{ -1.26e-6, -0.01e-6 },
			// DATA ( ( SE0(I,J), I=1,2), J = 11, 20 ),
			{ -0.63e-6, +0.00e-6 },
			{ -0.63e-6, +0.00e-6 },
			{ +0.46e-6, +0.00e-6 },
			{ +0.45e-6, +0.00e-6 },
			{ +0.36e-6, +0.00e-6 },
			{ -0.24e-6, -0.12e-6 },
			{ +0.32e-6, +0.00e-6 },
			{ +0.28e-6, +0.00e-6 },
			{ +0.27e-6, +0.00e-6 },
			{ +0.26e-6, +0.00e-6 },
			// DATA ( ( SE0(I,J), I=1,2), J = 21, 30 ),
			{ -0.21e-6, +0.00e-6 },
			{ +0.19e-6, +0.00e-6 },
			{ +0.18e-6, +0.00e-6 },
			{ -0.10e-6, +0.05e-6 },
			{ +0.15e-6, +0.00e-6 },
			{ -0.14e-6, +0.00e-6 },
			{ +0.14e-6, +0.00e-6 },
			{ -0.14e-6, +0.00e-6 },
			{ +0.14e-6, +0.00e-6 },
			{ +0.13e-6, +0.00e-6 },
			// DATA ( ( SE0(I,J), I=1,2), J = 31, NE0 ),
			{ -0.11e-6, +0.00e-6 },
			{ +0.11e-6, +0.00e-6 },
			{ +0.11e-6, +0.00e-6 }, };

	// Sine and cosine coefficients for t^1
	static double SE1[] =
	{ -0.87e-6, +0.00e-6, };

}
