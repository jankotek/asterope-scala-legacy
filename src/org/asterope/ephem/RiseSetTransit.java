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
 * Provides methods for calculation of the rise, set and transit instants. These
 * events can be obtained referred to the local horizon, or to any of the defined
 * twilight events. In the second case, no correction is made for the size of
 * the object nor the geometric position of the horizon, so the instant of the
 * middle of the event will be returned.
 * <P>
 * It is possible to pass any value in the methods to refer to results, for
 * example, to the default 34 arcminutes of refraction in the local horizon plus
 * the angular radius of the object, or to include the effects of the depression
 * of the horizon.
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class RiseSetTransit
{

	/**
	 * Constant ID for a circumpolar object, which has no rise or set times.
	 */
	public static final int CIRCUMPOLAR = -1;

	/**
	 * Constant ID for a never visible object, which has no rise or set times.
	 */
	public static final int ALWAYS_BELOW_HORIZON = -2;

	/**
	 * Event ID for calculation of rising and setting times for astronomical
	 * twilight. In this case, the calculated time will be the time when the
	 * centre of the object is at -18 degrees of geometrical elevation below the
	 * astronomical horizon.
	 */
	public static final double TWILIGHT_ASTRONOMICAL = 18.0 * EphemConstant.DEG_TO_RAD;

	/**
	 * Event ID for calculation of rising and setting times for nautical
	 * twilight. In this case, the calculated time will be the time when the
	 * centre of the object is at -12 degrees of geometric elevation below the
	 * astronomical horizon.
	 */
	public static final double TWILIGHT_NAUTICAL = 12.0 * EphemConstant.DEG_TO_RAD;

	/**
	 * Event ID for calculation of rising and setting times for civil twilight.
	 * In this case, the calculated time will be the time when the center of the
	 * object is at -6 degrees of geometric elevation below the astronomical
	 * horizon.
	 */
	public static final double TWILIGHT_CIVIL = 6.0 * EphemConstant.DEG_TO_RAD;

	/**
	 * Event ID for calculation of rising and setting times for the local
	 * horizon. In this case, the calculated time will be the time when the
	 * upper edge of the object is at -32.67 arcminutes of geometrical elevation
	 * (0.0 of apparent elevation) below the geometric horizon.
	 * <P>
	 * Note 32.67' is the refraction in the horizon following the Astronomical
	 * Almanac 1986, where an algorithm to correct from geometric to apparent
	 * elevation is given. Since we use that algorithm, this value is kept for
	 * consistency, instead of different values used by other authors, which are
	 * near 34'. In any case, this value depends on the atmospheric conditions,
	 * and the difference is below 10s in the time of the events.
	 * <P>
	 * Optionally, you can pass to the methods a value of 34 arcminutes or any
	 * other value, in radians.
	 */
	public static final double HORIZON_ASTRONOMICAL = (32.67 / 60.0) * EphemConstant.DEG_TO_RAD;

	/**
	 * Constant ID for obtaining the rise time only.
	 */
	public static final int EVENT_RISE = 0;

	/**
	 * Constant ID for obtaining the transit time only.
	 */
	public static final int EVENT_TRANSIT = 1;

	/**
	 * Constant ID for obtaining the set time only.
	 */
	public static final int EVENT_SET = 2;

	/**
	 * Constant ID for obtaining rise, set, and transit times.
	 */
	public static final int EVENT_ALL = 3;

	/**
	 * Compute next instants of rise or set assuming that the body is static.
	 * Results of rise and set fields could be equal to {@linkplain RiseSetTransit#CIRCUMPOLAR} or
	 * {@linkplain RiseSetTransit#ALWAYS_BELOW_HORIZON} in the corresponding case. Calculations are made
	 * taking into account the refraction in the horizon (about 33'), the size
	 * of the body, and the depression of the horizon, but only
	 * for events in the horizon.
	 * 
	 * @param time Time object.
	 * @param obs Observer object.
	 * @param eph Ephemeris object.
	 * @param ephem_obj Ephem object full of data.
	 * @param twilight_event Twilight/horizon event ID EphemConstant. It is the value
	 *        in radians for the altitude of the center of the object where the
	 *        event is to be considered.
	 * @param event Event ID constant for rise, set, transit, or all events.
	 * @return Ephem object.
	 * @ If the date is invalid.
	 */
	public static EphemElement nextRiseSetTransit(TimeElement time, ObserverElement obs, EphemerisElement eph,
			EphemElement ephem_obj, double twilight_event, int event) 
	{
		EphemElement ephem = (EphemElement) ephem_obj.clone();

		// Take into account the angular radius and the depression of the horizon
		// for horizon events
		double tmp = 0.0;
		if (twilight_event == RiseSetTransit.HORIZON_ASTRONOMICAL)
			tmp = tmp - ephem.angularRadius - horizonDepression(obs, eph);

		//double h = Math.sin(obs.latitude) * Math.sin(ephem.declination) + Math.cos(obs.latitude) * Math.cos(ephem.declination) * Math.cos(angh);
		//double alt = Math.asin(h);

		// Compute cosine of hour angle
		tmp = (-twilight_event + tmp - Math.sin(obs.latitude) * Math.sin(ephem.declination)) / (Math.cos(obs.latitude) * Math
				.cos(ephem.declination));

		// Compute local apparent sidereal time and julian day
		double sidereal_time = SiderealTime.apparentSiderealTime(time, obs, eph);
		double jd = TimeScale.getJD(time, obs, eph, TimeElement.Scale.LOCAL_TIME);

		// Make calculations for the meridian
		if (event == EVENT_TRANSIT || event == EVENT_ALL)
		{
			double transit_time = EphemConstant.RAD_TO_DAY * (ephem.rightAscension - sidereal_time);

			// Obtain the next event in time
			transit_time = getNextEvent(transit_time);

			double transit_alt = Math.asin(Math.sin(ephem.declination) * Math.sin(obs.latitude) + Math
					.cos(ephem.declination) * Math.cos(obs.latitude));
			ephem.transit = jd + transit_time;
			ephem.transitElevation = (float) transit_alt;
		}

		// Make calculations for rise and set
		if (tmp > 1.0)
		{
			if (event == EVENT_RISE || event == EVENT_ALL)
			{
				ephem.rise = RiseSetTransit.ALWAYS_BELOW_HORIZON;
			}
			if (event == EVENT_SET || event == EVENT_ALL)
			{
				ephem.set = RiseSetTransit.ALWAYS_BELOW_HORIZON;
			}
		}
		if (tmp < -1.0)
		{
			if (event == EVENT_RISE || event == EVENT_ALL)
			{
				ephem.rise = RiseSetTransit.CIRCUMPOLAR;
			}
			if (event == EVENT_SET || event == EVENT_ALL)
			{
				ephem.set = RiseSetTransit.CIRCUMPOLAR;
			}
		}
		if (Math.abs(tmp) <= 1.0)
		{
			double ang_hor = Math.acos(tmp);
			double rise_time = EphemConstant.RAD_TO_DAY * (ephem.rightAscension - ang_hor - sidereal_time);
			double set_time = EphemConstant.RAD_TO_DAY * (ephem.rightAscension + ang_hor - sidereal_time);

			// Obtain the next event in time
			rise_time = getNextEvent(rise_time);

			// Obtain the next event in time
			set_time = getNextEvent(set_time);

			if (event == EVENT_RISE || event == EVENT_ALL)
			{
				ephem.rise = jd + rise_time;
			}
			if (event == EVENT_SET || event == EVENT_ALL)
			{
				ephem.set = jd + set_time;
			}
		}

		return ephem;
	}

	/**
	 * Compute previous instants of rise or set assuming that the body is static.
	 * Results of rise and set fields could be equal to {@linkplain RiseSetTransit#CIRCUMPOLAR} or
	 * {@linkplain RiseSetTransit#ALWAYS_BELOW_HORIZON} in the corresponding case. Calculations are made
	 * taking into account the refraction in the horizon (about 33'), the size
	 * of the body, and the depression of the horizon, but only
	 * for events in the horizon.
	 * 
	 * @param time Time object.
	 * @param obs Observer object.
	 * @param eph Ephemeris object.
	 * @param ephem_obj Ephem object full of data.
	 * @param twilight_event Twilight/horizon event ID EphemConstant. It is the value
	 *        in radians for the altitude of the center of the object where the
	 *        event is to be considered.
	 * @param event Event ID constant for rise, set, transit, or all events.
	 * @return Ephem object.
	 * @ If the date is invalid.
	 */
	public static EphemElement previousRiseSetTransit(TimeElement time, ObserverElement obs, EphemerisElement eph,
			EphemElement ephem_obj, double twilight_event, int event) 
	{
		EphemElement ephem = (EphemElement) ephem_obj.clone();

		// Take into account the angular radius and the depresion of the horizon
		// for horizon events
		double tmp = 0.0;
		if (twilight_event == RiseSetTransit.HORIZON_ASTRONOMICAL)
			tmp = tmp - ephem.angularRadius - horizonDepression(obs, eph);

		// Compute cosine of hour angle
		tmp = (-twilight_event + tmp - Math.sin(obs.latitude) * Math.sin(ephem.declination)) / (Math.cos(obs.latitude) * Math
				.cos(ephem.declination));

		// Compute local apparent sidereal time and julian day
		double sidereal_time = SiderealTime.apparentSiderealTime(time, obs, eph);
		double jd = TimeScale.getJD(time, obs, eph, TimeElement.Scale.LOCAL_TIME);

		// Make calculations for the meridian
		if (event == EVENT_TRANSIT || event == EVENT_ALL)
		{
			double transit_time = EphemConstant.RAD_TO_DAY * (ephem.rightAscension - sidereal_time);

			// Obtain the last event in time
			transit_time = getLastEvent(transit_time);

			double transit_alt = Math.asin(Math.sin(ephem.declination) * Math.sin(obs.latitude) + Math
					.cos(ephem.declination) * Math.cos(obs.latitude));
			ephem.transit = jd + transit_time;
			ephem.transitElevation = (float) transit_alt;
		}

		// Make calculations for rise and set
		if (tmp > 1.0)
		{
			if (event == EVENT_RISE || event == EVENT_ALL)
			{
				ephem.rise = RiseSetTransit.ALWAYS_BELOW_HORIZON;
			}
			if (event == EVENT_SET || event == EVENT_ALL)
			{
				ephem.set = RiseSetTransit.ALWAYS_BELOW_HORIZON;
			}
		}
		if (tmp < -1.0)
		{
			if (event == EVENT_RISE || event == EVENT_ALL)
			{
				ephem.rise = RiseSetTransit.CIRCUMPOLAR;
			}
			if (event == EVENT_SET || event == EVENT_ALL)
			{
				ephem.set = RiseSetTransit.CIRCUMPOLAR;
			}
		}
		if (Math.abs(tmp) <= 1.0)
		{
			double ang_hor = Math.acos(tmp);
			double rise_time = EphemConstant.RAD_TO_DAY * (ephem.rightAscension - ang_hor - sidereal_time);
			double set_time = EphemConstant.RAD_TO_DAY * (ephem.rightAscension + ang_hor - sidereal_time);

			// Obtain the last event in time
			rise_time = getLastEvent(rise_time);

			// Obtain the last event in time
			set_time = getLastEvent(set_time);

			if (event == EVENT_RISE || event == EVENT_ALL)
			{
				ephem.rise = jd + rise_time;
			}
			if (event == EVENT_SET || event == EVENT_ALL)
			{
				ephem.set = jd + set_time;
			}
		}

		return ephem;
	}

	/**
	 * Compute instants of rise or set assuming that the body is static. Results
	 * of rise and set fields could be equal to {@linkplain RiseSetTransit#CIRCUMPOLAR} or
	 * {@linkplain RiseSetTransit#ALWAYS_BELOW_HORIZON} in the corresponding case. Calculations for
	 * physical twilight are made taking into account the refraction in the horizon
	 * (about 33'), the size of the body, and the depression of
	 * the horizon, but only for events in the horizon.
	 * <P>
	 * This method provides the nearest events in time. Adequate for objects at
	 * high elevation.
	 * 
	 * @param time Time object.
	 * @param obs Observer object.
	 * @param eph Ephemeris object.
	 * @param ephem_obj Ephem object full of data.
	 * @param twilight_event Twilight/horizon event ID EphemConstant. It is the value
	 *        in radians for the altitude of the center of the object where the
	 *        event is to be considered.
	 * @param event Event ID constant for rise, set, transit, or all events.
	 * @return Ephem object.
	 * @ If the date is invalid.
	 */
	public static EphemElement nearestRiseSetTransit(TimeElement time, ObserverElement obs, EphemerisElement eph,
			EphemElement ephem_obj, double twilight_event, int event) 
	{
		EphemElement ephem = (EphemElement) ephem_obj.clone();

		// Take into account the angular radius and the depression of the horizon
		// for horizon events
		double tmp = 0.0;
		if (twilight_event == RiseSetTransit.HORIZON_ASTRONOMICAL)
			tmp = tmp - ephem.angularRadius - horizonDepression(obs, eph);

		// Compute cosine of hour angle
		tmp = (-twilight_event + tmp - Math.sin(obs.latitude) * Math.sin(ephem.declination)) / (Math.cos(obs.latitude) * Math
				.cos(ephem.declination));

		// Compute local apparent sidereal time and julian day
		double sidereal_time = SiderealTime.apparentSiderealTime(time, obs, eph);
		double jd = TimeScale.getJD(time, obs, eph, TimeElement.Scale.LOCAL_TIME);

		// Make calculations for the meridian
		if (event == EVENT_TRANSIT || event == EVENT_ALL)
		{
			double transit_time = EphemConstant.RAD_TO_DAY * (ephem.rightAscension - sidereal_time);
			double transit_alt = Math.asin(Math.sin(ephem.declination) * Math.sin(obs.latitude) + Math
					.cos(ephem.declination) * Math.cos(obs.latitude));

			// Obtain the nearest event in time
			transit_time = getNearestEvent(transit_time);

			ephem.transit = jd + transit_time;
			ephem.transitElevation = (float) transit_alt;
		}

		// Make calculations for rise and set
		if (tmp > 1.0)
		{
			if (event == EVENT_RISE || event == EVENT_ALL)
			{
				ephem.rise = RiseSetTransit.ALWAYS_BELOW_HORIZON;
			}
			if (event == EVENT_SET || event == EVENT_ALL)
			{
				ephem.set = RiseSetTransit.ALWAYS_BELOW_HORIZON;
			}
		}
		if (tmp < -1.0)
		{
			if (event == EVENT_RISE || event == EVENT_ALL)
			{
				ephem.rise = RiseSetTransit.CIRCUMPOLAR;
			}
			if (event == EVENT_SET || event == EVENT_ALL)
			{
				ephem.set = RiseSetTransit.CIRCUMPOLAR;
			}
		}
		if (Math.abs(tmp) <= 1.0)
		{
			double ang_hor = Math.abs(Math.acos(tmp));
			double rise_time = EphemConstant.RAD_TO_DAY * (ephem.rightAscension - ang_hor - sidereal_time);
			double set_time = EphemConstant.RAD_TO_DAY * (ephem.rightAscension + ang_hor - sidereal_time);

			// Obtain the nearest event in time
			rise_time = getNearestEvent(rise_time);

			// Obtain the nearest event in time
			set_time = getNearestEvent(set_time);

			if (event == EVENT_RISE || event == EVENT_ALL)
			{
				ephem.rise = jd + rise_time;
			}
			if (event == EVENT_SET || event == EVENT_ALL)
			{
				ephem.set = jd + set_time;
			}

		}

		return ephem;
	}

	/**
	 * Compute instants of rise or set assuming that the body is static. Results
	 * of rise and set fields could be equal to {@linkplain RiseSetTransit#CIRCUMPOLAR} or
	 * {@linkplain RiseSetTransit#ALWAYS_BELOW_HORIZON} in the corresponding case. Calculations for
	 * physical twilight are made taking into account the refraction in the horizon
	 * (about 33'), the size of the body, and the depression of
	 * the horizon, but only for events in the horizon.
	 * <P>
	 * This method provides the last rise and nearest transit, and the next set
	 * time. Adequate for objects above the horizon.
	 * 
	 * @param time Time object.
	 * @param obs Observer object.
	 * @param eph Ephemeris object.
	 * @param ephem_obj Ephem object full of data.
	 * @param twilight_event Twilight/horizon event ID EphemConstant. It is the value
	 *        in radians for the altitude of the center of the object where the
	 *        event is to be considered.
	 * @param event Event ID constant for rise, set, transit, or all events.
	 * @return Ephem object.
	 * @ If the date is invalid.
	 */
	public static EphemElement currentRiseSetTransit(TimeElement time, ObserverElement obs, EphemerisElement eph,
			EphemElement ephem_obj, double twilight_event, int event) 
	{
		EphemElement ephem = (EphemElement) ephem_obj.clone();

		// Take into account the angular radius and the depression of the horizon
		// for horizon events
		double tmp = 0.0;
		if (twilight_event == RiseSetTransit.HORIZON_ASTRONOMICAL)
			tmp = tmp - ephem.angularRadius - horizonDepression(obs, eph);
		
		// Compute cosine of hour angle
		tmp = (-twilight_event + tmp - Math.sin(obs.latitude) * Math.sin(ephem.declination)) / (Math.cos(obs.latitude) * Math
				.cos(ephem.declination));

		// Compute local apparent sidereal time and Julian day
		double sidereal_time = SiderealTime.apparentSiderealTime(time, obs, eph);
		double jd = TimeScale.getJD(time, obs, eph, TimeElement.Scale.LOCAL_TIME);

		// Make calculations for the meridian
		if (event == EVENT_TRANSIT || event == EVENT_ALL)
		{
			double transit_time = EphemConstant.RAD_TO_DAY * (ephem.rightAscension - sidereal_time);
			double transit_alt = Math.asin(Math.sin(ephem.declination) * Math.sin(obs.latitude) + Math
					.cos(ephem.declination) * Math.cos(obs.latitude));

			// Obtain the nearest event in time
			transit_time = getNearestEvent(transit_time);

			ephem.transit = jd + transit_time;
			ephem.transitElevation = (float) transit_alt;
		}

		// Make calculations for rise and set
		if (tmp > 1.0)
		{
			if (event == EVENT_RISE || event == EVENT_ALL)
			{
				ephem.rise = RiseSetTransit.ALWAYS_BELOW_HORIZON;
			}
			if (event == EVENT_SET || event == EVENT_ALL)
			{
				ephem.set = RiseSetTransit.ALWAYS_BELOW_HORIZON;
			}
		}
		if (tmp < -1.0)
		{
			if (event == EVENT_RISE || event == EVENT_ALL)
			{
				ephem.rise = RiseSetTransit.CIRCUMPOLAR;
			}
			if (event == EVENT_SET || event == EVENT_ALL)
			{
				ephem.set = RiseSetTransit.CIRCUMPOLAR;
			}
		}
		if (Math.abs(tmp) <= 1.0)
		{
			double ang_hor = Math.abs(Math.acos(tmp));
			double rise_time = EphemConstant.RAD_TO_DAY * (ephem.rightAscension - ang_hor - sidereal_time);
			double set_time = EphemConstant.RAD_TO_DAY * (ephem.rightAscension + ang_hor - sidereal_time);

			// Obtain the last event in time
			rise_time = getLastEvent(rise_time);

			// Obtain the next event in time
			set_time = getNextEvent(set_time);

			if (event == EVENT_RISE || event == EVENT_ALL)
			{
				ephem.rise = jd + rise_time;
			}
			if (event == EVENT_SET || event == EVENT_ALL)
			{
				ephem.set = jd + set_time;
			}

		}

		return ephem;
	}

	private static double getLastEvent(double time_event)
	{
		// Obtain the last event in time
		time_event = EphemUtils.module(time_event, 1.0);
		if (time_event > 0.0)
			time_event -= 1.0;
		if (time_event < -1.0)
			time_event += 1.0;
		time_event = time_event / EphemConstant.SIDEREAL_DAY_LENGTH;

		return time_event;
	}

	private static double getNextEvent(double time_event)
	{
		// Obtain the next event in time
		time_event = EphemUtils.module(time_event, 1.0);
		if (time_event < 0.0)
			time_event += 1.0;
		if (time_event > 1.0)
			time_event -= 1.0;
		time_event = time_event / EphemConstant.SIDEREAL_DAY_LENGTH;

		return time_event;
	}

	private static double getNearestEvent(double time_event)
	{
		// Obtain the nearest event in time
		time_event = EphemUtils.module(time_event, 1.0);
		if (time_event > 0.5)
			time_event -= 1.0;
		if (time_event < -0.5)
			time_event += 1.0;
		time_event = time_event / EphemConstant.SIDEREAL_DAY_LENGTH;

		return time_event;
	}

	/**
	 * Gets the angle of depression of the horizon. An object will be just in
	 * the geometric horizon when it's elevation is equal to minus this value.
	 * This correction can modify the time of the events by some minutes.
	 * 
	 * @param obs Observer object.
	 * @param eph Ephemeris object.	 * 
	 * @return The angle in radians.
	 */
	public static double horizonDepression(ObserverElement obs, EphemerisElement eph) 
	{
		ReferenceEllipsoid.Method ellipsoid = ReferenceEllipsoid.getEllipsoid(eph);
		double rho = ReferenceEllipsoid.getEarthRadius(ellipsoid) * (0.99833 + 0.00167 * Math.cos(2.0 * obs.latitude));
		double depresion = Math.acos(Math.sqrt(rho / (rho + (double) obs.height / 1000.0)));

		return depresion;
	}

	/**
	 * Provides rise, set, and transit times correcting for the movement of the
	 * object. For objects above the horizon, the result is refered to the
	 * current day, otherwise they will be the next rise, set, and transit
	 * events.
	 * 
	 * @param time Time object containing the date.
	 * @param obs Observer object containing the observer position.
	 * @param eph Ephemeris object defining the ephemeris properties.
	 * @param ephem_elem Ephem object with the position of the source.
	 * @param twilight_event Twilight/horizon event ID to calculate.
	 * @param Ephem improves position if object is mooving, may be null	 * 
	 * @return Ephem object containing full ephemeris data.
	 */
	public static EphemElement obtainCurrentOrNextRiseSetTransit(TimeElement time, // Time Element
			ObserverElement obs, // Observer Element
			EphemerisElement eph, // Ephemeris Element
			EphemElement ephem_elem, double twilight_event,
			Ephem ephem) 
	{
		// Define both type of events, depending on the elevation of the object
		final int next_events = 0;
		final int current_events = 1;

		// Establish the adequate type
		int when = current_events;
		if (ephem_elem.elevation < 0.0)
			when = next_events;

		return RiseSetTransit.obtainCertainRiseSetTransit(time, obs, eph, ephem_elem, twilight_event, when, ephem);
	}

	/**
	 * Provides next rise, set, and transit times correcting for the movement of
	 * the object.
	 * 
	 * @param time Time object containing the date.
	 * @param obs Observer object containing the observer position.
	 * @param eph Ephemeris object defining the ephemeris properties.
	 * @param ephem_elem Ephem object with the position of the source.
	 * @param twilight_event Twilight/horizon event ID to calculate.
	 * @param Ephem improves position if object is mooving, may be null	 * 
	 * @return Ephem object containing full ephemeris data.
	 */
	public static EphemElement obtainNextRiseSetTransit(TimeElement time, // Time
																				// Element
			ObserverElement obs, // Observer Element
			EphemerisElement eph, // Ephemeris Element
			EphemElement ephem_elem, double twilight_event, Ephem ephem) 
	{
		// Establish the adequate type
		int when = 0;

		return RiseSetTransit.obtainCertainRiseSetTransit(time, obs, eph, ephem_elem, twilight_event, when,ephem);
	}

	/**
	 * Provides current (refered to the actual day) rise, set, and transit times
	 * correcting for the movement of the object.
	 * 
	 * @param time Time object containing the date.
	 * @param obs Observer object containing the observer position.
	 * @param eph Ephemeris object defining the ephemeris properties.
	 * @param ephem_elem Ephem object with the position of the source.
	 * @param twilight_event Twilight/horizon event ID to calculate.
	 * @param Ephem improves position if object is mooving, may be null	 * 
	 * @return Ephem object containing full ephemeris data.

	 */
	public static EphemElement obtainCurrentRiseSetTransit(TimeElement time, // Time
																					// Element
			ObserverElement obs, // Observer Element
			EphemerisElement eph, // Ephemeris Element
			EphemElement ephem_elem, double twilight_event,
			Ephem ephem) 
	{
		// Establish the adequate type
		int when = 1;

		return RiseSetTransit.obtainCertainRiseSetTransit(time, obs, eph, ephem_elem, twilight_event, when,ephem);
	}

	/**
	 * Provides previous rise, set, and transit times correcting for the
	 * movement of the object.
	 * 
	 * @param time Time object containing the date.
	 * @param obs Observer object containing the observer position.
	 * @param eph Ephemeris object defining the ephemeris properties.
	 * @param ephem_elem Ephem object with the position of the source.
	 * @param twilight_event Twilight/horizon event ID to calculate.
	 * @return Ephem object containing full ephemeris data.
	 */
	public static EphemElement obtainPreviousRiseSetTransit(TimeElement time, // Time
																					// Element
			ObserverElement obs, // Observer Element
			EphemerisElement eph, // Ephemeris Element
			EphemElement ephem_elem, double twilight_event,
			Ephem ephem) 
	{
		// Establish the adequate type
		int when = 2;

		return RiseSetTransit.obtainCertainRiseSetTransit(time, obs, eph, ephem_elem, twilight_event, when, ephem);
	}

	/**
	 * Provides nearest rise, set, and transit times correcting for the movement
	 * of the object.
	 * 
	 * @param time Time object containing the date.
	 * @param obs Observer object containing the observer position.
	 * @param eph Ephemeris object defining the ephemeris properties.
	 * @param ephem_elem Ephem object with the position of the source.
	 * @param twilight_event Twilight/horizon event ID to calculate.
	 * @param Ephem improves position if object is mooving, may be null
	 * @return Ephem object containing full ephemeris data.

	 */
	public static EphemElement obtainNearestRiseSetTransit(TimeElement time, // Time
																					// Element
			ObserverElement obs, // Observer Element
			EphemerisElement eph, // Ephemeris Element
			EphemElement ephem_elem, double twilight_event,
			Ephem ephem) 
	{
		// Establish the adequate type
		int when = 3;

		return RiseSetTransit.obtainCertainRiseSetTransit(time, obs, eph, ephem_elem, twilight_event, when, ephem);
	}

	/**
	 * ID constant for next events in {@linkplain RiseSetTransit#obtainCertainRiseSetTransit(TimeElement, ObserverElement, EphemerisElement, EphemElement, double, int)}.
	 */
	public static final int OBTAIN_NEXT_EVENTS = 0;
	/**
	 * ID constant for current events in {@linkplain RiseSetTransit#obtainCertainRiseSetTransit(TimeElement, ObserverElement, EphemerisElement, EphemElement, double, int)}.
	 */
	public static final int OBTAIN_CURRENT_EVENTS = 1;
	/**
	 * ID constant for previous events in {@linkplain RiseSetTransit#obtainCertainRiseSetTransit(TimeElement, ObserverElement, EphemerisElement, EphemElement, double, int)}.
	 */
	public static final int OBTAIN_PREVIOUS_EVENTS = 2;
	/**
	 * ID constant for nearest events in {@linkplain RiseSetTransit#obtainCertainRiseSetTransit(TimeElement, ObserverElement, EphemerisElement, EphemElement, double, int)}.
	 */
	public static final int OBTAIN_NEAREST_EVENTS = 3;
	
	/**
	 * Provides any rise, set, and transit times correcting for the movement
	 * of the object.
	 * 
	 * @param time Time object containing the date.
	 * @param obs Observer object containing the observer position.
	 * @param eph Ephemeris object defining the ephemeris properties.
	 * @param ephem_obj Ephem object with the position of the source.
	 * @param twilight_event Twilight/horizon event ID to calculate.
	 * @param how Set what kind of calculations will be made.
	 * @param Ephem improves position if object is mooving, may be null
	 * @return Ephem object containing full ephemeris data.
	 */
	private static EphemElement obtainCertainRiseSetTransit(TimeElement time, // Time
																			// Element
			ObserverElement obs, // Observer Element
			EphemerisElement eph, // Ephemeris Element
			EphemElement ephem_obj, double twilight_event, int how,
			Ephem ephem) 
	{
		EphemElement ephem_elem = (EphemElement) ephem_obj.clone();

		// Define both type of events, depending on the elevation of the object
		final int next_events = 0;
		final int current_events = 1;
		final int previous_events = 2;
		final int nearest_events = 3;

		// Initial set up
		int not_yet_calculated = -1;
		ephem_elem.rise = not_yet_calculated;
		ephem_elem.set = not_yet_calculated;
		ephem_elem.transit = not_yet_calculated;
		double last_time_event = not_yet_calculated;
		double time_event = not_yet_calculated;
		double dt = not_yet_calculated;

		// Declare new TimeElement
		TimeElement new_time = new TimeElement(time.calendar, time.timeScale);

		// Create new EphemerisElement with adequate input values. Force
		// apparent, equinox of date,
		// and topocentric values
		EphemerisElement eph_new = (EphemerisElement) eph.clone();
		eph_new.ephemType = EphemerisElement.Ephem.APPARENT;
		eph_new.equinox = EphemerisElement.EQUINOX_OF_DATE;
		eph_new.isTopocentric = EphemerisElement.TOPOCENTRIC;

		// Obtain event to better than 0.5 seconds of precission
		double precission_in_seconds = 0.5;

		// Return no data for geocentric position
		if (!eph_new.isTopocentric)
			return ephem_elem;

		// Calculate time of events
		for (int i = EVENT_RISE; i <= EVENT_SET; i++)
		{
			// Declare new Ephem object to work with it in the
			// calculation process
			EphemElement new_ephem_elem = new EphemElement();
			new_ephem_elem.rightAscension = ephem_elem.rightAscension;
			new_ephem_elem.declination = ephem_elem.declination;
			new_ephem_elem.angularRadius = ephem_elem.angularRadius;

			// Set maximum iterations to 10. Enough for general use.
			int n_iter = 0;
			int n_iter_max = 10;
			last_time_event = not_yet_calculated;
			dt = not_yet_calculated;
			do
			{
				n_iter++;
				switch (how)
				{
				case next_events: // If the object is initially below the
									// horizon
					new_ephem_elem = RiseSetTransit.nextRiseSetTransit(time, obs, eph_new, new_ephem_elem,
							twilight_event, i);
					break;
				case current_events: // If the object is initially above the
										// horizon
					new_ephem_elem = RiseSetTransit.currentRiseSetTransit(time, obs, eph_new, new_ephem_elem,
							twilight_event, i);
					break;
				case previous_events:
					new_ephem_elem = RiseSetTransit.previousRiseSetTransit(time, obs, eph_new, new_ephem_elem,
							twilight_event, i);
					break;
				case nearest_events:
					new_ephem_elem = RiseSetTransit.nearestRiseSetTransit(time, obs, eph_new, new_ephem_elem,
							twilight_event, i);
					break;
				}

				// Get time of the event
				time_event = new_ephem_elem.rise;
				if (i == EVENT_TRANSIT)
					time_event = new_ephem_elem.transit;
				if (i == EVENT_SET)
					time_event = new_ephem_elem.set;

				// Get elapsed time since last calculation
				dt = time_event - last_time_event;

				// Set elapsed time to zero if the object cannot be observed
				if (time_event == RiseSetTransit.ALWAYS_BELOW_HORIZON || time_event == RiseSetTransit.CIRCUMPOLAR)
					dt = 0.0;

				// If elapsed time is greater than the desired precission,
				// update time and
				// calculate ephemeris for the new time
				if (Math.abs(dt) > (precission_in_seconds / EphemConstant.SECONDS_PER_DAY) && n_iter < n_iter_max)
				{
					last_time_event = time_event;
					AstroDate new_astro = new AstroDate(time_event);
					new_time = new TimeElement(new_astro.toGCalendar(), TimeElement.Scale.LOCAL_TIME);
					if(ephem !=null)
						new_ephem_elem = ephem.getEphemeris(new_time, obs, eph_new, false);
				}

			} while (Math.abs(dt) > (precission_in_seconds / EphemConstant.SECONDS_PER_DAY) && n_iter < n_iter_max);

			// Set time of event in our output Ephem object
			if (i == EVENT_RISE)
				ephem_elem.rise = new_ephem_elem.rise;
			if (i == EVENT_TRANSIT)
				ephem_elem.transit = new_ephem_elem.transit;
			if (i == EVENT_TRANSIT)
				ephem_elem.transitElevation = new_ephem_elem.transitElevation;
			if (i == EVENT_SET)
				ephem_elem.set = new_ephem_elem.set;

		}
		return ephem_elem;
	}

}
