package skyview.process.imagefinder;

import org.asterope.geometry.Transformer;
import skyview.executive.Settings;
import skyview.executive.Val;
import skyview.survey.Image;

/**
 * This class finds the best images to be used for sampling using a recursive
 * rectangle algorithm. It looks for rectangles the boundaries of which can all
 * be sampled from the same input image. <br>
 * Settings are used by this class:
 * <dl>
 * <dt>MinEdge
 * <dt>Only images where we are at least this number of pixels from the edge
 * will be considered. The default is 0.
 * <dt>MaxRadius
 * <dt>Only images where the offset of the pixel from the center of the image is
 * less than this radius (calculations in pixels) will be considered. The
 * default is infinity.
 * <dt>CheckNaNs
 * <dt>is a flag whose value is ignored. If set, then images are checked to see
 * if the data value corresponding to a pixel position is a NaN when looking for
 * the best image. Images where a NaN is found are ignored. Note that this
 * applies only to the boundaries of the rectangles that are checked. If there
 * are NaNs in isolated regions or pixels these may still be used by this image
 * finder.
 * <dt>CornersOnly
 * <dd>is a flag whose value is ignored. If set, then only the corners of
 * rectanges are checked before the rectangle is filled in with values. This was
 * essentially the behavior of the default image finder prior to v2.02 of
 * Skyview.
 * </dl>
 * <ul>
 * <li>The output image is sampled in a rectangular grid with the samples spaced
 * no more than half the image size of the input images.
 * <li>For each rectangle, the corners of the image are projected into the frame
 * of each currently valid image in turn.
 * <li>If all four pixels project below, above, to the right or to the left of
 * the image bounds, then this image is marked as invalid for future
 * consideration in this rectangle or subrectangles. E.g., suppose that a given
 * corner is simultaneously below and to the left of the image (i.e., the corner
 * projects to pixel coordinates with x and y both less than 0). If the other
 * three corners are also below the image, even if some are to the right of the
 * image or some have X-values that would be within the image, then the image
 * can be eliminated from future consideration. However, if even one of the
 * other pixels is not below the image, then we can not use this criterion to
 * eliminate the image, even if that corner is also outside the image. Similarly
 * if the other three corners all project to the left of the image, then the
 * image can be eliminated.
 * 
 * <li>The best pixel is found for each corner. The algorithm for the best pixel
 * is defined by subclasses. If the pixel does not fall within any image it is
 * marked as NO_COVERAGE.
 * <li>A pixel may not correspond to any position on the sky (e.g., a pixel
 * outside the elliptical bounds the Aitoff projection). These pixels are marked
 * as NON-PHYSICAL
 * <li>If the best images are the same for all four corners, then the edges of
 * the rectangle is also checked. If the entire border comes from the same
 * candidate then the rectangle is filled in as coming from that sample.
 * <li>Otherwise the algorithm checks to see if the the values along the
 * horizontal or veritical edges are the same. If so, we split the rectangle in
 * half, so that the corners that match are kept together. If not we do a
 * recusion splitting the rectangle into quarters.
 * </ul>
 */
public abstract class RectRecurse extends ImageFinder {

	/** Transformation temporaries */
	private double[] t2 = new double[2];
	private double[] t3 = new double[3];

	/** Is a given image used in the transformation */
	private boolean[] imageUsed;

	/** The transformation from the output pixels to the celestial sphere */
	protected Transformer fromOut;

	private int pixelCount = 0;
	private int rectCount = 0;

	/** The output image */
	private Image output;

	/** The input images. */
	private Image[] input;

	/**
	 * Do we wish to check that the input image does not have a NaN value?
	 */
	private boolean checkNaNs = false;

	/**
	 * Do we want to only check on the corners of the rectangles and not the
	 * edges.
	 */
	private boolean cornersOnly = false;

	// Special checks?
	private double edgeMin = 0.;
	private boolean radiusCheck = false;
	private double radMaxSq;

	/**
	 * Should we retry pixels when we get a no coverage?
	 */
	private boolean retry = false;

