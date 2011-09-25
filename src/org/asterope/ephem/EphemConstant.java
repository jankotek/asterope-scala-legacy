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

/**
 * Some useful physical constants, preferently in International System of Units.
 * <P>
 * Values taken from CODATA 2006, CODATA 2002, and E.R. Cohen, B. N. Taylor, 
 * "The Fundamental Physical Constants", Phys. Today, August 1997. 
 * AU definition following JPL Ephemeris.
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @author M. Huss
 * @version 1.0
 */
public class EphemConstant
{
	/**
	 * Diference between Julian day and Modified Julian day, that starts on
	 * Nov, 17, 1858.
	 */
	public static double JD_MINUS_MJD = 2400000.5;
	/**
	 * Two times Pi.
	 */
	public static final double TWO_PI = 2D * Math.PI;

	/**
	 * Pi divided by two.
	 */
	public static final double PI_OVER_TWO = Math.PI / 2D;

	/**
	 * Degrees in a circle = 360.
	 */
	public static final double DEGREES_PER_CIRCLE = 360D;

	/**
	 * Arc minutes in one degree = 60.
	 */
	public static final double MINUTES_PER_DEGREE = 60D;

	/**
	 * Arc seconds in one degree = 3600.
	 */
	public static final double SECONDS_PER_DEGREE = 60D * MINUTES_PER_DEGREE;

	/**
	 * Arc seconds to radians.
	 */
	public static final double ARCSEC_TO_RAD = Math.PI / (180.0 * 3600.0);

	/**
	 * Radians to arc seconds.
	 */
	public static final double RAD_TO_ARCSEC = 1.0 / ARCSEC_TO_RAD;

	/**
	 * Arc seconds to degrees.
	 */
	public static final double ARCSEC_TO_DEG = 1.0 / 3600.0;

	/**
	 * Radians to hours.
	 */
	public static final double RAD_TO_HOUR = 180.0 / (15.0 * Math.PI);

	/**
	 * Radians to days.
	 */
	public static final double RAD_TO_DAY = RAD_TO_HOUR / 24.0;

	/**
	 * Radians to degrees.
	 */
	public static final double RAD_TO_DEG = 180.0 / Math.PI;

	/**
	 * Degrees to radians.
	 */
	public static final double DEG_TO_RAD = 1.0 / RAD_TO_DEG;

	/**
	 * Julian century conversion constant = 100 * days per year.
	 */
	public static final double JULIAN_DAYS_PER_CENTURY = 36525.0;

	/**
	 * Julian millenia conversion constant = 1000 * days per year.
	 */
	public static final double JULIAN_DAYS_PER_MILLENIA = 365250.0;

	/**
	 * Hours in one day as a double.
	 */
	public static final double HOURS_PER_DAY = 24D;

	/**
	 * The fraction of one day equivalent to one hour = 1/24.
	 */
	public static final double DAYS_PER_HOUR = 1D / HOURS_PER_DAY;

	/**
	 * Minutes in one hour as a double.
	 */
	public static final double MINUTES_PER_HOUR = 60D;

	/**
	 * Seconds in one minute.
	 */
	public static final double SECONDS_PER_MINUTE = 60D;

	/**
	 * Seconds in one hour.
	 */
	public static final double SECONDS_PER_HOUR = MINUTES_PER_HOUR * SECONDS_PER_MINUTE;

	/**
	 * Seconds in one day.
	 */
	public static final double SECONDS_PER_DAY = HOURS_PER_DAY * SECONDS_PER_HOUR;

	/**
	 * Milliseconds in one second.
	 */
	public static final double MILLISECONDS_PER_SECOND = 1000D;

	/**
	 * Milliseconds in one hour.
	 */
	public static final double MILLISECONDS_PER_HOUR = MILLISECONDS_PER_SECOND * SECONDS_PER_HOUR;

	/**
	 * Our default epoch. <BR>
	 * The Julian Day which represents noon on 2000-01-01.
	 */
	public static final double J2000 = 2451545.0;

