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
 * A class to obtain accurate ephemerides of stars.
 * <P>
 * To obtain star ephemeris follow these simple steps:
 * <P>
 * 
 * <pre>
 * // Read BSC5 or SKYMASTER 2000 catalogue
 * ReadElement roe = new ReadElement();
 * roe.setPath(PATH_TO_BSC5_FILE);
 * roe.setFormat(ReadElement.format_BSC5);
 * roe.readFileOfStars();
 * 
 * // Choose a star.
 * int my_star = roe.searchByName(&quot;Alp UMi&quot;);
 * StarElement star = (StarElement) roe.READ_ELEMENTS.elementAt(my_star);
 * 
 * // Calc ephemeris. 
 * StarEphemElement star_ephem = StarEphem.StarEphemeris(time, observer, eph, star, true);
 * </pre>
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class StarEphem
{
	
	/*
	 * Factors to eliminate E terms of aberration
	 */
	private static final double A[] = new double[]
	{ -1.62557e-6, -3.1919e-7, -1.3843e-7 };

	private static final double AD[] = new double[]
	{ 1.244e-3, -1.579e-3, -6.60e-4 }; // This is found in the original code by Moshier, from AA 1992
	//{ 1.245e-3, -1.580e-3, -6.59e-4 }; // This is found in AA 2004, based on articles from 1982-3

	/*
	 * Transformation matrix for rade2Vector direction vector, and motion vector in arc
	 * seconds per century
	 */
	private static final double MAT[] = new double[]
	{ 0.9999256782, -0.0111820611, -4.8579477e-3, 2.42395018e-6, -2.710663e-8, -1.177656e-8, 0.0111820610,
			0.9999374784, -2.71765e-5, 2.710663e-8, 2.42397878e-6, -6.587e-11, 4.8579479e-3, -2.71474e-5, 0.9999881997,
			1.177656e-8, -6.582e-11, 2.42410173e-6, -5.51e-4, -0.238565, 0.435739, 0.99994704, -0.01118251,
			-4.85767e-3, 0.238514, -2.667e-3, -8.541e-3, 0.01118251, 0.99995883, -2.718e-5, -0.435623, 0.012254,
			2.117e-3, 4.85767e-3, -2.714e-5, 1.00000956 };

	/**
	 * Converts FK4 B1950.0 catalogue coordinates to FK5 J2000.0 coordinates,
	 * supposing that the object is static.
	 * 
	 * @param loc Right Ascension and declination.
	 * @return Output coordinates.
	 */
	public static LocationElement transform_FK4_B1950_to_FK5_J2000(LocationElement loc)
	{
		StarElement star = new StarElement();
		star.rightAscension = loc.getLongitude();
		star.declination = loc.getLatitude();
		star.distance = loc.getRadius();
		star = StarEphem.transform_FK4_B1950_to_FK5_J2000(star);
		return new LocationElement(star.rightAscension, star.declination, loc.getRadius());
	}
	
	/**
	 * Converts FK4 B1950.0 catalogue coordinates to FK5 J2000.0 coordinates. AA
	 * page B58.
	 * <P>
	 * Method taken from C code by S. L. Moshier.
	 * 
	 * @param star Star input object.
	 * @return Output Star object.
	 */
	public static StarElement transform_FK4_B1950_to_FK5_J2000(StarElement star)
	{
		StarElement out = (StarElement) star.clone();

		LocationElement loc_FK4 = new LocationElement(star.rightAscension, star.declination, 1.0);
		double geo_eq_FK4[] = LocationElement.parseLocationElement(loc_FK4);

		/* space motion */
		double sindec = Math.sin(star.declination);
		double cosdec = Math.cos(star.declination);
		double cosra = Math.cos(star.rightAscension);
		double sinra = Math.sin(star.rightAscension);
		double vpi = 0.21094952663 * star.properMotionRadialV / (star.distance * EphemConstant.RAD_TO_ARCSEC);
		double m[] = new double[3];
		m[0] = -star.properMotionRA * cosdec * sinra - star.properMotionDEC * sindec * cosra + vpi * geo_eq_FK4[0];

		m[1] = star.properMotionRA * cosdec * cosra - star.properMotionDEC * sindec * sinra + vpi * geo_eq_FK4[1];

		m[2] = star.properMotionDEC * cosdec + vpi * geo_eq_FK4[2];

		double a, b, c;
		double R[] = new double[6];
		int i, j;

		a = 0.0;
		b = 0.0;
		for (i = 0; i < 3; i++)
		{
			m[i] *= 100.0 * EphemConstant.RAD_TO_ARCSEC;
			a += A[i] * geo_eq_FK4[i];
			b += AD[i] * geo_eq_FK4[i];
		}
		/*
		 * Remove E terms of aberration from FK4
		 */
		for (i = 0; i < 3; i++)
		{
			R[i] = geo_eq_FK4[i] - A[i] + a * geo_eq_FK4[i];
			R[i + 3] = m[i] - AD[i] + b * geo_eq_FK4[i];
		}

		/*
		 * Perform matrix multiplication
		 */
		double geo_eq_FK5[] = new double[3];
		double geo_eq_vel_FK5[] = new double[3];
		int v_mat = -1;
		for (i = 0; i < 6; i++)
		{
			a = 0.0;
			int u_R = -1;
			for (j = 0; j < 6; j++)
			{
				v_mat++;
				u_R++;
				a += R[u_R] * MAT[v_mat];
			}
			if (i < 3)
				geo_eq_FK5[i] = a;
			else
				geo_eq_vel_FK5[i - 3] = a;
		}

		/*
		 * Transform the answers into J2000 catalogue entries in radian measure.
		 */
		b = geo_eq_FK5[0] * geo_eq_FK5[0] + geo_eq_FK5[1] * geo_eq_FK5[1];
		a = b + geo_eq_FK5[2] * geo_eq_FK5[2];
		c = a;
		a = Math.sqrt(a);

		out.rightAscension = Math.atan2(geo_eq_FK5[1], geo_eq_FK5[0]);
		out.declination = Math.asin(geo_eq_FK5[2] / a);

		/* Note motion converted back to radians per (Julian) year */
		out.properMotionRA =  (0.01 * (geo_eq_FK5[0] * geo_eq_vel_FK5[1] - geo_eq_FK5[1] * geo_eq_vel_FK5[0]) / (EphemConstant.RAD_TO_ARCSEC * b));
		out.properMotionDEC =  (0.01 * (geo_eq_vel_FK5[2] * b - geo_eq_FK5[2] * (geo_eq_FK5[0] * geo_eq_vel_FK5[0] + geo_eq_FK5[1] * geo_eq_vel_FK5[1])) / (EphemConstant.RAD_TO_ARCSEC * c * Math
				.sqrt(b)));

		if (star.distance > 0.0)
		{
			c = 0.0;
			for (i = 0; i < 3; i++)
				c += geo_eq_FK5[i] * geo_eq_vel_FK5[i];

			/*
			 * divide by RTS to deconvert m (and therefore c) from arc seconds
			 * back to radians
			 */
			out.properMotionRadialV = (c / (21.094952663 * a / star.distance));
		}
		out.distance = star.distance * a;
		out.equinox = EphemConstant.J2000;

		return out;
	}

	
	/**
	 * Calculates ephemerides of stars. This method assumes that the star
	 * velocity is much lower than the speed of the light. This is a valid
	 * approximation for any star in our Galaxy, since velocities are below 1000
	 * km/s.
	 * <P>
	 * It is not recommended to use this method if the speed of the star is
	 * above 25% of the speed of light.
	 * 
	 * @param time Time object.
	 * @param obs Observer object.
	 * @param eph Ephemeris object.
	 * @param star Star object.
	 * @param fullEphemeris True for calculating full ephemeris, including rise,
	 *        set, transit times, constellation, and topocentric corrections.
	 * @param sunEphem is used to calculate geocentric position of Sun
	 * @return Output ephem object.

	 * @ Thrown if the calculation fails.
	 */
	public static StarEphemElement starEphemeris(TimeElement time, ObserverElement obs, EphemerisElement eph,
			StarElement star, boolean fullEphemeris, Ephem sunEphem)
	{
		//TODO check if EPH object is valid
		
		StarEphemElement out = new StarEphemElement();

		/*
		 * Convert from RA and Dec to equatorial rectangular direction
		 */
		/* Convert FK4 to FK5 catalogue */
		StarElement in = (StarElement) star.clone();
		if (star.frame == StarElement.FRAME_FK4)
		{
			in = StarEphem.transform_FK4_B1950_to_FK5_J2000(star);
		}
		LocationElement loc = new LocationElement(in.rightAscension, in.declination, 1.0);
		double q[] = LocationElement.parseLocationElement(loc);

		// Obtain julian day in Barycentric Dynamical Time
		double JD_TDB = TimeScale.getJD(time, obs, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
		EphemerisElement ephClone = (EphemerisElement) eph.clone();
		ephClone.targetBody = Target.Sun;
//		if (ephClone.algorithm == EphemerisElement.ALGORITHM_SERIES96_MOSHIERFORMOON && (JD_TDB < 2415020.5 || JD_TDB > 2488092.5))
//		{
//			ephClone.algorithm = EphemerisElement.ALGORITHM_MOSHIER;
//		}
//		if (ephClone.algorithm != EphemerisElement.ALGORITHM_SERIES96_MOSHIERFORMOON && ephClone.algorithm != EphemerisElement.ALGORITHM_MOSHIER)
//		{
//			ephClone.algorithm = EphemerisElement.ALGORITHM_MOSHIER;
//		}
		double e[] = new double[6];
		double e_j2000[] = new double[6];
//		if (ephClone.algorithm == EphemerisElement.ALGORITHM_SERIES96_MOSHIERFORMOON)
//		{
//			e_j2000 = Series96.getGeocentricPosition(JD_TDB, ephClone.targetBody, 0.0);
//		} else
//		{
			e = sunEphem.getGeocentricPosition(JD_TDB, ephClone.targetBody, 0.0);
			e_j2000 = EphemUtils.eclipticToEquatorial(e, JD_TDB, ephClone.ephemMethod);
//		}
		e = Precession.precessFromJ2000(in.equinox, e_j2000, ephClone.ephemMethod);

		/* space motion */
		double sindec = Math.sin(in.declination);
		double cosdec = Math.cos(in.declination);
		double cosra = Math.cos(in.rightAscension);
		double sinra = Math.sin(in.rightAscension);
		double vpi = 0.21094952663 * in.properMotionRadialV / (in.distance * EphemConstant.RAD_TO_ARCSEC);
		double m[] = new double[3];
		m[0] = -in.properMotionRA * cosdec * sinra - in.properMotionDEC * sindec * cosra + vpi * q[0];

		m[1] = in.properMotionRA * cosdec * cosra - in.properMotionDEC * sindec * sinra + vpi * q[1];

		m[2] = in.properMotionDEC * cosdec + vpi * q[2];

		// Add warning for possible incorrect result when the speed is too high
		// (25% c)
		double speed_check = in.properMotionRadialV * LocationElement.parseRectangularCoordinates(m).getRadius() / vpi;
		if (speed_check > (0.00025 * EphemConstant.SPEED_OF_LIGHT))
		{ //TODO is it 25% or 0.025% of speed?
			throw new IllegalArgumentException("the speed of the star " + star.name + " is " + speed_check + " km/s, which seems to be very high. Ephemeris could be wrong.");
		}

		/*
		 * Correct for proper motion and parallax
		 */
		double T = (JD_TDB - in.equinox) * 100.0 / EphemConstant.JULIAN_DAYS_PER_CENTURY;
		double p[] = new double[3];

		// Correction for differential light time
		boolean correction = true;
		if (ephClone.ephemType == EphemerisElement.Ephem.GEOMETRIC)
			correction = false;
		double light_time_before = in.distance * EphemConstant.RAD_TO_ARCSEC * EphemConstant.LIGHT_TIME_DAYS_PER_AU;
		double dT = 0.0;
		double ddT = 0.0;
		do
		{
			for (int i = 0; i < 3; i++)
			{
				p[i] = q[i] + (T + dT) * m[i] + e[i] / (in.distance * EphemConstant.RAD_TO_ARCSEC);
			}
			double norm = Math.sqrt(p[0] * p[0] + p[1] * p[1] + p[2] * p[2]);
			double light_time_now = in.distance * norm * EphemConstant.RAD_TO_ARCSEC * EphemConstant.LIGHT_TIME_DAYS_PER_AU;
			ddT = dT;
			dT = (light_time_now - light_time_before) * 100.0 / EphemConstant.JULIAN_DAYS_PER_CENTURY;
			ddT -= dT;
		} while (Math.abs(ddT) > (100.0 / (EphemConstant.SECONDS_PER_DAY * EphemConstant.JULIAN_DAYS_PER_CENTURY)) && correction);

		/* precess the star to J2000 equinox */
		p = Precession.precessToJ2000(in.equinox, p, ephClone.ephemMethod);
		e = e_j2000;

		/*
		 * Find vector from earth in direction of object, in AU units
		 */
		double EO = in.distance * EphemConstant.RAD_TO_ARCSEC;
		for (int i = 0; i < 3; i++)
		{
			p[i] = p[i] * EO;
		}

		// Correct for solar deflection and aberration
		if (ephClone.ephemType == EphemerisElement.Ephem.APPARENT)
		{
			LocationElement loc_p = LocationElement.parseRectangularCoordinates(p);
			double light_time = loc_p.getRadius() * EphemConstant.LIGHT_TIME_DAYS_PER_AU;
			p = EphemUtils.solarDeflection(p, e, p);
			p = EphemUtils.aberration(p, e, light_time);
		}

		/*
		 * Correct frame bias in J2000 epoch
		 */
		if (ephClone.frame == EphemerisElement.Frame.ICRS && star.frame != StarElement.FRAME_ICRS)
			p = EphemUtils.toICRSFrame(p);
		if (ephClone.frame == EphemerisElement.Frame.J2000 && star.frame == StarElement.FRAME_ICRS)
			p = EphemUtils.toJ2000Frame(p);

		// Transform from J2000 to mean equinox of date
		double geo_date[] = Precession.precessFromJ2000(JD_TDB, p, ephClone.ephemMethod);

		// Mean equatorial to true equatorial
		double true_eq[] = geo_date;
		if (ephClone.ephemType == EphemerisElement.Ephem.APPARENT)
		{
			/* Correct nutation */
			true_eq = Nutation.calcNutation(JD_TDB, geo_date, ephClone.ephemMethod, 
					Nutation.getNutationTheory(ephClone.ephemMethod));
		}

		/* Set coordinates to the output equinox */
		if (EphemerisElement.EQUINOX_OF_DATE != ephClone.equinox)
		{
			true_eq = Precession.precess(JD_TDB, ephClone.equinox, true_eq, ephClone.ephemMethod);
		}

		// Get equatorial coordinates
		LocationElement ephem_loc = LocationElement.parseRectangularCoordinates(true_eq);
		out.rightAscension = ephem_loc.getLongitude();
		out.declination = ephem_loc.getLatitude();
		out.distance = ephem_loc.getRadius() * EphemConstant.ARCSEC_TO_RAD;
		out.magnitude = in.magnitude + (double) (5.0 * Math.log(out.distance / in.distance) / Math.log(10.0));
		out.name = star.name;

		if (fullEphemeris)
		{
			/* Topocentric correction */
			EphemElement ephem = EphemElement.parseStarEphemElement(out);
			if (ephClone.isTopocentric)
				ephem = EphemUtils.topocentricCorrection(time, obs, ephem, eph);

			// Get results
			out.rightAscension = ephem.rightAscension;
			out.declination = ephem.declination;
			out.distance = ephem.distance / EphemConstant.RAD_TO_ARCSEC;
			out.magnitude = in.magnitude + (double) (5.0 * Math.log(out.distance / in.distance) / Math.log(10.0));

			/* Horizontal coordinates */
			if (ephClone.isTopocentric)
				ephem = EphemUtils.horizontalCoordinates(time, obs, ephem, eph);

			ephem = RiseSetTransit.currentRiseSetTransit(time, obs, eph, ephem, 34.0 * EphemConstant.DEG_TO_RAD / 60.0,
					RiseSetTransit.EVENT_ALL);

			/* Set coordinates to the output equinox */
			if (EphemerisElement.EQUINOX_OF_DATE != ephClone.equinox)
			{
				ephem = EphemUtils.toOutputEquinox(ephem, eph, JD_TDB);
			}

			// Get results
			out.azimuth = ephem.azimuth;
			out.elevation = ephem.elevation;
			out.paralacticAngle = ephem.paralacticAngle;
			out.rise = ephem.rise;
			out.set = ephem.set;
			out.transit = ephem.transit;
			out.transitElevation = ephem.transitElevation;
		}

		return out;
	}



	/**
	 * Right ascension in radians of the direction of the LSR. Default value is
	 * 18h.
	 */
	public static double lsrRA = 18.0 / EphemConstant.RAD_TO_HOUR;

	/**
	 * Declination in radians of the direction of the LSR. Default value is
	 * 30deg.
	 */
	public static double lsrDEC = 30.0 * EphemConstant.DEG_TO_RAD;

	/**
	 * Equinox in Julian day of the direction of the LSR. Default value is
	 * J1900.
	 */
	public static double lsrEquinox = EphemConstant.J1900;

	/**
	 * Speed in km/s of the direction of the LSR. Default value is 20 km/s.
	 */
	public static double lsrSpeed = 20.0;

	/**
	 * Transforms radial velocity from heliocentric to LSR.
	 * <P>
	 * The sun has a systematic motion relative to nearby stars, the mean
	 * depending on the spectral type of the stars used for comparison. The
	 * standard solar motion is defined to be the average velocity of spectral
	 * types A through G as found in general catalogs of radial velocity,
	 * regardless of luminosity class. This motion is 19.5 km/s toward 18 hrs
	 * right ascension and 30� declination for epoch 1900.0 (galactic
	 * co-ordinates l=56�, b=23�). Basic solar motion is the most probable
	 * velocity of stars in the solar neighborhood, so it is weighted more
	 * heavily by the radial velocities of stars of the most common spectral
	 * types (A, gK, dM) in the solar vicinity. In this system, the sun moves at
	 * 15.4 km/s toward l=51�, b=23�.
	 * <P>
	 * The conventional local standard of rest used for galactic studies is
	 * essentially based on the standard solar motion. It assumes the sun to
	 * move at the rounded velocity of 20.0 km/s toward 18 hrs right ascension
	 * and 30� declination for epoch 1900.0. This choice presumes that the
	 * earlier spectral types involved in determining the standard solar motion,
	 * being younger, more closely represent the velocity of the interstellar
	 * gas.
	 * <P>
	 * 
	 * @param star Input star with position and proper motions.
	 * @return Radial velocity measured in conventional LSR.
	 */
	public static double getLSRradialVelocityFromHeliocentric(StarElement star)
	{
		/* Convert FK4 to FK5 catalogue */
		StarElement in = (StarElement) star.clone();
		if (star.frame == StarElement.FRAME_FK4)
		{
			in = StarEphem.transform_FK4_B1950_to_FK5_J2000(star);
		}
		LocationElement loc = new LocationElement(in.rightAscension, in.declination, 1.0);
		double q[] = LocationElement.parseLocationElement(loc);

		/* space motion */
		double sindec = Math.sin(in.declination);
		double cosdec = Math.cos(in.declination);
		double cosra = Math.cos(in.rightAscension);
		double sinra = Math.sin(in.rightAscension);
		double vpi = 0.21094952663 * in.properMotionRadialV / (in.distance * EphemConstant.RAD_TO_ARCSEC);
		double m[] = new double[3];
		m[0] = -in.properMotionRA * cosdec * sinra - in.properMotionDEC * sindec * cosra + vpi * q[0];

		m[1] = in.properMotionRA * cosdec * cosra - in.properMotionDEC * sindec * sinra + vpi * q[1];

		m[2] = in.properMotionDEC * cosdec + vpi * q[2];

		// Pass vector to km/yr
		m[0] *= in.distance * EphemConstant.RAD_TO_ARCSEC * EphemConstant.AU;
		m[1] *= in.distance * EphemConstant.RAD_TO_ARCSEC * EphemConstant.AU;
		m[2] *= in.distance * EphemConstant.RAD_TO_ARCSEC * EphemConstant.AU;

		// Obtain space motion of lsr in the star equinox
		LocationElement loc_lsr = new LocationElement(lsrRA, lsrDEC, 1.0);
		if (in.equinox != lsrEquinox)
		{
			double pos[] = Precession.precess(lsrEquinox, in.equinox, LocationElement.parseLocationElement(loc_lsr),
					Precession.Method.CAPITAINE);
			loc_lsr = LocationElement.parseRectangularCoordinates(pos);
		}
		double q_lsr[] = LocationElement.parseLocationElement(loc_lsr);

		/* space motion */
		sindec = Math.sin(loc_lsr.getLatitude());
		cosdec = Math.cos(loc_lsr.getLatitude());
		cosra = Math.cos(loc_lsr.getLongitude());
		sinra = Math.sin(loc_lsr.getLongitude());
		double vpi_lsr = 0.21094952663 * lsrSpeed * EphemConstant.AU;
		double m_lsr[] = new double[3];
		m_lsr[0] = vpi_lsr * q_lsr[0];
		m_lsr[1] = vpi_lsr * q_lsr[1];
		m_lsr[2] = vpi_lsr * q_lsr[2];

		// Apply correction and pass to km/s
		double v_lsr[] = EphemUtils.sumVectors(m, m_lsr);
		LocationElement vloc_lsr = LocationElement.parseRectangularCoordinates(v_lsr);
		double speed_LSR = vloc_lsr.getRadius() / (EphemConstant.SECONDS_PER_DAY * EphemConstant.JULIAN_DAYS_PER_CENTURY / 100.0);
		return speed_LSR;
	}

}
