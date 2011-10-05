package org.asterope.gui

import org.asterope.data._
import org.asterope.util._
import org.asterope.chart.ChartBeans
import org.jdesktop.swingx.action.BoundAction


trait MainWindowActions extends ChartEditorFab{

  val resourceMap:ResourceMap
  val chartBeans:ChartBeans

  var lastSearchText = "";
  val searchObject = act{
    object d extends SearchDialog(chartBeans.nameResolver, resourceMap)
    resourceMap.injectComponents(d)
    resourceMap.injectActionFields(d)
    val res = Form.showDialog(new NameResolverResult(None, None,lastSearchText),d)
    if(res.isDefined){
      lastSearchText = res.get.queryString
      openChartOnObject(lastSearchText)
    }
  }
  
  
  val actRefresh = new EditorBoundAction()
  val actZoomIn = new EditorBoundAction()
  val actZoomOut = new EditorBoundAction()

  val actFov15m = new EditorBoundAction()
  val actFov30m = new EditorBoundAction()
  val actFov1d = new EditorBoundAction()
  val actFov2d = new EditorBoundAction()
  val actFov4d = new EditorBoundAction()
  val actFov8d = new EditorBoundAction()
  val actFov15d = new EditorBoundAction()
  val actFov30d = new EditorBoundAction()
  val actFov60d = new EditorBoundAction()
  val actFov120d = new EditorBoundAction()

  val actMoveUpLeft = new EditorBoundAction()
  val actMoveUp = new EditorBoundAction()
  val actMoveUpRight = new EditorBoundAction()
  val actMoveRight = new EditorBoundAction()
  val actMoveDownRight = new EditorBoundAction()
  val actMoveDown = new EditorBoundAction()
  val actMoveDownLeft = new EditorBoundAction()
  val actMoveLeft = new EditorBoundAction()

  val actMirrorVert = new EditorBoundAction()
  val actMirrorHoriz = new EditorBoundAction()
  val actRotateLeft = new EditorBoundAction()
  val actRotateRight = new EditorBoundAction()
  val actRotateCustom = new EditorBoundAction()
  val actTransformReset = new EditorBoundAction()


  val actCoordGridJ2000ShowLines = new EditorBoundAction()
  val actCoordGridB1950ShowLines = new EditorBoundAction()
  val actCoordGridEclipticShowLines = new EditorBoundAction()
  val actCoordGridGalacticShowLines = new EditorBoundAction()

  val actCoordGridJ2000ShowPoles = new EditorBoundAction()
  val actCoordGridB1950ShowPoles = new EditorBoundAction()
  val actCoordGridEclipticShowPoles = new EditorBoundAction()
  val actCoordGridGalacticShowPoles = new EditorBoundAction()

  val actCoordGridJ2000ShowEquator = new EditorBoundAction()
  val actCoordGridB1950ShowEquator = new EditorBoundAction()
  val actCoordGridEclipticShowEquator = new EditorBoundAction()
  val actCoordGridGalacticShowEquator = new EditorBoundAction()

  val actLimitMagCustom = new EditorBoundAction()
  val actMoreStars = new EditorBoundAction()
  val actLessStars = new EditorBoundAction()
  val actBiggerStars = new EditorBoundAction()
  val actSmallerStars = new EditorBoundAction()
  val actStarReset = new EditorBoundAction()

  val actShowGlobularCluster = new EditorBoundAction()
  val actShowOpenCluster = new EditorBoundAction()
  val actShowGalaxy = new EditorBoundAction()
  val actShowBrightNebula = new EditorBoundAction()
  val actShowPlanetaryNebula = new EditorBoundAction()
  val actShowDarkNebula = new EditorBoundAction()
  val actShowSupernovaRemnant = new EditorBoundAction()

  val actShowConstelLines = new EditorBoundAction()
  val actShowConstelBounds = new EditorBoundAction()
  val actShowLegend = new EditorBoundAction()
  val actInvertColors = new EditorBoundAction()
  
  val actChartSkyview = new EditorBoundAction()

  val actDSSAllSkySurvey  = new EditorBoundAction()
  val actMellingerAllSkySurvey  = new EditorBoundAction()
  val actNoneAllSkySurvey  = new EditorBoundAction()
  

}