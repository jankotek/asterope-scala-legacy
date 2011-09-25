package org.asterope.util


class GreekLetterTest extends ScalaTestCase{
	
	def testComplete{
		assert(GreekLetter.completeName("alph") === GreekLetter.Alpha);
		assert(GreekLetter.completeName("IO") === GreekLetter.Iota);

		intercept[IllegalArgumentException]{
			GreekLetter.completeName("ofijeqwjiod");
		}
	}
	
	def testThreeLetterRegExp{
		assert("alp".matches(GreekLetter.threeLetterRegularExp))
    assert("kap".matches(GreekLetter.threeLetterRegularExp))
		assert(!"qwr".matches(GreekLetter.threeLetterRegularExp))
	}

}