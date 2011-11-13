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

import org.asterope.ephem.moons.MoonOrbitalElement;

import java.io.Serializable;

/**
 * An adequate class for storing orbital elements.<P>
 * 
 * This class is suitable for storing orbital elements of planets, comets,
 * asteroids, and space probes. Below there's a list of available fields to 
 * access the data.
 * 
 * @see Ephem
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 * 
 */
public class OrbitalElement implements Serializable 
{

	private static final long serialVersionUID = -5379026363288021029L;

	/**
	 * Constructs an empty {@linkplain OrbitalElement} object.
	 */
	public OrbitalElement() { 
			semimajorAxis=0.0; meanLongitude=0.0; eccentricity=0.0; 
			perihelionLongitude=0.0; ascendingNodeLongitude=0.0; inclination=0.0;
		  referenceTime=0.0; meanAnomaly=0.0; argumentOfPerihelion=0.0; meanMotion=0.0;
		  referenceEquinox=0.0; beginOfApplicableTime=0.0; endOfApplicableTime=0.0;
		  absoluteMagnitude=0.0f;centralBody=null;magnitudeSlope=0.0f;perihelionDistance=0.0;}   

	/**
	 * Constructs an {@linkplain OrbitalElement} object giving the values of the main fields.
	 * Argument of perihelion is set to perihelion longitude minus ascending node
	 * longitude. Mean anomaly is set to mean longitude minus perihelion longitude.
	 * Mean motion is set to Constant.GAUSS / (sma * Math.sqrt(sma), assuming a
	 * massless object in heliocentric orbit.<P>
	 * 
	 * Is is necessary to set also the reference equinox to get correct ephemeris.
	 * 
	 * @param sma Semimajor axis in AU.
	 * @param mean_lon Mean Longitude in radians.
	 * @param ecc Eccentricity.
	 * @param perih_lon Perihelion longitude in radians.
	 * @param asc_node_lon Longitude of ascensing node in radians.
	 * @param incl Inclination in radians.
	 * @param ref_time Reference time (usually perihelion time) as a Julian day.
	 */
	public OrbitalElement( double sma, double mean_lon, 
		  double ecc,  double perih_lon, double asc_node_lon,
		  double incl, double ref_time) { 
		semimajorAxis=sma; meanLongitude=mean_lon; eccentricity=ecc; 
		perihelionLongitude=perih_lon;ascendingNodeLongitude=asc_node_lon; inclination=incl;
	  referenceTime=ref_time;argumentOfPerihelion=perih_lon-asc_node_lon;
	  meanAnomaly=mean_lon-perih_lon; 
	  meanMotion = EphemConstant.EARTH_MEAN_ORBIT_RATE / (sma * Math.sqrt(sma));
	  perihelionDistance = sma * (1.0 - ecc);
}

	/**
	 * Constructs an {@linkplain OrbitalElement} object giving the values of most of the fields.
	 * Argument of perihelion is set to perihelion longitude minus ascending node
	 * longitude. Mean anomaly is set to mean longitude minus perihelion longitude.
	 * 
	 * @param nom Name of the object
	 * @param sma Semimajor axis in AU.
	 * @param mean_lon Mean Longitude in radians.
	 * @param ecc Eccentricity.
	 * @param perih_lon Perihelion longitude in radians.
	 * @param asc_node_lon Longitude of ascensing node in radians.
	 * @param incl Inclination in radians.
	 * @param ref_time Reference time (usually perihelion time) as a Julian day.
	 * @param motion Mean motion in radians/day.
	 * @param equinox Reference equinox as a Julian day.
	 * @param init_time Begin of applicable time as a Julian day.
	 * @param final_time End of applicable time as a Julian day.
	 */
	public OrbitalElement( String nom, double sma, double mean_lon, 
		  double ecc,  double perih_lon, double asc_node_lon,
		  double incl, double ref_time, double motion, double equinox,
		  double init_time, double final_time, float mabs, float slope) { 
		semimajorAxis=sma; meanLongitude=mean_lon; eccentricity=ecc; 
		perihelionLongitude=perih_lon;ascendingNodeLongitude=asc_node_lon; inclination=incl;
	  referenceTime=ref_time;meanMotion=motion;referenceEquinox=equinox;
	  beginOfApplicableTime=init_time;endOfApplicableTime=final_time;
	  argumentOfPerihelion=perih_lon-asc_node_lon;meanAnomaly=mean_lon-perih_lon;
	  name=nom;absoluteMagnitude=mabs;magnitudeSlope=slope;
	  perihelionDistance = sma * (1.0 - ecc);
	  }

