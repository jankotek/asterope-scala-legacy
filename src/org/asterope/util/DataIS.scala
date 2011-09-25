package org.asterope.util


import java.net.URL
import java.io.{FileInputStream, File, BufferedInputStream, InputStream}

class DataIS(is:InputStream ) extends java.io.DataInputStream(is){

  def this(s:String) = this(new BufferedInputStream(new URL(s).openStream()))
  def this(file:File) = this(new BufferedInputStream(new FileInputStream(file)))

  def readLongLE = java.lang.Long.reverseBytes(readLong())
  def readIntLE = java.lang.Integer.reverseBytes(readInt())
  def readShortLE = java.lang.Short.reverseBytes(readShort())

  def readFloatLE = java.lang.Float.intBitsToFloat(readIntLE);
  def readDoubleLE = java.lang.Double.longBitsToDouble(readLongLE);

}