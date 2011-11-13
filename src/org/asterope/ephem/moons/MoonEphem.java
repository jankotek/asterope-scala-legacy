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
package org.asterope.ephem.moons;

import org.asterope.ephem.*;

import java.util.LinkedHashMap;
import java.util.Map;


/**
 * A class to obtain accurate ephemeris of natural satellites.
 * 
 * @see TimeElement
 * @see ObserverElement
 * @see EphemerisElement
 * @see MoonEphemElement
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class MoonEphem
{
	
	static MoshierPlanetEphem moshier = new MoshierPlanetEphem();

	static EphemElement getSatelliteEphem(TimeElement time, // Time Element
			ObserverElement obs, // Observer Element
			EphemerisElement eph)
	 {
//		EphemElement ephem_plan = Ephem.getEphemeris(time, obs, eph, false);
//		return ephem_plan;
		return moshier.getEphemeris(time, obs, eph,true);
	}
	
	static void setEphemerisOffset(EphemerisElement eph, double[] eq, double JD)
	 {
		eq = EphemUtils.toICRSFrame(eq);
		eq = EphemUtils.equatorialToEcliptic(eq, JD, eph.ephemMethod);
		moshier.setOffsetPosition(eq);

//		switch (eph.algorithm)
//		{
//		case EphemerisElement.ALGORITHM_JPL_DE200:
//		case EphemerisElement.ALGORITHM_JPL_DE403:
//		case EphemerisElement.ALGORITHM_JPL_DE405:
//		case EphemerisElement.ALGORITHM_JPL_DE406:
//		case EphemerisElement.ALGORITHM_JPL_DE413:
//		case EphemerisElement.ALGORITHM_JPL_DE414:
//			eq = EphemUtils.toICRSFrame(eq);
//			JPLEphemeris.setOffsetPosition(eq);
//			break;
//		case EphemerisElement.ALGORITHM_SERIES96_MOSHIERFORMOON:
//			Series96.setOffsetPosition(eq);
//			break;
//		case EphemerisElement.ALGORITHM_MOSHIER:
//			eq = EphemUtils.toICRSFrame(eq);
//			eq = EphemUtils.equatorialToEcliptic(eq, JD, eph.ephemMethod);
//			PlanetEphem.setOffsetPosition(eq);
//			break;
//		default:
//			throw new IllegalArgumentException("unsupported algorithm in natural satellites ephemeris calculations.");
//		}
	}
	
	/**
	 * Calculate position of minor satellites, providing full data. This method
	 * uses orbital elements from the Minor Planet Center.
	 * <P>
	 * This method returns the angular size of the body by assuming an albedo of
	 * 0.5 (icy object).
	 * 
	 * @param time Time object containing the date.
	 * @param obs Observer object containing the observer position.
	 * @param eph Ephemeris object with the target and ephemeris
	 *        properties.
	 * @param orbit Orbital elements with the adequate target orbital
	 *        elements.
	 * @return Moon ephem object containing full ephemeris data.
	 * @ If the calculation fails.
	 */
	public static MoonEphemElement calcMinorSatelliteFromMPC(TimeElement time, // Time
																			// Element
			ObserverElement obs, // Observer Element
			EphemerisElement eph, // Ephemeris Element
			OrbitalElement orbit) // Orbital Element
			
	{
		// Obtain dynamical time in julian days
		double JD = TimeScale.getJD(time, obs, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);

		// Construct new ephemeris objects
		EphemerisElement new_eph = (EphemerisElement) eph.clone();
		new_eph.targetBody = orbit.centralBody;
		EphemerisElement new_eph_sun = (EphemerisElement) eph.clone();
		new_eph_sun.targetBody = Target.Sun;
		EphemerisElement new_eph_moon = (EphemerisElement) eph.clone();

		// Reset position offset to zero
		MoonEphem.setEphemerisOffset(eph, new double[] {0.0, 0.0, 0.0 }, JD);

		// Obtain position of planet
		EphemElement ephem_plan = new EphemElement();
		ephem_plan = MoonEphem.getSatelliteEphem(time, obs, new_eph);

		// Obtain position of sun
		EphemElement ephem_sun = new EphemElement();
		ephem_sun = MoonEphem.getSatelliteEphem(time, obs, new_eph_sun);

		// Obtain new light time value
		double light_time_new = ephem_plan.lightTime;

		EphemElement ephem = new EphemElement();
		MoonEphemElement moon = new MoonEphemElement();
		double light_time = ephem_plan.lightTime;

		do
		{
			light_time = light_time_new;

			double plane_orbit_coords[] = OrbitEphem.orbitPlane(orbit, JD - light_time);
			double coords[] = OrbitEphem.toEclipticPlane(orbit, plane_orbit_coords);

			// Pass to equatorial
			double ELEM[] = EphemUtils.eclipticToEquatorial(coords, JD, eph.ephemMethod);

			// Precession to J2000 if necessary
			ELEM = Precession.precess(orbit.referenceEquinox, EphemConstant.J2000, ELEM, eph.ephemMethod);

			// Set satellite position
			MoonEphem.setEphemerisOffset(eph, new double[] {ELEM[0], ELEM[1], ELEM[2]}, JD);

			// Obtain position of satellite
			ephem = MoonEphem.getSatelliteEphem(time, obs, new_eph);

			light_time_new = ephem.lightTime;

		} while (Math.abs(light_time_new - light_time) > (0.001 / EphemConstant.SECONDS_PER_DAY));

		moon = MoonEphemElement.parseEphemElement(ephem);
		moon.name = orbit.name;
		new_eph_moon.targetBody = eph.targetBody;
		moon = MoonPhysicalParameters.physicalParameters(JD, ephem_sun, moon, new_eph_moon);
		moon = MoonEphem.satellitePhenomena(moon, ephem_plan, orbit.centralBody);

		// Reset position offset to zero
		MoonEphem.setEphemerisOffset(eph, new double[] { 0.0, 0.0, 0.0 }, JD);

		return moon;

	}


//	private static boolean APPROX = false;
	/**
	 * True to avoid differential light time corrections and satellite orientation
	 * calculations, improving speed. Only considered for JupiterL1, TASS, and UranusGUST86 theories.
	 * @param approx True or false.
	 */
