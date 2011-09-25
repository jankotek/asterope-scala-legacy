package skyview.survey;


import org.asterope.geometry.Transformer;
import org.asterope.geometry.WCS;

/** This class defines an image as the combination
 *  of a set of pixel values and a WCS describing the
 *  pixel coordinates.  some subclasses of the Image
 *  class may be 'read-only' images and may throw
 *  an Error if the user attempts to set the data values.
 */
public class Image implements skyview.Component {
    
    /** The WCS describing the transformation from
     *  the reference sphere to pixel coordinates.
     */
    private WCS         wcs;
    
    /** The data in the image. It should have dimension nx*ny*nz */
    protected double[]  data;
    
    /** The dimensions of the image.  In pixel space the image occupies 0-nx, 0-ny. */
    private int         nx,ny;
    
    /** The number of planes in the image. */
    private int         nz;
    
    /** An initial name */
    private String iName = "GenericImage";
    
    /** Should the data in the array be added to or reset */
    private boolean accumulate;
    
    /** Null constructor to be used in overriding classes */
    public Image() {
    }
    
    /** Get the name of the image */
    public String getName() {
	return iName;
    }
    
    /** set the name of the image */
    protected void setName(String name) {
	iName= name;
    }
    
    /** Get a description of the object */
    public String getDescription() {
	return "A set of pixel values along with a description of where in the sky they come from.";
    }
    
    /** Construct an image given the data and WCS with default depth */
    public Image (double[] data, WCS wcs, int width, int height){
	initialize(data, wcs, width, height, 1);
    }
    
    /** Construct an image given the data and WCS with specified depth */
    public Image (double[] data, WCS wcs, int width, int height, int depth) {
	initialize(data, wcs, width, height, depth);
    }
    
    /** Initialize an image. 
     *  Probably should be a protected method but used in some org.asterope.geometry calls.
     */
    public void initialize(double[] data, WCS wcs, int width, int height, int depth) {
	  
	this.data    = data;
	this.wcs     = wcs;
	this.nx      = width;
	this.ny      = height;
	this.nz      = depth;
    }
    
    /** Get the WCS associated with the image. */
    public WCS getWCS() {
	return wcs;
    }
    
    /** Get a pixels data associated with the image. */
    public double  getData(int npix) {
	return data[npix];
    }
    
    /** Get the data as an array */
    public double[] getDataArray() {
	return data;
    }
    
    /** Set the accumulation mode. */
    public void setAccumulate(boolean flag) {
	accumulate = flag;
    }
    
    /** Set the Data associated with the image.
     */
    public void setData(int npix, double newData) {
	if (data == null) {
	    data = new double[nx*ny*nz];
	}
	if (accumulate) {
	    data[npix] += newData;
	} else {
	    data[npix] = newData;
	}
    }
    
    /** Clear the data array */
    public void clearData() {
	data = null;
    }
    
    /** Set the data array */
    public void setDataArray(double[] newData) {
	data = newData;
    }
	
    /** Get the transformation to the pixel coordinates of the image */
    public Transformer getTransformer() {
	return wcs;
    }
    
    /** Get the width of the image */
    public int getWidth() {
	return nx;
    }
    
    /** Get the height of the image */
    public int getHeight() {
	return ny;
    }
    
    /** Get the number of planes in the image */
    public int getDepth() {
	return nz;
    }
    

    
    private double[] center = new double[2];
    
    /** Get the center position of the given output pixel */
    public double[] getCenter(int npix) {
	center[0] = npix%nx + 0.5;
	center[1] = npix/nx + 0.5;
	return center;
    }
    
    private double[][] corners = new double[2][4];
    
    /** Get the corners of the given output pixel */
    public double[][] getCorners(int npix) {
	
	corners[0][0] = npix%nx;
	corners[1][0] = npix/nx;
	corners[0][1] = corners[0][0] + 1;
	corners[1][1] = corners[1][0];
	corners[0][2] = corners[0][1];
	corners[1][2] = corners[1][0] + 1;
	corners[0][3] = corners[0][0];
	corners[1][3] = corners[1][2];
	return corners;
    }
    
    /** Make sure the image is ready for detailed use.  This
     *  may be overriden in classes which initially approximate the image.
     */
    public void validate(){
    }
    
    /** Is this image fully available? */
    public boolean valid() {
	return true;
    }
    
    /** Is this image tiled?  I.e., do we read in only
     *  a piece of the image at a time?
     */
    public boolean isTiled() {
	return false;
    }
    
    /** Get the base image if this is not the working image.
     */
    public Image getBaseImage() {
	// This image is its own base.
	return this;
    }
    
}
	
