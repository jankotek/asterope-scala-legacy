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

import org.asterope.ephem.moons.MoonEphemElement;

import java.io.Serializable;


/**
 * Convenient class for ephem data access.
 * <P>
 * <I><B>Description</B></I>
 * <P>
 * This class provides access to the data contained in any ephem
 * object. Below there's a list of available fields to access the data.
 * <P>
 * 
 * @see Ephem
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class EphemElement implements Serializable
{


	private static final long serialVersionUID = -302570028852181353L;


	/**
	 * Constructs an ephem object providing the values of the fields.
	 * This sets the values of all the fields except light time, rise, set,
	 * transit, transit elevation.
	 * 
	 * @param ra Right Ascension in radians.
	 * @param dec Declination in radians.
	 * @param r Distance to the observer in AU.
	 * @param tam Angular radius in radians.
	 * @param mag Apparent magnitude.
	 * @param sun_r Distance to the sun.
	 * @param elong Elongation in radians, only for objects different to the
	 *        sun.
	 * @param fase Phase percentage, only for objects different to the sun.
	 * @param fase_ang Phase angle in radians, only for objects different to the
	 *        sun.
	 * @param p0 Position angle of axis in radians.
	 * @param b0 Position angle of pole in radians.
	 * @param l0 Longitude of central meridian in radians.
	 * @param subs_lon Subsolar longitude in radians.
	 * @param subs_lat Subsolar latitude in radians.
	 * @param l0_I Longitude of central meridian, system I.
	 * @param l0_II Longitude of central meridian, system II.
	 * @param l0_III Longitude of central meridian, system III.
	 * @param limb Bright limb angle in radians.
	 * @param paralactic Paralactic angle in radians.
	 * @param azi Azimuth in radians.
	 * @param altitude Altitude in radians.
	 * @param ecl_lon Heliocentric ecliptic longitude in radians.
	 * @param ecl_lat Heliocentric ecliptic latitude in radians.
	 * @param np_ra North pole right ascension in radians.
	 * @param np_dec North pole declination in radians.
	 * @param defect Defect of illumination in radians.
	 * @param bright Surface brightness in mag/arcsecond^2.
	 */
	public EphemElement(double ra, double dec, double r, double tam, double mag, double sun_r, double elong,
			double fase, double fase_ang, double p0, double b0, double l0, double subs_lon, double subs_lat,
			double l0_I, double l0_II, double l0_III, double limb, double paralactic, double azi, double altitude,
			double ecl_lon, double ecl_lat, double np_ra, double np_dec, double defect, double bright)
	{
		rightAscension = ra;
		declination = dec;
		distance = r;
		angularRadius = tam;
		magnitude = mag;
		distanceFromSun = sun_r;
		elongation = elong;
		phase = fase;
		phaseAngle = fase_ang;
		positionAngleOfAxis = p0;
		positionAngleOfPole = b0;
		longitudeOfCentralMeridian = l0;
		subsolarLongitude = subs_lon;
		subsolarLatitude = subs_lat;
		longitudeOfCentralMeridianSystemI = l0_I;
		longitudeOfCentralMeridianSystemII = l0_II;
		longitudeOfCentralMeridianSystemIII = l0_III;
		brightLimbAngle = limb;
		paralacticAngle = paralactic;
		azimuth = azi;
		elevation = altitude;
		heliocentricEclipticLongitude = ecl_lon;
		heliocentricEclipticLatitude = ecl_lat;
		northPoleRA = np_ra;
		northPoleDEC = np_dec;
		defectOfIllumination = defect;
		surfaceBrightness = bright;
		setLocation(new LocationElement(ra, dec, r));
	}

	/**
	 * Constructs an ephem object providing the values of all fields.
	 * 
	 * @param ra Right Ascension in radians.
	 * @param dec Declination in radians.
	 * @param r Distance to the observer in AU.
	 * @param tam Angular radius in radians.
	 * @param mag Apparent magnitude.
	 * @param sun_r Distance to the sun.
	 * @param elong Elongation in radians, only for objects different to the
	 *        sun.
	 * @param fase Phase percentage, only for objects different to the sun.
	 * @param fase_ang Phase angle in radians, only for objects different to the
	 *        sun.
	 * @param p0 Position angle of axis in radians.
	 * @param b0 Position angle of pole in radians.
	 * @param l0 Longitude of central meridian in radians.
	 * @param subs_lon Subsolar longitude in radians.
	 * @param subs_lat Subsolar latitude in radians.
	 * @param l0_I Longitude of central meridian, system I.
	 * @param l0_II Longitude of central meridian, system II.
	 * @param l0_III Longitude of central meridian, system III.
	 * @param limb Bright limb angle in radians.
	 * @param paralactic Paralactic angle in radians.
	 * @param azi Azimuth in radians.
	 * @param altitude Altitude in radians.
	 * @param ecl_lon Heliocentric ecliptic longitude in radians.
	 * @param ecl_lat Heliocentric ecliptic latitude in radians.
	 * @param np_ra North pole right ascension in radians.
	 * @param np_dec North pole declination in radians.
	 * @param defect Defect of illumination in radians.
	 * @param bright Surface brightness in mag/arcsecond^2.
	 * @param lt Light-time.
	 * @param trise Rise date.
	 * @param tset Set date.
	 * @param ttransit Transit date.
	 * @param tr_elev Transit elevation.
	 */
	public EphemElement(double ra, double dec, double r, double tam, double mag, double sun_r, double elong,
			double fase, double fase_ang, double p0, double b0, double l0, double subs_lon, double subs_lat,
			double l0_I, double l0_II, double l0_III, double limb, double paralactic, double azi, double altitude,
			double ecl_lon, double ecl_lat, double np_ra, double np_dec, double defect, double bright, double lt,
			double trise, double tset, double ttransit, double tr_elev)
	{
		rightAscension = ra;
		declination = dec;
		distance = r;
		angularRadius = tam;
		magnitude = mag;
		distanceFromSun = sun_r;
		elongation = elong;
		phase = fase;
		phaseAngle = fase_ang;
		positionAngleOfAxis = p0;
		positionAngleOfPole = b0;
		longitudeOfCentralMeridian = l0;
		subsolarLongitude = subs_lon;
		subsolarLatitude = subs_lat;
		longitudeOfCentralMeridianSystemI = l0_I;
		longitudeOfCentralMeridianSystemII = l0_II;
		longitudeOfCentralMeridianSystemIII = l0_III;
		brightLimbAngle = limb;
		paralacticAngle = paralactic;
		azimuth = azi;
		elevation = altitude;
		heliocentricEclipticLongitude = ecl_lon;
		heliocentricEclipticLatitude = ecl_lat;
		northPoleRA = np_ra;
		northPoleDEC = np_dec;
		defectOfIllumination = defect;
		surfaceBrightness = bright;
		lightTime = lt;
		rise = trise;
		set = tset;
		transit = ttransit;
		transitElevation = tr_elev;
		setLocation(new LocationElement(ra, dec, r));
	}

	/**
	 * Constructs an empty ephem object.
	 */
	public EphemElement()
	{
		rightAscension = 0.0;
		declination = 0.0;
		distance = 0.0;
		angularRadius = 0.0f;
		magnitude = 0.0f;
		distanceFromSun = 0.0;
		elongation = 0.0f;
		phaseAngle = 0.0f;
		phase = 0.0f;
		positionAngleOfAxis = 0.0f;
		positionAngleOfPole = 0.0f;
		longitudeOfCentralMeridian = 0.0f;
		longitudeOfCentralMeridianSystemI = 0.0f;
		longitudeOfCentralMeridianSystemII = 0.0f;
		longitudeOfCentralMeridianSystemIII = 0.0f;
		brightLimbAngle = 0.0f;
		subsolarLatitude = 0.0f;
		subsolarLongitude = 0.0f;
		paralacticAngle = 0.0f;
		elevation = 0.0;
		azimuth = 0.0;
		heliocentricEclipticLongitude = 0.0;
		heliocentricEclipticLatitude = 0.0;
		northPoleRA = 0.0f;
		northPoleDEC = 0.0f;
		defectOfIllumination = 0.0f;
		surfaceBrightness = 0.0f;
		lightTime = 0.0f;
	}

	/**
	 * Right Ascension in radians.
	 */
	public double rightAscension;

	/**
	 * Declination in radians.
	 */
	public double declination;

	/**
	 * Distance to the observer in Astronomical Units.
	 */
	public double distance;

	/**
	 * Angular radius in radians.
	 */
	public double angularRadius;

	/**
	 * Apparent magnitude.
	 */
	public double magnitude;

	/**
	 * Distance from the Sun in Astronomical Units.
	 */
	public double distanceFromSun;

	/**
	 * Elongation in radians.
	 */
	public double elongation;

	/**
	 * Phase angle in radians. It can be negative if the ecliptic longitude
	 * of the Earth is below that of the planet (before opposition in 
	 * outer planets).
	 */
	public double phaseAngle;

	/**
	 * Visible phase percentage, from 0 to 1.
	 */
	public double phase;

	/**
	 * Position angle of axis in radians. 0 is towards north, 90 degrees towars
	 * East.
	 */
	public double positionAngleOfAxis;

	/**
	 * Position angle of pole in radians. Positive values means that the north
	 * hemisphere of the planet is towards the observer.
	 */
	public double positionAngleOfPole;

	/**
	 * Longitude of central meridian in radians. For giant planets (Jupiter,
	 * Saturn, Uranus, and Neptune) the value will be refered to system III of
	 * coordinates.
	 */
	public double longitudeOfCentralMeridian;

	/**
	 * Longitude of central meridian in radians for system I of coordinates
	 * (mean rotation of equatorial belt). Only available in Jupiter and Saturn.
	 */
	public double longitudeOfCentralMeridianSystemI;

	/**
	 * Longitude of central meridian in radians for system II of coordinates
	 * (mean rotation of the tropical belt). Only available in Jupiter,
	 * otherwise NULL.
	 */
	public double longitudeOfCentralMeridianSystemII;

	/**
	 * Longitude of central meridian in radians for system III of coordinates
	 * (rotation of magnetic field). Only available in giant planets (Jupiter,
	 * Saturn, Uranus, and Neptune).
	 */
	public double longitudeOfCentralMeridianSystemIII;

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
	 * Paralactic angle in radians.
	 */
	public double paralacticAngle;

	/**
	 * Azimuth in radians.
	 */
	public double azimuth;

	/**
	 * Geometric/apparent elevation in radians.
	 */
	public double elevation;

	/**
	 * Heliocentric ecliptic longitude in radians.
	 */
	public double heliocentricEclipticLongitude;

	/**
	 * Heliocentric ecliptic latitude in radians.
	 */
	public double heliocentricEclipticLatitude;

	/**
	 * Right ascension of the north pole of rotation.
	 */
	public double northPoleRA;

	/**
	 * Declination of the north pole of rotation.
	 */
	public double northPoleDEC;

	/**
	 * Defect of ilumination in radians.
	 */
	public double defectOfIllumination;

	/**
	 * Rise time as a Julian day in local time. If the object is above the
	 * horizon, then the value will be refered to the current day, otherwise it
	 * will be the next rise event in time.
	 */
	public double rise;

	/**
	 * Set time as aJjulian day in local time. If the object is above the
	 * horizon, then the value will be refered to the current day, otherwise it
	 * will be the next set event in time.
	 */
	public double set;

	/**
	 * Transit (maximum elevation) time as a Julian day in local time. If the
	 * object is above the horizon, then the value will be refered to the
	 * current day, otherwise it will be the next transit event in time.
	 */
	public double transit;

	/**
	 * Transit geometric elevation from horizon in radians.
	 */
	public double transitElevation;

	/**
	 * Surface brightness if mag/arcsecond^2, or 0 if the object is a point
	 * source or has no clear angular size.
	 */
	public double surfaceBrightness;

	/**
	 * Holds light time from observer to object in days.
	 */
	public double lightTime;
	/**
	 * Name of the body.
	 */
	public String name;


	/**
	 * To clone the object.
	 */
	public Object clone()
	{
		EphemElement ephem = new EphemElement(this.rightAscension, this.declination, this.distance,
				this.angularRadius, this.magnitude, this.distanceFromSun, this.elongation, this.phase,
				this.phaseAngle, this.positionAngleOfAxis, this.positionAngleOfPole,
				this.longitudeOfCentralMeridian, this.subsolarLongitude, this.subsolarLatitude,
				this.longitudeOfCentralMeridianSystemI, this.longitudeOfCentralMeridianSystemII,
				this.longitudeOfCentralMeridianSystemIII, this.brightLimbAngle, this.paralacticAngle,
				this.azimuth, this.elevation, this.heliocentricEclipticLongitude, this.heliocentricEclipticLatitude,
				this.northPoleRA, this.northPoleDEC, this.defectOfIllumination, this.surfaceBrightness);
		ephem.rise = this.rise;
		ephem.set = this.set;
		ephem.transit = this.transit;
		ephem.transitElevation = this.transitElevation;
		ephem.lightTime = this.lightTime;
		ephem.name = this.name;
		return ephem;
	}	
	/**
	 * Returns if the given object is equal to this ephemeris object.
	 */
	public boolean equals(Object e)
	{
		if (e == null) {

			return false;
		}
		boolean equals = true;
		EphemElement ephem  =(EphemElement) e;
		if (this.rightAscension != ephem.rightAscension) equals = false;
		if (this.declination != ephem.declination) equals = false;
		if (this.distance != ephem.distance) equals = false;
		if (this.phase != ephem.phase) equals = false;
		if (this.angularRadius != ephem.angularRadius) equals = false;
		if (this.magnitude != ephem.magnitude) equals = false;
		if (this.distanceFromSun != ephem.distanceFromSun) equals = false;
		if (this.elongation != ephem.elongation) equals = false;
		if (this.phaseAngle != ephem.phaseAngle) equals = false;
		if (this.positionAngleOfAxis != ephem.positionAngleOfAxis) equals = false;
		if (this.positionAngleOfPole != ephem.positionAngleOfPole) equals = false;
		if (this.longitudeOfCentralMeridian != ephem.longitudeOfCentralMeridian) equals = false;
		if (this.subsolarLongitude != ephem.subsolarLongitude) equals = false;
		if (this.subsolarLatitude != ephem.subsolarLatitude) equals = false;
		if (this.longitudeOfCentralMeridianSystemI != ephem.longitudeOfCentralMeridianSystemI) equals = false;
		if (this.longitudeOfCentralMeridianSystemII != ephem.longitudeOfCentralMeridianSystemII) equals = false;
		if (this.longitudeOfCentralMeridianSystemIII != ephem.longitudeOfCentralMeridianSystemIII) equals = false;
		if (this.brightLimbAngle != ephem.brightLimbAngle) equals = false;
		if (this.paralacticAngle != ephem.paralacticAngle) equals = false;
		if (this.heliocentricEclipticLatitude != ephem.heliocentricEclipticLatitude) equals = false;
		if (this.surfaceBrightness != ephem.surfaceBrightness) equals = false;
		if (this.defectOfIllumination != ephem.defectOfIllumination) equals = false;
		if (this.heliocentricEclipticLongitude != ephem.heliocentricEclipticLongitude) equals = false;
		if (this.azimuth != ephem.azimuth) equals = false;
		if (this.elevation != ephem.elevation) equals = false;
		if (this.northPoleRA != ephem.northPoleRA) equals = false;
		if (this.northPoleDEC != ephem.northPoleDEC) equals = false;
		if (this.rise != ephem.rise) equals = false;
		if (this.set != ephem.set) equals = false;
		if (this.transit != ephem.transit) equals = false;
		if (this.transitElevation != ephem.transitElevation) equals = false;
		if (this.lightTime != ephem.lightTime) equals = false;
		if (!this.name.equals(ephem.name)) equals = false;
		return equals;
	}
	
	private LocationElement location;
	/**
	 * Sets the location of this body in a custom coordinate system.
	 * @param loc Object location.
	 */
	public void setLocation(LocationElement loc) {
		if (loc == null) {
			location = null;
		} else {
			this.location = (LocationElement) loc.clone();
		}
	}
	/**
	 * Retrieves the location of this object.
	 * @return Location object.
	 */
	public LocationElement getLocation() {
		return this.location;
	}
	
