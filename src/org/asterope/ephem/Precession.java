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


import java.util.ArrayList;


/**
 * Precession of the equinox and ecliptic.
 * <P>
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class Precession
{
	
	public enum Method {
	/**
	 * EphemUtils.ID for selecting Capitaine formula of precession (to be
	 * officially adopted by the IAU, see Hilton et al. 2006). Capitaine et al.,
	 * Astronomy & Astrophysics 412, 567-586, 2003.
	 */
	CAPITAINE,

	/**
	 * EphemUtils.ID for selecting IAU2000 formula of precession. Note this
	 * formula is not recommended.
	 */
	IAU2000,

	/**
	 * Williams et al. precession method.
	 * <P>
	 * James G. Williams, "Contributions to the Earth's obliquity rate,
	 * precession, and nutation," Astron. J. 108, 711-724 (1994).
	 */
	WILLIAMS,

	/**
	 * JPL DE403 precession method, a little correction to Williams method.
	 * <P>
	 * James G. Williams, "Contributions to the Earth's obliquity rate,
	 * precession, and nutation," Astron. J. 108, 711-724 (1994).
	 */
	JPLDE403,

	/**
	 * Simon et al. precession method.
	 * <P>
	 * J. L. Simon, P. Bretagnon, J. Chapront, M. Chapront-Touze', G. Francou,
	 * and J. Laskar, "Numerical Expressions for precession formulae and mean
	 * elements for the Moon and the planets," Astronomy and Astrophysics 282,
	 * 663-683 (1994).
	 */
	SIMON,

	/**
	 * IAU 1976 precession method as given by Laskar 1986.
	 * <P>
	 * IAU Coefficients are from:
	 * <P>
	 * J. H. Lieske, T. Lederle, W. Fricke, and B. Morando, "Expressions for the
	 * Precession Quantities Based upon the IAU (1976) System of Astronomical
	 * EphemUtils.," Astronomy and Astrophysics 58, 1-16 (1977).
	 * <P>
	 * Newer formulas that cover a much longer time span are from: J. Laskar,
	 * "Secular terms of classical planetary theories using the results of
	 * general theory," Astronomy and Astrophysics 157, 59070 (1986).
	 * <P>
	 * See also:
	 * <P>
	 * P. Bretagnon and G. Francou, "Planetary theories in rectangular and
	 * spherical variables. VSOP87 solutions," Astronomy and Astrophysics 202,
	 * 309-315 (1988).
	 * <P>
	 * Laskar's expansions are said by Bretagnon and Francou to have "a
	 * precision of about 1" over 10000 years before and after J2000.0 in so far
	 * as the precession EphemUtils. p^0_A and epsilon^0_A are perfectly known."
	 * <P>
	 * Bretagnon and Francou's expansions for the node and inclination of the
	 * ecliptic were derived from Laskar's data but were truncated after the
	 * term in T**6. Moshier recomputed these expansions from Laskar's data,
	 * retaining powers up to T**10 in the result.
	 * <P>
	 * The following table indicates the differences between the result of the
	 * IAU formula and Laskar's formula using four different test vectors,
	 * checking at J2000 plus and minus the indicated number of years. Test by
	 * S. L. Moshier.
	 * <P>
	 * 
	 * <pre>
	 *   Years       Arc
	 * from J2000  Seconds
	 * ----------  -------
	 *       0      0.000
	 *     100      0.006
	 *     200      0.006
	 *     500      0.015
	 *    1000      0.28 
	 *    2000      6.40 
	 *    3000     38    
	 *   10000   9400    
	 * </pre>
	 */
	LASKAR
	}

	/**
	 * Get the precession matrices.
	 * 
	 * @param type Precession method.
	 * @return Vector with position angle, node, and inclination of Earth's
	 *         orbit.
	 */
	private static ArrayList<double[]> getMatrices(Method type)
	{
		ArrayList<double[]> v = new ArrayList<double[]>();

		switch (type)
		{
		case WILLIAMS:
			/*
			 * In WILLIAMS and SIMON, Laskar's terms of order higher than t^4
			 * have been retained, because Simon et al mention that the solution
			 * is the same except for the lower order terms.
			 */
			v.add(new double[]
					{ -8.66e-10, -4.759e-8, 2.424e-7, 1.3095e-5, 1.7451e-4, -1.8055e-3, -0.235316, 0.076, 110.5407,
							50287.70000 });
			/* Pi from Williams' 1994 paper, in radians. */
			v.add(new double[]
			{ 6.6402e-16, -2.69151e-15, -1.547021e-12, 7.521313e-12, 1.9e-10, -3.54e-9, -1.8103e-7, 1.26e-7,
					7.436169e-5, -0.04207794833, 3.052115282424 });
			v.add(new double[]
			{ 1.2147e-16, 7.3759e-17, -8.26287e-14, 2.503410e-13, 2.4650839e-11, -5.4000441e-11, 1.32115526e-9,
					-6.012e-7, -1.62442e-5, 0.00227850649, 0.0 });

			break;
		case JPLDE403:
			/* Corrections to Williams (1994) introduced in DE403. */
			v
					.add(new double[]
					{ -8.66e-10, -4.759e-8, 2.424e-7, 1.3095e-5, 1.7451e-4, -1.8055e-3, -0.235316, 0.076, 110.5414,
							50287.91959 });
			/* Pi from Williams' 1994 paper, in radians. No change in DE403. */
			v.add(new double[]
			{ 6.6402e-16, -2.69151e-15, -1.547021e-12, 7.521313e-12, 1.9e-10, -3.54e-9, -1.8103e-7, 1.26e-7,
					7.436169e-5, -0.04207794833, 3.052115282424 });
			v.add(new double[]
			{ 1.2147e-16, 7.3759e-17, -8.26287e-14, 2.503410e-13, 2.4650839e-11, -5.4000441e-11, 1.32115526e-9,
					-6.012e-7, -1.62442e-5, 0.00227850649, 0.0 });
			break;
		case SIMON:
			/* Precession coefficients from Simon et al: */
			v
					.add(new double[]
					{ -8.66e-10, -4.759e-8, 2.424e-7, 1.3095e-5, 1.7451e-4, -1.8055e-3, -0.235316, 0.07732, 111.2022,
							50288.200 });
			v.add(new double[]
			{ 6.6402e-16, -2.69151e-15, -1.547021e-12, 7.521313e-12, 1.9e-10, -3.54e-9, -1.8103e-7, 2.579e-8,
					7.4379679e-5, -0.0420782900, 3.0521126906 });
			v.add(new double[]
			{ 1.2147e-16, 7.3759e-17, -8.26287e-14, 2.503410e-13, 2.4650839e-11, -5.4000441e-11, 1.32115526e-9,
					-5.99908e-7, -1.624383e-5, 0.002278492868, 0.0 });

			break;
		case LASKAR:
			/* Precession coefficients taken from Laskar's paper: */
			v
					.add(new double[]
					{ -8.66e-10, -4.759e-8, 2.424e-7, 1.3095e-5, 1.7451e-4, -1.8055e-3, -0.235316, 0.07732, 111.1971,
							50290.966 });
			/*
			 * Node and inclination of the earth's orbit computed from Laskar's
			 * data as done in Bretagnon and Francou's paper. Units are radians.
			 */
			v.add(new double[]
			{ 6.6402e-16, -2.69151e-15, -1.547021e-12, 7.521313e-12, 6.3190131e-10, -3.48388152e-9, -1.813065896e-7,
					2.75036225e-8, 7.4394531426e-5, -0.042078604317, 3.052112654975 });
			v.add(new double[]
			{ 1.2147e-16, 7.3759e-17, -8.26287e-14, 2.503410e-13, 2.4650839e-11, -5.4000441e-11, 1.32115526e-9,
					-5.998737027e-7, -1.6242797091e-5, 0.002278495537, 0.0 });

			break;

		}

		return v;

	}

	/**
	 * EphemUtils. that define precession direction when calling getAngles.
	 * Currently not visible for the user (not necessary).
	 */
	private static final int PRECESS_FROM_J2000 = -1;

	private static final int PRECESS_TO_J2000 = 1;

	/**
	 * Get precession angles.
	 * 
	 * @param direction from or to J2000 epoch.
	 * @param JD Julian day of the output angles.
	 * @param type Precession method to apply.
	 * @return Array with the three precession angles.
	 */
	private static double[] getAngles(int direction, double JD, Method type)
	{
		double T, pA, W, z;
		double element1[], element2[], element3[];
		int p1 = -1, p2 = -1, p3 = -1;
		int i;
		double JD0 = EphemConstant.J2000;

		/*
		 * Each precession angle is specified by a polynomial in T = Julian
		 * centuries from JD0. See AA page B18.
		 */
		T = (JD - JD0) / EphemConstant.JULIAN_DAYS_PER_CENTURY;

		ArrayList<double[]> v = getMatrices(type);

		/*
		 * Precession in longitude
		 */
		T /= 10.0; /* thousands of years */
		p1++;
		element1 = v.get(0);
		pA = element1[p1];
		for (i = 0; i < 9; i++)
		{
			p1++;
			pA = pA * T + element1[p1];
		}
		pA *= EphemConstant.ARCSEC_TO_RAD * T;

		/*
		 * Node of the moving ecliptic on the JD0 ecliptic.
		 */
		p2++;
		element2 = v.get(1);
		W = element2[p2];
		for (i = 0; i < 10; i++)
		{
			p2++;
			W = W * T + element2[p2];
		}

		/*
		 * Rotate about new x axis by the inclination of the moving ecliptic on
		 * the JD0 ecliptic.
		 */
		p3++;
		element3 = v.get(2);
		z = element3[p3];
		for (i = 0; i < 10; i++)
		{
			p3++;
			z = z * T + element3[p3];
		}

		if (direction == PRECESS_TO_J2000)
			z = -z;

		return new double[]
		{ pA, W, z };
	}

	/**
	 * Precession following Capitaine et al. 2003.
	 * <P>
	 * Capitaine formula of precession is to be officially adopted by the IAU,
	 * see recommendation in the report of the IAU Division I Working Group on
	 * Precession and the Ecliptic (Hilton et al. 2006, Celest. Mech., 94,
	 * 351-367).
	 * <P>
	 * Reference: Capitaine et al., Astronomy & Astrophysics 412, 567-586,
	 * 2003.
	 * 
	 * @param JD0 Julian day of input vector (equatorial rectangular).
	 * @param JD Julian day of output. Either JD or JD0 must be equal to
	 *        EphemUtils.J2000.
	 * @param R Input vector.
	 * @return Vector refered to mean equinox and equator of JD.
	 */
	private static double[] precessionCapitaine(double JD0, double JD, double[] R)
	{
		double T = (JD - JD0) / EphemConstant.JULIAN_DAYS_PER_CENTURY;
		if (JD == EphemConstant.J2000)
			T = -T;

		double EPS0 = 84381.406;
		double PSIA = ((((-0.0000000951 * T + 0.000132851) * T - 0.00114045) * T - 1.0790069) * T + 5038.481507) * T;
		double OMEGAA = ((((+0.0000003337 * T - 0.000000467) * T - 0.00772503) * T + 0.0512623) * T - 0.025754) * T + EPS0;
		double CHIA = ((((-0.0000000560 * T + 0.000170663) * T - 0.00121197) * T - 2.3814292) * T + 10.556403) * T;

//		System.out.println(PSIA);
//		System.out.println(OMEGAA);
//		System.out.println(CHIA);

		double SA = Math.sin(EPS0 * EphemConstant.ARCSEC_TO_RAD);
		double CA = Math.cos(EPS0 * EphemConstant.ARCSEC_TO_RAD);
		double SB = Math.sin(-PSIA * EphemConstant.ARCSEC_TO_RAD);
		double CB = Math.cos(-PSIA * EphemConstant.ARCSEC_TO_RAD);
		double SC = Math.sin(-OMEGAA * EphemConstant.ARCSEC_TO_RAD);
		double CC = Math.cos(-OMEGAA * EphemConstant.ARCSEC_TO_RAD);
		double SD = Math.sin(CHIA * EphemConstant.ARCSEC_TO_RAD);
		double CD = Math.cos(CHIA * EphemConstant.ARCSEC_TO_RAD);

		// COMPUTE ELEMENTS OF PRECESSION ROTATION MATRIX
		// EQUIVALENT TO R3(CHI_A)R1(-OMEGA_A)R3(-PSI_A)R1(EPSILON_0)
		double XX = CD * CB - SB * SD * CC;
		double YX = CD * SB * CA + SD * CC * CB * CA - SA * SD * SC;
		double ZX = CD * SB * SA + SD * CC * CB * SA + CA * SD * SC;
		double XY = -SD * CB - SB * CD * CC;
		double YY = -SD * SB * CA + CD * CC * CB * CA - SA * CD * SC;
		double ZY = -SD * SB * SA + CD * CC * CB * SA + CA * CD * SC;
		double XZ = SB * SC;
		double YZ = -SC * CB * CA - SA * CC;
		double ZZ = -SC * CB * SA + CC * CA;

		double px = 0.0, py = 0.0, pz = 0.0;

		if (JD0 == EphemConstant.J2000)
		{
			// PERFORM ROTATION FROM J2000.0 TO EPOCH
			px = XX * R[0] + YX * R[1] + ZX * R[2];
			py = XY * R[0] + YY * R[1] + ZY * R[2];
			pz = XZ * R[0] + YZ * R[1] + ZZ * R[2];
		} else
		{
			// PERFORM ROTATION FROM EPOCH TO J2000.0
			px = XX * R[0] + XY * R[1] + XZ * R[2];
			py = YX * R[0] + YY * R[1] + YZ * R[2];
			pz = ZX * R[0] + ZY * R[1] + ZZ * R[2];
		}

		return new double[]
		{ px, py, pz };
	}

	/**
	 * Precession following IAU2000 definitions. From SOFA software library.
	 * <P>
	 * Reference: Capitaine et al., Astronomy & Astrophysics 400, 1145-1154,
	 * 2003. See also Lieske et al. 1977.
	 * 
	 * @param JD0 Julian day of input vector (equatorial rectangular).
	 * @param JD Julian day of output. Either JD or JD0 must be equal to
	 *        EphemUtils.J2000.
	 * @param R Input vector.
	 * @return Vector refered to mean equinox and equator of JD.
	 */
	private static double[] precessionIAU2000(double JD0, double JD, double[] R)
	{
		double T = (JD - JD0) / EphemConstant.JULIAN_DAYS_PER_CENTURY;
		double T0 = (JD - EphemConstant.J2000) / EphemConstant.JULIAN_DAYS_PER_CENTURY;
		if (JD == EphemConstant.J2000)
			T = -T;

		double EPS0 = 84381.448;
		double PSIA = ((((-0.0 * T + 0.0) * T - 0.001147) * T - 1.07259) * T + 5038.7784) * T - 0.29965 * T0;
		double OMEGAA = ((((+0.0 * T - 0.0) * T - 0.007726) * T + 0.05127) * T - 0.0) * T + EPS0 - 0.02524 * T0;
		double CHIA = ((((-0.0 * T + 0.0) * T - 0.001125) * T - 2.38064) * T + 10.5526) * T;

//		System.out.println(PSIA);
//		System.out.println(OMEGAA);
//		System.out.println(CHIA);
		
		double SA = Math.sin(EPS0 * EphemConstant.ARCSEC_TO_RAD);
		double CA = Math.cos(EPS0 * EphemConstant.ARCSEC_TO_RAD);
		double SB = Math.sin(-PSIA * EphemConstant.ARCSEC_TO_RAD);
		double CB = Math.cos(-PSIA * EphemConstant.ARCSEC_TO_RAD);
		double SC = Math.sin(-OMEGAA * EphemConstant.ARCSEC_TO_RAD);
		double CC = Math.cos(-OMEGAA * EphemConstant.ARCSEC_TO_RAD);
		double SD = Math.sin(CHIA * EphemConstant.ARCSEC_TO_RAD);
		double CD = Math.cos(CHIA * EphemConstant.ARCSEC_TO_RAD);

		// COMPUTE ELEMENTS OF PRECESSION ROTATION MATRIX
		// EQUIVALENT TO R3(CHI_A)R1(-OMEGA_A)R3(-PSI_A)R1(EPSILON_0)
		double XX = CD * CB - SB * SD * CC;
		double YX = CD * SB * CA + SD * CC * CB * CA - SA * SD * SC;
		double ZX = CD * SB * SA + SD * CC * CB * SA + CA * SD * SC;
		double XY = -SD * CB - SB * CD * CC;
		double YY = -SD * SB * CA + CD * CC * CB * CA - SA * CD * SC;
		double ZY = -SD * SB * SA + CD * CC * CB * SA + CA * CD * SC;
		double XZ = SB * SC;
		double YZ = -SC * CB * CA - SA * CC;
		double ZZ = -SC * CB * SA + CC * CA;

		double px = 0.0, py = 0.0, pz = 0.0;

		if (JD0 == EphemConstant.J2000)
		{
			// PERFORM ROTATION FROM J2000.0 TO EPOCH
			px = XX * R[0] + YX * R[1] + ZX * R[2];
			py = XY * R[0] + YY * R[1] + ZY * R[2];
			pz = XZ * R[0] + YZ * R[1] + ZZ * R[2];
		} else
		{
			// PERFORM ROTATION FROM EPOCH TO J2000.0
			px = XX * R[0] + XY * R[1] + XZ * R[2];
			py = YX * R[0] + YY * R[1] + YZ * R[2];
			pz = ZX * R[0] + ZY * R[1] + ZZ * R[2];
		}

		return new double[]
		{ px, py, pz };
	}

	/**
	 * Precess rectangular equatorial coordinates from J2000 epoch.
	 * 
	 * @param JD Equinox of the output in Julian day.
	 * @param R Array with x, y, z.
	 * @param type Precession method to apply.
	 * @return Array with corected x, y, z.
	 */
	public static double[] precessFromJ2000(double JD, double[] R, Method type)
	{
		double A, B, T, pA, W, z;
		double x[] = new double[3];
		double JD0 = EphemConstant.J2000;

		if (JD == JD0)
			return R;

		if (type == Method.IAU2000)
		{
			return Precession.precessionIAU2000(EphemConstant.J2000, JD, R);
		}
		if (type == Method.CAPITAINE)
		{
			return Precession.precessionCapitaine(EphemConstant.J2000, JD, R);
		}

		double angles[] = getAngles(PRECESS_FROM_J2000, JD, type);

		/*
		 * Each precession angle is specified by a polynomial in T = Julian
		 * centuries from JD0. See AA page B18.
		 */
		T = (JD - JD0) / EphemConstant.JULIAN_DAYS_PER_CENTURY;

		/*
		 * Implementation by elementary rotations using expansions. First rotate
		 * about the x axis from the initial equator to the ecliptic. (The input
		 * is equatorial.)
		 */
		double eps = Obliquity.meanObliquity((JD0 - EphemConstant.J2000) / EphemConstant.JULIAN_DAYS_PER_CENTURY, type);

		x[0] = R[0];
		z = Math.cos(eps) * R[1] + Math.sin(eps) * R[2];
		x[2] = -Math.sin(eps) * R[1] + Math.cos(eps) * R[2];
		x[1] = z;

		/*
		 * Precession in longitude
		 */
		T /= 10.0; /* thousands of years */
		pA = angles[0];

		/*
		 * Node of the moving ecliptic on the JD0 ecliptic.
		 */
		W = angles[1];

		/*
		 * Rotate about z axis to the node.
		 */
		z = W;
		B = Math.cos(z);
		A = Math.sin(z);
		z = B * x[0] + A * x[1];
		x[1] = -A * x[0] + B * x[1];
		x[0] = z;

		/*
		 * Rotate about new x axis by the inclination of the moving ecliptic on
		 * the JD0 ecliptic.
		 */
		z = angles[2];

		B = Math.cos(z);
		A = Math.sin(z);
		z = B * x[1] + A * x[2];
		x[2] = -A * x[1] + B * x[2];
		x[1] = z;

		/*
		 * Rotate about new z axis back from the node.
		 */
		z = -W - pA;
		B = Math.cos(z);
		A = Math.sin(z);
		z = B * x[0] + A * x[1];
		x[1] = -A * x[0] + B * x[1];
		x[0] = z;

		/*
		 * Rotate about x axis to final equator.
		 */
		eps = Obliquity.meanObliquity((JD - EphemConstant.J2000) / EphemConstant.JULIAN_DAYS_PER_CENTURY, type);

		z = Math.cos(eps) * x[1] - Math.sin(eps) * x[2];
		x[2] = Math.sin(eps) * x[1] + Math.cos(eps) * x[2];
		x[1] = z;

		return x;
	}

	/**
	 * Precess rectangular equatorial coordinates to J2000 epoch.
	 * 
	 * @param JD Equinox of the input in Julian day.
	 * @param R Array with x, y, z.
	 * @param type Precession method to apply.
	 * @return Array with corected x, y, z.
	 */
	public static double[] precessToJ2000(double JD, double[] R, Method type)
	{
		double A, B, T, pA, W, z;
		double x[] = new double[3];
		double JD0 = EphemConstant.J2000;

		if (JD == JD0)
			return R;

		if (type == Method.IAU2000)
		{
			return Precession.precessionIAU2000(JD, EphemConstant.J2000, R);
		}
		if (type == Method.CAPITAINE)
		{
			return Precession.precessionCapitaine(JD, EphemConstant.J2000, R);
		}

		double angles[] = getAngles(PRECESS_TO_J2000, JD, type);

		/*
		 * Each precession angle is specified by a polynomial in T = Julian
		 * centuries from JD0. See AA page B18.
		 */
		T = (JD - JD0) / EphemConstant.JULIAN_DAYS_PER_CENTURY;

		/*
		 * Implementation by elementary rotations using expansions. First rotate
		 * about the x axis from the initial equator to the ecliptic. (The input
		 * is equatorial.)
		 */
		double eps = Obliquity.meanObliquity((JD - EphemConstant.J2000) / EphemConstant.JULIAN_DAYS_PER_CENTURY, type);

		x[0] = R[0];
		z = Math.cos(eps) * R[1] + Math.sin(eps) * R[2];
		x[2] = -Math.sin(eps) * R[1] + Math.cos(eps) * R[2];
		x[1] = z;

		/*
		 * Precession in longitude
		 */
		T /= 10.0; /* thousands of years */
		pA = angles[0];

		/*
		 * Node of the moving ecliptic on the JD0 ecliptic.
		 */
		W = angles[1];

		/*
		 * Rotate about z axis to the node.
		 */
		z = W + pA;
		B = Math.cos(z);
		A = Math.sin(z);
		z = B * x[0] + A * x[1];
		x[1] = -A * x[0] + B * x[1];
		x[0] = z;

		/*
		 * Rotate about new x axis by the inclination of the moving ecliptic on
		 * the JD0 ecliptic.
		 */
		z = angles[2];

		B = Math.cos(z);
		A = Math.sin(z);
		z = B * x[1] + A * x[2];
		x[2] = -A * x[1] + B * x[2];
		x[1] = z;

		/*
		 * Rotate about new z axis back from the node.
		 */
		z = -W;
		B = Math.cos(z);
		A = Math.sin(z);
		z = B * x[0] + A * x[1];
		x[1] = -A * x[0] + B * x[1];
		x[0] = z;

		/*
		 * Rotate about x axis to final equator.
		 */
		eps = Obliquity.meanObliquity((JD0 - EphemConstant.J2000) / EphemConstant.JULIAN_DAYS_PER_CENTURY, type);

		z = Math.cos(eps) * x[1] - Math.sin(eps) * x[2];
		x[2] = Math.sin(eps) * x[1] + Math.cos(eps) * x[2];
		x[1] = z;

		return x;
	}

	/**
	 * Precess rectangular equatorial coordinates between two epochs.
	 * 
	 * @param JD0 Equinox of the input in Julian day.
	 * @param JD Equinox of the output in Julian day.
	 * @param R Array with x, y, z.
	 * @param type Precession method to apply.
	 * @return Array with corected x, y, z.
	 */
	public static double[] precess(double JD0, double JD, double[] R, Method type)
	{
		// Transform to J2000
		double to2000[] = Precession.precessToJ2000(JD0, R, type);

		// Transform to output date
		double toJD[] = Precession.precessFromJ2000(JD, to2000, type);

		return toJD;
	}

	/**
	 * Performs precession correction to the direction of the north pole of
	 * rotation, refered to J2000 epoch.
	 * 
	 * @param JD Desired date of the results.
	 * @param ephem {@linkplain EphemElement} with the input values of RA and DEC of north
	 *        pole.
	 * @param type Method to apply. EphemUtils. defined in <CODE>Ephem</CODE>
	 * @return {@linkplain EphemElement} object with the corrected RA and DEC.
	 */
	public static EphemElement precessPoleFromJ2000(double JD, EphemElement ephem, Method type)
	{
		// Transform spherical pole variables to rectangular
		double coord[] = LocationElement.parseLocationElement(new LocationElement(ephem.northPoleRA,
				ephem.northPoleDEC, ephem.distance));

		// Apply precession
		double eq[] = Precession.precessFromJ2000(JD, coord, type);

		// Transform to spherical variables
		LocationElement loc = LocationElement.parseRectangularCoordinates(eq);

		// Set values
		EphemElement ephemOut = (EphemElement) ephem.clone();
		ephemOut.northPoleRA = (float) loc.getLongitude();
		ephemOut.northPoleDEC = (float) loc.getLatitude();

		return ephemOut;
	}

	/**
	 * Performs precession in ecliptic coordinates both in positions and
	 * velocities.
	 * 
	 * @param JD0 Julian day of reference.
	 * @param JD Julian day of the results.
	 * @param coords Array with x, y, z, vx, vy, vz refered to mean ecliptic of
	 *        JD0.
	 * @param type Type of precession.
	 * @return Array with x, y, z, vx, vy, vz refered to mean ecliptic of JD.
	 */
	public static double[] precessPosAndVelInEcliptic(double JD0, double JD, double coords[], Method type)
	{
		/* Ecliptic to equatorial */
		double epsilon = Obliquity.meanObliquity(EphemUtils.toCenturies(JD0), type);
		coords = EphemUtils.rotateX(coords, epsilon);

		/* Precession */
		coords = Precession.precessPosAndVelInEquatorial(JD0, JD, coords, type);

		/* Equatorial to ecliptic */
		epsilon = Obliquity.meanObliquity(EphemUtils.toCenturies(JD), type);
		coords = EphemUtils.rotateX(coords, -epsilon);

		return coords;
	}

	/**
	 * Performs precession in equatorial coordinates both in positions and
	 * velocities.
	 * 
	 * @param JD0 Julian day of reference.
	 * @param JD Julian day of the results.
	 * @param eq Array with x, y, z, vx, vy, vz refered to mean equator of JD0.
	 * @param type Type of precession.
	 * @return Array with x, y, z, vx, vy, vz refered to mean equator of JD.
	 */
	public static double[] precessPosAndVelInEquatorial(double JD0, double JD, double eq[], Method type)
	{
		double pos[] = Precession.precess(JD0, JD, new double[]
		{ eq[0], eq[1], eq[2] }, type);
		double vel[] =
		{ 0.0, 0.0, 0.0 };

		if (!(eq.length < 6))
			vel = Precession.precess(JD0, JD, new double[]
			{ eq[3], eq[4], eq[5] }, type);
		return new double[]
		{ pos[0], pos[1], pos[2], vel[0], vel[1], vel[2] };
	}

	/**
	 * Transforms B1950 coordinates (FK4 system) to J2000, supposing that the
	 * object has no proper motion or is far away. Adequate for B1950 catalogs
	 * of deep sky objects.
	 * 
	 * @param eq Equatorial coordinates FK4 B1950.
	 * @return FK5 J2000 coordinates.
	 */
	public static double[] B1950ToJ2000(double eq[])
	{
		// Pass to spherical
		LocationElement loc = LocationElement.parseRectangularCoordinates(new double[] {eq[0], eq[1], eq[2]});
		LocationElement locV = new LocationElement(0.0, 0.0, 0.0);
		if (eq.length > 3) {
			locV = LocationElement.parseRectangularCoordinates(new double[] {eq[3], eq[4], eq[5]});
		}

		// Create an static star at this position in FK4 B1950
		StarElement star = new StarElement("", loc.getLongitude(), loc.getLatitude(), loc.getRadius(), 
				0.0f, (float) locV.getLongitude(), (float) locV.getLatitude(), (float) locV.getRadius(), 
				StarElement.EQUINOX_B1950, StarElement.FRAME_FK4);

		// Transform coordinates
		star = StarEphem.transform_FK4_B1950_to_FK5_J2000(star);

		// Pass to LocationElement
		LocationElement loc_out = new LocationElement(star.rightAscension, star.declination, loc.getRadius());
		LocationElement loc_outV = new LocationElement(star.properMotionRA, star.properMotionDEC, star.properMotionRadialV);

		// Pass to rectangular coordinates
		double eq_out[] = LocationElement.parseLocationElement(loc_out);
		double eq_outV[] = LocationElement.parseLocationElement(loc_outV);

		return EphemUtils.addDoubleArray(eq_out, eq_outV);
	}



}
