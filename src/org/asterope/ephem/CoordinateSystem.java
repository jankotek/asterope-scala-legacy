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
	 * Transform from horizontal (geometric) to equatorial coordinates.
	 * 
	 * @param loc Location object with horizontal coordinates.
	 * @param time Time object.
	 * @param obs Observer object.
	 * @param eph Ephemeris object.
	 * @return Location object with the equatorial coordinates.
	 * @ If the date is invalid.
	 */
	public static LocationElement horizontalToEquatorial(LocationElement loc, TimeElement time, ObserverElement obs,
			EphemerisElement eph)
	{
		double TSL = SiderealTime.apparentSiderealTime(time, obs, eph);
		RotateTo rot = new RotateTo(TSL, obs.latitude, Math.PI / 2.0, -loc.getLongitude(), loc.getLatitude());

		LocationElement loc_out = rotateTo(rot);
		loc_out.setRadius(loc.getRadius());

		return loc_out;
	}

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
	 * Transform from horizontal (geometric) to equatorial coordinates.
	 * 
	 * @param loc Location object with horizontal coordinates.
	 * @param ast Apparent Sidereal Time in radians.
	 * @param lat Latitude of observer in radians.
	 * @return Location object with the equatorial coordinates.
	 */
	public static LocationElement horizontalToEquatorial(LocationElement loc, double ast, double lat)
	{
		RotateTo rot = new RotateTo(ast, lat, Math.PI / 2.0, -loc.getLongitude(), loc.getLatitude());

		LocationElement loc_out = rotateTo(rot);
		loc_out.setRadius(loc.getRadius());

		return loc_out;
	}

	/**
	 * Transform from equatorial to horizontal coordinates.
	 * 
	 * @param loc Location object with equatorial coordinates.
	 * @param ast Apparent Sidereal Time in radians.
	 * @param obs Observer object.
	 * @param toApparentElevation True for obtaining apparent elevation.
	 * @return Location object with the horizontal coordinates.
	 * @ If the date is invalid.
	 */
	public static LocationElement equatorialToHorizontal(LocationElement loc, double ast, ObserverElement obs,
			boolean toApparentElevation)
	{
		RotateFrom rot = new RotateFrom(-ast, obs.latitude, Math.PI / 2.0, -loc.getLongitude(), loc.getLatitude());

		LocationElement loc_out = rotateFrom(rot);
		if (toApparentElevation)
		{
			double apparentAlt = EphemUtils.getApparentElevation(obs, loc_out.getLatitude());
			loc_out.setLatitude(apparentAlt);
		}
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
	 * Transform from galactic to equatorial coordinates.
	 * 
	 * @param loc Location object with galactic coordinates.
	 * @param time Time object. Only for symmetry in the calls, not used.
	 * @param obs Observer object. Only for symmetry in the calls, not
	 *        used.
	 * @param eph Ephemeris object. The equinox is taken into account.
	 * @return Location object with the equatorial coordinates.
	 * @ If the date is invalid.
	 */
	public static LocationElement galacticToEquatorial(LocationElement loc, TimeElement time, ObserverElement obs,
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
		RotateTo rot = new RotateTo(new_loc.getLongitude(), new_loc.getLatitude(), GALACTIC_POLE_NODE_J2000, loc
				.getLongitude(), loc.getLatitude());
		LocationElement loc_out = rotateTo(rot);
		loc_out.setRadius(loc.getRadius());

		return loc_out;
	}

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
	 * Transform from ecliptic to equatorial coordinates.
	 * 
	 * @param loc Location object with ecliptic coordinates.
	 * @param time Time object.
	 * @param obs Observer object.
	 * @param eph Ephemeris object. If the ephemeris type is apparent,
	 *        then the rotation will be done respect to the true ecliptic of
	 *        date.
	 * @return Location object with the equatorial coordinates.
	 * @ If the date is invalid.
	 */
	public static LocationElement eclipticToEquatorial(LocationElement loc, TimeElement time, ObserverElement obs,
			EphemerisElement eph)
	{
		double JD = TimeScale.getJD(time, obs, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
		double equinox = eph.equinox;
		if (equinox == EphemerisElement.EQUINOX_OF_DATE)
			equinox = JD;
		double obliquity = Obliquity.meanObliquity(EphemUtils.toCenturies(equinox), eph.ephemMethod);
		if (eph.ephemType ==EphemerisElement.Ephem.APPARENT)
			obliquity = Obliquity.trueObliquity(EphemUtils.toCenturies(equinox), eph.ephemMethod);
		RotateTo rot = new RotateTo(-Math.PI * 0.5, Math.PI * 0.5 - obliquity, 0.0, loc.getLongitude(), loc
				.getLatitude());

		LocationElement loc_out = rotateTo(rot);
		loc_out.setRadius(loc.getRadius());

		return loc_out;
	}

	/**
	 * Transform from equatorial to ecliptic coordinates.
	 * 
	 * @param loc Location object with equatorial coordinates.
	 * @param time Time object.
	 * @param obs Observer object.
	 * @param eph Ephemeris object. If the ephemeris type is apparent,
	 *        then the rotation will be done respect to the true ecliptic of
	 *        date.
	 * @return Location object with the ecliptic coordinates.
	 * @ If the date is invalid.
	 */
	public static LocationElement equatorialToEcliptic(LocationElement loc, TimeElement time, ObserverElement obs,
			EphemerisElement eph)
	{
		double JD = TimeScale.getJD(time, obs, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
		double equinox = eph.equinox;
		if (equinox == EphemerisElement.EQUINOX_OF_DATE)
			equinox = JD;
		double obliquity = Obliquity.meanObliquity(EphemUtils.toCenturies(equinox), eph.ephemMethod);
		if (eph.ephemType == EphemerisElement.Ephem.APPARENT)
			obliquity = Obliquity.trueObliquity(EphemUtils.toCenturies(equinox), eph.ephemMethod);
		RotateFrom rot = new RotateFrom(Math.PI * 0.5, Math.PI * 0.5 + obliquity, 0.0, loc.getLongitude(), loc
				.getLatitude());

		LocationElement loc_out = rotateFrom(rot);
		loc_out.setRadius(loc.getRadius());

		return loc_out;
	}

	/**
	 * Transform from galactic to equatorial coordinates.
	 * 
	 * @param loc Location object with galactic coordinates.
	 * @param pole_RA RA of north galactic pole in the correct equinox.
	 * @param pole_DEC DEC of north galactic pole in the correct equinox.
	 * @return Location object with the equatorial coordinates.
	 */
	public static LocationElement galacticToEquatorial(LocationElement loc, double pole_RA, double pole_DEC)
	{
		// Rotate
		RotateTo rot = new RotateTo(pole_RA, pole_DEC, GALACTIC_POLE_NODE_J2000, loc.getLongitude(), loc.getLatitude());
		LocationElement loc_out = rotateTo(rot);
		loc_out.setRadius(loc.getRadius());

		return loc_out;
	}

	/**
	 * Transform from equatorial to galactic coordinates.
	 * 
	 * @param loc Location object with equatorial coordinates.
	 * @param pole_RA RA of north galactic pole in the correct equinox.
	 * @param pole_DEC DEC of north galactic pole in the correct equinox.
	 * @return Location object with the galactic coordinates.
	 */
	public static LocationElement equatorialToGalactic(LocationElement loc, double pole_RA, double pole_DEC)
	{
		// Rotate
		RotateFrom rot = new RotateFrom(pole_RA, pole_DEC, GALACTIC_POLE_NODE_J2000, loc.getLongitude(), loc
				.getLatitude());
		LocationElement loc_out = rotateFrom(rot);
		loc_out.setRadius(loc.getRadius());

		return loc_out;
	}

	/**
	 * Transform from ecliptic to equatorial coordinates.
	 * 
	 * @param loc Location object with ecliptic coordinates.
	 * @param obliquity Correct obliquity in equinox and type (true or mean).
	 * @return Location object with the equatorial coordinates.
	 */
	public static LocationElement eclipticToEquatorial(LocationElement loc, double obliquity)
	{
		RotateTo rot = new RotateTo(-Math.PI * 0.5, Math.PI * 0.5 - obliquity, 0.0, loc.getLongitude(), loc
				.getLatitude());

		LocationElement loc_out = rotateTo(rot);
		loc_out.setRadius(loc.getRadius());

		return loc_out;
	}

	/**
	 * Transform from equatorial to ecliptic coordinates.
	 * 
	 * @param loc Location object with equatorial coordinates.
	 * @param obliquity Correct obliquity in equinox and type (true or mean).
	 * @return Location object with the ecliptic coordinates.
	 */
	public static LocationElement equatorialToEcliptic(LocationElement loc, double obliquity)
	{
		RotateFrom rot = new RotateFrom(Math.PI * 0.5, Math.PI * 0.5 + obliquity, 0.0, loc.getLongitude(), loc
				.getLatitude());

		LocationElement loc_out = rotateFrom(rot);
		loc_out.setRadius(loc.getRadius());

		return loc_out;
	}

	/**
	 * Generic 'rotation to'.
	 * 
	 * @param rot RotateTo object.
	 * @return Rotated input.
	 */
	static LocationElement rotateTo(RotateTo rot)
	{
		double lat = Math.asin(Math.sin(rot.Y) * Math.sin(rot.DELTA0) + Math.cos(rot.Y) * Math.cos(rot.DELTA0) * Math
				.sin(rot.X - rot.LON0));
		double lon = rot.ALFA0 + Math.atan2(Math.cos(rot.Y) * Math.cos(rot.X - rot.LON0), Math.sin(rot.Y) * Math
				.cos(rot.DELTA0) - Math.cos(rot.Y) * Math.sin(rot.DELTA0) * Math.sin(rot.X - rot.LON0));

		LocationElement loc = new LocationElement(lon, lat, 1.0);
		return loc;
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

//	/**
//	 * List different kind of coordinates.
//	 */
//	public static final String COORDINATES_TYPES[] = SkyRenderElement.COORDINATE_SYSTEMS;
//	/**
//	 * ID constant for equatorial coordinates.
//	 */
//	public static final int COORDINATE_SYSTEM_EQUATORIAL = SkyRenderElement.COORDINATE_SYSTEM_EQUATORIAL;
//	/**
//	 * ID constant for ecliptic coordinates.
//	 */
//	public static final int COORDINATE_SYSTEM_ECLIPTIC = SkyRenderElement.COORDINATE_SYSTEM_ECLIPTIC;
//	/**
//	 * ID constant for galactic coordinates.
//	 */
//	public static final int COORDINATE_SYSTEM_GALACTIC = SkyRenderElement.COORDINATE_SYSTEM_GALACTIC;
//	/**
//	 * ID constant for horizontal coordinates.
//	 */
//	public static final int COORDINATE_SYSTEM_HORIZONTAL = SkyRenderElement.COORDINATE_SYSTEM_HORIZONTAL;
//	/**
//	 * Transforms coordinates.
//	 * @param input Input type, as define in the id constants.
//	 * @param output Output type.
//	 * @param loc Input coordinates.
//	 * @param time Time object.
//	 * @param obs Observer object.
//	 * @param eph Ephemeris object.
//	 * @return Output coordinates.
//	 * @ If an error occurs.
//	 */
//	public static LocationElement transform(int input, int output, LocationElement loc,
//			TimeElement time, ObserverElement obs, EphemerisElement eph)
//	 {
//		LocationElement out = loc;
//		if (input == COORDINATE_SYSTEM_EQUATORIAL) {
//			if (output == COORDINATE_SYSTEM_ECLIPTIC) out = CoordinateSystem.equatorialToEcliptic(loc, time, obs, eph);
//			if (output == COORDINATE_SYSTEM_HORIZONTAL) out = CoordinateSystem.equatorialToHorizontal(loc, time, obs, eph);
//			if (output == COORDINATE_SYSTEM_GALACTIC) out = CoordinateSystem.equatorialToGalactic(loc, time, obs, eph);
//		}
//		if (input == COORDINATE_SYSTEM_ECLIPTIC) {
//			out = CoordinateSystem.eclipticToEquatorial(loc, time, obs, eph);
//			if (output == COORDINATE_SYSTEM_HORIZONTAL) out = CoordinateSystem.equatorialToHorizontal(loc, time, obs, eph);
//			if (output == COORDINATE_SYSTEM_GALACTIC) out = CoordinateSystem.equatorialToGalactic(loc, time, obs, eph);
//		}
//		if (input == COORDINATE_SYSTEM_HORIZONTAL) {
//			out = CoordinateSystem.horizontalToEquatorial(loc, time, obs, eph);
//			if (output == COORDINATE_SYSTEM_ECLIPTIC) out = CoordinateSystem.equatorialToEcliptic(loc, time, obs, eph);
//			if (output == COORDINATE_SYSTEM_GALACTIC) out = CoordinateSystem.equatorialToGalactic(loc, time, obs, eph);
//		}
//		if (input == COORDINATE_SYSTEM_GALACTIC) {
//			out = CoordinateSystem.galacticToEquatorial(loc, time, obs, eph);
//			if (output == COORDINATE_SYSTEM_ECLIPTIC) out = CoordinateSystem.equatorialToEcliptic(loc, time, obs, eph);
//			if (output == COORDINATE_SYSTEM_HORIZONTAL) out = CoordinateSystem.equatorialToHorizontal(loc, time, obs, eph);
//		}
//		return out;
//	}


//A RotateTo term
static class RotateTo
{
	RotateTo(double alfa0, double delta0, double l0, double x, double y)
	{
		ALFA0 = alfa0;
		DELTA0 = delta0;
		LON0 = l0;
		X = x;
		Y = y;
	}

	double ALFA0;
	double DELTA0;
	double LON0;
	double X;
	double Y;
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
