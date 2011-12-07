package org.asterope

import org.asterope.chart._
import org.asterope.data._
import org.asterope.gui._
import jdbm.{RecordManagerFactory, RecordManagerOptions, RecordManager}
import org.asterope.util._

/**
 * Provides primitive Dependency Injection framework.
 */

class Beans{

  /** is called just before shutdown starts */
  val onShutdown = new Publisher[Unit]();


  /****************************************************************************************
   * Data beans
   ****************************************************************************************/
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
    onShutdown.listen{e=>recman.close()}
    recman
  }

  lazy val liteStarDao = new LiteStarDao(recman)
  lazy val deepSkyDao = new DeepSkyDao(recman)
  lazy val milkyWayDao = new MilkyWayDao(recman)
  lazy val constelLineDao = new ConstelLineDao(recman)
  lazy val constelBoundaryDao = new ConstelBoundaryDao(recman)
  lazy val catalogDao = new CatalogDao(recman)
  lazy val nameResolver = new NameResolver(deepSkyDao,liteStarDao)


  /****************************************************************************************
   * Chart beans
   ****************************************************************************************/
  lazy val stars = new Stars(liteStarDao)
  lazy val deepSky = new DeepSkyPainter(deepSkyDao)
  lazy val milkyWay = new ChartMilkyWay(milkyWayDao)
  lazy val constelLine = new ChartConstelLine(constelLineDao)
  lazy val constelBoundary = new ChartConstelBoundary(constelBoundaryDao)
  lazy val legendBorder = new LegendBorder(stars,deepSky)


  /****************************************************************************************
   * GUI related beans
   ****************************************************************************************/
  lazy val resmap = new ResourceMap(classOf[MainWindow]);

  lazy val mainWin = new MainWindow(resmap)
  lazy val mainWinActions = new MainWindowActions(resmap, mainWin, nameResolver, chartEditorFab)
  lazy val mainWinMenu = new MainWindowMenu(mainWinActions,mainWin)

  lazy val welcomeEditor = new WelcomeEditor(mainWinActions);

  lazy val chartEditorFab:()=>ChartEditor = ()=> {
    new ChartEditor(mainWinActions,resmap,stars,deepSky, constelBoundary, constelLine, legendBorder,milkyWay)
  }





}
