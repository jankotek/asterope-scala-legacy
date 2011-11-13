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

import java.io.*;

/**
 * This is an implementation of the TASS 1.7 theory of the motion of the
 * satellites of Saturn. For reference see A. Vienne & L. Duriez, A&A 297,
 * 588-605 (1995), and A&A 324, 366 (1997). Objects are Mimas, Enceladus,
 * Tethys, Dione, Rhea, Titan, Hyperion, and Iapetus.
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 */
public class SaturnTASS17
{
	
	static final Target[] MOONS = {Target.Mimas, Target.Enceladus,
		Target.Tethys, Target.Dione, Target.Rhea, Target.Titan, Target.Hyperion, Target.Iapetus};
	/**
	 * Implements TASS 1.7 theory. Output vector contains ecliptic coordinates
	 * (AU) of eight satellites, and also the velocity components, in AU/day.
	 * Objects are Mimas, Enceladus, Tethys, Dione, Rhea, Titan, Hyperion, and
	 * Iapetus.
	 * <P>
	 * Positions and velocities of the satellites Mimas, Enceladus, Tethys,
	 * Dione, Rhea, Titan, Hyperion and Iapetus referred to the center of Saturn
	 * and to the mean ecliptic and mean equinox for J2000.0 epoch.
	 * 
	 * @param JD Julian day in TDB.
	 * @param nsat Satellite number, from 1 (Mimas) to 7 (Iapetus).
	 * @param truncate True for truncated version (faster), false for full
	 *        theory. The truncated version seems to have errors below 0.2
	 *        arcseconds.
	 * @return Array (x, y, z, vx, vy, vz).
	 * @ Thrown if the read process fails.
	 */
	public static double[] TASS17_theory(double JD, int nsat, boolean truncate) 
	{
		int icrt = 0;
		if (truncate)
			icrt = 1;
		SaturnTASS17 tass = new SaturnTASS17();
		if (!read_series)
			tass.LECSER(icrt);
		read_series = true;

		// Vector v = new Vector();
		double ELEM[] = new double[]
		{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };

		int IS = nsat;
		// for (int IS = 1; IS<9; IS++)
		// {
		if (IS == 7)
		{
			ELEM = ELEMHYP(JD);
		} else
		{
			double DLO[] = CALCLON(JD);
			ELEM = CALCELEM(JD, IS, DLO);
		}

		double pos[] = EDERED(ELEM, IS);
		// v.add(pos);

		// }

		// return v;
		return pos;
	}

	// Define global parameters of TASS 1.7
	private static final int NTMX = 250;
	private static double SERIES[][][][] = new double[NTMX + 1][4][5][9];
	private static int NTR[][] = new int[6][9];
	private static double AL0[] = new double[9];
	private static double AN0[] = new double[9];
	private static double IKS[][][][] = new double[NTMX + 1][5][9][9];
	private static double AAM[] = new double[10];
	private static double TMAS[] = new double[10];
	private static double AIA;
	private static double OMA;
	private static double GK1;
	private static boolean read_series = false;

	/**
	 * Path to redtass7.txt file
	 */
	private static final String PATH = SaturnTASS17.class.getPackage().getName().replace(".", "/") + "/redtass7.txt";

	private static double[] CALCELEM(double DJ, int IS, double DLO[])
	{
		double ELEM[] = new double[7];

		double T = (DJ - 2444240.0) / 365.25;
		double S = 0.0;
		for (int I = 1; I <= NTR[1][IS]; I++)
		{
			double PHAS = SERIES[I][2][1][IS];
			for (int JK = 1; JK <= 8; JK++)
			{
				PHAS = PHAS + IKS[I][1][IS][JK] * DLO[JK];
			}
			S += SERIES[I][1][1][IS] * Math.cos(PHAS + T * SERIES[I][3][1][IS]);
		}
		ELEM[1] = S;
		S = DLO[IS] + AL0[IS];
		for (int I = NTR[5][IS] + 1; I <= NTR[2][IS]; I++)
		{
			double PHAS = SERIES[I][2][2][IS];
			for (int JK = 1; JK <= 8; JK++)
			{
				PHAS = PHAS + IKS[I][2][IS][JK] * DLO[JK];
			}
			S += SERIES[I][1][2][IS] * Math.sin(PHAS + T * SERIES[I][3][2][IS]);
		}
		S = S + AN0[IS] * T;
		double CS = Math.cos(S);
		double SN = Math.sin(S);
		ELEM[2] = Math.atan2(SN, CS);
		double S1 = 0.0;
		double S2 = 0.0;
		for (int I = 1; I <= NTR[3][IS]; I++)
		{
			double PHAS = SERIES[I][2][3][IS];
			for (int JK = 1; JK <= 8; JK++)
			{
				PHAS = PHAS + IKS[I][3][IS][JK] * DLO[JK];
			}
			S1 += SERIES[I][1][3][IS] * Math.cos(PHAS + T * SERIES[I][3][3][IS]);
			S2 += SERIES[I][1][3][IS] * Math.sin(PHAS + T * SERIES[I][3][3][IS]);
		}
		ELEM[3] = S1;
		ELEM[4] = S2;
		S1 = 0.0;
		S2 = 0.0;
		for (int I = 1; I <= NTR[4][IS]; I++)
		{
			double PHAS = SERIES[I][2][4][IS];
			for (int JK = 1; JK <= 8; JK++)
			{
				PHAS = PHAS + IKS[I][4][IS][JK] * DLO[JK];
			}
			S1 += SERIES[I][1][4][IS] * Math.cos(PHAS + T * SERIES[I][3][4][IS]);
			S2 += SERIES[I][1][4][IS] * Math.sin(PHAS + T * SERIES[I][3][4][IS]);
		}
		ELEM[5] = S1;
		ELEM[6] = S2;

		return ELEM;
	}

