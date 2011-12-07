package org.asterope.gui

import org.asterope.data._
import org.asterope.util._

class MainWindowActions(
  resmap:ResourceMap,
  mainWin:MainWindow,
  nameResolver:NameResolver,
  chartEditorFab:()=>ChartEditor){


  var lastSearchText = "";
  val searchObject = act{
    object d extends SearchDialog(nameResolver, resmap)
    resmap.injectComponents(d)
    resmap.injectActionFields(d)
    val res = Form.showDialog(new NameResolverResult(None, None,lastSearchText),d)
    if(res.isDefined){
      lastSearchText = res.get.queryString
      openChartOnObject(lastSearchText)
    }
  }

  def openChartOnObject(objName:String){
    val pos = nameResolver.resolve(objName).pos.get
    val comp:ChartEditor = chartEditorFab() //create new ChartEditor using supplyed factory method
    mainWin.addEditor(objName,comp)
    comp.centerOnPosition(pos)

  }
  
  
  val actRefresh = new EditorBoundAction(mainWin)
  val actZoomIn = new EditorBoundAction(mainWin)
  val actZoomOut = new EditorBoundAction(mainWin)

  val actFov15m = new EditorBoundAction(mainWin)
  val actFov30m = new EditorBoundAction(mainWin)
  val actFov1d = new EditorBoundAction(mainWin)
  val actFov2d = new EditorBoundAction(mainWin)
  val actFov4d = new EditorBoundAction(mainWin)
  val actFov8d = new EditorBoundAction(mainWin)
  val actFov15d = new EditorBoundAction(mainWin)
  val actFov30d = new EditorBoundAction(mainWin)
  val actFov60d = new EditorBoundAction(mainWin)
  val actFov120d = new EditorBoundAction(mainWin)

  val actMoveUpLeft = new EditorBoundAction(mainWin)
  val actMoveUp = new EditorBoundAction(mainWin)
  val actMoveUpRight = new EditorBoundAction(mainWin)
  val actMoveRight = new EditorBoundAction(mainWin)
  val actMoveDownRight = new EditorBoundAction(mainWin)
  val actMoveDown = new EditorBoundAction(mainWin)
  val actMoveDownLeft = new EditorBoundAction(mainWin)
  val actMoveLeft = new EditorBoundAction(mainWin)

  val actMirrorVert = new EditorBoundAction(mainWin)
  val actMirrorHoriz = new EditorBoundAction(mainWin)
  val actRotateLeft = new EditorBoundAction(mainWin)
  val actRotateRight = new EditorBoundAction(mainWin)
  val actRotateCustom = new EditorBoundAction(mainWin)
  val actTransformReset = new EditorBoundAction(mainWin)


  val actCoordGridJ2000ShowLines = new EditorBoundAction(mainWin)
  val actCoordGridB1950ShowLines = new EditorBoundAction(mainWin)
  val actCoordGridEclipticShowLines = new EditorBoundAction(mainWin)
  val actCoordGridGalacticShowLines = new EditorBoundAction(mainWin)

  val actCoordGridJ2000ShowPoles = new EditorBoundAction(mainWin)
  val actCoordGridB1950ShowPoles = new EditorBoundAction(mainWin)
  val actCoordGridEclipticShowPoles = new EditorBoundAction(mainWin)
  val actCoordGridGalacticShowPoles = new EditorBoundAction(mainWin)

  val actCoordGridJ2000ShowEquator = new EditorBoundAction(mainWin)
  val actCoordGridB1950ShowEquator = new EditorBoundAction(mainWin)
  val actCoordGridEclipticShowEquator = new EditorBoundAction(mainWin)
  val actCoordGridGalacticShowEquator = new EditorBoundAction(mainWin)

  val actLimitMagCustom = new EditorBoundAction(mainWin)
  val actMoreStars = new EditorBoundAction(mainWin)
  val actLessStars = new EditorBoundAction(mainWin)
  val actBiggerStars = new EditorBoundAction(mainWin)
  val actSmallerStars = new EditorBoundAction(mainWin)
  val actStarReset = new EditorBoundAction(mainWin)

  val actShowGlobularCluster = new EditorBoundAction(mainWin)
  val actShowOpenCluster = new EditorBoundAction(mainWin)
  val actShowGalaxy = new EditorBoundAction(mainWin)
  val actShowBrightNebula = new EditorBoundAction(mainWin)
  val actShowPlanetaryNebula = new EditorBoundAction(mainWin)
  val actShowDarkNebula = new EditorBoundAction(mainWin)
  val actShowSupernovaRemnant = new EditorBoundAction(mainWin)

  val actShowConstelLines = new EditorBoundAction(mainWin)
  val actShowConstelBounds = new EditorBoundAction(mainWin)
  val actShowLegend = new EditorBoundAction(mainWin)
  val actInvertColors = new EditorBoundAction(mainWin)
  
  val actChartSkyview = new EditorBoundAction(mainWin)

  val actDSSAllSkySurvey  = new EditorBoundAction(mainWin)
  val actMellingerAllSkySurvey  = new EditorBoundAction(mainWin)
  val actNoneAllSkySurvey  = new EditorBoundAction(mainWin)
  

}