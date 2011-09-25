package skyview.survey;

import nom.tam.fits.Header;
import org.asterope.util.*;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import skyview.Position;
import skyview.Utilities;
import skyview.executive.Settings;
import skyview.executive.Val;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import java.io.BufferedInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.regex.Pattern;

/** This class defines a survey based upon an XML file
 *  which contains the metadata and image information for the survey.
 */
public class XMLSurvey implements Survey {
    
    /** The XML file that defines the survey */
    private String xmlFile;
    
    /** The default size of images. */
    private double surveySize;
    
    /** The list of image strings */
    private ArrayList<String> images;
    
    /** The class the is called to add survey specific settings.
     */
    private class SettingsCallBack extends DefaultHandler {
	
	/** Buffer to accumulate text into */
	private StringBuffer buf;
	
	/** Are we in an active element? */
	private boolean active = false;
	
	/** Are we in the survey settings? */
	private boolean inSettings = true;
	
	/** Are we in the metatable? */
	private boolean inMeta = false;
	
	
        public void startElement(String uri, String localName, String qName, Attributes attrib) {
	    
	    String lq = qName.toLowerCase();
	    
	    if (lq.equals("settings")  || 
		lq.equals("metatable") || 
		lq.equals("name")      ||
		lq.equals("shortname") ||
		lq.equals("onlinetext") ) {
		inSettings = true;
	    }
	    if (lq.equals("metatable")) {
		inMeta = true;
	    }
	    if (inSettings) {
	        active = true;
		buf    = new StringBuffer();
	    }
        }
    
        public void endElement(String uri, String localName, String qName) {
	    
	    String lq = qName.toLowerCase();
	    if (lq.equals("settings")   || 
		lq.equals("metatable")  || 
		lq.equals("name")       ||
		lq.equals("onlinetext") 
		) {
		inSettings = false;
		active     = false;
	    }
	    
	    // This means we finished a setting.
	    if (active  || lq.equals("name") || lq.equals("onlinetext") ) {
	        active = false;
		String s = new String(buf).trim();
		qName = qName.toLowerCase();
		if (inMeta) {
		    qName = "_meta_"+qName;
		}
		
		// Don't override settings that the user has specified.
		if (s.length()> 0 &&
                        //ignore meta
                        !qName.startsWith("_meta")) {
		    Settings.suggest(Val.valueOf(qName), s);
		}
	    }
	    
	    if (lq.equals("metatable") ) {
		inMeta = false;
	    }
        }

        public void characters(char[] arr, int start, int len) {
	    if (active) {
	        buf.append(arr, start, len);
	    }
        }
    }
    /** The class the is called to find images in the Survey
     *  XML file.
     */
    private class ImageFinderCallBack extends DefaultHandler {
	
	/** Buffer to accumulate text into */
	private StringBuffer buf;
	
	/** Are we in an active element? */
	private boolean active = false;
	
	/** The RA, Dec and size that the user is requesting. */
	private double ra, dec, requestSize;
	
	/** Used to break up the image strings */
	private Pattern pat = Pattern.compile("\\s+");
	
	/** Are we in the Images area? */
	private boolean inImages = false;
	
	/** Is this the first Image? */
	private boolean firstImage = true;
	
	/** The position of the center of the output image */
	private Position pos;
	
	/** Do we want to get images? */
	private boolean needImages;
	
	private void updatePosition() {
	    try {
	        double[] coords = pos.getCoordinates(Settings.get(Val.SurveyCoordinateSystem));
	        ra     = coords[0];
	        dec    = coords[1];
	    } catch (Exception e ) {
		System.err.println("Error with SurveyCoordinateSystem!"+Settings.get(Val.SurveyCoordinateSystem));
		throw new Error(e);
	    }
	}
	
	ImageFinderCallBack(Position pos, double size, boolean needImages) {
	    this.pos = pos;
	    this.needImages = needImages;
	    updatePosition();
	    
	    this.requestSize = size;
	}
    
        public void startElement(String uri, String localName, String qName, Attributes attrib) {
	    
	    if (inImages) {
		active = true;
		buf = new StringBuffer();
	    }
	    if (qName.equals("Images")) {
		inImages = true;
	    }
        }
    
