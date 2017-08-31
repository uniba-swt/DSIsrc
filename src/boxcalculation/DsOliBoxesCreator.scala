
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
 * DsOliBoxesCreator.scala created on Oct 29, 2014
 *
 * Description: Calculate strands (prior terminology
 * was boxes)
 */
package boxcalculation

import pointstograph.DsOliPointsToGraphs
import event.DsOliEvents
import event.DsOliMemoryEvent
import pointstograph.DsOliGraph
import pointstograph.DsOliVertexMemory
import pointstograph.DsOliVertex
import pointstograph.ITypeDB
import event.DsOliMWEvent
import extlogger.DsOliLogger
import scala.util.control.Breaks._
import scala.collection.mutable.ListBuffer
import boxcalculation.DsOliCycle._
import test.DsOliTestMethods
import PartialFunction._
import event.DsOliArtificialUndefEvent
import event.DsOliArtificialFreeEvent
import event.DsOliMWEvent
import event.DsOliFreeEvent
import event.DsOliVLSEvent
import event.DsOliEvent
import util.DsOliAddressUtils
import event.DsOliLValue
import event.DsOliRValue

/**
 * @author DSI
 *
 * @constructor creates an instance of the strands creator
 * @param events the event trace
 * @param ptgs the points-to graphs for the event trace
 * @param typeDB the type store for DSI
 */
class DsOliBoxesCreator(val events: DsOliEvents, val ptgs: DsOliPointsToGraphs, val typeDB: ITypeDB) extends IDsOliBoxCreator {

  val classSignature = "DsOliBoxesCreator::"

  /**
   * Convert a given vertex into a memory vertex
   *
   * @param vertex the vertex to convert
   * @return the vertex casted to a memory vertex
   */
  def getMemoryVertex(vertex: DsOliVertex): DsOliVertexMemory = {
    val funSignature = classSignature + "getMemoryVertex: "
    DsOliLogger.debug(funSignature + "entered: ")
    vertex match {
      case v: DsOliVertexMemory => v
      case _ => throw new Exception("Not a memory vertex")
    }
  }

  /**
   * Checks if the given memory regions are overlapping
   *
   * @param sourceBegin the start address of the first memory region
   * @param sourceEnd the end address of the first memory region
   * @param targetBegin the start address of the second memory region
   * @param targetEnd the end address of the second memory region
   * @return Boolean
   */
  def regionsOverlapping(sourceBegin: Long, sourceEnd: Long, targetBegin: Long, targetEnd: Long): Boolean = {
    val funSignature = classSignature + "regionsOverlapping: "
    DsOliLogger.debug(funSignature + "entered: ")
    if (sourceBegin <= targetBegin && targetBegin <= sourceEnd ||
      targetBegin <= sourceBegin && sourceBegin <= targetEnd) {
      true
    } else {
      false
    }
  }

  /**
   * Tests if two cells fulfill the linkage conditions to form a linked list,
   * i.e., a strand
   *
   * @param sourceAddr the source address of the pointer
   * @param targetAddre the target address where the pointer points to
   * @param ptg the points-to graph
   * @param currentBoxes the current set of strands
   * @return Option tuple of the source and target cell with the linkage offset
   */
  def minCond(sourceAddr: Long, targetAddr: Long, ptg: DsOliGraph, currentBoxes: DsOliBoxStep): Option[(DsOliCell, DsOliCell, Long)] = {
    val funSignature = classSignature + "minCond: "
    DsOliLogger.debug(funSignature + "entered: ")
    val nullTuple: (DsOliCell, DsOliCell, Long) = (null, null, 0)

    val retVal = try {

      // NULL?
      if (sourceAddr == 0 || targetAddr == 0) throw new Exception("Source/Target is NULL")

      // Do source and target vertices exist for the addresses?
      val vertexSourceOpt = ptg.getVertexForAddress(sourceAddr)
      val vertexTargetOpt = if (vertexSourceOpt.isDefined) ptg.getVertexForAddress(targetAddr) else throw new Exception("Source vertex not found")
      if (vertexTargetOpt.isEmpty) throw new Exception("Target vertex not found")

      // Get the vertices
      val vertexSource = getMemoryVertex(vertexSourceOpt.get)
      val vertexTarget = getMemoryVertex(vertexTargetOpt.get)

      // Calculate the offset into the target
      val targetOffset = targetAddr - vertexTarget.bAddr

      // Test if there exists a type at this boundary
      val targetCellTypeOpt = typeDB.isOffsetAtTypeBoundary(targetOffset, vertexTarget.vType.vType)
      if (targetCellTypeOpt.isEmpty) throw new Exception("Target type not at type boundary")
      var targetCellTypes = targetCellTypeOpt.get

      // Calculate the source offset
      val sourceOffset = sourceAddr - vertexSource.bAddr

      // Test if the target type is also found in the source
      val sourceCellTypeOpt = typeDB.getMatchingTypeForOffset(sourceOffset, vertexSource.vType.vType, targetCellTypes)
      if (sourceCellTypeOpt.isEmpty) throw new Exception("Source type does not match target type")
      val sourceCellTypeOffset = sourceCellTypeOpt.get._1
      val sourceCellType = sourceCellTypeOpt.get._2
      val targetCellType = sourceCellType
      DsOliLogger.debug(funSignature + "sourceCellType: " + sourceCellType + ", targetCellType: " + targetCellType)

      val sourceCellBAddr = vertexSource.bAddr + sourceOffset - sourceCellTypeOffset
      val sourceCellEAddr = sourceCellBAddr + sourceCellType.size - 1

      DsOliLogger.debug(funSignature + "sourceCellBAddr: " + sourceCellBAddr.toHexString + ", sourceCellEAddr: " + sourceCellEAddr.toHexString)

      // Overlapping memory regions?
      val targetCellEAddr = targetAddr + targetCellType.size - 1
      if (regionsOverlapping(sourceCellBAddr, sourceCellEAddr,
        targetAddr, targetCellEAddr)) throw new Exception("Memory regions are overlapping")

      DsOliLogger.debug(funSignature + "calculated offset: " + sourceCellTypeOffset)

      // Try to lookup the cell and use it, if it already exists. Otherwise use the newly created cell
      Some(currentBoxes.lookupOrUseCell(new DsOliCell(sourceCellBAddr, sourceCellEAddr, sourceCellType, vertexSource.id)),
        currentBoxes.lookupOrUseCell(new DsOliCell(targetAddr, targetCellEAddr, targetCellType, vertexTarget.id)),
        sourceCellTypeOffset)

    } catch {
      case e: Exception =>
        DsOliLogger.debug(funSignature + " " + e.getMessage() +
          "; sourceAddress: " + sourceAddr.toHexString +
          "; targetAddr: " + targetAddr.toHexString)
        if (e.getMessage().contains("None.get")) throw e
        None
    }

    return retVal
  }

