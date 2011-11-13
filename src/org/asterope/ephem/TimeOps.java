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

import java.util.Calendar;
import java.util.GregorianCalendar;


/**
 * TimeOps contains miscellaneous time-related functions.
 * <P>
 * Main functions are related to Daylight Saving Time. These methods works using
 * the current system settings, so its depends on the computer.
 * 
 * @author M. Huss
 * @version 1.0
 */
public class TimeOps
{
	/**
	 * Calculate the current Daylight Time offset ( 0 or -1 ). <BR>
	 * Add the result of this function to the current time to adjust.
	 * 
	 * @param cal A <TT>java.util.Calendar</TT> object which is used to get
	 *        the <TT>DST_OFFSET</TT> from (e.g., <TT>java.util.GregorianCalendar</TT>).
	 * @return <TT>DST_OFFSET</TT> in hours if Daylight time is in effect, 0
	 *         otherwise.
	 */
	public static int dstOffset(Calendar cal)
	{
		return (int) (cal.get(Calendar.DST_OFFSET) / EphemConstant.MILLISECONDS_PER_HOUR);
	}

	/**
	 * Calculate the current Daylight Time offset. <BR>
	 * Add the result of this function to the current time to adjust.<BR>
	 * This function uses a <TT>GregorianCalendar</TT> object.
	 * 
	 * @return <TT>DST_OFFSET</TT> in hours if Daylight time is in effect, 0
	 *         otherwise.
	 */
	public static int dstOffset()
	{
		return dstOffset(new GregorianCalendar());
	}

	/**
	 * Calculate the current Daylight Time offset in fractional days. <BR>
	 * Add the result of this function to the current time to adjust.
	 * 
	 * @param cal A <TT>java.util.Calendar</TT> object which is used to get
	 *        the <TT>DST_OFFSET</TT>.
	 * @return <TT>DST_OFFSET</TT> in days if Daylight time is in effect, 0
	 *         otherwise.
	 */
	public static double dstOffsetInDays(Calendar cal)
	{
		return (double) dstOffset(cal) * EphemConstant.DAYS_PER_HOUR;
	}

	/**
	 * Calculate the current Daylight Time offset in fractional days. <BR>
	 * Add the result of this function to the current time to adjust.<BR>
	 * This function uses a <TT>GregorianCalendar</TT> object.
	 * 
	 * @return <TT>DST_OFFSET</TT> in days if Daylight time is in effect, 0
	 *         otherwise.
	 */
	public static double dstOffsetInDays()
	{
		return (double) dstOffset() * EphemConstant.DAYS_PER_HOUR;
	}

	/**
	 * Determine the absolute time zone offset from UTC in hours (-12 to +12)
	 * for the spec'd Calendar.
	 * 
	 * @param cal The Calendar to use.
	 * @return The offset in hours.
	 */
	public static int tzOffset(Calendar cal)
	{
		return (int) (cal.get(Calendar.ZONE_OFFSET) / EphemConstant.MILLISECONDS_PER_HOUR);
	}

	/**
	 * Determine the absolute time zone offset from UTC in hours (-12 to +12)
	 * using the local timezone.
	 * 
	 * @return The offset in hours.
	 */
	public static int tzOffset()
	{
		return tzOffset(new GregorianCalendar());
	}

	/**
	 * Determine the absolute time zone offset from UTC in fractional days (-0.5
	 * to +0.5).
	 * 
	 * @param cal The Calendar to use.
	 * @return The offset in decimal day.
	 */
	public static double tzOffsetInDays(Calendar cal)
	{
		return (double) tzOffset(cal) * EphemConstant.DAYS_PER_HOUR;
	}

	/**
	 * Determine the absolute time zone offset from UTC in fractional days (-0.5
	 * to +0.5).
	 * 
	 * @return The offset in decimal day.
	 */
	public static double tzOffsetInDays()
	{
		return (double) tzOffset() * EphemConstant.DAYS_PER_HOUR;
	}

	/**
	 * Format a time as a <TT>String</TT> using the format <TT>HH:MM</TT>.
	 * <BR>
	 * The returned string will be "--:--" if the time is invalid.
	 * 
	 * @param t The time to format.
	 * @return The formatted String.
	 */
	public static String formatTime(double t)
	{
		String ft = "--:--";

		if (t >= 0D)
		{
			// round up to nearest minute
			int minutes = (int) (t * EphemConstant.HOURS_PER_DAY * EphemConstant.MINUTES_PER_HOUR + EphemConstant.ROUND_UP);
			ft = twoDigits(minutes / (int) EphemConstant.MINUTES_PER_HOUR) + ":" + twoDigits(minutes % (int) EphemConstant.MINUTES_PER_HOUR);
		}
		return ft;
	}

	/**
	 * Returns a string version of two digit number, with leading zero if needed
	 * The input is expected to be in the range 0 to 99.
	 * @param i The value.
	 * @return The string representation with two digits.
	 */
	public static String twoDigits(double i)
	{
		return (i >= 10) ? "" + i : "0" + i;
	}
	/**
	 * Returns a string version of two digit number, with leading zero if needed
	 * The input is expected to be in the range 0 to 99.
	 * @param i The value.
	 * @return The string representation with two digits.
	 */
	public static String twoDigits(int i)
	{
		return (i >= 10) ? "" + i : "0" + i;
	}
	/**
	 * Returns a string version of two digit number, with leading zero if needed
	 * The input is expected to be in the range 0 to 99.
	 * @param i The value.
	 * @return The string representation with two digits.
	 */
	public static String twoDigits(float i)
	{
		return (i >= 10) ? "" + i : "0" + i;
	}
}
