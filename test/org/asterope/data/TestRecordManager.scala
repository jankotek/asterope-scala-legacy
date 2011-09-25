package org.asterope.data;

/**
 * This trait mixes in static RecordManager
 * so it is not reopened each time test starts.
 * This speeds up test significantly
 */
trait TestRecordManager extends DataBeans{
  override lazy val recman = TestRecordManager.recman
}

object TestRecordManager{
  protected object dataBeans extends DataBeans;

  def recman = dataBeans.recman

  //shutdown recordManager with JVM
  Runtime.getRuntime.addShutdownHook(new Thread(){
    override def run(){
      dataBeans.shutdown()
    }
  })

}