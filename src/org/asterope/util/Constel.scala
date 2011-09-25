package org.asterope.util

import org.asterope.geometry.CoordinateSystem

/**
 * Enumeration of constellations and some related utils.
 * 
 * 
 * @author Jan Kotek
 *
 */
object
Constel extends Enumeration{

  val 	And	= Value("And")
  val 	Ant	= Value("Ant")
  val 	Aps	= Value("Aps")
  val 	Aqr	= Value("Aqr")
  val 	Aql	= Value("Aql")
  val 	Ara	= Value("Ara")
  val 	Ari	= Value("Ari")
  val 	Aur	= Value("Aur")
  val 	Boo	= Value("Boo")
  val 	Cae	= Value("Cae")
  val 	Cam	= Value("Cam")
  val 	Cnc	= Value("Cnc")
  val 	CVn	= Value("CVn")
  val 	CMa	= Value("CMa")
  val 	CMi	= Value("CMi")
  val 	Cap	= Value("Cap")
  val 	Car	= Value("Car")
  val 	Cas	= Value("Cas")
  val 	Cen	= Value("Cen")
  val 	Cep	= Value("Cep")
  val 	Cet	= Value("Cet")
  val 	Cha	= Value("Cha")
  val 	Cir	= Value("Cir")
  val 	Col	= Value("Col")
  val 	Com	= Value("Com")
  val 	CrA	= Value("CrA")
  val 	CrB	= Value("CrB")
  val 	Crv	= Value("Crv")
  val 	Crt	= Value("Crt")
  val 	Cru	= Value("Cru")
  val 	Cyg	= Value("Cyg")
  val 	Del	= Value("Del")
  val 	Dor	= Value("Dor")
  val 	Dra	= Value("Dra")
  val 	Equ	= Value("Equ")
  val 	Eri	= Value("Eri")
  val 	For	= Value("For")
  val 	Gem	= Value("Gem")
  val 	Gru	= Value("Gru")
  val 	Her	= Value("Her")
  val 	Hor	= Value("Hor")
  val 	Hya	= Value("Hya")
  val 	Hyi	= Value("Hyi")
  val 	Ind	= Value("Ind")
  val 	Lac	= Value("Lac")
  val 	Leo	= Value("Leo")
  val 	LMi	= Value("LMi")
  val 	Lep	= Value("Lep")
  val 	Lib	= Value("Lib")
  val 	Lup	= Value("Lup")
  val 	Lyn	= Value("Lyn")
  val 	Lyr	= Value("Lyr")
  val 	Men	= Value("Men")
  val 	Mic	= Value("Mic")
  val 	Mon	= Value("Mon")
  val 	Mus	= Value("Mus")
  val 	Nor	= Value("Nor")
  val 	Oct	= Value("Oct")
  val 	Oph	= Value("Oph")
  val 	Ori	= Value("Ori")
  val 	Pav	= Value("Pav")
  val 	Peg	= Value("Peg")
  val 	Per	= Value("Per")
  val 	Phe	= Value("Phe")
  val 	Pic	= Value("Pic")
  val 	Psc	= Value("Psc")
  val 	PsA	= Value("PsA")
  val 	Pup	= Value("Pup")
  val 	Pyx	= Value("Pyx")
  val 	Ret	= Value("Ret")
  val 	Sge	= Value("Sge")
  val 	Sgr	= Value("Sgr")
  val 	Sco	= Value("Sco")
  val 	Scl	= Value("Scl")
  val 	Sct	= Value("Sct")
  val 	Ser	= Value("Ser")
  val 	Sex	= Value("Sex")
  val 	Tau	= Value("Tau")
  val 	Tel	= Value("Tel")
  val 	Tri	= Value("Tri")
  val 	TrA	= Value("TrA")
  val 	Tuc	= Value("Tuc")
  val 	UMa	= Value("UMa")
  val 	UMi	= Value("UMi")
  val 	Vel	= Value("Vel")
  val 	Vir	= Value("Vir")
  val 	Vol	= Value("Vol")
  val 	Vul	= Value("Vul")

  
	 /**
	  * Regular expression to match constellation abreviation:
	  * (And|Ant|...|Vul) 	
	  */
	 lazy val abreviationRegExp:String = {
		 var ret =""
		 values.foreach(ret+=_+"|");
     ret += ret.toLowerCase + ret.toUpperCase 
		 ret = ret.substring(0,ret.length-1)
		 ret
	 }
	 
	 def fromLowerCase(constel:String):Option[Value] = {
     val lowerCase = constel.toLowerCase
		 values.foreach{c=>
		 	if(c.toString.toLowerCase.equals(lowerCase))
		 		return Some(c);
		 }
     None
	 }



	/**
	 * Return the constellation name corresponding to a given position.
	 * <P>
	 * Roman, Nancy Grace, "Identification of a Constellation from a Position"
	 * Pub. Astron. Soc. Pac. 99, 695, (1987).
	 * <P>
	 * This method comes from software by S. L. Moshier, taken from JParsec
	 *
	 */
  def constelOnPosition(pos:Vector3d):Constel.Value = {
    val pos1875 = Vector3d(j1875rotater.transform(pos.toArray))
    val ra0 = pos1875.getRa.toArcSec.toInt
    val de0 = pos1875.getDe.toArcSec.toInt

		/*
		 * FIND CONSTELLATION SUCH THAT THE DECLINATION ENTERED IS HIGHER THAN
		 * THE LOWER BOUNDARY OF THE CONSTELLATION WHEN THE UPPER AND LOWER
		 * RIGHT ASCENSIONS FOR THE CONSTELLATION BOUND THE ENTERED RIGHT
		 * ASCENSION
		 */
    var i = 0;
    while(i<spatialBounds.size){
      val raLow = spatialBounds(i)
      val raHi = spatialBounds(i+1)
      val de = spatialBounds(i+2)
      if (ra0 >= raLow && ra0 < raHi && de0 > de)
        return Constel(spatialBounds(i+3))
      i+=4
    }
    throw new Error("Constel not found for position: "+pos)
  }

  
  /** rotater to transform vector2Rade from J2000 to B1875 */
  lazy private val j1875rotater = CoordinateSystem.factory("J1875").getRotater
  /** rotater to transform vector2Rade from J1875 to J2000 */
  lazy private val j1875derotater = j1875rotater.inverse

  /*
   * Table of constellation boundaries for rapid identification.
   * Values: low ra, hi ra, de, constel (angles in Arc Sec)
   * Lines apply to equinox B1875
   *
   * Data are from from http://cdsarc.u-strasbg.fr/viz-bin/Cat?VI/42
   *
   */
  lazy private val spatialBounds:Array[Int] = Array(
    0,1296000,316800,UMi.id, 432000,783000,311400,UMi.id, 1134000,1242000,310200,UMi.id, 972000,1134000,309600,UMi.id, 0,432000,306000,Cep.id, 495000,576000,295200,Cam.id, 0,270000,288000,Cep.id, 576000,783000,288000,Cam.id, 945000,972000,288000,UMi.id, 1089000,1134000,288000,Dra.id,
    0,189420,277200,Cep.id, 621000,733500,277200,Cam.id, 892800,945000,270000,UMi.id, 1089000,1116000,270000,Cep.id, 430200,495000,264600,Cam.id, 495000,612000,264600,Dra.id, 702000,892800,252000,UMi.id, 167400,184500,244800,Cas.id, 1102500,1116000,241200,Dra.id, 612000,648000,239400,Dra.id,
    0,18000,237600,Cep.id, 756000,846000,237600,UMi.id, 1273500,1296000,237600,Cep.id, 648000,729000,230400,Dra.id, 729000,778500,226800,Dra.id, 1251000,1273500,226800,Cep.id, 329400,378000,223200,Cam.id, 1080000,1102500,221400,Dra.id, 1108980,1112400,219300,Cep.id, 378000,430200,216000,Cam.id,
    430200,454500,216000,UMa.id, 1067400,1080000,214200,Dra.id, 1080000,1108980,214200,Cep.id, 1234800,1251000,212700,Cep.id, 0,131400,210600,Cas.id, 1048500,1067400,208800,Dra.id, 91800,103020,207000,Cas.id, 131400,167400,205200,Cas.id, 167400,171000,205200,Cam.id, 1205100,1234800,202500,Cep.id,
    270000,329400,201600,Cam.id, 757800,778500,199800,UMa.id, 778500,1048500,199800,Dra.id, 171000,180000,198000,Cam.id, 1195200,1205100,198000,Cep.id, 1112400,1186200,197400,Cep.id, 0,91800,194400,Cas.id, 329400,351000,194400,Lyn.id, 652500,729000,190800,UMa.id, 823500,850500,190800,Dra.id,
    1186200,1195200,189900,Cep.id, 180000,270000,189000,Cam.id, 1234800,1260000,189000,Cas.id, 850500,918000,185400,Dra.id, 110280,135900,181800,Per.id, 918000,984600,181800,Dra.id, 0,73800,180000,Cas.id, 73800,90000,180000,Per.id, 351000,367200,180000,Lyn.id, 1260000,1296000,180000,Cas.id,
    729000,757800,174600,UMa.id, 0,60300,172800,Cas.id, 1273500,1296000,172800,Cas.id, 981480,984600,171000,Her.id, 984600,1030500,171000,Dra.id, 1030500,1035000,171000,Cyg.id, 90000,110280,169200,Per.id, 454500,495000,169200,UMa.id, 9000,46800,165600,Cas.id, 648000,652500,162000,UMa.id,
    367200,397800,160200,Lyn.id, 1183020,1186200,158400,Cyg.id, 1181280,1183020,157500,Cyg.id, 1035000,1047600,156600,Cyg.id, 495000,549000,151200,UMa.id, 549000,582300,144000,UMa.id, 833400,850500,144000,Boo.id, 850500,882000,144000,Her.id, 499500,517500,143100,Lyn.id, 0,135900,132300,And.id,
    135900,138600,132300,Per.id, 1045320,1047600,131400,Lyr.id, 243000,253380,129600,Per.id, 1173600,1181280,129600,Cyg.id, 1181280,1188000,129600,Lac.id, 352800,397800,127800,Aur.id, 397800,418500,127800,Lyn.id, 0,108000,126000,And.id, 1188000,1232100,126000,Lac.id, 1232100,1234800,124200,Lac.id,
    1234800,1269000,124200,And.id, 138600,146700,122400,Per.id, 582300,594000,122400,UMa.id, 648000,666000,122400,CVn.id, 418500,499500,120600,Lyn.id, 499500,533700,120600,LMi.id, 38700,76020,118800,And.id, 819900,833400,118800,Boo.id, 1269000,1282500,115500,And.id, 666000,715500,115200,CVn.id,
    1282500,1296000,112800,And.id, 753720,757800,110700,CVn.id, 130500,146700,110400,Tri.id, 146700,243000,110400,Per.id, 243000,256500,108000,Aur.id, 981480,1045320,108000,Lyr.id, 594000,648000,104400,UMa.id, 1062000,1129500,104400,Cyg.id, 256500,317700,102600,Aur.id, 533700,567000,102600,LMi.id,
    715500,753720,102600,CVn.id, 0,3600,100800,And.id, 76020,90000,100800,Tri.id, 317700,352800,100800,Aur.id, 425700,432000,100800,Gem.id, 1129500,1173600,100800,Cyg.id, 1039920,1062000,99000,Cyg.id, 103500,130500,98100,Tri.id, 873000,882000,97200,CrB.id, 814500,819900,93600,Boo.id,
    819900,873000,93600,CrB.id, 991800,1018800,93600,Lyr.id, 580500,594000,91800,LMi.id, 1018800,1039920,91800,Lyr.id, 90000,103500,90000,Tri.id, 38700,45900,85500,Psc.id, 567000,580500,84600,LMi.id, 1147500,1156500,84600,Vul.id, 307800,317700,82200,Tau.id, 3600,7680,79200,And.id,
    859500,865800,79200,Ser.id, 317700,335700,77400,Gem.id, 1071000,1093500,76500,Vul.id, 1018800,1039500,75900,Vul.id, 7680,45900,75600,And.id, 1093500,1110600,73800,Vul.id, 421620,425700,72000,Gem.id, 1110600,1147500,70200,Vul.id, 1039500,1071000,69000,Vul.id, 177300,181800,68400,Ari.id,
    1018800,1026000,66600,Sge.id, 307800,311400,64800,Ori.id, 335700,340620,63000,Gem.id, 1026000,1071000,58200,Sge.id, 268200,288000,57600,Tau.id, 859500,868500,57600,Her.id, 1071000,1093500,56700,Sge.id, 249300,268200,55800,Tau.id, 288000,302400,55800,Tau.id, 693000,729000,54000,Com.id,
    931500,985500,51600,Her.id, 640800,693000,50400,Com.id, 405000,421620,48600,Gem.id, 904500,931500,46200,Her.id, 0,7680,45000,Peg.id, 302400,311400,45000,Tau.id, 378000,405000,45000,Gem.id, 1140300,1152000,45000,Peg.id, 340620,374400,43200,Gem.id, 985500,1018800,43200,Her.id,
    1127280,1136700,42600,Del.id, 1136700,1140300,42600,Peg.id, 621900,640800,39600,Leo.id, 337080,340620,36000,Ori.id, 374400,378000,36000,Gem.id, 421620,427980,36000,Cnc.id, 1287000,1296000,36000,Peg.id, 90000,177300,35700,Ari.id, 1087680,1096200,30600,Del.id, 729000,814500,28800,Boo.id,
    1228500,1287000,27000,Peg.id, 427980,499500,25200,Cnc.id, 499500,580500,25200,Leo.id, 985500,1007760,22500,Oph.id, 1007760,1018800,22500,Aql.id, 1125000,1127280,21600,Del.id, 378000,378900,19800,CMi.id, 985500,994980,16200,Ser.id, 868500,904500,14400,Her.id, 985500,994980,10800,Oph.id,
    1159200,1170000,9900,Peg.id, 0,108000,7200,Psc.id, 1003500,1018800,7200,Ser.id, 1096200,1125000,7200,Del.id, 1125000,1152000,7200,Equ.id, 1152000,1159200,7200,Peg.id, 1188000,1228500,7200,Peg.id, 1170000,1188000,6300,Peg.id, 378900,388800,5400,CMi.id, 193500,249300,0,Tau.id,
    249300,252000,0,Ori.id, 388800,436500,0,CMi.id, 792000,814500,0,Vir.id, 963000,985500,0,Oph.id, 143100,177300,-6300,Cet.id, 177300,193500,-6300,Tau.id, 814500,878400,-11700,Ser.id, 252000,274500,-14400,Ori.id, 315000,337080,-14400,Ori.id, 963000,970200,-14400,Ser.id,
    985500,1003500,-14400,Ser.id, 1003500,1018800,-14400,Aql.id, 1228500,1287000,-14400,Psc.id, 580500,621900,-21600,Leo.id, 621900,639000,-21600,Vir.id, 0,18000,-25200,Psc.id, 1287000,1296000,-25200,Psc.id, 769500,792000,-28800,Vir.id, 859500,878400,-28800,Oph.id, 1080000,1108800,-32400,Aql.id,
    1152000,1180800,-32400,Aqr.id, 927000,970200,-36000,Oph.id, 315000,436500,-39600,Mon.id, 265500,274500,-39600,Eri.id, 274500,315000,-39600,Ori.id, 436500,451800,-39600,Hya.id, 517500,580500,-39600,Sex.id, 639000,693000,-39600,Vir.id, 949500,954000,-42000,Oph.id, 1018800,1080000,-43320,Aql.id,
    261000,265500,-52200,Eri.id, 1108800,1152000,-54000,Aqr.id, 927000,985500,-57600,Ser.id, 985500,1018800,-57600,Sct.id, 451800,463500,-61200,Hya.id, 878400,884280,-65700,Oph.id, 463500,490500,-68400,Hya.id, 580500,585000,-68400,Crt.id, 878400,884280,-69300,Sco.id, 846000,859500,-72000,Lib.id,
    679500,693000,-79200,Crv.id, 693000,769500,-79200,Vir.id, 490500,526500,-86400,Hya.id, 90000,143100,-87780,Cet.id, 143100,202500,-87780,Eri.id, 585000,639000,-88200,Crt.id, 639000,679500,-88200,Crv.id, 769500,805500,-88200,Lib.id, 878400,904500,-88500,Oph.id, 0,90000,-91800,Cet.id,
    1152000,1180800,-91800,Cap.id, 1180800,1287000,-91800,Aqr.id, 1287000,1296000,-91800,Cet.id, 526500,553500,-95400,Hya.id, 253800,261000,-98100,Eri.id, 261000,330300,-98100,Lep.id, 1080000,1152000,-100800,Cap.id, 553500,571500,-105000,Hya.id, 679500,805500,-106200,Hya.id, 805500,846000,-106200,Lib.id,
    846000,864000,-106200,Sco.id, 247500,253800,-108000,Eri.id, 904500,950400,-108000,Oph.id, 950400,963000,-108000,Sgr.id, 571500,585000,-112200,Hya.id, 330300,397800,-118800,CMa.id, 661500,679500,-118800,Hya.id, 585000,661500,-126000,Hya.id, 189000,202500,-129600,For.id, 451800,505800,-132300,Pyx.id,
    230400,247500,-133200,Eri.id, 963000,1035000,-133200,Sgr.id, 1152000,1242000,-133200,PsA.id, 1242000,1260000,-133200,Scl.id, 162000,189000,-142500,For.id, 505800,594000,-143100,Ant.id, 0,90000,-144000,Scl.id, 90000,162000,-144000,For.id, 208800,230400,-144000,Eri.id, 1260000,1296000,-144000,Scl.id,
    765000,805500,-151200,Cen.id, 846000,864000,-151200,Lup.id, 864000,886740,-151200,Sco.id, 261000,270000,-154800,Cae.id, 270000,355500,-154800,Col.id, 432000,451800,-154800,Pup.id, 184500,208800,-158400,Eri.id, 886740,963000,-163800,Sco.id, 963000,1035000,-163800,CrA.id, 1035000,1098000,-163800,Sgr.id,
    1098000,1152000,-163800,Mic.id, 162000,184500,-165600,Eri.id, 243000,261000,-167400,Cae.id, 828000,846000,-172800,Lup.id, 0,126000,-173400,Phe.id, 144000,162000,-176400,Eri.id, 220500,230400,-176400,Hor.id, 230400,243000,-176400,Cae.id, 1152000,1188000,-180000,Gru.id, 324000,432000,-182700,Pup.id,
    432000,441000,-182700,Vel.id, 130500,144000,-183600,Eri.id, 207000,220500,-183600,Hor.id, 0,99000,-185400,Phe.id, 324000,333000,-189000,Car.id, 441000,456300,-190800,Vel.id, 189000,207000,-191400,Hor.id, 207000,216000,-191400,Dor.id, 0,85500,-192600,Phe.id, 117000,130500,-194400,Eri.id,
    243000,270000,-194400,Pic.id, 812700,828000,-194400,Lup.id, 456300,477000,-196200,Vel.id, 333000,351000,-198000,Car.id, 639000,693000,-198000,Cen.id, 765000,812700,-198000,Lup.id, 812700,828000,-198000,Nor.id, 216000,234000,-203400,Dor.id, 477000,594000,-203400,Vel.id, 594000,607500,-203400,Cen.id,
    945000,972000,-205200,Ara.id, 972000,1098000,-205200,Tel.id, 1188000,1260000,-205200,Gru.id, 172800,189000,-207000,Hor.id, 270000,297000,-207000,Pic.id, 351000,369000,-208800,Car.id, 0,72000,-210600,Phe.id, 72000,117000,-210600,Eri.id, 1260000,1296000,-210600,Phe.id, 234000,247500,-212400,Dor.id,
    828000,886740,-216000,Nor.id, 1098000,1152000,-216000,Ind.id, 297000,324000,-219600,Pic.id, 819000,828000,-219600,Cir.id, 886740,895500,-219600,Ara.id, 805500,819000,-228900,Cir.id, 895500,904500,-228900,Ara.id, 324000,369000,-230400,Pic.id, 369000,487800,-230400,Car.id, 607500,639000,-230400,Cen.id,
    639000,693000,-230400,Cru.id, 693000,784800,-230400,Cen.id, 729000,738000,-234000,Cir.id, 904500,909000,-234000,Ara.id, 117000,172800,-243000,Hor.id, 172800,247500,-243000,Ret.id, 796500,805500,-243000,Cir.id, 909000,945000,-243000,Ara.id, 945000,972000,-243000,Pav.id, 1188000,1260000,-243000,Tuc.id,
    247500,355500,-252000,Dor.id, 738000,796500,-252000,Cir.id, 796500,918000,-252000,TrA.id, 0,72000,-270000,Tuc.id, 189000,247500,-270000,Hyi.id, 355500,487800,-270000,Vol.id, 487800,607500,-270000,Car.id, 607500,738000,-270000,Mus.id, 972000,1152000,-270000,Pav.id, 1152000,1260000,-270000,Ind.id,
    1260000,1296000,-270000,Tuc.id, 40500,72000,-273600,Tuc.id, 0,189000,-297000,Hyi.id, 414000,738000,-297000,Cha.id, 738000,972000,-297000,Aps.id, 189000,414000,-306000,Men.id, 0,1296000,-324000,Oct.id
  );


}
