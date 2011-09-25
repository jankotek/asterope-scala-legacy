package org.asterope.gui

import org.asterope.util._


class StatusBarTest extends ScalaTestCase {


  def testStatusBar(){
    val gc = new StatusBarMemoryUsage()
    waitUntil(gc.getString.size>2)
    val text1 = gc.getString
    assert(text1.matches("[0-9]+M / [0-9]+M"))

    //allocate 2 MB array, this should change memory usage
    var array:Array[Byte] = null
    fork("gc"){
      sleep(10)
      array = new Array[Byte](2e6.toInt)
    }

    waitUntil(text1 != gc.getString)
    val text2 = gc.getString
    assert(text2.matches("[0-9]+M / [0-9]+M"))

    //test gc
    array = null;
    gc.runGC.call()
    waitUntil(text2 != gc.getString)
    assert(gc.getString.matches("[0-9]+M / [0-9]+M"))


  }
}