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

import static org.asterope.ephem.EphemConstant.*;
import static org.asterope.ephem.EphemUtils.mod3600;
import static org.asterope.ephem.EphemUtils.sumVectors;



/**
 * A class for planetary and lunar ephemeris calculations, based on a fit to JPL
 * DE404 ephemeris over the interval 3000 B.D - 3000 A.D for giant planets, and
 * 1500 B.D. - 3000 A.D. for inner planets (including Mars). Errors are in the
 * arcsecond level when comparing to the long time spand ephemeris JPL DE406.
 * <P>
 * This code is based on the work by Steve L. Moshier.
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @author S. L. Moshier
 * @version 1.0
 */
public class MoshierPlanetEphem implements Ephem
{
	



	/* From Simon et al (1994) */
	private static double freqs[] =
	{
		/* Arc sec per 10000 Julian years. */
		53810162868.8982, 21066413643.3548, 12959774228.3429, 6890507749.3988, 1092566037.7991, 439960985.5372,
			154248119.3933, 78655032.0744, 52272245.1795 };

	private static double phases[] =
	{
		/* Arc sec. */
		252.25090552 * 3600., 181.97980085 * 3600., 100.46645683 * 3600., 355.43299958 * 3600., 34.35151874 * 3600.,
			50.07744430 * 3600., 314.05500511 * 3600., 304.34866548 * 3600., 860492.1546, };

	/* Compute mean elements at Julian date J. */
	private static double ss[][] = new double[20][41];
	private static double cc[][] = new double[20][41];
	private static double LP_equinox;
    private static double Ea_arcsec;

    /**
	 * Obtain mean elements of the planets.
	 * 
	 * @param J Julian day.
	 * @return An array with the mean longitudes.
	 */
	private double[] meanElements(double J)
	{
		double x, T, T2;

		double Args[] = new double[20];

		/* Time variables. T is in Julian centuries. */
		T = (J - J2000) / JULIAN_DAYS_PER_CENTURY;
		T2 = T * T;

		/*
		 * Mean longitudes of planets (Simon et al, 1994) .047" subtracted from
		 * term for offset to DE403 origin.
		 */

		/* Mercury */
		x = mod3600(538101628.6889819 * T + 908103.213);
		x += (6.39e-6 * T - 0.0192789) * T2;
		Args[0] = ARCSEC_TO_RAD * x;

		/* Venus */
		x = mod3600(210664136.4335482 * T + 655127.236);
		x += (-6.27e-6 * T + 0.0059381) * T2;
		Args[1] = ARCSEC_TO_RAD * x;

		/* Earth */
		x = mod3600(129597742.283429 * T + 361679.198);
		x += (-5.23e-6 * T - 2.04411e-2) * T2;
		Ea_arcsec = ARCSEC_TO_RAD * x;
		Args[2] = ARCSEC_TO_RAD * x;

		/* Mars */
		x = mod3600(68905077.493988 * T + 1279558.751);
		x += (-1.043e-5 * T + 0.0094264) * T2;
		Args[3] = ARCSEC_TO_RAD * x;

		/* Jupiter */
		x = mod3600(10925660.377991 * T + 123665.420);
		x += ((((-3.4e-10 * T + 5.91e-8) * T + 4.667e-6) * T + 5.706e-5) * T - 3.060378e-1) * T2;
		Args[4] = ARCSEC_TO_RAD * x;

		/* Saturn */
		x = mod3600(4399609.855372 * T + 180278.752);
		x += ((((8.3e-10 * T - 1.452e-7) * T - 1.1484e-5) * T - 1.6618e-4) * T + 7.561614E-1) * T2;
		Args[5] = ARCSEC_TO_RAD * x;

		/* Uranus */
		x = mod3600(1542481.193933 * T + 1130597.971) + (0.00002156 * T - 0.0175083) * T2;
		Args[6] = ARCSEC_TO_RAD * x;

		/* Neptune */
		x = mod3600(786550.320744 * T + 1095655.149) + (-0.00000895 * T + 0.0021103) * T2;
		Args[7] = ARCSEC_TO_RAD * x;

		/* Copied from cmoon.c, DE404 version. */
		/* Mean elongation of moon = D */
		x = mod3600(1.6029616009939659e+09 * T + 1.0722612202445078e+06);
		x += (((((-3.207663637426e-013 * T + 2.555243317839e-011) * T + 2.560078201452e-009) * T - 3.702060118571e-005) * T + 6.9492746836058421e-03) * T /*
																																							 * D,
																																							 * t^3
																																							 */
		- 6.7352202374457519e+00) * T2; /* D, t^2 */
		Args[9] = ARCSEC_TO_RAD * x;

		/* Mean distance of moon from its ascending node = F */
		x = mod3600(1.7395272628437717e+09 * T + 3.3577951412884740e+05);
		x += (((((4.474984866301e-013 * T + 4.189032191814e-011) * T - 2.790392351314e-009) * T - 2.165750777942e-006) * T - 7.5311878482337989e-04) * T /*
																																							 * F,
																																							 * t^3
																																							 */
		- 1.3117809789650071e+01) * T2; /* F, t^2 */
        double NF_arcsec = ARCSEC_TO_RAD * x;
		Args[10] = ARCSEC_TO_RAD * x;

		/* Mean anomaly of sun = l' (J. Laskar) */
		x = mod3600(1.2959658102304320e+08 * T + 1.2871027407441526e+06);
		x += ((((((((1.62e-20 * T - 1.0390e-17) * T - 3.83508e-15) * T + 4.237343e-13) * T + 8.8555011e-11) * T - 4.77258489e-8) * T - 1.1297037031e-5) * T + 8.7473717367324703e-05) * T - 5.5281306421783094e-01) * T2;
		Args[11] = ARCSEC_TO_RAD * x;

		/* Mean anomaly of moon = l */
		x = mod3600(1.7179159228846793e+09 * T + 4.8586817465825332e+05);
		x += (((((-1.755312760154e-012 * T + 3.452144225877e-011) * T - 2.506365935364e-008) * T - 2.536291235258e-004) * T + 5.2099641302735818e-02) * T /*
																																							 * l,
																																							 * t^3
																																							 */
		+ 3.1501359071894147e+01) * T2; /* l, t^2 */
		Args[12] = ARCSEC_TO_RAD * x;

		/* Mean longitude of moon, re mean ecliptic and equinox of date = L */
		x = mod3600(1.7325643720442266e+09 * T + 7.8593980921052420e+05);
		x += (((((7.200592540556e-014 * T + 2.235210987108e-010) * T - 1.024222633731e-008) * T - 6.073960534117e-005) * T + 6.9017248528380490e-03) * T /*
																																							 * L,
																																							 * t^3
																																							 */
		- 5.6550460027471399e+00) * T2; /* L, t^2 */
		LP_equinox = ARCSEC_TO_RAD * x;
		Args[13] = ARCSEC_TO_RAD * x;

		/* Precession of the equinox */
		x = (((((((((-8.66e-20 * T - 4.759e-17) * T + 2.424e-15) * T + 1.3095e-12) * T + 1.7451e-10) * T - 1.8055e-8) * T - 0.0000235316) * T + 0.000076) * T + 1.105414) * T + 5028.791959) * T;
		/* Moon's longitude re fixed J2000 equinox. */
        double pA_precession = ARCSEC_TO_RAD * x;

		/* Lunar free librations. */
		/* 74.7 years. Denoted W or LA. */
		x = (-0.112 * T + 1.73655499e6) * T - 389552.81;
		Args[14] = ARCSEC_TO_RAD * mod3600(x);

		/* 2.891725 years. Denoted LB. */
		Args[15] = ARCSEC_TO_RAD * mod3600(4.48175409e7 * T + 806045.7);

		/* 24.2 years. Denoted P or LC. */
		Args[16] = ARCSEC_TO_RAD * mod3600(5.36486787e6 * T - 391702.8);

		/* Usual node term re equinox of date, denoted NA. */
		Args[17] = LP_equinox - NF_arcsec;

		/* Fancy node term, denoted NB. */
		/* Capital Pi of ecliptic motion (Williams 1994). */
		x = (((-0.000004 * T + 0.000026) * T + 0.153382) * T - 867.919986) * T + 629543.967373;
		Args[18] = Args[17] + ARCSEC_TO_RAD * (3.24e5 - x) - pA_precession;

		return Args;
	}

