
package org.asterope.gui

import org.asterope.util._

class guiTest extends ScalaTestCase{

  def testDelayed{

    var counter = 0;
    val d = Bind.delayed(100,true,counter+=1)
    d.run() //trigger timer
    sleep(50)
    assert(counter===0)
    sleep(300)
    assert(counter===1)
    sleep(1000)
    assert(counter === 1)

    d.run();
    sleep(10)
    d.run();
    d.run();
    sleep(300)
    assert(counter === 2)
  }

}