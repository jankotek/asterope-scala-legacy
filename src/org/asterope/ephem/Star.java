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
 * Class that performs some simple calculations about star properties.
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class Star
{
	/**
	 * Calculate luminosity ratio L1/L2.
	 * 
	 * @param m1 Absolute magnitude of body 1.
	 * @param m2 Absolute magnitude of body 2.
	 * @return Luminosity ratio.
	 */
	public static double luminosityRatio(double m1, double m2)
	{
		double lum = Math.pow(10.0, (m2 - m1) / 2.5);
		return lum;
	}

	/**
	 * Computes the combined apparent magnitude of two objects.
	 * @param m1 Magnitude of body 1.
	 * @param m2 Magnitude of body 2.
	 * @return The combined magnitude.
	 */
	public static double combinedMagnitude(double m1, double m2)
	{
		if (m1 > m2) {
			double t = m1;
			m1 = m2;
			m2 = t;
		}
		double lum = Star.luminosityRatio(m1, m2);
		double m = m2 - 2.5 * Math.log10(1.0 + lum);
		return m;
	}
	
	/**
	 * Obtain absolute magnitude.
	 * 
	 * @param ap_mag Apparent magnitude.
	 * @param dist Distance in parsec.
	 * @return Absolute magnitude.
	 */
	public static double absoluteMagnitude(double ap_mag, double dist)
	{
		double abs_mag = ap_mag + 5 - 5 * Math.log(dist) / Math.log(10.0);
		return abs_mag;
	}

	/**
	 * Gets the distance of a star.
	 * 
	 * @param ap_mag Apparent magnitude.
	 * @param abs_mag Absolute magnitude.
	 * @return The distance in parsec.
	 */
	public static double distance(double ap_mag, double abs_mag)
	{
		double dist = Math.pow(10.0, (ap_mag + 5 - abs_mag) / 5.0);
		return dist;
	}

	/**
	 * Obtains the dynamical parallax using Kepler's third law.
	 * 
	 * @param a Distance in arcseconds.
	 * @param P Period in years.
	 * @param M Mass of the system in solar masses.
	 * @return Parallax in arcseconds.
	 */
	public static double dynamicalParallax(double a, double P, double M)
	{
		double par = a / (Math.pow(P * P * M, 1.0 / 3.0));
		return par;
	}

	/**
	 * Obtains gravitational energy of a body in J.
	 * 
	 * @param M Mass in solar masses.
	 * @param r Radius in sola radii
	 * @return Energy in J.
	 */
	public static double gravitationalEnergy(double M, double r)
	{
		double Eg = 3.0 * EphemConstant.GRAVITATIONAL_CONSTANT * M * M * EphemConstant.SUN_MASS * EphemConstant.SUN_MASS / (5.0 * r * EphemConstant.SUN_RADIUS * 1000.0);
		return Eg;
	}

	/**
	 * Obtains gravitational time scale, or the maximum time a given body could
	 * radiate energy during a contraction phase.
	 * 
	 * @param M Mass in solar masses.
	 * @param r Radius in solar radii.
	 * @param L Luminosity in J/s.
	 * @return Time in seconds.
	 */
	public static double gravitationalTimeScale(double M, double r, double L)
	{
		double t = gravitationalEnergy(M, r) / L;
		return t;
	}

	/**
	 * Obtains Schwartzchild radius for an object of certain mass. If the radius
	 * is lower than this value, then the object is a black hole.
	 * 
	 * @param M Mass in solar masses.
	 * @return Radius in solar radii.
	 */
	public static double schwartzchildRadius(double M)
	{
		double r = 2.0 * EphemConstant.GRAVITATIONAL_CONSTANT * M * EphemConstant.SUN_MASS / (EphemConstant.SPEED_OF_LIGHT * EphemConstant.SPEED_OF_LIGHT);

		return r / (1000.0 * EphemConstant.SUN_RADIUS);
	}

	/**
	 * Obtain wind mass lost ratio in solar masses by year. The effect in
	 * stellar evolution is neglective in most of the cases.
	 * 
	 * @param n Proton density in cm^-3 at certain distance from the star.
	 *        Typical value of 7 for the sun.
	 * @param v Proton velocity in km/s at the same distance. Typical value of
	 *        500 for the sun.
	 * @param r The distance where n and v and measured in AU. Typical value of
	 *        1 for the sun.
	 * @return Mass lose ratio in solar masses by year. Typical value of 5E-14
	 *         Msol/year for the sun.
	 */
	public static double stellarWindMass(double n, double v, double r)
	{
		double dm_dt = 4.0 * Math.PI * r * r * EphemConstant.AU * EphemConstant.AU * n * 1.0E15 * EphemConstant.H2_MASS * v / EphemConstant.SUN_MASS;

		return dm_dt * EphemConstant.SECONDS_PER_DAY * EphemConstant.TROPICAL_YEAR;
	}

	/**
	 * Returns the mass of an object using Kepler's third law.
	 * 
	 * @param a Distance of a measure in AU.
	 * @param P Oribtal period at that position in years.
	 * @return Mass in solar masses.
	 */
	public static double kepler3rdLawOfMasses(double a, double P)
	{
		double M = P * P / (a * a * a);

		return M;
	}

	/**
	 * Obtain total flux of a black body at certain temperature.
	 * 
	 * @param T Effective temperature.
	 * @return Flux in W/m^2.
	 */
	public static double blackBodyFlux(double T)
	{
		return EphemConstant.STEFAN_BOLTZMANN_CONSTANT * Math.pow(T, 4.0);
	}

	/**
	 * Applies Tully-Fisher relation for obtaining the luminosity of a spiral
	 * galaxy.
	 * 
	 * @param v Rotation velocity in km/s in the outer galaxy.
	 * @return Luminosity in solar units.
	 */
	public static double tullyFisherRelation(double v)
	{
		double beta = 4.55;
		double L0 = 1.0E10;

		double k = L0 / Math.pow(220.0, beta);
		double L = k * Math.pow(v, beta);

		return L;
	}

	/**
	 * Applies Fabber-Jackson relation for obtaining the luminosity of an
	 * elliptical galaxy.
	 * 
	 * @param sigma Velocity dispersion in km/s in the inner region.
	 * @return Luminosity in solar units.
	 */
	public static double fabberJacksonRelation(double sigma)
	{
		double beta = 4.55;
		double L0 = 1.0E10;

		double k = L0 / Math.pow(220.0, beta);
		double L = k * Math.pow(sigma, beta);

		return L;
	}

	/**
	 * Calculates Sechter luminosity function.
	 * 
	 * @param L Luminosity in solar units.
	 * @param alfa Slope parameter.
	 * @return Number of objects per rade2Vector of luminosity, dN/dL.
	 */
	public static double sechterRelation(double L, double alfa)
	{
		double L0 = 1.0E10;

		double phi0 = 1.0 / gammaFunction(1.0 + alfa);
		double phi = Math.pow(L / L0, alfa) * Math.exp(-L / L0) * phi0 / L0;

		return phi;
	}

	/**
	 * Obtains gamma function for evaluating Sechter luminosity function.
	 * 
	 * @param alfa Slope parameter. Must be integer or half-integer.
	 * @return Value of the function.
	 */
	public static double gammaFunction(double alfa)
	{
		if (alfa <= 1)
			return 0.0;

		double g = 1.0;
		do
		{
			g = g * (alfa - 1.0);
			alfa = alfa - 1.0;
		} while (alfa > 1.0);

		if (alfa == 0.5)
			g = g * Math.sqrt(Math.PI);

		return g;
	}

	/**
	 * Obtains gravitational redshift.
	 * 
	 * @param M Mass in solar masses.
	 * @param R Radio in solar radii.
	 * @return Redshift.
	 */
	public static double gravitationalRedshift(double M, double R)
	{
		double z = -1.0 + Math
				.sqrt(1.0 / (1.0 - 2.0 * M * EphemConstant.SUN_MASS * EphemConstant.GRAVITATIONAL_CONSTANT / (R * EphemConstant.SUN_RADIUS * 1000.0 * EphemConstant.SPEED_OF_LIGHT * EphemConstant.SPEED_OF_LIGHT)));

		return z;
	}

	/**
	 * Obtains cosmological redshift.
	 * 
	 * @param v Velocity of the source in m/s.
	 * @return Redshift.
	 */
	public static double cosmologicalRedshift(double v)
	{
		double z = -1.0 + Math.sqrt((EphemConstant.SPEED_OF_LIGHT + v) / (EphemConstant.SPEED_OF_LIGHT - v));

		return z;
	}

	/**
	 * Obtain wavelength of the peak emission of a black body applying Wien's
	 * approximation.
	 * 
	 * @param T Temperature in K.
	 * @return Wavelength in m.
	 */
	public static double wienApproximation(double T)
	{
		return EphemConstant.WIEN_CONSTANT / T;
	}

	/**
	 * Evaluates Planck's function.
	 * 
	 * @param T Temperature in K.
	 * @param lambda Wavelength in m.
	 * @return B in Jy/sr.
	 */
	public static double blackBody(double T, double lambda)
	{
		double nu = EphemConstant.SPEED_OF_LIGHT / lambda;
		double B = 2.0 * EphemConstant.PLANCK_CONSTANT * Math.pow(nu, 3.0) / (EphemConstant.SPEED_OF_LIGHT * EphemConstant.SPEED_OF_LIGHT);
		B = B / (Math.exp(EphemConstant.PLANCK_CONSTANT * nu / (EphemConstant.BOLTZMANN_CONSTANT * T)) - 1.0);

		B = B * EphemConstant.ERG_S_CM2_HZ_TO_JY;
		return B;
	}

	/**
	 * Evaluates Planck's function.
	 * 
	 * @param T Temperature in K.
	 * @param nu Frequency in Hz.
	 * @return B in Jy/sr.
	 */
	public static double blackBodyNu(double T, double nu)
	{
		double B = 2.0 * EphemConstant.PLANCK_CONSTANT * Math.pow(nu, 3.0) / (EphemConstant.SPEED_OF_LIGHT * EphemConstant.SPEED_OF_LIGHT);
		B = B / (Math.exp(EphemConstant.PLANCK_CONSTANT * nu / (EphemConstant.BOLTZMANN_CONSTANT * T)) - 1.0);

		B = B * EphemConstant.ERG_S_CM2_HZ_TO_JY;
		return B;
	}

	/**
	 * Evaluates error of Planck's function, taking into account certain error
	 * in the temperature.
	 * 
	 * @param T Temperature in K.
	 * @param dT Temperature error in K.
	 * @param lambda Wavelength in m.
	 * @return B error in Jy/sr.
	 */
	public static double blackBodyFluxError(double T, double dT, double lambda)
	{
		double nu = EphemConstant.SPEED_OF_LIGHT / lambda;
		double B = 2.0 * EphemConstant.PLANCK_CONSTANT * Math.pow(nu, 3.0) / (EphemConstant.SPEED_OF_LIGHT * EphemConstant.SPEED_OF_LIGHT);
		B = B / Math.pow(Math.exp(EphemConstant.PLANCK_CONSTANT * nu / (EphemConstant.BOLTZMANN_CONSTANT * T)) - 1.0, 2.0);
		B = B * Math.exp(EphemConstant.PLANCK_CONSTANT * nu / (EphemConstant.BOLTZMANN_CONSTANT * T));
		B *= -dT * EphemConstant.PLANCK_CONSTANT * nu / (EphemConstant.BOLTZMANN_CONSTANT * T * T);

		B = B * EphemConstant.ERG_S_CM2_HZ_TO_JY;
		return B;
	}

	/**
	 * Obtains surface brightness of an object.
	 * 
	 * @param m Magnitude.
	 * @param r Radius in arcseconds. If the object is not circular, an
	 *        equivalent value can be given so that the area (PI * r^2) in equal
	 *        to the area of the object in the sky.
	 * @return Brightness in mag/arcsecond^2, or 0 if radius is 0.
	 */
	public static double getSurfaceBrightness(double m, double r)
	{
		double bright = 0.0;
		if (r <= 0.0) return bright;
		
		double area = Math.PI * (r / 60.0) * (r / 60.0);
		double s1 = m + Math.log(area) / Math.log(Math.pow(100.0, 0.2));
		bright = s1 + 8.890756;
		return bright;
	}

	/**
	 * Obtain the luminosity using the mass-luminosity relation for main sequence
	 * stars.
	 * @param mass Mass in solar units.
	 * @return Luminosity in solar units.
	 */
	public static double getLuminosityFromMassLuminosityRelation(double mass)
	{
		double l = Math.pow(mass, 3.5);
		return l;
	}
	/**
	 * Obtain the luminosity using the mass-luminosity relation for main sequence
	 * stars.
	 * @param luminosity Luminosity in solar units.
	 * @return Mass in solar units.
	 */
	public static double getMassFromMassLuminosityRelation(double luminosity)
	{
		double m = Math.log(luminosity) / Math.log(3.5);
		return m;
	}

	/**
	 * Obtains approximate star life time for a given mass.
	 * @param mass Star mass in solar units.
	 * @return Lifetime in years.
	 */
	public static double getStarLifeTime(double mass)
	{
		double lum = getLuminosityFromMassLuminosityRelation(mass);
		double time = 1.0E10 * mass / lum;
		return time;
	}
	
	/**
	 * Obtains star radius.
	 * 
	 * @param luminosity Luminosity in solar units.
	 * @param temperature Temperature in K.
	 * @return Radius in solar radii.
	 */
	public static double getStarRadius(double luminosity, double temperature)
	{
		double rs = Math
				.sqrt(luminosity * EphemConstant.SUN_LUMINOSITY / (4.0 * Math.PI * EphemConstant.STEFAN_BOLTZMANN_CONSTANT * Math
						.pow(temperature, 4.0))) / (EphemConstant.SUN_RADIUS * 1000.0);

		return rs;
	}
	/**
	 * Obtains star luminosity.
	 * 
	 * @param radius Radius in solar untis.
	 * @param temperature Temperature in K.
	 * @return Luminosity in solar units.
	 */
	public static double getStarLuminosity(double radius, double temperature)
	{
		double luminosity =  Math.pow(radius * EphemConstant.SUN_RADIUS * 1000.0, 2.0) * 
			(4.0 * Math.PI * EphemConstant.STEFAN_BOLTZMANN_CONSTANT * Math.pow(temperature, 4.0));

		return luminosity / EphemConstant.SUN_LUMINOSITY;
	}
	
	/**
	 * Obtain distance modulus
	 * @param distance Distance in pc.
	 * @return Distance modulus.
	 */
	public static double getDistanceModulus(double distance)
	{
		double module = -5.0 + 5.0 * Math.log10(distance);
		return module;
	}

	/**
	 * Obtains the surface gravity.
	 * @param mass Mass in solar masses.
	 * @param radius Radius in solar radii.
	 * @return Gravity in m/s^2.
	 */
	public static double getSurfaceGravity(double mass, double radius)
	{
		double g = EphemConstant.GRAVITATIONAL_CONSTANT * mass * EphemConstant.SUN_MASS / (Math.pow(radius * EphemConstant.SUN_RADIUS * 1000.0, 2.0));
		return g;
	}

	/**
	 * Obtains the magnitude of a star given the flux on it, the background, and the
	 * same properties (including magnitude) of a comparison star close to it.
	 * @param flux Flux of the star.
	 * @param skyBackground Flux of the background.
	 * @param fluxComparison Flux of a comparison star.
	 * @param skyBackgroundComparison Sky background around comparison star.
	 * @param magComparison Magnitude of a comparison star.
	 * @return Magnitude of the star.
	 */
	public static double getStarMagnitude(double flux, double skyBackground, double fluxComparison, double skyBackgroundComparison,
			double magComparison)
	{
		flux = flux - skyBackground;
		fluxComparison = fluxComparison - skyBackgroundComparison;
		double mag = 2.5 * Math.log(fluxComparison / flux) / Math.log(10.0) + magComparison;
		return mag;
	}
	
	/**
	 * Calculates the size of an impact crater.
	 * @param impactorDiameter Diameter of the impactor in m.
	 * @param impactorDensity Density of the impactor in kg/m^3.
	 * @param impactorVelocity Velocity of the impactor in km/s.
	 * @param zenithAngle Zenith angle of the impactor trajectory in degrees.
	 * @return Diameter and depth of the impact crater in m.
	 */
	public static double[] impactCrater(double impactorDiameter, double impactorDensity, 
			double impactorVelocity, double zenithAngle)
	{
		double IV=impactorVelocity * 1000.0;
		double VI=Math.PI * impactorDiameter * impactorDiameter * impactorDiameter / 6.0;
		double GF=Math.pow((Math.sin((90.0 - zenithAngle) * EphemConstant.DEG_TO_RAD)), .33);
		double MI=impactorDensity * VI;
		double KE=.5 * MI * IV * IV;
		double KT=KE / 4.2E+12; // impactor kinetic energy in kT TNT
		double diameter=2.0 * 18.0 * Math.pow(KT, 0.3) * GF;
		double depth=9.0 * Math.pow(KT, 0.3) * GF;
		return new double[] {diameter, depth};
	}

}
