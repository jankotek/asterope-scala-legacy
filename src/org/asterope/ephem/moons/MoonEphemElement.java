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

import org.asterope.ephem.EphemElement;

import java.io.Serializable;

/**
 * Convenient class to store results of ephemeris of natural satellites.
 * 
 * @see MoonEphem
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class MoonEphemElement implements Serializable
{

	private static final long serialVersionUID = -7854981589429669386L;

	/**
	 * Constructs a MoonEphem object giving all the data except angular
	 * radius, magnitude, ecliptic position, phase angle, and axis orientation.
	 * 
	 * @param nom Name of the satellite.
	 * @param ra Right ascension.
	 * @param dec Declination.
	 * @param dist Distance.
	 * @param dist_sun Distance from the Sun.
	 * @param azi Azimuth.
	 * @param ele Elevation.
	 * @param ill Illumination fraction.
	 * @param elo Elongation in radians.
	 * @param ecl True if the satellite is not illuminated.
	 * @param ocl True if the satellite is oculted.
	 * @param tran True is the satellite is transiting as seen by the
	 *        observer.
	 * @param shadow_transiting True if the shadow is transiting.
	 * @param inf True if z is lower than 0.0.
	 * @param x X position in units of equatorial radii respect to planet and
	 *        the sky plane.
	 * @param y Y position in units of equatorial radii respect to planet and
	 *        the sky plane.
	 * @param z Z position in units of equatorial radii respect to planet and
	 *        the sky plane.
	 * @param x_sun X position in units of equatorial radii respect to Sun and
	 *        the sky plane.
	 * @param y_sun Y position in units of equatorial radii respect to Sun and
	 *        the sky plane.
	 * @param z_sun Z position in units of equatorial radii respect to Sun and
	 *        the sky plane.
	 */
	public MoonEphemElement(String nom, double ra, double dec, double dist, double dist_sun, double azi, double ele,
			double ill, double elo, boolean ecl, boolean ocl, boolean tran, boolean shadow_transiting,
			boolean inf, double x, double y, double z, double x_sun, double y_sun, double z_sun)
	{
		name = nom;
		rightAscension = ra;
		declination = dec;
		distance = dist;
		azimuth = azi;
		elevation = ele;
		distanceFromSun = dist_sun;
		eclipsed = ecl;
		occulted = ocl;
		shadowTransiting = shadow_transiting;
		transiting = tran;
		inferior = inf;
		xPosition = x;
		yPosition = y;
		zPosition = z;
		xPositionFromSun = x_sun;
		yPositionFromSun = y_sun;
		zPositionFromSun = z_sun;
		phase = ill;
		elongation = elo;
		mutualPhenomena = "";
	}

	/**
	 * Constructor of an empty object.
	 */
	public MoonEphemElement()
	{
		name = "";
		rightAscension = 0.0;
		declination = 0.0;
		distance = 0.0;
		azimuth = 0.0;
		elevation = 0.0;
		eclipsed = false;
		occulted = false;
		transiting = false;
		inferior = false;
		xPosition = 0.0;
		yPosition = 0.0;
		zPosition = 0.0;
		xPositionFromSun = 0.0;
		yPositionFromSun = 0.0;
		zPositionFromSun = 0.0;
		phase = 0.0f;
		elongation = 0.0f;
		magnitude = 0.0f;
		angularRadius = 0.0f;
		heliocentricEclipticLongitude = 0.0;
		heliocentricEclipticLatitude = 0.0;
		phaseAngle = 0.0f;
		paralacticAngle = 0.0f;
		mutualPhenomena = "";
	}

	/**
	 * Name of the satellite.
	 */
	public String name;

	/**
	 * Right ascension in radians.
	 */
	public double rightAscension;

	/**
	 * Declination in radians.
	 */
	public double declination;

	/**
	 * Distance in AU.
	 */
	public double distance;

	/**
	 * Distance from the Sun in Astronomical Units.
	 */
	public double distanceFromSun;

	/**
	 * Angular radius in radians.
	 */
	public double angularRadius;

	/**
	 * Azimuth in radians.
	 */
	public double azimuth;

	/**
	 * Elevation in radians.
	 */
	public double elevation;

	/**
	 * Elongation in radians.
	 */
	public double elongation;

	/**
	 * Apparent magnitude.
	 */
	public double magnitude;

	/**
	 * Visible phase percentage.
	 */
	public double phase;

	/**
	 * True if the center of the satellite is not illuminated by the Sun.
	 */
	public boolean eclipsed;

	/**
	 * True if the center of the satellite is in front of the planet as seen
	 * from the Sun.
	 */
	public boolean shadowTransiting;

	/**
	 * True if the center of the satellite is behind the planet as seen by the
	 * observer.
	 */
	public boolean occulted;

	/**
	 * True if the center of the satellite is in front of the planet as seen by
	 * the observer.
	 */
	public boolean transiting;

	/**
	 * True if the satellite is behind the planet, with z position lower than
	 * 0.0.
	 */
	public boolean inferior;

	/**
	 * Holds apparent x position of the satellite respect to the axis of the
	 * mother planet and the sky plane, in equatorial radii units, as seen from
	 * the observer.
	 */
	public double xPosition;

	/**
	 * Holds apparent y position of the satellite respect to the axis of the
	 * mother planet and the sky plane, in equatorial radii units, as seen from
	 * the observer.
	 */
	public double yPosition;

	/**
	 * Holds apparent z position of the satellite respect to the axis of the
	 * mother planet and the sky plane, in equatorial radii units, as seen from
	 * the observer.
	 */
	public double zPosition;

	/**
	 * Holds apparent x position of the satellite respect to the axis of the
	 * mother planet and the sky plane, in equatorial radii units, as seen from
	 * the Sun.
	 */
	public double xPositionFromSun;

	/**
	 * Holds apparent y position of the satellite respect to the axis of the
	 * mother planet and the sky plane, in equatorial radii units, as seen from
	 * the Sun.
	 */
	public double yPositionFromSun;

	/**
	 * Holds apparent z position of the satellite respect to the axis of the
	 * mother planet and the sky plane, in equatorial radii units, as seen from
	 * the Sun.
	 */
	public double zPositionFromSun;

	/**
	 * Holds a description of the mutual phenomena in a given instant.
	 * Format is ECLIPSED/OCCULTED BY and the name of the object producing
	 * the event. The object can be another satellite or the mother planet,
	 * the last case only when the eclipse/occultation is currently partial.
	 */
	public String mutualPhenomena;

	/**
	 * Position angle of axis in radians.
	 */
	public double positionAngleOfAxis;

	/**
	 * Position angle of pole in radians.
	 */
	public double positionAngleOfPole;

	/**
	 * Longitude of central meridian in radians. For giant planets (Jupiter,
	 * Saturn, Uranus, and Neptune) the value will be refered to system III of
	 * coordinates.
	 */
	public double longitudeOfCentralMeridian;

	/**
	 * Bright limb angle in radians.
	 */
	public double brightLimbAngle;

	/**
	 * Subsolar latitude. In case of Saturn, Uranus, and Neptune, can be
	 * considered the ring plane ilumination angle. It depends on the relative
	 * position of the Sun.
	 */
	public double subsolarLatitude;

	/**
	 * Subsolar longitude. It depends on the relative position of the Sun. In
	 * giant planets the value is refered to System III of coordinates.
	 */
	public double subsolarLongitude;

	/**
	 * Right ascension of the north pole of rotation.
	 */
	public double northPoleRA;

	/**
	 * Declination of the north pole of rotation.
	 */
	public double northPoleDEC;

	/**
	 * Heliocentric ecliptic longitude in radians.
	 */
	public double heliocentricEclipticLongitude;

	/**
	 * Heliocentric ecliptic latitude in radians.
	 */
	public double heliocentricEclipticLatitude;

	/**
	 * Phase angle in radians.
	 */
	public double phaseAngle;

	/**
	 * Paralactic angle in radians.
	 */
	public double paralacticAngle;

	/**
	 * Transform the corresponding information in an Ephem object into a
	 * moon ephem object.
	 * 
	 * @param ephem Ephem object.
	 * @return Moon ephem object.
	 */
	public static MoonEphemElement parseEphemElement(EphemElement ephem)
	{
		MoonEphemElement moon_ephem = new MoonEphemElement("", ephem.rightAscension, ephem.declination,
				ephem.distance, ephem.distanceFromSun, ephem.azimuth, ephem.elevation, ephem.phase, ephem.elongation,
				false, false, false, false, false, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0);

		moon_ephem.magnitude = ephem.magnitude;
		moon_ephem.angularRadius = ephem.angularRadius;
		moon_ephem.positionAngleOfAxis = ephem.positionAngleOfAxis;
		moon_ephem.positionAngleOfPole = ephem.positionAngleOfPole;
		moon_ephem.brightLimbAngle = ephem.brightLimbAngle;
		moon_ephem.subsolarLatitude = ephem.subsolarLatitude;
		moon_ephem.subsolarLongitude = ephem.subsolarLongitude;
		moon_ephem.longitudeOfCentralMeridian = ephem.longitudeOfCentralMeridian;
		moon_ephem.northPoleDEC = ephem.northPoleDEC;
		moon_ephem.northPoleRA = ephem.northPoleRA;
		moon_ephem.heliocentricEclipticLatitude = ephem.heliocentricEclipticLatitude;
		moon_ephem.heliocentricEclipticLongitude = ephem.heliocentricEclipticLongitude;
		moon_ephem.phaseAngle = ephem.phaseAngle;
		moon_ephem.paralacticAngle = ephem.paralacticAngle;
		moon_ephem.mutualPhenomena = "";

		return moon_ephem;
	}

	@Override
	public String toString() {
		return "MoonEphemElement [angularRadius=" + angularRadius + ",\n azimuth=" + azimuth + ",\n brightLimbAngle=" + brightLimbAngle
				+ ",\n declination=" + declination + ",\n distance=" + distance + ",\n distanceFromSun=" + distanceFromSun + ",\n eclipsed="
				+ eclipsed + ",\n elevation=" + elevation + ",\n elongation=" + elongation + ",\n heliocentricEclipticLatitude="
				+ heliocentricEclipticLatitude + ",\n heliocentricEclipticLongitude=" + heliocentricEclipticLongitude + ",\n inferior="
				+ inferior + ",\n longitudeOfCentralMeridian=" + longitudeOfCentralMeridian + ",\n magnitude=" + magnitude
				+ ",\n mutualPhenomena=" + mutualPhenomena + ",\n name=" + name + ",\n northPoleDEC=" + northPoleDEC + ",\n northPoleRA="
				+ northPoleRA + ",\n occulted=" + occulted + ",\n paralacticAngle=" + paralacticAngle + ",\n phase=" + phase + ",\n phaseAngle="
				+ phaseAngle + ",\n positionAngleOfAxis=" + positionAngleOfAxis + ",\n positionAngleOfPole=" + positionAngleOfPole
				+ ",\n rightAscension=" + rightAscension + ",\n shadowTransiting=" + shadowTransiting + ",\n subsolarLatitude="
				+ subsolarLatitude + ",\n subsolarLongitude=" + subsolarLongitude + ",\n transiting=" + transiting + ",\n xPosition=" + xPosition
				+ ",\n xPositionFromSun=" + xPositionFromSun + ",\n yPosition=" + yPosition + ",\n yPositionFromSun=" + yPositionFromSun
				+ ",\n zPosition=" + zPosition + ",\n zPositionFromSun=" + zPositionFromSun + "]";
	}

	/**
	 * To clone the object.
	 */
	public Object clone()
	{
		MoonEphemElement moon = new MoonEphemElement();
		moon.rightAscension = this.rightAscension;
		moon.name = this.name;
		moon.declination = this.declination;
		moon.distance = this.distance;
		moon.azimuth = this.azimuth;
		moon.angularRadius = this.angularRadius;
		moon.brightLimbAngle = this.brightLimbAngle;
		moon.distanceFromSun = this.distanceFromSun;
		moon.eclipsed = this.eclipsed;
		moon.elevation = this.elevation;
		moon.elongation = this.elongation;
		moon.heliocentricEclipticLatitude = this.heliocentricEclipticLatitude;
		moon.heliocentricEclipticLongitude = this.heliocentricEclipticLongitude;
		moon.inferior = this.inferior;
		moon.longitudeOfCentralMeridian = this.longitudeOfCentralMeridian;
		moon.magnitude = this.magnitude;
		moon.northPoleDEC = this.northPoleDEC;
		moon.northPoleRA = this.northPoleRA;
		moon.occulted = this.occulted;
		moon.paralacticAngle = this.paralacticAngle;
		moon.phase = this.phase;
		moon.phaseAngle = this.phaseAngle;
		moon.mutualPhenomena = this.mutualPhenomena;
		moon.positionAngleOfAxis = this.positionAngleOfAxis;
		moon.positionAngleOfPole = this.positionAngleOfPole;
		moon.shadowTransiting = this.shadowTransiting;
		moon.subsolarLatitude = this.subsolarLatitude;
		moon.subsolarLongitude = this.subsolarLongitude;
		moon.transiting = this.transiting;
		moon.xPosition = this.xPosition;
		moon.xPositionFromSun = this.xPositionFromSun;
		moon.yPosition = this.yPosition;
		moon.yPositionFromSun = this.yPositionFromSun;
		moon.zPosition = this.zPosition;
		moon.zPositionFromSun = this.zPositionFromSun;
		return moon;
	}
	/**
	 * Return true if the object is equals to another moon
	 * ephem object.
	 */
	public boolean equals(Object m)
	{
		if (m == null) {
			return false;
		}
		boolean equals = true;
		MoonEphemElement moon = (MoonEphemElement) m;
		if (moon.rightAscension != this.rightAscension) equals = false;
		if (!moon.name.equals(this.name)) equals = false;
		if (moon.declination != this.declination) equals = false;
		if (moon.distance != this.distance) equals = false;
		if (moon.azimuth != this.azimuth) equals = false;
		if (moon.angularRadius != this.angularRadius) equals = false;
		if (moon.brightLimbAngle != this.brightLimbAngle) equals = false;
		if (moon.distanceFromSun != this.distanceFromSun) equals = false;
		if (moon.eclipsed != this.eclipsed) equals = false;
		if (moon.elevation != this.elevation) equals = false;
		if (moon.elongation != this.elongation) equals = false;
		if (moon.heliocentricEclipticLatitude != this.heliocentricEclipticLatitude) equals = false;
		if (moon.heliocentricEclipticLongitude != this.heliocentricEclipticLongitude) equals = false;
		if (moon.inferior != this.inferior) equals = false;
		if (moon.longitudeOfCentralMeridian != this.longitudeOfCentralMeridian) equals = false;
		if (moon.magnitude != this.magnitude) equals = false;
		if (moon.northPoleDEC != this.northPoleDEC) equals = false;
		if (moon.northPoleRA != this.northPoleRA) equals = false;
		if (moon.occulted != this.occulted) equals = false;
		if (moon.paralacticAngle != this.paralacticAngle) equals = false;
		if (moon.phase != this.phase) equals = false;
		if (moon.phaseAngle != this.phaseAngle) equals = false;
		if (moon.mutualPhenomena != this.mutualPhenomena) equals = false;
		if (moon.positionAngleOfAxis != this.positionAngleOfAxis) equals = false;
		if (moon.positionAngleOfPole != this.positionAngleOfPole) equals = false;
		if (moon.shadowTransiting != this.shadowTransiting) equals = false;
		if (moon.subsolarLatitude != this.subsolarLatitude) equals = false;
		if (moon.subsolarLongitude != this.subsolarLongitude) equals = false;
		if (moon.transiting != this.transiting) equals = false;
		if (moon.xPosition != this.xPosition) equals = false;
		if (moon.xPositionFromSun != this.xPositionFromSun) equals = false;
		if (moon.yPosition != this.yPosition) equals = false;
		if (moon.yPositionFromSun != this.yPositionFromSun) equals = false;
		if (moon.zPosition != this.zPosition) equals = false;
		if (moon.zPositionFromSun != this.zPositionFromSun) equals = false;

		return equals;
	}
	
//	public Vector3d toVector(){
//		return Vector3d.rade2Vector(Arc.normalizeRa(rightAscension), declination);
//	}
}
