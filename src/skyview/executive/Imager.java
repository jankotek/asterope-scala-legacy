package skyview.executive;

// External classes used in this class.

import nom.tam.fits.Fits;
import nom.tam.fits.Header;
import nom.tam.util.ArrayFuncs;
import org.asterope.util.*;
import org.asterope.geometry.*;

import skyview.Position;
import skyview.process.imagefinder.ImageFinder;
import skyview.process.Processor;
import skyview.request.SourceCoordinates;
import org.asterope.util.*;

import skyview.sampler.*;
import skyview.survey.*;

import java.io.File;
import java.io.FileNotFoundException;
import java.lang.ref.SoftReference;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Pattern;

/**
 * This class generates an image or images from user inputs. Output images are
 * created by sampling one or more input images into a user specified grid of
 * pixels.<br>
 * Usage:<br>
 * <code>java org.asterope.geometry.test.Imager [key=value] [key=value] ...</code>
 * <br>
 * If the code is being executed from Jar file, then use <br>
 * <code>java -jar skyview.jar [key=value] [key=value]...</code><br>
 * The field keys are not case sensitive but the values may be.
 * <p>
 * <b> Valid field keys include:
 * <dl>
 * <dt>Lon
 * <dd>The longitude of the center of the output image in the coordinate system
 * selected. Note that the default coordinate system is J2000, so the longitude
 * is the right ascension. The value is specified in decimal degrees.
 * <dt>Lat
 * <dd>The latitude of the center of the output image in the coordinate system
 * selected. J2000 is the default coordinate system, so by default Lat
 * corresponds to declination. The value is specified in decimal degrees.
 * <dt>Position
 * <dd>The comma separated longitude and latitude. If Position and Lon/Lat are
 * specified then the values specified in Lon/Lat override those in position.
 * <dt>CopyWCS
 * <dd>An existing FITS file may be used to define the WCS to be used for the
 * output. If this argument is specified any other argument that specifies the
 * output image geometry is ignored.
 * 
 * <dt>Coordinates
 * <dd>A string describing the coordinate system to be used for the output
 * image. It comprises an inital letter which gives the coordiante system, which
 * prefixes a number giving the epoch of the coordiante system.
 * <ul>
 * <li>J: Julian (FK5) coordiantes, e.g., J2000.
 * <li>B: BesselianSphereDistorter coordinates, e.g., B1950.
 * <li>E: Julian Ecliptic coordinates, e.g., E2000.
 * <li>H: Helioecliptic coordinates (i.e., coordinates centered on the
 * instantaneous position of the sun), e.g., H2002.4356.
 * <li>G: Galactic coordinates. Characters after the first are ignored.
 * <li>I: ICRS coordinates. Characters after the first are ignored.
 * </ul>
 * <dt>Projection:
 * <dd>The projection used to convert from celestial to plane coordinates. The
 * projection string is the 3 character string used in the FITS WCS papers, with
 * only the first letter capitalized. Supported output projections include:
 * <ul>
 * <li>Tan: The tangent plane on gnomonic projection.
 * <li>Sin: The sine or orthographic projection.
 * <li>Ait: The Hammer-Aitoff projection.
 * <li>Car: The cartesian or plate-caree projection.
 * <li>Zea: The Zenithal equal area projection
 * <li>Csc: The COBE sperical cube projection.
 * <li>Toa: The HTM TOAST projection
 * </ul>
 * <dt>Sampler:
 * <dd>The sampler defines how the input images are sampled to get values at the
 * putput pixels. Sampling algorithims include:
 * <ul>
 * <li>NN: Nearest Neighbor.
 * <li>LI: [Bi-]Linear interpolation.
 * <li>Lanczos[n]: A Lanczos smoothly truncated Sinc interpolator of order n.
 * Lanczos defaults to order 3.
 * <li>Spline[n]: The n'th order spline interpolation. Defaults to cubic
 * splines. The order may range from 2 to 5.
 * <li>Clip: A flux conserving exact area resampler where output pixels serve as
 * clipping windows on the input image.
 * </ul>
 * <dt>Scale:
 * <dd>The size of the output pixels in degrees. If the pixels are square only a
 * single values is given. Non-square pixels can be specified as
 * Scale=xsize,ysize.
 * <dt>Size:
 * <dd>The size of the entire image in degrees.
 * <dt>Pixels:
 * <dd>The number of output pixels along an edge. If the output image is not
 * square this can be specified as Pixels=xPixels,yPixels
 * <dt>Rotation:
 * <dd>A rotation in the output plane to be applied to the data (in decimal
 * degrees)
 * <dt>Survey:
 * <dd>The survey from which the output image is to be created. More than one
 * survey can be specified as survey1,survey2,survey3,...
 * <dt>Ebins:
 * <dd>For surveys with a third dimension, the binning to be applied in this
 * dimension. Currently this is specified using as x0,dx,n where x0 is the
 * starting bin for the first output bin, dx is the width of the output bins,
 * and n is the number of output bins. These are expressed in terms of the input
 * energy bins, such that if the survey data has 10 bins, the first bin ranges
 * from 0-1, the second from 1-2 and the last from 9-10. If we wished to rebin
 * this data into four evenly spaced bins excluding the first and last bins,
 * then Ebins=1,2,4. A default binning is defined for each 3-d survey included
 * in SkyView.
 * <dt>Output:
 * <dd>The output file name. If data is being created from more than one survey,
 * then the survey short name will be appended to the name. The default output
 * filename is output.fits. The strings "-" or "stdout" are used to specify
 * writing to the standard output.
 * <dt>Compress:
 * <dd>Write the output in GZIP compressed form. The value field is ignored.
 * <dt>Float:
 * <dd>Write output in 4 byte reals rather than 8 byte. The value field is
 * ignored.
 * <dt>
 * <dd>
 * <p>
 * The following options control where the imager task finds survey data.
 * <p>
 * <dt>XMLRoot:
 * <dd>The directory containing the XML survey descriptions
 * <dt>Cache:
 * <dd>Directory location (or locations) where cached files (survey files
 * retrieved from remote locations) are to be found. The first cache location is
 * also used when a remote file is retrieved.
 * <dt>PurgeCache:
 * <dd>Should files cached during this retrieval be deleted after processing?
 * Only files cached during the current operation will be deleted. If there are
 * survey files in the cache area from previous requests that are used in the
 * current request, these will be retained.
 * <dt>SurveyXML:
 * <dd>Gives the name of a file containing an XML description of a survey. This
 * allows the user to create an image from a survey they are describing. Use a
 * "," to separate multiple survey files.
 * <dd>The following operations allow the user to override the basic processing
 * operations.
 * <dt>
 * <dd>
 * <p>
 * The following options control the classes that are used in processing the
 * user request. Most users can ignore these.
 * <p>
 * <dt>SurveyFinder:
 * <dd>The class of the object that finds the appropriate survey object for the
 * surveys the user has requested.
 * <dt>ImageFinder
 * <dd>The class of the object that finds the appropriate images within a survey
 * for each pixel of the output object.
 * <dt>PreProcessor:
 * <dd>One or more objects that do image pre-processing.
 * <dt>Mosaicker:
 * <dd>The object that actually generates the output image melding together the
 * input images.
 * <dt>PostProcessor:
 * <dd>One or more classes of object that do image post processing. One example
 * used by default in some surveys is the org.asterope.geometry.Deedger. You can
 * force de-edging by specifying the class, or turn it off by setting
 * PostProcessor=null.
 * <p>
 * <dt>NoFITS:
 * <dd>Do not create a FITS output file. This could be used for debugging, or
 * more likely in combination with graphics output created by the
 * skyview.ij.IJProcessor where the user wants only a JPEG or GIF and does not
 * need a FITS file. See the help for the IJProcessor for details on supported
 * formats and other options.
 * </dl>
 * 
 */