	/**
	 * Generic program to accumulate sum of trigonometric series in three
	 * variables (e.g., longitude, latitude, radius) of the same list of
	 * arguments.
	 * 
	 * @param J Julian day.
	 * @param arg_tbl
	 * @param distance
	 * @param lat_tbl
	 * @param lon_tbl
	 * @param rad_tbl
	 * @param max_harmonic
	 * @param max_power_of_t
	 * @param maxargs
	 * @param timescale
	 * @param trunclvl
	 * @return An array with x, y, z (AU).
	 */
	private double[] gplan(double J, int arg_tbl[], double distance, double lat_tbl[], double lon_tbl[],
			double rad_tbl[], int[] max_harmonic, int max_power_of_t, int maxargs, double timescale, double trunclvl)
	{

		int i, j, k, m, k1, ip, np, nt;
		int p[];
		double pl[];
		double pb[];
		double pr[];
		double su, cu, sv, cv;
		double T, t, sl, sb, sr;

		T = (J - J2000) / timescale;

		/* Calculate sin( i*MM ), etc. for needed multiple angles. */
		for (i = 0; i < 9; i++)
		{
			if ((max_harmonic[i]) > 0)
			{
				sr = (mod3600(freqs[i] * T) + phases[i]) * ARCSEC_TO_RAD;
				sscc(i, sr, max_harmonic[i]);
			}
		}

		/* Point to start of table of arguments. */
		p = arg_tbl;

		/* Point to tabulated cosine and sine amplitudes. */
		pl = lon_tbl;
		pb = lat_tbl;
		pr = rad_tbl;

		sl = 0.0;
		sb = 0.0;
		sr = 0.0;

		np = 0;
		nt = 0;
		cu = 0;

		int p_index = -1;
		int pl_index = -1;
		int pb_index = -1;
		int pr_index = -1;

		for (;;)
		{
			/* argument of sine and cosine */
			/* Number of periodic arguments. */
			p_index++;
			np = p[p_index];
			if (np < 0)
				break;
			if (np == 0)
			{ /* It is a polynomial term. */
				p_index++;
				nt = p[p_index];
				/* "Longitude" polynomial (phi). */
				pl_index++;
				cu = pl[pl_index];
				for (ip = 0; ip < nt; ip++)
				{
					pl_index++;
					cu = cu * T + pl[pl_index];
				}
				sl += mod3600(cu);
				/* "Latitude" polynomial (theta). */
				pb_index++;
				cu = pb[pb_index];
				for (ip = 0; ip < nt; ip++)
				{
					pb_index++;
					cu = cu * T + pb[pb_index];
				}
				sb += cu;
				/* Radius polynomial (psi). */
				pr_index++;
				cu = pr[pr_index];
				for (ip = 0; ip < nt; ip++)
				{
					pr_index++;
					cu = cu * T + pr[pr_index];
				}
				sr += cu;
				continue;
			}

			k1 = 0;
			cv = 0.0;
			sv = 0.0;
			for (ip = 0; ip < np; ip++)
			{
				/* What harmonic. */
				p_index++;
				j = p[p_index];
				/* Which planet. */
				p_index++;
				m = p[p_index] - 1;
				if (j != 0)
				{
					k = Math.abs(j) - 1;

					su = ss[m][k]; /* sin(k*angle) */
					if (j < 0)
						su = -su;

					cu = cc[m][k];
					if (k1 == 0)
					{ /* set first angle */
						sv = su;
						cv = cu;
						k1 = 1;
					} else
					{ /* combine angles */
						t = su * cv + cu * sv;
						cv = cu * cv - su * sv;
						sv = t;
					}
				}
			}

			/* Highest power of T. */
			p_index++;
			nt = p[p_index];
			/* Longitude. */
			pl_index++;
			cu = pl[pl_index];
			pl_index++;
			su = pl[pl_index];
			for (ip = 0; ip < nt; ip++)
			{
				pl_index++;
				cu = cu * T + pl[pl_index];
				pl_index++;
				su = su * T + pl[pl_index];
			}
			sl += cu * cv + su * sv;
			/* Latitude. */
			pb_index++;
			cu = pb[pb_index];
			pb_index++;
			su = pb[pb_index];
			for (ip = 0; ip < nt; ip++)
			{
				pb_index++;
				cu = cu * T + pb[pb_index];
				pb_index++;
				su = su * T + pb[pb_index];
			}
			sb += cu * cv + su * sv;
			/* Radius. */
			pr_index++;
			cu = pr[pr_index];
			pr_index++;
			su = pr[pr_index];
			for (ip = 0; ip < nt; ip++)
			{
				pr_index++;
				cu = cu * T + pr[pr_index];
				pr_index++;
				su = su * T + pr[pr_index];
			}
			sr += cu * cv + su * sv;
		}

		if (distance == 0.0) return new double[] {ARCSEC_TO_RAD * sl, ARCSEC_TO_RAD * sb,
				ARCSEC_TO_RAD * sr};
		
		double pobj[] = new double[3];
		pobj[0] = ARCSEC_TO_RAD * sl;
		pobj[1] = ARCSEC_TO_RAD * sb;
		pobj[2] = distance * (1.0 + ARCSEC_TO_RAD * sr);

		double x = pobj[2] * Math.cos(pobj[0]) * Math.cos(pobj[1]);
		double y = pobj[2] * Math.sin(pobj[0]) * Math.cos(pobj[1]);
		double z = pobj[2] * Math.sin(pobj[1]);

		return new double[]
		{ x, y, z };
	}

