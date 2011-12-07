package org.asterope.gui

import org.asterope.util.Angle
import javax.swing.JOptionPane
import org.asterope.util._
import org.asterope.chart.AllSkySurveyMem._
import org.asterope.chart._

/**
 * Actions for ChartEditor called from main menu.
 * Is in separate trait to make ChartEditor smaller.
 */

trait ChartEditorActions{ self:ChartEditor =>


  /*
   * zoom actions
   */

  val actRefresh = mainWinActions.actRefresh.editorAction(this){
    refresh();
  }

  val actZoomIn = mainWinActions.actZoomIn.editorAction(this){
    chartBase = chartBase.copy(fieldOfView = chartBase.fieldOfView * 0.5)
    refresh()
  }

  val actZoomOut = mainWinActions.actZoomOut.editorAction(this){
    chartBase = chartBase.copy(fieldOfView = chartBase.fieldOfView / 0.5)
    refresh()
  }

  private def setFov(fov:Angle) {
    chartBase = chartBase.copy(fieldOfView = fov);
    refresh();
  }

  val actFov15m = mainWinActions.actFov15m.editorAction(this){setFov(15.arcMinute)}
  val actFov30m = mainWinActions.actFov30m.editorAction(this){setFov(30.arcMinute)}
  val actFov1d = mainWinActions.actFov1d.editorAction(this){setFov(1.degree)}
  val actFov2d = mainWinActions.actFov1d.editorAction(this){setFov(2.degree)}
  val actFov4d = mainWinActions.actFov4d.editorAction(this){setFov(4.degree)}
  val actFov8d = mainWinActions.actFov8d.editorAction(this){setFov(8.degree)}
  val actFov15d = mainWinActions.actFov15d.editorAction(this){setFov(15.degree)}
  val actFov30d = mainWinActions.actFov30d.editorAction(this){setFov(30.degree)}
  val actFov60d = mainWinActions.actFov60d.editorAction(this){setFov(60.degree)}
  val actFov120d = mainWinActions.actFov120d.editorAction(this){setFov(120.degree)}


  /*
   * move actions
   */
  private def chartMove(x:Double, y:Double){
		val pos = chartBase.wcs.deproject(chartBase.width * x, chartBase.height * y)
		if(pos.isEmpty) return //not on map
		chartBase = chartBase.copy(position = pos.get)
		refresh()
	}

  val actMoveUpLeft = 	mainWinActions.actMoveUpLeft.editorAction(this){ chartMove(0.0, 0.0)}
  val actMoveUp = 		mainWinActions.actMoveUp.editorAction(this){ chartMove(0.5, 0.0)}
  val actMoveUpRight = 	mainWinActions.actMoveUpRight.editorAction(this){ chartMove(1.0, 0.0)}
  val actMoveRight = 	mainWinActions.actMoveRight.editorAction(this){ chartMove(1.0, 0.5)}
  val actMoveDownRight = mainWinActions.actMoveDownRight.editorAction(this){ chartMove(1.0, 1.0)}
  val actMoveDown = 	mainWinActions.actMoveDown.editorAction(this){ chartMove(0.5, 1.0)}
  val actMoveDownLeft = mainWinActions.actMoveDownLeft.editorAction(this){ chartMove(0.0, 1.0)}
  val actMoveLeft = 	mainWinActions.actMoveLeft.editorAction(this){ chartMove(0.0, 0.5)}

  val actMirrorVert = mainWinActions.actMirrorVert.editorAction(this){
    chartBase = chartBase.copy(xscale = -chartBase.xscale)
    refresh()
  }

  val actMirrorHoriz = mainWinActions.actMirrorHoriz.editorAction(this){
    chartBase = chartBase.copy(yscale = -chartBase.yscale)
    refresh()
  }
  onChartRefreshStart.listenInEDT{ m=>   //make sure checkbox are synchronized
    actMirrorVert.selected = Some(chartBase.xscale == -1);
    actMirrorHoriz.selected = Some(chartBase.yscale == -1);
  }


  val actRotateLeft = mainWinActions.actRotateLeft.editorAction(this){
    chartBase = chartBase.copy(rotation = chartBase.rotation + 45.degree)
    refresh()
  }

  val actRotateRight = mainWinActions.actRotateRight.editorAction(this){
    chartBase = chartBase.copy(rotation = chartBase.rotation - 45.degree)
    refresh()
  }

