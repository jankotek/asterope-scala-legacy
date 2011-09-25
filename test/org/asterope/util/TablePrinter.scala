package org.asterope.util;

import collection.mutable.LinkedHashMap
import java.io.PrintStream

/**
 * Prints simple formatted tables into STDOUT.
 */
class TablePrinter {


  protected val data = new LinkedHashMap[String, LinkedHashMap[String,String]]

  def addValue(col:String, row:String, value:String){
    if(!data.contains(col))
      data.put(col,new LinkedHashMap[String,String])
    data(col).put(row,value)
  }


  def print(){
    print(System.out)
  }

  def print(out:PrintStream){
    val cols = data.keys
    val rows = data.valuesIterator.flatMap(_.keys).toSet

    //print column headers
    out.println(cols.mkString("\t","\t",""))

    rows.foreach{row=>
      out.print(row+"\t"); //row name
      cols.foreach{col=>
        //value
        out.print(data(col).getOrElse(row,""))
        out.print("\t");
      }
      out.println()
    }
  }


  def perfTest(col:String,row:String, timeout:Long, block: =>Unit){
    val start = System.currentTimeMillis;
    var counter = 0;
    var time = start;
    while(start + timeout > time){
      //repeat code until timeout
      block;
      counter+=1
      if(counter%100==0){
        time = System.currentTimeMillis;
      }
    }
    val realtime = System.currentTimeMillis - start

    addValue(col,row,(counter/realtime).toFloat.toString)

  }


}