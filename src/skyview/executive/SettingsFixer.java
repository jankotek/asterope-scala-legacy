package skyview.executive;

/** This class looks at the settings and updates as needed for
 *  use with the standard Java classes.
 */
public class SettingsFixer {
    
        /** Update the settings associated with this smoother */
    public void updateSettings() {
	
	// Handle the smoothing argument and translate to a class
	//  to a smoothing postprocessor.
	
	if (Settings.has(Val.Smooth)  && Settings.get(Val.Smooth).length() > 0) {
	    String[] upd = Settings.getArray(Val.Postprocessor);
	    String cname = "skyview.data.BoxSmoother";
	    boolean found = false;
        for (String anUpd : upd) {
            if (cname.equals(anUpd)) {
                found = true;
                break;
            }
        }
	    if (!found) {
	        // Put smooth before other postprocessors.
	        if (Settings.has(Val.Postprocessor)) {
		    Settings.put(Val.Postprocessor, cname+","+Settings.get(Val.Postprocessor));
	        } else {
	            Settings.put(Val.Postprocessor, cname);
	        }
	    }
	}

      
	// If the user has requested graphic content, then they shouldn't
	// have to say they want a graphic image.
	if ( Settings.has(Val.Invert) ||
	     Settings.has(Val.LUT)    || Settings.has(Val.Scaling) ||
	     Settings.has(Val.quicklook) ||
	     Settings.has(Val.imagej)) {
	    Settings.add(Val.Postprocessor, "skyview.ij.IJProcessor");
	    if (!Settings.has(Val.quicklook) && !Settings.has(Val.quicklook)) {
		Settings.add(Val.quicklook, "jpg");
	    }
        }
	
	// Set JPEGs as the default quicklook format.
	if (Settings.has(Val.quicklook)) {
	    String ql = Settings.get(Val.quicklook);
	    if (ql == null || ql.length() == 0) {
		Settings.put(Val.quicklook, "jpeg");
	    }
	}
	
	// If the user has requested the AddingMosaicker, then
	// they should use the null image finder.
	if (Settings.has(Val.Mosaicker)) {
	    String mos = Settings.get(Val.Mosaicker);
	    if (mos.indexOf ("AddingMosaicker") >= 0) {
		Settings.put(Val.imagefinder, "Bypass");
	    }
	}
    }
}