	public static final int UNCHECKED = -1;
	public static final int NO_COVERAGE = -2;
	public static final int NON_PHYSICAL = -3;
	public static final int CONSUMED = -4;
	public static final int SPLIT_X = -16;
	public static final int SPLIT_Y = -32;
	public static final int SPLIT_XY = -48;

	/** The index giving the best image for each pixel */
	private int[] img;

	/**
	 * Find the best image for each output pixel.
	 * 
	 * @input An array of images that may be sampled to get the output image.
	 * @input The output image. In this routine we are interested in its
	 *        geometry, not its data.
	 * @return An index array which for each pixel in the output image gives the
	 *         best image to sample. Note that this has dimension int[nx*ny]
	 *         where nx changes most rapidly. The values of the index array can
	 *         be:
	 *         <ul>
	 *         <li>&gt;= 0: The pixel is best indexed with the given image.
	 *         <li>-1: [internal] The best image for this pixel has not yet been
	 *         determined.
	 *         <li>-2: This pixel is not on any of the input images.
	 *         <li>-3: This pixel does not represent a physical coordinate.
	 *         <li>-4: [in other methods] this pixel has already been processed.
	 *         </ul>
	 */
	public int[] findImages(Image[] input, Image output) {

		if (input == null || input.length == 0) {
			return null;
		}

		int np = output.getWidth() * output.getHeight();
		imageUsed = new boolean[input.length]; // set to false on
												// initialization.
		this.input = input;

		if (Settings.has(Val.findretry)) {
			retry = true;
		}
		try {
			this.fromOut = output.getTransformer().inverse();
		} catch (Exception e) {
			throw new Error("In findImages: Unexpected transformation error:"
					+ e);
		}
		this.output = output;

		// See if the user has requested that we not include
		// images where the data value is NaN.
		checkNaNs = Settings.has(Val.CheckNaNs);

		// Do we want to skip the edge checks?
		cornersOnly = Settings.has(Val.CornersOnly);

		if (checkNaNs) {
			for (Image anInput : input) {
				anInput.validate();
			}
		}

		// Minimum edge distance?
		if (Settings.has(Val.MinEdge)) {
			edgeMin = Double.parseDouble(Settings.get(Val.MinEdge));
		}

		if (Settings.has(Val.maxrad)) {
			double radMax = Double.parseDouble(Settings.get(Val.maxrad));
			// More convenient to compute (r)^2
			radMaxSq = radMax * radMax;
			radiusCheck = true;
		}

		// Define an array that gives the input image for each pixel.
		img = new int[np];
		java.util.Arrays.fill(img, UNCHECKED);

		// Let's get the relative scales of the input and output images
		double sin = input[0].getWCS().getScale();
		double sout = output.getWCS().getScale();

		// Find the smaller dimension of the input images.
		// We assume that the first image is typical even
		// if not all images have the same geometry.
		int nx = input[0].getWidth();
		int ny = input[0].getHeight();

		if (ny < nx) {
			nx = ny;
		}

		// Don't go more than half the size of an input image between checks.
		int maxDelta = (int) (nx * sin / (2 * sout));
		if (maxDelta < 1) {
			maxDelta = 1;
		}

		// At this top level we assume all images are eligible.
		boolean[] valid = new boolean[input.length];

		java.util.Arrays.fill(valid, true);

		int mx = output.getWidth();
		int my = output.getHeight();

		// Loop over the output image grid.
		for (int i = 0; i < mx; i += maxDelta) {

			for (int j = 0; j < my; j += maxDelta) {

				int ip = i + maxDelta - 1;
				if (ip >= mx) {
					ip = mx - 1;
				}
				int jp = j + maxDelta - 1;
				if (jp >= my) {
					jp = my - 1;
				}

				// Check the rectangle
				checkRectangle(valid, i, ip, j, jp, mx);
			}
		}

		// Let the user know how many images are actually used in the
		// resampling.
		int count = 0;
		for (int i = 0; i < input.length; i += 1) {
			if (imageUsed[i]) {
				count += 1;
			}
		}

		if (count > 0) {
			if (count != input.length) {
				Settings.err.println("  " + count + " of " + input.length
						+ " candidates selected.");
			}
			return img;
		} else {
			return null;
		}
	}

