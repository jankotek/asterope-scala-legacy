/*
 *  Copyright Jan Kotek 2009, http://asterope.org
 *  This program is distributed under GPL Version 3.0 in the hope that
 *  it will be useful, but WITHOUT ANY WARRANTY.
 */
package org.asterope.util

import scala.math._
import java.lang.{AssertionError, IllegalArgumentException}

/**
 * 3D vector. Usually is unit normalized (length=1) and represents point at the sky. 
 * This class is immutable, all operators produces new instances.
 * In many cases vector is represented as Array[Double], 
 * this class have conversion methods.
 *     
 */
@SerialVersionUID(-8665904793228421497L)
case class Vector3d(
                     x: Double,
                     y: Double,
                     z: Double) {

  if(java.lang.Double.isNaN(x) || java.lang.Double.isNaN(y) || java.lang.Double.isNaN(z))
    throw new IllegalArgumentException("not a number (NaN)")

  def this(arr: Array[Double]) = {
    this(arr(0), arr(1), arr(2))
    assert(arr.length == 3)
  }

  /** lazily initialized length */
  lazy val length: Double = sqrt(x * x + y * y + z * z)

  /** 
   * Calculates normalized (with length=1) vector.
   * @return normalized copy of this vector */
  def normalized: Vector3d = lengthMultiply(1d / length);

  /** 
   * Extends size of vector D times.
   * @return vector with size multiplied by d */
  def lengthMultiply(d: Double): Vector3d = Vector3d(x * d, y * d, z * d);

  /** 
   * Calculates dot product of this vector with other vector
   * @return dot product */
  def dot(v: Vector3d): Double = x * v.x + y * v.y + z * v.z;

  /** 
   * Calculates cross product of two vector. Resulting vector have 90 degree angle with both.
   * @return cross product */
  def cross(v: Vector3d): Vector3d = Vector3d(
    y * v.z - z * v.y,
    z * v.x - x * v.z,
    x * v.y - y * v.x);

  /**
   * Calculates angle between two vectors.  
   * @return angle */
  def angle(v: Vector3d): Double = {
    var d = dot(v) / (length * v.length);
    if (d < -1D)
      d = -1D;
    if (d > 1.0D)
      d = 1.0D;
    acos(d);
  }

  /** 
   * Adds two vectors together
   * @return sum*/
  def add(v: Vector3d): Vector3d = Vector3d(x + v.x, y + v.y, z + v.z)

  /** 
   * Subtract argument vector from this  
   * @return subtract */
  def sub(v: Vector3d): Vector3d = Vector3d(x - v.x, y - v.y, z - v.z)

  /** getters for java */
  def getX = x;
  def getY = y;
  def getZ = z;

  def getRaRadian: Double = {
    var phi = 0.0;
    if ((x != 0.0) || (y != 0))
      phi = atan2(y, x); // phi in [-pi,pi]

    if (phi < 0)
      phi += 2.0 * Pi; // phi in [0, 2pi]

    phi;
  }  
  
  def getDeRadian: Double = {
    val z2 = z / length;
    val theta = acos(z2);
    Pi / 2 -theta;
  }
  
  def getRa = getRaRadian.radian
  def getDe = getDeRadian.radian
  
  def toRaDeArray = Array[Double](getRaRadian,getDeRadian)

  def rotateVector(axis: Vector3d, angle: Angle): Vector3d = rotateVector(axis, angle.toRadian)
  /**
   *
   * Rotate vector around other vector by given angle
   * Based on this <a href="http://inside.mines.edu/~gmurray/ArbitraryAxisRotation/ArbitraryAxisRotation.html">work</a>
   * 
   * @param axis about which make rotation
   * @param angle to rotate in radians
   * @return rotated vector
   */
  def rotateVector(axis: Vector3d, angle: Double): Vector3d = {

    /* Step 1 */
    var q1x, q2x = this.x;
    var q1y, q2y = this.y;
    var q1z, q2z = this.z;

    var ux = axis.x;
    var uy = axis.y;
    var uz = axis.z;

    //normalize U
    val ud = 1.0D / sqrt(ux * ux + uy * uy + uz * uz);
    ux *= ud;
    uy *= ud;
    uz *= ud;

    val d = sqrt(uy * uy + uz * uz);

    /* Step 2 */
    if (d != 0) {
      q2x = q1x;
      q2y = q1y * uz / d - q1z * uy / d;
      q2z = q1y * uy / d + q1z * uz / d;
    } else {
      q2x = q1x;
      q2y = q1y;
      q2z = q1z;
    }

    /* Step 3 */
    q1x = q2x * d - q2z * ux;
    q1y = q2y;
    q1z = q2x * ux + q2z * d;

    /* Step 4 */
    q2x = q1x * cos(angle) - q1y * sin(angle);
    q2y = q1x * sin(angle) + q1y * cos(angle);
    q2z = q1z;

    /* Inverse of step 3 */
    q1x = q2x * d + q2z * ux;
    q1y = q2y;
    q1z = -q2x * ux + q2z * d;

    /* Inverse of step 2 */
    if (d != 0) {
      q2x = q1x;
      q2y = q1y * uz / d + q1z * uy / d;
      q2z = -q1y * uy / d + q1z * uz / d;
    } else {
      q2x = q1x;
      q2y = q1y;
      q2z = q1z;
    }

    /* Inverse of step 1 */
    new Vector3d(q2x, q2y, q2z);

  }



  def toArray: Array[Double] = {
    val a = new Array[Double](3)
    a(0) = x;
    a(1) = y;
    a(2) = z;
    a;
  }

  def ~=(v2: Vector3d): Boolean = angle(v2) < 1e-6

  def assertNormalized(){
    if(abs(length - 1) > 1e-6)
      throw new AssertionError("Vector is not normalized: " + this)
  }

  override def toString: String = "Vector3d( x=" + x + ", y=" + y + ", z=" + z +
    ", ra= " + (normalized.getRaRadian * Angle.R2D) + "d, de=" + (normalized.getDeRadian * Angle.R2D) + "d )"

}

