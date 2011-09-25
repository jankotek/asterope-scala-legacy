package org.asterope.data.catalog

import org.asterope.data._
import org.asterope.util._
import java.net.URL
import java.io.File
import collection.mutable.ListBuffer
import java.util.regex.Pattern

class Sky2000Catalog extends LineTranslateCatalog[LiteStar]{

  val urls: List[URL] = List(
    new URL(new File(".").toURI.toURL, "data/sky2000v5/ATT_sky2kv5.cat.bz2"),
    new URL("http://fdf.gsfc.nasa.gov/dist/generalProducts/attitude/ATT_sky2kv5.cat.gz")
    )


  protected val sky2000FormatId = Nomenclature.formatIdByPrefix("SKY2000")
  protected val hdFormatId = Nomenclature.formatIdByPrefix("HD")
  protected val ppmFormatId = Nomenclature.formatIdByPrefix("PPM")
  protected val hrFormatId = Nomenclature.formatIdByPrefix("HR")
  protected val saoFormatId = Nomenclature.formatIdByPrefix("SAO")


  def translateLine(line:String):Option[LiteStar] = {

//           if(v.matches("""[0-9-\+\.J]+""") ){
//         val prefix = if(col.getName == "Vname") "NSV" else col.getName //TODO sky2000 specific hack
//         names ::=  Nomenclature(prefix+" "+v) //contains only number, name of col is prefix
//       }else if(v.matches("(CD|CP)(-|\\+)([0-9]{2})([0-9]+)"))  //TODO just hack to import Sky2000, add proper space handling into Nomenclature
//         names ::= Nomenclature(v.replaceAll("(CD|CP)(-|\\+)([0-9]{2})([0-9]+)","$1$2$3 $4"))
//       else
//         names ::= Nomenclature(v)

    //last line is borged
    if (line.length < 4)
      return None;

    val l = parser.parseLine(line).get

    //position
    val ra = l.getRa().get
    val de = l.getDe().get

    val mag = l.getMag("Vmag").getOrElse{
          l.getMag("Vder").getOrElse{
            Log.warning("Magnitude not defined, skipping star: "+line)
            return None
          }
        }

    val nom = new ListBuffer[Nomenclature]();


    nom += Nomenclature.parseWithID("SKY2000 "+l.getString("SKY2000").get,sky2000FormatId)

    l.getString( "HD").foreach{ hd =>
     nom += Nomenclature("HD "+hd,hdFormatId)
    }
    //TODO handle HD uncertain and duplication flags
    //      42  A1    ---   m_HD      *[1239]? HD duplicity indication
    //        43  A1    ---   u_HD       HD identification uncertain
    if (l.getString( "SAO").isDefined) {
      var sao =	l.getString( "SAO").get
      if (l.getString("m_SAO").isDefined)
        sao += l.getString( "m_SAO").get
      sao = "SAO " + sao;
      nom += Nomenclature(sao.replace(":",""), saoFormatId);
    }

    l.getString( "DM").foreach{ dm=>
//      var dmComponent = 0;
//      if (l.getString( "m_DM") != null)
//        dmComponent = CatalogUtils.letterToInt(l.getString("m_DM"));
      nom+= Nomenclature.parse(dm.replaceAll("[\\ ]+", " ")
          .replaceAll("([A-Z]{2}[\\-+][0-9]{2})([0-9]+)","$1 $2") /*+ dmComponent * 100*/);
    }

    l.getString( "HR").foreach{hr=>
      nom+=Nomenclature.parseWithID("HR " + hr,hrFormatId); //TODO component number was removed here for star name import, what is it?
    }


    l.getString("PPM").foreach{ ppm =>
      nom+=Nomenclature.parseWithID("PPM "+ ppm,ppmFormatId )
    }


    l.getString("Name").map(_.replaceAll("[ ]+", " ").trim()). foreach{name =>
      if (name.startsWith("AG"))
        nom+=Nomenclature.parse(name);
      else {

        //posible values:
        // 78nu  Cet
        // 15    Tri
        // lam2For
        val pattern = Pattern.compile("^([0-9]*)([a-z]*)[ ]*([0-9]*)[ ]*([A-Z]{1}[A-Za-z]{2})$");
        val matcher = pattern.matcher(name);
        if (!matcher.matches)
          Log.warning("No match for name: " + name);
        else {

          def isNotBlank(s:String) = s!=null && s!=""
          if (isNotBlank(matcher.group(1))) {
            val id = matcher.group(1) + " " + matcher.group(4);
            nom+=Nomenclature.parse(id);
          }
          if (isNotBlank(matcher.group(2))) {
            var letter = ""+GreekLetter.smallGreekLetter(GreekLetter.completeName(matcher.group(2)));
            assert(letter != null, "greek letter not found: " + letter)
            //add binary star number
            if (isNotBlank(matcher.group(3)))
              letter += matcher.group(3);
            val id = letter + " " + matcher.group(4)
            nom+=Nomenclature.parse(id);
          }
        }

      }
    }


    val posAngle = l.getAngle("PA")
    val separation = l.getAngle("sep")

    //TODO handle VName and variability
    //  99-108  A10   ---     Name     Star name (or AGK3 number)
    // 109-118  A10   ---     Vname    Variable star name (or
    //                                        doubtful variability)


    return Some(LiteStar(ra=ra, de=de, mag=mag,names=nom.toList,
      posAngle = posAngle, separation = separation))
  }

