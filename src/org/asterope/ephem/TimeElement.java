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
import java.util.GregorianCalendar;


/**
 * An adequate class for storing the time of an observer.
 * <P>
 * <I><B>Description</B></I>
 * <P>
 * This library uses GregorianCalendar for dates equal or after October,
 * 15, 1582. For dates before that the library uses the Julian Calendar. The
 * Before Christ era (B.C.) is automatically selected by setting a null or
 * negative year as the year in the GregorianCalendar instance. Year 0
 * corresponds to year 1 B.C. (year 0 does not exist by itself). When using an
 * instance of {@linkplain AstroDate} occurs the same. The only difference is that here month
 * '1' is January. In GregorianCalendar January is month '0'. AstroDate instance
 * is recommended to avoid confussions.
 * <P>
 * The time can be Barycentric Dynamical Time, Terrestrial Time, Universal Time 
 * UT1/UTC, or Local Time. In the last
 * case the time will be corrected when making any kind of calculations using
 * the time zone field in the corresponding observer element instance.
 * <P>
 *
 * @see java.util.GregorianCalendar
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class TimeElement implements Serializable
{	

	private static final long serialVersionUID = 1232930001674655790L;

	/**
	 * Creates a default time object with the current computer's date
	 * and time scale set to local time.
	 */
	public TimeElement()
	{
		calendar = new GregorianCalendar();
		timeScale = Scale.LOCAL_TIME;
	}

	/**
	 * Creates a time object by giving the values of the fields.
	 * 
	 * @param obs_date Gregorian Calendar object with the date.
	 * @param ts Time scale ID constant.
	 */
	public TimeElement(GregorianCalendar obs_date, Scale ts)
	{
		calendar = obs_date;
		timeScale = ts;
	}

	/**
	 * Creates a time object from an {@linkplain AstroDate} instance.
	 * 
	 * @param astro {@linkplain AstroDate} instance.
	 * @param ts Time scale ID constant.
	 */
	public TimeElement(AstroDate astro, Scale ts)
	{
		calendar = astro.toGCalendar();
		timeScale = ts;
	}

	/**
	 * Creates a time object from a Julian day.
	 * 
	 * @param jd Julian day.
	 * @param ts Time scale ID constant.
	 */
	public TimeElement(double jd, Scale ts)
	{
		AstroDate astro = new AstroDate(jd);
		calendar = astro.toGCalendar();
		timeScale = ts;
	}

	/**
	 * A GregorianCalendar object for storing the date.
	 */
	public GregorianCalendar calendar;
	


	/**
	 * Time scale that defines the date object.
	 */
	public Scale timeScale;

	
	public enum Scale{
	/**
	 * Local Time (LT) ID constant for the time scale.
	 */
	LOCAL_TIME,

	/**
	 * Universal Time (UT1) ID constant for the time scale. UTC is the common
	 * scale given in hour signals, and differs from UT1 by as much as one
	 * second. UT1 is the scale commonly used in astronomical calculations.
	 * Correction from UTC-UT1 difference is applied when necessary, depending
	 * on the last time the file IERS_EOP.txt was updated. If there is no
	 * information available, then it will be supposed to be equal: UT1 = UTC.
	 * This happends before 1960 and always also in the future.
	 */
	UNIVERSAL_TIME_UT1,

	/**
	 * Universal Time Coordinate ID constant for the time scale. UTC is the
	 * common scale given in hour signals, and differs from UT1 by as much as
	 * one second. Correction from UTC-UT1 difference is applied, depending on
	 * the last time the file IERS_EOP.txt was updated. If there is no
	 * information available, then it will be supposed to be equal: UT1 = UTC.
	 */
	UNIVERSAL_TIME_UTC,

	/**
	 * Terrestrial Time (TT) ID constant for the time scale.
	 */
	TERRESTRIAL_TIME,

	/**
	 * Barycentric dynamical time (TDB) constant for the time scale.
	 */
	BARYCENTRIC_DYNAMICAL_TIME
	}
	/**
	 * Clones this instance.
	 */
	public Object clone()
	{
		long t = this.calendar.getTimeInMillis();
		GregorianCalendar g = new GregorianCalendar();
		g.setTimeInMillis(t);
		TimeElement time = new TimeElement(g, this.timeScale);
		return time;
	}
	/**
	 * Returns wether the input object is equals to this instance.
	 */
	public boolean equals(Object t)
	{
		if (t == null) {
			return false;
		}
		TimeElement te = (TimeElement) t;
		boolean equals = true;
		if (te.timeScale != this.timeScale) equals = false;
		if (te.calendar.getTimeInMillis() != this.calendar.getTimeInMillis()) equals = false;
		return equals;
	}
	

}

// end of class TimeElement