  /**
   * Expand the given cycle into a sequence again, by breaking
   * it apart at the given cell.
   *
   * @param cell the cell where to split the cycle
   * @param cycle the cycle to split
   * @param currentBoxes the set of strands
   */
  def expandCycleIntoSequence(cell: DsOliCell, cycle: DsOliCycle, currentBoxes: DsOliBoxStep): Unit = {
    val funSignature = classSignature + "expandCycleIntoSequence: "
    DsOliLogger.debug(funSignature + "entered. cell: " + cell + ", cycle.id: " + cycle.id)
    currentBoxes.boxes.foreach {
      boxTuple =>
        val (boxId, box) = boxTuple
        box match {
          case DsOliBox(_, offset, cType, cells, cycleId, cycleEntry) =>
            if (cycle.id == cycleId) {
              DsOliLogger.debug(funSignature + "matched, doing expansion on box: " + box + " cycle.id: " + cycle.id)
              DsOliLogger.debug(funSignature + "cycle: " + cycle)
              
              val cycleEntryCell = cycle.cells.find(_.id == cycleEntry).get
              val cycleEntryCellIndex = getCellIndexFromList(cycleEntryCell, cycle.cells).get
              val cycleEndIndex = getCellIndexFromList(cell, cycle.cells).get
              // exclusive cycle -> copy all cells over
              if (box.cells.length == 0) {
                if (cycleEndIndex < cycle.cells.length - 1) {
                  box.cells ++= cycle.cells.slice(cycleEndIndex + 1, cycle.cells.length)
                }
                box.cells ++= cycle.cells.slice(0, cycleEndIndex + 1)
              } else {

                // 
                if (cycleEntryCell == 0 && cycleEndIndex == 0) {
                  box.cells ++= cycle.cells
                } // Detect if entry cell comes before source cell: slice directly
                else if (cycleEntryCellIndex <= cycleEndIndex) {
                  DsOliLogger.debug(funSignature + "direct slice")
                  DsOliLogger.debug(funSignature + "cycleEntryCellIndex: " + cycleEntryCellIndex + " cycleEndIndex: " + cycleEndIndex)

                  box.cells ++= cycle.cells.slice(cycleEntryCellIndex, cycleEndIndex + 1)
                } else {
                  // source cell comes before the entry cell: 
                  // (slice from entry cell to end) + (slice from beginning to entry cell - 1)
                  DsOliLogger.debug(funSignature + "two slices")
                  box.cells ++= cycle.cells.slice(cycleEntryCellIndex, cycle.cells.length)
                  box.cells ++= cycle.cells.slice(0, cycleEndIndex + 1)
                }

                DsOliLogger.debug(funSignature + "reconstructed box: " + box)
              }
              box.cycleId = 0
              box.cycleEntryPoint = 0
            } else {
              DsOliLogger.debug(funSignature + "cycle id mismatch.")
            }
          case _ =>

            DsOliLogger.debug(funSignature + "no match: " + box)
        }
    }
  }

  /**
   * Remove the cycle from each strand where target cell is the last
   * element.
   *
   * @param cycle the cycle to cut
   * @param targetCell the cell on which to cut
   * @param currentBoxes the strands to check
   */
  def cutCycle(cycle: DsOliCycle, targetCell: DsOliCell, currentBoxes: DsOliBoxStep): Unit = {
    val funSignature = classSignature + "cutCycle: "
    DsOliLogger.debug(funSignature + "entered. cycle: " + cycle + ", targetCell: " + targetCell)
    currentBoxes.boxes.foreach {
      boxTuple =>
        val (boxId, box) = boxTuple
        if (box.cycleId == 0 && box.cells.last == targetCell) {

          DsOliLogger.debug(funSignature + "found box for cycle removal: " + box)
          box.cycleId = 0
          box.cycleEntryPoint = 0
        }
    }
  }

  /**
   * Remove the downstream strand part from all upstream strands
   * containing the downstream part.
   *
   * @param cell the cell where to cut
   * @param downstreamBox obsolete
   * @param offset the linkage offset
   * @param currentBoxes the strands to search
   */
  def cutDownstreamSeqFromUpstreamBoxes(cell: DsOliCell, downstreamBox: DsOliBox, offset: Long, currentBoxes: DsOliBoxStep): Unit = {
    val funSignature = classSignature + "cutDownstreamSeqFromUpstreamBoxes: "
    DsOliLogger.debug(funSignature + "entered: ")
    val delBoxes = new DsOliBoxStep()
    val newBoxes = new DsOliBoxStep()
    DsOliLogger.debug(funSignature + "entered. cell: " + cell + ", offset: " + offset)

    // Cycle through boxes and cut off the downstream sequence
    currentBoxes.boxes.foreach {
      box =>
        DsOliLogger.debug(funSignature + "testing box id : " + box._1 + ": " + box._2)
        condOpt(box) {
          case (boxId, DsOliBox(_, boxOffset, _, cells, _, _)) =>
            if (boxOffset == offset) {
              val cellIndexOpt = getCellIndex(cell, box._2)
              if (cellIndexOpt.isDefined) {

                DsOliLogger.debug(funSignature + "cellIndex found: " + cellIndexOpt.get)
                delBoxes.boxes.put(boxId, box._2)

                // New box with downstream part cut off
                val newBox = new DsOliBox(offset, box._2.cType, getCellSliceUpToIndex(box._2, cellIndexOpt.get + 1), 0, 0)
                DsOliLogger.debug(funSignature + "new box created with id: " + newBox.id + ": " + newBox)

                newBoxes.boxes.put(newBox.id, newBox)
              } else {
                DsOliLogger.debug(funSignature + "index for cell not found: " + cell)
              }
            } else {
              DsOliLogger.debug(funSignature + "offset mismatch: offset:" + offset + ", boxOffset: " + boxOffset)
            }
        }
    }

    // Actually add the new boxes
    newBoxes.boxes.foreach {
      boxTuple =>
        val (boxId, box) = boxTuple
        currentBoxes.boxes.put(boxId, box)
        DsOliLogger.debug(funSignature + "adding box id: " + boxId)
    }

    // Cleanup old boxes
    delBoxes.boxes.foreach {
      boxTuple =>
        val (boxId, box) = boxTuple
        currentBoxes.boxes -= boxId
        DsOliLogger.debug(funSignature + "deleting box id: " + boxId)
    }

  }

  /**
   * If downstream is not covered by any existing strand, add it
   *
   * @param downstreamBox the downstream strand
   * @param offset the linkage offset
   * @param currentBoxes the set of strands to check
   */
  def conditionallyAddDownstreamBoxToBoxSet(downstreamBox: DsOliBox, offset: Long, currentBoxes: DsOliBoxStep): Unit = {
    val funSignature = classSignature + "conditionallyAddDownstreamBoxToBoxSet: "
    DsOliLogger.debug(funSignature + "entered.")
    val downstreamCells = downstreamBox.cells
    if (!currentBoxes.boxes.exists {
      boxTuple =>
        val (boxId, box) = boxTuple
        box.offset == downstreamBox.offset && box.cells.containsSlice(downstreamCells)
    }) {
      DsOliLogger.debug(funSignature + "Add the downstream box: " + downstreamBox)
      currentBoxes.boxes.put(downstreamBox.id, downstreamBox)
    }
  }