public class Imager {

	// Used later in parsing.
	static Pattern comma = Pattern.compile(",");

	/**
	 * Clone of the array giving the mapping between input images and output
	 * pixels.
	 */
	protected int[] match = null;

	/** The name of the last survey processed */
	protected String lastSurvey = null;

	/**
	 * The Hash of survey images that have been created for this imager.
	 */
	protected static HashMap<String, SoftReference<ImageState>> doneImages = new HashMap<String, SoftReference<ImageState>>();

	/**
	 * A scalar adjustment to the output value for each input image used to
	 * minimize edge effects.
	 */
	protected double[] edgeAdjustments;

	/** Width of the image */
	protected int nx;

	/** Height of the image */
	protected int ny;

	/** The number of output planes */
	protected int nz = 1;

	/** The beginning of the first pixel in energy space. */
	protected double bin0;

	/** The width of pixels in energy space */
	protected double dBin;

	/** The Scaler object for the output image. */
	protected Scaler s;

	/** The Projection object for the output image. */
	protected Projection p;

	/** The CoordinateSystem object for the output image. */
	protected CoordinateSystem c;

	/** The sampler object used to create the output image. */
	protected Sampler samp;

	/** The lon/lat (or RA/dec) coordinates of the output image. */
	protected double lon = Double.NaN, lat = Double.NaN;

