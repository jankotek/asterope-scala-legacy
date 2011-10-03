package org.asterope.gui

import org.asterope.util._
import edu.umd.cs.piccolo._
import edu.umd.cs.piccolo.event._
import java.awt.event._

import edu.umd.cs.piccolo.util.PBounds
import org.jdesktop.swingx.action.BoundAction
import javax.swing._
import org.asterope.chart._


class ChartEditor(
    val beans:ChartBeans
  )
  extends PCanvas
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




  //refresh when canvas size changes
  addComponentListener(new ComponentAdapter{
    override def componentResized(e:ComponentEvent){
      refresh()
    }
  })
  
  /**
   * Notify when chart refresh starts 
   */
  lazy val onChartRefreshStart = new Publisher[Chart]()

  /**
   * Notify when chart refresh finishes 
   */
  lazy val onChartRefreshFinish = new Publisher[Chart]()


  private var chartBase = new Chart(executor = new EDTChartExecutor);
  private var coordGridConfig = CoordinateGrid.defaultConfig
  private var starsConfig = beans.stars.defaultConfig
  private var showLegend = true
  private var showConstelBounds = true
  private var showConstelLines = true;
  private var deepSkyConfig = beans.deepSky.defaultConfig
  private var aladinConfig:Option[AladinSurveyMem] = None


  def getChartBase = chartBase

  private var refreshWorker:SwingWorker[Chart, Unit] = null

  def refreshInProgress:Boolean = refreshWorker == null || !refreshWorker.isDone

  def refresh():Unit={
    if(refreshWorker!=null)
      while(!refreshWorker.isDone && !refreshWorker.cancel(true)) Thread.sleep(1)

    refreshWorker = new SwingWorker[Chart,Unit]{
      def doInBackground:Chart = {

      Thread.sleep(500)

      if (getInteracting){
        //if user used mouse to move chart, center on new position and update FOV
        val bounds = chartBase.camera.getViewBounds;
        val center = chartBase.wcs.deproject(bounds.getCenter2D);
        if(center.isDefined){
          val fov = chartBase.wcs.deproject(bounds.getOrigin).map(_.angle(center.get) * 2).getOrElse(120 * Angle.D2R);
          chartBase = chartBase.copy(position = center.get, fieldOfView = fov.radian)
        }
      }


      val chart = chartBase.copy(width = getWidth,
        height = if( !showLegend) getHeight else (getHeight - beans.legendBorder.height),
        legendHeight = if(showLegend) beans.legendBorder.height else 0,
        executor = new EDTChartExecutor
      )

      beans.stars.updateChart(chart,starsConfig)
      beans.deepSky.updateChart(chart,deepSkyConfig)

      if(showConstelBounds)
          beans.constelBoundary.updateChart(chart)

      if(showConstelLines)
          beans.constelLine.updateChart(chart)

      aladinConfig.foreach{mem=>
        AladinSurvey.updateChart(chart,mem)
      }

      beans.milkyWay.updateChart(chart)

      CoordinateGrid.updateChart(chart,coordGridConfig)

      if(showLegend)
          beans.legendBorder.updateChart(chart)

      chart
    }

      override def done{
        if(isCancelled) return

        val chart = get()
        //labels must be last,
        // placement alghorihm depends on graphic created by other features
        Labels.updateChart(chart)
        chartBase = chart;
        chartBase.executor.asInstanceOf[EDTChartExecutor].plugIntoSwing()
        getCamera.removeAllChildren();
        if(getInteracting)
           setInteracting(false) //this will cause repaint, but chart is already empy so no performace problem

        getCamera.addChild(chartBase.camera)
        onChartRefreshFinish.firePublish(chartBase)

      }
  }

    refreshWorker.execute()

  }





  def centerOnPosition(pos:Vector3d){
    chartBase = chartBase.copy(position = pos)
    refresh()
  }

  /*
   * zoom actions
   */

  val actRefresh = Main.actRefresh.editorAction(this){
    refresh();
  }

  val actZoomIn = Main.actZoomIn.editorAction(this){
    chartBase = chartBase.copy(fieldOfView = chartBase.fieldOfView * 0.5)
    refresh()
  }

  val actZoomOut = Main.actZoomOut.editorAction(this){
    chartBase = chartBase.copy(fieldOfView = chartBase.fieldOfView / 0.5)
    refresh()
  }

  private def setFov(fov:Angle) {
    chartBase = chartBase.copy(fieldOfView = fov);
    refresh();
  }

  val actFov15m = Main.actFov15m.editorAction(this){setFov(15.arcMinute)}
  val actFov30m = Main.actFov30m.editorAction(this){setFov(30.arcMinute)}
  val actFov1d = Main.actFov1d.editorAction(this){setFov(1.degree)}
  val actFov2d = Main.actFov1d.editorAction(this){setFov(2.degree)}
  val actFov4d = Main.actFov4d.editorAction(this){setFov(4.degree)}
  val actFov8d = Main.actFov8d.editorAction(this){setFov(8.degree)}
  val actFov15d = Main.actFov15d.editorAction(this){setFov(15.degree)}
  val actFov30d = Main.actFov30d.editorAction(this){setFov(30.degree)}
  val actFov60d = Main.actFov60d.editorAction(this){setFov(60.degree)}
  val actFov120d = Main.actFov120d.editorAction(this){setFov(120.degree)}


  /*
   * move actions
   */
  private def chartMove(x:Double, y:Double){
		val pos = chartBase.wcs.deproject(chartBase.width * x, chartBase.height * y)
		if(pos.isEmpty) return //not on map
		chartBase = chartBase.copy(position = pos.get)
		refresh()
	}

  val actMoveUpLeft = 	Main.actMoveUpLeft.editorAction(this){ chartMove(0.0, 0.0)}
  val actMoveUp = 		Main.actMoveUp.editorAction(this){ chartMove(0.5, 0.0)}
  val actMoveUpRight = 	Main.actMoveUpRight.editorAction(this){ chartMove(1.0, 0.0)}
  val actMoveRight = 	Main.actMoveRight.editorAction(this){ chartMove(1.0, 0.5)}
  val actMoveDownRight = Main.actMoveDownRight.editorAction(this){ chartMove(1.0, 1.0)}
  val actMoveDown = 	Main.actMoveDown.editorAction(this){ chartMove(0.5, 1.0)}
  val actMoveDownLeft = Main.actMoveDownLeft.editorAction(this){ chartMove(0.0, 1.0)}
  val actMoveLeft = 	Main.actMoveLeft.editorAction(this){ chartMove(0.0, 0.5)}

  val actMirrorVert = Main.actMirrorVert.editorAction(this){
    chartBase = chartBase.copy(xscale = -chartBase.xscale)
    refresh()
  }

  val actMirrorHoriz = Main.actMirrorHoriz.editorAction(this){
    chartBase = chartBase.copy(yscale = -chartBase.yscale)
    refresh()
  }
  onChartRefreshStart.listenInEDT{ m=>   //make sure checkbox are synchronized
    actMirrorVert.selected = Some(chartBase.xscale == -1);
    actMirrorHoriz.selected = Some(chartBase.yscale == -1);
  }


  val actRotateLeft = Main.actRotateLeft.editorAction(this){
    chartBase = chartBase.copy(rotation = chartBase.rotation + 45.degree)
    refresh()
  }

  val actRotateRight = Main.actRotateRight.editorAction(this){
    chartBase = chartBase.copy(rotation = chartBase.rotation - 45.degree)
    refresh()
  }

  val actRotateCustom = Main.actRotateCustom.editorAction(this){
    //TODO localization
    val ret = JOptionPane.showInputDialog("Rotation angle: ",
       chartBase.rotation.toDegree)
    if(ret != null){
      val rot = ret.toDouble.degree
      chartBase = chartBase.copy(rotation = rot)
      refresh()
    }
  }

  val actTransformReset = Main.actTransformReset.editorAction(this){
    chartBase = chartBase.copy(rotation = 0.degree, xscale = 1, yscale = 1)
    refresh()
  }



  /*
   * coordinate grids
   */


  val actCoordGridJ2000ShowLines = Main.actCoordGridJ2000ShowLines.editorAction(this){
    coordGridConfig = coordGridConfig
      .copy(coordinateGridJ2000 = coordGridConfig.coordinateGridJ2000
      .copy(showLines = !coordGridConfig.coordinateGridJ2000.showLines))
    refresh()
  }
  val actCoordGridB1950ShowLines = Main.actCoordGridB1950ShowLines.editorAction(this){
    coordGridConfig = coordGridConfig
      .copy(coordinateGridJ1950 = coordGridConfig.coordinateGridJ1950
      .copy(showLines = !coordGridConfig.coordinateGridJ1950.showLines))
    refresh()
  }
  val actCoordGridEclipticShowLines = Main.actCoordGridEclipticShowLines.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridEcliptic = coordGridConfig.coordinateGridEcliptic.copy(showLines = !coordGridConfig.coordinateGridEcliptic.showLines))
    refresh()
  }
  val actCoordGridGalacticShowLines = Main.actCoordGridGalacticShowLines.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridGalactic = coordGridConfig.coordinateGridGalactic.copy(showLines = !coordGridConfig.coordinateGridGalactic.showLines))
    refresh()
  }

  val actCoordGridJ2000ShowPoles = Main.actCoordGridJ2000ShowPoles.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridJ2000 = coordGridConfig.coordinateGridJ2000.copy(showPoles = !coordGridConfig.coordinateGridJ2000.showPoles))
    refresh()
  }
  val actCoordGridB1950ShowPoles = Main.actCoordGridB1950ShowPoles.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridJ1950 = coordGridConfig.coordinateGridJ1950.copy(showPoles = !coordGridConfig.coordinateGridJ1950.showPoles))
    refresh()
  }
  val actCoordGridEclipticShowPoles = Main.actCoordGridEclipticShowPoles.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridEcliptic = coordGridConfig.coordinateGridEcliptic.copy(showPoles = !coordGridConfig.coordinateGridEcliptic.showPoles))
    refresh()
  }
  val actCoordGridGalacticShowPoles = Main.actCoordGridGalacticShowPoles.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridGalactic = coordGridConfig.coordinateGridGalactic.copy(showPoles = !coordGridConfig.coordinateGridGalactic.showPoles))
    refresh()
  }

  val actCoordGridJ2000ShowEquator = Main.actCoordGridJ2000ShowEquator.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridJ2000 = coordGridConfig.coordinateGridJ2000.copy(showEquator = !coordGridConfig.coordinateGridJ2000.showEquator))
    refresh()
  }
  val actCoordGridB1950ShowEquator = Main.actCoordGridB1950ShowEquator.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridJ1950 = coordGridConfig.coordinateGridJ1950.copy(showEquator = !coordGridConfig.coordinateGridJ1950.showEquator))
    refresh()
  }
  val actCoordGridEclipticShowEquator = Main.actCoordGridEclipticShowEquator.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridEcliptic = coordGridConfig.coordinateGridEcliptic.copy(showEquator = !coordGridConfig.coordinateGridEcliptic.showEquator))
    refresh()
  }
  val actCoordGridGalacticShowEquator = Main.actCoordGridGalacticShowEquator.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridGalactic = coordGridConfig.coordinateGridGalactic.copy(showEquator = !coordGridConfig.coordinateGridGalactic.showEquator))
    refresh()
  }

   onChartRefreshStart.listenInEDT{ m=> //make sure check box states matches
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

  val actLimitMagCustom = Main.actLimitMagCustom.editorAction(this){
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

  val actMoreStars = Main.actMoreStars.editorAction(this){
    starsConfig = starsConfig.copy(limitStarMagDelta = starsConfig.limitStarMagDelta+0.5)
    refresh()
  }

  val actLessStars = Main.actLessStars.editorAction(this){

    starsConfig = starsConfig.copy(limitStarMagDelta = starsConfig.limitStarMagDelta-0.5)
    refresh()
  }

  val actBiggerStars = Main.actBiggerStars.editorAction(this){
    starsConfig = starsConfig.copy(starDiscMultiply = starsConfig.starDiscMultiply * 1.4)
    refresh()
  }

  val actSmallerStars = Main.actSmallerStars.editorAction(this){
    starsConfig = starsConfig.copy(starDiscMultiply = starsConfig.starDiscMultiply / 1.4)
    refresh()
  }

  val actStarReset = Main.actStarReset.editorAction(this){
    val orig = new StarsConfig();
    starsConfig = starsConfig.copy(starDiscMultiply = orig.starDiscMultiply,
        limitStarMagDelta = orig.limitStarMagDelta,
        limitStarMagForce = None
        )
    refresh()
  }

  /*
   * deep sky
   */
  val actShowGlobularCluster = Main.actShowGlobularCluster.editorAction(this){
    deepSkyConfig = deepSkyConfig.copy(
        showGlobularCluster = !deepSkyConfig.showGlobularCluster)
    refresh()
  }

  val actShowOpenCluster = Main.actShowOpenCluster.editorAction(this){
    deepSkyConfig = deepSkyConfig.copy(
        showOpenCluster = !deepSkyConfig.showOpenCluster)
    refresh()
  }

  val actShowGalaxy = Main.actShowGalaxy.editorAction(this){
    deepSkyConfig = deepSkyConfig.copy(
        showGalaxy = !deepSkyConfig.showGalaxy)
    refresh()
  }

  val actShowBrightNebula = Main.actShowBrightNebula.editorAction(this){
    deepSkyConfig = deepSkyConfig.copy(
        showBrightNebula = !deepSkyConfig.showBrightNebula)
    refresh()
  }

  val actShowPlanetaryNebula = Main.actShowPlanetaryNebula.editorAction(this){
    deepSkyConfig = deepSkyConfig.copy(
        showPlanetaryNebula = !deepSkyConfig.showPlanetaryNebula)
    refresh()
  }

  val actShowDarkNebula = Main.actShowDarkNebula.editorAction(this){
    deepSkyConfig = deepSkyConfig.copy(
        showDarkNebula = !deepSkyConfig.showDarkNebula)
    refresh()
  }

  val actShowSupernovaRemnant = Main.actShowSupernovaRemnant.editorAction(this){
    deepSkyConfig = deepSkyConfig.copy(
        showSupernovaRemnant = !deepSkyConfig.showSupernovaRemnant)
    refresh()
  }

  onChartRefreshStart.listenInEDT{ m =>

    actShowGlobularCluster.selected = Some(deepSkyConfig.showGlobularCluster)
    actShowOpenCluster.selected = Some(deepSkyConfig.showOpenCluster)
    actShowGalaxy.selected = Some(deepSkyConfig.showGalaxy)
    actShowBrightNebula.selected = Some(deepSkyConfig.showBrightNebula)
    actShowPlanetaryNebula.selected = Some(deepSkyConfig.showPlanetaryNebula)
    actShowDarkNebula.selected = Some(deepSkyConfig.showDarkNebula)
    actShowSupernovaRemnant.selected = Some(deepSkyConfig.showSupernovaRemnant)
  }

  val actShowConstelLines = Main.actShowConstelLines.editorAction(this){
    showConstelLines = !showConstelLines
    refresh()
  }
  val actShowConstelBounds = Main.actShowConstelBounds.editorAction(this){
    showConstelBounds = !showConstelBounds
    refresh()
  }

  val actShowLegend = Main.actShowLegend.editorAction(this){
    showLegend = !showLegend
    refresh()
  }


  val actInvertColors = Main.actInvertColors.editorAction(this){
    val colors =
      if(chartBase.colors == DarkBlueColors) LightColors
      else DarkBlueColors
    chartBase = chartBase.copy(colors = colors)
    refresh()
  }

  val actDSSAladinSurvey = Main.actDSSAladinSurvey.editorAction(this){
    aladinConfig = Some(new AladinSurveyMem(survey = AladinSurvey.dssColorSurvey))
    refresh()
  }

  val actMellingerAladinSurvey = Main.actMellingerAladinSurvey.editorAction(this){
    aladinConfig = Some(new AladinSurveyMem(survey = AladinSurvey.mellingerSurvey))
    refresh()
  }

  val actNoneAladinSurvey = Main.actNoneAladinSurvey.editorAction(this){
    aladinConfig = None
    refresh()
  }






  onChartRefreshStart.listenInEDT{m =>
    actShowConstelBounds.selected = Some(showConstelBounds)
    actShowConstelLines.selected = Some(showConstelLines)
    actShowLegend.selected = Some(showLegend)
    actDSSAladinSurvey.selected = Some(aladinConfig.isDefined && aladinConfig.get.survey == AladinSurvey.dssColorSurvey)
    actMellingerAladinSurvey.selected = Some(aladinConfig.isDefined && aladinConfig.get.survey == AladinSurvey.mellingerSurvey)
    actNoneAladinSurvey.selected = Some(!aladinConfig.isDefined)
  }




}


