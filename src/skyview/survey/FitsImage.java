package skyview.survey;

import nom.tam.fits.BasicHDU;
import nom.tam.fits.Fits;
import nom.tam.fits.Header;
import skyview.executive.Settings;
import skyview.executive.Val;
import org.asterope.util.GetURL;

/** This class defines an image gotten by reading a file */

public class FitsImage extends Image {
    
    private String fitsFile;
    private Header fitsHeader;
    
    public FitsImage(String file) throws SurveyException {
	
	Header h;
	org.asterope.geometry.WCS wcs;
	
	setName(file);
	data = null;
	
	this.fitsFile = file;
	
        nom.tam.util.ArrayDataInput inp = null;
	try {
		
	    Fits f = new Fits(file);
	    inp = f.getStream();
	
	    h = new Header(inp);
	    
	    //  Kludge to accommodate DSS2
	    if (h.getStringValue("REGION") != null) {
		setName(h.getStringValue("REGION")+":"+file);
	    }
	    
	} catch (Exception e) {
	    throw new SurveyException("Unable to read file:"+fitsFile);
	} finally {
	    if (inp != null) {
		try {
		    inp.close();
		} catch (Exception e) {
		}
	    }
	}
		   
	
        int naxis = h.getIntValue("NAXIS");
	if (naxis < 2) {
	    throw new SurveyException("Invalid FITS file: "+fitsFile+".  Dimensionality < 2");
	}
	int nx = h.getIntValue("NAXIS1");
	int ny = h.getIntValue("NAXIS2");
	int nz = 1;
	
	if (h.getIntValue("NAXIS") > 2) {
	    nz = h.getIntValue("NAXIS3");
	}
	
	if (naxis > 3) {
	    for(int i=4; i <= naxis; i += 1) {
		if (h.getIntValue("NAXIS"+i) > 1) {
		    throw new SurveyException("Invalid FITS file:"+fitsFile+".  Dimensionality > 3");
		}
	    }
	}
	
	try {
	    if (Settings.has(Val.PixelOffset)) {
		String[] crpOff= Settings.getArray(Val.PixelOffset);
		try {
		    double d1 = Double.parseDouble(crpOff[0]);
		    double d2 = d1;
		    if (crpOff.length > 0) {
			d1 = Double.parseDouble(crpOff[1]);
		    }
		    h.addValue("CRPIX1", h.getDoubleValue("CRPIX1")+d1, "");
		    h.addValue("CRPIX2", h.getDoubleValue("CRPIX2")+d2, "");
		} catch (Exception e) {
		    System.err.println("Error adding Pixel offset:"+Settings.get(Val.PixelOffset));
		    // Just go on after letting the user know.
		}
	    }
	    wcs = WCSFits.fromNormal(h);
	} catch (Exception e) {
	    throw new SurveyException("Unable to create WCS for file:"+fitsFile,e);
	}
	
	try {
	    initialize(null, wcs, nx, ny, nz);
	} catch(Exception e) {
	    throw new SurveyException("Error generating tranformation for file: "+file);
	}
	fitsHeader = h;
    }
    
    
    /** Defer reading the data until it is asked for. */
    public double getData(int npix) {
	
	Fits     f = null;
	Object   o;
	BasicHDU hdu;
	
	if (data == null) {
	    
	    try {
		// We're going to read everything, so
		// don't worry if it's a file or not.
		
		
		    java.net.URL url = new java.net.URL(fitsFile);
		    f = new Fits(GetURL.apply(url,false));
		    
	        hdu = f.readHDU();
	        o   = hdu.getData().getData();
		f.getStream().close();
	    } catch(Exception e) {
		throw new Error("Error reading FITS data for file: "+fitsFile+"\n\nException was:"+e);
	    }
	    
	    o = nom.tam.util.ArrayFuncs.flatten(o);
	    
	    // Data may not be double (and it may be scaled)
	    // We assume no scaling if the data is double...
	    if (! (o instanceof double[])) {
		
	        Header h = hdu.getHeader();
		double scale = h.getDoubleValue("BSCALE", 1);
		double zero  = h.getDoubleValue("BZERO", 0);
		
		// Bytes are signed integers in Java, but unsigned
		// in FITS, so if we are reading in a byte array
		// we'll need to convert the negative values.
		
		boolean bytearray = o instanceof byte[];
		
		o = nom.tam.util.ArrayFuncs.convertArray(o, double.class);
		
		data = (double[]) o;
		if (bytearray || scale != 1 || zero != 0) {
		    
		    for (int i=0; i<data.length; i += 1) {
			if (bytearray && data[i] < 0) {
			    data[i] += 256;
			}
			data[i] = scale*data[i] + zero;
		    }
		}
	    } else {
		data = (double[]) o;
	    }
	    double total = 0;
        for (double aData : data) {
            total += aData;
        }
	}
	
	return data[npix];
    }
    
    public Header getHeader() {
	return fitsHeader;
    }
}