	/** Return a particular candidate. */
	protected Image getCandidate(int i) {
		return input[i];
	}

	/**
	 * Set a strict geometry. This class ignores the strict geometry flag since
	 * it does boundary checking.
	 */
	public void setStrict(boolean flag) {
	}

	/**
	 * Handle a rectangle of the output image.
	 * 
	 * @param valid
	 *            Should this image be considered at this level of the
	 *            recursion?
	 * @param x0
	 *            The minimum x in the rectangle.
	 * @param x1
	 *            The maximum x in the rectangle.
	 * @param y0
	 *            The minimum y in the rectangle.
	 * @param y1
	 *            The maximum y in the rectangle.
	 * @param mx
	 *            The number of pixels in a row in the output image.
	 */
	private void checkRectangle(boolean[] valid, int x0, int x1, int y0,
			int y1, int mx) {

		rectCount += 1;
		if (rectCount > 5000 && rectCount % 1000 == 0) {
			Settings.err.println("  FindImage-pixels found:" + pixelCount
					+ " of " + img.length + "   Rectangles:" + rectCount
					+ "    " + 100 * pixelCount / img.length + "% complete");
		}

		int p00 = x0 + y0 * mx;
		int p01 = x1 + y0 * mx;
		int p10 = x0 + y1 * mx;
		int p11 = x1 + y1 * mx;

		// These are the flags to use for the next recursion
		// We'll fill this in when we check the corners.
		boolean[] newValid = new boolean[valid.length];

		int dx = x1 - x0 + 1;
		int dy = y1 - y0 + 1;

		// Check the corners
		int match = cornerMatch(new int[] { p00, p01, p10, p11 }, valid,
				newValid);

		if (dx < 2 && dy < 2) {
			// We were looking at two or four adjacent pixels so
			// they should be filled in. Even if they don't
			// match we can just return.
			return;
		}

		// Assume that we don't need another level of recursion
		boolean recurse = false;

		// Didn't find a match, so get ready for the next
		// level of recursion.
		if (match <= SPLIT_X) {
			recurse = true;

			if (match == SPLIT_X) {
				dx = (dx + 1) / 2;

			} else if (match == SPLIT_Y) {
				dy = (dy + 1) / 2;

			} else {
				dx = (dx + 1) / 2;
				dy = (dy + 1) / 2;
			}

		}

		if (!recurse && !cornersOnly) {

			if (edgeOff(match, p00, p01, 1, newValid)
					|| edgeOff(match, p10, p11, 1, newValid)) {
				// Found a problem on a horizontal border
				dx = (dx + 1) / 2;
				recurse = true;

			} else if (edgeOff(match, p00, p10, mx, newValid)
					|| edgeOff(match, p01, p11, mx, newValid)) {
				// Found a problem on a vertical border
				dy = (dy + 1) / 2;
				recurse = true;

			}
		}

		// Check to see if we need to go another level down.
		// Note that we are choosing a simple algorithm for
		// recursion. We could add checks to avoid recursing
		// to the single pixel/column/row level, but
		// for the nonce we're hoping the simplicity of the
		// algorithm balances the occasional extra checks.
		if (recurse) {

			for (int px = x0; px <= x1; px += dx) {
				for (int py = y0; py <= y1; py += dy) {

					int pxe = px + dx - 1;
					if (pxe > x1) {
						pxe = x1;
					}
					int pye = py + dy - 1;
					if (pye > y1) {
						pye = y1;
					}

					checkRectangle(newValid, px, pxe, py, pye, mx);
				}
			}
		} else {
			// Note that we've already checked all of the border pixels.
			// so just fill in the interior of the rectangle
			// Can't be sure if we checked the edges, so we'll fill
			// them in just in case.
			for (int j = y0; j <= y1; j += 1) {
				for (int i = x0; i <= x1; i += 1) {
					int offset = i + j * mx;
					if (img[offset] == UNCHECKED) {
						pixelCount += 1;
					}
					img[i + j * mx] = match;
				}
			}
		}

	}

