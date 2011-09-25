package org.asterope.data.catalog

/*
 *  Copyright Jan Kotek 2009, http://asterope.org
 *  This program is distributed under GPL Version 3.0 in the hope that
 *  it will be useful, but WITHOUT ANY WARRANTY.
 */

import java.io._
import java.net.URL
import io.Source
import org.asterope.util.GetURL

/**
 * Handles catalogs with various URLs in folders
 * <p>
 * Files from internet are auto downloaded on first use.
 * Everything is cached with {@link org.asterope.util.HttpGet}
 *
 */
trait UrlData {

  /** abstract, @return folders in which files are located */
  def urls:List[URL];

  def localFileURL(fileName:String):URL = new URL(new File(".").toURI.toURL, fileName)

  /**
   * Opens file as stream
   * @param file simple filename in folder
   * @return DataInputStream from file in folder
   */
  def openStream(file:String):DataInputStream = {

    var lastException:Exception = new IOException();
    //try to open all urls
    for(url2 <- urls)
      try{
        val url = makeChild(url2,file);

        return new DataInputStream(GetURL.apply(url))
      }catch{
        //failed, move to next
        case e:IOException => lastException = e;
      }
    //no url found, rethrow last exception
    throw new IOException("Can not open. File: "+file+", \n URLs:"+urls,lastException);
  }


  /**
   * Opens file as Reader
   * @param file simple file name in folder
   * @return BufferedReader from file in folder
   */
  def openReader(file:String):BufferedReader =  new BufferedReader( new InputStreamReader(openStream(file)))



  /**
   * Open files and reads it line by line
   * @return iterator over lines in given file */
  def readLines(file:String):Iterator[String] = Source.fromInputStream(openStream).getLines

  /** utility method create child from given url */
  protected def makeChild(url:URL, file:String):URL = {
	if (file == null || file == "") return url;
    if(url.toString.endsWith("/")) new URL(url.toString + file)
    else new URL(url.toString+"/"+file);
  }

  /**
   * Open URL as Stream
   * @return DataInputStream directly from one of URLs */
  def openStream():DataInputStream = openStream("")

  /**
   * Open URL and return it as Reader
   * @return BufferedReader directly from one URLs */
  def openReader():BufferedReader =  openReader("")

  /**
   * Open URL and read its lines
   * @return iterator over lines in one of URLs */
  def readLines():Iterator[String] = readLines("")


}
