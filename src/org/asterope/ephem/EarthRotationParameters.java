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

import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * A class to obtain the current Earth Orientation Parameters as defined by the
 * IERS.
 * 
 * @author T. Alonso Albi - OAN (Spain)
 * @version 1.0
 * @see Nutation
 */
public class EarthRotationParameters
{
	
	private static final Logger LOGGER = Logger.getLogger(EarthRotationParameters.class.getName());
//	/**
//	 * Path to the file of Earth Rotation Parameters, iau1980 version.
//	 */
//	public static final String PATH_TO_FILE_IAU1980 = FileIO.DATA_EOP_DIRECTORY + "IERS_EOP_iau1980.txt";

	/**
	 * Path to the file of Earth Rotation Parameters, iau2000 version.
	 */
	public static final String PATH_TO_FILE_IAU2000 = 
		EarthRotationParameters.class.getPackage().getName().replace(".", "/") 
		+ "/IERS_EOP_iau2000.txt";

	/**
	 * Path to the file of Earth Rotation Parameters.
	 */
	public static String pathToFile = PATH_TO_FILE_IAU2000;

	/**
	 * True for reading external files.
	 */
	public static boolean useExternalFile = false;

	/**
	 * Sets whether to apply EOP corrections or not. Default value is true.
	 */
	public static boolean applyEOPParameters = true;

	/**
	 * Sets whether to correct ephemeris for EOP effects or not. Default value
	 * is true.
	 * 
	 * @param eop True or false, as desired.
	 */
	public static void setEOPCorrectionToBeApplied(boolean eop)
	{
		applyEOPParameters = eop;
	}

	/**
	 * Sets the path to the file of Earth rotation parameters.
	 * 
	 * @param path Full path including extension.
	 */
	public static void setPath(String path)
	{
		pathToFile = path;
	}

	/**
	 * Sets wether to use or not an external file for IERS Earth Orientation
	 * Parameters.
	 * 
	 * @param external True for using the external file.
	 */
	public static void setExternalFile(boolean external)
	{
		useExternalFile = external;
	}

	/**
	 * Reads the example file with the Earth Orientation Parameters, formatted
	 * in the standard way, stablished by IERS.
	 * <P>
	 * An example of IERS format is:
	 * <P>
	 * 
	 * <pre>
     * Date      MJD      x          y        UT1-UTC       LOD         dX        dY        x Err     y Err   UT1-UTC Err  Lod Err     dY Err       dY Err  
     *                    &quot;          &quot;           s           s          &quot;         &quot;           &quot;          &quot;          s         s            &quot;           &quot;
     * (0h UTC)
	 * 1962   1   1  37665  -0.012700   0.213000   0.0326338   0.0017230   0.000000   0.000000   0.030000   0.030000  0.0020000  0.0014000    0.012000    0.002000
	 * 1962  JAN   1  37665-0.012700 0.213000 0.0326338   0.0017230   0.065037 0.000436
	 * <P>
	 * </pre>
	 * 
	 * @param JD Julian day in UTC.
	 * @return String with the values, in IERS format, or empty string if input
	 *         time is not applicable.
	 */
	private static String obtainEOPRecord(double JD)
	{
		// Obtain nearest midnight
		AstroDate astro = new AstroDate(0.5 + (int) JD);

		// Calculate record ID
//		String months = "JANFEBMARAPRMAYJUNJULAUGSEPOCTNOVDEC";
		int month = astro.getMonth();
		int day = astro.getDay();
		String monthV = "  "+month;
		if (month < 10) monthV = " " + monthV;
		String dayV = "  "+day;
		if (day < 10) dayV = " " + dayV;
		String record_to_find = astro.getYear() + monthV + dayV+"  ";
//		String record_to_find = astro.getYear() + "  " + months.substring((month - 1) * 3, month * 3) + "  ";
//		if (day < 10) record_to_find += " ";
//		record_to_find += day;

		// Define necesary variables
		String file_line = "";

		// Connect to the file
		String out = "";
		ArrayList<String> v = EphemUtils.readResource(pathToFile);
		int index = 0;
		while (index < v.size() && out.equals(""))
		{
			file_line = v.get(index);
			index ++;

			// Obtain object
			int a = file_line.indexOf(record_to_find);
			if (0 <= a)
				out = file_line;
			file_line = file_line.trim();
			if (file_line.length() > record_to_find.length()) {
				if (file_line.substring(0, record_to_find.length()).equals(record_to_find))
					break;
			}
		}

		return out;
	}

