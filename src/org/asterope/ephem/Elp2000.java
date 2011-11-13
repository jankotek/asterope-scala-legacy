/*
 * This file is part of JPARSEC library.
 * 
 * (C) Copyright 2006-2011 by T. Alonso Albi - OAN (Spain).
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


import static org.asterope.ephem.Elp2000Data.*;
/**
 * This class implements the Lunar Solution ELP2000 from the IMCCE. The whole
 * theory is applied.
 * <P>
 * Reference frame is mean dynamical ecliptic and inertial equinox of J2000
 * epoch.
 * <P>
 * References:
 * <P>
 * 1. <I>ELP 2000-85: a semi-analytical lunar ephemeris adequate for historical
 * times</I>, Chapront-Touze M., Chapront J., Astron. & Astrophys. 190, 342
 * (1988).
 * <P>
 * 2. <I>The Lunar Ephemeris ELP 2000</I>, Chapront-Touze M., Chapront J.,
 * Astron. & Astrophys. 124, 50 (1983).
 * 
 * @see org.asterope.ephem.Ephem
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class Elp2000
{

    /**
	 * Value of the Moon secular acceleration ("/cy^2) for DE200.
	 */
	private static final double MOON_SECULAR_ACCELERATION_DE200 = -23.8946;

    private static final double cpi = Math.PI;
    private static final double cpi2 = cpi *2.0;
    private static final double pis2 = cpi /2.0;
    private static final double rad = 648000.0 / cpi;
    private static final double deg = cpi / 180.0;
    private static final double c1 = 60.0;
    private static final double c2 = 3600.0;
    private static final double ath = 384747.9806743165;
    private static final double a0 = 384747.9806448954;
    private static final double am = 0.074801329518;
    private static final double alfa = 0.002571881335;
    private static final double dtasm = 2.0 * alfa / (3.0 * am);


    // Precession constant
	private static final double preces = 5029.0966 / rad;

    // Precession matrix
	private static final double 	p1 = 0.10180391e-4;
	private static final double 	p2 = 0.47020439e-6;
	private static final double 	p3 = -0.5417367e-9;
	private static final double 	p4 = -0.2507948e-11;
	private static final double 	p5 = 0.463486e-14;
	private static final double 	q1 = -0.113469002e-3;
	private static final double 	q2 = 0.12372674e-6;
	private static final double 	q3 = 0.1265417e-8;
	private static final double 	q4 = -0.1371808e-11;
	private static final double 	q5 = -0.320334e-14;

    // Lunar arguments
    private static final double Zw[][] = new double[6][6];
    static{
        Zw[1][1] = (218 + 18 / c1 + 59.95571 / c2) * deg;
        Zw[2][1] = (83 + 21 / c1 + 11.67475 / c2) * deg;
        Zw[3][1] = (125 + 2 / c1 + 40.39816 / c2) * deg;
        Zw[1][2] = 1732559343.73604 / rad;
        Zw[2][2] = 14643420.2632 / rad;
        Zw[3][2] = -6967919.3622 / rad;
        Zw[1][3] = -5.8883 / rad;
        Zw[2][3] = -38.2776 / rad;
        Zw[3][3] = 6.3622 / rad;
        Zw[1][4] = 0.6604e-2 / rad;
        Zw[2][4] = -0.45047e-1 / rad;
        Zw[3][4] = 0.7625e-2 / rad;
        Zw[1][5] = -0.3169e-4 / rad;
        Zw[2][5] = 0.21301e-3 / rad;
        Zw[3][5] = -0.3586e-4 / rad;
    }

    private static final double eart[] = new double[6];
    static{
        eart[1] = (100 + 27 / c1 + 59.22059 / c2) * deg;
        eart[2] = 129597742.2758 / rad;
        eart[3] = -0.0202 / rad;
        eart[4] = 0.9e-5 / rad;
        eart[5] = 0.15e-6 / rad;
    }

    private static final double peri[] = new double[6];
    static{
        peri[1] = (102 + 56 / c1 + 14.42753 / c2) * deg;
        peri[2] = 1161.2283 / rad;
        peri[3] = 0.5327 / rad;
        peri[4] = -0.138e-3 / rad;
        peri[5] = 0.0;
    }

		// Corrections of the constants (fit to DE200/LE200)
	private static final double delnu = +0.55604 / rad / Zw[1][2];
	private static final double dele = +0.01789 / rad;
	private static final double delg = -0.08066 / rad;
	private static final double delnp = -0.06424 / rad / Zw[1][2];
	private static final double delep = -0.12879 / rad;


    private static final double ZZp[][] = new double[9][3];
    static{
		// Planetary arguments
		ZZp[1][1] = (252 + 15 / c1 + 3.25986 / c2) * deg;
		ZZp[2][1] = (181 + 58 / c1 + 47.28305 / c2) * deg;
		ZZp[3][1] = eart[1];
		ZZp[4][1] = (355 + 25 / c1 + 59.78866 / c2) * deg;
		ZZp[5][1] = (34 + 21 / c1 + 5.34212 / c2) * deg;
		ZZp[6][1] = (50 + 4 / c1 + 38.89694 / c2) * deg;
		ZZp[7][1] = (314 + 3 / c1 + 18.01841 / c2) * deg;
		ZZp[8][1] = (304 + 20 / c1 + 55.19575 / c2) * deg;
		ZZp[1][2] = 538101628.68898 / rad;
		ZZp[2][2] = 210664136.43355 / rad;
		ZZp[3][2] = eart[2];
		ZZp[4][2] = 68905077.59284 / rad;
		ZZp[5][2] = 10925660.42861 / rad;
		ZZp[6][2] = 4399609.65932 / rad;
		ZZp[7][2] = 1542481.19393 / rad;
		ZZp[8][2] = 786550.32074 / rad;
    }

    // Delaunay's arguments
    private static final double del[][] = new double[5][6];
    static{
        for (int i = 1; i <= 5; i++)
		{
			del[1][i] = Zw[1][i] - eart[i];
			del[4][i] = Zw[1][i] - Zw[3][i];
			del[3][i] = Zw[1][i] - Zw[2][i];
			del[2][i] = eart[i] - peri[i];
		}
		del[1][1] = del[1][1] + cpi;
    }

    private static final double zeta[] = new double[3];
    static{
        zeta[1] = Zw[1][1];
		zeta[2] = Zw[1][2] + preces;
    }

	/**
	 * Computation of geocentric lunar coordinates from ELP 2000-82 and
	 * ELP2000-85 theories (M. Chapront-Touze and J. Chapront). Constants fitted
	 * to JPL's ephemerides DE200/LE200.
	 * <P>
	 * Reference frame in mean dynamical ecliptic and inertial equinox of J2000
	 * epoch.
	 * <P>
	 * Files, series, constants and coordinate systems are described in the
	 * notice LUNAR SOLUTION ELP 2000-82B, available from the IMCCE.
	 * 
	 * @param JD Julian Day in TDB.
	 * @param prec Truncation level in arcseconds. Pass 0 to use the complete
	 *        theory (slower).
	 * @return An array with the geocentric rectangular positions. Units are AU.
	 */
	public static double[] calc(double JD, // Julian Day
			double prec) // Truncation level
	{



		double ZZt[] = new double[6];
		double pre[] = new double[4];
		double ZZr[] = new double[4];

		ZZt[1] = 1.0;
		ZZt[2] = (JD - 2451545.0) / 36525.0;
		ZZt[3] = ZZt[2] * ZZt[2];
		ZZt[4] = ZZt[3] * ZZt[2];
		ZZt[5] = ZZt[4] * ZZt[2];

		pre[1] = prec - 1.0e-12;
		pre[2] = prec - 1.0e-12;
		pre[3] = prec * ath / rad;

		// Calculate element
		for (int ific = 1; ific <= 36; ific++)
		{

			int iv = (int) EphemUtils.module((double) (ific - 1.0), 3.0) + 1;
			switch (ific)
			{
			case 1:
				ZZr[iv] += calcELEM1_(LonSine0.ILU, LonSine0.COEF, ific, iv,  ZZt, pre);
				ZZr[iv] += calcELEM1_(LonSine1.ILU, LonSine1.COEF, ific, iv,  ZZt, pre);
				ZZr[iv] += calcELEM1_(LonSine2.ILU, LonSine2.COEF, ific, iv,  ZZt, pre);
				break;
			case 2:
				ZZr[iv] += calcELEM1_(LatSine0.ILU, LatSine0.COEF, ific, iv,  ZZt, pre);
				ZZr[iv] += calcELEM1_(LatSine1.ILU, LatSine1.COEF, ific, iv,  ZZt, pre);
				ZZr[iv] += calcELEM1_(LatSine2.ILU, LatSine2.COEF, ific, iv,  ZZt, pre);
				break;
			case 3:
                ZZr[iv] += calcELEM1_(RadCose0.ILU, RadCose0.COEF, ific, iv,  ZZt, pre);
                ZZr[iv] += calcELEM1_(RadCose1.ILU, RadCose1.COEF, ific, iv,  ZZt, pre);
				break;
			case 4:
				ZZr[iv] += calcELEM2_(lon_earth_perturb.ILU,lon_earth_perturb.COEF, ific, iv, ZZt, pre);
				break;
			case 5:
				ZZr[iv] += calcELEM2_(lat_earth_perturb.ILU, lat_earth_perturb.COEF, ific, iv, ZZt, pre);
				break;
			case 6:
				ZZr[iv] += calcELEM2_(rad_earth_perturb.ILU, rad_earth_perturb.COEF, ific, iv, ZZt, pre);
				break;
			case 7:
				ZZr[iv] += calcELEM2_(earth_perturb_t_Lon.ILU, earth_perturb_t_Lon.COEF,ific, iv, ZZt, pre);
				break;
			case 8:
				ZZr[iv] += calcELEM2_(earth_perturb_t_Lat.ILU, earth_perturb_t_Lat.COEF, ific, iv, ZZt, pre);
				break;
			case 9:
				ZZr[iv] += calcELEM2_(earth_perturb_t_Rad.ILU, earth_perturb_t_Rad.COEF,ific, iv,  ZZt, pre);
				break;
			case 10:
				ZZr[iv] += calcELEM3_(plan_perturb10_00_Lon.ILU,plan_perturb10_00_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb10_01_Lon.ILU,plan_perturb10_01_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb10_02_Lon.ILU,plan_perturb10_02_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb10_03_Lon.ILU,plan_perturb10_03_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb10_04_Lon.ILU,plan_perturb10_04_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb10_05_Lon.ILU,plan_perturb10_05_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb10_06_Lon.ILU,plan_perturb10_06_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb10_07_Lon.ILU,plan_perturb10_07_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_08_Lon.ILU,plan_perturb10_08_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb10_09_Lon.ILU,plan_perturb10_09_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_10_Lon.ILU,plan_perturb10_10_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_11_Lon.ILU,plan_perturb10_11_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_12_Lon.ILU,plan_perturb10_12_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_13_Lon.ILU,plan_perturb10_13_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_14_Lon.ILU,plan_perturb10_14_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_15_Lon.ILU,plan_perturb10_15_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_16_Lon.ILU,plan_perturb10_16_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_17_Lon.ILU,plan_perturb10_17_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_18_Lon.ILU,plan_perturb10_18_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_19_Lon.ILU,plan_perturb10_19_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_20_Lon.ILU,plan_perturb10_20_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_21_Lon.ILU,plan_perturb10_21_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_22_Lon.ILU,plan_perturb10_22_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_23_Lon.ILU,plan_perturb10_23_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_24_Lon.ILU,plan_perturb10_24_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_25_Lon.ILU,plan_perturb10_25_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_26_Lon.ILU,plan_perturb10_26_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_27_Lon.ILU,plan_perturb10_27_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_28_Lon.ILU,plan_perturb10_28_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_29_Lon.ILU,plan_perturb10_29_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_30_Lon.ILU,plan_perturb10_30_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_31_Lon.ILU,plan_perturb10_31_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_32_Lon.ILU,plan_perturb10_32_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_33_Lon.ILU,plan_perturb10_33_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_34_Lon.ILU,plan_perturb10_34_Lon.COEF, ific, iv, ZZt, pre);
                ZZr[iv] += calcELEM3_(plan_perturb10_35_Lon.ILU,plan_perturb10_35_Lon.COEF, ific, iv, ZZt, pre);
				break;
			case 11:
				ZZr[iv] += calcELEM3_(plan_perturb11_00_Lat.ILU,plan_perturb11_00_Lat.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb11_01_Lat.ILU,plan_perturb11_01_Lat.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb11_02_Lat.ILU,plan_perturb11_02_Lat.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb11_03_Lat.ILU,plan_perturb11_03_Lat.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb11_04_Lat.ILU,plan_perturb11_04_Lat.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb11_05_Lat.ILU,plan_perturb11_05_Lat.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb11_06_Lat.ILU,plan_perturb11_06_Lat.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb11_07_Lat.ILU,plan_perturb11_07_Lat.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb11_08_Lat.ILU,plan_perturb11_08_Lat.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb11_09_Lat.ILU,plan_perturb11_09_Lat.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb11_10_Lat.ILU,plan_perturb11_10_Lat.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb11_11_Lat.ILU,plan_perturb11_11_Lat.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb11_12_Lat.ILU,plan_perturb11_12_Lat.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb11_13_Lat.ILU,plan_perturb11_13_Lat.COEF, ific, iv, ZZt, pre);
				break;
			case 12:
				ZZr[iv] += calcELEM3_(plan_perturb12_00_Rad.ILU, plan_perturb12_00_Rad.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb12_01_Rad.ILU, plan_perturb12_01_Rad.COEF,  ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb12_02_Rad.ILU, plan_perturb12_02_Rad.COEF,  ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb12_03_Rad.ILU, plan_perturb12_03_Rad.COEF,  ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb12_04_Rad.ILU, plan_perturb12_04_Rad.COEF,  ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb12_05_Rad.ILU, plan_perturb12_05_Rad.COEF,  ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb12_06_Rad.ILU, plan_perturb12_06_Rad.COEF,  ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb12_07_Rad.ILU, plan_perturb12_07_Rad.COEF,  ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb12_08_Rad.ILU, plan_perturb12_08_Rad.COEF,  ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb12_09_Rad.ILU, plan_perturb12_09_Rad.COEF,  ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb12_10_Rad.ILU, plan_perturb12_10_Rad.COEF,  ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb12_11_Rad.ILU, plan_perturb12_11_Rad.COEF,  ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb12_12_Rad.ILU, plan_perturb12_12_Rad.COEF,  ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb12_13_Rad.ILU, plan_perturb12_13_Rad.COEF,  ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb12_14_Rad.ILU, plan_perturb12_14_Rad.COEF,  ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb12_15_Rad.ILU, plan_perturb12_15_Rad.COEF,  ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb12_16_Rad.ILU, plan_perturb12_16_Rad.COEF,  ific, iv, ZZt, pre);
				break;
			case 13:
				ZZr[iv] += calcELEM3_(plan_perturb13_00_Lon.ILU, plan_perturb13_00_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb13_01_Lon.ILU, plan_perturb13_01_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb13_02_Lon.ILU, plan_perturb13_02_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb13_03_Lon.ILU, plan_perturb13_03_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb13_04_Lon.ILU, plan_perturb13_04_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb13_05_Lon.ILU, plan_perturb13_05_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb13_06_Lon.ILU, plan_perturb13_06_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb13_07_Lon.ILU, plan_perturb13_07_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb13_08_Lon.ILU, plan_perturb13_08_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb13_09_Lon.ILU, plan_perturb13_09_Lon.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb13_10_Lon.ILU, plan_perturb13_10_Lon.COEF, ific, iv, ZZt, pre);
				break;
			case 14:
				ZZr[iv] += calcELEM3_(plan_perturb14_00_Lat.ILU, plan_perturb14_00_Lat.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb14_01_Lat.ILU, plan_perturb14_01_Lat.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb14_02_Lat.ILU, plan_perturb14_02_Lat.COEF,ific, iv, ZZt, pre);
				break;
			case 15:
				ZZr[iv] += calcELEM3_(plan_perturb15_00_Rad.ILU, plan_perturb15_00_Rad.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb15_01_Rad.ILU, plan_perturb15_01_Rad.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb15_02_Rad.ILU, plan_perturb15_02_Rad.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb15_03_Rad.ILU, plan_perturb15_03_Rad.COEF, ific, iv, ZZt, pre);
				ZZr[iv] += calcELEM3_(plan_perturb15_04_Rad.ILU, plan_perturb15_04_Rad.COEF, ific, iv, ZZt, pre);
				break;
			case 16:
				ZZr[iv] += calcELEM3_(plan_perturb2_Lon.ILU,plan_perturb2_Lon.COEF, ific, iv, ZZt, pre);
				break;
			case 17:
				ZZr[iv] += calcELEM3_(plan_perturb2_Lat.ILU,plan_perturb2_Lat.COEF, ific, iv, ZZt, pre);
				break;
			case 18:
				ZZr[iv] += calcELEM3_(plan_perturb2_Rad.ILU,plan_perturb2_Rad.COEF, ific, iv, ZZt, pre);
				break;
			case 19:
				ZZr[iv] += calcELEM3_(plan_perturb2_Lon_t.ILU,plan_perturb2_Lon_t.COEF, ific, iv, ZZt, pre);
				break;
			case 20:
				ZZr[iv] += calcELEM3_(plan_perturb2_Lat_t.ILU,plan_perturb2_Lat_t.COEF, ific, iv, ZZt, pre);
				break;
			case 21:
				ZZr[iv] += calcELEM3_(plan_perturb2_Rad_t.ILU, plan_perturb2_Rad_t.COEF, ific, iv, ZZt, pre);
				break;
			case 22:
				ZZr[iv] += calcELEM2_(tidal_Lon.ILU,tidal_Lon.COEF, ific, iv, ZZt, pre);
				break;
			case 23:
				ZZr[iv] += calcELEM2_(tidal_Lat.ILU,tidal_Lat.COEF, ific, iv, ZZt, pre);
				break;
			case 24:
				ZZr[iv] += calcELEM2_(tidal_Rad.ILU,tidal_Rad.COEF, ific, iv, ZZt, pre);
				break;
			case 25:
				ZZr[iv] += calcELEM2_(tidal_Lon_t.ILU, tidal_Lon_t.COEF, ific, iv, ZZt, pre);
				break;
			case 26:
				ZZr[iv] += calcELEM2_(tidal_Lat_t.ILU,tidal_Lat_t.COEF, ific, iv, ZZt, pre);
				break;
			case 27:
				ZZr[iv] += calcELEM2_(tidal_Rad_t.ILU, tidal_Rad_t.COEF, ific, iv, ZZt, pre);
				break;
			case 28:
				ZZr[iv] += calcELEM2_(moon_Lon.ILU, moon_Lon.COEF, ific, iv, ZZt, pre);
				break;
			case 29:
				ZZr[iv] += calcELEM2_(moon_Lat.ILU, moon_Lat.COEF, ific, iv, ZZt, pre);
				break;
			case 30:
				ZZr[iv] += calcELEM2_(moon_Rad.ILU, moon_Rad.COEF, ific, iv, ZZt, pre);
				break;
			case 31:
				ZZr[iv] += calcELEM2_(rel_Lon.ILU, rel_Lon.COEF, ific, iv, ZZt, pre);
				break;
			case 32:
				ZZr[iv] += calcELEM2_(rel_Lat.ILU,rel_Lat.COEF, ific, iv, ZZt, pre);
				break;
			case 33:
				ZZr[iv] += calcELEM2_(rel_Rad.ILU, rel_Rad.COEF, ific, iv, ZZt, pre);
				break;
			case 34:
				ZZr[iv] += calcELEM2_(plan_Lon.ILU,plan_Lon.COEF, ific, iv, ZZt, pre);
				break;
			case 35:
				ZZr[iv] += calcELEM2_(plan_Lat.ILU, plan_Lat.COEF, ific, iv, ZZt, pre);
				break;
			case 36:
				ZZr[iv] += calcELEM2_(plan_Rad.ILU,plan_Rad.COEF, ific, iv, ZZt, pre);
				break;
			}

		}

		ZZr[1] = ZZr[1] / rad + Zw[1][1] + Zw[1][2] * ZZt[2] + Zw[1][3] * ZZt[3] + Zw[1][4] * ZZt[4] + Zw[1][5] * ZZt[5];
		ZZr[2] = ZZr[2] / rad;
		ZZr[3] = ZZr[3] * a0 / ath;

		double x1 = ZZr[3] * Math.cos(ZZr[2]);
		double x2 = x1 * Math.sin(ZZr[1]);
		x1 = x1 * Math.cos(ZZr[1]);
		double x3 = ZZr[3] * Math.sin(ZZr[2]);

		double pw = (p1 + p2 * ZZt[2] + p3 * ZZt[3] + p4 * ZZt[4] + p5 * ZZt[5]) * ZZt[2];
		double qw = (q1 + q2 * ZZt[2] + q3 * ZZt[3] + q4 * ZZt[4] + q5 * ZZt[5]) * ZZt[2];
		double rra = 2.0 * Math.sqrt(1.0 - pw * pw - qw * qw);
		double pwqw = 2.0 * pw * qw;
		double pw2 = 1.0 - 2.0 * pw * pw;
		double qw2 = 1.0 - 2.0 * qw * qw;
		pw = pw * rra;
		qw = qw * rra;

		ZZr[1] = pw2 * x1 + pwqw * x2 + pw * x3;
		ZZr[2] = pwqw * x1 + qw2 * x2 - qw * x3;
		ZZr[3] = -pw * x1 + qw * x2 + (pw2 + qw2 - 1.0) * x3;

		return new double[]
		{ ZZr[1] / EphemConstant.AU, ZZr[2] / EphemConstant.AU, ZZr[3] / EphemConstant.AU };
	}

	// Main problem, files 1-3
	private static double calcELEM1_(byte[] ILU, double[] COEF, int ific, int iv, double[] ZZt, double[] pre)
	{
		double tgv, Zx = 0.0, Zy = 0.0, Zr = 0.0;

        if(ILU.length/4 != COEF.length/7) throw new IllegalArgumentException();
		for (int i = 0; i < ILU.length/4; i++)
		{
            final int iluI = i*4;
            final int coefI = i*7;
			if (Math.abs(COEF[0+coefI]) > pre[iv])
			{
				tgv = COEF[1+coefI] + dtasm * COEF[5+coefI];
				Zx = COEF[0+coefI] + tgv * (delnp - am * delnu) + COEF[2+coefI] * delg + COEF[3+coefI] * dele +COEF[4+coefI] * delep;
				Zy = 0.0;

				if (ific == 3)
					Zx -= 2.0 * COEF[0+coefI] * delnu / 3.0;

				for (int k = 1; k <= 5; k++)
				{
					for (int j = 1; j <= 4; j++)
					{
						Zy = Zy + ILU[j - 1+iluI] * del[j][k] * ZZt[k];
					}
				}

				if (iv == 3)
					Zy += pis2;

				Zy = EphemUtils.module(Zy, cpi2);

				Zr += Zx * Math.sin(Zy);
			}
		}

		return Zr;
	}


	// Figures - Tides - Relativity - Solar eccentricity, files 4-9, 22-36
	private static double calcELEM2_(byte[] ILU, double[] COEF, int ific, int iv, double[] ZZt, double[] pre)
	{
        if(ILU.length/5 != COEF.length/3) throw new IllegalArgumentException();

		double Zx = 0.0, Zy = 0.0, Zr = 0.0;


		for (int i = 0; i < ILU.length/5; i++)
		{
            final int iluI = i*5;
            final int coefI = i*3;
			if (COEF[1+coefI] > pre[iv])
			{
				Zx = COEF[1+coefI];
				if (ific >= 7 && ific <= 9)
					Zx = Zx * ZZt[2];
				if (ific >= 25 && ific <= 27)
					Zx = Zx * ZZt[2];
				if (ific >= 34 && ific <= 36)
					Zx = Zx * ZZt[3];

				Zy = COEF[0+coefI] * deg;
				for (int k = 1; k <= 2; k++)
				{
					Zy = Zy + ILU[0+iluI] * zeta[k] * ZZt[k];
					for (int j = 1; j <= 4; j++)
					{
						Zy = Zy + ILU[j+iluI] * del[j][k] * ZZt[k];
					}
				}
				Zy = EphemUtils.module(Zy, cpi2);
				Zr += Zx * Math.sin(Zy);
			}
		}

		return Zr;
	}



	// Planetary perturbations, files 10-21
	private static double calcELEM3_(byte[] ILU, double[] COEF, int ific, int iv,
			double[] ZZt, double[] pre)
	{

        if(ILU.length/11 != COEF.length/3) throw new IllegalArgumentException();

		double Zx = 0.0, Zy = 0.0, Zr = 0.0;


		for (int i = 0; i < ILU.length/11; i++)
		{
            final int iluI = i*11;
            final int coefI = i*3;

			if (COEF[1+coefI] > pre[iv])
			{
				Zx = COEF[1+coefI];
				if ((ific >= 13 && ific <= 15) || ific >= 19 && ific <= 21)
					Zx = Zx * ZZt[2];
				Zy = COEF[0+coefI] * deg;

				for (int k = 1; k <= 2; k++)
				{
					if (ific < 16)
					{
						double z = ILU[8+iluI] * del[1][k] + ILU[9+iluI] * del[3][k] + ILU[10+iluI] * del[4][k];
						Zy = Zy + z * ZZt[k];
						for (int j = 1; j <= 8; j++)
						{
							Zy = Zy + ILU[j - 1+iluI] * ZZp[j][k] * ZZt[k];
						}
					} else
					{
						for (int j = 1; j <= 4; j++)
						{
							Zy = Zy + ILU[j + 6+iluI] * del[j][k] * ZZt[k];
						}
						for (int j = 1; j <= 7; j++)
						{
							Zy = Zy + ILU[j - 1+iluI] * ZZp[j][k] * ZZt[k];
						}
					}
				}
				Zy = EphemUtils.module(Zy, cpi2);
				Zr += Zx * Math.sin(Zy);
			}
		}

		return Zr;
	}


	/**
	 * Transform J2000 mean inertial coordinates into equatorial FK5.
	 * 
	 * @param position Ecliptic coordinates (x, y, z) or (x, y, z, vx, vy, vz)
	 *        refered to mean ecliptic and inertial equinox of J2000.
	 * @return Equatorial FK5 coordiantes.
	 */
	public static double[] meanJ2000InertialToFK5(double position[])
	{
		double RotM[][] = new double[4][4];
		double out_pos[] = new double[3];
		double out_vel[] = new double[3];

		RotM[1][1] = 1.000000000000;
		RotM[1][2] = 0.000000437913;
		RotM[1][3] = -0.000000189859;
		RotM[2][1] = -0.000000477299;
		RotM[2][2] = 0.917482137607;
		RotM[2][3] = -0.397776981701;
		RotM[3][1] = 0.000000000000;
		RotM[3][2] = 0.397776981701;
		RotM[3][3] = 0.917482137607;

		// Apply rotation
		out_pos[0] = RotM[1][1] * position[0] + RotM[1][2] * position[1] + RotM[1][3] * position[2]; // x
		out_pos[1] = RotM[2][1] * position[0] + RotM[2][2] * position[1] + RotM[2][3] * position[2]; // y
		out_pos[2] = RotM[3][1] * position[0] + RotM[3][2] * position[1] + RotM[3][3] * position[2]; // z
		if (position.length > 3)
		{
			out_vel[0] = RotM[1][1] * position[3] + RotM[1][2] * position[4] + RotM[1][3] * position[5]; // vx
			out_vel[1] = RotM[2][1] * position[3] + RotM[2][2] * position[4] + RotM[2][3] * position[5]; // vy
			out_vel[2] = RotM[3][1] * position[3] + RotM[3][2] * position[4] + RotM[3][3] * position[5]; // vz

			return new double[]
			{ out_pos[0], out_pos[1], out_pos[2], out_vel[0], out_vel[1], out_vel[2] };
		}

		return out_pos;
	}

	/**
	 * Holds the value of the secular acceleration of the Moon. Currently equal
	 * to -25.858 arcsec/cent^2 (Chapront, Chapront-Touze and Francou, 2002).
	 */
	public static double moonSecularAcceleration = -25.858;

	/**
	 * Corrects Julian day of calculations of ELP2000 theory for secular
	 * acceleration of the Moon. This method uses the current value of static
	 * variable {@linkplain Elp2000#moonSecularAcceleration}.
	 * <P>
	 * Correction should be performed to standard dynamical time of calculations
	 * (Barycentric Dynamical Time), as obtained by using the corresponding methods.
	 * {@linkplain Elp2000#elp2000Ephemeris(org.asterope.ephem.TimeElement, org.asterope.ephem.ObserverElement, org.asterope.ephem.EphemerisElement)}
	 * accepts any time scale, so it is possible to use the
	 * output Julian day of this method with any time scale, unless a very
	 * little error (well below the uncertainty in TT-UT correction) could exist
	 * if this correction is applied to LT or UT, before the correction to TDB
	 * which is performed in {@linkplain Elp2000#elp2000Ephemeris(org.asterope.ephem.TimeElement, org.asterope.ephem.ObserverElement, org.asterope.ephem.EphemerisElement)}.
	 * <P>
	 * Correction for different years (using the default value) are as follows:
	 * 
	 * <pre>
	 * Year       Correction (seconds)
	 * -2000      -2796
	 * -1000      -1561
	 *     0      -683
	 *  1000      -163
	 *  1955       0.000
	 *  2000      -0.362
	 *  3000      -195
	 * </pre>
	 * 
	 * @param jd Julian day in TDB.
	 * @return Output (corrected) Julian day in TDB.
	 */
	public static double timeCorrectionForSecularAcceleration(double jd)
	{
		double cent = (jd - 2435109.0) / EphemConstant.JULIAN_DAYS_PER_CENTURY;
		double deltaT = 0.91072 * (moonSecularAcceleration - MOON_SECULAR_ACCELERATION_DE200) * cent * cent;

		return jd + deltaT / EphemConstant.SECONDS_PER_DAY;
	}

    //TODO  make nonstatic, use Dependency Injection
    private static final  MoshierPlanetEphem MOSHIER =  new MoshierPlanetEphem();

	/**
	 * Calculate Moon position (center of mass), providing full data. This
	 * method uses ELP2000 theory from the IMCCE. Typical error is below 0.01
	 * arcseconds when comparing to JPL DE200 Ephemeris. The position error in
	 * this theory can reach some arcseconds outside 20th century due to an
	 * uncertainty with Moon secular acceleration in JPL DE200 integration. So
	 * this method is not recommended outside period 1900-2100. Use {@linkplain PlanetEphem}
	 * instead, which gives almost similar accuracy and better time span, or
	 * the JPL ephemeris.
	 * <P>
	 * The time correction for Moon secular acceleration is automatically done, which means that
	 * this theory will match JPLDE405 up to the arcsecond level during several millenia. Another possible 
	 * correction you may want to apply is from center of mass to geometric center by means of
	 * {@linkplain PlanetEphem#fromMoonBarycenterToGeometricCenter(org.asterope.ephem.TimeElement, org.asterope.ephem.ObserverElement, org.asterope.ephem.EphemerisElement, org.asterope.ephem.EphemElement)}.
	 * <P>
	 * This method also uses Series96 theory for the position of the Earth
	 * between 1900 and 2100, and VSOP87A theory outside this interval. This has
	 * effects in the heliocentric position and physical ephemeris.
	 * <P>
	 * 
	 * @param time Time object containing the date.
	 * @param obs Observer object containing the observer position.
	 * @param eph Ephemeris object with the target and ephemeris
	 *        properties.
	 * @return Ephem object containing full ephemeris data.
	 */
	public static EphemElement elp2000Ephemeris(TimeElement time, // Time
																	// Element
			ObserverElement obs, // Observer Element
			EphemerisElement eph) // Ephemeris Element
	{
		if (eph.targetBody != Target.Moon)
			throw new IllegalArgumentException("target object is not the Moon.");

//		// Check Ephemeris object
//		if (!EphemerisElement.checkEphemeris(eph))
//		{
//			throw new JPARSECException("invalid ephemeris object.");
//		}

		// Set trucation_level to 0 arcsecond (full theory). It is tested that
		// a value of 0.001 produces similar results with much lower computer
		// time.
		double elp_truncation = 0; //0.001;

		// Obtain julian day in Barycentric Dynamical Time
		double JD_TDB = TimeScale.getJD(time, obs, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
		double JD_TDB_corrected = Elp2000.timeCorrectionForSecularAcceleration(JD_TDB);
		
		// Add warning for possible incorrect result. NO LONGER REQUIRED SINCE SECULAR ACCELERATION IS CORRECTED.
		//if (JD_TDB < 2415020.5 || JD_TDB > 2488092.5) JPARSECException.addWarning("This theory is not recommended for this date");

		// Obtain geocentric position
		double geo_eq[] = meanJ2000InertialToFK5(Elp2000.calc(JD_TDB_corrected, elp_truncation));

		// Obtain topocentric light_time
		LocationElement loc = LocationElement.parseRectangularCoordinates(geo_eq);
		double light_time = loc.getRadius() * EphemConstant.LIGHT_TIME_DAYS_PER_AU;
		if (eph.ephemType == EphemerisElement.Ephem.GEOMETRIC)
			light_time = 0.0;
		if (eph.ephemType != EphemerisElement.Ephem.GEOMETRIC && eph.targetBody != Target.Sun)
		{
			double topo[] = EphemUtils.topocentricObserver(time, obs, eph);
			geo_eq = meanJ2000InertialToFK5(Elp2000.calc(JD_TDB_corrected - light_time, elp_truncation));
			double light_time_corrected = EphemUtils.getTopocentricLightTime(geo_eq, topo, eph);
			// Iterate to obtain correct light time and geocentric position.
			// Typical differente in light time is 0.1 seconds for planets.
			// Iterate to a precission up to 0.001 seconds.
			do
			{
				light_time = light_time_corrected;
				geo_eq = meanJ2000InertialToFK5(Elp2000.calc(JD_TDB_corrected - light_time, elp_truncation));
				light_time_corrected = EphemUtils.getTopocentricLightTime(geo_eq, topo, eph);
			} while (Math.abs(light_time - light_time_corrected) > (0.001 / EphemConstant.SECONDS_PER_DAY));
			light_time = light_time_corrected;
		}

		// Obtain heliocentric ecliptic coordinates, mean equinox of date
		double earth[] =
		{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
//		if (JD_TDB > 2341972.5 && JD_TDB < 2488092.5)
//		{
//			try {
//				earth = Series96.getGeocentricPosition(JD_TDB, Target.Sun, light_time);
//			} catch (Exception exc) {
//				earth = PlanetEphem.getGeocentricPosition(JD_TDB, Target.Sun, light_time);
//			}
//		} else
//		{
//			try {
//				earth = Vsop.getFullGeocentricPosition(JD_TDB, Target.Sun, light_time);
//			} catch (Exception exc) {
//				earth = PlanetEphem.getGeocentricPosition(JD_TDB, Target.Sun, light_time);
//			}
//			earth = Vsop.meanEclipticJ2000ToEquatorial(earth);
//		}
        //TODO other theories used to predict Sun position. Find it if Moshier is good for this.
        earth = MOSHIER.getGeocentricPosition(JD_TDB,Target.Sun,light_time);

		double helio_object[] = EphemUtils.substractVector(Elp2000.calc(JD_TDB_corrected - light_time, elp_truncation), EphemUtils
				.equatorialToEcliptic(earth, EphemConstant.J2000, eph.ephemMethod));
		LocationElement loc_elem = LocationElement.parseRectangularCoordinates(Precession.precessPosAndVelInEcliptic(
				EphemConstant.J2000, JD_TDB, helio_object, eph.ephemMethod));

		// Correct for solar deflection
		if (eph.ephemType == EphemerisElement.Ephem.APPARENT)
		{
			if (eph.targetBody != Target.Sun)
				geo_eq = EphemUtils.solarDeflection(geo_eq, earth, EphemUtils.eclipticToEquatorial(helio_object,
						EphemConstant.J2000, eph.ephemMethod));
		}

		/* Correct frame bias in J2000 epoch */
		if (eph.frame == EphemerisElement.Frame.ICRS)
			geo_eq = EphemUtils.toICRSFrame(geo_eq);

		// Transform from J2000 to mean equinox of date
		double geo_date[] = Precession.precessFromJ2000(JD_TDB, geo_eq, eph.ephemMethod);

		// Mean equatorial to true equatorial
		double true_eq[] = geo_date;
		if (eph.ephemType == EphemerisElement.Ephem.APPARENT)
		{
			/* Correct nutation */
			true_eq = Nutation.calcNutation(JD_TDB, geo_date, eph.ephemMethod, Nutation
					.getNutationTheory(eph.ephemMethod));
		}

		// Get equatorial coordinates
		LocationElement ephem_loc = LocationElement.parseRectangularCoordinates(true_eq);

		// Set preliminary results

		EphemElement ephem_elem = new EphemElement();
		ephem_elem.rightAscension = ephem_loc.getLongitude();
		ephem_elem.declination = ephem_loc.getLatitude();
		ephem_elem.distance = ephem_loc.getRadius();
		ephem_elem.heliocentricEclipticLongitude = loc_elem.getLongitude();
		ephem_elem.heliocentricEclipticLatitude = loc_elem.getLatitude();
		ephem_elem.lightTime = (float) light_time;
		// Note distances are apparent, not true
		ephem_elem.distanceFromSun = loc_elem.getRadius();

		/* Topocentric correction */
		if (eph.isTopocentric)
			ephem_elem = EphemUtils.topocentricCorrection(time, obs, ephem_elem, eph);

		/* Physical ephemeris */
		EphemerisElement sun_eph = new EphemerisElement(Target.Sun, eph.ephemType, EphemerisElement.EQUINOX_OF_DATE,
				eph.isTopocentric, eph.ephemMethod, eph.frame);
//		try {
//			ephem_elem = PhysicalParameters.physicalParameters(JD_TDB, Vsop.vsopEphemeris(time, obs, sun_eph), ephem_elem,
//				eph);
//		} catch (Exception exc) {
//			ephem_elem = PhysicalParameters.physicalParameters(JD_TDB, PlanetEphem.MoshierEphemeris(time, obs, sun_eph), ephem_elem,
//					eph);
//		}

        //TODO find if Moshier usage is correct here
        ephem_elem = PhysicalParameters.physicalParameters(JD_TDB, MOSHIER.getEphemeris(time, obs, sun_eph,true), ephem_elem,eph);

		/* Horizontal coordinates */
		if (eph.isTopocentric)
			ephem_elem = EphemUtils.horizontalCoordinates(time, obs, ephem_elem, eph);

		/* Set coordinates to the output equinox */
		if (EphemerisElement.EQUINOX_OF_DATE != eph.equinox)
		{
			ephem_elem = EphemUtils.toOutputEquinox(ephem_elem, eph, JD_TDB);
		}

		ephem_elem.name = eph.targetBody.toString();
		return ephem_elem;
	}



}
