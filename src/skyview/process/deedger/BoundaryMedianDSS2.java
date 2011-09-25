package skyview.process.deedger;

import nom.tam.fits.Header;
import skyview.sampler.Sampler;
import skyview.survey.Image;

import java.util.ArrayList;

/** This class adjusts the 0 point for data taken
 *  from multiple images to try to minize edge effects.
 *  The alogithm used in this version is as follows:
 *  <ol>
 *    <li> Find all of the 'edge' pixels in the image
 *         and compute the jump over the edge.
 *    <ul>
 *        <li> We determine the edge pixels by looking at
 *             the source images of pixels which differ by one
 *             in the x or y dimension.  We sweep in both directions
 *        <li> The jump is simply the change in value between the
 *             two pixels.
 *    </ul>
 *    <li> Add the jump to the list of jumps between the two input images
 *         involved.  Increment a matrix counting jumps between image pairs.
 *    <li> Find the median jump for each pair of adjacent images.
 *         This will be used as the offset between these two images.
 *    <li> Create an absolute offset array with a NaN offset for each input image.
 *    <li> Find the source image which has the maximum number of
 *         pixels in the output image.  Make this the base
 *         image with an absolute offset of 0.
 *    <li> Consider the source images in two sets: the set for which
 *         an offset has been defined, and a set for which it has not.
 *         Initially only the base image is in the first set and all others
 *         are in the undefined set.
 *    <li> Find the largest entry in the counts matrix where the row corresponds
 *         to an image with a defined offset, and the column corresponds to an
 *         image with an undefined offset.
 *    <li> Set the offset of the undefined images as the offset
 *         of the defined image plus their relative offset.
 *    <li> Iterate until all input images have an offset defined.  If
 *         there are too few pixels in a boundary skip it.
 *    <li> Apply the calculated offsets to the appropriate pixels
 *         of the input image.
 *  </ol>
 */

public class BoundaryMedianDSS2 implements skyview.process.Processor {
    
    /** The matrix of edge shifts between images. */
    double[][] deltas;
    
    /** The arrays of edge pixels */
    ArrayList[][] shifts;
    
    /** The matrix of the number of edge pixels between two images */
    int[][] counts;
    
    /** The total number of edge pixels */
    int totalCount;
    
    /** The number of candidate source images */
    int nImage;
    
    /** The number of distinct DSS images */
    int rImage;
    
    /** The source images */
    Image[] inputs;
    
    /** The image whose edges are to be removed. */
    Image output;
    
    /** The array giving the source image for each pixel in the input image. 
     *  Note that we don't care about the actual source images -- we already
     *  have the pixel values.  We just want to locate where the edges are
     *  and know how to group the pixels for later adjustments.
     */
    int[] source;
    
    /** The dimesions of the image */
    int   nx;
    int   ny;
    
    /** The additive adjustment to be made to the image. */
    double[] offsets;
    
    /** The index of the real image corresponding to the tile
     *  in the inputs array.
     */
    int[] realImage;
    
    /** Get a name for this object */
    public String getName() {
	return "DSS2 Boundary Median Deedger";
    }
    
    /** Get a description of this object */
    public String getDescription() {
	return "Hide borders between input images by matching medians of border pixels: assume DSS2 style images";
    }
    
     
    /** Initialize the de-edger.
     *  @param inputs  The input images.
     *  @param output  The output user image
     *  @param source  The index array
     *  @param samp    The spatial sampler (not used)
     */
    
    public void process(Image[] inputs, Image output, int[] source, 
			Sampler samp) {
	
	if (output == null) {
	    return;
	}
	
	int nImage = inputs.length;
	realImage  = new int[nImage];
	
	java.util.HashMap<String, Integer> hm = new java.util.HashMap<String,Integer>();
	
	for (int i=0; i<nImage; i += 1) {
	    String name = inputs[i].getName();
	    int colon = name.indexOf(":");
	    if (colon < 0) {
		// This image apparently isn't used, since
		// we never got a real iamge.
		realImage[i] = -1;
	    } else {
	        name = name.substring(0, colon);
	        if (! hm.containsKey(name)) {
		    int val = hm.size();
		    hm.put(name, val);
	        } 
	        int ind = hm.get(name);
	        realImage[i] = ind;
	    }
	}
	int rImage = hm.size();
	
	
	deltas = new double[rImage][rImage];
	shifts = new ArrayList[rImage][rImage];
	for (int i=0; i <rImage; i += 1) {
	    shifts[i] = new ArrayList[rImage];
	}
        counts = new int[rImage][rImage];
	
	this.nImage = nImage;
	this.rImage = rImage;
	this.source = source;
	this.inputs = inputs;
	this.output = output;
	
	ny = output.getHeight();
	nx = output.getWidth();
	
	deedge();
    }
    
