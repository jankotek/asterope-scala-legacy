package org.asterope


import javax.swing.{JComponent, SwingUtilities, AbstractAction, Action}
import java.util.concurrent._
import org.apache.commons.math.geometry.Vector3D
import java.lang.{InterruptedException, IllegalAccessError}
import java.lang.reflect.InvocationTargetException

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
      case e:InterruptedException=>{
        futures.foreach(_.cancel(true))
        throw e
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

  class Vector3DExtra(v:Vector3D){

    def getRaRadian: Double = {
      var phi = 0.0;
      if ((v.getX != 0.0) || (v.getY != 0))
        phi = math.atan2(v.getY, v.getX); // phi in [-pi,pi]

      if (phi < 0)
        phi += 2.0 * math.Pi; // phi in [0, 2pi]

      phi;
    }

    def getDeRadian: Double = {
      val z2 = v.getZ / v.getNorm;
      val theta = math.acos(z2);
      math.Pi / 2 -theta;
    }

    def getRa = getRaRadian.radian
    def getDe = getDeRadian.radian

    def toRaDeArray = Array[Double](getRaRadian,getDeRadian)

    def toArray: Array[Double] = {
      val a = new Array[Double](3)
      a(0) = v.getX;
      a(1) = v.getY;
      a(2) = v.getZ;
      a;
    }

    def ~=(v2: Vector3D): Boolean = Vector3D.angle(v,v2) < 1e-6


    def assertNormalized(){
      if(math.abs(v.getNorm - 1) > 1e-6)
        throw new AssertionError("Vector is not normalized: " + this)
    }

  }

  /** vector where asterope points */
  def Vector3D_asterope = rade2Vector(Angle.parseRa("03", "45", "54.4"), Angle.parseDe("+", "24", "33", "17"));

  /** position of M31 galaxy */
  def Vector3D_m31 = rade2Vector(Angle.parseRa("00", "42", "44.3"), Angle.parseDe("+", "41", "16", "9"));
  def Vector3D_m51 = rade2Vector(Angle.D2R * 202.46820800000003, Angle.D2R * 47.19466700000001);
  def Vector3D_m13 = rade2Vector(Angle.D2R * 250.42266699999996, Angle.D2R * 36.460249999999995);

  /** position of galaxy centre */
  def Vector3D_galaxyCentre = rade2Vector(Angle.parseRa("17", "45", "40.04"), Angle.parseDe("-", "29", "00", "28.1"));

  /** position of galaxy north pole */
  def Vector3D_galaxyNorthPole = rade2Vector(192.859508 * Angle.D2R, 27.128336 * Angle.D2R);

  /** position of galaxy north pole */
  def Vector3D_eclipticNorthPole = rade2Vector(Angle.parseRa("18", "0", "0"), Angle.parseDe("+", "66", "33", "38.6"));


  def rade2Vector(ra: Angle, de: Angle): Vector3D = rade2Vector(Angle.normalizeRa(ra.toRadian), de.toRadian);

  def rade2Vector(ra: Double, de: Double): Vector3D = {
    Angle.assertRa(ra);
    Angle.assertDe(de);

    val theta = math.Pi / 2 - de
    val phi = ra;
    val stheta = math.sin(theta);
    val x = stheta * math.cos(phi);
    val y = stheta * math.sin(phi);
    val z = math.cos(theta);
    if (java.lang.Double.isNaN(x) || java.lang.Double.isNaN(y) || java.lang.Double.isNaN(z))
      throw new IllegalArgumentException("can not convert " + ra + " - " + de);
    new Vector3D(x, y, z);
  }

  def assertNormalized(v: Vector3D) {
    require(math.abs(v.getNorm - 1) < 1e6, "vector is not normalized");
  }


  implicit def vecto3dtoExtra(v:Vector3D) = new Vector3DExtra(v)

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