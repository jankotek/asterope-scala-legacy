package org.asterope.util

import collection.mutable.ArrayBuffer
import java.io.IOException
import java.util.logging.{Level, LogRecord, Handler}

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

    val oldLevel = Log.logger.getLevel
    try{
    Log.logger.setLevel(Level.INFO)
    val msg = new Object{
      override def toString:String = {
        fail("toString was called")
        "SHOULD NOT BE CALLED"
      }
    }

    Log.debug(msg.toString);
    }finally {
      Log.logger.setLevel(oldLevel)
    }


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