	/**
	 * Check whether all pixels on an edge of a rectangle all have the same best
	 * fit image.
	 * 
	 * @param match
	 *            The value each edge is to be compared against.
	 * @param p0
	 *            The first pixel offset to be checked.
	 * @param pe
	 *            The last pixel offset to be checked.
	 * @param dp
	 *            The spacing between pixels (either 1 or the number of pixels
	 *            in a row).
	 * @param valid
	 *            Should we consider this image?
	 * @return true if there is a discrepant pixel, or false if all values are
	 *         the same as std.
	 */
	protected boolean edgeOff(int match, int p0, int pe, int dp, boolean[] valid) {

		boolean val = false;
		for (int p = p0; p <= pe; p += dp) {
			if (bestFit(p, valid, false) != match) {
				val = true;
				break;
			}
		}
		return val;
	}

	/**
	 * Check the corners of a rectangle.
	 * 
	 * @param corners
	 *            The corners of the rectangle. It is assumed that there are
	 *            four elements in corners, and they are in the order p00, p01,
	 *            p10, p11
	 * @param valid
	 *            The images to be checked at the current level of recursion.
	 * @param newValid
	 *            The images to be checked at the next level of recursion. This
	 *            routine will look at the corners of the image in relation to
	 *            the bounds of the valid images to decide which images to
	 *            search at a future level of recursion.
	 */
	protected int cornerMatch(int[] corners, boolean[] valid, boolean[] newValid) {

		for (int i = 0; i < input.length; i += 1) {
			if (!valid[i]) {
				continue;
			}
			// The following looks at the four corners and notes if
			// they are all to the top, bottom, left or right of the
			// image. If so, then we assume that this input image cannot
			// contribute to this rectangle.
			// If any point is in the image, or if
			// the points are arranged such that a corner
			// of the image can cut into the rectangle, then
			// neither of x/yside can reach +/-4.
			int xside = 0;
			int yside = 0;
			int nx = input[i].getWidth();
			int ny = input[i].getHeight();

			for (int corner : corners) {
				t2 = getImage(input[i], getCelest(corner));

				if (t2[0] < 0)
					xside -= 1;
				if (t2[0] > nx)
					xside += 1;
				if (t2[1] < 0)
					yside -= 1;
				if (t2[1] > ny)
					yside += 1;
			}
			if (Math.abs(xside) != 4 && Math.abs(yside) != 4) {
				newValid[i] = true;
			}
		}
		// Now find the best image for each corner.
		int[] best = new int[corners.length];
		for (int j = 0; j < corners.length; j += 1) {
			best[j] = bestFit(corners[j], valid, false);
		}
		int match = best[0];
		for (int i = 1; i < best.length; i += 1) {
			if (match != best[i]) {

				// If the pairs connected by horizontal lines are the
				// same, then we only need to halve in Y.
				if (best[0] == best[1] && best[2] == best[3]) {
					return SPLIT_Y;

					// If the pairs connected by vertical lines are the same
					// then
					// we only need to halve in x
				} else if (best[0] == best[2] && best[1] == best[3]) {
					return SPLIT_X;

					// Otherwise quarter the rectangle.
				} else {
					return SPLIT_XY;
				}
			}
		}
		return match;
	}

	/**
	 * Get the celestial coordinates corresponding to a given pixel.
	 * 
	 * @param pix
	 *            The pixel index.
	 * @return The celestial coordinates as a unit vector. This is returned as a
	 *         pointer to the field t3.
	 */
	private double[] getCelest(int pix) {
		double[] tp = output.getCenter(pix);
		fromOut.transform(tp, t3);
		return t3;
	}

