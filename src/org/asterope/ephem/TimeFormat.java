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

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;


/**
 * A simple set of date and time formatters. This once trivial task got a bit
 * complicated after the addition of i18n support.
 * 
 * @author M. Huss
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class TimeFormat
{
	/**
	 * Return 'now' as a date <TT>String</TT> using the default Locale.
	 * 
	 * @param dateFmt The java.text.DateFormat constant to use (<TT>FULL</TT>,
	 *        <TT>LONG</TT>, <TT>MEDIUM</TT>, or <TT>SHORT</TT>).
	 * @return The current, formatted, date as a String.
	 */
	public static String dateNow(int dateFmt)
	{
		return DateFormat.getDateInstance(dateFmt).format(new Date());
	}

	/**
	 * Return 'now' as a date <TT>String</TT> using the default Locale and the
	 * <TT>DateFormat.MEDIUM</TT> size.
	 * 
	 * @return The current formatted date as a <TT>String</TT>.
	 */
	public static String dateNow()
	{
		return dateNow(DateFormat.MEDIUM);
	}

	/**
	 * Return 'now' as a date and time <TT>String</TT> using the default
	 * Locale and the <TT>DateFormat.MEDIUM</TT> size.
	 * 
	 * @return The current, formatted, date and time as a String.
	 */
	public static String dateTimeNow()
	{
		return DateFormat.getInstance().format(new Date());
	}

	/**
	 * Convert a <TT>Date</TT> into a date <TT>String</TT> using the default
	 * Locale.
	 * 
	 * @param d The java.util.Date to convert.
	 * @param dateFmt The <TT>java.text.DateFormat</TT> constant to use (<TT>FULL</TT>,
	 *        <TT>LONG</TT>, <TT>MEDIUM</TT>, or <TT>SHORT</TT>).
	 * @return The date formatted as a String.
	 */
	public static String date(Date d, int dateFmt)
	{
		return DateFormat.getDateInstance(dateFmt).format(d);
	}

	/**
	 * Convert a <TT>Date</TT> into a date <TT>String</TT> using the default
	 * Locale and the <TT>DateFormat.MEDIUM</TT> size.
	 * 
	 * @param d The <TT>java.util.Date</TT> to convert.
	 * @return The date formatted as a <TT>String</TT>.
	 */
	public static String date(Date d)
	{
		return date(d, DateFormat.MEDIUM);
	}

	/**
	 * Convert a <TT>Date</TT> into a time <TT>String</TT> using the default
	 * Locale.
	 * 
	 * @param d The <TT>java.util.Date</TT> to convert.
	 * @param dateFmt The <TT>java.text.DateFormat</TT> constant to use (<TT>FULL</TT>,
	 *        <TT>LONG</TT>, <TT>MEDIUM</TT>, or <TT>SHORT</TT>).
	 * @return The time formatted as a <TT>String</TT>.
	 */
	public static String time(Date d, int dateFmt)
	{
		return DateFormat.getTimeInstance(dateFmt).format(d);
	}

	/**
	 * Convert a <TT>Date</TT> into a time <TT>String</TT> using the default
	 * Locale and the <TT>DateFormat.MEDIUM</TT> size.
	 * 
	 * @param d The <TT>java.util.Date</TT> to convert.
	 * @return The time formatted as a <TT>String</TT>.
	 */
	public static String time(Date d)
	{
		return time(d, DateFormat.MEDIUM);
	}

	/**
	 * Convert a <TT>Date</TT> into a date and time <TT>String</TT> using
	 * the default Locale.
	 * 
	 * @param d The <TT>java.util.Date</TT> to convert.
	 * @param dtFmt The <TT>java.text.DateFormat</TT> constant to use (<TT>FULL</TT>,
	 *        <TT>LONG</TT>, <TT>MEDIUM</TT>, or <TT>SHORT</TT>).
	 * @return The date and time formatted as a <TT>String</TT>.
	 */
	public static String dateTime(Date d, int dtFmt)
	{
		return DateFormat.getDateTimeInstance(dtFmt, dtFmt).format(d);
	}

	/**
	 * Convert a <TT>Date</TT> into a date and time <TT>String</TT> using
	 * the default Locale and the <TT>DateFormat.MEDIUM</TT> size.
	 * 
	 * @param d The <TT>java.util.Date</TT> to convert.
	 * @return The date and time formatted as a <TT>String</TT>.
	 */
	public static String dateTime(Date d)
	{
		return dateTime(d, DateFormat.MEDIUM);
	}

	/**
	 * Convert a <TT>Calendar</TT> into a date and time <TT>String</TT>
	 * using the default Locale and the <TT>DateFormat.MEDIUM</TT> size.
	 * 
	 * @param c The <TT>java.util.Calendar</TT> to convert.
	 * @return The date and time formatted as a <TT>String</TT>.
	 */
	public static String dateTime(Calendar c)
	{
		String date = dateTime(c.getTime(), DateFormat.MEDIUM);
		if (c.get(GregorianCalendar.ERA) == GregorianCalendar.BC)
			date += " (B.C.)";
		return date;
	}

	/**
	 * Converts a julian day into a date and time <TT>String</TT> using the
	 * default Locale and the <TT>DateFormat.MEDIUM</TT> size.
	 * 
	 * @param jd The julian day.
	 * @return The date and time formatted as a <TT>String</TT>.
	 * @throws JPARSECException If the Julian day is invalid.
	 */
	public static String formatJulianDayAsDateAndTime(double jd)
	{
		AstroDate astro = new AstroDate(jd);
		return dateTime(astro.toGCalendar());
	}

	/**
	 * Converts a julian day into a date <TT>String</TT> using the default
	 * Locale and the <TT>DateFormat.MEDIUM</TT> size.
	 * 
	 * @param jd The julian day.
	 * @return The date formatted as a <TT>String</TT>.
	 * @throws JPARSECException If the Julian day is invalid.
	 */
	public static String formatJulianDayAsDate(double jd)
	{
		AstroDate astro = new AstroDate(jd);
		return date(astro.toGCalendar().getTime());
	}

	/**
	 * Converts a {@linkplain TimeElement} object into a date and time <TT>String</TT>
	 * using the default Locale and the <TT>DateFormat.MEDIUM</TT> size.
	 * 
	 * @param time {@linkplain TimeElement} object.
	 * @return The date and time formatted as a <TT>String</TT>.
	 */
	public static String formatTime(TimeElement time)
	{
		String date = dateTime(time.calendar);
		if (time.calendar.get(GregorianCalendar.ERA) == GregorianCalendar.BC)
			date += " (B.C.)";
		switch (time.timeScale)
		{
		case LOCAL_TIME:
			date += " LT";
			break;
		case UNIVERSAL_TIME_UT1:
			date += " UT";
			break;
		case UNIVERSAL_TIME_UTC:
			date += " UTC";
			break;
		case TERRESTRIAL_TIME:
			date += " TT";
			break;
		case BARYCENTRIC_DYNAMICAL_TIME:
			date += " TDB";
			break;
		}
		return date;
	}
}
