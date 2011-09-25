package skyview.process.imagefinder;

/** This class defines the superclass of algorithms
 *  that match a pixel of an output image with the
 *  one of a set of input images from which that pixel
 *  with be associated.
 */
import skyview.Utilities;
import skyview.survey.Image;

/**
 * This class finds the best images to be used for sampling using a recursive
 * rectangle algorithm. It looks for rectangles the boundaries of which can all
 * be sampled from the same input image. <br>
*/
public abstract class ImageFinder {
    
    /** This is the basic method in the image finder.
     *  It gives the index of the appropriate input image
     *  for each pixel in the output image.
     *  @param input An array of images from which the output image is to
     *               be interpolated.
     *  @param output An output image to be generated from the input.  This
     *                method does not change the output image.
     *  @return An integer array of the same dimension as one plane of the output
     *          image.  Each element of the array gives the index of the input image
     *          that should be used to get the value of that point.  A value -2 indicates
     *          that there is no suitable input image.  A value -3 indicates that
     *          the output pixel is not physical (e.g., it lies outside the valid region
     *          in the projection plane).  Zero and positive values give the appropriate indices.
     */
    public abstract int[] findImages(Image[] input, Image output);
    
    
    /** Create an ImageFinder given a class name or return an instance
     *  of the default class if no name is given.
     */
    public static ImageFinder factory(String type) {
	if (type == null) {
	    return new skyview.process.imagefinder.Border();
	} else {
	    return (ImageFinder) Utilities.newInstance(type, "skyview.process.imagefinder");
	}
    }
    
    /** Do stricter tests of the best pixel match */
    public void setStrict(boolean strict) {
    }
}
