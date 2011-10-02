package org.asterope.gui

import org.asterope.util._


class StatusBarTest extends ScalaTestCase {


  def testStatusBar(){
    val gc = onEDTWait{new StatusBarMemoryUsage()}
    waitUntil(gc.getText.size>2)
    val text1 = gc.getText
    assert(text1.matches("[0-9]+M / [0-9]+M"))

    //allocate 2 MB array, this should change memory usage
    var array:Array[Byte] = null
    fork("gc"){
      sleep(10)
      array = new Array[Byte](2e6.toInt)
    }

    waitUntil(text1 != gc.getText)
    val text2 = gc.getText
    assert(text2.matches("[0-9]+M / [0-9]+M"))

    //test gc
    array = null;
    gc.runGC.call()
    waitUntil(text2 != gc.getText)
    assert(gc.getText.matches("[0-9]+M / [0-9]+M"))


  }
}