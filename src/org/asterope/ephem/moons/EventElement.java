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


import org.asterope.ephem.*;

import java.io.Serializable;

/**
 * Manages data related to satellite events.
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class EventElement implements Serializable {
	static final long serialVersionUID = 1L;

	/**
	 * Holds the start time of the events as a Julian day in TDB.
	 */
	public double startTime;
	/**
	 * Holds the end time of the events as a Julian day in TDB.
	 */
	public double endTime;
	/**
	 * Main body id constant.
	 */
	public Target mainBody;
	/**
	 * Secondary body id constant, it anyone exists.
	 */
	public Target secondaryBody;
	/**
	 * Event id code. Constants defined in this class.
	 */
	public int eventType;
	/**
	 * Holds details on the event. For Moon events for example,
	 * it contains a number with the percentage of eclipse/occultation.
	 */
	public String details;
	/**
	 * Subevent id constant. Only takes sense on mutual events with the
	 * mother planet, while a satellite is partially eclipsed or occulted. In this case
	 * the event marks the beggining or the ending of the period when the
	 * satellite is fully eclipsed or occulted. Constants defined in this class.
	 */
	public int subType;
	/**
	 * True for a visible event, false otherwise. Note a given event
	 * must be visible from Earth and with positive elevation to be
	 * observable.
	 */
	public boolean visibleFromEarth;
	/**
	 * Elevation of the source in radians. Only available for apparent and topocentric
	 * calculations.
	 */
	public double elevation;
	
	/**
	 * ID code for an eclipse event.
	 */
	public static final int EVENT_ECLIPSED = 0;
	/**
	 * Id code for an occultation event.
	 */
	public static final int EVENT_OCCULTED = 1;
	/**
	 * Id code for a transit event.
	 */
	public static final int EVENT_TRANSIT = 2;
	/**
	 * Id code for a shadow transit event.
	 */
	public static final int EVENT_SHADOW_TRANSIT = 3;

	/**
	 * Id code for an event that begins.
	 */
	public static final int SUBEVENT_START = 1;
	/**
	 * Id code for an event that ends.
	 */
	public static final int SUBEVENT_END = 2;
	/**
	 * Id code for an event that begins and ends.
	 */
	public static final int SUBEVENT_NONE = 0;

	/**
	 * Holds a description of the events.
	 */
	public static final String[] EVENTS = new String[] {"Eclipsed", "Occulted", "Transiting",
		"Shadow transiting"};
	
	/**
	 * Empty constructor.
	 */
	public EventElement()
	{}
	/**
	 * Constructor for a simple event.
	 * @param jd Event time.
	 * @param main Main object involved.
	 * @param event Event id constant.
	 * @param details Details.
	 */
	public EventElement(double jd, Target main, int event, String details)
	{
		this.startTime = jd;
		this.endTime = jd;
		this.mainBody = main;
		this.secondaryBody = main;
		this.eventType = event;
		this.details = details;
		this.visibleFromEarth = true;
		this.subType = EventElement.SUBEVENT_NONE;
	}
	/**
	 * Full constructor.
	 * @param jdi initial time.
	 * @param jdf Final time.
	 * @param main Main object involved.
	 * @param secondary Secondary object involved.
	 * @param event Event id constant.
	 * @param details Details.
	 */
	public EventElement(double jdi, double jdf, Target main, Target secondary, int event, String details)
	{
		this.startTime = jdi;
		this.endTime = jdf;
		this.mainBody = main;
		this.secondaryBody = secondary;
		this.eventType = event;
		this.details = details;
		this.visibleFromEarth = true;
		this.subType = EventElement.SUBEVENT_NONE;
	}
	
	/**
	 * Transforms the event time into another time scale.
	 * @param obs Observer object.
	 * @param eph Ephemeris object.
	 * @param start True to transform start time, false for end time.
	 * @param timeScale Ouput time scale id constant.
	 * @return The output time.
	 * @throws JPARSECException If an error occurs.
	 */
	public double getEventTime(ObserverElement obs, EphemerisElement eph, boolean start, TimeElement.Scale timeScale)	{
		TimeElement time = new TimeElement(this.startTime, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
		if (!start) time = new TimeElement(this.endTime, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
		
		double out = TimeScale.getJD(time, obs, eph, timeScale);
		return out;
	}
	
	/**
	 * Reports the current event to the console.
	 * @throws JPARSECException If an error occurs.
	 */
	public void report(){
		System.out.println(this.mainBody.name()+" "+EventElement.EVENTS[this.eventType]+" by "+
				this.secondaryBody.name()+" between "+this.startTime+" and "+this.endTime+" (TDB). Details: "+this.details+".");
	}
	/**
	 * Clones this instance.
	 */
	public Object clone()
	{
		EventElement e = new EventElement(this.startTime, this.endTime, this.mainBody, this.secondaryBody,
				this.eventType, this.details);
		e.elevation = this.elevation;
		e.visibleFromEarth = this.visibleFromEarth;
		e.subType = this.subType;
		return e;
	}
	/**
	 * Returns wether the input Object contains the same information
	 * as this instance.
	 */
	public boolean equals(Object e)
	{
		if (e == null) {
			return false;
		}
		boolean equals = true;
		EventElement ee = (EventElement) e;
		if (!ee.details.equals(this.details)) equals = false;
		if (ee.endTime != this.endTime) equals = false;
		if (ee.elevation != this.elevation) equals = false;
		if (ee.startTime != this.startTime) equals = false;
		if (ee.mainBody != this.mainBody) equals = false;
		if (ee.eventType != this.eventType) equals = false;
		if (ee.secondaryBody != this.secondaryBody) equals = false;
		if (ee.visibleFromEarth != this.visibleFromEarth) equals = false;
		if (ee.subType != this.subType) equals = false;
		return equals;
	}
}