	/**
	 * Length of a tropical year in days.
	 */
	public static final double TROPICAL_YEAR = 365.242198781;

	/**
	 * B1950 epoch, equal to JD 2433282.42346. Bessel year starts when the RA of
	 * the sun is 18h 40m, and it is based on the rotation of a ficticious mean
	 * sun. It's length is equal to a tropical year.
	 */
	public static final double B1950 = 2433282.42345905;

	/**
	 * B1900 epoch, equal to JD 2415020.31352. Bessel year starts when the RA of
	 * the sun is 18h 40m, and it is based on the rotation of a ficticious mean
	 * sun. It's length is equal to a tropical year.
	 */
	public static final double B1900 = B1950 - 50.0 * EphemConstant.TROPICAL_YEAR;

	/**
	 * J1950 epoch. Noon on 1950-1-1.
	 */
	public static final double J1950 = EphemConstant.J1900 + 0.5 * EphemConstant.JULIAN_DAYS_PER_CENTURY;

	/**
	 * J2050 epoch. Noon on 2050-1-1.
	 */
	public static final double J2050 = EphemConstant.J2000 + 0.5 * EphemConstant.JULIAN_DAYS_PER_CENTURY;

	/**
	 * J1900 epoch. Noon on 1900-1-1.
	 */
	public static final double J1900 = 2415020.0;

	/**
	 * A convenient rounding value, equal to 0.5.
	 */
	public static final double ROUND_UP = 0.5;

	/**
	 * Astronomical Unit in km. As defined by JPL.
	 */
	public static final double AU = 149597870.691;

	/**
	 * Earth equatorial radius in km. IERS 2003 Conventions.
	 */
	public static final double EARTH_RADIUS = 6378.1366;

	/**
	 * Earth orbit mean rate in rad/day (Gauss Gravitational constant).
	 */
	public static final double EARTH_MEAN_ORBIT_RATE = 0.01720209895;

	/**
	 * Length of a parsec, in meters.
	 */
	public static final double PARSEC = 3.0856775807E+16;

	/**
	 * Length of a (Gregorian) light year, in meters.
	 */
	public static final double LIGHT_YEAR = 9460536207068016.0;

	/**
	 * Light time in days for 1 AU. DE405 definition.
	 */
	public static final double LIGHT_TIME_DAYS_PER_AU = 0.00577551833109;

	/**
	 * Earth flatenning factor. IERS 2003 Conventions.
	 */
	public static final double EARTH_FLATENNING = 298.25642;

	/**
	 * Nominal mean rotational angular velocity of Earth in radians per second.
	 * IERS 2003 Conventions.
	 */
	public static final double EARTH_MEAN_ROTATION_RATE = 7.2921150e-5;

	/**
	 * Speed of light in m/s, as it is defined.
	 */
	public static final double SPEED_OF_LIGHT = 299792458.0;

	/**
	 * Heliocentric gravitational constant, in m^3/s^2 (DE405).
	 */
	public static final double SUN_GRAVITATIONAL_CONSTANT = 1.32712440017987e20;

	/**
	 * Length of a sidereal day in days according to IERS Conventions.
	 */
	public static final double SIDEREAL_DAY_LENGTH = 1.00273781191135448;

	/**
	 * Planck constant in J*s. Value from CODATA 2006.
	 */
	public static final double PLANCK_CONSTANT = 6.62606896E-34;

	/**
	 * Constant to transform erg to Jules.
	 */
	public static final double ERGIO_TO_JULE = 1.0E-7;

	/**
	 * Constant to transform erg/(s cm^2 Hz) to Jansky.
	 */
	public static final double ERG_S_CM2_HZ_TO_JY = 1.0E23;

	/**
	 * Constant to transform Jy to W/(m^2 Hz).
	 */
	public static final double JY_TO_W_HZ_M2 = ERGIO_TO_JULE * 1E4 / ERG_S_CM2_HZ_TO_JY;

