package skyview.process.imagefinder;

import skyview.executive.Settings;
import skyview.executive.Val;
import skyview.survey.FitsImage;
import skyview.survey.Image;

/** This class selects the best image for a pixel by
 *  looking for the image with the longest exposure
 *  that has the pixel in the field of view.
 *  Use of this Finder may not be optimal when using higher
 *  order samples, since it will tend to take images out
 *  to the edges and thus may have problems there.
 *  May wish to specify the MinEdge setting to cope with tht.
 */
public class MaxExposure extends RectRecurse {
    
    private double[] exposures;
    String  expKey = "EXPOSURE";
    
    /** Find the appropriate images.
     *  This routine gets the exposures for all of the
     *  images before calling the standard BorderImageFinder.
     */
    public int[] findImages(Image[] input, Image output) {
	
	exposures = new double[input.length];
	java.util.Arrays.fill(exposures, -1);
	expKey = Settings.get(Val.exposurekeyword);
	return super.findImages(input, output);
    }
	
    
    /** The criterion for the best image */
    protected double criterion(int index, int nx, int ny, double tx, double ty) {
	if (exposures[index] < 0) {
	    Image img = getCandidate(index);
	    img.validate();
	    img  = img.getBaseImage();
	    if (img instanceof FitsImage) {
		exposures[index] = ((FitsImage) img).getHeader().getDoubleValue(expKey, 0);
	    } else {
		exposures[index] = 0;
	    }
	}
	return exposures[index];
    }
}
