package org.asterope.gui

import org.asterope.chart.ChartBeans
import net.infonode.docking.View
import javax.swing._

/**
 * Trait mixed in into MainView.
 * It provides Actions for MainView which are forwarded to active ChartEditor
 * It also provides factory method to create new ChartEditor tab
 */
trait ChartEditorFab extends ChartEditorActions with MainWindow{

  val chartBeans:ChartBeans

  def openChartOnObject(objName:String){
    val pos = chartBeans.nameResolver.resolve(objName).pos.get
    val comp = new ChartEditor(chartBeans)
    comp.centerOnPosition(pos)
    addEditor(objName,comp)
  }

  val actRefresh = editorForwardAction(classOf[ChartEditorActions])
  val actZoomIn = editorForwardAction(classOf[ChartEditorActions])
  val actZoomOut = editorForwardAction(classOf[ChartEditorActions])

  val actFov15m = editorForwardAction(classOf[ChartEditorActions])
  val actFov30m = editorForwardAction(classOf[ChartEditorActions])
  val actFov1d = editorForwardAction(classOf[ChartEditorActions])
  val actFov2d = editorForwardAction(classOf[ChartEditorActions])
  val actFov4d = editorForwardAction(classOf[ChartEditorActions])
  val actFov8d = editorForwardAction(classOf[ChartEditorActions])
  val actFov15d = editorForwardAction(classOf[ChartEditorActions])
  val actFov30d = editorForwardAction(classOf[ChartEditorActions])
  val actFov60d = editorForwardAction(classOf[ChartEditorActions])
  val actFov120d = editorForwardAction(classOf[ChartEditorActions])

  val actMoveUpLeft = editorForwardAction(classOf[ChartEditorActions])
  val actMoveUp = editorForwardAction(classOf[ChartEditorActions])
  val actMoveUpRight = editorForwardAction(classOf[ChartEditorActions])
  val actMoveRight = editorForwardAction(classOf[ChartEditorActions])
  val actMoveDownRight = editorForwardAction(classOf[ChartEditorActions])
  val actMoveDown = editorForwardAction(classOf[ChartEditorActions])
  val actMoveDownLeft = editorForwardAction(classOf[ChartEditorActions])
  val actMoveLeft = editorForwardAction(classOf[ChartEditorActions])

  val actMirrorVert = editorForwardAction(classOf[ChartEditorActions])
  val actMirrorHoriz = editorForwardAction(classOf[ChartEditorActions])
  val actRotateLeft = editorForwardAction(classOf[ChartEditorActions])
  val actRotateRight = editorForwardAction(classOf[ChartEditorActions])
  val actRotateCustom = editorForwardAction(classOf[ChartEditorActions])
  val actTransformReset = editorForwardAction(classOf[ChartEditorActions])


  val actCoordGridJ2000ShowLines = editorForwardAction(classOf[ChartEditorActions])
  val actCoordGridB1950ShowLines = editorForwardAction(classOf[ChartEditorActions])
  val actCoordGridEclipticShowLines = editorForwardAction(classOf[ChartEditorActions])
  val actCoordGridGalacticShowLines = editorForwardAction(classOf[ChartEditorActions])

  val actCoordGridJ2000ShowPoles = editorForwardAction(classOf[ChartEditorActions])
  val actCoordGridB1950ShowPoles = editorForwardAction(classOf[ChartEditorActions])
  val actCoordGridEclipticShowPoles = editorForwardAction(classOf[ChartEditorActions])
  val actCoordGridGalacticShowPoles = editorForwardAction(classOf[ChartEditorActions])

  val actCoordGridJ2000ShowEquator = editorForwardAction(classOf[ChartEditorActions])
  val actCoordGridB1950ShowEquator = editorForwardAction(classOf[ChartEditorActions])
  val actCoordGridEclipticShowEquator = editorForwardAction(classOf[ChartEditorActions])
  val actCoordGridGalacticShowEquator = editorForwardAction(classOf[ChartEditorActions])

  val actLimitMagCustom = editorForwardAction(classOf[ChartEditorActions])
  val actMoreStars = editorForwardAction(classOf[ChartEditorActions])
  val actLessStars = editorForwardAction(classOf[ChartEditorActions])
  val actBiggerStars = editorForwardAction(classOf[ChartEditorActions])
  val actSmallerStars = editorForwardAction(classOf[ChartEditorActions])
  val actStarReset = editorForwardAction(classOf[ChartEditorActions])

  val actShowGlobularCluster = editorForwardAction(classOf[ChartEditorActions])
  val actShowOpenCluster = editorForwardAction(classOf[ChartEditorActions])
  val actShowGalaxy = editorForwardAction(classOf[ChartEditorActions])
  val actShowBrightNebula = editorForwardAction(classOf[ChartEditorActions])
  val actShowPlanetaryNebula = editorForwardAction(classOf[ChartEditorActions])
  val actShowDarkNebula = editorForwardAction(classOf[ChartEditorActions])
  val actShowSupernovaRemnant = editorForwardAction(classOf[ChartEditorActions])

  val actShowConstelLines = editorForwardAction(classOf[ChartEditorActions])
  val actShowConstelBounds = editorForwardAction(classOf[ChartEditorActions])
  val actShowLegend = editorForwardAction(classOf[ChartEditorActions])
  val actInvertColors = editorForwardAction(classOf[ChartEditorActions])
  
  val actChartSkyview = editorForwardAction(classOf[ChartEditorActions])

  val actDSSAladinSurvey  = editorForwardAction(classOf[ChartEditorActions])
  val actMellingerAladinSurvey  = editorForwardAction(classOf[ChartEditorActions])
  val actNoneAladinSurvey  = editorForwardAction(classOf[ChartEditorActions])


}
