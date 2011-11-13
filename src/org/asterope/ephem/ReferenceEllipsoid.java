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
 * A class to apply geodetic/geocentric coordinates transformations using
 * different reference ellipsoids.
 * 
 * @author T. Alonso Albi - OAN (Spain), M. Huss
 * @version 1.0
 */
public class ReferenceEllipsoid
{
	
	public enum Method {
	/**
	 * ID constant for WSG84 reference ellipsoid.
	 */
	WGS84,

	/**
	 * ID constant for IERS1989 reference ellipsoid.
	 */
	IERS1989,

	/**
	 * ID constant for Merit (1983) reference ellipsoid.
	 */
	MERIT1983,

	/**
	 * ID constant for GRS80 reference ellipsoid.
	 */
	GRS80,

	/**
	 * ID constant for GRS67 reference ellipsoid.
	 */
	GRS67,

	/**
	 * ID constant for IAU1976 reference ellipsoid.
	 */
	IAU1976,

	/**
	 * ID constant for IAU1964 reference ellipsoid.
	 */
	IAU1964,

	/**
	 * ID constant for IERS2003 reference ellipsoid.
	 */
	IERS2003

	}
	public static final Method LATEST = Method.IERS2003;

	/**
	 * Earth radius in km;
	 */
	public double earthRadius;
	/**
	 * Inverse of the flattening factor.
	 */
	public double inverseFlatteningFactor;

	/**
	 * Constructor with specific parameters.
	 * @param er Earth radius.
	 * @param iff Inverse of flattening factor.
	 */
	public ReferenceEllipsoid(double er, double iff)
	{
		earthRadius = er;
		inverseFlatteningFactor = iff;
	}
	
	/**
	 * Obtain the parameters of certain reference ellipsoid.
	 * 
	 * @param ellipsoid The ID constant of the reference ellipsoid.
	 */
	public ReferenceEllipsoid(Method ellipsoid)
	{
		double A = 0.0, F = 0.0;

		switch (ellipsoid)
		{
		case IERS2003:
			// IERS 2003
			A = 6378.1366;
			F = 298.25642;
			break;
		case WGS84:
			// WGS 1984
			A = 6378.137;
			F = 298.257223563;
			break;
		case IERS1989:
			// IERS 1989
			A = 6378.136;
			F = 298.257;
			break;
		case MERIT1983:
			// Merit1983
			A = 6378.137;
			F = 298.257;
			break;
		case GRS80:
			// GRS80 (IUGG 1980)
			A = 6378.137;
			F = 298.257222;
			break;
		case GRS67:
			// GRS67 (IUGG 1967)
			A = 6378.160;
			F = 298.247167;
			break;
		case IAU1976:
			// IAU1976
			A = 6378.140;
			F = 298.257;
			break;
		case IAU1964:
			// IAU1964
			A = 6378.160;
			F = 298.25;
			break;
		default:
			throw new IllegalArgumentException("invalid ellipsoid " + ellipsoid + ".");
		}

		earthRadius = A;
		inverseFlatteningFactor = F;
	}

	/**
	 * Obtain radius of the Earth.
	 * 
	 * @param ellipsoid The ID constant of the reference ellipsoid.
	 * @return Earth radius in km.
	 */
	public static double getEarthRadius(Method ellipsoid)
	{
		ReferenceEllipsoid ref = new ReferenceEllipsoid(ellipsoid);

		return ref.earthRadius;
	}

	/**
	 * Obtain the inverse of the flatenning factor.
	 * 
	 * @param ellipsoid The ID constant of the reference ellipsoid.
	 * @return Inverse of the flatenning factor.
	 */
	public static double getInverseOfFlatenning(Method ellipsoid)
	{
		ReferenceEllipsoid ref = new ReferenceEllipsoid(ellipsoid);

		return ref.inverseFlatteningFactor;
	}

