package org.asterope.chart

import skyview.ij.IJProcessor
import edu.umd.cs.piccolo.nodes.{PText, PImage}
import skyview.sampler.Sampler
import java.util.concurrent.Executors
import skyview.executive.{Val, Imager, Settings}
import org.asterope.util._
import java.net.URL
import skyview.survey.{Util, Image}

/**
 * Draws skyview object into chart
 */

class ChartSkyview extends ChartFeature[ChartSkyview.Memento]{

  def defaultConfig = new ChartSkyview.Memento

    def updateChart(chart: ChartBase, m:ChartSkyview.Memento){
    if(m.useInternalEngine)
      updateChartInternalEngine(chart,m)
    else
      updateChartOnline(chart,m)
  }

  def updateChartInternalEngine(chart: ChartBase, m:ChartSkyview.Memento){
    
    ChartSkyview.lock.synchronized{
      try{
      /**
       * initialize skyview properties
       */
       Settings.put(Val.nofits, "true");
       Settings.put(Val.Scaling, "Linear");
       Settings.put(Val.Position,"dummyStuff");
       //this will save image to memory
       Settings.put(Val.quicklook, "object");

       Settings.put(Val.Survey,m.survey)
       Settings.put(Val.Scaling,m.scale.toString)
       Settings.put(Val.LUT,m.lut);
       if(m.invert) Settings.put(Val.Invert,"true")
       else Settings.remove(Val.Invert);

       Settings.put(Val.sampler,m.resample.toString());

       val  imager = new Imager(
              chart.width, chart.height,
              chart.wcs
        );

        imager.run()

        //collect result
        assert(IJProcessor.getSavedImages != null && !IJProcessor.getSavedImages.isEmpty, "image was not generated")
        val imgp = IJProcessor.getSavedImages().get(0);
        imgp.getProcessor().scale(1d,-1d); //for some reasons it is upside down, turn it back
        val img = new PImage(imgp.getImage());
        val layer = chart.getLayer(Layer.skyview)
        val text = new PText(m.survey)
        text.setTextPaint(chart.colors.fg)
        text.setGlobalTranslation(Point2d(chart.width - text.getWidth+3,chart.height - text.getHeight+3))


        chart.executor.sync{
          layer.removeAllChildren()
          layer.addChild(img);
          layer.addChild(text)
          layer.repaint()
        }
      }finally{
        if( IJProcessor.getSavedImages!=null)
          IJProcessor.getSavedImages.clear();
        Settings.clear()
        Imager.clear();
      }
    }
  }
  
  
  protected def updateChartOnline(chart:ChartBase, m:ChartSkyview.Memento) {
      var url = "http://skyview.gsfc.nasa.gov/cgi-bin/runquery.pl?"
      url += "&position=" + chart.position.getRa.toDegree + "," + chart.position.getDe.toDegree
      url += "&pixels=" + chart.width.toInt + "," + chart.height.toInt
      url += "&Scale=" + chart.pixelAngularSize.toDegree
      url += "&projection=" + chart.projection
      url += "&survey=" + m.survey
      if(chart.rotation!=0.degree)
        url += "&rotation=" + (chart.rotation.toDegree)
      //TODO xscale and yscale, mirror reverse does not work
      if (m.scale!= null) url += "&scaling=" + m.scale
      if (m.resample != null) url += "&sampler=" + m.resample
      if (m.lut != null) url += "&lut=" + m.lut
      Settings.err.println("GET " + url)
      val con1 = new URL(url).openConnection()
      con1.setConnectTimeout(15000)
      //service output is blocked while image is processed
      //read timeout is efetively timeout for processing image
      con1.setReadTimeout(5 * 60 * 1000)
      //TODO cancel button does not work while waiting for image
      val pageContent = io.Source.fromInputStream(con1.getInputStream).getLines.mkString

      if (pageContent == null) throw new Error("SkyView page not recivied")
      val urlreg = """.+x\['_output_ql'\]='\.\./([^']+)'.+""".r;
      var urlreg(imgUrl) = pageContent
      if (imgUrl == null) throw new RuntimeException("Invalid image url: " + imgUrl)
      imgUrl = "http://skyview.gsfc.nasa.gov/" + imgUrl
      Settings.err.println("GET IMG: " + imgUrl)
      val imgp = java.awt.Toolkit.getDefaultToolkit.getImage(new URL(imgUrl))
      if (imgp == null) throw new RuntimeException("can not download image")
      chart.executor.sync{
        //TODO code duplication
        val img = new PImage(imgp);
        val layer = chart.getLayer(Layer.skyview)
        val text = new PText(m.survey)
        text.setTextPaint(chart.colors.fg)
        text.setGlobalTranslation(Point2d(chart.width - text.getWidth+3,chart.height - text.getHeight+3))

        layer.removeAllChildren()
        layer.addChild(img);
        layer.addChild(text)
        layer.repaint()
      }
    }
  

