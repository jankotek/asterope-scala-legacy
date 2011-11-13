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

import java.io.Serializable;

/**
 * Convenient class for star ephem data access.
 * <P>
 * This class provides access to the data contained in any star ephem
 * object. Below there's a list of available fields 
 * to access the data.
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class StarEphemElement implements Serializable
{
	static final long serialVersionUID = 1L;

	/**
	 * Constructs an star ephem object providing the values of the fields.
	 * This sets the values of all the fields except light rise, set, transit,
	 * transit elevation.
	 * 
	 * @param ra Right Ascension in radians.
	 * @param dec Declination in radians.
	 * @param r Distance to the observer in AU.
	 * @param mag Apparent magnitude.
	 * @param paralactic Paralactic angle in radians.
	 * @param azi Azimuth in radians.
	 * @param altitude Elevation in radians.
	 */
	public StarEphemElement(double ra, double dec, double r, double mag, double paralactic, double azi, double altitude)
	{
		rightAscension = ra;
		declination = dec;
		distance = r;
		magnitude = mag;
		paralacticAngle = paralactic;
		elevation = altitude;
		azimuth = azi;
	}

	/**
	 * Constructs an empty star ephem object.
	 */
	public StarEphemElement()
	{
		rightAscension = 0.0;
		declination = 0.0;
		distance = 0.0;
		magnitude = 0.0f;
		paralacticAngle = 0.0f;
		elevation = 0.0;
		azimuth = 0.0;
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
	 * Distance to the observer in pc.
	 */
	public double distance;

	/**
	 * Apparent magnitude.
	 */
	public double magnitude;

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
	 * Rise time as a julian day in local time. If the object is above the
	 * horizon, then the value will be refered to the current day, otherwise it
	 * will be the next rise event in time.
	 */
	public double rise;

	/**
	 * Set time as a julian day in local time. If the object is above the
	 * horizon, then the value will be refered to the current day, otherwise it
	 * will be the next set event in time.
	 */
	public double set;

	/**
	 * Transit time as a julian day in local time. If the object is above the
	 * horizon, then the value will be refered to the current day, otherwise it
	 * will be the next transit event in time.
	 */
	public double transit;

	/**
	 * Transit geometrical elevation from horizon in radians.
	 */
	public double transitElevation;

	/**
	 * Name of the star.
	 */
	public String name;
	
	/**
	 * To clone the object.
	 */
	public Object clone()
	{
		StarEphemElement s = new StarEphemElement();
		s.azimuth = this.azimuth;
		s.declination = this.declination;
		s.distance = this.distance;
		s.elevation = this.elevation;
		s.magnitude = this.magnitude;
		s.paralacticAngle = this.paralacticAngle;
		s.rightAscension = this.rightAscension;
		s.rise = this.rise;
		s.set = this.set;
		s.transit = this.transit;
		s.transitElevation = this.transitElevation;
		s.name = this.name;

		return s;
	}
	/**
	 * Returns true if the input object is equals to this
	 * instance.
	 */
	public boolean equals(Object s)
	{
		if (s == null) {
			return false;
		}
		boolean equals = true;
		StarEphemElement see = (StarEphemElement) s;
		if (see.azimuth != this.azimuth) equals = false;
		if (!see.name.equals(this.name)) equals = false;
		if (see.declination != this.declination) equals = false;
		if (see.distance != this.distance) equals = false;
		if (see.elevation != this.elevation) equals = false;
		if (see.magnitude != this.magnitude) equals = false;
		if (see.paralacticAngle != this.paralacticAngle) equals = false;
		if (see.rightAscension != this.rightAscension) equals = false;
		if (see.rise != this.rise) equals = false;
		if (see.set != this.set) equals = false;
		if (see.transit != this.transit) equals = false;
		if (see.transitElevation != this.transitElevation) equals = false;
		return equals;
	}
}