    /** Perform the de-edging of the image */
    private void deedge() {
	
	totalCount = 0;
	
	// Sweep through Y to find horizontal edges.
	for (int i=0; i< ny-1; i += 1) {
	    for (int j=0; j<nx; j += 1) {
		int t0 = j + i*nx;
		int t1 = t0 + nx;
		check(t0,t1);
	    }
	}
	
	// Sweep through X to find vertical edges.
	for (int i=0; i< ny; i += 1) {
	    for (int j=0; j<nx-1; j += 1) {
		int t0 = j + i*nx;
		int t1 = t0 + 1;
		check(t0,t1);
	    }
	}
	
	if (totalCount == 0) {
	    return;
	}
	
	// Calculate the median shifts for each boundary.
	for (int i=0; i<rImage; i += 1) {
	    for (int j=i+1; j<rImage; j += 1) {
		if (shifts[i][j] != null) {
		    ArrayList<Double> list = shifts[i][j];
		    int cnt = list.size();
		    java.util.Collections.sort(list);
		    double median;
		    if (cnt % 2 == 0) {
			median = 0.5*(list.get(cnt/2-1) + list.get(cnt/2));
		    } else {
			median = list.get(cnt/2);
		    }
		    deltas[i][j] =  median;
		    deltas[j][i] = -median;
		} else {
		    deltas[i][j] = 0;
		    deltas[j][i] = 0;
		}
	    }
	}
	
	// Find the image that has the most pixels used in the output
	int[] tc = new int[rImage];
	int offIm= 0;
        for (int aSource : source) {
            if (aSource >= 0) {
                tc[realImage[aSource]] += 1;
            } else {
                offIm += 1;
            }
        }
	
	int maxInd = 0;
	for (int i=1; i<rImage; i += 1) {
	    if (tc[i] > tc[maxInd]) {
		maxInd = i;
	    }
	}
	
	// Set up the offsets array and mark all images as  'undefined' to start.
	offsets = new double[rImage];
	java.util.Arrays.fill(offsets, Double.NaN);
	
	// Define the offset for the biggest source image.
	offsets[maxInd] = 0;
	
	// Now we're ready to start doing some adjusting.
	// The idea is that every image for which offsets is NaN
	// can be adjusted to match an image whose offset is already
	// set.  We look for the largest entry in the counts array with
	// one side adjusted, and one side not.
	while (true) {
	    
	    int maxI   = -1;
	    int maxJ   = -1;
	    int cCount = 0;
	    
	    for (int i=0; i<rImage; i += 1) {
		// We want i to point to a 'defined' source image.
		if (Double.isNaN(offsets[i])) {
		    continue;
		}
		for (int j=0; j<rImage; j += 1) {
		    // We want j to point to an 'undefined' source image.
		    if (!Double.isNaN(offsets[j])) {
			continue;
		    }
		    
		    // Is this the best so far?
		    if (counts[i][j] > cCount) {
			cCount = counts[i][j];
			maxI = i;
			maxJ = j;
		    }
		}
	    }
	    
	    // Didn't find anything so we assume we're done.
	    if (maxI < 0) {
		break;
	    }
	    
	    // Require a minimal overlap.  If there are just
	    // a few pixels we may get wild results.
	    if (cCount < 10) {
		break;
	    }
	    
	    // If delta is positive, then the I'th image pixels were
	    // smaller than the J'th.
	    offsets[maxJ] = offsets[maxI] - deltas[maxI][maxJ];
	}
	
	for (int i=0; i<rImage; i += 1) {
	    if (Double.isNaN(offsets[i])) {
		offsets[i] = 0;
	    }
	}
	
	// Now adjust the image by adding in all of the offsets we just computed.
	for (int i=0; i<nx*ny; i += 1) {
	    if (source[i] >= 0) {
		double offset = offsets[realImage[source[i]]];
		output.setData(i, output.getData(i)+offset);
	    }
	}
    }
    
    /** Is there an edge between two pixesl?
     *  If so, then the edge matrices are updated.
     */
    private void check(int t0, int t1) {
	
	int s0 = realImage[source[t0]];
	int s1 = realImage[source[t1]];
	
	// Are they from the same image?  
	// Don't include edges in the actual image.
	if (s0 >= 0 && s1 >= 0 && s0 != s1) {
	    totalCount += 1;
	    
	    int smin;
	    int smax;
	    double delta = output.getData(t1) - output.getData(t0);
	    if (s0 < s1) {
	        smin = s0;
	        smax = s1;
	    } else {
		smin  = s1;
		smax  = s0;
		delta = -delta;
	    }
	    
	    if (shifts[smin][smax] == null) {
		shifts[smin][smax] = new ArrayList();
	    } 
	    
	    ArrayList list = shifts[smin][smax];
	    list.add(delta);
	    
	    counts[s0][s1] += 1;
	    counts[s1][s0] += 1;
	}
    }
    
    /** Update a FITS header with the processing done here. */
    public void updateHeader(Header h) {
      
	try {
	
	boolean first = true;
	    
	// Only update header if we actually did something!
	if (offsets == null) {
	    return;
	}
	
	for (int i=0; i < nImage; i += 1) {
	    if (realImage[i] >= 0 && offsets[realImage[i]] != 0) {
		if (first) {
		    h.insertHistory("");
		    h.insertHistory("Edge adjustments applied (org.asterope.geometry.DeedgerList");
		    h.insertHistory("");
		    first = false;
		}
		    
	        String cfile = inputs[i].getName();
		if (cfile.lastIndexOf('/') > 0) {
		    cfile = cfile.substring(cfile.lastIndexOf('/')+1);
		}
		h.insertHistory("     Image "+cfile+
			        " offset by "+offsets[realImage[i]]);
	    }
	}
	if (!first) {
	    h.insertHistory("");
	}
	} catch (nom.tam.fits.FitsException e) {
	    System.err.println("Error updating header:"+e);
	    // Continue
	}
    }
}
