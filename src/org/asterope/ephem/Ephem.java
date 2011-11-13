package org.asterope.ephem;


public interface Ephem {
	
	/**
	 * Calculate planetary positions, providing full data. This method applies
	 * the planetary algorithm selected in the ephemeris object. Full
	 * data includes rise, set, and transit times, refered to the current day or
	 * the next events in time (if the object is actually below the horizon).
	 * <P>
	 * As a particular case, natural satellite ephemeris are calculated using
	 * Series96 in the 20th and 21st centuries, otherwise Moshier method will be
	 * used.
	 * <P>
	 * 
	 * @param time Time object containing the date.
	 * @param obs Observer object containing the observer position.
	 * @param eph Ephemeris object defining the ephemeris properties.
	 * @param full_ephem True to obtain also instants of rise, set, transit.
	 * @return Ephem object containing full ephemeris data.

	 */
	EphemElement getEphemeris(TimeElement time, // Time Element
			ObserverElement obs, // Observer Element
			EphemerisElement eph, // Ephemeris Element
			boolean full_ephem);


	/**
	 * Get rectangular ecliptic geocentric position of a planet in equinox
	 * J2000.
	 * 
	 * @param JD Julian day in TDB.
	 * @param planet Planet ID.
	 * @return Array with x, y, z, vx, vy, vz coordinates.
	 */
	public double[] getGeocentricPosition(double JD, Target planet, double light_time);

}