//	public static void setEphemerisToApproximate(boolean approx)
//	{
//		APPROX = approx;
//	}

	/**
	 * This is an implementation of the 2007 Martian satellites ephemerides by 
	 * V. Lainey et al. For reference see
	 * Lainey, V., Dehant, V. and Paetzold, M. "First numerical ephemerides of the Martian moons", 
	 * Astron. Astrophys., vol 465 pp.1075-1084	(2007).
	 * <P>
	 * This is the best available theory for the motion of the Martian satellites
	 * Phobos and Deimos.
	 * <P>
	 * Satellites are positioned respect to Mars. For Mars coordinates,
	 * the algorithm selected in the ephemeris object is used. Valid algorithms
	 * includes the Moshier fit, Series96, and JPL ephemerides. Deflection due 
	 * to planet is ignored, since the effect is negligible.
	 * 
	 * @param time Time object.
	 * @param obs Observer object.
	 * @param eph Ephemeris object.
	 * @return An array of Moon ephem objects with the ephemeris.
	 * @ If the calculation fails.
	 */
	public static Map<Target,MoonEphemElement> martianSatellitesEphemerides_2007(TimeElement time, // Time
																						// Element
			ObserverElement obs, // Observer Element
			EphemerisElement eph) // Ephemeris Element
			
	{
			// Obtain dynamical time in julian days
			double JD = TimeScale.getJD(time, obs, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);

			// Construct new ephemeris objects
			EphemerisElement new_eph = (EphemerisElement) eph.clone();
			new_eph.targetBody = Target.Mars;
			EphemerisElement new_eph_sun = (EphemerisElement) eph.clone();
			new_eph_sun.targetBody = Target.Sun;
			EphemerisElement new_eph_moon = (EphemerisElement) eph.clone();

			// Reset position offset to zero
			MoonEphem.setEphemerisOffset(eph, new double[] {0.0, 0.0, 0.0 }, JD);

			// Obtain position of planet
			EphemElement ephem_mars = new EphemElement();
			ephem_mars = MoonEphem.getSatelliteEphem(time, obs, new_eph);

			// Obtain position of sun
			EphemElement ephem_sun = new EphemElement();
			ephem_sun = MoonEphem.getSatelliteEphem(time, obs, new_eph_sun);

			// Obtain new light time value
			double light_time_new = ephem_mars.lightTime;

			Map<Target,MoonEphemElement> moon= new LinkedHashMap<Target, MoonEphemElement>();
			for(Target nsat : new Target[]{Target.Phobos,Target.Deimos})
			{
				EphemElement ephem = null;
				double light_time = ephem_mars.lightTime;
				do
				{
					light_time = light_time_new;

					int nsat2 =Target.Phobos==nsat?Mars07.SATELLITE_PHOBOS:Mars07.SATELLITE_DEIMOS;
					double ELEM[] = Mars07.getMoonPosition(JD - light_time, nsat2, Mars07.OUTPUT_POSITIONS);

					// Set satellite position
					MoonEphem.setEphemerisOffset(eph, new double[] {ELEM[0], ELEM[1], ELEM[2]}, JD);

					// Obtain position of satellite
					ephem = MoonEphem.getSatelliteEphem(time, obs, new_eph);

					light_time_new = ephem.lightTime;
				} while (Math.abs(light_time_new - light_time) > (0.001 / EphemConstant.SECONDS_PER_DAY)); // && !APPROX);

				MoonEphemElement ret = MoonEphemElement.parseEphemElement(ephem);
				new_eph_moon.targetBody = nsat;
				ret = MoonPhysicalParameters.physicalParameters(JD, ephem_sun, ret, new_eph_moon);
				ret = MoonEphem.satellitePhenomena(ret, ephem_mars, Target.Mars);
				ret.name = nsat.name();
				moon.put(nsat, ret);

			}

			// Obtain relative phenomena

			moon = MoonEphem.satellitesPhenomena(moon, ephem_mars.angularRadius);

			// Reset position offset to zero
			MoonEphem.setEphemerisOffset(eph, new double[] { 0.0, 0.0, 0.0 }, JD);

			return moon;
	}
	
	private static final JupiterL1 jupiterl1 = new JupiterL1();

	/**
	 * This is an implementation of the JupiterL1 ephemerides by V. Lainey et al. For
	 * reference see Astronomy and Astrophysics 427, 371 (2004).
	 * <P>
	 * This is the best available theory for the motion of the main satellites
	 * of Jupiter: Io, Europa, Ganymede, and Callisto.
	 * <P>
	 * Satellites are positioned respect to Jupiter. For Jupiter coordinates,
	 * the algorithm selected in the ephemeris object is used. Valid algorithms
	 * includes the Moshier fit, Series96, and JPL ephemerides. Deflection due 
	 * to planet is ignored, since the effect is negligible.
	 * 
	 * @param time Time object.
	 * @param obs Observer object.
	 * @param eph Ephemeris object.
	 * @return An array of Moon ephem objects with the ephemeris.
	 * @ If the calculation fails.
	 */
	public static Map<Target,MoonEphemElement> galileanSatellitesEphemerides_L1(TimeElement time, // Time
																						// Element
			ObserverElement obs, // Observer Element
			EphemerisElement eph) // Ephemeris Element
			
	{
			// Obtain dynamical time in julian days
			double JD = TimeScale.getJD(time, obs, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);

			// Construct new ephemeris objects
			EphemerisElement new_eph = (EphemerisElement) eph.clone();
			new_eph.targetBody = Target.Jupiter;
			EphemerisElement new_eph_sun = (EphemerisElement) eph.clone();
			new_eph_sun.targetBody = Target.Sun;
			EphemerisElement new_eph_moon = (EphemerisElement) eph.clone();

			// Reset position offset to zero
			MoonEphem.setEphemerisOffset(eph, new double[] {0.0, 0.0, 0.0 }, JD);

			// Obtain position of planet
			EphemElement ephem_jup = new EphemElement();
			ephem_jup = MoonEphem.getSatelliteEphem(time, obs, new_eph); 

			// Obtain position of sun
			EphemElement ephem_sun = new EphemElement();
			ephem_sun = MoonEphem.getSatelliteEphem(time, obs, new_eph_sun);

			// Obtain new light time value
			double light_time_new = ephem_jup.lightTime;

			EphemElement ephem = null;
			Map<Target,MoonEphemElement> moon = new LinkedHashMap<Target, MoonEphemElement>();
			
			for (int counter = 0; counter< JupiterL1.MOONS.length;counter++ )
			{
				Target nsat = JupiterL1.MOONS[counter];
				double light_time = ephem_jup.lightTime;
				do
				{
					light_time = light_time_new;
					double ELEM[] = jupiterl1.L1_theory(JD - light_time, counter + 1);

					// Set satellite position
					MoonEphem.setEphemerisOffset(eph, new double[] {ELEM[0], ELEM[1], ELEM[2]}, JD);

					// Obtain position of satellite
					ephem = MoonEphem.getSatelliteEphem(time, obs, new_eph);

					light_time_new = ephem.lightTime;
				} while (Math.abs(light_time_new - light_time) > (0.001 / EphemConstant.SECONDS_PER_DAY)); // && !APPROX);

				MoonEphemElement ret = MoonEphemElement.parseEphemElement(ephem);
				new_eph_moon.targetBody = nsat;
				ret = MoonPhysicalParameters.physicalParameters(JD, ephem_sun, ret, new_eph_moon);
				ret = MoonEphem.satellitePhenomena(ret, ephem_jup, Target.Jupiter); 
				ret.name = nsat.name();
				moon.put(nsat, ret);				
			}
			
			// Obtain relative phenomena
			moon = MoonEphem.satellitesPhenomena(moon, ephem_jup.angularRadius);

			// Reset position offset to zero
			MoonEphem.setEphemerisOffset(eph, new double[] { 0.0, 0.0, 0.0 }, JD);
			
			return moon;

	}

	/**
	 * Calculate position of Saturn satellites, providing full data. This method
	 * uses TASS1.7 theory from the IMCCE. For reference see A&A 297, 588-605
	 * (1995), and A&A 324, 366 (1997). Objects are Mimas, Enceladus, Tethys,
	 * Dione, Rhea, Titan, Hyperion, Iapetus.
	 * <P>
	 * Satellites are positioned respect to Saturn. For Saturn coordinates, the
	 * the algorithm selected in the ephemeris object is used. Valid algorithms
	 * includes the Moshier fit, Series96, and JPL ephemerides. Deflection due 
	 * to planet is ignored, since the effect is negligible.
	 * 
	 * @param time Time object containing the date.
	 * @param obs Observer object containing the observer position.
	 * @param eph Ephemeris object with the target and ephemeris
	 *        properties.
	 * @param truncate False for using the whole TASS theory, true for using
	 *        only critical terms. Only used in the first call to this method,
	 *        since the terms are stored in memory. A true value improves
	 *        performance while keeping precission in the 0.2 arcsecond level.
	 * @return MoonEphemElement array of objects containing full ephemeris data
	 *         for each satellite.
	 * @ If the calculation fails.
	 */
	public static Map<Target,MoonEphemElement> saturnianSatellitesEphemerides_TASS17(TimeElement time, // Time
																								// Element
			ObserverElement obs, // Observer Element
			EphemerisElement eph, // Ephemeris Element
			boolean truncate) 
	{
			// Obtain dynamical time in julian days
			double JD = TimeScale.getJD(time, obs, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);

			// Construct new ephemeris objects
			EphemerisElement new_eph = (EphemerisElement) eph.clone();
			new_eph.targetBody = Target.Saturn;
			EphemerisElement new_eph_sun = (EphemerisElement) eph.clone();
			new_eph_sun.targetBody = Target.Sun;
			EphemerisElement new_eph_moon = (EphemerisElement) eph.clone();

			// Reset position offset to zero
			MoonEphem.setEphemerisOffset(eph, new double[] {0.0, 0.0, 0.0 }, JD);

			// Obtain position of planet
			EphemElement ephem_sat = new EphemElement();
			ephem_sat = MoonEphem.getSatelliteEphem(time, obs, new_eph);

			// Obtain position of sun
			EphemElement ephem_sun = new EphemElement();
			ephem_sun = MoonEphem.getSatelliteEphem(time, obs, new_eph_sun);

			// Obtain satellites positions
			double light_time_new = ephem_sat.lightTime;

			EphemElement ephem = null;
			Map<Target,MoonEphemElement> moon = new LinkedHashMap<Target, MoonEphemElement>();
			for (int i = 0; i < SaturnTASS17.MOONS.length; i++)
			{
				double light_time = ephem_sat.lightTime;
				do
				{
					light_time = light_time_new;

					double ecl[] = SaturnTASS17.TASS17_theory(JD - light_time, i + 1, truncate);

					// Pass to equatorial
					double ELEM[] = new double[3];
					ELEM = EphemUtils.eclipticToEquatorial(ecl, JD, eph.ephemMethod);

					// Set satellite position
					MoonEphem.setEphemerisOffset(eph, new double[] {ELEM[0], ELEM[1], ELEM[2]}, JD);

					// Obtain position of satellite
					ephem = MoonEphem.getSatelliteEphem(time, obs, new_eph);

					light_time_new = ephem.lightTime;
				} while (Math.abs(light_time_new - light_time) > (0.001 / EphemConstant.SECONDS_PER_DAY)); // && !APPROX);

				
				MoonEphemElement ret = MoonEphemElement.parseEphemElement(ephem);
				new_eph_moon.targetBody = SaturnTASS17.MOONS[i];
				ret = MoonPhysicalParameters.physicalParameters(JD, ephem_sun, ret, new_eph_moon);
				ret = MoonEphem.satellitePhenomena(ret, ephem_sat, Target.Saturn);
				ret.name = SaturnTASS17.MOONS[i].name();
				moon.put(SaturnTASS17.MOONS[i], ret);
				
					
			}
			moon = MoonEphem.satellitesPhenomena(moon, ephem_sat.angularRadius);
			// Reset position offset to zero
			MoonEphem.setEphemerisOffset(eph, new double[] { 0.0, 0.0, 0.0 }, JD);

			return moon;

	}

	/**
	 * Obtains the position of the Uranian satellites using UranusGUST86 theory. For
	 * reference see: UranusGUST86 - An analytical ephemeris of the Uranian
	 * satellites, Laskar J., Jacobson, R. Astron. Astrophys. 188, 212-224
	 * (1987). Objects are Miranda, Ariel, Umbriel, Titania, and Oberon.
	 * <P>
	 * Satellites are positioned respect to Uranus. For Uranus coordinates, the
	 * the algorithm selected in the ephemeris object is used. Valid algorithms
	 * includes the Moshier fit, Series96, and JPL ephemerides. Deflection due 
	 * to planet is ignored, since the effect is negligible.
	 * 
	 * @param time Time object containing the date.
	 * @param obs Observer object containing the observer position.
	 * @param eph Ephemeris object with the target and ephemeris
	 *        properties.
	 * @return Moon ephem object full of data.
	 * @ If the calculation fails.
	 */
	public static Map<Target,MoonEphemElement> uranianSatellitesEphemerides_GUST86(TimeElement time, // Time
																							// Element
			ObserverElement obs, // Observer Element
			EphemerisElement eph) // Ephemeris Element
			
	{
			// Obtain dynamical time in julian days
			double JD = TimeScale.getJD(time, obs, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);

			// Construct new ephemeris objects
			EphemerisElement new_eph = (EphemerisElement) eph.clone();
			new_eph.targetBody = Target.Uranus;
			EphemerisElement new_eph_sun = (EphemerisElement) eph.clone();
			new_eph_sun.targetBody = Target.Sun;
			EphemerisElement new_eph_moon = (EphemerisElement) eph.clone();

			// Reset position offset to zero
			MoonEphem.setEphemerisOffset(eph, new double[] {0.0, 0.0, 0.0 }, JD);

			// Obtain position of planet
			EphemElement ephem_ura = new EphemElement();
			ephem_ura = MoonEphem.getSatelliteEphem(time, obs, new_eph);

			// Obtain position of sun
			EphemElement ephem_sun = new EphemElement();
			ephem_sun = MoonEphem.getSatelliteEphem(time, obs, new_eph_sun);

			// Obtain satellites positions
			double light_time_new = ephem_ura.lightTime;

			EphemElement ephem = null;
			Map<Target,MoonEphemElement> moon = new LinkedHashMap<Target, MoonEphemElement>();
			for (int i = 0; i < UranusGUST86.MOONS.length; i++)
			{				
				double light_time = ephem_ura.lightTime;
				do
				{
					light_time = light_time_new;

					double eq_1950[] = UranusGUST86.GUST86_theory(JD - light_time, i + 1, 3);
					double eq[] = Precession.precess(EphemConstant.J1950, EphemConstant.J2000, eq_1950, eph.ephemMethod);

					// Set satellite position
					MoonEphem.setEphemerisOffset(eph, new double[] {eq[0], eq[1], eq[2]}, JD);

					// Obtain position of satellite
					ephem = MoonEphem.getSatelliteEphem(time, obs, new_eph);

					light_time_new = ephem.lightTime;

				} while (Math.abs(light_time_new - light_time) > (0.001 / EphemConstant.SECONDS_PER_DAY)); // && !APPROX);

				
				MoonEphemElement ret = MoonEphemElement.parseEphemElement(ephem);
				new_eph_moon.targetBody = UranusGUST86.MOONS[i];
				ret = MoonPhysicalParameters.physicalParameters(JD, ephem_sun, ret, new_eph_moon);
				ret = MoonEphem.satellitePhenomena(ret, ephem_ura, Target.Uranus);
				ret.name = UranusGUST86.MOONS[i].name();

					
			}
			// Obtain relative phenomena
			moon = MoonEphem.satellitesPhenomena(moon, ephem_ura.angularRadius);
			// Reset position offset to zero
			MoonEphem.setEphemerisOffset(eph, new double[] { 0.0, 0.0, 0.0 }, JD);

			return moon;

	}

	/**
	 * Obtain relative position of a satellite respect to it's mother planet.
	 * Based on code from the IMCCE.
	 * 
	 * @param VP Geocentric equatorial coordinates (x, y, z) of the planet.
	 * @param VS Planetocentric equatorial coordinates (x, y, z) of the
	 *        satellite.
	 * @return Array with the offset in right ascension and declination.
	 */
	public static double[] relativePosition(double[] VP, double[] VS)
	{
		double D = Math.sqrt(VP[0] * VP[0] + VP[1] * VP[1] + VP[2] * VP[2]);
		double ALPHA = Math.atan2(VP[1], VP[0]);
		double DELTA = Math.asin(VP[2] / D);
		double C = Math.cos(ALPHA);
		double S = Math.sin(ALPHA);
		double X = VS[0] * C + VS[1] * S;
		double Y = VS[1] * C - VS[0] * S;
		C = Math.cos(DELTA);
		S = Math.sin(DELTA);
		double Z = VS[2] * C - X * S;
		double CD1 = Math.atan(Y / D);
		double CD2 = Math.atan(Z / D);

		return new double[] { CD1, CD2, Z};
	}

	// Perform adequate rotations for Jovian satellites theories E2x3, E5, and
	// Saturnian satellites by Dourneau.
	static double[] getSatellitePosition(double X, double Y, double Z, double I, double PHI, double ii, double OMEGA,
			double L0, double B0, double L0_sun, double B0_sun)
	{
		// Rotation towards Jupiter's orbital plane
		double A1 = X;
		double B1 = Y * cos_deg(I) - Z * sin_deg(I);
		double C1 = Y * sin_deg(I) + Z * cos_deg(I);

		// Rotation towards ascending node of Jupiter's orbit
		double A2 = A1 * cos_deg(PHI) - B1 * sin_deg(PHI);
		double B2 = A1 * sin_deg(PHI) + B1 * cos_deg(PHI);
		double C2 = C1;

		// Rotation towards plane of ecliptic
		double A3 = A2;
		double B3 = B2 * cos_deg(ii) - C2 * sin_deg(ii);
		double C3 = B2 * sin_deg(ii) + C2 * cos_deg(ii);

		// Rotation towards the vernal equinox
		double A4 = A3 * cos_deg(OMEGA) - B3 * sin_deg(OMEGA);
		double B4 = A3 * sin_deg(OMEGA) + B3 * cos_deg(OMEGA);
		double C4 = C3;

		// Rotate to the ecliptic location of the planet
		double A5 = A4 * sin_deg(L0) - B4 * cos_deg(L0);
		double B5 = A4 * cos_deg(L0) + B4 * sin_deg(L0);
		double C5 = C4;

		double A6 = A5;
		double B6 = C5 * sin_deg(B0) + B5 * cos_deg(B0);
		double C6 = C5 * cos_deg(B0) - B5 * sin_deg(B0);

		// Same from the Sun
		double A5_sun = A4 * sin_deg(L0_sun) - B4 * cos_deg(L0_sun);
		double B5_sun = A4 * cos_deg(L0_sun) + B4 * sin_deg(L0_sun);
		double C5_sun = C4;

		double A6_sun = A5_sun;
		double B6_sun = C5_sun * sin_deg(B0_sun) + B5_sun * cos_deg(B0_sun);
		double C6_sun = C5_sun * cos_deg(B0_sun) - B5_sun * sin_deg(B0_sun);

		return new double[]
		{ A6, B6, C6, A6_sun, B6_sun, C6_sun };
	}

	// Obtains apparent positions for Jovian satellites theories E2x3, E5, and
	// Saturnian satellites by Dourneau.
	static double[] getApparentPosition(Target planet, double[] sat_pos, double D, double DELTA, double D_sun,
			double DELTA_sun, double K, double R) 
	{
			double x = sat_pos[0] * Math.cos(D) - sat_pos[2] * Math.sin(D);
			double y = sat_pos[0] * Math.sin(D) + sat_pos[2] * Math.cos(D);
			double z = sat_pos[1];
			double x_sun = sat_pos[3] * Math.cos(D_sun) - sat_pos[5] * Math.sin(D_sun);
			double y_sun = sat_pos[3] * Math.sin(D_sun) + sat_pos[5] * Math.cos(D_sun);
			double z_sun = sat_pos[4];

			double W = DELTA / (DELTA + z / (EphemConstant.AU / Target.getEquatorialRadius(planet)));
			double W_sun = DELTA_sun / (DELTA_sun + z_sun / (EphemConstant.AU / Target.getEquatorialRadius(planet)));

			// Project to the sky correcting differential light time
			x += (Math.abs(z) / K) * Math.sqrt(1.0 - squared(x / R));
			y *= W;
			x *= W;
			x_sun += (Math.abs(z_sun) / K) * Math.sqrt(1.0 - squared(x_sun / R));
			y_sun *= W_sun;
			x_sun *= W_sun;

			return new double[]
			{ x, y, z, x_sun, y_sun, z_sun };

	}

	/**
	 * Constant that holds the magnitude of a satellite when it is fully eclipsed/occulted.
	 */
	public static final int APPARENT_MAGNITUDE_WHEN_SATELLITE_IS_NOT_VISIBLE = 100;
	
	// Calculates satellites phenomena like transits, eclipses, etc., for
	// satellites theories based on equatorial coordinates instead of 
	// satellites positions relative to the planet's equator. 
	// Calculations are respect to the mother planet
	static MoonEphemElement satellitePhenomena(MoonEphemElement moon_obj, EphemElement ephem, Target planet)
			
	{
		MoonEphemElement moon = (MoonEphemElement) moon_obj.clone();

			// Obtain relative position
 			LocationElement locEphem = new LocationElement(ephem.rightAscension, ephem.declination, ephem.distance);
			LocationElement locMoon = new LocationElement(moon.rightAscension, moon.declination, moon.distance);

			double planetVector[] = LocationElement.parseLocationElement(locEphem);
			double satelliteVector[] = LocationElement.parseLocationElement(locMoon);
			double dif[] = EphemUtils.substractVector(planetVector, satelliteVector);
			double dp[] = MoonEphem.relativePosition(planetVector, dif);

			// Obtain relative position
			//double pz = moon.distance - ephem.distance;
			double pz = Math.sqrt(moon.distance * moon.distance - (dif[0] * dif[0] + dif[1] * dif[1])) - ephem.distance;
			double pr = LocationElement.getAngularDistance(locMoon, locEphem) / ephem.angularRadius;
			//double pr = Math.sqrt(dp[0] * dp[0] + dp[1] * dp[1]) / ephem.angularRadius;
			double pang = Math.atan2(dp[0], -dp[1]);
			//double pang = Math.PI-LocationElement.getPositionAngle(locEphem, locMoon);

			// From the Earth: rotate along the position angle of axis
			double ang = ephem.positionAngleOfAxis;
			double ppx = pr * Math.sin(pang + ang);
			double ppy = pr * Math.cos(pang + ang);
			double ppz = pz * EphemConstant.AU / Target.getEquatorialRadius(planet);

			moon.xPosition = ppx;
			moon.yPosition = ppy;
			moon.zPosition = ppz;

			// From the Sun: rotate to the bright limb angle, rotate along the
			// phase angle, and back
			ang = ephem.brightLimbAngle;
			ppx = pr * Math.sin(pang + ang);
			ppy = pr * Math.cos(pang + ang);

			double r = Math.sqrt(ppy * ppy + ppz * ppz);
			ang = Math.atan2(ppz, ppy) - Math.abs(ephem.phaseAngle);
			double y = r * Math.cos(ang);
			double z = r * Math.sin(ang);
			double x = -ppx;

			ang = ephem.positionAngleOfAxis - ephem.brightLimbAngle;
			pang = Math.atan2(-x, y);
			pr = Math.sqrt(x * x + y * y);
			ppx = pr * Math.sin(pang + ang);
			ppy = pr * Math.cos(pang + ang);
			ppz = z;

			moon.xPositionFromSun = ppx;
			moon.yPositionFromSun = ppy;
			moon.zPositionFromSun = ppz;

			// Check for events
			double flattening = Target.getEquatorialRadius(planet) / Target.getPolarRadius(planet);
			double satSize = moon.angularRadius / ephem.angularRadius;
			// From the observer
			boolean inferior = (moon.zPosition <= 0.0);
			double Y1 = moon.yPosition * flattening;
			boolean withinDisc = (Math.sqrt(moon.xPosition * moon.xPosition + Y1 * Y1) <= (1.0 + satSize));
			boolean transiting = withinDisc && inferior;
			boolean occulted = withinDisc && !inferior;

			moon.transiting = transiting;
			moon.occulted = occulted;
			moon.inferior = inferior;

			if (moon.occulted && Math.sqrt(moon.xPosition * moon.xPosition + Y1 * Y1) > (1.0 - satSize))
			{
				double occultedArea = MoonEphem.getOccultedArea(ephem.angularRadius, moon.angularRadius, Math.sqrt(moon.xPosition * moon.xPosition + Y1 * Y1) * ephem.angularRadius);
				occultedArea /= (Math.PI * Math.pow(moon.angularRadius, 2.0));
				occultedArea *= 100.0;
				if (occultedArea > 99.999) occultedArea = 100.0;
				if (occultedArea == 100.0) moon.magnitude = APPARENT_MAGNITUDE_WHEN_SATELLITE_IS_NOT_VISIBLE;
				if (occultedArea > 0.0 && occultedArea <= 100.0)
				{
					if (!moon.mutualPhenomena.equals("")) moon.mutualPhenomena += ", ";
					moon.mutualPhenomena += "OCCULTED BY " + planet.name() + " ("+(float) occultedArea+"%)";
					double fractionVisible = 1.0 - occultedArea / 100.0;
					moon.magnitude -= Math.log10(fractionVisible) * 2.5;
				}
			} else {
				if (moon.occulted) moon.magnitude = APPARENT_MAGNITUDE_WHEN_SATELLITE_IS_NOT_VISIBLE;			
			}

			// From Sun
			boolean inferior_sun = (moon.zPositionFromSun <= 0.0);
			Y1 = moon.yPositionFromSun * flattening;
			boolean withinDisc_sun = (Math.sqrt(moon.xPositionFromSun * moon.xPositionFromSun + Y1 * Y1) < (1.0 + satSize));
			boolean eclipsed = withinDisc_sun && !inferior_sun;
			boolean shadow_transiting = withinDisc_sun && inferior_sun;

			moon.eclipsed = eclipsed;
			moon.shadowTransiting = shadow_transiting;

			if (moon.eclipsed && Math.sqrt(moon.xPositionFromSun * moon.xPositionFromSun + Y1 * Y1) > (1.0 - satSize))
			{
				double occultedArea = MoonEphem.getOccultedArea(ephem.angularRadius, moon.angularRadius, Math.sqrt(moon.xPositionFromSun * moon.xPositionFromSun + Y1 * Y1) * ephem.angularRadius);
				occultedArea /= (Math.PI * Math.pow(moon.angularRadius, 2.0));
				occultedArea *= 100.0;
				if (occultedArea > 99.999) occultedArea = 100.0;
				if (occultedArea == 100.0) moon.magnitude = APPARENT_MAGNITUDE_WHEN_SATELLITE_IS_NOT_VISIBLE;
				if (occultedArea > 0.0 && occultedArea <= 100.0)
				{
					if (!moon.mutualPhenomena.equals("")) moon.mutualPhenomena += ", ";
					moon.mutualPhenomena += "ECLIPSED BY " + planet.name() + " ("+(float) occultedArea+"%)";
					double fractionVisible = 1.0 - occultedArea / 100.0;
					if (moon.magnitude != APPARENT_MAGNITUDE_WHEN_SATELLITE_IS_NOT_VISIBLE) moon.magnitude -= Math.log10(fractionVisible) * 2.5;
				}
			} else {
				if (moon.eclipsed) moon.magnitude = APPARENT_MAGNITUDE_WHEN_SATELLITE_IS_NOT_VISIBLE;			
			}

			return moon;

	}

	private static double getOccultedArea(double R, double r, double d)
	{
		if (R >= (r+d)) return Math.PI * r * r;
		double tmp = R;
		if (r > R) {
			R = r;
			r = tmp;
		}
		if (R >= (r+d)) return Math.PI * r * r;
			
		double a = r * r * Math.acos((d * d + r * r - R * R) / (2.0 * d * r));
		a += R * R * Math.acos((d * d + R * R - r * r) / (2.0 * d * R));
		a -= 0.5 * Math.sqrt((-d+r+R) * (d+r-R) * (d-r+R) * (d+r+R));
		return a;
	}
	
	// Calculates satellites phenomena like transits, eclipses, etc.
	// Calculations are respect to the other satellites
	static Map<Target,MoonEphemElement> satellitesPhenomena(Map<Target,MoonEphemElement> moons_obj, double planet_angular_radius)

	{
		//double fl = 1.0 - Target.getFlatenningFactor(Target.getCentralBody(Target.getID(moons_obj[0].name)));
		Map<Target,MoonEphemElement> moons = new LinkedHashMap<Target, MoonEphemElement>();
		for(Target i : moons_obj.keySet()){
			moons.put(i,(org.asterope.ephem.moons.MoonEphemElement) moons_obj.get(i).clone());
		}
		
		for (Target i : moons.keySet())
		{
			for (Target j : moons.keySet())				
			{
				if(j.ordinal()<=i.ordinal())
					continue;
				
				MoonEphemElement moonI = moons.get(i);
				MoonEphemElement moonJ = moons.get(j);
					
				double size = (moonI.angularRadius + moonJ.angularRadius) / planet_angular_radius;
				double r = Math.sqrt(Math.pow(moonI.xPosition - moonJ.xPosition, 2.0) + Math.pow(moonI.yPosition - moonJ.yPosition, 2.0));
				double ri = Math.sqrt(Math.pow(moonI.xPosition, 2.0) + Math.pow(moonI.yPosition, 2.0));
				double rj = Math.sqrt(Math.pow(moonJ.xPosition, 2.0) + Math.pow(moonJ.yPosition, 2.0));
				
/*				LocationElement loci = LocationElement.parseRectangularCoordinates(moonsI.xPosition,
						moonsI.yPosition, 0.0);
				LocationElement locj = LocationElement.parseRectangularCoordinates(moonJ.xPosition,
						moonJ.yPosition, 0.0);
				double ri = loci.getRadius(), rj = locj.getRadius();
				double r = LocationElement.getLinearDistance(loci, locj);
*/
				if (r <= size && (!moonI.inferior || ri > 1.0) && (!moonJ.inferior || rj > 1.0))
				{
					MoonEphemElement body_behind = moonI;
					MoonEphemElement body_infront = moonJ;
					if (moonI.zPosition < moonJ.zPosition)
					{
						body_behind = moonJ;
						body_infront = moonI;
					}

					double occultedArea = MoonEphem.getOccultedArea(body_infront.angularRadius, body_behind.angularRadius, r * planet_angular_radius);
					occultedArea /= (Math.PI * Math.pow(body_behind.angularRadius, 2.0));
					occultedArea *= 100.0;
					if (occultedArea > 99.999) occultedArea = 100.0;
//					int occulted = (int) (occultedArea + 0.5);
					
//					moons[body_behind].occulted = true;
//					if (occultedArea > 0.5) {
						if (!body_behind.mutualPhenomena.equals("")) body_behind.mutualPhenomena += ", ";
						body_behind.mutualPhenomena += "OCCULTED BY " + body_infront.name + " ("+(float) occultedArea+"%)";
						
						double fractionVisible = 1.0 - occultedArea / 100.0;
						if (fractionVisible == 0.0)
						{
							body_behind.magnitude = APPARENT_MAGNITUDE_WHEN_SATELLITE_IS_NOT_VISIBLE;
						} else {
							body_behind.magnitude -= Math.log10(fractionVisible) * 2.5;
						}
//					}
				}

				r = Math.sqrt(Math.pow(moonI.xPositionFromSun - moonJ.xPositionFromSun, 2.0) + Math.pow(moonI.yPositionFromSun - moonJ.yPositionFromSun, 2.0));
				ri = Math.sqrt(Math.pow(moonI.xPositionFromSun, 2.0) + Math.pow(moonI.yPositionFromSun, 2.0));
				rj = Math.sqrt(Math.pow(moonJ.xPositionFromSun, 2.0) + Math.pow(moonJ.yPositionFromSun, 2.0));
				
/*				loci = LocationElement.parseRectangularCoordinates(moonsI.xPositionFromSun,
						moonsI.yPositionFromSun, 0.0);
				locj = LocationElement.parseRectangularCoordinates(moonJ.xPositionFromSun,
						moonJ.yPositionFromSun, 0.0);
				ri = loci.getRadius();
				rj = locj.getRadius();
				r = LocationElement.getLinearDistance(loci, locj);
*/
				if (r <= size && (!moonI.inferior || ri > 1.0) && (!moonJ.inferior || rj > 1.0))
				{
					MoonEphemElement body_behind = moonI;
					MoonEphemElement body_infront = moonJ;
					if (moonI.zPositionFromSun < moonJ.zPositionFromSun)
					{
						body_behind = moonJ;
						body_infront = moonI;
					}

					double occultedArea = MoonEphem.getOccultedArea(body_infront.angularRadius, body_behind.angularRadius, r * planet_angular_radius);
					occultedArea /= (Math.PI * Math.pow(body_behind.angularRadius, 2.0));
					occultedArea *= 100.0;
					if (occultedArea > 99.999) occultedArea = 100.0;
//					int occulted = (int) (occultedArea + 0.5);
					
//					moons[body_behind].eclipsed = true;
//					if (occultedArea > 0.5) {
						if (!body_behind.mutualPhenomena.equals("")) body_behind.mutualPhenomena += ", ";
						body_behind.mutualPhenomena += "ECLIPSED BY " + body_infront.name + " ("+(float) occultedArea+"%)";
						
						double fractionVisible = 1.0 - occultedArea / 100.0;
						if (fractionVisible == 0.0)
						{
							body_behind.magnitude = APPARENT_MAGNITUDE_WHEN_SATELLITE_IS_NOT_VISIBLE;
						} else {
							if (body_behind.magnitude != APPARENT_MAGNITUDE_WHEN_SATELLITE_IS_NOT_VISIBLE) body_behind.magnitude -= Math.log10(fractionVisible) * 2.5;
						}
//					}
				}

			}
		}
		return moons;
	}

	/**
	 * Obtain position of Triton, the moon of Neptune.
	 * <P>
	 * Page 373 of the Explanatory Supplement of the Astronomical Almanac.
	 * <P>
	 * Code taken from Guide software.
	 * 
	 * @param time Time object containing the date.
	 * @param obs Observer object containing the observer position.
	 * @param eph Ephemeris object with the target and ephemeris
	 *        properties.
	 * @return Object with the ephemeris.
	 * @ If the calculation fails.
	 */
	public static MoonEphemElement triton(TimeElement time, // Time Element
			ObserverElement obs, // Observer Element
			EphemerisElement eph // Ephemeris Element
	) 
	{
			// Obtain ephemeris of Neptune
			EphemerisElement new_eph = (EphemerisElement) eph.clone();
			new_eph.targetBody = Target.Neptune;
			EphemElement ephem = moshier.getEphemeris(time, obs, new_eph,true);

			double jd = TimeScale.getJD(time, obs, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
			if (eph.ephemType != EphemerisElement.Ephem.GEOMETRIC)
				jd = jd - ephem.lightTime;

			double t_cent = (jd - EphemConstant.J2000) / 36525.;
			double n = (359.28 + 54.308 * t_cent) * EphemConstant.DEG_TO_RAD;
			double t0 = 2433282.5;
			double theta = (151.401 + .57806 * (jd - t0) / 365.25) * EphemConstant.DEG_TO_RAD;
			/*
			 * Semimajor axis is 488.49 arcseconds at one AU, so semimajor is in
			 * AU
			 */
			double semimajor = 488.49 * EphemConstant.DEG_TO_RAD / 3600.;
			double longitude = (200.913 + 61.2588532 * (jd - t0)) * EphemConstant.DEG_TO_RAD;
			double gamma = 158.996 * EphemConstant.DEG_TO_RAD;

			/* Calculate longitude and latitude on invariable plane: */
			double lon_on_ip = theta + Math.atan2(Math.sin(longitude) * Math.cos(gamma), Math.cos(longitude));
			double lat_on_ip = Math.asin(Math.sin(longitude) * Math.sin(gamma));

			/* Vector defining Triton position in invariable plane space: */
			double triton[] = LocationElement.parseLocationElement(new LocationElement(lon_on_ip, lat_on_ip, 1.0));

			/*
			 * This position of north pole is prior to IAU2000 recomendations.
			 * It seems better to maintain as it was defined in AA
			 */
			double ra = 298.72 * EphemConstant.DEG_TO_RAD + 2.58 * EphemConstant.DEG_TO_RAD * Math.sin(n) - 0.04 * EphemConstant.DEG_TO_RAD * Math
					.sin(n + n);
			double dec = 42.63 * EphemConstant.DEG_TO_RAD - 1.90 * EphemConstant.DEG_TO_RAD * Math.cos(n) + 0.01 * EphemConstant.DEG_TO_RAD * Math
					.cos(n + n);

			double pole[] = Precession.precessFromJ2000(EphemConstant.B1950, LocationElement
					.parseLocationElement(new LocationElement(ra, dec, 1.0)), eph.ephemMethod);
			LocationElement loc = LocationElement.parseRectangularCoordinates(pole);
			/* Vectors defining invariable plane, expressed in B1950: */
			double x_axis[] = LocationElement.parseLocationElement(new LocationElement(loc.getLongitude() + Math.PI,
					loc.getLatitude(), 1.0));
			double y_axis[] = LocationElement.parseLocationElement(new LocationElement(loc.getLongitude() + Math.PI,
					loc.getLatitude(), 1.0));
			double z_axis[] = LocationElement.parseLocationElement(new LocationElement(loc.getLongitude(), loc
					.getLatitude(), 1.0));

			/* Obtain position of Triton refered to B1950 */
			double vect_1950[] = new double[3];
			for (int i = 0; i < 3; i++)
			{
				vect_1950[i] = semimajor * (x_axis[i] * triton[0] + y_axis[i] * triton[1] + z_axis[i] * triton[2]);
			}

			/* Precess to date */
			double results[] = Precession.precess(EphemConstant.B1950, jd, vect_1950, eph.ephemMethod);

			// Obtain equatorial and horizontal coordinates
			LocationElement sat_loc = new LocationElement(ephem.rightAscension, ephem.declination, ephem.distance);
			double pos[] = LocationElement.parseLocationElement(sat_loc);
			double sat_pos[] = EphemUtils.sumVectors(pos, results);
			sat_loc = LocationElement.parseRectangularCoordinates(sat_pos);
			LocationElement hor_loc = CoordinateSystem.equatorialToHorizontal(sat_loc, time, obs, eph);

			// Set results of ephemeris
			String nom = Target.Triton.name();
			ra = sat_loc.getLongitude();
			dec = sat_loc.getLatitude();
			double dist = sat_loc.getRadius();
			double azi = hor_loc.getLongitude();
			double ele = hor_loc.getLatitude();
			double ill = ephem.phase;
			double elo = ephem.elongation;

			// From the observer
			double X = results[0];
			double Y = results[1];
			double Z = results[2];
			double flattening = Target.getEquatorialRadius(Target.Neptune) / Target.getPolarRadius(Target.Neptune);
			double x_sun = results[0];
			double y_sun = results[1];
			double z_sun = results[2];

			boolean inferior = (Z <= 0.0);
			double Y1 = Y * flattening;
			boolean withinDisc = (Math.sqrt(X * X + Y1 * Y1) < 1.0);
			boolean transiting = withinDisc && inferior;
			boolean occulted = withinDisc && !inferior;

			// From Sun
			boolean inferior_sun = (z_sun <= 0.0);
			Y1 = y_sun * flattening;
			boolean withinDisc_sun = (Math.sqrt(x_sun * x_sun + Y1 * Y1) < 1.0);
			boolean eclipsed = withinDisc_sun && !inferior_sun;
			boolean shadow_transiting = withinDisc_sun && inferior_sun;

			// Create ephemeris object
			MoonEphemElement moon = new MoonEphemElement(nom, ra, dec, dist,
					ephem.distanceFromSun + (dist - ephem.distance), azi, ele, (float) ill, (float) elo, eclipsed, occulted,
					transiting, shadow_transiting, inferior, X, Y, Z, x_sun, y_sun, z_sun);

			return moon;


	}


	static double squared(double a)
	{
		return a * a;
	}

	static double sin_deg(double deg)
	{
		return Math.sin(deg * EphemConstant.DEG_TO_RAD);
	}

	static double cos_deg(double deg)
	{
		return Math.cos(deg * EphemConstant.DEG_TO_RAD);
	}

	static double atan2_deg(double a1, double a2)
	{
		return Math.atan2(a1, a2) * EphemConstant.RAD_TO_DEG;
	}

	static double atan_deg(double tan)
	{
		return Math.atan(tan) * EphemConstant.RAD_TO_DEG;
	}

	static double asin_deg(double sin)
	{
		return Math.asin(sin) * EphemConstant.RAD_TO_DEG;
	}


}

