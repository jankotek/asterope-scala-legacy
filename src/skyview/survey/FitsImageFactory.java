package skyview.survey;

import skyview.executive.Settings;
import skyview.executive.Val;

/** A little class that creates FITS images using a factory method. */
public class FitsImageFactory implements ImageFactory {
    
    private boolean first      = true;
    private String  prefix     = null;
    private boolean havePrefix = false;
    
    public FitsImage factory(String file) {
	
	if (first) {
	    first = false;
	    if (Settings.has(Val.FilePrefix)) {
		prefix = Settings.get(Val.FilePrefix);
		havePrefix = true;
	    }
	}
	if (havePrefix) {
	    file = prefix + file;
	}
	
	try {
	    return new FitsImage(file);
	} catch (Exception e) {
	    e.printStackTrace();
	    throw new Error("Irrecoverable FITS error for file: "+file);
	}
    }
}