  /**
   * Destruct strands due to an memory write event.
   * Results in removal or splitting of strands.
   *
   * @param event the memory write event
   * @param ptg the points-to graph from the previous time step
   * @param currentBoxes copy of the previous time step to represent the current changes
   */
  def destructBoxes(event: DsOliMWEvent, ptg: DsOliGraph, currentBoxes: DsOliBoxStep): Unit = {
    val funSignature = classSignature + "destructBoxes: "

    DsOliLogger.debug(funSignature + "entered")
    val sourceAddress = event.lValueList.last.address
    val targetAddressOpt = ptg.getCurrentTargetAddr(sourceAddress)
    val targetAddress: Long = if (targetAddressOpt.isDefined) targetAddressOpt.get else 0

    DsOliLogger.debug(funSignature + "searching for sAddr: " + sourceAddress.toHexString + ", tAddr: " + targetAddress.toHexString)

    val sourceTargetCellsOpt = minCond(sourceAddress, targetAddress, ptg, currentBoxes)
    if (sourceTargetCellsOpt.isDefined) {

      val (sourceCell, targetCell, offset) = sourceTargetCellsOpt.get
      DsOliLogger.debug(funSignature + "minCond is fullfilled. " + sourceCell + ", " + targetCell + ", " + offset)

      if (isCyclic(sourceCell, offset, currentBoxes)) {
        DsOliLogger.debug(funSignature + "isCyclic on sourceCell")
        val cycleOpt = getCycle(sourceCell, offset, currentBoxes)

        if (cycleOpt.isDefined) {
          DsOliLogger.debug(funSignature + "do expansion and remove cycle: " + cycleOpt.get)
          expandCycleIntoSequence(sourceCell, cycleOpt.get, currentBoxes)
          currentBoxes.cycles -= cycleOpt.get.id
        } else {
          throw new Exception(funSignature + "no cycle found for sourceCell: " + sourceCell)
        }

      } else if (isCyclic(targetCell, offset, currentBoxes)) {
        DsOliLogger.debug(funSignature + "isCyclic on targetCell")
        val cycleOpt = getCycle(targetCell, offset, currentBoxes)

        if (cycleOpt.isDefined) {
          DsOliLogger.debug(funSignature + "do expansion and remove cycle: " + cycleOpt.get)
          cutCycle(cycleOpt.get, sourceCell, currentBoxes)
        } else {
          throw new Exception(funSignature + "no cycle found for targetCell: " + targetCell)
        }
      } else {
        DsOliLogger.debug(funSignature + "no cycle")
        val downstreamBoxOpt = getDownstreamBox(targetCell, offset, currentBoxes)
        if (downstreamBoxOpt.isDefined) {
          val downstreamBox = downstreamBoxOpt.get
          cutDownstreamSeqFromUpstreamBoxes(sourceCell, downstreamBox, offset, currentBoxes)
          conditionallyAddDownstreamBoxToBoxSet(downstreamBox, offset, currentBoxes)
        } else {
          DsOliLogger.error(funSignature + "no downstream box available.")
        }
      }
    } else {
      DsOliLogger.debug(funSignature + "minCond is not fullfilled.")
    }
  }

  /**
   * Checks, if the fulfilled linkage condition will lead to a cyclic strand
   *
   * @param sourceCell the source cell
   * @param targetCell the target Cell
   * @param offset the linkage offset
   * @param boxes the strands
   * @return Boolean
   */
  def willBeCyclic(sourceCell: DsOliCell, targetCell: DsOliCell, offset: Long, boxes: DsOliBoxStep): Boolean = {
    val funSignature = classSignature + "willBeCyclic: "
    DsOliLogger.debug(funSignature + "entered.")
    return boxes.boxes.exists {
      _ match {
        case (boxId, box) =>
          // cycleId == 0 => No cycle attached
          DsOliLogger.debug(funSignature + "testing box: " + box)
          if (box.cycleId == 0) {
            DsOliLogger.debug(funSignature + "non cyclic box")
            DsOliLogger.debug(funSignature + "box.offset(" + box.offset + ") == offset(" + offset + ")")
            DsOliLogger.debug(funSignature + "box.cells.last.id(" + box.cells.last.id + ") == sourceCell.id(" + sourceCell.id + ")")
            DsOliLogger.debug(funSignature + "box.cells.exists(cell => cell.id == targetCell.id): " + box.cells.exists(cell => cell.id == targetCell.id))
          } else {
            DsOliLogger.debug(funSignature + "cyclic box! box.cycleId(" + box.cycleId + ") == 0: " + (box.cycleId == 0))
          }
          if (box.cycleId == 0 &&
            box.offset == offset &&
            box.cells.last.id == sourceCell.id &&
            box.cells.exists(cell => cell.id == targetCell.id)) {
            DsOliLogger.debug(funSignature + "will be cyclic on box: " + box)
            DsOliLogger.debug(funSignature + "will be cyclic on box sourceCell: " + sourceCell)
            DsOliLogger.debug(funSignature + "will be cyclic on box targetCell: " + targetCell)
            true
          } else {
            DsOliLogger.debug(funSignature + "no cycle detected in this iteration")
            false
          }
        case _ =>
          DsOliLogger.warning(funSignature + "no box tuple")
          false
      }

    }
  }

  /**
   * Checks, if the given cell is in a cycle
   *
   * @param targetCell the cell to check
   * @param offset the linkage offset
   * @param boxes the set of boxes for the current time step
   * @return Boolean
   */
  def isCyclic(targetCell: DsOliCell, offset: Long, boxes: DsOliBoxStep): Boolean = {
    val funSignature = classSignature + "isCyclic: "
    DsOliLogger.debug(funSignature + "entered: ")
    DsOliLogger.debug(funSignature + "entered. with target cell: " + targetCell)
    return getCycle(targetCell, offset, boxes).isDefined
  }

  /**
   * Fetch a cycle where the given cell participates
   *
   * @param cell the cell to check for
   * @param offset the linkage offset
   * @param boxes the set of boxes for the current time step
   * @return Option the cycle
   */
  def getCycle(cell: DsOliCell, offset: Long, boxes: DsOliBoxStep): Option[DsOliCycle] = {
    val funSignature = classSignature + "getCycle: "
    DsOliLogger.debug(funSignature + "entered: ")
    var retCycle: Option[DsOliCycle] = None
    breakable {
      boxes.cycles.foreach {
        _ match {
          case (cycleID, cycle) =>
            DsOliLogger.debug(funSignature + "testing on cycleID: " + cycleID + " and cycle: " + cycle)
            if (cycle.offset == offset && cycle.cells.exists { iterCell =>
              DsOliLogger.debug(funSignature + "iterCell.id == cell.id: " + iterCell.id + "==" + cell.id + " iterCell: " + iterCell)
              iterCell.id == cell.id
            }) {
              DsOliLogger.debug(funSignature + "target cell is part of cycle")
              retCycle = Some(cycle)
              break
            } else {
              DsOliLogger.debug(funSignature + "no cycle detected in this iteration")
            }
          case _ =>
            DsOliLogger.debug(funSignature + "no box tuple")
        }
      }
    }
    return retCycle
  }

  /**
   * Fetch the index of a cell
   *
   * @param cell the cell to search for
   * @param box the strand to search in
   * @return Option the index
   */
  def getCellIndex(cell: DsOliCell, box: DsOliBox): Option[Int] = {
    val funSignature = classSignature + "getCellIndex: "
    DsOliLogger.debug(funSignature + "entered. cell: " + cell)
    getCellIndexFromList(cell, box.cells)
  }

