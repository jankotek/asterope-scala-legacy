//
// Licenced under GPLv2, see licence.txt
// (c)  Jan Kotek,
//
package org.asterope.healpix

/**
 * An iterator over ranges which does not require object creation
 * <p>
 * !!Implementation must return sorted ranges in iterator!!
 */
trait LongRangeIterator {
  /**move to next Range in iterator
   * @return true if more ranges are in iterator, false if iterator reached end
   */
  def moveToNext(): Boolean

  /**
   * @return first item in current range (inclusive)
   * @throws java.util.NoSuchElementException if no more elements are found
   */
  def first: Long

  /**
   * @return last item in current range (inclusive)
   * @throws java.util.NoSuchElementException if no more elements are found
   */
  def last: Long
}