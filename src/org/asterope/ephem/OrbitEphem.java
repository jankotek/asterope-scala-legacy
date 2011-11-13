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
 * Provides method for calculating ephemeris of planets, comets, and asteroids,
 * using orbital elements, as well as for obtaining data of the orbital
 * elements.
 * <P>
 * Orbital elements for comets, asteroids, and other related objects
 * (trans-Neptunian, Centaurs, Trojans, ...) can be read in two formats: the
 * official format of the Minor Planet Center, or the format of the commercial
 * program SkyMap.
 * <P>
 * Orbital elements of planets were taken from the IMCCE's VSOP87 theory. They
 * cover the time spand 1000 B.C. to 5000 A.D., in intervals of 1 year for
 * current epochs and 5 years in the remote past or future. Elements are refered
 * to mean equinox and ecliptic of J2000 epoch. All planets except Earth are
 * available.
 * <P>
 * These orbital elements are suitable for accurate ephemeris, with errors well
 * below the arcsecond level when comparing to the full VSOP87 theory. An
 * additional advantage is the calculation speed.<P>
 * 
 * Example of use applying the main Ephem class:<P>
 * <pre>
 * try
 * {
 *		AstroDate astro = new AstroDate(1, AstroDate.JANUARY, 2001, 0, 0, 0);
 *		TimeElement time = new TimeElement(astro.toGCalendar(), TimeElement.TERRESTRIAL_TIME);
 *		ObserverElement observer = new ObserverElement.parseCity(City.findCity("Madrid"));
 *		EphemerisElement eph = new EphemerisElement(Target.NOT_A_PLANET, EphemerisElement.EPHEM_APPARENT,
 *				EphemerisElement.EQUINOX_OF_DATE, EphemerisElement.TOPOCENTRIC, EphemerisElement.APPLY_WILLIAMS,
 *				EphemerisElement.FRAME_ICRS, EphemerisElement.ALGORITHM_ORBIT);
 *		eph.orbit = OrbitEphem.getOrbitalElementsOfMinorBody(&quot;Ceres&quot;);
 *		if (eph.orbit != null) {
 *			String name = &quot;Ceres&quot;;
 *			EphemElement ephem = Ephem.getEphemeris(time, observer, eph, true);
 *			ConsoleReport.basicReportToConsole(ephem, name);
 *		}
 * } catch (JPARSECException ve)
 * {
 *		ve.showException();
 * } 
 * </pre><P>
 * It is recommended to use the particular method for this class instead of the main Ephem
 * class. The process is the same as in the {@linkplain Probes} class, changing Probe by Comet or Asteroid.<P>
 * 
 * @see OrbitalElement
 * @see ReadFile
 * @see Ephem
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class OrbitEphem
{

	/**
	 * Obtain rectangular coordinates of the object in the orbit plane,
	 * according to the object's orbit: elliptic, parabolic, or hyperbolic.
	 * 
	 * @param orbit {@linkplain OrbitalElement} object.
	 * @param JD Julian day of calculations.
	 * @return Array with x, y, vx, vy in the orbit plane.
	 */
	public static double[] orbitPlane(OrbitalElement orbit, double JD)
	{
		double exc = orbit.eccentricity;

		if (exc < 1.0)
			return elliptic(orbit, JD);
		if (exc == 1.0)
			return parabolic(orbit, JD);
		if (exc > 1.0)
			return hyperbolic(orbit, JD);

		return new double[] { 0.0, 0.0, 0.0, 0.0 };
	}

	/**
	 * Calculate rectangular coordinates for an elliptic orbit.
	 * 
	 * @param orbit {@linkplain OrbitalElement} object. ECCENTRICITY is supposed to be lower
	 *        than unity.
	 * @param JD Julian Day of calculations.
	 * @return Array with x, y, vx, vy values in the orbit plane at time JD,
	 *         refered to the epoch of the orbital elements.
	 */
	public static double[] elliptic(OrbitalElement orbit, double JD)
	{
		double RHO, CE, SE, tmp;
		double mean_anom_at_epoch, m, de, E;
		double x, y, vx, vy;

		/*
		 * mean_anom_at_epoch is proportional to the area swept out by the
		 * radius vector of a circular orbit during the time between perihelion
		 * passage and Julian date JD. It is the mean anomaly at time JD.
		 */
		mean_anom_at_epoch = orbit.meanMotion * (JD - orbit.referenceTime) + orbit.meanAnomaly;
		m = EphemUtils.normalizeRadians(mean_anom_at_epoch);

		/*
		 * By Kepler's second law, m must be equal to the area swept out in the
		 * same time by an elliptical orbit of same total area. Integrate the
		 * ellipse expressed in polar coordinates r = a(1-e^2)/(1 + e cosW) with
		 * respect to the angle W to get an expression for the area swept out by
		 * the radius vector. The area is given by the mean anomaly; the angle
		 * is solved numerically. The answer is obtained in two steps. We first
		 * solve Kepler's equation M = E - eccent*sin(E) for the eccentric
		 * anomaly E. Then there is a closed form solution for W in terms of E.
		 */

		/*
		 * Initial guess improves convergency. Note we are following Danby's
		 * "The Solution of Kepler's Equation", Celestial Mechanics 31, (1983)
		 * 95-107, and Celestial Mechanics 40 (1987), 303-312.
		 */
		E = m + Math.sin(m) * .85 * orbit.eccentricity / Math.abs(Math.sin(m));
		int iteration = 0;
		de = 1.0;
		do
		{
			/*
			 * The approximate area swept out in the ellipse, temp = E - eccent *
			 * sin(E) ...minus the area swept out in the circle, -m ...should be
			 * zero. Use the derivative of the error to converge to solution by
			 * Newton's method.
			 */

			de = (m + orbit.eccentricity * Math.sin(E) - E) / (1.0 - orbit.eccentricity * Math.cos(E));
			E = E + de;
			iteration++;
		} while (iteration < 20 && Math.abs(de) > 1E-15);

		/*
		 * The exact formula for the area in the ellipse is
		 * 2.0*atan(c2*tan(0.5*W)) - c1*eccent*sin(W)/(1+e*cos(W)) where c1 =
		 * sqrt( 1.0 - eccent*eccent ) c2 = sqrt( (1.0-eccent)/(1.0+eccent) ).
		 * Substituting the following value of W yields the exact solution of
		 * true anomaly.
		 */
		// tmp = Math.sqrt( (1.0+orbit.ECCENTRICITY)/(1.0-orbit.ECCENTRICITY) );
		// double true_anomaly = 2.0 * Math.atan( tmp * Math.tan(0.5*E) );
		/*
		 * Obtain rectangular (cartesian) coordinates and velocities in the
		 * orbit plane.
		 */
		tmp = Math.sqrt(1.0 - orbit.eccentricity * orbit.eccentricity);
		CE = Math.cos(E);
		SE = Math.sin(E);

		// Obtain mean motion
		RHO = 1.0 - orbit.eccentricity * CE;

		// Obtain rectangular position and velocity
		x = orbit.semimajorAxis * (CE - orbit.eccentricity);
		y = orbit.semimajorAxis * tmp * SE;
		vx = -orbit.semimajorAxis * orbit.meanMotion * SE / RHO;
		vy = orbit.semimajorAxis * orbit.meanMotion * tmp * CE / RHO;

		return new double[]
		{ x, y, vx, vy };
	}

	/**
	 * Calculate rectangular coordinates for a parabolic orbit.
	 * 
	 * @param orbit {@linkplain OrbitalElement} object. ECCENTRICITY is supposed to be equal
	 *        to unity.
	 * @param JD Julian Day of calculations.
	 * @return Array with x, y, vx, vy values in the orbit plane at time JD,
	 *         refered to the epoch of the orbital elements.
	 */
	public static double[] parabolic(OrbitalElement orbit, double JD)
	{
		double Q, A, B, TAV2, k, r, x, y, vx, vy;

		// Perihelion distance
		Q = orbit.perihelionDistance;

		// Solve Baker's equation
		A = 3.0 * EphemConstant.EARTH_MEAN_ORBIT_RATE * (JD - orbit.referenceTime) / (2.0 * Q * Math.sqrt(2.0 * Q));
		B = Math.pow((A + Math.sqrt(A * A + 1.0)), 1.0 / 3.0);
		TAV2 = B - 1.0 / B;

		// Obtain mean motion
		k = EphemConstant.EARTH_MEAN_ORBIT_RATE / Math.sqrt(2.0 * Q);
		r = Q * (1.0 + TAV2 * TAV2);

		// Obtain rectangular position and velocity
		x = Q * (1.0 - TAV2 * TAV2);
		y = 2.0 * Q * TAV2;
		vx = -k * y / r;
		vy = k * (x / r + 1.0);

		return new double[]
		{ x, y, vx, vy };
	}

	/**
	 * Calculate rectangular coordinates for a hyperbolic orbit.
	 * 
	 * @param orbit {@linkplain OrbitalElement} object. ECCENTRICITY is supposed to be
	 *        greater than unity.
	 * @param JD Julian Day of calculations.
	 * @return Array with x, y, vx, vy values in the orbit plane at time JD,
	 *         refered to the epoch of the orbital elements.
	 */
	public static double[] hyperbolic(OrbitalElement orbit, double JD)
	{
		double mean_anom_at_epoch, m, E, DE, x, y, vx, vy;

		/*
		 * The equation of the hyperbola in polar coordinates r, theta is r =
		 * a(e^2 - 1)/(1 + e cos(theta)) so the perihelion distance q = a(e-1),
		 * the "mean distance" a = q/(e-1).
		 */
		double semimajor_axis = Math.abs(orbit.semimajorAxis);
		mean_anom_at_epoch = orbit.meanAnomaly + orbit.meanMotion * (JD - orbit.referenceTime);
		m = EphemUtils.normalizeRadians(mean_anom_at_epoch);

		/* solve M = -E + e sinh E */
		int iteration = 0;
		/*
		 * Initial guess improves convergency. Note we are following Danby's
		 * "The Solution of Kepler's Equation", Celestial Mechanics 31, (1983)
		 * 95-107, and Celestial Mechanics 40 (1987), 303-312.
		 */
		E = Math.log(m * 2.0 / orbit.eccentricity + 1.8);
		do
		{
			DE = (-m - E + orbit.eccentricity * Math.sinh(E)) / (1.0 - orbit.eccentricity * Math.cosh(E));
			E = E + DE;
		} while (Math.abs(E) < 100.0 && iteration < 20 && Math.abs(DE) > 1e-10);

		// If no convergency is reached, then retry with a more adequate initial
		// value. Sorry I can't give reference about this, I cannot remember now...
		if (Math.abs(E) > 100.0 || Math.abs(DE) > 1e-5)
		{
			E = (m / Math.abs(m)) * Math.pow(6.0 * Math.abs(m), 1.0 / 3.0);
			do
			{
				DE = (-m - E + orbit.eccentricity * Math.sinh(E)) / (1.0 - orbit.eccentricity * Math.cosh(E));
				E = E + DE;
			} while (Math.abs(E) < 100.0 && iteration < 20 && DE > 1e-10);
		}

		// Exception if no convergency is reached
		if (Math.abs(E) > 100.0 || Math.abs(DE) > 1e-5) 
			throw new RuntimeException("no convergency was reached when computing hyperbolic position in orbit plane.");
//			return new double[]	{ 0, 0, 0, 0 };

		// Obtain rectangular position and velocity
		x = semimajor_axis * (orbit.eccentricity - Math.cosh(E));
		y = semimajor_axis * Math.sqrt(orbit.eccentricity * orbit.eccentricity - 1.0) * Math.sinh(E);
		vx = orbit.meanMotion * semimajor_axis * Math.sinh(E) / (1.0 - orbit.eccentricity * Math.cosh(E));
		vy = -orbit.meanMotion * semimajor_axis * Math.sqrt(orbit.eccentricity * orbit.eccentricity - 1.0) * Math
				.cosh(E) / (1.0 - orbit.eccentricity * Math.cosh(E));

		return new double[] { x, y, vx, vy };
	}

	/**
	 * Transform coordinates from the orbit plane to the ecliptic plane.
	 * 
	 * @param orbit {@linkplain OrbitalElement} object.
	 * @param position Array with x, y, vx, vy values.
	 * @return Array with x, y, z, vx, vy, vz values.
	 */
	public static double[] toEclipticPlane(OrbitalElement orbit, double[] position)
	{
		double C1, C2, C3, S1, S2, S3;
		double MAT[][] = new double[4][4];
		double out[] = new double[6];

		// Obtain arguments
		C1 = Math.cos(orbit.argumentOfPerihelion);
		C2 = Math.cos(orbit.inclination);
		C3 = Math.cos(orbit.ascendingNodeLongitude);
		S1 = Math.sin(orbit.argumentOfPerihelion);
		S2 = Math.sin(orbit.inclination);
		S3 = Math.sin(orbit.ascendingNodeLongitude);

		// Calculate matrix
		MAT[1][1] = C1 * C3 - S1 * C2 * S3;
		MAT[1][2] = -S1 * C3 - C1 * C2 * S3;
		MAT[1][3] = S2 * S3;
		MAT[2][1] = C1 * S3 + S1 * C2 * C3;
		MAT[2][2] = -S1 * S3 + C1 * C2 * C3;
		MAT[2][3] = -S2 * C3;
		MAT[3][1] = S1 * S2;
		MAT[3][2] = C1 * S2;
		MAT[3][3] = C2;

		// Apply rotation
		out[0] = MAT[1][1] * position[0] + MAT[1][2] * position[1]; // x
		out[1] = MAT[2][1] * position[0] + MAT[2][2] * position[1]; // y
		out[2] = MAT[3][1] * position[0] + MAT[3][2] * position[1]; // z
		out[3] = MAT[1][1] * position[2] + MAT[1][2] * position[3]; // vx
		out[4] = MAT[2][1] * position[2] + MAT[2][2] * position[3]; // vy
		out[5] = MAT[3][1] * position[2] + MAT[3][2] * position[3]; // vz

		return out;
	}



	/**
	 * Obtain probable diameter of a dwarf object knowing the absolute magnitude
	 * and the albedo.
	 * <P>
	 * The value is given by evaluating a function that was obtained after a fit
	 * from a tabulated table from the Minor Planet Center, called <I>Conversion
	 * of Absolute Magnitude to Diameter</I>. The fit found is excelent in the
	 * albedo interval of the table, 0.05 to 0.5.
	 * <P>
	 * Typical icy objects in the outer Solar System will have an albedo of 0.5.
	 * Rocky objects lies between 0.25 and 0.05.
	 * 
	 * @param H Absolute magnitude.
	 * @param albedo Albedo of the object.
	 * @return Probable diameter in km.
	 */
	public static double getProbableDiameter(double H, double albedo)
	{
		double diameter = (4172.0 * Math.sqrt(albedo)) * Math.exp(-0.23 * H);

		return diameter;
	}

	/**
	 * Obtain probable albedo of a dwarf object knowing the absolute magnitude
	 * and the radius.
	 * <P>
	 * The value is given by evaluating a function that was obtained after a fit
	 * from a tabulated table from the Minor Planet Center, called <I>Conversion
	 * of Absolute Magnitude to Diameter</I>. The fit found is excelent in the
	 * albedo interval of the table, 0.05 to 0.5.
	 * <P>
	 * 
	 * @param H Absolute magnitude.
	 * @param diameter Diameter of the object in km.
	 * @return Probable albedo.
	 */
	public static double getProbableAlbedo(double H, double diameter)
	{
		double albedo = Math.pow((diameter / 4172.0) / Math.exp(-0.23 * H), 2.0);

		return albedo;
	}

	/**
	 * Obtains the mean motion of an object in a elliptic orbit given it's
	 * semimajor axis. This method takes use of the mass of the body, so it
	 * slightly improves accuracy respect to default value for a massless
	 * object.
	 * 
	 * @param planet Planet ID constant, can be any planet or Pluto.
	 * @param a Semimajor axis.
	 * @return The mean motion in radians per day.
	 */
	public static double obtainMeanMotion(Target planet, double a)
	{
		double mass = 0.0;
		mass = 1.0 / Target.getRelativeMass(planet);

		return obtainMeanMotion(mass, a);
	}

	/**
	 * Obtains the mean motion of an object in a elliptic orbit given it's
	 * semimajor axis and mass. The mass ob the body is considered, so it
	 * slightly improves accuracy respect to default value for a massless
	 * object.
	 * 
	 * @param mass Mass of the body in kg.
	 * @param a Semimajor axis.
	 * @return The mean motion in radians per day.
	 */
	public static double obtainMeanMotion(double mass, double a)
	{
		double G = EphemConstant.GRAVITATIONAL_CONSTANT * EphemConstant.SUN_MASS * Math.pow(EphemConstant.SECONDS_PER_DAY, 2.0) / Math
				.pow(EphemConstant.AU * 1000.0, 3.0);
		double mean_motion = Math.sqrt(G * (1.0 + mass / EphemConstant.SUN_MASS) / a) / a;
		return mean_motion;
	}

	/**
	 * Obtain a set of orbital elements knowing the position and velocity
	 * vectors in certain instant, according to the classical theory of the two
	 * body motion.
	 * <P>
	 * For reference see Practical Ephemeris Calculations, by Oliver
	 * Montenbruck, chapter 3.
	 * 
	 * @param pos Heliocentric position vector in ecliptic coordinates, in AU.
	 * @param v Heliocentric velocity vector in ecliptic coordinates, in AU /
	 *        day.
	 * @param jd Time of vectors as Julian day. Should be previously corrected
	 *        for light time if possible.
	 * @param mass Mass of the orbiting body in kg. Can be set to zero if it is
	 *        very low or unknown.
	 * @return A set of orbital elements describing the shape and orientation of
	 *         the orbit, and ready for subsequent ephemeris calculations (all
	 *         data except magnitude, mag slope, name, and applicable times).
	 *         Mean equinox and ecliptic of date.
	 */
	public static OrbitalElement obtainOrbitalElementsFromPositionAndVelocity(double pos[], double v[], double jd,
			double mass)
	{
		double c[] = new double[]
		{ pos[1] * v[2] - pos[2] * v[1], pos[2] * v[0] - pos[0] * v[2], pos[0] * v[1] - pos[1] * v[0] };

		LocationElement cloc = LocationElement.parseRectangularCoordinates(c);
		double modc = cloc.getRadius();
		if (modc == 0.0)
		{
			throw new IllegalArgumentException(
					"movement is in straight line through the sun. No solution.");
		}

		double inc = Math.acos(c[2] / modc);
		double omega = Math.atan2(c[0], -c[1]);

		double r = Math.sqrt(pos[0] * pos[0] + pos[1] * pos[1] + pos[2] * pos[2]);
		double sinu = pos[2] / (r * Math.sin(inc));
		double cosu = (pos[0] * Math.cos(omega) + pos[1] * Math.sin(omega)) / r;
		double u = Math.atan2(sinu, cosu);

		double v2 = v[0] * v[0] + v[1] * v[1] + v[2] * v[2];
		double G = EphemConstant.GRAVITATIONAL_CONSTANT * EphemConstant.SUN_MASS * Math.pow(EphemConstant.SECONDS_PER_DAY, 2.0) / Math
				.pow(EphemConstant.AU * 1000.0, 3.0);
		double a_inverse = 2.0 / r - v2 / (G * (1.0 + mass / EphemConstant.SUN_MASS));

		double p = modc * modc / (G * (1.0 + mass / EphemConstant.SUN_MASS));
		double exc = Math.sqrt(1.0 - p * a_inverse);
		double q = p / (1.0 + exc);

		double a = 0.0;
		if (a_inverse != 0.0)
			a = 1.0 / a_inverse;

		double true_anomaly = 0.0, mean_motion = 0.0, mean_anomaly = 0.0, perihelion_time = 0.0;
		// Elliptic motion
		if (a_inverse > 0.0)
		{
			double cosE = (1.0 - r * a_inverse) / exc;
			double sinE = (pos[0] * v[0] + pos[1] * v[1] + pos[2] * v[2]) / (exc * Math
					.sqrt(G * (1.0 + mass / EphemConstant.SUN_MASS) / a_inverse));
			double E = Math.atan2(sinE, cosE);

			mean_anomaly = E - exc * Math.sin(E);
			true_anomaly = 2.0 * Math.atan(Math.tan(E * 0.5) * Math.sqrt((1.0 + exc) / (1.0 - exc)));
			mean_motion = Math.sqrt(G * (1.0 + mass / EphemConstant.SUN_MASS) / a) / a;
			perihelion_time = jd - mean_anomaly / mean_motion;
		}
		// Parabolic motion
		if (a_inverse == 0.0)
		{
			true_anomaly = 2.0 * Math.atan((pos[0] * v[0] + pos[1] * v[1] + pos[2] * v[2]) / Math
					.sqrt(2.0 * q * G * (1.0 + mass / EphemConstant.SUN_MASS)));
			double vt = (1.0 / 3.0) * Math.pow(Math.tan(true_anomaly * 0.5), 3.0) + Math.tan(true_anomaly * 0.5);
			perihelion_time = jd - Math.sqrt(2.0 * Math.pow(q, 3.0) / (G * (1.0 + mass / EphemConstant.SUN_MASS))) * vt;
		}
		// Hyperbolic motion
		if (a_inverse < 0.0)
		{
			double cosH = (1.0 - r * a_inverse) / exc;
			double sinH = (pos[0] * v[0] + pos[1] * v[1] + pos[2] * v[2]) / (exc * Math
					.sqrt(Math.abs(a) * G * (1.0 + mass / EphemConstant.SUN_MASS)));
			double H = Math.atan2(sinH, cosH);

			mean_anomaly = -(H - exc * Math.sinh(H));
			true_anomaly = 2.0 * Math.atan(Math.tanh(H * 0.5) * Math.sqrt((1.0 + exc) / (1.0 - exc)));
			mean_motion = Math.sqrt(G * (1.0 + mass / EphemConstant.SUN_MASS) / Math.abs(a)) / Math.abs(a);
			perihelion_time = jd - (exc * Math.sinh(H) - H) * Math
					.sqrt(Math.pow(Math.abs(a), 3.0) / (G * (1.0 + mass / EphemConstant.SUN_MASS)));
		}

		double perih_arg = u - true_anomaly;
		double perih_lon = perih_arg + omega;

		OrbitalElement orbit = new OrbitalElement();
		orbit.argumentOfPerihelion = perih_arg;
		orbit.ascendingNodeLongitude = omega;
		orbit.eccentricity = exc;
		orbit.inclination = inc;
		orbit.meanAnomaly = mean_anomaly;
		orbit.meanLongitude = mean_anomaly + perih_lon;
		orbit.meanMotion = mean_motion;
		orbit.perihelionDistance = q;
		orbit.perihelionLongitude = perih_lon;
		orbit.referenceEquinox = jd;
		orbit.referenceTime = jd;
		if (a_inverse == 0.0)
			orbit.referenceTime = perihelion_time;
		orbit.semimajorAxis = a;

		return orbit;
	}
//
//	/**
//	 * Default path to MPC distant bodies file.
//	 */
//	public static final String PATH_TO_MPC_DISTANT_BODIES_FILE = FileIO.DATA_ORBITAL_ELEMENTS_DIRECTORY + "MPC_distant_bodies.txt";
//
//	/**
//	 * Default path to MPC bright bodies file.
//	 */
//	public static final String PATH_TO_MPC_BRIGHT_ASTEROIDS_FILE = FileIO.DATA_ORBITAL_ELEMENTS_DIRECTORY + "MPC_asteroids_bright.txt";
//
//	/**
//	 * Default path to SKYMAP distant bodies file.
//	 */
//	public static final String PATH_TO_SKYMAP_DISTANT_BODIES_FILE = FileIO.DATA_ORBITAL_ELEMENTS_DIRECTORY + "SKYMAP_distant_bodies.txt";
//
//	/**
//	 * Default path to MPC comets file.
//	 */
//	public static final String PATH_TO_MPC_COMETS_FILE = FileIO.DATA_ORBITAL_ELEMENTS_DIRECTORY + "MPC_comets.txt";
//
//	/**
//	 * Default path to SKYMAP comets file.
//	 */
//	public static final String PATH_TO_SKYMAP_COMETS_FILE = FileIO.DATA_ORBITAL_ELEMENTS_DIRECTORY + "SKYMAP_comets.txt";
//
	/**
//	 * For rade2Vector testing only.
//	 */
//	public static void main(String args[])
//	{
//		System.out.println("Orbit Test");
//
//			AstroDate astro = new AstroDate(2006, AstroDate.JANUARY, 1);
//			TimeElement time = new TimeElement(astro.toGCalendar(), TimeElement.UNIVERSAL_TIME_UT1);
//			CityElement city = City.findCity("Madrid");
//			ObserverElement observer = ObserverElement.parseCity(city);
//			EphemerisElement eph = new EphemerisElement(Target.MARS, EphemerisElement.EPHEM_APPARENT,
//					EphemerisElement.EQUINOX_OF_DATE, EphemerisElement.TOPOCENTRIC, EphemerisElement.APPLY_WILLIAMS,
//					EphemerisElement.FRAME_J2000);
//			eph.algorithm = EphemerisElement.ALGORITHM_VSOP87_ELP2000;
//
//			EphemElement ephem = Ephem.getEphemeris(time, observer, eph, true);
//			String name = Target.getName(eph.targetBody);
//			System.out.println("" + name + " RA: " + EphemUtils.formatRA(ephem.rightAscension));
//			System.out.println("" + name + " DEC: " + EphemUtils.formatDEC(ephem.declination));
//			System.out.println("" + name + " dist: " + ephem.distance);
//
//			// Calculate ephemeris
//			double JD = TimeScale.getJD(time, observer, eph, TimeScale.JD_TDB);
//			OrbitalElement orbit = getOrbitalElements(eph.targetBody, JD, EphemerisElement.ALGORITHM_VSOP87_ELP2000);
//			orbit.meanMotion = OrbitEphem.obtainMeanMotion(eph.targetBody, orbit.semimajorAxis);
//			eph.orbit = orbit;
//			ephem = orbitEphemeris(time, observer, eph);
//
//			System.out.println("ORBIT");
//
//			System.out.println("JD " + JD);
//			System.out.println("" + name + " RA: " + EphemUtils.formatRA(ephem.rightAscension));
//			System.out.println("" + name + " DEC: " + EphemUtils.formatDEC(ephem.declination));
//			System.out.println("" + name + " dist: " + ephem.distance);
//
//			// Calculate orbit of 2P Encke
//			ReadFile re = new ReadFile();
//			re.setFormat(ReadFile.FORMAT_SKYMAP);
//			re.setPath(OrbitEphem.PATH_TO_SKYMAP_COMETS_FILE);
//			re.readFileOfComets();
//			int index = re.searchByName("2P Encke");
//			observer = ObserverElement.parseObservatory(Observatory.findObservatorybyName("Yebes"));
//			OrbitalElement new_orbit = re.getOrbitalElement(index);
//
//			EphemerisElement another_eph = new EphemerisElement(Target.Comet, EphemerisElement.EPHEM_APPARENT,
//					EphemerisElement.EQUINOX_J2000, EphemerisElement.TOPOCENTRIC, EphemerisElement.APPLY_IAU2009,
//					EphemerisElement.FRAME_J2000, EphemerisElement.ALGORITHM_ORBIT, new_orbit);
//			AstroDate new_astro = new AstroDate(2451545.0);
//			TimeElement new_time = new TimeElement(new_astro.toGCalendar(), TimeElement.UNIVERSAL_TIME_UTC);
//			EphemElement another_ephem = Ephem.getEphemeris(new_time, observer, another_eph, true);
//			name = re.getObjectName(index);
//			JD = new_astro.jd();
//			System.out.println("JD " + JD + " / " + name);
//			ConsoleReport.fullEphemReportToConsole(another_ephem);
//			// Calculate orbit of Pluto
//			double pos[] = new double[]
//			{ -26.06710, -11.92126, 8.80594 };
//			double vel[] = new double[]
//			{ 0.001633041, -0.003103617, -0.000152622 };
//			double mass = 0.0;
//			AstroDate date = new AstroDate(1982, 1, 31);
//			double jd = date.jd();
//			OrbitalElement PlutoOrbit = OrbitEphem.obtainOrbitalElementsFromPositionAndVelocity(pos, vel, jd, mass);
//
//			System.out
//					.println("Osculating elemens for Pluto in 1982-1-31 obtained from position and velocity vectors:");
//			System.out.println("i = " + PlutoOrbit.inclination * EphemConstant.RAD_TO_DEG);
//			System.out.println("o = " + PlutoOrbit.ascendingNodeLongitude * EphemConstant.RAD_TO_DEG);
//			System.out.println("a = " + PlutoOrbit.semimajorAxis);
//			System.out.println("e = " + PlutoOrbit.eccentricity);
//			System.out.println("w = " + PlutoOrbit.argumentOfPerihelion * EphemConstant.RAD_TO_DEG);
//			System.out.println("M = " + PlutoOrbit.meanAnomaly * EphemConstant.RAD_TO_DEG);
//			System.out.println("n = " + PlutoOrbit.meanMotion * EphemConstant.RAD_TO_DEG);
//
//			PlutoOrbit = OrbitEphem.getOrbitalElements(Target.Pluto, jd, EphemerisElement.ALGORITHM_MOSHIER);
//
//			System.out.println("Same data obtained from Moshier ephemeris (DE404, i, o, and w refered to J2000):");
//			System.out.println("i = " + PlutoOrbit.inclination * EphemConstant.RAD_TO_DEG);
//			System.out.println("o = " + PlutoOrbit.ascendingNodeLongitude * EphemConstant.RAD_TO_DEG);
//			System.out.println("a = " + PlutoOrbit.semimajorAxis);
//			System.out.println("e = " + PlutoOrbit.eccentricity);
//			System.out.println("w = " + PlutoOrbit.argumentOfPerihelion * EphemConstant.RAD_TO_DEG);
//			System.out.println("M = " + PlutoOrbit.meanAnomaly * EphemConstant.RAD_TO_DEG);
//			System.out.println("n = " + PlutoOrbit.meanMotion * EphemConstant.RAD_TO_DEG);
//
//			System.out.println("Orbit determination example:");
//			AstroDate astros[] = new AstroDate[3];
//			TimeElement times[] = new TimeElement[3];
//			CityElement cities[] = new CityElement[3];
//			ObserverElement observers[] = new ObserverElement[3];
//			LocationElement locations[] = new LocationElement[3];
//			cities[0] = new CityElement("Tokyo", EphemUtils.parseDeclination(139, 32, 6.9), EphemUtils.parseDeclination(
//					35, 40, 23.6), 0, 58);
//			cities[1] = new CityElement("Olifantsfontein", EphemUtils.parseDeclination(28, 14, 51.1), Functions
//					.parseDeclination(-25, 57, 34.7), 0, 1544);
//			cities[2] = new CityElement("Olifantsfontein", EphemUtils.parseDeclination(28, 14, 51.1), Functions
//					.parseDeclination(-25, 57, 34.7), 0, 1544);
//			astros[0] = new AstroDate(1959, AstroDate.MARCH, 2, 18, 4, 57.67f);
//			astros[1] = new AstroDate(1959, AstroDate.MARCH, 2, 19, 19, 13.52f);
//			astros[2] = new AstroDate(1959, AstroDate.MARCH, 2, 19, 21, 8.99f);
//			double RA[] = new double[]
//			{ EphemUtils.parseRightAscension(14, 52, 31.01), EphemUtils.parseRightAscension(5, 47, 31.36),
//					EphemUtils.parseRightAscension(6, 35, 34.41) };
//			double DEC[] = new double[]
//			{ EphemUtils.parseDeclination(23, 59, 59.5), EphemUtils.parseDeclination(2, 8, 12.8),
//					EphemUtils.parseDeclination(7, 55, 30.0) };
//			for (int i = 0; i < 3; i++)
//			{
//				locations[i] = new LocationElement(RA[i], DEC[i], 1.0);
//				times[i] = new TimeElement(astros[i].toGCalendar(), TimeElement.UNIVERSAL_TIME_UT1);
//				observers[i] = ObserverElement.parseCity(cities[i]);
//			}
//			OrbitalElement myOrbit = OrbitEphem.solveOrbitByGaussMethod(locations, times, observers);
//			System.out.println("i = " + myOrbit.inclination * EphemConstant.RAD_TO_DEG);
//			System.out.println("o = " + myOrbit.ascendingNodeLongitude * EphemConstant.RAD_TO_DEG);
//			System.out.println("a = " + myOrbit.semimajorAxis);
//			System.out.println("e = " + myOrbit.eccentricity);
//			System.out.println("w = " + myOrbit.argumentOfPerihelion * EphemConstant.RAD_TO_DEG);
//			System.out.println("M = " + myOrbit.meanAnomaly * EphemConstant.RAD_TO_DEG);
//			System.out.println("n = " + myOrbit.meanMotion * EphemConstant.RAD_TO_DEG);
//
//	}
}