	private static double[] CALCLON(double DJ)
	{
		double DLO[] = new double[9];
		double T = (DJ - 2444240.0) / 365.25;
		for (int IS = 1; IS <= 8; IS++)
		{
			if (IS != 7)
			{
				double S = 0.0;
				for (int I = 1; I <= NTR[5][IS]; I++)
				{
					S += SERIES[I][1][2][IS] * Math.sin(SERIES[I][2][2][IS] + T * SERIES[I][3][2][IS]);
				}
				DLO[IS] = S;
			} else
			{
				DLO[IS] = 0.0;
			}
		}

		return DLO;
	}

	private void LECSER(int ICRT) 
	{
		double AM[] = new double[10];
		double TAM[] = new double[10];
		int IK[] = new int[9];
		String line = "";
		int NT = 0, KT = 0, IS = 0, IEQ = 0, ISAUT = 0;

		int initialize = 1;

		// Lets read the file entries
		try
		{
			InputStream is = getClass().getClassLoader().getResourceAsStream(SaturnTASS17.PATH);
			BufferedReader dis = new BufferedReader(new InputStreamReader(is));

			while ((line = dis.readLine()) != null)
			{

				if (initialize == 1)
				{
					double GK = Double.parseDouble(line.trim());
					line = dis.readLine();
					double TAS = Double.parseDouble(line.trim());
					GK1 = Math.pow(GK * 365.25, 2.0) / TAS;
					line = dis.readLine();
					AIA = Double.parseDouble(EphemUtils.getField(1, line, " ", true)) * EphemConstant.DEG_TO_RAD;
					OMA = Double.parseDouble(EphemUtils.getField(2, line, " ", true)) * EphemConstant.DEG_TO_RAD;
					line = dis.readLine();
					for (int i = 1; i <= 9; i++)
					{
						TAM[i] = Double.parseDouble(EphemUtils.getField(i, line, " ", true));
						TMAS[i] = 1.0 / TAM[i];
					}
					line = dis.readLine();
					for (int i = 1; i <= 9; i++)
					{
						AM[i] = Double.parseDouble(EphemUtils.getField(i, line, " ", true));
						AAM[i] = AM[i] * 365.25;
					}

					initialize = 4;
				}

				if (initialize == 3)
				{
					NT = Integer.parseInt(EphemUtils.getField(1, line, " ", true));
					double A1 = Double.parseDouble(EphemUtils.getField(2, line, " ", true));
					double A2 = Double.parseDouble(EphemUtils.getField(3, line, " ", true));
					double A3 = Double.parseDouble(EphemUtils.getField(4, line, " ", true));
					for (int i = 1; i < 9; i++)
					{
						IK[i] = Integer.parseInt(EphemUtils.getField(4 + i, line, " ", true));
					}

					if (NT < 9998)
					{
						if (KT == NTMX)
						{
							System.out.println("Value NTMX not valid, file corrupted");
							break;
						}

						if (ISAUT != 1)
						{
							KT++;
							SERIES[KT][1][IEQ][IS] = A1;
							SERIES[KT][2][IEQ][IS] = A2;
							SERIES[KT][3][IEQ][IS] = A3;
							for (int JS = 1; JS < 9; JS++)
							{
								IKS[KT][IEQ][IS][JS] = IK[JS];
							}
						}
					} else
					{
						if (NT == 9998)
						{
							if (ICRT == 1)
								ISAUT = 1;
							if (IEQ == 2)
								NTR[5][IS] = KT;
						} else
						{
							if (NT == 9999)
							{
								if ((IEQ == 2) && (NTR[5][IS] == 0))
									NTR[5][IS] = KT;
								NTR[IEQ][IS] = KT;
								initialize = 4;
							}
						}

					}
				}

				if (initialize == 2)
				{
					IS = Integer.parseInt(EphemUtils.getField(1, line, " ", true));
					IEQ = Integer.parseInt(EphemUtils.getField(2, line, " ", true));
					if (IS == 7)
						break;
					if (IEQ == 2)
					{
						line = dis.readLine();
						NT = Integer.parseInt(EphemUtils.getField(1, line, " ", true));
						AL0[IS] = Double.parseDouble(EphemUtils.getField(2, line, " ", true));
						AN0[IS] = Double.parseDouble(EphemUtils.getField(3, line, " ", true));
					}
					KT = 0;
					ISAUT = 0;
					initialize = 3;
				}

				if (initialize == 4)
					initialize = 2;

			}

			// Read elements for Hyperion
			line = dis.readLine();
			T0 = Double.parseDouble(line.trim());
			line = dis.readLine();
			AMM7 = Double.parseDouble(line.trim());

			line = dis.readLine();
			NBTP = Integer.parseInt(line.trim());
			line = dis.readLine();
			CSTP = Double.parseDouble(line.trim());
			for (int I = 1; I <= NBTP; I++)
			{
				line = dis.readLine();
				SERP[I] = Double.parseDouble(EphemUtils.getField(1, line, " ", true));
				FAP[I] = Double.parseDouble(EphemUtils.getField(2, line, " ", true));
				FRP[I] = Double.parseDouble(EphemUtils.getField(3, line, " ", true));
			}

			line = dis.readLine();
			NBTQ = Integer.parseInt(line.trim());
			line = dis.readLine();
			CSTQ = Double.parseDouble(line.trim());
			for (int I = 1; I <= NBTQ; I++)
			{
				line = dis.readLine();
				SERQ[I] = Double.parseDouble(EphemUtils.getField(1, line, " ", true));
				FAQ[I] = Double.parseDouble(EphemUtils.getField(2, line, " ", true));
				FRQ[I] = Double.parseDouble(EphemUtils.getField(3, line, " ", true));
			}

			line = dis.readLine();
			NBTZ = Integer.parseInt(line.trim());
			for (int I = 1; I <= NBTZ; I++)
			{
				line = dis.readLine();
				SERZ[I] = Double.parseDouble(EphemUtils.getField(1, line, " ", true));
				FAZ[I] = Double.parseDouble(EphemUtils.getField(2, line, " ", true));
				FRZ[I] = Double.parseDouble(EphemUtils.getField(3, line, " ", true));
			}

			line = dis.readLine();
			NBTZT = Integer.parseInt(line.trim());
			for (int I = 1; I <= NBTZT; I++)
			{
				line = dis.readLine();
				SERZT[I] = Double.parseDouble(EphemUtils.getField(1, line, " ", true));
				FAZT[I] = Double.parseDouble(EphemUtils.getField(2, line, " ", true));
				FRZT[I] = Double.parseDouble(EphemUtils.getField(3, line, " ", true));
			}

			dis.close();

		} catch (IOException e2)
		{
			throw new IOError(e2);
		}

	}