	/**
	 * Generic program to accumulate sum of trigonometric series in three
	 * variables (e.g., longitude, latitude, radius) of the same list of
	 * arguments.
	 * 
	 * @param J Julian day.
	 * @param arg_tbl
	 * @param distance
	 * @param lat_tbl
	 * @param lon_tbl
	 * @param rad_tbl
	 * @param max_harmonic
	 * @param max_power_of_t
	 * @param maxargs
	 * @param timescale
	 * @param trunclvl
	 * @return An array with x, y, z (AU).
	 */
	private double[] g3plan(double J, int arg_tbl[], double distance, double lat_tbl[], double lon_tbl[],
			double rad_tbl[], int[] max_harmonic, int max_power_of_t, int maxargs, double timescale, double trunclvl)
	{

		int i, j, k, m, k1, ip, np, nt;
		int p[];
		double pl[];
		double pb[];
		double pr[];
		double su, cu, sv, cv;
		double T, t, sl, sb, sr;

		double args[] = meanElements(J);
		// args[13] -= PlanetEphem.pA_precession; // Solo libraciones
		T = (J - J2000) / timescale;

		/* Calculate sin( i*MM ), etc. for needed multiple angles. */
		for (i = 0; i < maxargs; i++)
		{
			if ((max_harmonic[i]) > 0)
			{
				sscc(i, args[i], max_harmonic[i]);
			}
		}

		/* Point to start of table of arguments. */
		p = arg_tbl;

		/* Point to tabulated cosine and sine amplitudes. */
		pl = lon_tbl;
		pb = lat_tbl;
		pr = rad_tbl;

		sl = 0.0;
		sb = 0.0;
		sr = 0.0;

		np = 0;
		nt = 0;
		cu = 0;

		int p_index = -1;
		int pl_index = -1;
		int pb_index = -1;
		int pr_index = -1;

		for (;;)
		{
			/* argument of sine and cosine */
			/* Number of periodic arguments. */
			p_index++;
			np = p[p_index];
			if (np < 0)
				break;
			if (np == 0)
			{ /* It is a polynomial term. */
				p_index++;
				nt = p[p_index];
				/* "Longitude" polynomial (phi). */
				pl_index++;
				cu = pl[pl_index];
				for (ip = 0; ip < nt; ip++)
				{
					pl_index++;
					cu = cu * T + pl[pl_index];
				}
				sl += cu;
				/* "Latitude" polynomial (theta). */
				pb_index++;
				cu = pb[pb_index];
				for (ip = 0; ip < nt; ip++)
				{
					pb_index++;
					cu = cu * T + pb[pb_index];
				}
				sb += cu;
				/* Radius polynomial (psi). */
				pr_index++;
				cu = pr[pr_index];
				for (ip = 0; ip < nt; ip++)
				{
					pr_index++;
					cu = cu * T + pr[pr_index];
				}
				sr += cu;
				continue;
			}

			k1 = 0;
			cv = 0.0;
			sv = 0.0;
			for (ip = 0; ip < np; ip++)
			{
				/* What harmonic. */
				p_index++;
				j = p[p_index];
				/* Which planet. */
				p_index++;
				m = p[p_index] - 1;
				if (j != 0)
				{
					k = Math.abs(j) - 1;

					su = ss[m][k]; /* sin(k*angle) */
					if (j < 0)
						su = -su;

					cu = cc[m][k];
					if (k1 == 0)
					{ /* set first angle */
						sv = su;
						cv = cu;
						k1 = 1;
					} else
					{ /* combine angles */
						t = su * cv + cu * sv;
						cv = cu * cv - su * sv;
						sv = t;
					}
				}
			}

			/* Highest power of T. */
			p_index++;
			nt = p[p_index];
			/* Longitude. */
			pl_index++;
			cu = pl[pl_index];
			pl_index++;
			su = pl[pl_index];
			for (ip = 0; ip < nt; ip++)
			{
				pl_index++;
				cu = cu * T + pl[pl_index];
				pl_index++;
				su = su * T + pl[pl_index];
			}
			sl += cu * cv + su * sv;
			/* Latitude. */
			pb_index++;
			cu = pb[pb_index];
			pb_index++;
			su = pb[pb_index];
			for (ip = 0; ip < nt; ip++)
			{
				pb_index++;
				cu = cu * T + pb[pb_index];
				pb_index++;
				su = su * T + pb[pb_index];
			}
			sb += cu * cv + su * sv;
			/* Radius. */
			pr_index++;
			cu = pr[pr_index];
			pr_index++;
			su = pr[pr_index];
			for (ip = 0; ip < nt; ip++)
			{
				pr_index++;
				cu = cu * T + pr[pr_index];
				pr_index++;
				su = su * T + pr[pr_index];
			}
			sr += cu * cv + su * sv;
		}

		sl = sl * 0.0001;
		sb = sb * 0.0001;
		sr = sr * 0.0001;
		
		if (distance == 0.0) return new double[] {ARCSEC_TO_RAD * sl + MoshierPlanetEphem.Ea_arcsec, ARCSEC_TO_RAD * sb,
				ARCSEC_TO_RAD * sr};

		double pobj[] = new double[3];
		pobj[0] = ARCSEC_TO_RAD * sl + MoshierPlanetEphem.Ea_arcsec;
		pobj[1] = ARCSEC_TO_RAD * sb;
		pobj[2] = distance * (1.0 + ARCSEC_TO_RAD * sr);

		double x = pobj[2] * Math.cos(pobj[0]) * Math.cos(pobj[1]);
		double y = pobj[2] * Math.sin(pobj[0]) * Math.cos(pobj[1]);
		double z = pobj[2] * Math.sin(pobj[1]);

		return new double[]
		{ x, y, z };
	}

