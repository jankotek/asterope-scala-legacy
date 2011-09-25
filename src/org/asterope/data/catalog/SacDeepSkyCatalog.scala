package org.asterope.data.catalog

import org.asterope.data._
import org.asterope.util._
import java.net.URL
import collection.mutable.ListBuffer

class SacDeepSkyCatalog extends DataRowTranslateCatalog[DeepSky]{

    val urls: List[URL] = List(
          localFileURL("data/sac_deepsky/SAC_DeepSky_Ver81_Fence.txt")
	        //TODO this file is in zip on web, we should be able to read such cases
    )

    val catalogSize = 10344;

    /* format definition*/
    override val dataFormat = SimpleDelimiterLineParser(
		  delimiter = "\\|",
		  colNames = List("XX","OBJECT","OTHER","TYPE","CON","RA","DEC","MAG",
		 		  "SUBR","U2K","TI","SIZE_MAX","SIZE_MIN","PA","CLASS","NSTS",
		 		  "BRSTR","BCHM","NGC DESCR","NOTES"))

     def translate(line: DataRow): Option[DeepSky] = {
       if(line.getString("OBJECT").get.trim=="OBJECT")
         return None //ignore first line
       val ra2 = Angle.parseRa(line.getString("RA").get)
       val de2 = Angle.parseDe(line.getString("DEC").get)
       val dstype2 = DeepSkyType.withName(line.getString("TYPE").get.replaceAll("\\+","_"))


       def getSize(name:String):Option[Angle] = {
        line.getString(name).map{sizeMax=>
		      var sm = BigDecimal(sizeMax.substring(0,sizeMax.length-1).trim);
		      if(sizeMax(sizeMax.length-1) == 's')
			     sm = sm*BigDecimal(Angle.S2Uas)
		      else
			      sm = sm*BigDecimal(Angle.M2Uas)
		      new Angle(sm.toLongExact)
        }
       }

       val sizeMax2 = getSize("SIZE_MAX")
       val sizeMin2 = getSize("SIZE_MIN")
       val pa2:Option[Angle] = line.getInt("PA").map{_.degree}

	//TODO There are many objects which have no published magnitude,
	//they have been assigned a magnitude of 99.9.
	//Dark nebulae obviously have no magnitude, so we assigned them a mag of 79.9 to differentiate
	//them from objects with no magnitude given.
	//The reason we chose large values for objects without magnitudes is that a sort would find 0.0 or "" as a large value of brightness.
       val mag2:Option[Magnitude] = line.getDouble("MAG").filter(_<50).map(new Magnitude(_))

       val names2 = new ListBuffer[Nomenclature]
       line.getString("OBJECT")
          .map(_.trim)
          .map(preParseOtherName(_))
          .filter(_!="")
          .foreach(names2+=Nomenclature.parse(_))


       for(s <-line.getString("OTHER");
         s2 <- Nomenclature.split(preParseOtherName(s));
         n = s2.trim;
         if(n!="")){
            names2 += Nomenclature.parse(n)
         }


       return Some(new DeepSky(ra=ra2,de=de2, deepSkyType=dstype2, sizeMax=sizeMax2, sizeMin=sizeMin2,
          posAngle=pa2, mag = mag2,names = names2.toList))
     }


