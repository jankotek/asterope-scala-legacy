package skyview;

/** utlity functions to use with SkyView */
public class Utilities {

    
    /** Create an object of a given class.
     *  The input can be a class in a specified package,
     *  or a full specified class.
     *  We first try to instantiate the object within
     *  the specified package, then try it as a fully
     *  qualified class.
     */
    public static Object newInstance(String cls, String pkg) {

	if (pkg != null) {
	    try {
//		String fullName = pkg+"."+cls;
	        return Class.forName(pkg+"."+cls).newInstance();
	    } catch (Exception e) {
		// OK...  We'll try it without the package prefix
	    }
	}
    try {
        return Class.forName(cls).newInstance();
	} catch (Throwable e) {
		throw new RuntimeException("  Unable to instantiate dynamic class "+cls+" in package "+pkg,e);
	}
    }

}
