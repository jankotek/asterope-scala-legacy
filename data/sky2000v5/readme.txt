V/109               SKY2000 Catalog, Version 4         (Myers+ 2002)
================================================================================
SKY2000 Master Catalog, Version 4
     Myers J.R., Sande C.B., Miller A.C., Warren Jr. W.H., Tracewell D.A.
    <Goddard Space Flight Center, Flight Dynamics Division (2002)>
================================================================================
ADC_Keywords: Positional data; Cross identifications; Combined data; Surveys;

Description:
    The SKYMAP  Star Catalog System  consists of a  Master Catalog stellar
    database  and a collection of utility  software designed to create and
    maintain the database and to generate derivative mission star catalogs
    (run catalogs). It contains an extensive compilation of information on
    almost 300000 stars brighter than 8.0 mag.

    The original SKYMAP Master Catalog was generated in the early 1970's.
    Incremental updates and corrections were made over the following years
    but the first complete revision of the source data occurred with
    Version 4.0. This revision also produced a unique, consolidated source
    of astrometric information which can be used by the astronomical
    community. The derived quantities were removed and wideband and
    photometric data in the R (red) and I (infrared) systems were added.

    Version 4 of the SKY2000 Master Catalog was completed in April 2002;
    it marks the global replacement of the variability identifier and
    variability data fields. More details can be found in the description
    file sky2kv4.pdf.

    This version supersedes the previous versions 1(V/95), 2(V/102),
    and 3(V/105).

File Summary:
--------------------------------------------------------------------------------
 FileName    Lrecl   Records    Explanations
--------------------------------------------------------------------------------
ReadMe          80         .   This file
sky2kv4.dat    520    299167   The SkyMap 2000 Version 4 Catalog
sky2kv4.pdf      1    332080   Detailed description of version 4
apxv4.pdf        1     23744   Appendix: Enhancement for Version 4
refs.dat        80       210   Reference table
--------------------------------------------------------------------------------

See also:
    http://mmfd.gsfc.nasa.gov/dist/generalProducts/attitude/ATT_SKYMAP.html :
        Flight Dynamics' star catalog database


Byte-by-byte Description of file: sky2kv4.dat
--------------------------------------------------------------------------------
   Bytes Format Units   Label    Explanations
--------------------------------------------------------------------------------
   1-  7  A7    ---     ---      [SKY2000] Catalog designation
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
 194-202  F9.6  ---     GCI_X   *GCI unit vector in X (J2000)
 203-211  F9.6  ---     GCI_Y   *GCI unit vector in Y (J2000)
 212-220  F9.6  ---     GCI_Z   *GCI unit vector in Z (J2000)
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
     520  A1    ---   f_mag3    *[b] Blended passband #3 mag/color flag
--------------------------------------------------------------------------------
Note on SKY2000:
   Field added to SKY2000 Version 1 for SKY2000 Version 2.
   Note that there are 92 stars for which the SKY2000 name (based on the
   J2000 position) differs from their actual position, due to corrections
   performed on some positions; the SKY2000 names were intentionally kept
   identical to the original SKY2000 version. The list of these 92 stars
   can be found in the "List of stars with differences" section below.
Note on m_HD:
   1. indicates this the brighter component of a system with a companion
      >= 0.3 mag.
   2. indicates this is the fainter component of a system with a companion
      <= 0.3 mag.
   3. for a third component
   9 indicates that this is a blend of two HD stars.  The HD number is the
      lower of the two.
Note on GCI_X:
   cos(Dec)*cos(RA)
Note on GCI_Y:
   cos(Dec)*sin(RA)
Note on GCI_Z:
   sin(Dec)
Note on Sp:
   i.e. HD, AGK3, or SAO
Note on orbPer, magMax and magMin:
   Data for these fields have not been added to the SKY2000 Master Star
      Catalog.
Note on n_mag1:
   Photometry is on the RI system. This byte indicates whether it is
      Johnson or Russian)
Note on p_mag1:
   Filter used (currently, only R)
Note on n_mag2:
   Photometry is on the Johnson, Eggen, or Cousins system. This byte
      indicates which.
Note on p_mag2:
   Filter used (currently, only I)
Note on mag3, v-mag3, e_mag3, n_mag3, p_mag3, r_mag3 and f_mag3:
   Fields added to SKY2000 Version 1 for SKY2000 Version 2.
   The column p_mag3 contains X, which refers to the star trackers
   aboard the RXTE satellite. The response curve of these trackers
   covers the 0.4 to 1.1um range, with its peak sensitivity covering
   the R and I bands (.65 to .9um)
