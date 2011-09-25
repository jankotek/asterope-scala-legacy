package skyview.survey;

/** This interface is implemented by classes that dyamically
 *  generate a list of images (or more precisely the spells through
 *  which the images may be created by an ImageFactory).
 */
public interface ImageGenerator {
    public void getImages(double ra, double dec, double size, java.util.ArrayList<String> candidates);
}
