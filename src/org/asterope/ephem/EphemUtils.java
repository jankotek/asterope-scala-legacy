package org.asterope.ephem;

import java.io.*;
import java.text.DecimalFormat;
import java.util.ArrayList;

import static org.asterope.ephem.EphemConstant.*;

public final class EphemUtils {


	/**
	 * Module operation in arcseconds.
	 * 
	 * @param x Value in arcseconds.
	 * @return module.
	 */
	public static double mod3600(double x)
	{
		x = x - 1296000. * Math.floor(x / 1296000.);
		return x;
	}

	/**
	 * Multiply the components of a vector by a constant.
	 * 
	 * @param v Array (x, y, z) or (x, y, z, vx, vy, vz).
	 * @param val Numerical value.
	 * @return (x * val, y * val, z * val, ...).
	 */
	public static double[] scalarProduct(double v[], double val)
	{
		double v_out[] = new double[v.length];
		for (int i = 0; i < v.length; i++)
		{
			v_out[i] = v[i] * val;
		}
		return v_out;
	}

	/**
	 * Scalar product of two vectors.
	 * 
	 * @param v1 Array (x, y, z) or with arbitrary number of components.
	 * @param v2 Array (x, y, z) or with arbitrary number of components.
	 * @return Scalar product.
	 */
	public static double scalarProduct(double v1[], double v2[])
	{
		double out = 0.0;
		for (int i = 0; i < v1.length; i++)
		{
			out += v1[i] * v2[i];
		}
	
		return out;
	}
	
	/**
	 * Sums two vectors.
	 * 
	 * @param v1 Array (x, y, z) or (x, y, z, vx, vy, vz).
	 * @param v2 Array (x, y, z) or (x, y, z, vx, vy, vz).
	 * @return Sum of both vectors.
	 */
	public static double[] sumVectors(double v1[], double[] v2)
	{
		int n1 = v1.length;
		int n2 = v2.length;
		int n = n1;
		if (n2 < n1)
			n = n2;
		double v3[] = new double[n];
		for (int i = 0; i < n; i++)
		{
			v3[i] = v1[i] + v2[i];
		}
		return v3;
	}

	
    /**
     * Appends a double array to another array.
     * @param array1 First array.
     * @param array2 Second array to be append at the end of the first one.
     * @return An array.
     */
    public static double[] addDoubleArray(double[] array1, double[] array2)
    {
    	if (array1 == null) return array2;
    	if (array2 == null) return array1;
    	double out[] = new double[array1.length + array2.length];
    	System.arraycopy(array1, 0, out, 0, array1.length);
    	System.arraycopy(array2, 0, out, array1.length, array2.length);
    	return out;
    }
	/**
	 * Rotate a set of rectangular coordinates from X axis. Used for rotating
	 * from ecliptic to equatorial or back.
	 * 
	 * @param coords Array (x, y, z) or (x, y, z, vx, vy, vz).
	 * @param angle Rotation angle in radians.
	 * @return Rotated vector.
	 */
	public static double[] rotateX(double coords[], double angle)
	{
		double tmp = coords[1] * Math.cos(angle) - coords[2] * Math.sin(angle);
		coords[2] = coords[1] * Math.sin(angle) + coords[2] * Math.cos(angle);
		coords[1] = tmp;
	
		// Treat velocities if they are present
		if (coords.length > 3)
		{
			tmp = coords[4] * Math.cos(angle) - coords[5] * Math.sin(angle);
			coords[5] = coords[4] * Math.sin(angle) + coords[5] * Math.cos(angle);
			coords[4] = tmp;
		}
	
		return coords;
	}

	/**
	 * Substracts one vector from another one.
	 * 
	 * @param v1 Array (x, y, z) or (x, y, z, vx, vy, vz).
	 * @param v2 Array (x, y, z) or (x, y, z, vx, vy, vz).
	 * @return v1 - v2.
	 */
	public static double[] substractVector(double v1[], double[] v2)
	{
		int n1 = v1.length;
		int n2 = v2.length;
		int n = n1;
		if (n2 < n1)
			n = n2;
		double v3[] = new double[n];
		for (int i = 0; i < n; i++)
		{
			v3[i] = v1[i] - v2[i];
		}
		return v3;
	}

	
	/**
	 * Reduce an angle in radians to the range (0 - 2 Pi).
	 * 
	 * @param r Value in radians.
	 * @return The reduced radian value.
	 */
	public static double normalizeRadians(double r)
	{
		r = r - TWO_PI * Math.floor(r / TWO_PI);
		// Can't use Math.IEEEremainder here because remainder differs
		// from modulus for negative numbers.
		if (r < 0.)
			r += TWO_PI;

		return r;
	}
	
	/**
	 * Reduce an angle in degrees to the range (0 <= deg < 360).
	 * 
	 * @param d Value in degrees.
	 * @return The reduced degree value.
	 */
	public static double normalizeDegrees(double d)
	{
		d = d - DEGREES_PER_CIRCLE * Math.floor(d / DEGREES_PER_CIRCLE);
		// Can't use Math.IEEEremainder here because remainder differs
		// from modulus for negative numbers.
		if (d < 0.)
			d += DEGREES_PER_CIRCLE;

		return d;
	}

