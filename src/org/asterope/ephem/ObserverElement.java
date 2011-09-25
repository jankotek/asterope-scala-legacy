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
 * An adequate class for storing the position of an observer as a previous step
 * to calculate ephemeris.
 * <P>
 * <I><B>Description</B></I>
 * <P>
 * This class is suitable for storing data from the city and observatory
 * classes. Below there's a list of available fields to access the data, and
 * some methods to parse a {@linkplain CityElement} or an
 * {@linkplain ObservatoryElement} object. It is possible to store atmosferic
 * conditions as pressure and temperature, values that will be used when
 * obtaining apparent altitude of an object.
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class ObserverElement implements Serializable {

	private static final long serialVersionUID = 2368320752894147745L;
	
	public static final ObserverElement MADRID = new ObserverElement("Madrid",
			-3.7100 * EphemConstant.DEG_TO_RAD, 40.420 * EphemConstant.DEG_TO_RAD , 
			693, 1);
	static{
		MADRID.dstCode = "N1"; 
	}

	/**
	 * Constructs an empty observer. All values to 0.0 or empty strings.
	 */
	public ObserverElement() {
		name = "";
		longitude = 0.0;
		latitude = 0.0;
		height = 0;
		timeZone = 0.0;
		pressure = 0.0;
		temperature = 0.0;
		humidity = 0.0;
		isAnObservatory = false;
		dstCode = "";
	}

	/**
	 * Constructs a observer by giving the values of the fields. Pressure and
	 * temperature are set to default values.
	 * 
	 * @param loc_name
	 *            Name of the city/observatory, or otherwise the user location.
	 * @param loc_lon
	 *            Longitude in radians. West negative.
	 * @param loc_lat
	 *            Latitude in radians.
	 * @param loc_alt
	 *            Altitude in meters.
	 * @param tz
	 *            Time zone in hours.
	 */
	public ObserverElement(String loc_name, double loc_lon, double loc_lat,
			int loc_alt, double tz) {
		name = loc_name;
		longitude = loc_lon;
		latitude = loc_lat;
		height = loc_alt;
		timeZone = tz;
		pressure = DEFAULT_PRESSURE;
		temperature = DEFAULT_TEMPERATURE;
		humidity = DEFAULT_HUMIDITY;
		isAnObservatory = false;

	}

	/**
	 * Constructs a observer by giving the values of the fields.
	 * 
	 * @param loc_name
	 *            Name of the city/observatory, or otherwise the user location.
	 * @param loc_lon
	 *            Longitude in radians. West negative.
	 * @param loc_lat
	 *            Latitude in radians.
	 * @param loc_alt
	 *            Altitude in meters.
	 * @param tz
	 *            Time zone in hours.
	 * @param pres
	 *            Pressure in milibars.
	 * @param temp
	 *            Temperature in Celsius.
	 * @param humi
	 *            Humidity as a percentage, from 0 to 100.
	 */
	public ObserverElement(String loc_name, double loc_lon, double loc_lat,
			int loc_alt, double tz, double pres, double temp, double humi) {
		name = loc_name;
		longitude = loc_lon;
		latitude = loc_lat;
		height = loc_alt;
		timeZone = tz;
		isAnObservatory = false;
		pressure = pres;
		temperature = temp;
		humidity = humi;

	}


	/**
	 * Name of the location.
	 */
	public String name;

	/**
	 * Longitude in radians measured to the east of the observer.
	 */
	public double longitude;

	/**
	 * Geodetic latitude in radians of the observer.
	 */
	public double latitude;

	/**
	 * Height above sea level in meters of the observer.
	 */
	public int height;

	/**
	 * Time zone.
	 */
	public double timeZone;

	/**
	 * Pressure in milibars.
	 */
	public double pressure;

	/**
	 * Temperature in Celsius.
	 */
	public double temperature;

	/**
	 * Humidity as a percentage, from 0 to 100.
	 */
	public double humidity;

	/**
	 * Geocentric longitude in radians, only used when converting from geodetic.
	 */
	public double geoLon;

	/**
	 * Geocentric latitude in radians, only used when converting from geodetic.
	 */
	public double geoLat;

	/**
	 * Geocentric distance in Earth radii, only used when converting from
	 * geodetic.
	 */
	public double geoRad;

	/**
	 * Daylight Saving Time string code. Can be N1, N2, S1, S2 (as set by DST
	 * method in Country class), or the symbolic constants defined in this
	 * class, dst_USA_old_rule, dst_USA_auto_rule, and dst_USA_new_rule (new
	 * rule approved by the USA congress in 2005, and being applied since 2007).
	 */
	public String dstCode;

	/**
	 * Sets wether the observer is at some observatory or in a city.
	 */
	public boolean isAnObservatory;

	/**
	 * Default temperature of 20 Celsius.
	 */
	public static final int DEFAULT_TEMPERATURE = 20;

	/**
	 * Default humidity of 20%.
	 */
	public static final int DEFAULT_HUMIDITY = 20;

	/**
	 * Default pressure of 1013 mb.
	 */
	public static final int DEFAULT_PRESSURE = 1013;

	/**
	 * Symbolic constant for USA/CANADA old rule for DST.
	 */
	public static final String DST_USA_OLD_RULE = "USA_OLD";

	/**
	 * Symbolic constant for USA/CANADA new rule for DST.
	 */
	public static final String DST_USA_NEW_RULE = "USA_NEW";

	/**
	 * Symbolic constant for USA/CANADA automatic rule selection for DST. Before
	 * 2007 old rule will be applied, in 2007 and after the new one. This is the
	 * default behavior and it is not necessary to choose this setting for USA
	 * or Canada, although it could be useful if it is necessary to apply the
	 * same method in other countries.
	 */
	public static final String DST_USA_AUTO_RULE = "USA_AUTO";

	/**
	 * Get latitude in degrees.
	 * 
	 * @return Latitude in <B>degrees</B>
	 */
	public double getLatitudeDeg() {
		return Math.toDegrees(latitude);
	}

	/**
	 * Get latitude in radians.
	 * 
	 * @return Latitude in <B>radians</B>
	 */
	public double getLatitudeRad() {
		return latitude;
	}

	/**
	 * Set latitude in <B>degrees</B>.
	 * 
	 * @param lat
	 *            Latitude in <B>degrees</B>
	 */
	public void setLatitudeDeg(double lat) {
		latitude = Math.toRadians(lat);
	}

	/**
	 * Set latitude in <B>radians</B>.
	 * 
	 * @param lat
	 *            Latitude in <B>radians</B>
	 */
	public void setLatitudeRad(double lat) {
		latitude = lat;
	}

	/**
	 * Get longitude in degrees.
	 * 
	 * @return Longitude in <B>degrees</B>
	 */
	public double getLongitudeDeg() {
		return Math.toDegrees(longitude);
	}

	/**
	 * Get longitude in radians.
	 * 
	 * @return Longitude in <B>radians</B>
	 */
	public double getLongitudeRad() {
		return longitude;
	}

	/**
	 * Set longitude in degrees.
	 * 
	 * @param lon
	 *            Longitude in <B>degrees</B>
	 */
	public void setLongitudeDeg(double lon) {
		longitude = Math.toRadians(lon);
	}

	/**
	 * Set longitude in radians.
	 * 
	 * @param lon
	 *            Longitude in <B>radians</B>
	 */
	public void setLongitudeRad(double lon) {
		longitude = lon;
	}

	/**
	 * Get time zone offset.
	 * 
	 * @return Time zone offset from UTC (-12 to 12 inclusive)
	 */
	public double getTimeZone() {
		return timeZone;
	}

	/**
	 * Set time zone offset.
	 * 
	 * @param tz
	 *            Time zone offset from UTC (-12 to 12 inclusive)
	 */
	public void setTimeZone(int tz) {
		timeZone = tz;
	}

	/**
	 * Clones this instance.
	 */
	public Object clone() {
		if (this == null)
			return null;
		ObserverElement o = new ObserverElement();
		o.dstCode = this.dstCode;
		o.geoLat = this.geoLat;
		o.geoLon = this.geoLon;
		o.geoRad = this.geoRad;
		o.height = this.height;
		o.humidity = this.humidity;
		o.isAnObservatory = this.isAnObservatory;
		o.latitude = this.latitude;
		o.longitude = this.longitude;
		o.name = this.name;
		o.pressure = this.pressure;
		o.temperature = this.temperature;
		o.timeZone = this.timeZone;
		return o;
	}

	/**
	 * Checks if the actual {@linkplain ObserverElement} is similar to another
	 * or not.
	 */
	public boolean equals(Object o) {
		if (o == null) {
			return false;
		}

		ObserverElement obs = (ObserverElement) o;
		boolean equal = true;

		if (!obs.dstCode.equals(this.dstCode))
			equal = false;
		if (obs.geoLat != this.geoLat)
			equal = false;
		if (obs.geoLon != this.geoLon)
			equal = false;
		if (obs.geoRad != this.geoRad)
			equal = false;
		if (obs.height != this.height)
			equal = false;
		if (obs.humidity != this.humidity)
			equal = false;
		if (obs.isAnObservatory != this.isAnObservatory)
			equal = false;
		if (obs.latitude != this.latitude)
			equal = false;
		if (obs.longitude != this.longitude)
			equal = false;
		if (!obs.name.equals(this.name))
			equal = false;
		if (obs.pressure != this.pressure)
			equal = false;
		if (obs.temperature != this.temperature)
			equal = false;
		if (obs.timeZone != this.timeZone)
			equal = false;

		return equal;
	}


}
