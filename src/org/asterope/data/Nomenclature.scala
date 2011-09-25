package org.asterope.data

import java.lang.{Long => JLong}

import java.util.ArrayList
import jdbm._
import org.asterope.util._
import scala.collection.JavaConversions._
import collection.mutable.ListBuffer
import util.parsing.combinator.RegexParsers

import java.io.{ByteArrayInputStream, ByteArrayOutputStream}

case class Nomenclature( v:String, formatId:Int )
        extends Ordered[Nomenclature]{
  override def toString = v

  /**
   * nomenclature is sorted in space and case insensitive way
   * It makes it easier to find ID in sorted set
   *
   * Is lazy to save CPU  on deserialization
   */
  protected[data] lazy val comparableForm = toString.replace(" ","").toUpperCase

	override def compare(n2:Nomenclature) =
    comparableForm.compareToIgnoreCase(n2.comparableForm)

	override def equals(n2:Any) =
		n2.isInstanceOf[Nomenclature] && n2.asInstanceOf[Nomenclature].comparableForm == comparableForm

  def catalogPrefix = toString.split(" ")(0)

  //TODO formatId should be protected
  //TODO add some assertions if format is defined in nomenclature parser
}

/**
 * Class responsible for parsing and handling object names (such as NGC 7000 or M13).
 * There is no 'standart', Asterope follows syntax used in Simbad,
 * more detailed in here:
 *      http://cds.u-strasbg.fr/Dic/formats.htx
 *      http://cds.u-strasbg.fr/cgi-bin/Dic-Simbad?NGC
 */
object Nomenclature  {

