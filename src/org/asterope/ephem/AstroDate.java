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
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.TimeZone;



/**
 * A class for dates operations.
 * <P>
 * This class stores a date/time value at a precision of one second or better.
 * <P>
 * Selectable dates are not limited in years, but there are some invalid dates (non
 * existent), between October 5, 1582 and October 14, 1582. They can be
 * instantiated if desired, but it is not possible to work with them without the
 * corresponding exception.
 * <P>
 * Note: This library uses GregorianCalendar for dates equal or after October,
 * 15, 1582. For dates before that the library uses the Julian Calendar. The
 * Before Christ era (B.C.) is automatically selected by setting a zero or
 * negative year as the year in the GregorianCalendar instance. Year 0
 * corresponds to year 1 B.C. (year 0 does not exist by itself). When using an
 * instance of {@linkplain AstroDate} occurs the same. The only difference is that here month
 * '1' is January. In GregorianCalendar January is month '0'. To avoid
 * confusion it is recommended to use always {@linkplain AstroDate} as the instance for
 * dates, as well as the provided symbolic EphemConstant. for the months.
 * <P>
 * In the constructors the year is entered considering that year 0 does not exist, so year
 * -1 is 1 B.C. When retrieving the year the value returned follows the same rule. But
 * internally, in the instance, the year is hold in the astronomical way, where 1 B.C. is
 * year 0.
 * <P>
 * Date/time can be specified either in the Gregorian Calendar or the Julian
 * Calendar for any instant using Date_ops.
 * 
 * @see java.util.GregorianCalendar
 * @author T. Alonso Albi - OAN (Spain)
 * @author M. Huss
 * @version 1.0
 */
public class AstroDate implements Serializable
{
	

	private static final long serialVersionUID = 4454767498350689426L;

	/**
	 * Default constructor for current instant.
	 */
	public AstroDate()
	{
		AstroDate astro = new AstroDate(new GregorianCalendar());
		day = astro.day;
		month = astro.month;
		year = astro.year;
		second = astro.second;
	}

	/**
	 * Literal (member by member) constructor.
	 * @param year Year. Should not be zero.
	 * @param month Month of the year (1..12).
	 * @param day Day of the month (1...31).
	 * @param seconds Time in seconds past midnight. This must be in the range
	 *        from 0 to Astro.SECONDS_PER_DAY-1.
	 */
	public AstroDate(int year, int month, int day, double seconds)
	{
		if (year < 0) year++;
		this.day = day;
		this.month = month;
		this.year = year;
		this.second = seconds;
	}

	/**
	 * Explicit day, month, year, hour, minute, and second constructor.
	 * @param year Year. Should not be zero.
	 * @param month Month of the year (1..12).
	 * @param day Day of the month (1...31).
	 * @param hour Hour of the day (0...23).
	 * @param min Minute of the hour (0...59).
	 * @param sec Second of the minute (0...59).
	 */
	public AstroDate(int year, int month, int day, int hour, int min, double sec)
	{
		if(month<1 || month>12)			
			throw new IllegalArgumentException("illegal month");
		if(day<1 || day>31)
			throw new IllegalArgumentException("illegal day");
		if (year < 0) year++;
		this.day = day;
		this.month = month;
		this.year = year;
		this.second = (hour * EphemConstant.SECONDS_PER_HOUR + min * EphemConstant.SECONDS_PER_MINUTE + sec);
	}

	/**
	 * Year, month, day constructor (time defaults to 00:00:00 = midnight).
	 * @param year Year. Should not be zero.
	 * @param month Month of the year (1..12).
	 * @param day Day of the month (1...31).
	 */
	public AstroDate(int year, int month, int day)
	{
		if (year < 0) year++;
		this.day = day;
		this.month = month;
		this.year = year;
		this.second = 0;
	}

//	/**
//	 * Creates an instance from a date expressed in a common way,
//	 * like 2001 Jan 1, Feb 17 2005 10:00:00, MJD50000.125, and so on.<P>
//	 * The date is parsed by means of the cds package.
//	 * @param cdsDate Date.
//	 * @throws JPARSECException If an error occurs.
//	 */
//	public AstroDate(String cdsDate) {
//		try {
//			Date.parse(cdsDate);
//			Astrotime t = new Astrotime();
//			t.set(cdsDate);
//			
//			double jd = t.getJD();
//			AstroDate astro = new AstroDate(jd);
//			this.year = astro.year;
//			this.month = astro.month;
//			this.day = astro.day;
//			this.second = astro.second;
//		} catch (Exception e)
//		{
//			throw new IllegalArgumentExcpetion("could not understand "+cdsDate+" as a date.", e);
//		}
//	}
	
