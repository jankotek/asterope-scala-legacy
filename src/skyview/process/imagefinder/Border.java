package skyview.process.imagefinder;


/** This class finds the best images to be used for sampling using
 *  using the criterion that the best candidate image is the one where the 
 *  pixel is furthest from the edge.
 */
public class Border extends RectRecurse {

    /** The criterion is the minimum distance to the edge of the image. */
    protected double criterion(int i, int nx, int ny, double tx, double ty) {
	return minDist(tx, ty, nx, ny);
    }
}