	private static int NTP = 120, NTQ = 240, NTZ = 200, NTZT = 65;
	private static double SERP[] = new double[NTP + 1];
	private static double SERQ[] = new double[NTQ + 1];
	private static double SERZ[] = new double[NTZ + 1];
	private static double SERZT[] = new double[NTZT + 1];
	private static double FAP[] = new double[NTP + 1];
	private static double FAQ[] = new double[NTQ + 1];
	private static double FAZ[] = new double[NTZ + 1];
	private static double FAZT[] = new double[NTZT + 1];
	private static double FRP[] = new double[NTP + 1];
	private static double FRQ[] = new double[NTQ + 1];
	private static double FRZ[] = new double[NTZ + 1];
	private static double FRZT[] = new double[NTZT + 1];
	private static double NBTP, NBTQ, NBTZ, NBTZT, T0, CSTP, CSTQ, AMM7;

	private static double[] EDERED(double ELEM[], int ISAT)
	{
		double EPS = 1.0e-14;
		double xyz[] = new double[4];
		double vxyz[] = new double[4];
		double XYZ2[] = new double[4];
		double VXYZ2[] = new double[4];

		double AMO = AAM[ISAT] * (1.0 + ELEM[1]);
		double RMU = GK1 * (1.0 + TMAS[ISAT]);
		double DGA = Math.pow(RMU / (AMO * AMO), 1.0 / 3.0);
		double RL = ELEM[2];
		double RK = ELEM[3];
		double RH = ELEM[4];
		double FLE = RL - RK * Math.sin(RL) + RH * Math.cos(RL);
		double CORF = EPS;
		do
		{
			double CF = Math.cos(FLE);
			double SF = Math.sin(FLE);
			CORF = (RL - FLE + RK * SF - RH * CF) / (1 - RK * CF - RH * SF);
			FLE = FLE + CORF;
		} while (Math.abs(CORF) >= EPS);

		double CF = Math.cos(FLE);
		double SF = Math.sin(FLE);
		double DLF = -RK * SF + RH * CF;
		double RSAM1 = -RK * CF - RH * SF;
		double ASR = 1.0 / (1.0 + RSAM1);
		double PHI = Math.sqrt(1.0 - RK * RK - RH * RH);
		double PSI = 1.0 / (1.0 + PHI);
		double X1 = DGA * (CF - RK - PSI * RH * DLF);
		double Y1 = DGA * (SF - RH + PSI * RK * DLF);
		double VX1 = AMO * ASR * DGA * (-SF - PSI * RH * RSAM1);
		double VY1 = AMO * ASR * DGA * (CF + PSI * RK * RSAM1);
		double DWHO = 2.0 * Math.sqrt(1.0 - ELEM[6] * ELEM[6] - ELEM[5] * ELEM[5]);
		double RTP = 1.0 - 2.0 * ELEM[6] * ELEM[6];
		double RTQ = 1.0 - 2.0 * ELEM[5] * ELEM[5];
		double RDG = 2.0 * ELEM[6] * ELEM[5];
		XYZ2[1] = X1 * RTP + Y1 * RDG;
		XYZ2[2] = X1 * RDG + Y1 * RTQ;
		XYZ2[3] = (-X1 * ELEM[6] + Y1 * ELEM[5]) * DWHO;
		VXYZ2[1] = VX1 * RTP + VY1 * RDG;
		VXYZ2[2] = VX1 * RDG + VY1 * RTQ;
		VXYZ2[3] = (-VX1 * ELEM[6] + VY1 * ELEM[5]) * DWHO;
		double CI = Math.cos(AIA);
		double SI = Math.sin(AIA);
		double CO = Math.cos(OMA);
		double SO = Math.sin(OMA);

		xyz[1] = CO * XYZ2[1] - SO * CI * XYZ2[2] + SO * SI * XYZ2[3];
		xyz[2] = SO * XYZ2[1] + CO * CI * XYZ2[2] - CO * SI * XYZ2[3];
		xyz[3] = SI * XYZ2[2] + CI * XYZ2[3];
		vxyz[1] = CO * VXYZ2[1] - SO * CI * VXYZ2[2] + SO * SI * VXYZ2[3];
		vxyz[2] = SO * VXYZ2[1] + CO * CI * VXYZ2[2] - CO * SI * VXYZ2[3];
		vxyz[3] = SI * VXYZ2[2] + CI * VXYZ2[3];

		return new double[]
		{ xyz[1], xyz[2], xyz[3], vxyz[1] / 365.25, vxyz[2] / 365.25, vxyz[3] / 365.25 };
	}