  def clearChart(chart: ChartBase){
    chart.getLayer(Layer.skyview).removeAllChildren
  }

}



object ChartSkyview{

  /**
   * Skyview uses JVM singleton to download and generate images.
   * It means that all operations needs to be synchronized on single static lock.
   */
  protected object lock


  object ScaleEnum extends Enumeration{
    val  Log,Sqrt,Linear, HistEq, LogLog = Value;
  }

  object ResampleEnum extends Enumeration{
    val NN, Lanczos = Value
  }

  case class Memento(
    survey:String ="dss2b",
    scale:ScaleEnum.Value = ScaleEnum.Sqrt,
    resample:ResampleEnum.Value =ResampleEnum.Lanczos,
    lut:String = "colortables/b-w-linear.bin",
    invert:Boolean = false,
    useInternalEngine:Boolean = true    
  )
    
  lazy val surveys:Map[String,List[(String,String,String)]] = Map(
		  //generated by org.asterope.script.SkyviewSurveysMenu2$
		  "Radio" ->List(("4850mhz","surveys/xml/4850mhz.xml","4850 MHz Survey - GB6/PMN"), ("1420mhz","surveys/xml/1420mhz.xml","Bonn 1420 MHz Survey"), ("co","surveys/xml/co.xml","CO Galactic Plane Survey"), ("nh","surveys/xml/nh.xml","Dickey and Lockman HI map"), ("first","surveys/xml/first.xml","FIRST"), ("gtee35","surveys/xml/gtee35.xml","GTEE 0035 MHz Radio survey"), ("408mhz","surveys/xml/408mhz.xml","HI All-Sky Continuum Survey"), ("nvss","surveys/xml/nvss.xml","NVSS"), ("sumss","surveys/xml/sumss.xml","Sydney University Molonglo Sky Survey"), ("vlss","surveys/xml/vlss.xml","VLA Low-frequency Sky Survey"), ("wenss","surveys/xml/wenss.xml","Westerbork Northern Sky Survey")),
		  "Infrared" ->List(("cobe","surveys/xml/cobe.xml","Cosmic Background Explorer DIRBE"), ("cobeaam","surveys/xml/cobeaam.xml","Cosmic Background Explorer DIRBE Annual Average Map"), ("cobezsma","surveys/xml/cobezsma.xml","Cosmic Background Explorer DIRBE Zodi-Subtracted Mission Average"), ("iras100","surveys/xml/iras100.xml","IRAS Sky Survey Atlas: 100 micron"), ("iras12","surveys/xml/iras12.xml","IRAS Sky Survey Atlas: 12 micron"), ("iras25","surveys/xml/iras25.xml","IRAS Sky Survey Atlas: 25 micron"), ("iras60","surveys/xml/iras60.xml","IRAS Sky Survey Atlas: 60 micron"), ("iris100","surveys/xml/iris100.xml","Improved Reprocessing of the IRAS Survey: 100"), ("iris12","surveys/xml/iris12.xml","Improved Reprocessing of the IRAS Survey: 12"), ("iris25","surveys/xml/iris25.xml","Improved Reprocessing of the IRAS Survey: 25"), ("iris60","surveys/xml/iris60.xml","Improved Reprocessing of the IRAS Survey: 60"), ("sfd100m","surveys/xml/sfd100m.xml","Schlegel, Finkbeiner and Davis 100 micron survey"), ("sfddust","surveys/xml/sfddust.xml","Schlegel, Finkbeiner and Davis Dust Survey"), ("2massh","surveys/xml/2massh.xml","Two Micron All Sky Survey (H-Band)"), ("2massj","surveys/xml/2massj.xml","Two Micron All Sky Survey (J-Band)"), ("2massk","surveys/xml/2massk.xml","Two Micron All Sky Survey (K-Band)"), ("wmapilc","surveys/xml/wmapilc.xml","WMAP Five Year Galaxy Removed"), ("wmapk","surveys/xml/wmapk.xml","WMAP Five Year K-Band"), ("wmapka","surveys/xml/wmapka.xml","WMAP Five Year Ka-Band"), ("wmapq","surveys/xml/wmapq.xml","WMAP Five Year Q-Band"), ("wmapv","surveys/xml/wmapv.xml","WMAP Five Year V-Band"), ("wmapw","surveys/xml/wmapw.xml","WMAP Five Year W-band")),
		  "Optical" ->List(("dss2b","surveys/xml/dss2b.xml","2nd Digitized Sky Survey (Blue)"), ("dss2r","surveys/xml/dss2r.xml","2nd Digitized Sky Survey (Red)"), ("dss2ir","surveys/xml/dss2ir.xml","2nd Digitized Sky Survey-Near Infrared"), ("dss1b","surveys/xml/dss1b.xml","First Digitized Sky Survey: Blue Plates"), ("dss1r","surveys/xml/dss1r.xml","First Digitized Sky Survey: Red Plates"), ("halpha","surveys/xml/halpha.xml","H-alpha Full Sky Map"), ("mellingerb","surveys/xml/mellingerb.xml","Mellinger All Sky Mosaic: Blue"), ("mellingerg","surveys/xml/mellingerg.xml","Mellinger All Sky Mosaic: Green"), ("mellingerr","surveys/xml/mellingerr.xml","Mellinger All Sky Mosaic: Red"), ("dss","surveys/xml/dss.xml","Original Digitized Sky Survey"), ("sdssg","surveys/xml/sdssg.xml","Sloan Digitized Sky Survey G-band"), ("sdssi","surveys/xml/sdssi.xml","Sloan Digitzed Sky Survey I-band"), ("sdssr","surveys/xml/sdssr.xml","Sloan Digitzed Sky Survey R-band"), ("sdssu","surveys/xml/sdssu.xml","Sloan Digitzed Sky Survey U-band"), ("sdssz","surveys/xml/sdssz.xml","Sloan Digitzed Sky Survey Z-band"), ("shassa_c","surveys/xml/shassa_c.xml","The Southern H-Alpha Sky Survey Atlas: Continuum"), ("shassa_cc","surveys/xml/shassa_cc.xml","The Southern H-Alpha Sky Survey Atlas: Continuum-Corrected"), ("shassa_h","surveys/xml/shassa_h.xml","The Southern H-Alpha Sky Survey Atlas: H-alpha"), ("shassa_sm","surveys/xml/shassa_sm.xml","The Southern H-Alpha Sky Survey Atlas: Smoothed")),
		  "Ultraviolet" ->List(("euve171","surveys/xml/euve171.xml","Extreme Ultraviolet Explorer: 171 A"), ("euve405","surveys/xml/euve405.xml","Extreme Ultraviolet Explorer: 405 A"), ("euve555","surveys/xml/euve555.xml","Extreme Ultraviolet Explorer: 555 A"), ("euve83","surveys/xml/euve83.xml","Extreme Ultraviolet Explorer: 83 A"), ("galexfar","surveys/xml/galexfar.xml","Galaxy Explorer All Sky Survey: Far UV"), ("galexnear","surveys/xml/galexnear.xml","Galaxy Explorer All Sky Survey: Near UV"), ("wfcf1","surveys/xml/wfcf1.xml","ROSAT Wide Field Camera: F1"), ("wfcf2","surveys/xml/wfcf2.xml","ROSAT Wide Field Camera: F2")),
		  "X-ray" ->List(("granat_sigma_sig","surveys/xml/granat_sigma_sig.xml","GRANAT/SIGMA"), ("granat_sigma_flux","surveys/xml/granat_sigma_flux.xml","GRANAT/SIGMA Flux"), ("heao1","surveys/xml/heao1.xml","HEAO 1A"), ("integralspi_gc","surveys/xml/integralspi_gc.xml","INTEGRAL/Spectral Imager Galactic Center Survey"), ("pspc1int","surveys/xml/pspc1int.xml","PSPC summed pointed observations, 1 degree cutoff, intensity"), ("pspc2cnt","surveys/xml/pspc2cnt.xml","PSPC summed pointed observations, 2 degree cutoff, counts"), ("pspc2exp","surveys/xml/pspc2exp.xml","PSPC summed pointed observations, 2 degree cutoff, exposure"), ("pspc2int","surveys/xml/pspc2int.xml","PSPC summed pointed observations, 2 degree cutoff, intensity"), ("rassintsb","surveys/xml/rassintsb.xml","ROSAT All-Sky Broad Band Intensity"), ("rassint","surveys/xml/rassint.xml","ROSAT All-Sky Broad Band Intenstiy"), ("rassinthb","surveys/xml/rassinthb.xml","ROSAT All-Sky Hard Band Intensity"), ("rassback1","surveys/xml/rassback1.xml","ROSAT All-Sky X-ray Background Survey: Band 1"), ("rassback2","surveys/xml/rassback2.xml","ROSAT All-Sky X-ray Background Survey: Band 2"), ("rassback3","surveys/xml/rassback3.xml","ROSAT All-Sky X-ray Background Survey: Band 3"), ("rassback4","surveys/xml/rassback4.xml","ROSAT All-Sky X-ray Background Survey: Band 4"), ("rassback5","surveys/xml/rassback5.xml","ROSAT All-Sky X-ray Background Survey: Band 5"), ("rassback6","surveys/xml/rassback6.xml","ROSAT All-Sky X-ray Background Survey: Band 6"), ("rassback7","surveys/xml/rassback7.xml","ROSAT All-Sky X-ray Background Survey: Band 7"), ("rass.25kev","surveys/xml/rass.25kev.xml","ROSAT All-Sky X-ray Survey"), ("rass1.5kev","surveys/xml/rass1.5kev.xml","ROSAT All-Sky X-ray Survey 1.5 keV"), ("rass.75kev","surveys/xml/rass.75kev.xml","ROSAT All-Sky X-ray Survey 3/4 keV"), ("rass3bb","surveys/xml/rass3bb.xml","ROSAT All-Sky X-ray Survey Broad Band"), ("rass3hb","surveys/xml/rass3hb.xml","ROSAT All-Sky X-ray Survey Hard Band"), ("rass3sb","surveys/xml/rass3sb.xml","ROSAT All-Sky X-ray Survey Soft Band"), ("hriint","surveys/xml/hriint.xml","ROSAT High Resolution Image Pointed Observations Mosaic: Intensity"), ("rxte3_20k_sig","surveys/xml/rxte3_20k_sig.xml","RXTE Allsky 3-20keV Significance"), ("rxte3_8k_sig","surveys/xml/rxte3_8k_sig.xml","RXTE Allsky 3-8keV Significance"), ("rxte8_20k_sig","surveys/xml/rxte8_20k_sig.xml","RXTE Allsky 8-20keV Significance"), ("batflux4","surveys/xml/batflux4.xml","Swift BAT All-Sky Survey: Flux 100-195 keV"), ("batflux0","surveys/xml/batflux0.xml","Swift BAT All-Sky Survey: Flux 14-195 keV"), ("batflux1","surveys/xml/batflux1.xml","Swift BAT All-Sky Survey: Flux 14-24 keV"), ("batflux2","surveys/xml/batflux2.xml","Swift BAT All-Sky Survey: Flux 24-50 keV"), ("batflux3","surveys/xml/batflux3.xml","Swift BAT All-Sky Survey: Flux 50-100 keV"), ("batsig4","surveys/xml/batsig4.xml","Swift BAT All-Sky Survey: Significance 100-195 keV"), ("batsig0","surveys/xml/batsig0.xml","Swift BAT All-Sky Survey: Significance 14-195 keV"), ("batsig1","surveys/xml/batsig1.xml","Swift BAT All-Sky Survey: Significance 14-24 keV"), ("batsig2","surveys/xml/batsig2.xml","Swift BAT All-Sky Survey: Significance 24-50 keV"), ("batsig3","surveys/xml/batsig3.xml","Swift BAT All-Sky Survey: Significance 50-100 keV")),
		  "Gamma Ray" ->List(("comptel","surveys/xml/comptel.xml","CGRO Compton Telescope: 3 channel data"), ("egret3d","surveys/xml/egret3d.xml","Energetic Gamma-Ray Event Telescope: 10 channel data"), ("egrethard","surveys/xml/egrethard.xml","Energetic Gamma-Ray Event Telescope: Hard"), ("egretsoft","surveys/xml/egretsoft.xml","Energetic Gamma-Ray Event Telescope: Soft"))
  )

}

