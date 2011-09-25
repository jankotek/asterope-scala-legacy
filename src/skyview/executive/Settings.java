package skyview.executive;


import java.io.PrintStream;
import java.util.HashMap;
import java.util.regex.Pattern;

/** This class defines a singleton where SkyView preferences/settings
 *  Testchange...
 *  can be set and gotten from anywhere in the system.
 *  A setting is simply a key=string value.  When there
 *  is to be more than one value for the key it should be
 *  specified as string1,string2,string3.  A comma
 *  is not allowed as a character within a setting.
 *  Keys are case insensitive.<p>
 *  When specified in the command line Settings may sometimes be set 
 *  with just the keyword.  This is treated as equivalent to key=1.
 */
public class Settings {

    /**
     * Asterope redirects system output to GUI window
     * Those two streams are used to do it
     */
    public static PrintStream out = System.out;
    public static PrintStream err = System.err;

    /**
     * If this variable is set to true,
     * it means that Skyview processing should stop.
     */
    public static boolean cancel = false;



    /** The hashmap storing the settings */
    private static final HashMap<Val,String> single = new HashMap<Val,String>();


    /** Used to split the hashmap */
    private static Pattern comma = Pattern.compile(",");



    static {
        initializeSettings();
    }

    static void initializeSettings() {
        put(Val.coordinates,"J");
        put(Val.equinox,"2000");
        put(Val.projection,"Tan");
        put(Val.equinox,"2000");
        put(Val.sampler,"NN");
        put(Val.pixels,"300");

        put(Val.output,"output");

    }




    /** Don't allow anyone else to create a settings object. */
    private Settings() {
    }

    /** Get a value corresponding to the key */
    public static String get(Val key) {
        if (key == null) {
            return null;
        }
        return  single.get(key);
    }

    /** Get a values corresponding to a key or the default */
    public static String get(Val key, String dft) {
        String gt = get(key);
        if (gt == null) {
            return dft;
        } else {
            return gt;
        }
    }

    /** Get the values corresponding to a key as an array of strings.  Returns
     * null rather than a 0 length array if the value is not set.
     */
    public static String[] getArray(Val key) {
        String gt = get(key);
        if (gt == null) {
            return new String[0];
        } else {
            return comma.split(gt);
        }
    }

    /** This method works like put except that
     *  it does not add a pair if the keys is in the _nullvalues setting
     *  or if the Setting is already set.
     */
    public static void suggest(Val key, String value) {
        if (Settings.has(key)) {
            return;
        }
        if (Settings.has(Val._nullvalues)) {
            String[] keys = Settings.getArray(Val._nullvalues);
            for (String nullKey: keys) {
                if (nullKey.equals(key)) {
                    return;
                }
            }
        }
        Settings.put(key, value);
    }

    /** Save a key and value */
    public static void put(Val key, String value) {

        if (value == null) {
            value = "1";
        }
        if (value.equals("null")) {
            Settings.add(Val._nullvalues, key.toString());
            single.remove(key);
            return;
        }

        if (value.length() > 1 && (value.charAt(0) == '\'' || value.charAt(0) == '"')) {
            char last = value.charAt(value.length()-1);
            if (value.charAt(0) == last) {
                value = value.substring(1,value.length()-1);
            }
        }

        single.put(key, value);
    }

    /** Remove key */
    public static void remove(Val key) {
        single.remove(key);
    }




    /** Add a setting to a list -- but only if it is
     *  not already in the list.
     */
    public static void add(Val key, String value) {

        // If we try to add a null it's OK if it's the only
        // value, but we can't add it to a list sensibly.
        if (value == null) {
            if (!Settings.has(key)) {
                Settings.put(key, value);
            }
            return;
        }

        String[] oldVals = Settings.getArray(key);

        // If the old value is an explicit null just replace it.
        if (oldVals.length == 1 && oldVals[0].equals("null")) {
            Settings.put(key,value);
            return;
        }

        String newValue = "";
        String comma    = "";
        for (String oldVal : oldVals) {
            if (oldVal.equals(value)) {
                return;
            }
            newValue += comma + oldVal;
            comma = ",";
        }
        newValue += comma+value;
        Settings.put(key, newValue);
    }

    /** Check if the given key has been set */
    public static boolean has(Val key) {
        return single.containsKey(key);
    }

    /** Return the array of keys in the current settings */
    public static Val[] getKeys() {
        return single.keySet().toArray(new Val[single.keySet().size()]);
    }



    /** remove any previous settings*/
    public static void clear(){
        single.clear();
        initializeSettings();

    }

    public static void checkCancelled(){
        if(cancel){
            cancel = false;
            throw new RuntimeException("Skyview operation cancelled");
        }
    }
}