	/**
	 * Convert a Julian day value to Julian centuries referenced to epoch J2000.
	 * 
	 * @param jd Julian day number.
	 * @return Julian centuries from J2000.
	 */
	public static double toCenturies(double jd)
	{
		return (jd - J2000) / JULIAN_DAYS_PER_CENTURY;
	}
	
	/**
	 * Format right ascension. Significant digits are adapted to common
	 * ephemeris precision.
	 * 
	 * @param ra Right ascension in radians.
	 * @return String with the format ##h ##m ##.####s.
	 */
	public static String formatRA(double ra)
	{
		String out = "";
		DecimalFormat formatter = new DecimalFormat("00.0000");
		DecimalFormat formatter0 = new DecimalFormat("00");
		ra = normalizeRadians(ra);
		double ra_h = ra * RAD_TO_HOUR;
		double ra_m = (ra_h - Math.floor(ra_h)) * 60.0;
		double ra_s = (ra_m - Math.floor(ra_m)) * 60.0;
		ra_h = Math.floor(ra_h);
		ra_m = Math.floor(ra_m);

		out = "" + formatter0.format(ra_h) + "h " + formatter0.format(ra_m) + "m " + formatter.format(ra_s) + "s";
		out = out.replaceAll(",", ".");

		return out;
	}

	/**
	 * Format declination. Significant digits are adapted to common ephemeris
	 * precision.
	 * 
	 * @param dec Declination in radians. Must be in the range -Pi/2 to +Pi/2.
	 * @return String with the format $##� ##' ##.###'' ($ is the sign).
	 */
	public static String formatDEC(double dec)
	{
		String out = "";
		DecimalFormat formatter = new DecimalFormat("00.000");
		DecimalFormat formatter0 = new DecimalFormat("00");
		double dec_d = Math.abs(dec) * RAD_TO_DEG;
		double dec_m = (dec_d - Math.floor(dec_d)) * 60.0;
		double dec_s = (dec_m - Math.floor(dec_m)) * 60.0;
		dec_d = Math.floor(dec_d);
		dec_m = Math.floor(dec_m);

		out = "" + formatter0.format(dec_d) + "d " + formatter0.format(dec_m) + "' " + formatter.format(dec_s) + "\"";
		if (dec < 0.0)
			out = "-" + out;
		out = out.replaceAll( ",", ".");

		return out;
	}

	/**
	 * Formats an angle. For low values the degrees/minutes parts are skipped
	 * when are null. For angles above +260 degrees, the value is represented as
	 * a negative angle.
	 * 
	 * @param val Numerical value representing an angle
	 * @param secDecimals Number of decimal places in the arcseconds.
	 * @return String with the adequate format.
	 */
	public static String formatAngle(double val, int secDecimals)
	{
		String digit = "0";
		String out = "";
		for (int i = 0; i < secDecimals; i++)
		{
			out += digit;
		}

		val = EphemUtils.normalizeDegrees(val * EphemConstant.RAD_TO_DEG);
		if (val > 260.0)
			val = val - 360.0;

		DecimalFormat formatter = new DecimalFormat("###");
		if (secDecimals > 0) formatter = new DecimalFormat("##0." + out);

		double val_d = Math.abs(val);
		double val_m = (val_d - Math.floor(val_d)) * 60.0;
		double val_s = (val_m - Math.floor(val_m)) * 60.0;

		val_d = (int) val_d;
		val_m = (int) val_m;

		// Round up to arcminutes properly
		if (secDecimals == 0)
		{
			val_s = Math.floor(val_s + 0.5);
			if (val_s == 60.0)
			{
				val_s = 0.0;
				val_m++;
				if (val_m == 60.0)
				{
					val_m = 0.0;
					val_d++;
				}
			}
		}

		if (val_d == 0.0)
		{
			if (val_m == 0.0)
			{
				out = "" + formatter.format(val_s) + "\"";
			} else
			{
				out = "" + (int) val_m + "' " + formatter.format(val_s) + "\"";
			}
		} else
		{
			out = "" + (int) val_d + "� " + (int) val_m + "' " + formatter.format(val_s) + "\"";
		}
		if (val < 0.0)
			out = "-" + out;

		out = out.replace( ",", ".");
		return out;
	}
	/**
	 * Returns declination in radians given degrees, minutes, and arcseconds. A
	 * minus sign can be set in degrees for southern positions.
	 * 
	 * @param deg Degrees.
	 * @param min Arcminutes.
	 * @param sec Arcseconds
	 * @return Declination in radians
	 */
	public static double parseDeclination(double deg, double min, double sec)
	{
		double dec = Math.abs(deg) + min / SECONDS_PER_MINUTE + sec / SECONDS_PER_DEGREE;
		dec = dec * DEG_TO_RAD;
		if (deg < 0.0)
			dec = -dec;
		return dec;
	}

