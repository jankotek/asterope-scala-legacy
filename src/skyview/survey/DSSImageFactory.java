package skyview.survey;

import skyview.executive.Settings;
import skyview.executive.Val;

/** A little class that creates FITS images using a factory method. */
public class DSSImageFactory implements ImageFactory {
    
     private boolean first = true;
     private boolean hasPrefix = false;
     private String prefix;
     public DSSImage factory(String file) {
	
	  if (first) {
	      first = false;
	      if (Settings.has(Val.FilePrefix)) {
		  prefix = Settings.get(Val.FilePrefix);
		  hasPrefix = true;
	      }
	  }
	  if (hasPrefix) {
	      file = prefix + file;
	  }
	  try {
	       return new DSSImage(file);
	  } catch (Exception e) {
	      System.err.println("Got exception on file:"+file+"\nException:"+e);
	      return null;
	  }
    }
}