object Vector3d {

  def random: Vector3d = Vector3d(math.random -0.5, math.random -0.5, math.random -0.5).normalized

  /** default factory method for Vector3d */
  def apply(xyz: Array[Double]): Vector3d = {
    require(xyz.length == 3)
    new Vector3d(xyz(0), xyz(1), xyz(2));
  }

  def rade2Vector(ra: Angle, de: Angle): Vector3d = rade2Vector(Angle.normalizeRa(ra.toRadian), de.toRadian);

  def rade2Vector(ra: Double, de: Double): Vector3d = {
    Angle.assertRa(ra);
    Angle.assertDe(de);

    val theta = math.Pi / 2 - de
    val phi = ra;
    val stheta = sin(theta);
    val x = stheta * cos(phi);
    val y = stheta * sin(phi);
    val z = cos(theta);
    if (java.lang.Double.isNaN(x) || java.lang.Double.isNaN(y) || java.lang.Double.isNaN(z))
      throw new IllegalArgumentException("can not convert " + ra + " - " + de);
    new Vector3d(x, y, z);
  }

  def assertNormalized(v: Vector3d) {
    require(abs(v.length - 1) < 1e6, "vector is not normalized");
  }

  val zeroPoint = Vector3d(1, 0, 0);

  val northPole = Vector3d(0, 0, 1);

  val southPole = Vector3d(0, 0, -1);

  /** vector where asterope points */
  def asterope = rade2Vector(Angle.parseRa("03", "45", "54.4"), Angle.parseDe("+", "24", "33", "17"));

  /** position of M31 galaxy */
  def m31 = rade2Vector(Angle.parseRa("00", "42", "44.3"), Angle.parseDe("+", "41", "16", "9"));
  def m51 = rade2Vector(Angle.D2R * 202.46820800000003, Angle.D2R * 47.19466700000001);
  def m13 = rade2Vector(Angle.D2R * 250.42266699999996, Angle.D2R * 36.460249999999995);

  /** position of galaxy centre */
  def galaxyCentre = rade2Vector(Angle.parseRa("17", "45", "40.04"), Angle.parseDe("-", "29", "00", "28.1"));

  /** position of galaxy north pole */
  def galaxyNorthPole = rade2Vector(192.859508 * Angle.D2R, 27.128336 * Angle.D2R);

  /** position of galaxy north pole */
  def eclipticNorthPole = rade2Vector(Angle.parseRa("18", "0", "0"), Angle.parseDe("+", "66", "33", "38.6"));

}