	/**
	 * Generic program to accumulate sum of trigonometric series in two
	 * variables (e.g., longitude, radius) of the same list of arguments.
	 * 
	 * @param J Julian day.
	 * @param arg_tbl
	 * @param distance
	 * @param lat_tbl
	 * @param lon_tbl
	 * @param rad_tbl
	 * @param max_harmonic
	 * @param max_power_of_t
	 * @param maxargs
	 * @param timescale
	 * @param trunclvl
	 * @return An array with x, y, z (AU).
	 */
	private double[] g2plan(double J, int arg_tbl[], double distance, double lat_tbl[], double lon_tbl[],
			double rad_tbl[], int[] max_harmonic, int max_power_of_t, int maxargs, double timescale, double trunclvl,
			double lat)
	{

		int i, j, k, m, k1, ip, np, nt;
		int p[];
		double pl[];
		double pr[];
		double su, cu, sv, cv;
		double T, t, sl, sr;

		double args[] = meanElements(J);
		// args[13] -= PlanetEphem.pA_precession; // Solo libraciones
		T = (J - J2000) / timescale;

		/* Calculate sin( i*MM ), etc. for needed multiple angles. */
		for (i = 0; i < maxargs; i++)
		{
			if ((max_harmonic[i]) > 0)
			{
				sscc(i, args[i], max_harmonic[i]);
			}
		}

		/* Point to start of table of arguments. */
		p = arg_tbl;

		/* Point to tabulated cosine and sine amplitudes. */
		pl = lon_tbl;
		pr = rad_tbl;

		sl = 0.0;
		sr = 0.0;

		np = 0;
		nt = 0;
		cu = 0;

		int p_index = -1;
		int pl_index = -1;
		int pr_index = -1;

		for (;;)
		{
			/* argument of sine and cosine */
			/* Number of periodic arguments. */
			p_index++;
			np = p[p_index];
			if (np < 0)
				break;
			if (np == 0)
			{ /* It is a polynomial term. */
				p_index++;
				nt = p[p_index];
				/* "Longitude" polynomial (phi). */
				pl_index++;
				cu = pl[pl_index];
				for (ip = 0; ip < nt; ip++)
				{
					pl_index++;
					cu = cu * T + pl[pl_index];
				}
				sl += cu;
				/* Radius polynomial (psi). */
				pr_index++;
				cu = pr[pr_index];
				for (ip = 0; ip < nt; ip++)
				{
					pr_index++;
					cu = cu * T + pr[pr_index];
				}
				sr += cu;
				continue;
			}

			k1 = 0;
			cv = 0.0;
			sv = 0.0;
			for (ip = 0; ip < np; ip++)
			{
				/* What harmonic. */
				p_index++;
				j = p[p_index];
				/* Which planet. */
				p_index++;
				m = p[p_index] - 1;
				if (j != 0)
				{
					k = Math.abs(j) - 1;

					su = ss[m][k]; /* sin(k*angle) */
					if (j < 0)
						su = -su;

					cu = cc[m][k];
					if (k1 == 0)
					{ /* set first angle */
						sv = su;
						cv = cu;
						k1 = 1;
					} else
					{ /* combine angles */
						t = su * cv + cu * sv;
						cv = cu * cv - su * sv;
						sv = t;
					}
				}
			}

			/* Highest power of T. */
			p_index++;
			nt = p[p_index];
			/* Longitude. */
			pl_index++;
			cu = pl[pl_index];
			pl_index++;
			su = pl[pl_index];
			for (ip = 0; ip < nt; ip++)
			{
				pl_index++;
				cu = cu * T + pl[pl_index];
				pl_index++;
				su = su * T + pl[pl_index];
			}
			sl += cu * cv + su * sv;
			/* Radius. */
			pr_index++;
			cu = pr[pr_index];
			pr_index++;
			su = pr[pr_index];
			for (ip = 0; ip < nt; ip++)
			{
				pr_index++;
				cu = cu * T + pr[pr_index];
				pr_index++;
				su = su * T + pr[pr_index];
			}
			sr += cu * cv + su * sv;
		}

		double pobj[] = new double[3];
		sl = sl * 0.0001;
		sr = sr * 0.0001;
		
		if (distance == 0.0) return new double[] {ARCSEC_TO_RAD * sl + MoshierPlanetEphem.LP_equinox, 
				lat, ARCSEC_TO_RAD * sr};

		pobj[0] = ARCSEC_TO_RAD * sl + MoshierPlanetEphem.LP_equinox;
		pobj[1] = lat;
		pobj[2] = distance * (1.0 + ARCSEC_TO_RAD * sr);

		double x = pobj[2] * Math.cos(pobj[0]) * Math.cos(pobj[1]);
		double y = pobj[2] * Math.sin(pobj[0]) * Math.cos(pobj[1]);
		double z = pobj[2] * Math.sin(pobj[1]);

		return new double[]
		{ x, y, z };
	}