  /**
   * Convert the cell into an index of the list representation of the cycle
   *
   * @param cell the cell to search for
   * @param cells the list representation of the cycle
   * @return Option the index of the cell
   */
  def getCellIndexFromList(cell: DsOliCell, cells: ListBuffer[DsOliCell]): Option[Int] = {
    val funSignature = classSignature + "getCellIndexFromList: "
    DsOliLogger.debug(funSignature + "entered. cell: " + cell)
    val cellIterator = cells.iterator
    var retCellIndex = 0
    var continue = true
    while (continue && cellIterator.hasNext) {
      val testCell = cellIterator.next
      if (testCell.id == cell.id) {
        DsOliLogger.debug(funSignature + "id match: testCell.id == cell.id : " + testCell.id + " == " + cell.id)
        continue = false
      } else {
        DsOliLogger.debug(funSignature + "mismatch: testCell.id != cell.id : " + testCell.id + " != " + cell.id)
      }
      retCellIndex += 1
    }
    if (!continue) {
      retCellIndex -= 1
      Some(retCellIndex)
    } else {
      None
    }
  }

  /**
   * Find a strand with the given cell and offset
   *
   * @param segmentCell the cell to find
   * @param offset the linkage offset
   * @param boxes the strands to search in
   * @return Option tuple with the cell index and the strand
   */
  def boxExistsWithOffsetAndCell(segmentCell: DsOliCell, offset: Long, boxes: DsOliBoxStep): Option[(Int, DsOliBox)] = {
    val funSignature = classSignature + "boxExistsWithOffsetAndCell: "
    DsOliLogger.debug(funSignature + "entered. segmentCell: " + segmentCell + ", offset: " + offset)
    var retVal: Option[(Int, DsOliBox)] = None
    var retBox: DsOliBox = null
    var retCellIndex = 0
    breakable {
      boxes.boxes.foreach {
        boxTuple =>
          val (boxID, box) = boxTuple
          box match {
            case DsOliBox(_, boxOffset, _, _, _, _) =>
              if (boxOffset == offset) {
                DsOliLogger.debug(funSignature + "offset match.")
                val cellIndexOpt = getCellIndex(segmentCell, box)
                if (cellIndexOpt.isDefined) {
                  DsOliLogger.debug(funSignature + "cell index match: " + cellIndexOpt.get)
                  retVal = Some(cellIndexOpt.get, boxes.boxes.get(boxID).get)
                  break
                }
              } else {
                DsOliLogger.debug(funSignature + "offset mismatch match.")
              }

            case _ =>
              DsOliLogger.debug(funSignature + "no match")
          }
      }
    }
    return retVal
  }

  /**
   * Get the slice of cells from the start index to the end of the strand
   *
   * @param box the strand to slice
   * @param startIndex the index to start
   * @return list of cells
   */
  def getCellSliceToEnd(box: DsOliBox, startIndex: Int): ListBuffer[DsOliCell] = {
    val funSignature = classSignature + "getCellSliceToEnd: "
    DsOliLogger.debug(funSignature + "entered: ")
    return box.cells.slice(startIndex, box.cells.length)
  }

  /**
   * Get the slice of cells from the start of the strand until the end index
   *
   * @param box the strand to slice
   * @param endIndex the index to end
   * @param list of cells
   */
  def getCellSliceUpToIndex(box: DsOliBox, endIndex: Int): ListBuffer[DsOliCell] = {
    val funSignature = classSignature + "getCellSliceUpToIndex: "
    DsOliLogger.debug(funSignature + "entered: ")
    return box.cells.slice(0, endIndex)
  }

  /**
   * Get the slice of cells of the strand from the start index until the end index
   *
   * @param box the strand to slice
   * @param startIndex the index to start
   * @param endIndex the index to end
   * @param list of cells
   */
  def getCellSliceBetweenIndices(box: DsOliBox, startIndex: Int, endIndex: Int): ListBuffer[DsOliCell] = {
    val funSignature = classSignature + "getCellSliceBetweenIndices: "
    DsOliLogger.debug(funSignature + "entered: ")
    return if (endIndex + 1 > box.cells.length) {
      getCellSliceToEnd(box, startIndex)
    } else {
      box.cells.slice(startIndex, endIndex + 1)
    }
  }

  def calculateUpstreamBoxes(cell: DsOliCell, offset: Long, boxes: DsOliBoxStep): Option[DsOliBoxStep] = {
    val funSignature = classSignature + "calculateUpstreamBoxes: "
    DsOliLogger.debug(funSignature + "entered: ")
    val delBoxes = new DsOliBoxStep()
    val newBoxes = new DsOliBoxStep()
    DsOliLogger.debug(funSignature + "entered. cell: " + cell + ", offset: " + offset)
    boxes.boxes.foreach {
      box =>
        DsOliLogger.debug(funSignature + "testing box id : " + box._1 + ": " + box._2)
        box match {
          case (boxId, DsOliBox(_, boxOffset, _, cells, _, _)) =>
            if (boxOffset == offset) {
              DsOliLogger.debug(funSignature + "offset match")
              val cellIndexOpt = getCellIndex(cell, box._2)
              if (cellIndexOpt.isDefined) {
                DsOliLogger.debug(funSignature + "cellIndex found: " + cellIndexOpt.get)
                delBoxes.boxes.put(boxId, box._2)
                val newBox = new DsOliBox(offset, box._2.cType, getCellSliceUpToIndex(box._2, cellIndexOpt.get + 1), 0, 0)
                DsOliLogger.debug(funSignature + "new box created with id: " + newBox.id + ": " + newBox)
                newBoxes.boxes.put(newBox.id, newBox)
              } else {
                DsOliLogger.debug(funSignature + "index for cell not found: " + cell)
              }
            } else {
              DsOliLogger.debug(funSignature + "offset mismatch")
            }

          case _ => None
        }
    }

    delBoxes.boxes.foreach {
      boxTuple =>
        val (boxId, box) = boxTuple
        boxes.boxes -= boxId
        DsOliLogger.debug(funSignature + "deleting box id: " + boxId)
    }

    return if (newBoxes.boxes.size == 0) {
      DsOliLogger.debug(funSignature + "no new boxes.")
      None
    } else {
      DsOliLogger.debug(funSignature + "got new boxes.")
      newBoxes.boxes.foreach {
        boxes.boxes += _
      }
      Some(newBoxes)
    }
  }

  def getDownstreamBox(target: DsOliCell, offset: Long, boxes: DsOliBoxStep): Option[DsOliBox] = {
    val funSignature = classSignature + "getDownstreamBox: "
    DsOliLogger.debug(funSignature + "entered: ")
    val boxWithSegCellOpt = boxExistsWithOffsetAndCell(target, offset, boxes)

    if (boxWithSegCellOpt.isDefined) {

      val (cellIndex, downstreamBox) = boxWithSegCellOpt.get

      DsOliLogger.debug(funSignature + "box exists: cellIndex: " + cellIndex + ", downstreamBox: " + downstreamBox)

      val cellSlice = getCellSliceToEnd(downstreamBox, cellIndex)

      // Test, if we were slicing on the last element
      if (cellSlice.length > 0) {
        val newDownstreamBox = new DsOliBox(offset, downstreamBox.cType, cellSlice, downstreamBox.cycleId, downstreamBox.cycleEntryPoint)
        DsOliLogger.debug(funSignature + "new box created with id: " + newDownstreamBox.id)
        DsOliLogger.debug(funSignature + "box exists: cellIndex: " + cellIndex + ", cellSlice: " + cellSlice)
        DsOliLogger.debug(funSignature + "box exists: cellIndex: " + cellIndex + ", newDownstreamBox: " + newDownstreamBox)

        Some(newDownstreamBox)
      } else {
        DsOliLogger.debug(funSignature + "no downstream box exists (last element).")
        None
      }
    } else {
      DsOliLogger.debug(funSignature + "no downstream box exists")
      None
    }

  }

