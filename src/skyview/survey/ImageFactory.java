package skyview.survey;

/** This interface is implemented by methods which can
 *  build an image using the given string.  Since the string
 *  may sometimes be complex and do magic things it is sometimes
 *  referred to a the 'spell' that is used ot create the Image.
 *  In simple cases, the string is just the file name or URL
 *  of a file from which the image will be derived.
 */
public interface ImageFactory {
    
    /** Create an image given the appropriate spell */
    public abstract skyview.survey.Image factory(String spell);
}
