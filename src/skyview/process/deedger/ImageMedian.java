package skyview.process.deedger;

import nom.tam.fits.Header;

import skyview.sampler.Sampler;
import skyview.survey.Image;

/** This class adjusts the 0 points for data taken
 *  from multiple images to try to minimize edge effects.
 *  This class just normalizes the medians of all image.
 */

public class ImageMedian implements skyview.process.Processor {
    
    /** The matrix of the number of edge pixels between two images */
    int[] counts;
    
    /** The total number of edge pixels */
    int totalCount;
    
    /** The number of candidate source images */
    int nImage;
    
    /** The source images */
    Image[] inputs;
    
    /** The image whose edges are to be removed. */
    Image output;
    
    /** The array giving the source image for each pixel in the input image. 
     */
    int[] source;
    
    /** The dimesions of the image */
    int   nx;
    int   ny;
    
    /** The additive adjustment to be made to the image. */
    double[] offsets;
    
    /** Get a name for this object */
    public String getName() {
	return "Image Median Deedger:";
    }
    
    /** Get a description of this object */
    public String getDescription() {
	return "Normalize images to have the same median";
    }
    
     
    /** Initialized the de-edger.
     *  @param inputs  The input images.
     *  @param output  The output user image
     *  @param source  The index array
     *  @param samp    The spatial sampler (not used)
     *  @param dsamp   The energy sampler (not used)
     */
    
    public void process(Image[] inputs, Image output, int[] source, 
			Sampler samp) {
	
	if (output == null) {
	    return;
	}
	
	int nImage = inputs.length;
	
        counts = new int[nImage];
	
	
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
	
	offsets = new double[nImage];
	
	// Get the number of pixels for each input.
        for (int aSource : source) {
            if (aSource >= 0 && aSource < nImage) {
                counts[aSource] += 1;
            }
        }
	
	// Create the arrays we need to generate medians
	double[][] arrays = new double[nImage][];
	int[]      cnts   = new int[nImage];
	
	for (int j=0; j<arrays.length; j += 1) {
	    arrays[j] = new double[counts[j]];
	}
	
	// Sort the output pixels into arrays from each input
	for (int i=0; i<source.length; i += 1) {
	    int j = source[i];
	    arrays[j][cnts[j]] = output.getData(i);
	    cnts[j]           += 1;
	}
	
	// Find the median for each input image
	for (int j=0; j<nImage; j += 1) {
	    int cnt = counts[j];
	    double[] arr = arrays[j];
	    if (cnt > 0) {
		java.util.Arrays.sort(arr);
		if (cnt % 2 == 0) {
		    offsets[j] = 0.5*(arr[cnt/2-1]+ arr[cnt/2]);
		} else {
		    offsets[j] = arr[cnt/2];
		}
	    }
	}
	
	// Normalize everything to the image with the maximum number of pixels
	int jmax = 0;
	for (int j=1; j<nImage; j += 1) {
	    if (counts[j] > counts[jmax]) {
		jmax = j;
	    }
	}
	
	double norm = offsets[jmax];
	for (int j=0; j<nImage; j += 1) {
	    if (counts[j] > 0) {
		offsets[j] -= norm;
		offsets[j] *= -1;
	    }
	}
	    
	
	// Now adjust the image by adding in all of the offsets we just computed.
	for (int i=0; i<source.length; i += 1) {
	    double offset = offsets[source[i]];
	    if (offset != 0) {
		output.setData(i, output.getData(i)+offset);
	    }
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
		    h.insertHistory("Edge adjustments applied (skyview.geometry.MedianFilter");
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