  private def preParseOtherName(s:String) = {
    var s2 = s
    .trim.replace("; ",";")
    .replaceAll("[ ]+", " ")

    .replaceAll("[A-Za-z ]+ Dwarf","") //TODO object names are skiped now
    .replaceAll("WLM","") //TODO object names are skiped now
    .replaceAll("Coma I","") //TODO object names are skiped now
    .replaceAll("Omega Centauri","") //TODO object names are skiped now
    .replaceAll("Coalsack","") //TODO object names are skiped now
    .replaceAll("Lg Magellanic Cl","") //TODO object names are skiped now
    .replaceAll("Eridanus Cluster","") //TODO object names are skiped now
    .replaceAll("Hercules Galxy Cl","") //TODO object names are skiped now
    .replaceAll("Zwicky's triplet","") //TODO object names are skiped now
    .replaceAll("Lg Magellanic Cl","") //TODO object names are skiped now
    .replaceAll("Hydra (A|I)","") //TODO object names are skiped now
    .replaceAll("Leo (III|II|I)","") //TODO object names are skiped now
    .replaceAll("Small Magellanc Cl","") //TODO object names are skiped now
    .replaceAll("Dark Doodad","") //TODO object names are skiped now
    .replaceAll("Wild's triplet","") //TODO object names are skiped now
    .replaceAll("Perseus I","") //TODO object names are skiped now
    .replaceAll("Sextans A","") //TODO object names are skiped now
    .replaceAll("Sextans B dwarf","") //TODO object names are skiped now
    .replaceAll("Hyades","") //TODO object names are skiped now
    .replaceAll("Ursa Major I","") //TODO object names are skiped now

    .replaceAll("47 Tucanae","47 Tuc")

    s2 = s2.replaceAll("-[ ]+","-")
    .replaceAll("\\.[ ]+",".")
    .replaceAll("[+]{1}[ ]+","+")
    .replaceAll("V V","VV")
    .replaceAll("([0-9]+)a$","$1A")
    .replaceAll("([0-9]+)a;","$1A;")
    .replaceAll("([0-9]+)b$","$1B")
    .replaceAll("([0-9]+)b;","$1B;")
    .replaceAll("([0-9]+)c$","$1C")
    .replaceAll("([0-9]+)c;","$1C;")
    .replaceAll("([0-9]+)e$","$1E")
    .replaceAll("([0-9]+)e;","$1E;")
    .replaceAll("([0-9]+)h$","$1H")
    .replaceAll("([0-9]+)h;","$1H;")
    .replaceAll("([0-9]+)o$","$1O")
    .replaceAll("([0-9]+)o;","$1O;")
    .replaceAll("([0-9]+)p$","$1P")
    .replaceAll("([0-9]+)p;","$1P;")


    s2 = s2.replaceAll("He1","Hen 1")
    .replaceAll("He2","Hen 2")
    .replaceAll("He3","Hen 3")
    .replaceAll(";He ",";Hen ")
    .replaceAll("^He ","Hen ")
    .replaceAll("BV 5-","BV ")
    .replaceAll("Mayall ","My ")
    .replaceAll("MCG([+0-9]+)","MCG $1")
    .replaceAll("MCG ([0-9]+)","MCG +$1")
    .replaceAll("M[1-4]+-[0-9]+","")           //unknown catalog M1 / M4
    .replaceAll("Arp ","APG ")
    .replaceAll("Abell ","ACO ")
    .replaceAll("Berk ","Berkley ")
    .replaceAll("^B ","Barnard ")
    .replaceAll("SH2-","Sh 2-")
    .replaceAll("Sh2-","Sh 2-")
    .replaceAll("Sh1-","Sh 1-")
    .replaceAll("Nassau ","Na ")
    .replaceAll("Peimbert 1-1","Pe 1-1") //this object have wrong catalog, by Simbad it is Perek
    .replaceAll("Peimbert ","PB ")
    .replaceAll("Longmore ","Lo ")
    .replaceAll("Merrill ","Me ")
    .replaceAll("IRAS","IRAS ")
    .replaceAll("^Ap [0-9]+$","") //unknown catalog
    .replaceAll("^AM [0-9]+-[0-9]+$","") //unknown catalog
    .replaceAll("^Cannon [0-9]+-[0-9]+$","") //unknown catalog
    .replaceAll("F703","Fath 703") //unknown catalog

    s2 = s2.replaceAll(";Ter ",";Terzan ")
    .replaceAll("^Ter ","Terzan ")

    .replaceAll(";The ",";Th ")
    .replaceAll("^The ","Th ")
    .replaceAll("KCPG ","KPG ")
    .replaceAll("Jones ","Jn ")


    .replaceAll("^Ced ","Cederblad ")
    .replaceAll(";Ced ",";Cederblad ")
    .replaceAll("^Shapley ","Sp ")
    .replaceAll("^vdBHa ","VdBH ")
    .replaceAll("^vdB-Ha ","VdBH ")
    .replaceAll("^vdBH ","VdBH ")
    .replaceAll("^CRL ","RAFGL ")
    .replaceAll(";CRL ",";RAFGL ")
    .replaceAll("^vdB ","VdB ")
    .replaceAll("Vd1-1","Vd 1")

    .replaceAll("K 1-1","")

    s2=s2.replaceAll(";vdB ","VdB ")
    .replaceAll("^3C([0-9]+)","3C $1")
    .replaceAll(";3C([0-9]+)",";3C $1")
    .replaceAll("H IV [0-9]+","") //unknown cat
    .replaceAll("H II [0-9]+","") //unknown cat
    .replaceAll("H III [0-9]+","") //unknown cat
    .replaceAll("H I [0-9]+","") //unknown cat
    .replaceAll("Hoffleit ","Hf ")
    .replaceAll("Shane ","Sn ")
    .replaceAll("Djorgovski ","Djorg ")

    .replaceAll("Apriamasvili ","Apriamaswili ")

    .replaceAll("Tr ","Trumpler ")
    .replaceAll("^H1-","H 1-")
    .replaceAll("IC 3319-20","IC 3319")


    s2=s2
    .replaceAll("H2-","Haro 2-")
    .replaceAll("Haro 1-","H 1-")
    .replaceAll("Haro 2-","H 2-")
    .replaceAll("Haro 3-","H 3-")
    .replaceAll("Perek ","Pe ")
    .replaceAll("Markarian ","Mrk ")
    .replaceAll("Hubble ","Hb ")
    .replaceAll("Hart-Triton ","HaTr ")
    .replaceAll("Ho [0-9]+[a-z]{1}","") //unknown cat
    .replace("0538+498","")    //clearly fault
    .replace("1641+399","")    //clearly fault
    .replace("1100+772","")    //clearly fault
    .replaceAll("^ZwG ([0-9]+) ([0-9]+)","ZwG $1.$2")
    .replaceAll("Hu [0-9]+-[0-9]+","")    //does not matter
    .replace("SG 1 4","")    //does not matter
    .replaceAll("New [0-9]+","")    //does not matter
    .replaceAll("III Zw [0-9]+","")    //does not matter
    .replaceAll("II Zw [0-9]+","")    //does not matter
    .replaceAll("IV Zw [0-9]+","")    //does not matter
    .replaceAll("I Zw [0-9]+","")    //does not matter
    .replace("Le Gentil","LeGentil") //no space
    .replace("UGC 124 72","")
    .replace("R 80","")
    .replace("SG 3.148","")
    .replace("2SZ 37","SZ II 37")

    s2=s2.replace("Keeler 690","")
    .replace("SG 3.202","")
    .replace("DRCG 10-7","")
    .replace("DHW3","DeHt 3")
    .replaceAll("^J ","Jonckheere ")
    .replaceAll(";J ",";Jonckheere ")
    .replaceAll("Sanduleak ","San ")
    .replaceAll("Menzel ","Mz ")
    .replaceAll("CGCG ","Z ")
    .replaceAll("Mkn 79","Mrk 79")
    .replaceAll("RCW1","RCW 1")

    .replace(" B4201",";GC 4201")
    .replaceAll("Baade ([0-9]+)","Ba $1")
    .replace("0134+329","")
    .replace("Velghe 26","Ve 7-26")

    .replaceAll("NGC ([0-9]+)-[0-9]+","") //already contains other name with 'A/B' components
    .replaceAll("K([0-9]+)-[0-9]+","")  //hard to parse in simbad
      //ESO may contain info about object type.
      //this is not necessary for identification (for example Simbad does not use it)
      //strip it out
    .replaceAll("ESO ([0-9]+)-([A-Z]+)([0-9]+)","ESO $1-$3")
     if(s2 == "P.1") throw new Error(s)
      s2
  }


}