package org.asterope.ephem.examples;

import org.asterope.ephem.*;
import org.asterope.ephem.moons.EventElement;
import org.asterope.ephem.moons.MoonEvent;

import java.util.List;

/**
 * A class to demostrate events related to natural satellites.
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class MoonOcultation {

	/**
	 * For rade2Vector testing only.
	 */
	public static void main (String args[])
	{
			// i -> initial, f -> ending times for search
			AstroDate astroi = new AstroDate(2454948.7783598904-0.1); // (2008, AstroDate.JUNE, 15, 0, 0, 0);
			AstroDate astrof = new AstroDate(2454948.7826654227+0.1); // (2010, AstroDate.JANUARY, 1, 0, 0, 0);

			TimeElement timei = new TimeElement(astroi.toGCalendar(), TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
			TimeElement timef = new TimeElement(astrof.toGCalendar(), TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);

			ObserverElement observer = ObserverElement.MADRID;

			EphemerisElement eph = new EphemerisElement(Target.Jupiter, EphemerisElement.Ephem.APPARENT,
					EphemerisElement.EQUINOX_OF_DATE, EphemerisElement.GEOCENTRIC, Precession.Method.IAU2000,
					EphemerisElement.Frame.ICRS);


			// Calculate events
			List<EventElement> ev = MoonEvent.mutualPhenomena(timei, observer, eph, timef, 30, false);
			
			// Show results
			System.out.println("FOUND EVENTS");
			for (EventElement evi:ev)
			{
				AstroDate astroI = new AstroDate(evi.startTime);
				TimeElement timeI = new TimeElement(astroI, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
				double jdI = TimeScale.getJD(timeI, observer, eph, TimeElement.Scale.UNIVERSAL_TIME_UT1);
				AstroDate astroI2 = new AstroDate(jdI);
				String eDateI = astroI2.getYear()+"-"+astroI2.getMonth()+"-"+astroI2.getDay();
				String eTimeI = astroI2.getHour()+":"+astroI2.getMinute()+":"+(int) (astroI2.getSecond()+0.5);
				
				AstroDate astroF = new AstroDate(evi.endTime);
				TimeElement timeF = new TimeElement(astroF, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
				double jdF = TimeScale.getJD(timeF, observer, eph, TimeElement.Scale.UNIVERSAL_TIME_UT1);
				AstroDate astro2F = new AstroDate(jdF);
				String eDateF = astro2F.getYear()+"-"+astro2F.getMonth()+"-"+astro2F.getDay();
				String eTimeF = astro2F.getHour()+":"+astro2F.getMinute()+":"+(int) (astro2F.getSecond()+0.5);

				String visible = "YES";
				if ((evi.elevation * EphemConstant.RAD_TO_DEG) < 20) visible = "DIFFICULT";
				if ((evi.elevation * EphemConstant.RAD_TO_DEG) < 5) visible = "NO";
				if (!evi.visibleFromEarth) visible = "NO*";
				System.out.println(evi.mainBody.name()+" & "+ EventElement.EVENTS[evi.eventType]+" by & "+evi.secondaryBody.name()+" & "+eDateI+" & "+eTimeI+" & "+eTimeF+" & "+(int) (Double.parseDouble(evi.details)+0.5)+" & "+visible);
				
				// Return a chart of the event. A magnitude of 100 is a default value when the entire
				// satellite is eclipsed/occulted.
				
				//TODO fix chart in this example
//				CreateChart ch = MoonEvent.lightCurve(evi, TimeElement.Scale.UNIVERSAL_TIME_UTC, true, false);
//				ch.showChartInJFreeChartPanel();
			}
	}

}
