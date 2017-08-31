
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
 * DsOliDsNaming.scala created on Jun 19, 2015
 *
 * Description: The class for executing the
 * DSI data structure detection.
 *
 */
package dsnaming

import event.DsOliEvents
import dsnaming.DsOliConConfClassificationTag._
import pointstograph.DsOliPointsToGraphs
import boxcalculation.DsOliBoxSteps
import entrypoint.DsOliEntryPointCreator
import scalax.collection.mutable.Graph
import boxcalculation.DsOliBox
import boxcalculation.DsOliBoxStep
import pointstograph.DsOliVertex
import pointstograph.DsOliVertexMemory
import util.DsOliAddressUtils
import util.DsOliGraphUtils
import boxcalculation.DsOliCell
import dsnaming.DsOliConConfTag._
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.Set
import pointstograph.DsOliGraph
import pointstograph.DsOliDiEdge
import pointstograph.DsOliPTGCreator
import boxcalculation.DsOliBoxesCreator
import util.DsOliTimeStepContainer
import scala.util.control.Breaks._
import util.DsOliBooleanUtils
import extlogger.DsOliLogger
import scala.collection.mutable.HashMap
import scala.collection.mutable.Queue
import pointstograph.DsOliVertexMemory
import test.DsOliTestMethods
import event.DsOliMemoryEvent
import event.DsOliVLSEvent
import event.DsOliMWEvent
import util.DsOliTimeStepContainer
import pointstograph.DsOliVertexMemory
import util.DsOliTimeStepContainer
import pointstograph.DsOliVertexNull
import pointstograph.DsOliVertexMemory
import util.DsOliTimeStepContainer
import java.lang.String._
import dsnaming.DsOliDataStructures._
import PartialFunction._
import pointstograph.DsOliType
import pointstograph.ITypeDB
import util.DsOliGraphUtils
import event.DsOliEvent
import util.DsOliGeneralPurposeUtils
import scala.concurrent.Await
import scala.concurrent.duration.Duration
import scala.concurrent._
import scala.concurrent.ExecutionContext.Implicits._
import scala.concurrent.duration._
import java.lang.management.ThreadMXBean
import java.lang.management.ManagementFactory
import pointstograph.DsOliVertexMemory

/**
 * @author DSI
 *
 * @constructor creates the DSI's naming instance
 * @param events the event trace
 * @param ptgs the points-to graphs for each time step
 * @param boxSteps the strand sets for each time step
 * @param boxCreator the strand creator instance
 * @param eptCreator the entry pointer tag creator instance
 * @param typeDB the type DB
 *
 */
