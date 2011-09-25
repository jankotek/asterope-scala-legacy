package org.asterope.util

import java.io._
import java.net._
import org.apache.commons.compress.compressors.bzip2._
import java.util.zip._

/**
 * Downloads content from internet and caches it in local folder
 */
object GetURL {

  private var urlOverride = Map[URL, URL]()
  
  /** Folder with cached files */
  //TODO hardcoded cacheFolder
  val cacheFolder = new File("netcache");

  //load URL overrides from config folder
  //TODO hardcoded config folder
  val urlOverrideFile = new File("profile/url_override.properties")


  //load URL overrides from config file
  if(urlOverrideFile.getParentFile.exists()){
    if(urlOverrideFile.exists()){
      val props = IOUtil.loadPropsFromFile(urlOverrideFile)
      loadUrlOverrides(props)
    }else{
      //config file does not exists, create sample one
      val urlOverrideSampleContent = """#
# In this file you can override remote location with local folder.
# So you can download remote catalogs and store them locally.
#
# Local folders use syntax from URLs, it must be prefixed with 'file:///'
# Also each pair (_remote,_local) should have unique name.
# An example:

cat1_remote = ftp://example.com/UCAC3/
cat1_local = file:///c:/UCAC3

cat2_remote = ftp://example.com/XMPCC/
cat2_local = file:///XMPCC"""
      IOUtil.writeToFile(urlOverrideFile,urlOverrideSampleContent)
    }

  }

  /** Load URL overrides from properties file file */
  def loadUrlOverrides(p:Map[String, String]){
    for(
      remoteKey <-p.keys.filter(_.endsWith("_remote"));
      localKey = remoteKey.replaceAll("_remote$","_local");
      remoteURL = new URL(p(remoteKey));
      localURL = new URL(p(localKey))
      ){
      addFolderOverride(remoteURL,localURL)
    }
  }


  /**
   * Remote URLs can be downloaded by user and stored locally.
   * This methods overrides remote location with other (preferably local and faster).
   *
   * @param prefix of remote url
   * @param localFolder folder on local machine (or local network)
   */
  def addFolderOverride(prefix:URL, localFolder:URL){
    //make sure it has protocol prefix
    urlOverride += prefix -> localFolder;
  }
    
  /**Converts URL to cached file */   
  def urlToCachedFile(url:URL):File ={
    //remove protocol
    var s = url.getHost + "/"+url.getFile()
    if(s.endsWith("/")) s+="INDEX"
    //filter out characters which may cause problem on filesystem
    val illegalFileChars = Array(':','*','?','"','>','<','|')
    illegalFileChars.foreach{c=>
      s = s.replace(c,'#')
    }
    
    new File(cacheFolder,s)
  }
  
  /**
   * Downloads file from Internet, saves it to cache and returns InputStream
   * File is not downloaded if it already exists in cache 
   */
  def apply(url2:URL, decompress:Boolean = false):InputStream = {

    //check if remote URL is overriden with local folder
    val url:URL = {
      val s = url2.toString
      //TODO sort urlOverrides so longest gets applyed first
      urlOverride.keys.find(k=>s.startsWith(k.toString)).map{key=>
        //replace prefix with value
        s.replace(key.toString,urlOverride(key).toString)
      }.map(new URL(_)).getOrElse(url2)
    }

    val bzip2Decompress = decompress && url.toString.endsWith(".bz2")
    val gzipDecompress = decompress && url.toString.endsWith(".gz")

    if(url.getProtocol == "file"){
      val f = new File(url.getFile)
      var in:InputStream = new BufferedInputStream(new FileInputStream(f));
      //decompress file if needed
      if(url.toString.endsWith(".bz2")) in = new BZip2CompressorInputStream(in)
      if(url.toString.endsWith(".gz")) in = new GZIPInputStream(in)
      return in
    }

    var f = urlToCachedFile(url)
    if(bzip2Decompress)
      f = new File(f.getPath.replaceAll("\\.bz2$",""))
    if(gzipDecompress)
      f = new File(f.getPath.replaceAll("\\.gz$",""))
    

    
    if(!f.exists){
    	Log.debug("GET: "+url)
    	//download to file under temp name, so concurrent downloads do not collide
    	val f2 = new File(f.getPath+"_TEMP_"+math.random)
    	
    	try{
    		val con = url.openConnection();
    		//set timeout, this has to be done before InputStream is open 
    		//and connection is established     		
    		con.setReadTimeout(15000);
    		con.setConnectTimeout(15000);
    		var in:InputStream = new BufferedInputStream(con.getInputStream);
    		//decompress file if needed
    		if(bzip2Decompress)
    		  in = new BZip2CompressorInputStream(in)
    		if(gzipDecompress)
    		  in = new GZIPInputStream(in)    		

    		//create parent folders
    		f2.getParentFile.mkdirs()
    		val out = new BufferedOutputStream(new java.io.FileOutputStream(f2));

    		IOUtil.copy(in,out,32768)
    		
    		in.close();
    		out.close();
    		
    		//rename tmp f2 to f
    		f2.renameTo(f)
    		
    	}finally{
    	  //clean up tmp file
    	  if(f2.exists && !f2.delete)
    	    f2.deleteOnExit()
    	}
    }
    
    val out = new BufferedInputStream(new FileInputStream(f))

    //decompress if needed
    if(f.getPath.endsWith(".gz"))
      new GZIPInputStream(out){
    	override def skip(i:Long):Long = {
    		Log.warning("skip() on GZIPInputStream is unefective. Use GetURL(url,decompress=true)")
    		super.skip(i)
    	}      
      }
    else if(f.getPath.endsWith(".bz2"))
      new BZip2CompressorInputStream(out){
    	override def skip(i:Long):Long = {
    		Log.warning("skip() on BZip2CompressorInputStream is unefective. Use GetURL(url,decompress=true)")
    		super.skip(i)
    	}
      }
    else 
    	out
    
   
  }
  
  
  

}