package org.asterope.gui

import javax.swing._

/**
 * Defines menu used in main window
 */
trait MainWindowMenu
  extends ChartEditorActions
  with MainWindowActions{



  lazy val fileMenu = new JMenu{
    setName("fileMenu")

  }

  lazy val navigationMenu = new JMenu(){
    setName("navigationMenu")
    add(searchObject)
    add(actRefresh)
    add(actZoomOut)
    add(actZoomIn)
    add(new JMenu {
      setName("fovMenu")
      add(actFov15m)
      add(actFov30m)
      add(actFov1d)
      add(actFov2d)
      add(actFov4d)
      add(actFov8d)
      add(actFov15d)
      add(actFov30d)
      add(actFov60d)
      add(actFov120d)
    })

    add(new JMenu() {
      setName("moveMenu")
      add(actMoveUpRight)
      add(actMoveUp)
      add(actMoveUpLeft)
      add(actMoveLeft)
      add(actMoveDownLeft)
      add(actMoveDown)
      add(actMoveDownRight)
      add(actMoveRight)

    })

  }

  lazy val viewMenu  = new JMenu() {
      setName("viewMenu")
      add(new JMenu() {
        setName("transformMenu")
        add(new JCheckBoxMenuItem(actMirrorVert));
        add(new JCheckBoxMenuItem(actMirrorHoriz));
        add(new JSeparator())
        add(actRotateLeft);
        add(actRotateRight);
        add(actRotateCustom);
        add(new JSeparator())
        add(actTransformReset);
      })
      add(new JMenu() {
        setName("coordGridMenu")
        add(new JMenu() {
          setName("coordGridJ2000Menu")
          add(new JCheckBoxMenuItem(actCoordGridJ2000ShowLines));
          add(new JCheckBoxMenuItem(actCoordGridJ2000ShowPoles));
          add(new JCheckBoxMenuItem(actCoordGridJ2000ShowEquator));
        })
        add(new JMenu() {
          setName("coordGridB1950Menu")
          add(new JCheckBoxMenuItem(actCoordGridB1950ShowLines));
          add(new JCheckBoxMenuItem(actCoordGridB1950ShowPoles));
          add(new JCheckBoxMenuItem(actCoordGridB1950ShowEquator));
        })
        add(new JMenu() {
          setName("coordGridEclipticMenu")
          add(new JCheckBoxMenuItem(actCoordGridEclipticShowLines));
          add(new JCheckBoxMenuItem(actCoordGridEclipticShowPoles));
          add(new JCheckBoxMenuItem(actCoordGridEclipticShowEquator));
        })
        add(new JMenu() {
          setName("coordGridGalacticMenu")
          add(new JCheckBoxMenuItem(actCoordGridGalacticShowLines));
          add(new JCheckBoxMenuItem(actCoordGridGalacticShowPoles));
          add(new JCheckBoxMenuItem(actCoordGridGalacticShowEquator));
        })
      })
      add(new JMenu() {
        setName("starMenu")
        add(actLimitMagCustom)
        add(actMoreStars)
        add(actLessStars)
        add(new JSeparator)
        add(actBiggerStars)
        add(actSmallerStars)
        add(new JSeparator)
        add(actStarReset)
      })
      add(new JMenu() {
        setName("deepSkyMenu")
        add(new JCheckBoxMenuItem(actShowGlobularCluster))
        add(new JCheckBoxMenuItem(actShowOpenCluster))
        add(new JCheckBoxMenuItem(actShowGalaxy))
        add(new JCheckBoxMenuItem(actShowBrightNebula))
        add(new JCheckBoxMenuItem(actShowPlanetaryNebula))
        add(new JCheckBoxMenuItem(actShowDarkNebula))
        add(new JCheckBoxMenuItem(actShowSupernovaRemnant))
      })
      add(new JMenu() {
        setName("constelMenu")
        add(new JCheckBoxMenuItem(actShowConstelBounds))
        add(new JCheckBoxMenuItem(actShowConstelLines))
      })
      add(actInvertColors)
      add(new JCheckBoxMenuItem(actShowLegend))
    }


  lazy val imageMenu  = new JMenu() {
      setName("imageMenu")
      add(new JRadioButtonMenuItem(actDSSAladinSurvey))

      add(new JRadioButtonMenuItem(actMellingerAladinSurvey))
      add(new JRadioButtonMenuItem(actNoneAladinSurvey))


      add(new JSeparator)
      add(actChartSkyview)
  }


  lazy val menu = new JMenuBar{
    add(fileMenu)
    add(navigationMenu)
    add(viewMenu)
    add(imageMenu)
  }


}