	/**
	 * Returns right ascension in radians given hours, minutes, and seconds of
	 * time.
	 * 
	 * @param hour Hours.
	 * @param min Minutes.
	 * @param sec Seconds.
	 * @return Right ascension value in radians.
	 */
	public static double parseRightAscension(double hour, double min, double sec)
	{
		double ra = Math.abs(hour) + min / SECONDS_PER_MINUTE + sec / SECONDS_PER_DEGREE;
		ra = ra / RAD_TO_HOUR;
		return ra;

	}


	/**
	 * Transform mean ecliptic geocentric rectangular coordinates to mean
	 * equatorial.
	 * 
	 * @param pos ecliptic coordinates as an (x, y, z) or (x, y, z, vx, vy, vz)
	 *        vector.
	 * @param JD Julian day in Terrestrial Time.
	 * @param eph_method Integer defining ephemeris properties.
	 * @return An array containing the new coordinates.
	 */
	public static double[] eclipticToEquatorial(double pos[], double JD, Precession.Method eph_method)
	{
		double epsilon = Obliquity.meanObliquity(toCenturies(JD), eph_method);
		pos = rotateX(pos, epsilon);

		return pos;
	}

	/**
	 * Transform mean equatorial geocentric rectangular coordinates to mean
	 * ecliptic.
	 * 
	 * @param pos equatorial coordinates as an (x, y, z) or (x, y, z, vx, vy,
	 *        vz) vector.
	 * @param JD Julian day in Terrestrial Time.
	 * @param eph_method Integer defining ephemeris properties.
	 * @return An array containing the new coordinates.
	 */
	public static double[] equatorialToEcliptic(double pos[], double JD, Precession.Method eph_method)
	{
		double epsilon = Obliquity.meanObliquity(toCenturies(JD), eph_method);
		pos = rotateX(pos, -epsilon);

		return pos;
	}


	/**
	 * Reads a resource and returns it contents as an array of strings. Path is taken from
	 * the current established path in this instance, and this path should be a local resource
	 * available.
	 * <P>
	 * 
	 * @return Array of strings.
	 */
	public static  ArrayList<String> readResource(String pathToFile)
	{
		// Define necesary variables
		ArrayList<String> vec = new ArrayList<String>();
		String file_line = "";

		// Connect to the file
		try
		{
			InputStream is = EphemUtils.class.getClassLoader().getResourceAsStream(pathToFile);
			BufferedReader dis = new BufferedReader(new InputStreamReader(is));

			while ((file_line = dis.readLine()) != null)
			{
				// Store object in ArrayList
				vec.add(file_line);
			}

			// Close file
			dis.close();

			return vec;
		} catch (IOException e){
			throw new IOError(e);
		}

	}
	
	/**
	 * Get certain field of a string. Fields are supposed to be separated by a
	 * string called separator. If the separator is a blank space or tab several
	 * consecutive separators can be treated as one separator only if skip is
	 * set to true, which is the default value for other methods in the library.
	 * 
	 * @param field Number of field. 1, 2, ...
	 * @param text String with fields.
	 * @param separator String that defines the separator.
	 * @param skip True to consider several consecutive separators as one only if separator
	 * is one or several blank spaces. False has 50% better performance in this case.
	 * @return The field in the desired position number.
	 */
	public static String getField(int field, String text, String separator, boolean skip)
	{
		if (text == null) return null;
		if (!separator.trim().equals("")) skip = false;
		int space;
		int err = 0;
		if (skip) text = text.trim();
		String myfield = "";
		for (int i = 0; i < field; i++)
		{
			space = text.indexOf(separator);

			if (space >= 0)
			{
				myfield = text.substring(0, space);
				if (skip) myfield = myfield.trim();
				text = text.substring(space + separator.length());
				if (skip) text = text.trim();
			} else {
				space = text.length();
				myfield = text.substring(0, space);
				if (skip) myfield = myfield.trim();
				text = "";
			}

			if (space < 0) {
				err = 1;
				break;
			}
		}

		if (err == 1)
			myfield = null;

		return myfield;
		
	}


