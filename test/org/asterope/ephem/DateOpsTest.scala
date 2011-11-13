package org.asterope.ephem

import java.util.{SimpleTimeZone, Calendar, GregorianCalendar}
import org.asterope.util.ScalaTestCase

class DateOpsTest extends ScalaTestCase{

  def testDateOps()
	{
		System.out.println("DateOps Test");

					// AstroDate ad = new AstroDate();
			// double jd = dmyToDoubleDay(ad);

			// GregorianCalendar gc = new GregorianCalendar();
			// System.out.println( gc.get(Calendar.DST_OFFSET) );

			// GregorianCalendar gc = Date_ops.jdToCal( );
			// System.out.println( DateU.dateTime(gc) );

			// UTC now test

			val utc = new GregorianCalendar(new SimpleTimeZone(0, "UTC"));

			var month = utc.get(Calendar.MONTH) + 1;
			var day = utc.get(Calendar.DAY_OF_MONTH);
			var year = utc.get(Calendar.YEAR);
			var hour = utc.get(Calendar.HOUR_OF_DAY);
			var minute = utc.get(Calendar.MINUTE);
			var seconds = utc.get(Calendar.SECOND);

			val astroDateUTC = new AstroDate(year, month, day, hour, minute, seconds);
			var julianDay_AstroDate = astroDateUTC.jd() - TimeOps.tzOffset(utc) / EphemConstant.HOURS_PER_DAY; // Astrodate
																												// has
																												// no
																												// built-in
																												// TZ

			println("For current date in Greenwich:\njulianDay_AstroDate = " + julianDay_AstroDate);

			var julianDay_Date_ops = DateOps.calendarToDoubleDay(utc);
			println("julianDay_Date_ops = " + julianDay_Date_ops);

      val diff1 =  (julianDay_Date_ops - julianDay_AstroDate) * 24
			println("julianDay_Date_ops - julianDay_AstroDate = " + diff1 + " hours");
      assert(diff1 === 0)

			// Local TZ now test

			val now = new GregorianCalendar();

			month = now.get(Calendar.MONTH) + 1;
			day = now.get(Calendar.DAY_OF_MONTH);
			year = now.get(Calendar.YEAR);
			hour = now.get(Calendar.HOUR_OF_DAY);
			minute = now.get(Calendar.MINUTE);
			seconds = now.get(Calendar.SECOND);

			val astroDateNow = new AstroDate(year, month, day, hour, minute, seconds);
			julianDay_AstroDate = astroDateNow.jd() - TimeOps.tzOffset(now) / EphemConstant.HOURS_PER_DAY; // Astrodate
																										// has no
																										// built-in
																										// TZ

			println("For current date local TZ:\njulianDay_AstroDate = " + julianDay_AstroDate);

			julianDay_Date_ops = DateOps.calendarToDoubleDay(now);
			println("julianDay_Date_ops = " + julianDay_Date_ops);

      val diff2 = (julianDay_Date_ops - julianDay_AstroDate) * 24
			println("julianDay_Date_ops - julianDay_AstroDate = " +  diff2 + " hours");
      assert(diff2 === 0)

			// J2000

			val astroDate = new AstroDate();
			julianDay_AstroDate = astroDate.jd();
			println("\nFor J2000:\njulianDay_AstroDate = " + julianDay_AstroDate);
			println("julianDay_Date_ops = " + DateOps.dmyToDay(astroDate));
			println("TimeOps.tzOffset() = " + TimeOps.tzOffset());
      assert( TimeOps.tzOffset()===0)


	}

}
