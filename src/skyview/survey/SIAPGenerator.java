package skyview.survey;

import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import skyview.executive.Settings;
import skyview.executive.Val;
import org.asterope.util.*;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;

/** This class gets a set of candidates from a SIAP request */
public class SIAPGenerator implements ImageGenerator {
    
    /** The descriptions of the images we are interested in */
    java.util.ArrayList<String> spells = new java.util.ArrayList<String>();

    /** Find the base URL for this SIAP service */
    protected String getBaseURL() {
	return Settings.get(Val.SiapURL);
    }
    String filterField;
    String filterValue;
    boolean filtering;
    
    /** Get images from a SIAP service */
    public void getImages(double ra, double dec, double size, java.util.ArrayList<String> spells)  {
	
	String urlString = getBaseURL();
	filterField      = Settings.get(Val.SiapFilterField);
	filterValue      = Settings.get(Val.SiapFilterValue);
	filtering        = filterField != null && filterValue != null;
	int timeout      = 15 * 1000;  // Default to 15 seconds.
	if (Settings.has(Val.SIATimeout) ) {
	    try {
		timeout = Integer.parseInt("SIATimeout")*1000;
	    } catch (Exception e) {}
	}
	
	urlString       += "&POS="+ra+","+dec+"&SIZE="+(1.4*size);
	System.err.println("  SIAP request URL:"+urlString);
	
      try {
	URL siaURL = new URL(urlString);
	
    InputStream   bi = GetURL.apply(siaURL,false);
	ByteArrayOutputStream bo = new ByteArrayOutputStream(32768);
	
    IOUtil.copy(bi, bo,1024*8);
	bi.close();
	bo.close();
	
        String response = bo.toString();
	response = response.replaceAll("<!DOCTYPE.*", "");
	    
	java.io.ByteArrayInputStream byi = new java.io.ByteArrayInputStream(response.getBytes());
	try {
            SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
	    // This should fill images with the strings for any images we want.
            sp.parse(byi, new SIAPGenerator.SIAPParserCallBack(spells));
	    
        } catch(Exception e) {
	    throw new Error("Error parsing SIAP:"+e);
        }
      } catch (java.net.SocketTimeoutException e) {
	  System.err.println("Timeout querying SIA URL:"+urlString);
	  return;
      } catch (Exception e) {
	  throw new Error("Unable to do IO in SIAP processing:"+e);
      }
    }

    private class SIAPParserCallBack extends DefaultHandler {
	
	
	/** Buffer to accumulate text into */
	private StringBuffer buf;
	
	/** Are we in an active element? */
	private boolean active = false;
	
	private int fieldCount = 0;
	
	private java.util.HashMap<String, Integer> fields = new java.util.HashMap<String, Integer>();
	private java.util.ArrayList<String> values = new java.util.ArrayList<String>();
	
	private String proj    = Settings.get(Val.SiapProjection);
	private String csys    = Settings.get(Val.SiapCoordinates);
	private String naxis   = Settings.get(Val.SiapNAXIS);
	private String scaling = Settings.get(Val.SiapScaling);
	private String maxImageString = Settings.get(Val.SiapMaxImages);
	private int maxImages;
	private int imageCount = 0;
	
	
	java.util.ArrayList<String> spells;
	
	SIAPParserCallBack(java.util.ArrayList<String> spells) {
	    this.spells = spells;
	    if (maxImageString != null) {
		maxImages = Integer.parseInt(maxImageString);
	    }
	}
	
        public void startElement(String uri, String localName, String qName, Attributes attrib) {
	    
	    
	    if (qName.equals("FIELD")) {
		String ucd = attrib.getValue("ucd");
		if(ucd != null && ucd.length() > 0) {
		    fields.put(ucd.toUpperCase(), fieldCount);
		}
		String name = attrib.getValue("name");
		if (name != null && name.length() > 0) {
		    fields.put(name, fieldCount);
		}
		
		String id = attrib.getValue("ID");
		if (id != null && id.length() > 0) {
		    fields.put(id, fieldCount);
		}
		
		fieldCount += 1;
		
	    } else if (qName.equals("TR") ) {
		values.clear();

		
	    } else if (qName.equals("TD")) {
	        active = true;
		buf    = new StringBuffer();
	    }
		
        }
    