	/**
	 * Transform coordinates from ICRS frame to J2000 mean dynamical frame. From
	 * NOVAS package, based on Hilton and Honenkerk 2004, <I>Astronomy &
	 * Astrophysics, 413, 765-770, EQ. (6) AND (8)</I>.
	 * 
	 * @param geo_eq Geocentric rectangular equatorial coordinates of the
	 *        object, mean equinox J2000.
	 * @return vector (x, y, z) with the corrected frame.
	 */
	public static double[] toJ2000Frame(double[] geo_eq)
	{
		// XI0, ETA0, AND DA0 ARE ICRS FRAME BIASES IN ARCSECONDS TAKEN
		// FROM IERS CONVENTIONS (2003), CHAPTER 5
		double XI0 = -0.0166170;
		double ETA0 = -0.0068192;
		double DA0 = -0.01460;

		// COMPUTE ELEMENTS OF ROTATION MATRIX (TO FIRST ORDER)
		double XX = 1.0;
		double YX = -DA0 * ARCSEC_TO_RAD;
		double ZX = XI0 * ARCSEC_TO_RAD;
		double XY = DA0 * ARCSEC_TO_RAD;
		double YY = 1.0;
		double ZY = ETA0 * ARCSEC_TO_RAD;
		double XZ = -XI0 * ARCSEC_TO_RAD;
		double YZ = -ETA0 * ARCSEC_TO_RAD;
		double ZZ = 1.0;

		// INCLUDE SECOND-ORDER CORRECTIONS TO DIAGONAL ELEMENTS
		XX = 1.0 - 0.5 * (YX * YX + ZX * ZX);
		YY = 1.0 - 0.5 * (YX * YX + ZY * ZY);
		ZZ = 1.0 - 0.5 * (ZY * ZY + ZX * ZX);

		// PERFORM ROTATION FROM ICRS TO DYNAMICAL SYSTEM
		double x = XX * geo_eq[0] + XY * geo_eq[1] + XZ * geo_eq[2];
		double y = YX * geo_eq[0] + YY * geo_eq[1] + YZ * geo_eq[2];
		double z = ZX * geo_eq[0] + ZY * geo_eq[1] + ZZ * geo_eq[2];

		return new double[]
		{ x, y, z };
	}
	
	/**
	 * Obtain horizontal coordinates (azimuth, altitude) as seen by the
	 * observer. Resulting altitude will be apparent, not geometic, if the value
	 * of ephemeris type is {@linkplain EphemerisElement#EPHEM_APPARENT}. This 
	 * method requires previous ephemeris calculations, since it only adds 
	 * azimuth, elevation, and paralactic angle to the Ephem object.
	 * <P>
	 * The azimuth is considered equal to zero when an object in towards south.
	 * 
	 * @param time Time object.
	 * @param obs Observer object.
	 * @param ephem Ephem object.
	 * @param eph Ephemeris object defining the ephemeris properties.
	 * @return EphemElement with horizontal coordinates.
	 * @throws JPARSECException Thrown if the method fails, for example because
	 *         of an invalid date.
	 */
	public static EphemElement horizontalCoordinates(TimeElement time, ObserverElement obs, EphemElement ephem,
			EphemerisElement eph)
	{
		// Obtain local apparent sidereal time
		double lst = SiderealTime.apparentSiderealTime(time, obs, eph);

		// Hour angle
		double angh = lst - ephem.rightAscension;

		// Obtain azimuth and geometric alt
		double h = Math.sin(obs.latitude) * Math.sin(ephem.declination) + Math.cos(obs.latitude) * Math.cos(ephem.declination) * Math
				.cos(angh);
		double alt = Math.asin(h);
		double y = Math.sin(angh);
		double x = Math.cos(angh) * Math.sin(obs.latitude) - Math.tan(ephem.declination) * Math.cos(obs.latitude);
		double azi = Math.atan2(y, x);

		// Paralactic angle
		x = (Math.sin(obs.latitude) / Math.cos(obs.latitude)) * Math.cos(ephem.declination) - Math.sin(ephem.declination) * Math
				.cos(angh);
		double p = 0.0;
		if (x != 0.0)
		{
			p = Math.atan2(y, x);
		} else
		{
			p = (y / Math.abs(y)) * Math.PI / 2.0;
		}

		// Correct altitude to obtain the apparent value if necessary
		if (eph.ephemType == EphemerisElement.Ephem.APPARENT)
			alt = getApparentElevation(obs, alt);

		// Set results
		EphemElement ephem_elem = (EphemElement) ephem.clone();
		ephem_elem.elevation = alt;
		ephem_elem.azimuth = azi;
		ephem_elem.paralacticAngle = (float) normalizeRadians(p);

		return ephem_elem;
	}
	
	