	/**
	 * Transform from geodetic to geocentric coordinates. The geocentric
	 * distance is set to Earth equatorial radii. For reference see Astronomical
	 * Almanac, page K5.
	 * 
	 * @param obs Observer object.
	 * @return Observer object, only with the geocentric fields.
	 */
	public ObserverElement geodeticToGeocentric(ObserverElement obs)
	{
		// Get ellipsoid
		double Earth_radius = this.earthRadius;
		double Earth_flatenning = this.inverseFlatteningFactor;

		// Apply calculations
		double flat = Earth_flatenning;
		double co = Math.cos(obs.latitude);
		double si = Math.sin(obs.latitude);
		double fl = 1.0 - 1.0 / flat;
		fl = fl * fl;
		si = si * si;
		double u = 1.0 / Math.sqrt(co * co + fl * si);
		double a = Earth_radius * u * 1000.0 + obs.height;
		double b = Earth_radius * fl * u * 1000.0 + obs.height;
		double rho = Math.sqrt(a * a * co * co + b * b * si);
		double geo_lat = Math.acos(a * co / rho);
		if (obs.latitude < 0.0)
			geo_lat = -geo_lat;
		rho = rho / (1000.0 * Earth_radius);

		obs.geoLat = geo_lat;
		obs.geoLon = obs.longitude;
		obs.geoRad = rho;

		return obs;
	}

	/**
	 * Transform from geocentric/planetocentric coordinates to geodetic. The
	 * geodetic distance is set as a height above mean elipsoid height. The
	 * method is an analitical inversion of the geodetic to geocentric
	 * transformation, that produces an slightly error in latitude in the
	 * milliarcsecond level.
	 * 
	 * @param obs Observer object.
	 * @return Observer object, only with the geodetic fields.
	 */
	public ObserverElement geocentricToGeodetic(ObserverElement obs)
	{
		// Get ellipsoid
		double Earth_radius = this.earthRadius;
		double Earth_flatenning = this.inverseFlatteningFactor;

		// Apply calculations
		double flat = Earth_flatenning;
		double fl = 1.0 - 1.0 / flat;
		fl = fl * fl;
		double rho = obs.geoRad;
		double lat = Math.atan(Math.tan(obs.geoLat) / fl);
		double co = Math.cos(lat);
		double si = Math.sin(lat);
		double u = 1.0 / Math.sqrt(co * co + fl * si * si);
		double a = Earth_radius * u * 1000.0;
		double b = Earth_radius * fl * u * 1000.0;

		rho = rho * (1000.0 * Earth_radius);

		double coef_A = co * co + si * si;
		double coef_B = 2.0 * a * co * co + 2.0 * b * si * si;
		double coef_C = a * a * co * co + b * b * si * si - rho * rho;

		double alt = (-coef_B + Math.sqrt(coef_B * coef_B - 4.0 * coef_A * coef_C)) / (2.0 * coef_A);
		lat = Math.acos(rho * Math.cos(obs.geoLat) / (a + alt));
		if (obs.geoLat < 0.0)
			lat = -lat;

		obs.latitude = lat;
		obs.longitude = obs.geoLon;
		obs.height = (int) Math.floor(alt + 0.5);

		return obs;
	}

	/**
	 * Gets the adequate reference ellipsoid for certain ephemeris properties.
	 * The returned ellipsoid is IAU1976 for Laskar ephemeris, and IERS2003 for
	 * IAU2000 and IAU2009 ephemeris. For different ephemeris methods IERS1989
	 * ellipsoid is returned.
	 * 
	 * @param eph Ephemeris properties.
	 * @return Integer ID value for the ellipsoid.
	 */
	public static Method getEllipsoid(EphemerisElement eph)
	{
		Method ellipsoid = Method.IERS1989;
		switch (eph.ephemMethod)
		{
		case LASKAR:
			ellipsoid = Method.IAU1976;
			break;
		case IAU2000:
			ellipsoid = Method.IERS2003;
			break;
		case CAPITAINE:
			ellipsoid = Method.IERS2003;
			break;
		}

		return ellipsoid;
	}

}
