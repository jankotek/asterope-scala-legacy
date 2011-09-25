package skyview.request;



import java.util.regex.Pattern;

import skyview.Position;


/** Converts user input to coordinates and returns a position
 *  object that can be used to get a position in any coordinate system.
 *  The class attempts to handle most coordinate strings locally
 */
public class SourceCoordinates {

    /** Text the user entered */
    private String enteredText  = null;
    
    /** A position object representing this coordinate */
    private Position pos        = null;
    

    /** The coordinate System to be used. */
    private String coords;
    

    
    /** Have we done the conversion already? */
    private boolean converted = false;
    
    private double[] values;
    



    /*----------------------------------------------------------------------*/
    /** Constructor 
     *  @param s        text entered as coordinates or object name
     *  @param csn      name of coordinate system
     *  @param equinox  equinox of coordinate system
     *  @param resolver resolver to be used to resolve object name
     */
    /*----------------------------------------------------------------------*/
    public SourceCoordinates(String s, String csn, double equinox) {
       
	if (csn == null || csn.length() == 0) {
	    csn = "J2000";
	}
	
	char initial = csn.charAt(0);
	String rest = csn.substring(1);
	
	try {
	    equinox = Double.parseDouble(rest);
	} catch (Exception e) {
	    // If it didn't work we use the old value.
	}
	
	if (! (initial == 'G' || initial == 'I') ) {
	    csn = initial+""+equinox;
	}

	this.coords      = csn;
	this.enteredText = s;
    }
    
    public SourceCoordinates(String lon, String lat, String coords) throws IllegalArgumentException {
	lon = lon.trim();
	lat = lat.trim();
	this.coords      = coords;
	this.enteredText = lon+", "+lat;
	if (!process(new String[]{lon,lat})) {
	    throw new IllegalArgumentException("Invalid coordinates:"+enteredText);
	}
	converted = true;
    }
    
    public static SourceCoordinates factory(String s, String csn, double equinox) {
	return new SourceCoordinates(s, csn, equinox);
    }
    
    public static SourceCoordinates factory(String lon, String lat, String coords) {
	try {
	    return new SourceCoordinates(lon, lat, coords);
	} catch (IllegalArgumentException e) {
	    return null;
	}
    }
    
    /*----------------------------------------------------------------------*/
    /** Get the position associated with these coordinates.
    /*----------------------------------------------------------------------*/
    public Position getPosition () {
	return pos;
    }

    /*----------------------------------------------------------------------*/
    /**  convert user input string to coords
     *   @return true if coordinates were successfully resolved, false if not
     */
    /*----------------------------------------------------------------------*/
    public boolean convertToCoords() {
	if (converted) {
	    return true;
	}
	converted = true;
	

        // First check to see if this might can be parsed locally.
        // If there are only the characters '0-9 ,+-,:' in the input
        // string then we assume we can parse it locally.
        if (Pattern.matches("^[\\s0-9\\.\\+\\-\\,\\:]+", enteredText)) {
 	    return parseLocal(enteredText);
        } else {
            throw new RuntimeException("could not parse coordinates: "+enteredText);
        }
    }
   

    
    private boolean parseLocal(String input) {
	
	// Get rid of leading and trailing spaces.
	input = input.trim();
	
	// Connect signs to the appropriate values.
	input = skyview.survey.Util.replace(input, "\\+\\s+", "\\+", true);
	input = skyview.survey.Util.replace(input, "\\-\\s+", "\\-", true);
	
	
	// First check to see if we split on commas.
	String[] commas = input.split(",");
	if (commas.length == 2) {
	    return process(commas);
	    
	} else if (commas.length > 2) {
	    System.err.println("Error: Too many commas");
	    return false;
	    
	} else {
	    
	    // Next check to see if we split on the sign of the declination.
	    // Note that the RA may have a sign but if so it is the first
	    // character of the string.
	    String prefix = "";
	    String sign   = "-";
	    
	    if (input.charAt(0) == '+'  || input.charAt(0) == '-') {
		prefix = input.substring(0,1);
		input  = input.substring(1);
	    }
	    
	    String[] signs = Pattern.compile("(\\+|\\-)").split(input);
	    if (signs.length == 2) {
		signs[0] = prefix+signs[0];
		if (input.indexOf("+") >= 0) {
		    sign = "+";
		}
		signs[1] = sign+signs[1];
		return process(signs);
		
	    } else if (signs.length > 2) {
		System.err.println("Error in signs");
		return false;
		
		
	    } else {
		// Last chance... Let's split on spaces/colons
		// 
		input = prefix + input;
		
		String[] spaces = Pattern.compile("\\s+").split(input);
		
		if (spaces.length == 2) {
		    return process(spaces);
		} else if (spaces.length == 6) {
		    return process(new String[]{spaces[0]+" "+spaces[1]+" "+spaces[2],
			                 spaces[2]+" "+spaces[4]+" "+spaces[5]} );
		} else {
		    System.err.println("Unable to process input");
		    return false;
		}
	    }
	}
    }
    
    private boolean  process(String[] fields) {
	values = new double[2];
	for (int i=0; i<2; i += 1) {
	    if (!parseField(i, fields[i])) {
		return false;
	    }
	}
	try {
	    pos = new Position(values[0], values[1], coords);
	} catch (Exception e) {
	    return false;
	}
	return true;
    }
    
    private boolean  parseField(int index, String field) {
	
	field = field.trim();
	
        // Get rid of spaces around colons.
	field = skyview.survey.Util.replace(field, "\\s*\\:\\s*", "\\:", true);
	
	String[] comp = Pattern.compile("(\\s+|\\:)").split(field);
	
	if (comp.length > 3) {
	    return false;
	}
	
	double value  = 0;
	double sign   = 1;
	double ratio  = 1;

	if (comp[0].charAt(0) == '-') {
	    sign = -1;
	    comp[0] = comp[0].substring(1);
	} else if (comp[0].charAt(0) == '+') {
	    comp[0] = comp[0].substring(1);
	}
	
	for (int i=0; i<comp.length; i += 1) {
	    
	    if (comp[i].length() == 0) {
		return false;
	    }
	    if (i != comp.length-1 && comp[i].indexOf(".") >= 0) {
	        return false;
	    }
	    if (comp[i].indexOf("+") >= 0 || comp[i].indexOf("-") >= 0) {
		return false;
	    }
	    value += sign*Double.parseDouble(comp[i])/ratio;
	    ratio *= 60;
	}
	
	// Sexagesimal hours.
	String xcoords = coords.toUpperCase();
	if (index == 0 && comp.length > 1 && 
	    (xcoords.startsWith("J") || xcoords.startsWith("B"))) {
	    value *= 15;
	}
	values[index] = value;
	return true;
    }
}