	/** The current Survey object being used. */
	protected Survey surv;

	/** The candidate images for use in resampling */
	protected Image[] cand;

	/** The current output image */
	protected Image output;

	/** An imager instance */
	private static Imager lastImager;


	/** Processors used */
	private ArrayList<Processor> processes;

	/** The WCS used */
	private WCS wcs;

	private WCS defaultWCS;

	/**
	 * Version of imager. The third number is intended to be the SVN repository
	 * number, but that's hard to get so we've given up on it.
	 */
	private static String version = "2.6";

	public static String getVersion() {
		return version;
	}

	/**
	 * Initialize the imager. Make sure that choices have been made for the
	 * basic elements needed for running the code.
	 */
	public Imager() {

		lastImager = this;
	}

	/**
	 * Custom constructor used by Asterope. It supplyes some arguments without
	 * using Settings class
	 * 
	 * @param width
	 *            of image
	 * @param height
	 *            of image
	 * @param defaultWCS
	 *            World Coordinate System to use
	 */
	public Imager(double width, double height, WCS defaultWCS) {
		this.defaultWCS = defaultWCS;
		this.nx = (int) width;
		this.ny = (int) height;
	}

	/** Get the sampler name. */
	public String getSamplerName() {
		return samp.getName();
	}

	/** Get the Scaler used for the output image. */
	public Scaler getScaler() {
		return s;
	}

	/** Get the output data as 1-d double array */
	public Object getImageData() {
		return output.getDataArray();
	}

	/** Get the central longitude/RA of the output image */
	public double getLon() {
		return lon;
	}

	/** Get the central latitude/declination of the output image */
	public double getLat() {
		return lat;
	}

	/** Get the current survey being processed */
	public Survey getSurvey() {
		return surv;
	}

	/** Get the energy depth of the output image */
	public int getPixelDepth() {
		return output.getDepth();
	}

	/** Get the width of the output image in pixels */
	public int getPixelWidth() {
		return output.getWidth();
	}

	/** Get the height of the output image in pixels */
	public int getPixelHeight() {
		return output.getHeight();
	}

	public void checkUpdateSettings() {
		new SettingsFixer().updateSettings();
	}

	public boolean init() throws Exception {
		// Are there any classes that want to take a look
		// at the Settings before we begin processing?
		checkUpdateSettings();

		if (Settings.get(Val.Survey) == null) {
			Settings.err.println("No survey specified");
			return false;

		} else if (!Settings.has(Val.Position)
				&& (Settings.get(Val.Lon) == null || Settings.get(Val.Lat) == null)
				&& (!Settings.has(Val.CopyWCS))) {
			Settings.err.println("No position specified");
			return false;
		}

		if (!Settings.has(Val.output)) {
			Settings.put(Val.output, "output");
		}
		return true;
	}

