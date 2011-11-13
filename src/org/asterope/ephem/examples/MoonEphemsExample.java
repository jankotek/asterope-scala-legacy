package org.asterope.ephem.examples;

import org.asterope.ephem.*;
import org.asterope.ephem.moons.MoonEphem;
import org.asterope.ephem.moons.MoonEphemElement;

import java.util.Map;


public class MoonEphemsExample {

	public static void main(String[] args) {
		AstroDate astroi = new AstroDate(2009,4,21,6,41,43); 
		TimeElement time = new TimeElement(astroi,TimeElement.Scale.UNIVERSAL_TIME_UTC);
		
		ObserverElement obs = ObserverElement.MADRID;
		
		// The ephemeris object defines the target body and how to calculate ephemeris. The algorithm
		// is set to Moshier, which is the best way for general calculations
		EphemerisElement eph = new EphemerisElement(Target.Jupiter, EphemerisElement.Ephem.APPARENT,
				EphemerisElement.EQUINOX_OF_DATE, EphemerisElement.TOPOCENTRIC, Precession.Method.IAU2000,
				EphemerisElement.Frame.J2000);
		
		Map<Target,MoonEphemElement> e = MoonEphem.galileanSatellitesEphemerides_L1(time, obs, eph);
		System.out.println(e.keySet());
		for(Target t :e.keySet()){
			System.out.println(t +" + "+e.get(t).rightAscension);
		}
		for(Target t :e.keySet()){
			System.out.println(t +" + "+e.get(t).mutualPhenomena);
		}		
		

	}
}
