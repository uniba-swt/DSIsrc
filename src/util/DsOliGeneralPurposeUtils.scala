
/**
 *
 * Copyright (C) 2017 University of Bamberg, Software Technologies Research Group
 * <https://www.uni-bamberg.de/>, <http://www.swt-bamberg.de/>
 *
 * This file is part of the Data Structure Investigator (DSI) project, which received financial support by the
 * German Research Foundation (DFG) under grant no. LU 1748/4-1, see
 * <http://www.swt-bamberg.de/dsi/>.
 *
 * DSI is licensed under the GNU GENERAL PUBLIC LICENSE (Version 3), see
 * the LICENSE file at the project's top-level directory for details or consult <http://www.gnu.org/licenses/>.
 *
 * DSI is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or any later version.
 *
 * DSI is a RESEARCH PROTOTYPE and distributed WITHOUT ANY
 * WARRANTY, without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
 * details.
 *
 * The following people contributed to the conception and realization of the present DSI distribution (in
 * alphabetic order by surname):
 *
 * - Jan H. Boockmann
 * - Gerald Lüttgen
 * - Thomas Rupprecht
 * - David H. White
 *
 */
 
 /**
 * @author DSI
 *
 * DsOliGeneralPurposeUtils.scala created on Feb 9, 2016
 *
 * Description: General purpose helper functions for timing
 */
package util

import java.util.concurrent.TimeUnit

object DsOliGeneralPurposeUtils {
  // Flag to indicate if time should be measured
  val measureTime = false
  
  /** 
   * Get the current time in milliseconds
   * 
   */
  def getCurrentTimeInMillis(): Option[Long] = {
    if (!measureTime) return None
    Some(System.currentTimeMillis())
  }

  /**
   * Calculate elapsed time (wall clock)
   * 
   * @param startTime the start time
   * @param msg the msg to add to the returned string
   * @param a string representing the elapsed time
   */
  def calculateElapsedTimeInMillis(startTime: Option[Long], msg: String): Option[String] = {

    if (!measureTime || startTime.isEmpty) return None

    val endTime = DsOliGeneralPurposeUtils.getCurrentTimeInMillis
    if (endTime.isEmpty) return None

    Some(msg + ": wall-clock time elapsed: " + TimeUnit.MILLISECONDS.toSeconds((endTime.get - startTime.get)) + " [s]")

  }

  /**
   * Convert milliseconds to seconds
   * 
   * @param startTime the start time
   * @param endTime the end time
   * @return the elapsed time in seconds
   */
  def millisToSeconds(startTime: Option[Long], endTime: Option[Long]): Option[Long] = {
    Some(TimeUnit.MILLISECONDS.toSeconds((endTime.get - startTime.get)))
  }

  /**
   * Elapsed time
   * 
   * @param startTime the start time
   * @param endTime the end time
   * @return the elapsed time
   */
  def millisDiff(startTime: Option[Long], endTime: Option[Long]): Option[Long] = {
    Some(endTime.get - startTime.get)
  }

  /**
   * Print the elapsed time
   * 
   * @param startTime the start time
   * @param msg the msg to print
   */
  def printElapsedTimeInMillis(startTime: Option[Long], msg: String): Unit = {
    if (!measureTime) return
    println(DsOliGeneralPurposeUtils.calculateElapsedTimeInMillis(startTime, msg).get)
  }

  /**
   * Print the elapsed time in milliseconds
   * 
   * @param startTime the start time
   * @param endTime the end time
   * @param msg the msg to print
   */
  def printElapsedTimeInMillis(startTime: Option[Long], endTime: Option[Long], totalTime: Long, msg: String): Unit = {
    if (!measureTime) return
    //println(msg + ": wall-clock time elapsed: " + millisDiff(startTime, endTime).get + " [ms] -> " + (millisDiff(startTime, endTime).get / 1000.0) +
    //" [s] percent of total: " + (DsOliGeneralPurposeUtils.round2Digits((millisDiff(startTime, endTime).get.toFloat / totalTime) * 100)) + "%")

    val measuredToSeconds = 1000000000.0
    println("dsi-timing: " + msg + ": " + (millisDiff(startTime, endTime).get / measuredToSeconds) + " [s]")
  }

  /**
   * [Deprecated]
   */
  def logElapsedTimeInMillis(): Unit = {
    // Deprecated
  }

  /**
   * Round to 2 digits
   * 
   * @param floatVal the float value
   * @return the float rounded to two digits
   */
  def round2Digits(floatVal: Float): Float = {
    Math.round(floatVal * 100).toFloat / 100
  }
}