	/** Run the command */
	public void run() throws Exception {
		if (!init()) {
			return;
		}
		Settings.err.println("Imager starting v" + version + ".");
		String surv = Settings.get(Val.Survey);

		Settings.err.println("\nProcessing survey:" + surv);
		processSurvey(surv);
	}

	protected Survey loadSurvey(String surveyID) throws Exception {

		Survey surv = XMLSurveyFinder.find(surveyID);


		// Save the current settings before we add any survey specific ones.
		surv.updateSettings();
		return surv;
	}

	protected WCS loadWCS() throws Exception {
		if (this.defaultWCS != null) // dont use Settings if other options was
										// already supplyed
			return defaultWCS;

		WCSFits wcs = null;

		if (Settings.has(Val.CopyWCS)) {
			wcs = copyWCS(Settings.get(Val.CopyWCS));
			s = wcs.getScaler();
			int[] axes = wcs.headerNaxis();
			nx = axes[0];
			ny = axes[1];

		} else {
			if (Settings.get(Val.pixels) != null
					&& Settings.get(Val.pixels).length() > 0) {
				String[] pix = comma.split(Settings.get(Val.pixels));
				try {
					nx = Integer.parseInt(pix[0].trim());
				} catch (Exception e) {
					throw new Exception("Invalid pixels setting:"
							+ Settings.get(Val.pixels));
				}

				if (pix.length > 1) {
					try {
						ny = Integer.parseInt(pix[1].trim());
					} catch (Exception e) {
						throw new Exception("Invalid pixels setting:"
								+ Settings.get(Val.pixels));
					}
				} else {
					ny = nx;
				}
			} else {
				nx = 300;
				ny = 300;
			}
			wcs = specifyWCS(nx, ny);
		}
		Header h = new Header();
		wcs.updateHeader(h, getScaler(), new double[] { getLon(), getLat() },
				Settings.get(Val.projection), Settings.get(Val.coordinates));

		// // Save the key WCS parameters for possible further use.
		// Settings.put(Val._CRPIX1, h.getDoubleValue("CRPIX1")+"");
		// Settings.put(Val._CRPIX2, h.getDoubleValue("CRPIX2")+"");
		// Settings.put(Val._CRVAL1, h.getDoubleValue("CRVAL1")+"");
		// Settings.put(Val._CRVAL2, h.getDoubleValue("CRVAL2")+"");
		// Settings.put(Val._CDELT1, h.getDoubleValue("CDELT1")+"");
		// Settings.put(Val._CDELT2, h.getDoubleValue("CDELT2")+"");
		// Settings.put(Val._CD1_1, h.getDoubleValue("CD1_1")+"");
		// Settings.put(Val._CD1_2, h.getDoubleValue("CD1_2")+"");
		// Settings.put(Val._CD2_1, h.getDoubleValue("CD2_1")+"");
		// Settings.put(Val._CD2_2, h.getDoubleValue("CD2_2")+"");

		return wcs;
	}

	protected Position loadPosition() throws Exception {

		// Find the center of the image.
		double[] cpix = new double[] { nx / 2., ny / 2. };
		double[] cunit = new double[3];
		wcs.inverse().transform(cpix, cunit);

		cpix = new Vector3d(cunit).toRaDeArray();
		cpix[0] = Math.toDegrees(cpix[0]);
		cpix[1] = Math.toDegrees(cpix[1]);

		if ((cpix[0] != cpix[0]) || (cpix[1] != cpix[1])) {
			// Maybe on the edge of fixed projection, or
			// outside Sin or Tan coverage.
			Settings.err
					.println("  Unable to locate center position in projection -- rounding error at edge?");
			return null;
		} else {
			return new Position(cpix[0], cpix[1], "J2000");
		}
	}

	protected Image loadImage() throws Exception {
		double[] data = new double[nx * ny * this.nz];
		return new Image(data, wcs, nx, ny, nz);
	}