  /**
   * Map of known formarts. Key is ID used in JDBM storage,
   * value is formart itself.
   * Never ever change already used id or format, as it would ruin
   * store integrity.
   *
   * One catalog can have more than one format,
   * for example some parts are optional
   * Parser does not have 'optional components', so you have to use
   * new id and format.
   *
   * Checkout http://cds.u-strasbg.fr/cgi-bin/Dic-Simbad
   * if you would like to add new format
   *
   */
  protected val formats = Map[Int,String] (
    //0 is reserved for names without format
    1 ->"M NNN",
    2 ->"NGC NNNN",
    3 ->"NGC NNNNA",
    4 ->"IC NNNN",
    5 ->"UGC NNNNN",
    6 ->"MCG +FF-FF-NNNN",
    7 ->"MCG +FF-FF-NNNNA",
    8 ->"LEDA NNNNNNN", //TODO PGC and LEDA are equal, unifi them
    9 ->"PGC NNNNNNN",
    10 ->"ACO NNNN",  //abell cluster of galaxies
    11 ->"ESO FFF-NNNN",
    12 ->"ESO FFF-NNNNA",
    13 ->"SAO NNNNNN",
    14 ->"IRAS HHMM+DDMM",
    15 ->"IRAS FFFFF+DDMM",
    16 ->"SKY2000 JHHMMSS.ss+DDMMSS.s",
    17 ->"HD NNNNNN",
    18 ->"PPM NNNNNN",
    19 ->"WDS HHMMm+DDMM", //NOTE WDS does not seems to be part of Vizier
    20 ->"HIP NNNNNN",
    21 ->"HIP NNNNNNA",
    22 ->"TYC FFFF-NNNNN-N",
    23 ->"UCAC3 FFF-NNNNNN",

    68 ->"GN CCC", // greek letters from bayer
    69 ->"G CCC", // greek letters from bayer
    70 ->"NNN CCC", // bright star from flamsteed
    71 ->"AA CCC", // variable stars
    72 ->"A CCC", // variable stars
    73 ->"a CCC", // variable stars, TODO strict parsing should be in this case
    74 ->"ANNNN CCC", // variable stars
    75 ->"NSV NNNNNN", // variable stars

    100 ->"BD+DD NNNNN", //durchmunsterung
    101 ->"SD+DD NNNNN", //durchmunsterung
    102 ->"CD+DD NNNNN", //durchmunsterung
    103 ->"CP+DD NNNNN", //durchmunsterung
    104 ->"AG+DD NNNNN", //durchmunsterung
    105 ->"BD+DD NNNNNa", //durchmunsterung
    106 ->"SD+DD NNNNNa", //durchmunsterung
    107 ->"CD+DD NNNNNa", //durchmunsterung
    108 ->"CP+DD NNNNNa", //durchmunsterung
    109 ->"AG+DD NNNNNa", //durchmunsterung

    117 ->"HR NNNNNN", //harward revisited

	

    999-> "VV N-NNN",  
    1000 -> "VV NNN",
    1001 -> "ZwG NNN.NNN",
    1002 -> "NPM1G +NN NNN",
    1003 -> "AND R",
    1004 -> "APG NNN",
    1005 -> "APG NNNA",
    1006 -> "Mrk NNNN",
    1007 -> "OCL NNNN",
    1008 -> "PK NNN+NN.N", //TODO BB galactic long should be here
    1009 -> "Vy N-N",
    1010 -> "Hen N-NNN",
    1011 -> "Av-Hunter N", //WTF is this cat?
    1012 -> "GCL NNN",
    1013 -> "Berkley NNN",
    1014 -> "LDN NNNN",
    1015 -> "AP N-NN",
    1016 -> "Sh N-NNN",
    1017 -> "Czernik NN",
    1018 -> "Na N",
    1019 -> "King NN",
    1020 -> "Do NNNNN",
    1021 -> "PB NN",
    1022 -> "Cr NNNN", //no idea what this cat is, maybe Cerro Chile..somethink
    1023 -> "Me N-N",
    1024 -> "Pal NN",
    1025 -> "RAFGL NNNN",
    1026 -> "Ru NNN", //no idea about this cat
    1027 -> "Lynga NN",
    1028 -> "Hogg NNN",
    1029 -> "Harvard NN",

    1032 -> "Dunlop NNN", //no idea
    1033 -> "Lac R-NN", //lacaile
    1034 -> "Sp N", //Shapley
    1035 -> "DoDz NN", 
    1036 -> "Latysev N",
    1037 -> "LBN NNNN",
    1038 -> "SS NNN",
    1039 -> "Sh N-NNN",
    1040 -> "Mel NNN",
    1041 -> "Stock NN",
    1042 -> "3C NNN",
    1043 -> "3C NNN.N",
    1044 -> "Tombaugh N",
    1045 -> "Kemble N", //no idea
    1046 -> "Mayer N", //no idea
    1047 -> "Bochum NN", //no idea
    1048 -> "Hf NN",
    1049 -> "Trumpler NN",
    1050 -> "Cederblad NNN",
    1051 -> "Cederblad NNNA",
    1052 -> "RCW NNN",
    1053 -> "RCW NNNA",
    1055 -> "Danks N",
    1056 -> "UGCA NNN",
    1057 -> "Haffner NN",
    1058 -> "Pe N-NN",
    1059 -> "Graham N", //no idea
    1060 -> "Biur NN", //no idea
    1061 -> "Gum NN", //no idea
    1062 -> "Sher N",
    1063 -> "Pismis NN",
    1064 -> "Fein N",
    1065 -> "RCW NNN",
    1066 -> "BV N",
    1067 -> "DDO N",
    1068 -> "Loden NNN",
    1069 -> "H N-NN",
    1070 -> "Hb NN",
    1071 -> "Basel NNN",
    1072 -> "Basel NNNA",
    1073 -> "Barnard NNN",
    1074 -> "Barnard NNNA",
    1075 -> "Maffei N",
    1076 -> "Frolov N",
    1077 -> "Fleming N",
    1078 -> "Be NNN",
    1079 -> "KAZ NNN",
    1080 -> "VdBH NNN",
    1081 -> "VdBH NNNA",
    1082 -> "VdB NNN",
    1083 -> "VdBH NNNA",
    1084 -> "KUG HHMM+DDd",      
    1085 -> "Sa NNN",
    1086 -> "Auner N",
    1087 -> "Holmberg R",
    1088 -> "Lo NN",
    1089 -> "SL NN",
    1090 -> "Upgren NN",
    1091 -> "PC NN",
    1092 -> "PC NN",
    1093 -> "PNG NNN.N+NN.N", //TODO format is wrong checkout http://cds.u-strasbg.fr/cgi-bin/Dic-Simbad?PN
    1094 -> "Roslund N",
    1095 -> "Ho NNN",
    1096 -> "Ho NNNA",
    1097 -> "Bark N", //No idea
    1098 -> "LeGentil N",
    1099 -> "FCC NNN",
    1100 -> "Jonckheere NNNN",
    1101 -> "San N-N",
    1102 -> "Arp-Madore N",
    1103 -> "GC NNNNN",
    1104 -> "Mz N",
    1105 -> "Z FFF-NNN",
    1106 -> "Fath NNN",
    1107 -> "Steph N",
    1108 -> "Moffat N",
    1109 -> "HP N", //unknown cat
    1110 -> "Th N-NN",
    1111 -> "DG NNN",
    1112 -> "Lund NNNN", //unknown cat
    1113 -> "HaTr N",
    1114 -> "KPG NNN",
    1115 -> "KPG NNNA",
    1116 -> "Jn N",
    1117 -> "Kr N-N",
    1118 -> "HCG NNN",
    1119 -> "HCG NNNA",
    1120 -> "Blanco N",
    1121 -> "Vd N",
    1122 -> "Hav-Moffat N",
    1123 -> "Terzan NN",
    1124 -> "Antalova N", //unknown cat
    1125 -> "Ton NNN", 
    1126 -> "Hf N-NN",
    1127 -> "Apriamaswili N",
    1128 -> "Sn N",
    1129 -> "Archinal N",
    1130 -> "YM NN",
    1131 -> "Djorg N",
    1132 -> "SwSt N",
    1133 -> "DeHt N",
    1134 -> "Ba N",
    1135 -> "Simeis NNN",
    1136 -> "Winnecke N",
    1137 -> "Waterloo N",
    1138 -> "Ve N-NN",
    1139 -> "My NNN",
    1140 -> "VCC NNNN",
    1141 -> "SZ R NNN",
	 
    999999 -> "SAWDADWADWADWAD N" //not really catalog, just lazy to handle comma at end of list
  )


