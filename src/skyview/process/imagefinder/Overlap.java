package skyview.process.imagefinder;

import org.asterope.geometry.Transformer;
import skyview.survey.Image;

/** This class is a simple image finder which returns all images
 *  that may have some overlap with the output image.
 *  Unlike the conventional image finders it does not return
 *  an integer array giving the image to sample for each output
 *  pixel.  A zero-element array is returned and is not expected
 *  to be used.  However in the input image array, any image
 *  where there is apparently no overlap between the input image
 *  and the output image is set to null.
 *  <p>
 *  This image finder should not be used with the standard mosaicker.
 *  It is anticipated to be used in mosaickers which will analyze
 *  each input image for overlap, presumably because they are going to
 *  add data from multiple images.
 */
public class Overlap extends ImageFinder {
    
    /** Find the images that it makes sense to sample.
     * @input input  [may be modified] An array of images that may be sampled to get the output image.
     * On return from this routine, any elements of the array which do not overlap
     * with th output will be replaced by null.
     * @input output The output image. Only its geometry is used.
     * @return A zero-length dummy array or null if no input images
     *         overlap the output.
     */
    
    public int[] findImages(Image[] input, Image output) {
	
	if (input == null || input.length == 0) {
	    return null;
	}
	
	int imgCnt = 0;
	double ow = output.getWidth();
	double oh = output.getHeight();
	Transformer fromOut = null;
	
	double[][] coords = new double[4][3];
	double[] corners = new double[2];
	// Find the corners of the output image.
	try {
            fromOut = output.getTransformer().inverse();
	    corners[0] = 0;  corners[1] = 0;
	    fromOut.transform(corners, coords[0]);
	    
	    corners[0] = ow; corners[1] = 0;
	    fromOut.transform(corners, coords[1]);
	    
	    corners[0] = ow; corners[1] = oh;
	    fromOut.transform(corners, coords[2]);
	    
	    corners[0] = 0;  corners[1] = oh;
	    fromOut.transform(corners, coords[3]);
	} catch(Exception e) {
	    throw new Error("In findImages: Unexpected transformation error for output image:"+e);
	}
	
	
	for (int i=0; i< input.length; i += 1) {
	    if (overlap(coords, input[i])) {
		imgCnt += 1;
	    } else {
		input[i] = null;
	    }
	}
	System.err.println("  Overlap Finder: Number of candidates to process: "+imgCnt);
	if (imgCnt > 0) {
	    return new int[0];
	} else {
	    return null;
	}
	
    }
    
    /** See where the four corners of the output image show up
     *  in the test image.  If they are all to one side of the input,
     *  then there is (modulo distortions) no overlap between the two
     *  images.  We assume this is being used when both the input and
     *  output cover a relatively small fraction of the sky and we
     *  are not near any projection discontinuities so that we can
     *  ignore projection distortions.
     *  @param corners  The corners of the image
     *  @param test     The image we are checking for overlap.
     */
    private boolean overlap(double[][] corners, Image test) {
	
	// These count the number of times the corner pixel
	// is above, below or to each side of the input.
	
	int lower  = 0;
	int higher = 0;
	int left   = 0;
	int right  = 0;
	
	double[] pos = new double[2];
        for (double[] corner : corners) {
            try {
                test.getTransformer().transform(corner, pos);
                if (pos[1] < 0) {
                    lower += 1;
                } else if (pos[1] > test.getHeight()) {
                    higher += 1;
                }
                if (pos[0] < 0) {
                    left += 1;
                } else if (pos[0] > test.getWidth()) {
                    right += 1;
                }
            } catch (Exception e) {
                System.err.println("Error return: false");
                return false;
            }
        }
	
	// There is no overlap if all four corners are off the
	// image to the same side
	
	return lower != 4 && higher != 4 && left != 4 && right != 4;
    }
		      
}
