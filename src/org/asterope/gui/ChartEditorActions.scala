package org.asterope.gui

import javax.swing.Action


/**
 * List of Actions defined by ChartEditor,
 * it is used in ChartEditor but also in Main view
 */
trait ChartEditorActions{

  val actRefresh:Action
  val actZoomIn:Action
  val actZoomOut:Action

  val actFov15m:Action
  val actFov30m:Action
  val actFov1d:Action
  val actFov2d:Action
  val actFov4d:Action
  val actFov8d:Action
  val actFov15d:Action
  val actFov30d:Action
  val actFov60d:Action
  val actFov120d:Action


  val actMoveUpLeft:Action
  val actMoveUp:Action
  val actMoveUpRight:Action
  val actMoveRight:Action
  val actMoveDownRight:Action
  val actMoveDown:Action
  val actMoveDownLeft:Action
  val actMoveLeft:Action

  val actMirrorVert:Action
  val actMirrorHoriz:Action
  val actRotateLeft:Action
  val actRotateRight:Action
  val actRotateCustom:Action
  val actTransformReset:Action

  val actCoordGridJ2000ShowLines:Action
  val actCoordGridB1950ShowLines:Action
  val actCoordGridEclipticShowLines:Action
  val actCoordGridGalacticShowLines:Action

  val actCoordGridJ2000ShowPoles:Action
  val actCoordGridB1950ShowPoles:Action
  val actCoordGridEclipticShowPoles:Action
  val actCoordGridGalacticShowPoles:Action

  val actCoordGridJ2000ShowEquator:Action
  val actCoordGridB1950ShowEquator:Action
  val actCoordGridEclipticShowEquator:Action
  val actCoordGridGalacticShowEquator:Action

  val actLimitMagCustom:Action
  val actMoreStars:Action
  val actLessStars:Action
  val actBiggerStars:Action
  val actSmallerStars:Action
  val actStarReset:Action

  val actShowGlobularCluster:Action
  val actShowOpenCluster:Action
  val actShowGalaxy:Action
  val actShowBrightNebula:Action
  val actShowPlanetaryNebula:Action
  val actShowDarkNebula:Action
  val actShowSupernovaRemnant:Action

  val actShowConstelLines:Action
  val actShowConstelBounds:Action
  val actShowLegend:Action
  val actInvertColors:Action
  
  val actChartSkyview:Action

  val actDSSAladinSurvey:Action
  val actMellingerAladinSurvey:Action
  val actNoneAladinSurvey:Action

}
