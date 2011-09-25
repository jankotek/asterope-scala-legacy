//
// Licenced under GPLv2, see licence.txt
// (c) Jan Kotek,
//
package org.asterope.healpix

import java.util.Arrays
import java.util.NoSuchElementException
import collection.mutable.{ArrayBuffer, Buffer}
import java.io._
import org.asterope.util.LongPacker

/**
 * This class represents range based sets of long values.
 * First and last boundaries are stored in sorted long[] array so it consumes very little place
 * This is essentially equivilent to run length encoding of a bitmap-based
 * set and thus works very well for sets with long runs of integers, but is
 * quite poor for sets with very short runs.
 * <p>
 * Is readonly, so is thread safe.
 * <p>
 * To construct new LongRangeSet use {@link LongRangeSetBuilder} if
 * our values are sorted.
 * <p>
 * Inspired by Justin F. Chapweske and Soren Bak
 * @author Jan Kotek
 * empty constructor is only for serialization
 */

@SerialVersionUID(-7543399451387806240L)
class LongRangeSet extends Externalizable{

  /**sorted ranges, even is first, odd is last */
  private var ranges: Array[Long] = null

  /**
   * Construct new LongRangeSet from given values
   * <p>
   * Dont use directly, use {@link LongRangeSetBuilder} instead
   * <p>
   * This constructor makes integrity check and copyes values into new array.
   * When LongRangeSet is constructed inside LongRangeSet this operation can be
   * skipped and set can be constructed way faster.
   *
   */
  def this(values: Array[Long], size: Int) {
    this ()

    if (size % 2 != 0) throw new IllegalArgumentException("not divide by 2")
    if (size < 0) throw new IllegalArgumentException("negative size")
    if (values.length < size) throw new IllegalArgumentException("too small array")
    ranges = new Array[Long](size)

    var oldLast: Long = Long.MinValue

    var i: Int = 0
    while (i < size / 2) {
        val first: Long = values(i * 2)
        val last: Long = values(i * 2 + 1)
        //check that is sorted
        if (first > last)
          throw new IllegalArgumentException("first > last")
        if (oldLast + 1 >= first && first != Long.MinValue)
          throw new IllegalArgumentException("values are not sorted at oldLast: " + oldLast + ", first: " + first)
        //move to new array
        ranges(i * 2) = first
        ranges(i * 2 + 1) = last
      //TODO should oldLast be updated?
//        oldLast = last;
        i+=1
    }

  }

  /**
   * @return Iterator over all longs in this set
   */
  def longIterator: Iterator[Long] = {
    if (isEmpty)
      LongRangeSetBuilder.EMPTY_LONG_ITERATOR

    new Iterator[Long] {
      private var pos = 0
      private var value:Long = first

      def hasNext: Boolean = {
        pos < ranges.length && value <= ranges(pos + 1)
      }

      def nextLong(): Long = {
        if (pos >= ranges.length || value > ranges(pos + 1))
          throw new NoSuchElementException
        val ret: Long = value
        //move to next position
        if (value < ranges(pos + 1)){
          value += 1  //move to next value
        }else {
          pos += 2 //move to next range
          if (pos < ranges.length)
            value = ranges(pos)
        }
        ret
      }

      //using this method probably causes boxing
      def next() = nextLong();

    }
  }

  /**
   * @return LongRangeIterator over ranges in this set
   */
  def rangeIterator: LongRangeIterator = {
    if (isEmpty)
      return LongRangeSetBuilder.EMPTY_ITERATOR

    new LongRangeIterator {

      private var pos: Int = -2

      def moveToNext(): Boolean = {
        pos += 2
        pos < ranges.length
      }

      def first: Long = {
        if (pos < 0) throw new IllegalAccessError("Call moveToNext() first")
        if (pos >= ranges.length) throw new NoSuchElementException
        ranges(pos)
      }

      def last: Long = {
        if (pos < 0) throw new IllegalAccessError("Call moveToNext() first")
        if (pos >= ranges.length) throw new NoSuchElementException
        ranges(pos + 1)
      }


    }
  }


  /**
   * @return first element in set
   * @throws NoSuchElementException if set isEmpty()
   */
  def first: Long = {
    if (isEmpty) throw new NoSuchElementException
    ranges(0)
  }

  /**
   * @return last element in set
   * @throws NoSuchElementException if set isEmpty()
   */
  def last: Long = {
    if (isEmpty) throw new NoSuchElementException
    ranges(ranges.length - 1)
  }

  /**
   * @param i The integer to check to see if it is in this set..
   * @return true if i is in the set.
   */
  def contains(i: Long): Boolean = {
    var pos: Int = Arrays.binarySearch(ranges, i)
    if (pos > 0)
      return true

    pos = -(pos + 1)
    pos % 2 != 0
  }