	/**
	 * Constructs an {@linkplain OrbitalElement} object giving the values of most of the fields, but
	 * in a different way sometimes used.
	 * 
	 * @param nom Name of the object
	 * @param sma Semimajor axis in AU.
	 * @param arg_perih Argument of perihelion.
	 * @param ecc Eccentricity.
	 * @param mean_anomaly Mean anomaly in radians.
	 * @param asc_node_lon Longitude of ascensing node in radians.
	 * @param incl Inclination in radians.
	 * @param ref_time Reference time (usually perihelion time) as a Julian day.
	 * @param motion Mean motion in radians/day.
	 * @param equinox Reference equinox as a Julian day.
	 * @param init_time Begin of applicable time as a Julian day.
	 * @param final_time End of applicable time as a Julian day.
	 */
	public OrbitalElement( String nom, double sma, double arg_perih, 
		  double ecc,  double mean_anomaly, double asc_node_lon,
		  double incl, double ref_time, double motion, double equinox,
		  double init_time, double final_time) { 
		semimajorAxis=sma; meanLongitude=mean_anomaly+arg_perih+asc_node_lon; eccentricity=ecc; 
		perihelionLongitude=arg_perih + asc_node_lon;ascendingNodeLongitude=asc_node_lon; inclination=incl;
	  referenceTime=ref_time;meanMotion=motion;referenceEquinox=equinox;
	  beginOfApplicableTime=init_time;endOfApplicableTime=final_time;
	  argumentOfPerihelion=arg_perih;meanAnomaly=mean_anomaly;
	  name=nom;absoluteMagnitude=0.0f;magnitudeSlope=0.0f;
	  perihelionDistance = sma * (1.0 - ecc);
	  }