  def formatIdByPrefix(prefix:String):Int = {
     val c = Nomenclature.formats.filter(_._2.startsWith(prefix))
     assert(c.size == 1,"more than one/zero candidate found for '"+prefix+"'")
     c.first._1
  }

  def formatIdByExact(str:String):Int = {
     val c = Nomenclature.formats.filter(_._2 == str)
     assert(c.size == 1,"more than one/zero candidate found for '"+str+"'")
     c.first._1
  }

  

  /**
   * Parses nomenclature formarts such as 'NGC NNNNA'.
   * It uses Scala grammar library.
   *
   * Should be private, but protected is for testing
   */
  protected[data] object FormatParser extends RegexParsers{

    /**
     * Formarts parsed into List of FormatElem,
     * used for serialization
     */
    lazy val formatElems:Map[Int,List[FormatElem]] = {
        formats.map{case(id,format)=>
          (id->parseFormat(format))
        }
    }


    /**
     * Grammars for each format
     */
    lazy val formatGrammars:Map[Int,Parser[Any]] =
        formatElems.map{case(id,format)=>
          val parsers = format.map(_.parser)
          def recursive(l:List[Parser[Any]]):Parser[Any] =
              if(l.tail == Nil) l.head
              else l.head~recursive(l.tail)
          val grammar:Parser[Any] = recursive(parsers)                    
          (id->grammar)
        }



    /**
     * Base element in nomenclature format.
     * It have two responsibilites:
     *   1) parser which tokenizes name string (such as 'NGC 7000')
     *   2) serialize its token into store (for '7000' it writtes an Int'
     *
     * Lot of tokens does not need to be serialized,
     * beacuse thei are already defined in nomenclature format
     * (such as 'NGC ' prefix)
     *
     */
    protected trait FormatElem extends Serializer[String] {
      def parser:Parser[String]
    }

    /**
     * Represents positive number, is written as packedInt
     */
    case class NumberElem(len:Int) extends FormatElem{
      def parser = ("[0-9]{1,"+len+"}").r
      def deserialize(in:SerializerInput) = in.readPackedInt.toString
      def serialize(out:SerializerOutput,s:String) = out.writePackedInt(s.toInt)
    }

    /**
     * Number in roman numerals
     * //TODO currently only up to 10
     */
    case class RomanNumberElem(len:Int) extends FormatElem{
      //def parser = ("(I|II|III|IV|V|VI|VII|VIII|IX|X)").r
      def parser = "[IVX]+".r
      def deserialize(in:SerializerInput) = in.readUTF
      def serialize(out:SerializerOutput,s:String) = out.writeUTF(s)
    }

    /**
      * Represents positive number, is written as packedInt
      */
     case class DotElem() extends FormatElem{
       def parser = "."
       def deserialize(in:SerializerInput) = "."
       def serialize(out:SerializerOutput,s:String) = Unit
     }


    /**
     * Represents required +- sign
     */
    case class SignElem() extends FormatElem{
      def parser = "[+-]{1}".r
      def deserialize(in:SerializerInput) = if(in.readBoolean)"+"else"-"
      def serialize(out:SerializerOutput,s:String) = out.writeBoolean(s=="+")      
    }

    /**
     * Upper case text with fixed width
     */
    case class UpperCaseElem(len:Int) extends FormatElem{
      def parser = ("[A-Z]{"+len+"}").r
      def deserialize(in:SerializerInput) = in.readUTF
      def serialize(out:SerializerOutput,s:String) = {
        assert(s.size == len)
        out.writeUTF(s)  //TODO more efficient space usage than UTF
      }
    }

    /**
     * lower case text with fixed width
     */
    case class LowerCaseElem(len:Int) extends FormatElem{
      def parser = ("[a-z]{"+len+"}").r
      def deserialize(in:SerializerInput) = in.readUTF
      def serialize(out:SerializerOutput,s:String) = {
        assert(s.size == len)
        out.writeUTF(s)  //TODO more efficient space usage than UTF
      }
    }

    /**
     * Text with fixed width
     */
    case class TextElem(len:Int) extends FormatElem{
      def parser = ("[A-Za-z]{"+len+"}").r
      def deserialize(in:SerializerInput) = in.readUTF
      def serialize(out:SerializerOutput,s:String) = {
        assert(s.size == len)
        out.writeUTF(s)  //TODO more efficient space usage than UTF
      }
    }

    /** hours in RA */
    case class RaHourElem() extends FormatElem{      
      def parser = ("""[0-2]{1}[0-9]{1}""").r
      def deserialize(in:SerializerInput) = addZeros(2,in.readByte)
      def serialize(out:SerializerOutput,s:String) = {out.writeByte(s.toInt)}
    }

    case class MinuteElem() extends FormatElem{
      def parser = ("""[0-9]{2}""").r
      def deserialize(in:SerializerInput) = addZeros(2,in.readByte)
      def serialize(out:SerializerOutput,s:String) = {out.writeByte(s.toInt)}
    }

    case class SecondElem() extends FormatElem{
      def parser = ("""[0-9]{2}""").r
      def deserialize(in:SerializerInput) = addZeros(2,in.readByte)
      def serialize(out:SerializerOutput,s:String) = {out.writeByte(s.toInt)}
    }


    case class SecondElem2(len:Int) extends FormatElem{    //TODO replace with fixed size int
      def parser = ("[0-9]{"+len+"}").r
      def deserialize(in:SerializerInput) = addZeros(len,in.readPackedInt)
      def serialize(out:SerializerOutput,s:String) = {out.writePackedInt(s.toInt)}
    }


    /** Degrees in declination */
    case class DeDegreeElem(places:Int) extends FormatElem{
      //NOTE d can have decimal multiplyer, so d can be for example 3600
      def parser = ("[0-9]{"+places+"}").r
      def deserialize(in:SerializerInput) = addZeros(places,in.readPackedInt)
      def serialize(out:SerializerOutput,s:String) = {out.writePackedInt(s.toInt)}
    }

    /**
     * String which does not vary and is not serialized.
     * For example 'NGC ' prefix or '-' separators between numbers.
     */
    case class FixedElem(v:String) extends FormatElem{
      assert(!v.contains(" "),"replace space with _ first")
      def parser = v 
      def deserialize(in:SerializerInput) = v.replace('_',' ')
      def serialize(out:SerializerOutput,s:String) = Unit
    }

    case class ConstelElem() extends FormatElem{
      def parser = "[A-Za-z]{3}".r
      def deserialize(in:SerializerInput) = Constel(in.readByte()).toString
      def serialize(out:SerializerOutput,s:String) = {
         val id = Constel.fromLowerCase(s).get.id;
         out.writeByte(id)
      }
    }

    case class GreekLetterElem() extends FormatElem{
      def parser = GreekLetter.threeLetterRegularExp.r
      def deserialize(in:SerializerInput) = GreekLetter.smallGreekLetter(GreekLetter(in.readByte())).toString
      def serialize(out:SerializerOutput,s:String) = {
         val id = GreekLetter.completeName(s).id;
         out.writeByte(id)
      }

    }
    /*   GRAMMAR DEFINITION FOR FORMAT */
    val number:Parser[FormatElem] = """[N]+""".r ^^ (s=>NumberElem(s.size))
    val romanNumber:Parser[FormatElem] = """[R]+""".r ^^ (s=>RomanNumberElem(s.size))
    val field:Parser[FormatElem] = """[F]+""".r ^^ (s=>NumberElem(s.size)) //TODO does field have leading zeros?
    val sign:Parser[FormatElem] = "+" ^^ (s=>SignElem())
    val dot:Parser[FormatElem] = "." ^^ (s=>DotElem())
    val space:Parser[FormatElem] = "_" ^^ (s=>FixedElem(s))
    val julianPos:Parser[FormatElem] = "J" ^^ (s=>FixedElem(s))
    val minus:Parser[FormatElem] = "-" ^^ (s=>FixedElem(s))
    val upperCase:Parser[FormatElem] = "[A]+".r ^^ (s=>UpperCaseElem(s.size))
    val lowerCase:Parser[FormatElem] = "[a]+".r ^^ (s=>LowerCaseElem(s.size))
    val raHour:Parser[FormatElem] = "HH" ^^ (s=>RaHourElem())
    val minute:Parser[FormatElem] = "MM" ^^ (s=>MinuteElem())
    val minute2:Parser[FormatElem] = "[m]+".r ^^ (s=>SecondElem2(s.size))
    val second:Parser[FormatElem] = "SS" ^^ (s=>SecondElem())
    val second2:Parser[FormatElem] = "[s]+".r ^^ (s=>SecondElem2(s.size))
    val deDegree:Parser[FormatElem] = """[D]{1,3}[d]{0,5}""".r ^^ (s=>DeDegreeElem(s.size))
    val text:Parser[FormatElem] = "[a]+".r ^^ (s=>TextElem(s.size))
    val constel:Parser[FormatElem] = "CCC" ^^ (s=>ConstelElem())
    val greekLetter:Parser[FormatElem] = "G" ^^ (s=>GreekLetterElem())
    /* fixed prefix to identify catalog*/
    val prefix:Parser[FormatElem] = "[A-Za-z0-9]{1}[0-9A-Za-z-]*".r ^^ (s=>FixedElem(s))

    /**
     * Main parser which combines all subparsers. 
     *
     */
    val repeat = rep(space|number|field|romanNumber|raHour|minute|minute2|deDegree|upperCase|lowerCase|text|sign|dot|minus|constel
                |second|second2|julianPos|greekLetter)
    val withPrefix:Parser[List[FormatElem]] = (opt(prefix) ~ repeat) ^^ {
      case Some(prefix) ~ l => prefix :: l
      case l:List[FormatElem] =>l
    }



    /**
     * Main method in nomenclature format parser.
     * From format it makes list of FormatElem
     *
     */
    def parseFormat(catFormat:String):List[FormatElem] = {
      try{
        val grammar = if(catFormat.endsWith("CCC")) repeat else withPrefix        
	//NOTE: catalog format requires strict parsing of spaces,
	//      so it is replaced with '_' during parsing
        parseAll(grammar,catFormat.replace(' ','_')).get
      }catch{
        case e:RuntimeException =>
          throw new RuntimeException("Could not parse format '"+catFormat+"'",e)
      }
    }

    def parseNomenclature(name:String,id:Int): Option[Nomenclature] = {
      val name2 = name.replace(' ','_');
      val grammar = formatGrammars(id);

      if(!parseAll(grammar, name2).isEmpty)
        Some(new Nomenclature(name,id))
      else
        None
    }


    def parseNomenclature(name:String): Option[Nomenclature] = {
      val name2 = name.replace(' ','_');
      formatGrammars.foreach{case(id,grammar) =>
          if(!parseAll(grammar, name2).isEmpty)
              return Some(new Nomenclature(name,id))
      }
      None
    }
  }

