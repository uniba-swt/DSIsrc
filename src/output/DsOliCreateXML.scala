
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
 * DsOliCreateXML.scala created on Dec 11, 2015
 *
 * Description: Create the XML output to be used by various backends
 */
package output

import pointstograph.DsOliPointsToGraphs
import boxcalculation.DsOliBoxSteps
import pointstograph.ITypeDB
import scala.collection.mutable.HashMap
import pointstograph.DsOliVertex
import pointstograph.DsOliVertexMemory
import pointstograph.DsOliDiEdge
import boxcalculation.DsOliCell
import boxcalculation.DsOliBox
import scala.collection.mutable.ListBuffer
import boxcalculation.DsOliBoxesCreator
import java.io.PrintWriter
import java.io.File
import test.DsOliTestMethods
import extutil.DsOliPathUtils
import util.DsOliTimeStepContainer
import dsnaming.DsOliMetaBoxGraph
import dsnaming.DsOliMetaBoxGraphVertexBoxes
import scala.util.control.Breaks._
import util.DsOliGraphUtils
import dsnaming.DsOliConConfTag
import dsnaming.DsOliConConfTag._
import dsnaming.DsOliConConfClassificationTag
import dsnaming.DsOliConConfClassificationTag._
import dsnaming.DsOliMetaBoxGraphDiEdge
import dsnaming.DsOliMetaBoxGraphVertex
import dsnaming.DsOliMetaBoxGraphVertexEP
import dsnaming.DsOliConConf
import dsnaming.DsOliMetaBoxGraphVertexBoxes
import util.DsOliAddressUtils

/**
 * @author DSI
 *
 */