//	public Vector3d getPosition(){
//		return Vector3d.rade2Vector(Arc.normalizeRa(rightAscension), declination);
//	}

	/**
	 * Transform the corresponding information in an {@linkplain MoonEphemElement} object
	 * into a ephem object.
	 * 
	 * @param moon_ephem {@linkplain MoonEphemElement} object.
	 * @param jdEpoch Epoch as Julian day when the ephemeris were obtained.
	 * @return Ephem object.
	 */
	public static EphemElement parseMoonEphemElement(MoonEphemElement moon_ephem, double jdEpoch){
		// Fields missing: central meridian in systems I II III, defect of
		// illumination, and surface brightness
		EphemElement ephem = new EphemElement(moon_ephem.rightAscension, moon_ephem.declination, moon_ephem.distance,
				moon_ephem.angularRadius, moon_ephem.magnitude, moon_ephem.distanceFromSun, moon_ephem.elongation,
				moon_ephem.phase, moon_ephem.phaseAngle, moon_ephem.positionAngleOfAxis,
				moon_ephem.positionAngleOfPole, moon_ephem.longitudeOfCentralMeridian,
				moon_ephem.subsolarLongitude, moon_ephem.subsolarLatitude, 0.0f, 0.0f, 0.0f,
				moon_ephem.brightLimbAngle, moon_ephem.paralacticAngle, moon_ephem.azimuth, moon_ephem.elevation,
				moon_ephem.heliocentricEclipticLongitude, moon_ephem.heliocentricEclipticLatitude, moon_ephem.northPoleRA,
				moon_ephem.northPoleDEC, 0.0f, 0.0f);

		ephem.lightTime = (double) (ephem.distance * EphemConstant.LIGHT_TIME_DAYS_PER_AU);
		ephem.surfaceBrightness = (double) Star.getSurfaceBrightness(ephem.magnitude, ephem.angularRadius * EphemConstant.RAD_TO_ARCSEC);
		ephem.defectOfIllumination = (double) ((1.0 - ephem.phase) * ephem.angularRadius);
		ephem.name = moon_ephem.name;
		return ephem;
	}
	
	/**
	 * Transform the corresponding information in an {@linkplain StarEphemElement} object
	 * into a ephem object.
	 * 
	 * @param star_ephem {@linkplain StarEphemElement} object.
	 * @return Ephem object.
	 */
	public static EphemElement parseStarEphemElement(StarEphemElement star_ephem)
	{
		EphemElement ephem = new EphemElement(star_ephem.rightAscension, star_ephem.declination, 
				star_ephem.distance * EphemConstant.PARSEC / (EphemConstant.AU * 1000.0),
				0.0f, star_ephem.magnitude, star_ephem.distance * EphemConstant.PARSEC / (EphemConstant.AU * 1000.0), 
				0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
				0.0f, star_ephem.paralacticAngle, star_ephem.azimuth, star_ephem.elevation, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f,
				0.0f);

		ephem.lightTime = (double) (ephem.distance * EphemConstant.LIGHT_TIME_DAYS_PER_AU);
		ephem.rise = star_ephem.rise;
		ephem.set = star_ephem.set;
		ephem.transit = star_ephem.transit;
		ephem.transitElevation = star_ephem.transitElevation;
		ephem.name = star_ephem.name;
		
		return ephem;
	}
	
	@Override
	public String toString() {
		return "EphemElement [angularRadius=" + angularRadius + ",\n azimuth=" + azimuth + ",\n brightLimbAngle=" + brightLimbAngle
				+ ",\n declination=" + declination + ",\n defectOfIllumination=" + defectOfIllumination
				+ ",\n distance=" + distance + ",\n distanceFromSun=" + distanceFromSun + ",\n elevation=" + elevation + ",\n elongation="
				+ elongation + ",\n heliocentricEclipticLatitude=" + heliocentricEclipticLatitude + ",\n heliocentricEclipticLongitude="
				+ heliocentricEclipticLongitude + ",\n lightTime=" + lightTime + ",\n location=" + location + ",\n longitudeOfCentralMeridian="
				+ longitudeOfCentralMeridian + ",\n longitudeOfCentralMeridianSystemI=" + longitudeOfCentralMeridianSystemI
				+ ",\n longitudeOfCentralMeridianSystemII=" + longitudeOfCentralMeridianSystemII + ",\n longitudeOfCentralMeridianSystemIII="
				+ longitudeOfCentralMeridianSystemIII + ",\n magnitude=" + magnitude + ",\n name=" + name + ",\n northPoleDEC=" + northPoleDEC
				+ ",\n northPoleRA=" + northPoleRA + ",\n paralacticAngle=" + paralacticAngle + ",\n phase=" + phase + ",\n phaseAngle="
				+ phaseAngle + ",\n positionAngleOfAxis=" + positionAngleOfAxis + ",\n positionAngleOfPole=" + positionAngleOfPole
				+ ",\n rightAscension=" + rightAscension + ",\n rise=" + rise + ",\n set=" + set + ",\n subsolarLatitude=" + subsolarLatitude
				+ ",\n subsolarLongitude=" + subsolarLongitude + ",\n surfaceBrightness=" + surfaceBrightness + ",\n transit=" + transit
				+ ",\n transitElevation=" + transitElevation + "]";
	}
}

// end of class EphemElement