  object serializer extends Serializer[Nomenclature]{
    override def  deserialize(in:SerializerInput):Nomenclature = {
      val id = in.readPackedInt
      if(id == 0){
        return new Nomenclature(in.readUTF,id)
      }
      val elems = FormatParser.formatElems(id)
      var sb = new StringBuilder();
      elems.foreach{e=>
        sb = sb.append(e.deserialize(in))
      }
      return new Nomenclature(sb.toString,id)
    }
    override def serialize(out:SerializerOutput,nom:Nomenclature ){
      val id = nom.formatId;

      out.writePackedInt(id)

      if(id == 0){
        //0 is exception for Names which does not have fixed format
        //write as normal string
        out.writeUTF(nom.v)
        return
      }

      val elems = FormatParser.formatElems(id)
      val grammar = FormatParser.formatGrammars(id)

      var str = nom.v.replace(' ','_')

      elems.foreach{n=>
        val result = FormatParser.parse(n.parser,  str).get.replace('_',' ')
        n.serialize(out,result)
        //remove already consumed chunk
        str = str.drop(result.size)
      }

      //entire string should be consumed
      assert(str == "","string not fully consumed :"+nom+", rem:'"+str+"', format: "+elems)
    }
  }


  object arrayListSerializer extends Serializer[ArrayList[Nomenclature]]{
    override def  deserialize(in:SerializerInput):ArrayList[Nomenclature] = {
      val ret = new ArrayList[Nomenclature](in.readPackedInt)
      for(i <-0 until ret.size){
        ret(i) = serializer.deserialize(in)
      }
      ret
    }
    override def serialize(out:SerializerOutput,nom:ArrayList[Nomenclature]){
      out.writePackedInt(nom.size)
      nom.foreach{n=>serializer.serialize(out,n)}
    }
  }

