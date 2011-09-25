package org.asterope.data.catalog;

import java.io._
import org.asterope.util.ScalaTestCase

class DataParserAdcTest extends ScalaTestCase {

  /** one record from TYCHO catalog */
  val TL = "0001 00008 1| |  2.31750494|  2.23184345|  -16.3|   -9.0| 68| 73| 1.7| 1.8|1958.89|1951.94| 4|1.0|1.0|0.9|1.0|12.146|0.158|12.146|0.223|999| |         |  2.31754222|  2.23186444|1.67|1.54| 88.0|100.8| |-0.2";

  val TYCHO2_DEF = "   1-  4  I4      ---     TYC1     [1,9537]+= TYC1 from TYC or GSC (1)\n" +
          "   6- 10  I5      ---     TYC2     [1,12121]  TYC2 from TYC or GSC (1)\n" +
          "      12  I1      ---     TYC3     [1,3]      TYC3 from TYC (1)\n" +
          "      14  A1      ---     pflag    [ PX] mean position flag (2)\n" +
          "  16- 27  F12.8   deg     RAmdeg   []? Mean Right Asc, ICRS, epoch=J2000 (3)\n" +
          "  29- 40  F12.8   deg     DEmdeg   []? Mean Decl, ICRS, at epoch=J2000 (3)\n" +
          "  42- 48  F7.1    mas/yr  pmRA     [-4418.0,6544.2]? prop. mot. in RA*cos(dec)\n" +
          "  50- 56  F7.1    mas/yr  pmDE     [-5774.3,10277.3]? prop. mot. in Dec\n" +
          "  58- 60  I3      mas   e_RAmdeg   [3,183]? s.e. RA*cos(dec),at mean epoch (5)\n" +
          "  62- 64  I3      mas   e_DEmdeg   [1,184]? s.e. of Dec at mean epoch (5)\n" +
          "  66- 69  F4.1   mas/yr e_pmRA     [0.2,11.5]? s.e. prop mot in RA*cos(dec)(5)\n" +
          "  71- 74  F4.1   mas/yr e_pmDE     [0.2,10.3]? s.e. of proper motion in Dec(5)\n" +
          "  76- 82  F7.2    yr      EpRAm    [1915.95,1992.53]? mean epoch of RA (4)\n" +
          "  84- 90  F7.2    yr      EpDEm    [1911.94,1992.01]? mean epoch of Dec (4)\n" +
          "  92- 93  I2      ---     Num      [2,36]? Number of positions used\n" +
          "  95- 97  F3.1    ---   q_RAmdeg   [0.0,9.9]? Goodness of fit for mean RA (6)\n" +
          "  99-101  F3.1    ---   q_DEmdeg   [0.0,9.9]? Goodness of fit for mean Dec (6)\n" +
          " 103-105  F3.1    ---   q_pmRA     [0.0,9.9]? Goodness of fit for pmRA (6)\n" +
          " 107-109  F3.1    ---   q_pmDE     [0.0,9.9]? Goodness of fit for pmDE (6)\n" +
          " 111-116  F6.3    mag     BTmag    [2.183,16.581]? Tycho-2 BT magnitude (7)\n" +
          " 118-122  F5.3    mag   e_BTmag    [0.014,1.977]? s.e. of BT (7)\n" +
          " 124-129  F6.3    mag     VTmag    [1.905,15.193]? Tycho-2 VT magnitude (7)\n" +
          " 131-135  F5.3    mag   e_VTmag    [0.009,1.468]? s.e. of VT (7)\n" +
          " 137-139  I3      ---     prox     [3,999] proximity indicator (8)\n" +
          "     141  A1      ---     TYC      [T] Tycho-1 star (9)\n" +
          " 143-148  I6      ---     HIP      [1,120404]? Hipparcos number\n" +
          " 149-151  A3      ---     CCDM     CCDM component identifier for HIP stars(10)\n" +
          " 153-164  F12.8   deg     RAdeg    Observed Tycho-2 Right Ascension, ICRS\n" +
          " 166-177  F12.8   deg     DEdeg    Observed Tycho-2 Declination, ICRS\n" +
          " 179-182  F4.2    yr    EpRA-1990  [0.81,2.13]  epoch-1990 of RAdeg\n" +
          " 184-187  F4.2    yr    EpDE-1990  [0.72,2.36]  epoch-1990 of DEdeg\n" +
          " 189-193  F5.1    mas   e_RAdeg    s.e.RA*cos(dec), of observed Tycho-2 RA (5)\n" +
          " 195-199  F5.1    mas   e_DEdeg    s.e. of observed Tycho-2 Dec (5)\n" +
          "     201  A1      ---     posflg   [ DP] type of Tycho-2 solution (11)\n" +
          " 203-206  F4.1    ---     corr     [-1,1] correlation (RAdeg,DEdeg)";

