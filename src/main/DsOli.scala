
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
 
 package main

import event._
import simpleopts.SimpleOpts
import scala.collection.mutable.ListBuffer
import org.xml.sax.SAXException
import pointstograph._
import pointstograph.DsOliDiEdge._
import scalax.collection.mutable.Graph
import scalax.collection.GraphPredef._
import scalax.collection.GraphEdge._
import scala.reflect.ClassTag
import scalax.collection.mutable.DefaultGraphImpl
import test._
import extlogger.DsOliLogger
import boxcalculation.DsOliBoxesCreator
import boxcalculation.IDsOliBoxCreator
import typeparser.TypeDB
import typeparser.DsOliTypeParser
import entrypoint.IDsOliEntryPointCreator
import entrypoint.DsOliEntryPointCreator
import entrypoint.IDsOliEPTWriter
import entrypoint.DsOliEPTXMLWriter
import util.DsOliPathUtils
import dsnaming.IDsOliDsNaming
import dsnaming.DsOliDsNaming
import output.DsOliCreateXML
import util.DsOliGeneralPurposeUtils

object DsOli {

  def main(args: Array[String]): Unit = {

    // ***** config area *********
    val debug = false
    val fileLog = false
    val version = "DsOli version 0.5.3"
    val naming = true
    //val createSCXML = naming && true
    val createXML = false
    val createEPTs = false
    val writeEPTs = false
    // ***** config area end *****

    // Set the logger options (see dsoliLogger) 
    if (debug)
      DsOliLogger.setDebugLevel
    else
      DsOliLogger.setErrorLevel
    DsOliLogger.enableFiltersForFile
    if (!fileLog) DsOliLogger.disableFileLog

    try {

      DsOliLogger.info(version)
      println(version)
      DsOliLogger.printLoggerStatus

      // Primitive parser for command line arguments
      val cmdLineParser = new SimpleOpts()
      cmdLineParser.parse(args)

      // Save the current path to the trace file
      DsOliPathUtils.project = cmdLineParser.parsedOptions("xml");
      DsOliLogger.logPath = DsOliPathUtils.getLogFilePath
      println(DsOliPathUtils.project)
      DsOliLogger.printLoggerStatus

      val measuredToSeconds = 1000000000.0
      val startTime: Long = System.nanoTime 

      // Create the type DB
      val typeParser = new DsOliTypeParser(cmdLineParser.parsedOptions("typexml"))
      val typeDB: ITypeDB = typeParser.parseTypes()

      // Create the parser for the XML event trace
      val eventParser: IDsOliEventParser = new DsOliEventParser(
        cmdLineParser.parsedOptions("xml"),
        cmdLineParser.parsedOptions("xsd"),
        typeDB)

      // Parse the trace and return events container
      val startTimeEvents = Some(System.nanoTime) 
      val startTimeAll = startTimeEvents
      val events = eventParser.parse
      val endTimeEvents = Some(System.nanoTime)
      println("DsOli: events parsed")
      DsOliLogger.flushBuffer

      // Pass the created event trace and the type DB to the PTG creator
      val ptgCreator: IDsOliPTGCreator = new DsOliPTGCreator(events, typeDB)
      val startTimePTG = Some(System.nanoTime) 
      val ptgs = ptgCreator.createPTGs
      val endTimePTG = Some(System.nanoTime)
      println("DsOli: ptg created")
      DsOliLogger.flushBuffer

      // Output all results thus far. Omit the boxes
      //DsOliTestMethods.printResults(events, ptgs, null, null, typeDB)

      // Box calculation
      val boxCreator: IDsOliBoxCreator = new DsOliBoxesCreator(events, ptgs, typeDB)
      val startTimeStds = Some(System.nanoTime)
      val boxes = boxCreator.createBoxes
      val endTimeStds = Some(System.nanoTime)
      println("DsOli: boxes created")
      DsOliLogger.flushBuffer

      // Output all results thus far. Omit the epts
      DsOliTestMethods.printResults(events, ptgs, boxes, null, typeDB)

      // Entry point creator needs to be instantiated here to be able to pass it to naming step
      val epCreator: IDsOliEntryPointCreator = new DsOliEntryPointCreator(events, ptgs, boxes, boxCreator.asInstanceOf[DsOliBoxesCreator], typeDB)

      // Ds naming
      if (naming) {
        val dsNamer: DsOliDsNaming = new DsOliDsNaming(events, ptgs, boxes, boxCreator.asInstanceOf[DsOliBoxesCreator], epCreator.asInstanceOf[DsOliEntryPointCreator], typeDB)
        val startTimeNaming = Some(System.nanoTime) 
        val (mbgs, mergedMbgs, aggGraph, epAggCnt, labeledAggGraph) = dsNamer.dsNaming
        // Sequential execution: val (mbgs, mergedMbgs, aggGraph, epAggCnt, labeledAggGraph) = dsNamer.dsNamingSeq
        val endTimeNaming = Some(System.nanoTime)
        println("DsOli: naming done")

        println("Total number of events: " + events.events.length)
        println("Memory events: " + events.events.count(event => event.isInstanceOf[DsOliMemoryEvent]))
        println("Memory write events: " + events.events.count(event => event.isInstanceOf[DsOliMWEvent]))

        // Create the XML output
        val endTimePrint = Some(System.nanoTime) 
        val endTimeAll = endTimePrint
        val totalTime = endTimeAll.get - startTimeAll.get

        DsOliGeneralPurposeUtils.printElapsedTimeInMillis(startTimeAll, endTimeAll, totalTime, "dsAll    time")
        DsOliGeneralPurposeUtils.printElapsedTimeInMillis(startTimeEvents, endTimeEvents, totalTime, "dsEvents time")
        DsOliGeneralPurposeUtils.printElapsedTimeInMillis(startTimePTG, endTimePTG, totalTime, "dsPTG    time")
        DsOliGeneralPurposeUtils.printElapsedTimeInMillis(startTimeStds, endTimeStds, totalTime, "dsStds   time")
        DsOliGeneralPurposeUtils.printElapsedTimeInMillis(startTimeNaming, endTimeNaming, totalTime, "dsNaming time")

        if (createXML) {
          val ptgToXML = new DsOliCreateXML(ptgs, boxes, typeDB, boxCreator.asInstanceOf[DsOliBoxesCreator], mbgs, mergedMbgs, aggGraph, epAggCnt)
          ptgToXML.createXML
          println("DsOli: xml created")
          DsOliTestMethods.printResults(events, ptgs, boxes, null, typeDB)
        }
      }

      // Entry point calculation
      if (createEPTs) {
        // Entry point calculation
        val epts = epCreator.createEPTs()
        println("DsOli: epts created")
        if (writeEPTs) {
          // Write entry point trace to file
          val epWrite: IDsOliEPTWriter = new DsOliEPTXMLWriter(epts, events, cmdLineParser.parsedOptions("featuretracexsd"))
          epWrite.writeEPTTrace
          println("DsOli: epts written")
        }

      }

      // Finally print the graphs for each step
      //DsOliTestMethods.printResults(events, ptgs, boxes, epts, typeDB)

      // At the end flush the buffer, which might still hold some unwritten data
      DsOliLogger.flushBuffer

      val endTime: Long = System.nanoTime 

      println("DsOli: Done")
      println("start time: " + startTime)
      println("end time: " + endTime)
      println("duration: " + ((endTime - startTime) / measuredToSeconds))
    } catch {
      case e: Throwable =>
        DsOliLogger.flushBuffer
        throw e
      case e: SAXException => DsOliLogger.error("XSD/XML Error: " + e.getMessage())
      case e: Exception => DsOliLogger.error("Error occured: " + e.getMessage())
    }
  }

}