	/**
	 * Day (with decimals), Month, Year constructor (time defaults to 00:00:00 =
	 * midnight).
	 * @param year Year.
	 * @param month Month of the year (1..12).
	 * @param day Day of the month (1...31) and fraction of days.
	 */
	public AstroDate(int year, int month, double day)
	{
		if (year < 0) year++;
		this.day = (int) day;
		this.month = month;
		this.year = year;
		this.second = ((day - (int) day) * EphemConstant.SECONDS_PER_DAY);
	}

	/**
	 * Convert a GregorianCalendar instance to an {@linkplain AstroDate}. <BR>
	 * 
	 * @param cal An instance of java.util.GregorianCalendar.
	 */
	public AstroDate(GregorianCalendar cal)
	{
		this.year = cal.get(GregorianCalendar.YEAR);
		this.month = cal.get(GregorianCalendar.MONTH) + 1;
		this.day = cal.get(GregorianCalendar.DATE);

		this.second = (cal.get(GregorianCalendar.HOUR_OF_DAY) * EphemConstant.SECONDS_PER_HOUR + cal
				.get(GregorianCalendar.MINUTE) * EphemConstant.SECONDS_PER_MINUTE + cal.get(GregorianCalendar.SECOND) + cal
				.get(GregorianCalendar.MILLISECOND) / 1000.0);

		if (cal.get(GregorianCalendar.ERA) == GregorianCalendar.BC)
		{
			this.year = -cal.get(GregorianCalendar.YEAR) + 1;
		}
	}

	/**
	 * Julian Day constructor of a Gregorian {@linkplain AstroDate}. <BR>
	 * 
	 * @param jd Julian day number.
	 */
	public AstroDate(double jd)
	{

//		if (jd < 0.0 || jd > 5373851.0)
//			throw new JPARSECException(
//					"invalid julian day " + jd + ", outside time spand (0.0 - 5373851.0) or years (-4712 to 10000).");
		if (jd < 2299160.0 && jd >= 2299150.0)
		{
			throw new IllegalArgumentException("invalid julian day " + jd + ". This date does not exist.");
		}

		// The conversion formulas are from Meeus,
		// Chapter 7.

		double Z = Math.floor(jd + 0.5);
		double F = jd + 0.5 - Z;
		double A = Z;
		if (Z >= 2299161D)
		{
			int a = (int) ((Z - 1867216.25) / 36524.25);
			A += 1 + a - a / 4;
		}
		double B = A + 1524;
		int C = (int) ((B - 122.1) / 365.25);
		int D = (int) (C * 365.25);
		int E = (int) ((B - D) / 30.6001);

		double exactDay = F + B - D - (int) (30.6001 * E);
		day = (int) exactDay;
		month = (E < 14) ? E - 1 : E - 13;
		year = C - 4715;
		if (month > 2)
			year--;

		second = ((exactDay - day) * EphemConstant.SECONDS_PER_DAY);
	}

	// -------------------------------------------------------------------------
	// functions
	// -------------------------------------------------------------------------

