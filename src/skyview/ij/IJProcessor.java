package skyview.ij;

import ij.IJ;
import ij.ImagePlus;
import ij.WindowManager;
import ij.io.FileSaver;
import ij.plugin.ContrastEnhancer;
import ij.plugin.LutLoader;
import ij.process.ByteProcessor;
import ij.process.FloatProcessor;
import ij.process.ImageConverter;
import ij.process.ImageProcessor;
import skyview.executive.Val;
import skyview.sampler.Sampler;
import skyview.executive.Settings;
import skyview.survey.Image;


import java.awt.image.BufferedImage;
import java.awt.image.IndexColorModel;
import java.util.ArrayList;

/** This class uses ImageJ to create non-FITS image products.
 *  The following keyword settings are supported:
 *
 *  <dl>
 *   <dt> Quicklook
 *     <dd> Specifies the format to be used for a quicklook image.
 *     Supported formats are: JPEG, TIFF and PNG.
 *   <dt> Scaling
 *      <dd> Specifies how the brigtness of the image is to be scaled.
 *      Supported values include:
 *      <dl><dt> Log <dd> Logarithmic scaling.
 *          <dt> Sqrt <dd> Scaling as the square root of the pixel value.
 *          <dt> Linear <dd> Linear scaling.
 *          <dt> HistEq <dd> Histogram equalization scaling.
 *          <dt> LogLog </dd> Double log scaling.
 *      </dl>
 *   <dt> Inverse <dd> Invert the color table.
 *   <dt> Lut <dd> Load a look-up table.
 *  </dl>
 *  If any of these keywords are found, the updateSettings will
 *  ensure that there the IJProcessor is included as a postprocessor,
 *  so the user need not explicitly specify this.
 */
public class IJProcessor implements skyview.process.Processor {


    private static ArrayList<ImagePlus> savedImages;

    private static String[] stdLUTs= {"fire", "grays", "ice", "spectrum", "3-3-2 rgb",
            "red", "green", "cyan", "magenta", "yellow", "red/green"};


    private ImageProcessor ip;

    private int nx;
    private int ny;

    static  IndexColorModel icm;

//    private HashSet<Color> colorHash;

    static {
        byte[] r = new byte[256];
        for (int i=0; i<r.length; i += 1) {
            r[i] = (byte)i;
        }
        icm = new IndexColorModel(8, 256, r, r, r);
    }


    private skyview.survey.Image output;

    public String getName() {
        return "IJProcess";
    }
    public String getDescription() {
        return "Do image processing in ImageJ";
    }

    private void processMin(String min) {
        if (min != null) {
            try {
                double fmin = Double.parseDouble(min);
                ip.min(fmin);
            } catch (Exception e) {
                System.err.println("  Error parsing min value: "+min);
            }
        }
    }

    private void processMax(String max) {
        if (max != null) {
            try {
                double fmax = Double.parseDouble(max);
                ip.max(fmax);
            } catch (Exception e) {
                System.err.println("  Error parsing max value: "+max);
            }
        }
    }

    private void processScale(String scale) {
        if (scale == null) {
            scale = "log";
        }
        scale = scale.toLowerCase();
        if ( (scale.equals("log") || scale.equals("sqrt")) &&
                !Settings.has(Val.Min)) {
            double zmin = 1.e20;
            double zmax = -1.e20;
            double[] data = output.getDataArray();
            for (double aData : data) {
                if (aData > 0 && aData < zmin) {
                    zmin = aData;
                }
                if (aData > zmax) {
                    zmax = aData;
                }
            }
            if (zmax/zmin > 100000) {
                zmin = zmax/100000;
            }
            ip.min(zmin);
        }

        if (scale.equals("log")) {
            ip.log();

        } else if (scale.equals("loglog")) {

            double[] data = output.getDataArray();
            double min = Double.NaN;
            for (double aData : data) {
                if (min != min || (min == min && aData > 0 && min > aData)) {
                    min = aData;
                }
            }
            ip.multiply(1.01/min);
            ip.log();  // Smallest >zero should scale to 1.01
            ip.log();
        } else if (scale.equals("sqrt")) {
            ip.sqrt();
        }
        if (scale.equals("histeq")) {
            ImagePlus imp = new ImagePlus(" ", ip);
            new ImageConverter(imp).convertToGray8();
            new ContrastEnhancer().equalize(imp);
            ip = imp.getProcessor();
        } else {
            if (Settings.has(Val.Min) && Settings.has(Val.Max)) {
                standardScale(scale);
            }
        }
    }