	/**
	 * Reads an external file with the Earth Orientation Parameters, formatted
	 * in the standard way, stablished by IERS.
	 * <P>
	 * An example of IERS format is:
	 * <P>
	 * 
	 * <pre>
	 *   Date         MJD     x        y       UT1-UTC       LOD       dPsi     dEpsilon
	 *                        &quot;        &quot;         s            s          &quot;         &quot;
	 *   1962  JAN   1  37665-0.012700 0.213000 0.0326338   0.0017230   0.065037 0.000436
	 * <P>
	 * </pre>
	 * 
	 * @param JD Julian day in UTC.
	 * @return String with the values, in IERS format, or empty string if input
	 *         time is not applicable.
	 */
	private static String obtainEOPRecordFromExternalFile(double JD)
	{
		// Obtain nearest midnight
		AstroDate astro = new AstroDate(0.5 + (int) JD);

		// Calculate record ID
//		String months = "JANFEBMARAPRMAYJUNJULAUGSEPOCTNOVDEC";
		int month = astro.getMonth();
		int day = astro.getDay();
		String monthV = "  "+month;
		if (month < 10) monthV = " " + monthV;
		String dayV = "  "+day;
		if (day < 10) dayV = " " + dayV;
		String record_to_find = astro.getYear() + monthV + dayV+"  ";
//		String record_to_find = astro.getYear() + "  " + months.substring((month - 1) * 3, month * 3) + "  ";
//		if (day < 10) record_to_find += " ";
//		record_to_find += day;

		// Define necesary variables
		String file_line = "";

		// Connect to the file
		String out = "";
		try
		{
			URLConnection Connection = (new URL("file:" + pathToFile)).openConnection();
			InputStream is = Connection.getInputStream();
			BufferedReader dis = new BufferedReader(new InputStreamReader(is));

			while ((file_line = dis.readLine()) != null && out.equals(""))
			{

				// Obtain object
				int a = file_line.indexOf(record_to_find);
				if (0 <= a)
					out = file_line;
				file_line = file_line.trim();
				if (file_line.length() > record_to_find.length()) {
					if (file_line.substring(0, record_to_find.length()).equals(record_to_find))
						break;
				}
			}

			// Close file
			dis.close();

		} catch (IOException e1){
			throw new IOError(e1);
		}

		return out;

	}

	/**
	 * Obtains Earth Orientation Parameters. Results set to static variables.
	 * The results are set to 0 in case of inaceptable input date (prior to
	 * January, 1, 1962, or beyond the last date in the IERS EOP input file). If
	 * this correction is not desired, then UT1-UTC is calculated, but not the
	 * pole offsets, which will remain to 0.0.
	 * <P>
	 * Values for IAU2000 model are automatically transformed from dx, dy
	 * celestial pole offsets to dpsi, deps. For external files, this
	 * transformation must be manually done by calling dxdy_to_dpsideps method.
	 * <P>
	 * There are two methods: IAU1980 and IAU2000. The first one can be applied
	 * when using Laskar 1986 precession method. The second is for IAU2000 or
	 * Capitaine et al. 2003 precession methods. This distinction is
	 * automatically performed. For other precession methods this correction is
	 * not applied. The difference between Laskar precession plus this
	 * correction and IAU2000 model is about 10 milliarcseconds for current
	 * dates. Without this correction, errors amount to about 50 milliarcseconds
	 * for Laskar method.
	 * 
	 * @param T Julian centuries from J2000.
	 * @param method Method to apply for precession.
	 */
	public static void obtainEOP(double T, Precession.Method method)
	{
		double JD = EphemConstant.J2000 + T * EphemConstant.JULIAN_DAYS_PER_CENTURY;

		// Don't repeat calculations for similar dates
		if (Math.abs(JD - EarthRotationParameters.lastJD) < 0.5 && method == EarthRotationParameters.lastMethod)
			return;

		clearEOP();

		String record = "";
		if (useExternalFile)
		{
			record = EarthRotationParameters.obtainEOPRecordFromExternalFile(JD);
		} else
		{
            //TODO find what is difference between IAU2000 and IAU1980 and if first one can be used with ELP2000
//			if (method ==  Precession.Method.IAU2000 || method ==  Precession.Method.CAPITAINE)
				setPath(PATH_TO_FILE_IAU2000);

//			else{
//				throw new UnsupportedOperationException("IAU1980 file is not included");
//				//setPath(PATH_TO_FILE_IAU1980);
//			}
			record = EarthRotationParameters.obtainEOPRecord(JD);
		}

		double EOP[] = new double[]
		{ 0.0, 0.0, 0.0 };
		if (!record.equals(""))
		{
			EOP[0] = Double.parseDouble(EphemUtils.getField(7, record, " ", true));
			EOP[1] = Double.parseDouble(EphemUtils.getField(9, record, " ", true));
			EOP[2] = Double.parseDouble(EphemUtils.getField(10, record, " ", true));
			
//			EOP[0] = Double.parseDouble(record.substring(40, 51).trim()); // UT1-UTC
//			EOP[1] = Double.parseDouble(record.substring(64, 73).trim()); // dPsi
//			EOP[2] = Double.parseDouble(record.substring(73).trim()); // dEpsilon
		} else
		{
			LOGGER.log(Level.WARNING, "some data are not avaliable" );
					//.addWarning(Translate.translate(FileIO.getField(1, Translate.TRANSLATE[Translate.JPARSEC_UT1_UTC_NOT_AVAILABLE], Translate.SEPARATOR, true)));
		}

		EarthRotationParameters.UT1minusUTC = EOP[0];
		EarthRotationParameters.lastJD = JD;
		EarthRotationParameters.lastMethod = method;

		if (!applyEOPParameters)
			return;


		if ((method == Precession.Method.IAU2000 || method == Precession.Method.CAPITAINE) && !useExternalFile)
		{
			double EOP_2000[] = dxdyTOdpsideps(EOP[1], EOP[2], JD);
			EarthRotationParameters.dPsi = EOP_2000[0];
			EarthRotationParameters.dEpsilon = EOP_2000[1];
		} else
		{
			EarthRotationParameters.dPsi = EOP[1];
			EarthRotationParameters.dEpsilon = EOP[2];
		}
	}