        public void endElement(String uri, String localName, String qName) {
	    
	    if (active) {
	        active = false;
		String s = new String(buf).trim();
		
	        if (qName.equals("Image")  && needImages) {
		    
		    // Check if this image is close enough to be a candidate
		    // for mosaicking.
		    
		    if (firstImage) {
			surveySize = Double.parseDouble(Settings.get(Val.ImageSize));
			firstImage = false;
		    }
		    // Could cause problems if filenames have white space in them.
		    // Might be better to have subfields in the <Image> element.
		    String[] tokens = pat.split(s);
		    double xRA = 0, xDec = 0;
		    try {
		        xRA  = Double.parseDouble(tokens[1]);
		        xDec = Double.parseDouble(tokens[2]);
		    } catch (Exception e) {
			throw new Error(e);
		    }
		    double distance =  Math.toDegrees(Angle$.MODULE$.distance(
		    		Math.toRadians(ra), Math.toRadians(dec), 
		    		Math.toRadians(xRA), Math.toRadians(xDec)));
		    
		    // Coefficient below probably could be 1/sqrt(2) for diagonals along squares,
		    // but we make it a little larger as a safety factor
		    if (distance < (surveySize + requestSize)) {
		        images.add(tokens[0]);
		    }
		    
		} else if (qName.equals("ImageGenerator")) {
		    
		    // This is the name of a class that can generate image names dynamically.
		    skyview.survey.ImageGenerator gen = 
		      (skyview.survey.ImageGenerator) Utilities.newInstance(
                      s, "skyview.survey");
		    if (gen == null) {
			throw new Error("Unable to create image generator:"+s);
		    } 
		    try {
		        gen.getImages(ra, dec, requestSize, images);
		    } catch (Exception e) {
			System.err.println("Unable to invoke ImageGenerator:"+s+"\nException: "+e);
		    }
		    
	        } else {
		    // Everything else goes into the Settings.  However
		    // unlike elements in the <Settings> area, we don't
		    // defer to what's already there, we replace it.
		    Settings.put(Val.valueOf(qName), s);
		    // Following Images are in the given coordinate system
		    if (qName.toLowerCase().equals("surveycoordinatesystem")) {
			updatePosition();
		    }
		}
	    }
        }

        public void characters(char[] arr, int start, int len) {
	    if (active) {
	        buf.append(arr, start, len);
	    }
        }
    }
    
    /** The class is used when we update an image generated from 
     *  a survey.
     */
    private class HeaderUpdateCallBack extends DefaultHandler {
	
	/** Buffer to accumulate text into */
	private StringBuffer buf;
	
	/** Are we in an active element? */
	private boolean active = false;
	
	/** Is this a metafield? */
	private boolean meta = false;
	
	/** Is this the first metadata field? */
	private boolean firstMeta = true;
	
	/** The FITS header to be updated */
	private Header h;
	
        private Pattern pat = Pattern.compile("\\n");
	
	
	HeaderUpdateCallBack(Header fitsHeader) {
	    this.h = fitsHeader;
	}
    
        public void startElement(String uri, String localName, String qName, Attributes attrib) {
	    
	    if (meta || qName.equals("FITS")) {
		active=true;
		buf = new StringBuffer();
	    }
	    // Include the survey metadata in the output.
	    if (qName.equals("MetaTable")) {
		meta = true;
	    }
        }
    
