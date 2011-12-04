package org.asterope.data

import org.asterope.data.catalog._
import org.asterope.util._
import java.io._
import java.util.zip._
import scala.io.Source
import jdbm.RecordManagerOptions
import org.apache.commons.math.geometry.Vector3D
import org.asterope.Beans

/**
 * This script is called from Ant at build time. 
 * It builds database distributed with Asterope from 
 * source files located at 'data' folder.    
 * 
 * @author Jan Kotek
 */
object CompileDb extends Beans with App{

  System.setProperty(RecordManagerOptions.APPEND_TO_END,"true")

	def commit(){
		recman.commit()
		recman.clearCache() //data from previous catalog are not needed and would block memory
	}
	
	/** Tycho2 crossid*/
	val SKY2000_TYC2_FILE = new File("data/tyc2-sky2000.csv.gz");
	/** Hipparcos crossid */
	val SKY2000_HIP_FILE = new File("data/hip-sky2000.csv.gz");
	/** Hipparcos ids which did not pass throw automatic filters. Manually edited*/
	val SKY2000_HIP_CUSTOM_FILE = new File("data/hip-sky2000.custom.csv");

	/** Hipparcos double stars crossid */
	val SKY2000_HIPDOUBLE_FILE = new File("data/hipdouble-sky2000.csv.gz");
	/** Well known star names */
	val STAR_NAMES_FILE = new File("data/star_names.fab");	
	/** Milkyway pixels taken from PP3 */
	val MILKYWAY_FILE = new File("data/milkyway.dat.gz");
	/** Constel art lines taken from Stellarium */
	val CONSTELLATION_LINES_FILE = new File("data/constellationship.fab");

	
	/** open InputStream from gziped file  */
	def gzip(f:File) = new GZIPInputStream(new FileInputStream(f))
	

	
	if(!catalogDao.isCatalogImported("sacDeepSky")){
		println("Importing SAC Deep Sky catalog")
    val sac = new SacDeepSkyCatalog()
    sac.queryForAll.foreach{ds=>
      deepSkyDao.addDeepSky(ds)
    }

		catalogDao.setCatalogImported("sacDeepSky")
		commit()
	}

	if(!catalogDao.isCatalogImported("sky2000")){
		System.out.println("Importing Sky2000 catalog");
		val cat = new Sky2000Catalog()
    cat.queryForAll.foreach{s=>
      liteStarDao.addStar(s)
    }

		catalogDao.setCatalogImported("sky2000")
		commit()

	}

	if(!catalogDao.isCatalogImported("hipCrossid")){
		println("Importing HIP names")
    val hipFormatId = Nomenclature.formatIdByExact("HIP NNNNNN")
		val hipSrc = Source.fromInputStream(gzip(SKY2000_HIP_FILE));		
		val hipSrcCustom = Source.fromFile(SKY2000_HIP_CUSTOM_FILE);
		(hipSrc.getLines ++ hipSrcCustom.getLines)
			.filter(!_.startsWith("#"))
			.foreach{ s=>
				val ss = s.split(",");
				val sky2000 = "SKY2000 "+ss(0).trim
				val hip = Nomenclature.parseWithID("HIP "+ss(1).trim.toInt,hipFormatId);

				liteStarDao.addName(sky2000,hip)
			}
		catalogDao.setCatalogImported("hipCrossid")
		commit()
	}
	
	if(!catalogDao.isCatalogImported("hipDoubleCrossid")){
		println("Importing HIP-Double names")
    val hipFormatId = Nomenclature.formatIdByExact("HIP NNNNNNA")
		val hipSrc = Source.fromInputStream(gzip(SKY2000_HIPDOUBLE_FILE));		
		hipSrc.getLines.filter(!_.startsWith("#")).foreach{ s=>
			val ss = s.split(",");
			val sky2000 = "SKY2000 "+ss(0)
			assert(ss(2).size == 1,"too long component"+ss(2))
			val comp:Char = ss(2)(0)					
			val hip = Nomenclature.parseWithID("HIP "+ss(3).toInt+comp,hipFormatId);
			liteStarDao.addName(sky2000,hip)
		}
		catalogDao.setCatalogImported("hipDoubleCrossid")
		commit()
	}
	