  /**
   * Create a upstream strand containing the cell
   *
   * @param cell the cell to create a strand for
   * @param offset the linkage offset of the strand to create
   * @return instance of set of strands containing the upstream strand only
   */
  def createBoxFromCellAndOffset(cell: DsOliCell, offset: Long): DsOliBoxStep = {
    val funSignature = classSignature + "createBoxFromCellAndOffset: "
    DsOliLogger.debug(funSignature + "entered: ")
    val tmpUpstream = new DsOliBoxStep()
    val upstreamCells = new ListBuffer[DsOliCell]()
    upstreamCells += cell
    val upstreamBox = new DsOliBox(offset, upstreamCells)
    DsOliLogger.debug(funSignature + "new box created with id: " + upstreamBox.id + ": " + upstreamBox)
    tmpUpstream.boxes.put(upstreamBox.id, upstreamBox)
    return tmpUpstream
  }

  /**
   * Either use the strand set Option or create a
   * new strand set.
   *
   * @param boxes Option with strand set
   * @param cell the cell to create strand from
   * @param offset the linkage offset for the strand
   * @return instance of strand sets
   */
  def createOrUseBox(boxes: Option[DsOliBoxStep], cell: DsOliCell, offset: Long): DsOliBoxStep = {
    val funSignature = classSignature + "createOrUseBox: "
    DsOliLogger.debug(funSignature + "entered: ")
    return if (boxes.isDefined) {
      boxes.get
    } else {
      createBoxFromCellAndOffset(cell, offset)
    }
  }

  /**
   * Merge two strands. Second strand is appended
   * to first strand
   *
   * @param first the first strand
   * @param second the second strand
   * @param currentBoxes set of strands
   */
  def merge(first: DsOliBox, second: DsOliBox, currentBoxes: DsOliBoxStep): Unit = {
    val funSignature = classSignature + "merge: "
    DsOliLogger.debug(funSignature + "entered: ")
    first match {
      case DsOliBox(_, offset, _, _, 0, 0) =>
        val firstDeepCopy = first.deepCopy(currentBoxes)
        val secondDeepCopy = second.deepCopy(currentBoxes)
        val tmpBox = new DsOliBox(offset, first.cType, firstDeepCopy.cells ++= secondDeepCopy.cells, second.cycleId, second.cycleEntryPoint)
        DsOliLogger.debug(funSignature + "new box created with id: " + tmpBox.id + ": " + tmpBox)
        currentBoxes.boxes.put(tmpBox.id, tmpBox)
      case _ => throw new Exception(funSignature + "first box has a cycle: " + first)
    }
  }

  /**
   * Each upstream strand gets merged with the downstream strand
   *
   * @param upstream the set of upstream strands
   * @param downstreamBox the downstream strand
   * @param currentBoxes the strand set containing all strands
   */
  def mergeBoxes(upstream: DsOliBoxStep, downstreamBox: DsOliBox, currentBoxes: DsOliBoxStep): Unit = {
    val funSignature = classSignature + "mergeBoxes: "
    DsOliLogger.debug(funSignature + "entered: ")

    // Creation
    upstream.boxes.foreach {
      _ match {
        case (boxId, box) =>
          DsOliLogger.debug(funSignature + "merging: upstream box: " + box + ", downstream box: " + downstreamBox)
          merge(box, downstreamBox, currentBoxes)
        case _ =>
      }
    }

  }

  /**
   * Remove all strands, which are left over, i.e,
   * the upstream strands as they got merged and
   * the downstream strand if present.
   *
   * @param upstream the set of upstream strands
   * @param downstreamBox the downstream strand
   * @param currentBoxes the set of strands
   */
  def removeObsoleteBoxes(upstream: DsOliBoxStep, downstreamBox: DsOliBox, currentBoxes: DsOliBoxStep): Unit = {
    val funSignature = classSignature + "removeObsoleteBoxes: "
    DsOliLogger.debug(funSignature + "entered: ")
    // Cleanup of upstream boxes
    upstream.boxes.foreach {
      boxTuple =>
        val (boxId, box) = boxTuple
        currentBoxes.boxes -= boxId
        DsOliLogger.debug(funSignature + "deleting upstream box id: " + boxId)
    }

    // Downstream box might not be present (due to new creation).
    // Only remove if present
    if (currentBoxes.boxes.contains(downstreamBox.id))
      currentBoxes.boxes -= downstreamBox.id
  }
  /**
   * Create a cycle for the source cell and the target cell
   *
   * @param sourceCell the source cell
   * @param targetCell the target cell
   * @param offset the linkage offset
   * @param currentBoxes the strands to search in
   * @return Option the id of the create cycle
   */
  def createCycle(sourceCell: DsOliCell, targetCell: DsOliCell, offset: Long, currentBoxes: DsOliBoxStep): Option[CycleId] = {
    val funSignature = classSignature + "createCycle: "
    DsOliLogger.debug(funSignature + "entered. sourceCell: " + sourceCell + ", targetCell: " + targetCell + ", offset: " + offset)
    currentBoxes.boxes.foreach {
      boxTuple =>
        val (boxId, box) = boxTuple
        box match {
          case DsOliBox(_, boxOffset, _, cells, 0, 0) =>
            if (boxOffset == offset) {
              DsOliLogger.debug(funSignature + "Found box " + box + " without a cycle. cells: " + cells)
              if (box.cells.contains(sourceCell) && box.cells.contains(targetCell)) {
                DsOliLogger.debug(funSignature + "box cells contain source and target cell")
                val newCycle = new DsOliCycle(offset)
                val targetIndexOpt = getCellIndex(targetCell, box)
                val sourceIndexOpt = getCellIndex(sourceCell, box)
                if (targetIndexOpt.isDefined && sourceIndexOpt.isDefined) {
                  val targetIndex = targetIndexOpt.get
                  val sourceIndex = sourceIndexOpt.get
                  DsOliLogger.debug(funSignature + "creating slice: targetIndex: " + targetIndex + ", sourceIndex: " + sourceIndex)
                  val cycleCells = getCellSliceBetweenIndices(box, targetIndex, sourceIndex)
                  newCycle.addCells(cycleCells)
                  currentBoxes.cycles.put(newCycle.id, newCycle)
                  DsOliLogger.debug(funSignature + "new cycle: " + newCycle)
                  return Some(newCycle.id)
                } else {
                  throw new Exception(funSignature + "source/target index not found: " + targetCell + ", " + sourceCell)
                }
              } else {
                DsOliLogger.debug(funSignature + "box cells do not contain source and target cell")
              }
            } else {
              DsOliLogger.debug(funSignature + "offset mismatch")
            }

          case _ =>
            DsOliLogger.debug(funSignature + "Found box with cycle: " + box)
        }
    }
    return None
  }

