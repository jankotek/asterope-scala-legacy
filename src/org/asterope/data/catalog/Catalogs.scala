/*
 *  Copyright Jan Kotek 2009, http://asterope.org
 *  This program is distributed under GPL Version 3.0 in the hope that
 *  it will be useful, but WITHOUT ANY WARRANTY.
 */

package org.asterope.data.catalog

import java.io._
import org.asterope.util._
import org.asterope.data._
import scala.collection.JavaConversions._


/**
 * Catalog is general way to import data into Asterope.
 * Data importers should implement this interface
 */
trait Catalog[E]{

    /**
     * Get all items from catalog.
     * <p>
     * @return iterator over all object in catalog
     */
    def  queryForAll():Iterator[E];

    /**
     * Get approximately size of catalog.
     * This value does not have to be exact, just for quick orientation.
     * !!! DO NOT load all object to count them !!!
     * <p>
     * @return approximatly number of items in catalog, or -1 if size is unknown
     */
    def catalogSize:Int;

}



/**
 * Catalog which downloads given URL, parse each lines to colums and convert it to object.
 * Usefull to eliminate boiler plate code.  For sample usage see {@link org.asterope.data.catalog.other.PP3MilkyWayCatalog}
 * @see LineTranslateCatalog
 */
trait DataRowTranslateCatalog[E] extends Catalog[E] with UrlData{

  override def queryForAll():Iterator[E] = {
		for(
		    line<-readLines;
		    parsed <- dataFormat.parseLine(line);		    
		    e<-translate(parsed)
		   ) yield e
  }

  /** convert DataRow to object returned in iterator. @return object or null if value is not defined  */
  def translate(line:DataRow):Option[E];

  /**
   * Line translator which converts string line into DataRow
   * @return line translator
   */
  def dataFormat: LineParser;

}



/**
 * Catalog which downloads given URL and convert each line to object.
 *
 * @see DataRowTranslateCatalog
 */
trait LineTranslateCatalog[E] extends Catalog[E] with UrlData{

  override def queryForAll():Iterator[E] = {
     return translateLines((line: String)=>{
       translateLine(line):Option[E]
     })
   }

  def translateLines[F](f: (String) =>Option[F]): Iterator[F]={
	for(
	    line<-readLines;
	    obj <- f(line)
		)yield obj
  }

  /** translate given line to object. @return object for given line or null can not be parsed */
  def translateLine(line: String):Option[E];

}

trait VizierTsvCatalog[E] extends Catalog[E] with UrlData{
  override def queryForAll():Iterator[E] = {
	val reader = openReader();
	val parser = DataParserAdc.vizierTsvLineParser(reader)

	for(
	    line<-readLines;
	    parsed <- parser.parseLine(line);		    
	    e<-translate(parsed)
	 ) yield e
  }

  /** convert DataRow to object returned in iterator. @return object or null if value is not defined  */
  def translate(line:DataRow):Option[E];

}