	/**
	 * Generic program to accumulate sum of trigonometric series in one
	 * variables (e.g., latitude) of the same list of arguments.
	 * 
	 * @param J Julian day.
	 * @param arg_tbl
	 * @param distance
	 * @param lat_tbl
	 * @param lon_tbl
	 * @param rad_tbl
	 * @param max_harmonic
	 * @param max_power_of_t
	 * @param maxargs
	 * @param timescale
	 * @param trunclvl
	 * @return Latitude (rad).
	 */
	private  double g1plan(double J, int arg_tbl[], double distance, double lat_tbl[], double lon_tbl[],
			double rad_tbl[], int[] max_harmonic, int max_power_of_t, int maxargs, double timescale, double trunclvl)
	{

		int i, j, k, m, k1, ip, np, nt;
		int p[];
		double pb[];
		double su, cu, sv, cv;
		double T, t, sb;

		double args[] = meanElements(J);
		T = (J - J2000) / timescale;

		/* Calculate sin( i*MM ), etc. for needed multiple angles. */
		for (i = 0; i < maxargs; i++)
		{
			if ((max_harmonic[i]) > 0)
			{
				sscc(i, args[i], max_harmonic[i]);
			}
		}

		/* Point to start of table of arguments. */
		p = arg_tbl;

		/* Point to tabulated cosine and sine amplitudes. */
		pb = lat_tbl;

		sb = 0.0;

		np = 0;
		nt = 0;
		cu = 0;

		int p_index = -1;
		int pb_index = -1;

		for (;;)
		{
			/* argument of sine and cosine */
			/* Number of periodic arguments. */
			p_index++;
			np = p[p_index];
			if (np < 0)
				break;
			if (np == 0)
			{ /* It is a polynomial term. */
				p_index++;
				nt = p[p_index];
				/* "Latitude" polynomial (theta). */
				pb_index++;
				cu = pb[pb_index];
				for (ip = 0; ip < nt; ip++)
				{
					pb_index++;
					cu = cu * T + pb[pb_index];
				}
				sb += cu;
				continue;
			}

			k1 = 0;
			cv = 0.0;
			sv = 0.0;
			for (ip = 0; ip < np; ip++)
			{
				/* What harmonic. */
				p_index++;
				j = p[p_index];
				/* Which planet. */
				p_index++;
				m = p[p_index] - 1;
				if (j != 0)
				{
					k = Math.abs(j) - 1;

					su = ss[m][k]; /* sin(k*angle) */
					if (j < 0)
						su = -su;

					cu = cc[m][k];
					if (k1 == 0)
					{ /* set first angle */
						sv = su;
						cv = cu;
						k1 = 1;
					} else
					{ /* combine angles */
						t = su * cv + cu * sv;
						cv = cu * cv - su * sv;
						sv = t;
					}
				}
			}

			/* Highest power of T. */
			p_index++;
			nt = p[p_index];
			/* Latitude. */
			pb_index++;
			cu = pb[pb_index];
			pb_index++;
			su = pb[pb_index];
			for (ip = 0; ip < nt; ip++)
			{
				pb_index++;
				cu = cu * T + pb[pb_index];
				pb_index++;
				su = su * T + pb[pb_index];
			}
			sb += cu * cv + su * sv;
		}

		double pobj[] = new double[3];
		sb = sb * 0.0001;
		
		if (distance == 0.0) return (ARCSEC_TO_RAD * sb);

		pobj[0] = 0.0;
		pobj[1] = ARCSEC_TO_RAD * sb;
		pobj[2] = 1.0;

		// double x = pobj[2] * Math.cos(pobj[0]) * Math.cos(pobj[1]);
		// double y = pobj[2] * Math.sin(pobj[0]) * Math.cos(pobj[1]);
		// double z = pobj[2] * Math.sin(pobj[1]);

		return pobj[1];
	}

	/**
	 * Prepare lookup table of sin and cos ( i*Lj ) for required multiple
	 * angles.
	 * 
	 * @param k
	 * @param arg
	 * @param n
	 * @return
	 */
	private  void sscc(int k, double arg, int n)
	{
		double cu, su, cv, sv, s;
		int i;

		su = Math.sin(arg);
		cu = Math.cos(arg);
		ss[k][0] = su; /* sin(L) */
		cc[k][0] = cu; /* cos(L) */
		sv = 2.0 * su * cu;
		cv = cu * cu - su * su;
		ss[k][1] = sv; /* sin(2L) */
		cc[k][1] = cv;
		for (i = 2; i < n; i++)
		{
			s = su * cv + cu * sv;
			cv = cu * cv - su * sv;
			sv = s;
			ss[k][i] = sv; /* sin( i+1 L ) */
			cc[k][i] = cv;
		}
	}

	
	
