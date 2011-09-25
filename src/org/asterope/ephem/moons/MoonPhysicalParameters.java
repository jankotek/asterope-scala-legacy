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


/**
 * A class to obtain the orientation and physical ephemeris of a satellite,
 * based on IAU2000 recomendations.
 * 
 * @see MoonEphem
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class MoonPhysicalParameters
{
	/**
	 * Obtain physical parameters of the satellite: angular radius, visual
	 * magnitude, and axis orientation. Any satellite with known information
	 * (see IAU2000 report by Seidelmann et al.) is supported. No supported
	 * objects will have zero in the corresponding fields.
	 * 
	 * @param JD Julian day in dynamical time.
	 * @param ephem_sun Ephem object with ephemeris of sun.
	 * @param ephem_obj Moon ephem object to take and store data.
	 * @param eph Ephemeris object defining the ephemeris properties.
	 * @return Moon ephem with the completed data.
	 */
	public static MoonEphemElement physicalParameters(double JD, // Julian
																	// day
			EphemElement ephem_sun, // Ephem Element
			MoonEphemElement ephem_obj, // Ephem Element
			EphemerisElement eph) // Ephemeris Element
	{
		MoonEphemElement ephem = (MoonEphemElement) ephem_obj.clone();

		// Put distances and angles in a more confortable way
		double RE = ephem_sun.distance;
		double RP = ephem.distanceFromSun;
		double RO = ephem.distance;
		double LP = ephem.heliocentricEclipticLongitude;
		double eq_sun[] = LocationElement.parseLocationElement(new LocationElement(ephem_sun.rightAscension,
				ephem_sun.declination, ephem_sun.distance));
		double LE = Math.PI + LocationElement.parseRectangularCoordinates(
				EphemUtils.equatorialToEcliptic(eq_sun, JD, eph.ephemMethod)).getLongitude();

		if (eph.targetBody != Target.Sun)
		{
			// Elongation
			double DELO = (RE * RE + RO * RO - RP * RP) / (2.0 * RE * RO);
			ephem.elongation = (float) Math.acos(DELO);

			// Phase and phase angle. Note phase angle can be
			// negative to represent the case LE < LP (before opposition)
			double DPH = ((RP * RP + RO * RO - RE * RE) / (2.0 * RP * RO));
			double DPHA = RE * Math.sin(LE - LP) / RO; // Sin(phase angle)
			ephem.phaseAngle = (float) (Math.acos(DPH) * DPHA / Math.abs(DPHA));
			ephem.phase = (float) ((1.0 + DPH) * 0.5);
		}

		// Visual magnitude and axis orientation
		double calc_time = EphemUtils.toCenturies(JD);
		double rr = ephem.distance * ephem.distanceFromSun;
		double mag = 0.0;
		ephem.northPoleRA = 0.0f;
		ephem.northPoleDEC = 0.0f;
		double lon0 = 0.0; // Initial longitude at JD = 2451545.0
		double rot_per_day = 0.0; // Degrees per day of rotation of satellite
		double shape = Target.getFlatenningFactor(eph.targetBody); // (equatorial
																	// - polar
																	// radius) /
																	// (equatorial
																	// radius)
		double J1 = (073.32 + 91472.9 * calc_time) * EphemConstant.DEG_TO_RAD;
		double J2 = (024.62 + 45137.2 * calc_time) * EphemConstant.DEG_TO_RAD;
		double J3 = (283.90 + 4850.7 * calc_time) * EphemConstant.DEG_TO_RAD;
		double J4 = (355.80 + 1191.3 * calc_time) * EphemConstant.DEG_TO_RAD;
		double J5 = (119.90 + 262.1 * calc_time) * EphemConstant.DEG_TO_RAD;
		double J6 = (229.80 + 64.3 * calc_time) * EphemConstant.DEG_TO_RAD;
		double J7 = (352.25 + 2382.6 * calc_time) * EphemConstant.DEG_TO_RAD;
		double J8 = (113.35 + 6070.0 * calc_time) * EphemConstant.DEG_TO_RAD;
		double S1 = (353.32 + 75706.7 * calc_time) * EphemConstant.DEG_TO_RAD;
		double S2 = (028.72 + 75706.7 * calc_time) * EphemConstant.DEG_TO_RAD;
		double S3 = (177.40 - 36505.5 * calc_time) * EphemConstant.DEG_TO_RAD;
		double S4 = (300.00 - 7225.9 * calc_time) * EphemConstant.DEG_TO_RAD;
		double S5 = (316.45 + 506.2 * calc_time) * EphemConstant.DEG_TO_RAD;
		double S6 = (354.20 - 1016.3 * calc_time) * EphemConstant.DEG_TO_RAD;
		double S7 = (29.80 - 52.1 * calc_time) * EphemConstant.DEG_TO_RAD;
		double U1 = (115.75 + 54991.87 * calc_time) * EphemConstant.DEG_TO_RAD;
		double U2 = (141.69 + 41887.66 * calc_time) * EphemConstant.DEG_TO_RAD;
		double U3 = (135.03 + 29927.35 * calc_time) * EphemConstant.DEG_TO_RAD;
		double U4 = (061.77 + 25733.59 * calc_time) * EphemConstant.DEG_TO_RAD;
		double U5 = (249.32 + 24471.46 * calc_time) * EphemConstant.DEG_TO_RAD;
		double U6 = (043.86 + 22278.41 * calc_time) * EphemConstant.DEG_TO_RAD;
		double U7 = (077.66 + 20289.42 * calc_time) * EphemConstant.DEG_TO_RAD;
		double U8 = (157.36 + 16652.76 * calc_time) * EphemConstant.DEG_TO_RAD;
		double U9 = (101.81 + 12872.63 * calc_time) * EphemConstant.DEG_TO_RAD;
		double U10 = (138.64 + 8061.81 * calc_time) * EphemConstant.DEG_TO_RAD;
		double U11 = (102.23 - 2024.22 * calc_time) * EphemConstant.DEG_TO_RAD;
		double U12 = (316.41 + 2863.96 * calc_time) * EphemConstant.DEG_TO_RAD;
		double U13 = (304.01 - 51.94 * calc_time) * EphemConstant.DEG_TO_RAD;
		double U14 = (308.71 - 93.17 * calc_time) * EphemConstant.DEG_TO_RAD;
		double U15 = (340.82 - 75.32 * calc_time) * EphemConstant.DEG_TO_RAD;
		double U16 = (259.14 - 504.81 * calc_time) * EphemConstant.DEG_TO_RAD;
		double N0 = (357.85 + 52.316 * calc_time) * EphemConstant.DEG_TO_RAD;
		double N1 = (323.92 + 62606.6 * calc_time) * EphemConstant.DEG_TO_RAD;
		double N2 = (220.51 + 55064.2 * calc_time) * EphemConstant.DEG_TO_RAD;
		double N3 = (354.27 + 46564.5 * calc_time) * EphemConstant.DEG_TO_RAD;
		double N4 = (075.31 + 26109.4 * calc_time) * EphemConstant.DEG_TO_RAD;
		double N5 = (033.36 + 14325.4 * calc_time) * EphemConstant.DEG_TO_RAD;
		double N6 = (142.61 + 2824.6 * calc_time) * EphemConstant.DEG_TO_RAD;
		double N7 = (177.85 + 52.316 * calc_time) * EphemConstant.DEG_TO_RAD;

		switch (eph.targetBody)
		{
		case Phobos:
			mag = 13.25 + 5.0 * Math.log10(rr);
			double M1 = (169.51 - 0.4357640 * (JD - EphemConstant.J2000)) * EphemConstant.DEG_TO_RAD;
			double M2 = (192.93 + 1128.4096700 * (JD - EphemConstant.J2000) + 8.864 * calc_time * calc_time) * EphemConstant.DEG_TO_RAD;
			ephem.northPoleRA = (float) ((317.68 - 0.108 * calc_time + 1.79 * Math.sin(M1)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((52.90 - 0.061 * calc_time - 1.08 * Math.cos(M1)) * EphemConstant.DEG_TO_RAD);
			rot_per_day = 1128.8445850;
			lon0 = 35.06 + 8.864 * calc_time * calc_time - 1.42 * Math.sin(M1) - 0.78 * Math.sin(M2);
			break;
		case Deimos:
			mag = 13.88 + 5.0 * Math.log10(rr);
			double M3 = (53.57 - 0.0181510 * (JD - EphemConstant.J2000)) * EphemConstant.DEG_TO_RAD;
			ephem.northPoleRA = (float) ((316.65 - 0.108 * calc_time + 2.98 * Math.sin(M3)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((53.52 - 0.061 * calc_time - 1.78 * Math.cos(M3)) * EphemConstant.DEG_TO_RAD);
			rot_per_day = 285.1618970;
			lon0 = 79.41 - 0.520 * calc_time * calc_time - 2.58 * Math.sin(M3) - 0.19 * Math.sin(M3);
			break;
		case Io:
			mag = -1.64 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((268.05 - 0.009 * calc_time + 0.094 * Math.sin(J3) + 0.024 * Math.sin(J4)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((64.50 + 0.003 * calc_time + 0.040 * Math.cos(J3) + 0.011 * Math.cos(J4)) * EphemConstant.DEG_TO_RAD);
			lon0 = 200.39 - 0.085 * Math.sin(J3) - 0.022 * Math.sin(J4);
			rot_per_day = 203.4889538;
			break;
		case Europe:
			mag = -1.37 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((268.08 - 0.009 * calc_time + 1.086 * Math.sin(J4) + 0.060 * Math.sin(J5) + 0.015 * Math
					.sin(J6) + 0.009 * Math.sin(J7)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((64.51 + 0.003 * calc_time + 0.468 * Math.cos(J4) + 0.026 * Math.cos(J5) + 0.007 * Math
					.cos(J6) + 0.002 * Math.cos(J7)) * EphemConstant.DEG_TO_RAD);
			lon0 = 36.022 - 0.980 * Math.sin(J4) - 0.054 * Math.sin(J5) - 0.014 * Math.sin(J6) - 0.008 * Math.sin(J7);
			rot_per_day = 101.3747235;
			break;
		case Ganymede:
			mag = -2.04 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((268.20 - 0.009 * calc_time - 0.037 * Math.sin(J4) + 0.431 * Math.sin(J5) + 0.091 * Math
					.sin(J6)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((64.57 + 0.003 * calc_time - 0.016 * Math.cos(J4) + 0.186 * Math.cos(J5) + 0.039 * Math
					.cos(J6)) * EphemConstant.DEG_TO_RAD);
			lon0 = 44.064 + 0.033 * Math.sin(J4) - 0.389 * Math.sin(J5) - 0.082 * Math.sin(J6);
			rot_per_day = 50.3176081;
			break;
		case Callisto:
			mag = -1.0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((268.72 - 0.009 * calc_time - 0.068 * Math.sin(J5) + 0.590 * Math.sin(J6) + 0.010 * Math
					.sin(J8)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((64.83 + 0.003 * calc_time - 0.029 * Math.cos(J5) + 0.254 * Math.cos(J6) - 0.004 * Math
					.cos(J8)) * EphemConstant.DEG_TO_RAD);
			lon0 = 259.51 + 0.061 * Math.sin(J5) - 0.533 * Math.sin(J6) - 0.009 * Math.sin(J8);
			rot_per_day = 21.5710715;
			break;
		case Mimas:
			mag = 3.3 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((40.66 - 0.036 * calc_time + 13.56 * Math.sin(S3)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((83.52 - 0.004 * calc_time - 1.53 * Math.cos(S3)) * EphemConstant.DEG_TO_RAD);
			lon0 = 337.46 - 13.48 * Math.sin(S3) - 44.85 * Math.sin(S5);
			rot_per_day = 381.9945550;
			break;
		case Enceladus:
			mag = 2.1 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((40.66 - 0.036 * calc_time) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((83.52 - 0.004 * calc_time) * EphemConstant.DEG_TO_RAD);
			lon0 = 2.82;
			rot_per_day = 262.7318996;
			break;
		case Tethys:
			mag = 0.6 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((40.66 - 0.036 * calc_time + 9.66 * Math.sin(S4)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((83.52 - 0.004 * calc_time - 1.09 * Math.cos(S4)) * EphemConstant.DEG_TO_RAD);
			lon0 = 10.45 - 9.60 * Math.sin(S4) + 2.23 * Math.sin(S5);
			rot_per_day = 190.6979085;
			break;
		case Dione:
			mag = 0.88 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((40.66 - 0.036 * calc_time) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((83.52 - 0.004 * calc_time) * EphemConstant.DEG_TO_RAD);
			lon0 = 357.00;
			rot_per_day = 131.5349316;
			break;
		case Rhea:
			mag = 0.16 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((40.38 - 0.036 * calc_time + 3.10 * Math.sin(S6)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((83.55 - 0.004 * calc_time - 0.35 * Math.cos(S6)) * EphemConstant.DEG_TO_RAD);
			lon0 = 235.16 - 3.08 * Math.sin(S6);
			rot_per_day = 79.6900478;
			break;
		case Titan:
			mag = -1.29 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((36.41 - 0.036 * calc_time + 2.66 * Math.sin(S7)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((83.94 - 0.004 * calc_time - 0.30 * Math.cos(S7)) * EphemConstant.DEG_TO_RAD);
			lon0 = 189.64 - 2.64 * Math.sin(S7);
			rot_per_day = 22.5769768;
			break;
		case Hyperion: // No data available
			mag = 4.63 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = 0.0f;
			ephem.northPoleDEC = 0.0f;
			lon0 = 0.0;
			rot_per_day = 0.0;
			break;
		case Iapetus:
			mag = 1.5 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((318.16 - 3.949 * calc_time) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((75.03 - 1.143 * calc_time) * EphemConstant.DEG_TO_RAD);
			lon0 = 350.20;
			rot_per_day = 4.5379572;
			break;
		case Miranda:
			mag = 3.6 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((257.43 + 4.41 * Math.sin(U11) - 0.04 * Math.sin(2.0 * U11)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((-15.08 + 4.25 * Math.cos(U11) - 0.02 * Math.cos(2.0 * U11)) * EphemConstant.DEG_TO_RAD);
			lon0 = 30.70 - 1.27 * Math.sin(U12) + 0.15 * Math.sin(2.0 * U12) + 1.15 * Math.sin(U11) - 0.09 * Math
					.sin(2.0 * U11);
			rot_per_day = -254.6906892;
			break;
		case Ariel:
			mag = 1.45 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((257.43 + 0.29 * Math.sin(U13)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((-15.10 + 0.28 * Math.cos(U13)) * EphemConstant.DEG_TO_RAD);
			lon0 = 156.22 + 0.05 * Math.sin(U12) + 0.08 * Math.sin(U13);
			rot_per_day = -142.8356681;
			break;
		case Umbriel:
			mag = 2.1 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((257.43 + 0.21 * Math.sin(U14)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((-15.10 + 0.20 * Math.cos(U14)) * EphemConstant.DEG_TO_RAD);
			lon0 = 108.95 - 0.09 * Math.sin(U12) + 0.086 * Math.sin(U14);
			rot_per_day = -86.8688923;
			break;
		case Titania:
			mag = 1.02 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((257.43 + 0.29 * Math.sin(U15)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((-15.10 + 0.28 * Math.cos(U15)) * EphemConstant.DEG_TO_RAD);
			lon0 = 77.74 + 0.08 * Math.sin(U15);
			rot_per_day = -41.3514316;
			break;
		case Oberon:
			mag = 1.23 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((257.43 + 0.16 * Math.sin(U16)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((-15.10 + 0.16 * Math.cos(U16)) * EphemConstant.DEG_TO_RAD);
			lon0 = 6.77 + 0.04 * Math.sin(U16);
			rot_per_day = -26.7394932;
			break;
		case Triton:
			mag = -1.24 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((299.36 - 32.35 * Math.sin(N7) - 6.28 * Math.sin(2.0 * N7) - 2.08 * Math
					.sin(3.0 * N7) - 0.74 * Math.sin(4.0 * N7) - 0.28 * Math.sin(5.0 * N7) - 0.11 * Math.sin(6.0 * N7) - 0.07 * Math
					.sin(7.0 * N7) - 0.02 * Math.sin(8.0 * N7) - 0.01 * Math.sin(9.0 * N7)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((41.17 + 22.55 * Math.cos(N7) + 2.10 * Math.cos(2.0 * N7) + 0.55 * Math
					.cos(3.0 * N7) + 0.16 * Math.cos(4.0 * N7) + 0.05 * Math.cos(5.0 * N7) + 0.02 * Math.cos(6.0 * N7) + 0.01 * Math
					.cos(7.0 * N7)) * EphemConstant.DEG_TO_RAD);
			lon0 = 296.53 + 22.25 * Math.sin(N7) + 6.73 * Math.sin(2.0 * N7) + 2.05 * Math.sin(3.0 * N7) + 0.74 * Math
					.sin(4.0 * N7) + 0.28 * Math.sin(5.0 * N7) + 0.11 * Math.sin(6.0 * N7) + 0.07 * Math.sin(7.0 * N7) + 0.02 * Math
					.sin(8.0 * N7) + 0.01 * Math.sin(9.0 * N7);
			rot_per_day = -61.2572637;
			break;
		case Charon:
			mag = -0.99 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) (313.02 * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) (9.09 * EphemConstant.DEG_TO_RAD);
			lon0 = 56.77;
			rot_per_day = -56.3623195;
			break;
		case Metis:
			mag = 10.9 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((268.05 - 0.009 * calc_time) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((64.49 + 0.003 * calc_time) * EphemConstant.DEG_TO_RAD);
			lon0 = 346.09;
			rot_per_day = 1221.2547301;
			break;
		case Adrastea:
			mag = 12.5 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((268.05 - 0.009 * calc_time) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((64.49 + 0.003 * calc_time) * EphemConstant.DEG_TO_RAD);
			lon0 = 33.29;
			rot_per_day = 1206.9986602;
			break;
		case Amalthea:
			mag = 7.5 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((268.05 - 0.009 * calc_time - 0.84 * Math.sin(J1) + 0.01 * Math.sin(2.0 * J1)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((64.49 + 0.003 * calc_time - 0.36 * Math.cos(J1)) * EphemConstant.DEG_TO_RAD);
			lon0 = 231.67 + 0.76 * Math.sin(J1) - 0.01 * Math.sin(2.0 * J1);
			rot_per_day = 722.6314560;
			break;
		case Thebe:
			mag = 9.1 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((268.05 - 0.009 * calc_time - 2.11 * Math.sin(J2) + 0.04 * Math.sin(2.0 * J2)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((64.49 + 0.003 * calc_time - 0.91 * Math.cos(J2) + 0.01 * Math.cos(2.0 * J2)) * EphemConstant.DEG_TO_RAD);
			lon0 = 8.56 + 1.91 * Math.sin(J2) - 0.04 * Math.sin(2.0 * J2);
			rot_per_day = 533.7004100;
			break;
		case Pan:
			mag = 9.4 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((40.6 - 0.036 * calc_time) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((83.5 - 0.004 * calc_time) * EphemConstant.DEG_TO_RAD);
			lon0 = 48.8;
			rot_per_day = 626.0440000;
			break;
		case Atlas:
			mag = 8.4 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((40.58 - 0.036 * calc_time) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((83.53 - 0.004 * calc_time) * EphemConstant.DEG_TO_RAD);
			lon0 = 137.88;
			rot_per_day = 598.3060000;
			break;
		case Prometheus:
			mag = 6.2 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((40.58 - 0.036 * calc_time) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((83.53 - 0.004 * calc_time) * EphemConstant.DEG_TO_RAD);
			lon0 = 296.14;
			rot_per_day = 587.2890000;
			break;
		case Pandora:
			mag = 6.9 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((40.58 - 0.036 * calc_time) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((83.53 - 0.004 * calc_time) * EphemConstant.DEG_TO_RAD);
			lon0 = 162.92;
			rot_per_day = 572.7891000;
			break;
		case Epimetheus:
			mag = 6.1 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((40.58 - 0.036 * calc_time - 3.153 * Math.sin(S1) + 0.086 * Math.sin(2.0 * S1)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((83.52 - 0.004 * calc_time - 0.356 * Math.cos(S1) + 0.005 * Math.cos(2.0 * S1)) * EphemConstant.DEG_TO_RAD);
			lon0 = 293.87 + 3.133 * Math.sin(S1) - 0.086 * Math.sin(2.0 * S1);
			rot_per_day = 518.4907239;
			break;
		case Janus:
			mag = 4.9 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((40.58 - 0.036 * calc_time - 1.623 * Math.sin(S2) + 0.023 * Math.sin(2.0 * S2)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((83.52 - 0.004 * calc_time - 0.183 * Math.cos(S2) + 0.001 * Math.cos(2.0 * S2)) * EphemConstant.DEG_TO_RAD);
			lon0 = 58.83 + 1.613 * Math.sin(S2) - 0.023 * Math.sin(2.0 * S2);
			rot_per_day = 518.2359876;
			break;
		case Telesto:
			mag = 8.9 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((50.51 - 0.036 * calc_time) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((84.06 - 0.004 * calc_time) * EphemConstant.DEG_TO_RAD);
			lon0 = 56.88;
			rot_per_day = 190.6979332;
			break;
		case Calypso:
			mag = 9.1 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((36.41 - 0.036 * calc_time) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((85.04 - 0.004 * calc_time) * EphemConstant.DEG_TO_RAD);
			lon0 = 153.51;
			rot_per_day = 190.6742373;
			break;
		case Helene:
			mag = 8.8 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((40.85 - 0.036 * calc_time) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((83.34 - 0.004 * calc_time) * EphemConstant.DEG_TO_RAD);
			lon0 = 245.12;
			rot_per_day = 131.6174056;
			break;
		case Phoebe:
			mag = 6.9 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) (355.00 * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) (68.70 * EphemConstant.DEG_TO_RAD);
			lon0 = 304.70;
			rot_per_day = 930.8338720;
			break;
		case Cordelia:
			mag = 0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((257.31 - 0.15 * Math.sin(U1)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((-15.18 + 0.14 * Math.cos(U1)) * EphemConstant.DEG_TO_RAD);
			lon0 = 127.69 - 0.04 * Math.sin(U1);
			rot_per_day = -1074.520573;
			break;
		case Ophelia:
			mag = 0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((257.31 - 0.09 * Math.sin(U2)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((-15.18 + 0.09 * Math.cos(U2)) * EphemConstant.DEG_TO_RAD);
			lon0 = 130.35 - 0.03 * Math.sin(U2);
			rot_per_day = -956.406815;
			break;
		case Bianca:
			mag = 0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((257.31 - 0.16 * Math.sin(U3)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((-15.18 + 0.16 * Math.cos(U3)) * EphemConstant.DEG_TO_RAD);
			lon0 = 105.46 - 0.04 * Math.sin(U3);
			rot_per_day = -828.3914760;
			break;
		case Cressida:
			mag = 0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((257.31 - 0.04 * Math.sin(U4)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((-15.18 + 0.04 * Math.cos(U4)) * EphemConstant.DEG_TO_RAD);
			lon0 = 59.16 - 0.01 * Math.sin(U4);
			rot_per_day = -776.581632;
			break;
		case Desdemona:
			mag = 0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((257.31 - 0.17 * Math.sin(U5)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((-15.18 + 0.16 * Math.cos(U5)) * EphemConstant.DEG_TO_RAD);
			lon0 = 95.08 - 0.04 * Math.sin(U5);
			rot_per_day = -760.0531690;
			break;
		case Juliet:
			mag = 0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((257.31 - 0.06 * Math.sin(U6)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((-15.18 + 0.06 * Math.cos(U6)) * EphemConstant.DEG_TO_RAD);
			lon0 = 302.56 - 0.02 * Math.sin(U6);
			rot_per_day = -730.1253660;
			break;
		case Portia:
			mag = 0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((257.31 - 0.09 * Math.sin(U7)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((-15.18 + 0.09 * Math.cos(U7)) * EphemConstant.DEG_TO_RAD);
			lon0 = 25.03 - 0.02 * Math.sin(U7);
			rot_per_day = -701.4865870;
			break;
		case Rosalind:
			mag = 0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((257.31 - 0.29 * Math.sin(U8)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((-15.18 + 0.28 * Math.cos(U8)) * EphemConstant.DEG_TO_RAD);
			lon0 = 314.90 - 0.08 * Math.sin(U8);
			rot_per_day = -644.6311260;
			break;
		case Belinda:
			mag = 0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((257.31 - 0.03 * Math.sin(U9)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((-15.18 + 0.03 * Math.cos(U9)) * EphemConstant.DEG_TO_RAD);
			lon0 = 297.46 - 0.01 * Math.sin(U9);
			rot_per_day = -577.3628170;
			break;
		case Puck:
			mag = 0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((257.31 - 0.33 * Math.sin(U10)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((-15.18 + 0.31 * Math.cos(U10)) * EphemConstant.DEG_TO_RAD);
			lon0 = 91.24 - 0.09 * Math.sin(U10);
			rot_per_day = -472.5450690;
			break;
		case Naiad:
			mag = 0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((299.36 + 0.70 * Math.sin(N0) - 6.49 * Math.sin(N1) + 0.25 * Math.sin(2.0 * N1)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((43.36 - 0.51 * Math.cos(N0) - 4.75 * Math.cos(N1) + 0.09 * Math.cos(2.0 * N1)) * EphemConstant.DEG_TO_RAD);
			lon0 = 254.06 - 0.48 * Math.sin(N0) + 4.40 * Math.sin(N1) - 0.27 * Math.sin(2.0 * N1);
			rot_per_day = 1222.8441209;
			break;
		case Thalassa:
			mag = 0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((299.36 + 0.70 * Math.sin(N0) - 0.28 * Math.sin(N2)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((43.45 - 0.51 * Math.cos(N0) - 0.21 * Math.cos(N2)) * EphemConstant.DEG_TO_RAD);
			lon0 = 102.06 - 0.48 * Math.sin(N0) + 0.19 * Math.sin(N2);
			rot_per_day = 1155.7555612;
			break;
		case Despina:
			mag = 0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((299.36 + 0.70 * Math.sin(N0) - 0.09 * Math.sin(N3)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((43.45 - 0.51 * Math.cos(N0) - 0.07 * Math.cos(N3)) * EphemConstant.DEG_TO_RAD);
			lon0 = 306.51 - 0.49 * Math.sin(N0) + 0.06 * Math.sin(N3);
			rot_per_day = 1075.7341562;
			break;
		case Galatea:
			mag = 0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((299.36 + 0.70 * Math.sin(N0) - 0.07 * Math.sin(N4)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((43.43 - 0.51 * Math.cos(N0) - 0.05 * Math.cos(N4)) * EphemConstant.DEG_TO_RAD);
			lon0 = 258.09 - 0.48 * Math.sin(N0) + 0.05 * Math.sin(N4);
			rot_per_day = 839.6597686;
			break;
		case Larissa:
			mag = 0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((299.36 + 0.70 * Math.sin(N0) - 0.27 * Math.sin(N5)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((43.41 - 0.51 * Math.cos(N0) - 0.20 * Math.cos(N5)) * EphemConstant.DEG_TO_RAD);
			lon0 = 179.41 - 0.48 * Math.sin(N0) + 0.19 * Math.sin(N5);
			rot_per_day = 649.0534470;
			break;
		case Proteus:
			mag = 0 + 5.0 * Math.log10(rr);
			ephem.northPoleRA = (float) ((299.27 + 0.70 * Math.sin(N0) - 0.05 * Math.sin(N6)) * EphemConstant.DEG_TO_RAD);
			ephem.northPoleDEC = (float) ((42.91 - 0.51 * Math.cos(N0) - 0.04 * Math.cos(N6)) * EphemConstant.DEG_TO_RAD);
			lon0 = 93.38 - 0.48 * Math.sin(N0) + 0.04 * Math.sin(N6);
			rot_per_day = 320.7654228;
			break;

		default:
			// Satellite cannot be found. Set object to not_a_planet to avoid
			// calculating
			// axis orientation, because default case means we have no
			// orientation information
			// about this object.
			eph.targetBody = Target.NOT_A_PLANET;
		}

		// Apparent visual magnitude
		ephem.magnitude = (float) mag;

		// Angular radius in radians
		ephem.angularRadius = (float) Math.atan(Target.getEquatorialRadius(eph.targetBody) / (ephem.distance * EphemConstant.AU));

		// Now compute disk orientation as seen by the observer
		ephem.positionAngleOfAxis = 0.0f;
		ephem.positionAngleOfPole = 0.0f;
		ephem.brightLimbAngle = 0.0f;
		ephem.subsolarLatitude = 0.0f;
		ephem.subsolarLongitude = 0.0f;
		ephem.longitudeOfCentralMeridian = 0.0f;
		if (eph.targetBody != Target.NOT_A_PLANET)
			ephem = calcAxis(JD, ephem_sun, ephem, lon0, rot_per_day, shape, eph);

		return ephem;
	}
	

	/**
	 * Calculate orientation of a satellite providing the position and the
	 * direction of the north pole of rotation. The IAU 2000 models are used,
	 * see <I> REPORT OF THE IAU/IAG WORKING GROUP ON CARTOGRAPHIC COORDINATES
	 * AND ROTATIONAL ELEMENTS OF THE PLANETS AND SATELLITES: 2000</I>, P. K.
	 * Seidelmann et al, in Celestial Mechanics and Dynamical Astronomy, 2002.
	 * <P>
	 * 
	 * @param JD Julian day in TDB.
	 * @param ephem_sun Ephem object with all data for the sun.
	 * @param ephem_obj Moon ephem object with all data completed except those
	 *        fields related to the disk orientation.
	 * @param lon0 Initial longitude of satellite at JD = 2451545.0.
	 * @param rot_per_day Rotation speed in degrees/day.
	 * @param shape Shape factor for the satellite = (equatorial radius - polar
	 *        radius) / mean radius. 0.0 means a perfect spherical satellite.
	 *        For the Earth is 1.0/Earth_flatenning.
	 * @param eph Ephemeris object defining the ephemeris properties.
	 * @return Moon ephem object with all the fields.
	 */
	public static MoonEphemElement calcAxis(double JD, EphemElement ephem_sun, MoonEphemElement ephem_obj, double lon0,
			double rot_per_day, double shape, EphemerisElement eph)
	 {
		EphemElement ephem = EphemElement.parseMoonEphemElement(ephem_obj, JD);
		EphemerisElement new_eph = (EphemerisElement) eph.clone();
		new_eph.targetBody = Target.NOT_A_PLANET;

		ephem = PhysicalParameters.calcAxis(JD, ephem_sun, ephem, lon0, rot_per_day, shape, new_eph);

		MoonEphemElement moon_ephem = (MoonEphemElement) ephem_obj.clone();

		moon_ephem.positionAngleOfAxis = ephem.positionAngleOfAxis;
		moon_ephem.positionAngleOfPole = ephem.positionAngleOfPole;
		moon_ephem.brightLimbAngle = ephem.brightLimbAngle;
		moon_ephem.subsolarLatitude = ephem.subsolarLatitude;
		moon_ephem.subsolarLongitude = ephem.subsolarLongitude;
		moon_ephem.longitudeOfCentralMeridian = ephem.longitudeOfCentralMeridian;
		moon_ephem.northPoleDEC = ephem.northPoleDEC;
		moon_ephem.northPoleRA = ephem.northPoleRA;

		return moon_ephem;
	}
}