	/**
	 * Convert an {@linkplain AstroDate} to a Julian Day. See Meeus, Astronomical
	 * Algorithms, chapter 7.
	 * 
	 * @param ad The date to convert.
	 * @param julian true = Julian calendar, else Gregorian.
	 * @return The Julian Day that corresponds to the specified {@linkplain AstroDate}.
	 */
	public static double jd(AstroDate ad, boolean julian)
	{

		// The conversion formulas are from Meeus, chapter 7.

		int D = ad.day;
		int M = ad.month;
		int Y = ad.year;
		if (M < 3)
		{
			Y--;
			M += 12;
		}
		int A = Y / 100;
		int B = julian ? 0 : 2 - A + A / 4;

		double dayFraction = ad.second / EphemConstant.SECONDS_PER_DAY;

		double jd = dayFraction + (int) (365.25D * (Y + 4716)) + (int) (30.6001 * (M + 1)) + D + B - 1524.5;

//		if (jd < 0.0 || jd > 5373851.0)
//			throw new JPARSECException(
//					"invalid julian day " + jd + ", outside time spand (0.0 - 5373851.0) or years (-4712 to 10000).");
		if (jd < 2299160.0 && jd >= 2299150.0)
		{
			throw new IllegalArgumentException("invalid julian day " + jd + ". This date does not exist.");
		}

		return jd;
	}

	/**
	 * Convert an {@linkplain AstroDate} to a Julian Day.
	 * <P>
	 * Assumes the {@linkplain AstroDate} is specified using the Gregorian
	 * calendar if the {@linkplain AstroDate} is October, 15, 1582 or later. The Julian
	 * calendar will be used for dates before October, 5, 1582. Intermediate
	 * dates should not be used. They are not limited due to a lack of support
	 * on this issue from java.util.GregorianCalendar.
	 * 
	 * @param ad The date to convert.
	 * @return The Julian Day that corresponds to the specified {@linkplain AstroDate}.
	 */
	public static double jd(AstroDate ad)
	{

		int D = ad.day;
		int M = ad.month;
		int Y = ad.year;

		boolean julian = false;

		if (Y < 1582)
			julian = true;
		if (Y == 1582 && M < 10)
			julian = true;
		if (Y == 1582 && M == 10 && D < 5)
			julian = true;

		double jd = jd(ad, julian);

		return jd;
	}

	/**
	 * Convert this instance of {@linkplain AstroDate} to a Julian Day. <BR>
	 * 
	 * @param julian true = Julian calendar, else Gregorian.
	 * @return The Julian Day that corresponds to this {@linkplain AstroDate}
	 *         instance.
	 */
	public double jd(boolean julian)
	{
		return jd(this, julian);
	}

	/**
	 * Convert this instance of {@linkplain AstroDate} to a Julian Day.
	 * <P>
	 * Assumes the {@linkplain AstroDate} is specified using the Gregorian
	 * calendar if the {@linkplain AstroDate} is October, 15, 1582 or later. The Julian
	 * calendar will be used for dates before October, 5, 1582. Intermediate
	 * dates should not be used. They are not limited due to a lack of support
	 * on this issue from java.util.GregorianCalendar.
	 * 
	 * @return The Julian Day that corresponds to this {@linkplain AstroDate}
	 *         instance.
	 */
	public double jd()
	{

		int D = this.day;
		int M = this.month;
		int Y = this.year;

		boolean julian = false;

		if (Y < 1582)
			julian = true;
		if (Y == 1582 && M < 10)
			julian = true;
		if (Y == 1582 && M == 10 && D < 5)
			julian = true;

		return jd(this, julian);
	}

	/**
	 * Check if the {@linkplain AstroDate} instance contains an invalid date. A date is
	 * invalid between October 5, 1582 and October 14, 1582.
	 * 
	 * @return true if the date is invalid, false otherwise.
	 */
	public boolean isInvalid()
	{
		int D = this.day;
		int M = this.month;
		int Y = this.year;

		boolean invalid = false;

		if (Y == 1582 && M == 10 && (D >= 5 && D < 15))
			invalid = true;

		return invalid;
	}

	/**
	 * Gets the year. <BR>
	 * 
	 * @return The year part of this instance of {@linkplain AstroDate}.
	 */
	public int getYear()
	{
		int y = year;
		if (y <= 0) y--;
		return y;
	}

	/**
	 * Sets the year. <BR>
	 * 
	 * @param y The year part of this instance of {@linkplain AstroDate}.
	 */
	public void setYear(int y)
	{
		year = y;
	}