	/**
	 * Obtain position of a planet. Rectangular heliocentric coordinates mean
	 * equinox and ecliptic J2000.
	 * 
	 * @param JD Time in Julian day.
	 * @param planet Planet ID 
	 * @return Array with the x, y, z results.
	 */
	private  double[] getHeliocentricEclipticPositionJ2000(double JD, Target planet)
	{

		// Default values for Planet.SUN
		double p[] = new double[]
		{ 0.0, 0.0, 0.0 };

		switch (planet)
		{
		case Planet_LUNAR_LIBRATIONS:
			p = gplan(JD, Moshier_libration.args, Moshier_libration.distance, Moshier_libration.tabb, Moshier_libration.tabl,
					Moshier_libration.tabr, Moshier_libration.max_harmonic, Moshier_libration.max_power_of_t,
					Moshier_libration.maxargs, Moshier_libration.timescale, Moshier_libration.trunclvl);
			return p;
		case Mercury:
			p = gplan(JD, Moshier_Mercury.args, Moshier_Mercury.distance, Moshier_Mercury.tabb, Moshier_Mercury.tabl,
					Moshier_Mercury.tabr, Moshier_Mercury.max_harmonic, Moshier_Mercury.max_power_of_t,
					Moshier_Mercury.maxargs, Moshier_Mercury.timescale, Moshier_Mercury.trunclvl);
			break;
		case Venus:
			p = gplan(JD, Moshier_Venus.args, Moshier_Venus.distance, Moshier_Venus.tabb, Moshier_Venus.tabl,
					Moshier_Venus.tabr, Moshier_Venus.max_harmonic, Moshier_Venus.max_power_of_t,
					Moshier_Venus.maxargs, Moshier_Venus.timescale, Moshier_Venus.trunclvl);
			break;
		case Earth_Moon_barycenter:
			p = g3plan(JD, Moshier_Earth_Moon_Barycenter.args, Moshier_Earth_Moon_Barycenter.distance,
					Moshier_Earth_Moon_Barycenter.tabb, Moshier_Earth_Moon_Barycenter.tabl,
					Moshier_Earth_Moon_Barycenter.tabr, Moshier_Earth_Moon_Barycenter.max_harmonic,
					Moshier_Earth_Moon_Barycenter.max_power_of_t, Moshier_Earth_Moon_Barycenter.maxargs,
					Moshier_Earth_Moon_Barycenter.timescale, Moshier_Earth_Moon_Barycenter.trunclvl);
			break;
		case Mars:
			p = gplan(JD, Moshier_Mars.args, Moshier_Mars.distance, Moshier_Mars.tabb, Moshier_Mars.tabl,
					Moshier_Mars.tabr, Moshier_Mars.max_harmonic, Moshier_Mars.max_power_of_t, Moshier_Mars.maxargs,
					Moshier_Mars.timescale, Moshier_Mars.trunclvl);
			break;
		case Jupiter:
			p = gplan(JD, Moshier_Jupiter.args, Moshier_Jupiter.distance, Moshier_Jupiter.tabb, Moshier_Jupiter.tabl,
					Moshier_Jupiter.tabr, Moshier_Jupiter.max_harmonic, Moshier_Jupiter.max_power_of_t,
					Moshier_Jupiter.maxargs, Moshier_Jupiter.timescale, Moshier_Jupiter.trunclvl);
			break;
		case Saturn:
			p = gplan(JD, Moshier_Saturn.args, Moshier_Saturn.distance, Moshier_Saturn.tabb, Moshier_Saturn.tabl,
					Moshier_Saturn.tabr, Moshier_Saturn.max_harmonic, Moshier_Saturn.max_power_of_t,
					Moshier_Saturn.maxargs, Moshier_Saturn.timescale, Moshier_Saturn.trunclvl);
			break;
		case Uranus:
			p = gplan(JD, Moshier_Uranus.args, Moshier_Uranus.distance, Moshier_Uranus.tabb, Moshier_Uranus.tabl,
					Moshier_Uranus.tabr, Moshier_Uranus.max_harmonic, Moshier_Uranus.max_power_of_t,
					Moshier_Uranus.maxargs, Moshier_Uranus.timescale, Moshier_Uranus.trunclvl);
			break;
		case Neptune:
			p = gplan(JD, Moshier_Neptune.args, Moshier_Neptune.distance, Moshier_Neptune.tabb, Moshier_Neptune.tabl,
					Moshier_Neptune.tabr, Moshier_Neptune.max_harmonic, Moshier_Neptune.max_power_of_t,
					Moshier_Neptune.maxargs, Moshier_Neptune.timescale, Moshier_Neptune.trunclvl);
			break;
		case Pluto:
			p = gplan(JD, Moshier_Pluto.args, Moshier_Pluto.distance, Moshier_Pluto.tabb, Moshier_Pluto.tabl,
					Moshier_Pluto.tabr, Moshier_Pluto.max_harmonic, Moshier_Pluto.max_power_of_t,
					Moshier_Pluto.maxargs, Moshier_Pluto.timescale, Moshier_Pluto.trunclvl);
			break;
		case Moon:
			double moon_lat = g1plan(JD, Moshier_Moon_lat.args, Moshier_Moon_lat.distance, Moshier_Moon_lat.tabl,
					Moshier_Moon_lat.tabb, Moshier_Moon_lat.tabr, Moshier_Moon_lat.max_harmonic,
					Moshier_Moon_lat.max_power_of_t, Moshier_Moon_lat.maxargs, Moshier_Moon_lat.timescale,
					Moshier_Moon_lat.trunclvl);
			p = g2plan(JD, Moshier_Moon_lon_rad.args, Moshier_Moon_lon_rad.distance, Moshier_Moon_lon_rad.tabb,
					Moshier_Moon_lon_rad.tabl, Moshier_Moon_lon_rad.tabr, Moshier_Moon_lon_rad.max_harmonic,
					Moshier_Moon_lon_rad.max_power_of_t, Moshier_Moon_lon_rad.maxargs, Moshier_Moon_lon_rad.timescale,
					Moshier_Moon_lon_rad.trunclvl, moon_lat);
			// Here we apply Williams formula to pass to J2000, since this is
			// the one chosen by Moshier
			p = Precession.precessPosAndVelInEcliptic(JD, J2000, p, Precession.Method.WILLIAMS);
			break;
		case Earth:
			p = g3plan(JD, Moshier_Earth_Moon_Barycenter.args, Moshier_Earth_Moon_Barycenter.distance,
					Moshier_Earth_Moon_Barycenter.tabb, Moshier_Earth_Moon_Barycenter.tabl,
					Moshier_Earth_Moon_Barycenter.tabr, Moshier_Earth_Moon_Barycenter.max_harmonic,
					Moshier_Earth_Moon_Barycenter.max_power_of_t, Moshier_Earth_Moon_Barycenter.maxargs,
					Moshier_Earth_Moon_Barycenter.timescale, Moshier_Earth_Moon_Barycenter.trunclvl);
			moon_lat = g1plan(JD, Moshier_Moon_lat.args, Moshier_Moon_lat.distance, Moshier_Moon_lat.tabl,
					Moshier_Moon_lat.tabb, Moshier_Moon_lat.tabr, Moshier_Moon_lat.max_harmonic,
					Moshier_Moon_lat.max_power_of_t, Moshier_Moon_lat.maxargs, Moshier_Moon_lat.timescale,
					Moshier_Moon_lat.trunclvl);
			double p_moon[] = g2plan(JD, Moshier_Moon_lon_rad.args, Moshier_Moon_lon_rad.distance,
					Moshier_Moon_lon_rad.tabb, Moshier_Moon_lon_rad.tabl, Moshier_Moon_lon_rad.tabr,
					Moshier_Moon_lon_rad.max_harmonic, Moshier_Moon_lon_rad.max_power_of_t,
					Moshier_Moon_lon_rad.maxargs, Moshier_Moon_lon_rad.timescale, Moshier_Moon_lon_rad.trunclvl,
					moon_lat);
			// Here we apply Williams formula to pass to J2000, since this is
			// the one chosen by Moshier
			p_moon = Precession.precessPosAndVelInEcliptic(JD, J2000, p_moon, Precession.Method.WILLIAMS);

			// Earth position is found indirectly, knowing the position of the
			// Earth-Moon
			// barycenter and the geocentric Moon
			double Earth_Moon_mass_ratio = EphemConstant.MOON_RELATIVE_MASS / EphemConstant.EARTH_RELATIVE_MASS;

			double c = 1.0 / (Earth_Moon_mass_ratio + 1.0);
			p[0] = p[0] - c * p_moon[0];
			p[1] = p[1] - c * p_moon[1];
			p[2] = p[2] - c * p_moon[2];

			break;
		case Sun: break;
		default: throw new IllegalArgumentException("Body not supported by Moshier ephem: "+planet);
		}

		return p;
	}

