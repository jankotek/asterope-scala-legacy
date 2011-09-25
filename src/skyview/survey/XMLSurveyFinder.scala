package skyview.survey
import java.io.File

/** Translate a simple survey name into an XML file.
 * 
 *  Surveys can come from one source (other two were removed):
 *   <ul>
 *     <li> If the resource survey.manifest
 *      is available, each line in the manifest defines a survey XML description.
 *      As a system resource this file will be searched for in all of the
 *      places where a class might be looked for.  When SkyView is
 *      executed within a JAR file the survey manifest will be included.
 *    </ul>
 *   If a survey is defined in multiple locations, the later definition
 *   (in terms of this listing) supercedes the earlier.  Thus users
 *   can override the default definitions of surveys.
 *   
 */
object XMLSurveyFinder {
  
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
  
  lazy val surveys:Set[String] = surveyFileList.map(new File(_).getName.replaceAll("\\.xml$",""))

  
  def find(name:String) = {
    val file = findSurveyFile(name)
    new XMLSurvey(file)
  }
  
  def findSurveyFile(name:String):String = surveyFileList
		  .find(_.endsWith(name+".xml"))
		  .getOrElse(throw new IllegalArgumentException("Survey not found:"+name))
  

  
}