	/**
	 * Constructs an {@linkplain OrbitalElement} object giving the values of all the fields.
	 * 
	 * @param nom Name of the object
	 * @param sma Semimajor axis in AU.
	 * @param arg_perih Argument of perihelion.
	 * @param ecc Eccentricity.
	 * @param mean_anomaly Mean anomaly in radians.
	 * @param asc_node_lon Longitude of ascensing node in radians.
	 * @param incl Inclination in radians.
	 * @param ref_time Reference time (usually perihelion time) as a Julian day.
	 * @param motion Mean motion in radians/day.
	 * @param equinox Reference equinox as a Julian day.
	 * @param init_time Begin of applicable time as a Julian day.
	 * @param final_time End of applicable time as a Julian day.
	 * @param mean_lon Mean longitude.
	 * @param perih_lon Perihelion longitude.
	 * @param perih_dist Perihelion distance.
	 * @param abs_mag Absolute magnitude.
	 * @param mag_slope Magnitude slope.
	 */
	public OrbitalElement( String nom, double sma, double arg_perih, 
		  double ecc,  double mean_anomaly, double asc_node_lon,
		  double incl, double ref_time, double motion, double equinox,
		  double init_time, double final_time, double mean_lon, double perih_lon,
		  double perih_dist, float abs_mag, float mag_slope) { 
		name=nom; semimajorAxis=sma; argumentOfPerihelion=arg_perih; eccentricity=ecc; 
		meanAnomaly=mean_anomaly; ascendingNodeLongitude=asc_node_lon; inclination=incl;
	  referenceTime=ref_time;meanMotion=motion;referenceEquinox=equinox;
	  beginOfApplicableTime=init_time;endOfApplicableTime=final_time;
	  meanLongitude=mean_lon; perihelionLongitude=perih_lon;
	  perihelionDistance = perih_dist;
	  absoluteMagnitude=abs_mag;magnitudeSlope=mag_slope;
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
  public double perihelionLongitude;
  
  /**
   * Ascending node longitude in radians.
   */
  public double ascendingNodeLongitude;

  /**
   * Inclination of orbit in radians.
   */
  public double inclination;

  /**
   * Reference time in Julian Day. Usually perihelion time in
   * comets.
   */
  public double referenceTime;

  /**
   * Mean anomaly = mean longitude - longitude of perihelion.
   * Sometimes it is needed as a replacement to mean longitude. Radians.
   */
  public double meanAnomaly;

  /**
   * Argument of perihelion = longitude of perihelion - long. ascending node.
   * Sometimes it is needed as a replacement to long. of perihelion. Radians.
   */
  public double argumentOfPerihelion;

  /**
   * Mean motion in rad/day.
   */
  public double meanMotion;

  /**
   * Equinox of the orbital elements as a Julian day.
   */
  public double referenceEquinox;

  /**
   * Julian day of the beginning of the interval where these orbital
   * elements are applicable. Currently used only for space probes.
   */
  public double beginOfApplicableTime;

  /**
   * Name of the object.
   */
  public String name;
  /**
   * Julian day of the ending of the interval where these orbital
   * elements are applicable. Currently used only for space probes.
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
  public double perihelionDistance;

  /**
   * Central body, only for orbital elements of minor satellites.
   */
  public Target centralBody;

    /**
	 * OrbitalElement representing an invalid orbital element set. Just an empty object.
	 */
	public static final OrbitalElement INVALID_ORBIT = new OrbitalElement();

	/**
	 * Transforms a {@linkplain MoonOrbitalElement} object into an {@linkplain OrbitalElement} object, correcting for 
	 * longitude and periapsis precession rates to certain Julian day for subsequent ephemeris
	 * calculation.
	 * 
	 * @param moon_orbit Input object.
	 * @param jd Julian day when the elements should be calculated, i.e. subsequent calculation time.
	 * @return Output object.
	 */
	public static OrbitalElement parseMoonOrbitalElement(MoonOrbitalElement moon_orbit, double jd)
	{
        double dt = jd - moon_orbit.referenceTime;

		OrbitalElement orbit = new OrbitalElement(moon_orbit.name, moon_orbit.semimajorAxis,
				moon_orbit.meanLongitude + dt * moon_orbit.meanMotion, moon_orbit.eccentricity, 
				moon_orbit.periapsisLongitude + dt * moon_orbit.argumentOfPeriapsisPrecessionRate,
				moon_orbit.ascendingNodeLongitude + dt * moon_orbit.ascendingNodePrecessionRate, 
				moon_orbit.inclination, jd,
				moon_orbit.meanMotion, moon_orbit.referenceEquinox, moon_orbit.beginOfApplicableTime,
				moon_orbit.endOfApplicableTime, moon_orbit.absoluteMagnitude, moon_orbit.magnitudeSlope);

		orbit.centralBody = moon_orbit.centralBody;
		
		return orbit;
	}
	
	/**
	 * Checks if the OrbitalElement is invalid or not.
	 * 
	 * @return True if it is invalid, false otherwise.
	 */
	public boolean isInvalid()
	{
		boolean invalid = false;
		
		if (
				this.absoluteMagnitude == INVALID_ORBIT.absoluteMagnitude &&
				this.argumentOfPerihelion == INVALID_ORBIT.argumentOfPerihelion &&
				this.ascendingNodeLongitude == INVALID_ORBIT.ascendingNodeLongitude &&
				this.beginOfApplicableTime == INVALID_ORBIT.beginOfApplicableTime &&
				this.centralBody == INVALID_ORBIT.centralBody &&
				this.eccentricity == INVALID_ORBIT.eccentricity &&
				this.endOfApplicableTime == INVALID_ORBIT.endOfApplicableTime &&
				this.inclination == INVALID_ORBIT.inclination &&
				this.magnitudeSlope == INVALID_ORBIT.magnitudeSlope &&
				this.meanAnomaly == INVALID_ORBIT.meanAnomaly &&
				this.meanLongitude == INVALID_ORBIT.meanLongitude &&
				this.meanMotion == INVALID_ORBIT.meanMotion &&
				this.perihelionDistance == INVALID_ORBIT.perihelionDistance &&
				this.perihelionLongitude == INVALID_ORBIT.perihelionLongitude &&
				this.semimajorAxis == INVALID_ORBIT.semimajorAxis &&
				this.referenceEquinox == INVALID_ORBIT.referenceEquinox &&
				this.centralBody == INVALID_ORBIT.centralBody &&
				this.referenceTime == INVALID_ORBIT.referenceTime) invalid = true;
		
		return invalid;
	}
	
	/**
	 * To clone the object.
	 */
	public Object clone()
	{
		if (this == null) return null;
		OrbitalElement orbit = new OrbitalElement(this.name, this.semimajorAxis, this.argumentOfPerihelion,
				this.eccentricity, this.meanAnomaly, this.ascendingNodeLongitude, this.inclination,
				this.referenceTime, this.meanMotion, this.referenceEquinox, this.beginOfApplicableTime,
				this.endOfApplicableTime, this.meanLongitude, this.perihelionLongitude,
				this.perihelionDistance, this.absoluteMagnitude, this.magnitudeSlope);
		orbit.centralBody = this.centralBody;
		return orbit;
	}
	/**
	 * Returns if a given object is equal to this orbital
	 * elements object.
	 */
	public boolean equals(Object o)
	{
		if (o == null) {
			return false;
		}
		boolean equals = true;
		OrbitalElement orbit = (OrbitalElement) o;
		if (!this.name.equals(orbit.name)) equals = false;
		if (this.semimajorAxis != orbit.semimajorAxis) equals = false;
		if (this.argumentOfPerihelion != orbit.argumentOfPerihelion) equals = false;
		if (this.inclination != orbit.inclination) equals = false;
		if (this.beginOfApplicableTime != orbit.beginOfApplicableTime) equals = false;
		if (this.perihelionLongitude != orbit.perihelionLongitude) equals = false;
		if (this.magnitudeSlope != orbit.magnitudeSlope) equals = false;
		if (this.ascendingNodeLongitude != orbit.ascendingNodeLongitude) equals = false;
		if (this.referenceEquinox != orbit.referenceEquinox) equals = false;
		if (this.meanLongitude != orbit.meanLongitude) equals = false;
		if (this.meanAnomaly != orbit.meanAnomaly) equals = false;
		if (this.meanMotion != orbit.meanMotion) equals = false;
		if (this.endOfApplicableTime != orbit.endOfApplicableTime) equals = false;
		if (this.perihelionDistance != orbit.perihelionDistance) equals = false;
		if (this.centralBody != orbit.centralBody) equals = false;
		if (this.eccentricity != orbit.eccentricity) equals = false;
		if (this.referenceTime != orbit.referenceTime) equals = false;
		if (this.absoluteMagnitude != orbit.absoluteMagnitude) equals = false;		
		return equals;
	}

	/**
	 * To obtain the maximum apparent magnitude of an asteroid.
	 * @return Maximum magnitude.
	 */
	public double getAsteroidMaximumMagnitude()
	{
		double phase_angle = 0.0, distance = this.semimajorAxis - 1.0, distanceFromSun = this.semimajorAxis;
		if (distance < 0.0) distance = -distance;
		double tmp = Math.tan(Math.abs(phase_angle) * 0.5);
		double magnitude = this.absoluteMagnitude + 5.0 * Math
				.log(distance * distanceFromSun) / Math.log(10.0) - 2.5 * (Math
				.log(1.0 - this.magnitudeSlope) / Math.log(10.0)) * Math.exp(-3.33 * Math.pow(tmp, 0.63)) + this.magnitudeSlope * Math
				.exp(-1.87 * Math.pow(tmp, 1.22));
		return magnitude;
	}

	/**
	 * To obtain the maximum apparent magnitude of a comet.
	 * @return Maximum magnitude.
	 */
	public double getCometMaximumMagnitude()
	{
		double distance = this.semimajorAxis - 1.0, distanceFromSun = this.semimajorAxis;
		if (distance < 0.0) distance = -distance;
		double magnitude = this.absoluteMagnitude + 5.0 * Math.log(distance) / Math.log(10.0) + this.magnitudeSlope * Math
				.log(distanceFromSun) / Math.log(10.0);
		return magnitude;
	}
}

// end of class OrbitalElement
