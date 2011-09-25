package org.asterope.util

import javax.swing.JLabel
import org.asterope.gui._

class MasterDetailTest extends ScalaTestCase{

  def testMasterDetail{

    val md = onEDTWait{
       new MasterDetail[String](){
        def createDetail(item:String) = new JLabel(){
          setName(item+"Name")
          setText(item+"Text")
        }
      }
    }

    onEDTWait{
      md.items+="aaa"
      md.items+="bbb"
      md.items+="ccc"
    }

    assert(md.items.size === 3)
    assert(md.items.contains("aaa"))

    assert(md.openedItems.size === 0)
    onEDTWait{
      md.master.setSelectedValue("bbb",true)
    }
    Thread.sleep(200)
    assert(md.openedItems.size === 1)
    assert(md.openedItems("bbb").getName()==="bbbName")

  }

}