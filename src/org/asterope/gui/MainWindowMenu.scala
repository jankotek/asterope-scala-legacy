package org.asterope.gui

import javax.swing._

/**
 * Defines menu used in main window
 */
class MainWindowMenu(a:MainWindowActions,mainWin:MainWindow){


  mainWin.resmap.injectActionFields(a)

  lazy val fileMenu = new JMenu{
    setName("fileMenu")

  }

  lazy val navigationMenu = new JMenu(){
    setName("navigationMenu")
    add(a.searchObject)
    add(a.actRefresh)
    add(a.actZoomOut)
    add(a.actZoomIn)
    add(new  JMenu {
      setName("fovMenu")
      add(a.actFov15m)
      add(a.actFov30m)
      add(a.actFov1d)
      add(a.actFov2d)
      add(a.actFov4d)
      add(a.actFov8d)
      add(a.actFov15d)
      add(a.actFov30d)
      add(a.actFov60d)
      add(a.actFov120d)
    })

    add(new  JMenu() {
      setName("moveMenu")
      add(a.actMoveUpRight)
      add(a.actMoveUp)
      add(a.actMoveUpLeft)
      add(a.actMoveLeft)
      add(a.actMoveDownLeft)
      add(a.actMoveDown)
      add(a.actMoveDownRight)
      add(a.actMoveRight)

    })

  }

  lazy val viewMenu  = new JMenu() {
      setName("viewMenu")
      add(new  JMenu() {
        setName("transformMenu")
        add(new  JCheckBoxMenuItem(a.actMirrorVert));
        add(new  JCheckBoxMenuItem(a.actMirrorHoriz));
        add(new  JSeparator())
        add(a.actRotateLeft);
        add(a.actRotateRight);
        add(a.actRotateCustom);
        add(new  JSeparator())
        add(a.actTransformReset);
      })
      add(new  JMenu() {
        setName("coordGridMenu")
        add(new  JMenu() {
          setName("coordGridJ2000Menu")
          add(new  JCheckBoxMenuItem(a.actCoordGridJ2000ShowLines));
          add(new  JCheckBoxMenuItem(a.actCoordGridJ2000ShowPoles));
          add(new  JCheckBoxMenuItem(a.actCoordGridJ2000ShowEquator));
        })
        add(new  JMenu() {
          setName("coordGridB1950Menu")
          add(new  JCheckBoxMenuItem(a.actCoordGridB1950ShowLines));
          add(new  JCheckBoxMenuItem(a.actCoordGridB1950ShowPoles));
          add(new  JCheckBoxMenuItem(a.actCoordGridB1950ShowEquator));
        })
        add(new  JMenu() {
          setName("coordGridEclipticMenu")
          add(new  JCheckBoxMenuItem(a.actCoordGridEclipticShowLines));
          add(new  JCheckBoxMenuItem(a.actCoordGridEclipticShowPoles));
          add(new  JCheckBoxMenuItem(a.actCoordGridEclipticShowEquator));
        })
        add(new  JMenu() {
          setName("coordGridGalacticMenu")
          add(new  JCheckBoxMenuItem(a.actCoordGridGalacticShowLines));
          add(new  JCheckBoxMenuItem(a.actCoordGridGalacticShowPoles));
          add(new  JCheckBoxMenuItem(a.actCoordGridGalacticShowEquator));
        })
      })
      add(new  JMenu() {
        setName("starMenu")
        add(a.actLimitMagCustom)
        add(a.actMoreStars)
        add(a.actLessStars)
        add(new  JSeparator)
        add(a.actBiggerStars)
        add(a.actSmallerStars)
        add(new  JSeparator)
        add(a.actStarReset)
      })
      add(new  JMenu() {
        setName("deepSkyMenu")
        add(new  JCheckBoxMenuItem(a.actShowGlobularCluster))
        add(new  JCheckBoxMenuItem(a.actShowOpenCluster))
        add(new  JCheckBoxMenuItem(a.actShowGalaxy))
        add(new  JCheckBoxMenuItem(a.actShowBrightNebula))
        add(new  JCheckBoxMenuItem(a.actShowPlanetaryNebula))
        add(new  JCheckBoxMenuItem(a.actShowDarkNebula))
        add(new  JCheckBoxMenuItem(a.actShowSupernovaRemnant))
      })
      add(new  JMenu() {
        setName("constelMenu")
        add(new  JCheckBoxMenuItem(a.actShowConstelBounds))
        add(new  JCheckBoxMenuItem(a.actShowConstelLines))
      })
      add(a.actInvertColors)
      add(new  JCheckBoxMenuItem(a.actShowLegend))
    }


  lazy val imageMenu  = new JMenu() {
      setName("imageMenu")
      add(new  JRadioButtonMenuItem(a.actDSSAllSkySurvey))

      add(new  JRadioButtonMenuItem(a.actMellingerAllSkySurvey))
      add(new  JRadioButtonMenuItem(a.actNoneAllSkySurvey))


      add(new  JSeparator)
      add(a.actChartSkyview)
  }


  mainWin.menu.add(fileMenu)
  mainWin.menu.add(navigationMenu)
  mainWin.menu.add(viewMenu)
  mainWin.menu.add(imageMenu)


}