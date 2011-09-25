package org.asterope.data

import org.asterope.util._
import jdbm._

/**
 * Defines basic data beans
 */

trait DataBeans extends Beans{

  val DB_PATH = "profile/db/db";

  lazy val recman:RecordManager = {
    //make sure folder exists
    val parentFolder = new java.io.File(DB_PATH).getParentFile();
    parentFolder.mkdirs();
    //init
    Log.info("Creating record manager at location "+parentFolder.getAbsolutePath );
    val props = new java.util.Properties();
    //set this property in batch import mode
    props.put(RecordManagerOptions.APPEND_TO_END, System.getProperty(RecordManagerOptions.APPEND_TO_END,"false"))
    val recman = RecordManagerFactory.createRecordManager(DB_PATH,props)

    //add shutdown code
    onShutdown{recman.close()}
    recman
  }


  lazy val liteStarDao = new LiteStarDao(recman)
  lazy val deepSkyDao = new DeepSkyDao(recman)
  lazy val milkyWayDao = new MilkyWayDao(recman)
  lazy val constelLineDao = new ConstelLineDao(recman)
  lazy val constelBoundaryDao = new ConstelBoundaryDao(recman)
  lazy val catalogDao = new CatalogDao(recman)
  lazy val nameResolver = new NameResolver(deepSkyDao,liteStarDao)

}