  object listSerializer extends Serializer[List[Nomenclature]]{
    override def  deserialize(in:SerializerInput):List[Nomenclature] = {
      val l = ListBuffer[Nomenclature]()
      val size = in.readPackedInt
      for(i <-0 until size){
         l += serializer.deserialize(in)
      }
      l.toList
    }
    override def serialize(out:SerializerOutput,nom:List[Nomenclature]){
      out.writePackedInt(nom.size)
      nom.foreach{n=>serializer.serialize(out,n)}
    }
  }

  def parseWithID(v:String,id:Int):Nomenclature = {
    val ret = FormatParser.parseNomenclature(v,id).getOrElse{
      throw new Error("Nomenclature could not be parsed: '"+v+"'")
    }
    //make sure that nomenclature does not change during serialization
    normalize(ret)
  }

  def parse(v:String):Nomenclature = {
    val ret = FormatParser.parseNomenclature(v).getOrElse{
      throw new Error("Nomenclature could not be parsed: '"+v+"'")
    }
    //make sure that nomenclature does not change during serialization
    normalize(ret)
  }

  /**
   * Returns nomenclature which represents common name such as 'Vega' or 'Andromeda Galaxy'.
   * This nomenclature does not have format and is treated as string.
   */
  def justName(name:String):Nomenclature = {
    new Nomenclature(name,0)
  } 

