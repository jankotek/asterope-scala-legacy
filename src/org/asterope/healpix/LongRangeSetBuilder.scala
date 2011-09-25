//
// Licenced under GPLv2, see licence.txt
// (c) Jan Kotek,
//
package org.asterope.healpix


/**
 * Builder for LongRangeSet . LongRangeSet is unmodifiable, this class is
 * 'factory' to create new instances.
 * <P>
 * To keep it fast and simple, this fab only supports appending. Uour ranges must be already
 * sorted. This work  for most of Healpix based operations.
 * <p>
 * LongRangeSet can also be constructed using {@link LongSet}
 *
 */
object LongRangeSetBuilder {
  /**
   * empty LongRangeSet
   */
  val EMPTY: LongRangeSet = new LongRangeSet(new Array[Long](0), 0)

  object EMPTY_ITERATOR extends LongRangeIterator {
    def first: Long = throw new NoSuchElementException
    def last: Long = throw new NoSuchElementException
    def moveToNext(): Boolean = false
  }


  object EMPTY_LONG_ITERATOR extends Iterator[Long]{
    def hasNext: Boolean = false
    def next(): Long = throw new NoSuchElementException
    def nextLong(): Long = throw new NoSuchElementException
  }


}

class LongRangeSetBuilder(arraySize:Int) {

  if (arraySize % 2 != 0) throw new IllegalArgumentException("not divide by 2")
  /**sorted list of ranges.*/
  protected var ranges: Array[Long] = new Array[Long](arraySize)
  /**current position*/
  protected var pos: Int = 0


  def this() = this(32)


  /**make sure underling array have at least given size*/
  def ensureSize(arraySize: Int){
    if (arraySize % 2 != 0) throw new IllegalArgumentException("not divide by 2")
    if (ranges.length < arraySize) {
      val newRanges: Array[Long] = new Array[Long](arraySize)
      System.arraycopy(ranges, 0, newRanges, 0, ranges.length)
      ranges = newRanges
    }
  }

  /**append single long into builder
   * @param first - long to append
   */
  def append(first: Long){
    appendRange(first, first)
  }

  /**
   * append range into builder
   * @param first long in range (inclusive)
   * @param last long in range(inclusive)
   */
  def appendRange(first: Long, last: Long){
    if (first > last) throw new IllegalArgumentException("first > last")
    if (pos > 0) {
      if (twoOrBigger && first <= lastLast)
        throw new IllegalArgumentException("Could not merge, ranges must be added sorted! lastLast:" + lastLast + ", newFirst:" + first)
      if (first <= lastX + 1) {
         //Check if new range overlaps with last one.
         //In this case update last range, instead of adding new one

        ranges(pos - 2) = math.min(first, lastFirst)
        ranges(pos - 1) = math.max(last, lastX)
        return
      }
    }
    //make sure there is space
    if (pos + 2 > ranges.length)
      ensureSize(ranges.length * 2)
    //insert
    ranges(pos) = first
    ranges(pos + 1) = last
    pos += 2
  }

  protected def lastX: Long = {
    ranges(pos - 1)
  }

  protected def twoOrBigger: Boolean = {
    pos > 3
  }

  protected def lastLast: Long = {
    ranges(pos - 3)
  }

  protected def lastFirst: Long = {
    ranges(pos - 2)
  }

  /**
   * appends all ranges from iterator
   * @param iter LongRangeIterator
   */
  def appendRanges(iter: LongRangeIterator){
    while (iter.moveToNext) appendRange(iter.first, iter.last)
  }

  /**
   * append all ranges from given LongRangeSet
   * @param set LongRangeSet to append
   */
  def appendRangeSet(set: LongRangeSet){
    appendRanges(set.rangeIterator)
  }

  /**@return number of added ranges so far*/
  def size: Int = {
    pos / 2
  }

  /**
   * Construct new LongRangeSet from appended values *
   * @return LongRangeSet with appended values
   */
  def build: LongRangeSet = {
    if (pos == 0) return LongRangeSetBuilder.EMPTY
    new LongRangeSet(ranges, pos)
  }

}