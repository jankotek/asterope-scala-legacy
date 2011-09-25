package org.asterope.util

import java.util.logging.{LogRecord, Handler}
import collection.mutable.ArrayBuffer
import java.io.IOException

/** collects testing messages from logger */
object LogHandler extends Handler {
    val data = new ArrayBuffer[LogRecord]()
    def flush(){}
    def close(){}

    def publish(record: LogRecord){
        data+=record
    }

    lazy val hook:Unit = Log.logger.addHandler(this)
}

class LogTest extends ScalaTestCase{

  override def setUp(){
    LogHandler.hook
    LogHandler.data.clear()
  }

  def testMsgInlined(){

    val msg = new Object{
      override def toString:String = {
        fail("toString was called")
        "SHOULD NOT BE CALLED"
      }
    }
    //this test case will fail, if debug is enabled.
    Log.debug(msg.toString);

  }

  def test_Method_Location(){
    Log.warning("Testing message")
    assert(LogHandler.data(0).getSourceMethodName==="test_Method_Location")
  }

  def test_uncaught_exception_handled(){
    fork("test"){
      throw new IOException()
    }
    while(LogHandler.data.size<0)
    assert(LogHandler.data(0).getThrown.isInstanceOf[IOException])
  }

  def test_uncaught_in_EDT(){
    onEDT{
      throw new IOException()
    }
    while(LogHandler.data.size<0)
    assert(LogHandler.data(0).getThrown.isInstanceOf[IOException])
  }

}