  def testParseADSDef(){
        val p =  DataParserAdc.ADCLineParser(TYCHO2_DEF);

        val ra = p.getCol("RAmdeg");
        assert(ra != null);
        assert(ra.start === 15);
        assert(ra.end ===  27);
        //assert(ra.typ ===  "F12.8");
        assert(ra.unit.get ===  "deg");
        assert(ra.colDesc.get ===  "[]? Mean Right Asc, ICRS, epoch=J2000 (3)");

        val epRa = p.getCol("EpRA-1990");
        assert( epRa !=null);
        assert( epRa.start ===  178);
        assert( epRa.end ===  182);
        //assert( epRa.typ ===  "F4.2");
        assert( epRa.unit.get ===  "yr");
        assert( epRa.colDesc.get ===  "[0.81,2.13]  epoch-1990 of RAdeg");

      val pmRa = p.getCol("pmRA");
      assert( pmRa !=null);
      assert( pmRa.start ===  41);
      assert( pmRa.end ===  48);
      //assert( pmRa.typ ===  "F7.1");
      assert( pmRa.unit.get ===  "mas/yr");
      assert( pmRa.colDesc.get ===  "[-4418.0,6544.2]? prop. mot. in RA*cos(dec)");

    }

    def testParseLine(){
        val p = DataParserAdc.ADCLineParser(TYCHO2_DEF);
        val l = p.parseLine(TL).get

        assert( l.getString( "TYC1").get ===  "0001");
        assert( l.getString( "TYC2").get ===  "00008");
        assert( l.getString( "TYC3").get ===  "1");
        assert( l.getDouble("RAmdeg").get ===   2.31750494);
        assert( l.getDouble("DEmdeg").get ===   2.23184345);
    }

    def testParseTSVCols {
      val tsv = """#
#   VizieR Astronomical Server: vizier.u-strasbg.fr 	2009-12-25T10:57:46
#   (replaces the 'Astrores' format originally described at
#    http://vizier.u-strasbg.fr/doc/astrores.htx)
#   In case of problem, please report to:	question@simbad.u-strasbg.fr
#
#
#Coosys	J2000:	eq_FK5 J2000
#INFO	-ref=VIZ4b34996d4bcf
#INFO	-out.max=50000

#RESOURCE=yCat_1239
#Name: I/239
#Title: The Hipparcos and Tycho Catalogues (ESA 1997)
#Coosys	J2000_1991.250:	eq_FK5 J2000
#Table	I_239_hip_main:
#Name: I/239/hip_main
#Title: The Hipparcos Main Catalogue\vizContent{timeSerie}
#Column	_RAJ2000	(F10.6)	Right ascension (FK5) Equinox=J2000.0 Epoch=J2000.000, proper motions taken into account  (computed by VizieR, not part of the original data)	[ucd=pos.eq.ra;meta.main]
#Column	_DEJ2000	(F10.6)	Declination (FK5) Equinox=J2000.0 Epoch=J2000.000, proper motions taken into account  (computed by VizieR, not part of the original data)	[ucd=pos.eq.dec;meta.main]
#Column	HIP	(I6)	Identifier (HIP number) (H1)	[ucd=meta.id;meta.main]
#Column	Vmag	(F5.2)	? Magnitude in Johnson V (H5)	[ucd=phot.mag;em.opt.V]
#Column	B-V	(F6.3)	? Johnson B-V colour (H37)	[ucd=phot.color;em.opt.B;em.opt.V]
_RAJ2000;_DEJ2000;HIP;Vmag;B-V
deg;deg;;mag;mag
----------;----------;------;-----;------
  0.000899;  1.089009;     1; 9.10; 0.482"""

      val is = new BufferedReader(new InputStreamReader(new ByteArrayInputStream(tsv.getBytes)))
      val p = DataParserAdc.vizierTsvLineParser(is)

      assert(p.getCol("_RAJ2000").unit.get === "deg")
      assert(p.getCol("_DEJ2000").unit.get === "deg")
      assert(p.getCol("Vmag").unit.get === "mag")

      val l1 = p.parseLine(is.readLine).get
      assert(l1.getString("_RAJ2000").get === "0.000899")
      assert(l1.getString("_DEJ2000").get === "1.089009")
      assert(l1.getString("HIP").get === "1")
      assert(l1.getString("Vmag").get === "9.10")
      assert(l1.getString("B-V").get === "0.482")

    }

}