	/**
	 * Boltzmann constant in J/K. Value from CODATA 2006.
	 */
	public static final double BOLTZMANN_CONSTANT = 1.3806504E-23;

	/**
	 * Hz to K transform coefficient.
	 */
	public static final double HZ_TO_K = PLANCK_CONSTANT / BOLTZMANN_CONSTANT;

	/**
	 * cm^-1 to K transform coefficient.
	 */
	public static final double CM_TO_K = HZ_TO_K * SPEED_OF_LIGHT * 100d;

	/**
	 * Mass of the sun in Kg. Value from E. R. Cohen, B. N. Taylor, <I>The
	 * Fundamental Physical Constants</I>, Physics Today, august 1997.
	 */
	public static final double SUN_MASS = 1.98892E30;

	/**
	 * Luminosity of the sun in W.
	 */
	public static final double SUN_LUMINOSITY = 3.827E26;

	/**
	 * Stefan-Boltzmann en W/(m^2 K^4). Value from CODATA 2006.
	 */
	public static final double STEFAN_BOLTZMANN_CONSTANT = .00000005670400;

	/**
	 * Electron charge in C (=> J/eV). Value from CODATA 2002.
	 */
	public static final double ELECTRON_CHARGE = 1.602176487E-19;

	/**
	 * Avogadro number in mol^-1.
	 */
	public static final double AVOGADRO_CONSTANT = 6.02214179E+23;

	/**
	 * Wien constant in m K. Value from CODATA 2002.
	 */
	public static final double WIEN_CONSTANT = .0028977685;

	/**
	 * Gravitational constant in m^3/(kg s^2). Value from CODATA 2002.
	 */
	public static final double GRAVITATIONAL_CONSTANT = 6.67428E-11;

	/**
	 * Earth's gravity value at sea level in m/s^2. Value from CODATA 2002.
	 */
	public static final double GRAVITY_FIELD = 9.80665;

	/**
	 * molar volume of an ideal gas in m^3/mol. Value from CODATA 2002
	 */
	public static final double VOLUME_OF_1MOL_IDEAL_GAS = .022710981;

	/**
	 * Bohr's magneton in Mev/T. Value from CODATA 2002
	 */
	public static final double BOHR_MAGNETON = 5.7883817555E-11;

	/**
	 * Electron mass in Kg. Value from CODATA 2006.
	 */
	public static final double ELECTRON_MASS = 9.10938215E-31;

	/**
	 * Proton mass in Kg. Value from CODATA 2006.
	 */
	public static final double PROTON_MASS = 1.672621637E-27;

	/**
	 * Solar mean radius in km.
	 */
	public static final double SUN_RADIUS = 696000.0;

	/**
	 * Fine structure constant. Value from CODATA 2002.
	 */
	public static final double FINE_STRUCTURE_CONSTANT = 0.0072973525376;

	/**
	 * Classical radius of the electron in m. Value from CODATA 2002.
	 */
	public static final double ELECTRON_RADIUS = 2.8179402894E-15;

	/**
	 * Bohr radius in m. Value from CODATA 2002.
	 */
	public static final double BOHR_RADIUS = 5.2917720859E-11;

	/**
	 * One inch in m.
	 */
	public static final double INCH = .0254;

	/**
	 * Temperature of 0 Celsius in K.
	 */
	public static final double TEMPERATURE_OF_0_CELSIUS = 273.15;

	/**
	 * One atm in Pa (=760 torr).
	 */
	public static final double ATMOSPHERE = 101325.0;

	/**
	 * Atomic rade2Vector mass, or 1/12 of the mass of the C-12 isotope, in Kg. Value
	 * from CODATA 2006.
	 */
	public static final double AMU = 1.660538782E-27;

	/**
	 * Mass of the hydrogen molecule in Kg.
	 */
	public static final double H2_MASS = 2.0 * PROTON_MASS + ELECTRON_MASS;
	
	public static final double MOON_RELATIVE_MASS = 27068700.387534;
	
	public static final double EARTH_RELATIVE_MASS = 332946.050895;

}