  def containsAll(first: Long, last: Long): Boolean = {
    if (first > last)
      throw new IllegalArgumentException("First is bigger then last")
    if (isEmpty || last < first || first > last)
      return false

    val firstIndex: Int = Arrays.binarySearch(ranges, first)
    val lastIndex: Int = Arrays.binarySearch(ranges, last)

    if (firstIndex >= 0 && lastIndex >= 0)
      lastIndex - firstIndex < 2
    else
      false
  }

  def containsAny(first: Long, last: Long): Boolean = {
    if (first > last)
      throw new IllegalArgumentException("First is bigger then last")
    if (isEmpty || last < first || first > last)
      return false

    val firstIndex: Int = Arrays.binarySearch(ranges, first)
    if(!(firstIndex < 0) || !(firstIndex % 2 == -1))
      return true
    val lastIndex: Int = Arrays.binarySearch(ranges, last)

    !(lastIndex < 0 &&  firstIndex == lastIndex)
  }

  def containsAll(iter: Iterator[Long]): Boolean = {
    while (iter.hasNext)
      if (!contains(iter.next()))
        return false
    true
  }

  def containsAny(iter: Iterator[Long]): Boolean = {
    while (iter.hasNext)
      if (contains(iter.next()))
        return true
    false
  }

  def containsAll(iter: LongRangeIterator): Boolean = {
    while (iter.moveToNext())
      if (!containsAll(iter.first, iter.last))
        return false
    true
  }

  def containsAny(iter: LongRangeIterator): Boolean = {
    while (iter.moveToNext())
      if (containsAny(iter.first, iter.last))
        return true
    false
  }

  /**
   * @return number of longs (pixels) in this set. !!NOT number of ranges!!
   */
  def size: Long = {
    var size: Long = 0
    val iter: LongRangeIterator = rangeIterator
    while (iter.moveToNext()) {
      size += 1 + iter.last - iter.first
    }
    size
  }

  /**
   * @return number of ranges in this set
   */
  def rangeCount: Int = ranges.length / 2


  def rangeFirst(i: Int): Long =  ranges(i * 2)


  def rangeLast(i: Int): Long =  ranges(i * 2 + 1)


  /**
   * Convert all items in range set to array.
   * With large set, this method will fail with OutOfMemoryException
   * @return array of elements in collection
   */
  def toArray: Array[Long] = {
    val ret: Array[Long] = new Array[Long](size.toInt)
    val iter = longIterator
    var i: Int = 0
    while (iter.hasNext) {
      ret(i) = iter.next()
      i += 1
    }
    ret
  }

  def toBuffer:Buffer[Long] = {
    val ret = new ArrayBuffer[Long]()
    ret.appendAll(longIterator)
    ret
  }

  override def toString: String = {
    val s: StringBuilder = new StringBuilder
    s.append('[')
    val iter: LongRangeIterator = rangeIterator
    while (iter.moveToNext()) {
      if (s.length > 1) s.append(',')
      s.append(iter.first)
      s.append('-')
      s.append(iter.last)
    }
    s.append(']')
    s.toString()
  }

  override def hashCode: Int = {
    val prime: Int = 31
    prime + Arrays.hashCode(ranges)
  }

  override def equals(obj: Any): Boolean = {
    if (obj == null) return false
    if (!(obj.isInstanceOf[LongRangeSet])) return false
    val other: LongRangeSet = obj.asInstanceOf[LongRangeSet]
    Arrays.equals(ranges, other.ranges)
  }

  /**
   * Create new LongRangeSet with complement (inversion) of values in this set.
   * Bounds of complement  are Long.MIN_VALUE and Long.MAX_VALUE
   * if operation is called twice, original set is produced
   * <p>
   * This operation is FAST. Requires only one traversal of ranges.
   * It does not decompress RangeSet to pixels.
   * <p>
   * This operation does not modify original collection.
   *
   * @return inverted LongRangeSet
   */
  def complement: LongRangeSet = {
    val b = new LongRangeSetBuilder(ranges.length + 2)
    var last: Long = Long.MinValue
    val iter = rangeIterator
    while (iter.moveToNext()) {
      if (iter.first != Long.MinValue) //was already starting with Long.MIN_VALUE, ignore first range
        b.appendRange(last, iter.first - 1)
      last = iter.last + 1
    }
    //close
    if (
      //case with completely empty set
      last == Long.MinValue && b.size == 0 ||
      //case when last was not Long.MAX_VALUE (there is +1 overflow) and close prev value
      last != Long.MinValue && b.size > 0)
        b.appendRange(last, Long.MaxValue)
    b.build
  }

