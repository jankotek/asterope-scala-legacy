package skyview.process.deedger;

import nom.tam.fits.Header;
import skyview.sampler.Sampler;
import skyview.survey.Image;

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
 *    <li> Add the jump at each edge pixel to the any previous
 *         jumps for these two source images.  When we finish
 *         we have a matrix of the total deltas along the edges
 *         shared by each pair of images.  We compute the delta's
 *         in both directions, so that deltas[a][b] = - deltas[b][a]
 *         Also keep track of the number of edge pixels for each pair
 *         of images.  [Note that many of the candidate images will
 *         not have been used in any pixels in the output image.  This
 *         will simply correspond to empty rows and columsn in the deltas
 *         and counts matrices.]
 *    <li> Create an offset array with a NaN offset for each input image.
 *    <li> Find the source image which has the maximum number of
 *         pixels in the output image.  Make this the base
 *         image with an offset of 0.
 *    <li> Consider the source images in two sets: the set for which
 *         an offset has been defined, and a set for which it has not.
 *         Initially only the base image is in the first set and all others
 *         are in the undefined set.
 *    <li> Find the largest entry in the counts matrix where the row corresponds
 *         to an image with a defined offset, and the column corresponds to an
 *         image with an undefined offset.
 *    <li> Assign an offset to the image associcated with the column such that
 *         the total delta along the edge should become 0.  Remember that
 *         the defined image will change too.
 *    <li> Iterate until all images have an offset defined.
 *  </ol>
 */

public class BoundaryAverage implements skyview.process.Processor {
    
    /** The matrix of edge shifts between images. */
    double[][] deltas;
    
    /** The matrix of the number of edge pixels between two images */
    int[][] counts;
    
    /** The total number of edge pixels */
    int totalCount;
    
    /** The number of candidate source images */
    int nImage;
    
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
    
    /** Get a name for this object */
    public String getName() {
	return "Boundary Average Deedger:";
    }
    
    /** Get a description of this object */
    public String getDescription() {
	return "Hide borders between input images by making the average shift across boundaries 0.";
    }
    
     
    /** Initialized the de-edger.
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
	
	deltas = new double[nImage][nImage];
        counts = new int[nImage][nImage];
	
	this.nImage = nImage;
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
	
	// Find the image that has the most pixels used in the output
	
	int[] tc = new int[nImage];
        for (int aSource : source) {
            if (aSource >= 0) {
                tc[aSource] += 1;
            }
        }
	
	int maxInd = 0;
	for (int i=1; i<nImage; i += 1) {
	    if (tc[i] > tc[maxInd]) {
		maxInd = i;
	    }
	}
	
	// Set up the offsets array and mark all images as  'offset undefined' to start with.
	offsets = new double[nImage];
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
	    
	    for (int i=0; i<nImage; i += 1) {
		// We want i to point to a 'defined' source image.
		if (Double.isNaN(offsets[i])) {
		    continue;
		}
		for (int j=0; j<nImage; j += 1) {
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
	    
	    double xOffset = deltas[maxI][maxJ];
	    // If delta is positive, then the I'th image pixels were
	    // smaller than the J'th.
	    offsets[maxJ] = offsets[maxI] - xOffset/counts[maxI][maxJ];
	}
	
	int xccc = 0;
	
	for (int i=0; i<nImage; i += 1) {
	    if (Double.isNaN(offsets[i])) {
		offsets[i] = 0;
	    }
	}
	
	// Now adjust the image by adding in all of the offsets we just computed.
	for (int i=0; i<nx*ny; i += 1) {
	    double offset = offsets[source[i]];
	    if (offset >= 0) {
		output.setData(i, output.getData(i)+offset);
		xccc += 1;
		
	    }
	}
    }
    
    /** Is there an edge between two pixesl?
     *  If so, then the edge matrices are updated.
     */
    private void check(int t0, int t1) {
	
	
	int s0 = source[t0];
	int s1 = source[t1];
	
	// Are they from the same image?  
	// Don't include edges in the actual image.
	
	if (s0 >= 0 && s1 >= 0 && s0 != s1) {
	    totalCount += 1;
	   
	    double v0 = output.getData(t0);
	    double v1 = output.getData(t1);
	    
	    // We could probably use a triangular matrix, but
	    // at the cost of making the rest of the program
	    // a little more complex.
	    deltas[s0][s1] += v1-v0;
	    deltas[s1][s0] += v0-v1;
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
	    if (offsets[i] != 0) {
		if (first) {
		    h.insertHistory("");
		    h.insertHistory("Edge adjustments applied (org.asterope.geometry.Deedger");
		    h.insertHistory("");
		    first = false;
		}
		    
	        String cfile = inputs[i].getName();
		if (cfile.lastIndexOf('/') > 0) {
		    cfile = cfile.substring(cfile.lastIndexOf('/')+1);
		}
		h.insertHistory("     Image "+cfile+
			        " offset by "+offsets[i]);
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
