package org.asterope.data


import Nomenclature._
import java.io._
import jdbm.{SerializerInput, SerializerOutput}
import org.asterope.util.ScalaTestCase

class NomenclatureTest extends ScalaTestCase{

  def testParseBasics{
    assert(parse("NGC 7000") !== None)
    assert(parse("M 113") !== None)
    assert(parse("11 UMa") !== None)
    assert(parse("11 UMA") !== None)
    assert(parse("11 uma") !== None)
    assert(parse("VY uma") !== None)
    assert(parse("R UMa") !== None)
    assert(parse("SKY2000 J000000.08-052939.7") !== None)
    assert(parse("SKY2000 J201342.52-613260.0") !== None)

    assert(parse("V401 And") !== None)

  }

  def testNameSerialization{
    serAndDeser(Nomenclature.justName("Vega"))
    serAndDeser(Nomenclature.justName("Andromeda Galaxy"))
  }

  def serAndDeser(s:String){
    serAndDeser(parse(s))
  }

  def serAndDeser(nom:Nomenclature){
    val out = new ByteArrayOutputStream();

    serializer.serialize(new SerializerOutput(out),nom)
    val in = new ByteArrayInputStream(out.toByteArray)
    val nom2 = serializer.deserialize(new SerializerInput(in))
    assert(nom2 === nom)
    assert(nom2.toString === nom.toString)
  }

  def testSerialize{

    serAndDeser("NGC 7000")
    serAndDeser("NGC 800")
    serAndDeser("M 13")
    serAndDeser("11 UMa")
    serAndDeser("R UMa")
    serAndDeser("SKY2000 J000000.08-052939.7")
    serAndDeser("SKY2000 J000000.21+010520.4")

//TODO fix test case bellow, for now I just dont care    
//    serAndDeser("ZwG 123.001") //it should keep 00 if there is '.' before them
  }

  def testFormatParser{
    import FormatParser._
    val parsed = FormatParser.parseFormat("NGC NNNN")
    assert(parsed === List(FixedElem("NGC"),FixedElem("_"),NumberElem(4)))
  }

  def testSplit{
    assert(Nomenclature.split("NGC 4000; NGC 111") === List("NGC 4000","NGC 111"))
    assert(Nomenclature.split("11alpuma") === List("11 uma","alp uma"))
  }

  def testLeadingZero{
    val id = "SKY2000 J000000.21+010520.4"
    assert(Nomenclature.parse(id).toString === id)
  }

  def testCompare{
    val n1 = Nomenclature.parse("SKY2000 J000000.08-052939.7")
    val n2 = Nomenclature.parse("SKY2000 J000000.21+010520.4")
    assert(n1 !== n2)
    assert(n1 ?< n2)
  }
}