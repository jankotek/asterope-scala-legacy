package org.asterope.util

/**
 * Enumeration of greek letters and some additional utilities
 * 
 * @author Jan Kotek
 */
object GreekLetter extends Enumeration{
	
	  val Alpha, Beta, Gamma, Delta, Epsilon, Zeta, Eta, Theta, Iota, Kappa, Lambda, Mu,
      	Nu, Xi, Omicron, Pi, Rho, Sigma, Tau, Upsilon, Phi, Chi, Psi, Omega = Value;

	
	  /** @return capital character for given value */ 
	  def capitalGreekLetter(name:Value):Char = name match{	 	  
		case  Alpha => '\u0391'
		case  Beta => '\u0392'
		case  Gamma => '\u0393'
		case  Delta => '\u0394'
		case  Epsilon => '\u0395'
		case  Zeta => '\u0396'
		case  Eta => '\u0397'
		case  Theta => '\u0398'
		case  Iota => '\u0399'
		case  Kappa => '\u039A'
		case  Lambda => '\u039B'
		case  Mu => '\u039C'
		case  Nu => '\u039D'
		case  Xi => '\u039E'
		case  Omicron => '\u039F'
		case  Pi => '\u03A0'
		case  Rho => '\u03A1'
		case  Sigma => '\u03A3'
		case  Tau => '\u03A4'
		case  Upsilon => '\u03A5'
		case  Phi => '\u03A6'
		case  Chi => '\u03A7'
		case  Psi => '\u03A8'
		case  Omega => '\u03A9'
	  }
	   
	  /** @return lower case character for given value */ 
	  def smallGreekLetter(name:Value):Char = name match{	 	  
		case  Alpha => '\u03B1'
		case  Beta => '\u03B2'
		case  Gamma => '\u03B3'
		case  Delta => '\u03B4'
		case  Epsilon => '\u03B5'
		case  Zeta => '\u03B6'
		case  Eta => '\u03B7'
		case  Theta => '\u03B8'
		case  Iota => '\u03B9'
		case  Kappa => '\u03BA'
		case  Lambda => '\u03BB'
		case  Mu => '\u03BC'
		case  Nu => '\u03BD'
		case  Xi => '\u03BE'
		case  Omicron => '\u03BF'
		case  Pi => '\u03C0'
		case  Rho => '\u03C1'
		case  Sigma => '\u03C3'
		case  Tau => '\u03C4'
		case  Upsilon => '\u03C5'
		case  Phi => '\u03C6'
		case  Chi => '\u03C7'
		case  Psi => '\u03C8'
		case  Omega => '\u03C9'
	  }
	   
	val  threeLetterRegularExp:String = {
		 var ret = ""
		 values.map(_.toString).foreach{s=>
		 	ret+=s+"|"
      val short = s.substring(0,math.min(3,s.size))
      if(short!=s)
        ret+=short+"|"
		 }    
     ret+=ret.toLowerCase+ret.toUpperCase
     values.foreach{s:Value=>
       ret+=smallGreekLetter(s)+"|"
     }      
		 ret = ret.substring(0,ret.length-1)
		 ret
	}
    /** try to convert greek letter name from short form to full name, or fail */
    def completeName(shortName:String):GreekLetter.Value = {
    	values.foreach{l:Value=>
            if(l.toString.toLowerCase().startsWith(shortName.toLowerCase())
            		|| l.toString.equalsIgnoreCase(shortName)
                || smallGreekLetter(l).toString == shortName
                || capitalGreekLetter(l).toString == shortName)
                return l;
        }
        throw new IllegalArgumentException("Not found greek letter for "+shortName);
    }
}