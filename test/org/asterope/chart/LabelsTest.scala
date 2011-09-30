package org.asterope.chart

import org.asterope.data._
import org.asterope.util._

class LabelsTest extends ScalaTestCase{
	
	
	def testSelectNgcM13Name{
		
		val ngc = Nomenclature.parse("NGC 7000")
		val m = Nomenclature.parse("M 13")
		val ic = Nomenclature.parse("IC 6999")
		
		var ids = List(ngc,m,ic)


		object so extends HasNomenclature{
			override def names = ids
		}
		
		assert(Labels.selectName(so) === "7000 M13")
		
		ids = List(m)
		assert(Labels.selectName(so) === "M13")

		ids = List(ic,m)
		assert(Labels.selectName(so) === "M13")

		ids = List(ic,ngc)
		assert(Labels.selectName(so) === "7000")

		ids = List(ngc)
		assert(Labels.selectName(so) === "7000")
		
	}
	
	def testStarNames{
		val num = Nomenclature.parse("1 UMa")
		val greek = Nomenclature.parse(GreekLetter.Alpha+" "+Constel.UMa)
		val hip = Nomenclature.parse("HIP 2")
		
		val alpha = GreekLetter.smallGreekLetter(GreekLetter.Alpha).toString
		
		var ids = List(num,greek,hip)
		
	  object so extends HasNomenclature{
			override def names = ids
		}
		
		ids = List(greek)
		println("'"+Labels.selectName(so)+"'")
		assert(Labels.selectName(so) === alpha)

		ids = List(num)
		assert(Labels.selectName(so) === "1")


		ids = List(hip)
		assert(Labels.selectName(so) === "H2")

		ids = List(hip,num)
		assert(Labels.selectName(so) === "1")


		
	}

}
