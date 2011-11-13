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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * A class to calculate events related to natural satellites.
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class MoonEvent {

	/**
	 * Set to true to avoid launching warnings. This improves performance.
	 */
	public static boolean skipWarnings = true;
	
	private static double myjd;
	
	private static Map<Target,MoonEphemElement> getEphem(ObserverElement observer, EphemerisElement eph)
	{
		TimeElement myTime = new TimeElement(myjd, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
		Map<Target,MoonEphemElement> ephem;
		switch (eph.targetBody)
		{
		case Mars:
			ephem = MoonEphem.martianSatellitesEphemerides_2007(myTime, observer, eph);
			break;
		case Jupiter:
			ephem = MoonEphem.galileanSatellitesEphemerides_L1(myTime, observer, eph);
			break;
		case Saturn:
			ephem = MoonEphem.saturnianSatellitesEphemerides_TASS17(myTime, observer, eph, false);
			break;
		case Uranus:
			ephem = MoonEphem.uranianSatellitesEphemerides_GUST86(myTime, observer, eph);
			break;
		default:
			throw new IllegalArgumentException("unsupported body for mutual phenomena. Use Mars, Jupiter, Saturn, or Uranus.");
		}
		return ephem;
	}
	
	/**
	 * Obtains all mutual phenomena. Supported objects are Mars (2007 numerical integration theory), 
	 * Jupiter (JupiterL1 theory), Saturn (TASS 1.7), and Uranus (UranusGUST86).<P>
	 * Results match those obtained by IMCCE with a maximum difference of 1s (PHESAT, Saturn satellites) and
	 * 2-3 minutes (PHEMU, Jupiter satellites). The reason for the poor matching in Jupiter is unknown.
	 * 
	 * @param time Time object for the initial calculation time.
	 * @param observer Observer object.
	 * @param eph Ephemeris object.
	 * @param timef Time oject for the final calculation time.
	 * @param precission Precission in seconds for the search. A good value is 30. Note that
	 * the accuracy of the returned events (1 second) is independent of this parameter. A
	 * value of 30 only means that some events of shorter duration could be missed.
	 * @param all True to return all events including occultations/eclipses by the mother planet itself. Note
	 * that only events in a partial phase will be returned, i.e., the interval while the eclipse/occultation is in progress. 
	 * @return Events visible for the observer.
	 * @ If an error occurs.
	 */
	public static List<EventElement> mutualPhenomena(TimeElement time, ObserverElement observer,
			EphemerisElement eph, TimeElement timef, int precission, boolean all)
	{
		boolean approx = true;
//		MoonEphem.setEphemerisToApproximate(approx);
		ArrayList<EventElement> vector = new ArrayList<EventElement>();
		double jd = TimeScale.getJD(time, observer, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
		double step = (double) precission / EphemConstant.SECONDS_PER_DAY;
		double jdf = TimeScale.getJD(timef, observer, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
		String target = eph.targetBody.toString();
		for (myjd = jd; myjd < jdf; myjd = myjd + step)
		{
			Map<Target,MoonEphemElement> ephem = MoonEvent.getEphem(observer, eph);
			boolean eventExist = false;
			for (Target i : ephem.keySet())
			{
				if (!ephem.get(i).mutualPhenomena.equals("") && (all || ephem.get(i).mutualPhenomena.indexOf(target) < 0) && (!eph.isTopocentric || eph.isTopocentric && ephem.get(i).elevation > 0)) {
					double oldJD = myjd;
					EventElement[] ev = MoonEvent.getMutualEventDetails(observer, eph, step, approx);
					// Since myjd maybe modified by getMutualEventDetails, we need to update the ephem array
					if (myjd < oldJD) myjd = oldJD;
					ephem = MoonEvent.getEphem(observer, eph);

					if (ev != null) {
                        for (EventElement anEv : ev) {
                            if (anEv == null)
                                continue;
//							AstroDate astro = new AstroDate(ev[j].startTime);
//							System.out.println(Target.getName(ev[j].mainBody)+" "+EventElement.EVENTS[ev[j].eventType]+" by "+Target.getName(ev[j].secondaryBody)+" between "+ev[j].startTime+" and "+ev[j].endTime+" (TDB), in date "+astro.getYear()+"-"+astro.getMonth()+"-"+astro.getDay()+". Max percentage "+ev[j].details+"%. "+ev[j].visibleFromEarth+" / "+ev[j].subType);
                            if (all || (!all && anEv.secondaryBody != eph.targetBody)) vector.add(anEv);
                            if ((anEv.endTime + step) > myjd) myjd = anEv.endTime; //+step;
                        }
						eventExist = true;
					}
                }
			}

			double minDist = -1.0;
			if (!eventExist)
			{
				for (Target i:ephem.keySet())
				{
					for (Target j:ephem.keySet())
					{
						if(j.ordinal()<=i.ordinal())
							continue;
						
						MoonEphemElement ephemI = ephem.get(i);
						MoonEphemElement ephemJ = ephem.get(i);
						LocationElement loci = LocationElement.parseRectangularCoordinates(ephemI.xPosition,
								ephemI.yPosition, 0.0);
						LocationElement locj = LocationElement.parseRectangularCoordinates(ephemJ.xPosition,
								ephemJ.yPosition, 0.0);
						double r = LocationElement.getLinearDistance(loci, locj);
						if (r < minDist || minDist < 0.0) minDist = r;
						
						loci = LocationElement.parseRectangularCoordinates(ephemI.xPositionFromSun,
								ephemI.yPositionFromSun, 0.0);
						locj = LocationElement.parseRectangularCoordinates(ephemJ.xPositionFromSun,
								ephemJ.yPositionFromSun, 0.0);
						r = LocationElement.getLinearDistance(loci, locj);
						if (r < minDist || minDist < 0.0) minDist = r;
					}
				}
				if (minDist > 0) myjd += minDist * step * EphemConstant.SECONDS_PER_DAY / 15000.0;
//				System.out.println(myjd + " / "+(100.0 * (myjd - jd) / (jdf - jd)));
			}
		}
		
		return vector;
	}
	
	private static EventElement[] getMutualEventDetails(ObserverElement observer, EphemerisElement eph,
			double mystep, boolean approx)
	{
//		MoonEphem.setEphemerisToApproximate(false);
		boolean started = false;
		boolean eventFound = false; 
		EventElement ev[] = null;
		double step = 2.0 / EphemConstant.SECONDS_PER_DAY;
		myjd = myjd - mystep*0.5;
		double jd = myjd;
		double endTime = 1.0; //1200.0 * 60.0 / Constant.SECONDS_PER_DAY;
		for (myjd = jd; myjd < jd + endTime; myjd = myjd + step)
		{
			TimeElement myTime = new TimeElement(myjd, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
			MoonEphemElement ephem[] = new MoonEphemElement[]{};
			switch (eph.targetBody)
			{
			case Mars:
				ephem = MoonEphem.martianSatellitesEphemerides_2007(myTime, observer, eph).values().toArray(ephem);
				break;
			case Jupiter:
				ephem = MoonEphem.galileanSatellitesEphemerides_L1(myTime, observer, eph).values().toArray(ephem);
				break;
			case Saturn:
				ephem =  MoonEphem.saturnianSatellitesEphemerides_TASS17(myTime, observer, eph, false).values().toArray(ephem);
				break;
			case Uranus:
				ephem = MoonEphem.uranianSatellitesEphemerides_GUST86(myTime, observer, eph).values().toArray(ephem);
				break;
			default:
				throw new IllegalArgumentException("unsupported body for mutual phenomena. Use Mars, Jupiter, Saturn, or Uranus.");
			}
			if (!started) {
				ev = new EventElement[ephem.length];
				started = true;
			}

			boolean eventPersists = false;
			for (int i = 0; i<ephem.length; i++)
			{
				if (!ephem[i].mutualPhenomena.equals("")) {
					eventPersists = true;
					if (myjd == jd) {
						myjd = myjd - step; //2 * mystep;
						jd = myjd;
						myjd -= step;
						if (!skipWarnings) System.err.println("an event ("+ephem[i].name + " " + ephem[i].mutualPhenomena+") was found on the initial calculation time. " +
								"Initial calculation time was moved back to "+myjd+" (TDB).");
					} else {
						if (ev[i] == null) {
							eventFound = true;
							int phenom = EventElement.EVENT_OCCULTED;
							if (ephem[i].mutualPhenomena.toLowerCase().indexOf("eclipse") >= 0) phenom = EventElement.EVENT_ECLIPSED;
							String second = ephem[i].mutualPhenomena.substring(ephem[i].mutualPhenomena.toLowerCase().indexOf("by ")+2).trim();
							String per = second.substring(second.indexOf("(") + 1, second.indexOf("%"));
							second = second.substring(0, second.indexOf("(")).trim();
							ev[i] = new EventElement(myjd - step * 0.5, -1.0, Target.valueOf(ephem[i].name), Target.valueOf(second), phenom, per);
							ev[i].elevation = ephem[i].elevation;
							if (ephem[i].eclipsed && phenom == EventElement.EVENT_OCCULTED) ev[i].visibleFromEarth = false;						
							if (ephem[i].occulted && phenom == EventElement.EVENT_ECLIPSED) ev[i].visibleFromEarth = false;						
						} else {
							String second = ephem[i].mutualPhenomena.substring(ephem[i].mutualPhenomena.toLowerCase().indexOf("by ")+2).trim();
							String per = second.substring(second.indexOf("(") + 1, second.indexOf("%"));
							if (Double.parseDouble(per) > Double.parseDouble(ev[i].details)) ev[i].details = per;
						}
					}
				} else {
					if (ev[i] != null) {
						if (ev[i].endTime == -1.0) {
							ev[i].endTime = myjd - step * 0.5;
							if (ephem[i].occulted && ev[i].eventType == EventElement.EVENT_OCCULTED) ev[i].subType = EventElement.SUBEVENT_START;						
							if (!ephem[i].occulted && ev[i].eventType == EventElement.EVENT_OCCULTED) ev[i].subType = EventElement.SUBEVENT_END;						
							if (ephem[i].eclipsed && ev[i].eventType == EventElement.EVENT_ECLIPSED) ev[i].subType = EventElement.SUBEVENT_START;						
							if (!ephem[i].eclipsed && ev[i].eventType == EventElement.EVENT_ECLIPSED) ev[i].subType = EventElement.SUBEVENT_END;						
						}
					}
				}
			}

			if (!eventPersists && eventFound) break;
		}
		return ev;
	}
	
//	/**
//	 * Obtains the light curve of a given mutual event.
//	 * @param event Event to calculate.
//	 * @param outputTimeScale Time scale for the x axis. Constants define in {@linkplain TimeElement}.
//	 * @param magnitude True to represent magnitude instead of non-eclipsed/oculted disk fraction.
//	 * @param combined True to represent combined magnitudes of the two satellites. If the chart represents
//	 * non-eclipsed/oculted disk fraction, this parameter will have no effect.
//	 * @return The chart.
//	 * @ If an error occurs.
//	 */
//	public static CreateChart lightCurve(EventElement event, TimeElement.Scale outputTimeScale, boolean magnitude, boolean combined)
//	 {
//		Target mother = Target.getCentralBody(event.mainBody);
//
//		ObserverElement observer = ObserverElement.MADRID;
//		EphemerisElement eph = new EphemerisElement(mother, EphemerisElement.Ephem.APPARENT,
//				EphemerisElement.EQUINOX_OF_DATE, EphemerisElement.GEOCENTRIC, Precession.Method.IAU2000,
//				EphemerisElement.Frame.ICRS);
//
////		boolean approx = false;
////		MoonEphem.setEphemerisToApproximate(approx);
//		ArrayList<double[]> vector = new ArrayList<double[]>();
//		double duration = event.endTime - event.startTime;
//		TimeElement time = new TimeElement(event.startTime - duration / 10.0, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
//		TimeElement timef = new TimeElement(event.endTime + duration / 10.0, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
//		double jd = TimeScale.getJD(time, observer, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
//		double jdf = TimeScale.getJD(timef, observer, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
//		//int precission = 2;
//		double step = (jdf - jd) / 100.0; //(double) precission / Constant.SECONDS_PER_DAY;
//		
//		Target index = null, index2 = null;
//		for (myjd = jd; myjd < jdf; myjd = myjd + step)
//		{
//			TimeElement myTime = new TimeElement(myjd, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
//			Map<Target,MoonEphemElement> ephem;
//			switch (eph.targetBody)
//			{
//			case Mars:
//				ephem = MoonEphem.martianSatellitesEphemerides_2007(myTime, observer, eph);
//				break;
//			case Jupiter:
//				ephem = MoonEphem.galileanSatellitesEphemerides_L1(myTime, observer, eph);
//				break;
//			case Saturn:
//				ephem = MoonEphem.saturnianSatellitesEphemerides_TASS17(myTime, observer, eph, false);
//				break;
//			case Uranus:
//				ephem = MoonEphem.uranianSatellitesEphemerides_GUST86(myTime, observer, eph);
//				break;
//			default:
//				throw new IllegalArgumentException("unsupported body for mutual phenomena. Use Mars, Jupiter, Saturn, or Uranus.");
//			}
//
//			boolean eventExist = false;
//			if (index == null || index2 == null) {
//				for (Target i :ephem.keySet())
//				{
//					if (ephem.get(i).name.equals(event.mainBody.name())) index = i;
//					if (ephem.get(i).name.equals(event.secondaryBody.name())) index2 = i;
//				}
//			}
//			for (Target i :ephem.keySet())
//			{
//				MoonEphemElement ephemI = ephem.get(i);
//				if (!ephemI.mutualPhenomena.equals("") && ephemI.name.equals(event.mainBody.name())) {
//					String second = ephemI.mutualPhenomena.substring(ephemI.mutualPhenomena.toLowerCase().indexOf("by ")+2).trim();
//					String per = second.substring(second.indexOf("(") + 1, second.indexOf("%"));
//					double fraction = 1.0 - Double.parseDouble(per) / 100.0;
//					if (magnitude) {
//						if (combined) vector.add(new double[] {myjd, Star.combinedMagnitude(ephemI.magnitude, ephemI.magnitude)});
//						if (!combined) vector.add(new double[] {myjd, ephemI.magnitude});
//					} else {
//						vector.add(new double[] {myjd, fraction});
//					}
//					eventExist = true;
//				}
//			}
//			if (!eventExist) {
//				if (magnitude) {
//					if (combined) vector.add(new double[] {myjd, Star.combinedMagnitude(ephem.get(index).magnitude, ephem.get(index2).magnitude)});					
//					if (!combined) vector.add(new double[] {myjd, ephem.get(index).magnitude});					
//				} else {
//					if (index !=null) {
//						if (ephem.get(index).eclipsed || ephem.get(index).occulted)
//						{
//							vector.add(new double[] {myjd, 0.0});
//						} else {
//							vector.add(new double[] {myjd, 1.0});							
//						}
//					}
//					vector.add(new double[] {myjd, 1.0});
//				}
//			}
//		}
//		
////		MoonEphem.setEphemerisToApproximate(false);
//
//		String x[] = new String[vector.size()];
//		String y[] = new String[vector.size()];
//		double dy[] = new double[vector.size()];
//		for (int i=0; i<vector.size(); i++)
//		{
//			double set[] = vector.get(i);
//			TimeElement myTime = new TimeElement(set[0], TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
//			double outjd = TimeScale.getJD(myTime, observer, eph, outputTimeScale);
//			x[i] = ""+outjd;
//			y[i] = ""+set[1];
//			dy[i] = 0.005;
//		}
//		ChartSeriesElement chartSeries = new ChartSeriesElement(x, y, null, dy,
//				"Visible disk fraction", false, Color.BLACK, ChartSeriesElement.SHAPE_CIRCLE,
//				ChartSeriesElement.REGRESSION_NONE);
//		chartSeries.showLines = true;
//		chartSeries.showErrorBars = false;
//		ChartSeriesElement series[] = new ChartSeriesElement[] {chartSeries};
//		ChartElement chart = new ChartElement(series, ChartElement.TYPE_XY_CHART, ChartElement.SUBTYPE_XY_SCATTER,
//				event.mainBody.name()+" "+EventElement.EVENTS[event.eventType]+" by "+" "+event.secondaryBody.name()+". Light curve ", 
//				"Julian day"+" ("+outputTimeScale+")", "Visible disk fraction"+" "+" of "+
//				" "+event.mainBody.name(), false, 400, 400);
//
//		if (magnitude) chart.yLabel = "Magnitude of "+" "+event.mainBody.name();
//		if (magnitude && combined) chart.yLabel = "Combined magnitude of  "+event.mainBody.name()+" and "+event.secondaryBody.name();
//		
//		return new CreateChart(chart);
//	}
//	
//	/**
//	 * Returns the longitude of the Great Red Spot (GRS), using historical observations.
//	 * Data available only since 1969. For previous dates and dates after last update a
//	 * linear extrapolation is used. Data is regularly updated from Sky & Telescope and ALPO.
//	 * @param JD_TDB Julian day.
//	 * @return GRS longitude (system II), in radians.
//	 * @ If an error occurs.
//	 */
//	public static double getJupiterGRSLongitude(double JD_TDB)
//	 {
//		String jarpath = FileIO.DATA_ORBITAL_ELEMENTS_DIRECTORY + "JupiterGRS.txt";
//		String file[] = DataSet.arrayListToStringArray(ReadFile.readAnyFile(FileIO.DATA_ORBITAL_ELEMENTS_JARFILE, jarpath));
//		
//		double jd[] = new double[file.length];
//		double lon[] = new double[file.length];
//		for (int i=0; i<file.length; i++)
//		{
//			String date = FileIO.getField(1, file[i], ",", true);
//			int year = Integer.parseInt(FileIO.getField(1, date, "-", true));
//			int month = Integer.parseInt(FileIO.getField(2, date, "-", true));
//			int day = Integer.parseInt(FileIO.getField(3, date, "-", true));
//			jd[i] = new AstroDate(year, month, day).jd();
//			lon[i] = Double.parseDouble(FileIO.getField(2, file[i], ",", true));
//		}
//		
//		Interpolation i = new Interpolation(jd, lon, true);
//		double lonGRS = i.linearInterpolation(JD_TDB);
//		return lonGRS * EphemConstant.DEG_TO_RAD;
//	}
//	
//	/**
//	 * Returns the instant of the next transit of the Great Red Spot.
//	 * Calculations are performed by default for the geocenter, using
//	 * Moshier algorithms and latest IAU resolutions.
//	 * @param JD_TDB Input dynamical time as a Julian day.
//	 * @return Output dynamical time of the next transit, with a theorical
//	 * precission better than one second (obviously unrealistic).
//	 * @ If an error occurs.
//	 */
//	public static double getJupiterGRSNextTransitTime(double JD_TDB){
//		ObserverElement observer = ObserverElement.MADRID;
//		EphemerisElement eph = new EphemerisElement(Target.Jupiter, EphemerisElement.Ephem.APPARENT,
//				EphemerisElement.EQUINOX_OF_DATE, EphemerisElement.GEOCENTRIC, Precession.Method.IAU2000,
//				EphemerisElement.Frame.ICRS);
//
//		double prec = 1.0 / EphemConstant.SECONDS_PER_DAY;
//		double jd;
//		for (jd = JD_TDB; jd < JD_TDB + 1.0; jd = jd + prec)
//		{
//			AstroDate astro = new AstroDate(jd);
//			TimeElement time = new TimeElement(astro, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
//			EphemElement ephem = Ephem.getEphemeris(time, observer, eph, false);
//			double GRS_lon = MoonEvent.getJupiterGRSLongitude(JD_TDB);
//			
//			double dif = GRS_lon - ephem.longitudeOfCentralMeridianSystemII;
//			dif = EphemUtils.normalizeRadians(dif);
//			double dt = dif * 9.9 / (24.0 * 2.0 * Math.PI); // 9.6 before, not 9.9
//			jd = jd + dt * 0.5;
//			if (dt < prec) {
//				jd = jd + dt * 0.5;
//				break;
//			}
//		}
//		return jd;
//	}

	/**
	 * Obtains all non-mutual phenomena (eclipses, occultations, transits, shadow transits).
	 * Supported objects are Mars (2007 numerical integration theory), 
	 * Jupiter (JupiterL1 theory), Saturn (TASS 1.7), and Uranus (UranusGUST86).
	 * @param time Time object for the initial calculation time.
	 * @param observer Observer object.
	 * @param eph Ephemeris object.
	 * @param timef Time oject for the final calculation time.
	 * @param precission Precission in seconds for the search. A good value is 100. 
	 * @param accuracy This will be the accuracy in seconds of the returned event times. 
	 * A good value is 10-30s, but can be reduced for Mars.
	 * @return Events visible for the observer.
	 * @ If an error occurs.
	 */
	public static List<EventElement> phenomena(TimeElement time, ObserverElement observer,
			EphemerisElement eph, TimeElement timef, int precission, int accuracy)
	 {
		boolean approx = true;
//		MoonEphem.setEphemerisToApproximate(approx);
		List<EventElement> vector = new ArrayList<EventElement>();
		double jd = TimeScale.getJD(time, observer, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
		double step = (double) precission / EphemConstant.SECONDS_PER_DAY;
		double step2 = (double) accuracy * 2.0 / EphemConstant.SECONDS_PER_DAY;
		double jdf = TimeScale.getJD(timef, observer, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
		
		for (myjd = jd; myjd < jdf; myjd = myjd + step)
		{
//			System.out.println(myjd+"/"+jdf+"/"+step+"/"+(jdf-myjd)/step);
			Map<Target,MoonEphemElement> ephem = MoonEvent.getEphem(observer, eph);
			boolean eventExist = false;
			for (Target i: ephem.keySet())
			{
				MoonEphemElement ephemI = ephem.get(i);
				if ((ephemI.occulted || ephemI.eclipsed || ephemI.transiting || ephemI.shadowTransiting) && (!eph.isTopocentric || eph.isTopocentric && ephemI.elevation > 0)) {
					eventExist = true;
					double oldJD = myjd;
					List<EventElement> ev = MoonEvent.getEventDetails(observer, eph, step, step2, approx);
					// Since myjd maybe modified by getEventDetails, we need to update the ephem array
					ephem = MoonEvent.getEphem(observer, eph);
					if (myjd < oldJD) myjd = oldJD;
					
					if (ev != null) {
						for (EventElement j:ev)
						{
							
//							AstroDate astro = new AstroDate(ev[j].startTime);
//							System.out.println(Target.getName(ev[j].mainBody)+" "+EventElement.EVENTS[ev[j].eventType]+" by "+Target.getName(ev[j].secondaryBody)+" between "+ev[j].startTime+" and "+ev[j].endTime+" (TDB), in date "+astro.getYear()+"-"+astro.getMonth()+"-"+astro.getDay());
							vector.add(j);
							if ((j.endTime+step) > myjd) myjd = j.endTime+step;
						}
					}
                }
			}
			
			double minDist = -1.0;
			if (!eventExist)
			{
				for (Target i:ephem.keySet())
				{
					MoonEphemElement ephemI = ephem.get(i);
					LocationElement loci = LocationElement.parseRectangularCoordinates(ephemI.xPosition,
							ephemI.yPosition, 0.0);
					LocationElement locj = LocationElement.parseRectangularCoordinates(0.0, 0.0, 0.0);
					double r = LocationElement.getLinearDistance(loci, locj);
					if (r < minDist || minDist < 0.0) minDist = r;
					
					loci = LocationElement.parseRectangularCoordinates(ephemI.xPositionFromSun,
							ephemI.yPositionFromSun, 0.0);
					locj = LocationElement.parseRectangularCoordinates(0.0, 0.0, 0.0);
					r = LocationElement.getLinearDistance(loci, locj);
					if (r < minDist || minDist < 0.0) minDist = r;
				}
				if (minDist > 1.0)
					myjd += (minDist - 1.0) * step * EphemConstant.SECONDS_PER_DAY / 15000.0;
//				System.out.println(myjd + " / "+(100.0 * (myjd - jd) / (jdf - jd)));
			}

		}
		
		
		return vector;
	}
	
	private static List<EventElement> getEventDetails(ObserverElement observer, EphemerisElement eph,
			double mystep, double step, boolean approx)
	 {
//		MoonEphem.setEphemerisToApproximate(false);
		Map<Target,MoonEphemElement> ephem;
		boolean started = false;
		boolean eventFound = false; 
		Map<Target,EventElement> ev0 = null, ev1 = null;
		//double step = mystep; //1.0 / Constant.SECONDS_PER_DAY;
		myjd = myjd - mystep; //* 1.5;
		double jd = myjd;
		double endTime = 1.0; //1200.0 * 60.0 / Constant.SECONDS_PER_DAY;
		for (myjd = jd; myjd < jd + endTime; myjd = myjd + step)
		{
			TimeElement myTime = new TimeElement(myjd, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);			
			switch (eph.targetBody)
			{
			case Mars:
				ephem = MoonEphem.martianSatellitesEphemerides_2007(myTime, observer, eph);
				break;
			case Jupiter:
				ephem = MoonEphem.galileanSatellitesEphemerides_L1(myTime, observer, eph);
				break;
			case Saturn:
				ephem = MoonEphem.saturnianSatellitesEphemerides_TASS17(myTime, observer, eph, false);
				break;
			case Uranus:
				ephem = MoonEphem.uranianSatellitesEphemerides_GUST86(myTime, observer, eph);
				break;
			default:
				throw new IllegalArgumentException("unsupported body for phenomena. Use Mars, Jupiter, Saturn, or Uranus.");
			}
			if (!started) {
				ev0 = new LinkedHashMap<Target, EventElement>();
				ev1 = new LinkedHashMap<Target, EventElement>();
				started = true;
			}

			boolean eventPersists = false;
			String eventList = "";
			for (Target i:ephem.keySet())
			{
				MoonEphemElement ephemI = ephem.get(i);
				String ph = "";
				if (ephemI.occulted) ph += "O";
				if (ephemI.eclipsed) ph += "E";
				if (ephemI.transiting) ph += "T";
				if (ephemI.shadowTransiting) ph += "S";
				eventList += ph + ",";

				if (ephemI.occulted || ephemI.eclipsed || ephemI.transiting || ephemI.shadowTransiting) {
					if (myjd == jd) {
						myjd = myjd - 2.0 * mystep;
						jd = myjd;
						if (!skipWarnings) System.err.println("an event was found on the initial calculation time. " +
								"Initial calculation time was moved back to "+myjd+" (TDB).");
						myjd -= step;
					} else {						
						eventPersists = true;
						if (ev0.get(i) == null && ephemI.occulted) {
							eventFound = true;
							int phenom = EventElement.EVENT_OCCULTED;
							ev0.put(i,new EventElement(myjd - step * 0.5, -1.0, Target.valueOf(ephemI.name), eph.targetBody, phenom, ""));
							ev0.get(i).elevation = ephemI.elevation;
							if (ephemI.eclipsed) ev0.get(i).visibleFromEarth = false;						
						}
						if (ev1.get(i) == null && ephemI.eclipsed) {
							eventFound = true;
							int phenom = EventElement.EVENT_ECLIPSED;
							ev1.put(i, new EventElement(myjd - step * 0.5, -1.0, Target.valueOf(ephemI.name), eph.targetBody, phenom, ""));
							ev1.get(i).elevation = ephemI.elevation;
							if (ephemI.occulted) ev0.get(i).visibleFromEarth = false; //TODO maybe ev1 should be here						
						}
						
						if (ev0.get(i) == null && ephemI.transiting) {
							eventFound = true;
							int phenom = EventElement.EVENT_TRANSIT;
							ev0.put(i, new EventElement(myjd - step * 0.5, -1.0, Target.valueOf(ephemI.name), eph.targetBody, phenom, ""));
							ev0.get(i).elevation = ephemI.elevation;
						}
						if (ev1.get(i) == null && ephemI.shadowTransiting) {
							eventFound = true;
							int phenom = EventElement.EVENT_SHADOW_TRANSIT;
							ev1.put(i, new EventElement(myjd - step * 0.5, -1.0, Target.valueOf(ephemI.name), eph.targetBody, phenom, ""));
							ev1.get(i).elevation = ephemI.elevation;
						}
					}

					if (ev0.get(i) != null && !ephemI.occulted && !ephemI.transiting) {
						if (ev0.get(i).endTime == -1.0) {
							ev0.get(i).endTime = myjd - step * 0.5;
						}
					}
					if (ev1.get(i) != null && !ephemI.eclipsed && !ephemI.shadowTransiting) {
						if (ev1.get(i).endTime == -1.0) {
							ev1.get(i).endTime = myjd - step * 0.5;
						}
					}
					
				}
				if (ev0.get(i) != null && !ephemI.occulted && !ephemI.transiting) {
					if (ev0.get(i).endTime == -1.0) {
						ev0.get(i).endTime = myjd - step * 0.5;
					}
				}
				if (ev1.get(i) != null && !ephemI.eclipsed && !ephemI.shadowTransiting) {
					if (ev1.get(i).endTime == -1.0) {
						ev1.get(i).endTime = myjd - step * 0.5;
					}
				}
			}
			
			if (!eventPersists && eventFound) break;
			
			// Acelerate calculations
			if (eventPersists) {
				double timeStep = 20 * step;
				double step2 = 20.0 / EphemConstant.SECONDS_PER_DAY;
				if (timeStep < step2) timeStep = step2;
				myTime = new TimeElement(myjd + timeStep, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
				switch (eph.targetBody)
				{
				case Mars:
					ephem = MoonEphem.martianSatellitesEphemerides_2007(myTime, observer, eph);
					break;
				case Jupiter:
					ephem = MoonEphem.galileanSatellitesEphemerides_L1(myTime, observer, eph);
					break;
				case Saturn:
					ephem = MoonEphem.saturnianSatellitesEphemerides_TASS17(myTime, observer, eph, false);
					break;
				case Uranus:
					ephem = MoonEphem.uranianSatellitesEphemerides_GUST86(myTime, observer, eph);
					break;
				default:
					throw new IllegalArgumentException("unsupported body for phenomena. Use Mars, Jupiter, Saturn, or Uranus.");
				}
				String newEventList = "";
				for (Target i :ephem.keySet())
				{
					MoonEphemElement ephemI = ephem.get(i);
					String ph = "";
					if (ephemI.occulted) ph += "O";
					if (ephemI.eclipsed) ph += "E";
					if (ephemI.transiting) ph += "T";
					if (ephemI.shadowTransiting) ph += "S";
					newEventList += ph + ",";
				}
				if (newEventList.equals(eventList)) myjd += timeStep;
			}
		}
			
		List<EventElement> out = new ArrayList<EventElement>();

		out.addAll(ev0.values());
		out.addAll(ev1.values());
		//TODO remove duplicates
//		MoonEphem.setEphemerisToApproximate(approx);
		return out;
	}

	/**
	 * For rade2Vector testing only.
	 */
	public static void main1 (String args[])
	{
		System.out.println("MoonEvent test - mutual phenomena");

//			Translate.setDefaultLanguage(Translate.LANGUAGE_SPANISH);
//			AstroDate astro = new AstroDate(2007, AstroDate.JUNE, 1, 23, 0, 0);
//			double jd = MoonEvent.getJupiterGRSNextTransitTime(astro.jd());
//			System.out.println(jd+"/"+astro.jd());
			
			double delta = 1.0 / (24.0 * 60.0);
			
	//		AstroDate astro = new AstroDate(2448318.3326 + 0.0046 + delta * 0.0); //2003, AstroDate.JANUARY, 6, 23, 0, 0);
//			AstroDate astroi = new AstroDate(2448215.7097 + 0.0055 + delta * 0.0); //2003, AstroDate.JANUARY, 6, 23, 0, 0);
	//		AstroDate astro = new AstroDate(2448325.3686 + 0.0053 - delta * 0 * 2.8); //2003, AstroDate.JANUARY, 6, 23, 0, 0);

//			AstroDate astrof = new AstroDate(2448348.451 + 0.005 + delta * 0.0); //2003, AstroDate.JANUARY, 6, 23, 0, 0);
//			AstroDate astrof = new AstroDate(2448216.5); //2003, AstroDate.JANUARY, 6, 23, 0, 0);
			
	//		AstroDate astro = new AstroDate(2452751.386 + 0.009 + delta * 0.0); //2003, AstroDate.JANUARY, 6, 23, 0, 0);
	//		AstroDate astro = new AstroDate(2452646.466 + 0.014 + delta * 0.0); //2003, AstroDate.JANUARY, 6, 23, 0, 0);
	//		AstroDate astro = new AstroDate(2452717.352 + 0.014 - delta * 0.0); //2003, AstroDate.JANUARY, 6, 23, 0, 0);

			AstroDate astroi = new AstroDate(2011, AstroDate.JANUARY, 1, 0, 0, 0);
			AstroDate astrof = new AstroDate(2012, AstroDate.JANUARY, 1, 0, 0, 0);

			TimeElement timei = new TimeElement(astroi.toGCalendar(), TimeElement.Scale.UNIVERSAL_TIME_UTC);
			TimeElement timef = new TimeElement(astrof.toGCalendar(), TimeElement.Scale.UNIVERSAL_TIME_UTC);
			ObserverElement observer = ObserverElement.MADRID;
			
			EphemerisElement eph = new EphemerisElement(Target.Jupiter, EphemerisElement.Ephem.APPARENT,
					EphemerisElement.EQUINOX_OF_DATE, EphemerisElement.GEOCENTRIC, Precession.Method.CAPITAINE,
					EphemerisElement.Frame.ICRS);


			List<EventElement> ev= MoonEvent.mutualPhenomena(timei, observer, eph, timef, 30, false);
			System.out.println("FOUND EVENTS");
			for (EventElement evi :ev)
			{
				AstroDate astroI = new AstroDate(evi.startTime);
				TimeElement timeI = new TimeElement(astroI, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
				double jdI = TimeScale.getJD(timeI, observer, eph, TimeElement.Scale.UNIVERSAL_TIME_UT1);
				AstroDate astroI2 = new AstroDate(jdI);
				String eDateI = astroI2.getYear()+"-"+astroI2.getMonth()+"-"+astroI2.getDay();
				String eTimeI = astroI2.getHour()+":"+astroI2.getMinute()+":"+(int) (astroI2.getSecond()+0.5);
				
				AstroDate astroF = new AstroDate(evi.endTime);
				TimeElement timeF = new TimeElement(astroF, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
				double jdF = TimeScale.getJD(timeF, observer, eph,  TimeElement.Scale.UNIVERSAL_TIME_UT1);
				AstroDate astro2F = new AstroDate(jdF);
				String eDateF = astro2F.getYear()+"-"+astro2F.getMonth()+"-"+astro2F.getDay();
				String eTimeF = astro2F.getHour()+":"+astro2F.getMinute()+":"+(int) (astro2F.getSecond()+0.5);

				String visible = "YES";
				if ((evi.elevation * EphemConstant.RAD_TO_DEG) < 20) visible = "DIFFICULT";
				if ((evi.elevation * EphemConstant.RAD_TO_DEG) < 5) visible = "NO";
				if (!evi.visibleFromEarth) visible = "NO*";
				System.out.println(evi.mainBody.name()+" & "+EventElement.EVENTS[evi.eventType]+" & "+evi.secondaryBody.name()+" & "+eDateI+" & "+eTimeI+" & "+eTimeF+" & "+(int) (Double.parseDouble(evi.details)+0.5)+" & "+visible);
//				CreateChart ch = MoonEvent.lightCurve(evi, TimeElement.UNIVERSAL_TIME_UTC, true);
//				ch.showChartInJFreeChartPanel();
			}
	}
	
	/**
	 * For rade2Vector testing only.
	 */
	public static void main (String args[])
	{
		System.out.println("MoonEvent test - phenomena");

			AstroDate astroi = new AstroDate(2011, AstroDate.JANUARY, 1, 0, 0, 0);
			AstroDate astrof = new AstroDate(2011, AstroDate.JANUARY, 2, 0, 0, 0);

			TimeElement timei = new TimeElement(astroi.toGCalendar(), TimeElement.Scale.LOCAL_TIME);
			TimeElement timef = new TimeElement(astrof.toGCalendar(), TimeElement.Scale.LOCAL_TIME);

			ObserverElement observer = ObserverElement.MADRID;
			EphemerisElement eph = new EphemerisElement(Target.Jupiter, EphemerisElement.Ephem.APPARENT,
					EphemerisElement.EQUINOX_OF_DATE, EphemerisElement.GEOCENTRIC, Precession.Method.CAPITAINE,
					EphemerisElement.Frame.ICRS);

			List<EventElement> ev = MoonEvent.phenomena(timei, observer, eph, timef, 100, 10);
			System.out.println("FOUND EVENTS");
			for (EventElement evI :ev)
			{
				AstroDate astroI = new AstroDate(evI.startTime);
				TimeElement timeI = new TimeElement(astroI, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
				double jdI = TimeScale.getJD(timeI, observer, eph, TimeElement.Scale.LOCAL_TIME);
				AstroDate astroI2 = new AstroDate(jdI);
				String eDateI = TimeFormat.formatJulianDayAsDate(jdI); //astroI2.getYear()+"-"+astroI2.getMonth()+"-"+astroI2.getDay();
				String eTimeI = astroI2.getHour()+":"+astroI2.getMinute()+":"+(int) (astroI2.getSecond()+0.5);
				
				AstroDate astroF = new AstroDate(evI.endTime);
				TimeElement timeF = new TimeElement(astroF, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
				double jdF = TimeScale.getJD(timeF, observer, eph, TimeElement.Scale.LOCAL_TIME);
				AstroDate astro2F = new AstroDate(jdF);
				//String eDateF = astro2F.getYear()+"-"+astro2F.getMonth()+"-"+astro2F.getDay();
				String eTimeF = astro2F.getHour()+":"+astro2F.getMinute()+":"+(int) (astro2F.getSecond()+0.5);

				String visible = "YES";
				if ((evI.elevation * EphemConstant.RAD_TO_DEG) < 20) visible = "DIFFICULT";
				if ((evI.elevation * EphemConstant.RAD_TO_DEG) < 5) visible = "NO";
				if (!evI.visibleFromEarth) visible = "NO*";
				System.out.println(evI.mainBody.name()+" & "+EventElement.EVENTS[evI.eventType]+" & "+evI.secondaryBody.name()+" & "+eDateI+" & "+eTimeI+" & "+eTimeF+" & "+evI.details+" & "+visible);
				if (visible.equals("YES")) System.out.println(evI.mainBody.name()+" & "+EventElement.EVENTS[evI.eventType]+" & "+eDateI+" & "+eTimeI+" & "+eTimeF+" & "+evI.details);
//				CreateChart ch = MoonEvent.lightCurve(ev[i], TimeElement.UNIVERSAL_TIME_UTC, true);
//				ch.showChartInJFreeChartPanel();
			}
	}

	/*
Io & Transiting & Jupiter & 2009-1-15 & 4:58:55 & 7:19:35 & 
Io & Shadow transiting & Jupiter & 2009-1-15 & 5:8:15 & 7:29:15 & 
Europa & Transiting & Jupiter & 2009-1-15 & 4:15:35 & 7:11:15 & 
Europa & Shadow transiting & Jupiter & 2009-1-15 & 4:34:55 & 7:30:55 & 

Ganymede Eclipsed by Europa between 2454845.102967196 and 2454845.126335125 (TDB). Max percentage 8.297671078170813%.
Ganymede Occulted by Europa between 2454845.162161179 and 2454845.1719759405 (TDB). Max percentage 0.36119453228943493%.
Ganymede Eclipsed by Europa between 2454851.997310898 and 2454852.006882605 (TDB). Max percentage 0.6240102367514662%.
Ganymede Occulted by Callisto between 2454917.739249511 and 2454917.743300415 (TDB). Max percentage 5.433097883260895%.
Io Occulted by Callisto between 2454918.629225297 and 2454918.6318294494 (TDB). Max percentage 15.078785449680849%.
Callisto Occulted by Io between 2454926.808984695 and 2454926.8165309504 (TDB). Max percentage 57.91962083012325%.
Callisto Occulted by Io between 2454927.2849362125 and 2454927.295850505 (TDB). Max percentage 57.78720592588158%.
Callisto Occulted by Io between 2454927.9166656714 and 2454927.922336937 (TDB). Max percentage 43.99118020596609%.
Ganymede Occulted by Io between 2454931.497952492 and 2454931.5007765507 (TDB). Max percentage 6.135342436242531%.
Io Occulted by Ganymede between 2454934.537350826 and 2454934.5396772022 (TDB). Max percentage 15.260193461034024%.
Europa Occulted by Callisto between 2454934.765574302 and 2454934.7751575834 (TDB). Max percentage 70.66507662441262%.
Io Occulted by Callisto between 2454936.046640253 and 2454936.050529121 (TDB). Max percentage 67.98032846267805%.
Europa Occulted by Callisto between 2454936.700631513 and 2454936.7093120213 (TDB). Max percentage 99.3777731729035%.
Ganymede Occulted by Callisto between 2454937.1239394913 and 2454937.1320297252 (TDB). Max percentage 37.53727566997487%.
Ganymede Occulted by Io between 2454938.624872508 and 2454938.628761376 (TDB). Max percentage 26.7875845602143%.
Io Occulted by Ganymede between 2454941.657282875 and 2454941.6609286885 (TDB). Max percentage 64.7998336915981%.
Callisto Occulted by Europa between 2454943.3935353304 and 2454943.3987436355 (TDB). Max percentage 24.241481560666035%.
Europa Occulted by Io between 2454944.3495812393 and 2454944.350310402 (TDB). Max percentage 0.704448202188062%.
Ganymede Occulted by Io between 2454945.7474755393 and 2454945.7515958874 (TDB). Max percentage 48.4646238528211%.
Ganymede Occulted by Europa between 2454945.9741139906 and 2454945.976845457 (TDB). Max percentage 5.409762053104898%.
Europa Occulted by Io between 2454947.8970051673 and 2454947.898741269 (TDB). Max percentage 9.984105148866268%.
Io Occulted by Ganymede between 2454948.7783598904 and 2454948.7826654227 (TDB). Max percentage 100.00000000000003%.
Europa Occulted by Io between 2454951.444601782 and 2454951.4468702883 (TDB). Max percentage 23.443025671345495%.
Europa Occulted by Callisto between 2454952.0272583417 and 2454952.0290523134 (TDB). Max percentage 3.9027694114977556%.
Ganymede Occulted by Io between 2454952.8668960556 and 2454952.8707964974 (TDB). Max percentage 48.47323389145919%.
Ganymede Occulted by Europa between 2454953.1147560338 and 2454953.1187722157 (TDB). Max percentage 22.561834104135727%.
Io Occulted by Europa between 2454953.7129933676 and 2454953.714254935 (TDB). Max percentage 3.086963474325695%.
Europa Occulted by Io between 2454954.992079373 and 2454954.9947182476 (TDB). Max percentage 38.82509089483746%.
Europa Occulted by Ganymede between 2454955.7074841773 and 2454955.71003046 (TDB). Max percentage 10.298233823404507%.
Io Occulted by Ganymede between 2454955.900691888 and 2454955.9053677884 (TDB). Max percentage 100.00000000000003%.
Io Occulted by Europa between 2454957.259831802 and 2454957.261741514 (TDB). Max percentage 12.167544351221949%.
Europa Occulted by Io between 2454958.539531008 and 2454958.5424360847 (TDB). Max percentage 54.92012319108329%.
Ganymede Occulted by Io between 2454959.983874243 and 2454959.9871496884 (TDB). Max percentage 26.506324863039115%.
Ganymede Occulted by Europa between 2454960.2535667983 and 2454960.2580227926 (TDB). Max percentage 35.44900102017415%.
Io Occulted by Europa between 2454960.806544856 and 2454960.8088249364 (TDB). Max percentage 23.975671111933504%.
Europa Occulted by Io between 2454962.0867989957 and 2454962.089923979 (TDB). Max percentage 70.99183450020774%.
Europa Occulted by Ganymede between 2454962.8474358404 and 2454962.851938131 (TDB). Max percentage 64.00937379036564%.
Io Occulted by Ganymede between 2454963.0255862908 and 2454963.0304820975 (TDB). Max percentage 73.60340559912731%.
Io Occulted by Europa between 2454964.3530360837 and 2454964.355547644 (TDB). Max percentage 37.306775721285135%.
Europa Occulted by Io between 2454965.634030391 and 2454965.637305836 (TDB). Max percentage 86.21933049934722%.
Ganymede Occulted by Io between 2454967.098928024 and 2454967.100999772 (TDB). Max percentage 5.876548752554518%.
Ganymede Occulted by Europa between 2454967.390313678 and 2454967.3947233763 (TDB). Max percentage 35.4549129004174%.
Ganymede Eclipsed by Callisto between 2454967.6734413365 and 2454967.6773996484 (TDB). Max percentage 5.76159693949048%.
Io Occulted by Europa between 2454967.899260615 and 2454967.9019226376 (TDB). Max percentage 51.40293716581493%.
Io Eclipsed by Callisto between 2454968.2792818374 and 2454968.2842818103 (TDB). Max percentage 53.84404562171103%.
Io Eclipsed by Callisto between 2454968.949310016 and 2454968.9601780125 (TDB). Max percentage 77.44072676051867%.
Europa Occulted by Io between 2454969.181045398 and 2454969.1844365834 (TDB). Max percentage 98.8407820939479%.
Io Eclipsed by Callisto between 2454969.381540513 and 2454969.3862511357 (TDB). Max percentage 11.149062957467875%.
Europa Occulted by Ganymede between 2454969.986439298 and 2454969.991774917 (TDB). Max percentage 100.00000000000003%.
Io Occulted by Ganymede between 2454970.1543194214 and 2454970.1594930044 (TDB). Max percentage 47.28341881556892%.
Io Occulted by Europa between 2454971.4452469978 and 2454971.4479900384 (TDB). Max percentage 65.34576559378536%.
Europa Occulted by Io between 2454972.7280242904 and 2454972.731496494 (TDB). Max percentage 100.00000000000003%.
Ganymede Occulted by Europa between 2454974.5251197405 and 2454974.5290549044 (TDB). Max percentage 27.728116487088133%.
Io Occulted by Europa between 2454974.990956425 and 2454974.993699466 (TDB). Max percentage 73.30487424833932%.
Europa Occulted by Io between 2454976.274735339 and 2454976.2782654124 (TDB). Max percentage 99.62392212299548%.
Europa Occulted by Ganymede between 2454977.1243365956 and 2454977.129961565 (TDB). Max percentage 100.00000000000003%.
Io Occulted by Ganymede between 2454977.2908056243 and 2454977.296777814 (TDB). Max percentage 32.45790072416453%.
Callisto Eclipsed by Io between 2454977.5559452455 and 2454977.559486893 (TDB). Max percentage 23.181515736016063%.
Io Occulted by Ganymede between 2454977.8348913444 and 2454977.8514653286 (TDB). Max percentage 96.65296543423612%.
Io Occulted by Ganymede between 2454978.205185955 and 2454978.2131025787 (TDB). Max percentage 23.24940620186856%.
Io Occulted by Europa between 2454978.536396202 and 2454978.539116095 (TDB). Max percentage 72.93883455950314%.
Europa Occulted by Io between 2454979.821420315 and 2454979.8249851107 (TDB). Max percentage 90.61988869333408%.
Ganymede Occulted by Europa between 2454981.6575213587 and 2454981.6605306016 (TDB). Max percentage 11.013246374843597%.
Io Occulted by Europa between 2454982.08154798 and 2454982.084198429 (TDB). Max percentage 61.85336240886221%.
Europa Occulted by Io between 2454983.367813438 and 2454983.3714013817 (TDB). Max percentage 80.71907756786645%.
Europa Occulted by Ganymede between 2454984.2609479562 and 2454984.2665034817 (TDB). Max percentage 91.40586858721817%.
Io Occulted by Ganymede between 2454984.446310773 and 2454984.4557898883 (TDB). Max percentage 30.689251895067265%.
Europa Eclipsed by Callisto between 2454984.574953094 and 2454984.5926034614 (TDB). Max percentage 94.36069790396861%.
Io Occulted by Ganymede between 2454984.768011064 and 2454984.7851290265 (TDB). Max percentage 86.37730926037864%.
Io Occulted by Europa between 2454985.6264288034 and 2454985.628963512 (TDB). Max percentage 49.80067684405062%.
Europa Eclipsed by Callisto between 2454986.4453877215 and 2454986.451892316 (TDB). Max percentage 67.00808680050902%.
Europa Occulted by Io between 2454986.914202965 and 2454986.9178024824 (TDB). Max percentage 71.74698809623487%.
Ganymede Eclipsed by Callisto between 2454986.9859283855 and 2454986.9900371595 (TDB). Max percentage 2.8874224105196395%.
Ganymede Occulted by Europa between 2454988.78806995 and 2454988.789111611 (TDB). Max percentage 0.4125348481082824%.
Io Occulted by Europa between 2454989.1710099676 and 2454989.173394214 (TDB). Max percentage 38.59780855833386%.
Europa Occulted by Io between 2454990.4602675387 and 2454990.46387863 (TDB). Max percentage 63.83557765262985%.
Io Eclipsed by Ganymede between 2454991.3962880066 and 2454991.399621322 (TDB), in date 2009-6-8. Max percentage 4.641502514810673%.
Europa Occulted by Ganymede between 2454991.3963921727 and 2454991.4016004778 (TDB), in date 2009-6-8. Max percentage 58.325597852973424%.
Io Eclipsed by Ganymede between 2454991.9342332957 and 2454991.9443258336 (TDB), in date 2009-6-9. Max percentage 15.255486856662934%.
Io Eclipsed by Ganymede between 2454992.311619605 and 2454992.318656604 (TDB), in date 2009-6-9. Max percentage 17.12157057973126%.
Io Occulted by Europa between 2454992.715310224 and 2454992.71752086 (TDB), in date 2009-6-10. Max percentage 28.610639706768787%.
Callisto Eclipsed by Ganymede between 2454992.808422496 and 2454992.8162349537 (TDB), in date 2009-6-10. Max percentage 84.20123173843149%.
Callisto Eclipsed by Europa between 2454993.106745457 and 2454993.113076441 (TDB), in date 2009-6-10. Max percentage 35.92657712262592%.
Callisto Eclipsed by Io between 2454993.7572937687 and 2454993.7611131924 (TDB), in date 2009-6-11. Max percentage 45.24776116131044%.
Europa Occulted by Io between 2454994.0063530793 and 2454994.009964171 (TDB), in date 2009-6-11. Max percentage 57.16395258556639%.
Io Occulted by Europa between 2454996.259324436 and 2454996.26132674 (TDB), in date 2009-6-13. Max percentage 19.98904962153328%.
Europa Occulted by Io between 2454997.552086679 and 2454997.5557093443 (TDB), in date 2009-6-15. Max percentage 51.563054638714945%.
Europa Occulted by Ganymede between 2454998.5308798132 and 2454998.535520992 (TDB), in date 2009-6-16. Max percentage 32.14022655030911%.
Io Eclipsed by Ganymede between 2454998.5590598388 and 2454998.5681222896 (TDB), in date 2009-6-16. Max percentage 20.204249649811203%.
Io Eclipsed by Ganymede between 2454998.8589305338 and 2454998.871650372 (TDB), in date 2009-6-16. Max percentage 22.830237815245024%.
Io Eclipsed by Ganymede between 2454999.467929412 and 2454999.4734965116 (TDB), in date 2009-6-16. Max percentage 31.676318047081537%.
Io Occulted by Europa between 2454999.803071396 and 2454999.8048190717 (TDB), in date 2009-6-17. Max percentage 12.832027395405191%.
Europa Occulted by Io between 2455001.097870041 and 2455001.1015158547 (TDB), in date 2009-6-18. Max percentage 47.13785081281114%.
Europa Eclipsed by Callisto between 2455001.7154907407 and 2455001.719819421 (TDB), in date 2009-6-19. Max percentage 65.95513442374381%.
Io Eclipsed by Callisto between 2455001.8558608885 and 2455001.8589627235 (TDB), in date 2009-6-19. Max percentage 15.496851122498517%.
Ganymede Eclipsed by Callisto between 2455002.4715372208 and 2455002.4776135767 (TDB), in date 2009-6-19. Max percentage 72.80188567952688%.
Io Eclipsed by Callisto between 2455002.705206001 and 2455002.7239906215 (TDB), in date 2009-6-20. Max percentage 56.06439750911485%.
Io Eclipsed by Callisto between 2455002.893946691 and 2455002.91145817 (TDB), in date 2009-6-20. Max percentage 70.46057703491203%.
Io Occulted by Europa between 2455003.346520849 and 2455003.3479791745 (TDB), in date 2009-6-20. Max percentage 7.178154646899648%.
Europa Occulted by Io between 2455004.6432973742 and 2455004.64697791 (TDB), in date 2009-6-22. Max percentage 43.67047214009241%.
Europa Occulted by Ganymede between 2455005.6639957796 and 2455005.6679193694 (TDB), in date 2009-6-23. Max percentage 15.396853365241153%.
Io Eclipsed by Ganymede between 2455006.604000918 and 2455006.609093483 (TDB), in date 2009-6-24. Max percentage 51.78316724728514%.
Io Occulted by Europa between 2455006.889723057 and 2455006.890834162 (TDB), in date 2009-6-24. Max percentage 3.058631681651389%.
Europa Eclipsed by Io between 2455008.117017136 and 2455008.1178157427 (TDB), in date 2009-6-25. Max percentage 0.4222564214438079%.
Europa Occulted by Io between 2455008.188794869 and 2455008.1925217006 (TDB), in date 2009-6-25. Max percentage 41.271190490745454%.
Ganymede Eclipsed by Io between 2455009.6528036143 and 2455009.6559517453 (TDB), in date 2009-6-27. Max percentage 24.264695457339055%.
Callisto Eclipsed by Europa between 2455010.4264236605 and 2455010.4306134526 (TDB), in date 2009-6-27. Max percentage 41.496119357702575%.
Io Occulted by Europa between 2455010.4326839433 and 2455010.43330894 (TDB), in date 2009-6-27. Max percentage 0.5329737105365251%.
Callisto Eclipsed by Io between 2455011.13846231 and 2455011.1416220153 (TDB), in date 2009-6-28. Max percentage 11.565228074840899%.
Europa Eclipsed by Io between 2455011.6647114777 and 2455011.6663897093 (TDB), in date 2009-6-29. Max percentage 3.5404489131990418%.
Europa Occulted by Io between 2455011.7339257463 and 2455011.737733596 (TDB), in date 2009-6-29. Max percentage 39.72177387673038%.
Callisto Eclipsed by Ganymede between 2455012.1209266824 and 2455012.1303247795 (TDB), in date 2009-6-29. Max percentage 88.28860401524553%.
Europa Occulted by Ganymede between 2455012.796288086 and 2455012.799459365 (TDB), in date 2009-6-30. Max percentage 6.515855269946849%.
Io Eclipsed by Ganymede between 2455013.73140361 and 2455013.736206825 (TDB), in date 2009-7-1. Max percentage 76.14283262714747%.
Europa Eclipsed by Io between 2455015.212996115 and 2455015.215229899 (TDB), in date 2009-7-2. Max percentage 7.914161890360773%.
Europa Occulted by Io between 2455015.279206842 and 2455015.283107284 (TDB), in date 2009-7-2. Max percentage 39.17222324804095%.
Ganymede Eclipsed by Io between 2455016.766373858 and 2455016.7699618014 (TDB), in date 2009-7-4. Max percentage 38.14955564487821%.
Europa Eclipsed by Io between 2455018.761375973 and 2455018.7640495696 (TDB), in date 2009-7-6. Max percentage 12.973389238423938%.
Europa Occulted by Io between 2455018.824124679 and 2455018.828152435 (TDB), in date 2009-7-6. Max percentage 39.376631873653196%.
Europa Occulted by Ganymede between 2455019.927269972 and 2455019.9298625505 (TDB), in date 2009-7-7. Max percentage 2.8918746172369474%.
Io Eclipsed by Ganymede between 2455020.8534980994 and 2455020.85805826 (TDB), in date 2009-7-8. Max percentage 99.12522944088701%.
Europa Eclipsed by Io between 2455022.310282169 and 2455022.313360856 (TDB), in date 2009-7-9. Max percentage 18.339043500308723%.
Europa Occulted by Io between 2455022.369267047 and 2455022.373468413 (TDB), in date 2009-7-9. Max percentage 40.537736613996515%.
Ganymede Eclipsed by Io between 2455023.8811603663 and 2455023.885083956 (TDB), in date 2009-7-11. Max percentage 48.51337060439552%.
Europa Eclipsed by Io between 2455025.859238441 and 2455025.8626874965 (TDB), in date 2009-7-13. Max percentage 23.857816037835892%.
Europa Occulted by Io between 2455025.914041492 and 2455025.9184396164 (TDB), in date 2009-7-13. Max percentage 42.365449880731994%.
Europa Eclipsed by Ganymede between 2455026.9484133804 and 2455026.9515036414 (TDB), in date 2009-7-14. Max percentage 5.2599689332392625%.
Europa Occulted by Ganymede between 2455027.0574439364 and 2455027.060036515 (TDB), in date 2009-7-14. Max percentage 2.3112974934068764%.
Callisto Eclipsed by Europa between 2455027.81431647 and 2455027.8179275617 (TDB), in date 2009-7-15. Max percentage 21.557081864387833%.
Io Eclipsed by Ganymede between 2455027.972358969 and 2455027.9766645012 (TDB), in date 2009-7-15. Max percentage 100.00000000000003%.
Europa Eclipsed by Io between 2455029.4088463555 and 2455029.412642631 (TDB), in date 2009-7-16. Max percentage 29.273724695122986%.
Europa Occulted by Io between 2455029.4591544494 and 2455029.463807202 (TDB), in date 2009-7-16. Max percentage 45.14693596459161%.
Ganymede Eclipsed by Io between 2455030.997294463 and 2455031.0014958293 (TDB), in date 2009-7-18. Max percentage 48.51099779264601%.
Ganymede Eclipsed by Europa between 2455031.434752298 and 2455031.4361064574 (TDB), in date 2009-7-18. Max percentage 1.0541726802369427%.
Io Eclipsed by Europa between 2455031.644738352 and 2455031.6455832548 (TDB), in date 2009-7-19. Max percentage 1.428509916736592%.
Europa Eclipsed by Io between 2455032.9585434217 and 2455032.9626753437 (TDB), in date 2009-7-20. Max percentage 34.54454147323657%.
Europa Occulted by Io between 2455033.003956014 and 2455033.0088749686 (TDB), in date 2009-7-20. Max percentage 48.55352208810684%.
Europa Eclipsed by Ganymede between 2455034.0969029795 and 2455034.1028635954 (TDB), in date 2009-7-21. Max percentage 33.158751677291434%.
Europa Occulted by Ganymede between 2455034.1872383463 and 2455034.1905600876 (TDB), in date 2009-7-21. Max percentage 3.8882330787186845%.
Io Eclipsed by Ganymede between 2455035.089093316 and 2455035.09308635 (TDB), in date 2009-7-22. Max percentage 100.00000000000003%.
Io Eclipsed by Europa between 2455035.190337202 and 2455035.1916797874 (TDB), in date 2009-7-22. Max percentage 6.017747604980841%.
Europa Eclipsed by Io between 2455036.509022229 and 2455036.5135129453 (TDB), in date 2009-7-24. Max percentage 39.428101286691195%.
Europa Occulted by Io between 2455036.549248292 and 2455036.554468171 (TDB), in date 2009-7-24. Max percentage 52.94089439462275%.
Ganymede Eclipsed by Callisto between 2455037.211201453 and 2455037.2179259537 (TDB), in date 2009-7-24. Max percentage 16.76424819931301%.
Ganymede Eclipsed by Io between 2455038.11561294 and 2455038.1200689343 (TDB), in date 2009-7-25. Max percentage 48.50623528605196%.
Ganymede Eclipsed by Europa between 2455038.567901025 and 2455038.5705861957 (TDB), in date 2009-7-26. Max percentage 9.520308428274173%.
Io Eclipsed by Europa between 2455038.7360150716 and 2455038.7376701552 (TDB), in date 2009-7-26. Max percentage 12.246143819423864%.
Europa Eclipsed by Io between 2455040.0596368955 and 2455040.0644864063 (TDB), in date 2009-7-27. Max percentage 43.95148052375946%.
Europa Occulted by Io between 2455040.0942623597 and 2455040.099829459 (TDB), in date 2009-7-27. Max percentage 57.909021237896305%.
Europa Eclipsed by Ganymede between 2455041.2495559272 and 2455041.257588291 (TDB), in date 2009-7-28. Max percentage 68.5775968734954%.
Europa Occulted by Ganymede between 2455041.317795781 and 2455041.3223906634 (TDB), in date 2009-7-28. Max percentage 8.055860839084602%.
Io Eclipsed by Ganymede between 2455042.204476202 and 2455042.2080641454 (TDB), in date 2009-7-29. Max percentage 75.14217908812685%.
Europa Eclipsed by Io between 2455043.6112549356 and 2455043.6164979627 (TDB), in date 2009-7-31. Max percentage 47.8525522774479%.
Europa Occulted by Io between 2455043.6399536305 and 2455043.6459258203 (TDB), in date 2009-7-31. Max percentage 63.869583294524304%.
Ganymede Eclipsed by Io between 2455045.236488423 and 2455045.2412337675 (TDB), in date 2009-8-1. Max percentage 43.37986461788957%.
Ganymede Eclipsed by Europa between 2455045.7011053846 and 2455045.704461848 (TDB), in date 2009-8-2. Max percentage 21.631668562500582%.
Europa Eclipsed by Io between 2455047.163122406 and 2455047.1687936718 (TDB), in date 2009-8-3. Max percentage 51.215468996438176%.
Europa Occulted by Io between 2455047.1854904 and 2455047.1918792543 (TDB), in date 2009-8-3. Max percentage 70.32489943816815%.
Europa Eclipsed by Ganymede between 2455048.40782568 and 2455048.4178371998 (TDB), in date 2009-8-4. Max percentage 99.26776124783163%.
Europa Occulted by Ganymede between 2455048.450808463 and 2455048.457023707 (TDB), in date 2009-8-4. Max percentage 15.15201253854228%.
Io Eclipsed by Ganymede between 2455049.3191495794 and 2455049.3222166924 (TDB), in date 2009-8-5. Max percentage 44.3731804707518%.
Europa Eclipsed by Io between 2455050.716323365 and 2455050.722480739 (TDB), in date 2009-8-7. Max percentage 53.71726291610025%.
Europa Occulted by Io between 2455050.731950575 and 2455050.7388602598 (TDB), in date 2009-8-7. Max percentage 77.71189686635547%.
Ganymede Eclipsed by Io between 2455052.3615148687 and 2455052.3666768777 (TDB), in date 2009-8-8. Max percentage 35.46919237700313%.
Europa Eclipsed by Io between 2455054.270062715 and 2455054.2767872154 (TDB), in date 2009-8-10. Max percentage 55.518220048000785%.
Europa Occulted by Io between 2455054.278451033 and 2455054.285904696 (TDB), in date 2009-8-10. Max percentage 85.38929117839487%.
Europa Eclipsed by Ganymede between 2455055.5735708396 and 2455055.5857929955 (TDB), in date 2009-8-12. Max percentage 100.00000000000003%.
Europa Occulted by Ganymede between 2455055.5874761348 and 2455055.595682109 (TDB), in date 2009-8-12. Max percentage 24.687497965609552%.
Io Eclipsed by Ganymede between 2455056.433186429 and 2455056.4354780833 (TDB), in date 2009-8-12. Max percentage 17.28661521814208%.
Europa Eclipsed by Io between 2455057.825627194 and 2455057.8343192763 (TDB), in date 2009-8-14. Max percentage 56.10968748619688%.
Europa Occulted by Io between 2455058.4103827896 and 2455058.4247345636 (TDB), in date 2009-8-14. Max percentage 1.6347937248622606%.
Europa Occulted by Io between 2455058.5163748045 and 2455058.543573731 (TDB), in date 2009-8-15. Max percentage 19.975702739874034%.
Ganymede Eclipsed by Io between 2455059.493183977 and 2455059.499167741 (TDB), in date 2009-8-15. Max percentage 30.18865789254634%.
Ganymede Eclipsed by Io between 2455060.1847006297 and 2455060.2314480613 (TDB), in date 2009-8-16. Max percentage 48.40832284269358%.
Ganymede Occulted by Io between 2455060.3405876723 and 2455060.3490945706 (TDB), in date 2009-8-16. Max percentage 0.4971438071472949%.
Ganymede Eclipsed by Io between 2455060.3548144884 and 2455060.3783444534 (TDB), in date 2009-8-16. Max percentage 38.54166756092777%.
Europa Occulted by Io between 2455061.3742920165 and 2455061.3906576685 (TDB), in date 2009-8-17. Max percentage 99.96992124980294%.
Europa Occulted by Io between 2455062.1008370975 and 2455062.116519883 (TDB), in date 2009-8-18. Max percentage 29.825321535140702%.
Europa Eclipsed by Io between 2455062.1227680384 and 2455062.127443939 (TDB), in date 2009-8-18. Max percentage 0.9552570878551319%.
Europa Occulted by Ganymede between 2455062.73142016 and 2455062.7421839903 (TDB), in date 2009-8-19. Max percentage 35.08811788275008%.
Europa Eclipsed by Ganymede between 2455062.7525287927 and 2455062.7678643577 (TDB), in date 2009-8-19. Max percentage 100.00000000000003%.
Europa Eclipsed by Ganymede between 2455064.242623981 and 2455064.247531362 (TDB), in date 2009-8-20. Max percentage 0.7449951595907646%.
Europa Occulted by Io between 2455064.924280074 and 2455064.9340832615 (TDB), in date 2009-8-21. Max percentage 100.00000000000003%.
Europa Eclipsed by Io between 2455064.942226371 and 2455064.951740208 (TDB), in date 2009-8-21. Max percentage 52.87297628646552%.
Europa Occulted by Io between 2455065.6665022457 and 2455065.6787128276 (TDB), in date 2009-8-22. Max percentage 36.20161933213624%.
Europa Eclipsed by Io between 2455065.6937191654 and 2455065.700420518 (TDB), in date 2009-8-22. Max percentage 7.695168437706508%.
Ganymede Eclipsed by Io between 2455066.6378524993 and 2455066.645815419 (TDB), in date 2009-8-23. Max percentage 27.498093918897055%.
Ganymede Eclipsed by Io between 2455067.0988484086 and 2455067.114542768 (TDB), in date 2009-8-23. Max percentage 48.39980362568689%.
Ganymede Occulted by Io between 2455067.139410616 and 2455067.153692946 (TDB), in date 2009-8-23. Max percentage 26.577381934350907%.
Ganymede Eclipsed by Io between 2455067.5594613594 and 2455067.5664636362 (TDB), in date 2009-8-24. Max percentage 15.877894186468996%.
Europa Occulted by Io between 2455068.47518439 and 2455068.4861565526 (TDB), in date 2009-8-24. Max percentage 100.00000000000003%.
Europa Eclipsed by Io between 2455068.5049346234 and 2455068.5162540064 (TDB), in date 2009-8-25. Max percentage 48.779204816463576%.
Europa Occulted by Io between 2455069.224013934 and 2455069.234407396 (TDB), in date 2009-8-25. Max percentage 39.99774394412545%.
Europa Eclipsed by Io between 2455069.2571709147 and 2455069.2641268955 (TDB), in date 2009-8-25. Max percentage 16.349454270597104%.
Europa Occulted by Ganymede between 2455069.8867718913 and 2455069.9013435715 (TDB), in date 2009-8-26. Max percentage 43.72308277778345%.
Europa Eclipsed by Ganymede between 2455069.957061356 and 2455069.978727905 (TDB), in date 2009-8-26. Max percentage 85.61341507696463%.
Europa Eclipsed by Ganymede between 2455071.4495595903 and 2455071.452244761 (TDB), in date 2009-8-27. Max percentage 0.403545603957178%.
Europa Occulted by Io between 2455072.029343173 and 2455072.0420167153 (TDB), in date 2009-8-28. Max percentage 92.49286853605619%.
Europa Eclipsed by Io between 2455072.0752718146 and 2455072.090132845 (TDB), in date 2009-8-28. Max percentage 41.075177442209245%.
Europa Occulted by Io between 2455072.779141127 and 2455072.788261448 (TDB), in date 2009-8-29. Max percentage 42.75698255289292%.
Europa Eclipsed by Io between 2455072.8177687447 and 2455072.8246552814 (TDB), in date 2009-8-29. Max percentage 26.459702958602175%.
Ganymede Occulted by Io between 2455073.739968268 and 2455073.744864075 (TDB), in date 2009-8-30. Max percentage 2.4805878786946636%.
Ganymede Eclipsed by Io between 2455073.823938542 and 2455073.84456343 (TDB), in date 2009-8-30. Max percentage 32.03030991882541%.
Ganymede Eclipsed by Io between 2455074.0041485433 and 2455074.030409975 (TDB), in date 2009-8-30. Max percentage 46.180966539727926%.
Ganymede Occulted by Io between 2455074.0998428105 and 2455074.1150857834 (TDB), in date 2009-8-30. Max percentage 33.25287036930663%.
Ganymede Eclipsed by Io between 2455074.7086275057 and 2455074.711220084 (TDB), in date 2009-8-31. Max percentage 1.767812232189897%.
Europa Occulted by Io between 2455075.586048025 and 2455075.60126785 (TDB), in date 2009-9-1. Max percentage 79.85787167062472%.
Europa Eclipsed by Io between 2455075.660786179 and 2455075.6889804704 (TDB), in date 2009-9-1. Max percentage 26.94020837723953%.
Europa Eclipsed by Io between 2455075.7865491216 and 2455075.7988175736 (TDB), in date 2009-9-1. Max percentage 1.1485533366701428%.
Europa Occulted by Io between 2455075.8672772595 and 2455075.8704253905 (TDB), in date 2009-9-1. Max percentage 0.13376990692034615%.
Europa Occulted by Io between 2455076.331086693 and 2455076.339315815 (TDB), in date 2009-9-1. Max percentage 44.05650519505217%.
Europa Eclipsed by Io between 2455076.3750508763 and 2455076.381798525 (TDB), in date 2009-9-1. Max percentage 37.27223060130251%.
Europa Occulted by Ganymede between 2455077.068293498 and 2455077.0912447623 (TDB), in date 2009-9-2. Max percentage 45.55507679853306%.
Europa Occulted by Io between 2455079.150644296 and 2455079.171454368 (TDB), in date 2009-9-4. Max percentage 63.190717663813466%.
Europa Occulted by Io between 2455079.349320657 and 2455079.3609872605 (TDB), in date 2009-9-4. Max percentage 3.5782499659514655%.
Europa Occulted by Io between 2455079.8824056215 and 2455079.889894007 (TDB), in date 2009-9-5. Max percentage 44.826279695747004%.
Europa Eclipsed by Io between 2455079.9312606254 and 2455079.9377999417 (TDB), in date 2009-9-5. Max percentage 49.07925060147329%.
Ganymede Occulted by Io between 2455080.922344655 and 2455081.038107916 (TDB), in date 2009-9-6. Max percentage 30.33677284424702%.
Europa Occulted by Io between 2455082.7315416387 and 2455082.8366568093 (TDB), in date 2009-9-8. Max percentage 37.98006694103817%.
Europa Occulted by Io between 2455083.4319412406 and 2455083.4388509253 (TDB), in date 2009-9-8. Max percentage 44.64944989442893%.
Europa Eclipsed by Io between 2455083.48553401 and 2455083.491864994 (TDB), in date 2009-9-8. Max percentage 61.27311587107705%.
Europa Occulted by Io between 2455086.981425735 and 2455086.9878145894 (TDB), in date 2009-9-12. Max percentage 44.218111634584325%.
Europa Eclipsed by Io between 2455087.0393255777 and 2455087.0454366556 (TDB), in date 2009-9-12. Max percentage 74.12948027909951%.
Ganymede Eclipsed by Europa between 2455088.5043018986 and 2455088.506570405 (TDB), in date 2009-9-14. Max percentage 5.549127389281861%.
Europa Occulted by Io between 2455090.529769235 and 2455090.5357298506 (TDB), in date 2009-9-16. Max percentage 43.17992106203724%.
Europa Eclipsed by Io between 2455090.59180505 and 2455090.597684648 (TDB), in date 2009-9-16. Max percentage 86.87977875716244%.
Europa Occulted by Io between 2455094.0782453506 and 2455094.083824024 (TDB), in date 2009-9-19. Max percentage 42.0682429343246%.
Europa Eclipsed by Io between 2455094.1440306185 and 2455094.149667162 (TDB), in date 2009-9-19. Max percentage 98.51943692289939%.
Io Eclipsed by Europa between 2455095.4717982733 and 2455095.473488079 (TDB), in date 2009-9-20. Max percentage 12.683878749625514%.
Ganymede Occulted by Europa between 2455095.5327038467 and 2455095.5363380862 (TDB), in date 2009-9-21. Max percentage 29.98042534381887%.
Europa Occulted by Io between 2455097.625976938 and 2455097.631219965 (TDB), in date 2009-9-23. Max percentage 40.62499305893951%.
Europa Eclipsed by Io between 2455097.695305386 and 2455097.7006873013 (TDB), in date 2009-9-23. Max percentage 100.00000000000003%.
Io Eclipsed by Europa between 2455099.018134526 and 2455099.0195349813 (TDB), in date 2009-9-24. Max percentage 6.812452671856674%.
Europa Occulted by Ganymede between 2455099.9652307364 and 2455099.9685987737 (TDB), in date 2009-9-25. Max percentage 3.3886323863592187%.
Europa Occulted by Io between 2455101.173974975 and 2455101.178917078 (TDB), in date 2009-9-26. Max percentage 39.28112388607939%.
Europa Eclipsed by Io between 2455101.246462772 and 2455101.251566911 (TDB), in date 2009-9-26. Max percentage 99.323863964806%.
Io Eclipsed by Europa between 2455102.564574533 and 2455102.565569898 (TDB), in date 2009-9-28. Max percentage 2.28848921255159%.
Ganymede Occulted by Europa between 2455102.651731243 and 2455102.6555969627 (TDB), in date 2009-9-28. Max percentage 35.49466450013955%.
Ganymede Occulted by Io between 2455103.1096066777 and 2455103.1102663963 (TDB), in date 2009-9-28. Max percentage 0.09172898605917801%.
Europa Occulted by Io between 2455104.7214688384 and 2455104.726144739 (TDB), in date 2009-9-30. Max percentage 37.83506706146837%.
Europa Eclipsed by Io between 2455104.7968912898 and 2455104.8017176525 (TDB), in date 2009-9-30. Max percentage 87.85884617512976%.
Io Occulted by Europa between 2455106.047312361 and 2455106.0498239216 (TDB), in date 2009-10-1. Max percentage 71.6197222294124%.
Europa Occulted by Ganymede between 2455107.103807133 and 2455107.108772384 (TDB), in date 2009-10-2. Max percentage 15.367522193296798%.
Europa Occulted by Io between 2455108.269275464 and 2455108.2737198845 (TDB), in date 2009-10-3. Max percentage 36.642157940888346%.
Europa Eclipsed by Io between 2455108.3472712543 and 2455108.3517966927 (TDB), in date 2009-10-3. Max percentage 74.2102895867905%.
Io Occulted by Europa between 2455109.590536884 and 2455109.5930715925 (TDB), in date 2009-10-5. Max percentage 73.34151667533729%.
Ganymede Occulted by Europa between 2455109.773169112 and 2455109.777127424 (TDB), in date 2009-10-5. Max percentage 35.49013812478197%.
Ganymede Occulted by Io between 2455110.218966505 and 2455110.2205984406 (TDB), in date 2009-10-5. Max percentage 1.621348947896481%.
Europa Occulted by Io between 2455111.8166871625 and 2455111.8209232506 (TDB), in date 2009-10-7. Max percentage 35.52632112513353%.
Europa Eclipsed by Io between 2455111.8970514084 and 2455111.9012527745 (TDB), in date 2009-10-7. Max percentage 60.539122750987495%.
Io Occulted by Europa between 2455113.134058856 and 2455113.1366051384 (TDB), in date 2009-10-8. Max percentage 73.33846323618215%.
Io Occulted by Ganymede between 2455113.242785483 and 2455113.244880379 (TDB), in date 2009-10-8. Max percentage 8.866342090271688%.
Europa Occulted by Ganymede between 2455114.2429201137 and 2455114.248533509 (TDB), in date 2009-10-9. Max percentage 30.30697003969155%.
Europa Occulted by Io between 2455115.3644848634 and 2455115.3685241933 (TDB), in date 2009-10-10. Max percentage 34.79619606904972%.
Europa Eclipsed by Io between 2455115.4468606524 and 2455115.450680076 (TDB), in date 2009-10-10. Max percentage 47.03386980831804%.
Io Occulted by Europa between 2455116.677903462 and 2455116.6804613187 (TDB), in date 2009-10-12. Max percentage 73.3353110124186%.
Ganymede Occulted by Europa between 2455116.8971721 and 2455116.90115356 (TDB), in date 2009-10-12. Max percentage 35.48525973952481%.
Ganymede Occulted by Io between 2455117.3288855543 and 2455117.3308647103 (TDB), in date 2009-10-12. Max percentage 3.549335830871843%.
Europa Occulted by Io between 2455118.9119814583 and 2455118.915870326 (TDB), in date 2009-10-14. Max percentage 34.28867251041743%.
Europa Eclipsed by Io between 2455118.996168632 and 2455118.9995945394 (TDB), in date 2009-10-14. Max percentage 34.295624497732476%.
Io Occulted by Europa between 2455120.2220679047 and 2455120.224625761 (TDB), in date 2009-10-15. Max percentage 73.33207528365192%.
Io Occulted by Ganymede between 2455120.3531083767 and 2455120.3560134536 (TDB), in date 2009-10-15. Max percentage 21.257289887489577%.
Europa Occulted by Ganymede between 2455121.3822784577 and 2455121.3881349075 (TDB), in date 2009-10-16. Max percentage 45.24323234949356%.
Europa Occulted by Io between 2455122.459881876 and 2455122.463620282 (TDB), in date 2009-10-17. Max percentage 34.26919692742032%.
Europa Eclipsed by Io between 2455122.5455467594 and 2455122.5484981323 (TDB), in date 2009-10-18. Max percentage 22.442647672485165%.
Io Occulted by Europa between 2455123.7665527617 and 2455123.769122192 (TDB), in date 2009-10-19. Max percentage 73.30888157572902%.
Ganymede Occulted by Europa between 2455124.0239653345 and 2455124.0279352204 (TDB), in date 2009-10-19. Max percentage 35.48011245585593%.
Ganymede Occulted by Io between 2455124.43970922 and 2455124.44181569 (TDB), in date 2009-10-19. Max percentage 5.042115184314094%.
Europa Occulted by Io between 2455126.0075413496 and 2455126.011175589 (TDB), in date 2009-10-21. Max percentage 34.59752649174558%.
Europa Eclipsed by Io between 2455126.094529359 and 2455126.0969251795 (TDB), in date 2009-10-21. Max percentage 12.133303477907784%.
Io Occulted by Europa between 2455127.3113524364 and 2455127.313933441 (TDB), in date 2009-10-22. Max percentage 72.38317742190942%.
Io Occulted by Ganymede between 2455127.467476692 and 2455127.4710414875 (TDB), in date 2009-10-22. Max percentage 33.1893743557086%.
Europa Occulted by Ganymede between 2455128.522440946 and 2455128.528297396 (TDB), in date 2009-10-24. Max percentage 58.10926014681483%.
Europa Occulted by Io between 2455129.5555862756 and 2455129.559127923 (TDB), in date 2009-10-25. Max percentage 35.53241105354508%.
Europa Eclipsed by Io between 2455129.6436389973 and 2455129.645270933 (TDB), in date 2009-10-25. Max percentage 3.9265341892300714%.
Io Occulted by Europa between 2455130.856490436 and 2455130.8590830145 (TDB), in date 2009-10-26. Max percentage 71.54950534474156%.
Ganymede Occulted by Europa between 2455131.153273532 and 2455131.157266566 (TDB), in date 2009-10-26. Max percentage 35.47476745814321%.
Ganymede Occulted by Io between 2455131.5513167745 and 2455131.5534232445 (TDB), in date 2009-10-27. Max percentage 5.57836422792317%.
Europa Occulted by Io between 2455133.103467975 and 2455133.106951752 (TDB), in date 2009-10-28. Max percentage 36.96478216206921%.
Io Occulted by Europa between 2455134.401909789 and 2455134.4045139416 (TDB), in date 2009-10-29. Max percentage 71.16914690091113%.
Io Occulted by Ganymede between 2455134.5868096217 and 2455134.591057284 (TDB), in date 2009-10-30. Max percentage 43.30184178992083%.
Europa Occulted by Ganymede between 2455135.6636691475 and 2455135.669409857 (TDB), in date 2009-10-31. Max percentage 67.26991063749544%.
Europa Occulted by Io between 2455136.651719248 and 2455136.6551335813 (TDB), in date 2009-11-1. Max percentage 39.13768263535661%.
Io Occulted by Europa between 2455137.9476758176 and 2455137.950291544 (TDB), in date 2009-11-2. Max percentage 71.35176762351401%.
Ganymede Occulted by Europa between 2455138.2853324222 and 2455138.289360178 (TDB), in date 2009-11-2. Max percentage 35.46928510289138%.
Ganymede Occulted by Io between 2455138.6641585855 and 2455138.6661261674 (TDB), in date 2009-11-3. Max percentage 4.867745465603875%.
Europa Occulted by Io between 2455140.199848588 and 2455140.2032397734 (TDB), in date 2009-11-4. Max percentage 41.960162222389705%.
Io Occulted by Europa between 2455141.4937087274 and 2455141.496347602 (TDB), in date 2009-11-5. Max percentage 72.03968926184699%.
Io Occulted by Ganymede between 2455141.713078232 and 2455141.718228667 (TDB), in date 2009-11-6. Max percentage 51.59788528302735%.
Europa Occulted by Ganymede between 2455142.806072056 and 2455142.811592859 (TDB), in date 2009-11-7. Max percentage 71.69953938831007%.
Europa Occulted by Io between 2455143.7482879404 and 2455143.7516559777 (TDB), in date 2009-11-8. Max percentage 45.655071759727534%.
Io Occulted by Europa between 2455145.0400680183 and 2455145.042730041 (TDB), in date 2009-11-9. Max percentage 73.02111216469814%.
Ganymede Occulted by Europa between 2455145.419581851 and 2455145.423702199 (TDB), in date 2009-11-9. Max percentage 35.463764203035595%.
Europa Occulted by Io between 2455147.296675232 and 2455147.300031695 (TDB), in date 2009-11-11. Max percentage 50.155181662980844%.
Io Occulted by Europa between 2455148.586704855 and 2455148.5893900255 (TDB), in date 2009-11-13. Max percentage 73.30464068536332%.
Io Occulted by Ganymede between 2455148.85050607 and 2455148.8572189966 (TDB), in date 2009-11-13. Max percentage 59.388542925065046%.
Io Occulted by Ganymede between 2455149.431691969 and 2455149.4495622423 (TDB), in date 2009-11-13. Max percentage 100.00000000000003%.
Io Occulted by Ganymede between 2455149.7596529117 and 2455149.771030165 (TDB), in date 2009-11-14. Max percentage 60.72041544263161%.
Europa Occulted by Ganymede between 2455149.949911886 and 2455149.955143339 (TDB), in date 2009-11-14. Max percentage 70.80066913702173%.
Europa Occulted by Io between 2455150.845324272 and 2455150.848680735 (TDB), in date 2009-11-15. Max percentage 55.66012555599721%.
Io Occulted by Europa between 2455152.1336444286 and 2455152.136352747 (TDB), in date 2009-11-16. Max percentage 73.3011421524906%.
Ganymede Occulted by Europa between 2455152.5562110483 and 2455152.5604471364 (TDB), in date 2009-11-17. Max percentage 35.45822146241776%.
Europa Occulted by Io between 2455154.393965311 and 2455154.397321774 (TDB), in date 2009-11-18. Max percentage 62.11102362892076%.
Io Occulted by Europa between 2455155.68085299 and 2455155.683584457 (TDB), in date 2009-11-20. Max percentage 73.29766284477772%.
Io Occulted by Ganymede between 2455156.0108683603 and 2455156.021956263 (TDB), in date 2009-11-20. Max percentage 70.11187772512612%.
Io Occulted by Ganymede between 2455156.3571687336 and 2455156.3744255845 (TDB), in date 2009-11-20. Max percentage 100.00000000000003%.
Io Occulted by Ganymede between 2455156.928209488 and 2455156.9339964935 (TDB), in date 2009-11-21. Max percentage 31.544405153849308%.
Europa Occulted by Ganymede between 2455157.0945923664 and 2455157.099453451 (TDB), in date 2009-11-21. Max percentage 63.89263640189027%.
Europa Occulted by Io between 2455157.9428551523 and 2455157.9462116156 (TDB), in date 2009-11-22. Max percentage 69.67700023431553%.
Io Occulted by Europa between 2455159.2283580303 and 2455159.231112645 (TDB), in date 2009-11-23. Max percentage 73.2941763670043%.
Ganymede Occulted by Europa between 2455159.6951085273 and 2455159.6994603556 (TDB), in date 2009-11-24. Max percentage 35.45272456288977%.
Europa Occulted by Io between 2455161.491728829 and 2455161.4950737185 (TDB), in date 2009-11-25. Max percentage 78.23634497005193%.
Io Occulted by Europa between 2455162.7761399434 and 2455162.778917706 (TDB), in date 2009-11-27. Max percentage 72.93378485358268%.
Io Occulted by Ganymede between 2455164.072634606 and 2455164.075643849 (TDB), in date 2009-11-28. Max percentage 8.322027778170366%.
Europa Occulted by Ganymede between 2455164.240441578 and 2455164.2448049802 (TDB), in date 2009-11-28. Max percentage 50.53500052633407%.
Europa Occulted by Io between 2455165.0408343025 and 2455165.044156044 (TDB), in date 2009-11-29. Max percentage 87.76710673340027%.
Io Occulted by Europa between 2455166.324168036 and 2455166.326957373 (TDB), in date 2009-11-30. Max percentage 68.93317276702497%.
Ganymede Occulted by Europa between 2455166.836321407 and 2455166.8407079573 (TDB), in date 2009-12-1. Max percentage 35.44727151265485%.
Europa Occulted by Io between 2455168.589922078 and 2455168.593232245 (TDB), in date 2009-12-3. Max percentage 97.34218257763371%.
Io Occulted by Europa between 2455169.8724833224 and 2455169.875272659 (TDB), in date 2009-12-4. Max percentage 63.511096075889284%.
Europa Occulted by Ganymede between 2455171.3870036122 and 2455171.390672574 (TDB), in date 2009-12-5. Max percentage 31.281625441994663%.
Europa Occulted by Io between 2455172.1392334406 and 2455172.142497312 (TDB), in date 2009-12-6. Max percentage 100.00000000000003%.
Io Occulted by Europa between 2455173.421035224 and 2455173.423824561 (TDB), in date 2009-12-7. Max percentage 57.36841895271157%.
Ganymede Occulted by Europa between 2455173.979977404 and 2455173.984248214 (TDB), in date 2009-12-8. Max percentage 31.686136978714025%.
Europa Occulted by Io between 2455175.6885348647 and 2455175.691740866 (TDB), in date 2009-12-10. Max percentage 100.00000000000003%.
Io Occulted by Europa between 2455176.9698682847 and 2455176.9726344733 (TDB), in date 2009-12-11. Max percentage 50.788942940721945%.
Europa Occulted by Ganymede between 2455178.5345392125 and 2455178.537004477 (TDB), in date 2009-12-13. Max percentage 9.749495211133926%.
Europa Occulted by Io between 2455179.238026065 and 2455179.2411279 (TDB), in date 2009-12-13. Max percentage 91.19434041153325%.
Io Occulted by Europa between 2455180.518940753 and 2455180.5216606455 (TDB), in date 2009-12-15. Max percentage 43.92573609099797%.
Ganymede Occulted by Europa between 2455181.125781705 and 2455181.129624277 (TDB), in date 2009-12-15. Max percentage 18.800408441854366%.
Europa Occulted by Io between 2455182.7875353717 and 2455182.7905098926 (TDB), in date 2009-12-17. Max percentage 76.70654514337865%.
Io Occulted by Europa between 2455184.0682754805 and 2455184.070937503 (TDB), in date 2009-12-18. Max percentage 36.95062857598755%.
Europa Occulted by Io between 2455186.3372036093 and 2455186.340016094 (TDB), in date 2009-12-20. Max percentage 60.97777212167389%.
Io Occulted by Europa between 2455187.6178403515 and 2455187.620409782 (TDB), in date 2009-12-22. Max percentage 29.987279775017278%.
Ganymede Occulted by Europa between 2455188.274108732 and 2455188.276909643 (TDB), in date 2009-12-22. Max percentage 5.672723750452878%.
Europa Occulted by Io between 2455189.8869184097 and 2455189.8894994142 (TDB), in date 2009-12-24. Max percentage 44.90367178438476%.
Io Occulted by Europa between 2455191.1676901965 and 2455191.170132313 (TDB), in date 2009-12-25. Max percentage 23.223631215490386%.
Europa Occulted by Io between 2455193.4367546225 and 2455193.4390347027 (TDB), in date 2009-12-27. Max percentage 29.16684150574853%.
Io Occulted by Europa between 2455194.7177441507 and 2455194.720024231 (TDB), in date 2009-12-29. Max percentage 16.80605234191468%.
Europa Occulted by Io between 2455196.9866847573 and 2455196.988536599 (TDB), in date 2009-12-31. Max percentage 14.781455605052903%.
	 
	 */
	/*
Predictions using UranusGUST86 of the more probable events
  Date  (TT)               begins      maximum         dist.  flux       magn.      magn.     magn.     magn.      impact   
  of maximum   Event       at                    dur.         R           R         R          V        no alb.     param.  
  year mth day             h  m  s     h  m  s    s    (UR)                         2 sat                           (")     
  2006  5  5   5 OC 1 P   10 24 38.   10 26 19.   202   3.9   0.014      0.016      0.016     0.015     0.009      0.051    
  2006  5 11   1 OC 5 c               17 36 10.         3.9                                                        0.1      
  2006  6  3   1 OC 5 P   10  3  3.   10  7 44.   562   3.8   0.128      0.149      0.149     0.157     0.168      0.025    
  2006  6 16   1 OC 5 T    0 36 55.    0 39 12.   273   3.8   0.130      0.152      0.152     0.160     0.171      0.001    
  2006  6 22   5 OC 1 c                8 20 56.         3.4                                                        0.1      
  2006  7  2   5 OC 1 A    9 40  0.    9 45 50.   700   3.7   0.256      0.322      0.322     0.298     0.171      0.004    
  2006  7  8   1 OC 5 T   16 37 33.   16 40 39.   372   4.0   0.130      0.152      0.152     0.160     0.171      0.001    
  2006  7 15   5 OC 1 P    0  8  3.    0  8 15.    24   3.8   0.000      0.000      0.000     0.000     0.000      0.058    
  2007  3 10   1 OC 5 P    4 32  7.    4 33 54.   214   3.8   0.075      0.084      0.084     0.089     0.094      0.035    
  2007  3 16   5 OC 1 A   12 13  4.   12 14 50.   213   3.5   0.257      0.322      0.322     0.298     0.171      0.021    
  2007  3 25   1 OC 5 P   12 17 46.   12 18 25.    78   0.6   0.027      0.030      0.030     0.031     0.033      0.045    
  2007  3 26   5 OC 1 P   13 53 39.   13 59 20.   681   3.6   0.167      0.198      0.198     0.185     0.108      0.033    
  2007  4  1   1 EC 5 P   20 38 37.   20 41  2.   231   4.0   0.186      0.224      0.025     0.224     0.224      0.047    
  2007  4  8   5 EC 1 P    4  3  5.    4  5 39.   278   3.8   0.148      0.174      0.151     0.174     0.174      0.027    
  2007  4 14   1 EC 5 P   11 45 58.   11 48  7.   227   3.6   0.813      1.822      0.115     1.822     1.822      0.030    
  2007  4 20   5 EC 1 P   19 33 31.   19 35  5.   150   3.1   0.055      0.061      0.053     0.061     0.061      0.043    
  2007  4 22   1 OC 5 P   12 37 24.   12 38 15.   102   2.6   0.033      0.036      0.036     0.038     0.040      0.044    
  2007  4 24   2 OC 1 P    6 20  4.    6 25 39.   671   6.2   0.131      0.152      0.152     0.147     0.112      0.054    
  2007  4 27   1 EC 5      3 27 45.    3 28 31.         2.8   0.006      0.006      0.001     0.006     0.006      0.058    
  2007  4 28   5 OC 1 P   20 30 46.   20 31 16.    61   3.0   0.010      0.010      0.010     0.010     0.006      0.052    
  2007  4 30   1 OC 2 P   11 21 20.   11 26 28.   617   6.2   0.259      0.325      0.325     0.348     0.537      0.013    
  2007  4 30   3 OC 2 P    4 37 51.    4 42 44.   585   8.5   0.201      0.244      0.244     0.263     0.440      0.020    
  2007  4 30   5 EC 1 A   20 10 22.   20 14 12.   429   3.9   0.164      0.194      0.168     0.194     0.194      0.018    
  2007  5  3   1 OC 3 P    2 12 58.    2 22  3.  1108   6.1   0.274      0.348      0.348     0.354     0.379      0.027    
  2007  5  3   1 OC 3 P   12 57 18.   13  9 45.  1493   1.0   0.218      0.267      0.267     0.272     0.290      0.037    
  2007  5  3   2 OC 1 P    9 11 59.    9 14  2.   246   3.6   0.294      0.378      0.378     0.365     0.270      0.035    
  2007  5  3   2 OC 3 A    8 32 48.    8 35 45.   353   3.2   0.329      0.434      0.434     0.442     0.476      0.000    
  2007  5  3   4 OC 1 P   21 34 53.   21 37 28.   309   3.1   0.246      0.307      0.307     0.300     0.243      0.043    
  2007  5  3   4 OC 3 A   23 36 54.   23 40 45.   463   2.3   0.409      0.570      0.570     0.591     0.714      0.000    
  2007  5  4   1 OC 3 P    8  2 22.    8  7 39.   634   6.3   0.214      0.261      0.261     0.266     0.284      0.038    
  2007  5  4   4 OC 2 P   19  5 31.   19  8 36.   370   4.1   0.201      0.244      0.244     0.263     0.435      0.021    
  2007  5  5   1 OC 2 P    0 34 48.    0 36 43.   230   0.8   0.244      0.304      0.304     0.325     0.498      0.016    
  2007  5  6   2 OC 1 P   17  1 29.   17  4 51.   403   6.1   0.203      0.246      0.246     0.238     0.179      0.045    
  2007  5  7   1 EC 5 P    3 35 57.    3 38 43.   302   3.9   0.975      4.017      0.140     4.017     4.017      0.023    
  2007  5 11   5 OC 4 c               11 45 34.         3.8                                                        0.1      
  2007  5 29   1 EC 5 T   19 46 18.   19 50 35.   481   3.9   1.000      *****      0.144     *****     *****      0.005    
  2007  6  1   1 EC 5 P   10 49 40.   10 51  3.   139   2.4   0.944      3.125      0.135     3.125     3.125      0.023    
  2007  6  7   5 EC 1 A   18 45  3.   18 46 26.   140   1.8   0.163      0.194      0.168     0.194     0.194      0.011    
  2007  6 14   1 EC 5 T    2 43 23.    2 44 43.   136   1.4   1.000      *****      0.144     *****     *****      0.003    
  2007  6 20   5 EC 1 A   10 41  8.   10 42 24.   127   0.7   0.164      0.195      0.169     0.195     0.195      0.011    
  2007  6 26   1 EC 5 P   18 40 50.   18 42  1.   115   0.3   0.973      3.902      0.139     3.902     3.902      0.020    
  2007  6 27   5 EC 1 A   19 19 38.   19 23 51.   471   3.9   0.163      0.194      0.168     0.194     0.194      0.023    
  2007  6 29   2 EC 5 P   19  5 20.   19  7 23.   200   3.5   0.670      1.203      0.173     1.203     1.203      0.033    
  2007  7  1   5 EC 2 P   21 17 33.   21 19 29.   191   3.0   0.129      0.150      0.115     0.150     0.150      0.030    
  2007  7  3   2 EC 5 P   23 37  1.   23 38 38.   156   2.8   0.950      3.256      0.255     3.256     3.256      0.021    
  2007  7  6   5 EC 2 A    1 54 15.    1 55 41.   131   2.3   0.135      0.158      0.121     0.158     0.158      0.029    
  2007  7  8   2 EC 5 P    4 16 33.    4 17 56.   127   2.1   0.843      2.008      0.223     2.008     2.008      0.027    
  2007  7 10   5 EC 2 P    6 37 10.    6 38 16.   126   1.6   0.068      0.076      0.059     0.076     0.076      0.041    
  2007  7 12   2 EC 5 P    9  0 30.    9  1 29.   107   1.3   0.269      0.341      0.066     0.341     0.341      0.045    
  2007  7 12   5 OC 3 A    3 45 11.    3 52 21.   860   1.3   0.099      0.113      0.113     0.111     0.096      0.012    
  2007  7 14   5 EC 2 c               11 24  6.         0.8                                                        0.1      
  2007  7 16   2 EC 5 c               13 47 54.         0.5                                                        0.1      
  2007  7 22   1 EC 5 c                2 40  5.         0.2                                                        0.1      
  2007  7 26   1 EC 5 P   18 58 30.   19  2 41.   457   3.9   0.718      1.374      0.101     1.374     1.374      0.033    
  2007  7 30   5 OC 4 P   11 11  6.   11 17  4.   715   2.5   0.044      0.049      0.049     0.043     0.043      0.055    
  2007  8  5   4 OC 2 P   13 42 56.   13 52 20.  1159   8.5   0.022      0.025      0.025     0.026     0.041      0.08*    
  2007  8  6   1 OC 5 P   10 34 41.   10 36 24.   205   3.6   0.108      0.124      0.124     0.131     0.139      0.031    
  2007  8  6   4 OC 2 P    0 56 35.    1 11 39.  1807   3.9   0.049      0.054      0.054     0.058     0.091      0.07*    
  2007  8 13   1 OC 2 P    3  0  5.    3  5 15.   620   6.3   0.235      0.291      0.291     0.311     0.475      0.019    
  2007  8 13   1 OC 4 P   13 21 24.   13 24  4.   321   2.0   0.301      0.389      0.389     0.350     0.440      0.022    
  2007  8 14   2 OC 1 P   20  9 22.   20 11 20.   235   1.6   0.483      0.717      0.717     0.688     0.488      0.018    
  2007  8 14   2 OC 4 P    1 29  4.    1 32 32.   417   7.1   0.193      0.233      0.233     0.211     0.262      0.044    
  2007  8 14   2 OC 5 T   22 55 29.   22 57 15.   212   3.3   0.129      0.150      0.150     0.158     0.169      0.002    
  2007  8 15   2 OC 3 A    9 10 48.    9 15 10.   523   8.0   0.329      0.434      0.434     0.442     0.476      0.004    
  2007  8 16   1 OC 2 P   11 28 24.   11 30 30.   252   2.9   0.222      0.273      0.273     0.291     0.442      0.022    
  2007  8 19   2 OC 1 P    7 53 21.    7 58 32.   622   6.2   0.085      0.097      0.097     0.094     0.072      0.065    
  2007  8 20   5 OC 2 P   13 48 27.   13 54  6.   692   3.5   0.054      0.061      0.061     0.062     0.070      0.043    
  2007  8 22   2 EC 5 T   14 58  6.   15  2 15.   440   3.9   1.000      *****      0.270     *****     *****      0.016    
  2007  8 24   1 OC 2 P   12 14 45.   12 21 51.   852   6.1   0.109      0.126      0.126     0.133     0.195      0.047    
  2007  8 24   5 EC 3 A   13 40 55.   13 43 30.   227   2.7   0.096      0.110      0.097     0.110     0.110      0.026    
  2007  8 25   1 OC 2 P    7 14  5.    7 54 56.  9944   3.2   0.033      0.036      0.036     0.039     0.055      0.07*    
  2007  9  1   5 EC 1 A   18  6  9.   18  7 27.   132   1.2   0.164      0.194      0.168     0.194     0.194      0.009    
  2007  9  6   3 EC 5 T   20 25  7.   20 26 59.   173   0.1   1.000      *****      0.126     *****     *****      0.012    
  2007  9  8   1 EC 5 T    2  5  0.    2  6 19.   133   1.7   1.000      *****      0.144     *****     *****      0.017    
  2007  9 14   5 EC 1 p   10  2 39.   10  3 35.         2.3   0.006      0.006      0.006     0.006     0.006      0.055    
  2007  9 22   1 EC 5 P   18 20 51.   18 26  7.   587   3.8   0.839      1.983      0.119     1.983     1.983      0.029    
  2007  9 29   5 OC 1 P    1 19 34.    1 22  4.   300   3.9   0.185      0.222      0.222     0.206     0.121      0.035    
  2007 10  7   2 EC 5 P   18  0 20.   18  1 45.   133   2.1   0.888      2.377      0.236     2.377     2.377      0.025    
  2007 10  8   1 OC 5 P    0 43 20.    0 44 28.   136   1.7   0.127      0.147      0.147     0.155     0.166      0.03*    
  2007 10  9   5 EC 2 c               20 24  6.         2.5                                                        0.1      
  2007 10 11   5 OC 1 A   16 36 12.   16 38  6.   228   3.5   0.257      0.322      0.322     0.298     0.171      0.003    
  2007 10 12   3 EC 5 P    0  0 55.    0  2 46.   129   1.9   0.365      0.492      0.044     0.492     0.492      0.056    
  2007 10 12   4 EC 5 T    9 47 17.    9 51  0.   344   3.6   1.000      *****      0.137     *****     *****      0.006    
  2007 10 18   1 OC 5 c                0 29 44.         3.1                                                        0.1      
  2007 10 21   1 EC 2 P   22 53  5.   23 11 32.  2087   0.5   0.199      0.241      0.075     0.241     0.241      0.055    
  2007 10 21   5 EC 1 A   18  5 29.   18 12  8.   756   3.7   0.165      0.196      0.169     0.196     0.196      0.014    
  2007 10 26   1 EC 5 P    1 25 50.    1 27 40.   190   3.1   0.766      1.579      0.108     1.579     1.579      0.031    
  2007 11  2   5 EC 3 A   20 29 37.   20 31 60.   203   2.8   0.070      0.079      0.070     0.079     0.079      0.045    
  2007 11 11   1 EC 3 P    3  1 53.    3 13 30.  1237   0.6   0.234      0.289      0.145     0.289     0.289      0.052    
  2007 11 19   1 EC 5 T   18  0  3.   18  8  1.   903   3.6   1.000      *****      0.144     *****     *****      0.017    
  2007 11 21   2 EC 1 P   16 44 53.   16 47 19.   217   5.2   0.100      0.114      0.075     0.114     0.114      0.064    
  2007 11 23   4 EC 2 p    5 57  6.    5 59 38.         9.1   0.005      0.005      0.002     0.005     0.005      0.096    
  2007 11 24   1 EC 2 c               15 36 11.         5.3                                                        0.1      
  2007 11 27   1 EC 2 P   22 50 29.   22 53 11.   274   4.8   0.280      0.356      0.107     0.356     0.356      0.047    
  2007 11 28   1 EC 3 P    1 35 55.    1 40 28.   466   5.9   0.346      0.462      0.223     0.462     0.462      0.038    
  2007 11 29   2 EC 3 A   12 41 30.   12 48 11.   691   8.9   0.526      0.810      0.496     0.810     0.810      0.003    
  2007 11 29   2 EC 4 p   19  2  9.   19  4 57.         7.3   0.026      0.029      0.019     0.029     0.029      0.088    
  2007 11 30   1 EC 5 T    8 52 22.    8 54 38.   226   3.4   1.000      *****      0.144     *****     *****      0.009    
  2007 11 30   2 EC 1 P   21 33  4.   21 35 23.   185   5.7   0.048      0.053      0.035     0.053     0.053      0.070    
  2007 11 30   3 EC 4 P   18 32 16.   18 46 34.  1482  15.6   0.762      1.558      0.491     1.558     1.558      0.020    
  2007 12  2   1 EC 2 P   13 37 18.   13 38 57.   114   0.2   0.065      0.073      0.024     0.073     0.073      0.068    
  2007 12  2   3 EC 2 P   17 10 38.   17 13 41.   265   2.3   0.356      0.478      0.124     0.478     0.478      0.057    
  2007 12  3   3 EC 1 P   12  3 26.   12  7 28.   410   5.0   0.882      2.320      0.572     2.320     2.320      0.020    
  2007 12  4   2 EC 1 P    5  2 59.    5  5 31.   280   4.2   0.593      0.975      0.545     0.975     0.975      0.025    
  2007 12  6   4 EC 1 P    3 20 33.    3 23 23.   201   3.5   0.174      0.208      0.096     0.208     0.208      0.070    
  2007 12  6   4 EC 2 P    5 58 46.    6  2  8.   272   4.7   0.311      0.405      0.115     0.405     0.405      0.058    
  2007 12  6   4 EC 3 P   13 50  4.   13 55  8.   475   7.9   0.636      1.097      0.438     1.097     1.097      0.023    
  2007 12  7   1 EC 2 P    3 28 38.    3 32 30.   400   6.0   0.275      0.349      0.105     0.349     0.349      0.048    
  2007 12  7   1 EC 3 A   13  1 21.   13  4 11.   277   1.3   0.517      0.789      0.352     0.789     0.789      0.006    
  2007 12  8   2 EC 1 P   19 54 16.   19 56 13.   187   0.8   0.453      0.655      0.390     0.655     0.655      0.034    
  2007 12  8   2 EC 3 P    1 52 13.    1 56 44.   445   7.3   0.418      0.587      0.374     0.587     0.587      0.029    
  2007 12 10   1 EC 2 A   11 14 13.   11 16 42.   257   3.7   0.832      1.936      0.353     1.936     1.936      0.001    
  2007 12 11   3 EC 1 P   13 12  7.   13 14 41.   238   0.8   0.590      0.969      0.348     0.969     0.969      0.041    
  2007 12 11   3 EC 2 P    4 42  2.    4 45 32.   359   4.9   0.945      3.148      0.366     3.148     3.148      0.009    
  2007 12 12   1 EC 4 A   14 40 21.   14 43 17.   268   1.0   0.485      0.720      0.311     0.720     0.720      0.017    
  2007 12 12   2 EC 4 A   16 30 44.   16 34  3.   315   1.7   0.508      0.770      0.458     0.770     0.770      0.014    
  2007 12 12   3 EC 4 A    1  3 10.    1  7 56.   416   3.3   0.721      1.387      0.459     1.387     1.387      0.003    
  2007 12 13   2 EC 1 P    9  4 22.    9  9 38.   580   6.2   0.528      0.815      0.470     0.815     0.815      0.030    
  2007 12 15   1 EC 2 A    2 12 17.    2 14 27.   220   1.5   0.810      1.805      0.343     1.805     1.805      0.002    
  2007 12 15   1 EC 3 P   14  0 60.   14  3 56.   253   4.7   0.110      0.127      0.066     0.127     0.127      0.069    
  2007 12 16   2 EC 1 P   17 30 47.   17 32 59.   220   3.1   0.513      0.782      0.454     0.782     0.782      0.030    
  2007 12 17   2 EC 5 T    7 29 21.    7 31 49.   254   3.6   1.000      *****      0.270     *****     *****      0.019    
  2007 12 17   4 EC 3 A   14 10 56.   14 18 30.   749  13.9   0.775      1.618      0.563     1.618     1.618      0.004    
  2007 12 19   1 EC 2 P   14 17 41.   14 25 26.   862   6.2   0.706      1.331      0.292     1.331     1.331      0.017    
  2007 12 19   3 EC 2 P   16 37 50.   16 41 37.   338   7.2   0.305      0.396      0.105     0.396     0.396      0.061    
  2007 12 21   2 EC 1 P    8 28 13.    8 30 19.   206   2.2   0.520      0.796      0.461     0.796     0.796      0.029    
  2007 12 24   2 EC 1 P   18 44 52.   18 53 54.  1010   6.1   0.362      0.489      0.300     0.489     0.489      0.042    
  2007 12 25   2 EC 1 P   10 26 20.   10 57 47.  3675   1.4   0.910      2.611      1.011     2.611     2.611      0.006    
  2007 12 25   2 EC 1 P   17  0  3.   17 24 14.  2764   5.2   0.814      1.827      0.848     1.827     1.827      0.011    
  2007 12 28   3 EC 2 p    5 42  2.    5 45  5.         8.8   0.027      0.030      0.009     0.030     0.030      0.090    
  2007 12 30   1 EC 2 P   23 25  1.   23 29  0.   395   6.3   0.086      0.098      0.032     0.098     0.098      0.065    
  2008  1  4   1 EC 5 T   16 15 21.   16 17 50.   268   3.7   1.000      *****      0.144     *****     *****      0.020    
  2008  1  6   3 EC 2 p    2 36 41.    3  3  6.         6.9   0.013      0.014      0.004     0.014     0.014      0.091    
  2008  1  6   3 EC 2 p    6 19 52.    6 27 53.         5.5   0.001      0.001      0.000     0.001     0.001      0.095    
  2008  1  6   1 OC 5 P   15 27 39.   15 29  9.   180   3.6   0.068      0.076      0.076     0.080     0.085      0.037    
  2008  1  9   1 OC 5 P    7 28  3.    7 28 47.    88   1.3   0.034      0.038      0.038     0.040     0.042      0.044    
  2008  1 23   5 OC 1 P    0  3 11.    0  5  2.   223   3.9   0.033      0.037      0.037     0.035     0.021      0.048    
  2008  1 24   1 OC 5 T   15 57 51.   15 59  2.   141   0.8   0.130      0.152      0.152     0.160     0.171      0.002    
  2008  1 27   2 OC 5 P   12 35 11.   12 36  0.    97   0.2   0.046      0.052      0.052     0.054     0.058      0.041    
  2008  1 29   5 OC 2 P   15  0 46.   15  1 50.   129   0.6   0.107      0.123      0.123     0.126     0.142      0.029    
  2008  1 31   2 OC 5 T   17 22 37.   17 23 52.   150   1.0   0.129      0.150      0.150     0.158     0.169      0.003    
  2008  2  2   5 OC 2 A   19 47 32.   19 48 52.   158   1.4   0.126      0.146      0.146     0.149     0.169      0.006    
  2008  2  4   2 OC 5 T   22  7 43.   22  9  6.   165   1.8   0.129      0.150      0.150     0.158     0.169      0.002    
  2008  2  7   5 OC 2 A    0 31 24.    0 32 50.   173   2.2   0.126      0.146      0.146     0.149     0.169      0.001    
  2008  2  8   1 EC 5 T   23 29  1.   23 32  8.   344   3.9   1.000      *****      0.144     *****     *****      0.007    
  2008  2  9   2 OC 5 T    2 49 25.    2 50 55.   182   2.6   0.129      0.150      0.150     0.158     0.169      0.002    
  2008  2 11   5 OC 2 A    5  9 52.    5 11 23.   182   2.9   0.126      0.146      0.146     0.149     0.169      0.021    
  2008  2 13   2 OC 5 P    7 24 36.    7 25 42.   132   3.2   0.038      0.042      0.042     0.044     0.047      0.043    
  2008  2 15   2 OC 3 A   10 23 57.   11  5 28.  5479   4.3   0.329      0.434      0.434     0.442     0.476      0.006    
  2008  2 15   2 OC 3 P   17 24 56.   18  7 51.  4655   7.6   0.323      0.423      0.423     0.431     0.464      0.02*    
  2008  2 18   1 OC 2 P   23 40 60.   23 42 28.   175   2.8   0.052      0.058      0.058     0.062     0.089      0.056    
  2008  2 19   3 OC 1 P   17 51 59.   17 54 51.   345   3.7   0.393      0.542      0.542     0.529     0.424      0.021    
  2008  2 20   2 OC 1 P   20 59  7.   21 23 59.  3311   5.0   0.550      0.868      0.868     0.830     0.577      0.011    
  2008  2 21   2 OC 1 P   19 33 41.   19 42 33.  1064   6.1   0.370      0.501      0.501     0.483     0.352      0.027    
  2008  2 21   2 OC 1 T    2 19 44.    2 57 19.  4190   2.0   0.663      1.181      1.181     1.120     0.745      0.001    
  2008  2 23   4 OC 2 P   20 21 57.   20 24 44.   334   5.6   0.089      0.102      0.102     0.109     0.172      0.051    
  2008  3 15   1 OC 5 P    6 25 43.    6 28  3.   278   3.9   0.039      0.043      0.043     0.045     0.048      0.042    
  2008  4 13   5 EC 1 A    5 59 42.    6  3 45.   449   4.0   0.164      0.194      0.168     0.194     0.194      0.001    
  2008  4 23   2 EC 5 c               18 59 53.         3.8                                                        0.1      
  2008  5  7   3 EC 5 P   13 32 14.   13 36 11.   209   0.8   0.132      0.153      0.016     0.153     0.153      0.064    
  2008  5 29   5 EC 3 P   15 28 51.   15 34 32.   505   1.6   0.044      0.049      0.043     0.049     0.049      0.053    
  2008  7 15   5 EC 1 A   11 35 53.   11 41 20.   605   3.9   0.165      0.195      0.169     0.195     0.195      0.021    
  2008  8 12   1 EC 5 T   22 12 35.   22 24  9.  1317   3.2   1.000      *****      0.144     *****     *****      0.021    
  2008  8 19   5 EC 2 P    9 16 57.    9 26 44.  1024   1.8   0.102      0.117      0.090     0.117     0.117      0.036    
  2008  8 23   5 OC 2 A   21 16 58.   21 27 22.  1304   1.7   0.126      0.146      0.146     0.149     0.169      0.024    
  2008 10 30   5 OC 4 A   16  1 49.   16  6 17.   535   0.3   0.103      0.118      0.118     0.104     0.103      0.033    
  2008 12 13   5 OC 1 A    3 10 22.    3 15 21.   597   3.8   0.256      0.322      0.322     0.298     0.171      0.018    
  2008 12 19   2 EC 5 P   17  0 22.   17  7 39.   775   0.8   0.643      1.119      0.166     1.119     1.119      0.035    
  2008 12 23   5 OC 4 A    6 18 53.    6 23 33.   559   0.5   0.103      0.118      0.118     0.104     0.103      0.028    
  2009  1 11   1 OC 5 P    3  2 22.    3  6 14.   464   3.7   0.043      0.048      0.048     0.050     0.054      0.042    
  2009  1 11   1 OC 5 T   16 29 44.   16 36 20.   776   3.7   0.130      0.152      0.152     0.160     0.171      0.001    
  2009  6 11   1 EC 5 T   18 47 57.   19 10 52.  2636   2.2   1.000      *****      0.144     *****     *****      0.010    
  2009 12  2   1 EC 5 T    8 44 39.    9  6 29.  2504   1.9   1.000      *****      0.144     *****     *****      0.021    

	 */
}