	/**
	 * Gets the month. <BR>
	 * 
	 * @return The month part of this instance of {@linkplain AstroDate} (1..12).
	 */
	public int getMonth()
	{
		return month;
	}

	/**
	 * Sets the month. <BR>
	 * 
	 * @param m The month part of this instance of {@linkplain AstroDate} (1..12)<BR>.
	 *        Value is not checked!
	 */
	public void setMonth(int m)
	{
		month = m;
	}

	/**
	 * Gets the day. <BR>
	 * 
	 * @return The day part of this instance of {@linkplain AstroDate} (1..31).
	 */
	public int getDay()
	{
		return day;
	}

	/**
	 * Gets the day plus fraction of days. <BR>
	 * 
	 * @return The day plus fraction of day part of this instance of {@linkplain AstroDate}
	 *         [1.0 ... 32.0).
	 */
	public double getDayPlusFraction()
	{
		return day + second / EphemConstant.SECONDS_PER_DAY;
	}

	/**
	 * Sets the day. <BR>
	 * 
	 * @param d The day part of this instance of {@linkplain AstroDate} (1..31).
	 *        Value is not checked!
	 */
	public void setDay(int d)
	{
		day = d;
	}

	/**
	 * Returns the hours for this instance.
	 * @return Hourd.
	 */
	public int getHour()
	{
		double hour = 24.0 * this.second / EphemConstant.SECONDS_PER_DAY;
		return (int) hour;
	}
	/**
	 * Returns the minutes for this instance.
	 * @return Minutes.
	 */
	public int getMinute()
	{
		double hour = 24.0 * this.second / EphemConstant.SECONDS_PER_DAY;
		double min = (hour - (int) hour) * 60.0;
		return (int) min;
	}
	/**
	 * Returns the seconds (elapsed from last minute) for this instance.
	 * @return Seconds.
	 */
	public double getSecond()
	{
		double hour = 24.0 * this.second / EphemConstant.SECONDS_PER_DAY;
		double min = (hour - (int) hour) * 60.0;
		double sec = (min - (int) min) * 60.0;
		return sec;
	}
	/**
	 * Get the Hour. <BR>
	 * This function truncates, and does not round up to nearest hour. For
	 * example, this function will return '1' at all times from 01:00:00 to
	 * 01:59:59 inclusive.
	 * 
	 * @return The hour of the day for this instance of {@linkplain AstroDate},
	 *         not rounded.
	 */
	public int hour()
	{
		return (int) (second / EphemConstant.SECONDS_PER_HOUR);
	}

	/**
	 * Get the rounded hour. <BR>
	 * Returns the hour of the day rounded to nearest hour. For example, this
	 * function will return '1' at times 01:00:00 to 01:29:59, and '2' at times
	 * 01:30:00 to 01:59:59.
	 * 
	 * @return The hour of the day for this instance of {@linkplain AstroDate},
	 *         rounded to the nearest hour.
	 */
	public int hourRound()
	{
		return (int) ((second / EphemConstant.SECONDS_PER_HOUR) + EphemConstant.ROUND_UP);
	}

	/**
	 * Get the minute. <BR>
	 * This function truncates, and does not round up to nearest minute. For
	 * example, this function will return 20 at all times from 1:20:00 to
	 * 1:20:59 inclusive.
	 * 
	 * @return The minute of the hour for this instance of {@linkplain AstroDate},
	 *         not rounded.
	 */
	public int minute()
	{
		return (int) ((second - (hour() * EphemConstant.SECONDS_PER_HOUR)) / EphemConstant.SECONDS_PER_MINUTE);
	}

	/**
	 * Get the rounded minute. <BR>
	 * Returns the minute of the hour for this instance of {@linkplain AstroDate},
	 * rounded to nearest minute. For example, this function will return 20 at
	 * times 1:20:00 to 1:20:29, and 21 at times 1:20:30 to 1:20:59.
	 * 
	 * @return The minute of the hour for this instance of {@linkplain AstroDate},
	 *         rounded to the nearest minute.
	 */
	public int minuteRound()
	{
		return (int) (((second - (hour() * EphemConstant.SECONDS_PER_HOUR)) / EphemConstant.SECONDS_PER_MINUTE) + EphemConstant.ROUND_UP);
	}

