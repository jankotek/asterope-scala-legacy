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

import org.asterope.ephem.EphemConstant;
import org.asterope.ephem.EphemUtils;
import org.asterope.ephem.Target;

/**
 * A class to obtain the position of the satellites of Uranus. Objects are
 * Miranda, Ariel, Umbriel, Titania, and Oberon.
 * <P>
 * For reference see: UranusGUST86 - An analytical ephemeris of the Uranian
 * satellites, Laskar J., Jacobson, R., Astron. Astrophys. 188, 212-224 (1987).
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class UranusGUST86
{
	
	public static final Target[] MOONS =  {Target.Miranda, Target.Ariel, Target.Umbriel, Target.Titania, Target.Oberon};
	
	private static double AN[] = new double[6];
	private static double AE[] = new double[6];
	private static double AI[] = new double[6];

	/**
	 * Returns the orbital elements given the output from
	 * the UranusGUST86 theory.
	 * @param GUST86 The output from the UranusGUST86 theory.
	 * @return The 6 orbital elements.
	 */
	public static MoonOrbitalElement getElements(double GUST86[])
	{
		MoonOrbitalElement m = new MoonOrbitalElement();
		m.ascendingNodeLongitude = Math.atan2(GUST86[5], GUST86[4]);
		m.inclination = 2.0 * Math.asin(GUST86[4] / Math.cos(m.ascendingNodeLongitude));
		m.meanLongitude = GUST86[1];
		m.semimajorAxis = GUST86[0] / EphemConstant.AU;
		m.periapsisLongitude = Math.atan2(GUST86[3], GUST86[2]);
		m.eccentricity = GUST86[2] / Math.cos(m.periapsisLongitude);
		m.centralBody = Target.Uranus;
		m.referenceEquinox = EphemConstant.B1950;
		return m;
	}
	/**
	 * UranusGUST86 theory. Output array depends on the value of ICODE.
	 * 
	 * @param TJJ Julian day in TDB.
	 * @param IS Satellite ID, from 1 (Miranda) to 5 (Oberon).
	 * @param ICODE Output format. 1 for eliptic elements B1950, 2 for
	 *        uranocentric vector J1950 refered to Uranus equator, 3 for
	 *        uranocentric equatorial vector J1950.
	 * @return Array with (x, y, z, vx, vy, vz), or with the 6 eliptic elements:<P>
	 *         SEMIMAJOR AXIS (KM).<P>
	 *         LONGITUDE (RD).<P>
	 *         K=E*COS(PI).<P>
	 *         H=E*SIN(PI).<P>
	 *         Q=SIN(I/2)*COS(GOM).<P>
	 *         P=SIN(I/2)*SIN(GOM).<P>
	 *         E : ECCENTRICITY.<P>
	 *         PI : LONGITUDE OF PERIASTRE.<P>
	 *         I : INCLINATION.<P>
	 *         GOM : ASCENDING NODE LONGITUDE.<P>
	 */
	public static double[] GUST86_theory(double TJJ, int IS, int ICODE)
	{
		/*
		 * ---- SATELLITES D'URANUS (LASKAR 1986, LASKAR and JACOBSON, 1987)
		 * -------- VERSION PRELIMINAIRE 0.0 - G. FRANCOU JUIN 88. EN ENTREE :
		 * TJJ DATE JULIENNE TEMPS DYNAMIQUE (REEL DP). IS INDICE DU SATELLITE
		 * (ENTIER). 1 MIRANDA. 2 ARIEL. 3 UMBRIEL. 4 TITANIA. 5 OBERON. LE
		 * PARAMETRE ISAT PEUT ETRE UNE COMBINAISON D'INDICES. EX : ISAT=24
		 * CORRESPOND A ARIEL + TITANIA. ICODE INDICE DU CODE DES CALCULS
		 * (ENTIER). 1 CALCUL DES ELEMENTS ELLIPTIQUES RAPPORTES AU REPERE UME50
		 * (EQUATEUR D'URANUS ET EQUINOXE MOYENS B1950). 2 CALCUL DES X,Y,Z
		 * URANOCENTRIQUES RAPPORTES AU REPERE UME50 (EQUATEUR D'URANUS ET
		 * EQUINOXE MOYENS B1950). 3 CALCUL DES X,Y,Z URANOCENTRIQUES RAPPORTES
		 * AU REPERE EME50 (EQUATEUR CELESTE ET EQUINOXE MOYENS B1950). EN
		 * SORTIE : R(I,J) TABLE DES COORDONNES (REELS DP). I : INDICE
		 * COORDONNEES (1<=I<=6). J : INDICE SATELLITES (1<=J<=5). POUR
		 * ICODE=1 : R(1,J) : DEMI GRAND AXE (KM). R(2,J) : LONGITUDE MOYENNE
		 * (RD). R(3,J) : K=E*COS(PI). R(4,J) : H=E*SIN(PI). R(5,J) :
		 * Q=SIN(I/2)*COS(GOM). R(6,J) : P=SIN(I/2)*SIN(GOM). E : EXCENTRICITE.
		 * PI : LONGITUDE DU PERIASTRE. I : INCLINAISON. GOM : LONGITUDE DU
		 * NOEUD ASCENDANT. POUR ICODE=2 ET ICODE=3 : R(I,J),I=1,3 : POSITIONS
		 * X, Y, Z (AU). R(I,J),I=4,6 : VITESSES X, Y, Z (AU/DAY).
		 */
		double EL[][] = new double[7][6];
		double XU[][] = new double[7][6];
		double XE[][] = new double[7][6];

		double FQN[] = new double[]
		{ 0.0, 4445190.550E-06, 2492952.519E-06, 1516148.111E-06, 721718.509E-06, 466692.120E-06 };
		double FQE[] = new double[]
		{ 0.0, 20.082, 6.217, 2.865, 2.078, 0.386 };
		double FQI[] = new double[]
		{ 0.0, -20.309, -6.288, -2.836, -1.843, -0.259 };
		double PHN[] = new double[]
		{ 0.0, -238051.E-06, 3098046.E-06, 2285402.E-06, 856359.E-06, -915592.E-06 };
		double PHE[] = new double[]
		{ 0.0, 0.611392, 2.408974, 2.067774, 0.735131, 0.426767 };
		double PHI[] = new double[]
		{ 0.0, 5.702313, 0.395757, 0.589326, 1.746237, 4.206896 };
		double GMS[] = new double[]
		{ 0.0, 4.4, 86.1, 84.0, 230.0, 200.0 };
		double RMU[] = new double[6];
		double JSAT[] = new double[6];
		double TRANS[][] = new double[4][4];

		double GMSU = 5794554.5, ALF = 76.60666666666667, DEL = 15.03222222222222, T0 = 2444239.5;

		/*
		 * ---- INITIALISATIONS
		 * --------------------------------------------------
		 */
		double ANJ = 365.25;
		double GMU = GMSU;
		for (int I = 1; I <= 5; I++)
		{
			GMU = GMU - GMS[I];
		}
		for (int I = 1; I <= 5; I++)
		{
			RMU[I] = GMU + GMS[I];
		}

		ALF = ALF * EphemConstant.DEG_TO_RAD;
		DEL = DEL * EphemConstant.DEG_TO_RAD;
		double SA = Math.sin(ALF);
		double CA = Math.cos(ALF);
		double SD = Math.sin(DEL);
		double CD = Math.cos(DEL);
		TRANS[1][1] = SA;
		TRANS[2][1] = -CA;
		TRANS[3][1] = 0.0;
		TRANS[1][2] = CA * SD;
		TRANS[2][2] = SA * SD;
		TRANS[3][2] = -CD;
		TRANS[1][3] = CA * CD;
		TRANS[2][3] = SA * CD;
		TRANS[3][3] = SD;
		/*
		 * ---- TEST DES PARAMETRES
		 * ----------------------------------------------
		 */
		JSAT[IS] = 1;

		/*
		 * ---- CALCUL DES COORDONNEES D'URANUS ET DU TEMPS DE LUMIERE
		 * -----------
		 */
		double T = TJJ - T0;

		/*
		 * ---- CALCUL DES ARGUMENTS
		 * ---------------------------------------------
		 */

		for (int I = 1; I <= 5; I++)
		{
			AN[I] = FQN[I] * T + PHN[I];
			AE[I] = FQE[I] * EphemConstant.DEG_TO_RAD / ANJ * T + PHE[I];
			AI[I] = FQI[I] * EphemConstant.DEG_TO_RAD / ANJ * T + PHI[I];
			AN[I] = EphemUtils.normalizeRadians(AN[I]);
			AE[I] = EphemUtils.normalizeRadians(AE[I]);
			AI[I] = EphemUtils.normalizeRadians(AI[I]);
		}

		/*
		 * ---- <<< DEBUT BOUCLE DE CALCUL SATELLITES D'URANUS >>>
		 * ---------------
		 */

		/*
		 * ---- CALCUL DES ELEMENTS ELLIPTIQUES (REPERE UME50)
		 * -------------------
		 */
		double RES[] = new double[7];
		switch (IS)
		{
		case 1: // Miranda
			RES = MIREL(T);
			break;
		case 2: // Ariel
			RES = ARIEL(T);
			break;
		case 3: // Umbriel
			RES = UMBEL(T);
			break;
		case 4: // Titania
			RES = TITEL(T);
			break;
		case 5: // Oberon
			RES = OBREL(T);
			break;
		}
		double RN = RES[0];
		double RL = RES[1];
		double RK = RES[2];
		double RH = RES[3];
		double RQ = RES[4];
		double RP = RES[5];

		EL[1][IS] = Math.pow((RMU[IS] * EphemConstant.SECONDS_PER_DAY * EphemConstant.SECONDS_PER_DAY / (RN * RN)), (1.0 / 3.0));
		RL = EphemUtils.normalizeRadians(RL);
		if (RL < 0.0)
			RL = RL + 2.0 * Math.PI;
		EL[2][IS] = RL;
		EL[3][IS] = RK;
		EL[4][IS] = RH;
		EL[5][IS] = RQ;
		EL[6][IS] = RP;

		if (ICODE == 1)
			return new double[]
			{ EL[1][IS], EL[2][IS], EL[3][IS], EL[4][IS], EL[5][IS], EL[6][IS] };

		/*
		 * ---- CALCUL DES COORDONNEES X,Y,Z (REPERE UME50)
		 * ----------------------
		 */

		double pXU[] = ELLIPX(EL, RMU, IS);
		XU[1][IS] = pXU[1] / EphemConstant.AU;
		XU[2][IS] = pXU[2] / EphemConstant.AU;
		XU[3][IS] = pXU[3] / EphemConstant.AU;
		XU[4][IS] = pXU[4] * EphemConstant.SECONDS_PER_DAY / EphemConstant.AU;
		XU[5][IS] = pXU[5] * EphemConstant.SECONDS_PER_DAY / EphemConstant.AU;
		XU[6][IS] = pXU[6] * EphemConstant.SECONDS_PER_DAY / EphemConstant.AU;
		if (ICODE == 2)
			return new double[]
			{ XU[1][IS], XU[2][IS], XU[3][IS], XU[4][IS], XU[5][IS], XU[6][IS] };

		/*
		 * ---- CALCUL DES COORDONNEES X,Y,Z (REPERE EME50)
		 * ----------------------
		 */

		for (int IV = 1; IV <= 3; IV++)
		{
			XE[IV][IS] = 0.0;
			XE[IV + 3][IS] = 0.0;
			for (int J = 1; J <= 3; J++)
			{
				XE[IV][IS] = XE[IV][IS] + TRANS[IV][J] * XU[J][IS];
				XE[IV + 3][IS] = XE[IV + 3][IS] + TRANS[IV][J] * XU[J + 3][IS];
			}
		}

		return new double[]
		{ XE[1][IS], XE[2][IS], XE[3][IS], XE[4][IS], XE[5][IS], XE[6][IS] };

	}

	private static double[] MIREL(double T)
	{
		/*
		 * ---- CALCUL DES ELEMENTS ELLIPTIQUES DE MIRANDA (UranusGUST86)
		 * --------------
		 */

		/*
		 * ---- RN => MOYEN MOUVEMENT (RADIAN/JOUR)
		 * ------------------------------
		 */
		double RN = 4443522.67E-06 - 34.92E-06 * Math.cos(AN[1] - 3.0 * AN[2] + 2.0 * AN[3]) + 8.47E-06 * Math
				.cos(2.0 * AN[1] - 6.0 * AN[2] + 4.0 * AN[3]) + 1.31E-06 * Math
				.cos(3.0 * AN[1] - 9.0 * AN[2] + 6.0 * AN[3]) - 52.28E-06 * Math.cos(AN[1] - AN[2]) - 136.65E-06 * Math
				.cos(2.0 * AN[1] - 2.0 * AN[2]);
		/*
		 * ---- RL => LONGITUDE MOYENNE (RADIAN)
		 * ---------------------------------
		 */
		double RL = -238051.58E-06 + 4445190.55E-06 * T + 25472.17E-06 * Math.sin(AN[1] - 3.0 * AN[2] + 2.0 * AN[3]) - 3088.31E-06 * Math
				.sin(2.0 * AN[1] - 6.0 * AN[2] + 4.0 * AN[3]) - 318.10E-06 * Math
				.sin(3.0 * AN[1] - 9.0 * AN[2] + 6.0 * AN[3]) - 37.49E-06 * Math
				.sin(4.0 * AN[1] - 12.0 * AN[2] + 8.0 * AN[3]) - 57.85E-06 * Math.sin(AN[1] - AN[2]) - 62.32E-06 * Math
				.sin(2.0 * AN[1] - 2.0 * AN[2]) - 27.95E-06 * Math.sin(3.0 * AN[1] - 3.0 * AN[2]);
		/*
		 * ---- Z = K + IH
		 * ------------------------------------------------------
		 */
		double RK = 1312.38E-06 * Math.cos(AE[1]) + 71.81E-06 * Math.cos(AE[2]) + 69.77E-06 * Math.cos(AE[3]) + 6.75E-06 * Math
				.cos(AE[4]) + 6.27E-06 * Math.cos(AE[5]) - 123.31E-06 * Math.cos(-AN[1] + 2.0 * AN[2]) + 39.52E-06 * Math
				.cos(-2.0 * AN[1] + 3.0 * AN[2]) + 194.10E-06 * Math.cos(AN[1]);
		// *
		double RH = 1312.38E-06 * Math.sin(AE[1]) + 71.81E-06 * Math.sin(AE[2]) + 69.77E-06 * Math.sin(AE[3]) + 6.75E-06 * Math
				.sin(AE[4]) + 6.27E-06 * Math.sin(AE[5]) - 123.31E-06 * Math.sin(-AN[1] + 2.0 * AN[2]) + 39.52E-06 * Math
				.sin(-2.0 * AN[1] + 3.0 * AN[2]) + 194.10E-06 * Math.sin(AN[1]);
		/*
		 * ---- ZETA = Q + IP
		 * ----------------------------------------------------
		 */
		double RQ = 37871.71E-06 * Math.cos(AI[1]) + 27.01E-06 * Math.cos(AI[2]) + 30.76E-06 * Math.cos(AI[3]) + 12.18E-06 * Math
				.cos(AI[4]) + 5.37E-06 * Math.cos(AI[5]);
		// *
		double RP = 37871.71E-06 * Math.sin(AI[1]) + 27.01E-06 * Math.sin(AI[2]) + 30.76E-06 * Math.sin(AI[3]) + 12.18E-06 * Math
				.sin(AI[4]) + 5.37E-06 * Math.sin(AI[5]);

		return new double[]
		{ RN, RL, RK, RH, RQ, RP };
	}

	private static double[] ARIEL(double T)
	{

		/*
		 * ---- CALCUL DES ELEMENTS ELLIPTIQUES D'ARIEL (UranusGUST86)
		 * -----------------
		 */

		/*
		 * ---- RN => MOYEN MOUVEMENT (RADIAN/JOUR)
		 * ------------------------------
		 */
		double RN = 2492542.57E-06 + 2.55E-06 * Math.cos(AN[1] - 3.0 * AN[2] + 2.0 * AN[3]) - 42.16E-06 * Math
				.cos(AN[2] - AN[3]) - 102.56E-06 * Math.cos(2.0 * AN[2] - 2.0 * AN[3]);
		/*
		 * ---- RL => LONGITUDE MOYENNE (RADIAN)
		 * ---------------------------------
		 */
		double RL = 3098046.41E-06 + 2492952.52E-06 * T - 1860.50E-06 * Math.sin(AN[1] - 3.0 * AN[2] + 2.0 * AN[3]) + 219.99E-06 * Math
				.sin(2.0 * AN[1] - 6.0 * AN[2] + 4.0 * AN[3]) + 23.10E-06 * Math
				.sin(3.0 * AN[1] - 9.0 * AN[2] + 6.0 * AN[3]) + 4.30E-06 * Math
				.sin(4.0 * AN[1] - 12.0 * AN[2] + 8.0 * AN[3]) - 90.11E-06 * Math.sin(AN[2] - AN[3]) - 91.07E-06 * Math
				.sin(2.0 * AN[2] - 2.0 * AN[3]) - 42.75E-06 * Math.sin(3.0 * AN[2] - 3.0 * AN[3]) - 16.49E-06 * Math
				.sin(2.0 * AN[2] - 2.0 * AN[4]);
		/*
		 * ---- Z = K + IH
		 * -------------------------------------------------------
		 */
		double RK = -3.35E-06 * Math.cos(AE[1]) + 1187.63E-06 * Math.cos(AE[2]) + 861.59E-06 * Math.cos(AE[3]) + 71.50E-06 * Math
				.cos(AE[4]) + 55.59E-06 * Math.cos(AE[5]) - 84.60E-06 * Math.cos(-AN[2] + 2.0 * AN[3]) + 91.81E-06 * Math
				.cos(-2.0 * AN[2] + 3.0 * AN[3]) + 20.03E-06 * Math.cos(-AN[2] + 2.0 * AN[4]) + 89.77E-06 * Math
				.cos(AN[2]);
		// *
		double RH = -3.35E-06 * Math.sin(AE[1]) + 1187.63E-06 * Math.sin(AE[2]) + 861.59E-06 * Math.sin(AE[3]) + 71.50E-06 * Math
				.sin(AE[4]) + 55.59E-06 * Math.sin(AE[5]) - 84.60E-06 * Math.sin(-AN[2] + 2.0 * AN[3]) + 91.81E-06 * Math
				.sin(-2.0 * AN[2] + 3.0 * AN[3]) + 20.03E-06 * Math.sin(-AN[2] + 2.0 * AN[4]) + 89.77E-06 * Math
				.sin(AN[2]);
		/*
		 * ---- ZETA = Q + IP
		 * ----------------------------------------------------
		 */
		double RQ = -121.75E-06 * Math.cos(AI[1]) + 358.25E-06 * Math.cos(AI[2]) + 290.08E-06 * Math.cos(AI[3]) + 97.78E-06 * Math
				.cos(AI[4]) + 33.97E-06 * Math.cos(AI[5]);
		// *
		double RP = -121.75E-06 * Math.sin(AI[1]) + 358.25E-06 * Math.sin(AI[2]) + 290.08E-06 * Math.sin(AI[3]) + 97.78E-06 * Math
				.sin(AI[4]) + 33.97E-06 * Math.sin(AI[5]);

		return new double[]
		{ RN, RL, RK, RH, RQ, RP };
	}

	private static double[] UMBEL(double T)
	{

		/*
		 * ---- CALCUL DES ELEMENTS ELLIPTIQUES D'UMBRIEL (UranusGUST86)
		 * ---------------
		 */

		/*
		 * ---- RN => MOYEN MOUVEMENT (RADIAN/JOUR)
		 * ------------------------------
		 */
		double RN = 1515954.90E-06 + 9.74E-06 * Math.cos(AN[3] - 2.0 * AN[4] + AE[3]) - 106.00E-06 * Math
				.cos(AN[2] - AN[3]) + 54.16E-06 * Math.cos(2.0 * AN[2] - 2.0 * AN[3]) - 23.59E-06 * Math
				.cos(AN[3] - AN[4]) - 70.70E-06 * Math.cos(2.0 * AN[3] - 2.0 * AN[4]) - 36.28E-06 * Math
				.cos(3.0 * AN[3] - 3.0 * AN[4]);
		/*
		 * ---- RL => LONGITUDE MOYENNE (RADIAN)
		 * ---------------------------------
		 */
		double RL1 = 2285401.69E-06 + 1516148.11E-06 * T + 660.57E-06 * Math.sin(AN[1] - 3.0 * AN[2] + 2.0 * AN[3]) - 76.51E-06 * Math
				.sin(2.0 * AN[1] - 6.0 * AN[2] + 4.0 * AN[3]) - 8.96E-06 * Math
				.sin(3.0 * AN[1] - 9.0 * AN[2] + 6.0 * AN[3]) - 2.53E-06 * Math
				.sin(4.0 * AN[1] - 12.0 * AN[2] + 8.0 * AN[3]) - 52.91E-06 * Math
				.sin(AN[3] - 4.0 * AN[4] + 3.0 * AN[5]) - 7.34E-06 * Math.sin(AN[3] - 2.0 * AN[4] + AE[5]) - 1.83E-06 * Math
				.sin(AN[3] - 2.0 * AN[4] + AE[4]) + 147.91E-06 * Math.sin(AN[3] - 2.0 * AN[4] + AE[3]);
		double RL2 = -7.77E-06 * Math.sin(AN[3] - 2.0 * AN[4] + AE[2]) + 97.76E-06 * Math.sin(AN[2] - AN[3]) + 73.13E-06 * Math
				.sin(2.0 * AN[2] - 2.0 * AN[3]) + 34.71E-06 * Math.sin(3.0 * AN[2] - 3.0 * AN[3]) + 18.89E-06 * Math
				.sin(4.0 * AN[2] - 4.0 * AN[3]) - 67.89E-06 * Math.sin(AN[3] - AN[4]) - 82.86E-06 * Math
				.sin(2.0 * AN[3] - 2.0 * AN[4]);
		double RL3 = -33.81E-06 * Math.sin(3.0 * AN[3] - 3.0 * AN[4]) - 15.79E-06 * Math.sin(4.0 * AN[3] - 4.0 * AN[4]) - 10.21E-06 * Math
				.sin(AN[3] - AN[5]) - 17.08E-06 * Math.sin(2.0 * AN[3] - 2.0 * AN[5]);
		// *
		double RL = RL1 + RL2 + RL3;
		/*
		 * ---- Z = K + IH
		 * -------------------------------------------------------
		 */
		double RK1 = -0.21E-06 * Math.cos(AE[1]) - 227.95E-06 * Math.cos(AE[2]) + 3904.69E-06 * Math.cos(AE[3]) + 309.17E-06 * Math
				.cos(AE[4]) + 221.92E-06 * Math.cos(AE[5]) + 29.34E-06 * Math.cos(AN[2]) + 26.20E-06 * Math.cos(AN[3]) + 51.19E-06 * Math
				.cos(-AN[2] + 2.0 * AN[3]) - 103.86E-06 * Math.cos(-2.0 * AN[2] + 3.0 * AN[3]) - 27.16E-06 * Math
				.cos(-3.0 * AN[2] + 4.0 * AN[3]);
		double RK2 = -16.22E-06 * Math.cos(AN[4]) + 549.23E-06 * Math.cos(-AN[3] + 2.0 * AN[4]) + 34.70E-06 * Math
				.cos(-2.0 * AN[3] + 3.0 * AN[4]) + 12.81E-06 * Math.cos(-3.0 * AN[3] + 4.0 * AN[4]) + 21.81E-06 * Math
				.cos(-AN[3] + 2.0 * AN[5]) + 46.25E-06 * Math.cos(AN[3]);
		// *
		double RK = RK1 + RK2;
		// *
		double RH1 = -0.21E-06 * Math.sin(AE[1]) - 227.95E-06 * Math.sin(AE[2]) + 3904.69E-06 * Math.sin(AE[3]) + 309.17E-06 * Math
				.sin(AE[4]) + 221.92E-06 * Math.sin(AE[5]) + 29.34E-06 * Math.sin(AN[2]) + 26.20E-06 * Math.sin(AN[3]) + 51.19E-06 * Math
				.sin(-AN[2] + 2.0 * AN[3]) - 103.86E-06 * Math.sin(-2.0 * AN[2] + 3.0 * AN[3]) - 27.16E-06 * Math
				.sin(-3.0 * AN[2] + 4.0 * AN[3]);
		double RH2 = -16.22E-06 * Math.sin(AN[4]) + 549.23E-06 * Math.sin(-AN[3] + 2.0 * AN[4]) + 34.70E-06 * Math
				.sin(-2.0 * AN[3] + 3.0 * AN[4]) + 12.81E-06 * Math.sin(-3.0 * AN[3] + 4.0 * AN[4]) + 21.81E-06 * Math
				.sin(-AN[3] + 2.0 * AN[5]) + 46.25E-06 * Math.sin(AN[3]);
		// *
		double RH = RH1 + RH2;
		/*
		 * ---- ZETA = Q + IP
		 * ----------------------------------------------------
		 */
		double RQ = -10.86E-06 * Math.cos(AI[1]) - 81.51E-06 * Math.cos(AI[2]) + 1113.36E-06 * Math.cos(AI[3]) + 350.14E-06 * Math
				.cos(AI[4]) + 106.50E-06 * Math.cos(AI[5]);
		// *
		double RP = -10.86E-06 * Math.sin(AI[1]) - 81.51E-06 * Math.sin(AI[2]) + 1113.36E-06 * Math.sin(AI[3]) + 350.14E-06 * Math
				.sin(AI[4]) + 106.50E-06 * Math.sin(AI[5]);

		return new double[]
		{ RN, RL, RK, RH, RQ, RP };

	}

	private static double[] TITEL(double T)
	{
		/*
		 * ---- CALCUL DES ELEMENTS ELLIPTIQUES DE TITANIA (UranusGUST86)
		 * --------------
		 */

		/*
		 * ---- RN => MOYEN MOUVEMENT (RADIAN/JOUR)
		 * ------------------------------
		 */
		double RN1 = 721663.16E-06 - 2.64E-06 * Math.cos(AN[3] - 2.0 * AN[4] + AE[3]) - 2.16E-06 * Math
				.cos(2.0 * AN[4] - 3.0 * AN[5] + AE[5]) + 6.45E-06 * Math.cos(2.0 * AN[4] - 3.0 * AN[5] + AE[4]) - 1.11E-06 * Math
				.cos(2.0 * AN[4] - 3.0 * AN[5] + AE[3]);
		double RN2 = -62.23E-06 * Math.cos(AN[2] - AN[4]) - 56.13E-06 * Math.cos(AN[3] - AN[4]) - 39.94E-06 * Math
				.cos(AN[4] - AN[5]) - 91.85E-06 * Math.cos(2.0 * AN[4] - 2.0 * AN[5]) - 58.31E-06 * Math
				.cos(3.0 * AN[4] - 3.0 * AN[5]) - 38.60E-06 * Math.cos(4.0 * AN[4] - 4.0 * AN[5]) - 26.18E-06 * Math
				.cos(5.0 * AN[4] - 5.0 * AN[5]) - 18.06E-06 * Math.cos(6.0 * AN[4] - 6.0 * AN[5]);
		// *
		double RN = RN1 + RN2;
		/*
		 * ---- RL => LONGITUDE MOYENNE (RADIAN)
		 * --------------------------------
		 */
		double RL1 = 856358.79E-06 + 721718.51E-06 * T + 20.61E-06 * Math.sin(AN[3] - 4.0 * AN[4] + 3.0 * AN[5]) - 2.07E-06 * Math
				.sin(AN[3] - 2.0 * AN[4] + AE[5]) - 2.88E-06 * Math.sin(AN[3] - 2.0 * AN[4] + AE[4]) - 40.79E-06 * Math
				.sin(AN[3] - 2.0 * AN[4] + AE[3]) + 2.11E-06 * Math.sin(AN[3] - 2.0 * AN[4] + AE[2]) - 51.83E-06 * Math
				.sin(2.0 * AN[4] - 3.0 * AN[5] + AE[5]) + 159.87E-06 * Math.sin(2.0 * AN[4] - 3.0 * AN[5] + AE[4]);
		double RL2 = -35.05E-06 * Math.sin(2.0 * AN[4] - 3.0 * AN[5] + AE[3]) - 1.56E-06 * Math
				.sin(3.0 * AN[4] - 4.0 * AN[5] + AE[5]) + 40.54E-06 * Math.sin(AN[2] - AN[4]) + 46.17E-06 * Math
				.sin(AN[3] - AN[4]) - 317.76E-06 * Math.sin(AN[4] - AN[5]) - 305.59E-06 * Math
				.sin(2.0 * AN[4] - 2.0 * AN[5]) - 148.36E-06 * Math.sin(3.0 * AN[4] - 3.0 * AN[5]) - 82.92E-06 * Math
				.sin(4.0 * AN[4] - 4.0 * AN[5]);
		double RL3 = -49.98E-06 * Math.sin(5.0 * AN[4] - 5.0 * AN[5]) - 31.56E-06 * Math.sin(6.0 * AN[4] - 6.0 * AN[5]) - 20.56E-06 * Math
				.sin(7.0 * AN[4] - 7.0 * AN[5]) - 13.69E-06 * Math.sin(8.0 * AN[4] - 8.0 * AN[5]);
		// *
		double RL = RL1 + RL2 + RL3;
		/*
		 * ---- Z= K + IH
		 * -------------------------------------------------------
		 */
		double RK1 = -0.02E-06 * Math.cos(AE[1]) - 1.29E-06 * Math.cos(AE[2]) - 324.51E-06 * Math.cos(AE[3]) + 932.81E-06 * Math
				.cos(AE[4]) + 1120.89E-06 * Math.cos(AE[5]) + 33.86E-06 * Math.cos(AN[2]) + 17.46E-06 * Math.cos(AN[4]) + 16.58E-06 * Math
				.cos(-AN[2] + 2.0 * AN[4]) + 28.89E-06 * Math.cos(AN[3]) - 35.86E-06 * Math.cos(-AN[3] + 2.0 * AN[4]);
		double RK2 = -17.86E-06 * Math.cos(AN[4]) - 32.10E-06 * Math.cos(AN[5]) - 177.83E-06 * Math
				.cos(-AN[4] + 2.0 * AN[5]) + 793.43E-06 * Math.cos(-2.0 * AN[4] + 3.0 * AN[5]) + 99.48E-06 * Math
				.cos(-3.0 * AN[4] + 4.0 * AN[5]) + 44.83E-06 * Math.cos(-4.0 * AN[4] + 5.0 * AN[5]) + 25.13E-06 * Math
				.cos(-5.0 * AN[4] + 6.0 * AN[5]) + 15.43E-06 * Math.cos(-6.0 * AN[4] + 7.0 * AN[5]);
		// *
		double RK = RK1 + RK2;
		// *
		double RH1 = -0.02E-06 * Math.sin(AE[1]) - 1.29E-06 * Math.sin(AE[2]) - 324.51E-06 * Math.sin(AE[3]) + 932.81E-06 * Math
				.sin(AE[4]) + 1120.89E-06 * Math.sin(AE[5]) + 33.86E-06 * Math.sin(AN[2]) + 17.46E-06 * Math.sin(AN[4]) + 16.58E-06 * Math
				.sin(-AN[2] + 2.0 * AN[4]) + 28.89E-06 * Math.sin(AN[3]) - 35.86E-06 * Math.sin(-AN[3] + 2.0 * AN[4]);
		double RH2 = -17.86E-06 * Math.sin(AN[4]) - 32.10E-06 * Math.sin(AN[5]) - 177.83E-06 * Math
				.sin(-AN[4] + 2.0 * AN[5]) + 793.43E-06 * Math.sin(-2.0 * AN[4] + 3.0 * AN[5]) + 99.48E-06 * Math
				.sin(-3.0 * AN[4] + 4.0 * AN[5]) + 44.83E-06 * Math.sin(-4.0 * AN[4] + 5.0 * AN[5]) + 25.13E-06 * Math
				.sin(-5.0 * AN[4] + 6.0 * AN[5]) + 15.43E-06 * Math.sin(-6.0 * AN[4] + 7.0 * AN[5]);
		// *
		double RH = RH1 + RH2;
		/*
		 * ---- ZETA= Q + IP
		 * ----------------------------------------------------
		 */
		double RQ = -1.43E-06 * Math.cos(AI[1]) - 1.06E-06 * Math.cos(AI[2]) - 140.13E-06 * Math.cos(AI[3]) + 685.72E-06 * Math
				.cos(AI[4]) + 378.32E-06 * Math.cos(AI[5]);
		// *
		double RP = -1.43E-06 * Math.sin(AI[1]) - 1.06E-06 * Math.sin(AI[2]) - 140.13E-06 * Math.sin(AI[3]) + 685.72E-06 * Math
				.sin(AI[4]) + 378.32E-06 * Math.sin(AI[5]);

		return new double[]
		{ RN, RL, RK, RH, RQ, RP };

	}

	private static double[] OBREL(double T)
	{
		/*
		 * ---- CALCUL DES ELEMENTS ELLIPTIQUES D'OBERON (UranusGUST86)
		 * ---------------
		 */

		/*
		 * ---- RN => MOYEN MOUVEMENT (RADIAN/JOUR)
		 * -----------------------------
		 */
		double RN1 = 466580.54E-06 + 2.08E-06 * Math.cos(2.0 * AN[4] - 3.0 * AN[5] + AE[5]) - 6.22E-06 * Math
				.cos(2.0 * AN[4] - 3.0 * AN[5] + AE[4]) + 1.07E-06 * Math.cos(2.0 * AN[4] - 3.0 * AN[5] + AE[3]) - 43.10E-06 * Math
				.cos(AN[2] - AN[5]);
		double RN2 = -38.94E-06 * Math.cos(AN[3] - AN[5]) - 80.11E-06 * Math.cos(AN[4] - AN[5]) + 59.06E-06 * Math
				.cos(2.0 * AN[4] - 2.0 * AN[5]) + 37.49E-06 * Math.cos(3.0 * AN[4] - 3.0 * AN[5]) + 24.82E-06 * Math
				.cos(4.0 * AN[4] - 4.0 * AN[5]) + 16.84E-06 * Math.cos(5.0 * AN[4] - 5.0 * AN[5]);
		// *
		double RN = RN1 + RN2;
		/*
		 * ---- RL => LONGITUDE MOYENNE (RADIAN)
		 * --------------------------------
		 */
		double RL1 = -915591.80E-06 + 466692.12E-06 * T - 7.82E-06 * Math.sin(AN[3] - 4.0 * AN[4] + 3.0 * AN[5]) + 51.29E-06 * Math
				.sin(2.0 * AN[4] - 3.0 * AN[5] + AE[5]) - 158.24E-06 * Math.sin(2.0 * AN[4] - 3.0 * AN[5] + AE[4]) + 34.51E-06 * Math
				.sin(2.0 * AN[4] - 3.0 * AN[5] + AE[3]) + 47.51E-06 * Math.sin(AN[2] - AN[5]) + 38.96E-06 * Math
				.sin(AN[3] - AN[5]) + 359.73E-06 * Math.sin(AN[4] - AN[5]);
		double RL2 = 282.78E-06 * Math.sin(2.0 * AN[4] - 2.0 * AN[5]) + 138.60E-06 * Math
				.sin(3.0 * AN[4] - 3.0 * AN[5]) + 78.03E-06 * Math.sin(4.0 * AN[4] - 4.0 * AN[5]) + 47.29E-06 * Math
				.sin(5.0 * AN[4] - 5.0 * AN[5]) + 30.00E-06 * Math.sin(6.0 * AN[4] - 6.0 * AN[5]) + 19.62E-06 * Math
				.sin(7.0 * AN[4] - 7.0 * AN[5]) + 13.11E-06 * Math.sin(8.0 * AN[4] - 8.0 * AN[5]);
		// *
		double RL = RL1 + RL2;
		/*
		 * ---- Z = K + IH
		 * ------------------------------------------------------
		 */
		double RK1 = 0.00E-06 * Math.cos(AE[1]) - 0.35E-06 * Math.cos(AE[2]) + 74.53E-06 * Math.cos(AE[3]) - 758.68E-06 * Math
				.cos(AE[4]) + 1397.34E-06 * Math.cos(AE[5]) + 39.00E-06 * Math.cos(AN[2]) + 17.66E-06 * Math
				.cos(-AN[2] + 2.0 * AN[5]);
		double RK2 = 32.42E-06 * Math.cos(AN[3]) + 79.75E-06 * Math.cos(AN[4]) + 75.66E-06 * Math.cos(AN[5]) + 134.04E-06 * Math
				.cos(-AN[4] + 2.0 * AN[5]) - 987.26E-06 * Math.cos(-2.0 * AN[4] + 3.0 * AN[5]) - 126.09E-06 * Math
				.cos(-3.0 * AN[4] + 4.0 * AN[5]) - 57.42E-06 * Math.cos(-4.0 * AN[4] + 5.0 * AN[5]) - 32.41E-06 * Math
				.cos(-5.0 * AN[4] + 6.0 * AN[5]) - 19.99E-06 * Math.cos(-6.0 * AN[4] + 7.0 * AN[5]) - 12.94E-06 * Math
				.cos(-7.0 * AN[4] + 8.0 * AN[5]);
		// *
		double RK = RK1 + RK2;
		// *
		double RH1 = 0.00E-06 * Math.sin(AE[1]) - 0.35E-06 * Math.sin(AE[2]) + 74.53E-06 * Math.sin(AE[3]) - 758.68E-06 * Math
				.sin(AE[4]) + 1397.34E-06 * Math.sin(AE[5]) + 39.00E-06 * Math.sin(AN[2]) + 17.66E-06 * Math
				.sin(-AN[2] + 2.0 * AN[5]);
		double RH2 = 32.42E-06 * Math.sin(AN[3]) + 79.75E-06 * Math.sin(AN[4]) + 75.66E-06 * Math.sin(AN[5]) + 134.04E-06 * Math
				.sin(-AN[4] + 2.0 * AN[5]) - 987.26E-06 * Math.sin(-2.0 * AN[4] + 3.0 * AN[5]) - 126.09E-06 * Math
				.sin(-3.0 * AN[4] + 4.0 * AN[5]) - 57.42E-06 * Math.sin(-4.0 * AN[4] + 5.0 * AN[5]) - 32.41E-06 * Math
				.sin(-5.0 * AN[4] + 6.0 * AN[5]) - 19.99E-06 * Math.sin(-6.0 * AN[4] + 7.0 * AN[5]) - 12.94E-06 * Math
				.sin(-7.0 * AN[4] + 8.0 * AN[5]);
		// *
		double RH = RH1 + RH2;
		/*
		 * ---- ZETA = Q + IP
		 * ---------------------------------------------------
		 */
		double RQ = -0.44E-06 * Math.cos(AI[1]) - 0.31E-06 * Math.cos(AI[2]) + 36.89E-06 * Math.cos(AI[3]) - 596.33E-06 * Math
				.cos(AI[4]) + 451.69E-06 * Math.cos(AI[5]);
		// *
		double RP = -0.44E-06 * Math.sin(AI[1]) - 0.31E-06 * Math.sin(AI[2]) + 36.89E-06 * Math.sin(AI[3]) - 596.33E-06 * Math
				.sin(AI[4]) + 451.69E-06 * Math.sin(AI[5]);

		return new double[]
		{ RN, RL, RK, RH, RQ, RP };

	}

	/*
	 * ---- ELLIPX 1.1 18 MARS 1986 J. LASKAR -----------------------------
	 * CALCUL DES COORDONNEES RECTANGULAIRES (POSITIONS ET VITESSES) ET DE LEURS
	 * DERIVEES PARTIELLES PAR RAPPORT AUX ELEMENTS ELLIPTIQUES A PARTIR DES
	 * ELEMENTS ELLIPTIQUES. ELL[6] : ELEMENTS ELLIPTIQUES A: DEMI-GRAND AXE L:
	 * LONGITUDE MOYENNE K: EXC*Math.cos(LONG NOEUD+ ARG PERI) H:
	 * EXC*Math.sin(LONG NOEUD+ ARG PERI) Q: SIN(I/2)*Math.cos(LONG NOEUD) P:
	 * SIN(I/2)*Math.sin(LONG NOEUD) RMU : CONSTANTE DE GRAVITATION DU PROBLEME
	 * DE DEUX CORPS RMU = G*M1*(1+M2/M1) M1 MASSE CENTRALE M2 MASSE DU CORPS
	 * CONSIDERE XYZ[6] : (1:3) POSITIONS ET (4:6) VITESSES DXYZ(6,7) : DERIVEES
	 * PARTIELLES DXYZ(I,J)=DRON(XYZ(I))/DRON(ELL(J))
	 * DXYZ(I,7)=DRON(XYZ(I))/DRON(RMU) IDER : 0 CALCUL DES XYZ SEULEMENT 1
	 * CALCUL DES DERIVEES PARTIELLES AUSSI IPRT : IMPRESSIONS SI IPRT.GE.1
	 * SUBROUTINE UTILISEE : KEPLKH ---- DECLARATIONS
	 * -----------------------------------------------------
	 */
	private static double[] ELLIPX(double[][] ELL, double[] RMU, int IS)
	{
		double ROT[][] = new double[4][3];
		double TX1[] = new double[3];
		double TX1T[] = new double[3];

		/*
		 * ---- ELEMENTS UTILES
		 * --------------------------------------------------
		 */
		double RA = ELL[1][IS];
		double RL = ELL[2][IS];
		double RK = ELL[3][IS];
		double RH = ELL[4][IS];
		double RQ = ELL[5][IS];
		double RP = ELL[6][IS];
		double RN = Math.sqrt(RMU[IS] / Math.pow(RA, 3.0));
		double PHI = Math.sqrt(1.0 - RK * RK - RH * RH);
		double RKI = Math.sqrt(1.0 - RQ * RQ - RP * RP);
		double PSI = 1.0 / (1.0 + PHI);
		/*
		 * ---- MATRICE DE ROTATION
		 * ----------------------------------------------
		 */
		ROT[1][1] = 1.0 - 2.0 * RP * RP;
		ROT[1][2] = 2.0 * RP * RQ;
		ROT[2][1] = 2.0 * RP * RQ;
		ROT[2][2] = 1.0 - 2.0 * RQ * RQ;
		ROT[3][1] = -2.0 * RP * RKI;
		ROT[3][2] = 2.0 * RQ * RKI;
		/*
		 * ---- CALCUL DE LA LONGITUDE EXCENTRIQUE F
		 * ----------------------------- ---- F = ANOMALIE EXCENTRIQUE E +
		 * LONGITUDE DU PERIAPSE OMEGAPI -------
		 */
		double F = KEPLKH(RL, RK, RH);
		double SF = Math.sin(F);
		double CF = Math.cos(F);
		double RLMF = -RK * SF + RH * CF;
		double UMRSA = RK * CF + RH * SF;
		double ASR = 1.0 / (1.0 - UMRSA);
		double RNA2SR = RN * RA * ASR;
		/*
		 * ---- CALCUL DE TX1 ET TX1T
		 * --------------------------------------------
		 */
		TX1[1] = RA * (CF - PSI * RH * RLMF - RK);
		TX1[2] = RA * (SF + PSI * RK * RLMF - RH);
		TX1T[1] = RNA2SR * (-SF + PSI * RH * UMRSA);
		TX1T[2] = RNA2SR * (CF - PSI * RK * UMRSA);
		/*
		 * ---- CALCUL DE XYZ
		 * ----------------------------------------------------
		 */
		double out[] = new double[]
		{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
		for (int I = 1; I <= 3; I++)
		{
			for (int J = 1; J <= 2; J++)
			{
				out[I] = out[I] + ROT[I][J] * TX1[J];
				out[I + 3] = out[I + 3] + ROT[I][J] * TX1T[J];
			}
		}

		// PASS FROM FK4 TO FK5
		double out_FK5[] = FK4_TO_FK5(out);

		return out_FK5;
	}

	// FK4 TO FK5 for equatorial mean B1950 coordinates: Frieck rotation by
	// 0.525" in z axis
	private static double[] FK4_TO_FK5(double v[])
	{
		double IROT = 1.0;
		double SDRAD = 0.4848136811095360E-5;
		double AZ = -0.525 * IROT * SDRAD;
		double SA = Math.sin(AZ);
		double CA = Math.cos(AZ);
		double V1 = v[1] * CA + v[2] * SA;
		double V2 = v[2] * CA - v[1] * SA;
		double V3 = v[3];

		double V4 = 0.0, V5 = 0.0, V6 = 0.0;
		if (v.length > 4)
		{
			V4 = v[4] * CA + v[5] * SA;
			V5 = v[5] * CA - v[4] * SA;
			V6 = v[6];
		}

		return new double[]
		{ 0.0, V1, V2, V3, V4, V5, V6 };

	}

	private static double KEPLKH(double RL, double RK, double RH)
	{
		/*
		 * ---- KEPLKH 1.0 12 DECEMBRE 1985 J. LASKAR --------------------------
		 * RESOLUTION DE L'EQUATION DE KEPLER EN VARIABLES LONGITUDES, K, H
		 * -----------------------------------------------------------------------
		 */
		if (RL == 0.0)
			return 0.0;

		int ITMAX = 20, K = 0;
		double EPS = 1.0E-16;
		double F0 = RL, F = 0.0;
		double E0 = Math.abs(RL), E = 0.0;
		for (int IT = 1; IT <= ITMAX; IT++)
		{
			K = 0;
			double SF = Math.sin(F0);
			double CF = Math.cos(F0);
			double FF0 = F0 - RK * SF + RH * CF - RL;
			double FPF0 = 1.0 - RK * CF - RH * SF;
			double SDIR = FF0 / FPF0;

			do
			{
				F = F0 - SDIR * Math.pow(0.50, K);
				E = Math.abs(F - F0);
				K++;
			} while (E > E0);

			K--;

			if (K == 0 && E <= EPS && FF0 <= EPS)
				return F;

			F0 = F;
			E0 = E;
		}

		return F;
	}
}