	protected Image[] loadCandidates(Position pos) throws Exception {
		// Get max scale using scale and image size
		double maxSize = Math.max(nx, ny) * wcs.getScale() * 180 / Math.PI;
		if (maxSize <= 0) {
			throw new Exception("Ouput region has nil size");
		}
		Image[] cand = surv.getImages(pos, maxSize);
		// --- If no candidates there is no reason to continue
		if (cand.length == 0) {
			// String msg =
			// "Survey: "+Settings.get("_currentSurvey")+" No candidate images were found in the region.  Position may be outside the coverage area.";
			// Settings.put("ErrorMsg", msg);
			Settings.err
					.println("  No candidate images.  Processing of this survey is completed.");
			// No output image.
			output = null;
		} else {
			Settings.err.println("  Number of candidate source images is "
					+ cand.length + ".");
		}
		return cand;
	}

	protected void loadSamplers() throws Exception {
		String sampling = Settings.get(Val.sampler);

		// Get the appropriate sampler
		if (sampling.equals("NN"))
			samp = new NN();
		else
			samp = new Lanczos(3);

	}

	protected int[] reuseMatch(String surveyID) {
		if (match != null && Settings.get(Val.GeometryTwin) != null) {
			// Note that we're assuming that we process twins sequentially.
			String primary = "(^|.*,)" + lastSurvey + "($|,.*)";

			if (Pattern.compile(primary, Pattern.CASE_INSENSITIVE)
					.matcher(Settings.get(Val.GeometryTwin)).find()) {
				Settings.err.println("  Reusing geometry match from:"
						+ lastSurvey);
				return match;
			}
		}
		return null;
	}

	protected int[] loadMatch(String surveyID) throws Exception {

		// If the previous survey has the same geometry as this one,
		// we don't need to recompute the match array.
		ImageFinder imFin = ImageFinder.factory(Settings.get(Val.imagefinder));
		imFin.setStrict(Settings.has(Val.StrictGeometry));
		match = imFin.findImages(cand, output);
		lastSurvey = surveyID;

		if (match == null) {
			match = new int[output.getWidth() * output.getHeight()];
			java.util.Arrays.fill(match,
					skyview.process.imagefinder.Border.NO_COVERAGE);
		}

		return match;
	}

	protected void doProcess(Val type) throws Exception {

		String[] procNames = Settings.getArray(type);
		for (String procName : procNames) {
			Settings.checkCancelled();
			dynoProcess(procName);
		}
	}

	public void dynoProcess(String name) throws Exception {
		Processor proc = (Processor) Class.forName(name).newInstance();
		proc.process(cand, output, match, samp);
		processes.add(proc);
	}

	protected ImageState haveImage(String surveyID, WCS wcs) {
		ImageState oldImage = null;

		SoftReference<ImageState> wr = doneImages.get(surveyID.toLowerCase());
		if (wr != null) {
			oldImage = wr.get();
		}
		if (oldImage != null
				&& oldImage.output.getWCS().getScale() == wcs.getScale()) {
			Settings.err.println("  Using cached image for " + surveyID);
			return oldImage;
		} else {
			return null;
		}
	}

	/** Process a particular survey. */
	public void processSurvey(String surveyID) throws Exception {
		Settings.put(Val._currentSurvey, surveyID);
		output = loadAndProcessSurvey(surveyID);
		postprocessSurvey();
		if (match != null && output != null && !Settings.has(Val.nofits)) {
			createFitsFile();
		}
	}

	public Image loadAndProcessSurvey(String surveyID) throws Exception {

		processes = new ArrayList<Processor>();
		surv = loadSurvey(surveyID);
		if (surv == null) {
			return null;
		}
		wcs = loadWCS();
		if (wcs == null) {
			// Error message should alread be printed.
			return null;
		}
		if (haveImage(surveyID, wcs) != null) {
			ImageState is = haveImage(surveyID, wcs);
			cand = is.sources;
			samp = is.samp;
			processes = is.procs;
			return is.output;
		}

		Position pos = loadPosition();
		if (pos == null) {
			return null;
		}

		output = loadImage();
		cand = loadCandidates(pos);
		// Reuse the previous geometry?
		match = reuseMatch(surveyID);

		if (match == null && (cand != null && cand.length > 0)) {
			loadMatch(surveyID);
		}

		if (match != null) {
			loadSamplers();

			doProcess(Val.Preprocessor);

			if (Settings.get(Val.Mosaicker) == null) {
				Settings.put(Val.Mosaicker, "skyview.process.Mosaicker");
			}
			doProcess(Val.Mosaicker);
		}
		if (output != null) {
			doneImages.put(surveyID, new SoftReference<ImageState>(
					new ImageState(cand, output, match, samp, processes)));
		}
		return output;
	}

