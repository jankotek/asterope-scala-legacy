package org.asterope.util

import java.io._
import java.util.Properties

/**
 * Various IO related utils.
 * 
 * This object is dumping place for code which was not important enough
 * to make it into `org.asterope.util` package objects. 
 * 
 * I won't  include IO Commons into Asterope, so some of this code is duplicate.
 */
object IOUtil {
  
  /** Copy from one stream to other */   
  def copy(in:InputStream, out:OutputStream, bufferSize:Int = 1024 * 4){

	  val buffer = new Array[Byte](bufferSize);
	  var len = in.read(buffer)
      while ( len > 0) {    			
    	out.write(buffer, 0, len);
    	len = in.read(buffer)
      }

  }
  
  /** read file into string */
  def loadToString(in:InputStream):String = {
    val buf = new ByteArrayOutputStream()
    copy(in,buf)
    new String(buf.toByteArray)    
  }
  
  /** read file into String */
  def loadToString(f:File): String = loadToString(new FileInputStream(f))

  /**
   * Load java properties file into Scala map
   */
  def loadPropsFromFile(f:File):Map[String, String] = {
    val props = new Properties()
    props.load(new FileInputStream(f))
    import collection.JavaConversions._
    props.keys().map(k=> k.asInstanceOf[String] -> props.get(k).asInstanceOf[String]).toMap
  }


  def writeToFile(f:File, content:String){
    val in = new FileWriter(f)
    in.write(content)
    in.close()
  }

  
  def bitsEncode(v:List[Boolean]):Byte = {
      assert(v.size<=8)
      var v2 = v.reverse;
      while(v2.size<8) v2 ::= false;
      var b = 0;
      v2.foreach{vv=>
        b = (b<<1) + (if(vv) 1 else 0)
      }
      b.toByte
  }


  def bitsDecode(b:Byte):List[Boolean] = {
      var b2 = b.toInt;
      (0 until 8).map{s=>
        val r = b2%2==1
        b2 = b2>>1;
        r
      }.toList
  }
  
}