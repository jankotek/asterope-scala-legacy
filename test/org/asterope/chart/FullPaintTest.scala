package org.asterope.chart

import java.io.File
import org.asterope.util.ScalaTestCase
import org.asterope.data.TestRecordManager

/**
 * Paints some charts into `build/tmp` folder.  
 * 
 * @author Jan Kotek
 */
//TODO one test case left behind after CLI was removed
/*
class FullPaintTest extends ScalaTestCase {




	lazy val cli = new CliChart with TestRecordManager

	lazy val folder = {
		val f = new File("build/tmp");
		f.mkdirs
		f
	}
	
	def testPaintAsteropeInAllFormats{		
		val args = Array("--find","Asterope","--output","");
		def saveTo(ext:String){
			println("extension: "+ext)
			val file = new File(folder,"asterope."+ext);
			if(file.exists) file.delete
			assert(!file.exists)
			args(3) = file.getPath
			cli.call(args)
			assert(file.exists && file.length>10000, "ext failed:"+ext)
		}
		saveTo("png")
		saveTo("svg")
		
	}
	
	def testPaintNorthPole{
		val file = new File(folder,"northPole.png")
		if(file.exists) file.delete
		assert(!file.exists)
		
		val args = Array("--find","Polaris","--output",file.getPath);
		cli.call(args)
		assert(file.exists && file.length>10000)
	}
	
	def testPaintM31{
		val file = new File(folder,"m31.png");
		if(file.exists) file.delete
		assert(!file.exists)
		
		val args = Array("--find","M31","--output",file.getPath);
		cli.call(args)
		assert(file.exists && file.length>10000)
	}
	
	def testPaintGalaxyCenter{
		val file = new File(folder,"galaxyCentre.png")
		if(file.exists) file.delete
		assert(!file.exists)
		
		val args = Array("--find","M7","--output",file.getPath);
		cli.call(args)
		
		assert(file.exists && file.length>10000)
	}


}
 */