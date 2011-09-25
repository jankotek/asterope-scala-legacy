package skyview.survey;

/** This class is used to terminate the parsing of
 *  of XML file (typically a survey description file).
 *  Since this is the only way to do this, and it is
 *  a normal path, we do not call use Exception in the name.
 *  This class must extend either RuntimeException or
 *  Error since it will be thrown in methods that implement
 *  interfaces that do not permit checked exceptions.
 */
public class ParsingTermination extends RuntimeException {
    private static final long serialVersionUID = 1L;
}
