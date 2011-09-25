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

import org.asterope.ephem.EphemConstant;
import org.asterope.ephem.Target;

import java.io.Serializable;

/**
 * An adequate class for storing orbital elements of irregular natural
 * satellites.
 * <P>
 * <I><B>Description</B></I>
 * <P>
 * This class is suitable for storing orbital elements of natural satellites.
 * Below there's a list of available fields to access the data.
 * <P>
 * 
 * @see MoonEphem
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class MoonOrbitalElement implements Serializable
{
	static final long serialVersionUID = 1L;

	/**
	 * Constructs an empty moon orbital object.
	 */
	public MoonOrbitalElement()
	{
		semimajorAxis = 0.0;
		meanLongitude = 0.0;
		eccentricity = 0.0;
		periapsisLongitude = 0.0;
		ascendingNodeLongitude = 0.0;
		inclination = 0.0;
		referenceTime = 0.0;
		meanAnomaly = 0.0;
		argumentOfPeriapsis = 0.0;
		meanMotion = 0.0;
		referenceEquinox = 0.0;
		beginOfApplicableTime = 0.0;
		endOfApplicableTime = 0.0;
		referencePlane = 0;
		LaplacePoleRA = 0.0;
		LaplacePoleDEC = 0.0;
		argumentOfPeriapsisPrecessionRate = 0.0;
		ascendingNodePrecessionRate = 0.0;
		referenceEphemeris = "";
	}

	/**
	 * Constructs a moon orbit object giving the values of the main
	 * fields. Argument of periapsis is set to periapsis longitude minus
	 * ascending node longitude. Mean anomaly is set to mean longitude minus
	 * periapsis longitude. Mean motion is set to Constant.GAUSS / (sma *
	 * Math.sqrt(sma), assuming a massless object in planetocentric orbit.
	 * <P>
	 * Is is necessary to set also the reference equinox to get correct
	 * ephemeris.
	 * 
	 * @param sma Semimajor axis in AU.
	 * @param mean_lon Mean Longitude in radians.
	 * @param ecc Eccentricity.
	 * @param peri_lon Periapsis longitude in radians.
	 * @param asc_node_lon Longitude of ascensing node in radians.
	 * @param incl Inclination in radians.
	 * @param ref_time Reference time (usually perihelion time) as a Julian day.
	 */
	public MoonOrbitalElement(double sma, double mean_lon, double ecc, double peri_lon, double asc_node_lon,
			double incl, double ref_time)
	{
		semimajorAxis = sma;
		meanLongitude = mean_lon;
		eccentricity = ecc;
		periapsisLongitude = peri_lon;
		ascendingNodeLongitude = asc_node_lon;
		inclination = incl;
		referenceTime = ref_time;
		argumentOfPeriapsis = peri_lon - asc_node_lon;
		meanAnomaly = mean_lon - peri_lon;
		meanMotion = EphemConstant.EARTH_MEAN_ORBIT_RATE / (sma * Math.sqrt(sma));
		referencePlane = 0;
		LaplacePoleRA = 0.0;
		LaplacePoleDEC = 0.0;
		argumentOfPeriapsisPrecessionRate = 0.0;
		ascendingNodePrecessionRate = 0.0;
		referenceEphemeris = "";
	}

	/**
	 * Constructs a moon orbit object giving the values of all the
	 * fields.
	 * 
	 * @param nom Name of the object
	 * @param sma Semimajor axis in AU.
	 * @param mean_anom Mean anomaly in radians.
	 * @param ecc Eccentricity.
	 * @param arg_peri Argument of periapsis in radians.
	 * @param asc_node_lon Longitude of ascensing node in radians.
	 * @param incl Inclination in radians.
	 * @param ref_time Reference time (usually perihelion time) as a Julian day.
	 * @param motion Mean motion in radians/day.
	 * @param equinox Reference equinox as a Julian day.
	 * @param init_time Begin of applicable time as a Julian day.
	 * @param final_time End of applicable time as a Julian day.
	 * @param ref_plane Reference plane for the elements.
	 * @param Laplace_RA Right ascension of the Laplace local plane.
	 * @param Laplace_DEC Declination of the Laplace local plane.
	 * @param prec_peri Precession of the periapsis, rad/day.
	 * @param prec_node Precession of the ascending node, rad/day.
	 * @param ref_ephem Ephemeris reference.
	 */
	public MoonOrbitalElement(String nom, double sma, double mean_anom, double ecc, double arg_peri,
			double asc_node_lon, double incl, double ref_time, double motion, double equinox, double init_time,
			double final_time, float mabs, float slope, int ref_plane, double Laplace_RA, double Laplace_DEC,
			double prec_peri, double prec_node, String ref_ephem)
	{
		semimajorAxis = sma;
		meanLongitude = mean_anom + arg_peri + asc_node_lon;
		eccentricity = ecc;
		periapsisLongitude = arg_peri + asc_node_lon;
		ascendingNodeLongitude = asc_node_lon;
		inclination = incl;
		referenceTime = ref_time;
		meanMotion = motion;
		referenceEquinox = equinox;
		beginOfApplicableTime = init_time;
		endOfApplicableTime = final_time;
		argumentOfPeriapsis = arg_peri;
		meanAnomaly = mean_anom;
		name = nom;
		absoluteMagnitude = mabs;
		magnitudeSlope = slope;
		referencePlane = ref_plane;
		LaplacePoleRA = Laplace_RA;
		LaplacePoleDEC = Laplace_DEC;
		argumentOfPeriapsisPrecessionRate = prec_peri;
		ascendingNodePrecessionRate = prec_node;
		referenceEphemeris = ref_ephem;
	}

	/**
	 * Semimajor axis of the orbit in AU.
	 */
	public double semimajorAxis;

	/**
	 * Mean longitude at reference time in radians.
	 */
	public double meanLongitude;

	/**
	 * Eccentricity.
	 */
	public double eccentricity;

	/**
	 * Perihelion longitude in radians.
	 */
	public double periapsisLongitude;

	/**
	 * Ascending node longitude in radians.
	 */
	public double ascendingNodeLongitude;

	/**
	 * Inclination of orbit in radians.
	 */
	public double inclination;

	/**
	 * Reference time in Julian Day. Usually perihelion time in comets.
	 */
	public double referenceTime;

	/**
	 * Mean anomaly = mean longitude - longitude of periapsis. Sometimes it is
	 * needed as a replacement to mean longitude. Radians.
	 */
	public double meanAnomaly;

	/**
	 * Argument of periapsis = longitude of periapsis - long. ascending node.
	 * Sometimes it is needed as a replacement to long. of perihelion. Radians.
	 */
	public double argumentOfPeriapsis;

	/**
	 * Mean motion in rad/day.
	 */
	public double meanMotion;

	/**
	 * Equinox of the orbital elements as a Julian day.
	 */
	public double referenceEquinox;

	/**
	 * Julian day of the beginning of the interval where these orbital elements
	 * are applicable. Currently used only for space probes.
	 */
	public double beginOfApplicableTime;

	/**
	 * Name of the object.
	 */
	public String name;

	/**
	 * Julian day of the ending of the interval where these orbital elements are
	 * applicable. Currently used only for space probes.
	 */
	public double endOfApplicableTime;

	/**
	 * Absolute magnitude.
	 */
	public float absoluteMagnitude;

	/**
	 * Magnitude slope for comets.
	 */
	public float magnitudeSlope;

	/**
	 * Perihelion distance in AU for comets.
	 */
	public double periapsisDistance;

	/**
	 * Central body ID.
	 */
	public Target centralBody;

	/**
	 * Reference plane for the elements.
	 */
	public int referencePlane;

	/**
	 * Right ascension of the local Laplace plane.
	 */
	public double LaplacePoleRA;

	/**
	 * Declination of the local Laplace plane.
	 */
	public double LaplacePoleDEC;

	/**
	 * Precession rate of the periapsis, rad/day.
	 */
	public double argumentOfPeriapsisPrecessionRate;

	/**
	 * Precession rate of the ascending node, rad/day.
	 */
	public double ascendingNodePrecessionRate;

	/**
	 * Reference ephemeris.
	 */
	public String referenceEphemeris;

	// Values for planes must be greater than/equal to 0

	/**
	 * Reference plane for Laplace.
	 */
	public static final int PLANE_LAPLACE = 0;

	/**
	 * Reference plane for ecliptic.
	 */
	public static final int PLANE_ECLIPTIC = 1;

	/**
	 * Reference plane for planet equator.
	 */
	public static final int PLANE_PLANET_EQUATOR = 2;

	/**
	 * Reference plane for celestial equator.
	 */
	public static final int PLANE_CELESTIAL_EQUATOR = 3;

	/**
	 * Moon orbit object representing an invalid orbital element set. Just an
	 * empty object.
	 */
	public static final MoonOrbitalElement INVALID_MOON_ORBIT = new MoonOrbitalElement();

	/**
	 * Checks if the object is invalid or not.
	 * 
	 * @return True if it is invalid, false otherwise.
	 */
	public boolean isInvalid()
	{
		boolean invalid = false;

		if (this.absoluteMagnitude == INVALID_MOON_ORBIT.absoluteMagnitude && 
				this.argumentOfPeriapsis == INVALID_MOON_ORBIT.argumentOfPeriapsis && 
				this.argumentOfPeriapsisPrecessionRate == INVALID_MOON_ORBIT.argumentOfPeriapsisPrecessionRate && 
				this.ascendingNodeLongitude == INVALID_MOON_ORBIT.ascendingNodeLongitude && 
				this.ascendingNodePrecessionRate == INVALID_MOON_ORBIT.ascendingNodePrecessionRate && 
				this.beginOfApplicableTime == INVALID_MOON_ORBIT.beginOfApplicableTime && 
				this.centralBody == INVALID_MOON_ORBIT.centralBody && 
				this.eccentricity == INVALID_MOON_ORBIT.eccentricity && 
				this.endOfApplicableTime == INVALID_MOON_ORBIT.endOfApplicableTime && 
				this.inclination == INVALID_MOON_ORBIT.inclination && 
				this.LaplacePoleDEC == INVALID_MOON_ORBIT.LaplacePoleDEC && 
				this.LaplacePoleRA == INVALID_MOON_ORBIT.LaplacePoleRA && 
				this.magnitudeSlope == INVALID_MOON_ORBIT.magnitudeSlope && 
				this.meanAnomaly == INVALID_MOON_ORBIT.meanAnomaly && 
				this.meanLongitude == INVALID_MOON_ORBIT.meanLongitude && 
				this.meanMotion == INVALID_MOON_ORBIT.meanMotion && 
				this.periapsisDistance == INVALID_MOON_ORBIT.periapsisDistance && 
				this.periapsisLongitude == INVALID_MOON_ORBIT.periapsisLongitude && 
				this.semimajorAxis == INVALID_MOON_ORBIT.semimajorAxis && 
				this.referenceEphemeris == INVALID_MOON_ORBIT.referenceEphemeris && 
				this.referenceEquinox == INVALID_MOON_ORBIT.referenceEquinox && 
				this.referencePlane == INVALID_MOON_ORBIT.referencePlane && 
				this.referenceTime == INVALID_MOON_ORBIT.referenceTime)
			invalid = true;

		return invalid;
	}

	/**
	 * To clone the object.
	 */
	public Object clone()
	{
		MoonOrbitalElement orbit = new MoonOrbitalElement(this.name, this.semimajorAxis, this.meanAnomaly,
				this.eccentricity, this.argumentOfPeriapsis, this.ascendingNodeLongitude, this.inclination,
				this.referenceTime, this.meanMotion, this.referenceEquinox, this.beginOfApplicableTime,
				this.endOfApplicableTime, this.absoluteMagnitude, this.magnitudeSlope, this.referencePlane,
				this.LaplacePoleRA, this.LaplacePoleDEC, this.argumentOfPeriapsisPrecessionRate,
				this.ascendingNodePrecessionRate, this.referenceEphemeris);

		orbit.centralBody = this.centralBody;
		orbit.periapsisDistance = this.periapsisDistance;
		orbit.periapsisLongitude = this.periapsisLongitude;
		return orbit;
	}
	/**
	 * Returns true if the input object is equals to this moon orbital
	 * element object..
	 */
	public boolean equals(Object o)
	{
		if (o == null) {
			return false;
		}

		boolean equals = true;
		MoonOrbitalElement orbit = (MoonOrbitalElement) o;
		if (orbit.name.equals(this.name)) equals = false;
		if (orbit.semimajorAxis != this.semimajorAxis) equals = false;
		if (orbit.meanAnomaly != this.meanAnomaly) equals = false;
		if (orbit.eccentricity != this.eccentricity) equals = false;
		if (orbit.argumentOfPeriapsis != this.argumentOfPeriapsis) equals = false;
		if (orbit.ascendingNodeLongitude != this.ascendingNodeLongitude) equals = false;
		if (orbit.inclination != this.inclination) equals = false;
		if (orbit.referenceTime != this.referenceTime) equals = false;
		if (orbit.meanMotion != this.meanMotion) equals = false;
		if (orbit.referenceEquinox != this.referenceEquinox) equals = false;
		if (orbit.beginOfApplicableTime != this.beginOfApplicableTime) equals = false;
		if (orbit.endOfApplicableTime != this.endOfApplicableTime) equals = false;
		if (orbit.absoluteMagnitude != this.absoluteMagnitude) equals = false;
		if (orbit.magnitudeSlope != this.magnitudeSlope) equals = false;
		if (orbit.LaplacePoleRA != this.LaplacePoleRA) equals = false;
		if (orbit.LaplacePoleDEC != this.LaplacePoleDEC) equals = false;
		if (orbit.argumentOfPeriapsisPrecessionRate != this.argumentOfPeriapsisPrecessionRate) equals = false;
		if (orbit.referencePlane != this.referencePlane) equals = false;
		if (orbit.ascendingNodePrecessionRate != this.ascendingNodePrecessionRate) equals = false;
		if (orbit.referenceEphemeris != this.referenceEphemeris) equals = false;
		if (orbit.centralBody != this.centralBody) equals = false;
		if (orbit.periapsisDistance != this.periapsisDistance) equals = false;
		if (orbit.periapsisLongitude != this.periapsisLongitude) equals = false;
		if (orbit.meanLongitude != this.meanLongitude) equals = false;
		return equals;
	}
}

// end of class OrbitalElement
