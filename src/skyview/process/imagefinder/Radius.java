package skyview.process.imagefinder;

/** This class finds the best images to be used for sampling using
 *  a recursive rectangle algorithm.  It looks for rectangles the
 *  boundaries of which can all be sampled from the same input image.
 *  <br>
 */
public class Radius extends Border {
    
    
    /** Being close to the center is the goal.
     * 
     *  @return The inverse of 4 r^2.  This is a little faster to computer
     *          so we use this rather than inverse of the radius.
     */
    
    protected double criterion(int i, int nx, int ny, double x, double y) {
	
        // How far are we from the center of the image?
	double delta = (2*x-nx)*(2*x-nx) + (2*y-ny)*(2*y-ny);
	if (delta == 0) {
	    return 1.e100;
	} else {
	    return 1/delta;
	}
    }
}
