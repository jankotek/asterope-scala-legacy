package skyview.survey;


/** This class instantiates DSS images.
 *  These are read only images.
 */

import nom.tam.fits.Header;
import nom.tam.fits.HeaderCard;
import skyview.executive.Settings;
import skyview.executive.Val;
import org.asterope.util.*;

import java.io.*;
import java.net.URL;

/** This class defines an image as the combination
 *  of a set of pixel values and a WCS describing the
 *  pixel coordinates.
 */
public class DSSImage extends Image {
    
    // Define an array of 28x28 pointers to a double array.
    private int[][] dssData;
    private static final char[] suffixes={'0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
				    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j',
				    'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't',
                                    'u', 'v', 'w', 'x', 'y', 'z'};
    
    private String directory;
    private String plate;

    private String[] urlPrefixes = Settings.getArray(Val.LocalURL);
    private int nTiles   = 28;
    private int tileSize = 500;
    
    private int xSize;

    private int subimageCount = 0;
    private int[] dssIndex = new int[60];
    
    /** Construct a DSS image */
    public DSSImage (String directory) throws Exception {
	
	// DSS1 images are scanned as a 14000x13999 image and
	// broken up into 28x28 tiles of 500x500 pixels.
	// DSS2 images are scanned as a 23040x23040 image and
	// broken up into 30x30 tiles of 768x768 pixels.
	// The DSS header files do not seem to have any information
	// specifying the tiling, so that this is encoded
	// directly in the source code.
	
	this.directory   = directory;
	int slash = directory.lastIndexOf('/');
	
	plate     = directory.substring(slash+1);
	Header h  = getHeader(directory+"/"+plate+".hhh");
	WCSFits w  = WCSFits.fromDSS(h);
	int[]  axes = w.headerNaxis();
	xSize = axes[0];
    int ySize = axes[1];
	if (xSize > 14000) {
	    nTiles   = 30;
	    tileSize = 768;
	}
	
	initialize(null, w, xSize, ySize, 1);
	setName(plate);
    }
    
    /** Get a pixels data associated with the image. */
    public double  getData(int npix) {
	
	if (dssData == null) {
	    dssData = new int[nTiles*nTiles][];
	}
	
	int x = npix % xSize;
	int y = npix / xSize;
	
	int px = x/tileSize;
	int py = y/tileSize;
	
	
	x = x%tileSize;
	y = y%tileSize;
	
	int tile = px + nTiles*py;
	if (dssData[tile] == null) {
	    try {
		String fname = plate+'.'+suffixes[py]+suffixes[px];
		String file = directory+"/"+fname;
		file = Util.replacePrefix(file, urlPrefixes);

	     dssData[tile] = HDecompressor.decompress(getFile(file,fname));
	    } catch (Exception e) {
		throw new Error("Unable to decompress file:"+ directory+"/"+plate+'.'+suffixes[py]+suffixes[px],e);
	    }

	    // Keep only the last few subimages in memory to avoid
	    // using it all up!
	    int idx = subimageCount%dssIndex.length;
	    if (subimageCount > dssIndex.length) {
		dssData[dssIndex[idx]] = null;
	    }
	    dssIndex[idx]  = tile;
	    subimageCount += 1;
	}
//	System.err.printf("Pixel: %d, px,py: %d %d; x,y: %d %d, val %d\n",
//			         npix, px, py, x, y, dssData[tile][x+tileSize*y]);
			  
	return (double) dssData[tile][x+tileSize*y];
    }

    public InputStream getFile(String file, String fname) throws Exception{
        if (file.startsWith("http:")) {
            return GetURL.apply(new URL(file),false);
        } else {
            return new BufferedInputStream(new FileInputStream(file));
        }

    }
    
    /** Get the data as an array */
    public double[] getDataArray() {
	throw new Error("getDataArray: Invalid operation on DSS image");
    }
    
    /** Set the Data associated with the image.
     */
    public void setData(int npix, double newData) {
	throw new Error("setData: Invalid operation on DSS image");
    }
    
    /** Clear the data array */
    public void clearData() {
	dssData = null;
    }
    
    /** Set the data array */
    public void setDataArray(double[] newData) {
	throw new Error("setDataArray: Invalid operation on DSS image");
    }
    
    private Header getHeader(String headerFile) throws Exception {
	
	byte[] buf = new byte[80];
	Header h   = new Header();

	
	headerFile = Util.replacePrefix(headerFile, urlPrefixes);
	String cacheFile = headerFile.substring(headerFile.lastIndexOf("/")+1);
	DataInputStream in = new DataInputStream(
			getFile(headerFile,cacheFile));
	nom.tam.util.Cursor c = h.iterator();
        while (true) {
	    in.readFully(buf);
	    
	    String s = new String(buf);
	    HeaderCard hc = new HeaderCard(s);
	    String key = hc.getKey();
	    if (!key.equals("") && !key.equals("COMMENT") && !key.equals("HISTORY")) {
	        c.add(hc.getKey(), hc);
	    }
	    if (s.substring(0, 4).equals("END ")) {
		break;
	    }
	}
	// Putting  an offset of 1 matches the results with the
	// old SkyView pages.  This gets some support when we
	// download cutouts from the ST image server which seems
	// to have an offset also.
	// 
	// Note that this is only for the old DSS headers.
	// The new ones have CNPIX1/2 = 0
	
	if (h.getDoubleValue("CNPIX1") == 1  &&
	    h.getDoubleValue("CNPIX2") == 1) {
	    h.addValue("CNPIX1", 0, null);
	    h.addValue("CNPIX2", 0, null);
	}
	return h;
    }
    
    /** Is this image tiled */
    public boolean isTiled() {
	return true;
    }
}
	
