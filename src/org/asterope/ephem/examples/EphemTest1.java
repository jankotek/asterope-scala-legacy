package org.asterope.ephem.examples;


import org.asterope.ephem.*;


/**
 * Simple example of ephemeris calculations using only a main program.
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class EphemTest1 {

	
	
	/**
	 * A program to test ephemeris calculations.
	 * @param args Initial arguments, unused.
	 */
	public static void main(String[] args) {
		
			// We need three objects: TimeElement, ObserverElement, and EphemerisElement
			AstroDate astro = new AstroDate(1,  AstroDate.JANUARY, 2000);
			TimeElement time = new TimeElement(astro, TimeElement.Scale.UNIVERSAL_TIME_UTC);
			
			ObserverElement obs = new ObserverElement("Madrid",
					-3.7100 * EphemConstant.DEG_TO_RAD, 40.420 * EphemConstant.DEG_TO_RAD , 
					693, 1);
			
			// The ephemeris object defines the target body and how to calculate ephemeris. The algorithm
			// is set to Moshier, which is the best way for general calculations
			EphemerisElement eph = new EphemerisElement(Target.Saturn, EphemerisElement.Ephem.APPARENT,
					EphemerisElement.EQUINOX_OF_DATE, EphemerisElement.TOPOCENTRIC, Precession.Method.IAU2000,
					EphemerisElement.Frame.J2000);

			// Include rise/set/transit calculations
			boolean full_ephem = true;
			MoshierPlanetEphem moshier = new MoshierPlanetEphem();

			// Calculate
			EphemElement ephem = moshier.getEphemeris(time, obs, eph,full_ephem);

			// Report results
			System.out.println(ephem);
			
	}

}
