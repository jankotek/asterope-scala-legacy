package org.asterope.script

import org.asterope.util._
import org.asterope.data.{DeepSkyDao, Nomenclature}

object CNebulaXOutlines extends App{


  val dir = new java.io.File("data/Outlines/")

  dir.listFiles().sortBy(_.getName.toUpperCase).foreach{f=>
    val in = new DataIS(f)
    val name = f.getName.toUpperCase.replace(".OUT","")
      .replaceAll("^B","Barnard ")
      .replaceAll("^CED","Cederblad ")
      .replaceAll("^GUM","Gum ")
      .replaceAll("^IC","IC ")
      .replaceAll("^NGC","NGC ")
      .replaceAll("^LBN","LBN ")
      .replaceAll("^RCW","RCW ")
      .replaceAll("^LDN","LDN ")
      .replaceAll("^SH","Sh ")
      .replaceAll("^VDBH","VdBH ")
      .replaceAll("^VDB","VdB ")
      .replaceAll("^COALSACK","Coalsack")

    if(!name.startsWith("Barnard") && !name.startsWith("Coal")&& !name.startsWith("LMC"))
      Nomenclature.parse(name)
    println("<outline id=\""+name+"\" author=\"Mark Smedley,CNebulaX\">")

    while(in.available>0){
      print(in.readFloatLE+" ")
      print(in.readFloatLE+" ")
    }
    println()
    println("</outline>")
  }

  println()
  println()
  println()
  println()


}