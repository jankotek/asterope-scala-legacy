package org.asterope.util

import java.util.logging._

/**
 * Logger for Asterope.
 * All methods are inlined.
 * Java util logger is used as backend, but it may change in future.
 */

object Log {

  val logger: Logger = Logger.getLogger(getClass.getName)

  //initialize logging to 'profile/asterope.log'
  private val fileHandler = new FileHandler("profile/asterope.log",false){
    setLevel(Level.ALL)

  }
  logger.setLevel(Level.ALL)
  logger.addHandler(fileHandler)

  def debug(@inline msg: => String) {
    log(Level.FINE, msg)
  }

  def info(@inline msg: => String) {
    log(Level.INFO, msg)
  }

  def warning(@inline msg: => String) {
    log(Level.WARNING, msg)
  }

  def error(@inline msg: => String) {
    log(Level.SEVERE, msg)
  }

  def debug(@inline msg: => String, t: Throwable) {
    log(Level.FINE, msg, t)
  }

  def info(@inline msg: => String, t: Throwable) {
    log(Level.INFO, msg, t)
  }

  def warning(@inline msg: => String, t: Throwable) {
    log(Level.WARNING, msg, t)
  }

  def error(@inline msg: => String, t: Throwable) {
    log(Level.SEVERE, msg, t)
  }
  
  @inline private def log(level: Level, @inline msg: => String) {
    log(level, msg, null)
  }

  @inline private def log(level: Level, @inline msg: => String, t: Throwable) {
    if (logger.isLoggable(level)) {
      val record: LogRecord = new LogRecord(level, msg)
      if (t != null)
        record.setThrown(t)
      //fill log
       Thread.currentThread().getStackTrace
         .find(c=>c.getClassName!=SELF && c.getClassName!=THREAD)
         .foreach{frame=>
        record.setSourceClassName(frame.getClassName)
        record.setSourceMethodName(frame.getMethodName)

      }

      logger.log(record)
    }
  }

  /** local class name */
  private val SELF: String = getClass.getName
  private val THREAD: String = classOf[Thread].getName

  //hook EDT uncaught exception handler
  System.setProperty("sun.awt.exception.handler", classOf[EDTUncaughtExceptionHandler].getName)

  //set general uncaught exception handler
  private object uncaughtExceptionHandler extends Thread.UncaughtExceptionHandler{
    def uncaughtException(t:Thread, e:Throwable){
      Log.error("Uncaught exception in thread "+t.getName,e)
    }
  }
  Thread.setDefaultUncaughtExceptionHandler(uncaughtExceptionHandler)

}


/** undocumented hack to catch exceptions on EDT thread */
protected[util] class EDTUncaughtExceptionHandler{
    /**WARNING: Don't change the signature of this method!*/
    def handle(e:Throwable) {
      Log.error("Uncaught exception in EDT",e)
    }
}