    /** User has specified both min and max, so use that
     *  to define the translation to intensity rather than
     *  the actual pixel values.
     *  @param scale type (sqrt, log...)
     */
    private void standardScale(String scale) {
        float mn = Float.parseFloat(Settings.get(Val.Min));
        float mx = Float.parseFloat(Settings.get(Val.Max));
        if (mx <= mn) {
            System.err.println("Scaling has Max < Min");
        }
        if (scale.equals("sqrt")) {
            mn = (float) Math.sqrt(mn);
            mx = (float) Math.sqrt(mx);
        } else if (scale.equals("log")) {
            mn = (float) Math.log(mn);
            mx = (float)  Math.log(mx);
        }
        float delta   = (mx-mn)/256;
        float[] data = (float[]) ip.getPixels();
        for (int i=0; i<data.length; i += 1) {
            data[i] = (data[i]-mn)/delta;
        }
        ip.setPixels(data);
        ip = ip.convertToByte(false);
    }





    public void process(Image[] inputs, Image output, int[] source,
                        Sampler samp) {

        // If there is no output just return.  Also do no image processing
        // if we are just getting the image for contour.
        //

        if (output == null) {
            return;
        }
        // Get what we need from the output image.
        double img[] = output.getDataArray();

        nx           = output.getWidth();
        ny           = output.getHeight();
        int nz       = output.getDepth();
        if (nz > 1) {
            int len = nx*ny;
            double[] ximg = new double[nx*ny];
            for (int p=0; p<ximg.length; p += 1) {
                for (int z=0; z<nz; z += 1) {
                    ximg[p] += img[len*z + p];
                }
            }
            img = ximg;
        }

        this.output  = output;


        String out = Settings.get(Val.output);


        ip = new FloatProcessor(output.getWidth(), output.getHeight(), img);

        // Astronomers have Y start at the bottom, but ImageJ uses the typical image
        // convention and starts from the top.

        ip.flipVertical();


        // First process things that actually change the pixel values.
        // Here we need to treat the data as real.
        processMin(Settings.get(Val.Min));
        processMax(Settings.get(Val.Max));

        // Allow different scalings for different surveys
        if (Settings.has(Val.Scaling)) {
            processScale(Settings.get(Val.Scaling));
        } else {
            processScale(null);
        }


        ImagePlus imp = new ImagePlus("", ip);
        new ImageConverter(imp).convertToGray8();
        ip = imp.getProcessor();
        ip.setValue(255);

        // Now we're done with the pixels -- we play
        // with the color tables.
        processLUT(Settings.get(Val.LUT));

        // This one's easy enought to do here!
        if (Settings.has(Val.Invert)) {
            ip.invertLut();
        }




        // Time to write out the file.
        if (Settings.has(Val.quicklook)) {
            writeFile(out);
        }

        // Display the image.
        if (Settings.has(Val.imagej)) {
            String sname = Settings.get(Val.Survey);
            imp = new ImagePlus(sname, ip);
            showImp(imp);
        }
    }

    private void showImp(ImagePlus imp) {
        if (imp == null) {
            return;
        }
        if (IJ.getInstance() == null) {
            //TODO find out if there is problem with skyview exit
//	    ImageJ ij = new ImageJ();
//	    ij.setExitWhenQuitting(true);
        }
        imp.show();
    }