/*
ROCKS
0.0035588596031140593
0.001587909370744096
4.079661019750992E-6
-6.545994489675651
14.62802417538852
0.008942327998278533

UranusGUST86
* 0.0018528551695243993
* -0.001286817353891473
* 0.0031747821791967556

* 0.0015506126321969335
* -1.0452393694356154E-4
* -9.52423005474941E-4

*/

/*
Ephemeris for Charon is from Tholen, D.J. (1985) Astron. J., 90,
2353-2359

void
plusat(const double jd, double &X, double &Y, double &Z)
{
  double td = jd - 2445000.5;                      // Julian days from
                                                   // reference date
  const double a = 19130 / AU_to_km;               // semimajor axis (km)
  
  const double n = 360 / 6.38723;                  // mean motion (degrees/day)
  const double E = (78.6 + n * td) * deg_to_rad;   // eccentric anomaly
  const double i = 94.3 * deg_to_rad;              // inclination of orbit
  const double o = 223.7 * deg_to_rad;             // longitude of ascending node

  // rectangular coordinates on the orbit plane, x-axis is toward
  // pericenter
  X = a * cos(E);
  Y = a * sin(E);
  Z = 0;

  // rotate towards Earth equator B1950
  rotateX(X, Y, Z, -i);

  // rotate to vernal equinox
  rotateZ(X, Y, Z, -o);

  // precess to J2000
  precessB1950J2000(X, Y, Z);
}
*/
