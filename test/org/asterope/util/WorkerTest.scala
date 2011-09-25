package org.asterope.util

import org.asterope.util._
import java.util.concurrent.atomic.AtomicInteger
import java.lang.reflect.InvocationTargetException

class WorkerTest extends ScalaTestCase {

  var delay = 0;
  lazy val w = new Worker[Unit](delay){
    onFinished{u=>
      onFinishedCalled = true
      onInterruptedCalled = false
      failedWithException = None
    }
    onFailed{e=>
      onFinishedCalled = false
      onInterruptedCalled = false
      failedWithException = Some(e)
    }


    onInterrupted{u=>
      onFinishedCalled = false
      onInterruptedCalled = true
      failedWithException = None
    }


  }
  val count = new AtomicInteger(0)
  /** true if onFinished event was published */
  var onFinishedCalled = false
  /** contains last exception if any*/
  var failedWithException:Option[Throwable] = None
  /** true if onInterrupted was called */
  var onInterruptedCalled = false

  def reset(){
    count.set(0)
    onFinishedCalled = false
    failedWithException = None
    onInterruptedCalled = false
  }


  def testRun(){
    w.addTask{e=>
     count.incrementAndGet()
    }
    w.run()
    w.waitWhileRunning()
    assert(count.get() === 1)
    assert(onFinishedCalled && !onInterruptedCalled && failedWithException.isEmpty)
  }

  def testRun20(){
    for(i<-1 to 20){
      w.addTask{e=>
        count.incrementAndGet()
      }
    }
    w.run()
    w.waitWhileRunning()
    assert(count.get() === 20)
    assert(onFinishedCalled && !onInterruptedCalled && failedWithException.isEmpty)
  }

  def testInterrupt(){
    w.addTask{e=>
      sleep(100)
      count.incrementAndGet()

    }
    w.run();
    w.interrupt();
    w.waitWhileRunning()
    //sleep will throw `InterruptedException` so `count` is never incremented
    assert(count.get === 0)
    assert(!onFinishedCalled && onInterruptedCalled && failedWithException.isEmpty)
  }

  def testLongRunningInterrupt(){
    w.addTask{e=>
      for(i<-1 to 100000) count.incrementAndGet()
      checkInterrupted()
      for(i<-1 to 100000) count.incrementAndGet()
    }
    w.run();
    w.interrupt();
    w.waitWhileRunning()
    assert(count.get() === 100000)
    assert(!onFinishedCalled && onInterruptedCalled && failedWithException.isEmpty)
  }

  def testFailOnException(){
    w.addTask{e=>
      sleep(1)
      throw new RuntimeException
    }
    w.addTask{e=>
      sleep(100)
      count.incrementAndGet()
    }
    w.run()
    intercept[InvocationTargetException]{
      w.waitWhileRunning()
    }
    //second task should still finish fine
    assert(count.get() === 1)
    assert(!onFinishedCalled && !onInterruptedCalled && failedWithException.isDefined)
  }


  def testReuse(){
    var throwError = false;
    w.addTask{e=>
      sleep(10)
      count.incrementAndGet()
    }
    w.addTask{e=>
      sleep(10)
      if(throwError) throw new Error()
    }

    def runNormal(){
      throwError = false
      reset()
      w.run()
      w.waitWhileRunning()
      assert(count.get === 1)
      assert(onFinishedCalled && !onInterruptedCalled && failedWithException.isEmpty)
    }

    def runInterrupt(){
      throwError = false
      reset()
      w.run()
      w.interrupt()
      w.waitWhileRunning()
      assert(count.get === 0)
      assert(!onFinishedCalled && onInterruptedCalled && failedWithException.isEmpty)
    }

    def runFail(){
      throwError = true
      reset()
      w.run()
      w.waitWhileRunning()
      assert(count.get === 1)
      assert(!onFinishedCalled && !onInterruptedCalled && !failedWithException.isEmpty)
    }

    runFail()
    runNormal()
    runNormal()
    runInterrupt()
    runFail()
    runFail()
    runInterrupt()
    runNormal()
  }

  def testInterruptOnSecondRun(){
    w.addTask{e=>
      count.incrementAndGet()
      sleep(10)
      count.incrementAndGet()
    }

    w.run()
    sleep(5)
    w.run()
    assert(count.get == 1)
    assert(!onFinishedCalled && onInterruptedCalled && failedWithException.isEmpty)
    onInterruptedCalled = false
    w.waitWhileRunning()
    assert(count.get == 3)
    assert(onFinishedCalled && !onInterruptedCalled && failedWithException.isEmpty)

  }

  def testInterruptOnSecondRunWithDelay(){
    delay = 500
    var finished = false
    w.addTask{e=>
      try{
        count.incrementAndGet()
        sleep(500)
        count.incrementAndGet()
      }finally{
        finished = true;
      }
    }

    w.run()
    sleep(750)
    assert(count.get == 1)
    w.run()
    assert(finished === true)
    assert(!onFinishedCalled && onInterruptedCalled && failedWithException.isEmpty)
    onInterruptedCalled = false
    sleep(1500)
    w.waitWhileRunning()
    assert(count.get === 3)
    assert(onFinishedCalled && !onInterruptedCalled && failedWithException.isEmpty)

  }

  def testDelayed(){
    delay = 500
    w.addTask{e=>
      count.incrementAndGet()
      sleep(500)
      count.incrementAndGet()
    }


    w.run()
    Thread.sleep(750)
    assert(count.get===1)
    w.waitWhileRunning()
    assert(count.get === 2)
    assert(onFinishedCalled && !onInterruptedCalled && failedWithException.isEmpty)
  }

  def testDelayedCoalescent(){
    delay = 500
    w.addTask{e=>
      count.incrementAndGet()
      sleep(500)
      count.incrementAndGet()
    }
    w.run()
    sleep(250)
    w.run() //second run() should postpone first one
    reset()
    sleep(200)
    assert(count.get===0)
    sleep(600)
    assert(count.get===1)
    w.waitWhileRunning()
    assert(onFinishedCalled && !onInterruptedCalled && failedWithException.isEmpty)
  }
}