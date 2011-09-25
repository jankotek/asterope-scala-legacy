package org.asterope.data.catalog

import java.io._
import org.asterope.data.{Nomenclature, LiteStar}
import org.asterope.util._
import java.net.URL

/**
 * Catalog which reads UCAC3 catalog
 * http://cdsarc.u-strasbg.fr/viz-bin/ftp-index?/ftp/cats/aliases/U/UCAC3
 */
class Ucac3Catalog extends Catalog[LiteStar]{
  
    val baseURL = "http://cdsarc.u-strasbg.fr/ftp/cats/aliases/U/UCAC3/UCAC3/";
        
    val recordSize = 84

    protected val  ucacNomenclatureFormatId = Nomenclature.formatIdByPrefix("UCAC3")
    

  /**
   * This is a text file (ASCII).  Each line contains data for an area
   * of sky (bin) 0.5 deg wide in declination (indexed by zone number from
   * 1 to 360), and 0.1 h wide in RA (indexed from 1 to 240).  So there are
   * a total of 360 * 240 = 86400 lines on this file.
   *
   * !!!! u3index.unf is corrupted in distribution from CDS. Read asc instead
   *
   */
    protected  val index:Array[Int] = {
      val indexIn = GetURL(new URL(baseURL+"/u3index.asc"))
      val lines = io.Source.fromInputStream(indexIn).getLines
      val r = new Array[Int](360*240*2)
      for(i<-0 until 360*240){
        assert(lines.hasNext,"file u3index.unf corrupted ")
        val line = lines.next
        val split = line.trim.split("[ ]+")
        r(i*2) = split(0).toInt;
        r(i*2+1) = split(1).toInt;
      }
//TODO put some integrity check in place
//      //test integrity of index 
//      (0 until 360).foreach{zone=>
//        var offset = 0;
//        (0 until 240).foreach{zoneRa=>
//          val pos = (zone * 240 + zoneRa)*2
//          assert(offset == r(pos))
//          offset+=r(pos+1)
//        }        
//      }
      r
    }




    def queryForAll():Iterator[LiteStar] = {       		
        for(
            zone <- (1 to 360).iterator;
            url = makeURL(zone);
            in = new DataIS(GetURL(url, decompress=true));
            
            zoneStartIndex = (zone-1) * 240;
            raOffset <- 0 until 240;
            starOffset = index(zoneStartIndex);
            numberOfRecordsToRead = index(zoneStartIndex+raOffset+1);
            i <- 0 until numberOfRecordsToRead;
            binary = new Ucac3BinaryEntry(in);
            name = Nomenclature("UCAC3 "+zone+"-"+(i+starOffset+1),ucacNomenclatureFormatId);
            star = parseBinary(binary,name)
        )
          yield star         
    }

    def queryByDisc(center:Vector3d, radius:Angle):Iterator[LiteStar] = {
       val startZone = math.max(0, (center.getDe - radius - 2.degree + 90.degree).toDegree * 2).toInt
       val stopZone = math.min(360, (center.getDe + radius + 2.degree + 90.degree).toDegree * 2).toInt
       for{
          zone <- (startZone until stopZone).iterator;
          url = makeURL(zone);          
          de = -90.degree + (0.5 * zone).degree
          zoneRa <- 0 until 240;
          ra = (0.1 * 15 * zoneRa).degree;
          pos = Vector3d.rade2Vector(ra,de);
          //only continue if zone is near enought to center
          if(center.angle(pos)<radius.toRadian + Angle.D2R * 2);

          //lets dive into file
          indexOffset = (zone * 240 + zoneRa) * 2;
          starOffset = index(indexOffset)
          numberOfRecordsToRead = index(indexOffset +1) ;
          //open resource to read
          in = {
            val in = new DataIS(GetURL(url, decompress=true));
            //skip to position we are interested in
            in.skip(starOffset*recordSize)
            in
          }
          i<- 0 until numberOfRecordsToRead;
          binary = new Ucac3BinaryEntry(in);
          name = Nomenclature("UCAC3 "+zone+"-"+(starOffset+i+1),ucacNomenclatureFormatId);
          star = parseBinary(binary,name)
       }
          yield star         

          
    }
    
    def catalogSize:Int = 100766420;
    
    protected def makeURL(zone:Int):URL = {
    	def addZeros(zone:Int):String = (1000+zone).toString.substring(1);
    	new URL(baseURL+"/z"+addZeros(zone)+".bz2")
    }
    
        