	/**
	 * Get the second.
	 * 
	 * @return The second of the minute for this instance of {@linkplain AstroDate}.
	 */
	public int second()
	{
		return (int) (second - (hour() * EphemConstant.SECONDS_PER_HOUR) - (minute() * EphemConstant.SECONDS_PER_MINUTE));
	}

	/**
	 * Sets the fraction of day.
	 * @param f Fraction of day.
	 */
	public void setDayFraction(double f)
	{
		second = f * EphemConstant.SECONDS_PER_DAY;
	}
	
	/**
	 * Returns the fraction of day elapsed from previous midnight.
	 * @param jd Julian day.
	 * @return Day fraction.
	 */
	public static double getDayFraction(double jd)
	{
		double frac = jd - (int) jd + 0.5;
		if (frac > 1.0) frac = frac - 1.0;
		return frac;
	}
	
	/**
	 * Convert this {@linkplain AstroDate} instance to a GregorianCalendar. <BR>
	 * 
	 * @return An instance of java.util.GregorianCalendar built using
	 *         this instance of {@linkplain AstroDate}.
	 */
	public GregorianCalendar toGCalendar()
	{
		int myyear = year;
		int era = GregorianCalendar.AD;
		if (myyear <= 0)
		{
			myyear = -(myyear - 1);
			era = GregorianCalendar.BC;
		}

		GregorianCalendar cal = new GregorianCalendar(myyear, month - 1, day, hour(), minute(), second());
		cal.set(GregorianCalendar.ERA, era);

		// Account for second fractions
		long time_in_ms = cal.getTimeInMillis();
		long new_time_in_ms = (long) (time_in_ms + (second - (int) second) * 1000.0);
		cal.setTimeInMillis(new_time_in_ms);
		return cal;
	}

	/**
	 * Convert this {@linkplain AstroDate} instance to a String,
	 * formatted to the minute. This function rounds the exact time to the
	 * nearest minute.
	 * <P>
	 * The format of the returned string is YYYY-MM-DD hh:mm.
	 * 
	 * @return A formatted date/time String.
	 */
	public String toMinString()
	{
		return fmt(year, 4, '-') + fmt(month, '-') + fmt(day, ' ') + fmt(hour(), ':') + fmt(minuteRound(), 2);
	}

	/**
	 * Convert this {@linkplain AstroDate} instance to a String formatted to the
	 * minute, with Time Zone indicator. <BR>
	 * Rounds the time to the nearest minute.
	 * <P>
	 * The format of the returned string is YYYY-MM-DD hh:mm TZ,
	 * where TZ is a locale-specific timezone name (e.g., "EST").
	 * 
	 * @return A formatted date/time String.
	 */
	public String toMinStringTZ()
	{
		TimeZone tz = TimeZone.getDefault();
		return toMinString() + ' ' + tz.getDisplayName(dstOffset(toGCalendar()) != 0, TimeZone.SHORT);
	}

	/**
	 * Convert <B>this</B> {@linkplain AstroDate} instance to a String.
	 * <P>
	 * The format of the returned string is YYYY-MM-DD hh:mm:ss.
	 * 
	 * @return A formatted date/time String.
	 */
	public String toString()
	{
		return "" + fmt(year, 4, '-') + fmt(month, 2, '-') + fmt(day, 2, ' ') + fmt(hour(), 2, ':') + fmt(minute(), 2, ':') + fmt(
				second(), 2);
	}

	/**
	 * Convert <B>this</B> {@linkplain AstroDate} instance to a String,
	 * with Time Zone indicator.
	 * <P>
	 * The format of the returned string is YYYY-MM-DD hh:mm:ss TZ,
	 * where TZ is a locale-specific timezone name (e.g., "EST").
	 * 
	 * @return A formatted date/time String.
	 */
	public String toStringTZ()
	{
		TimeZone tz = TimeZone.getDefault();
		return toString() + ' ' + tz.getDisplayName(dstOffset(toGCalendar()) != 0, TimeZone.SHORT);
	}