	if(!catalogDao.isCatalogImported("tyc2Crossid")){
		println("Importing TYC2 names")
    val tycFormatId = Nomenclature.formatIdByPrefix("TYC")
		val hipSrc = Source.fromInputStream(gzip(SKY2000_TYC2_FILE));
		var counter = 0;
		hipSrc.getLines.filter(!_.startsWith("#")).foreach{ s=>
			val ss = s.split(",");
			val sky2000 = "SKY2000 "+ss(4)
			val tyc = Nomenclature.parseWithID("TYC "+ss(0).toInt+"-"+ss(1).toInt+"-"+ss(2).toInt,tycFormatId)
			liteStarDao.addName(sky2000,tyc)
			counter+=1
			if(counter%50000==0){
				commit()
				println(" "+counter);
			}
			
		}
		catalogDao.setCatalogImported("tyc2Crossid")
		commit()
	}

	if(!catalogDao.isCatalogImported("formerNamesCrossid")){
		println("Importing former names")
		val hipSrc = Source.fromFile(STAR_NAMES_FILE);		
		hipSrc.getLines.filter(!_.startsWith("#")).foreach{ s=>			
			val ss:Array[String] = s.split("\\|");
			val hip = "HIP "+ss(0).trim
      //name does not have format, so use simple string to represent it 
			val name = Nomenclature.justName(ss(1).trim); 
			liteStarDao.addName(hip,name)
		}
		catalogDao.setCatalogImported("formerNamesCrossid")
		commit()
	}

	if(!catalogDao.isCatalogImported("milkyWayPixel")){
		println("Importing MilkyWay pixels")
		val src = Source.fromInputStream(gzip(MILKYWAY_FILE));
		src.getLines.filter(!_.startsWith("#")).foreach{ s=>
			val ss = s.split(" ")
			val pos = rade2Vector(
					Angle.normalizeRa(ss(0).toDouble * Angle.H2R), Angle.D2R * ss(1).toDouble )
			val gray = ss(2).toInt
			if(gray>10)	 //if gray is too small, dont add
				milkyWayDao.addMilkyWayPixel(new MilkyWayPixel(pos,gray))
		}
		catalogDao.setCatalogImported("milkyWayPixel")
		commit()
	}
	
	if(!catalogDao.isCatalogImported("constellationLine")){
		println("Importing Constel Lines")
		val src = Source.fromFile(CONSTELLATION_LINES_FILE);
		for(
				line <-src.getLines;
				if(!line.startsWith("#") && line.trim!="");
				split = line.replaceAll("[ ]+"," ").split(" ");
				constellation = Constel.withName(split(0));
				lineCount = split(1).toInt;
				i <- 0 until lineCount;
				hip1 = split(2+i*2).toInt;
				hip2 = split(2+i*2+1).toInt
		){		
			def findHip(hip:String):Vector3D = {
				List(hip,hip+"A",hip+"B").foreach{ h=>
					val iter = liteStarDao.objectsByName(h)
					if(iter.hasNext) 
						return iter.next.vector
				}
				throw new Error("ID not found:"+hip)	
			}			

			val v1 = findHip("HIP "+hip1)
			val v2 = findHip("HIP "+hip2)
			val line = new ConstelLine(v1,v2,
					hip1.toInt, hip2, constellation.toString, 1)
			constelLineDao.add(line)

		}
		catalogDao.setCatalogImported("constellationLine")
		commit()
	}

  if(!catalogDao.isCatalogImported("constellationBoundary")){
    println("Importing Constel Boundary Lines")
    CompileDbUtils.readConstelBounds.foreach{
      constelBoundaryDao.add(_)
    }
    catalogDao.setCatalogImported("constellationBoundary")
    commit()    
  }



	println("Defrag")
	recman.defrag()
	println("Done");
  onShutdown.firePublish(Unit)

}