  private def addZeros(numberOfZeros:Int, i:Int):String = {
	//TODO move to utils
   	var ret = i.toString;
   	while(ret.size<numberOfZeros){
   		ret = "0"+ret;
	}
	return ret;

  }

  //TODO remove
  private val bayerFlamstedRegExp = ("([0-9]*)(["
				+GreekLetter.threeLetterRegularExp
				+"]*)[ ]*([0-9]*)[ ]*("
				+Constel.abreviationRegExp+")").r
  //TODO remove
  def split(str:String):List[String] = {    
    val ret = new ListBuffer[String]
    val vals = str.trim().replaceAll("[\\ \\t]+"," ");
    vals.split(";").foreach{v=>
      if(bayerFlamstedRegExp.pattern.matcher(v).matches){        
        //TODO sky2000 specific hack
        val bayerFlamstedRegExp(flamsteed,bayer,component,const) = v;
        if(flamsteed!="")
          ret += (flamsteed+" "+const)
        if(bayer!="")
          ret += (bayer+component+" "+const)
      }else
        ret+=v;
    }
    ret.filter(!_.contains("*"))
       .filter(!_.contains("?"))
       .map(_.trim())
       .filter(_!="")
       .toList

  }

  def normalize(nom:Nomenclature):Nomenclature = {
    val out = new ByteArrayOutputStream();
    serializer.serialize(new SerializerOutput(out),nom)
    val in = new ByteArrayInputStream(out.toByteArray)
    serializer.deserialize(new SerializerInput(in))
  }


  def recidsByMame(name2:String, map:java.util.SortedMap[Nomenclature,java.lang.Iterable[JLong]]):Iterator[JLong]={
    val nameNom = new Nomenclature(name2,-1)
		val name = nameNom.comparableForm
		if(!name2.endsWith("%")){

			val ret = map.get(nameNom)
			if(ret == null) Nil.iterator
			else ret.iterator
		}else{
      assert(name.endsWith("%"))
			val namePrefix = name.substring(0, name.length -1)
			val name2 = Nomenclature.parse(namePrefix);
			//get subset which start at given prefix
			val subIter = map.tailMap(name2).entrySet.iterator;

			subIter
				.takeWhile(_.getKey.comparableForm.startsWith(namePrefix))
				.flatMap(_.getValue.iterator) //translate from Map.Entry to bunch of Longs
		}
  }

}

trait HasNomenclature{
  def names:List[Nomenclature]
}