	private static double[] ELEMHYP(double DJ)
	{
		double WT;
		double T = DJ - T0;

		double P = CSTP;
		for (int I = 1; I <= NBTP; I++)
		{
			WT = T * FRP[I] + FAP[I];
			P = P + SERP[I] * Math.cos(WT);
		}

		double Q = CSTQ;
		for (int I = 1; I <= NBTQ; I++)
		{
			WT = T * FRQ[I] + FAQ[I];
			Q = Q + SERQ[I] * Math.sin(WT);
		}

		double ZR = 0.0;
		double ZI = 0.0;
		for (int I = 1; I <= NBTZ; I++)
		{
			WT = T * FRZ[I] + FAZ[I];
			ZR = ZR + SERZ[I] * Math.cos(WT);
			ZI = ZI + SERZ[I] * Math.sin(WT);
		}

		double ZTR = 0.0;
		double ZTI = 0.0;
		for (int I = 1; I <= NBTZT; I++)
		{
			WT = T * FRZT[I] + FAZT[I];
			ZTR = ZTR + SERZT[I] * Math.cos(WT);
			ZTI = ZTI + SERZT[I] * Math.sin(WT);
		}

		double VL = EphemUtils.normalizeRadians(AMM7 * T + Q);

		double ELEM[] = new double[]
		{ 0.0, P, VL, ZR, ZI, ZTR, ZTI };

		return ELEM;
	}
}