  val actRotateCustom = mainWinActions.actRotateCustom.editorAction(this){
    //TODO localization
    val ret = JOptionPane.showInputDialog("Rotation angle: ",
       chartBase.rotation.toDegree)
    if(ret != null){
      val rot = ret.toDouble.degree
      chartBase = chartBase.copy(rotation = rot)
      refresh()
    }
  }

  val actTransformReset = mainWinActions.actTransformReset.editorAction(this){
    chartBase = chartBase.copy(rotation = 0.degree, xscale = 1, yscale = 1)
    refresh()
  }



  /*
   * coordinate grids
   */


  val actCoordGridJ2000ShowLines = mainWinActions.actCoordGridJ2000ShowLines.editorAction(this){
    coordGridConfig = coordGridConfig
      .copy(coordinateGridJ2000 = coordGridConfig.coordinateGridJ2000
      .copy(showLines = !coordGridConfig.coordinateGridJ2000.showLines))
    refresh()
  }
  val actCoordGridB1950ShowLines = mainWinActions.actCoordGridB1950ShowLines.editorAction(this){
    coordGridConfig = coordGridConfig
      .copy(coordinateGridJ1950 = coordGridConfig.coordinateGridJ1950
      .copy(showLines = !coordGridConfig.coordinateGridJ1950.showLines))
    refresh()
  }
  val actCoordGridEclipticShowLines = mainWinActions.actCoordGridEclipticShowLines.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridEcliptic = coordGridConfig.coordinateGridEcliptic.copy(showLines = !coordGridConfig.coordinateGridEcliptic.showLines))
    refresh()
  }
  val actCoordGridGalacticShowLines = mainWinActions.actCoordGridGalacticShowLines.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridGalactic = coordGridConfig.coordinateGridGalactic.copy(showLines = !coordGridConfig.coordinateGridGalactic.showLines))
    refresh()
  }

  val actCoordGridJ2000ShowPoles = mainWinActions.actCoordGridJ2000ShowPoles.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridJ2000 = coordGridConfig.coordinateGridJ2000.copy(showPoles = !coordGridConfig.coordinateGridJ2000.showPoles))
    refresh()
  }
  val actCoordGridB1950ShowPoles = mainWinActions.actCoordGridB1950ShowPoles.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridJ1950 = coordGridConfig.coordinateGridJ1950.copy(showPoles = !coordGridConfig.coordinateGridJ1950.showPoles))
    refresh()
  }
  val actCoordGridEclipticShowPoles = mainWinActions.actCoordGridEclipticShowPoles.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridEcliptic = coordGridConfig.coordinateGridEcliptic.copy(showPoles = !coordGridConfig.coordinateGridEcliptic.showPoles))
    refresh()
  }
  val actCoordGridGalacticShowPoles = mainWinActions.actCoordGridGalacticShowPoles.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridGalactic = coordGridConfig.coordinateGridGalactic.copy(showPoles = !coordGridConfig.coordinateGridGalactic.showPoles))
    refresh()
  }

  val actCoordGridJ2000ShowEquator = mainWinActions.actCoordGridJ2000ShowEquator.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridJ2000 = coordGridConfig.coordinateGridJ2000.copy(showEquator = !coordGridConfig.coordinateGridJ2000.showEquator))
    refresh()
  }
  val actCoordGridB1950ShowEquator = mainWinActions.actCoordGridB1950ShowEquator.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridJ1950 = coordGridConfig.coordinateGridJ1950.copy(showEquator = !coordGridConfig.coordinateGridJ1950.showEquator))
    refresh()
  }
  val actCoordGridEclipticShowEquator = mainWinActions.actCoordGridEclipticShowEquator.editorAction(this){
    coordGridConfig = coordGridConfig.copy(coordinateGridEcliptic = coordGridConfig.coordinateGridEcliptic.copy(showEquator = !coordGridConfig.coordinateGridEcliptic.showEquator))
    refresh()
  }
  val actCoordGridGalacticShowEquator = mainWinActions.actCoordGridGalacticShowEquator.editorAction(this){
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

  val actLimitMagCustom = mainWinActions.actLimitMagCustom.editorAction(this){
      //TODO localization
        val ret = JOptionPane.showInputDialog("Limit magnitude:",
        stars.calculateLimitStarMag(chartBase, starsConfig));
        if(ret!=null){
          starsConfig = starsConfig.copy(limitStarMagForce = Some(Magnitude(ret.toDouble)))
        }else{
          starsConfig = starsConfig.copy(limitStarMagForce = None)
        }
        refresh()
  }

  val actMoreStars = mainWinActions.actMoreStars.editorAction(this){
    starsConfig = starsConfig.copy(limitStarMagDelta = starsConfig.limitStarMagDelta+0.5)
    refresh()
  }

  val actLessStars = mainWinActions.actLessStars.editorAction(this){

    starsConfig = starsConfig.copy(limitStarMagDelta = starsConfig.limitStarMagDelta-0.5)
    refresh()
  }

  val actBiggerStars = mainWinActions.actBiggerStars.editorAction(this){
    starsConfig = starsConfig.copy(starDiscMultiply = starsConfig.starDiscMultiply * 1.4)
    refresh()
  }

  val actSmallerStars = mainWinActions.actSmallerStars.editorAction(this){
    starsConfig = starsConfig.copy(starDiscMultiply = starsConfig.starDiscMultiply / 1.4)
    refresh()
  }

  val actStarReset = mainWinActions.actStarReset.editorAction(this){
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
  val actShowGlobularCluster = mainWinActions.actShowGlobularCluster.editorAction(this){
    deepSkyConfig = deepSkyConfig.copy(
        showGlobularCluster = !deepSkyConfig.showGlobularCluster)
    refresh()
  }

  val actShowOpenCluster = mainWinActions.actShowOpenCluster.editorAction(this){
    deepSkyConfig = deepSkyConfig.copy(
        showOpenCluster = !deepSkyConfig.showOpenCluster)
    refresh()
  }

  val actShowGalaxy = mainWinActions.actShowGalaxy.editorAction(this){
    deepSkyConfig = deepSkyConfig.copy(
        showGalaxy = !deepSkyConfig.showGalaxy)
    refresh()
  }

  val actShowBrightNebula = mainWinActions.actShowBrightNebula.editorAction(this){
    deepSkyConfig = deepSkyConfig.copy(
        showBrightNebula = !deepSkyConfig.showBrightNebula)
    refresh()
  }

  val actShowPlanetaryNebula = mainWinActions.actShowPlanetaryNebula.editorAction(this){
    deepSkyConfig = deepSkyConfig.copy(
        showPlanetaryNebula = !deepSkyConfig.showPlanetaryNebula)
    refresh()
  }

  val actShowDarkNebula = mainWinActions.actShowDarkNebula.editorAction(this){
    deepSkyConfig = deepSkyConfig.copy(
        showDarkNebula = !deepSkyConfig.showDarkNebula)
    refresh()
  }

  val actShowSupernovaRemnant = mainWinActions.actShowSupernovaRemnant.editorAction(this){
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

  val actShowConstelLines = mainWinActions.actShowConstelLines.editorAction(this){
    showConstelLines = !showConstelLines
    refresh()
  }
  val actShowConstelBounds = mainWinActions.actShowConstelBounds.editorAction(this){
    showConstelBounds = !showConstelBounds
    refresh()
  }

  val actShowLegend = mainWinActions.actShowLegend.editorAction(this){
    showLegend = !showLegend
    refresh()
  }


  val actInvertColors = mainWinActions.actInvertColors.editorAction(this){
    val colors =
      if(chartBase.colors == DarkBlueColors) LightColors
      else DarkBlueColors
    chartBase = chartBase.copy(colors = colors)
    refresh()
  }

  val actDSSAllSkySurvey = mainWinActions.actDSSAllSkySurvey.editorAction(this){
    allSkyConfig = Some(new AllSkySurveyMem(survey = AllSkySurvey.dssColorSurvey))
    refresh()
  }

  val actMellingerAllSkySurvey = mainWinActions.actMellingerAllSkySurvey.editorAction(this){
    allSkyConfig = Some(new AllSkySurveyMem(survey = AllSkySurvey.mellingerSurvey))
    refresh()
  }

  val actNoneAllSkySurvey = mainWinActions.actNoneAllSkySurvey.editorAction(this){
    allSkyConfig = None
    refresh()
  }






  onChartRefreshStart.listenInEDT{m =>
    actShowConstelBounds.selected = Some(showConstelBounds)
    actShowConstelLines.selected = Some(showConstelLines)
    actShowLegend.selected = Some(showLegend)
    actDSSAllSkySurvey.selected = Some(allSkyConfig.isDefined && allSkyConfig.get.survey == AllSkySurvey.dssColorSurvey)
    actMellingerAllSkySurvey.selected = Some(allSkyConfig.isDefined && allSkyConfig.get.survey == AllSkySurvey.mellingerSurvey)
    actNoneAllSkySurvey.selected = Some(!allSkyConfig.isDefined)
  }


}