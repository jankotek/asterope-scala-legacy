package org.asterope.data.catalog

import org.asterope.util._

class DataParserTest extends ScalaTestCase{

	def testDataLine{
		val d = DataRow(List(
				DataItem(colName="aa",value=Some("11")),
				DataItem(colName="bb",value=Some("11.1")),
				DataItem(colName="cc",value=Some("11x1"))
				))

		assert(d.getString("aa").get === "11");
		assert(d.getInt("aa").get === 11);
		assert(d.getDouble("aa").get === 11);
		assert(d.getBigDecimal("aa").get === 11);
		intercept[NumberFormatException]{
			assert(d.getInt("bb").get === 11.1);
		}


		assert(d.getDouble("bb").get === 11.1);
		assert(d.getBigDecimal("bb").get === 11.1);
		intercept[NumberFormatException]{
			assert(d.getBigDecimal("cc").get === 11.1);
		}

		intercept[NumberFormatException]{
			assert(d.getDouble("cc").get === 11.1);
		}

	}

	def testSimpleDelimiterLineParser{

		val p = new SimpleDelimiterLineParser(
				colNames=List("a","b"),
				colUnits = List("mag","deg")
				);

		val line = "aa; bb ;;dd"

		assert(
			DataRow(List(
					DataItem(colName = "a", value=Some("aa"), unit=Some("mag")),
					DataItem(colName = "b", value=Some("bb"), unit=Some("deg")),
					DataItem(colName = "2", value=None),
					DataItem(colName = "3", value=Some("dd"))
				)) === p.parseLine(line).get
		)
	}


	def testFixedWidthLineParser{
		val p = new FixedWidthLineParser(
				cols = List(
						FixedWidthColumn(start=0, end=5, colName="aa",unit=Some("mag")),
						FixedWidthColumn(start=5, end=6, colName="bb"),
						FixedWidthColumn(start=6, end= -1, colName="cc")
				))

		val line = "0123456789abcdef  "
		assert(
				DataRow(List(
						DataItem(colName="aa", value=Some("01234"),unit=Some("mag")),
						DataItem(colName="bb", value=Some("5")),
						DataItem(colName="cc", value=Some("6789abcdef"))
				)) === p.parseLine(line).get
		)
	}
}
