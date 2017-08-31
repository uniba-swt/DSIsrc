
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
 * DsOliGraph.scala created on Oct 9, 2014
 *
 * Description: A graph
 */
package pointstograph

import scalax.collection.mutable.Graph
import scala.collection.mutable.HashMap
import pointstograph.DsOliDiEdge._
import scalax.collection.GraphPredef._
import scalax.collection.GraphEdge._
import extlogger._
import scala.collection.mutable.ListBuffer

class DsOliGraph(val ptgs: DsOliPointsToGraphs) {
  var graph = Graph[DsOliVertex, DsOliDiEdge]()

  // Keep track of the last used node / edge
  var lastEdge: DsOliDiEdge[DsOliVertex] = null
  var lastNode: DsOliVertex = null

  // Storage for graphs created by artificial events
  var artificialGraphs = new ListBuffer[DsOliGraph]()

  /**
   * Add a new edge to the graph
   *
   * @param edge the edge to add
   * @return Boolean if the edge was added
   */
  def add(edge: DsOliDiEdge[DsOliVertex]): Boolean = {
    val retVal = graph.add(edge)
    if (retVal) {
      DsOliLogger.debug("DsOliGraph::add: recording last edge. lastEdge prev = " + lastEdge)
      lastEdge = edge
      DsOliLogger.debug("DsOliGraph::add: recording last edge. lastEdge cur  = " + lastEdge)

      val sourceId = edge.source.id
      val targetId = edge.target.id
      ptgs.idToNode.put(sourceId, edge.source)
      ptgs.idToNode.put(targetId, edge.target)
    }
    return retVal
  }

  /**
   * Add a new node to the graph
   *
   * @param node the vertex to add
   * @return Boolean if the node was added
   */
  def add(node: DsOliVertex): Boolean = {
    val retVal = graph.add(node)
    if (retVal) {
      // Keep track of the last added node
      lastNode = node
      ptgs.idToNode.put(node.id, node)
    }
    return retVal
  }

  /**
   * Fetch a vertex by its id
   *
   * @param id the id of the vertex
   * @return Option the vertex
   */
  def get(id: Long): Option[DsOliVertex] = {
    val node = if (ptgs.idToNode.contains(id)) {
      Some(graph.get(ptgs.idToNode.get(id).get).value)
    } else {
      None
    }
    return node
  }

  /**
   * Do a deep copy of the graph
   */
  def deepCopy(): DsOliGraph = {
    var newGraph = new DsOliGraph(ptgs)

    DsOliLogger.debug("DsOliGraph::deepCopy: nodes")
    val iter = graph.nodes.iterator
    while (iter.hasNext) {
      val next = iter.next.value
      val curNode = next match {
        // Predefined vertices are reused
        case n: DsOliVertexPredefined =>
          DsOliLogger.debug("DsOliGraph::deepCopy: Predefined vertex: " + n)
          n
        case n: DsOliVertexMemory =>
          DsOliLogger.debug("DsOliGraph::deepCopy: Memory vertex: " + n)
          new DsOliVertexMemory(n.bAddr, n.eAddr, n.vType, n.id)
        case n =>
          DsOliLogger.debug("DsOliGraph::deepCopy: Default: " + n);
          n
      }

      newGraph.graph.add(curNode)
      newGraph.ptgs.idToNode.put(next.id, curNode)
    }

    DsOliLogger.debug("DsOliGraph::deepCopy: edges")
    val edgeIter = graph.edges.iterator
    while (edgeIter.hasNext) {
      val next = edgeIter.next.edge
      val source = next.source.value
      val target = next.target.value
      DsOliLogger.debug("DsOliGraph::deepCopy: source " + source + "," + source.id + "; target: " + target + "," + target.id)

      if (newGraph.ptgs.idToNode.contains(source.id) && newGraph.ptgs.idToNode.contains(target.id)) {
        val sourceVertex = newGraph.ptgs.idToNode.get(source.id).get
        val targetVertex = newGraph.ptgs.idToNode.get(target.id).get
        val edge = sourceVertex ~> targetVertex toDsOliDiEdge (next.id, next.sAddr, next.sOffset, next.tAddr, next.tOffset)
        newGraph.graph.add(edge)
      }

    }
    return newGraph
  }

  /**
   * Calculate the current target address of a given source address,
   * i.e, find the edge for the given source address where there
   * can exist only one.
   *
   * @param sAddr the source address to search for
   * @return Option of the corresponding target address
   */
  def getCurrentTargetAddr(sAddr: Long): Option[Long] = {

    DsOliLogger.debug("DsOliGraph::getCurrentTargetAddr: searching for sAddr: " + sAddr.toHexString)

    // Fetch the vertex for the source address or return
    val sourceVertexOpt = this.getVertexForAddress(sAddr)
    val retVal = try {
      val sourceVertex = if (sourceVertexOpt.isDefined) sourceVertexOpt.get else throw new Exception("Source vertex not found")
      val sOffset = sourceVertex match {
        case s: DsOliVertexMemory => sAddr - s.bAddr
        case _ => throw new Exception("Source is not a memory vertex")
      }

      DsOliLogger.debug("DsOliGraph::getCurrentTargetAddr: sOffset: " + sOffset.toHexString)
      val edgeIter = this.graph.edges.iterator
      var foundEdge = false
      var tAddr: Long = 0
      while (foundEdge == false && edgeIter.hasNext) {
        val edge = edgeIter.next.edge

        DsOliLogger.debug("DsOliGraph::getCurrentTargetAddr: testing against " + edge.sAddr.toHexString + " && edge.sOffset " + edge.sOffset.toHexString)
        //if (edge.sAddr == sAddr && edge.sOffset == sOffset) {
        if (edge.sAddr + edge.sOffset == sAddr) {
          tAddr = edge.tAddr + edge.tOffset
          foundEdge = true
          DsOliLogger.debug("DsOliGraph::getCurrentTargetAddr: found edge and tAddr: " + tAddr.toHexString)
        }
      }
      if (foundEdge) {
        Some(tAddr)
      } else {
        DsOliLogger.debug("DsOliGraph::getCurrentTargetAddr: no edge found")
        None
      }
    } catch {
      case e: Exception =>
        DsOliLogger.debug("DsOliGraph::getCurrentTargetAddr: " + e.getMessage() + "; sAddr: " + sAddr.toHexString)
        None
    }
    return retVal
  }

  /**
   * Helper to cast from vertex to memory vertex
   *
   * @param vertex the vertex to cast
   * @returns instance of a memory vertex
   */
  def getMemoryVertex(vertex: DsOliVertex): DsOliVertexMemory = {
    vertex match {
      case v: DsOliVertexMemory => v
      case _ => throw new Exception("Not a memory vertex")
    }
  }

  /**
   * Search for the corresponding vertex for a given address
   *
   * @param address the address to search for
   * @returns instance of the vertex containing the address
   */
  def getVertexForAddress(address: Long): Option[DsOliVertex] = {
    // Iterate through all nodes
    val iter = graph.nodes.iterator
    while (iter.hasNext) {
      val next = iter.next.value
      next match {
        case n: DsOliVertexMemory =>
          if (n.bAddr <= address && address <= n.eAddr) {
            return Some(n)
          } case _ =>
      }
    }
    return None
  }
}

object DsOliGraph {
  // These nodes are common for all graphs: NULL and undefined
  val vertexNull = new DsOliVertexNull(0)
  val vertexUndef = new DsOliVertexUndef(1)
}