   def parseBinary(b:Ucac3BinaryEntry, ucacId:Nomenclature) = {
       val ra = b.ra.mas       
       val de = b.spd.mas - 90.degree
       val mag = new Magnitude(1e-3 * b.im2)
       var names = List(ucacId)

//  2MASS ID contains RA/DE, but in here is just number, decode it!
//       if(b.id2m!=null)
//        names+=new Nomenclature("2MASS "+b.id2m);
       LiteStar(ra,de,mag,names = names)
       
   }
        
}

class Ucac3BinaryEntry(private var d:DataIS){ 
      import java.lang.Short
   val ra     :Int = d.readIntLE // mas         right ascension at  epoch J2000.0 (ICRS)  (1)
   val spd    :Int = d.readIntLE // mas         south pole distance epoch J2000.0 (ICRS)  (1)
   val im1    :Short= d.readShortLE  // millimag    UCAC fit model magnitude                  (2)
   val im2    :Short = d.readShortLE// millimag    UCAC aperture  magnitude                  (2)
   val sigmag :Short = d.readShortLE// millimag    UCAC error on magnitude (larger of sc.mod)(3)
   val objt   :Byte  = d.readByte //             object type                               (4)   
   val dsf    :Byte  = d.readByte //             double star flag                          (5)   

   val sigra  :Short = d.readShortLE // mas         s.e. at central epoch in RA (*cos Dec)      
   val sigdc  :Short = d.readShortLE // mas         s.e. at central epoch in Dec                 
   val na1    :Byte  = d.readByte //             total # of CCD images of this star
   val nu1    :Byte  = d.readByte //             # of CCD images used for this star        (6)
   val us1    :Byte  = d.readByte //             # catalogs (epochs) used for proper motions
   val cn1    :Byte  = d.readByte//             total numb. catalogs (epochs) initial match
 
   val cepra  :Short = d.readShortLE // 0.01 yr     central epoch for mean RA, minus 1900     
   val cepdc  :Short = d.readShortLE // 0.01 yr     central epoch for mean Dec,minus 1900  
   val pmrac  :Int = d.readIntLE // 0.1 mas/yr  proper motion in RA*cos(Dec)           
   val pmdc   :Int = d.readIntLE// 0.1 mas/yr  proper motion in Dec                    
   val sigpmr :Short = d.readShortLE // 0.1 mas/yr  s.e. of pmRA * cos Dec                   
   val sigpmd :Short = d.readShortLE // 0.1 mas/yr  s.e. of pmDec                            
   
   val id2m   :Int = d.readIntLE //             2MASS pts_key star identifier          
   val jmag   :Short = d.readShortLE  // millimag    2MASS J  magnitude                     
   val hmag   :Short = d.readShortLE // millimag    2MASS H  magnitude                       
   val kmag   :Short = d.readShortLE // millimag    2MASS K_s magnitude                     
   val icqflg0:Byte = d.readByte 
   val icqflg1 :Byte = d.readByte  
   val icqflg2 :Byte = d.readByte
   // * 3         2MASS cc_flg*10 + phot.qual.flag          (7)
   val e2mpho0:Byte  = d.readByte
   val e2mpho1 :Byte = d.readByte 
   val e2mpho2 :Byte  = d.readByte
       // * 3         2MASS error photom. (1/100 mag)           (8)
  
   val smB    :Short = d.readShortLE  // millimag    SuperCosmos Bmag
   val smR2   :Short = d.readShortLE  // millimag    SC R2mag                                  (9)
   val smI    :Short = d.readShortLE  // millimag    SC Imag
   val clbl   :Byte = d.readByte  //             SC star/galaxy classif./quality flag     (10)
   val qfB    :Byte = d.readByte  //             SC quality flag Bmag                     (11)
   val qfR2   :Byte = d.readByte  //             SC quality flag R2mag                    (11)
   val qfI    :Byte = d.readByte  //             SC quality flag Imag                     (11)
  
   val catflg0:Byte = d.readByte  
   val catflg1:Byte = d.readByte 
   val catflg2:Byte = d.readByte 
   val catflg3:Byte = d.readByte 
   val catflg4:Byte = d.readByte 
   val catflg5:Byte = d.readByte 
   val catflg6:Byte = d.readByte 
   val catflg7:Byte = d.readByte 
   val catflg8:Byte = d.readByte 
   val catflg9:Byte = d.readByte 
       // * 10        mmf flag for 10 major catalogs matched   (12)
  
   val g1     :Byte = d.readByte   //             Yale SPM object type (g-flag)            (13)
   val c1     :Byte = d.readByte   //             Yale SPM input cat.  (c-flag)            (14)
   val leda   :Byte = d.readByte   //             LEDA galaxy match flag                   (15)
   val x2m    :Byte = d.readByte   //             2MASS extend.source flag                 (16)
   val rn     :Int = d.readIntLE//             MPOS star number; identifies HPM stars   (17)
   
   d = null;
}




