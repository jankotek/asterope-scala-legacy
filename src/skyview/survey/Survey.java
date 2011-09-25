package skyview.survey;

import skyview.Position;

/** methods for describing and manipulating survey data */
public interface Survey extends skyview.Component {
    
    /** Find a list of images that are within size of the given position. */
    public abstract Image[] getImages(Position pos, double size) throws Exception;
    
    /** Update a FITS header for an image generated from this survey. */
    public abstract void updateHeader(nom.tam.fits.Header fitsHeader);
    
    /** Update the system settings with survey specific options.
     *  Normally these should not override settings that the user has already
     *  specified.
     */
    public abstract void updateSettings();
}