	/**
	 * Correct geometric altitude and obtain the apparent value. The correction
	 * is made following Astronomical Almanac. For altitude above 15 degrees a
	 * simple formula is used with an accuracy "usually about 0.1'". For lower
	 * geometric altitudes a fit formula from the Astronomical Almanac 1986 is
	 * used, which gives the correction for observed altitudes. This formula is
	 * inverted numerically to get the observed from the geometric value.
	 * Accuracy about 0.2' for -20C < T < +40C and 970mb < P < 1050mb.
	 * <P>
	 * This correction is automatically done for apparent coordinates.
	 * 
	 * @param obs Observer object containing values of pressure and
	 *        temperature.
	 * @param alt Geometric altitude in radians.
	 * @return Apparent altitude in radians.
	 */
	public static double getApparentElevation(ObserverElement obs, double alt)
	{
		double alt_deg = alt * RAD_TO_DEG;

		if (alt_deg >= -2.0 && alt_deg < 90.0)
		{
			if (alt_deg > 15.0)
			{
				/*
				 * For high altitude angle, AA 1986 page B61 Accuracy "usually
				 * about 0.1'".
				 */
				alt += 0.00452 * DEG_TO_RAD * obs.pressure / ((273.0 + obs.temperature) * Math.tan(alt));
			} else
			{
				/*
				 * Formula for low altitude is from the Almanac for Computers.
				 * It gives the correction for observed altitude, so has to be
				 * inverted numerically to get the observed from the true.
				 * Accuracy about 0.2' for -20C < T < +40C and 970mb < P <
				 * 1050mb.
				 */

				/*
				 * Start iteration assuming correction = 0
				 */
				double y = alt * RAD_TO_DEG;
				double correction = 0.0;

				/*
				 * Invert Almanac for Computers formula numerically
				 */
				double P = (obs.pressure - 80.0) / 930.0;
				double Q = 4.8e-3 * (obs.temperature - 10.0);
				double y0 = y;
				double correction0 = correction;

				for (int i = 0; i < 4; i++)
				{
					double N = y + (7.31 / (y + 4.4));
					N = 1.0 / Math.tan(N *DEG_TO_RAD);
					correction = N * P / (60.0 + Q * (N + 39.0));
					N = y - y0;
					y0 = correction - correction0 - N; /*
														 * denominator of
														 * derivative
														 */

					if ((N != 0.0) && (y0 != 0.0))
					{
						/*
						 * Newton iteration with numerically estimated
						 * derivative
						 */
						N = y - N * (alt * RAD_TO_DEG + correction - y) / y0;
					} else
					{

						/* Can't do it on first pass */
						N = alt * RAD_TO_DEG + correction;
					}

					y0 = y;
					correction0 = correction;
					y = N;
				}

				alt += correction * DEG_TO_RAD;

			}
		}

		return alt;
	}

	
	/**
	 * Return topocentric rectangular coordinates of observer in the ICRS
	 * system, for J2000 true equinox and equator.
	 * 
	 * @param time Time object.
	 * @param obs Observer object.
	 * @param eph Ephemeris object.
	 * @return (x, y, z, vx, vy, vz) of observer in AU. Null values for
	 *         geocenter.
	 * @throws JPARSECException Thrown if the method fails, for example because
	 *         of an invalid date or reference ellipsoid.
	 */
	public static double[] topocentricObserver(TimeElement time, ObserverElement obs, EphemerisElement eph)
	{

		if (eph.isTopocentric)
		{
			double JD = TimeScale.getJD(time, obs, eph, TimeElement.Scale.BARYCENTRIC_DYNAMICAL_TIME);
			double gast = SiderealTime.apparentSiderealTime(time, obs, eph);
			ReferenceEllipsoid ref = new ReferenceEllipsoid(ReferenceEllipsoid.getEllipsoid(eph));
			ObserverElement geo_obs = ref.geodeticToGeocentric(obs);
			geo_obs.geoRad *= ref.earthRadius / AU;
			double geo_pos[] = LocationElement.parseLocationElement(new LocationElement(geo_obs.geoLon,
					geo_obs.geoLat, geo_obs.geoRad));
			double vel_mod = Math.sqrt(geo_pos[0] * geo_pos[0] + geo_pos[1] * geo_pos[1]) * EARTH_MEAN_ROTATION_RATE;
			double geo_vel[] =
			{ -vel_mod * Math.sin(gast), vel_mod * Math.cos(gast), 0.0 };

			double true_pos[] = Nutation.calcNutation(JD, geo_pos, eph.ephemMethod, Nutation.Method.NUTATION_IAU2000);
			double true_vel[] = Nutation.calcNutation(JD, geo_vel, eph.ephemMethod, Nutation.Method.NUTATION_IAU2000);

			true_pos = Precession.precessToJ2000(JD, true_pos, eph.ephemMethod);
			true_vel = Precession.precessToJ2000(JD, true_vel, eph.ephemMethod);

			true_pos = toICRSFrame(true_pos);
			true_vel = toICRSFrame(true_vel);

			double topo[] =
			{ true_pos[0], true_pos[1], true_pos[2], true_vel[0], true_vel[1], true_vel[2] };

			return topo;
		}

		return new double[]
		{ 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
	}

	/**
	 * Transform coordinates from J2000 mean pole to ICRS frame. From NOVAS
	 * package, based on Hilton and Honenkerk 2004, <I>Astronomy &
	 * Astrophysics 413, 765-770, EQ. (6) AND (8)</I>.
	 * 
	 * @param geo_eq Geocentric rectangular equatorial coordinates of the
	 *        object, mean equinox J2000.
	 * @return vector (x, y, z) with the corrected frame.
	 */
	public static double[] toICRSFrame(double[] geo_eq)
	{
		// XI0, ETA0, AND DA0 ARE ICRS FRAME BIASES IN ARCSECONDS TAKEN
		// FROM IERS CONVENTIONS (2003), CHAPTER 5
		double XI0 = -0.0166170;
		double ETA0 = -0.0068192;
		double DA0 = -0.01460;

		// COMPUTE ELEMENTS OF ROTATION MATRIX (TO FIRST ORDER)
		double XX = 1.0;
		double YX = -DA0 * ARCSEC_TO_RAD;
		double ZX = XI0 * ARCSEC_TO_RAD;
		double XY = DA0 * ARCSEC_TO_RAD;
		double YY = 1.0;
		double ZY = ETA0 * ARCSEC_TO_RAD;
		double XZ = -XI0 * ARCSEC_TO_RAD;
		double YZ = -ETA0 * ARCSEC_TO_RAD;
		double ZZ = 1.0;

		// INCLUDE SECOND-ORDER CORRECTIONS TO DIAGONAL ELEMENTS
		XX = 1.0 - 0.5 * (YX * YX + ZX * ZX);
		YY = 1.0 - 0.5 * (YX * YX + ZY * ZY);
		ZZ = 1.0 - 0.5 * (ZY * ZY + ZX * ZX);

		// PERFORM ROTATION FROM DYNAMICAL SYSTEM TO ICRS
		double x = XX * geo_eq[0] + YX * geo_eq[1] + ZX * geo_eq[2];
		double y = XY * geo_eq[0] + YY * geo_eq[1] + ZY * geo_eq[2];
		double z = XZ * geo_eq[0] + YZ * geo_eq[1] + ZZ * geo_eq[2];

		return new double[] { x, y, z };
	}
	
	/**
	 * Transform previous calculated position in an Ephem object from
	 * true equinox of date to true output equinox.
	 * 
	 * @param ephem Input Ephem object.
	 * @param eph Ephemeris object.
	 * @param JD_TDB Julian date of results.
	 * @return Ephem object.
	 */
	public static EphemElement toOutputEquinox(EphemElement ephem, EphemerisElement eph, double JD_TDB)
	{
		EphemElement ephem_elem = (EphemElement) ephem.clone();

		double true_eq[] = LocationElement.parseLocationElement(new LocationElement(ephem_elem.rightAscension,
				ephem_elem.declination, ephem_elem.distance));
		true_eq = Precession.precess(JD_TDB, eph.equinox, true_eq, eph.ephemMethod);
		LocationElement final_loc = LocationElement.parseRectangularCoordinates(true_eq);
		ephem_elem.rightAscension = final_loc.getLongitude();
		ephem_elem.declination = final_loc.getLatitude();

		true_eq = eclipticToEquatorial(LocationElement.parseLocationElement(new LocationElement(
				ephem_elem.heliocentricEclipticLongitude, ephem_elem.heliocentricEclipticLatitude,
				ephem_elem.distanceFromSun)), JD_TDB, eph.ephemMethod);
		true_eq = Precession.precess(JD_TDB, eph.equinox, true_eq, eph.ephemMethod);
		LocationElement heliocentric_loc = LocationElement.parseRectangularCoordinates(equatorialToEcliptic(
				true_eq, JD_TDB, eph.ephemMethod));
		ephem_elem.heliocentricEclipticLongitude = heliocentric_loc.getLongitude();
		ephem_elem.heliocentricEclipticLatitude = heliocentric_loc.getLatitude();

		return ephem_elem;
	}
	
	/**
	 * Correct apparent coordinates for solar deflection, using an algorithm
	 * from NOVAS package, based on Murray (1981), <I>Monthly Notices Royal
	 * Astronomical Society 195, 639-648</I>.
	 * 
	 * @param vep Vector from Earth (observer) to the planet (deflected body).
	 * @param ves Vector from Earth (observer) to sun (deflector body).
	 * @param vsp Vector from sun (deflector body) to planet (deflected body).
	 * @return Array containing (x, y, z) corrected for deflection.
	 */
	public static double[] solarDeflection(double vep[], // Earth-Planet
															// vector
			double ves[], // Earth-Sun vector
			double vsp[]) // Sun-Planet vector
	{
		double deflector[] =
		{ 0.0, 0.0, 0.0 };
		double relative_mass = 1.0;

		return deflectionCorrection(vep, ves, vsp, deflector, relative_mass);
	}


	/**
	 * Correct apparent coordinates for deflection, using an algorithm from
	 * NOVAS package, based on Murray (1981) <I>Monthly Notices Royal
	 * Astronomical Society 195, 639-648</I>. This correction is usually 
	 * lower than 1 arcsecond, and can be neglected most of the times. Only 
	 * for apparent coordinates.
	 * 
	 * @param vep Vector from Earth (observer) to the planet (deflected body).
	 * @param ves Vector from Earth (observer) to sun (deflector body).
	 * @param vsp Vector from sun (deflector body) to planet (deflected body).
	 * @param deflector Vector from sun to deflector body. (0, 0, 0) if it is
	 *        the sun.
	 * @param relative_mass Sun (deflector) mass divided by planet (deflected
	 *        body) mass. Equal to unity if the deflector body is the sun.
	 * @return Array containing (x, y, z) corrected for deflection.
	 */
	public static double[] deflectionCorrection(double vep[], // Earth-Planet vector
			double ves[], // Earth-Sun vector
			double vsp[], // Sun-Planet vector
			double deflector[], // Sun-deflector vector
			double relative_mass) // Sun-Planet mass ratio
	{
		// Sun-Earth vector
		double vse[] =
		{ -ves[0], -ves[1], -ves[2] };

		// Deflector to Earth vector
		double deflector_to_earth[] =
		{ vse[0] - deflector[0], vse[1] - deflector[1], vse[2] - deflector[2] };

		// Deflector de planet vector
		double deflector_to_planet[] =
		{ vsp[0] - deflector[0], vsp[1] - deflector[1], vsp[2] - deflector[2] };

		// Pass to spherical
		LocationElement loc_sun = LocationElement.parseRectangularCoordinates(deflector_to_earth);
		LocationElement loc_plan = LocationElement.parseRectangularCoordinates(deflector_to_planet);
		LocationElement loc_geoc = LocationElement.parseRectangularCoordinates(vep);

		// COMPUTE NORMALIZED DOT PRODUCTS OF VECTORS
		double DOT_PLANET = vep[0] * deflector_to_planet[0] + vep[1] * deflector_to_planet[1] + vep[2] * deflector_to_planet[2];
		double DOT_EARTH = deflector_to_earth[0] * vep[0] + deflector_to_earth[1] * vep[1] + deflector_to_earth[2] * vep[2];
		double DOT_DEFLECTOR = deflector_to_planet[0] * deflector_to_earth[0] + deflector_to_planet[1] * deflector_to_earth[1] + deflector_to_planet[2] * deflector_to_earth[2];

		DOT_PLANET = DOT_PLANET / (loc_geoc.getRadius() * loc_plan.getRadius());
		DOT_EARTH = DOT_EARTH / (loc_geoc.getRadius() * loc_sun.getRadius());
		DOT_DEFLECTOR = DOT_DEFLECTOR / (loc_sun.getRadius() * loc_plan.getRadius());

		// IF GRAVITATING BODY IS OBSERVED OBJECT, OR IS ON A STRAIGHT LINE
		// TOWARD OR AWAY FROM OBSERVED OBJECT TO WITHIN 1 ARCSEC,
		// DEFLECTION IS SET TO ZERO
		if (Math.abs(DOT_DEFLECTOR) > 0.99999999999)
			return vep;

		// COMPUTE SCALAR FACTORS
		double FAC1 = SUN_GRAVITATIONAL_CONSTANT * 2.0 / (Math.pow(SPEED_OF_LIGHT, 2.0) * AU * 1000.0 * loc_sun
				.getRadius() * relative_mass);
		double FAC2 = 1.0 + DOT_DEFLECTOR;

		// CONSTRUCT CORRECTED POSITION VECTOR
		for (int i = 0; i < 3; i++)
		{
			double v = vep[i] / loc_geoc.getRadius() + FAC1 * (DOT_PLANET * deflector_to_earth[i] / loc_sun.getRadius() - DOT_EARTH * deflector_to_planet[i] / loc_plan
					.getRadius()) / FAC2;

			vep[i] = v * loc_geoc.getRadius();
		}

		return vep;
	}
	
	/**
	 * Obtain the module of the integer division.
	 * 
	 * @param val1 Number to divide
	 * @param val2 Number that val1 will be divided by
	 * @return The rest of the integer division
	 */
	public static double module(double val1, double val2)
	{
		double val3 = val1 / val2;
		double rest = val1 - val2 * (int) val3;

		return rest;
	}
	
	/**
	 * Correct position for aberration, including relativistic effects.
	 * <P>
	 * Adapted from NOVAS package. Algorithm based on Murray (1981), <I>Monthly
	 * Notices Royal Astronomical Society 195, 639-648</I>.
	 * 
	 * @param geo_pos Geocentric position.
	 * @param earth Heliocentric position and velocity of Earth.
	 * @param light_time Light time in days.
	 * @return Geocentric coordinates.
	 */
	public static double[] aberration(double geo_pos[], double earth[], double light_time)
	{
		double geo_vel[] = new double[]
		{ earth[3], earth[4], earth[5] };
		double TL = light_time;
		double P1MAG = TL / LIGHT_TIME_DAYS_PER_AU;
		double VEMAG = LocationElement.parseRectangularCoordinates(geo_vel).getRadius();
		double BETA = VEMAG * LIGHT_TIME_DAYS_PER_AU;
		double DOT = geo_pos[0] * geo_vel[0] + geo_pos[1] * geo_vel[1] + geo_pos[2] * geo_vel[2];
		double COSD = DOT / (P1MAG * VEMAG);
		double GAMMAI = Math.sqrt(1.0 - BETA * BETA);
		double P = BETA * COSD;
		double Q = (1.0 + P / (1.0 + GAMMAI)) * TL;
		double R = 1.0 + P;

		for (int i = 0; i < 3; i++)
		{
			geo_pos[i] = (GAMMAI * geo_pos[i] + Q * geo_vel[i]) / R;
		}

		return new double[]
		{ geo_pos[0], geo_pos[1], geo_pos[2] };
	}
	

	/**
	 * Performs topocentric correction of right ascension, declination, and
	 * distance, and also diurnal aberration, and adds the results to the
	 * {@linkplain EphemElement} object.
	 * 
	 * @param ephem The {@linkplain EphemElement} to take from and set the results to.
	 * @param obs The {@linkplain ObserverElement} that defines observer's position.
	 * @param time Time object.
	 * @param eph Ephemeris object defining the ephemeris properties.
	 * @return The {@linkplain EphemElement} with the corrected values.
	 * @throws JPARSECException Thrown if the method fails, for example because
	 *         of an invalid date or reference ellipsoid.
	 */
	public static EphemElement topocentricCorrection(TimeElement time, ObserverElement obs, EphemElement ephem,
			EphemerisElement eph)
	{
		// Object coordinates
		double eq_geo[] = LocationElement.parseLocationElement(new LocationElement(ephem.rightAscension,
				ephem.declination, ephem.distance));

		// The Earth is not a perfect sphere. Pass to geocentric.
		ReferenceEllipsoid ref = new ReferenceEllipsoid(ReferenceEllipsoid.getEllipsoid(eph));
		ObserverElement observer = ref.geodeticToGeocentric(obs);

		// Obtain local apparent sidereal time
		double lst = SiderealTime.apparentSiderealTime(time, obs, eph);

		// Obtain topocentric rectangular coordinates (diurnal parallax). See AA
		// 1986, page D3.
		double xtopo = eq_geo[0] - observer.geoRad * (ref.earthRadius / AU) * Math
				.cos(observer.geoLat) * Math.cos(lst);
		double ytopo = eq_geo[1] - observer.geoRad * (ref.earthRadius / AU) * Math
				.cos(observer.geoLat) * Math.sin(lst);
		double ztopo = eq_geo[2] - observer.geoRad * (ref.earthRadius / AU) * Math
				.sin(observer.geoLat);

		// Obtain topocentric equatorial coordinates
		LocationElement loc = LocationElement.parseRectangularCoordinates(xtopo, ytopo, ztopo);

		/* Diurnal aberration */
		double dra = 0.0;
		double ddec = 0.0;
		if (eph.ephemType == EphemerisElement.Ephem.APPARENT)
		{
			if (Math.cos(ephem.declination) != 0.0)
				dra = 1.5472e-6 * observer.geoRad * Math.cos(observer.geoLat) * Math
						.cos(lst - ephem.rightAscension) / Math.cos(ephem.declination);
			ddec = 1.5472e-6 * observer.geoRad * Math.cos(observer.geoLat) * Math.sin(ephem.declination) * Math
					.sin(lst - ephem.rightAscension);
		}

		/* Set values */
		EphemElement ephemOut = (EphemElement) ephem.clone();
		ephemOut.rightAscension = loc.getLongitude() + dra;
		ephemOut.declination = loc.getLatitude() + ddec;

		return ephemOut;
	}

	/**
	 * Obtain light time distance in days from the position of the object and
	 * the observer, taking into account the ephemeris type.
	 * 
	 * @param geo_eq Goecentric equatorial coordinates of object.
	 * @param topo Topocentric equatorial coordinates of observer.
	 * @param eph Ephemeris object defining the ephemeris properties.
	 * @return Light time in days. Will be null if ephemeris are geometric.
	 */
	public static double getTopocentricLightTime(double geo_eq[], double topo[], EphemerisElement eph)
	{
		// Obtain geocentric positions
		double p[] = getGeocentricPosition(geo_eq, topo);
		double r = Math.sqrt(p[0] * p[0] + p[1] * p[1] + p[2] * p[2]);

		// Obtain light time to correct the time of ephemeris
		if (eph.ephemType == EphemerisElement.Ephem.GEOMETRIC)
			r = 0.0;
		double light_time = LIGHT_TIME_DAYS_PER_AU * r;

		return light_time;
	}
	
	/**
	 * Obtain geocentric equatorial coordinates.
	 * 
	 * @param p_obj Equatorial vector from origin to object.
	 * @param p_earth Equatorial vector from origin to observer.
	 * @return Array with x, y, z coordinates.
	 */
	public static double[] getGeocentricPosition(double p_obj[], double p_earth[])
	{
		return substractVector(p_obj, p_earth);
	}
	

	/**
	 * Obtain geocentric ecliptic coordinates.
	 * 
	 * @param loc_elem LocationElement for the outer object.
	 * @param loc_earth LocationElement for the Earth or the mother object.
	 * @return Array with x, y, z coordinates.
	 */
	public static double[] getGeocentricPosition(LocationElement loc_elem, LocationElement loc_earth)
	{
		// Obtain ecliptic coordinates
		double p1[] = LocationElement.parseLocationElement(loc_elem);
		double p2[] = LocationElement.parseLocationElement(loc_earth);

		// Obtain geocentric positions
		return EphemUtils.getGeocentricPosition(p1, p2);
	}
	
	

}
