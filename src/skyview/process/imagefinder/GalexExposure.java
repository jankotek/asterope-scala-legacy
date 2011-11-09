package skyview.process.imagefinder;

import nom.tam.fits.Header;
import org.apache.commons.math.geometry.Vector3D;
import org.asterope.util.*;

import skyview.survey.FitsImage;
import skyview.survey.Image;
import skyview.survey.ProxyImage;

import java.util.ArrayList;

/** This class extends the MaxExposure class to handle
 *  GALEX images where the center of the field is not necessarily
 *  the center of the exposed data.
 */
public class GalexExposure extends MaxExposure {
    
    /** We store the centers for each input in this
     *  array list so that we don't need to recompute it
     *  for evey pixel.
     */
    private ArrayList<double[]> centers = new ArrayList<double[]>();
    
    /** Find the square of the offset from the center of the field of view.
     */
    protected double radiusSquared(double tx, double ty, 
				   double nx, double ny,
				   int index, Image input) {
	
	double[] center = null;
	if (index < centers.size()) {
	    center = centers.get(index);
	} else {
	    for (int i=centers.size(); i<= index; i += 1) {
		centers.add(null);
	    }
	}
	
	// If we already know the center of the image
	// we can just calculate the offset.
	if (center == null) {
	    
	    if (!input.valid()) {
		input.validate();
	    }
	    
	    FitsImage img;
	    if (input instanceof FitsImage) {
		img = (FitsImage) input;
	    } else if (input instanceof ProxyImage) {
		img = (FitsImage) ((ProxyImage) input).getBaseImage();
	    } else {
		System.err.println("  Invalid image type for GALEX image");
		centers.set(index, new double[]{Double.NaN, Double.NaN});
		return Double.NaN;
	    }
	    Header hdr = img.getHeader();
	    double actRA   = Math.toRadians(hdr.getDoubleValue("AVASPRA"));
	    double actDec  = Math.toRadians(hdr.getDoubleValue("AVASPDEC"));

        Vector3D temp = org.asterope.util.package$.MODULE$.rade2Vector(actRA, actDec);
	    double[] pixelCenter = getImage(input, new double[]{temp.getX(),  temp.getY(), temp.getZ()});
	    center = pixelCenter.clone();
	    centers.set(index,center);
	}
	
	// Calculate the offset (squared).
	return ( (tx-center[0])*(tx-center[0]) + (ty-center[1])*(ty-center[1]) );
    }
}
