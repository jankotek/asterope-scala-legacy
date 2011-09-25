package skyview.survey;

/** This class implements a cache where images
 *  may be stored after being retrieved.  This class
 *  will generate a FitsImage if the file is in the
 *  cache, or a proxy image if it is not.  The proxy
 *  will have an approximation to the WCS of the real image.
 */
import org.asterope.geometry._;
import org.asterope.util._;
import skyview.executive.Settings;
import skyview.executive.Val;

import java.io.File;
import java.net.URL;

class CachingImageFactory extends ImageFactory {

    def factory(spell2:String):Image = {
        var spell = spell2
        if (Settings.get(Val.SpellSuffix) != null) {
            spell += Settings.get(Val.SpellSuffix);
        }
        if (Settings.get(Val.SpellPrefix) != null) {
            spell = Settings.get(Val.SpellPrefix) + spell;
        }
        // The spell given to this parser is:
        //   url,file,ra,dec,proj,csys,nx,ny,dx,dy
        //    0    1   2  3    4   5    6  7  8  9
        //
        //   url,file,ra,dec,proj,csys,nx,ny,cd11,cd12,cd21,cd22
        //    0    1   2  3    4   5    6  7 8     9   10   11
        //
        // For projections with a fixed reference value, rather than
        // ra and dec, the numbers give the reference pixel values.

        val tokens = spell.split(",");

        val file        = tokens(1);
        // OK it doesn't exist now, so we'll create a proxy for it.
        var s:Scaler = null;
        val    nx = tokens(6).toInt;
        val    ny = tokens(7).toInt;

        // First create the scaler.
        if (tokens.length == 10) {
            // Just CDELTs specified.
            val dx = math.toRadians(tokens(8).toDouble);
            val dy = math.toRadians(tokens(9).toDouble);
            s = new Scaler(0.5*nx, 0.5*ny, -1/dx, 0, 0, 1/dy);
        } else {
            val m00 = math.toRadians(tokens(8).toDouble);
            val m01 = math.toRadians(tokens(9).toDouble);
            val m10 = math.toRadians(tokens(10).toDouble);
            val m11 = math.toRadians(tokens(11).toDouble);
            val det = m00*m11 - m10*m01;
            s = new Scaler(0.5*nx, 0.5*ny, m11/det, -m01/det, -m10/det, m00/det);
        }

        var p:Projection = null;
        val     crval1 = math.toRadians(tokens(2).toDouble);
        val     crval2 = math.toRadians(tokens(3).toDouble);

            if (tokens(4).equalsIgnoreCase("Car") ||
                    tokens(4).equalsIgnoreCase("Ait") ||
                    tokens(4).equalsIgnoreCase("Csc")) {
                // Get a new scaler to shift to the offset specified in the
                // spell.  This shift occurs after everything else.
                s = s.add (new Scaler(tokens(2).toDouble - 0.5 - 0.5*nx,
                        tokens(3).toDouble - 0.5 - 0.5*ny,
                        1,0,0,1));
                p = new Projection(tokens(4));

            } else if (tokens(4).equalsIgnoreCase("Ncp")) {

                // Sin projection with projection centered at pole.
                val xproj = Array[Double](crval1, math.Pi/2);
                if (crval2 < 0) {
                    xproj(1) = - xproj(1);
                }

                val poleOffset = math.sin(xproj(1)-crval2);
                // Have we handled South pole here?

                p = new Projection("Sin", xproj);

                // NCP scales the Y-axis to accommodate the distortion of the SIN projection away
                // from the pole.
                var ncpScale = new Scaler(0, poleOffset, 1, 0, 0, 1);
                ncpScale = ncpScale.add(new Scaler(0., 0., 1,0,0,1/math.sin( crval2 ) ) );
                s = ncpScale.add(s);

            } else {
                p = new Projection(tokens(4),
                        Array[Double](crval1, crval2));

            }

        val c = CoordinateSystem.factory(tokens(5));
        val    w = new WCS(c, p, null,s);

        new ProxyImage(tokens(0)+","+file, w, nx, ny, 1,urlRetrivierFab);
    }

    object urlRetrivierFab extends ImageFactory{

      def factory(spell:String):Image = {
        val tokens = spell.split(",")

        // Retrieve to temporary name and rename only after
        // successful retrieval.        
        return new FitsImage(tokens(0));
      }
    }
}