class DsOliCreateXML(ptgs: DsOliPointsToGraphs,
  boxSteps: DsOliBoxSteps,
  typeDB: ITypeDB,
  boxesCreator: DsOliBoxesCreator,
  dsOliMbgs: DsOliTimeStepContainer[DsOliMetaBoxGraph],
  dsOliMergedMbgs: DsOliTimeStepContainer[DsOliMetaBoxGraph],
  aggGraphs: DsOliTimeStepContainer[(DsOliVertexMemory, DsOliMetaBoxGraph)],
  epAggCnt: ListBuffer[(DsOliVertexMemory, Int, Int, Int)]) {

  val paddingIndent = "    "

  val position = "position"
  val positionOpen = "<" + position + ">"
  val positionClose = "</" + position + ">"

  val ptg = "ptg"
  val ptgOpen = "<" + ptg + ">"
  val ptgClose = "</" + ptg + ">"

  val addr = "addr"
  val addrOpen = "<" + addr + ">"
  val addrClose = "</" + addr + ">"

  val addrAsVertex = "addrAsVertex"
  val addrAsVertexOpen = "<" + addrAsVertex + ">"
  val addrAsVertexClose = "</" + addrAsVertex + ">"

  val bAddr = "bAddr"
  val bAddrOpen = "<" + bAddr + ">"
  val bAddrClose = "</" + bAddr + ">"

  val eAddr = "eAddr"
  val eAddrOpen = "<" + eAddr + ">"
  val eAddrClose = "</" + eAddr + ">"

  val strand = "strand"
  val strandOpen = "<" + strand + ">"
  val strandClose = "</" + strand + ">"

  val strands = "strands"
  val strandsOpen = "<" + strands + ">"
  val strandsClose = "</" + strands + ">"

  val strandId = "id"
  val strandIdOpen = "<" + strandId + ">"
  val strandIdClose = "</" + strandId + ">"

  val cell = "cell"
  val cellOpen = "<" + cell + ">"
  val cellClose = "</" + cell + ">"

  val cellType = "cellType"
  val cellTypeOpen = "<" + cellType + ">"
  val cellTypeClose = "</" + cellType + ">"

  val cellRef = "cellRef"
  val cellRefOpen = "<" + cellRef + ">"
  val cellRefClose = "</" + cellRef + ">"

  val cellPosition = "position"
  val cellPositionOpen = "<" + cellPosition + ">"
  val cellPositionClose = "</" + cellPosition + ">"

  val cellId = "cellId"
  val cellIdOpen = "<" + cellId + ">"
  val cellIdClose = "</" + cellId + ">"

  val cells = "cells"
  val cellsOpen = "<" + cells + ">"
  val cellsClose = "</" + cells + ">"

  val CpointerHex = "CpointerHex"
  val CpointerHexOpen = "<" + CpointerHex + ">"
  val CpointerHexClose = "</" + CpointerHex + ">"

  val cellSequences = "cellSequences"
  val cellSequencesOpen = "<" + cellSequences + ">"
  val cellSequencesClose = "</" + cellSequences + ">"

  val cellSequence = "cellSequence"
  val cellSequenceOpen = "<" + cellSequence + ">"
  val cellSequenceClose = "</" + cellSequence + ">"

  val cyclicSequence = "cyclicSequence"
  val cyclicSequenceOpen = "<" + cyclicSequence + ">"
  val cyclicSequenceClose = "</" + cyclicSequence + ">"

  val linearSequence = "linearSequence"
  val linearSequenceOpen = "<" + linearSequence + ">"
  val linearSequenceClose = "</" + linearSequence + ">"

  val cycle = "cycle"
  val cycleOpen = "<" + cycle + ">"
  val cycleClose = "</" + cycle + ">"

  val cycleId = "cycleId"
  val cycleIdOpen = "<" + cycleId + ">"
  val cycleIdClose = "</" + cycleId + ">"

  val cycles = "cycles"
  val cyclesOpen = "<" + cycles + ">"
  val cyclesClose = "</" + cycles + ">"

  val edge = "edge"
  val edgeOpen = "<" + edge + ">"
  val edgeClose = "</" + edge + ">"

  val edgeId = "id"
  val edgeIdOpen = "<" + edgeId + ">"
  val edgeIdClose = "</" + edgeId + ">"

  val edges = "edges"
  val edgesOpen = "<" + edges + ">"
  val edgesClose = "</" + edges + ">"

  val id = "id"
  val idOpen = "<" + id + ">"
  val idClose = "</" + id + ">"

  val linkageOffset = "linkageOffset"
  val linkageOffsetOpen = "<" + linkageOffset + ">"
  val linkageOffsetClose = "</" + linkageOffset + ">"

  val name = "name"
  val nameOpen = "<" + name + ">"
  val nameClose = "</" + name + ">"

  val offset = "offset"
  val offsetOpen = "<" + offset + ">"
  val offsetClose = "</" + offset + ">"

  val size = "size"
  val sizeOpen = "<" + size + ">"
  val sizeClose = "</" + size + ">"

  val source = "source"
  val sourceOpen = "<" + source + ">"
  val sourceClose = "</" + source + ">"

  val struct = "struct"
  val structOpen = "<" + struct + ">"
  val structClose = "</" + struct + ">"

  val target = "target"
  val targetOpen = "<" + target + ">"
  val targetClose = "</" + target + ">"

  val typeTag = "type"
  val typeOpen = "<" + typeTag + ">"
  val typeClose = "</" + typeTag + ">"

  val vertex = "vertex"
  val vertexOpen = "<" + vertex + ">"
  val vertexClose = "</" + vertex + ">"

  val vertexId = "id"
  val vertexIdOpen = "<" + vertexId + ">"
  val vertexIdClose = "</" + vertexId + ">"

  val addrAsVertexId = "vertexId"
  val addrAsVertexIdOpen = "<" + addrAsVertexId + ">"
  val addrAsVertexIdClose = "</" + addrAsVertexId + ">"

  val vertices = "vertices"
  val verticesOpen = "<" + vertices + ">"
  val verticesClose = "</" + vertices + ">"

  val beginEventTimeStep = "beginEventTimeStep"
  val beginEventTimeStepOpen = "<" + beginEventTimeStep + ">"
  val beginEventTimeStepClose = "</" + beginEventTimeStep + ">"

  val endEventTimeStep = "ceaseEventTimeStep"
  val endEventTimeStepOpen = "<" + endEventTimeStep + ">"
  val endEventTimeStepClose = "</" + endEventTimeStep + ">"

  val classSignature = "DsOliCreateXML"

  /**
   * Fetch all vertices and edges from the PTGs over all time steps.
   * Additionally calculate the start and end times for each vertex and edge.
   *
   * @param verticesStore where the vertices with their start and end time get stored
   * @param edgesStore where the edges with their start and end time get stored
   *
   */
  def calculateVerticesAndEdges(verticesStore: HashMap[Long, (DsOliVertex, Long, Long)],
    edgesStore: HashMap[Long, (DsOliDiEdge[DsOliVertex], Long, Long)]): Unit = {

    // Cycle through PTGs for each time step
    for (i <- 0 until ptgs.graphs.length) {
      val graph = ptgs.graphs(i)

      // Process the vertices
      graph.graph.nodes.iterator.foreach {
        node =>
          val vertex = node.value
          // If not present, store start time
          if (!verticesStore.contains(vertex.id)) {
            verticesStore.put(vertex.id, (vertex, i, -1))
          }

          // Always update the end time, so once vertex disappears the end time 
          // is automatically the last time step where the vertex was alive
          val (vertexRef, start, end) = verticesStore.get(vertex.id).get
          verticesStore.put(vertex.id, (vertexRef, start, i))
      }

      // Process the edges
      graph.graph.edges.iterator.foreach {
        edge =>
          val outerEdge = edge.toOuter
          // If not present, store start time
          if (!edgesStore.contains(outerEdge.id)) {
            edgesStore.put(outerEdge.id, (outerEdge, i, -1))
          }

          // Always update the end time, so once outerEdge disappears the end time 
          // is automatically the last time step where the outerEdge was alive
          val (outerEdgeRef, start, end) = edgesStore.get(outerEdge.id).get
          edgesStore.put(outerEdge.id, (outerEdgeRef, start, i))
      }
    }
  }

  /**
   * Generic method for writing a tag and its attributes.
   *
   * @param tag the XML tag
   * @param id the id of the tag
   * @param begin start time
   * @param end end time
   * @param xmlBuffer the StringBuffer to write to
   */
  def writeAttribues(tag: String, id: Long, begin: Long, end: Long, xmlBuffer: StringBuffer): Unit = {
    xmlBuffer.append("<" + tag + " ")

    xmlBuffer.append("id=\"")
    xmlBuffer.append(id)
    xmlBuffer.append("\" ")

    xmlBuffer.append(beginEventTimeStep + "=\"")
    xmlBuffer.append(begin)
    xmlBuffer.append("\" ")

    xmlBuffer.append(endEventTimeStep + "=\"")
    xmlBuffer.append(end)
    xmlBuffer.append("\" >")

  }

  /**
   * Generic method to write an XML element.
   *
   * @param elementOpen opening XML tag
   * @param elementClose closing XML tag
   * @param value the actual value of the the element
   * @param xmlBuffer the StringBuffer to write to
   */
  def writeElement(elementOpen: String, elementClose: String, value: String, xmlBuffer: StringBuffer): Unit = {
    xmlBuffer.append(elementOpen)
    xmlBuffer.append(value)
    xmlBuffer.append(elementClose + "\n")
  }

  /**
   * Write the found vertices to the XML buffer
   *
   * @param xmlBuffer the StringBuffer for storing the XML
   * @param verticesStore the vertices together with their start/end times
   */
  def writeVertices(xmlBuffer: StringBuffer, verticesStore: HashMap[Long, (DsOliVertex, Long, Long)]): Unit = {
    xmlBuffer.append(verticesOpen)
    verticesStore.values.toSeq.sortBy(_._1.id).foreach {
      vals =>
        val (vertexRef, begin, end) = vals
        if (vertexRef.isInstanceOf[DsOliVertexMemory]) {
          val memVertex = vertexRef.asInstanceOf[DsOliVertexMemory]

          writeAttribues("vertex", vertexRef.id, begin, end, xmlBuffer)

          // Write the vertex properties: start/end address, size, type
          writeElement(bAddrOpen, bAddrClose, "0x" + memVertex.bAddr.toHexString, xmlBuffer)
          writeElement(eAddrOpen, eAddrClose, "0x" + memVertex.eAddr.toHexString, xmlBuffer)
          writeElement(sizeOpen, sizeClose, memVertex.vType.size.toString, xmlBuffer)
          writeElement(typeOpen, typeClose, memVertex.vType.vType, xmlBuffer)

          xmlBuffer.append(vertexClose + "\n")
        }
    }
    xmlBuffer.append(verticesClose + "\n")
  }

  /**
   * Write the edge element.
   *
   * @param elemOpen the edge opening tag
   * @param elemClose the edge closing tag
   * @param id the edge id
   * @param addr the start address of the vertex where the edge originates
   * @param offset the offset from addr where the edge actually starts (addr + offset = edge start addr)
   * @param xmlBuffer the StringBuffer to write to
   */
  def writeEdgeElement(elemOpen: String, elemClose: String, id: Long, addr: Long, offset: Long, xmlBuffer: StringBuffer): Unit = {

    xmlBuffer.append(elemOpen)
    xmlBuffer.append(addrAsVertexOpen)
    writeElement(addrAsVertexIdOpen, addrAsVertexIdClose, id.toString, xmlBuffer)
    writeElement(offsetOpen, offsetClose, offset.toString, xmlBuffer)
    xmlBuffer.append(addrAsVertexClose)
    writeElement(addrOpen, addrClose, "0x" + (addr + offset).toHexString, xmlBuffer)
    xmlBuffer.append(elemClose + "\n")

  }

  /**
   * Write the edges into the XML Buffer.
   *
   * @param xmlBuffer the StringBuffer to write to
   * @param edgesStore the edges with their corresponding start/end times
   */
  def writeEdges(xmlBuffer: StringBuffer, edgesStore: HashMap[Long, (DsOliDiEdge[DsOliVertex], Long, Long)]): Unit = {
    xmlBuffer.append(edgesOpen)
    edgesStore.values.toSeq.sortBy(_._1.id).foreach {
      vals =>
        val (edgeRef, begin, end) = vals
        writeAttribues("edge", edgeRef.id, begin, end, xmlBuffer)

        writeEdgeElement(sourceOpen, sourceClose, edgeRef.source.id, edgeRef.sAddr, edgeRef.sOffset, xmlBuffer)
        writeEdgeElement(targetOpen, targetClose, edgeRef.target.id, edgeRef.tAddr, edgeRef.tOffset, xmlBuffer)

        xmlBuffer.append(edgeClose + "\n")
    }
    xmlBuffer.append(edgesClose + "\n")

  }

  /**
   * Write the edges into the XML Buffer.
   *
   * @param xmlBuffer the StringBuffer to write to
   * @param edgesStore the edges with their corresponding start/end times
   */

  def writeCells(xmlBuffer: StringBuffer, cellsStore: HashMap[Long, (DsOliCell, Long, Long)]): Unit = {
    xmlBuffer.append(cellsOpen)
    cellsStore.values.toSeq.sortBy(_._1.id).foreach {
      vals =>
        val (cellRef, begin, end) = vals
        writeAttribues("cell", cellRef.id, begin, end, xmlBuffer)

        writeElement(nameOpen, nameClose, "TBD", xmlBuffer)
        writeElement(typeOpen, typeClose, cellRef.cType.vType, xmlBuffer)
        writeElement(bAddrOpen, bAddrClose, "0x" + cellRef.bAddr.toHexString, xmlBuffer)
        writeElement(eAddrOpen, eAddrClose, "0x" + cellRef.eAddr.toHexString, xmlBuffer)
        writeElement(sizeOpen, sizeClose, cellRef.cType.size.toString, xmlBuffer)
        writeElement(addrAsVertexIdOpen, addrAsVertexIdClose, cellRef.vertexId.toString, xmlBuffer)

        xmlBuffer.append(cellClose + "\n")
    }
    xmlBuffer.append(cellsClose + "\n")

  }

  /**
   * Fetch all strands and cells over all time steps. Additionally
   * calculate the start and end times for each strand and cell.
   *
   * @param cellsStore where the cells with their start and end time get stored
   * @param strandsStore where the strands with their start and end time get stored
   *
   */
  def calculateStrandsAndCells(cellsStore: HashMap[Long, (DsOliCell, Long, Long)],
    strandsStore: HashMap[Long, (DsOliBox, Long, Long)]): Unit = {

    // Cycle through all strands for each time step
    for (i <- 0 until boxSteps.boxSteps.length) {
      // Fetch the current time step
      val boxStep = boxSteps.boxSteps(i)

      // Cycle through all strands for the time step
      boxStep.boxes.foreach {
        boxTuple =>
          val (boxId, box) = boxTuple
          // If not present, store start time
          if (!strandsStore.contains(box.id)) {
            strandsStore.put(box.id, (box, i, -1))
          }

          // Always update the end time, so once box disappears the end time 
          // is automatically the last time step where the box was alive
          val (boxRef, start, end) = strandsStore.get(box.id).get
          strandsStore.put(box.id, (boxRef, start, i))

          // Linear sequence
          box.cells.foreach {
            cell =>
              // If not present, store start time
              if (!cellsStore.contains(cell.id)) {
                cellsStore.put(cell.id, (cell, i, -1))
              }

              // Always update the end time, so once cell disappears the end time 
              // is automatically the last time step where the cell was alive
              val (cellRef, start, end) = cellsStore.get(cell.id).get
              cellsStore.put(cell.id, (cellRef, start, i))

          }

          // Cyclic sequence
          if (box.cycleId != 0) {
            boxStep.cycles.get(box.cycleId).get.cells.foreach {
              cell =>
                // If not present, store start time
                if (!cellsStore.contains(cell.id)) {
                  cellsStore.put(cell.id, (cell, i, -1))
                }

                // Always update the end time, so once cell disappears the end time 
                // is automatically the last time step where the cell was alive
                val (cellRef, start, end) = cellsStore.get(cell.id).get
                cellsStore.put(cell.id, (cellRef, start, i))

            }
          }
      }
    }

  }

  /**
   * Write the cells of the linear part of the strand
   *
   * @param xmlBuffer the StringBuffer to write to
   * @param strandRef the strand
   */
  def writeLinearCells(xmlBuffer: StringBuffer, strandRef: DsOliBox): Unit = {

    xmlBuffer.append(linearSequenceOpen)
    for (i <- 0 until strandRef.cells.length) {
      xmlBuffer.append("<cellRef ")
      xmlBuffer.append(cellPosition + "=\"" + i + "\" " + cellId + "=\"" + strandRef.cells(i).id + "\"/>")
    }
    xmlBuffer.append(linearSequenceClose + "\n")

  }

  /**
   * Write the cells of the cyclic part of the strand
   *
   * @param xmlBuffer the StringBuffer to write to
   * @param begin the start time of the strand
   * @param end the end time of the strand
   * @param strandRef the strand
   */
  def writeCyclicCells(xmlBuffer: StringBuffer, begin: Long, end: Long, strandRef: DsOliBox): Unit = {
    xmlBuffer.append(cyclicSequenceOpen)
    if (strandRef.cycleId != 0) {
      val cycle = boxSteps.boxSteps(begin.toInt).cycles.get(strandRef.cycleId).get
      val cycleEntryCell = cycle.cells.find(_.id == strandRef.cycleEntryPoint).get
      // Get the entry cell into the cycle
      val cycleEntryCellIndex = boxesCreator.getCellIndexFromList(cycleEntryCell, cycle.cells).get
      // Get the slice up until (not including) the entry cell
      val firstSlice = cycle.cells.slice(0, cycleEntryCellIndex)
      // Get the slice from the entry to the end
      val secondSlice = cycle.cells.slice(cycleEntryCellIndex, cycle.cells.size)
      // Start by printing the second slice, which is the entry point. 0 is the index where we start counting
      xmlBuffer.append(printCyclicSlice(secondSlice, 0))
      // Now print the remaining part of the cycle, which starts at index secondSlice.length
      xmlBuffer.append(printCyclicSlice(firstSlice, secondSlice.length))
    }
    xmlBuffer.append(cyclicSequenceClose + "\n")
  }

  /**
   * Write the strands into the XML Buffer.
   *
   * @param xmlBuffer the StringBuffer to write to
   * @param strandsStore the strands with their corresponding start/end times
   */
  def writeStrands(xmlBuffer: StringBuffer, strandsStore: HashMap[Long, (DsOliBox, Long, Long)]): Unit = {
    xmlBuffer.append(strandsOpen)
    strandsStore.values.toSeq.sortBy(_._1.id).foreach {
      vals =>
        val (strandRef, begin, end) = vals

        writeAttribues("strand", strandRef.id, begin, end, xmlBuffer)

        writeElement(cellTypeOpen, cellTypeClose, strandRef.cType.vType, xmlBuffer)
        writeElement(linkageOffsetOpen, linkageOffsetClose, strandRef.offset.toString, xmlBuffer)

        // Write the linear and cyclic cell sequences
        // Open XML tags
        xmlBuffer.append(cellSequencesOpen)
        xmlBuffer.append("<cellSequence ")
        xmlBuffer.append(beginEventTimeStep + "=\"")
        xmlBuffer.append(begin)
        xmlBuffer.append("\" ")

        xmlBuffer.append(endEventTimeStep + "=\"")
        xmlBuffer.append(end)
        xmlBuffer.append("\">\n")

        // Write the linear cell sequence
        writeLinearCells(xmlBuffer, strandRef)

        // Write the cyclic cell sequence
        writeCyclicCells(xmlBuffer, begin, end, strandRef)

        // Close XML tags
        xmlBuffer.append(cellSequenceClose + "\n")
        xmlBuffer.append(cellSequencesClose + "\n")
        xmlBuffer.append(strandClose + "\n")
    }
    xmlBuffer.append(strandsClose + "\n")
  }

  def printCyclicSlice(cells: ListBuffer[DsOliCell], index: Int): String = {
    val xmlBuffer = new StringBuffer()
    var itIndex = index
    cells.foreach {
      cell =>
        xmlBuffer.append("<cellRef ")
        xmlBuffer.append(cellPosition + "=\"" + itIndex + "\" " + cellId + "=\"" + cell.id + "\"/>")
        itIndex += 1
    }
    xmlBuffer.toString
  }

  val strandConns = "<strand-connections>"
  val strandConnsEnd = "</strand-connections>"

  val strandConn = "<strand-connection"
  val strandConnEnd = "</strand-connection>"

  val overlay = "<overlay/>"
  val indirect = "<indirect/>"

  val kindTag = "<kind>"
  val kindTagEnd = "</kind>"

  val label = "<label>"
  val labelEnd = "</label>"

  val globalLabel = "<global-label"
  val globalLabelEnd = "</global-label>"

  val localLabel = "<local-label"
  val localLabelEnd = "</local-label>"

  val curLabel = "<cur-label>"
  val curLabelEnd = "</cur-label>"

  val nestingInd = "<nesting-indirect>"
  val nestingIndEnd = "</nesting-indirect>"

  val nestingOvl = "<nesting-overlay>"
  val nestingOvlEnd = "</nesting-overlay>"

  val parent = "<parent"
  val parentEnd = "</parent>"

  val child = "<child"
  val childEnd = "</child>"

  val epochs = "<epochs>"
  val epochsEnd = "</epochs>"

  val epoch = "<epoch"
  val epochEnd = "</epoch>"

  val cellPair = "<cell-pair "
  val cellPairEnd = "</cell-pair>"

  val cellPairs = "<cell-pairs "
  val cellPairsEnd = "</cell-pairs>"

  val cellIdLeft = "<cell-id-left>"
  val cellIdLeftEnd = "</cell-id-left>"

  val cellIdRight = "<cell-id-right>"
  val cellIdRightEnd = "</cell-id-right>"

  val i1o = "<intersecting-1node-overlay>"
  val i1oEnd = "</intersecting-1node-overlay>"

  val shn = "<shared-head-node "
  val shnEnd = "</shared-head-node>"

  val i2o = "<intersecting-2node-overlay>"
  val i2oEnd = "</intersecting-2node-overlay>"

  val i1i = "<intersecting-1node-indirect>"
  val i1iEnd = "</intersecting-1node-indirect>"

  val list1 = "<list1"
  val list1End = "</list1>"

  val list2 = "<list2"
  val list2End = "</list2>"

  val dll = "<dll>"
  val dllEnd = "</dll>"

  val cdll = "<cdll>"
  val cdllEnd = "</cdll>"

  val fwd = "<fwd"
  val fwdEnd = "</fwd>"

  val rev = "<rev"
  val revEnd = "</rev>"

  val unclassified = "<unclassified/>"
  def writeEdgeLabel(xmlBuffer: StringBuffer, highestTag: DsOliConConfClassificationTag,
    evidence: Int,
    sourceBox: DsOliMetaBoxGraphVertexBoxes, sourceId: Long,
    targetBox: DsOliMetaBoxGraphVertexBoxes, targetId: Long,
    strandSetIdName: String): Unit = {
    xmlBuffer.append("<label>\n")
    if (highestTag == DsOliConConfClassificationTag.No) {
      xmlBuffer.append("<nesting-overlay evidence=\"" + evidence + "\">\n")
      xmlBuffer.append(parent + " " + strandSetIdName + "=\"" + sourceId + "\"/>\n")
      xmlBuffer.append(child + " " + strandSetIdName + "=\"" + targetId + "\" />\n")
      xmlBuffer.append(nestingOvlEnd + "\n")

    } else if (highestTag == DsOliConConfClassificationTag.Ni) {
      xmlBuffer.append("<nesting-indirect evidence=\"" + evidence + "\">\n")
      xmlBuffer.append(parent + " " + strandSetIdName + "=\"" + sourceId + "\"/>\n")
      xmlBuffer.append(child + " " + strandSetIdName + "=\"" + targetId + "\" />\n")
      xmlBuffer.append(nestingIndEnd + "\n")
    } else if (highestTag == DsOliConConfClassificationTag.CDLL) {
      val (fwdId, revId) = if (sourceBox.boxes.head.offset < targetBox.boxes.head.offset) {
        (sourceId, targetId)
      } else {
        (targetId, sourceId)
      }
      xmlBuffer.append("<cdll evidence=\"" + evidence + "\">\n")
      xmlBuffer.append(fwd + " " + strandSetIdName + "=\"" + fwdId + "\"/>\n")
      xmlBuffer.append(rev + " " + strandSetIdName + "=\"" + revId + "\"/>\n")
      xmlBuffer.append(cdllEnd + "\n")
    } else if (highestTag == DsOliConConfClassificationTag.DLL) {
      val (fwdId, revId) = if (sourceBox.boxes.head.offset < targetBox.boxes.head.offset) {
        (sourceId, targetId)
      } else {
        (targetId, sourceId)
      }
      xmlBuffer.append("<dll evidence=\"" + evidence + "\">\n")
      xmlBuffer.append(fwd + " " + strandSetIdName + "=\"" + fwdId + "\"/>\n")
      xmlBuffer.append(rev + " " + strandSetIdName + "=\"" + revId + "\"/>\n")
      xmlBuffer.append(dllEnd + "\n")
    } else if (highestTag == DsOliConConfClassificationTag.I1i) {
      xmlBuffer.append("<intersecting-1node-indirect evidence=\"" + evidence + "\">\n")
      xmlBuffer.append(list1 + " " + strandSetIdName + "=\"" + sourceId + "\"/>\n")
      xmlBuffer.append(list2 + " " + strandSetIdName + "=\"" + targetId + "\"/>\n")
      xmlBuffer.append(i1iEnd + "\n")
    } else if (highestTag == DsOliConConfClassificationTag.I1o) {
      xmlBuffer.append("<intersecting-1node-overlay evidence=\"" + evidence + "\">\n")
      xmlBuffer.append(list1 + " " + strandSetIdName + "=\"" + sourceId + "\"/>\n")
      xmlBuffer.append(list2 + " " + strandSetIdName + "=\"" + targetId + "\"/>\n")
      xmlBuffer.append(i1oEnd + "\n")
    } else if (highestTag == DsOliConConfClassificationTag.I2o) {
      val (fwdId, revId) = if (sourceBox.boxes.head.offset < targetBox.boxes.head.offset) {
        (sourceId, targetId)
      } else {
        (targetId, sourceId)
      }
      xmlBuffer.append("<intersecting-2node-overlay evidence=\"" + evidence + "\">\n")
      xmlBuffer.append(list1 + " " + strandSetIdName + "=\"" + fwdId + "\"/>\n")
      xmlBuffer.append(list2 + " " + strandSetIdName + "=\"" + revId + "\"/>\n")
      xmlBuffer.append(i2oEnd + "\n")

    } else if (highestTag == DsOliConConfClassificationTag.SHN) {
      xmlBuffer.append(shn + " evidence=\"" + evidence + "\">\n")
      xmlBuffer.append(list1 + " " + strandSetIdName + "=\"" + sourceId + "\"/>\n")
      xmlBuffer.append(list2 + " " + strandSetIdName + "=\"" + targetId + "\"/>\n")
      xmlBuffer.append(shnEnd + "\n")
    } else {
      xmlBuffer.append(" <!-- " + highestTag + " -->\n")
    }
    xmlBuffer.append("</label>\n")
  }

  /**
   * Write the strand label
   *
   * @param xmlBuffer the StringBuffer to write to
   * @param highestTag the label of the strand connection
   * @param sourceBox the source vertex
   * @param targetBox the target vertex
   */
  def writeStrandLabel(xmlBuffer: StringBuffer, highestTag: DsOliConConfClassificationTag, sourceBox: DsOliBox, targetBox: DsOliBox): Unit = {
    xmlBuffer.append(label + "\n")
    if (highestTag == DsOliConConfClassificationTag.No) {
      xmlBuffer.append(nestingOvl + "\n")
      xmlBuffer.append(parent + " strandId=\"" + sourceBox.id + "\"/>\n")
      xmlBuffer.append(child + " strandId=\"" + targetBox.id + "\" />\n")
      xmlBuffer.append(nestingOvlEnd + "\n")

    } else if (highestTag == DsOliConConfClassificationTag.Ni) {
      xmlBuffer.append(nestingInd + "\n")
      xmlBuffer.append(parent + " strandId=\"" + sourceBox.id + "\"/>\n")
      xmlBuffer.append(child + " strandId=\"" + targetBox.id + "\" />\n")
      xmlBuffer.append(nestingIndEnd + "\n")
    } else if (highestTag == DsOliConConfClassificationTag.CDLL) {
      // Canonicalize
      val (fwdId, revId) = if (sourceBox.offset < targetBox.offset) {
        (sourceBox.id, targetBox.id)
      } else {
        (targetBox.id, sourceBox.id)
      }
      xmlBuffer.append(cdll + "\n")
      xmlBuffer.append(fwd + " strandId=\"" + fwdId + "\"/>\n")
      xmlBuffer.append(rev + " strandId=\"" + revId + "\"/>\n")
      xmlBuffer.append(cdllEnd + "\n")
    } else if (highestTag == DsOliConConfClassificationTag.DLL) {
      // Canonicalize
      val (fwdId, revId) = if (sourceBox.offset < targetBox.offset) {
        (sourceBox.id, targetBox.id)
      } else {
        (targetBox.id, sourceBox.id)
      }
      xmlBuffer.append(dll + "\n")
      xmlBuffer.append(fwd + " strandId=\"" + fwdId + "\"/>\n")
      xmlBuffer.append(rev + " strandId=\"" + revId + "\"/>\n")
      xmlBuffer.append(dllEnd + "\n")
    } else if (highestTag == DsOliConConfClassificationTag.I1i) {
      xmlBuffer.append(i1i + "\n")
      xmlBuffer.append(list1 + " strandId=\"" + sourceBox.id + "\"/>\n")
      xmlBuffer.append(list2 + " strandId=\"" + targetBox.id + "\"/>\n")
      xmlBuffer.append(i1iEnd + "\n")
    } else if (highestTag == DsOliConConfClassificationTag.I1o) {
      xmlBuffer.append(i1o + "\n")
      xmlBuffer.append(list1 + " strandId=\"" + sourceBox.id + "\"/>\n")
      xmlBuffer.append(list2 + " strandId=\"" + targetBox.id + "\"/>\n")
      xmlBuffer.append(i1oEnd + "\n")
    } else if (highestTag == DsOliConConfClassificationTag.I2o) {
      // Canonicalize
      val (fwdId, revId) = if (sourceBox.offset < targetBox.offset) {
        (sourceBox.id, targetBox.id)
      } else {
        (targetBox.id, sourceBox.id)
      }
      xmlBuffer.append(i2o + "\n")
      xmlBuffer.append(list1 + " strandId=\"" + fwdId + "\"/>\n")
      xmlBuffer.append(list2 + " strandId=\"" + revId + "\"/>\n")
      xmlBuffer.append(i2oEnd + "\n")
    } else if (highestTag == DsOliConConfClassificationTag.SHN) {
      xmlBuffer.append(shn + ">\n")
      xmlBuffer.append(list1 + " strandId=\"" + sourceBox.id + "\"/>\n")
      xmlBuffer.append(list2 + " strandId=\"" + targetBox.id + "\"/>\n")
      xmlBuffer.append(shnEnd + "\n")
    } else {
      xmlBuffer.append(unclassified + " <!-- " + highestTag + " -->\n")
    }
    xmlBuffer.append(labelEnd + "\n")
  }

  def writeStrandConnPreamble(xmlBuffer: StringBuffer, id: Long, tag: DsOliConConfTag, t: Int): Unit = {
    xmlBuffer.append(strandConn + " id=\"" + id + "\" beginEventTimeStep=\"" + t + "\" ceaseEventTimeStep=\"" + t + "\">\n")
    xmlBuffer.append(kindTag + "\n")
    if (tag == DsOliConConfTag.ccOverlay) {
      xmlBuffer.append(overlay + "\n")
    } else {
      xmlBuffer.append(indirect + "\n")
    }
    xmlBuffer.append(kindTagEnd + "\n")
  }

  /**
   * Sort the aggregated strand graphs by the longest running ep.
   *
   * @param aggs The ASGs to sort
   * @return the sorted ASG
   */
  def sortAggGraphs(aggs: DsOliTimeStepContainer[(DsOliVertexMemory, DsOliMetaBoxGraph)]): ListBuffer[(DsOliVertexMemory, DsOliMetaBoxGraph)] = {
    // Cycle through all ASGs
    aggs.steps.sortBy {
      aggGraphTuple =>
        val (ep, aggGraph) = aggGraphTuple
        // Find the corresponding ep in the count store
        val epTupleOpt = epAggCnt.find {
          epCntTuple =>
            val (epCnt, _, _, cnt) = epCntTuple
            // Are the eps equal
            epCnt == ep
        }

        if (epTupleOpt.isDefined) {
          val (_, _, _, cnt) = epTupleOpt.get
          cnt
        } else {
          0
        }
    }.reverse

  }

  /**
   * Print debug information for the ASGs
   *
   * @param sortedAggGraphs the ASG to print
   */
  def dbgPrintSortedAggs(sortedAggGraphs: ListBuffer[(DsOliVertexMemory, DsOliMetaBoxGraph)]): Unit = {
    sortedAggGraphs.foreach {
      aggGraphTuple =>
        val (ep, aggGraph) = aggGraphTuple
        println("ep: " + ep.id + " aggCnt: " + epAggCnt.find(epCntTuple => epCntTuple._1 == ep).get._3)
    }
  }

  /**
   * Helper for decomposing the edge
   *
   * @param edge the edge to decompose
   * @return the decomposed edge
   */
  def getEdgeElements(edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]): (DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex], DsOliMetaBoxGraphVertex, DsOliMetaBoxGraphVertex) = {
    (edge, edge.source, edge.target)
  }

  /**
   * Helper function to get the source and target strands vertices and their IDs
   * @param source the source vertex
   * @param target the target vertex
   * @return
   */
  def getSourceTargetElements(source: DsOliMetaBoxGraphVertex, target: DsOliMetaBoxGraphVertex): (DsOliMetaBoxGraphVertexBoxes, DsOliMetaBoxGraphVertexBoxes, Long, Long) = {
    val sourceBoxes = source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]
    val targetBoxes = target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]

    val sourceId = sourceBoxes.boxes.head.id
    val targetId = targetBoxes.boxes.head.id
    (sourceBoxes, targetBoxes, sourceId, targetId)

  }

  /**
   * Fetch the both the connection configuration count and the sorted count and
   * for convenience extract the label with the highest evidence count together
   * with the actual evidence count.
   *
   * @param edge the strand connection edge
   * @return (hash map with connection configurations, sorted list with labels, the highest label, the highest evidence count)
   */
  def getTagElements(edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]): (HashMap[DsOliConConfClassificationTag, (Int, Int)], List[DsOliConConfClassificationTag], DsOliConConfClassificationTag, Int) = {
    val ccCount = DsOliGraphUtils.conConfClassAgg(edge.conConfClass)
    val sortedccCount = DsOliGraphUtils.highestConConfClass(ccCount)
    val highestTag = sortedccCount.head
    val highestTagEvidence = ccCount.get(highestTag).get._2
    (ccCount, sortedccCount, highestTag, highestTagEvidence)
  }

  // Detects currently supported tags
  def supportedTag(highestTag: DsOliConConfClassificationTag): Boolean = {
    highestTag == DsOliConConfClassificationTag.Ni ||
      highestTag == DsOliConConfClassificationTag.No ||
      highestTag == DsOliConConfClassificationTag.DLL ||
      highestTag == DsOliConConfClassificationTag.CDLL ||
      highestTag == DsOliConConfClassificationTag.I1o ||
      highestTag == DsOliConConfClassificationTag.SHN
  }

  def filterOutBiDiEdges(biDiEdgeFilter: ListBuffer[(Long, Long)], sourceId: Long, targetId: Long): Boolean = {
    biDiEdgeFilter.find(tuple => (tuple._1 == sourceId && tuple._2 == targetId)
      || (tuple._2 == sourceId && tuple._1 == targetId)).isDefined
  }

  /**
   * Remove edges between ep and strands from the edge set,
   * i.e. keep only the edges between strands.
   *
   * @param xmlBuffer
   * @param graph the graph that contains the edges
   * @return set of edges
   */
  def filterGraphEdges(xmlBuffer: StringBuffer, graph: DsOliMetaBoxGraph) = {
    graph.graph.edges.filter { edgeInner =>
      // Filter for connections which are actually between vertices of the fsg
      val (edge, source, target) = getEdgeElements(edgeInner.toOuter)
      if (!(source.isInstanceOf[DsOliMetaBoxGraphVertexBoxes] && target.isInstanceOf[DsOliMetaBoxGraphVertexBoxes])) {
        xmlBuffer.append("<!-- filterGraphEdges: filtering out edge.id: " + edge.id + "-->")
      }
      source.isInstanceOf[DsOliMetaBoxGraphVertexBoxes] && target.isInstanceOf[DsOliMetaBoxGraphVertexBoxes]
    }
  }

  def writePreambel(xmlBuffer: StringBuffer, ep: DsOliVertexMemory, id: Long, tag: DsOliConConfTag, t: Int, highestTagEvidence: Int, highestTag: DsOliConConfClassificationTag, sourceBox: DsOliBox, targetBox: DsOliBox): Unit = {
    writeStrandConnPreamble(xmlBuffer, id, tag, t)
    // Write the global label
    xmlBuffer.append(epochs + "\n")
    xmlBuffer.append(epoch + " beginEventTimeStep=\"" + t + "\" ceaseEventTimeStep=\"" + t + "\"> \n")
  }

  def writeNesting(xmlBuffer: StringBuffer, box1: DsOliBox, id1: DsOliCell.CellId, box2: DsOliBox, id2: DsOliCell.CellId): Unit = {
    xmlBuffer.append(cellPairs + "\n")
    xmlBuffer.append(cellPair + " leftCellId=\"" + id1 + "\" rightCellId=\"" + id2 + "\" /> \n")
    xmlBuffer.append(cellPairsEnd + "\n")
  }

  def writeEnd(xmlBuffer: StringBuffer, highestTagLocalEvidence: Int, highestTagLocal: DsOliConConfClassificationTag, sourceBox: DsOliBox, targetBox: DsOliBox): Unit = {
    xmlBuffer.append(localLabel + " evidence=\"" + highestTagLocalEvidence + "\">\n")
    writeStrandLabel(xmlBuffer, highestTagLocal, sourceBox, targetBox)
    xmlBuffer.append(localLabelEnd + "\n")

    xmlBuffer.append(epochEnd + "\n")
    xmlBuffer.append(epochsEnd + "\n")

    xmlBuffer.append(strandConnEnd + "\n")
  }

  def writeNonNesting(xmlBuffer: StringBuffer, sourceBox: DsOliBox, targetBox: DsOliBox, highestTag: DsOliConConfClassificationTag, t: Int, edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]): Unit = {
    if (highestTag == DsOliConConfClassificationTag.CDLL || highestTag == DsOliConConfClassificationTag.DLL) {
      val (fwdBox, revBox) = if (sourceBox.offset < targetBox.offset) {
        (sourceBox, targetBox)
      } else {
        (targetBox, sourceBox)
      }
      val revBoxCells = if (highestTag == DsOliConConfClassificationTag.CDLL && revBox.cycleId != 0) {
        val boxStep = boxSteps.boxSteps(t)
        boxStep.cycles.get(revBox.cycleId).get.cells
      } else {
        revBox.cells
      }

      val fwdCells = if (highestTag == DsOliConConfClassificationTag.CDLL && fwdBox.cycleId != 0) {
        val boxStep = boxSteps.boxSteps(t)
        boxStep.cycles.get(fwdBox.cycleId).get.cells
      } else {
        fwdBox.cells
      }

      println("t: " + t + " fwdBox.len: " + fwdCells.length + " revBox.len: " + revBoxCells.length)
      var index = 0
      xmlBuffer.append(cellPairs + "\n")

      fwdCells.foreach {
        cell =>
          var found = false
          edge.ccSet.foreach {
            ccSetTuple =>
              val (cell1, cell2, box1, box2) = ccSetTuple
              if (cell1 == cell) {
                xmlBuffer.append(cellPair + " leftCellId=\"" + cell1.id + "\" rightCellId=\"" + cell2.id + "\" /> \n")
                found = true
              } else {
              }
          }
          if (!found) {
            xmlBuffer.append("<!-- no corresponding cell for " + cell + " found -->\n")
          }
      }
      xmlBuffer.append(cellPairsEnd + "\n")
    } else if (DsOliConConfClassificationTag.I1o == highestTag || DsOliConConfClassificationTag.SHN == highestTag) {
      xmlBuffer.append(cellPairs + "\n")
      val curPtg = this.ptgs.graphs(t)
      sourceBox.cells.foreach {
        sourceCell =>
          targetBox.cells.foreach {
            targetCell =>

              val sourceCellVertex = curPtg.getVertexForAddress(sourceCell.bAddr)
              val targetCellVertex = curPtg.getVertexForAddress(targetCell.bAddr)
              if (sourceCellVertex.isDefined && targetCellVertex.isDefined && sourceCellVertex.get == targetCellVertex.get) {
                //if(DsOliAddressUtils.addressInRange(sourceCell.bAddr , sourceCell.eAddr , targetCell.bAddr, targetCell.eAddr)){
                xmlBuffer.append(cellPair + " leftCellId=\"" + sourceCell.id + "\" rightCellId=\"" + targetCell.id + "\" /> \n")
                xmlBuffer.append("<!-- leftCell: " + sourceCell + " rightCell: " + targetCell + " -->\n")
              }
          }
      }
      xmlBuffer.append(cellPairsEnd + "\n")
    }
  }

  var strandConnIds: ListBuffer[(Long, DsOliBox, DsOliBox, Long)] = null

  /**
   * Test if overlay label has a direction
   *
   * @param tag the tag to test
   * @return boolean
   */
  def overlayLabelHasDirection(tag: DsOliConConfClassificationTag): Boolean = {
    tag == DsOliConConfClassificationTag.BT ||
      tag == DsOliConConfClassificationTag.SLo2 ||
      tag == DsOliConConfClassificationTag.No
  }

  /**
   * Canonicalize the given source and target
   *
   * @param xmlBuffer the StringBuffer to write to
   * @param highestTag the highest label of the first edge
   * @param highestTagEvidence the highest evidence count of the first edge
   * @param sourceBoxes the source strand
   * @param targetBoxes the target strand
   * @param edge the edge from sourceBoxes to targetBoxes
   * @param highestTagCor the highest label of the corresponding edge
   * @param highestTagCorEvidence the highest evidence count of the corresponding edge
   * @param sourceBoxesCor the source strand of the corresponding edge (i.e. targetBoxes)
   * @param targetBoxesCor the target strand of the corresponding edge (i.e. sourceBoxes)
   * @param edgeCor the corresponding edge
   * @return (highestTag: DsOliConConfClassificationTag, sourceBox: DsOliBox, targetBox: DsOliBox)
   */
  def getEdgeWithHigherOrderOrEvidence(xmlBuffer: StringBuffer,
    highestTag: DsOliConConfClassificationTag, highestTagEvidence: Int,
    sourceBoxes: DsOliMetaBoxGraphVertexBoxes, targetBoxes: DsOliMetaBoxGraphVertexBoxes, edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex],
    highestTagCor: DsOliConConfClassificationTag, highestTagCorEvidence: Int,
    sourceBoxesCor: DsOliMetaBoxGraphVertexBoxes, targetBoxesCor: DsOliMetaBoxGraphVertexBoxes, edgeCor: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]): (DsOliConConfClassificationTag, Long, DsOliBox, DsOliBox, DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]) = {
    // Compare the tags directly
    if (highestTag > highestTagCor) {
      xmlBuffer.append("<!-- highestTag > highestTagCor -->\n")
      (highestTag, highestTagEvidence, sourceBoxes.boxes.head, targetBoxes.boxes.head, edge)
    } else if (highestTag < highestTagCor) {
      xmlBuffer.append("<!-- highestTag < highestTagCor -->\n")
      (highestTagCor, highestTagCorEvidence, sourceBoxesCor.boxes.head, targetBoxesCor.boxes.head, edgeCor)
    } else {
      // Tags are equal, compare evidence
      if (highestTagEvidence > highestTagCorEvidence) {
        xmlBuffer.append("<!-- highestTagEvidence > highestTagCorEvidence -->\n")
        (highestTag, highestTagEvidence, sourceBoxes.boxes.head, targetBoxes.boxes.head, edge)
      } else {
        xmlBuffer.append("<!-- highestTagEvidence < highestTagCorEvidence -->\n")
        (highestTagCor, highestTagCorEvidence, sourceBoxesCor.boxes.head, targetBoxesCor.boxes.head, edgeCor)
      }
    }
  }

  /**
   * Write the indirect strand connection
   *
   * @param xmlBuffer
   * @param edge
   * @param sourceBoxes
   * @param targetBoxes
   * @param sourceAggBoxes
   * @param targetAggBoxes
   * @param t
   * @return
   */
  def writeIndirectStrandConnection(xmlBuffer: StringBuffer, edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex],
    sourceBoxes: DsOliMetaBoxGraphVertexBoxes, targetBoxes: DsOliMetaBoxGraphVertexBoxes,
    sourceAggBoxes: DsOliMetaBoxGraphVertexBoxes, targetAggBoxes: DsOliMetaBoxGraphVertexBoxes, t: Long): Boolean = {

    // The source and target strand exist in the ASG
    if (!(sourceAggBoxes.boxes.exists(_.id == sourceBoxes.boxes.head.id) &&
      targetAggBoxes.boxes.exists(_.id == targetBoxes.boxes.head.id))) {
      xmlBuffer.append("<!-- source target not found in agg -->\n")
      return false
    }

    // Write the properties of the strand connections: start/end time step
    xmlBuffer.append(strandConn + " id=\"" + edge.id + "\" beginEventTimeStep=\"" + t + "\" ceaseEventTimeStep=\"" + t + "\">\n")
    // Indirect strand connection
    xmlBuffer.append("<kind>\n")
    xmlBuffer.append("<indirect/>\n")
    xmlBuffer.append("<params>\n")
    // The offsets of the SC configuration
    xmlBuffer.append("<y>" + edge.conConf.offsets._1 + "</y>\n")
    xmlBuffer.append("<z>" + edge.conConf.offsets._2 + "</z>\n")
    xmlBuffer.append("</params>\n")
    xmlBuffer.append("</kind>\n")

    // Write the cell pairs
    writeCellPairs(xmlBuffer, edge)

    val (ccCountLocal, sortedccCountLocal, highestTagLocal, highestTagLocalEvidence) = getTagElements(edge)
    val sourceBox = sourceBoxes.boxes.head
    val targetBox = targetBoxes.boxes.head

    strandConnIds.append((edge.id, sourceBox, targetBox, t))

    writeStrandLabel(xmlBuffer, highestTagLocal, sourceBox, targetBox)

    xmlBuffer.append(strandConnEnd + "\n")

    // Stop with first match!
    true

  }

  /**
   * Write the cell pairs of the connection
   *
   * @param xmlBuffer the StringBuffer to write to
   * @param edgeCanonical the canonicalized edge
   */
  def writeCellPairs(xmlBuffer: StringBuffer, edgeCanonical: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]): Unit = {
    val ccSet = edgeCanonical.ccSet
    val ccSetFirst = ccSet.head
    val (leftStrandId, rightStrandId) = (ccSetFirst._3.id, ccSetFirst._4.id)
    xmlBuffer.append("<cell-pairs leftStrandId=\"" + leftStrandId + "\" rightStrandId=\"" + rightStrandId + "\">\n")
    ccSet.foreach {
      ccSetTuple =>
        val (cell1, cell2, box1, box2) = ccSetTuple

        xmlBuffer.append("<cell-pair leftCellId=\"" + cell1.id + "\" rightCellId=\"" + cell2.id + "\"/>\n")

        if (box1.id != leftStrandId || box2.id != rightStrandId) {
          throw new Exception("Error id missmatch: leftStrandId: " + leftStrandId + " box1.id: " + box1.id +
            " rightStrandId: " + rightStrandId + " box2.id: " + box2.id)
        }
    }
    xmlBuffer.append("</cell-pairs>\n")
  }

  /**
   * Write the overlay strand connection
   *
   * @param xmlBuffer
   * @param edge
   * @param sourceBoxes
   * @param targetBoxes
   * @param sourceAggBoxes
   * @param targetAggBoxes
   * @param t
   * @param biDiEdgeFilter
   * @param sourceId
   * @param targetId
   * @param source
   * @param target
   * @param mbg
   * @return
   */
  def writeOverlayStrandConnection(xmlBuffer: StringBuffer, edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex],
    sourceBoxes: DsOliMetaBoxGraphVertexBoxes, targetBoxes: DsOliMetaBoxGraphVertexBoxes,
    sourceAggBoxes: DsOliMetaBoxGraphVertexBoxes, targetAggBoxes: DsOliMetaBoxGraphVertexBoxes, t: Long,
    biDiEdgeFilter: ListBuffer[(Long, Long)], sourceId: Long, targetId: Long,
    source: DsOliMetaBoxGraphVertex, target: DsOliMetaBoxGraphVertex,
    mbg: DsOliMetaBoxGraph): Boolean = {

    // Test, that the source and target strands exist in the ASG
    if (!sourceAggBoxes.boxes.exists(_.id == sourceBoxes.boxes.head.id) ||
      !targetAggBoxes.boxes.exists(_.id == targetBoxes.boxes.head.id)) {
      xmlBuffer.append("<!-- skipping strand connection (biDi case): " + edge.id + " -->\n")
      return false
    }

    biDiEdgeFilter.append((sourceId, targetId))

    // Fetch the corresponding bidirectional edge
    val edgeCor = DsOliGraphUtils.getCorrespondingBiDiEdge(edge, mbg)
    val (sourceCor, targetCor) = (edgeCor.source, edgeCor.target)
    val (sourceBoxesCor, targetBoxesCor, sourceIdCor, targetIdCor) = getSourceTargetElements(sourceCor, targetCor)

    // Get the properties of the edge and the corresponding edge
    val (ccCount, sortedccCount, highestTag, highestTagEvidence) = getTagElements(edge)
    val (ccCountCor, sortedccCountCor, highestTagCor, highestTagCorEvidence) = getTagElements(edgeCor)

    // if labels are the same && have no direction -> canonicalize
    if (highestTag == highestTagCor && !overlayLabelHasDirection(highestTag)) {
      xmlBuffer.append("<!-- same labels without direction: highestTag: " + highestTag + " highestTagCor: " + highestTagCor + "-->\n")

      val sourceBox = sourceBoxes.boxes.head
      val targetBox = targetBoxes.boxes.head
      xmlBuffer.append("<!-- same labels without direction: sourceBox.offset: " + sourceBox.offset + " targetBox.offset: " + targetBox.offset + "-->\n")

      // Choose the source according to the linkage offset: the strand with the minor linkage offset becomes the source
      val (sourceBoxCanonical, targetBoxCanonical, edgeCanonical) = if (sourceBox.offset < targetBox.offset) {
        (sourceBox, targetBox, edge)
      } else {
        (targetBox, sourceBox, edgeCor)
      }

      // Write overlay strand connection properties
      xmlBuffer.append(strandConn + " id=\"" + edgeCanonical.id + "\" beginEventTimeStep=\"" + t + "\" ceaseEventTimeStep=\"" + t + "\">\n")
      xmlBuffer.append("<kind>\n")
      xmlBuffer.append("<overlay/>\n")
      xmlBuffer.append("<params>\n")
      xmlBuffer.append("<x>" + edgeCanonical.conConf.offsets._1 + "</x>\n")
      xmlBuffer.append("<w>" + edgeCanonical.conConf.offsets._2 + "</w>\n")
      xmlBuffer.append("</params>\n")
      xmlBuffer.append("</kind>\n")

      strandConnIds.append((edgeCanonical.id, sourceBoxCanonical, targetBoxCanonical, t))

      // Now print the cell pairs based on the cc sets found on the returned edge
      writeCellPairs(xmlBuffer, edgeCanonical)

      // Now print the actual label
      writeStrandLabel(xmlBuffer, highestTag, sourceBoxCanonical, targetBoxCanonical)

    } // if labels are different && and both have no direction -> should not happen
    else if (highestTag != highestTagCor && (!overlayLabelHasDirection(highestTag) && !overlayLabelHasDirection(highestTagCor))) {
      xmlBuffer.append("<!-- different labels both without direction: highestTag: " + highestTag + " highestTagCor: " + highestTagCor + "-->\n")
      xmlBuffer.append("<!-- highestTag: " + highestTag + " direction: " + overlayLabelHasDirection(highestTag) + "-->\n")
      xmlBuffer.append("<!-- highestTagCor: " + highestTagCor + " direction: " + overlayLabelHasDirection(highestTagCor) + "-->\n")
    } // if at least one label has a direction
    else if ((overlayLabelHasDirection(highestTag) || overlayLabelHasDirection(highestTagCor))) {
      //   -> choose by taxonomy or if still ambiguous choose by highest evidence
      val (highestTagLocal, evidenceLocal, sourceBoxTax, targetBoxTax, edgeTax) = getEdgeWithHigherOrderOrEvidence(
        xmlBuffer,
        highestTag, highestTagEvidence, sourceBoxes, targetBoxes, edge,
        highestTagCor, highestTagCorEvidence, sourceBoxesCor, targetBoxesCor, edgeCor)

      // Write overlay strand connection properties
      xmlBuffer.append(strandConn + " id=\"" + edgeTax.id + "\" beginEventTimeStep=\"" + t + "\" ceaseEventTimeStep=\"" + t + "\">\n")
      xmlBuffer.append("<kind><overlay/>\n")
      xmlBuffer.append("<params>\n")
      xmlBuffer.append("<x>" + edgeTax.conConf.offsets._1 + "</x>\n")
      xmlBuffer.append("<w>" + edgeTax.conConf.offsets._2 + "</w>\n")
      xmlBuffer.append("</params>\n")
      xmlBuffer.append("</kind>\n")

      strandConnIds.append((edgeTax.id, sourceBoxTax, targetBoxTax, t))

      xmlBuffer.append("<!-- at least one direction: highestTag: " + highestTag + " highestTagCor: " + highestTagCor + "-->\n")
      xmlBuffer.append("<!-- highestTagLocal: " + highestTagLocal + " evidenceLocal: " + evidenceLocal + "-->\n")

      // Now print the cell pairs based on the cc sets found on the returned edge
      writeCellPairs(xmlBuffer, edgeTax)

      // Now print the actual label
      writeStrandLabel(xmlBuffer, highestTagLocal, sourceBoxTax, targetBoxTax)

    }
    xmlBuffer.append(strandConnEnd + "\n")
    true
  }

  /**
   * @param xmlBuffer
   */
  def writeStrandConns2(xmlBuffer: StringBuffer): Unit = {

    // Sort agg graphs by longest running ep
    val sortedAggGraphs = sortAggGraphs(this.aggGraphs)

    dbgPrintSortedAggs(sortedAggGraphs)

    strandConnIds = new ListBuffer[(Long, DsOliBox, DsOliBox, Long)]()

    xmlBuffer.append(strandConns + "\n")
    var t = 0

    // Iterate over all strand graphs (time step based)
    this.dsOliMbgs.steps.foreach {
      mbg =>

        t += 1

        val biDiEdgeFilter = new ListBuffer[(Long, Long)]()

        // Iterate over all strand connections (per time step)
        val filteredFsgEdges = filterGraphEdges(xmlBuffer, mbg)
        filteredFsgEdges.foreach {
          edgeInner =>

            val (edge, source, target) = getEdgeElements(edgeInner.toOuter)
            val (sourceBoxes, targetBoxes, sourceId, targetId) = getSourceTargetElements(source, target)

            // Test for bi directional edges
            if (filterOutBiDiEdges(biDiEdgeFilter, sourceId, targetId)) {
              // Nothing
              xmlBuffer.append("<!-- marking/skipping time step as bidi: " + t + " edge.id: " + edge.id + " sourceId: " + sourceId + " targetId: " + targetId + " present -->\n")
            } else {

              // Iterate over agg graphs (ep sorted) and search for strand connection
              breakable {
                sortedAggGraphs.foreach {
                  aggGraphTuple =>

                    val (ep, aggGraph) = aggGraphTuple
                    val filteredAggEdges = filterGraphEdges(xmlBuffer, aggGraph)

                    filteredAggEdges.iterator.foreach {
                      edgeAggInner =>
                        val (edgeAgg, sourceAgg, targetAgg) = getEdgeElements(edgeAggInner.toOuter)

                        val sourceAggBoxes = sourceAgg.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]
                        val targetAggBoxes = targetAgg.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]

                        val isBiDiEdge = DsOliGraphUtils.isBidirectionalEdge(edge, mbg)
                        val isBiDiEdgeAgg = DsOliGraphUtils.isBidirectionalEdge(edgeAgg, aggGraph)

                        // Both edges should be either bi di or non bi di 
                        if (isBiDiEdge != isBiDiEdgeAgg) {
                          System.err.println("isBiDiEdge: " + isBiDiEdge + " != isBiDiEdgeAgg: " + isBiDiEdgeAgg)
                          xmlBuffer.append("<!-- error " + "isBiDiEdge: " + isBiDiEdge + " != isBiDiEdgeAgg: " + isBiDiEdgeAgg + "-->\n")
                        }

                        // Test for bidirectional edge
                        if (!isBiDiEdge) {

                          // Stop after first matched ASG
                          if (writeIndirectStrandConnection(xmlBuffer, edge, sourceBoxes, targetBoxes, sourceAggBoxes, targetAggBoxes, t)) {
                            break
                          }

                        } else {

                          // Stop after first matched ASG
                          if (writeOverlayStrandConnection(xmlBuffer, edge, sourceBoxes, targetBoxes, sourceAggBoxes,
                            targetAggBoxes, t, biDiEdgeFilter, sourceId, targetId, source, target, mbg)) {
                            break
                          }

                        }
                    }
                }
              }
            }
        }
    }
    xmlBuffer.append(strandConnsEnd + "\n")
  }

  /**
   * Check if the entry point is contigous
   *
   * @param elem
   * @param sgSourceEp
   * @param sgTargetBoxes
   * @param sgEdge
   * @return
   */
  def isSameEPConnection(elem: (DsOliMetaBoxGraphVertexEP, DsOliConConf, Long, Long, DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex], DsOliBox),
    sgSourceEp: DsOliMetaBoxGraphVertexEP, sgTargetBoxes: DsOliMetaBoxGraphVertexBoxes,
    sgEdge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]): Boolean = {
    val (tEp, tConConf, tStart, tEnd, tEdge, tTargetStrand) = elem
    // Must be the same source ep
    tEp.ep.id == sgSourceEp.ep.id &&
      // The connection configuration must be the same
      tConConf == sgEdge.conConf &&
      // The target box must be the same and the target 
      // sg vertex should always only contain one box!
      tTargetStrand.id == sgTargetBoxes.boxes.head.id

  }

  var eptIdMappings: HashMap[Long, Long] = null

  /**
   * Write the entry points and entry point connections
   *
   * @param xmlBuffer
   */
  def writeEPs(xmlBuffer: StringBuffer): Unit = {
    xmlBuffer.append("<entry-points>")
    var epId = 0

    eptIdMappings = new HashMap[Long, Long]()

    // Write the entry points
    this.epAggCnt.foreach {
      epAgg =>
        val (ep, vepStart, vepEnd, cnt) = epAgg
        eptIdMappings.put(ep.id, epId)
        xmlBuffer.append("<entry-point id=\"" + epId + "\" beginEventTimeStep=\"" + vepStart + "\" ceaseEventTimeStep=\"" + vepEnd + "\">")
        xmlBuffer.append("<points-to-vertex-id>" + ep.id + "</points-to-vertex-id>")
        xmlBuffer.append("</entry-point>")
        epId += 1
    }
    xmlBuffer.append("</entry-points>")

    // Write the entry point connections: 1) collect the ep connections 2) write the ep connections

    // 1) collect the ep connections
    xmlBuffer.append("<entry-point-connections>")
    val eptConnections = new ListBuffer[(DsOliMetaBoxGraphVertexEP, DsOliConConf, Long, Long, DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex], DsOliBox)]
    var curTimeStep = 1;

    // Cycle through all strand graphs for each time step
    this.dsOliMbgs.steps.foreach {
      sg =>

        // Cycle through all edges of the current strand graph and
        sg.graph.edges.iterator.foreach {
          sgEdgeInner =>
            val sgEdge = sgEdgeInner.toOuter
            if (sgEdge.source.isInstanceOf[DsOliMetaBoxGraphVertexEP] &&
              sgEdge.target.isInstanceOf[DsOliMetaBoxGraphVertexBoxes]) {

              val sgSourceEp = sgEdge.source.asInstanceOf[DsOliMetaBoxGraphVertexEP]
              val sgTargetBoxes = sgEdge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]

              if (eptConnections.exists { elem =>
                isSameEPConnection(elem, sgSourceEp, sgTargetBoxes, sgEdge)
              }) {
                // ept connection already present, update time stamp
                val eptElem = eptConnections.find(elem => isSameEPConnection(elem, sgSourceEp, sgTargetBoxes, sgEdge)).get
                eptConnections.remove(eptConnections.indexOf(eptElem))
                eptConnections.append((eptElem._1, eptElem._2, eptElem._3, curTimeStep, eptElem._5, eptElem._6))
              } else {
                // ept connection not present yet, add
                eptConnections.append((sgSourceEp, sgEdge.conConf, curTimeStep, curTimeStep,
                  sgEdge, sgTargetBoxes.boxes.head))
              }
            }
        }
        curTimeStep += 1
    }

    // 2) Write the ep connections
    var eptConId = 0
    eptConnections.foreach {
      eptConnection =>
        val (tEp, tConConf, tStart, tEnd, tEdge, tTargetStrand) = eptConnection
        xmlBuffer.append("<entry-point-connection id=\"" + eptConId + "\" beginEventTimeStep=\"" + tStart + "\" ceaseEventTimeStep=\"" + tEnd + "\">")
        xmlBuffer.append("<source-entry-point-id>" + eptIdMappings.get(tEp.ep.id).get + "</source-entry-point-id>")
        xmlBuffer.append("<type>")

        // Detect if we have an entry pointer or 
        // if the element is placed on the stack
        if (tConConf.tag == ccEntryPointer) {
          xmlBuffer.append("<pointer>")
          xmlBuffer.append("<param-x>" + tConConf.offsets._1 + "</param-x>")
          xmlBuffer.append("<param-y>" + tConConf.offsets._2 + "</param-y>")
          xmlBuffer.append("</pointer>")
        } else {
          xmlBuffer.append("<stack>")
          xmlBuffer.append("<param-z>" + tConConf.offsets._2 + "</param-z>")
          xmlBuffer.append("</stack>")
        }

        xmlBuffer.append("</type>")
        xmlBuffer.append("<target-strand-id>" + tTargetStrand.id + "</target-strand-id>")
        xmlBuffer.append("</entry-point-connection>")
        eptConId += 1
    }
    xmlBuffer.append("</entry-point-connections>")
  }

  /**
   * Checks, if the node contains all strands from
   * the given element
   *
   * @param node the node (strand set vertex) to check against
   * @param elem the element to check
   * @return Boolean
   */
  def contiguousFSGVertex(node: DsOliMetaBoxGraphVertexBoxes,
    elem: (DsOliMetaBoxGraphVertexBoxes, Long, Long, Long)): Boolean = {
    val (tVertex, tStart, tEnd, tNumElems) = elem
    // Same number of box elements as before
    node.boxes.size == tNumElems &&
      // All box ids match
      tVertex.boxes.forall(box => node.boxes.exists(boxTest => box.id == boxTest.id))
  }

  /**
   * Checks, if the node element is a subset of
   * the node
   *
   * @param node the node (strand set vertex) to check against
   * @param elem the element to check
   * @return Boolean
   */
  def subsetFSGVertexExists(node: DsOliMetaBoxGraphVertexBoxes,
    elem: (DsOliMetaBoxGraphVertexBoxes, Long, Long, Long)): Boolean = {
    val (tVertex, tStart, tEnd, tNumElems) = elem
    // Stored strand set must always remain a subset of
    // the following vertices
    tVertex.boxes.subsetOf(node.boxes) &&
      // AND as soon as the number of strands decreases
      // it is no longer valid
      node.boxes.size >= tNumElems
  }

  /**
   * Fetch the label together with its count and summed up evidence
   * @param xmlBuffer
   * @param edgeOuter
   * @return Hash Map: key: label value: (label count, summed evidence counts))
   */
  def aggStrandConnectionLabels(xmlBuffer: StringBuffer, edgeOuter: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]): HashMap[DsOliConConfClassificationTag, (Int, Int)] = {

    val ccCount = new HashMap[DsOliConConfClassificationTag, (Int, Int)]()
    edgeOuter.strandConns.foreach {
      strandSetEdge =>
        // Again, filter out the correct edges because of the
        // representation of bidirectional edges with two directed edges
        if (this.strandConnIds.exists {
          idTuple =>
            val (id, _, _, _) = idTuple
            id == strandSetEdge.id
        }) {
          strandSetEdge.conConfClass.foreach { ccClass =>
            // Create or update HashMap entry
            if (ccCount.contains(ccClass.classification)) {
              // Update: increment label count and sum evidence
              ccCount.put(ccClass.classification, (ccCount.get(ccClass.classification).get._1 + 1, ccCount.get(ccClass.classification).get._2 + ccClass.evidence))
            } else {
              ccCount.put(ccClass.classification, (1, ccClass.evidence))
            }
          }
        } else {
          xmlBuffer.append("<!-- skipping in agg writeStrandConnectionLabels edge.id: " + strandSetEdge.id + " because this one is not used in strand-connections. -->")
        }
    }
    ccCount
  }

  def writeStrandConnections(xmlBuffer: StringBuffer, edgeOuter: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex],
    id1: Long, id2: Long): Unit = {

    val source = edgeOuter.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]
    val target = edgeOuter.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]

    edgeOuter.strandConns.foreach {
      strandSetEdge =>

        // Again, filter out the correct edges because of the
        // representation of bidirectional edges with two directed edges
        if (this.strandConnIds.exists {
          idTuple =>
            val (id, _, _, _) = idTuple
            id == strandSetEdge.id
        }) {

          xmlBuffer.append("<!-- source: " + strandSetEdge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.foldLeft("")((hist, cur) => hist + "," + cur.id)
            + " target: " + strandSetEdge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.foldLeft("")((hist, cur) => hist + "," + cur.id) + "-->\n");
          xmlBuffer.append("<strand-connection>\n")
          xmlBuffer.append("<id>" + strandSetEdge.id + "</id>\n")
          xmlBuffer.append("<mapping>\n")
          xmlBuffer.append("<mapping-entry agg-strand-set-id=\"" + id1 + "\" strand-id=\"" + strandSetEdge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id + "\"/>\n")
          xmlBuffer.append("<mapping-entry agg-strand-set-id=\"" + id2 + "\" strand-id=\"" + strandSetEdge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id + "\"/>\n")
          xmlBuffer.append("</mapping>\n")

          xmlBuffer.append("</strand-connection>\n")
        } else {
          xmlBuffer.append("<!-- skipping edge.id: " + strandSetEdge.id + " because this one is not used in strand-connections. -->")
        }
    }
  }

  /**
   * Fetch the corresponding ASG
   *
   * @param aggVerticesWithId the mappings between the vertex and the ID
   * @param boxes
   * @param vep
   * @return
   */
  def findAGGVertex(aggVerticesWithId: ListBuffer[(DsOliMetaBoxGraphVertexBoxes, Long, Long)], boxes: DsOliMetaBoxGraphVertexBoxes, vep: DsOliVertexMemory) = {
    aggVerticesWithId.find {
      elem =>
        val (vertex, _, vepId) = elem
        vep.id == vepId &&
          vertex.boxes.size == boxes.boxes.size &&
          vertex.boxes.forall(box => boxes.boxes.exists(boxTest => box.id == boxTest.id))
    }.get
  }

  /**
   * Write the ASG connections
   *
   * @param xmlBuffer
   * @param aggVerticesWithId the mapping between the ASG vertices to their corresponding IDs
   */
  def writeAGGGraph(xmlBuffer: StringBuffer, aggVerticesWithId: ListBuffer[(DsOliMetaBoxGraphVertexBoxes, Long, Long)]): Unit = {

    xmlBuffer.append("<agg-strand-connection-sets>")
    var id = 0
    this.aggGraphs.steps.foreach {
      elem =>
        val (vep, agg) = elem

        val biDiEdges = new ListBuffer[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]()
        agg.graph.edges.foreach {
          edgeInner =>
            val edgeOuter = edgeInner.toOuter
            val source = edgeOuter.source
            val target = edgeOuter.target

            if (!biDiEdges.contains(edgeOuter)) {
              if (edgeOuter.strandConns != null &&
                edgeOuter.strandConns.exists {
                  strandSetEdge =>

                    // Again, filter out the correct edges because of the
                    // representation of bidirectional edges with two directed edges
                    this.strandConnIds.exists {
                      idTuple =>
                        val (id, _, _, _) = idTuple
                        id == strandSetEdge.id
                    }
                }) {

                if (source.isInstanceOf[DsOliMetaBoxGraphVertexBoxes] &&
                  target.isInstanceOf[DsOliMetaBoxGraphVertexBoxes]) {

                  id += 1
                  xmlBuffer.append("<agg-strand-connection-set id=\"" + id + "\" entry-point-id=\"" + eptIdMappings.get(vep.id).get + "\">")
                  val sourceVertex = findAGGVertex(aggVerticesWithId, edgeOuter.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes], vep)
                  val targetVertex = findAGGVertex(aggVerticesWithId, edgeOuter.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes], vep)

                  // Write the kind of the connection
                  xmlBuffer.append("<kind>")
                  val isBiDiEdge = DsOliGraphUtils.isBidirectionalEdge(edgeOuter, agg)
                  if (isBiDiEdge) {
                    xmlBuffer.append("<overlay/>")
                  } else {
                    xmlBuffer.append("<unidirectional/>")
                  }
                  xmlBuffer.append("</kind>")

                  // Fetch the corresponding edge in case of a bidirectional connection
                  val edgeOuterCor: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex] = if (isBiDiEdge) {
                    val tmp = DsOliGraphUtils.getCorrespondingBiDiEdge(edgeOuter, agg)
                    biDiEdges.append(tmp)
                    tmp
                  } else {
                    null
                  }

                  // Write (both) connections 
                  if (edgeOuter.strandConns != null || (edgeOuterCor != null && edgeOuterCor.strandConns != null)) {
                    xmlBuffer.append("<strand-connections>")
                    if (edgeOuter.strandConns != null) {
                      writeStrandConnections(xmlBuffer, edgeOuter, sourceVertex._2, targetVertex._2)
                    }
                    xmlBuffer.append("<!-- cor -->")
                    if (edgeOuterCor != null) {
                      writeStrandConnections(xmlBuffer, edgeOuterCor, targetVertex._2, sourceVertex._2)
                    }
                    xmlBuffer.append("</strand-connections>")
                  }

                  // Detect labels
                  xmlBuffer.append("<labels>")

                  // In case of bidirectional edge, write the edge
                  if (edgeOuterCor != null) {
                    val (ccCountCor, sortedccCountCor, highestTagCor, highestTagEvidenceCor) = getTagElements(edgeOuterCor)

                    val ccCount = aggStrandConnectionLabels(xmlBuffer, edgeOuter)
                    ccCount.foreach {
                      cc =>
                        val (tag, (cnt, evidence)) = cc
                        //writeEdgeLabel(xmlBuffer, tag, evidence, sourceVertex._1, sourceVertex._2, targetVertex._1, targetVertex._2, "aggStrandSetId")
                        writeEdgeLabel(xmlBuffer, tag, evidence, sourceVertex._1, sourceVertex._2, targetVertex._1, targetVertex._2, "agg-strand-set-id")
                    }
                    if (isBiDiEdge) {
                      val ccCountCor = aggStrandConnectionLabels(xmlBuffer, edgeOuterCor)
                      val sourceVertexCor = findAGGVertex(aggVerticesWithId, edgeOuterCor.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes], vep)
                      val targetVertexCor = findAGGVertex(aggVerticesWithId, edgeOuterCor.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes], vep)
                      xmlBuffer.append("<!-- cor -->")
                      ccCountCor.foreach {
                        cc =>
                          val (tag, (cnt, evidence)) = cc
                          //writeEdgeLabel(xmlBuffer, tag, evidence, sourceVertexCor._1, sourceVertexCor._2, targetVertexCor._1, targetVertexCor._2, "aggStrandSetId")
                          writeEdgeLabel(xmlBuffer, tag, evidence, sourceVertexCor._1, sourceVertexCor._2, targetVertexCor._1, targetVertexCor._2, "agg-strand-set-id")
                      }
                    }
                  }
                  xmlBuffer.append("</labels>")
                }
                xmlBuffer.append("</agg-strand-connection-set>")
              }
            }
        }
    }
    xmlBuffer.append("</agg-strand-connection-sets>")
  }

  /**
   * Write the ASG
   * @param xmlBuffer
   */
  def writeAGG(xmlBuffer: StringBuffer): Unit = {
    xmlBuffer.append("<agg-strand-sets>")

    // First write the ASG vertices (i.e., the strand sets) with 
    // a generated ID. Again the mapping between the vertex and
    // the strand set needs to be recorded and will be used by
    // writeAGGGraph.

    // The ID
    var aggId = 0

    // The vertex to ID mapping
    val aggVerticesWithId = new ListBuffer[(DsOliMetaBoxGraphVertexBoxes, Long, Long)]

    // Cycle through each ASG of each time step
    this.aggGraphs.steps.foreach {
      elem =>
        val (vep, agg) = elem
        agg.graph.nodes.foreach {
          node =>
            val aggVertex = node.value

            if (aggVertex.isInstanceOf[DsOliMetaBoxGraphVertexBoxes]) {
              // Use the mapping of the entry points to their corresponding IDs
              xmlBuffer.append("<agg-strand-set id=\"" + aggId + "\" entry-point-id=\"" + eptIdMappings.get(vep.id).get + "\">")
              xmlBuffer.append("<strands>")
              val aggVertexBoxes = aggVertex.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]
              aggVertexBoxes.boxes.foreach {
                box =>
                  xmlBuffer.append("<strand-id>" + box.id + "</strand-id>")
              }
              xmlBuffer.append("</strands>")
              xmlBuffer.append("</agg-strand-set>")
              aggVerticesWithId.append((aggVertexBoxes, aggId, vep.id))
              aggId += 1
            }
        }
    }
    xmlBuffer.append("</agg-strand-sets>")

    writeAGGGraph(xmlBuffer, aggVerticesWithId)
  }

  /**
   * Write the folded strand graph (FSG)
   * @param xmlBuffer
   */
  def writeFSG2(xmlBuffer: StringBuffer): Unit = {
    xmlBuffer.append("<fsg-strand-sets>")

    val fsgVertices = new ListBuffer[(DsOliMetaBoxGraphVertexBoxes, Long, Long, Long)]
    val fsgVerticesDone = new ListBuffer[(DsOliMetaBoxGraphVertexBoxes, Long, Long, Long)]

    // Cycle through all FSGs
    var currentTimeStep = 1
    this.dsOliMergedMbgs.steps.foreach {
      fsg =>
        println(currentTimeStep + ": fsg.graph.nodes.size: " + fsg.graph.nodes.size)

        // Fetch only the strand set vertices, i.e., leave out ep vertices
        val boxNodes = fsg.graph.nodes.filter(node => node.value.isInstanceOf[DsOliMetaBoxGraphVertexBoxes])
        println(currentTimeStep + ": fsg boxNodes.size: " + boxNodes.size)

        // Check through all strand set vertices
        boxNodes.foreach {
          nodeInner =>
            val node = nodeInner.value.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]
            println(currentTimeStep + ": " + node)

            // Is the vertex contiguous as a whole?
            if (fsgVertices.exists {
              elem =>
                contiguousFSGVertex(node, elem)
            }) {
              // Found contiguous vertex
              val foundNode = fsgVertices.find {
                elem =>
                  contiguousFSGVertex(node, elem)
              }.get
              fsgVertices.remove(fsgVertices.indexOf(foundNode))
              fsgVertices.append((node, foundNode._2, currentTimeStep, node.boxes.size))

              // Does there exist a node which subsumes the current one?
            } else if (fsgVertices.exists {
              elem =>
                subsetFSGVertexExists(node, elem)
            }) {
              // I think, that I need to move the processed vertex into
              // a done-list as soon as it was present but is no longer
              // valid. Otherwise it might be the case, that the above
              // exist test might be true again after some time period.
              // But then we want to create a new vertex.

              // The element is subsumed: remove from the current live set to done
              val doneNode = fsgVertices.find { elem =>
                subsetFSGVertexExists(node, elem)
              }.get
              fsgVertices.remove(fsgVertices.indexOf(doneNode))
              fsgVerticesDone.append(doneNode)

              // But we also need to record the node as a new reincarnation
              fsgVertices.append((node, currentTimeStep, currentTimeStep, node.boxes.size))
            } else {

              // We need to create a new vertex here
              fsgVertices.append((node, currentTimeStep, currentTimeStep, node.boxes.size))
            }
        }
        currentTimeStep += 1
    }
    // Everything processed, need to merge done and live elements
    fsgVerticesDone.appendAll(fsgVertices)

    // Write out the collected strand sets. 
    // ! The (FSG) strand sets do NOT have an ID per default. Instead the
    // ! ID gets created with the fsgVertexId. Unfortunately a mapping
    // ! needs to be kept to be able to refer to the ID later on.

    // Mapping between FSG vertex and generated ID
    val fsgVerticesWithId = new ListBuffer[(DsOliMetaBoxGraphVertexBoxes, Long)]
    // The FSG ID
    var fsgVertexId = 0
    fsgVerticesDone.foreach {
      elem =>
        val (tVertex, tStart, tEnd, tNumElems) = elem
        xmlBuffer.append("<fsg-strand-set id=\"" + fsgVertexId + "\" beginEventTimeStep=\"" + tStart + "\" ceaseEventTimeStep=\"" + tEnd + "\">")
        xmlBuffer.append("<strands>")
        tVertex.boxes.foreach {
          box =>
            xmlBuffer.append("<strand-id>" + box.id + "</strand-id>")

        }
        fsgVerticesWithId.append((tVertex, fsgVertexId))
        fsgVertexId += 1
        xmlBuffer.append("</strands>")
        xmlBuffer.append("</fsg-strand-set>")
    }

    xmlBuffer.append("</fsg-strand-sets>")

    // Now write the FSG as a whole
    writeFSGGraph(xmlBuffer, fsgVerticesWithId)
  }

  /**
   * Write the actual strand connections
   *
   * @param xmlBuffer
   * @param edgeOuter
   * @param id1 the id of the first FSG vertex
   * @param id2 the id of the second fSG vertex
   */
  def writeStrandConnectionsFSG(xmlBuffer: StringBuffer, edgeOuter: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex],
    id1: Long, id2: Long): Unit = {

    val source = edgeOuter.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]
    val target = edgeOuter.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]

    // Cycle through all strand connections
    edgeOuter.strandConns.foreach {
      strandSetEdge =>

        // Again, filter out the correct edges because of the
        // representation of bidirectional edges with two directed edges
        if (this.strandConnIds.exists {
          idTuple =>
            val (id, _, _, _) = idTuple
            id == strandSetEdge.id
        }) {

          xmlBuffer.append("<!-- source: " + strandSetEdge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.foldLeft("")((hist, cur) => hist + "," + cur.id)
            + " target: " + strandSetEdge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.foldLeft("")((hist, cur) => hist + "," + cur.id) + "-->\n");
          xmlBuffer.append("<strand-connection>\n")
          xmlBuffer.append("<id>" + strandSetEdge.id + "</id>\n")
          xmlBuffer.append("<mapping>\n")
          xmlBuffer.append("<mapping-entry fsg-strand-set-id=\"" + id1 + "\" strand-id=\"" + strandSetEdge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id + "\"/>\n")
          xmlBuffer.append("<mapping-entry fsg-strand-set-id=\"" + id2 + "\" strand-id=\"" + strandSetEdge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id + "\"/>\n")
          xmlBuffer.append("</mapping>\n")

          xmlBuffer.append("</strand-connection>\n")
        } else {
          xmlBuffer.append("<!-- skipping edge.id: " + strandSetEdge.id + " because this one is not used in strand-connections. -->")
        }
    }
  }

  /**
   * Fetch the vertex from the vertex to ID mapping
   *
   * @param aggVerticesWithId the mapping
   * @param boxes
   * @return
   */
  def findFSGVertex(aggVerticesWithId: ListBuffer[(DsOliMetaBoxGraphVertexBoxes, Long)], boxes: DsOliMetaBoxGraphVertexBoxes) = {
    aggVerticesWithId.find {
      elem =>
        val (vertex, boxId) = elem
        vertex.boxes.size == boxes.boxes.size &&
          vertex.boxes.forall(box => boxes.boxes.exists(boxTest => box.id == boxTest.id))
    }.get
  }

  /**
   * Write out the FSG strand connection sets
   *
   * @param xmlBuffer
   * @param fsgVerticesWithId the FSG vertex to ID mapping
   */
  def writeFSGGraph(xmlBuffer: StringBuffer, fsgVerticesWithId: ListBuffer[(DsOliMetaBoxGraphVertexBoxes, Long)]): Unit = {

    xmlBuffer.append("<fsg-strand-connection-sets>")
    var id = 0
    var t = 0

    // Cycle through all FSG in each time step
    this.dsOliMergedMbgs.steps.foreach {
      fsg =>

        t += 1

        // Filter for bidirectional edges
        val biDiEdges = new ListBuffer[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]()

        // Cycle through the edges of the current FSG
        fsg.graph.edges.foreach {
          edgeInner =>

            val edgeOuter = edgeInner.toOuter
            val source = edgeOuter.source
            val target = edgeOuter.target

            // Check, if the bidirectional edge exists
            if (!biDiEdges.contains(edgeOuter)) {

              // Check, that the strand connections are not empty
              // and that the strand connection id exists in the mapping
              if (edgeOuter.strandConns != null &&
                edgeOuter.strandConns.exists {
                  strandSetEdge =>

                    // Again, filter out the correct edges because of the
                    // representation of bidirectional edges with two directed edges
                    this.strandConnIds.exists {
                      idTuple =>
                        val (id, _, _, _) = idTuple
                        id == strandSetEdge.id
                    }
                }) {

                // We are only interested in connections between strands, i.e., not between eps and strands
                if (source.isInstanceOf[DsOliMetaBoxGraphVertexBoxes] &&
                  target.isInstanceOf[DsOliMetaBoxGraphVertexBoxes]) {

                  id += 1
                  xmlBuffer.append("<fsg-strand-connection-set id=\"" + id + "\" beginEventTimeStep=\"" + t + "\" ceaseEventTimeStep=\"" + t + "\">\n")

                  // Fetch the vertex with the corresponding ID, which was created in writeFSG2()
                  val sourceVertex = findFSGVertex(fsgVerticesWithId, edgeOuter.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes])
                  val targetVertex = findFSGVertex(fsgVerticesWithId, edgeOuter.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes])

                  // Write the overlay or unidirectional connection
                  xmlBuffer.append("<kind>")
                  val isBiDiEdge = DsOliGraphUtils.isBidirectionalEdge(edgeOuter, fsg)
                  if (isBiDiEdge) {
                    xmlBuffer.append("<overlay/>")
                  } else {
                    xmlBuffer.append("<unidirectional/>")
                  }
                  xmlBuffer.append("</kind>")

                  // Fetch the corresponding bidirectional edge, if there is one
                  val edgeOuterCor: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex] = if (isBiDiEdge) {
                    val tmp = DsOliGraphUtils.getCorrespondingBiDiEdge(edgeOuter, fsg)
                    biDiEdges.append(tmp)
                    tmp
                  } else {
                    null
                  }

                  // Write the strand connections of both directions
                  if (edgeOuter.strandConns != null || (edgeOuterCor != null && edgeOuterCor.strandConns != null)) {
                    xmlBuffer.append("<strand-connections>")
                    if (edgeOuter.strandConns != null) {
                      writeStrandConnectionsFSG(xmlBuffer, edgeOuter, sourceVertex._2, targetVertex._2)
                    }
                    xmlBuffer.append("<!-- cor -->")
                    if (edgeOuterCor != null) {
                      writeStrandConnectionsFSG(xmlBuffer, edgeOuterCor, targetVertex._2, sourceVertex._2)
                    }
                    xmlBuffer.append("</strand-connections>")
                  }

                  // Detect labels
                  xmlBuffer.append("<labels>")

                  if (edgeOuterCor != null) {
                    val (ccCountCor, sortedccCountCor, highestTagCor, highestTagEvidenceCor) = getTagElements(edgeOuterCor)

                    // Fetch the labels with the label count and summed evidence
                    val ccCount = aggStrandConnectionLabels(xmlBuffer, edgeOuter)

                    // Print the labels
                    ccCount.foreach {
                      cc =>
                        val (tag, (cnt, evidence)) = cc
                        writeEdgeLabel(xmlBuffer, tag, evidence, sourceVertex._1, sourceVertex._2, targetVertex._1, targetVertex._2, "fsg-strand-set-id")
                    }

                    // If this is bidirectional, do the corresponding edge as well
                    if (isBiDiEdge) {
                      val ccCountCor = aggStrandConnectionLabels(xmlBuffer, edgeOuterCor)
                      val sourceVertexCor = findFSGVertex(fsgVerticesWithId, edgeOuterCor.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes])
                      val targetVertexCor = findFSGVertex(fsgVerticesWithId, edgeOuterCor.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes])
                      xmlBuffer.append("<!-- cor -->")
                      ccCountCor.foreach {
                        cc =>
                          val (tag, (cnt, evidence)) = cc
                          writeEdgeLabel(xmlBuffer, tag, evidence, sourceVertexCor._1, sourceVertexCor._2, targetVertexCor._1, targetVertexCor._2, "fsg-strand-set-id")
                      }
                    }
                  }
                  xmlBuffer.append("</labels>")
                }
                xmlBuffer.append("</fsg-strand-connection-set>")
              }
            }
        }
    }
    xmlBuffer.append("</fsg-strand-connection-sets>")
  }

  /**
   * Create an XML representation of all the information created by DSI during the analysis.
   * This includes low level information about PTGs (e.g. memory vertices and edges including
   * their lifetime), cells,  representation of the SG, FSG and AGG.
   *
   * The XML is collected in a StringBuffer and gets written out as a whole after all information
   * is gathered.
   *
   */
  def createXML(): Unit = {
    val funSignature = classSignature + "::createXML: "
    val xmlBuffer = new StringBuffer();

    // Stores the vertices together with their start/end times
    val verticesStore = new HashMap[Long, (DsOliVertex, Long, Long)]()
    // Stores the edges together with their start/end times
    val edgesStore = new HashMap[Long, (DsOliDiEdge[DsOliVertex], Long, Long)]()
    // Stores the cells together with their start/end times
    val cellsStore = new HashMap[Long, (DsOliCell, Long, Long)]()
    // Stores the strands together with their start/end times
    val strandsStore = new HashMap[Long, (DsOliBox, Long, Long)]()

    // Start of the XML file
    xmlBuffer.append("<?xml version=\"1.0\"?>\n")
    xmlBuffer.append(ptgOpen + "\n")

    calculateVerticesAndEdges(verticesStore, edgesStore)
    calculateStrandsAndCells(cellsStore, strandsStore)

    writeVertices(xmlBuffer, verticesStore)
    writeEdges(xmlBuffer, edgesStore)
    writeCells(xmlBuffer, cellsStore)
    writeStrands(xmlBuffer, strandsStore)

    writeStrandConns2(xmlBuffer)
    writeEPs(xmlBuffer)
    writeFSG2(xmlBuffer)
    writeAGG(xmlBuffer)

    // End of the XML file
    xmlBuffer.append(ptgClose + "\n")

    var dirPath = DsOliPathUtils.getPath
    val writer = new PrintWriter(dirPath + "dsi-" + DsOliPathUtils.getXMLFile + "-artifacts.xml")
    writer.write(xmlBuffer.toString)
    writer.close()
  }

}