Note on r_RV, r_Umag, r_Vmag, r_mag1, r_mag2, r_mag3:
   The references are normally found in the file "refs.dat"; a few
   references are however not explicited: 71, 82, 85, 86.
--------------------------------------------------------------------------------

List of stars with differences between name and position:
  ---------------------------------------------------------
    Seq#     SKY2000 Name        RA  (J2000)  Dec   Dist(")
  ---------------------------------------------------------
     950 J000455.93-371753.8   000524.427-372126.50   315.4
    1387 J000808.40+445310.0   000808.398+445310.58     0.6
    2083 J001210.13+001437.7   001209.900+001634.50   116.9
    5270 J003100.22+554727.9   003100.493+553908.38   499.5
    5821 J003412.63+575504.9   003405.774+575556.67    55.3
    6593 J003825.35+405909.5   003826.477+405909.55     0.3
    6857 J003956.41+674254.5   003956.260+674255.10     2.3
    8886 J005143.88+382517.1   005144.063+382515.96     2.1
    8904 J005150.54+503811.4   005150.543+503811.03     0.4
    9198 J005334.66+683508.6   005336.548+683522.69    30.9
    9528 J005547.94+200033.4   005548.109+200101.99    28.7
    9703 J005646.97+602146.0   005646.973+602146.21     0.2
    9777 J005712.10+593127.0   005714.978+593157.27    42.6
    9780 J005716.17+602000.5   005716.136+602000.77     0.4
    9783 J005717.10+602020.2   005717.110+602020.18     0.0
   10772 J010301.74+100706.1   010303.755+100746.34    45.3
   10935 J010357.75+211612.0   010357.743+211611.94     0.1
   11136 J010508.17+261420.2   010509.190+261428.54     9.2
   13071 J011658.04+095300.4   011700.059+095310.01    20.2
   13749 J012051.58+685436.7   012057.976+685303.06   109.7
   15451 J013110.09+102228.6   013110.020+102227.74     1.2
   15809 J013306.83+404941.1   013309.314+405020.29    50.0
   16096 J013432.35+762751.6   013448.661+762657.84   170.6
   16173 J013514.37+282632.6   013514.741+282634.29     4.1
   16237 J013541.75+211408.7   013537.290+211655.00   177.1
   16248 J013540.66+314534.9   013541.442+314452.45    44.0
   16726 J013827.64+055951.0   013827.682+055951.64     0.7
   18290 J022122.07+664538.2   014746.099+635021.96 21199.1
   19219 J015259.53+620857.2   015303.902+620931.22    43.8
   19689 J015553.33+641656.4   015553.254+641655.52     1.0
   19920 J015707.86+594512.6   015710.630+594454.56    28.8
   21819 J020759.17+214729.3   020810.542+214729.70    36.8
   22727 J021338.62+622324.9   021339.961+622411.04    50.0
   24482 J022329.47+740618.0   022334.308+740612.34     6.7
   25751 J023115.88+470232.6   023115.874+470231.71     0.9
   25842 J023148.35+302507.7   023148.346+302507.33     0.4
   27242 J023958.13+363253.2   024000.560+363246.49     7.2
   28322 J024619.41+022205.5   024619.401+022206.02     0.5
   29476 J025327.34+121909.0   025327.743+121852.41    16.7
   30807 J030144.35+023315.1   030145.061+023309.56     5.8
   31555 J030637.69+241316.1   030632.700+241317.05    52.2
   31591 J030637.69+241232.5   030642.811+241232.22     4.4
   35015 J032801.40+202751.4   032801.540+202744.74     6.9
   38210 J034700.17+412533.2   034702.115+412538.15     6.3
   38733 J034953.70+640228.9   034955.230+640254.24    33.4
   40493 J040046.44+083813.8   040047.751+083739.51    35.4
   42973 J041522.00-073934.6   041521.759-073930.58     4.4
   47334 J044118.04+290639.1   044118.328+291458.63   499.5
   50223 J045703.16+281627.1   045703.183+281536.67    50.4
   53895 J051443.18+291012.5   051443.030+291831.06   498.6
   56520 J052707.39+341128.0   052707.351+340859.02   149.0
   61148 J054627.52+442013.9   054630.106+442020.76    18.7
   63142 J055434.32+582347.3   055439.283+582435.19    69.1
   63469 J055559.93+135628.7   055600.202+135627.96     2.9
   63957 J055742.86+211158.4   055752.636+211045.45   123.0
   64553 J060019.70+094206.8   060019.729+094207.32     0.7
   65328 J060325.74+273834.4   060326.612+273830.13    12.8
   65714 J060506.41+085728.5   060502.071+085722.07    32.0
   65737 J060506.41+085728.4   060506.415+085728.52     0.1
   66909 J060925.78+342712.8   060926.361+342732.45    19.7
   85169 J071223.78+073709.6   071223.323+073645.54    24.8
   87407 J072006.52-215257.9   072006.525-215258.43     0.5
   94966 J074729.46+055232.4   074736.654+055341.58    69.3
  103406 J081925.69-334908.5   081925.385-334414.30   294.2
  109409 J084314.48+632139.3   084313.967+632002.60    97.0
  117700 J092040.34+080946.3   092041.940+081002.06    28.5
  130493 J102446.02-183843.4   102446.107-183832.64    10.8
  131810 J103135.55+144056.4   103136.379+151105.39  1809.0
  139309 J111051.56+052901.3   111050.378+052735.11    86.3
  140644 J111810.94+313145.6   111810.937+313145.25     0.4
  148904 J120623.01-654233.9   120623.228-654229.55     4.5
  152081 J122519.94+004755.3   122514.395+004610.92   133.1
  168779 J140245.35-782409.3   140245.471-782413.76     4.8
  175819 J144228.46-645841.9   144228.036-645842.87     6.3
  180247 J150727.68+245534.7   150723.589+245607.93    56.7
  186382 J153943.97+530309.5   153939.753+530118.62   127.6
  190048 J155840.93-540332.5   155840.854-540332.30     1.1
  190268 J155946.90+172234.9   155946.928+172234.73     0.4
  190348 J160007.69-382333.5   160007.658-382333.58     0.4
  201050 J165414.09-415008.2   165414.117-415008.66     0.6
  219122 J181430.12+324923.0   181504.796+324926.84   221.8
  237187 J192347.18+311822.8   192347.188+311822.53     0.3
  237917 J192627.59-130753.6   192628.012-130801.70     8.1
  238454 J192824.36+120355.8   192824.493+120407.26    11.6
  251065 J201342.52-613260.0   201342.525-613300.00     0.0
  255760 J203015.53+665255.7   203014.891+664406.83   528.9
  258626 J204118.18+480832.3   204118.270+480828.83     3.7
  259938 J204627.58-265154.0   204627.266-265149.69     6.3
  274773 J215144.80+412252.0   215144.779+412252.12     0.2
  280604 J221950.08+304933.4   221950.248+304933.21     2.1
  283383 J223256.24+802705.2   223359.681+802800.46   127.0
  289440 J230530.54-241823.7   230530.530-241824.09     0.4
  ---------------------------------------------------------

