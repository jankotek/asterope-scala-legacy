package org.asterope.gui

import org.asterope.chart._
import javax.swing._
import org.asterope.util._
import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.event._
import java.awt.event._

import edu.umd.cs.piccolo.util.PBounds



class ChartEditor(
    val beans:ChartBeans
  )
  extends PCanvas
  with ChartEditorActions
  with ChartWindowSkyviewActions
  {

  setPanEventHandler(null)
  setZoomEventHandler(null)
  setBackground(java.awt.Color.black)


    //install zoom handler
    getCamera.addInputEventListener(new PBasicInputEventHandler {
      override def mouseClicked(event: PInputEvent) {
        if (event.isMouseEvent && event.getButton == 2 && event.getClickCount == 1) {
          event.setHandled(true);
          if(!getInteracting) setInteracting(true)

          // if middle button is pressed, center at given location
          val viewPos = event.getPosition;
          val width = chartBase.camera.getViewBounds.getWidth;
          val height = chartBase.camera.getViewBounds.getHeight;
          val bounds = new PBounds(viewPos.getX - width / 2, viewPos.getY - height / 2, width, height);
          chartBase.camera.setViewBounds(bounds);
          refresh()

      }
      }
      override def mouseWheelRotated(event: PInputEvent) {
        if (event.isMouseWheelEvent) {
          event.setHandled(true);
          if(!getInteracting) setInteracting(true)

          // handles zoom event on mouse wheel
          val newScale = 1 + 0.1 * event.getWheelRotation;
          val viewPos = event.getPosition;
          chartBase.camera.scaleViewAboutPoint(newScale, viewPos.getX, viewPos.getY);
          refresh()
          event.setHandled(true)
        }
      }
    })




  /** repaints map with delay, multiple events are merged, so map repaints only once */
  private val refreshWorker = new Worker[ChartBase](delay = 200)

  //refresh when canvas size changes
  addComponentListener(new ComponentAdapter{
    override def componentResized(e:ComponentEvent){
      refresh()
    }
  })
  
  /**
   * Notify when chart refresh starts 
   */
  lazy val onChartRefreshStart = new Publisher[ChartBase]()

  /**
   * Notify when chart refresh finishes 
   */
  lazy val onChartRefreshFinish = new Publisher[ChartBase]()

  /** indicates if refresh is currently in progress */
  def isRefreshInProgress:Boolean = _isRefreshInProgress
  private var _isRefreshInProgress = false;

  private var chartBase = new ChartBase();
  private var coordGridConfig = beans.coordinateGrid.defaultConfig
  private var starsConfig = beans.stars.defaultConfig
  private var showLegend = true
  private var showConstelBounds = true
  private var showConstelLines = true;
  private var deepSkyConfig = beans.deepSky.defaultConfig
  private var aladinConfig:Option[AladinSurvey.Mem] = None


  def getChartBase = chartBase

  def refresh(){
    refreshWorker.run(chartBase)
  }

  refreshWorker.preprocess{chartBase2 =>
    _isRefreshInProgress = true;
    if (getInteracting){
      //if user used mouse to move chart, center on new position and update FOV
      val bounds = chartBase.camera.getViewBounds;
      val center = chartBase.wcs.deproject(bounds.getCenter2D);
      if(center.isDefined){
    	  val fov = chartBase.wcs.deproject(bounds.getOrigin).map(_.angle(center.get) * 2).getOrElse(120 * Angle.D2R);
    	  chartBase = chartBase.copy(position = center.get, fieldOfView = fov.radian)
      }
    }


    chartBase = chartBase.copy(width = getWidth,
      height = if( !showLegend) getHeight else (getHeight - beans.legendBorder.height),
      legendHeight = if(showLegend) beans.legendBorder.height else 0,
      executor = new EDTChartExecutor
    )
    chartBase
  }

  refreshWorker.addTask{chartBase =>
    onChartRefreshStart.firePublish(chartBase)
  }

  refreshWorker.addTask{chartBase =>
      beans.stars.updateChart(chartBase,starsConfig)
  }

  refreshWorker.addTask{chartBase =>
      beans.deepSky.updateChart(chartBase,deepSkyConfig)
  }

  refreshWorker.addTask{chartBase =>
    if(showConstelBounds)
        beans.constelBoundary.updateChart(chartBase)
  }

  refreshWorker.addTask{chartBase =>
    if(showConstelLines)
        beans.constelLine.updateChart(chartBase)
  }

  refreshWorker.addTask{chartBase =>
    aladinConfig.foreach{mem=>
      beans.aladinSurvey.updateChart(chartBase,mem)
    }
  }


  refreshWorker.addTask{chartBase =>
      beans.milkyWay.updateChart(chartBase)
  }

  refreshWorker.addTask{chartBase =>
      beans.coordinateGrid.updateChart(chartBase,coordGridConfig)
  }

  refreshWorker.addTask{chartBase =>
    if(showLegend)
        beans.legendBorder.updateChart(chartBase)
  }

  refreshWorker.onFinished.listenInEDT{chartBase=>
      _isRefreshInProgress = false;
      //labels must be last,
      // placement alghorihm depends on graphic created by other features
      beans.labels.updateChart(chartBase)

      chartBase.executor.asInstanceOf[EDTChartExecutor].plugIntoSwing()
      getCamera.removeAllChildren();
      if(getInteracting)
          setInteracting(false) //this will cause repaint, but chart is already empy so no performace problem


      getCamera.addChild(chartBase.camera)
      onChartRefreshFinish.firePublish(chartBase)

  }

  refreshWorker.onFailed{
    _isRefreshInProgress = false;
    _.printStackTrace()
  }


  def centerOnPosition(pos:Vector3d){
    chartBase = chartBase.copy(position = pos)
    refresh()
  }

  /*
   * zoom actions
   */

  val actRefresh = chartAct{
    refresh();
  }

  val actZoomIn = chartAct{
    chartBase = chartBase.copy(fieldOfView = chartBase.fieldOfView * 0.5)
    refresh()
  }

  val actZoomOut = chartAct{
    chartBase = chartBase.copy(fieldOfView = chartBase.fieldOfView / 0.5)
    refresh()
  }

  private def setFov(fov:Angle) {
    chartBase = chartBase.copy(fieldOfView = fov);
    refresh();
  }

  val actFov15m = chartAct{setFov(15.arcMinute)}
  val actFov30m = chartAct{setFov(30.arcMinute)}
  val actFov1d = chartAct{setFov(1.degree)}
  val actFov2d = chartAct{setFov(2.degree)}
  val actFov4d = chartAct{setFov(4.degree)}
  val actFov8d = chartAct{setFov(8.degree)}
  val actFov15d = chartAct{setFov(15.degree)}
  val actFov30d = chartAct{setFov(30.degree)}
  val actFov60d = chartAct{setFov(60.degree)}
  val actFov120d = chartAct{setFov(120.degree)}


  /*
   * move actions
   */
  private def chartMove(x:Double, y:Double){
		val pos = chartBase.wcs.deproject(chartBase.width * x, chartBase.height * y)
		if(pos.isEmpty) return //not on map
		chartBase = chartBase.copy(position = pos.get)
		refresh()
	}

  val actMoveUpLeft = 	chartAct{ chartMove(0.0, 0.0)}
  val actMoveUp = 		chartAct{ chartMove(0.5, 0.0)}
  val actMoveUpRight = 	chartAct{ chartMove(1.0, 0.0)}
  val actMoveRight = 	chartAct{ chartMove(1.0, 0.5)}
  val actMoveDownRight = chartAct{ chartMove(1.0, 1.0)}
  val actMoveDown = 	chartAct{ chartMove(0.5, 1.0)}
  val actMoveDownLeft = chartAct{ chartMove(0.0, 1.0)}
  val actMoveLeft = 	chartAct{ chartMove(0.0, 0.5)}

  val actMirrorVert = chartAct{
    chartBase = chartBase.copy(xscale = -chartBase.xscale)
    refresh()
  }

  val actMirrorHoriz = chartAct{
    chartBase = chartBase.copy(yscale = -chartBase.yscale)
    refresh()
  }
  onChartRefreshStart{ m=>   //make sure checkbox are synchronized
    actMirrorVert.selected = Some(chartBase.xscale == -1);
    actMirrorHoriz.selected = Some(chartBase.yscale == -1);
  }


  val actRotateLeft = chartAct{
    chartBase = chartBase.copy(rotation = chartBase.rotation + 45.degree)
    refresh()
  }

  val actRotateRight = chartAct{
    chartBase = chartBase.copy(rotation = chartBase.rotation - 45.degree)
    refresh()
  }

  val actRotateCustom = chartAct{
    //TODO localization
    val ret = JOptionPane.showInputDialog("Rotation angle: ",
       chartBase.rotation.toDegree)
    if(ret != null){
      val rot = ret.toDouble.degree
      chartBase = chartBase.copy(rotation = rot)
      refresh()
    }
  }

  val actTransformReset = chartAct{
    chartBase = chartBase.copy(rotation = 0.degree, xscale = 1, yscale = 1)
    refresh()
  }



  /*
   * coordinate grids
   */


  val actCoordGridJ2000ShowLines = chartAct{
    coordGridConfig = coordGridConfig
      .copy(coordinateGridJ2000 = coordGridConfig.coordinateGridJ2000
      .copy(showLines = !coordGridConfig.coordinateGridJ2000.showLines))
    refresh()
  }
  val actCoordGridB1950ShowLines = chartAct{
    coordGridConfig = coordGridConfig
      .copy(coordinateGridJ1950 = coordGridConfig.coordinateGridJ1950
      .copy(showLines = !coordGridConfig.coordinateGridJ1950.showLines))
    refresh()
  }
  val actCoordGridEclipticShowLines = chartAct{
    coordGridConfig = coordGridConfig.copy(coordinateGridEcliptic = coordGridConfig.coordinateGridEcliptic.copy(showLines = !coordGridConfig.coordinateGridEcliptic.showLines))
    refresh()
  }
  val actCoordGridGalacticShowLines = chartAct{
    coordGridConfig = coordGridConfig.copy(coordinateGridGalactic = coordGridConfig.coordinateGridGalactic.copy(showLines = !coordGridConfig.coordinateGridGalactic.showLines))
    refresh()
  }

  val actCoordGridJ2000ShowPoles = chartAct{
    coordGridConfig = coordGridConfig.copy(coordinateGridJ2000 = coordGridConfig.coordinateGridJ2000.copy(showPoles = !coordGridConfig.coordinateGridJ2000.showPoles))
    refresh()
  }
  val actCoordGridB1950ShowPoles = chartAct{
    coordGridConfig = coordGridConfig.copy(coordinateGridJ1950 = coordGridConfig.coordinateGridJ1950.copy(showPoles = !coordGridConfig.coordinateGridJ1950.showPoles))
    refresh()
  }
  val actCoordGridEclipticShowPoles = chartAct{
    coordGridConfig = coordGridConfig.copy(coordinateGridEcliptic = coordGridConfig.coordinateGridEcliptic.copy(showPoles = !coordGridConfig.coordinateGridEcliptic.showPoles))
    refresh()
  }
  val actCoordGridGalacticShowPoles = chartAct{
    coordGridConfig = coordGridConfig.copy(coordinateGridGalactic = coordGridConfig.coordinateGridGalactic.copy(showPoles = !coordGridConfig.coordinateGridGalactic.showPoles))
    refresh()
  }

  val actCoordGridJ2000ShowEquator = chartAct{
    coordGridConfig = coordGridConfig.copy(coordinateGridJ2000 = coordGridConfig.coordinateGridJ2000.copy(showEquator = !coordGridConfig.coordinateGridJ2000.showEquator))
    refresh()
  }
  val actCoordGridB1950ShowEquator = chartAct{
    coordGridConfig = coordGridConfig.copy(coordinateGridJ1950 = coordGridConfig.coordinateGridJ1950.copy(showEquator = !coordGridConfig.coordinateGridJ1950.showEquator))
    refresh()
  }
  val actCoordGridEclipticShowEquator = chartAct{
    coordGridConfig = coordGridConfig.copy(coordinateGridEcliptic = coordGridConfig.coordinateGridEcliptic.copy(showEquator = !coordGridConfig.coordinateGridEcliptic.showEquator))
    refresh()
  }
  val actCoordGridGalacticShowEquator = chartAct{
    coordGridConfig = coordGridConfig.copy(coordinateGridGalactic = coordGridConfig.coordinateGridGalactic.copy(showEquator = !coordGridConfig.coordinateGridGalactic.showEquator))
    refresh()
  }

   onChartRefreshStart{ m=> //make sure check box states matches
    actCoordGridJ2000ShowLines.selected = Some(coordGridConfig.coordinateGridJ2000.showLines)
    actCoordGridB1950ShowLines.selected = Some(coordGridConfig.coordinateGridJ1950.showLines)
    actCoordGridEclipticShowLines.selected = Some(coordGridConfig.coordinateGridEcliptic.showLines)
    actCoordGridGalacticShowLines.selected = Some(coordGridConfig.coordinateGridGalactic.showLines)
    actCoordGridJ2000ShowPoles.selected = Some(coordGridConfig.coordinateGridJ2000.showPoles)
    actCoordGridB1950ShowPoles.selected = Some(coordGridConfig.coordinateGridJ1950.showPoles)
    actCoordGridEclipticShowPoles.selected = Some(coordGridConfig.coordinateGridEcliptic.showPoles)
    actCoordGridGalacticShowPoles.selected = Some(coordGridConfig.coordinateGridGalactic.showPoles)
    actCoordGridJ2000ShowEquator.selected = Some(coordGridConfig.coordinateGridJ2000.showEquator)
    actCoordGridB1950ShowEquator.selected = Some(coordGridConfig.coordinateGridJ1950.showEquator)
    actCoordGridEclipticShowEquator.selected = Some(coordGridConfig.coordinateGridEcliptic.showEquator)
    actCoordGridGalacticShowEquator.selected = Some(coordGridConfig.coordinateGridGalactic.showEquator)
  }

  /*
   * stars
   */

  val actLimitMagCustom = chartAct{
      //TODO localization
        val ret = JOptionPane.showInputDialog("Limit magnitude:",
          beans.stars.calculateLimitStarMag(chartBase, starsConfig));
        if(ret!=null){
          starsConfig = starsConfig.copy(limitStarMagForce = Some(Magnitude(ret.toDouble)))
        }else{
          starsConfig = starsConfig.copy(limitStarMagForce = None)
        }
        refresh()
  }

  val actMoreStars = chartAct{
    starsConfig = starsConfig.copy(limitStarMagDelta = starsConfig.limitStarMagDelta+0.5)
    refresh()
  }

  val actLessStars = chartAct{

    starsConfig = starsConfig.copy(limitStarMagDelta = starsConfig.limitStarMagDelta-0.5)
    refresh()
  }

  val actBiggerStars = chartAct{
    starsConfig = starsConfig.copy(starDiscMultiply = starsConfig.starDiscMultiply * 1.4)
    refresh()
  }

  val actSmallerStars = chartAct{
    starsConfig = starsConfig.copy(starDiscMultiply = starsConfig.starDiscMultiply / 1.4)
    refresh()
  }

  val actStarReset = chartAct{
    val orig = new ChartStarsConfig();
    starsConfig = starsConfig.copy(starDiscMultiply = orig.starDiscMultiply,
        limitStarMagDelta = orig.limitStarMagDelta,
        limitStarMagForce = None
        )
    refresh()
  }

  /*
   * deep sky
   */
  val actShowGlobularCluster = chartAct{
    deepSkyConfig = deepSkyConfig.copy(
        showGlobularCluster = !deepSkyConfig.showGlobularCluster)
    refresh()
  }

  val actShowOpenCluster = chartAct{
    deepSkyConfig = deepSkyConfig.copy(
        showOpenCluster = !deepSkyConfig.showOpenCluster)
    refresh()
  }

  val actShowGalaxy = chartAct{
    deepSkyConfig = deepSkyConfig.copy(
        showGalaxy = !deepSkyConfig.showGalaxy)
    refresh()
  }

  val actShowBrightNebula = chartAct{
    deepSkyConfig = deepSkyConfig.copy(
        showBrightNebula = !deepSkyConfig.showBrightNebula)
    refresh()
  }

  val actShowPlanetaryNebula = chartAct{
    deepSkyConfig = deepSkyConfig.copy(
        showPlanetaryNebula = !deepSkyConfig.showPlanetaryNebula)
    refresh()
  }

  val actShowDarkNebula = chartAct{
    deepSkyConfig = deepSkyConfig.copy(
        showDarkNebula = !deepSkyConfig.showDarkNebula)
    refresh()
  }

  val actShowSupernovaRemnant = chartAct{
    deepSkyConfig = deepSkyConfig.copy(
        showSupernovaRemnant = !deepSkyConfig.showSupernovaRemnant)
    refresh()
  }

  onChartRefreshStart{ m =>

    actShowGlobularCluster.selected = Some(deepSkyConfig.showGlobularCluster)
    actShowOpenCluster.selected = Some(deepSkyConfig.showOpenCluster)
    actShowGalaxy.selected = Some(deepSkyConfig.showGalaxy)
    actShowBrightNebula.selected = Some(deepSkyConfig.showBrightNebula)
    actShowPlanetaryNebula.selected = Some(deepSkyConfig.showPlanetaryNebula)
    actShowDarkNebula.selected = Some(deepSkyConfig.showDarkNebula)
    actShowSupernovaRemnant.selected = Some(deepSkyConfig.showSupernovaRemnant)
  }

  val actShowConstelLines = chartAct{
    showConstelLines = !showConstelLines
    refresh()
  }
  val actShowConstelBounds = chartAct{
    showConstelBounds = !showConstelBounds
    refresh()
  }

  val actShowLegend = chartAct{
    showLegend = !showLegend
    refresh()
  }


  val actInvertColors = chartAct{
    val colors =
      if(chartBase.colors == DarkBlueColors) LightColors
      else DarkBlueColors
    chartBase = chartBase.copy(colors = colors)
    refresh()
  }

  val actDSSAladinSurvey = chartAct{
    aladinConfig = Some(new AladinSurvey.Mem(survey = AladinSurvey.dssColorSurvey))
    refresh()
  }

  val actMellingerAladinSurvey = chartAct{
    aladinConfig = Some(new AladinSurvey.Mem(survey = AladinSurvey.mellingerSurvey))
    refresh()
  }

  val actNoneAladinSurvey = chartAct{
    aladinConfig = None
    refresh()
  }



  /**
   * Action used for Chart editor.
   * It is disabled while map is being modified
   */
  def chartAct(block: => Unit):Action = act(block)


  onChartRefreshStart{m =>
    actShowConstelBounds.selected = Some(showConstelBounds)
    actShowConstelLines.selected = Some(showConstelLines)
    actShowLegend.selected = Some(showLegend)
    actDSSAladinSurvey.selected = Some(aladinConfig.isDefined && aladinConfig.get.survey == AladinSurvey.dssColorSurvey)
    actMellingerAladinSurvey.selected = Some(aladinConfig.isDefined && aladinConfig.get.survey == AladinSurvey.mellingerSurvey)
    actNoneAladinSurvey.selected = Some(!aladinConfig.isDefined)
  }




}