	/**
	 * Transforms dx and dy values into dpsi and deps. Capitaine et al. formulae
	 * for obliquity and precession is used.
	 * 
	 * @param DX Celestial pole x offset in arcseconds.
	 * @param DY Celestial pole y offset in arcseconds.
	 * @param TJD Julian day.
	 * @return dpsi and deps celestial pole offsets in arcseconds.
	 */
	public static double[] dxdyTOdpsideps(double DX, double DY, double TJD)
	{
		double T = (TJD - EphemConstant.J2000) / EphemConstant.JULIAN_DAYS_PER_CENTURY;

		// COMPUTE SINE OF MEAN OBLIQUITY OF DATE
		double SINE = Math.sin(Obliquity.meanObliquity(T, Precession.Method.CAPITAINE));

		/**
		 * THE FOLLOWING ALGORITHM, TO TRANSFORM DX AND DY TO DELTA-DELTA-PSI
		 * AND DELTA-DELTA-EPSILON, IS FROM G. KAPLAN (2003), USNO/AA TECHNICAL
		 * NOTE 2003-03, EQS. (7)-(9). TRIVIAL MODEL OF POLE TRAJECTORY IN GCRS
		 * ALLOWS COMPUTATION OF DZ
		 */
		double X = (2004.19 * T) / EphemConstant.RAD_TO_ARCSEC;
		double DZ = -(X + 0.5 * Math.pow(X, 3.0)) * DX;

		// FORM POLE OFFSET VECTOR (OBSERVED - MODELED) IN GCRS 
		double DP1[] = new double[]
		{ DX / EphemConstant.RAD_TO_ARCSEC, DY / EphemConstant.RAD_TO_ARCSEC, DZ / EphemConstant.RAD_TO_ARCSEC };

		// PRECESS POLE OFFSET VECTOR TO MEAN EQUATOR AND EQUINOX OF DATE
		double DP2[] = EphemUtils.toJ2000Frame(DP1);
		double DP3[] = Precession.precessFromJ2000(TJD, DP2, Precession.Method.CAPITAINE);

		// COMPUTE DELTA-DELTA-PSI AND DELTA-DELTA-EPSILON IN ARCSECONDS
		double PSICOR = (DP3[0] / SINE) * EphemConstant.RAD_TO_ARCSEC;
		double EPSCOR = (DP3[1]) * EphemConstant.RAD_TO_ARCSEC;

		return new double[]
		{ PSICOR, EPSCOR };
	}

	/**
	 * Resets all EOP parameters to 0. It is not necessary to call this method
	 * even when changing between precession methods.
	 */
	public static void clearEOP()
	{
		EarthRotationParameters.UT1minusUTC = 0.0;
		EarthRotationParameters.lastJD = 0.0;
		EarthRotationParameters.dPsi = 0.0;
		EarthRotationParameters.dEpsilon = 0.0;
		EarthRotationParameters.lastMethod = null;
	}

	/**
	 * Hols UT1-UTC in seconds.
	 */
	public static double UT1minusUTC = 0.0;

	/**
	 * Holds dPsi in arcseconds.
	 */
	public static double dPsi = 0.0;

	/**
	 * Holds dEpsilon in arcseconds.
	 */
	public static double dEpsilon = 0.0;

	/**
	 * Holds last calculation time.
	 */
	public static double lastJD = 0.0;

	/**
	 * Holds last calculation time.
	 */
	public static Precession.Method lastMethod = null;
	

}
