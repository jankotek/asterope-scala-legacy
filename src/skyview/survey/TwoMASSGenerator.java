package skyview.survey;

import org.asterope.util.*;
import org.xml.sax.Attributes;
import org.xml.sax.helpers.DefaultHandler;
import skyview.executive.Settings;
import skyview.executive.Val;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.*;
import java.net.URL;
import java.net.URLConnection;
import java.util.regex.Pattern;

/** This is s special class just defined for a specific 2MASS SIAP service.
 *  When we get a 2MASS service with better defined columns we can get rid of this
 *  class.
 */
public class TwoMASSGenerator implements ImageGenerator {
    
    java.util.ArrayList<String> spells = new java.util.ArrayList<String>();
    
    /** Get images from a SIAP service */
    public void getImages(double ra, double dec, double size, java.util.ArrayList<String> spells)  {
	
	String urlString = Settings.get(Val.SiapURL);
	
	urlString       += "POS="+ra+","+dec+"&SIZE="+size;
	System.err.println("  SIAP URL:"+urlString);
    
	int timeout      = 15 * 1000;  // Default to 15 seconds.
	if (Settings.has(Val.SIATimeout) ) {
	    try {
		timeout = Integer.parseInt("SIATimeout")*1000;
	    } catch (Exception e) {}
	}
	
	try {

	    URL siaURL = new URL(urlString);
        InputStream   bi = GetURL.apply(siaURL,false);
 	    java.io.ByteArrayOutputStream bo = new java.io.ByteArrayOutputStream(32768);
	
 	    IOUtil.copy(bi, bo,1024*8);
 	    
	    bi.close();
	    bo.close();
	
            String response = bo.toString();
	    response = response.replaceAll("<!DOCTYPE.*", "");
	    
	    java.io.ByteArrayInputStream byi = new java.io.ByteArrayInputStream(response.getBytes());
	    try {
		SAXParserFactory sf = SAXParserFactory.newInstance();
		sf.setValidating(false);
                SAXParser sp = sf.newSAXParser();		
	        // This should fill images with the strings for any images we want.
                sp.parse(byi, new TwoMASSGenerator.SIAPParserCallBack(spells));
            } catch(Exception e) {
		System.err.println("  SIAP error:"+e);
		e.printStackTrace(System.err);
	        throw new Error("Error parsing SIAP:"+e);
            }
        } catch (java.net.SocketTimeoutException e) {
	    throw new Error("Timeout querying SIA URL:"+urlString,e);

	} catch (Exception e) {
	    throw new Error("Unable to do IO in SIAP processing on URL: \n"+urlString,e);
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
	
	private String proj           = Settings.get(Val.SiapProjection);
	private String csys           = Settings.get(Val.SiapCoordinates);
	private String naxis          = Settings.get(Val.SiapNAXIS);
//	private String scaling        = Settings.get("SiapScaling");
	private String filterValue    = Settings.get(Val.SiapFilter);
	private String filterField    = Settings.get(Val.SiapFilterField);
	
	java.util.ArrayList<String> spells;
	
	SIAPParserCallBack(java.util.ArrayList<String> spells) {
	    this.spells = spells;
	}
	
        public void startElement(String uri, String localName, String qName, Attributes attrib) {
	    
	    if (qName.equals("FIELD")) {
		String ucd = attrib.getValue("ucd");
		if(ucd != null && ucd.length() > 1) {
		    fields.put(ucd, fieldCount);
		}
		String id  = attrib.getValue("ID");
		if (id != null) {
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
    
	private String getUCD(String ucd) {
	    if (fields.containsKey(ucd)) {
		int i   = fields.get(ucd);
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
		if (!getUCD("VOX:Image_Format").equals("image/fits")) {
		    return;
		}
		
		// Check for the right band.
		if (!getUCD(filterField).equals(filterValue)) {
		    return;
		}
		
		/** Heres where all the work goes... */
		String spell = "";
		String url    = getUCD("VOX:Image_AccessReference");
		String file   = url.substring(url.lastIndexOf('/')+1);
		file          = skyview.survey.Util.replace(file, ".*name=","", false);
		file          = skyview.survey.Util.replace(file, ".fits", "", false);
		String coadd  = getUCD("coadd_key");
		file         += "."+coadd+".fits.gz";
//		String ra     = getUCD("POS_EQ_RA_MAIN");
//		String dec    = getUCD("POS_EQ_DEC_MAIN");
		String crval  = getUCD("VOX:WCS_CoordRefValue");
		crval = skyview.survey.Util.replace(crval, "(\\S)\\s+(\\S)", "$1,$2", true); 
		
		// 2MASS specific....
		// Note that this uses the ID which is also stored in the values 
		String crota  = getUCD("crota2");
		double rot = Math.toRadians(Double.parseDouble(crota));
		String scale  = getUCD("VOX:Image_Scale");
		String[] scales = Pattern.compile(" +").split(scale);
		double xs = Double.parseDouble(scales[0]);
		double ys = Double.parseDouble(scales[1]);
		String scaling = Math.cos(rot)*xs +","+Math.sin(rot)*xs+","+Math.sin(rot)*ys+","+Math.cos(rot)*ys;
		
		
		if (naxis == null) {
		    naxis  = getUCD("VOX:Image_Naxis");
		    naxis = skyview.survey.Util.replace(naxis, "(\\S)\\s+(\\S)", "$1,$2", true); 
		}
		
		spell = url + "," + file + "," + crval + "," + proj+","+csys+","+naxis + ","+scaling;
		spells.add(spell);
	    }
	}
		    
        public void characters(char[] arr, int start, int len) {
	    if (active) {
	        buf.append(arr, start, len);
	    }
        }
	
    }
}
    
    
    
    