  /**
   * Remove all cells from the cycle from the strand
   *
   * @param cells the cells of the strand
   * @param cycleCells the cells of the cycle
   * @return Option tuple the start cell and the remaining cells
   */
  def removeCycleElements(cells: ListBuffer[DsOliCell], cycleCells: ListBuffer[DsOliCell]): Option[(DsOliCell, ListBuffer[DsOliCell])] = {
    val funSignature = classSignature + "removeCycleElements: "
    DsOliLogger.debug(funSignature + "entered. cycleCells: " + cycleCells)
    var cellIter = cells.last
    val cellIterIndexOpt = getCellIndexFromList(cellIter, cells)
    var cellIterIndex = cellIterIndexOpt.get
    DsOliLogger.debug(funSignature + "cellIter: " + cellIter)
    var cellInCycle = cycleCells.contains(cellIter)
    DsOliLogger.debug(funSignature + "cellIterIndex: " + cellIterIndex + ", cellInCycle: " + cellInCycle)
    while (cellIterIndex > 0 && cellInCycle) {
      cellIterIndex -= 1
      cellIter = cells(cellIterIndex)
      DsOliLogger.debug(funSignature + "testing: cellIter: " + cellIter)
      cellInCycle = cycleCells.contains(cellIter)
      DsOliLogger.debug(funSignature + "cellIterIndex: " + cellIterIndex + ", cellInCycle: " + cellInCycle)
    }

    if (!cellInCycle) {
      DsOliLogger.debug(funSignature + "constructing entry point with index: " + cellIterIndex)
      Some((cells(cellIterIndex + 1), cells.slice(0, cellIterIndex)))
    } else {
      // Everything was removed
      DsOliLogger.debug(funSignature + "everything was removed (complete cycle)")
      Some((cells(0), new ListBuffer()))
    }

  }

  /**
   * Add a cycle reference to each strand participating in the given cycle
   * and remove the elements of the cycle from the participating strand
   *
   * @param id the cycle id
   * @param currentBoxes the strands to search in
   *
   */
  def compressSequenceIntoCycle(id: CycleId, currentBoxes: DsOliBoxStep): Unit = {
    val funSignature = classSignature + "compressSequenceIntoCycle: "
    DsOliLogger.debug(funSignature + "entered. id " + id)
    val cycleOpt = currentBoxes.cycles.get(id)
    if (cycleOpt.isDefined) {
      val cycle = cycleOpt.get
      currentBoxes.boxes.foreach {
        boxTuple =>
          val (boxId, box) = boxTuple
          box match {
            case DsOliBox(_, boxOffset, _, cells, 0, 0) =>
              if (boxOffset == cycle.offset) {
                DsOliLogger.debug(funSignature + "found box with offset")
                if (cycle.cells.contains(cells.last)) {
                  DsOliLogger.debug(funSignature + "cells.last contained in cycle.cells")
                  val cyclePropertiesOpt = removeCycleElements(cells, cycle.cells)
                  if (cyclePropertiesOpt.isDefined) {
                    val (startCell, remainingCells) = cyclePropertiesOpt.get
                    val newBox = new DsOliBox(box.offset, box.cType, remainingCells, cycle.id, startCell.id)
                    currentBoxes.boxes -= box.id
                    currentBoxes.boxes.put(newBox.id, newBox)
                  }

                } else {
                  DsOliLogger.debug(funSignature + "cells.last is not an element of cycle.cells")
                }
              } else {
                DsOliLogger.debug(funSignature + "offset mismatch")
              }
            case _ =>
              DsOliLogger.debug(funSignature + "no match")
          }
      }
    } else {
      throw new Exception(funSignature + "id not defined: " + id)
    }
  }

  /**
   * Append the cycle to each strand where the last cell is
   * the source cell.
   *
   * @param cycle the cycle to append
   * @param sourceCell the cell to consider
   * @param targetCell the entry point to the cycle
   * @param currentBoxes the strands to search in
   */
  def appendCycle(cycle: DsOliCycle, sourceCell: DsOliCell, targetCell: DsOliCell, currentBoxes: DsOliBoxStep): Unit = {
    val funSignature = classSignature + "appendCycle: "
    DsOliLogger.debug(funSignature + "entered. cycle: " + cycle + ", sourceCell: " + sourceCell + ", targetCell: " + targetCell)
    currentBoxes.boxes.foreach {
      boxTuple =>
        val (boxId, box) = boxTuple
        condOpt(box) {
          case DsOliBox(_, offset, _, cells, 0, 0) =>
            if (offset == cycle.offset && cells.last == sourceCell) {
              DsOliLogger.debug(funSignature + "found box to append cycle to: " + box)
              box.cycleEntryPoint = targetCell.id
              box.cycleId = cycle.id
            }
        }
    }
  }

  /**
   * Fetch the set of upstream strands
   *
   * @param cell the cell to test
   * @param offset the linkage offset
   * @param currentBoxes the strands to search
   * @return Option set of upstream strands
   */
  def getUpstreamBoxes(cell: DsOliCell, offset: Long, currentBoxes: DsOliBoxStep): Option[DsOliBoxStep] = {
    val funSignature = classSignature + "getUpstreamBoxes: "
    DsOliLogger.debug(funSignature + "entered. cell: " + cell + ", offset: " + offset)
    val upstreamBoxes = new DsOliBoxStep()
    currentBoxes.boxes.foreach {
      box =>
        DsOliLogger.debug(funSignature + "testing box id : " + box._1 + ": " + box._2)
        condOpt(box) {
          case (boxId, DsOliBox(_, boxOffset, _, cells, _, _)) =>
            DsOliLogger.debug(funSignature + "testing for offset matches on boxes: " + offset + "==" + box._2.offset + " boxOffset: " + boxOffset)
            // Do some additional tests on the cells: not null and size greater zero 
            // to be able to use the method in the cyclic part of the algorithm
            if (boxOffset == offset && cells != null && cells.size > 0 && cells.last == cell) {
              DsOliLogger.debug(funSignature + "cells.last equals cell.")
              upstreamBoxes.boxes.put(boxId, box._2)
            } else {
              DsOliLogger.debug(funSignature + "offsets do NOT match OR cells.last NOT equal cell.")
            }
        }
    }
    return if (upstreamBoxes.boxes.size > 0) Some(upstreamBoxes) else None
  }

  /**
   * Fetch the downstream strand that starts with the
   * given cell, if there exists one
   *
   * @param targetCell the target cell to search for
   * @param offset the linkage offset
   * @param currentBoxes the strand set
   * @return Option the strand instance
   */
  def getDownstreamBoxIfExists(targetCell: DsOliCell, offset: Long, currentBoxes: DsOliBoxStep): Option[DsOliBox] = {
    val funSignature = classSignature + "getDownstreamBoxIfExists: "
    DsOliLogger.debug(funSignature + "entered.")
    var foundBox: Option[DsOliBox] = None
    return if (currentBoxes.boxes.exists {
      boxTuple =>
        val (boxId, box) = boxTuple
        foundBox = Some(box)
        box.offset == offset && box.cells.length > 0 && box.cells.head == targetCell
    }) {
      DsOliLogger.debug(funSignature + "Found downstream box: " + foundBox.get)
      foundBox
    } else {
      None
    }
  }

