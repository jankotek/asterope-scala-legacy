package org.asterope.healpix

import org.asterope.util.ScalaTestCase


class LongRangeSetBuilderTest extends ScalaTestCase {
  def testBuild(){
    val b = new LongRangeSetBuilder
    b.appendRange(1, 5)
    b.appendRange(10, 15)
    b.append(16)
    b.appendRange(13, 20)
    b.append(21)

    val iter = b.build.rangeIterator
    assert(iter.moveToNext)
    assert(1 === iter.first)
    assert(5 === iter.last)
    assert(iter.moveToNext)
    assert(10 === iter.first)
    assert(21 === iter.last)
    assert(!iter.moveToNext)
  }

  def testMoveFirst(){
    val b = new LongRangeSetBuilder()

    b.appendRange(1,5)
    b.appendRange(10,12)
    b.appendRange(8,16) //builder should extend last range, instead of adding new one

    val i = b.build.rangeIterator
    assert(i.moveToNext)
    assert(1 === i.first )
    assert(5 === i.last)
    assert(i.moveToNext)
    assert(8 === i.first)
    assert(16 === i.last)
    assert(!i.moveToNext)

    intercept[Exception]{
      //overlap with first range, should throw an exception
      b.appendRange(4,16)
    }
  }
}