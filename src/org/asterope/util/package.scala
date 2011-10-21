package org.asterope


import java.lang.{IllegalAccessError, InterruptedException}
import javax.swing.{JComponent, SwingUtilities, AbstractAction, Action}
import java.io._
import java.util.concurrent._

/**
 * Various general purpose utilities. 
 * 
 * Asterope also adds lot of predefined methods. 
 * Those methods are defined in this package object
 */
package object util{

  private val executor = Executors.newScheduledThreadPool(8)

  def fork( block: =>Any):Unit = {
    executor.submit(Runnable(block),Unit)
  }

  def future[E](t: =>E):Future[E] = {
    executor.submit(Callable(t));
  }

  def waitOrInterrupt(futures:Iterable[Future[_]]){
    try{
      futures.foreach(_.get)
    }catch{
      //in case this thread was interrupted, forward it to other threads
      case _:InterruptedException=>{
        futures.foreach(_.cancel(true))
      }
    }
  }

  def Runnable(block: =>Unit) = new Runnable {
    def run(){
      block
    }
  }

  def Callable[E](block: =>E) = new Callable[E] {
    def call():E={
      block
    }
  }


  def stopWatch(block: =>Unit): Long = {
	  val  l = System.currentTimeMillis();
	  block;
	  System.currentTimeMillis() - l
  }

  /** 
   * Check if current thread was interrupted (tasks was cancelled by user).
   * If yes it throws `InterruptedException`
   */
  def checkInterrupted(){
    if(Thread.currentThread().isInterrupted)
      throw new InterruptedException("Thread interrupted")
  }

/*
 * Asterope implicit conversions for angle. To enable `1.degree` etc
 */
  implicit def int2angle(d:Int) = Angle.int2angle(d)
  implicit def long2angle(d:Long) = Angle.long2angle(d)
  implicit def double2angle(d:Double) = Angle.double2angle(d)

  /**
   * Schedule the given code to be executed on the Swing event dispatching
   * thread (EDT). Returns immediately.
   */
  def  onEDT[E](op: =>E) {
    SwingUtilities.invokeLater(Runnable(op))
  }

  /**
   * Calculate value on the Swing event dispatching thread (EDT).
   * This method schedules block to be called on EDT,
   * then it transfers and return result back to current thread.
   *
   * @return value calculated on EDT
   */
    def onEDTWait[E](block: => E):E = {
      if(isEDT)return block
      var ret:E = null.asInstanceOf[E];
      SwingUtilities invokeAndWait org.asterope.util.Runnable({ret = block})
      ret
    }
  /**
   * Check if current thread is Swing event dispatching thread (EDT)
   */
  def isEDT = SwingUtilities.isEventDispatchThread


  /**
   * Throws AssertionError if current thread is Swing event dispatching thread (EDT)
   */
  def assertEDT(){
    if(!isEDT)
      throw new IllegalAccessError("Must be called from Swing event dispatching thread (EDT)");
  }

  /**
   * Throws AssertionError if current thread is NOT Swing event dispatching thread (EDT)
   */
  def assertNotEDT(){
      if(isEDT)
        throw new IllegalAccessError("Can not be called from Swing event dispatching thread (EDT)");
  }
  /** guick factory method for new MigLayout, so it does not have to be imported*/
  def MigLayout(args:String="fillx") = new net.miginfocom.swing.MigLayout(args)

  /** implicitly converts javax.swingAction to ScalaAction */
  implicit def action2ScalaAction(act:javax.swing.Action):ScalaAction = new ScalaAction(act)

  protected class _withNameSupport[E <: JComponent](c:E){
    /** Changes component name and returns component itself. 
     * Is provided by implicit conversion defined in `org.asterope.util` package
     */
    def withName(name:String):E = {
      c.setName(name)
      c
    }
  }

  /** adds `withName` method to Swing components */
  implicit def _withNameImplicit[E <: JComponent](c:E) = new _withNameSupport(c)



  /** factory method which takes code block and wraps it into an action*/
  def act(title: String,body: =>Unit):Action = new AbstractAction(title) {
    def actionPerformed(a: java.awt.event.ActionEvent){
      body
    }
  }

  /** factory method which takes code block and wraps it into an action*/
  def act(body: =>Unit):Action = act("undefined",body)


}