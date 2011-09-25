package skyview.executive;

/**
 * An key for Skyview settings. This is first step in removing Settings static object
 */
public enum Val {
    /** if set postprocess image with ImageJ*/
    imagej,

    /** Disable forced exit at end of main method */
    noexit,

    /** currently processed survey */
    Survey,
    /** current RA&Dec in degrees, comma separated */
    Position,
    /** another method to specify position */
    Lat,
    /** another method to specify position */
    Lon,
    /** copy position and FOV from fits file */
    CopyWCS,
    /** save result to this file */
    output,
    /** image scale */
    scale,
    /** coordinate system used such as J2000 */
    coordinates,
    /** projection from sphere to plane */
    projection,
    /** reference coordinates for projection */
    RefCoords,
    /** 2d rotation applied to result image */
    Rotation,
    /** 2d offset applied to result image */
    Offset,
    /** equinox used in coordinate system such as J2000 */
    equinox,
//    _CRPIX1, _CD2_2, _CD2_1, _CD1_2, _CD1_1, _CDELT2, _CDELT1, _CRVAL2, _CRVAL1, _CRPIX2,

    /** image sampler to use */
    sampler,
    /** image finder used to fetch images */
    imagefinder,
    //not sure
    GeometryTwin,
    //not sure
    StrictGeometry,
    /** identifies currently processed survey */
    _currentSurvey,
    /** do not create fits file at end */
    nofits,
    /** save preview to JPG */
    quicklook,
    /** array of preprocessors which modifies image */
    Preprocessor,
    /** array of postprocessors which modifies image */
    Postprocessor,
    /** add mosaicker to an image*/
    Mosaicker,
    
    /** force DSS WCS, !Ignored*/
    usedsswcs,

    /** deedger */
    deedger,

    /** use 4 byte float rather than 1 byte float.
     * From comment:
      	     This is the mandatory keyword that anyone using
      	     the old interface would use.  We'll set the
	     code to use 4-byte reals, but someone can
	     say float=null to disable this.  This way
	     people using the old interface get 4-byte reals
	     as before.
     */
    Float,
    /** comma separated image width and height */
    Size,
    /** fits specific stuff */
    exposurekeyword,
    /** for ImageFinder, if set it will not include some images in input */
    CheckNaNs,
    /** for ImageFinder, Do we want to skip the edge checks?*/
    CornersOnly,
    /** something to do with Mosaicker */
    MinEdge,
    /** something to do with Mosaicker */
    maxrad,
    /** RectRecurse Image finder, Should we retry pixels when we get a no coverage? */
    findretry,
    /** image smoothing done in post processing */
    Smooth,
    /** image scaler */
    Scaling,

    /** invert LUT at quick look */
    Invert,
    /** lut (color combination to be used at quicklook*/
    LUT,

    /** used in DSS, not sure how */
    ImageSize,
    /** used by Settings.suggest*/
    _nullvalues,
    /**This string is used to distinguish the various DSS2 surveys.*/
    DSS2Prefix,
    /** file prefix used by ImageFactory. Probably for caching */
    FilePrefix,
    /** used by XML survey*/
    SpellSuffix,
    /** used by XML survey*/
    SpellPrefix,
    /** local URL where to fetch DSS images from */
    LocalURL,
    /** used by XML */
    shortname,
    /** not sure, propably needed*/
     SurveyCoordinateSystem,
    /** maximal image size when requesting from remote server*/
    MaxRequestSize,
    /** maximal image size when requesting from remote server*/
    LargeImage,
    /** easy*/
    ImageFactory,

    /** from FITS*/
    PixelOffset,
    /** from FITS*/
    ExposureFileMatch,
    /** from FITS*/
    ExposureFileGen, Level,
    TileY, TileX, Subdiv, Format, Min, Max,   XXX, YYY,

    /** for SIAP */
    SiapURL, SiapFilterField, SiapFilterValue, SIATimeout, SiapProjection, SiapCoordinates, SiapNAXIS,
    SiapScaling, SiapMaxImages, SiapFilter,

    pixels,

    //this is required by survey XML parser
    name,geometrytwin

}
