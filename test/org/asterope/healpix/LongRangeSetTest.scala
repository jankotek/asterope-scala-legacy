package org.asterope.healpix

import junit.framework.TestCase
import junit.framework.Assert._
import java.util.ArrayList


class LongRangeSetTest extends TestCase {
  def testAddRange() {
    b.appendRange(1, 10)
    b.appendRange(30, 40)
    val rs = b.build

    assert(!rs.contains(0))
    assert(rs.contains(1))
    assert(rs.contains(5))
    assert(rs.contains(10))
    assert(!rs.contains(11))
    assert(!rs.contains(29))
    assert(rs.contains(30))
    assert(rs.contains(35))
    assert(rs.contains(40))
    assert(!rs.contains(41))
  }

  def testIter() {
    b.appendRange(1, 10)
    b.appendRange(30, 40)
    var rs2: LongRangeSet = b.build
    var rs: ArrayList[Long] = new ArrayList[Long]
    var iter = rs2.longIterator
    while (iter.hasNext) {
      rs.add(iter.next)
    }
    assert(!rs.contains(0L))
    assert(rs.contains(1L))
    assert(rs.contains(5L))
    assert(rs.contains(10L))
    assert(!rs.contains(11L))
    assert(!rs.contains(29L))
    assert(rs.contains(30L))
    assert(rs.contains(35L))
    assert(rs.contains(40L))
    assert(!rs.contains(41L))
  }

  def testComplement() {
    b.appendRange(20, 30)
    b.append(40)
    b.append(42)
    b.appendRange(50, 60)
    var rs: LongRangeSet = b.build
    var iter: LongRangeIterator = rs.complement.rangeIterator
    assertTrue(iter.moveToNext)
    assertEquals(Long.MinValue, iter.first)
    assertEquals(19, iter.last)
    assertTrue(iter.moveToNext)
    assertEquals(31, iter.first)
    assertEquals(39, iter.last)
    assertTrue(iter.moveToNext)
    assertEquals(41, iter.first)
    assertEquals(41, iter.last)
    assertTrue(iter.moveToNext)
    assertEquals(43, iter.first)
    assertEquals(49, iter.last)
    assertTrue(iter.moveToNext)
    assertEquals(61, iter.first)
    assertEquals(Long.MaxValue, iter.last)
    assertTrue(!iter.moveToNext)
    assertEquals(rs.complement.complement, rs)
  }

  def testUnion() {
    b.appendRange(20, 30)
    b.appendRange(40, 50)
    var r1: LongRangeSet = b.build
    b2.appendRange(1, 10)
    b2.appendRange(45, 55)
    var r2: LongRangeSet = b2.build
    b3.appendRange(1, 10)
    b3.appendRange(20, 30)
    b3.appendRange(40, 55)
    var r3: LongRangeSet = b3.build
    assertEquals(r3, r1.union(r2))
  }

  def testIntersect() {
    b.appendRange(20, 30)
    b.appendRange(40, 50)
    var r1: LongRangeSet = b.build
    b2.appendRange(1, 10)
    b2.appendRange(22, 23)
    b2.appendRange(45, 55)
    var r2: LongRangeSet = b2.build
    b3.appendRange(22, 23)
    b3.appendRange(45, 50)
    var r3: LongRangeSet = b3.build
    assertEquals(r3, r1.intersect(r2))
  }

  def testIntersect2() {
    b.appendRange(10, 100)
    b.appendRange(110, 120)
    b.appendRange(200, 220)
    var r1: LongRangeSet = b.build
    b2.appendRange(20, 30)
    b2.appendRange(40, 50)
    b2.appendRange(90, 200)
    var r2: LongRangeSet = b2.build
    b3.appendRange(20, 30)
    b3.appendRange(40, 50)
    b3.appendRange(90, 100)
    b3.appendRange(110, 120)
    b3.appendRange(200, 200)
    var r3: LongRangeSet = b3.build
    assertEquals(r3, r1.intersect(r2))
  }

  def testSubstract() {
    b.appendRange(20, 30)
    b.appendRange(40, 50)
    var r1: LongRangeSet = b.build
    b2.appendRange(1, 10)
    b2.appendRange(45, 55)
    var r2: LongRangeSet = b2.build
    b3.appendRange(20, 30)
    b3.appendRange(40, 44)
    var r3: LongRangeSet = b3.build
    assertEquals(r3, r1.substract(r2))
  }

  def testContainsAll() {
    b.appendRange(20, 30)
    b.appendRange(40, 50)
    var r1: LongRangeSet = b.build
    assertFalse(r1.containsAll(0, 10))
    assertFalse(r1.containsAll(10, 20))
    assertFalse(r1.containsAll(19, 19))
    assertTrue(r1.containsAll(20, 20))
    assertFalse(r1.containsAll(21, 21))
    assertTrue(r1.containsAll(20, 30))
    assertFalse(r1.containsAll(25, 35))
    assertTrue(r1.containsAll(30, 30))
    assertFalse(r1.containsAll(31, 31))
    assertFalse(r1.containsAll(35, 37))
    assertFalse(r1.containsAll(35, 45))

    assertTrue(r1.containsAll(40, 40))
    assertFalse(r1.containsAll(45, 55))
    assertFalse(r1.containsAll(60, 70))
  }

  def testContainsAny() {
    b.appendRange(20, 30)
    b.appendRange(40, 50)
    var r1: LongRangeSet = b.build
    assertFalse(r1.containsAny(0, 10))
    assertTrue(r1.containsAny(10, 20))
    assertFalse(r1.containsAny(19, 19))
    assertTrue(r1.containsAny(20, 20))
    assertTrue(r1.containsAny(21, 21))
    assertTrue(r1.containsAny(20, 30))
    assertTrue(r1.containsAny(25, 35))
    assertTrue(r1.containsAny(30, 30))
    assertFalse(r1.containsAny(31, 31))
    assertFalse(r1.containsAny(35, 37))
    assertTrue(r1.containsAny(35, 45))

    assertTrue(r1.containsAny(40, 40))
    assertTrue(r1.containsAny(45, 55))
    assertFalse(r1.containsAny(60, 70))
  }

  private[healpix] var b: LongRangeSetBuilder = new LongRangeSetBuilder
  private[healpix] var b2: LongRangeSetBuilder = new LongRangeSetBuilder
  private[healpix] var b3: LongRangeSetBuilder = new LongRangeSetBuilder
}