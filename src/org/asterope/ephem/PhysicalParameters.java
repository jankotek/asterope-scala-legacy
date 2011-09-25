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
 * A class to obtain the orientation and physical ephemeris of a planet, based
 * on IAU2000 recomendations.
 * 
 * @see Ephem
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class PhysicalParameters
{

	/**
	 * Obtain physical parameters of the planet: elongation, phase, phase angle,
	 * angular radius, visual magnitude (AA supplement), and axis orientation.
	 * This method is applicable for the Sun, the Moon, any planet, Pluto, and
	 * the asteroids Ida, Vesta, Gaspra, and Eros. Previous calculation of basic
	 * ephemeris is required.
	 * 
	 * @param JD Julian day in dynamical time.
	 * @param ephem_sun Ephem object with ephemeris of sun.
	 * @param ephem_obj Ephem object to take and store data.
	 * @param eph Ephemeris object defining the ephemeris properties.
	 * @return EphemElement with the completed data.
	 * @throws JPARSECException If the object data cannot be found.
	 */
	public static EphemElement physicalParameters(double JD, // Julian day
			EphemElement ephem_sun, // Ephem Element
			EphemElement ephem_obj, // Ephem Element
			EphemerisElement eph) // Ephemeris Element
			
	{
		EphemElement ephem = (EphemElement) ephem_obj.clone();

		// Put distances and angles in a more comfortable way
		double RE = ephem_sun.distance;
		double RP = ephem.distanceFromSun;
		double RO = ephem.distance;
		double LP = ephem.heliocentricEclipticLongitude;
		double eq_sun[] = LocationElement.parseLocationElement(new LocationElement(ephem_sun.rightAscension,
				ephem_sun.declination, ephem_sun.distance));
		double LE = Math.PI + LocationElement.parseRectangularCoordinates(
				EphemUtils.equatorialToEcliptic(eq_sun, JD, eph.ephemMethod)).getLongitude();

		// Angular radius in radians
		ephem.angularRadius = (float) Math.atan(Target.getEquatorialRadius(eph.targetBody) / (ephem.distance * EphemConstant.AU));

		if (eph.targetBody != Target.Sun)
		{
			// Elongation
			double DELO = (RE * RE + RO * RO - RP * RP) / (2.0 * RE * RO);
			ephem.elongation = (float) Math.acos(DELO);

			// Phase and phase angle. Note phase angle can be
			// negative to represent the case LE < LP (before opposition)
			double DPH = ((RP * RP + RO * RO - RE * RE) / (2.0 * RP * RO));
			double DPHA = RE * Math.sin(LE - LP) / RO; // Sin(phase angle)
			ephem.phaseAngle = (float) Math.acos(DPH);
			if (Math.signum(DPHA) < 0) ephem.phaseAngle = -ephem.phaseAngle;
			ephem.phase = (float) ((1.0 + DPH) * 0.5);

			// Defect of illumination
			ephem.defectOfIllumination = (float) (1.0 - ephem.phase) * ephem.angularRadius;
		}



		// Continue only for supported objects
		if (!Target.isPlanet(eph.targetBody) && eph.targetBody != Target.Pluto)
			return ephem;

		// Visual magnitude and axis orientation
		double calc_time = EphemUtils.toCenturies(JD);
		double rr = ephem.distance * ephem.distanceFromSun;
		double PH = Math.abs(ephem.phaseAngle) * EphemConstant.RAD_TO_DEG;
		double mag = 0.0;
		double lon0 = 0.0; // Initial longitude at JD = 2451545.0
		double rot_per_day = 0.0; // Degrees per day of rotation of planet
		double shape = Target.getFlatenningFactor(eph.targetBody); // (equatorial - polar radius) / (equatorial radius)
		switch (eph.targetBody)
		{
		case Sun:
			mag = -26.71;
			ephem.northPoleRA = (float) (286.13 * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) (63.87 * EphemConstant.DEG_TO_RAD);
			rot_per_day = 14.1844;
			lon0 = 84.1;
			break;
		case Mercury:
			mag = -0.36 + 5.0 * Math.log10(rr) + 0.038 * PH - 0.000273 * PH * PH + 2.0E-6 * Math.pow(PH, 3.0);
			ephem.northPoleRA = (float) ((281.01 - 0.033 * calc_time) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((61.45 - 0.005 * calc_time) * EphemConstant.DEG_TO_RAD);
			lon0 = 329.548;
			rot_per_day = 6.1385025;
			break;
		case Venus:
			mag = -4.29 + 5.0 * Math.log10(rr) + 0.0009 * PH + 0.000239 * PH * PH - 6.5E-7 * Math.pow(PH, 3.0);
			ephem.northPoleRA = (float) (272.76 * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) (67.16 * EphemConstant.DEG_TO_RAD);
			lon0 = 160.20;
			rot_per_day = -1.4813688;
			break;
		case Earth: // Here the observer is supposed to be outside the
							// Earth
			mag = -3.86 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((0.0 - 0.641 * calc_time) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((90.0 - 0.557 * calc_time) * EphemConstant.DEG_TO_RAD);
			lon0 = 190.147;
			rot_per_day = 360.9856235;
			break;
		case Mars:
			mag = -1.52 + 5.0 * Math.log10(rr) + 0.016 * PH;
			ephem.northPoleRA = (float)  ((317.68143 - 0.1061 * calc_time) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((52.8865 - 0.0609 * calc_time) * EphemConstant.DEG_TO_RAD);
			lon0 = 176.630; // Value published in Preprint 2002 IAU and used by
							// JPL
			// Previous value -176.753 in IAU travaux 2001 seems to be wrong
			rot_per_day = 350.89198226;
			break;
		case Jupiter:
			mag = -9.25 + 5.0 * Math.log10(rr) + 0.005 * PH;
			ephem.northPoleRA = (float) ((268.05 - 0.009 * calc_time) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((64.49 + 0.003 * calc_time) * EphemConstant.DEG_TO_RAD);
			lon0 = 284.95; // for System III
			rot_per_day = 870.536642;
			break;
		case Saturn:
			mag = -8.88 + 5.0 * Math.log10(rr) + 0.044 * PH;
			ephem.northPoleRA = (float) ((40.589 - 0.036 * calc_time) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((83.537 - 0.004 * calc_time) * EphemConstant.DEG_TO_RAD);
			lon0 = 38.9; // for System III
			rot_per_day = 810.7939024;
			break;
		case Uranus:
			mag = -7.19 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) (257.311 * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) (-15.175 * EphemConstant.DEG_TO_RAD);
			lon0 = 203.81;
			rot_per_day = -501.1600928;
			break;
		case Neptune:
			mag = -6.87 + 5.0 * Math.log10(rr);
			double tmp = (357.85 + 52.316 * calc_time) * EphemConstant.DEG_TO_RAD;
			ephem.northPoleRA = (float) ((299.36 + 0.70 * Math.sin(tmp)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((43.46 - 0.51 * Math.cos(tmp)) * EphemConstant.DEG_TO_RAD);
			lon0 = 253.18 - 0.48 * Math.sin(tmp);
			rot_per_day = 536.3128492;
			break;
		case Pluto:
			mag = -1.01 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) (313.02 * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) (9.09 * EphemConstant.DEG_TO_RAD);
			lon0 = 236.77;
			rot_per_day = -56.3623195;
			break;
		case Moon:
			mag = 0.23 + 5.0 * Math.log10(rr) + 0.026 * PH + 4.0E-9 * Math.pow(PH, 4.0);
			ephem = moonAxis(JD, ephem_sun, ephem, eph);
			break;
		case Ida:
			mag = 0.0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) (348.76 * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) (87.12 * EphemConstant.DEG_TO_RAD);
			lon0 = 265.95;
			rot_per_day = -1864.6280070;
			break;
		case Gaspra:
			mag = 0.0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) (9.47 * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) (26.70 * EphemConstant.DEG_TO_RAD);
			lon0 = 83.67;
			rot_per_day = 1226.9114850;
			break;
		case Vesta:
			mag = 0.0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) (301.0 * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) (41.0 * EphemConstant.DEG_TO_RAD);
			lon0 = 292.0;
			rot_per_day = 1617.332776;
			break;
		case Eros:
			mag = 0.0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) (11.35 * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) (17.22 * EphemConstant.DEG_TO_RAD);
			lon0 = 326.07;
			rot_per_day = 1639.38864745;
			break;
		default:
			
			//FIXME handle JPL numbers
//			String object = Target.getName(eph.targetBody);
//			if (object.length() > 0)
//			{
				throw new IllegalArgumentException("invalid object " + eph.targetBody + ".");
//			} else
//			{
//				throw new IllegalArgumentException("cannot find object number " + eph.targetBody + ".");
//			}
		}
		ephem.magnitude = (float) mag;

		// Compute surface brightness
		ephem.surfaceBrightness = (float) Star.getSurfaceBrightness(mag, ephem.angularRadius * EphemConstant.RAD_TO_ARCSEC);

		// Now compute disk orientation as seen by the observer
		if (eph.targetBody != Target.Moon)
		{
			ephem = calcAxis(JD, ephem_sun, ephem, lon0, rot_per_day, shape, eph);
		}

		// Correct visual magnitude of Saturn because of the rings brightness
		if (eph.targetBody == Target.Saturn)
		{
			double sinB = Math.abs(Math.sin(ephem.positionAngleOfPole));
			double ring_magn = -2.6 * sinB + 1.25 * sinB * sinB;
			ephem.magnitude += ring_magn;
		}

		return ephem;
	}

	/**
	 * Calculate moon axis orientation as established by the IAU. These formulae
	 * is an approximation of the real movement of the lunar axis. Previous
	 * calculation of lunar ephemeris is required.
	 * 
	 * @param JD Julian day.
	 * @param ephem_sun Ephem object with data for the sun.
	 * @param ephem_obj Ephem object of the moon.
	 * @param eph Ephemeris object.
	 * @return Ephem object with the data completed.
	 */
	public static EphemElement moonAxis(double JD, EphemElement ephem_sun, EphemElement ephem_obj, EphemerisElement eph)
	{
		double d, d2, E1, E2, E3, E4, E5, E6, E7, E8, E9, E10, E11, E12, E13;
		double ra, dec, lon;

		EphemElement ephem = (EphemElement) ephem_obj.clone();

		d = JD - EphemConstant.J2000;
		d2 = d * d;
		double tmp = EphemUtils.toCenturies(JD);

		E1 = (125.045 - 0.0529921 * d) * EphemConstant.DEG_TO_RAD;
		E2 = (250.089 - 0.1059842 * d) * EphemConstant.DEG_TO_RAD;
		E3 = (260.008 + 13.0120009 * d) * EphemConstant.DEG_TO_RAD;
		E4 = (176.625 + 13.3407154 * d) * EphemConstant.DEG_TO_RAD;
		E5 = (357.529 + 0.9856003 * d) * EphemConstant.DEG_TO_RAD;
		E6 = (311.589 + 26.4057084 * d) * EphemConstant.DEG_TO_RAD;
		E7 = (134.963 + 13.0649930 * d) * EphemConstant.DEG_TO_RAD;
		E8 = (276.617 + 0.3287146 * d) * EphemConstant.DEG_TO_RAD;
		E9 = (34.226 + 1.7484877 * d) * EphemConstant.DEG_TO_RAD;
		E10 = (15.134 - 0.1589763 * d) * EphemConstant.DEG_TO_RAD;
		E11 = (119.743 + 0.0036096 * d) * EphemConstant.DEG_TO_RAD;
		E12 = (239.961 + 0.1643573 * d) * EphemConstant.DEG_TO_RAD;
		E13 = (25.053 + 12.9590088 * d) * EphemConstant.DEG_TO_RAD;

		ra = 269.9949 + 0.0031 * tmp - 3.8787 * Math.sin(E1) - 0.1204 * Math.sin(E2) + 0.0700 * Math.sin(E3) - 0.0172 * Math
				.sin(E4);
		ra = ra + 0.0072 * Math.sin(E6) - 0.0052 * Math.sin(E10) + 0.0043 * Math.sin(E13);

		dec = 66.5392 + 0.0130 * tmp + 1.5419 * Math.cos(E1) + 0.0239 * Math.cos(E2) - 0.0278 * Math.cos(E3) + 0.0068 * Math
				.cos(E4);
		dec = dec - 0.0029 * Math.cos(E6) + 0.0009 * Math.cos(E7) + 0.0008 * Math.cos(E10) - 0.0009 * Math.cos(E13);

		lon = 38.3213 + 13.17635815 * d - 1.4 / 1000000000000.0 * d2 + 3.5610 * Math.sin(E1) + 0.1208 * Math.sin(E2) - 0.0642 * Math
				.sin(E3);
		lon = lon + 0.0158 * Math.sin(E4) + 0.0252 * Math.sin(E5) - 0.0066 * Math.sin(E6) - 0.0047 * Math.sin(E7) - 0.0044 * Math
				.sin(E13);
		lon = lon - 0.0046 * Math.sin(E8) + 0.0028 * Math.sin(E9) + 0.0052 * Math.sin(E10) + 0.0040 * Math.sin(E11) + 0.0019 * Math
				.sin(E12);

		// Set parameters according to IAU
		ephem.northPoleRA = (float) (ra * EphemConstant.DEG_TO_RAD);
		ephem.northPoleDEC = (float) (dec * EphemConstant.DEG_TO_RAD);
		double rot_per_day = 0.0;
		double lon0 = lon;
		double shape = 0.0;
		ephem = calcAxis(JD, ephem_sun, ephem, lon0, rot_per_day, shape, eph);

		return ephem;
	}

	/**
	 * Calculate orientation of a planet providing the position and the
	 * direction of the north pole of rotation. Previous calculation of basic
	 * ephemeris is required, as well as knowledge of axis orientation. The
	 * IAU 2000 models are used, see <I> REPORT OF THE IAU/IAG WORKING GROUP ON
	 * CARTOGRAPHIC COORDINATES AND ROTATIONAL ELEMENTS OF THE PLANETS AND
	 * SATELLITES: 2000</I>, P. K. Seidelmann et al, in Celestial Mechanics and
	 * Dynamical Astronomy, 2002.
	 * <P>
	 * Results are very close to those of the JPL, and almost exact to the IMCCE
	 * ephemeris server. In Neptune, a discrepancy of 0.5 degrees is found with
	 * JPL (checked in 2006). It's seems that JPL doesn't apply the recommended
	 * sinusoidal corrections to the north pole of rotation.
	 * <P>
	 * Note 1: The program use the dynamical north pole of rotation. In giant
	 * planets, an offset between dynamical and magnetic north pole will exist.
	 * So, for these planets, longitude of central meridian in System III and
	 * subsolar longitude values should be taken with caution.
	 * <P>
	 * Note 2: No correction is performed for object radius. In giant planets
	 * like Jupiter, with radius of 70 000 km = 0.25 light-seconds, a
	 * discrepancy up to 0.003 degrees can be expected with other sources. Since
	 * this is a difference in light time that affects the central meridian
	 * longitude on the disk in certain instant, this correction depends on the
	 * position in the disk surface.
	 * 
	 * @param JD Julian day in TDB.
	 * @param ephem_sun EphemElement with all data for the sun.
	 * @param ephem_obj EphemElement with all data completed except those fields
	 *        related to the disk orientation.
	 * @param lon0 Initial longitude of planet at JD = 2451545.0.
	 * @param rot_per_day Rotation speed in degrees/day.
	 * @param shape Shape or flatenning factor for the planet = (equatorial
	 *        radius - polar radius) / equatorial radius. 0.0 means a perfect
	 *        spherical planet. For the Earth is 1.0/EphemConstant.Earth_flatenning.
	 * @param eph Ephemeris object defining the ephemeris properties.
	 * @return Ephem object with all the fields.
	 */
	public static EphemElement calcAxis(double JD, EphemElement ephem_sun, EphemElement ephem_obj, double lon0,
			double rot_per_day, double shape, EphemerisElement eph)
	{
		EphemElement ephem = (EphemElement) ephem_obj.clone();

		double N, D, delta_lon = 0.0;

		// Obtain direction of the north pole of rotation refered to the equinox
		// of date
		ephem = Precession.precessPoleFromJ2000(JD, ephem, eph.ephemMethod);

		/* Correct julian day and obtain it in TCB (Barycentric Coordinate
		 * Time), refered to the barycenter of the solar system. Note that 
		 * following G. H. Kaplan et al. 2006 (astro-ph/0602086) = USNO 
		 * Circular 179, this correction is not needed, since the time 
		 * argument in Seidelmann et al. 2002 is TDB, not TCB. So there was
		 * an error in* Seidelmann et al. 2002.
		 */
		 // JD = JD + DynamicalTime.TCB_TDB(JD) / EphemConstant.seconds_per_day;

		// Obtain position angle of pole as seen from Earth
		ephem.positionAngleOfPole = (float) dotProduct(ephem.northPoleRA, ephem.northPoleDEC, ephem.rightAscension,
				ephem.declination);

		// Correct value (planetocentric to planeto-geodetic latitude)
		ephem.positionAngleOfPole = (float) Math.atan(Math.tan(ephem.positionAngleOfPole) / Math.pow(1.0 - shape, 2.0));

		// To obtain the ilumination angle of equator as seen from Earth, first
		// we need the position of the planet as seen from the sun
		if (eph.targetBody != Target.Sun)
		{

			double fromSun[] = EphemUtils.getGeocentricPosition(new LocationElement(ephem.rightAscension,
					ephem.declination, ephem.distance), new LocationElement(ephem_sun.rightAscension,
					ephem_sun.declination, ephem_sun.distance));

			LocationElement ephem_loc = LocationElement.parseRectangularCoordinates(fromSun);
			double ra = ephem_loc.getLongitude();
			double dec = ephem_loc.getLatitude();

			// Obtain subsolar latitude and longitude
			ephem.subsolarLatitude = (float) dotProduct(ephem.northPoleRA, ephem.northPoleDEC, ra, dec);

			// Correct value (planetocentric to planeto-geodetic latitude)
			ephem.subsolarLatitude = (float) Math.atan(Math.tan(ephem.subsolarLatitude) / Math.pow(1.0 - shape, 2.0));

			// Get subsolar position
			D = Math.cos(dec) * Math.sin(ephem.northPoleRA - ra);
			N = Math.sin(ephem.northPoleDEC) * Math.cos(dec) * Math.cos(ephem.northPoleRA - ra);
			N = N - Math.cos(ephem.northPoleDEC) * Math.sin(dec);
			if (D != 0.0)
				delta_lon = Math.atan(N / D) * EphemConstant.RAD_TO_DEG;
			if (D < 0.0)
				delta_lon = delta_lon + 180.0;
			ephem.subsolarLongitude = (float) (rot_per_day * ((JD - EphemConstant.J2000) - EphemConstant.LIGHT_TIME_DAYS_PER_AU * ephem.distance) - delta_lon + lon0);
			if (rot_per_day < 0.0)
				ephem.subsolarLongitude = 360.0f - ephem.subsolarLongitude;
			ephem.subsolarLongitude = (float) EphemUtils.normalizeRadians(Math.toRadians(ephem.subsolarLongitude));
		}

		// Compute position angle of axis
		ephem.positionAngleOfAxis = (float) (Math.PI + Math.atan2(Math.cos(ephem.northPoleDEC) * Math
				.sin(ephem.rightAscension - ephem.northPoleRA), Math.cos(ephem.northPoleDEC) * Math
				.sin(ephem.declination) * Math.cos(ephem.rightAscension - ephem.northPoleRA) - Math
				.sin(ephem.northPoleDEC) * Math.cos(ephem.declination)));

		// Compute bright limb angle
		ephem.brightLimbAngle = (float) (Math.PI + Math.atan2(Math.cos(ephem_sun.declination) * Math
				.sin(ephem.rightAscension - ephem_sun.rightAscension), Math.cos(ephem_sun.declination) * Math
				.sin(ephem.declination) * Math.cos(ephem.rightAscension - ephem_sun.rightAscension) - Math
				.sin(ephem_sun.declination) * Math.cos(ephem.declination)));

		// Compute longitude of central meridian
		D = Math.cos(ephem.declination) * Math.sin(ephem.northPoleRA - ephem.rightAscension);
		N = Math.sin(ephem.northPoleDEC) * Math.cos(ephem.declination) * Math
				.cos(ephem.northPoleRA - ephem.rightAscension);
		N = N - Math.cos(ephem.northPoleDEC) * Math.sin(ephem.declination);
		delta_lon = Math.atan2(N, D) * EphemConstant.RAD_TO_DEG;

		double meridian0 = (rot_per_day * ((JD - EphemConstant.J2000) - EphemConstant.LIGHT_TIME_DAYS_PER_AU * ephem.distance) - delta_lon);
		double meridian = meridian0 + lon0;
		if (rot_per_day < 0.0)
			meridian = 360.0 - meridian;
		meridian = EphemUtils.normalizeDegrees(meridian);
		ephem.longitudeOfCentralMeridian = (float) Math.toRadians(meridian);

		// Establish adequate values for Jupiter, Saturn, and other bodies
		switch (eph.targetBody)
		{
		case Jupiter:
			double tmp1 = -(-67.1 + delta_lon - (meridian0 + delta_lon) * 877.9 / rot_per_day); // Equatorial
																								// belt
			double tmp2 = -(-43.3 + delta_lon - (meridian0 + delta_lon) * 870.27 / rot_per_day); // Tropical
																									// belt

			tmp1 = EphemUtils.normalizeDegrees(tmp1);
			tmp2 = EphemUtils.normalizeDegrees(tmp2);

			ephem.longitudeOfCentralMeridianSystemI = (float) Math.toRadians(tmp1);
			ephem.longitudeOfCentralMeridianSystemII = (float) Math.toRadians(tmp2);
			ephem.longitudeOfCentralMeridianSystemIII = ephem.longitudeOfCentralMeridian;
			break;
		case Saturn:
			double tmp = -(-227.2037 + delta_lon - (meridian0 + delta_lon) * 844.3 / rot_per_day); // Equatorial
																									// belt

			tmp = EphemUtils.normalizeDegrees(tmp);

			ephem.longitudeOfCentralMeridianSystemI = (float) Math.toRadians(tmp);
			ephem.longitudeOfCentralMeridianSystemIII = ephem.longitudeOfCentralMeridian;
			break;
		case Sun: // This inversion is due to historical reasons
			ephem.longitudeOfCentralMeridian = (float) EphemUtils
					.normalizeRadians(2.0 * Math.PI - ephem.longitudeOfCentralMeridian);
			break;
		case Earth: // This inversion is due to historical reasons
			ephem.longitudeOfCentralMeridian = (float) EphemUtils
					.normalizeRadians(2.0 * Math.PI - ephem.longitudeOfCentralMeridian);
			break;
		case Moon: // This inversion is due to historical EphemUtils
			ephem.longitudeOfCentralMeridian = (float) EphemUtils
					.normalizeRadians(2.0 * Math.PI - ephem.longitudeOfCentralMeridian);
			break;
		case Uranus: // Set value also for System III
			ephem.longitudeOfCentralMeridianSystemIII = ephem.longitudeOfCentralMeridian;
			break;
		case Neptune: // Set value also for System III
			ephem.longitudeOfCentralMeridianSystemIII = ephem.longitudeOfCentralMeridian;
			break;
		}

		return ephem;
	}

	/**
	 * Performs adequate dot product for axis orientation calculations. The
	 * result is the planetocentric latitude of the object supposed that the
	 * object's axis is pointing to pole_ra, pole_dec, and the object is
	 * observed is a position p_ra, p_dec. The value should later be corrected
	 * to planetogeodetic by applying the formula: geo_lat =
	 * atan(tan(planeto_lat) / (1.0 - shape)^2), where shape = (equatorial -
	 * polar radius) / (equatorial radius).
	 * 
	 * @param pole_ra Right ascension of the north pole.
	 * @param pole_dec Declination of the north pole.
	 * @param p_ra Right ascension of some planet as seen by the observer.
	 * @param p_dec Declination of some planet as seen by the observer.
	 * @return Result of the dot product as a double precission value.
	 */
	static double dotProduct(double pole_ra, double pole_dec, double p_ra, double p_dec)
	{
		double incc = 0.0, DOT;

		pole_dec = Math.PI / 2.0 - pole_dec;
		p_dec = (Math.PI / 2.0) - p_dec;

		DOT = Math.sin(pole_dec) * Math.cos(pole_ra) * Math.sin(p_dec) * Math.cos(p_ra);
		DOT = DOT + Math.sin(pole_dec) * Math.sin(pole_ra) * Math.sin(p_dec) * Math.sin(p_ra);
		DOT = DOT + Math.cos(pole_dec) * Math.cos(p_dec);

		incc = -Math.asin(DOT);

		return incc;
	}
}