	/**
	 * Ecliptic mean J2000 rectangular position of a satellite. Vector (0.0,
	 * 0.0, 0.0) for obtaining the position of the planet.
	 */
	private double[] planetocentricPositionOfPlanetSatellite =
	{ 0.0, 0.0, 0.0 };

	/**
	 * Sets an offset from the position of a planet to refer the ephemeris to.
	 * Useful for obtaining satellite ephemerides.
	 * 
	 * @param eq Mean ecliptic vector J2000, components in AU.
	 */
	public  void setOffsetPosition(double eq[])
	{
		planetocentricPositionOfPlanetSatellite = eq;
	}

	/**
	 * Get rectangular ecliptic geocentric position of a planet in equinox
	 * J2000.
	 * 
	 * @param JD Julian day in TDB.
	 * @param planet Planet ID.
	 * @return Array with x, y, z, vx, vy, vz coordinates.
	 */
	public double[] getGeocentricPosition(double JD, Target planet, double light_time)
	{
		// Heliocentric position corrected for light time
		double helio_object[] = getHeliocentricEclipticPositionJ2000(JD - light_time, planet);
		helio_object = sumVectors(helio_object, planetocentricPositionOfPlanetSatellite);

		if (planet == Target.Moon)
			return new double[]
			{ helio_object[0], helio_object[1], helio_object[2], 0.0, 0.0, 0.0 };

		// Compute position of Earth
		double helio_earth[] = getHeliocentricEclipticPositionJ2000(JD, Target.Earth);
		double time_step = 0.001;
		double helio_earth_plus[] = getHeliocentricEclipticPositionJ2000(JD + time_step, Target.Earth);
		double helio_earth_vel[] =
		{ (helio_earth_plus[0] - helio_earth[0]) / time_step, (helio_earth_plus[1] - helio_earth[1]) / time_step,
				(helio_earth_plus[2] - helio_earth[2]) / time_step };

		// Compute geocentric position of the object, and
		// also velocity vector of the geocenter
		double geo_pos[] = new double[]
		{ -helio_earth[0] + helio_object[0], -helio_earth[1] + helio_object[1], -helio_earth[2] + helio_object[2],

		helio_earth_vel[0], helio_earth_vel[1], helio_earth_vel[2], };

		return geo_pos;

	}


	private  EphemElement MoshierCalc(TimeElement time, // Time Element
			ObserverElement obs, // Observer Element
			EphemerisElement eph) // Ephemeris Element
			