  /**
   * Create the downstream strand either from
   * an existing strand containing the target
   * cell or the given target cell
   *
   * @param targetCell the target cell
   * @param offset linkage offset
   * @param currentBoxes strand set to search in
   * @return the strand instance
   */
  def createDownstreamBox(targetCell: DsOliCell, offset: Long, currentBoxes: DsOliBoxStep): DsOliBox = {
    val funSignature = classSignature + "createDownstreamBox: "
    DsOliLogger.debug(funSignature + "entered: ")
    var downstreamBoxOpt: Option[DsOliBox] = None
    breakable {
      currentBoxes.boxes.foreach {
        boxTuple =>
          val (boxId, box) = boxTuple
          condOpt(box) {
            case DsOliBox(_, boxOffset, cType, cells, cycleId, cycleEntryPoint) =>
              if (boxOffset == offset) {
                DsOliLogger.debug(funSignature + "found box with offset")
                if (cells.contains(targetCell)) {
                  DsOliLogger.debug(funSignature + "targetCell contained in cells")
                  val targetCellIndexOpt = getCellIndex(targetCell, box)
                  if (targetCellIndexOpt.isDefined) {
                    val downstreamBox = new DsOliBox(offset, cType, getCellSliceToEnd(box, targetCellIndexOpt.get), cycleId, cycleEntryPoint)
                    downstreamBoxOpt = Some(downstreamBox)
                    break
                  } else {
                    DsOliLogger.error(funSignature)
                  }
                } else {
                  DsOliLogger.debug(funSignature + "targetCell not contained in cells")
                }
              } else {
                DsOliLogger.debug(funSignature + "offset mismatch")
              }

          }
      }
    }
    if (downstreamBoxOpt.isEmpty) {
      val downstreamBoxStep = createOrUseBox(None, targetCell, offset)
      downstreamBoxOpt = Some(downstreamBoxStep.boxes.iterator.next._2)
    }
    return downstreamBoxOpt.get
  }

  /**
   * Construct strands in case of a memory write event
   *
   * @param event the memory write event
   * @param ptg the points-to graph
   * @param currentBoxes the set of strands
   */
  def constructBoxes(event: DsOliMWEvent, ptg: DsOliGraph, currentBoxes: DsOliBoxStep): Unit = {
    val funSignature = classSignature + "constructBoxes: "
    DsOliLogger.debug(funSignature + "entered")
    val sourceAddress = event.lValueList.last.address
    val targetAddress = event.rValue.content

    DsOliLogger.debug(funSignature + "searching for sAddr: " + sourceAddress.toHexString + ", tAddr: " + targetAddress.toHexString)

    val sourceTargetCellsOpt = minCond(sourceAddress, targetAddress, ptg, currentBoxes)
    if (sourceTargetCellsOpt.isDefined) {

      val (sourceCell, targetCell, offset) = sourceTargetCellsOpt.get
      DsOliLogger.debug(funSignature + "minCond is fullfilled. " + sourceCell + ", " + targetCell + ", " + offset)
      if (willBeCyclic(sourceCell, targetCell, offset, currentBoxes)) {
        val cycleIdOpt = createCycle(sourceCell, targetCell, offset, currentBoxes)
        if (cycleIdOpt.isDefined) {
          compressSequenceIntoCycle(cycleIdOpt.get, currentBoxes)
        } else {
          throw new Exception(funSignature + "no cycle id found")
        }
      } else if (isCyclic(targetCell, offset, currentBoxes)) {
        val cycleOpt = getCycle(targetCell, offset, currentBoxes)
        if (cycleOpt.isDefined) {
          DsOliLogger.debug(funSignature + "targetCell is cyclic.")
          val upstreamOpt = getUpstreamBoxes(sourceCell, offset, currentBoxes)
          if (upstreamOpt.isEmpty) {
            DsOliLogger.debug(funSignature + "no box present for source cell. Creating new box.")
            val newSourceBoxStep = createBoxFromCellAndOffset(sourceCell, offset)
            val (newSourceBoxId, newSourceBox) = newSourceBoxStep.boxes.head
            currentBoxes.boxes.put(newSourceBoxId, newSourceBox)
          }
          DsOliLogger.debug(funSignature + "appending cycle.")
          appendCycle(cycleOpt.get, sourceCell, targetCell, currentBoxes)
        } else {
          throw new Exception(funSignature + "no cycle found for targetCell: " + targetCell)
        }
      } else {
        //val (upstreamOpt, downstreamOpt) = segmentBoxesConstruct(sourceCell, targetCell, offset, currentBoxes)
        val upstreamOpt = getUpstreamBoxes(sourceCell, offset, currentBoxes)

        // Create the upstream part, if it doesn't exist
        val upstream = createOrUseBox(upstreamOpt, sourceCell, offset)

        // Try to fetch the downstream box
        val downstreamOpt = getDownstreamBoxIfExists(targetCell, offset, currentBoxes)

        // Create the downstream part if it doesn't exist
        val downstream = if (downstreamOpt.isDefined) downstreamOpt.get else createDownstreamBox(targetCell, offset, currentBoxes)

        mergeBoxes(upstream, downstream, currentBoxes)

        removeObsoleteBoxes(upstream, downstream, currentBoxes)
      }

    } else {
      DsOliLogger.debug(funSignature + "minCond is not fullfilled.")
    }
  }

  /**
   * Check, if the given cycle is referenced
   * by any strand
   *
   * @param currentBoxes the set of strands to search in
   * @param offset the linkage offset
   * @param cycleId the ID of the cycle to search for
   * @return Boolean
   */
  def cycleIsReferenced(currentBoxes: DsOliBoxStep, offset: Long, cycleId: CycleId): Boolean = {
    val funSignature = classSignature + "cycleIsReferenced: "
    DsOliLogger.debug(funSignature + "entered: ")
    currentBoxes.boxes.exists {
      checkBoxTuple =>
        val (checkBoxId, checkBox) = checkBoxTuple
        checkBox match {
          case DsOliBox(_, offset, _, checkCells, boxCycleId, _) =>
            if (boxCycleId == cycleId) {
              DsOliLogger.debug(funSignature + "cycle ids match.")
              if (checkCells != null && checkCells.length != 0) {
                DsOliLogger.debug(funSignature + "found box which references cycle of exclusive cycle box.")
                true
              } else {
                false
              }
            } else {
              DsOliLogger.debug(funSignature + "cycle ids mismatch.")
              false
            }
          case _ => false
        }
    }
  }

  /**
   * Cleanup the strands:
   * - remove the strands which exclusively represent a cycle,
   *   if the cycle is referenced in any other strand
   * - add new strand which exclusively represents a cycle
   *   if the cycle was not referenced in any strand yet
   *
   * @param currentBoxes the strands to clean up
   */
  def cleanupBoxes(currentBoxes: DsOliBoxStep): Unit = {
    val funSignature = classSignature + "cleanupBoxes: "
    DsOliLogger.debug(funSignature + "entered.")

    // Cleanup the boxes which exclusively represent a cycle
    var delBoxes = Set[DsOliBox]()
    currentBoxes.boxes.foreach {
      boxTuple =>
        val (boxId, box) = boxTuple
        box match {
          case DsOliBox(_, offset, _, cells, cycleId, _) =>
            if (cells == null || cells.length == 0) {
              DsOliLogger.debug(funSignature + "found exclusive cycle box.")
              if (cycleIsReferenced(currentBoxes, offset, cycleId)) {
                delBoxes += box
              }
            }
          case _ =>
            DsOliLogger.debug(funSignature + "unmatched box: " + box.id)
        }
    }
    delBoxes.foreach {
      box =>
        DsOliLogger.debug(funSignature + "removing box: " + box)
        currentBoxes.boxes -= box.id
    }

    // Create exclusive cycle boxes for unreferenced cycles
    var addBoxes = Set[DsOliBox]()
    currentBoxes.cycles.foreach {
      cycleTuple =>
        val (cycleId, cycle) = cycleTuple
        DsOliLogger.debug(funSignature + "testing cycle: " + cycle)
        if (!currentBoxes.boxes.exists {
          boxTuple =>
            val (_, box) = boxTuple
            if (box.cycleId == cycleId) true else false
        }) {
          DsOliLogger.debug(funSignature + "found no box with cycle id: " + cycleId)
          val tmpBox = new DsOliBox(cycle.offset, cycle.cells(0).cType, cycle.id, cycle.cells(0).id)
          DsOliLogger.debug(funSignature + "new box created with id: " + tmpBox.id + ": " + tmpBox)
          addBoxes += tmpBox
        }
    }

    addBoxes.foreach {
      box =>
        DsOliLogger.debug(funSignature + "adding box: " + box)
        currentBoxes.boxes.put(box.id, box)
    }
  }

