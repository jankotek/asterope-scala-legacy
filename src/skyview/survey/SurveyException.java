package skyview.survey;

/** This class is used for exceptions problems with
 *  the data in a survey.
 */

public class SurveyException extends Exception {

    static final long serialVersionUID = 1L;

    public SurveyException() {
    }
    
    public SurveyException(String cause) {
	super(cause);
    }
    
    public SurveyException(String cause,Throwable e) {
	super(cause,e);
    }

}
    
    