    private void writeFile(String outStem) {

        String    sname = Settings.get(Val.Survey);

        ImagePlus imp1  = new ImagePlus(sname, ip);

        String format = Settings.get(Val.quicklook);
        if (format == null  || format.length() == 0) {
            format = "jpeg";
        }
        format = format.toLowerCase();

        if (format.equals("object")) {
            if (savedImages == null) {
                savedImages = new ArrayList<ImagePlus>();
            }
            savedImages.add(imp1);


        } else if (format.equals("jpeg") || format.equals("jpg")) {
            String file = outStem;
            if (!file.equals("-")) {
                file += ".jpg";
            }
            new FileSaver(imp1).saveAsJpeg(file);
            System.err.println("  Creating quicklook image: "+file);


        } else if (format.equals("tiff")) {
            new FileSaver(imp1).saveAsTiff(outStem+".tiff");
            System.err.println("  Creating quicklook image: "+outStem+".tiff");

        } else if (format.equals("png")) {
            new FileSaver(imp1).saveAsPng(outStem+".png");
            System.err.println("  Creating quicklook image: "+outStem+".png");
        } else {
            System.err.println("  Error: Unrecognized quicklook format: "+format);
        }
    }

    public void updateHeader(nom.tam.fits.Header header) {
        // Doesn't know anything about the FITS header,
        // so just skip it.
    }

    private void processLUT(String origLUT) {
        if (origLUT == null) {
            return;
        }

        String lut = origLUT.toLowerCase();
        for (String stdLUT : stdLUTs) {
            if (stdLUT.equals(lut)) {
                WindowManager.setTempCurrentImage(new ImagePlus(" ", ip));
                if (lut.equals("3-3-2 rgb")) {
                    lut = "3-3-2 RGB";
                }
                new LutLoader().run(lut);
                return;
            }
        }

        byte[] red   = new byte[256];
        byte[] green = new byte[256];
        byte[] blue  = new byte[256];
        try {
            java.io.InputStream is =  skyview.survey.Util.getResourceOrFile(lut);

            // If user is opening an LUT file, then on many operating
            // systems we need to use the correct case.
            if (is == null && !lut.equals(origLUT)) {
                is = skyview.survey.Util.getResourceOrFile(origLUT);
            }
            if (is == null) {
                System.err.println("  Error: Unable to find LUT "+origLUT);
                return;
            }

            java.io.DataInputStream dis = new java.io.DataInputStream(is);
            dis.readFully(red);
            dis.readFully(green);
            dis.readFully(blue);
            dis.close();
            dis = null;

        } catch (Exception e) {
            System.err.println("  Error trying to open/read LUT: "+origLUT+". "+e);
            return;
        }

        IndexColorModel   cm = new IndexColorModel(8, 256, red, green, blue);
        ip.setColorModel(cm);
    }



    void drawLine(double x0, double y0, double x1, double y1) {
        int ix0 = (int)(x0+.5);
        int ix1 = (int)(x1+.5);
        int iy0 = (int)(y0+.5);
        int iy1 = (int)(y1+.5);
        ip.drawLine(ix0, iy0, ix1, iy1);
    }

    /**
     * Converts image to BufferedImage which can be processed by Java2d
     * @return buffered image
     */
    public BufferedImage toBufferedImage(){
        // Convert to byte if needed.
        if (! (ip instanceof ByteProcessor) ) {
            ImagePlus imp = new ImagePlus("", ip);
            new ImageConverter(imp).convertToGray8();
            ip = imp.getProcessor();
        }
        byte[] pixels = (byte[]) ip.getPixels();


        BufferedImage bi = new BufferedImage(nx, ny, BufferedImage.TYPE_BYTE_INDEXED, icm);
        int [] buf = new int[nx];

        // Copy the data into the buffer.
        for (int i = 0; i < ny; i++) {
            //create ABGR pixel array
            for( int j = 0; j < nx; j++) {
                int a = pixels[i*nx+j] & 0xFF;
                buf[j] = (0xFF << 24) | (a << 16) | (a << 8) | a;
            }
            //set pixels
            bi.setRGB(0, i, nx, 1, buf, 0, nx);
        }
        return bi;
    }


    public static ArrayList<ImagePlus> getSavedImages() {
        return savedImages;
    }
}

