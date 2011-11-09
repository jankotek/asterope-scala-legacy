package org.asterope.skyview

import scala.xml._
import org.asterope.util._
import org.apache.commons.math.geometry.Vector3D

/**
 * Class responsible for parsing survey definition files. 
 *
 *
 * Original Skyview had various IDs for each survey (ShortName)
 * Asterope uses single id: xml survey file name. 
 */
object XmlSurvey {
  
  
  /**
   * Contains list of XML Survey files from `survey.manifest`
   */
  lazy val surveyFileList:Set[String] = {
    val manifestFile = "surveys/survey.manifest"
    val in = getClass.getClassLoader.getResourceAsStream(manifestFile)
    assert(in!=null, "Survey manifest resource not found: "+manifestFile)
	  val src = io.Source.fromInputStream(in)
	  src.getLines().map(_.trim).toSet
  }

  /**
   * Parse XML into SurveyDetail
   *
   * Original Skyview used Sax (stream XML) to parse XML files.
   * This had better performance, but was harder to code.
   * In Asterope we just load everything into DOM (biggest XML has 5 MB).
   */
  def loadXmlFile(fileName:String):SurveyDetail = {
    val in = getClass.getClassLoader.getResourceAsStream(fileName)
    assert(in!=null, "Could not find XML Survey resource"+fileName)
    val xml = XML.load(in)
    val shortNames = (xml\"ShortName").head.text.split(",").map(_.trim).toList
    val name =(xml\"Name").head.text.trim
    val desc = (xml\"Description").head.text.trim
    val fits =(xml\"FITS").headOption.map(_.text.trim)
    val settings:Map[String,String] = (xml\"Settings"\"_").map(n=>(n.label, n.text.trim)).toMap
    val metaTable:Map[String,String] = (xml\"MetaTable"\"_").map(n=>(n.label, n.text.trim)).toMap
    val imageFilePrefix = (xml\"Images"\"FilePrefix").headOption.map(_.text.trim)
    val imageFab = (xml\"Images"\"ImageFactory").headOption.map(_.text.trim)
    val imageSpellPrefix = (xml\"Images"\"SpellPrefix").headOption.map(_.text.trim)
    val imageSpellSuffix = (xml\"Images"\"SpellSuffix").headOption.map(_.text.trim)
    val imageSize = (xml\"Images"\"ImageSize").headOption.map(_.text.trim.toDouble.degree)

    val images:List[SurveyImageInfo] = (xml\"Images"\"Image").map(_.text.trim).map(SurveyImageInfo(_)).toList
    
    new SurveyDetail(shortNames=shortNames, name=name, description=desc,
      settings=settings, metaTable = metaTable, fits =fits,
      imageFilePrefix = imageFilePrefix, imageFactory=imageFab, imageSize = imageSize,
      imageSpellPrefix = imageSpellPrefix, imageSpellSuffix = imageSpellSuffix,
      images = images)
  }
  

}


case class SurveyDetail(shortNames:List[String], name:String, description:String,
    settings:Map[String,String], metaTable:Map[String,String], fits:Option[String],
    imageFilePrefix:Option[String], imageFactory:Option[String], imageSize:Option[Angle],
    imageSpellPrefix:Option[String],imageSpellSuffix:Option[String],
    images:List[SurveyImageInfo]){
  
  /** Find candidate images from this survey.*/
  def findCandidates(pos:Vector3D, size:Angle):List[SurveyImageInfo]={
    //TODO there is 'LargeImage' setting which completely changes behaviour, implement it!
    val distance:Double =  size.toRadian + imageSize.map(_.toRadian).getOrElse(0.0)
    
	images.filter(c=>Vector3D.angle(c.pos,pos) <= distance)
  }

}

case class SurveyImageInfo(content:String){
  val pos:Vector3D = {
    val split = content.split("[ ]+")
    val ra = split(1).toDouble.degree
    val de = split(2).toDouble.degree
    rade2Vector(ra,de)
   
  }
  
  def file = content.split("[ ]+")(0)
}