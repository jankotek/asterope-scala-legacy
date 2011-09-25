package org.asterope.util

import java.lang.Thread

/**
 * An multithreaded execution worker class. Is used on interactive task in GUI.
 * 
 * Runnin task can be cancelled using `interrupt` method. 
 * In this case all threads are interrupted with `InterruptedException`. 
 * You should periodically check `checkInterrupted()` to make program more interractive
 * 
 * Worker can be triggered with `run` method. It starts all scheduled tasks.
 * There is an optional delay between `run` and starting tasks.
 * Delay resets, if `run` is called multiple times before delay expires.  
 *  
 */
class Worker[E](delay:Long = 0 ) {

  private object lock

  private val runnable =
      if(delay==0) Runnable(run2())
      else org.asterope.util.Bind.delayed(delay,true,(run2()))



  /** called when all tasks finished normally */
  val onFinished = new Publisher[E]
  /** called after all tasks finished after being interrupted */
  val onInterrupted = new Publisher[E]
  /** called when one or more tasks throwed an exception */
  val onFailed = new Publisher[Throwable]

  private var tasks = Set[(E)=>Unit]()
  private var runningThreads = Set[Thread]()
  private var lastFailedException:Option[Throwable] = None
  private var wasInterrupted = false

  private var value:Option[E] = None
  private var preprocessors = Set[(E)=>E]()

  def addTask(c:(E)=>Unit){
    lock.synchronized{
      tasks += c
    }
  }

  def preprocess(c:(E)=>E){
    lock.synchronized{
      preprocessors += c
    }
  }


  final def run(value:E){
    lock.synchronized{
      //first interrupt any running tasks
      if(!runningThreads.isEmpty){
        interrupt()
      }
      this.value = Some(value)
    }
    runnable.run()
  }


  private def run2(){
    lock.synchronized{
      //first interrupt any running tasks
      if(!runningThreads.isEmpty){
        interrupt()
        waitWhileRunning()
      }

      var v = value.get
      preprocessors.foreach{c=>
        v = c(v)
      }

      tasks.foreach{t=>
        val thread = new Thread(){
          override def run(){
            try{
              t(v) //call tasks
            }catch{
              //thread was interrupted, exception can be ignored
              case e:InterruptedException => {wasInterrupted = true}
              case e:java.nio.channels.ClosedByInterruptException => {wasInterrupted = true}
              case e:Throwable=>{lastFailedException=Some(e)}
            }finally{
              lock.synchronized{
                //remove thread from list of running threads
                assert(runningThreads.contains(this))
                runningThreads -= this
                if(runningThreads.isEmpty){
                  if(lastFailedException.isEmpty && !wasInterrupted)
                     onFinished.firePublish(v)
                  else if (lastFailedException.isDefined)
                    onFailed.firePublish(lastFailedException.get)
                  else
                    onInterrupted.firePublish(v)
                  lastFailedException = None
                  wasInterrupted = false
                }
                lock.notify()
              }
            }
          }
          setDaemon(true)
        };
        runningThreads+=thread
        lock.notify()
        thread.start()
      }
    }
  }

  def interrupt(){
    lock.synchronized{
      wasInterrupted = true
      runningThreads.foreach(_.interrupt())
    }
  }

  def waitWhileRunning(){
    lock.synchronized{
      while(!runningThreads.isEmpty)
        lock.wait()
    }
  }

}