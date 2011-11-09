package org.asterope.skyview

import org.asterope.util._
import org.apache.commons.math.geometry.Vector3D

class XmlSurveyTest extends ScalaTestCase{

  def testSurveyList(){
    assert(XmlSurvey.surveyFileList.contains("surveys/xml/dss.xml"))
  }

  def testLoadFromSurveyFile(){
    val dss = XmlSurvey.loadXmlFile("surveys/xml/dss.xml")

    assert(dss.name === "Original Digitized Sky Survey")
    assert(dss.shortNames === List("DSSOld","Digitized Sky Survey","DSS"))
    assert(dss.description.contains("exposure of the M31 region that is distributed on"))
    assert(dss.settings === Map("Scale"->"0.0004722222", "Deedger"->"skyview.process.Deedger"))
    assert(dss.metaTable("Regime") ==="Optical")
    assert(dss.metaTable("NSurvey") ==="1")
    assert(dss.fits.get.contains("SURVEY  = 'Digitized Sky Survey'"))

    assert(dss.imageFilePrefix.get === "http://skyview.gsfc.nasa.gov/surveys/dss/")
    assert(dss.imageFactory.get === "skyview.survey.DSSImageFactory")
    assert(dss.imageSize.get === 6.degree)
    assert(dss.images.contains(SurveyImageInfo("s068    235.267275   -70.161172    1976.2430")))
  }


  def testLoadAllSurveys(){
    XmlSurvey.surveyFileList.foreach{file=>
      try{
        XmlSurvey.loadXmlFile(file)
      }catch{
        case e:Exception => throw new Error("Could not parse: "+file,e)
      }
    }
  }
  
  def testFindSurveys(){
	val surv = XmlSurvey.loadXmlFile("surveys/xml/dss.xml")
    val images = surv.findCandidates(rade2Vector(0,0),5.degree)
    
    assert(images.size === 18)

  }
}