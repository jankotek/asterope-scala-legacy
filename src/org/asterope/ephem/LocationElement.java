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
 * This is a convenience class used for passing around polar coordinates. Units
 * are radians for longitude and latitude, AU for distance.
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class LocationElement implements Serializable
{

	private static final long serialVersionUID = -1797358704223649908L;

	/**
	 * Pseudo-enum indices into vector form of {@linkplain LocationElement}.
	 */
	public final static int LATITUDE = 1, LONGITUDE = 0, RADIUS = 2;

	/**
	 * Default constructor.
	 */
	public LocationElement()
	{
		this.lat = 0.0;
		this.lon = 0.0;
		this.rad = 0.0;
	}

	/**
	 * Explicit constructor.
	 * 
	 * @param lon longitude.
	 * @param lat latitude.
	 * @param rad radius.
	 */
	public LocationElement(double lon, double lat, double rad)
	{
		this.lat = lat;
		this.lon = lon;
		this.rad = rad;
	}

	/**
	 * Vector constructor.
	 * 
	 * @param vector { lat, lon, rad }
	 */
	public LocationElement(double vector[])
	{
		set(vector);
	}

	/**
	 * Gets the latitude.
	 * 
	 * @return The latitude value of this instance.
	 */
	public double getLatitude()
	{
		return lat;
	}

	/**
	 * Gets the longitude.
	 * 
	 * @return The longitude value of this instance.
	 */
	public double getLongitude()
	{
		return lon;
	}

	/**
	 * Gets the radius.
	 * 
	 * @return The radius value of this instance.
	 */
	public double getRadius()
	{
		return rad;
	}

	/**
	 * Get all values in this instance as a vector.
	 * <P>
	 * The vector is an array of six doubles, latitude, longitude, radius,
	 * latitude speed, longitude speed, radius speed, in that order.
	 * 
	 * @return v[0] = longitude, v[1] = latitude, v[2] = radius.
	 */
	public double[] get()
	{
		double vector[] = new double[6];
		vector[LATITUDE] = lat;
		vector[LONGITUDE] = lon;
		vector[RADIUS] = rad;
		return vector;
	}

	/**
	 * Set the latitude.
	 * 
	 * @param d The new latitude value.
	 */
	public void setLatitude(double d)
	{
		lat = d;
	}

	/**
	 * Set the longitude.
	 * 
	 * @param d The new longitude value.
	 */
	public void setLongitude(double d)
	{
		lon = d;
	}

	/**
	 * Set the radius.
	 * 
	 * @param d The new radius value.
	 */
	public void setRadius(double d)
	{
		rad = d;
	}

	/**
	 * Set all members of this instance from a vector.
	 * <P>
	 * The vector is an array of three doubles, latitude, longitude, radius, in
	 * that order.
	 * 
	 * @param vector v[0] = latitude, v[1] = longitude, v[2] = radius.
	 */
	public void set(double vector[])
	{
		lat = vector[LATITUDE];
		lon = vector[LONGITUDE];
		rad = vector[RADIUS];
	}

	/**
	 * Set all members of this instance individually.
	 * 
	 * @param lon The new longitude.
	 * @param lat The new latitude.
	 * @param rad The new radius.
	 */
	public void set(double lon, double lat, double rad)
	{
		this.lat = lat;
		this.lon = lon;
		this.rad = rad;
	}

	/**
	 * latitude.
	 */
	private double lat;

	/**
	 * longitude.
	 */
	private double lon;

	/**
	 * radius.
	 */
	private double rad;

	/**
	 * Transforms rectangular coordinates x, y, z contained in an array to a
	 * {@linkplain LocationElement}. Coordinate system is independent: equatorial, ecliptic,
	 * or any other.
	 * 
	 * @param v (x, y, z) vector.
	 * @return Location object.
	 */
	public static LocationElement parseRectangularCoordinates(double v[])
	{
		double lon = 0.0;
		double lat = EphemConstant.PI_OVER_TWO;
		if (v[2] < 0.0)
			lat = -lat;
		if (v[1] != 0.0 || v[0] != 0.0)
		{
			lon = Math.atan2(v[1], v[0]);
			lat = Math.atan(v[2] / Math.sqrt(v[0] * v[0] + v[1] * v[1]));
		}
		double rad = Math.sqrt(v[0] * v[0] + v[1] * v[1] + v[2] * v[2]);

		LocationElement loc = new LocationElement(lon, lat, rad);
		return loc;
	}

	/**
	 * Transforms rectangular coordinates x, y, z to a {@linkplain LocationElement}.
	 * Coordinate system is independent: equatorial, ecliptic, or any other.
	 * 
	 * @param x X coordinate.
	 * @param y Y coordinate.
	 * @param z Z coordinate.
	 * @return Location object.
	 */
	public static LocationElement parseRectangularCoordinates(double x, double y, double z)
	{
		double v[] = new double[] { x, y, z };
		LocationElement loc = LocationElement.parseRectangularCoordinates(v);
		return loc;
	}

	/**
	 * Transforms a {@linkplain LocationElement} into a set of rectangular coordinates x, y,
	 * z.
	 * 
	 * @param loc Location object.
	 * @return Array with (x, y, z) vector.
	 */
	public static double[] parseLocationElement(LocationElement loc)
	{
		double x = loc.rad * Math.cos(loc.lon) * Math.cos(loc.lat);
		double y = loc.rad * Math.sin(loc.lon) * Math.cos(loc.lat);
		double z = loc.rad * Math.sin(loc.lat);

		return new double[] { x, y, z };
	}

	/**
	 * Transforms a {@linkplain LocationElement} into a set of rectangular coordinates x, y,
	 * z.
	 * 
	 * @return Array with (x, y, z) vector.
	 */
	public double[] getRectangularCoordinates()
	{
		return LocationElement.parseLocationElement(this);
	}

	/**
	 * Obtain linear distance between two spherical positions.
	 * 
	 * @param loc1 Location object.
	 * @param loc2 Location object.
	 * @return Linear distance.
	 */
	public static double getLinearDistance(LocationElement loc1, LocationElement loc2)
	{
		double[] xyz1 = parseLocationElement(loc1);
		double[] xyz2 = parseLocationElement(loc2);

		double dx = xyz1[0] - xyz2[0];
		double dy = xyz1[1] - xyz2[1];
		double dz = xyz1[2] - xyz2[2];

		double r = Math.sqrt(dx * dx + dy * dy + dz * dz);

		return r;
		//return getAngularDistance(loc1,loc2)*loc1.rad;
	}

	/**
	 * Obtain angular distance between two spherical coordinates.
	 * 
	 * @param loc1 Location object.
	 * @param loc2 Location object.
	 * @return The distance in radians, from 0 to PI.
	 */
	public static double getAngularDistance(LocationElement loc1, LocationElement loc2)
	{		
		LocationElement cl1 = new LocationElement(loc1.getLongitude(), loc1.getLatitude(), 1.0);
		LocationElement cl2 = new LocationElement(loc2.getLongitude(), loc2.getLatitude(), 1.0);

		double[] xyz1 = parseLocationElement(cl1);
		double[] xyz2 = parseLocationElement(cl2);

		double dx = xyz1[0] - xyz2[0];
		double dy = xyz1[1] - xyz2[1];
		double dz = xyz1[2] - xyz2[2];

		double r2 = dx * dx + dy * dy + dz * dz;

		return Math.acos(1.0 - r2 * 0.5);
/*
 		// Haversine formula
 		double dLat = loc1.lat - loc2.lat;
		double dLon = loc1.lon - loc2.lon;
		double a = FastMath.sin(dLat/2) * FastMath.sin(dLat/2) + FastMath.cos(loc1.lat) * FastMath.cos(loc2.lat) * FastMath.sin(dLon/2) * FastMath.sin(dLon/2); 
		return 2.0 * FastMath.atan2(Math.sqrt(a), Math.sqrt(1.0-a)); 
*/
		// Too exact
/*		LocationElement cl1 = new LocationElement(loc1.getLongitude(), loc1.getLatitude(), 1.0);
		LocationElement cl2 = new LocationElement(loc2.getLongitude(), loc2.getLatitude(), 1.0);

		double[] xyz1 = parseLocationElement(cl1);
		double[] xyz2 = parseLocationElement(cl2);

		double dx = xyz1[0] - xyz2[0];
		double dy = xyz1[1] - xyz2[1];
		double dz = xyz1[2] - xyz2[2];

		double r2 = dx * dx + dy * dy + dz * dz;

		return Math.acos(1.0 - r2 * 0.5);
*/		
	}

	/**
	 * Obtain position angle between two spherical coordinates. Use this function only if the two
	 * positions have similar latitudes, otherwise it could give wrong results. Good performance.
	 * 
	 * @param loc1 Location object.
	 * @param loc2 Location object.
	 * @return The position angle in radians.
	 */
	public static double getApproximatePositionAngle(LocationElement loc1, LocationElement loc2)
	{
		return Math.atan2(Math.cos(loc2.getLatitude()) * Math.sin(loc1.getLongitude() - loc2.getLongitude()), 
				Math.cos(loc2.getLatitude()) * Math.sin(loc1.getLatitude()) * Math.cos(loc1.getLongitude() - loc2.getLongitude()) - Math.sin(loc2.getLatitude()) * Math.cos(loc1.getLatitude()));
	}

	/**
	 * Obtain exact position angle between two spherical coordinates. Performance will be poor.
	 * 
	 * @param loc1 Location object.
	 * @param loc2 Location object.
	 * @return The position angle in radians.
	 */
	public static double getPositionAngle(LocationElement loc1, LocationElement loc2)
	{
		double A = loc1.getLongitude() - loc2.getLongitude();
		double sinl1 = Math.cos(loc1.lat);
		double cosl1 = Math.sin(loc1.lat);
		double sinl2 = Math.cos(loc2.lat);
		
		double cosa = Math.sin(loc2.lat) * cosl1 + sinl2 * sinl1 * Math.cos(A);
		double a = Math.acos(cosa);
		double sina = Math.sin(a);
		double sinPA = (sinl2 * Math.sin(A)) / sina;
		double PA = Math.asin(sinPA);
		double cosPA = Math.cos(PA);
		
		// Now the problem: the correct result is PA or PI-PA? Let's check the other side of the ABC spherical triangle
		double cosb1 = cosl1 * cosa + sinl1 * sina * cosPA;
		double cosb2 = cosl1 * cosa - sinl1 * sina * cosPA;
		double cosb = -Math.cos(loc2.getLatitude());
		double dif1 = Math.abs(cosb-cosb1);
		double dif2 = Math.abs(cosb-cosb2);
		if (dif2 < dif1) 
			PA = Math.PI-PA;
		return PA;
		
	}

	/**
	 * To clone the object.
	 */
	public Object clone()
	{
		LocationElement loc = new LocationElement(this.getLongitude(), this.getLatitude(),
				this.getRadius());
		return loc;
	}
	/**
	 * Returns true if the input object is equals to this instance.
	 */
	public boolean equals(Object l)
	{
		if (l == null) {
			return false;
		}

		boolean equals = false;
		LocationElement loc = (LocationElement) l;
		if (loc.getLongitude() == this.getLongitude() &&
				loc.getLatitude() == this.getLatitude() &&
				loc.getRadius() == this.getRadius()) equals = true;
		return equals;
	}
}