	/**
	 * Given a point at x,y in an image of size a,b in the rectangle 0,a 0,b
	 * find the minimum distance to the edge. We assume that x,y is contained in
	 * the rectangle. If x,y is outside the rectangle, then this should return a
	 * negative number.
	 */
	protected double minDist(double x, double y, double a, double b) {
		return Math.min(Math.min(x, a - x), Math.min(y, b - y));

	}

	/**
	 * Convert an input unit vector to a position in an image.
	 * 
	 * @param img
	 *            The image we are transforming into the plane of.
	 * @param inp
	 *            The input unit celestial coordinate unit vector.
	 * @return The coordinate plane tuple. This is returned as a pointer to the
	 *         field t2.
	 */
	protected double[] getImage(Image img, double[] inp) {
		img.getTransformer().transform(inp, t2);
		return t2;
	}

	/**
	 * Find the best image to use for a given unit vector.
	 * 
	 * @param pix
	 *            The output pixel we are testing (pix = x + width*y)
	 * @param valid
	 *            Should we test this image
	 * 
	 * @return The best image, or special values.
	 */
	private int bestFit(int pix, boolean[] valid, boolean secondTry) {

		int val = img[pix];
		// First check to see if this pixel has already been done.
		if (val != UNCHECKED) {
			return val;
		}

		pixelCount += 1;

		t3 = getCelest(pix);

		// mx is the the greatest distance from an edge that we have found
		// so far. We start not knowing if any image contains
		// this pixel.
		double mx = -1;

		// We start by assuming that the position is not in any of the images.
		int best = NO_COVERAGE;

		// We might be off the projection...
		if (Double.isNaN(t3[0])) {
			img[pix] = NON_PHYSICAL;
			return NON_PHYSICAL;
		}

		// Check each image in turn.
		for (int i = 0; i < input.length; i += 1) {

			if (secondTry || valid[i]) {
				t2 = getImage(input[i], t3);

				double tx = t2[0];
				double ty = t2[1];

				int nx = input[i].getWidth();
				int ny = input[i].getHeight();

				double mn = minDist(tx, ty, nx, ny);

				// Are we inside the image?
				if (mn >= edgeMin) {
					if (radiusCheck) {
						double radsq = radiusSquared(tx, ty, nx, ny, i,
								input[i]);
						if (radsq > radMaxSq) {
							continue;
						}
					}

					// Do we need to check the data value?
					if (checkNaNs) {
						int px = (int) tx;
						int py = (int) ty;

						if (Double.isNaN(input[i].getData(px + nx * py))) {
							continue;
						}
					}

					double crit = criterion(i, nx, ny, tx, ty);
					if (crit > mx) {
						mx = crit;
						best = i;
					}
				}
			}
		}

		// Note that this image will be used when we mosiack the image.
		if (best >= 0) {
			imageUsed[best] = true;
		}

		if (best == NO_COVERAGE) {
			if (retry && !secondTry) {
				best = bestFit(pix, valid, true);
			}
		}
		img[pix] = best;
		return best;
	}

	/**
	 * The default for this is to use the radius from the center of the image.
	 * The last two arguments may be used in extending classes. when the center
	 * of the image is not the center of the field of view.
	 */
	protected double radiusSquared(double tx, double ty, double nx, double ny,
			int index, Image input) {
		return (tx - 0.5 * nx) * (tx - 0.5 * nx) + (ty - 0.5 * ny)
				* (ty - 0.5 * ny);
	}

	/**
	 * The criterion function should return a larger value for more desirable
	 * inputs.
	 * 
	 * @param image
	 *            The index of the image being considered
	 * @param nx
	 *            ,ny The size of the candidate image
	 * @param tx
	 *            ,ty Where the desired pixel would be sampled from the
	 *            candidate.
	 */
	protected abstract double criterion(int image, int nx, int ny, double tx,
			double ty);

	/** Debugging output */
	public void printOut(int[] arr, int mx) {

		int off = 0;
		while (off < arr.length) {

			if (arr[off] < 0) {
				System.err.print(" " + arr[off]);
			} else {
				System.err.print("  " + arr[off]);
			}
			off += 1;
			if (off % mx == 0) {
				System.err.println("");
			}
		}
	}
}