  /**
   * Create new LongRangeSet which contains union of values from
   * original set and parameter
   * <p>
   * This operation is FAST. Requires only one traversal of ranges.
   * It does not decompress RangeSet to pixels.
   * <p>
   * This operation does not modify original collection.
   *
   *
   * @param rs LongRangeSet to make union with
   * @return LongRangeSet contains union of original set and parameter set
   */
  def union(rs: LongRangeSet): LongRangeSet = {
    val it1 = rangeIterator
    val it2 = rs.rangeIterator

    val rsb = new LongRangeSetBuilder

    //boolean indicates if iterator have more data
    var run1: Boolean = it1.moveToNext()
    var run2: Boolean = it2.moveToNext()

  	//problem is that data appended in builder must be sorted
   	//so use two iterators at the same time and produce sorted result
    while (run1 || run2) { //repeat until any of iterators have data
      if (run1 && (!run2 || it1.last < it2.first)) {
        //scroll first iterator until it overlaps
        rsb.appendRange(it1.first, it1.last)
        run1 = it1.moveToNext()
      }else if (run2 && (!run1 || it2.last < it1.first)) {
        //scroll second iterator until it overlaps
        rsb.appendRange(it2.first, it2.last)
        run2 = it2.moveToNext()
      }else if (run1 && run2) {
        //overlap
        val minFirst: Long = math.min(it1.first, it2.first)
        val maxLast: Long = math.max(it1.last, it2.last)
        rsb.appendRange(minFirst, maxLast)
        run1 = it1.moveToNext()
        run2 = it2.moveToNext()
      }else {
        //should not be here
        throw new InternalError
      }
    }
    rsb.build
  }

  /**
   * Construct new LongRangeSet with intersection of values from original set and
   * parameter set.
   * <p>
   * This operation is FAST. Requires only one traversal of ranges.
   * It does not decompress RangeSet to pixels.
   * <p>
   * This operation does not modify original collection.

   * @param rs The set with which to intersect with this set.
   * @return new set that represents the intersect of original and parameter set
   */
  def intersect(rs: LongRangeSet): LongRangeSet = {
    if (isEmpty) return rs
    if (rs.isEmpty) return this
    if (first > rs.last || last < rs.first) return LongRangeSetBuilder.EMPTY

    val it1 = rangeIterator
    val it2 = rs.rangeIterator

    val rsb = new LongRangeSetBuilder

    //boolean indicates if iterator have more data
    var run1: Boolean = it1.moveToNext()
    var run2: Boolean = it2.moveToNext()

   	//problem is that data appended in builder must be sorted
   	//so use two iterators at the same time and produce sorted result
    while (run1 && run2) {//repeat until both iterators have data
      if (it1.last < it2.first) {
        //scroll first iterator until it overlaps
        run1 = it1.moveToNext()
      }
      else if (it2.last < it1.first) {
        //scroll second iterator until it overlaps
        run2 = it2.moveToNext()
      }else if (it1.first < it2.first && it1.last > it2.last) {
        //second range inside first
        rsb.appendRange(it2.first, it2.last)
        run2 = it2.moveToNext()
      }else if (it2.first < it1.first && it2.last > it1.last) {
        //first range inside first
        rsb.appendRange(it1.first, it1.last)
        run1 = it1.moveToNext()
      }else {
        //overlap
        val maxFirst: Long = math.max(it1.first, it2.first)
        val minLast: Long = math.min(it1.last, it2.last)
        rsb.appendRange(maxFirst, minLast)
        if (it1.last < it2.last)
          run1 = it1.moveToNext()
        else
          run2 = it2.moveToNext()
      }
    }
    rsb.build
  }

  /**
   *
   * Construct new LongRangeSet with values which are in original set, but not in parameter.
   * <p>
   * [1-5].substract[4-6] == [1-3]
   * <p>
   * This operation is FAST. Requires only one traversal of ranges.
   * It does not decompress RangeSet to pixels.

   * <p>
   * This operation does not modify original collection.

   * @ p substract this set from original
   * @return result of substraction
   */
  def substract(rs: LongRangeSet): LongRangeSet = {
    if (isEmpty || rs.isEmpty ||first > rs.last || last < rs.first)
      this
    else
      intersect(rs.complement)
  }

  /**
   * @return true if set does not have any ranges
   */
  def isEmpty: Boolean = ranges.length == 0


  def writeExternal(out: ObjectOutput){

    out.writeInt(ranges.length)
    var last: Long = 0
    for (i <- ranges) {
      //write packed differences between values, this way it ocupies less space
      val diff: Long = i - last
      LongPacker.packLong(out, diff)
      last = i
    }
  }

  def readExternal(in: ObjectInput){
  if (ranges != null)
    throw new IllegalAccessError("already initialized")
  val size: Int = in.readInt
  val arr: Array[Long] = new Array[Long](size)
  var last: Long = 0
  for(i<-0 until size){
        val v = last + LongPacker.unpackLong(in)
        arr(i) = v
        last = v
  }
  ranges = arr
}


}