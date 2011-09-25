package org.asterope.data

import jdbm.RecordManagerFactory
import java.io.File
import org.asterope.util._

class LiteStarDaoTest extends ScalaTestCase{
  lazy val file= File.createTempFile("asterope","dbtest")
  lazy val recman =  RecordManagerFactory.createRecordManager(file.getPath)
  lazy val dao = new LiteStarDao(recman)

  def testByName{
    val star = new LiteStar(ra=0.degree,de=0.degree,mag=Magnitude(1), names=List(Nomenclature.parse("HIP 11")));

    assert(star.names === List(Nomenclature.parse("HIP 11")) )
    dao.addStar(star);


    val star2 = dao.objectsByName("HIP 11").next
    assert(star === star2)

    dao.addName("HIP 11",Nomenclature.parse("HIP 23"))
    assert(dao.objectsByName("HIP 23").hasNext)




  }
}