	public void postprocessSurvey() throws Exception {

		// Do we have any postprocesing? (Maybe even if the image failed)
		// Use lastMatch rather than match, since match may have been consumed
		// by the mosaicker.
		doProcess(Val.deedger);
		doProcess(Val.Postprocessor);
	}

	/** Create the FITS file */
	public void createFitsFile() throws Exception {

		Scaler scaler = getScaler();
		Object data = getImageData();

		if (data == null) {
			Settings.err.println("  Unexpected error: No image data found!");
			return;
		}

		if (Settings.get(Val.Float) != null) {
			data = nom.tam.util.ArrayFuncs.convertArray(data, float.class);
		}

		int nx = getPixelWidth();
		int ny = getPixelHeight();
		int nz = getPixelDepth();

		Header h = new Header();
		h.addValue("SIMPLE", true, "Written by SkyView " + new java.util.Date());
		if (Settings.has(Val.Float)) {
			h.addValue("BITPIX", -32, "4 byte floating point");
		} else {
			h.addValue("BITPIX", -64, "8 byte floating point");
		}
		if (nz == 1) {
			h.addValue("NAXIS", 2, "Two dimensional image");
		} else {
			h.addValue("NAXIS", 3, "Three dimensional image");
		}
		h.addValue("NAXIS1", nx, "Width of image");
		h.addValue("NAXIS2", ny, "Height of image");
		if (nz != 1) {
			h.addValue("NAXIS3", nz, "Depth of image");
		}

		if (Settings.has(Val.CopyWCS)) {
			if (wcs instanceof WCSFits)
				((WCSFits) wcs).copyToHeader(h);
		} else {
			wcs.updateHeader(h, scaler, new double[] { getLon(), getLat() },
					Settings.get(Val.projection), Settings.get(Val.coordinates));
		}

		Survey surv = getSurvey();
		surv.updateHeader(h);

		h.insertHistory("");
		h.insertHistory(" Settings used in processing:");
		h.insertHistory("");
		Val[] keys = Settings.getKeys();

		java.util.Arrays.sort(keys);
		for (Val key : keys) {

			if (key.toString().charAt(0) == '_') {
				continue; // Skip internal communication settings.
			}

			String val = Settings.get(key);
			if (val == null) {
				h.insertHistory(key + " is null");
			} else if (val.equals("1")) {
				h.insertHistory(key.toString());
			} else {
				h.insertHistory(key + " = " + val);
			}
		}

		// Add in the images we used.
		h.insertHistory("");
		h.insertHistory(" Map generated at: " + new java.util.Date());
		h.insertHistory("");
		h.insertHistory(" Resampler used: " + getSamplerName());
		h.insertHistory("");

		// Update the headers to reflect processing.
		for (Processor p : processes) {
			p.updateHeader(h);
		}

		writeFits(h, data);
	}

	/**
	 * Write the FITS file. Handle special cases of writing to STDOUT and a
	 * compressed output.
	 */
	// private void writeFits(Fits f) throws Exception {
	private void writeFits(Header h, Object data) throws Exception {

		java.io.OutputStream base;

		String out = Settings.get(Val.output);

		// Writing to Standard out?
		if (out.equals("-") || out.equalsIgnoreCase("stdout")) {
			Settings.err.println("  Sending output to standard output stream");
			base = System.out;
		} else {
			String path = new File(out).getName();
			if (path.indexOf('.') < 0) {
				out = out + ".fits";
			}
			Settings.err.println("  Opening FITS file: " + out);
			base = new java.io.FileOutputStream(out);
		}

		nom.tam.util.BufferedDataOutputStream bds = new nom.tam.util.BufferedDataOutputStream(
				base);

		// Writing out header and data separately.
		h.write(bds);
		bds.writeArray(data);
		long len = ArrayFuncs.computeLSize(data);
		long need = 2880 - len % 2880;
		if (need != 2880) {
			byte[] buf = new byte[(int) need];
			bds.write(buf);
		}

		bds.close();
	}

