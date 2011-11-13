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
 * A class to perform calendrical conversions between Gregorian and Julian
 * calendars.
 * <P>
 * Note that the Julian Day Number (JD), widely used in astronomical
 * calculations, is different from and not directly related to the Julian
 * Calendar.
 * <P>
 * Based on code by B. Gray (www.projectpluto.com)
 * 
 * @author M. Huss
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class DateOps
{
	/**
	 * Abbreviated month names.
	 */
	final String monthNames[] =
	{ "Jan", "Feb", "Mar", "Apr", "May", "Jun", "Jul", "Aug", "Sep", "Oct", "Nov", "Dec" };

	/**
	 * Pseudo-enum for calendar type.
	 */
	public static final int GREGORIAN = 0, JULIAN = 1;

	/**
	 * Converts a day/month/year to a Julian day in long form.
	 * 
	 * @param day Day of the month (1..31).
	 * @param month Month of the Year (1..12).
	 * @param year Year.
	 * @param calendar GREGORIAN or JULIAN.
	 * @return The corresponding Julian day number.
	 */
	public static long dmyToDay(int day, int month, int year, int calendar)
	{
		/*
		 * Bill Gray's Comments: This function gets calendar data for the
		 * current year, including the Julian day of New Years Day for that
		 * year. After that, all it has to do is add up the days in intervening
		 * months, plus the day of the month, and it's done.
		 */
		DateConversionData dcd = new DateConversionData(year);
		long jd = 0;
		if (getCalendarData(dcd, calendar))
		{
			jd = dcd.yearEndDays;
			for (int i = 0; i < (month - 1); i++)
			{
				jd += dcd.monthDays[i];
			}
			jd += (long) (day - 1);
		}
		return jd;
	}

	/**
	 * Converts a GREGORIAN day/month/year to a Julian day in long
	 * form.
	 * 
	 * @param day Day of the month (1..31).
	 * @param month Month of the Year (1..12).
	 * @param year Year.
	 * @return The corresponding Julian day number.
	 */
	public static long dmyToDay(int day, int month, int year)
	{
		return dmyToDay(day, month, year, GREGORIAN);
	}

	/**
	 * Converts an AstroDate to a Julian day in long form. <BR>
	 * This function uses the GREGORIAN calendar.
	 * 
	 * @param d AstroDate to convert.
	 * @return The corresponding Julian day number.
	 */
	public static long dmyToDay(AstroDate d)
	{
		return dmyToDay(d.day, d.month, d.year, GREGORIAN);
	}

	/**
	 * Converts a day/month/year to a Julian day in double form. <BR>
	 * This function uses the GREGORIAN calendar.
	 * 
	 * @param d AstroDate to convert.
	 * @return The corresponding Julian day number.
	 */
	public static double dmyToDoubleDay(AstroDate d)
	{
		return d.second / EphemConstant.SECONDS_PER_DAY - .5 + dmyToDay(d.day, d.month, d.year, GREGORIAN);
	}

	/**
	 * Converts a Julian day to an AstroDate.
	 * 
	 * @param jd Julian day to convert.
	 * @param d AstroDate to put the results into.
	 * @param calendar GREGORIAN or JULIAN.
	 */
	static void dayToDmy(long jd, AstroDate d, int calendar)
	{
		/*
		 * Bill Gray's Comments: Estimates the year corresponding to an input JD
		 * and calls getCalendarData() for that year. Occasionally, it will find
		 * that the guesstimate was off; in such cases, it moves ahead or back a
		 * year and tries again. Once it's done, jd - yearEndDays gives the
		 * number of days since New Years Day; by subtracting monthDays[]
		 * values, we quickly determine which month and day of month we're in.
		 */
		d.day = -1; /* to signal an error */
		switch (calendar)
		{
		case GREGORIAN:
		case JULIAN:
			d.year = (int) ((jd - E_JULIAN_GREGORIAN) / 365L);
			break;
		default: /* undefined calendar */
			return;
		} // end switch()

		DateConversionData dcd = new DateConversionData(d.year);

		do
		{
			if (!getCalendarData(dcd, calendar))
				return;

			if (dcd.yearEndDays > jd)
				d.year--;

			if (dcd.nextYearEndDays <= jd)
				d.year++;

			dcd.year = d.year;

		} while (dcd.yearEndDays > jd || dcd.nextYearEndDays <= jd);

		long currJd = dcd.yearEndDays;
		d.month = -1;
		for (int i = 0; i < DateConversionData.MONTH_DAYS; i++)
		{
			d.day = (int) (jd - currJd);
			if (d.day < dcd.monthDays[i])
			{
				d.month = i + 1;
				d.day++;
				return;
			}
			currJd += (long) (dcd.monthDays[i]);
		}
		return;
	}

	/**
	 * Converts a Julian day in long form to a GREGORIAN AstroDate.
	 * 
	 * @param jd Julian day to convert.
	 * @param d AstroDate to insert results into.
	 */
	static void dayToDmy(long jd, AstroDate d)
	{
		dayToDmy(jd, d, GREGORIAN);
	}

	/**
	 * Converts a Julian day in double form to an AstroDate. <BR>
	 * This function has higher precision than the long version.
	 * 
	 * @param jd Julian day to convert.
	 * @param d AstroDate to insert results into.
	 * @param calendar GREGORIAN or JULIAN.
	 */
	static void dayToDmy(double jd, AstroDate d, int calendar)
	{
		dayToDmy((long) jd, d, calendar);
		d.second = (int) (0.5 + (jd - Math.floor(jd)) * EphemConstant.SECONDS_PER_DAY);
	}

	/**
	 * Converts a Julian day in double form to a GREGORIAN AstroDate.
	 * <BR>
	 * This function has higher precision than the long version.
	 * 
	 * @param jd Julian day to convert.
	 * @param d AstroDate to insert results into.
	 */
	static void dayToDmy(double jd, AstroDate d)
	{
		dayToDmy(jd, d, GREGORIAN);
	}

	// --------------------------------------------------------------------------

	/**
	 * Convert a Java Calendar to Julian day value in long
	 * form.
	 * 
	 * @param cal java.util.Calendar to convert.
	 * @return The corresponding Julian day.
	 */
	public static long calendarToDay(Calendar cal)
	{
		return dmyToDay(cal.get(Calendar.DATE), cal.get(Calendar.MONTH) + 1, // Calendar
																				// has
																				// 0-based
																				// months
																				// (!)
				cal.get(Calendar.YEAR));
	}

	/**
	 * Convert a Java Calendar to a Julian day value in double form.
	 * 
	 * @param cal java.util.Calendar to convert.
	 * @return The corresponding Julian day.
	 */
	public static double calendarToDoubleDay(Calendar cal)
	{

		double hours = (double) cal.get(Calendar.HOUR_OF_DAY) + cal.get(Calendar.MINUTE) / EphemConstant.MINUTES_PER_HOUR + cal
				.get(Calendar.SECOND) / EphemConstant.SECONDS_PER_HOUR - TimeOps.tzOffset(cal);
		return (double) calendarToDay(cal) + hours / EphemConstant.HOURS_PER_DAY - 0.5;
	}

	/**
	 * Calculate the <B>current</B> Julian day value in double form.
	 * 
	 * @return The corresponding Julian day.
	 */
	public static double nowToDoubleDay()
	{
		return calendarToDoubleDay(new GregorianCalendar());
	}

	/**
	 * Calculate the <B>current</B> Julian day value in long form.
	 * 
	 * @return The corresponding Julian day.
	 */
	public static long nowToDay()
	{
		return calendarToDay(new GregorianCalendar());
	}

	/**
	 * Calculate the Julian day number for the date when Daylight time started
	 * in a given year (before 2007) in United States and Canada.
	 * <P>
	 * Please note that the new 'rule' is not applied in the whole USA, for
	 * example in Arizona, Hawai, Puerto Rico, Virgin Islands, and American
	 * Samoa.
	 * 
	 * @param year The year of interest.
	 * @return The corresponding Julian day.
	 * @deprecated As of August 2005 law, approved by the congress of the USA,
	 *             now the DST starts in the second Monday of March.
	 */
	public static double getFirstSundayOfApril(int year)
	{
		// first Sunday in April
		long jd = dmyToDay(1, 4, year);
		while (6 != (jd % 7))
			// Sunday
			jd++;

		return jd - 0.5;
	}

	/**
	 * Calculate the Julian day number for the date when Daylight time ends in a
	 * given year (2007 and after) in United States.
	 * 
	 * @param year The year of interest.
	 * @return The corresponding Julian day.
	 */
	public static double getFirstSundayOfNovember(int year)
	{
		// first Sunday in November
		long jd = dmyToDay(1, 11, year);
		while (6 != (jd % 7))
			// Sunday
			jd++;

		return jd - 0.5;
	}

	/**
	 * Calculate the Julian day number for the date when Daylight time starts in
	 * a given year (2007 and after) in United States.
	 * 
	 * @param year The year of interest.
	 * @return The corresponding Julian day.
	 */
	public static double getSecondMondayOfApril(int year)
	{
		// first Monday in April
		long jd = dmyToDay(1, 4, year);
		while (0 != (jd % 7))
			// Monday
			jd++;

		return jd - 0.5 + 7.0;
	}

	/**
	 * Calculate the Julian day number for the date when Daylight time ended in
	 * a given year (before 2007) in United States and other countries.
	 * <P>
	 * Note that as of August 2005 law, approved by the congress of the USA, now
	 * the DST ends in the first Sunday of November in that country and Canada,
	 * except in the following regions that maintain the previous rule: Arizona,
	 * Hawai, Puerto Rico, Virgin Islands, and American Samoa.
	 * 
	 * @param year The year of interest.
	 * @return The corresponding Julian day.
	 */
	public static double getLastSundayOfOctober(int year)
	{
		// last Sunday in October
		long jd = dmyToDay(31, 10, year);
		while (6 != (jd % 7))
			// Sunday
			jd--;

		return jd - 0.5;
	}

	/**
	 * Calculate the Julian day number for the date when Daylight time starts in
	 * a given year in some countries in the north hemisphere, or ends in others
	 * in the southern one.
	 * 
	 * @param year The year of interest.
	 * @return The corresponding Julian day.
	 */
	public static double getLastSundayOfMarch(int year)
	{
		// last Sunday in March
		long jd = dmyToDay(31, 3, year);
		while (6 != (jd % 7))
			// Sunday
			jd--;

		return jd - 0.5;
	}

	/**
	 * Calculate the Julian day number for the date when Daylight time ends in
	 * some countries of the north hemisphere, or starts in others in the
	 * southern one.
	 * 
	 * @param year The year of interest.
	 * @return The corresponding Julian day.
	 */
	public static double getLastSundayOfNovember(int year)
	{
		// last Sunday in November
		long jd = dmyToDay(31, 11, year);
		while (6 != (jd % 7))
			// Sunday
			jd--;

		return jd - 0.5;
	}

	/**
	 * Calculate the Julian day number for the date when Daylight time starts in
	 * a given year in some countries of the north hemisphere, or ends in other
	 * countries in the southern one.
	 * 
	 * @param year The year of interest.
	 * @return The corresponding Julian day.
	 */
	public static double getLastSundayOfApril(int year)
	{
		// last Sunday in April
		long jd = dmyToDay(31, 4, year);
		while (6 != (jd % 7))
			// Sunday
			jd--;

		return jd - 0.5;
	}

	/**
	 * Returns the number of days in a given month in the Gregorian calendar.
	 * @param year Year.
	 * @param month Month.
	 * @return Days in month.
	 */
	public static int getDaysInMonth(int year, int month)
	{
		DateConversionData dcd = new DateConversionData(year);

		int n = dcd.monthDays[month - 1];
		if (month == 2 && isLeapYear(year)) n++;
		return n;
	}
	
	/**
	 * Returns true if the year is a leap year in the Gregorian Calendar.
	 * 
	 * @param year Year value, as it is given by an AstroDate instance.
	 * @return true if it is a leap year, false otherwise.
	 */
	public static boolean isLeapYear(int year)
	{
		boolean isLeap = false;
		int aux1;
		int aux2;
		int aux3;

		aux1 = year % 4;
		aux2 = year % 100;
		aux3 = year % 400;

		if (aux1 == 0 && (aux2 == 0 && aux3 == 0 || aux2 != 0))
		{
			isLeap = true;
		}

		return isLeap;
	}

	/**
	 * Obtain the date of Easter.
	 * <P>
	 * See Meeus, Astronomical Algorithms, p 67.
	 * 
	 * @param year Year in the Gregorian calendar.
	 * @return Julian day.
	 */
	public static long easterDate(int year)
	{
		long year2 = year + 5700000L;
		long a = year2 % 19L, b = year2 / 100L, c = year2 % 100L;
		long d = b / 4L, e = b % 4L, f = (b + 8L) / 25L;
		long g = (b - f + 1L) / 3L, h = (19L * a + b - d - g + 15L) % 30L;
		long i = c / 4L, k = c % 4L, l = (32L + e + e + i + i - h - k) % 7L;
		long m = (a + 11L * h + 22L * l) / 451L, tval = h + l - 7L * m + 114L;

		int month = (int) (tval / 31L);
		int day = (int) (tval % 31L) + 1;

		long jd = dmyToDay(day, month, year);

		return jd;
	}

	// -------------------------------------------------------------------------

	private static final long E_JULIAN_GREGORIAN = 1721060L;

	/**
	 * Gregorian and Julian calendar calculations (combined for simplicity)
	 * <P>
	 * Bill Gray's Comments:<BR>
	 * It's common to implement Gregorian/Julian calendar code with the aid of
	 * cryptic formulae, rather than through simple lookup tables. For example,
	 * consider this formula from Fliegel and Van Flandern, to convert Gregorian
	 * (D)ay, (M)onth, (Y)ear to JD:
	 * <P>
	 *  JD = (1461*(Y+4800+(M-14)/12))/4+(367*(M-2-12*((M-14)/12)))/12
	 * -(3*((Y+4900+(M-14)/12)/100))/4+D-32075 
	 * <P>
	 * The only way to verify that they work is to feed through all possible
	 * cases. Personally, I like to be able to look at a chunk of code and see
	 * what it means. It should resemble the Reformation view of the Bible:
	 * anyone can read it and witness the truth thereof.
	 * 
	 * @param dcd The {@linkplain DateConversionData} to use.
	 * @param julian true for Julian calendar, else Gregorian.
	 */
	private static void getJulGregYearData(DateConversionData dcd, boolean julian)
	{
		if (dcd.year >= 0)
		{
			dcd.yearEndDays = dcd.year * 365 + dcd.year / 4;
			if (!julian)
				dcd.yearEndDays += -dcd.year / 100L + dcd.year / 400L;
		} else
		{
			dcd.yearEndDays = (dcd.year * 365) + (dcd.year - 3) / 4;
			if (!julian)
				dcd.yearEndDays += -(dcd.year - 99) / 100 + (dcd.year - 399) / 400;
		}

		if (julian)
			dcd.yearEndDays -= 2;

		if (0 == (dcd.year % 4))
		{
			if (0 != (dcd.year % 100) || 0 == (dcd.year % 400) || julian)
			{
				dcd.monthDays[1] = 29;
				dcd.yearEndDays--;
			}
		}
		dcd.yearEndDays += E_JULIAN_GREGORIAN + 1;
	}

	// -------------------------------------------------------------------------
	/**
	 * @param dcd The {@linkplain DateConversionData} to use.
	 * @param calendar true for Julian calendar, else Gregorian.
	 * @return true if successful, false otherwise.
	 */
	private static boolean getCalendarData(DateConversionData dcd, int calendar)
	{
		boolean isOk = true;

		// dcd.monthDays[0] = 0;
		switch (calendar)
		{
		case GREGORIAN:
		case JULIAN:
			getJulGregYearData(dcd, (JULIAN == calendar));
			break;
		default:
			isOk = false;
			break;
		}
		if (isOk)
		{
			//
			// days[1] = JD of "New Years Eve" + 1; that is, New Years Day of
			// the following year. If you have days[0] <= JD < days[1], JD is in the
			// current year.
			//
			dcd.nextYearEndDays = dcd.yearEndDays;
			for (int i = 0; i < DateConversionData.MONTH_DAYS; i++)
				dcd.nextYearEndDays += dcd.monthDays[i];
		}
		return (isOk);
	}

}

/**
 * A support class for Date_ops.
 */
class DateConversionData
{
	/**
	 * Gregorian/Julian calendar values.
	 */
	static final int sMonthDays[] =
	{ 31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31, 0 };

	/**
	 * Number of entries in MonthDays table.
	 */
	public static final int MONTH_DAYS = 13;

	/**
	 * Number of days in each month.
	 */
	int monthDays[];

	/**
	 * Number of days to end of year.
	 */
	long yearEndDays;

	/**
	 * Number of days to end of following year.
	 */
	long nextYearEndDays;

	int year;

	/**
	 * Constructor.
	 *
	 * @param y year.
	 */
	DateConversionData(int y)
	{
		monthDays = new int[MONTH_DAYS];
		for (int i = 0; i < MONTH_DAYS; i++)
			monthDays[i] = sMonthDays[i];
		year = y;
	}
}
