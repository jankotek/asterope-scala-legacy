package skyview.process.imagefinder;

import skyview.survey.Image;

/** This class is a simple image finder which returns all images
 *  and a 0 length array.
 */
public class Bypass extends ImageFinder {
    
    public int[] findImages(Image[] input, Image output) {
	
	if (input == null || input.length == 0) {
	    return null;
	}
	System.err.println("  ImageFinder bypassed:"+input.length+" images");
	return new int[0];
    }
}
