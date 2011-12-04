package org.asterope.util

import org.asterope.Beans

/**
 * test case which provides access to ChartBeans
 */
abstract class BeansTestCase extends ScalaTestCase{

   object beans extends Beans{
     override lazy val recman = BeansTestCase.recman
   }

}

protected[util] object BeansTestCase{


  /** cache RecordManager between test cases */
  private object beansPermanent extends Beans;

  def recman = beansPermanent.recman

  //shutdown recordManager with JVM
  Runtime.getRuntime.addShutdownHook(new Thread(){
    override def run(){
      beansPermanent.onShutdown.firePublish(Unit)
    }
  })
}