  /**
   * Vertex IDs might change, so update the IDs inside the cells
   *
   * @param currentBoxes the strands to search through
   * @param ptg the points-to graph to search through
   */
  def recalculateCellVertexReferences(currentBoxes: DsOliBoxStep, ptg: DsOliGraph): Unit = {
    val funSignature = classSignature + "recalculateCellVertexReferences: "
    DsOliLogger.debug(funSignature + "entered: ")
    currentBoxes.boxes.foreach {
      boxTuple =>
        val (boxId, box) = boxTuple
        box.cells.foreach {
          cell =>
            ptg.graph.nodes.iterator.foreach {
              nodeOuter =>
                condOpt(nodeOuter.value) {
                  case v: DsOliVertexMemory =>
                    if (v.bAddr <= cell.bAddr && cell.eAddr <= v.eAddr) {
                      DsOliLogger.debug(funSignature + "found vertex.id: " + v.id + " for cell with vertex.id: " + cell.vertexId)
                      cell.vertexId = v.id;
                    }
                }

            }
        }
    }
  }

  /**
   * Remove all strands affected by the free / VLS
   *
   * @param event the free / VLS event
   * @param ptg the points-to graph
   * @param boxes the strands to search in
   */
  def freeVLSForBoxes(event: DsOliEvent, ptg: DsOliGraph, boxes: DsOliBoxStep): Unit = {
    val funSignature = classSignature + "freeVLSForBoxes: "
    DsOliLogger.debug(funSignature + "entered: event: " + event)
    DsOliLogger.debug(funSignature + "\tptg: " + ptg)
    DsOliLogger.debug(funSignature + "\tboxes: " + boxes)
    val addressOpt = DsOliAddressUtils.getStartAddressFromFreeVLS(event)
    if (addressOpt.isDefined) {
      val vOpt = ptg.getVertexForAddress(addressOpt.get)
      if (vOpt.isDefined) {
        val v: DsOliVertexMemory = ptg.getMemoryVertex(vOpt.get)
        val delBoxes = new ListBuffer[DsOliBox]()
        boxes.boxes.foreach {
          boxTuple =>
            val (boxId, box) = boxTuple
            if (box.cells.exists(cell => DsOliAddressUtils.addressInRange(v.bAddr, v.eAddr, cell.bAddr, cell.eAddr))) {
              // At this point all edges should be cut, so just remove the box completely
              delBoxes += box
            }
        }

        // Remove all boxes
        delBoxes.foreach {
          boxes.boxes -= _.id
        }
      } else {
        DsOliLogger.warning(funSignature + "no vertex found for address: " + addressOpt.get.toHexString)
        DsOliLogger.warning(funSignature + "\tevent: " + event)
      }

    }

  }

  /**
   * Iterate through each event of the event trace and
   * calculate the strands for each time step.
   */
  override def createBoxes(): DsOliBoxSteps = {
    val funSignature = classSignature + "createBoxes: ";
    // Store for the calculated strands
    val boxesForSteps = new DsOliBoxSteps()
    // Empty first step
    val stepZero = new DsOliBoxStep()

    DsOliLogger.debug(funSignature + "entered")
    boxesForSteps.append(stepZero)

    // Cycle through all events
    var i = 0
    this.events.events.foreach {
      event =>
        i += 1
        print("Boxes: " + i + "/" + this.events.events.size + "\r")
        DsOliLogger.debug(funSignature + "#Event " + event.id + "# Step " + i + " box event " + event.id + "  ****")
        // Fetch the strands from the previous time step
        val currentBoxesOpt = boxesForSteps.get(i - 1)
        val currentBoxes = if (currentBoxesOpt.isDefined) {
          currentBoxesOpt.get.deepCopy
        } else {
          throw new Exception(funSignature + "Unable to get boxes from previous step: " + i)
        }

        var prevPtg = ptgs.get(i - 1)
        // Do we have artificial events? The artificial events also include the original
        // event which triggered the creation of the artificial events
        if (event.artificialEvents.length != 0) {
          DsOliLogger.debug(funSignature + "event has artificial events: " + event)
          // Get the current ptg, which in case of artificial events
          // is the first graph in the list
          var u = 0
          var curPtg: DsOliGraph = null
          // Process all intermediate artificial events
          event.artificialEvents.foreach {
            artificialEvent =>
              DsOliLogger.debug("\t" + funSignature + "artificial event: " + artificialEvent)
              curPtg = ptgs.get(i).artificialGraphs(u)
              u += 1
              artificialEvent match {
                case e: DsOliArtificialUndefEvent =>
                  DsOliLogger.debug("\t" + funSignature + "artificial undef event: " + e)
                  destructBoxes(e, prevPtg, currentBoxes)
                  cleanupBoxes(currentBoxes)
                case e: DsOliMWEvent =>
                  DsOliLogger.debug("\t" + funSignature + "original artificial memory write event: " + e)
                  destructBoxes(e, prevPtg, currentBoxes)
                  constructBoxes(e, curPtg, currentBoxes)
                  cleanupBoxes(currentBoxes)
                case e: DsOliArtificialFreeEvent =>
                  DsOliLogger.debug("\t" + funSignature + "artificial free event: " + e)
                  freeVLSForBoxes(e, prevPtg, currentBoxes)
                case e: DsOliFreeEvent =>
                  DsOliLogger.debug("\t" + funSignature + "original artificial free event: " + e)
                  freeVLSForBoxes(e, prevPtg, currentBoxes)
                case e: DsOliVLSEvent =>
                  DsOliLogger.debug("\t" + funSignature + "original artificial vls event: " + e)
                  freeVLSForBoxes(e, prevPtg, currentBoxes)
                case _ => throw new Exception(funSignature + "unknown event: " + artificialEvent)
              }
              prevPtg = curPtg

              // Record the artificial boxes
              currentBoxes.artificialBoxes += currentBoxes.deepCopy
          }
        } else {
          var curPtg = ptgs.get(i)
          val currentBoxes01 = currentBoxes.deepCopy

          event match {
            case e: DsOliMWEvent =>
              DsOliLogger.debug(funSignature + "memory write event")
              DsOliLogger.debug(funSignature + "recalculate cell references to vertex, due to new vertex creation")
              recalculateCellVertexReferences(currentBoxes, ptgs.get(i))

              destructBoxes(e, ptgs.get(i - 1), currentBoxes)
              constructBoxes(e, ptgs.get(i), currentBoxes)
              cleanupBoxes(currentBoxes)
              DsOliLogger.debug(funSignature + "finished memory write event")
            case _ => 

          }
        }

        DsOliLogger.debug(funSignature + "createdBoxes: " + currentBoxes)
        boxesForSteps.append(currentBoxes)

        DsOliLogger.debug(funSignature + "#Done Event " + event.id + "# Ending with step " + i + "****\n")

    }
    return boxesForSteps
  }
}