Byte-by-byte Description of file: refs.dat
--------------------------------------------------------------------------------
 Bytes  Format   Units    Label     Explanations
--------------------------------------------------------------------------------
  2-3   I2       ---      RefNo     [1/99]+= Reference number
  4-7   A4       ---    n_RefNo    *Note on the addition of the reference
 8-80   A73      ---      Text     *Text of reference
--------------------------------------------------------------------------------
Note on n_RefNo:
    (98) = via SKYMAP Master Star Catalog, Version 3.7 (Cat. <V/77>)
     .0  = new sources for Version 4.0
     .0a = new sources for Version 4.0a
     +2  = new sources for SKY2000 Version 2
    All other sources added for Versions 4.1 and later.
Note on Text:
   The first line contains generally the title, followed by an asterisk (*)
   when item referenced in the paper, and the number(s) assigned to the
   catalog in the CDS / ADC Archives within < >
   The following lines provide the full reference.
--------------------------------------------------------------------------------

Acknowledgements;
    It is a pleasure to thank Chris Sande and Wayne H. Warren Jr
    for giving a copy of their files.

History:
    The catalog was split into 24 parts at CDS, to avoid too large files.
  * 03-Apr-2002: The unit of the proper motion in RA has been corrected
    from arcsec/yr to s/yr
================================================================================
(End)                                   Francois Ochsenbein [CDS]    16-Sep-2001