	/**
	 * Format a String representation of an integer at the specified
	 * width.
	 * <P>
	 * Note that this function will return an incorrect representation if the
	 * integer is wider than the specified width. For example:<BR>
	 *  fmt( 1, 3 ) will return "001".<BR>
	 *  fmt( 12, 3 ) will return "012".<BR>
	 *  fmt( 1234, 3 ) will return "<B>234</B>."
	 * 
	 * @param i The integer to format.
	 * @param w The format width.
	 * @return A formatted String.
	 */
	static String fmt(int i, int w)
	{
		StringBuffer sb = new StringBuffer();
		while (w-- > 0)
		{
			sb.append((char) ('0' + (i % 10)));
			i /= 10;
		}
		sb.reverse();
		return sb.toString();
	}

	/**
	 * Format a String representation of an integer at the specified
	 * width, and add the specified suffix.
	 * <P>
	 * Note that this will return an incorrect representation if the integer is
	 * wider than the specified width. For example:<BR>
	 *  fmt( 1, 3, ':' ) will return "001:".<BR>
	 *  fmt( 12, 3, ':' ) will return "012:".<BR>
	 *  fmt( 1234, 3, ':' ) will return "<B>234:</B>."
	 * 
	 * @param i The integer to format.
	 * @param w The format width.
	 * @param suffix The character to append.
	 * @return A formatted String.
	 */
	static String fmt(int i, int w, char suffix)
	{
		return fmt(i, w) + suffix;
	}

	// -------------------------------------------------------------------------
	/**
	 * Day of the month.
	 */
	int day;

	/**
	 * Month of the year.
	 */
	int month;

	/**
	 * Year.
	 */
	int year;

	/**
	 * Seconds past midnight == day fraction. <BR>
	 * Valid values range from 0 to Astro.SECONDS_PER_DAY-1.
	 */
	double second;

	/**
	 * ID EphemEphemConstant.for month January.
	 */
	public static final int JANUARY = 1;

	/**
	 * ID EphemEphemConstant.for month February.
	 */
	public static final int FEBRUARY = 2;

	/**
	 * ID EphemEphemConstant.for month March.
	 */
	public static final int MARCH = 3;

	/**
	 * ID EphemEphemConstant.for month April.
	 */
	public static final int APRIL = 4;

	/**
	 * ID EphemEphemConstant.for month May.
	 */
	public static final int MAY = 5;

	/**
	 * ID EphemEphemConstant.for month June.
	 */
	public static final int JUNE = 6;

	/**
	 * ID EphemEphemConstant.for month July.
	 */
	public static final int JULY = 7;

	/**
	 * ID EphemEphemConstant.for month August.
	 */
	public static final int AUGUST = 8;

	/**
	 * ID EphemEphemConstant.for month September.
	 */
	public static final int SEPTEMBER = 9;

	/**
	 * ID EphemEphemConstant.for month October.
	 */
	public static final int OCTOBER = 10;

	/**
	 * ID EphemEphemConstant.for month November.
	 */
	public static final int NOVEMBER = 11;

	/**
	 * ID EphemEphemConstant.for month December.
	 */
	public static final int DECEMBER = 12;


	/**
	 * Calculate the current Daylight Time offset ( 0 or -1 ). <BR>
	 * Add the result of this function to the current time to adjust.
	 * 
	 * @param cal A <TT>java.util.Calendar</TT> object which is used to get
	 *        the <TT>DST_OFFSET</TT> from (e.g., <TT>java.util.GregorianCalendar</TT>).
	 * @return <TT>DST_OFFSET</TT> in hours if Daylight time is in effect, 0
	 *         otherwise.
	 */
	private static int dstOffset(Calendar cal)
	{
		return (int) (cal.get(Calendar.DST_OFFSET) / EphemConstant.MILLISECONDS_PER_HOUR);
	}
	


}