	{

		// Obtain julian day in Barycentric Dynamical Time
		double JD_TDB = TimeScale.getJD(time, obs, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);

		// Check Julian day for time spand validity
		if (JD_TDB < 1228000.5 && (eph.targetBody == Target.Mercury || eph.targetBody == Target.Venus || eph.targetBody == Target.Earth_Moon_barycenter || eph.targetBody == Target.Earth || eph.targetBody == Target.Sun || eph.targetBody == Target.Moon || eph.targetBody == Target.Mars))
			throw new IllegalArgumentException("invalid date.");
		if (JD_TDB < 625296.5 && (eph.targetBody == Target.Jupiter || eph.targetBody == Target.Saturn || eph.targetBody == Target.Uranus || eph.targetBody == Target.Neptune || eph.targetBody == Target.Pluto))
			throw new IllegalArgumentException("invalid date.");
		if (JD_TDB > 2817057.5)
			throw new IllegalArgumentException("invalid date.");

		// Obtain geocentric position
		double geo_eq[] = EphemUtils.eclipticToEquatorial(
					getGeocentricPosition(JD_TDB, eph.targetBody, 0.0),
				J2000, eph.ephemMethod);

		// Obtain topocentric light_time
		LocationElement loc = LocationElement.parseRectangularCoordinates(geo_eq);
		double light_time = loc.getRadius() * LIGHT_TIME_DAYS_PER_AU;
		if (eph.ephemType == EphemerisElement.Ephem.GEOMETRIC)
			light_time = 0.0;
		if (eph.ephemType != EphemerisElement.Ephem.GEOMETRIC && eph.targetBody != Target.Sun)
		{
			double topo[] = EphemUtils.topocentricObserver(time, obs, eph);
			geo_eq = EphemUtils.eclipticToEquatorial(
					getGeocentricPosition(JD_TDB, eph.targetBody, light_time), J2000, eph.ephemMethod);
			double light_time_corrected = EphemUtils.getTopocentricLightTime(geo_eq, topo, eph);
			// Iterate to obtain correct light time and geocentric position.
			// Iterate to a precission up to 0.001 seconds (below the
			// milliarsecond).
			do
			{
				light_time = light_time_corrected;
				geo_eq = EphemUtils.eclipticToEquatorial(
							getGeocentricPosition(JD_TDB, eph.targetBody,
						light_time_corrected), J2000, eph.ephemMethod);
				light_time_corrected = EphemUtils.getTopocentricLightTime(geo_eq, topo, eph);
			} while (Math.abs(light_time - light_time_corrected) > (0.001 / SECONDS_PER_DAY));
			light_time = light_time_corrected;
		}

		// Obtain heliocentric ecliptic coordinates
		double helio_object[] = getHeliocentricEclipticPositionJ2000(JD_TDB - light_time, eph.targetBody);
		helio_object = sumVectors(helio_object, planetocentricPositionOfPlanetSatellite);

		if (eph.targetBody == Target.Moon)
		{
			double geo_sun[] = getGeocentricPosition(JD_TDB, Target.Sun, light_time);
			helio_object = EphemUtils.substractVector(helio_object, geo_sun);

		}
		LocationElement loc_elem = LocationElement.parseRectangularCoordinates(Precession.precessPosAndVelInEcliptic(
				J2000, JD_TDB, helio_object, eph.ephemMethod));

		// Correct for solar deflection and aberration
		if (eph.ephemType == EphemerisElement.Ephem.APPARENT)
		{
			double earth[] = EphemUtils.eclipticToEquatorial(getGeocentricPosition(JD_TDB, Target.Sun,
					light_time), J2000, eph.ephemMethod);
			if (eph.targetBody != Target.Sun)
				geo_eq = EphemUtils.solarDeflection(geo_eq, earth, helio_object);
			if (eph.targetBody != Target.Moon)
				geo_eq = EphemUtils.aberration(geo_eq, earth, light_time);
		}

		/*
		 * Correct frame bias in J2000 epoch, in this case to J2000 mean frame
		 * since DE40x uses ICRS by default
		 */
		if (eph.frame == EphemerisElement.Frame.J2000)
			geo_eq = EphemUtils.toJ2000Frame(geo_eq);

		// Transform from J2000 to mean equinox of date
		double geo_date[] = Precession.precessFromJ2000(JD_TDB, geo_eq, eph.ephemMethod);

		// Mean equatorial to true equatorial
		double true_eq[] = geo_date;
		if (eph.ephemType == EphemerisElement.Ephem.APPARENT)
		{
			/* Correct nutation */
			true_eq = Nutation.calcNutation(JD_TDB, geo_date, eph.ephemMethod, Nutation.getNutationTheory(eph.ephemMethod));
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

		ephem_elem.name = eph.targetBody.name();
		return ephem_elem;
	}

	public EphemElement getEphemeris(TimeElement time, ObserverElement obs, EphemerisElement eph, boolean fullEphem) {


		EphemElement ephem_elem = MoshierCalc(time, obs, eph);

		// Obtain julian day in Barycentric Dynamical Time
		double JD_TDB = TimeScale.getJD(time, obs, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);

		/* Physical ephemeris */
		EphemerisElement sun_eph = new EphemerisElement(Target.Sun, eph.ephemType, EphemerisElement.EQUINOX_OF_DATE,
				eph.isTopocentric, eph.ephemMethod, eph.frame);
		ephem_elem = PhysicalParameters.physicalParameters(JD_TDB, MoshierCalc(time, obs, sun_eph), ephem_elem, eph);

		/* Horizontal coordinates */
		if (eph.isTopocentric)
			ephem_elem = EphemUtils.horizontalCoordinates(time, obs, ephem_elem, eph);

		/* Set coordinates to the output equinox */
		if (EphemerisElement.EQUINOX_OF_DATE != eph.equinox)
		{
			ephem_elem = EphemUtils.toOutputEquinox(ephem_elem, eph, JD_TDB);
		}

		return ephem_elem;
	}


//	/**
//	 * For rade2Vector testing only.
//	 */
//	public static void main(String args[])
//	{
//		System.out.println("PlanetEphem Test");
//
//			AstroDate astro = new AstroDate(2009, AstroDate.JANUARY, 21, 0, 0 , 0);
////			AstroDate astro = new AstroDate(1986, AstroDate.JANUARY, 1, 0, 0, 0);
//			TimeElement time = new TimeElement(astro.toGCalendar(), TimeElement.UNIVERSAL_TIME_UT1);
//			CityElement city = City.findCity("Madrid");
//			//ObservatoryElement observatory = Observatory.findObservatorybyName("Yebes");
//			EphemerisElement eph = new EphemerisElement(Planet.SUN, EphemerisElement.EPHEM_APPARENT,
//					EphemerisElement.EQUINOX_OF_DATE, EphemerisElement.GEOCENTRIC, EphemerisElement.APPLY_IAU2000, 
//					EphemerisElement.FRAME_ICRS);
//			eph.algorithm = EphemerisElement.Algorithm.ALGORITHM_MOSHIER;
//			ObserverElement observer = ObserverElement.parseCity(city);
////			ObserverElement observer = ObserverElement.parseObservatory(observatory);
//
//			EphemElement ephem = MoshierPlanetEphem.MoshierEphemeris(time, observer, eph);
//			ephem = RiseSetTransit.obtainCurrentOrNextRiseSetTransit(time, observer, eph, ephem, 34.0 * DEG_TO_RAD / 60.0 + ephem.angularRadius);
//
//			double JD = TimeScale.getJD(time, observer, eph, TimeScale.JD_TDB);
//			astro = new AstroDate(JD);
//			System.out.println("JD " + JD+" "+astro.getYear()+"-"+astro.getMonth()+"-"+astro.getDay());
//			System.out.println("MOSHIER");
//
//			LocationElement loc = CoordinateSystem.equatorialToEcliptic(
//					new LocationElement(ephem.rightAscension, ephem.declination, ephem.distance), time, observer, eph);
//			System.out.println("Ecl lon "+formatAngle(loc.getLongitude(), 4));
//			System.out.println("Ecl lat "+formatAngle(loc.getLatitude(), 4));
//
//			System.out.println("lon "+formatAngle(ephem.rightAscension, 4));
//			System.out.println("lat "+formatAngle(ephem.declination, 4));
//			ConsoleReport.fullEphemReportToConsole(ephem);
//
//			double lib[] = MoshierPlanetEphem.getHeliocentricEclipticPositionJ2000(JD, Planet.Planet_LUNAR_LIBRATIONS);
//			System.out.println(lib[0]+"/"+lib[1]+"/"+lib[2]);			
//
//	}
//	


}
