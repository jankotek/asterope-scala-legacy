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
 * A class to performs coordinates transformations.
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class CoordinateSystem
{


	/**
	 * Transform from equatorial to horizontal (geometric) coordinates.
	 * 
	 * @param loc Location object with equatorial coordinates.
	 * @param time Time object.
	 * @param obs Observer object.
	 * @param eph Ephemeris object.
	 * @return Location object with the horizontal coordinates.
	 * @ If the date is invalid.
	 */
	public static LocationElement equatorialToHorizontal(LocationElement loc, TimeElement time, ObserverElement obs,
			EphemerisElement eph)
	{
		//if (!eph.isTopocentric) return null;

		double TSL = SiderealTime.apparentSiderealTime(time, obs, eph);
		RotateFrom rot = new RotateFrom(-TSL, obs.latitude, Math.PI / 2.0, -loc.getLongitude(), loc.getLatitude());

		LocationElement loc_out = rotateFrom(rot);
		loc_out.setRadius(loc.getRadius());

		return loc_out;
	}


	/**
	 * J2000 right ascension of the galactic pole.
	 */
	public static final double GALACTIC_POLE_RA_J2000 = EphemUtils.parseRightAscension(12, 51.44, 0.0);

	/**
	 * J2000 declination of the galactic pole.
	 */
	public static final double GALACTIC_POLE_DEC_J2000 = EphemUtils.parseDeclination(27, 7.7, 0.0);

	/**
	 * J2000 node of the galactic pole.
	 */
	public static final double GALACTIC_POLE_NODE_J2000 = EphemUtils.parseDeclination(33.64, 0.0, 0.0);

	/**
	 * Transform from equatorial to galactic coordinates.
	 * 
	 * @param loc Location object with equatorial coordinates.
	 * @param time Time object. Only for symmetry in the calls, not used.
	 * @param obs Observer object. Only for symmetry in the calls, not
	 *        used.
	 * @param eph Ephemeris object. The equinox is taken into account.
	 * @return Location object with the galactic coordinates.
	 * @ If the date is invalid.
	 */
	public static LocationElement equatorialToGalactic(LocationElement loc, TimeElement time, ObserverElement obs,
			EphemerisElement eph)
	{
		// Transform galactic pole to input equinox
		double pos[] = LocationElement.parseLocationElement(new LocationElement(GALACTIC_POLE_RA_J2000,
				GALACTIC_POLE_DEC_J2000, 1.0));
		double JD = TimeScale.getJD(time, obs, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
		double equinox = eph.equinox;
		if (equinox == EphemerisElement.EQUINOX_OF_DATE)
			equinox = JD;
		double new_pos[] = Precession.precess(EphemConstant.J2000, equinox, pos, eph.ephemMethod);
		LocationElement new_loc = LocationElement.parseRectangularCoordinates(new_pos);

		// Rotate
		RotateFrom rot = new RotateFrom(new_loc.getLongitude(), new_loc.getLatitude(), GALACTIC_POLE_NODE_J2000, loc
				.getLongitude(), loc.getLatitude());
		LocationElement loc_out = rotateFrom(rot);
		loc_out.setRadius(loc.getRadius());

		return loc_out;
	}


	/**
	 * Generic 'rotation from'.
	 *
	 * @param rot RotateFrom object.
	 * @return Rotated input.
	 */
	static LocationElement rotateFrom(RotateFrom rot)
	{
		double y = Math
				.asin(Math.sin(rot.DELTA) * Math.sin(rot.DELTA0) + Math.cos(rot.DELTA) * Math.cos(rot.DELTA0) * Math
						.cos(rot.ALFA - rot.ALFA0));
		double x = rot.LON0 + Math.atan2(Math.sin(rot.DELTA) - Math.sin(y) * Math.sin(rot.DELTA0),
				Math.cos(rot.DELTA) * Math.cos(rot.DELTA0) * Math.sin(rot.ALFA - rot.ALFA0));

		LocationElement loc = new LocationElement(x, y, 1.0);
		return loc;
	}






    // A RotateFrom term
static class RotateFrom
{
	RotateFrom(double alfa0, double delta0, double l0, double alfa, double delta)
	{
		ALFA0 = alfa0;
		DELTA0 = delta0;
		LON0 = l0;
		ALFA = alfa;
		DELTA = delta;
	}

	double ALFA0;
	double DELTA0;
	double LON0;
	double ALFA;
	double DELTA;
}
}