  val catalogSize = 299460 - 2; //two records doesn ot have magnitude and are not included


  lazy val DEF = """   1-  7  A7    ---     ---      [SKY2000] Catalog designation
   9- 27  A19   ---     SKY2000 *Identifier based on J2000 position
  28- 35  I8    ---     ID       Skymap number
  36- 41  I6    ---     HD       ?Henry Draper <III/135> number
      42  A1    ---   m_HD      *[1239]? HD duplicity indication
      43  A1    ---   u_HD       HD identification uncertain
  44- 49  I6    ---     SAO      ? SAO <I/131> number
      50  A1    ---   m_SAO      SAO component
  51- 61  A11   ---     DM       Durchmusterung (BD <I/122>; SD <I/119>;
                                        CD <I/114>;  CP <I/108>)
      62  A1    ---   m_DM       Durchmusterung supplement letter
      63  A1    ---   u_DM       [: ] DM identification uncertain
  64- 67  I4    ---     HR       ?Harvard Revised <V/50> num. (=BS)
  68- 77  A10   ---     WDS      Washington Double Stars <I/237> number
  78- 82  A5    ---   m_WDS      WDS components
      83  A1    ---   u_WDS      [: ] WDS identification uncertain
  84- 89  I6    ---     PPM      ?Position and Proper Motion number
                                         (<I/146>, <I/193>, <I/208>)
      90  A1    ---   u_PPM      [: ] PPM identification uncertain
  91- 98  I8    ---   ID_merg    ?Skymap num. of last skymap entry merged
                                       with this star
  99-108  A10   ---     Name     Star name (or AGK3 number)
 109-118  A10   ---     Vname    Variable star name (or
                                        doubtful variability)
 119-120  I2    h       RAh      Right ascension (J2000) hours
 121-122  I2    min     RAm      Right ascension (J2000) minutes
 123-129  F7.4  s       RAs      Right ascension (J2000) seconds
     130  A1    ---     DE-      Declination sign
 131-132  I2    deg     DEd      Declination degrees (J2000)
 133-134  I2    arcmin  DEm      Declination minutes (J2000)
 135-140  F6.3  arcsec  DEs      Declination seconds (J2000)
 141-146  F6.4  arcsec e_pos     Position uncertainty
     147  A1    ---   f_pos      [b] Blended position flag
 148-149  I2    ---   r_pos      Source of position
 150-157  F8.5  s/a     pmRA     Proper motion in RA (J2000)
 158-165  F8.4 arcsec/a pmDE     Proper motion in Dec (J2000)
 166-167  I2    ---   r_pm       ?Source of proper motion data
 168-173  F6.1  km/s    RV       ?Radial velocity
 174-175  I2    ---   r_RV      *?Source of radial velocity data
 176-183  F8.5  arcsec  Plx      ?Trigonometric parallax
 184-191  F8.6  arcsec e_Plx     ?Trigonometric parallax uncertainty
 192-193  I2    ---   r_Plx      ?Source of trigonometric parallax data
 194-202  F9.6  ---     GCI_X   *GCI rade2Vector vector in X (J2000)
 203-211  F9.6  ---     GCI_Y   *GCI rade2Vector vector in Y (J2000)
 212-220  F9.6  ---     GCI_Z   *GCI rade2Vector vector in Z (J2000)
 221-226  F6.2  deg     GLON     Galactic longitude (B1950)
 227-232  F6.2  deg     GLAT     Galactic latitude (B1950)
 233-238  F6.3  mag     Vmag     ?Observed visual magnitude (V or v)
 239-243  F5.2  mag     Vder     ?Derived visual magnitude
 244-248  F5.3  mag   e_Vmag     ?Derived v or observed visual magnitude
                                        uncertainty
     249  A1    ---   f_Vmag     [b] Blended visual magnitude flag
 250-251  I2    ---   r_Vmag    *?Source of visual magnitude
     252  I1    ---   n_Vmag     ?V magnitude derivation flag
 253-258  F6.3  mag     Bmag     ?B-magnitude (observed)
 259-264  F6.3  mag     B-V      ?B-V color (observed)
 265-269  F5.3  mag   e_Bmag     ?B or (B-V) magnitude uncertainty
     270  A1    ---   f_Bmag     [b] Blended b-magnitude flag
 271-272  I2    ---   r_Bmag     ?Source of b-magnitude
 273-278  F6.3  mag     Umag     ?U-magnitude (observed)
 279-284  F6.3  mag     U-B      ?U-B color (observed)
 285-289  F5.3  mag   e_Umag     ?U or (U-B) magnitude uncertainty
     290  A1    ---   n_Umag     Blended u-magnitude flag
 291-292  I2    ---   r_Umag    *?Source of u-magnitude
 293-296  F4.1  mag     Ptv      ?Photovisual magnitude (observed)
 297-298  I2    ---   r_Ptv      ?Source of ptv magnitudes
 299-302  F4.1  mag     Ptg      ?Photographic magnitude (observed)
 303-304  I2    ---   r_Ptg      ?Source of ptg magnitudes
 305-334  A30   ---     SpMK     Morgan-Keenan (MK) spectral type
 335-336  I2    ---   r_SpMK     ?Source of MK spectral type data
 337-339  A3    ---     Sp      *One-dimensional spectral class
 340-341  I2    ---   r_Sp       ?Source of one-dimen. spectral class
 342-348  F7.3  arcsec  sep      ?Separation of brightest and second
                                        brightest components
 349-353  F5.2  mag     Dmag     ?Magnitude difference of the brightest
                                        and second brightest components
 354-360  F7.2  yr      orbPer  *?Orbital period
 361-363  I3    deg     PA       ?Position angle
 364-370  F7.2  yr      date     ?Year of observation (AD)
 371-372  I2    ---   r_dup      ?Source of multiplicity data
     373  A1    ---   n_Dmag     Passband of multiple star mag. dif.
 374-380  F7.4  deg     dist1    ?Distance to nearest neighboring star in
                                        the master catalog
 381-387  F7.4  deg     dist2    ?Dist. to nearest neighboring master
                                        cat. star no more than 2 mag. fainter
 388-395  I8    ---     ID_A     ?Skymap number of primary component
 396-403  I8    ---     ID_B     ?Skymap number of second component
 404-411  I8    ---     ID_C     ?Skymap number of third component
 412-416  F5.2  mag     magMax  *?Maximum variable magnitude
 417-421  F5.2  mag     magMin  *?Minimum variable magnitude
 422-426  F5.2  mag     varAmp  ?Variability amplitude
     427  A1    ---   n_varAmp   Passband of variability amplitude
 428-435  F8.2  d       varPer   ?Period of variability
 436-443  F8.2  d       varEp    ?Epoch of variability (JD-2400000)
 444-446  I3    ---     varTyp   ?Type of variable star
 447-448  I2    ---   r_var      ?Source of variability data
 449-454  F6.3  mag     mag1     ?Passband #1-magnitude (observed)
 455-460  F6.3  mag     v-mag1   ?v - passband #1 color
 461-465  F5.3  mag   e_mag1     ?Passband #1 uncertainty in mag. or col.
     466  A1    ---   n_mag1    *[RJC] Passband #1 photometric system
     467  A1    ---   p_mag1    *[R] Passband #1
 468-469  I2    ---   r_mag1    *?Source of passband #1: mag. or color
 470-475  F6.3  mag     mag2     ?Passband #2-magnitude (observed)
 476-481  F6.3  mag     v-mag2   ?v - passband #2 color
 482-486  F5.3  mag   e_mag2     ?Passband #2 uncertainty in mag. or col.
     487  A1    ---   n_mag2    *[JEC] Passband #2 photometric system
     488  A1    ---   p_mag2    *[I] Passband #2
 489-490  I2    ---   r_mag2    *?Source of passband #2: mag. or color
 491-496  F6.3  mag     ci1-2    ?Passband #1 - passband #2 color
     497  A1    ---   f_mag1     [b] Blended passband #1 mag/color flag
     498  A1    ---   f_mag2     [b] Blended passband #2 mag/color flag
 499-504  F6.3  mag     mag3    *?Passband #3-magnitude (observed)
 505-510  F6.3  mag     v-mag3  *?v - passband #3 color
 511-515  F5.3  mag   e_mag3    *?Passband #3 uncertainty in mag. or col.
     516  A1    ---   n_mag3    *Passband #3 photometric system
     517  A1    ---   p_mag3    *[X] Passband #3
 518-519  I2    ---   r_mag3    *?Source of passband #3: mag. or color
     520  A1    ---   f_mag3    *[b] Blended passb    and #3 mag/color flag""";

  lazy val parser = DataParserAdc.ADCLineParser(DEF);

}