	/** Find the value of a given field given
	 *  the UCD, name or ID of the field.
	 */
	private String getFieldValue(String id) {
	    id = id.toUpperCase();
	    if (fields.containsKey(id)) {
		int i   = fields.get(id);
		return values.get(i);
	    } else {
		return null;
	    }
	}
	
        public void endElement(String uri, String localName, String qName) {
	    
	    
	    // This means we finished a setting.
	    if (active) {
		
	        active = false;
		String s = new String(buf).trim();
		
		if(qName.equals("TD")) {
		    values.add(s);
		    
		}
	    } else if (qName.equals("TR")) {
		// Check if this is a FITS file.
		String format = getFieldValue("VOX:Image_Format").toLowerCase();
		// Just look for FITS somewhere in the format.
		if (format.indexOf("fits") < 0) {
		    return;
		}
		
		if (filtering) {  // If there is a filter value then compare as appropriate.		                        
		    String filter = getFieldValue(filterField);
		    if (filter != null  && !filter.equals(filterValue)) {
			return;
		    }
		}
			
		if (maxImageString != null) {
		    if (imageCount >= maxImages) {
			return;
		    }
		}
		/** Heres where all the work goes... */
		String spell = "";
		String url    = getFieldValue("VOX:Image_AccessReference");
		
		String file = getFieldValue("VOX:File_Name");
		
		if (file == null) {
		    file = url.substring(url.lastIndexOf('/')+1);
		}
		
		String ra      = getFieldValue("POS_EQ_RA_MAIN");
		String dec     = getFieldValue("POS_EQ_DEC_MAIN");
		if (ra == null) {
		    ra = getFieldValue("pos.eq.ra;meta.main");
		}
		if (dec == null) {
		    dec = getFieldValue("pos.eq.dec;meta.main");
		}
		
		boolean invert = false;
		String projstr = getFieldValue("VOX:WCS_CoordProjection");
		if (projstr != null) {
		    // If Dec comes before RA we need to flip the order
		    // of axes.
		    invert = proj.matches(".*\\-(DEC|LAT).*\\-(RA|LON).*");
		}
		
		String crval   = mashVal(getFieldValue("VOX:WCS_CoordRefValue"), invert, 2);
		
		// The following may be set generally.  If so
		// don't query.
		if (scaling == null) {
		    scaling = mashVal(getFieldValue("VOX:WCS_CDMatrix"), invert, 4);
		}
		if (scaling == null) {
		    scaling = mashVal(getFieldValue("VOX:Image_Scale"), invert, 2);
		}
		
		if (naxis == null) {
		    naxis  = mashVal(getFieldValue("VOX:Image_Naxis"), invert, 2);
		}
		
		if (crval == null) {
		    crval = ra + "," +dec;
		}
		
		spell = url + "," + file + "," + crval + "," + proj+","+csys+","+naxis + ","+scaling;
		spells.add(spell);
		imageCount += 1;
	    }
	}
	
	/** Take the input string, split by spaces or commas
	 *  invert array if needed and join with spaces.
	 *  @param input   The string to be parsed
	 *  @param invert  Are the coordinates reversed?
	 *  @param count   The expected number of parameters.
	 */
	private String mashVal(String input, boolean invert, int count) {
	    if (input == null) {
		return null;
	    }
	    input = input.trim();
	    String[] tokens = input.split(" ");
	    if (tokens.length == 1) {
		tokens = input.split(",");
	    }
	    String output = "";
	    
	    int curr = 0;
	    int delta = 1;
	    
	    if (invert) {
		curr = tokens.length-1;
		delta = -1;
	    }
	    
	    String sep = "";
        for (String token : tokens) {
            output += sep + tokens[curr];
            curr += delta;
            sep = ",";
        }
	    if (tokens.length < count) {
		String last = tokens[tokens.length-1];
		for (int i=tokens.length; i<count; i += 1) {
		    output += sep + last;
		    sep     = ",";
		}
	    }
		    
	    return output;
	}
		    
        public void characters(char[] arr, int start, int len) {
	    if (active) {
	        buf.append(arr, start, len);
	    }
        }
	
    }
}