        public void endElement(String uri, String localName, String qName) {
	    
	  try {
	    if (active) {
	        active = false;
		String s = new String(buf).trim();
	        if (meta) {
		    
		    if (firstMeta) {
			h.insertComment("");
			h.insertComment(" SkyView Survey metadata ");
			h.insertComment("");
		        firstMeta = false;
		    }
		    
		    // The metadata may include HTML of various kinds.
		    // Let's get rid of that...
		    s = skyview.survey.Util.replace(s, "<[^>]*>", "", true);
		    s = skyview.survey.Util.replace(s, "&amp;", "&", true);
		    s = skyview.survey.Util.replace(s, "&gt;", ">", true);
		    s = skyview.survey.Util.replace(s, "&lt;", "<", true);
		    s = skyview.survey.Util.replace(s, "\n", " ", true);
		    
		    String comHead = qName+":";
		    if (comHead.length() < 13) {
			comHead = comHead + "               ".substring(0,13-comHead.length());
		    }
		    
		    String comment = comHead+s;
		    
		    while (comment != null && comment.length() > 0) {
			
			if (comment.length() > 70) {
			    String next    = "          "+comment.substring(70);
			    String current = comment.substring(0,70);
			    h.insertComment(current);
			    comment = next;
			    
			} else {
			    
			    h.insertComment(comment);
			    comment = null;
			}
		    }
		}
		
		if (qName.equals("FITS")) {
		    h.insertComment("");
		    h.insertComment("Survey specific cards");
		    h.insertComment("");
		    // Add cards specifically for this survey.
		    String[] tokens = pat.split(s);
		    for (String tok: tokens) {
			h.addLine(tok);	
		    }
		}
	    }
	    if (qName.equals("MetaTable")) {
		meta = false;
	    }
	  } catch(nom.tam.fits.FitsException e) {
	      throw new Error("Unexpected FITS exception in HeaderUpdateCallBack:"+e);
	  }
        }

        public void characters(char[] arr, int start, int len) {
	    if (active) {
	        buf.append(arr, start, len);
	    }
        }
	}

    /** Create a survey whose characteristics are given in 
     *  an XML file.
     */
    public XMLSurvey(String file) {
	this.xmlFile = file;
    }
    
    /** Get the name of the compontent */
    public String getName() {
	return "Survey:XML";
    }
    
    /** Get a description of the component */
    public String getDescription() {
	return "A survey defined by an XML file which contains the metadata and image descriptions";
    }

    /** Find candidate images from this survey.
     *  @param pos   A position object.
     *  @param size  The size (in radians) over which we should look for candidates.
     */
    public Image[] getImages(Position pos, double size) throws Exception {  
	
	
	/** Get the coordinates in the native coordinate system of the
	 *  survey.  If none is specified this defaults to J2000.
	 */
        SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
	images = new ArrayList<String>();
	
	boolean needImages = true;
	
	if (
	  Settings.has(Val.MaxRequestSize) &&
	  Settings.has(Val.LargeImage) &&
	  Double.parseDouble(Settings.get(Val.MaxRequestSize)) < size) {
	    String[] fields = Settings.getArray(Val.LargeImage);
        images.addAll(Arrays.asList(fields));
	    needImages = false;
        }
	// This should fill images with the strings for any images we want.
	// If we don't need images we may still need other info from
	// the <Images> area.
        doParse(sp, new XMLSurvey.ImageFinderCallBack(pos, size, needImages));
	
	String imageFactory = Settings.get(Val.ImageFactory);
	
	if (images.size() == 0) {
	    return new Image[0];
	} else {
	    Image[] list       = new Image[images.size()];
	    
	    // Create the image factory
	    ImageFactory imFac = (ImageFactory) 
	      Utilities.newInstance(imageFactory, "skyview.survey");
	    if (imFac == null) {
		throw new Error("Unable to create image factory");
	    }
	    for (int i=0; i<images.size(); i += 1) {
		String s = images.get(i);
		list[i]  = imFac.factory(s);
	    }
	    return list;
	}
    }
    
    /** Update a FITS header with information from the XML file */
    
    public void updateHeader(Header h) {
        try {
            SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
            new XMLSurvey.HeaderUpdateCallBack(h);
            doParse(sp, new XMLSurvey.HeaderUpdateCallBack(h));
        } catch(Exception e) {
	    throw new Error("Error updating header:",e);
        }
    }
    
    /** Update the system settings */
    public void updateSettings() {
	try {
            SAXParser sp = SAXParserFactory.newInstance().newSAXParser();
	    // This should fill images with the strings for any images we want.
            doParse(sp, new XMLSurvey.SettingsCallBack());
         } catch(Exception e) {
	    throw new Error("Error updating header when reading file:"+xmlFile,e);
        }
    }
    
    /** Run a parser */
    protected void doParse(SAXParser sp, DefaultHandler handler) throws Exception {
	InputStream is = Util.getResourceOrFile(xmlFile);
	is             = new BufferedInputStream(is);	
	sp.parse(new InputSource(is), handler);
	is.close();
    }
}