class DsOliDsNaming(val events: DsOliEvents, val ptgs: DsOliPointsToGraphs,
  val boxSteps: DsOliBoxSteps, val boxCreator: DsOliBoxesCreator, val eptCreator: DsOliEntryPointCreator, val typeDB: ITypeDB) extends IDsOliDsNaming {

  // The signature for debug prints
  val classSignature = "Name::"

  // The strand graphs for each time step
  val dsOliMbgs = new DsOliTimeStepContainer[DsOliMetaBoxGraph]()
  // The folded strand graphs for each time step
  val dsOliMergedMbgs = new DsOliTimeStepContainer[DsOliMetaBoxGraph]()

  /**
   * Create a strand vertex for each strand in the
   * strand set.
   *
   * @param mbg the empty strand graph which gets initialized
   * @param boxStep the set of strands to transfer to the strand graph
   */
  def initMBGWithBoxes(mbg: Graph[DsOliMetaBoxGraphVertex, DsOliMetaBoxGraphDiEdge], boxStep: DsOliBoxStep) {
    val funSignature = classSignature + "ntMBGWthBxs: "
    DsOliLogger.debug(funSignature + "entered: ")
    // Each strand will become a vertex in the strand graph
    boxStep.boxes.foreach {
      box =>
        DsOliLogger.debug(funSignature + "adding: " + box._2)
        mbg.add(new DsOliMetaBoxGraphVertexBoxes(box._2))
    }
  }

  /**
   * Get the most upstream cell of a
   * strand in the vertex
   *
   * @param box the strand to inspect
   * @param boxStep the current strand set including cycles
   * @param vertex the vertex through which the strand runs
   * @return Boolean
   */
  def getTopMostBoxCell(box: DsOliBox, boxStep: DsOliBoxStep, vertex: DsOliVertexMemory): DsOliCell = {
    val funSignature = classSignature + "gtTpMstBxCll: "
    DsOliLogger.debug(funSignature + "entered: " + box)
    // Currently the first cell is chosen in the set of cells which are inside the vertex.
    // This practically means, that the box should only have one cell per vertex to be safe!
    val cells = if (box.cycleId != 0) {
      if (boxStep.cycles.contains(box.cycleId)) {
        box.cells ++ boxStep.cycles.get(box.cycleId).get.cells
      } else {
        throw new Exception(funSignature + "cycle id not found: " + box.cycleId)
      }
    } else {
      box.cells
    }
    cells.filter(cell => DsOliAddressUtils.addressInRange(vertex.bAddr, vertex.eAddr, cell.bAddr, cell.eAddr)).head
  }

  /**
   * Add the entry pointer vertices to the strand graph and save
   * all found entry pointers in the eps set.
   *
   * @param t the current time step
   * @param dsOliMbg the current strand graph
   * @param boxStep the set of strands
   * @param eps stores the set of entry pointers
   */
  def addEPVertices(t: Int, dsOliMbg: DsOliMetaBoxGraph, boxStep: DsOliBoxStep, eps: HashMap[Long, (Int, DsOliVertexMemory)]): Unit = {
    val funSignature = classSignature + "ddPVrtcs: "
    DsOliLogger.debug(funSignature + "entered: ")

    // Fetch the current points-to graph
    val ptg = ptgs.graphs(t)
    // Get the edges and vertices sets
    val edges = ptg.graph.edges
    val vertices = ptg.graph.nodes

    val edgeIterator = edges.iterator
    // Process ep pointers
    while (edgeIterator.hasNext) {
      val edge = edgeIterator.next.toOuter

      // Here the actual ep filtering is done
      if (eptCreator.isEP(edge.sAddr + edge.sOffset, edge.tAddr + edge.tOffset, boxStep, ptg).isDefined) {
        dsOliMbg.addEPVertex(edge.source)

        // Keep track of the entry pointers
        if (!eps.contains(edge.source.id)) eps.put(edge.source.id, (t, ptg.getMemoryVertex(edge.source)))

        // Can only be an ept, if target is a memory vertex
        val targetVertex = ptg.getMemoryVertex(edge.target)

        // Get all boxes running through this target vertex
        val boxesThroughTarget = boxStep.boxes.filter(boxTuple => eptCreator.boxRunsThroughVertex(targetVertex, boxTuple._2.cells, boxTuple._2.cycleId, boxStep.cycles))
        boxesThroughTarget.foreach {
          boxTuple =>
            val (boxId, box) = boxTuple
            val cell = getTopMostBoxCell(box, boxStep, targetVertex)
            val coff = (cell.bAddr + box.offset) - (edge.tAddr + edge.tOffset)
            val conConf = new DsOliConConf(ccEntryPointer, (edge.sOffset, coff))
            // Boxes must always be present at this stage
            val mbgVertex = dsOliMbg.getBoxVertex(box).get
            val epEdge = new DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex](dsOliMbg.getEPVertex(edge.source).get, mbgVertex, conConf)
            epEdge.strandConns.add(epEdge)
            dsOliMbg.graph.add(epEdge)
        }
      }
    }

    // Process static eps with associated boxes
    val vertexIterator = vertices.iterator
    while (vertexIterator.hasNext) {
      val vertex = vertexIterator.next.value
      if (vertex.isInstanceOf[DsOliVertexMemory]
        && DsOliAddressUtils.isStatic(vertex.asInstanceOf[DsOliVertexMemory])) {
        val vertexMem = ptg.getMemoryVertex(vertex)
        val boxesThroughTarget = boxStep.boxes.filter(boxTuple => eptCreator.boxRunsThroughVertex(vertexMem, boxTuple._2.cells, boxTuple._2.cycleId, boxStep.cycles))

        // Process this vertex only if we actually have an ep (boxes exist through this vertex)
        if (boxesThroughTarget.size > 0) {
          dsOliMbg.addEPVertex(vertex)

          // Keep track of the entry pointers
          if (!eps.contains(vertex.id)) eps.put(vertex.id, (t, ptg.getMemoryVertex(vertex)))

          boxesThroughTarget.foreach {
            boxTuple =>
              val (boxId, box) = boxTuple
              val cell = getTopMostBoxCell(box, boxStep, vertexMem)
              val coff = (cell.bAddr + box.offset) - vertexMem.bAddr
              val conConf = new DsOliConConf(ccStaticEP, (0, coff))
              val mbgVertex = dsOliMbg.getBoxVertex(box).get

              val epEdge = new DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex](dsOliMbg.getEPVertex(vertex).get, mbgVertex, conConf)
              epEdge.strandConns.add(epEdge)
              dsOliMbg.graph.add(epEdge)
          }
        }
      }
    }
  }

  /**
   * Calculate combinations of elements of the list
   *
   * @param list the list to produce combinations for
   * @return a list with the combinations
   */
  def calculateCombinations[A](list: List[A]): List[(A, A)] = {
    val funSignature = classSignature + "clcltCmbntns: "
    DsOliLogger.debug(funSignature + "entered: ")
    val retList = ListBuffer[(A, A)]()
    for (i <- 0 until list.length) {
      for (e <- (i + 1) until list.length) {
        retList.append((list(i), list(e)))
        retList.append((list(e), list(i)))
      }
    }
    retList.toList
  }

  /**
   * Calculate combination between edges
   *
   * @param list list of edges to combine
   * @return list of edge tuples
   */
  def calculateEdgeCombinations[A](list: List[A]): List[(A, A)] = {
    val funSignature = classSignature + "clcltdgCmbntns[]: "
    DsOliLogger.debug(funSignature + "entered: ")
    val retList = ListBuffer[(A, A)]()
    for (i <- 0 until list.length) {
      for (e <- (i + 1) until list.length) {
        retList.append((list(i), list(e)))
      }
    }
    retList.toList
  }

  /**
   * Fetch all vertices through which a strand runs
   *
   * @param ptg the points-to graph
   * @param box the strand to find the corresponding vertices for
   * @param boxStep the set of strands
   * @return the list of vertices
   */
  def calculateVerticesForBox(ptg: DsOliGraph, box: DsOliBox, boxStep: DsOliBoxStep): List[DsOliVertexMemory] = {
    val funSignature = classSignature + "clcltVrtcsFrBx: "
    DsOliLogger.debug(funSignature + "entered: ")

    // We are only interested in memory vertices and the strand needs to run through the memory vertex
    val innerNodesThroughBox = ptg.graph.nodes.iterator.filter(node => node.value.isInstanceOf[DsOliVertexMemory]).
      filter(node => eptCreator.boxRunsThroughVertex(ptg.getMemoryVertex(node.value), box.cells, box.cycleId, boxStep.cycles))

    // Create the list of outer nodes with the appropriate cast to memory vertex
    innerNodesThroughBox.map(innerNode => ptg.getMemoryVertex(innerNode.value)).toList
  }

  /**
   * Check through all cells and see if there exists a cell
   * covering the given address
   *
   * @param cells the list of cells to check the address against
   * @param addr the address to check
   * @return Boolean
   */
  def edgeStartAddrInCells(cells: ListBuffer[DsOliCell], addr: Long): Boolean = {
    cells.exists {
      cell =>
        DsOliAddressUtils.addressInRange(cell.bAddr, cell.eAddr, addr, addr)
    }
  }

  /**
   * Checks, that the given address lies inside of the
   * given strand
   *
   * @param box the strand to check
   * @param addr the address to check
   * @param boxStep the set of strands including the cycles
   * @return Boolean
   */
  def startAddrInBox(box: DsOliBox, addr: Long, boxStep: DsOliBoxStep): Boolean = {
    // First check the linear part
    val startAddrInBoxLinear = edgeStartAddrInCells(box.cells, addr)

    // If not found already in linear part, try the (potentially present) cyclic part
    if (!startAddrInBoxLinear) {
      if (box.cycleId != 0) {
        edgeStartAddrInCells(boxStep.cycles.get(box.cycleId).get.cells, addr)
      } else {
        false
      }
    } else {
      true
    }
  }

  /**
   * Calculate the pointer based connections between strands.
   * The test is performed from strand A to strand B, i.e.,
   * strand A is the source, strand B is the target.
   *
   * @param ptg the points-to graph
   * @param boxStep the current strand set
   * @param verticesA the vertices through strand A
   * @param verticesB the vertices through strand B
   * @param boxA the strand A
   * @param boxB the strand B
   * @return the connections between the given strands
   */
  def calculateEdgesBetweenBoxes(ptg: DsOliGraph, boxStep: DsOliBoxStep,
    verticesA: List[DsOliVertexMemory], verticesB: List[DsOliVertexMemory],
    boxA: DsOliBox, boxB: DsOliBox): ListBuffer[DsOliDiEdge[DsOliVertex]] = {
    val funSignature = classSignature + "clcltdgsBtwnBxs: "
    DsOliLogger.debug(funSignature + "entered: boxA.id: " + boxA.id + " boxB.id: " + boxB.id)
    var retEdges = new ListBuffer[DsOliDiEdge[DsOliVertex]]()

    // Process all edges of the points-to graph and 
    // filter out the edges which connect two strands
    // without fulfilling a linkage condition
    ptg.graph.edges.iterator.foreach {
      edgeInner =>
        val edge = edgeInner.toOuter
        DsOliLogger.debug(funSignature + "testing edge: " + edge)

        // Optional: source and target are not the same
        val sourceNotTarget = true //edge.source != edge.target

        // Check through the vertices of a strand a to find the source
        // of the current edge
        val sourceInA = verticesA.exists { vertex =>
          DsOliLogger.debug(funSignature + "testing vertex (A): " + vertex)
          DsOliLogger.debug(funSignature + "against source: " + edge.source)
          vertex == edge.source
        }
        // Check through the vertices of a strand a to find the source
        // of the current edge
        val targetInB = verticesB.exists { vertex =>
          DsOliLogger.debug(funSignature + "testing vertex (B): " + vertex)
          DsOliLogger.debug(funSignature + "against target: " + edge.target)
          vertex == edge.target
        }
        val noMinCond = boxCreator.minCond(edge.sAddr + edge.sOffset, edge.tAddr + edge.tOffset, ptg, boxStep).isEmpty

        val startAddrInBoxA = startAddrInBox(boxA, edge.sAddr + edge.sOffset, boxStep) // sAddr + sOffset
        val startAddrInBoxB = startAddrInBox(boxB, edge.tAddr + edge.tOffset, boxStep) // tAddr + tOffset

        DsOliLogger.debug(funSignature + "sourceNotTarget: " + sourceNotTarget)
        DsOliLogger.debug(funSignature + "sourceInA: " + sourceInA)
        DsOliLogger.debug(funSignature + "targetInB: " + targetInB)
        DsOliLogger.debug(funSignature + "noMinCond: " + noMinCond)
        DsOliLogger.debug(funSignature + "startAddrInBoxA: " + startAddrInBoxA)
        DsOliLogger.debug(funSignature + "startBddrInBoxB: " + startAddrInBoxB)
        if (sourceNotTarget && sourceInA && targetInB && noMinCond && startAddrInBoxA && startAddrInBoxB) {
          retEdges.append(edge)
        }

    }
    DsOliLogger.debug(funSignature + "retEdges found: " + retEdges)
    retEdges
  }

  /**
   * Collect the cell pairs between strand A and strand B
   *
   * @param boxA the first strand
   * @param boxB the second strand
   * @param boxStep the current strand set
   * @param ptg the current points-to graph
   * @return a tuple with the number of cell pairs and a list of the actual cell pairs
   */
  def cellPairs(boxA: DsOliBox, boxB: DsOliBox, boxStep: DsOliBoxStep, ptg: DsOliGraph): (Int, ListBuffer[(DsOliCell, DsOliCell)]) = {
    val funSignature = classSignature + "cllPrs: "
    // Currently this only works for one struct per vertex with one cell per box of the vertex.
    // For support of multiple structs per vertex one needs to find the minimal enclosing type for the cells.
    // In case of a box, which runs through multiple cells per vertex, one needs to select the top most cell per box.
    DsOliLogger.debug(funSignature + "entered")

    // Collect the cells
    val boxACells = (if (boxA.cycleId != 0) boxStep.cycles.get(boxA.cycleId).get.cells ++ boxA.cells else boxA.cells)
    val boxBCells = (if (boxB.cycleId != 0) boxStep.cycles.get(boxB.cycleId).get.cells ++ boxB.cells else boxB.cells)

    val cellPairs = new ListBuffer[(DsOliCell, DsOliCell)]()

    // Check the cells of strand A by fetching the
    // corresponding vertex for the cell and then 
    // try to find a cell type inside of this vertex
    // which covers cell A.
    val pairCnt = boxACells.count { cellA =>

      // Get vertex
      val vertOpt = ptg.getVertexForAddress(cellA.bAddr)
      if (vertOpt.isDefined) {

        // Get the memory vertex
        val vertex = ptg.getMemoryVertex(vertOpt.get)

        // Try to find a cell which covers the current cell
        val expandOpt = this.typeDB.expandCellTypeOutwards(vertex, cellA, boxB, boxStep)
        if (expandOpt.isDefined) {
          val (_, _, cellB) = expandOpt.get
          cellPairs.append((cellA, cellB))
          true
        } else {
          false
        }
      } else {
        false
      }
    }
    (pairCnt, cellPairs)
  }

  /**
   * Produce all cyclic cell sequences, where each
   * cell of the initial sequence is the start cell
   *
   * @param cyclicCells the cell sequence to start from
   * @return a list of cell sequences
   */
  def getAllBoxRotations(cyclicCells: ListBuffer[DsOliCell]): ListBuffer[ListBuffer[DsOliCell]] = {
    val funSignature = classSignature + "gtllBxRttns: "
    DsOliLogger.debug(funSignature + "entered: ")
    val retList = new ListBuffer[ListBuffer[DsOliCell]]
    // Iterate through all cells of the list to make
    // each of it the start sequence once
    for (i <- 0 until cyclicCells.length) {
      val cellList = new ListBuffer[DsOliCell]
      // Iterate over all cell sequences again and create
      // the new sequence by wrapping it around with the
      // modulus operator
      for (e <- 0 until cyclicCells.length) {
        cellList.append(cyclicCells((i + e) % cyclicCells.length))
      }
      DsOliLogger.debug(funSignature + cellList)
      retList.append(cellList)
    }
    retList
  }

  /**
   * Checks, that the strands run in the reverse
   * order.
   *
   * @param boxA the first strand
   * @param boxB the second strand
   * @param boxStep the current strand set including cycles
   * @param ptg the current points-to graph
   */
  def connectionsRespectListOrderingCyclic(boxA: DsOliBox, boxB: DsOliBox, boxStep: DsOliBoxStep, ptg: DsOliGraph): Boolean = {
    val funSignature = classSignature + "cnnctnsRspctLstrdrng: "
    DsOliLogger.debug(funSignature + "entered: ")
    // All cells from strand A must produce the same Vertex 
    // as the cells in the reverse direction of strand B
    val boxBRevIter = boxB.cells.reverse.iterator
    // Test the cyclic part
    val cyclicSeqCorrect = if (boxA.cycleId != 0 && boxB.cycleId != 0) {

      val boxAcyclic = boxStep.cycles.get(boxA.cycleId).get.cells
      val boxBcyclic = boxStep.cycles.get(boxB.cycleId).get.cells
      val boxRotations = getAllBoxRotations(boxBcyclic)
      boxRotations.exists {
        boxRotation =>
          val boxBRev = boxRotation.reverse
          DsOliLogger.debug(funSignature + "boxBRev: " + boxBRev)
          val boxBRevIter = boxRotation.reverse.iterator
          // Cycle forwards through cells of strand A and
          // check backwards through cells of strand B
          // by comparing the vertices for both cells 
          // which always must be equal
          val forall = boxAcyclic.forall {
            cellA =>
              DsOliLogger.debug(funSignature + "testing against cellA: " + cellA)
              val hasNext = boxBRevIter.hasNext
              // Produced vertices must be equal
              if (hasNext) {
                val next = boxBRevIter.next
                DsOliLogger.debug(funSignature + "testing against: " + next)
                DsOliLogger.debug(funSignature + "vertexA: " + ptg.getVertexForAddress(cellA.bAddr))
                DsOliLogger.debug(funSignature + "vertexB: " + ptg.getVertexForAddress(next.bAddr))
                ptg.getVertexForAddress(cellA.bAddr) == ptg.getVertexForAddress(next.bAddr)
              } else {
                DsOliLogger.debug(funSignature + "nothing to test against")
                false
              }
          }
          DsOliLogger.debug(funSignature + "forall: " + forall)
          forall
      }
    } else if (boxA.cycleId == 0 && boxB.cycleId == 0) {
      true
    } else {
      // only one of the two boxes is cyclic
      false
    }
    cyclicSeqCorrect
  }

  /**
   * Check, that strands are running in reverse
   *
   * @param boxA the first strand
   * @param boxB the second strand
   * @param boxStep the current strand set
   * @param ptg the current points-to graph
   * @return Boolean
   */
  def connectionsRespectListOrdering(boxA: DsOliBox, boxB: DsOliBox, boxStep: DsOliBoxStep, ptg: DsOliGraph): Boolean = {
    val funSignature = classSignature + "cnnctnsRspctLstrdrng: "
    DsOliLogger.debug(funSignature + "entered: ")
    // All cells from strand A must produce the same Vertex as the cells in 
    // the reverse direction of strand B
    val boxBRevIter = boxB.cells.reverse.iterator
    boxA.cells.forall {
      cellA =>
        boxBRevIter.hasNext &&
          ptg.getVertexForAddress(cellA.bAddr) == ptg.getVertexForAddress(boxBRevIter.next.bAddr)
    }
  }

  /**
   * Checks for a cyclic doubly linked list
   *
   * @param boxALen length of first strand
   * @param boxBLen length of second strand
   * @param numCellPairs number of connections between strands
   * @param boxA the first strand
   * @param boxB the second strand
   * @param boxStep the current strand set
   * @param ptg the current points-to graph
   * @return Boolean
   */
  def isCDLL(boxALen: Int, boxBLen: Int, numCellPairs: Int, boxA: DsOliBox, boxB: DsOliBox,
    boxStep: DsOliBoxStep, ptg: DsOliGraph): Boolean = {
    val funSignature = classSignature + "sCDLL: "

    // Debug
    DsOliLogger.debug(funSignature + "entered: ")
    DsOliLogger.debug(funSignature + "boxA.cycleId: " + boxA.cycleId + " boxA.cType: " + boxA.cType)
    DsOliLogger.debug(funSignature + "boxB.cycleId: " + boxB.cycleId + " boxB.cType: " + boxB.cType)

    // Both are cyclic, the strand length is equal and we have at least two elements, 
    // all elements are connected and the strand types are equal
    if (boxA.cycleId != 0 && boxB.cycleId != 0 && boxALen == boxBLen && boxALen > 1 && boxALen == numCellPairs && boxA.cType == boxB.cType) {
      connectionsRespectListOrderingCyclic(boxA, boxB, boxStep, ptg)
    } else false
  }

  /**
   * Checks for a doubly linked list
   *
   * @param boxALen length of first strand
   * @param boxBLen length of second strand
   * @param numCellPairs number of connections between strands
   * @param boxA the first strand
   * @param boxB the second strand
   * @param boxStep the current strand set
   * @param ptg the current points-to graph
   * @return Boolean
   */
  def isDLL(boxALen: Int, boxBLen: Int, numCellPairs: Int, boxA: DsOliBox,
    boxB: DsOliBox, boxStep: DsOliBoxStep, ptg: DsOliGraph): Boolean = {
    val funSignature = classSignature + "sDLL: "
    DsOliLogger.debug(funSignature + "entered: ")
    // Both are non cyclic, the strand length is equal and we have at least two elements, 
    // all elements are connected and the strand types are equal
    if (boxA.cycleId == 0 && boxB.cycleId == 0 && boxALen == boxBLen && boxALen > 1 &&
      boxALen == numCellPairs && boxA.cType == boxB.cType) {
      connectionsRespectListOrdering(boxA, boxB, boxStep, ptg)
    } else false
  }

  /**
   * Checks if both strands are intersecting on
   * two ore more connections
   *
   * @param boxALen length of first strand
   * @param boxBLen length of second strand
   * @param numCellPairs number of connections between strands
   * @param boxA the first strand
   * @param boxB the second strand
   * @param boxStep the current strand set
   * @param ptg the current points-to graph
   * @return Boolean
   */
  def isIntersecting2N(boxALen: Int, boxBLen: Int, numCellPairs: Int, boxA: DsOliBox,
    boxB: DsOliBox, boxStep: DsOliBoxStep, ptg: DsOliGraph): Boolean = {
    val funSignature = classSignature + "sntrsctng2N: "
    DsOliLogger.debug(funSignature + "entered: ")
    numCellPairs > 1
  }

  /**
   * Checks if the strand intersect at the same
   * head node
   *
   * @param boxALen length of first strand
   * @param boxBLen length of second strand
   * @param numCellPairs number of connections between strands
   * @param boxA the first strand
   * @param boxB the second strand
   * @param boxStep the current strand set
   * @param ptg the current points-to graph
   * @return Boolean
   */
  def isIntersectingSameHead(boxALen: Int, boxBLen: Int, numCellPairs: Int, boxA: DsOliBox,
    boxB: DsOliBox, boxStep: DsOliBoxStep, cellPrs: ListBuffer[(DsOliCell, DsOliCell)], ptg: DsOliGraph): Boolean = {
    val funSignature = classSignature + "sntrsctngSmHd: "
    DsOliLogger.debug(funSignature + "entered: ")
    // Non cyclic, only one intersection and the 
    // source and target cells of the given cell
    // pair are the same for strand A and B
    boxA.cycleId == 0 && boxB.cycleId == 0 &&
      numCellPairs == 1 &&
      boxA.cells.head == cellPrs.head._1 &&
      boxB.cells.head == cellPrs.head._2
  }

  /**
   * Checks if both strands are intersecting on
   * one connection
   *
   * @param boxALen length of first strand
   * @param boxBLen length of second strand
   * @param numCellPairs number of connections between strands
   * @param boxA the first strand
   * @param boxB the second strand
   * @param boxStep the current strand set
   * @param ptg the current points-to graph
   * @return Boolean
   */
  def isIntersecting(boxALen: Int, boxBLen: Int, numCellPairs: Int, boxA: DsOliBox,
    boxB: DsOliBox, boxStep: DsOliBoxStep, ptg: DsOliGraph): Boolean = {
    val funSignature = classSignature + "sntrsctng: "
    DsOliLogger.debug(funSignature + "entered: ")
    numCellPairs == 1
  }

  /**
   * Checks, if there is nesting on indirection
   * *
   * @param ccSet set of cell pairs (currently unused)
   * @param edge the edge to operate on
   * @param boxA the first strand
   * @param boxB the second strand
   * @param edgeSourceVertex the source strand vertex
   * @param edgeTargetVertex the target strand vertex
   * @param graph the graph to operate on
   * @param boxStep the current strand set including cycles
   * @return option the set of edges showing the nesting
   */
  def isPointerNesting(ccSet: Set[(DsOliCell, DsOliCell, DsOliBox, DsOliBox)],
    edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex], boxA: DsOliBox, boxB: DsOliBox,
    edgeSourceVertex: DsOliMetaBoxGraphVertexBoxes, edgeTargetVertex: DsOliMetaBoxGraphVertexBoxes,
    graph: DsOliMetaBoxGraph, boxStep: DsOliBoxStep): Option[Set[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]] = {
    val funSignature = classSignature + "sPntrNstng: "
    DsOliLogger.debug(funSignature + "entered: ")

    val nestOpt = nodeHasNesting(edgeSourceVertex, graph, edge.ccSet, edge.conConf,
      edgeTargetVertex.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxType, boxStep)

    nestOpt
  }

  /**
   * Checks, if there is intersection on one node on indirection
   * *
   * @param ccSet set of cell pairs (currently unused)
   * @param edge the edge to operate on
   * @param boxA the first strand
   * @param boxB the second strand
   * @param edgeSourceVertex the source strand vertex
   * @param edgeTargetVertex the target strand vertex
   * @param graph the graph to operate on
   * @param boxStep the current strand set including cycles
   * @return Boolean
   */
  def isPointerIntersecting(ccSet: Set[(DsOliCell, DsOliCell, DsOliBox, DsOliBox)], edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex], boxA: DsOliBox, boxB: DsOliBox, edgeSourceVertex: DsOliMetaBoxGraphVertexBoxes, edgeTargetVertex: DsOliMetaBoxGraphVertexBoxes, graph: DsOliMetaBoxGraph): Boolean = {
    val funSignature = classSignature + "sPntrntrsctng: "

    DsOliLogger.debug(funSignature + "entered: ccSet: " + ccSet)
    DsOliLogger.debug(funSignature + "entered: ccSet.size: " + ccSet.size)
    DsOliLogger.debug(funSignature + "entered: ccSet.head._2: " + ccSet.head._2)

    ccSet.size == 1
  }

  /**
   * Get the strand length, i.e., number of cells
   *
   * @param box the strand to measure
   * @param boxStep the current strand set including cycles
   * @return the length of the strand
   */
  def calculateBoxLen(box: DsOliBox, boxStep: DsOliBoxStep): Int = {
    val funSignature = classSignature + "clcltBxLn: "
    DsOliLogger.debug(funSignature + "entered: ")
    box.cells.length + (if (box.cycleId != 0) boxStep.cycles.get(box.cycleId).get.cells.length else 0)
  }

  /**
   * Classify the overlay connection configuration
   *
   * @param cc the connection configuration
   * @param ccSet set of cell pairs (currently unused)
   * @param boxA the first strand
   * @param boxB the second strand
   * @param boxStep the current strand set including cycles
   * @param ptg the current points-to graph
   * @return Set of classifications (which actually only contains one element)
   */
  def classifyCC(cc: DsOliConConf, ccSet: Set[(DsOliCell, DsOliCell, DsOliBox, DsOliBox)],
    boxA: DsOliBox, boxB: DsOliBox, boxStep: DsOliBoxStep, ptg: DsOliGraph): Set[DsOliConConfClassification] = {
    val funSignature = classSignature + "clssfyCC: "

    DsOliLogger.debug(funSignature + "entered: boxA.id = " + boxA.id + " :: boxB.id = " + boxB.id)

    // Fetch the cell pairs between the strands
    val (numCellPairs, cellPrs) = cellPairs(boxA, boxB, boxStep, ptg)

    // Get the strand length
    val boxALen = calculateBoxLen(boxA, boxStep)
    val boxBLen = calculateBoxLen(boxB, boxStep)

    // Debug
    DsOliLogger.debug(funSignature + "numCellPairs: " + numCellPairs)
    DsOliLogger.debug(funSignature + "boxALen: " + boxALen)
    DsOliLogger.debug(funSignature + "boxBLen: " + boxBLen)

    // Classify according to DSI's hierarchy
    // CDLL
    if (isCDLL(boxALen, boxBLen, numCellPairs, boxA, boxB, boxStep, ptg)) {
      DsOliLogger.debug(funSignature + "found CDLL")
      Set[DsOliConConfClassification](new DsOliConConfClassification(CDLL, boxALen + boxBLen + numCellPairs))
    } // DLL
    else if (isDLL(boxALen, boxBLen, numCellPairs, boxA, boxB, boxStep, ptg)) {
      DsOliLogger.debug(funSignature + "found DLL")
      Set[DsOliConConfClassification](new DsOliConConfClassification(DLL, boxALen + boxBLen + numCellPairs))
    } // Intersecting lists 2+ pairs
    else if (isIntersecting2N(boxALen, boxBLen, numCellPairs, boxA, boxB, boxStep, ptg)) {
      DsOliLogger.debug(funSignature + "found intersecting list")
      Set[DsOliConConfClassification](new DsOliConConfClassification(I2o, numCellPairs))
    } // Intersecting lists 1 pair head node
    else if (isIntersectingSameHead(boxALen, boxBLen, numCellPairs, boxA, boxB, boxStep, cellPrs, ptg)) {
      DsOliLogger.debug(funSignature + "found intersecting list")
      Set[DsOliConConfClassification](new DsOliConConfClassification(SHN, 3))
    } // Intersecting lists 1 pair
    else if (isIntersecting(boxALen, boxBLen, numCellPairs, boxA, boxB, boxStep, ptg)) {
      DsOliLogger.debug(funSignature + "found intersecting list")
      Set[DsOliConConfClassification](new DsOliConConfClassification(I1o, numCellPairs))
    } // Default unclassified
    else {
      DsOliLogger.debug(funSignature + "found ovly default")
      Set[DsOliConConfClassification](new DsOliConConfClassification)
    }
  }

  /**
   * Classify the indirect connection configuration
   *
   * @param cc the connection configuration
   * @param ccSet set of cell pairs (currently unused)
   * @param edge the edge to operate on
   * @param boxA the first strand
   * @param boxB the second strand
   * @param boxStep the current strand set including cycles
   * @param graph the graph to operate on
   * @return tuple with set of classifications and set of edges
   */
  def classifyCCPtr(cc: DsOliConConf, ccSet: Set[(DsOliCell, DsOliCell, DsOliBox, DsOliBox)],
    edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex], boxA: DsOliBox, boxB: DsOliBox,
    boxStep: DsOliBoxStep, graph: DsOliMetaBoxGraph): (Set[DsOliConConfClassification], Set[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]) = {
    val funSignature = classSignature + "clssfyCCPtr: "

    // Fetch the source and target strand vertices
    val edgeSourceVertex = edge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]
    val edgeTargetVertex = edge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]

    // Nesting on pointers
    val ptrNestingOpt = isPointerNesting(ccSet, edge, boxA, boxB, edgeSourceVertex, edgeTargetVertex, graph, boxStep)
    if (ptrNestingOpt.isDefined) {
      DsOliLogger.debug(funSignature + "found ptr nesting")
      (Set[DsOliConConfClassification](new DsOliConConfClassification(Ni, 1)), ptrNestingOpt.get)
      // Intersecting on pointers
    } else if (isPointerIntersecting(ccSet, edge, boxA, boxB, edgeSourceVertex, edgeTargetVertex, graph)) {
      DsOliLogger.debug(funSignature + "found ptr intersecting one")
      (Set[DsOliConConfClassification](new DsOliConConfClassification(I1i, 1)), Set(edge))
    } else {
      // Default unclassified
      DsOliLogger.debug(funSignature + "found ptr default")
      (Set[DsOliConConfClassification](new DsOliConConfClassification(ccNoPtrClassification, 0)), Set(edge))
    }

  }

  /**
   * Initialize the connection configuration classification and
   * add the edge into the strand graph
   *
   * @param offsGroups the connection configurations
   * @param boxA the source strand
   * @param boxB the target strand
   * @param t the current time step
   * @param boxStep the current strand set including the cycles
   * @param ptg the current points-to graph
   * @param dsOliMbg the graph to store everything
   */
  def classifyCCsAndAddEdges(offsGroups: DsOliOffsGroups, boxA: DsOliBox, boxB: DsOliBox, t: Int, boxStep: DsOliBoxStep, ptg: DsOliGraph, dsOliMbg: DsOliMetaBoxGraph): Unit = {
    //    val funSignature = classSignature + "classifyCCsAndAddEdges: "
    val funSignature = classSignature + "clssfyCCsnddddgs: "
    DsOliLogger.debug(funSignature + "entered: ")
    offsGroups.conConfToCells.foreach {
      case (cc, ccSet) =>
        val ccClassified = Set[DsOliConConfClassification](new DsOliConConfClassification)
        val mbgVertexA = dsOliMbg.getBoxVertex(boxA).get
        val mbgVertexB = dsOliMbg.getBoxVertex(boxB).get
        val edge = new DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex](mbgVertexA, mbgVertexB, ccSet, cc, ccClassified,
          Set[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]())
        edge.strandConns.add(edge)
        dsOliMbg.graph.add(edge)
    }
  }

  /**
   * Find the cell, which matches the given address
   *
   * @param cells the cells to search in for the given address
   * @param address the address to search for in the cells
   * @param vertex the vertex in which the cell resides
   * @return Option the found cell
   */
  def findCell(cells: ListBuffer[DsOliCell], address: Long, vertex: DsOliVertexMemory): Option[DsOliCell] = {
    cells.find(cell => cell.bAddr <= address && address <= cell.eAddr &&
      cell.bAddr >= vertex.bAddr && cell.eAddr <= vertex.eAddr)
  }

  /**
   * Fetch the cell inside of a strand and the given vertex. The given address
   * makes the cell unique, as in principle multiple cells can reside inside
   * of a given vertex.
   *
   * @param box the strand that contains the cells to search in
   * @param boxStep the strand set including the cycles
   * @param vertex the vertex in which the cell needs to reside
   * @param address the address that needs to be matched with the cell
   * @return Option the found cell
   */
  def getCellForEdgeInVertex(box: DsOliBox, boxStep: DsOliBoxStep, vertex: DsOliVertexMemory, address: Long): DsOliCell = {
    // Edge start address is inside of cell and cell is inside of vertex
    // Linear sequence present?
    val cellOpt = if (box.cells.size > 0) {
      findCell(box.cells, address, vertex)
    } else {
      None
    }
    // If found in linear sequence -> return
    if (cellOpt.isDefined) {
      cellOpt.get
      // Not found in linear sequence -> try cyclic sequence
    } else if (box.cycleId != 0) {
      findCell(boxStep.cycles.get(box.cycleId).get.cells, address, vertex).get
      // Not found at all, should not happen
    } else {
      throw new Exception("Error 101: No cell found for edge")
    }
  }

  /**
   * Calculate the pointer connection configurations between the
   * given strands for the strand graph
   *
   * @param t the current time step
   * @param edgesAB the edges between strands A and B
   * @param boxA the source strand A
   * @param boxB the target strand B
   * @param boxStep the set of strands including cycles
   * @param ptg the current points-to graph
   * @param dsOliMbg the strand graph to add the connection configurations to
   */
  def addPtrCCs(t: Int, edgesAB: ListBuffer[DsOliDiEdge[DsOliVertex]], boxA: DsOliBox, boxB: DsOliBox, boxStep: DsOliBoxStep, ptg: DsOliGraph, dsOliMbg: DsOliMetaBoxGraph): Unit = {
    //    val funSignature = classSignature + "addPtrCCs: "
    val funSignature = classSignature + "ddPtrCCs: "
    DsOliLogger.debug(funSignature + "entered: ")

    val offsGroups = new DsOliOffsGroups()
    edgesAB.foreach { edge =>

      val vertexS = ptg.getMemoryVertex(edge.source)
      
      val cellA = getCellForEdgeInVertex(boxA, boxStep, vertexS, edge.sAddr + edge.sOffset)
      val cellB = getCellForEdgeInVertex(boxB, boxStep, ptg.getMemoryVertex(edge.target), edge.tAddr + edge.tOffset)

      DsOliLogger.debug(funSignature + "found cellA: " + cellA)
      DsOliLogger.debug(funSignature + "found cellB: " + cellB)

      // (source: rel to cell, target: rel to incoming ptr)
      val coff = (cellB.bAddr + boxB.offset) - (edge.tAddr + edge.tOffset)
      DsOliLogger.debug(funSignature + "calculated offset: " + ((edge.sAddr + edge.sOffset) - cellA.bAddr) + "::" + coff)

      val conConf = new DsOliConConf(ccPointer, (edge.sAddr + edge.sOffset - cellA.bAddr, coff))
      DsOliLogger.debug(funSignature + "adding: conConf: " + conConf + "(cellA,cellB): " + (cellA, cellB))

      offsGroups.addCC(conConf, (cellA, cellB, boxA, boxB))
    }
    classifyCCsAndAddEdges(offsGroups, boxA, boxB, t, boxStep, ptg, dsOliMbg)
  }

  /**
   * Add the overlay connection configurations
   *
   * @param verticesAB the set of vertices which contain strands A and B
   * @param boxA the source strand A
   * @param boxB the target strand B
   * @param boxStep the current strand set including cycles
   * @param t the current time step
   * @param ptg the current points-to graph
   * @param dsOliMbg the graph to store the connections in
   */
  def addOverlayCCs(verticesAB: scala.collection.immutable.Set[DsOliVertexMemory],
    boxA: DsOliBox, boxB: DsOliBox, boxStep: DsOliBoxStep, t: Int, ptg: DsOliGraph, dsOliMbg: DsOliMetaBoxGraph): Unit = {
    //    val funSignature = classSignature + "addOverlayCCs: "
    val funSignature = classSignature + "ddvrlyCCs: "
    DsOliLogger.debug(funSignature + "entered: ")
    // Store the connection configurations
    val offsGroups = new DsOliOffsGroups()
    // Cycle through all vertices which show overlay connections
    // and process each cell of the source strand to find the
    // corresponding overlay connection
    verticesAB.foreach {
      vertex =>
        DsOliLogger.debug(funSignature + "processing vertex: " + vertex)

        // Fetch the cells of the source strand
        val cells = if (boxA.cycleId == 0) {
          DsOliLogger.debug(funSignature + "non cyclic, using linear cells only.")
          boxA.cells
        } else {
          DsOliLogger.debug(funSignature + "cyclic, adding cyclic cells to linear cells")
          boxA.cells ++ boxStep.cycles.get(boxA.cycleId).get.cells
        }
        // Cycle through the cells and calculate the overlay connection configurations:
        // most important the offset calculation
        cells.foreach {
          cellA =>
            DsOliLogger.debug(funSignature + "processing cell: " + cellA)
            // Only process the current vertex
            if (DsOliAddressUtils.addressInRange(vertex.bAddr, vertex.eAddr, cellA.bAddr, cellA.eAddr)) {
              // Expand this cell type as long as the first cell from boxB is enclosed
              val expandOpt = this.typeDB.expandCellTypeOutwards(vertex, cellA, boxB, boxStep)
              if (expandOpt.isDefined) {
                val (startAddr, fieldType, cellB) = expandOpt.get
                DsOliLogger.debug(funSignature + "expansion was successful: " + cellB)
                // Record the overlay connection configuration
                val w = (cellB.bAddr + boxB.offset) - cellA.bAddr
                val x = (cellA.bAddr + boxA.offset) - cellB.bAddr
                val conConf = new DsOliConConf(ccOverlay, (w, x))
                offsGroups.addCC(conConf, (cellA, cellB, boxA, boxB))

              } else {
                DsOliLogger.debug(funSignature + "no expansion")
              }
            }
        }

    }
    // Actually create the connections between the strands in the strand
    classifyCCsAndAddEdges(offsGroups, boxA, boxB, t, boxStep, ptg, dsOliMbg)
  }

  /**
   * Calculate all strands that are running through a given vertex
   *
   * @param nodeInner the vertex to check against
   * @param boxStep the current set of strands
   *
   * @return a list of strands running through the vertex
   */
  def calculateBoxesForVertex(nodeInner: DsOliVertexMemory, boxStep: DsOliBoxStep) = {

    val boxesInVertex = new ListBuffer[DsOliBox.BoxId]
    boxStep.boxes.foreach {
      boxTuple =>
        val box = boxTuple._2
        box.cells.foreach {
          cell =>
            if (DsOliAddressUtils.addressInRange(nodeInner.bAddr, nodeInner.eAddr, cell.bAddr, cell.eAddr)) {
              boxesInVertex.append(box.id)
            }
        }
        // Cyclic part needs to be checked separately 
        if (box.cycleId != 0) {
          boxStep.cycles.get(box.cycleId).get.cells.foreach {
            cell =>
              if (DsOliAddressUtils.addressInRange(nodeInner.bAddr, nodeInner.eAddr, cell.bAddr, cell.eAddr)) {
                boxesInVertex.append(box.id)
              }
          }
        }
    }

    boxesInVertex

  }

  /**
   * Does the tuple exist in the list
   *
   * @param tuple the tuple to search for
   * @param testList the list to test against
   * @return Boolean
   */
  def tupleExists(tuple: (DsOliBox.BoxId, DsOliBox.BoxId), testList: ListBuffer[(DsOliBox.BoxId, DsOliBox.BoxId)]): Boolean = {
    testList.exists(compareTuple => compareTuple._1 == tuple._1 && compareTuple._2 == tuple._2)
  }

  /**
   * Optimized strand combination calculation, which iterates the
   * PTG to check all vertices for overlay connections and all
   * edges for indirect connections. Only strands which are
   * either connected by overlay or indirection are chosen for
   * further analysis for building up the strand graph.
   *
   * @param ptg the points-to graph for the current time step
   * @param boxStep the set of strands for the current time step
   * @return a list with strand tuples, that are connected: (overlay: both directions are recorded, indirect: only one direction is considered)
   */
  def calculateCombinationsOptimized(ptg: DsOliGraph, boxStep: DsOliBoxStep) = {
    val funSignature = classSignature + "calculateCombinationsOptimized: "
    val strandsOverlay = new ListBuffer[(DsOliBox.BoxId, DsOliBox.BoxId)]()

    // Fetch the overlay connections
    ptg.graph.nodes.iterator.foreach {
      nodeIter =>
        val node = nodeIter.value
        // Strands are only running through memory vertices
        if (node.isInstanceOf[DsOliVertexMemory]) {
          val nodeMemory = node.asInstanceOf[DsOliVertexMemory]
          val strandsForVertex = calculateBoxesForVertex(nodeMemory, boxStep)

          // All strand permutations need to be calculated as each strand can reach all other strands
          // within the vertex
          val strandCombinationsForVertex = calculateCombinations(strandsForVertex.toList)

          strandCombinationsForVertex.foreach {
            combinationTuple =>
              if (!tupleExists(combinationTuple, strandsOverlay)) {
                strandsOverlay.append(combinationTuple)
              }
          }
        }
    }

    // Fetch the indirect connections
    ptg.graph.edges.iterator.foreach {
      edgeIter =>
        val edge = edgeIter.toOuter
        val sourceVertex = edge.source
        val targetVertex = edge.target

        // Strands are only running through memory vertices
        if (sourceVertex.isInstanceOf[DsOliVertexMemory] && targetVertex.isInstanceOf[DsOliVertexMemory]) {
          val source = sourceVertex.asInstanceOf[DsOliVertexMemory]
          val target = targetVertex.asInstanceOf[DsOliVertexMemory]
          val strandsForSourceVertex = calculateBoxesForVertex(source, boxStep)
          val strandsForTargetVertex = calculateBoxesForVertex(target, boxStep)

          // All source strands are connected with all target strands
          strandsForSourceVertex.foreach {
            strandSource =>
              strandsForTargetVertex.foreach {
                strandTarget =>
                  val combinationTuple = (strandSource, strandTarget)
                  if (!tupleExists(combinationTuple, strandsOverlay)) {
                    strandsOverlay.append(combinationTuple)
                  }
              }
          }
        }
    }

    // Filter the connections for self references and create the final tuple list
    val strandsOverlayRet = new ListBuffer[((DsOliBox.BoxId, DsOliBox), (DsOliBox.BoxId, DsOliBox))]
    strandsOverlay.foreach {
      strand =>
        val strandOne = strand._1
        val strandTwo = strand._2
        // Self references are not needed
        if (strandOne != strandTwo) {
          strandsOverlayRet.append(((strandOne, boxStep.boxes.get(strandOne).get), (strandTwo, boxStep.boxes.get(strandTwo).get)))
        }
    }
    strandsOverlayRet

  }

  /**
   * Create the strand graph
   *
   * @param t the current time step
   * @param eps stores the set of entry pointers that are calculated
   * @param ptg the current points-to graph
   * @param boxStep the set of strands for the current time step
   * @return a strand graph instance
   */
  def calculateMetaBoxGraph(t: Int, eps: HashMap[Long, (Int, DsOliVertexMemory)],
    ptg: DsOliGraph, boxStep: DsOliBoxStep): DsOliMetaBoxGraph = {
    val funSignature = classSignature + "clcltMtBxGrph: "
    DsOliLogger.debug(funSignature + "entered: ")

    // An empty strand graph
    val dsOliMbg = new DsOliMetaBoxGraph()

    // Initialize the strand graph
    initMBGWithBoxes(dsOliMbg.graph, boxStep)
    // Calculate the entry pointer vertices 
    addEPVertices(t, dsOliMbg, boxStep, eps)

    // Cycle through all combination of box pairs (direction is important)
    val boxCombinations = calculateCombinations(boxStep.boxes.toList)
    //val boxCombinations = calculateCombinationsOptimized(ptg, boxStep)

    DsOliLogger.debug(funSignature + "boxStep.boxes: " + boxStep.boxes)
    DsOliLogger.debug(funSignature + "boxCombinations: " + boxCombinations)
    //println(funSignature + "boxCombinations: " + boxCombinations)
    //System.exit(1)
    // Process all strand combinations and calculate the 
    // strand connections between the strands
    boxCombinations.foreach {
      boxesTuple =>
        val (boxATuple, boxBTuple) = boxesTuple
        val (boxAId, boxA) = boxATuple
        val (boxBId, boxB) = boxBTuple

        DsOliLogger.debug(funSignature + "boxA: " + boxA)
        DsOliLogger.debug(funSignature + "boxB: " + boxB)

        // Calculate vertices for boxes
        val verticesA = calculateVerticesForBox(ptg, boxA, boxStep)
        val verticesB = calculateVerticesForBox(ptg, boxB, boxStep)
        DsOliLogger.debug(funSignature + "verticesA: " + verticesA)
        DsOliLogger.debug(funSignature + "verticesB: " + verticesB)

        // Pointer based connection configurations
        // Get edges between source and target boxes
        val edgesAB = calculateEdgesBetweenBoxes(ptg, boxStep, verticesA, verticesB, boxA, boxB)
        DsOliLogger.debug(funSignature + "edgesAB: " + edgesAB)
        addPtrCCs(t, edgesAB, boxA, boxB, boxStep, ptg, dsOliMbg)

        // Overlay based connection configurations: the intersection 
        // between the set of vertices for strand A and B are actually
        // the vertices in which both strand A and strand B are present,
        // i.e., the ones with overlay connections.
        val verticesAB = verticesA.toSet.intersect(verticesB.toSet)
        DsOliLogger.debug(funSignature + "verticesAB: " + verticesAB)
        addOverlayCCs(verticesAB, boxA, boxB, boxStep, t, ptg, dsOliMbg)
    }

    dsOliMbg

  }

  /**
   * Generates instances of the vertex properties
   *
   * @param vertex the vertex to fetch the properties for
   * @return the vertex properties as an instance
   */
  def mbgVertexProperties(vertex: DsOliMetaBoxGraphVertex): DsOliMetaBoxGraphVertexProperties = {
    val funSignature = classSignature + "mbgVrtxPrprts: "
    DsOliLogger.debug(funSignature + "entered: " + vertex)
    vertex match {
      // Entry pointer vertex
      case ep: DsOliMetaBoxGraphVertexEP => new DsOliMetaBoxGraphVertexProperties(true, ep.ep.asInstanceOf[DsOliVertexMemory].vType, 0)
      // Strand set vertex
      case box: DsOliMetaBoxGraphVertexBoxes =>

        DsOliLogger.debug(funSignature + "box.boxType: " + box.boxType)
        DsOliLogger.debug(funSignature + "box.linkOffset: " + box.linkOffset)

        new DsOliMetaBoxGraphVertexProperties(false, box.boxType, box.linkOffset)

    }
  }

  /**
   * Test if the two vertices have sharing
   *
   * @param vertex1 the first vertex
   * @param vertex2 the second vertex
   * @param dsOliMbg the graph to operate on
   * @return Boolean
   */
  def sharingTest(vertex1: DsOliMetaBoxGraphVertex, vertex2: DsOliMetaBoxGraphVertex, dsOliMbg: DsOliMetaBoxGraph): Boolean = {
    val funSignature = classSignature + "shrngTst: "
    DsOliLogger.debug(funSignature + "entered: ")

    // Stores edges from vertex one to two
    val v1ToV2 = new ListBuffer[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]()
    // Stores edges from vertex two to one
    val v2ToV1 = new ListBuffer[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]()

    // Collect the edges
    dsOliMbg.graph.edges.foreach { e =>
      if (e.source == vertex1 && e.target == vertex2) v1ToV2.append(e.toOuter)
      if (e.source == vertex2 && e.target == vertex1) v2ToV1.append(e.toOuter)
    }
    (
      // No connections is fine
      (v1ToV2.size == 0 && v2ToV1.size == 0)
      ||
      (
        // Be sure, that the sizes match
        //v1ToV2.size == v2ToV1.size &&
        // All connections must be overlays and of the same cc
        //v1ToV2.forall(edge1 => edge1.conConf.tag == ccOverlay && v2ToV1.forall(edge2 => edge2.conConf.tag == ccOverlay && edge2.conConf.offsets == edge1.conConf.offsets))))
        // As long as there exists one connection between the vertices that shows sharing, this is enough
        v1ToV2.exists((edge1 => edge1.conConf.tag == ccOverlay && v2ToV1.exists(edge2 => edge2.conConf.tag == ccOverlay && edge2.conConf.offsets == edge1.conConf.offsets)))))
    // v2ToV1.exists((edge1 => edge1.conConf.tag == ccOverlay && v1ToV2.exists(edge2 => edge2.conConf.tag == ccOverlay && edge2.conConf.offsets == edge1.conConf.offsets)))))
  }

  /**
   * Checks, if the two vertices are connected via one common
   * parent vertex.
   *
   * @param vertexA the first vertex
   * @param vertexB the second vertex
   * @param dsOliMbg the graph to operate on
   * @return Boolean
   */
  def threeVerticesCondition(vertexA: DsOliMetaBoxGraphVertex, vertexB: DsOliMetaBoxGraphVertex, dsOliMbg: DsOliMetaBoxGraph): Boolean = {
    val funSignature = classSignature + "thrVrtcsCndtn: "
    DsOliLogger.debug(funSignature + "entered: ")
    val mbg = dsOliMbg.graph

    // Select possible edges that either have A or B
    // as a target
    val possibleThreeVEdges = mbg.edges.filter {
      edgeInner =>
        edgeInner.target == vertexA || edgeInner.target == vertexB
    }
    // Split the set for A and B
    val vertexAEdges = possibleThreeVEdges.filter(e => e.target == vertexA)
    val vertexBEdges = possibleThreeVEdges.filter(e => e.target == vertexB)

    val threeVEdges = vertexAEdges.exists(eA => vertexBEdges.exists(eB => eA.source == eB.source && eA.conConf == eB.conConf))
    threeVEdges && sharingTest(vertexA, vertexB, dsOliMbg)
  }

  /**
   * Checks if there is exclusive sharing between the vertices,
   * i.e., no other
   *
   * @param vertexA the first vertex
   * @param vertexB the second vertex
   * @param dsOliMbg the graph to test on
   * @return Boolean
   */
  def isolatedSharing(vertexA: DsOliMetaBoxGraphVertex, vertexB: DsOliMetaBoxGraphVertex, dsOliMbg: DsOliMetaBoxGraph): Boolean = {
    val funSignature = classSignature + "sltdShrng: "
    DsOliLogger.debug(funSignature + "entered: ")
    // 1) Find edges between the vertex A (source) and vertex B (target) that are overlay
    // 2) Find edges between the vertex B (source) and vertex C (target) that are overlay
    // => these are sharing candidates
    val sharingOpt = (dsOliMbg.graph.edges.find(e => e.source == vertexA && e.target == vertexB && e.conConf.tag == ccOverlay &&
      dsOliMbg.graph.edges.exists(e2 => e2.source == vertexB && e2.target == vertexA && e2.conConf.tag == ccOverlay && e2.conConf.offsets == e.conConf.offsets)))
    // Sharing is found
    // Sharing candidates are found
    (sharingOpt.isDefined &&
      // Test for isolation
      dsOliMbg.graph.edges.forall { e =>
        DsOliBooleanUtils.implies(
          ((e.source == vertexA || e.source == vertexB) || (e.target == vertexA || e.target == vertexB)),
          sharingTest(e.source, e.target, dsOliMbg))
      })
  }

  /**
   * Checks, if either the source or the target of the edge
   * are matched against the given vertices.
   *
   * @param edge the edge to test
   * @param v1 first candidate for the source of the edge
   * @param v2 second candidate for the source of the edge
   * @param dsOliMbg the graph (used for convenience methods)
   * @return Boolean
   */
  def edgeSourceOrTargetMatch(edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex], v1: DsOliMetaBoxGraphVertex, v2: DsOliMetaBoxGraphVertex, dsOliMbg: DsOliMetaBoxGraph): Boolean = {
    val funSignature = classSignature + "dgSrcrTrgtMtch: "
    DsOliLogger.debug(funSignature + "entered edge: " + edge + ", e.source: " + dsOliMbg.printNode(edge.source, prefix = "", suffix = "") + "; e.target: " + dsOliMbg.printNode(edge.target, prefix = "", suffix = ""))
    DsOliLogger.debug(funSignature + "entered v1: " + dsOliMbg.printNode(v1, prefix = "", suffix = "") + "; v2: " + dsOliMbg.printNode(v2, prefix = "", suffix = ""))
    // Test source or target
    edgeSourceMatch(edge, v1, v2) || edgeTargetMatch(edge, v1, v2)
  }

  /**
   * Checks if the source of the edge is either vertex one or two
   *
   * @param edge the edge to test
   * @param v1 first candidate for the source of the edge
   * @param v2 second candidate for the source of the edge
   * @return Boolean
   */
  def edgeSourceMatch(edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex], v1: DsOliMetaBoxGraphVertex, v2: DsOliMetaBoxGraphVertex): Boolean = {
    val funSignature = classSignature + "dgSrcMtch: "
    DsOliLogger.debug(funSignature + "entered: edge: " + edge + "; v1: " + v1 + "; v2: " + v2)
    edge.source == v1 || edge.source == v2
  }

  /**
   * Checks if the target of the edge is either vertex one or two
   *
   * @param edge the edge to test
   * @param v1 first candidate for the target of the edge
   * @param v2 second candidate for the target of the edge
   * @return Boolean
   */
  def edgeTargetMatch(edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex],
    v1: DsOliMetaBoxGraphVertex, v2: DsOliMetaBoxGraphVertex): Boolean = {
    val funSignature = classSignature + "dgTrgtMtch: "
    DsOliLogger.debug(funSignature + "entered: edge: " + edge + "; v1: " + v1 + "; v2: " + v2)
    edge.target == v1 || edge.target == v2
  }

  /**
   * Merge all edges of the given edge set
   *
   * @param dsOliMbg the graph to record the merged edges
   * @param mergeEdgeCandidates the set of edges to merge
   */
  def mergeEdges(dsOliMbg: DsOliMetaBoxGraph, mergeEdgeCandidates: Set[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]], t: Int, cnt: Int): Unit = {
    val funSignature = classSignature + "mrgdgs: "

    DsOliLogger.debug(funSignature + "entered now: " + dsOliMbg)
    DsOliLogger.debug(funSignature)

    // Key: strand source, strand target, connection configuration offsets -> Value: List of edges between them
    // used to group edges according to source, target and connection configuration
    val edgeMap = new HashMap[(DsOliMetaBoxGraphVertex, DsOliMetaBoxGraphVertex, Long, Long), ListBuffer[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]]()

    // Group all edges according to source, target and connection configuration
    mergeEdgeCandidates.foreach {
      edge =>
        DsOliLogger.debug(funSignature + "processing: " + edge)
        // Only merge edges, where a merge has occurred
        val keyTuple = (edge.source, edge.target, edge.conConf.offsets._1, edge.conConf.offsets._2)
        if (!edgeMap.contains(keyTuple)) {
          edgeMap.put(keyTuple, new ListBuffer[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]())
        }
        DsOliLogger.debug(funSignature + "grouping: " + edge)
        edgeMap.get(keyTuple).get.append(edge)
    }

    if (edgeMap.size == 0) DsOliLogger.debug(funSignature + "edgeMap.size == 0")

    // Process all edge groups
    edgeMap.foreach {
      kvTuple =>
        val ((source, target, sourceOffset, targetOffset), v) = kvTuple

        DsOliLogger.debug(funSignature + "processing source: " + source)

        // If more than one edge per group -> merge
        if (v.length > 1) {
          DsOliLogger.debug(funSignature + "Found edges to merge: " + v.length)

          var mergedCCSet = Set[(DsOliCell, DsOliCell, DsOliBox, DsOliBox)]()
          var mergedConConfClass = Set[DsOliConConfClassification]()
          var mergedEdgeSet = Set[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]()
          // Collect all the information over all edges first
          // by iterating over edges and removing them in the same step
          v.foreach {
            edge =>
              DsOliLogger.debug(funSignature + "merging: " + edge)
              DsOliLogger.debug(funSignature + "merging.conConfClass: " + edge.conConfClass)
              DsOliLogger.debug(funSignature + "mergedConConfClass: " + mergedConConfClass)
              // Carry over the connection configuration set
              if (edge.ccSet != null) {
                mergedCCSet = mergedCCSet.union(edge.ccSet)
              }
              if (edge.strandConns != null) {
                mergedEdgeSet = mergedEdgeSet.union(edge.strandConns)
              }

              // Keep the connection configuration classification
              mergedConConfClass = mergedConConfClass.union(edge.conConfClass)

              // Remove the edge
              dsOliMbg.graph.remove(edge)
          }

          DsOliLogger.debug(funSignature + "mergedConConfClass: " + mergedConConfClass)

          // Pick the first conConf. It should be the same for all
          val conConf = v.head.conConf
          // Create a new (merged) edge exactly once
          dsOliMbg.graph.add(new DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex](source, target,
            mergedCCSet, conConf, mergedConConfClass, mergedEdgeSet))

        } else {
          // Directly add the edge, as no merge is needed on only one element
          DsOliLogger.debug(funSignature + "No edges to merge, only one edge in category")
          dsOliMbg.graph.add(v.head)
        }
    }

  }

  /**
   * Calculate the structural repetition for the strand graph, which
   * results in the folded strand graph
   *
   * @param dsOliMbg the strand graph on which the structural repetition will be performed
   * @param t the current time step
   * @return a folded strand graph instance
   */
  def mergeMetaBoxGraph(dsOliMbg: DsOliMetaBoxGraph, t: Int): DsOliMetaBoxGraph = {
    val funSignature = classSignature + "mrgMtBxGrph: "

    DsOliLogger.debug(funSignature + "entered: ")
    DsOliLogger.debug("Calculating time step: " + t)

    // Merge all vertices that perform the same role
    // to reinforce found evidences (structural repetition)
    var completed = false
    var cnt = 0
    while (!completed) {
      completed = true
      val verticesCombinations = calculateEdgeCombinations(dsOliMbg.graph.nodes.toList)
      DsOliLogger.debug(funSignature + "vertices combinations done. attempting to merge.")
      breakable {
        DsOliLogger.debug("Vertices combinations: " + verticesCombinations.size)
        verticesCombinations.foreach {
          case (vertex1Inner, vertex2Inner) =>
            // Try to process as much of the calculated combinations as possible
            if (dsOliMbg.graph.nodes.contains(vertex1Inner) && dsOliMbg.graph.nodes.contains(vertex2Inner)) {

              val vertex1 = vertex1Inner.value
              val vertex2 = vertex2Inner.value

              DsOliLogger.debug(funSignature + "testing vertex1, vertex2: " + vertex1 + "," + vertex2)

              // Merge is possible: 
              // 1) both strand vertices need to have the same properties, i.e., same type of strands and same linkage offset
              // 2) both strand vertices are connected through a common vertex
              // 3) or isolated sharing of the strands
              val v1Props = mbgVertexProperties(vertex1)
              val v2Props = mbgVertexProperties(vertex2)
              if (!v1Props.isEP && !v2Props.isEP && v1Props == v2Props) {
                //if (mbgVertexProperties(vertex1) == mbgVertexProperties(vertex2)) {
                if (threeVerticesCondition(vertex1, vertex2, dsOliMbg) ||
                  isolatedSharing(vertex1, vertex2, dsOliMbg)) {

                  // Debug
                  DsOliLogger.debug(funSignature + "merge condition fullfilled vertex1, vertex2: " + dsOliMbg.printNode(vertex1, prefix = "", suffix = "") + "," + dsOliMbg.printNode(vertex2, prefix = "", suffix = ""))
                  DsOliLogger.debug(funSignature + "graph: " + dsOliMbg)

                  // Collect edges where v1 and/or v2 participate
                  val edges12 = dsOliMbg.graph.edges.filter(e => edgeSourceOrTargetMatch(e.toOuter, vertex1, vertex2, dsOliMbg))
                  DsOliLogger.debug(funSignature + "found edges for removal: " + edges12)

                  // Remove old vertices
                  val v1Rem = dsOliMbg.graph.remove(vertex1)
                  val v2Rem = dsOliMbg.graph.remove(vertex2)
                  DsOliLogger.debug(funSignature + "v1Rem : " + v1Rem + "; v2Rem: " + v2Rem)

                  // Create and add merged vertex: merge by unioning the strand sets
                  val mbgBoxVertex1 = vertex1.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]
                  val mbgBoxVertex2 = vertex2.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]
                  val mergedVertex = new DsOliMetaBoxGraphVertexBoxes(mbgBoxVertex1.boxes.union(mbgBoxVertex2.boxes))
                  dsOliMbg.graph.add(mergedVertex)

                  // Debug
                  DsOliLogger.debug(funSignature + "mergedVertex: " + dsOliMbg.printNode(mergedVertex, prefix = "", suffix = ""))
                  DsOliLogger.debug(funSignature + "mergedVertex: " + mergedVertex)
                  DsOliLogger.debug(funSignature + "graph before node merges: " + dsOliMbg)

                  // Collect all edges between source and target that need to get merged
                  val mergeSet = Set[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]()
                  // Remove old edges and add new ones
                  edges12.foreach { e =>
                    val eOuter = e.toOuter

                    // Debug
                    DsOliLogger.debug(funSignature + "removing edge: " + e)
                    DsOliLogger.debug(funSignature + "removing edge.source: " + dsOliMbg.printNode(e.source.value, prefix = "", suffix = ""))
                    DsOliLogger.debug(funSignature + "removing edge.target: " + dsOliMbg.printNode(e.target.value, prefix = "", suffix = ""))
                    DsOliLogger.debug(funSignature + "eOuter: " + eOuter.conConfClass)

                    // Remove old edge
                    dsOliMbg.graph.remove(eOuter)
                    val newSource = if (edgeSourceMatch(eOuter, vertex1, vertex2)) mergedVertex else eOuter.source
                    val newTarget = if (edgeTargetMatch(eOuter, vertex1, vertex2)) mergedVertex else eOuter.target
                    DsOliLogger.debug(funSignature + "newSource: " + dsOliMbg.printNode(newSource, prefix = "", suffix = ""))
                    DsOliLogger.debug(funSignature + "newTarget: " + dsOliMbg.printNode(newTarget, prefix = "", suffix = ""))
                    val newEdgeSet = Set[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]()
                    if (eOuter.strandConns != null) {
                      eOuter.strandConns.foreach {
                        strandConn =>
                          newEdgeSet.add(strandConn)
                      }
                    }
                    // Note: also carry over the cc set
                    val newEdge = new DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex](newSource, newTarget, eOuter.ccSet, eOuter.conConf, eOuter.conConfClass, newEdgeSet)
                    mergeSet.add(newEdge)
                    DsOliLogger.debug(funSignature + "newEdge: " + newEdge)
                    DsOliLogger.debug(funSignature + "newEdge done: ")
                  }

                  DsOliLogger.debug(funSignature + "graph after node merges: " + dsOliMbg)

                  // Merge edges
                  mergeEdges(dsOliMbg, mergeSet, t, cnt)
                  cnt += 1

                  // We will do one more run in the end, where no changes occur. 
                  completed = false
                }
              }
            }
        }
      }
    }
    // Indicate, that we altered the graph
    dsOliMbg
  }

  /**
   * Recursive stripped down maximum common subgraph
   *
   * @param g1 the ASG
   * @param g2 the FSG
   * @param v1 the entry pointer vertex of g1
   * @param v2 the entry pointer vertex of g2
   * @param subGraph stores the result
   * @param vertexMapping keeps a mapping between the corresponding vertices v2 -> v1
   * @param padding debug
   * @param newEdgesExistingVert save new edges found on existing vertices
   */
  def mcs_rec(g1: DsOliMetaBoxGraph, g2: DsOliMetaBoxGraph, v1: DsOliMetaBoxGraphVertex, v2: DsOliMetaBoxGraphVertex,
    subgraph: DsOliMetaBoxGraph, vertexMapping: HashMap[DsOliMetaBoxGraphVertex, DsOliMetaBoxGraphVertex], padding: String): Unit = {

    val funSignature = classSignature + "mcs_rc: "
    DsOliLogger.debug(funSignature + padding + "entered v1: " + v1)
    DsOliLogger.debug(funSignature + padding + "entered v2: " + v2)

    // Always add the vertex at first
    subgraph.graph.add(v1)
    DsOliLogger.debug(funSignature + padding + "adding mapping: " + v2 + " -> " + v1)
    //if (vertexMapping.contains(v2)) throw new Exception(funSignature + padding + "v2 already in mapping: " + v2 + " -> " + vertexMapping.get(v2) + " :: new mapping: " + v1)
    // Keep a mapping between the vertices used for later lookups
    // outside of the mcs algorithm part
    vertexMapping.put(v2, v1)

    // Process all edges of the first graph and see if
    // there exist corresponding outgoing edges in the
    // second graph
    g1.graph.edges.iterator.foreach {
      edgeInner =>
        val edge = edgeInner.toOuter
        DsOliLogger.debug(funSignature + padding + "testing edge: " + edge)

        // Only outgoing edges from v1
        if (edge.source == v1) {
          // Does there exist a corresponding edge in g2:
          // - connection configurations need to match
          // - the properties of the target vertices need to match
          val existsG2EdgeOpt = g2.graph.edges.find { g2Edge =>
            DsOliLogger.debug(funSignature + padding + "g2 testing edge: " + g2Edge)
            g2Edge.source == v2 &&
              g2Edge.conConf == edge.conConf &&
              mbgVertexProperties(g2Edge.target) == mbgVertexProperties(edge.target)
          }

          // Debug
          val allExistingEdges = g2.graph.edges.filter { g2Edge =>
            DsOliLogger.debug(funSignature + padding + "g2 testing edge: " + g2Edge)
            g2Edge.source == v2 &&
              g2Edge.conConf == edge.conConf &&
              mbgVertexProperties(g2Edge.target) == mbgVertexProperties(edge.target)

          }
          if (allExistingEdges.size > 1) {
            DsOliLogger.debug(funSignature + "size of edges greater 1")
            allExistingEdges.foreach {
              edge =>
                DsOliLogger.debug(funSignature + "\tsize report edge: " + edge)
                DsOliLogger.debug(funSignature + "\tsize report edge.conConf: " + edge.conConf)
            }
          }
          // Debug end

          // Does a corresponding edge actually exist?
          if (existsG2EdgeOpt.isDefined) {

            DsOliLogger.debug(funSignature + padding + "corresponding edge exists for g1 edge: " + edge)
            DsOliLogger.debug(funSignature + padding + "corresponding g2 edge: " + existsG2EdgeOpt.get)
            //DsOliLogger.debug(funSignature + padding + "corresponding edge: " + edge)
            //  Does the target already exist the the subgraph
            val targetExistsOpt = subgraph.graph.nodes.iterator.find {
              nodeInner =>
                nodeInner == edge.target
            }

            val edgeFromG2 = existsG2EdgeOpt.get.toOuter
            DsOliLogger.debug(funSignature + padding + "edge.conConfClass before: " + edge.conConfClass)

            // Decide which connection configuration classification to use. If both are present, 
            // union them, which actually does the temporal repetition.
            edgeInner.conConfClass = if (edge.conConfClass != null && edgeFromG2.conConfClass != null) {
              DsOliLogger.debug(funSignature + padding + "edge.conConfClass: " + edge.conConfClass)
              DsOliLogger.debug(funSignature + padding + "edgeFromG2.conConfClass: " + edgeFromG2.conConfClass)
              edge.conConfClass.union(edgeFromG2.conConfClass)
            } else if (edge.conConfClass != null) {
              edge.conConfClass
            } else {
              DsOliLogger.debug(funSignature + padding + "edgeFromG2.conConfClass: " + edgeFromG2.conConfClass)
              edgeFromG2.conConfClass
            }

            edgeFromG2.strandConns.foreach {
              conn =>
                edgeInner.strandConns.add(conn)
            }

            DsOliLogger.debug(funSignature + padding + "edge.conConfClass after: " + edge.conConfClass)
            DsOliLogger.debug(funSignature + padding + "edgeInner.conConfClass after: " + edgeInner.conConfClass)

            DsOliLogger.debug("mcs_rec: merge source & target")
            mergeBoxes(edge.target, existsG2EdgeOpt.get.target)
            mergeBoxes(edge.source, existsG2EdgeOpt.get.source)

            // If the target already exist, only add the edge as the
            // graph will be processed due to the recursion anyway.
            if (targetExistsOpt.isDefined) {
              DsOliLogger.debug(funSignature + padding + "target exists, only add the edge without recursion: " + edge)
              // Only add the edge
              subgraph.graph.add(edge.source)
              subgraph.graph.add(edge)
              // And aggregate the boxes
              val targetInner = targetExistsOpt.get
              //mergeBoxes(targetInner, edge.target)
            } else {
              DsOliLogger.debug(funSignature + padding + "target does not exist, add the edge and recurse: " + edge)
              val v1Temp = edge.target
              val v2Temp = existsG2EdgeOpt.get.target
              subgraph.graph.add(v1Temp)
              subgraph.graph.add(edge)

              DsOliLogger.debug(funSignature + padding + "recurse with v1Temp, v2Temp: " + v1Temp + "," + v2Temp)

              mcs_rec(g1, g2, v1Temp, v2Temp, subgraph, vertexMapping, padding + " ")
            }
          } else {
            DsOliLogger.debug(funSignature + padding + "no corresponding edge for: " + edge)
          }
        } else {
          DsOliLogger.debug(funSignature + padding + "edge does not have v1 as source: " + edge)
        }
    }
  }

  /**
   * Setup for the stripped down maximum common subgraph calculation
   *
   * @param g1 the ASG
   * @param g2 the FSG
   * @param vep the entry pointer vertex of the points-to graph
   * @return tuple with the subgraph, vertex mapping between g1 and g2 and found new edges on existing vertices
   */
  def mcs(g1: DsOliMetaBoxGraph, g2: DsOliMetaBoxGraph,
    vep: DsOliVertexMemory): (DsOliMetaBoxGraph, HashMap[DsOliMetaBoxGraphVertex, DsOliMetaBoxGraphVertex]) = {
    val funSignature = classSignature + "mcs: "
    DsOliLogger.debug(funSignature + "entered: ")
    val subgraph = new DsOliMetaBoxGraph
    val vertexMapping = new HashMap[DsOliMetaBoxGraphVertex, DsOliMetaBoxGraphVertex]()
    mcs_rec(g1, g2, g1.getEPVertex(vep).get, g2.getEPVertex(vep).get, subgraph, vertexMapping, "")
    (subgraph, vertexMapping)
  }

  /**
   * Recursively calculate the difference between the two graphs,
   * where one is represented as an actual graph and the second
   * is represented as a list of vertices (vertexMapping).
   *
   * @param graph the graph to diff
   * @param vertex the current vertex to look at
   * @param breadCrumbVertices prevent loops in the recursive processing
   * @param diffEdges store the diffed edges that where found
   * @param vertexMapping the other graph represented as vertices
   */
  def graphDiffRec(graph: DsOliMetaBoxGraph, vertex: DsOliMetaBoxGraphVertex, breadCrumbVertices: ListBuffer[DsOliMetaBoxGraphVertex],
    diffEdges: ListBuffer[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]], vertexMapping: Set[DsOliMetaBoxGraphVertex]): Unit = {

    graph.graph.edges.iterator.foreach {
      edgeInner =>
        // Only process outgoing edges 
        if (edgeInner.source == vertex) {

          // Only add the edge, if it was not found in the mapping
          if (!vertexMapping.contains(edgeInner.target) || !vertexMapping.contains(edgeInner.source)) diffEdges.append(edgeInner.toOuter)

          // Only unprocessed targets need recursion
          if (!breadCrumbVertices.contains(edgeInner.target.value)) {

            // Keep track of visited nodes
            breadCrumbVertices.append(edgeInner.target.value)

            // Recurse with current target
            graphDiffRec(graph, edgeInner.target.value, breadCrumbVertices, diffEdges, vertexMapping)
          }
        }

    }
  }

  /**
   * Calculate difference of to graphs by using the
   * given graph directly and the vertex mapping.
   *
   * @param graph the graph to diff
   * @param vep the entry pointer vertex of the points-to graph
   * @param vertexMapping the vertices of the other graph to diff against
   * @param newEdgesExistingVert set of new edges that were added to existing vertices (would otherwise be missed)
   * @return a graph representing the difference
   */
  def graphDiff(graph: DsOliMetaBoxGraph, vep: DsOliVertexMemory, vertexMapping: Set[DsOliMetaBoxGraphVertex]): DsOliMetaBoxGraph = {
    val funSignature = classSignature + "grphDff: "
    DsOliLogger.debug(funSignature + "entered: ")

    // Fetch the entry pointer vertex
    val vepVertex = graph.getEPVertex(vep).get
    // The graph containing the difference of both graphs
    val diffGraph = new DsOliMetaBoxGraph
    // The diffed edges
    val diffEdges = new ListBuffer[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]

    // Recursively calculate the differences
    graphDiffRec(graph, vepVertex, new ListBuffer[DsOliMetaBoxGraphVertex](), diffEdges, vertexMapping)

    // Process the differences that where found
    diffEdges.foreach {
      edge =>
        val source = edge.source
        val target = edge.target
        diffGraph.graph.add(source)
        diffGraph.graph.add(target)
        diffGraph.graph.add(edge)
    }
    diffGraph
  }

  /**
   * Merge two strand set vertices
   *
   * @param vertex the first vertex (result gets assigned to this one)
   * @param edgeSourceOrTarget the second vertex
   */
  def mergeBoxes(vertex: DsOliMetaBoxGraphVertex, edgeSourceOrTarget: DsOliMetaBoxGraphVertex): Unit = {
    if (vertex.isInstanceOf[DsOliMetaBoxGraphVertexBoxes] && edgeSourceOrTarget.isInstanceOf[DsOliMetaBoxGraphVertexBoxes]) {
      val vertexBoxes = vertex.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]
      vertexBoxes.boxes = vertexBoxes.boxes.union(edgeSourceOrTarget.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes)
      DsOliLogger.debug("vertex: " + vertexBoxes.linkOffset + " type: " + vertexBoxes.boxType)
      vertexBoxes.boxes.foreach {
        box =>
          DsOliLogger.debug("\tbox: " + box.id)
      }
    }
  }

  /**
   * Calculate the temporal repetition resulting in
   * the aggregated strand graph (ASG) from the point of
   * view of the given entry pointer.
   *
   * @param vepStart the start time step of the entry pointer
   * @param vep the entry pointer vertex
   * @return tuple with the entry pointer, the created ASG the number of life time steps and the number of aggregations
   *
   */
  def calculateAggregateEPGraph(vepStart: Int, vep: DsOliVertexMemory): (DsOliVertexMemory, DsOliMetaBoxGraph, Int, Int) = {
    val funSignature = classSignature + "clcltggrgtPGrph: "
    DsOliLogger.debug(funSignature + "entered: vepStart: " + vepStart + ", vep: " + vep)

    // The ASG that will be created
    val agg = new DsOliMetaBoxGraph

    // Add the entry pointer vertex first
    agg.addEPVertex(vep)

    // The entry pointer is the root of the graphs
    val vaggRoot = vep
    var t = vepStart
    // mergedmbgs does not start with empty element at position zero -> -1
    var mmbgT = this.dsOliMergedMbgs.get(t - 1).get

    // Flag to indicate termination
    var continue = true
    var numberTimeStepsAlive = 0
    // Count how many aggregation the entry pointer has seen
    var aggCount = 0

    // Continue until termination condition is met or 
    // entry pointer vertex is gone
    while (continue && this.ptgs.get(t).graph.nodes.contains(vep)) {

      DsOliLogger.debug(funSignature + "t: " + t)
      val tmpTimeStep3 = new DsOliTimeStepContainer[(DsOliVertexMemory, DsOliMetaBoxGraph)]
      tmpTimeStep3.append((vep, agg))

      numberTimeStepsAlive += 1

      // The frontier for the FSG and the ASG
      val VmbgFrontier = new Queue[DsOliMetaBoxGraphVertex]
      val VaggFrontier = new Queue[DsOliMetaBoxGraphVertex]
      // Keep a bread crumb of processed vertices
      val VmbgProcessed = new HashMap[DsOliMetaBoxGraphVertex, Boolean]

      // Important: vep might still be present in the graph, but it is not
      // guaranteed that it currently acts as an ep. So maybe one needs to 
      // skip a mmbg.
      DsOliLogger.debug(funSignature + "this.events.events(t - 1).isInstanceOf[DsOliMemoryEvent] " +
        (this.events.events(t - 1).isInstanceOf[DsOliMemoryEvent]))
      DsOliLogger.debug(funSignature + "!this.events.events(t - 1).isInstanceOf[DsOliVLSEvent] " +
        (!this.events.events(t - 1).isInstanceOf[DsOliVLSEvent]))
      DsOliLogger.debug(funSignature + "mmbgT.getEPVertex(vep).isDefined " +
        mmbgT.getEPVertex(vep).isDefined)

      if (this.events.events(t - 1).isInstanceOf[DsOliMemoryEvent] &&
        !this.events.events(t - 1).isInstanceOf[DsOliVLSEvent] && mmbgT.getEPVertex(vep).isDefined) {
        DsOliLogger.debug(funSignature + "calculating time step: " + t + " vep.id: " + vep.id)

        DsOliLogger.debug("vep: " + vep.id + " time: " + t)
        aggCount += 1

        DsOliLogger.debug(funSignature + "calling mcs")
        val (subgraph, vertexMapping) = mcs(agg, mmbgT, vep)
        DsOliLogger.debug(funSignature + "calculate graph difference")
        val mmbgTDiff = graphDiff(mmbgT, vep, vertexMapping.foldLeft(Set[DsOliMetaBoxGraphVertex]())((history, item) => history += item._1))

        vertexMapping.foreach {
          mapping =>
            DsOliLogger.debug(funSignature + "mapping: " + mapping._1 + " -> " + mapping._2)
        }
        DsOliLogger.debug(funSignature + "calculate agg graph")

        // Calculate the agg graph by iterating the diff graph and 
        // finding the connection points with the help of the vertex mapping
        mmbgTDiff.graph.edges.iterator.foreach {
          edgeInner =>
            val edge = edgeInner.toOuter


            val edgeSourceAggOpt = vertexMapping.get(edge.source)
            val edgeTargetAggOpt = vertexMapping.get(edge.target)

            DsOliLogger.debug(funSignature + "agg edge: " + edge)
            DsOliLogger.debug("vep: " + vep.id + " time: " + t)

            // First do the outgoing direction: if source not in mapping -> create new
            val edgeSourceAgg = if (!edgeSourceAggOpt.isDefined) {
              DsOliLogger.debug(funSignature + "source does not exist in mapping: " + edge.source)
              val tmpVaggNew = if (edge.source.isInstanceOf[DsOliMetaBoxGraphVertexEP]) {
                new DsOliMetaBoxGraphVertexEP(edge.source.asInstanceOf[DsOliMetaBoxGraphVertexEP].ep)
              } else {
                new DsOliMetaBoxGraphVertexBoxes(edge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes)
              }
              vertexMapping.put(edge.source, tmpVaggNew)
              agg.graph.add(tmpVaggNew)

              DsOliLogger.debug(funSignature + "new source created: " + tmpVaggNew)
              DsOliLogger.debug(funSignature + "new source mapping: " + edge.source + " -> " + tmpVaggNew)

              tmpVaggNew
            } else {
              val tmpSource = edgeSourceAggOpt.get
              mergeBoxes(tmpSource, edge.source)
              tmpSource
            }

            // Incoming direction: if target not in mapping -> create new
            val edgeTargetAgg = if (!edgeTargetAggOpt.isDefined) {
              DsOliLogger.debug(funSignature + "target does not exist in mapping: " + edge.target)

              val tmpVaggNew = new DsOliMetaBoxGraphVertexBoxes(edge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes)
              agg.graph.add(tmpVaggNew)
              vertexMapping.put(edge.target, tmpVaggNew)

              DsOliLogger.debug(funSignature + "new target created: " + tmpVaggNew)
              DsOliLogger.debug(funSignature + "new target mapping: " + edge.target + " -> " + tmpVaggNew)

              tmpVaggNew
            } else {
              val tmpTarget = edgeTargetAggOpt.get
              mergeBoxes(tmpTarget, edge.target)
              tmpTarget
            }

            DsOliLogger.debug(funSignature + "fetching conConfClass: " + edge.conConfClass)
            val conConfClass = if (edge.conConfClass == null) {
              Set[DsOliConConfClassification]()
            } else {
              edge.conConfClass
            }
            val newEdge = new DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex](edgeSourceAgg, edgeTargetAgg, edge.conConf, conConfClass, t)
            newEdge.ccSetPerTimeStep = new HashMap[Long, Set[(DsOliCell, DsOliCell, DsOliBox, DsOliBox)]]()
            newEdge.ccSetPerTimeStep.put(t, edge.ccSet)
            edge.strandConns.foreach {
              conn =>
                newEdge.strandConns.add(conn)
            }

            DsOliLogger.debug(funSignature + "new edge created: " + newEdge)
            DsOliLogger.debug(funSignature + "new edge conConfClass: " + newEdge.conConfClass)

            agg.graph.add(newEdge)
        }

      }

      val tmpTimeStep = new DsOliTimeStepContainer[(DsOliVertexMemory, DsOliMetaBoxGraph)]
      tmpTimeStep.append((vep, agg))

      DsOliLogger.debug(funSignature + "Trying to fetch ep: " + vep + " = " + (agg.getEPVertex(vep).isDefined))
      val tmpTimeStep2 = new DsOliTimeStepContainer[(DsOliVertexMemory, DsOliMetaBoxGraph)]
      tmpTimeStep2.append((vep, agg))

      t += 1
      if (t <= this.dsOliMergedMbgs.steps.size) {
        mmbgT = this.dsOliMergedMbgs.get(t - 1).get
      } else {
        DsOliLogger.debug(funSignature + "set continue to false: t(" + t +
          ") > steps.size(" + this.dsOliMergedMbgs.steps.size + ")")
        continue = false
      }
    }

    val tmpVep = agg.getEPVertex(vep)
    tmpVep.get.asInstanceOf[DsOliMetaBoxGraphVertexEP].end = vepStart + numberTimeStepsAlive
    tmpVep.get.asInstanceOf[DsOliMetaBoxGraphVertexEP].start = vepStart
    tmpVep.get.asInstanceOf[DsOliMetaBoxGraphVertexEP].aggCount = aggCount

    // Need to revert the last addition for the time steps alive
    (vep, agg, numberTimeStepsAlive - 1, aggCount)
  }

  /**
   * Check the actual shape predicate of the skip list
   *
   * @param boxesHorizontal the horizontal strand set
   * @param boxesVertical the vertical strand set
   * @param boxStep the current strand set
   * @param ptg the current points-to graph
   */
  def checkSkipListProperties(boxesHorizontal: List[DsOliBox], boxesVertical: List[DsOliBox], boxStep: DsOliBoxStep, ptg: DsOliGraph): Boolean = {
    //    val funSignature = classSignature + "checkSkipListProperties: "
    val funSignature = classSignature + "chckSkpLstPrprts: "
    DsOliLogger.debug(funSignature + "entered: ")

    // See: http://www.swt-bamberg.de/research/pdf-ds/papers/issta16app.pdf for details (Skip List - Overlay)

    // Test C 
    // Intersection test: strands only intersect in one point or not at all
    val intersectionTest = boxesHorizontal.forall {
      boxH =>
        DsOliLogger.debug(funSignature + "boxH.id: " + boxH.id)

        boxesVertical.forall { boxV =>
          DsOliLogger.debug(funSignature + "boxV.id: " + boxV.id)

          val (cellPairsCnt, _) = cellPairs(boxH, boxV, boxStep, ptg)
          DsOliLogger.debug(funSignature + "boxV.id(" + boxV.id + ") <-> boxH cell pairs count: " + cellPairsCnt)

          // Need to make sure, that there is exactly one connection 
          // between the boxes, if there exists one. (no connection might also be possible)
          cellPairsCnt == 0 || cellPairsCnt == 1
        }
    }

    // Test A
    // Vertical to horizontal connection
    var vLevel = 0
    val boxesHorizontalRev = boxesHorizontal.reverse
    val boxesHorizontalHeight = boxesHorizontalRev.length
    val vToHTest = boxesVertical.forall {
      boxV =>
        vLevel = 0
        val boxVLength = boxV.cells.length
        boxV.cells.forall {
          cellV =>
            // This might fail, if we have skip lists with multiple horizontal
            // strands of the same length
            val index = vLevel + boxesHorizontalHeight - boxVLength
            try {
              val horizontalContainsCell = boxesHorizontalRev(index).cells.contains(cellV)
              vLevel += 1
              horizontalContainsCell
            } catch {
              case e: Exception => false
            }
        }
    }

    // Test B
    // Rectangle property
    val numHorizontalLists = boxesHorizontal.length - 1
    var rectangleProperty = true
    for (j <- 0 until numHorizontalLists) {
      DsOliLogger.debug(funSignature + "rectangle property check j: " + j)

      val sH = boxesHorizontalRev(j)
      val sHPrime = boxesHorizontalRev(j + 1)
      val hListLen = sH.cells.length - 1

      DsOliLogger.debug(funSignature + "testing sH: " + sH)
      DsOliLogger.debug(funSignature + "sHPrime: " + sHPrime)
      for (i <- 0 until hListLen) {
        DsOliLogger.debug(funSignature + "rectangle property check i: " + i)

        val c1 = sH.cells(i)
        val c2 = sH.cells(i + 1)
        // Debug
        DsOliLogger.debug(funSignature + "c1: " + c1)
        DsOliLogger.debug(funSignature + "c2: " + c2)
        val c1toVFilt = boxesVertical.filter { box => box.cells.contains(c1) }
        val c2toVFilt = boxesVertical.filter { box => box.cells.contains(c2) }

        DsOliLogger.debug(funSignature + "c1toVFilt: " + c1toVFilt.length)
        DsOliLogger.debug(funSignature + "c2toVFilt: " + c2toVFilt.length)

        if (c1toVFilt.length == 1 && c2toVFilt.length == 1) {

          // Debug
          DsOliLogger.debug(funSignature + "found c1 in : " + c1toVFilt.head)
          DsOliLogger.debug(funSignature + "found c2 in : " + c2toVFilt.head)

          val c1toVIndex = boxesVertical.indexOf(c1toVFilt.head)
          val c2toVIndex = boxesVertical.indexOf(c2toVFilt.head)

          DsOliLogger.debug(funSignature + "c1toVIndex: " + c1toVIndex)
          DsOliLogger.debug(funSignature + "c2toVIndex: " + c2toVIndex)

          try {
            val c1Prime = boxesVertical(c1toVIndex).cells(boxesVertical(c1toVIndex).cells.indexOf(c1) + 1)
            val c2Prime = boxesVertical(c2toVIndex).cells(boxesVertical(c2toVIndex).cells.indexOf(c2) + 1)

            // Debug
            DsOliLogger.debug(funSignature + "c1Prime: " + c1Prime)
            DsOliLogger.debug(funSignature + "c2Prime: " + c2Prime)

            val a = sHPrime.cells.indexOf(c1Prime)
            val b = sHPrime.cells.indexOf(c2Prime)

            DsOliLogger.debug(funSignature + "a: " + a)
            DsOliLogger.debug(funSignature + "b: " + b)
            if (a != -1 && b != -1 && a < b) {
              DsOliLogger.debug(funSignature + "found rectangle: ")
            } else {
              rectangleProperty = false
              DsOliLogger.debug(funSignature + "rectangle property violated (1): ")
            }
          } catch {
            case e: Exception =>
              rectangleProperty = false
              DsOliLogger.debug(funSignature + "rectangle property violated (3): ")
          }
        } else {
          DsOliLogger.debug(funSignature + "rectangle property violated (2): ")
          rectangleProperty = false
        }
      }
    }

    DsOliLogger.debug(funSignature + "intersectionTest: " + intersectionTest + ", vToHTest: " + vToHTest + ", rectangleProperty: " + rectangleProperty)
    return intersectionTest && vToHTest && rectangleProperty

  }

  /**
   * Checks, that strands in the graph are non-cyclic
   *
   * @param graph the graph to check
   * @return Boolean
   */
  def nonCyclicBoxesStrandGraph(graph: DsOliMetaBoxGraph): Boolean = {
    val funSignature = classSignature + "nnCyclcBxs: "
    DsOliLogger.debug(funSignature + "entered: ")

    // Test that all strands have no cycle reference
    graph.graph.nodes.forall {
      vertex =>
        val vertexOuter = vertex.value

        // Entry pointer vertex must be obeyed as this can be part of
        // the sub graph
        val isEP = vertexOuter.isInstanceOf[DsOliMetaBoxGraphVertexEP]

        // All strands must have a cycle reference of zero
        val nonCyclic = if (vertexOuter.isInstanceOf[DsOliMetaBoxGraphVertexBoxes]) {
          val boxV = vertexOuter.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]
          boxV.boxes.forall {
            box =>
              // No cycle reference
              box.cycleId == 0
          }
        } else {
          true
        }

        isEP || nonCyclic
    }
  }

  /**
   * Fetch the edges and cells make the area predicate
   *
   * @param graph the graph to test
   * @param edgesTree stores the edges forming the data structure
   * @param treeCells stores the cells forming the data structure
   * @param ptg the current points-to graph
   */
  def collectEdgesStrandGraph(graph: DsOliMetaBoxGraph, edgesTree: ListBuffer[DsOliDiEdge[DsOliVertex]], treeCells: HashMap[Long, DsOliCell], ptg: DsOliGraph): Unit = {
    //    val funSignature = classSignature + "collectEdgesStrandGraph: "
    val funSignature = classSignature + "cllctdgsStrndGrph: "

    DsOliLogger.debug(funSignature + "entered")

    // Check all vertices of the graph and thus all
    // strands and fetch the edges from the points-to
    // graph which are actually inside of a strand 
    // forming the area predicate.
    graph.graph.nodes.foreach {
      vertex =>

        val boxV = vertex.value.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]
        ptg.graph.edges.iterator.foreach {
          edgeInner =>
            DsOliLogger.debug(funSignature + "dumping edge: " + edgeInner)
        }
        boxV.boxes.foreach {
          box =>
            DsOliLogger.debug(funSignature + "dumping box: " + box)
        }

        boxV.boxes.foreach {
          box =>

            // Cyclic test can be omitted as this is already done
            // in nonCyclicBoxes test
            ptg.graph.edges.iterator.foreach {
              edgeInner =>

                // Test if there exists a cell inside of the current strand which
                // matches both the source and target address of the current edge
                val edgeSourceAddr = edgeInner.sAddr + edgeInner.sOffset
                val edgeTargetAddr = edgeInner.tAddr + edgeInner.tOffset
                val sAddrMatch = box.cells.exists(cell => DsOliAddressUtils.addressInRange(cell.bAddr, cell.eAddr, edgeSourceAddr, edgeSourceAddr))
                val tAddrMatch = box.cells.exists(cell => DsOliAddressUtils.addressInRange(cell.bAddr, cell.eAddr, edgeTargetAddr, edgeTargetAddr))
                // Source and target address are found, save the edge
                if (sAddrMatch && tAddrMatch) {
                  DsOliLogger.debug(funSignature + "match on edge: " + edgeInner.toOuter)
                  edgesTree.append(edgeInner.toOuter)
                } else {
                  DsOliLogger.debug(funSignature + "no match on edge: " + edgeInner.toOuter)
                  DsOliLogger.debug(funSignature + "\tedgeSourceAddr: " + edgeSourceAddr + " edgeTargetAddr: " + edgeTargetAddr)
                }

            }

            // Always save all cells of the strand participating in the area predicate
            box.cells.foreach(cell => if (!treeCells.contains(cell.id)) treeCells.put(cell.id, cell))
        }
    }
  }

  /**
   * Sort the participating strands of the skip list into
   * two different categories denoted by the linkage
   * offset. Each set corresponds to either the
   * horizontal or vertical strands. This decision is
   * made later on, though.
   *
   * @param graph the sub graph representing the skip list
   * @return tuple of strands, one for each linkage offset
   */
  def getSkipListBoxes(graph: DsOliMetaBoxGraph): (ListBuffer[DsOliBox], ListBuffer[DsOliBox]) = {
    val funSignature = classSignature + "gtSkpLstBxs: "
    DsOliLogger.debug(funSignature + "entered: ")

    // Storage for the strands of different linkage offsets
    val boxesA = new ListBuffer[DsOliBox]
    val boxesB = new ListBuffer[DsOliBox]

    // There should only be two different offsets, which are represented by any of the edges
    val offsetA = graph.graph.edges.head.toOuter.conConf.offsets._1
    val offsetB = graph.graph.edges.head.toOuter.conConf.offsets._2

    // Sort the boxes into category A and B
    graph.graph.nodes.foreach {
      nodeInner =>
        val node = nodeInner.value
        // Offset A
        if (graph.graph.edges.exists { edgeInner =>
          val edge = edgeInner.toOuter
          edge.source == node && edge.conConf.offsets._1 == offsetA
        }) {
          boxesA.append(node.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head)
        } // Offset B
        else if (graph.graph.edges.exists { edgeInner =>
          val edge = edgeInner.toOuter
          edge.source == node && edge.conConf.offsets._1 == offsetB
        }) {
          boxesB.append(node.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head)
        }
    }
    (boxesA, boxesB)
  }

  /**
   * The skip list predicate. If the predicate matches
   * the evidence is distributed over the graph.
   *
   * @param ptg the current points-to graph
   * @param graph the area predicate graph
   * @return Boolean
   */
  def skipListTestStrandGraph(ptg: DsOliGraph, graph: DsOliMetaBoxGraph, boxStep: DsOliBoxStep): Boolean = {
    val funSignature = classSignature + "skpLstTstStrndGrph:"

    val (boxesA, boxesB) = getSkipListBoxes(graph)

    // Sort source and target boxes by length in descending order
    val boxesSource = boxesA.toList.sortBy(box => box.cells.length).reverse
    val boxesTarget = boxesB.toList.sortBy(box => box.cells.length).reverse

    // No need to check further
    if (boxesSource.size == 0 || boxesTarget.size == 0) {
      return false
    }

    // Get the length of the first boxes
    val boxSourceLen = boxesSource.head.cells.length
    val boxTargetLen = boxesTarget.head.cells.length
    DsOliLogger.debug(funSignature + "boxSourceLen: " + boxSourceLen + " boxTargetLen: " + boxTargetLen)

    // Try to guess the possible orientation of the skip list: 
    // Longest strand: horizontal direction
    val isSkipList = if (boxSourceLen <= 1 || boxTargetLen <= 1) {
      DsOliLogger.debug(funSignature + "boxSourceLen || boxTargetLen <= 1 ")
      false
    } else if (boxSourceLen > boxTargetLen) {
      DsOliLogger.debug(funSignature + "boxSourceLen > boxTargetLen")
      checkSkipListProperties(boxesSource, boxesTarget, boxStep, ptg)
    } else if (boxSourceLen < boxTargetLen) {
      DsOliLogger.debug(funSignature + "boxSourceLen < boxTargetLen")
      checkSkipListProperties(boxesTarget, boxesSource, boxStep, ptg)
    } else {
      DsOliLogger.debug(funSignature + "else path")
      false
    }

    // The skip list predicate is matched, now
    // distribute the evidence accross the graph
    if (isSkipList) {
      DsOliLogger.debug(funSignature + "skip list predicate is fullfilled")
      // Add the skip list evidence to all edges
      graph.graph.edges.foreach {
        edge =>
          edge.conConfClass.add(new DsOliConConfClassification(SLo2, 3))
      }
      true
    } else {
      DsOliLogger.debug(funSignature + "skip list predicate is not fullfilled")
      false
    }

  }

  /**
   * The binary tree predicate. If the predicate matches
   * the evidence is distributed over the graph.
   *
   * @param ptg the current points-to graph
   * @param graph the area predicate graph
   * @return Boolean
   */
  def binaryTreeTestStrandGraph(ptg: DsOliGraph, graph: DsOliMetaBoxGraph): Boolean = {
    val funSignature = classSignature + "bnryTrTstStrndGrph:"

    // Precondition: all boxes must be non cyclic
    val isNonCyclic = nonCyclicBoxesStrandGraph(graph)

    // If precondition is not met, copy over bi dir edges untouched
    if (isNonCyclic) {
      DsOliLogger.debug(funSignature + "no cyclic boxes. continue")

      // Calculate edges inside of both vertices
      var edgesTree = new ListBuffer[DsOliDiEdge[DsOliVertex]]
      var treeCells = new HashMap[Long, DsOliCell]()
      collectEdgesStrandGraph(graph, edgesTree, treeCells, ptg)

      DsOliLogger.debug(funSignature + "edgesTree.size: " + edgesTree.size)
      DsOliLogger.debug(funSignature + "treeCells.size: " + treeCells.size)

      // Find the root cell by iterating all cells of the
      // area predicate and checking:
      // - no incoming pointers exist into the cell 
      // - the number of outgoing pointers is either 0, 1 or 2
      var roots = new ListBuffer[DsOliCell]
      treeCells.foreach {
        cellKV =>

          // The cell id and the actual cell
          val (cellId, cell) = cellKV

          // Check, that no pointers are incoming to the cell
          val noIncomingPtrs = !edgesTree.exists {
            edge =>
              DsOliAddressUtils.addressInRange(cell.bAddr, cell.eAddr,
                edge.tAddr + edge.tOffset, edge.tAddr + edge.tOffset)
          }

          val outgoingPtrsCnt = edgesTree.count {
            edge =>
              DsOliAddressUtils.addressInRange(cell.bAddr, cell.eAddr, edge.sAddr + edge.sOffset, edge.sAddr + edge.sOffset)
          }
          // Number of outgoing pointers: 0,1,2
          val outgoingPtrs = 0 <= outgoingPtrsCnt && outgoingPtrsCnt <= 2
          if (noIncomingPtrs && outgoingPtrs) {
            DsOliLogger.debug(funSignature + "found root: outgoingPtrsCnt = " + outgoingPtrsCnt + "; noIncomingPtrs = " + noIncomingPtrs + "; cell = " + cell)
            // Calculate edges inside of both vertices
            roots.append(cell)
          } else {
            DsOliLogger.debug(funSignature + "no root: outgoingPtrsCnt = " + outgoingPtrsCnt + "; noIncomingPtrs = " + noIncomingPtrs)
          }
      }

      // There can be only one root candidate. If there are 
      // more the predicate failed
      if (roots.size == 1) {

        // Fetch the root cell candidate and remove it from the
        // remaining cells
        val root = roots.head
        treeCells.-=(root.id)
        DsOliLogger.debug(funSignature + "Found root element: " + root)

        // All other cells need exactly one incoming and outoing pointer
        // to fulfill the binary tree label
        val nodeProperty = treeCells.forall {
          cellKV =>
            val (cellId, cell) = cellKV

            // Count the number of incoming edges to the cell
            val incomingCnt = edgesTree.count {
              edge =>
                DsOliAddressUtils.addressInRange(cell.bAddr, cell.eAddr,
                  edge.tAddr + edge.tOffset, edge.tAddr + edge.tOffset)
            }

            // Number of incoming pointers: exactly one; if this fails, we do not need to check any further
            val incomingPtrs = incomingCnt == 1
            val outgoingCnt = edgesTree.count {
              edge =>
                val sourceIsCell = DsOliAddressUtils.addressInRange(cell.bAddr, cell.eAddr, edge.sAddr + edge.sOffset, edge.sAddr + edge.sOffset)
                // Copy over the cells without the cell under investigation
                var tmpTreeCells = treeCells.filter(filtCell => filtCell._2 != cell)
                val targetInTree = tmpTreeCells.exists {
                  cellKV =>
                    val (cellId, cell) = cellKV
                    DsOliAddressUtils.addressInRange(cell.bAddr, cell.eAddr, edge.tAddr + edge.tOffset, edge.tAddr + edge.tOffset)
                }
                sourceIsCell && targetInTree
            }
            val outgoingPtrs = 0 <= outgoingCnt && outgoingCnt <= 2
            DsOliLogger.debug(funSignature + "node property: incomingCnt = " + incomingCnt + "; outgoingCnt = " + outgoingCnt + " result (must be true): " + (incomingPtrs && outgoingPtrs))
            incomingPtrs && outgoingPtrs
        }

        // The binary tree predicate is matched, now
        // distribute the evidence across the graph
        if (nodeProperty) {
          DsOliLogger.debug(funSignature + "found a tree")
          // Evidence distribution
          graph.graph.edges.foreach {
            edge =>
              edge.toOuter.conConfClass.add(new DsOliConConfClassification(BT, 2))
          }
          true
        } else {
          DsOliLogger.debug(funSignature + "Node property not matched")
          false
        }
      } else {
        DsOliLogger.debug(funSignature + "Wrong number of root elements: " + roots.size + " :: " + roots)
        false
      }
    } else {
      DsOliLogger.debug(funSignature + "is non cyclic failed")
      false
    }
  }

  val dsHierarchy = Array(dsTree, dsSkipOvly, dsNesting, dsDLL)

  /**
   * Recursively find the tree and skip list area predicate
   *
   * @param node the vertex to start from
   * @param typeA the type of the target
   * @param typeB the type of the source
   * @param offsetA the first connection configuration offset
   * @param offsetB the second connection configuration offset
   * @param graph the graph to operate on
   * @param subGraph the graph to store the elements in (also used as a bread crumb for recursion)
   * @param indent debug string
   */
  def findTreeAreaPredicateRec(node: DsOliMetaBoxGraphVertexBoxes, typeA: DsOliType, typeB: DsOliType, offsetA: Long, offsetB: Long,
    graph: DsOliMetaBoxGraph, subGraph: DsOliMetaBoxGraph, indent: String): Unit = {
    val funSignature = classSignature + "fndTrrPrdctRc: "
    DsOliLogger.debug(funSignature + "entered: ")
    DsOliLogger.debug(indent + funSignature + "entered: node.id: " + node.boxes.head.id)
    // Process all bidirectional edges starting from node
    val biDirs = graph.graph.edges.filter(edgeBiDi => edgeBiDi.source == node && DsOliGraphUtils.isBidirectionalEdge(edgeBiDi.toOuter, graph))
    biDirs.foreach {
      edgeBiDiInner =>
        val edgeBiDi = edgeBiDiInner.toOuter

        // First check alternation
        if (edgeBiDi.conConf.offsets._1 == offsetA &&
          edgeBiDi.conConf.offsets._2 == offsetB &&
          edgeBiDi.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxType == typeB) {

          DsOliLogger.debug(indent + funSignature + "Found the correct bi di edge")
          // Only recurse, if the target was not already visited before
          if (!subGraph.graph.nodes.contains(edgeBiDi.target)) {
            DsOliLogger.debug(indent + funSignature + "Adding edge and target")

            subGraph.graph.add(edgeBiDi.target)
            subGraph.graph.add(edgeBiDi)
            val corEdge = DsOliGraphUtils.getCorrespondingBiDiEdge(edgeBiDi, graph)
            subGraph.graph.add(corEdge)

            // Debug
            DsOliLogger.debug("> adding corEdge: " + corEdge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id + "->"
              + corEdge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id)
            DsOliLogger.debug(indent + funSignature + "Target was not visited yet -> Recurse")

            findTreeAreaPredicateRec(edgeBiDi.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes], typeB, typeA, offsetB, offsetA, graph, subGraph, indent + ">")
          } else {

            DsOliLogger.debug(indent + funSignature + "Only adding edge and target, no recursion.")
            subGraph.graph.add(edgeBiDi.target)
            subGraph.graph.add(edgeBiDi)
            val corEdge = DsOliGraphUtils.getCorrespondingBiDiEdge(edgeBiDi, graph)

            // Debug
            DsOliLogger.debug("> adding corEdge: " + corEdge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id + "->"
              + corEdge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id)
            subGraph.graph.add(corEdge)
            DsOliLogger.debug(indent + funSignature + "Target already visited -> no recursion: " + edgeBiDi.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id)
          }
        } else {
          DsOliLogger.debug(indent + funSignature + "Check did not succeed")
        }
    }
  }

  /**
   * Find the longest path in the given graph
   * 
   * @param node the node to start
   * @param graph the graph to check
   * @param recLevel the recursion depth
   * @param breadCrumb avoid iterating paths multiple times
   * @return the recursion depth
   */
  def findLongestPath(node: DsOliMetaBoxGraphVertex, subGraph: DsOliMetaBoxGraph, recLevel: Long, breadCrumb: ListBuffer[DsOliMetaBoxGraphVertex]): Long = {
    var longest_01 = 0l
    val localBreadCrumb = new ListBuffer[DsOliMetaBoxGraphVertex]()
    localBreadCrumb.appendAll(breadCrumb)
    localBreadCrumb.append(node)
    subGraph.graph.edges.iterator.foreach {
      edgeInner =>
        if (edgeInner.toOuter.source == node && !breadCrumb.contains(edgeInner.toOuter.target)) {
          val retDepth = findLongestPath(edgeInner.toOuter.target, subGraph, recLevel + 1, localBreadCrumb)
          if (retDepth - recLevel > longest_01) {
            longest_01 = retDepth - recLevel
          }
        }
    }
    longest_01 + recLevel
  }

  /**
   * Check the path length for a given graph
   * 
   * @param subGraph the graph to check
   * @return boolean
   */
  def pathCheck(subGraph: DsOliMetaBoxGraph): Boolean = {
    subGraph.graph.nodes.exists {
      node =>
        val retLen = findLongestPath(node.value, subGraph, 0, new ListBuffer[DsOliMetaBoxGraphVertex])
        retLen >= 3
    }
  }

  /**
   * Search for the binary tree and skip list area predicate
   *
   * @param edge the handle to start from to find the area predicate
   * @param graph the graph to operate on
   * @return Option the area predicate graph
   */
  def treeAreaPredicate(edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex], graph: DsOliMetaBoxGraph): Option[DsOliMetaBoxGraph] = {
    val funSignature = classSignature + "trrPrdct: "
    DsOliLogger.debug(funSignature + "entered: ")
    // Only, if this is a bidirectional edge
    if (DsOliGraphUtils.isBidirectionalEdge(edge, graph)) {
      val subGraph = new DsOliMetaBoxGraph
      val strandSource = edge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]
      val strandTarget = edge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]

      // Debug
      DsOliLogger.debug("> before calling findTreeAreaPredicateRec: strandSource.box: " + strandSource.boxes.head.id)
      DsOliLogger.debug("> before calling findTreeAreaPredicateRec: strandTarget.box: " + strandTarget.boxes.head.id)

      subGraph.graph.add(edge.source)
      subGraph.graph.add(edge.target)
      subGraph.graph.add(edge)

      val corEdge = DsOliGraphUtils.getCorrespondingBiDiEdge(edge, graph)
      DsOliLogger.debug("> adding corEdge: " + corEdge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id + "->"
        + corEdge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id)
      subGraph.graph.add(corEdge)

      // Start from target and explore graph
      DsOliLogger.debug("> before calling findTreeAreaPredicateRec with strandTarget")
      findTreeAreaPredicateRec(strandTarget, strandTarget.boxType, strandSource.boxType, edge.conConf.offsets._2, edge.conConf.offsets._1, graph, subGraph, ">")

      // Continue from source and explore graph
      DsOliLogger.debug("> before calling findTreeAreaPredicateRec with strandSource")
      findTreeAreaPredicateRec(strandSource, strandSource.boxType, strandTarget.boxType, edge.conConf.offsets._1, edge.conConf.offsets._2, graph, subGraph, ">")

      if (pathCheck(subGraph)) {
        Some(subGraph)
      } else {
        None
      }

    } else {
      None
    }
  }

  /**
   * Checks for the presence of a binary tree and afterwards for
   * the presence of a skip list, as both operate on the same
   * area predicate.
   *
   * @param edge the edge to start with
   * @param graph the graph to operate on
   * @param ptg the current points-to graph
   * @param boxStep the current strand set including cycles
   * @return Option the subgraph of the matched data structure
   */
  def treeDs(edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex], graph: DsOliMetaBoxGraph,
    ptg: DsOliGraph, boxStep: DsOliBoxStep): Option[DsOliMetaBoxGraph] = {
    val funSignature = classSignature + "trDs: "
    DsOliLogger.debug(funSignature + "entered: ")

    // Try to find the area predicate which is identical for
    // trees and skip lists
    val treeSubGraph = treeAreaPredicate(edge, graph)

    // If area predicate is not matched, no need to check further
    if (treeSubGraph.isDefined) {

      DsOliLogger.debug(funSignature + "we got a treeSubGraph: " + treeSubGraph)

      // Test for the binary tree first: calculate the shape predicate on the
      // area predicate sub graph
      if (binaryTreeTestStrandGraph(ptg, treeSubGraph.get)) {
        DsOliLogger.debug(funSignature + "Shape predicate for tree fullfilled")

        // Return the sub graph containing the evidence
        treeSubGraph
      } else {
        DsOliLogger.debug(funSignature + "Shape predicate for tree failed")

        // Now test for a skip list to obey DSI's precedence for data structure matching
        // on the area predicate sub graph
        if (skipListTestStrandGraph(ptg, treeSubGraph.get, boxStep)) {
          DsOliLogger.debug(funSignature + "Shape predicate for skip list fullfilled")

          // Return the sub graph containing the evidence
          treeSubGraph
        } else {
          DsOliLogger.debug(funSignature + "Shape predicate for skip list failed")

          None
        }
      }
    } else {
      DsOliLogger.debug(funSignature + "we got no treeSubGraph: ")

      None
    }
  }

  /**
   * Determine if nesting on overlay occurs on the given nesting direction
   *
   * @param node the vertex to start from
   * @param ccSet the set of connections
   * @param conConf the connection configurations
   * @param boxType the type of the strand
   * @param boxStep the current strand set
   * @return Option the set of found edges representing the nesting
   */
  def nodeHasNesting(node: DsOliMetaBoxGraphVertex, graph: DsOliMetaBoxGraph, ccSet: Set[(DsOliCell, DsOliCell, DsOliBox, DsOliBox)],
    conConf: DsOliConConf, boxType: DsOliType, boxStep: DsOliBoxStep): Option[Set[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]] = {
    val funSignature = classSignature + "ndHsNstng: "

    DsOliLogger.debug(funSignature + "entered: ")
    DsOliLogger.debug(funSignature + "entered: ccSet.size: " + ccSet.size + "; ccSet: " + ccSet)

    // Immediately stop with multiple connections, i.e., the source and 
    // target strands only intersect in one place
    if (ccSet.size > 1) return None

    val nestingCount = graph.graph.edges.filter {
      edgeInner =>
        val edge = edgeInner.toOuter

        // Only operate on strand set vertices
        val isBoxVertex = edge.target.isInstanceOf[DsOliMetaBoxGraphVertexBoxes]

        // Nesting is tested from the point of view of the given node, so 
        // edges need to originate from the node and the connection configuration
        // needs to match
        if (edge.source == node && edge.conConf == conConf && isBoxVertex) {

          // Get the target vertex of the edge
          val targetBox = edge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes]

          // Test the connection between the source and the target 
          // by inspecting the cells
          val targetCellsAreFirst = edge.ccSet.exists {
            cc =>

              val (cellA, cellB, boxA, boxB) = cc

              DsOliLogger.debug(funSignature + "testing conConf: " + conConf + " cc: " + cc + " against " + targetBox.boxes.head)
              DsOliLogger.debug(funSignature + "testing boxB == targetBox.boxes.head: " + (boxB == targetBox.boxes.head))

              // The strand set of the target vertex should only contain
              // one strand and should match with the inspected connection
              if (boxB == targetBox.boxes.head) {

                // Cyclic or non cyclie?
                if (boxB.cycleId == 0) {

                  // Non cyclic

                  // Debug
                  DsOliLogger.debug(funSignature + "testing boxB.cells.head: " + boxB.cells.head)
                  DsOliLogger.debug(funSignature + "testing cellB: " + cellB)
                  DsOliLogger.debug(funSignature + "testing boxB.cells.head == cellB: " + (boxB.cells.head == cellB))

                  // The target cell needs to be part of the cells
                  val linear = boxB.cells.exists(cell => cell == cellB)
                  DsOliLogger.debug(funSignature + "linear: " + linear)

                  linear
                } else {

                  // Cylic

                  DsOliLogger.debug(funSignature + "cyclicity is beeing checked")

                  // Match either in linear or cyclic part
                  val linear = boxB.cells.exists(cell => cell == cellB)
                  val cyclic = boxStep.cycles.get(boxB.cycleId).get.cells.exists(cell => cell == cellB)

                  DsOliLogger.debug(funSignature + "linear: " + linear + ", cyclic: " + cyclic)

                  linear || cyclic
                }
              } else {
                DsOliLogger.debug(funSignature + "not the correct box")
                false
              }
          }

          // Types need to match and the cells need to be found
          targetBox.boxType == boxType && targetCellsAreFirst

        } else {
          false
        }
    }

    // The nesting predicate is fulfilled, 
    // if more than one element is present
    if (nestingCount.size > 1) {
      DsOliLogger.debug(funSignature + "nestingCount.size > 1: " + nestingCount)
      // Transform from inner to outer
      Some(nestingCount.foldLeft(Set[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]()) {
        (h, c) => h.add(c.toOuter); h
      })
    } else {
      DsOliLogger.debug(funSignature + "nestingCount.size <= 1")
      None
    }
  }

  /**
   * The area predicate for overlay nesting
   *
   * @param edge the edge functioning as a handle for the data structure predicate
   * @param graph the graph to operate on
   * @param boxStep the current strand set including cycles
   * @return Option the graph with the distributed evidences
   */
  def nestingAreaPredicate(edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex],
    graph: DsOliMetaBoxGraph, boxStep: DsOliBoxStep): Option[DsOliMetaBoxGraph] = {
    val funSignature = classSignature + "nstngrPrdct: "
    DsOliLogger.debug(funSignature + "entered: ")

    // Overlay nesting can only happen, if this is a bidirectional edge
    if (DsOliGraphUtils.isBidirectionalEdge(edge, graph) &&
      edge.source.isInstanceOf[DsOliMetaBoxGraphVertexBoxes] &&
      edge.target.isInstanceOf[DsOliMetaBoxGraphVertexBoxes]) {

      // Overlay nesting can happen in both directions
      // Test one direction first
      DsOliLogger.debug(funSignature + "testing first orientation: ")
      val nestingOpt = nodeHasNesting(edge.source, graph, edge.ccSet, edge.conConf, edge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxType, boxStep)
      val (nesting, conConf) = if (nestingOpt.isDefined) {
        // Match on first direction
        DsOliLogger.debug(funSignature + "found nesting on first orientation: " + nestingOpt.get)
        (nestingOpt, edge.conConf)
      } else {
        // No nesting on first direction: test reverse orientation
        DsOliLogger.debug(funSignature + "testing reverse orientation: ")
        val edgeBiDi = DsOliGraphUtils.getCorrespondingBiDiEdge(edge, graph)
        val nestingOptBiDi = nodeHasNesting(edge.target, graph, edgeBiDi.ccSet, edgeBiDi.conConf, edge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxType, boxStep)
        if (nestingOptBiDi.isDefined) {
          DsOliLogger.debug(funSignature + "found nesting on second orientation: " + nestingOptBiDi.get)
          (nestingOptBiDi, edgeBiDi.conConf)
        } else {
          DsOliLogger.debug(funSignature + "found no nesting: ")
          (None, None)
        }
      }

      // The overlay nesting predicate is matched, now
      // distribute the evidence accross the graph
      if (nesting.isDefined) {
        val subGraph = new DsOliMetaBoxGraph
        nesting.get.foreach {
          nestingEdge =>
            DsOliLogger.debug(funSignature + "adding nesting classification on edge: " + nestingEdge)

            val nestingEdgeOuter = nestingEdge
            nestingEdgeOuter.conConfClass = Set(new DsOliConConfClassificationNesting(No, 1, //nesting.get.size,
              nestingEdge.conConf.offsets._1, nestingEdge.conConf.offsets._2))
            subGraph.graph.add(nestingEdgeOuter)
            // Add the corresponding bi directional edge!
            subGraph.graph.add(DsOliGraphUtils.getCorrespondingBiDiEdge(nestingEdgeOuter, graph))
            if (nestingEdge.source.isInstanceOf[DsOliMetaBoxGraphVertexBoxes] && nestingEdge.target.isInstanceOf[DsOliMetaBoxGraphVertexBoxes]) {
              DsOliLogger.debug(funSignature + "adding to subgrah: nestingEdge: " +
                nestingEdge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id + "->"
                + nestingEdge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id)
            } else {
              DsOliLogger.debug(funSignature + "adding to subgrah: nestingEdge: " + nestingEdge)
            }
        }
        Some(subGraph)
      } else {
        None
      }

    } else {
      DsOliLogger.debug(funSignature + "not an overlay connection. Skipping nesting test.")
      None
    }
  }

  /**
   * Test the overlay nesting predicate
   *
   * @param edge the edge functioning as a handle for the data structure predicate
   * @param graph the graph to operate on
   * @param ptg the current points-to graph
   * @param boxStep the current strand set including cycles
   * @return Option the sub graph representing the nesting with distributed evidences
   */
  def nestingDs(edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex], graph: DsOliMetaBoxGraph,
    ptg: DsOliGraph, boxStep: DsOliBoxStep): Option[DsOliMetaBoxGraph] = {
    val funSignature = classSignature + "nstngDs: "
    DsOliLogger.debug(funSignature + "entered: ")
    val nestingSubGraph = nestingAreaPredicate(edge, graph, boxStep)
    // No shape predicate required, area predicate is sufficent
    nestingSubGraph
  }

  /**
   * Actually apply DSIs predicates. Some aspects of the hierarchy
   * are encoded inside of the test methods.
   *
   * @param ds the data structure to test for
   * @param edge the edge functioning as a handle for the data structure predicate
   * @param graph the graph to operate on
   * @param ptg the current points-to graph
   * @param boxStep the current strand set including cycles
   * @param eventStep the current time step
   * @return Option a new graph instance containing the vertices and edges of the found data structure
   */
  def checkAreaPredicate(ds: DsOliDataStructures, edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex], graph: DsOliMetaBoxGraph, ptg: DsOliGraph, boxStep: DsOliBoxStep): Option[DsOliMetaBoxGraph] = {
    val funSignature = classSignature + "chckrPrdct: "
    DsOliLogger.debug(funSignature + "entered: ")
    if (ds == dsTree) {
      // Test the binary tree AND the skip list predicate
      treeDs(edge, graph, ptg, boxStep)
    } else if (ds == dsNesting) {
      // Test the overaly nesting (indirect nesting is tested in the DLL predicate
      // according to DSI's data structure hierarchy
      nestingDs(edge, graph, ptg, boxStep)
    } else if (ds == dsDLL) {
      if (edge.conConf.tag == ccOverlay && edge.source.isInstanceOf[DsOliMetaBoxGraphVertexBoxes] && edge.target.isInstanceOf[DsOliMetaBoxGraphVertexBoxes]
        && DsOliGraphUtils.isBidirectionalEdge(edge, graph)) {
        DsOliLogger.debug(funSignature + "bidi edge found: ")
        val source = edge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head
        val target = edge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head
        val conClass = classifyCC(edge.conConf, edge.ccSet, source, target, boxStep, ptg)
        val subGraph = new DsOliMetaBoxGraph
        edge.conConfClass = conClass
        subGraph.graph.add(edge)
        DsOliLogger.debug(funSignature + "adding to subgrah: edge: " + edge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id + "->"
          + edge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id)
        val corEdge = DsOliGraphUtils.getCorrespondingBiDiEdge(edge, graph)
        corEdge.conConfClass = Set(new DsOliConConfClassification(conClass.head.classification, conClass.head.evidence))
        subGraph.graph.add(corEdge)
        DsOliLogger.debug(funSignature + "adding to subgrah: corEdge: " + edge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id + "->"
          + corEdge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id)
        Some(subGraph)
      } else if (edge.conConf.tag == ccPointer && edge.source.isInstanceOf[DsOliMetaBoxGraphVertexBoxes] && edge.target.isInstanceOf[DsOliMetaBoxGraphVertexBoxes]) {
        DsOliLogger.debug(funSignature + "normal edge found: ")
        val source = edge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head
        val target = edge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head
        val (conClass, edgeSet) = classifyCCPtr(edge.conConf, edge.ccSet, edge, source, target, boxStep, graph)
        val subGraph = new DsOliMetaBoxGraph
        edgeSet.foreach {
          edge =>
            // Always create a new set!
            edge.conConfClass = Set(new DsOliConConfClassification(conClass.head.classification, conClass.head.evidence))
            subGraph.graph.add(edge)
        }
        DsOliLogger.debug(funSignature + "adding to subgrah: edge: " + edge.source.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id + "->"
          + edge.target.asInstanceOf[DsOliMetaBoxGraphVertexBoxes].boxes.head.id)
        Some(subGraph)
      } else {
        None
      }
    } else {
      None
    }
  }

  /**
   * Write all labels for the edges into a file. Mainly for debugging.
   *
   * @param evidenceEdges the edges to print
   * @param eventStep the time step
   * @param event the current event
   */
  def printConnections(evidenceEdges: ListBuffer[(DsOliDataStructures, ListBuffer[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]])],
    eventStep: Long, event: DsOliEvent): Unit = {
    // Print the connections between the boxes in an specified order
    val funSignature = classSignature + "dtctDs: "
    DsOliLogger.writeLabelLog(funSignature + "time: " + eventStep + " event: " + event.getClass())
    // First group by label: e.g. all DLLs, SLs, etc.
    val groupedByLabel = evidenceEdges.groupBy(e => e._1)

    // Iterate label groups sorted
    groupedByLabel.toSeq.sortBy(elem => elem._1).foreach {
      group =>
        val (key, value) = group

        DsOliLogger.writeLabelLog(funSignature + "\tchecking: " + key)

        // Now collect all list buffers for each of the label groups and add them up
        val allBoxes = value.foldLeft(new ListBuffer[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]()) {
          (hist, cur) =>
            hist.appendAll(cur._2)
            hist
        }

        // Now group all the list buffers by the source id
        val groupedBySourceBoxId = allBoxes.groupBy { e =>
          val (sourceStr, targetStr) = DsOliGraphUtils.sourceTargetStrHelper(e)
          sourceStr
        }

        // Now sort the source ids: Important, this is not numeric sort, but string sort!
        groupedBySourceBoxId.toSeq.sortBy(elem => elem._1).foreach {
          sgroup =>
            val (source, edges) = sgroup

            // Group all the edges of one source id by its target id now
            val groupeByTargetBoxId = edges.groupBy {
              e =>
                val (sourceStr, targetStr) = DsOliGraphUtils.sourceTargetStrHelper(e)
                targetStr
            }

            // Now sort the target ids: Important, this is not numeric sort, but string sort!
            groupeByTargetBoxId.toList.sortBy(elem => elem._1).foreach {
              t =>
                val (target, edgesInner) = t
                edgesInner.foreach {
                  edgeInner =>
                    DsOliLogger.writeLabelLog(funSignature + "\t\tboxids: " + source + " -> " + target)
                    DsOliLogger.writeLabelLog(funSignature + "\t\tclass: " + edgeInner.conConfClass)
                }
            }
        }
    }
  }

  /**
   * Detect the data structure according to DSI's detection
   * predicates and hierarchy. The found data structure
   * evidences are saved in the strand graph passed to the
   * method.
   *
   * @param dsOliMbg the current strand graph (evidences will be saved there)
   * @param ptg the current points-to graph
   * @param boxStep the current strand set
   * @param eventStep the current time step
   * @param event the event
   */
  def detectDs(dsOliMbg: DsOliMetaBoxGraph, ptg: DsOliGraph, boxStep: DsOliBoxStep, eventStep: Long, event: DsOliEvent): Unit = {
    val funSignature = classSignature + "dtctDs: "
    DsOliLogger.debug(funSignature + "entered: ")

    // Operate on a copy
    val dsOliMbgDs = dsOliMbg.deepCopy

    // Remove all classifications on the edges
    // BE CAREFUL: The reset of the conConfClass with clear had strange side effects!
    // After clear some edges were not removable from graph.edges!
    val edgesToClear = new ListBuffer[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]()
    dsOliMbgDs.graph.edges.foreach {
      edge =>
        if (edge.toOuter.conConfClass != null)
          edgesToClear.append(edge.toOuter)
    }
    edgesToClear.foreach {
      edge =>
        edge.conConfClass = Set[DsOliConConfClassification]()
    }
    val evidenceEdges = new ListBuffer[(DsOliDataStructures, ListBuffer[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]])]()

    // Iterate over all edges of the graph and inspect
    // each edge according to DSI's data structure taxonomy.
    // This includes removing edges which are actually used 
    // by the predicate of the matched data structure. 
    // Iterate until no more edge is present in the graph.
    while (dsOliMbgDs.graph.edges.size > 0) {

      // Get an arbitrary edge as a handle
      var edgeInner = dsOliMbgDs.graph.edges.head
      var edgeOuter = edgeInner.toOuter

      // Debug
      val (sourceStr, targetStr) = DsOliGraphUtils.sourceTargetStrHelper(edgeOuter)
      DsOliLogger.debug(funSignature + "checking edge: " + sourceStr + " -> " + targetStr)

      // Check all area predicates now
      var foundShape = false
      breakable {

        // Check through each listed predicate, where the hierarchy
        // is given by the order in which the predicates are listed:
        // first has highest precedence
        dsHierarchy.foreach {
          dsComplexityLevel =>
            DsOliLogger.debug(funSignature + "calling checkAreaPredicate with level: " + dsComplexityLevel)

            // The actual predicate checker
            val subGraphOpt = checkAreaPredicate(dsComplexityLevel, edgeOuter, dsOliMbgDs, ptg, boxStep)

            DsOliLogger.debug(funSignature + "done with checkAreaPredicate with level: " + dsComplexityLevel)

            // If shape predicate is true
            if (subGraphOpt.isDefined) {
              val buffer = new ListBuffer[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]]()
              foundShape = true

              DsOliLogger.debug(funSignature + "found subGraph: " + subGraphOpt.get)

              // Transform edge set to list and remove all edges sequentially.
              // Must be done like this, as each edge needs to be mapped back 
              // from subGraph do dsOliMbgDs
              val edgesRem = subGraphOpt.get.graph.edges.toList
              edgesRem.foreach {
                edgeInner =>
                  // Remove all edges which form this ds

                  // Mapping back from subGraph to dsOliMbgDs
                  val remEdge = DsOliGraphUtils.findCorrespondingEdge(edgeInner.toOuter, dsOliMbgDs)

                  if (remEdge.isDefined) {
                    val (sourceStr, targetStr) = DsOliGraphUtils.sourceTargetStrHelper(remEdge.get.toOuter)
                    DsOliLogger.debug("Removing edge taken from shape sub graph: remEdge = " + sourceStr + "->" + targetStr)

                    // Do the removal
                    dsOliMbgDs.graph.edges.remove(remEdge.get)
                  } else {
                    DsOliLogger.error(funSignature + "No corresponding edge found!")
                  }

                  // Transfer the evidence counts from the matched sub graph (subGraph) to
                  // the actual graph (dsOliMbg)
                  val evidenceEdge = DsOliGraphUtils.findCorrespondingEdge(edgeInner.toOuter, dsOliMbg)
                  if (evidenceEdge.isDefined) {
                    if (edgeInner.toOuter.conConfClass != null && edgeInner.toOuter.conConfClass.size > 0) {
                      val diff = edgeInner.toOuter.conConfClass.head
                      val (source, target) = DsOliGraphUtils.sourceTargetStrHelper(edgeInner.toOuter)
                      DsOliLogger.debug(funSignature + "evidbg edgeInner.conConfClass: " + edgeInner.toOuter.conConfClass)
                      DsOliLogger.debug(funSignature + "evidbg evidenceEdge.conConfClass: " + evidenceEdge.get.conConfClass)
                      buffer.append(evidenceEdge.get.toOuter)
                      evidenceEdge.get.conConfClass.add(diff)
                      DsOliLogger.debug(funSignature + "adding diff: " + diff)
                    } else {
                      DsOliLogger.error(funSignature + "evidbg edgeInner.conConfClass not set: " + edgeInner.toOuter)
                    }
                  } else {
                    DsOliLogger.error(funSignature + "no corresponding edge for evidence transfer found.")
                  }
              }
              evidenceEdges.append((dsComplexityLevel, buffer))
              break
            }
        }
      }

      if (!foundShape) {
        DsOliLogger.debug("Removing checked edge: " + edgeInner)
        dsOliMbgDs.graph.edges.remove(edgeInner)
      }

    }
    printConnections(evidenceEdges, eventStep, event)

    // Safety step to mark all edges without a classification as unclassified
    dsOliMbg.graph.edges.foreach {
      edgeInner =>
        if (edgeInner.conConfClass == null) {
          edgeInner.conConfClass = Set()
        }
        if (edgeInner.conConfClass.size == 0) {
          edgeInner.conConfClass.add(new DsOliConConfClassification)
        }
    }
  }

  /**
   * Sequential version of the data structure detection phase. See dsNaming for
   * further details
   *
   * @return tuple with the strand graph (SG), the folded strand graph (FSG), the aggregated strand graph (ASG),
   * the aggregation count per entry pointer, labeled ASG (currently unused)
   */
  def dsNamingSeq: (DsOliTimeStepContainer[DsOliMetaBoxGraph], DsOliTimeStepContainer[DsOliMetaBoxGraph], DsOliTimeStepContainer[(DsOliVertexMemory, DsOliMetaBoxGraph)], ListBuffer[(DsOliVertexMemory, Int, Int, Int)], DsOliTimeStepContainer[(DsOliVertexMemory, DsOliMetaBoxGraph)]) = {
    val funSignature = classSignature + "dsNmng: "
    DsOliLogger.debug(funSignature + "entered: ")
    val eps = new HashMap[Long, (Int, DsOliVertexMemory)]()

    // Create the SG and FSG for each time step
    for (t <- 0 until this.events.events.length) {
      print("Naming: " + t + "/" + this.events.events.size + "\r")
      val event = this.events.events(t)
      val eventStep = t + 1

      DsOliLogger.debug(funSignature + "sg + fsg calculation: " + eventStep)
      DsOliLogger.debug(funSignature + "#Event " + event.id + "# Step " + eventStep + " event " + event.id + " naming_" + event.id + "_  ****")

      val ptg = ptgs.graphs(eventStep)
      val boxStep = this.boxSteps.boxSteps(eventStep)
      val dsOliMbg = calculateMetaBoxGraph(eventStep, eps, ptg, boxStep)

      dsOliMbgs.append(dsOliMbg)
      detectDs(dsOliMbg, ptg, boxStep, eventStep, event)

      DsOliLogger.debug(funSignature + "calculateMetaBoxGraph done")

      val dsOliMergedMbg = mergeMetaBoxGraph(dsOliMbg.deepCopy, eventStep)

      DsOliLogger.debug(funSignature + "after merge and class two graph: " + dsOliMergedMbg)
      dsOliMergedMbgs.append(dsOliMergedMbg)

      DsOliLogger.debug(funSignature + "#Done Event " + event.id + "# Step " + eventStep + " event " + event.id + "****")

    }

    val aggGraphs = new DsOliTimeStepContainer[(DsOliVertexMemory, DsOliMetaBoxGraph)]()
    val epAggCnt = new ListBuffer[(DsOliVertexMemory, Int, Int, Int)]

    DsOliLogger.debug(funSignature + "#After all events: aggregate")
    DsOliLogger.debug("Aggregate start")

    for ((vepStart, vep) <- eps.values) {
      DsOliLogger.debug(funSignature + "#After all events: aggregate ep: " + vep + "(start: " + vepStart + ")")
      val (vepret, graphret, timeStepsAlive, aggCnt) = calculateAggregateEPGraph(vepStart, vep)
      aggGraphs.append((vepret, graphret))
      epAggCnt.append((vep, vepStart, vepStart + timeStepsAlive, aggCnt))
      DsOliLogger.debug("greptoken: vep.id: " + vep.id + " : " + vepStart + " -> " + (vepStart + timeStepsAlive) + " : steps alive: " + timeStepsAlive)
    }

    DsOliLogger.debug("Aggregate end")
    DsOliLogger.debug(funSignature + "Aggregate end")

    DsOliTestMethods.typeDB = this.typeDB
    DsOliTestMethods.printMbgs(this.dsOliMbgs, this.dsOliMergedMbgs, aggGraphs, boxSteps = this.boxSteps)

    val labeledAggGraphs = new DsOliTimeStepContainer[(DsOliVertexMemory, DsOliMetaBoxGraph)]()
    DsOliLogger.debug(funSignature + "label end")

    (this.dsOliMbgs, this.dsOliMergedMbgs, aggGraphs, epAggCnt, labeledAggGraphs)
  }

  /**
   * Parallelized version of the data structure detection phase.
   * The strand graph is created (calculateMetaBoxGraph), the
   * data structure detection is performed (detectDs) and the
   * structural repetition is executed (mergeMetaBoxGraph).
   * Finally the temporal repetition is calculated
   * (calculateAggregatedEPGraph).
   *
   * @return tuple with the strand graph (SG), the folded strand graph (FSG), the aggregated strand graph (ASG),
   * the aggregation count per entry pointer, labeled ASG (currently unused)
   */
  def dsNaming: (DsOliTimeStepContainer[DsOliMetaBoxGraph], DsOliTimeStepContainer[DsOliMetaBoxGraph], DsOliTimeStepContainer[(DsOliVertexMemory, DsOliMetaBoxGraph)], ListBuffer[(DsOliVertexMemory, Int, Int, Int)], DsOliTimeStepContainer[(DsOliVertexMemory, DsOliMetaBoxGraph)]) = {
    val funSignature = classSignature + "dsNmng: "
    DsOliLogger.debug(funSignature + "entered: ")

    // Spawn off a list of futures: one future executes the SG creation, DS detection on the SG and FSG creation
    val sgDsFsgParallelStart = Some(System.nanoTime())
    var sgDsFsgParallelEnd: Option[Long] = None
    val futures = (0 to this.events.events.length - 1).toList.map {
      t =>
        val event = this.events.events(t)
        val eventStep = t + 1

        DsOliLogger.debug(funSignature + "sg + fsg calculation: " + eventStep)
        DsOliLogger.debug(funSignature + "#Event " + event.id + "# Step " + eventStep +
          " event " + event.id + " naming_" + event.id + "_  ****")

        // Prepare the data which gets passed to the future
        val ptg = ptgs.graphs(eventStep)
        val boxStep = this.boxSteps.boxSteps(eventStep)
        val eps = new HashMap[Long, (Int, DsOliVertexMemory)]()

        // Create the future
        future {

          // Calculate the strand graph (SG)
          val sgTimingStart = Some(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime())
          val allTimingStart = sgTimingStart
          val dsOliMbg = calculateMetaBoxGraph(eventStep, eps, ptg, boxStep)
          val sgTimingEnd = Some(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime())

          // Detect the data structures on the SG
          val dsDetectTimingStart = Some(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime())
          detectDs(dsOliMbg, ptg, boxStep, eventStep, event)
          val dsDetectTimingEnd = Some(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime())

          DsOliLogger.debug(funSignature + "calculateMetaBoxGraph done")

          // Calculate the folded stradn graph (FSG) for structural repetition
          val fsgTimingStart = Some(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime())
          val dsOliMergedMbg = mergeMetaBoxGraph(dsOliMbg.deepCopy, eventStep)
          val fsgTimingEnd = Some(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime())
          val allTimingEnd = fsgTimingEnd

          DsOliLogger.debug(funSignature + "after merge and class two graph: " + dsOliMergedMbg)
          DsOliLogger.debug(funSignature + "#Done Event " + event.id + "# Step " + eventStep +
            " event " + event.id + "****")
          println("asg/fsg done event: " + eventStep)

          // Can be used for timing
          val allMilli = DsOliGeneralPurposeUtils.millisDiff(allTimingStart, allTimingEnd).get
          val sgMilli = DsOliGeneralPurposeUtils.millisDiff(sgTimingStart, sgTimingEnd).get
          val dsMilli = DsOliGeneralPurposeUtils.millisDiff(dsDetectTimingStart, dsDetectTimingEnd).get
          val fsgMilli = DsOliGeneralPurposeUtils.millisDiff(fsgTimingStart, fsgTimingEnd).get

          (t, dsOliMbg, dsOliMergedMbg, eps, (allMilli, sgMilli, dsMilli, fsgMilli))
        }
    }

    // Stores the ASG specific information
    val aggGraphs = new DsOliTimeStepContainer[(DsOliVertexMemory, DsOliMetaBoxGraph)]()
    val epAggCnt = new ListBuffer[(DsOliVertexMemory, Int, Int, Int)]
    val labeledAggGraphs = new DsOliTimeStepContainer[(DsOliVertexMemory, DsOliMetaBoxGraph)]()

    // Can be used for timing
    var allTimingsAdded = 0L
    var sgTimingsAdded = 0L
    var dsTimingsAdded = 0L
    var fsgTimingsAdded = 0L
    var asgTimingsAdded = 0L

    val eps = new HashMap[Long, (Int, DsOliVertexMemory)]()
    val futuresSeq = Future sequence futures

    // Barrier which collects the results and creates Futures for ASG creation afterwards
    var asgParallelStart: Option[Long] = None
    var asgParallelEnd: Option[Long] = None
    val asgFuture = futuresSeq flatMap {
      list =>
        sgDsFsgParallelEnd = Some(System.nanoTime())
        // Collect results: Sort by time step
        list.sortBy(tuple => tuple._1).foreach {
          case (u, sg, fsg, epstmp, timings) =>

            // First fetch the eps collected by each future
            // As this is ordered, the first appearance should be the correct starting point of this ep
            epstmp.keys.foreach {
              key =>
                if (!eps.contains(key)) {
                  eps.put(key, epstmp.get(key).get)
                }
            }
            val (allT, sgT, dsT, fsgT) = timings
            allTimingsAdded += allT
            sgTimingsAdded += sgT
            dsTimingsAdded += dsT
            fsgTimingsAdded += fsgT

            // Then collect the SG and the FSG produced by the futures
            this.dsOliMbgs.append(sg)
            this.dsOliMergedMbgs.append(fsg)

        }

        asgParallelStart = Some(System.nanoTime())
        // Now spawn off the aggregation futures
        val asgFutures = eps.values map {
          case (vepStart, vep) =>
            future {
              DsOliLogger.debug(funSignature + "#After all events: aggregate ep: " + vep + "(start: " + vepStart + ")")

              val aggTimingStart = Some(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime())
              // Calculate the aggregated strand graph (ASG) for the temporal repetition
              val (vepret, graphret, timeStepsAlive, aggCnt) = calculateAggregateEPGraph(vepStart, vep)
              val aggTimingEnd = Some(ManagementFactory.getThreadMXBean().getCurrentThreadCpuTime())

              DsOliLogger.debug(funSignature + "vep.id: " + vep.id + " : " + vepStart + " -> " +
                (vepStart + timeStepsAlive) + " : steps alive: " + timeStepsAlive)

              ((vepret, graphret), (vep, vepStart, vepStart + timeStepsAlive, aggCnt), DsOliGeneralPurposeUtils.millisDiff(aggTimingStart, aggTimingEnd).get)
            }
        }
        Future sequence asgFutures
    }

    // Barrier: Collect the data from the aggregation phase
    val result = asgFuture flatMap {
      results =>
        asgParallelEnd = Some(System.nanoTime())
        results.foreach {
          case (aggGraph, epCnt, timings) =>
            aggGraphs.append(aggGraph)
            epAggCnt.append(epCnt)
            asgTimingsAdded += timings
            allTimingsAdded += timings
        }

        val sgDsFsgTimings = DsOliGeneralPurposeUtils.millisDiff(sgDsFsgParallelStart, sgDsFsgParallelEnd).get
        val asgTimings = DsOliGeneralPurposeUtils.millisDiff(asgParallelStart, asgParallelEnd).get

        val measuredToSeconds = 1000000000.0

        val allTimingsAddedInSec = (allTimingsAdded / measuredToSeconds)
        val sgTimingsAddedInSec = (sgTimingsAdded / measuredToSeconds)
        val dsTimingsAddedInSec = (dsTimingsAdded / measuredToSeconds)
        val fsgTimingsAddedInSec = (fsgTimingsAdded / measuredToSeconds)
        val sgDsFsgTimingsInSec = (sgDsFsgTimings / measuredToSeconds)
        val asgTimingsAddedInSec = (asgTimingsAdded / measuredToSeconds)
        val asgTimingsInSec = (asgTimings / measuredToSeconds)

        println("dsi-timing: all-total time: " + allTimingsAddedInSec + " [s] mean: " + (allTimingsAddedInSec / this.events.events.length) + " [s]")
        println("dsi-timing: sg-total time: " + sgTimingsAddedInSec + " [s] mean: " + (sgTimingsAddedInSec / this.events.events.length) + " [s] percent of all: " + (DsOliGeneralPurposeUtils.round2Digits(((sgTimingsAddedInSec.toFloat / allTimingsAddedInSec.toFloat) * 100))) + " %")
        println("dsi-timing: ds-total time: " + dsTimingsAddedInSec + " [s] mean: " + (dsTimingsAddedInSec / this.events.events.length) + " [s] percent of all: " + (DsOliGeneralPurposeUtils.round2Digits(((dsTimingsAddedInSec.toFloat / allTimingsAddedInSec.toFloat) * 100))) + " %")
        println("dsi-timing: fsg-total time: " + fsgTimingsAddedInSec + " [s] mean: " + (fsgTimingsAddedInSec / this.events.events.length) + " [s] percent of all: " + (DsOliGeneralPurposeUtils.round2Digits(((fsgTimingsAddedInSec.toFloat / allTimingsAddedInSec.toFloat) * 100))) + " %")
        println("dsi-timing: sg-ds-fsg-parallel-total time: " + sgDsFsgTimingsInSec + " [s] mean: " + (sgDsFsgTimingsInSec / this.events.events.length) + " [s] percent of all: " + (DsOliGeneralPurposeUtils.round2Digits(((sgDsFsgTimingsInSec.toFloat / allTimingsAddedInSec.toFloat) * 100))) + " %")
        println("dsi-timing: asg-total time: " + asgTimingsAddedInSec + " [s] mean: " + (asgTimingsAddedInSec / this.events.events.length) + " [s] percent of all: " + (DsOliGeneralPurposeUtils.round2Digits(((asgTimingsAddedInSec.toFloat / allTimingsAddedInSec.toFloat) * 100))) + " %")
        println("dsi-timing: asg-parallel-total time: " + asgTimingsInSec + " [s] mean: " + (asgTimingsInSec / this.events.events.length) + " [s] percent of all: " + (DsOliGeneralPurposeUtils.round2Digits(((asgTimingsInSec.toFloat / allTimingsAddedInSec.toFloat) * 100))) + " %")

        // This is only to satisfy the return interface
        future {
          DsOliTestMethods.typeDB = this.typeDB
          (this.dsOliMbgs, this.dsOliMergedMbgs, aggGraphs, epAggCnt, labeledAggGraphs)
        }
    }

    //  Await the result
    val ret = Await.result(result, Duration.Inf)
    DsOliTestMethods.printMbgs(this.dsOliMbgs, this.dsOliMergedMbgs, aggGraphs, boxSteps = this.boxSteps)

    ret
  }
}