	private WCSFits specifyWCS(int nx, int ny) throws Exception {

		String csys = Settings.get(Val.coordinates);
		String proj = Settings.get(Val.projection);
		String equin = Settings.get(Val.equinox);
		// The input position may be specified as 'position=ra,dec' or
		// 'lon=ra lat=dec'. The values are in the specified coordiante
		// system (J2000 by default).

		c = CoordinateSystem.factory(csys, equin);
		if (c != null) {
			Settings.put(Val.coordinates, c.getName());
		} else {
			Settings.err.println("Invalid coordinates:" + csys + " "
					+ Settings.get(Val.equinox));
			return null;
		}

		csys = c.getName();

		double[] posn = null;
		if (Settings.get(Val.Position) != null) {

			SourceCoordinates sc = new SourceCoordinates(
					Settings.get(Val.Position), csys, Double.parseDouble(equin));

			sc.convertToCoords();
			Position ps = sc.getPosition();
			if (ps == null) {
				throw new Exception("Unable to recognize target/position: "
						+ Settings.get(Val.Position));
			}
			posn = ps.getCoordinates(csys);
			// Settings.put(Val.ReqXPos, ""+posn[0]);
			// Settings.put(Val.ReqYPos, ""+posn[1]);

		} else if (Settings.has(Val.Lon) && Settings.has(Val.Lat)) {
			SourceCoordinates sc = SourceCoordinates.factory(
					Settings.get(Val.Lon), Settings.get(Val.Lat),
					Settings.get(Val.coordinates));
			if (sc == null) {
				Settings.err.println("Invalid coordinates:"
						+ Settings.get(Val.Lon) + ", " + Settings.get(Val.Lat)
						+ " in " + Settings.get(Val.coordinates));
				return null;
			}
			posn = sc.getPosition().getCoordinates(csys);
			Settings.put(Val.Position,
					Settings.get(Val.Lon) + ", " + Settings.get(Val.Lat));
		} else {
			Settings.err.println("Error: No position specified");
			return null;
		}

		lon = posn[0];
		lat = posn[1];

		if (lon == Double.NaN || lat == Double.NaN) {
			throw new Error("Invalid position/coordinates specified.");
		}

		double xscale = 1. / 3600;
		double yscale = 1. / 3600;

		// Do Size first, since that will normally be
		// a user specified item, but scale is provided
		// by the survey even if the user doesn't specify it
		// so we'd never see the user specified size.

		String sz = Settings.get(Val.Size);
		if (sz != null && sz.length() > 0
				&& !sz.toLowerCase().equals("default")) {
			String[] sizes = comma.split(sz);
			try {
				double xsize = Double.parseDouble(sizes[0]);
				double ysize = xsize;
				if (sizes.length > 1) {
					ysize = Double.parseDouble(sizes[1]);
				}
				xscale = xsize / nx;
				yscale = ysize / ny;
			} catch (Exception e) {
				throw new Exception("Invalid size setting:" + sz, e);
			}

		} else if (Settings.get(Val.scale) != null) {
			String[] scales = comma.split(Settings.get(Val.scale));

			xscale = Double.parseDouble(scales[0]);

			if (scales.length > 1) {
				yscale = Double.parseDouble(scales[1]);
			} else {
				yscale = xscale;
			}

		} else {
			xscale = 1 / 3600.;
			yscale = xscale;
		}
		Settings.put(Val.Size, xscale * nx + "," + yscale * ny);

		double[] center = Projection.fixedPoint(proj);
		if (center != null) {
			p = new Projection(proj);

			// Does the user want a non-standard center for
			// a fixed projection?
			if (Settings.has(Val.RefCoords)) {
				String[] coords = Settings.getArray(Val.RefCoords);
				try {
					double lon = Math.toRadians(Double.parseDouble(coords[0]));
					double lat = Math.toRadians(Double.parseDouble(coords[1]));
					if (lon != center[0] || lat != center[1]) {
						p.setReference(lon, lat);
						Settings.err
								.println("  Using non-standard image center:"
										+ Settings.get(Val.RefCoords));
					} else {
						Settings.err
								.println("  New reference center matches original");
					}
				} catch (Exception e) {
					throw new Error("Error resetting reference coordinates to:"
							+ Settings.get(Val.RefCoords), e);

				}
			}

			// Find where the requested center is with respect to
			// the fixed center of this projection.
			Converter cvt = new Converter(new Transformer[]{
				p.getRotater(),p.getProjecter()});

			double[] uv = Vector3d$.MODULE$.rade2Vector(Math.toRadians(lon),
					Math.toRadians(lat)).toArray();
			double[] coords = cvt.transform(uv);
			s = new Scaler(0.5 * nx + coords[0] / Math.toRadians(xscale), 0.5
					* ny - coords[1] / Math.toRadians(yscale), -1
					/ Math.toRadians(xscale), 0, 0, 1 / Math.toRadians(yscale));

		} else {

			p = new Projection(proj, new double[] { Math.toRadians(lon),
					Math.toRadians(lat) });
			s = new Scaler(0.5 * nx, 0.5 * ny, -1 / Math.toRadians(xscale), 0,
					0, 1 / Math.toRadians(yscale));
		}

		String rot = Settings.get(Val.Rotation);

		if (rot != null && rot.length() > 0) {
			double angle;
			try {
				angle = Math.toRadians(Double.parseDouble(rot));
			} catch (Exception e) {
				throw new Exception("Invalid rotation setting:" + rot);
			}
			Scaler rScale = new Scaler(0, 0, Math.cos(angle), Math.sin(angle),
					-Math.sin(angle), Math.cos(angle));
			s = rScale.add(s);
		}

		if (Settings.has(Val.Offset)) {
			double[] deltas = new double[2];
			try {
				String[] offsets = Settings.getArray(Val.Offset);
				if (offsets.length == 2) {
					deltas[0] = -Double.parseDouble(offsets[0]);
					deltas[1] = -Double.parseDouble(offsets[1]);
				} else {
					deltas[0] = -Double.parseDouble(offsets[0]);
					deltas[1] = deltas[0];
				}
				Scaler translate = new Scaler(deltas[0], deltas[1], 1, 0, 0, 1);
				s = s.add(translate);

			} catch (Exception e) {
				Settings.err.println("Error parsing/applying offset:"
						+ Settings.get(Val.Offset));
			}
		}
		return new WCSFits(c, p, null,s,
				null,null);
	}

	private WCSFits copyWCS(String file) throws Exception {
		if (!new File(file).exists())
			throw new FileNotFoundException("File not found:" + file);
		// This should only read the first header of the file.
		Header hdr = new Header(new Fits(file).getStream());
		return WCSFits.fromNormal(hdr);
	}

	/**
	 * Get an Imager object -- normally the last one created.
	 */
	public static Imager getImager() {
		if (lastImager == null) {
			new Imager();
		}
		return lastImager;
	}

	private class ImageState {
		private Image[] sources;
		private Image output;
		// private int[] match;
		private Sampler samp;
		private ArrayList<Processor> procs;

		ImageState(Image[] sources, Image output, int[] match, Sampler samp,
				ArrayList<Processor> procs) {
			this.sources = sources;
			this.output = output;
			// this.match = match;
			this.samp = samp;
			this.procs = (ArrayList<Processor>) procs.clone(); // What processes
																// have already
																// been applied?
		}
	}

	public static void clear() {
		doneImages.clear();
		lastImager = null;
	}
}
