
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
 * DsOliGraphCleaner.scala created on Nov 20, 2015
 *
 * Description: Clean the graph to produce one edge per bi-directional edge,
 * as bi-directional edges are represented with two edges during computation
 */
package dsnaming

import scala.collection.mutable.HashMap
import dsnaming.DsOliConConfTag._
import util.DsOliGraphUtils
import scalax.collection.edge.LUnDiEdge
import scalax.collection.edge.Implicits._
import scalax.collection.mutable.Graph
import scalax.collection.GraphEdge.EdgeLike
import scalax.collection.GraphEdge.UnDiEdge
import dsnaming.DsOliConConfClassificationTag._
import extlogger.DsOliLogger

/**
 * @author DSI
 *
 */
object DsOliGraphCleaner {

  /**
   * Create a textual representation of the nesting on overlay 
   * direction of the edge, i.e, from which source strand to which target strand
   * 
   * @param edge the edge to inspect
   * 
   * @return the string representation indicating the nesting direction
   */
  def collectEdgeInformation(edge: DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex]): String = {
    var retStr = ""
    var foundOvlNesting = false
    var foundPtrNesting = false
    edge.conConfClass.foreach {
      cc =>
        // Only indicate the overlay nesting direction
        if (foundOvlNesting == false && cc.classification == No) {
          val (src, tgt) = DsOliGraphUtils.sourceTargetStrHelper(edge)
          retStr += src + "-&gt;" + tgt
          foundOvlNesting = true
        } //retStr += ":" + cc.evidence  + "\n"
    }
    if (retStr.size == 0) "-" else retStr
  }

  /**
   * Fetch the labels of the bi-directional edges, e.g., overlay nesting is
   * might be only on one edge of the two to indicate the direction
   * 
   * @param edgeConConf the connection configuration of the edge
   * @param corEdgeConf the connection configuration of the corresponding edge
   * @return the unioned configurations
   */
  def uniqueLabels(edgeConConf: scala.collection.mutable.Set[DsOliConConfClassification], corEdgeConConf: scala.collection.mutable.Set[DsOliConConfClassification]): scala.collection.mutable.Set[DsOliConConfClassification] = {
    var first = scala.collection.mutable.Set[DsOliConConfClassification]()
    // Check all labels in the corresponding edge against the initial edge: if not present in initial edge, add to cor edge
    corEdgeConConf.foreach {
      conConf =>
        if (!edgeConConf.exists(firstConConf => firstConConf.classification == conConf.classification && firstConConf.evidence == conConf.evidence)) {
          first.add(conConf)
        }
    }
    // Return the union of the newly created set and the initial set
    edgeConConf.union(first)
  }

  /**
   * Merge bi-directional edges to one edge for displaying to the end user
   *
   * @param graph the graph to process
   * @return the merged graph
   */
  def cleanGraph(graph: DsOliMetaBoxGraph): Graph[DsOliMetaBoxGraphVertex, UnDiEdge] = {
    val ret = Graph[DsOliMetaBoxGraphVertex, UnDiEdge]()
    graph.graph.nodes.foreach {
      node =>
        ret.add(node)
    }

    // Keep track of the processed edges
    val processedEdges = new HashMap[DsOliMetaBoxGraphDiEdge[DsOliMetaBoxGraphVertex], Boolean]()

    // Cycle through all edges
    graph.graph.edges.iterator.foreach {
      edgeInner =>
        val edgeOuter = edgeInner.toOuter

        // Prevent double processing of bi-directional edges
        if (!processedEdges.contains(edgeOuter)) {

          // EPs are always directed
          if (!edgeOuter.source.isInstanceOf[DsOliMetaBoxGraphVertexEP]) {

            // If corresponding edge exists we found a bi-directional edge
            if (DsOliGraphUtils.isBidirectionalEdge(edgeOuter, graph)) {

              // add LUnDi
              val corEdge = DsOliGraphUtils.getCorrespondingBiDiEdge(edgeOuter, graph)
              processedEdges.put(corEdge, true)

              var edgeLabel = "<<TABLE BORDER=\"0\" ALIGN=\"left\" BGCOLOR=\"#CCCCCC\" COLOR=\"black\" CELLBORDER=\"0\" CELLPADDING=\"0\" CELLSPACING=\"0\"><TR><TD COLSPAN=\"2\">t: " + edgeOuter.startPoint + " tag: " + (if (edgeOuter.conConf.tag == ccOverlay) "ovl" else "ptr") + " off: " + edgeOuter.conConf.offsets._1 + ":" + edgeOuter.conConf.offsets._2 + "</TD></TR>"

              // Collect the edge information and create the unique labels
              val edgeOvlNesting = collectEdgeInformation(edgeOuter)
              val corEdgeOvlNesting = collectEdgeInformation(corEdge)
              if (edgeOvlNesting.length > 1 || corEdgeOvlNesting.length > 1) {
                edgeLabel += "<TR><TD COLSPAN=\"2\"> N: " + edgeOvlNesting + ", " + corEdgeOvlNesting + "</TD></TR>"
              }
              val edgeClasses = uniqueLabels(edgeOuter.conConfClass, corEdge.conConfClass)
              edgeLabel += DsOliGraphUtils.compactCCClass(edgeClasses)
              edgeLabel += "</TABLE>>"

              // Save the bi-directional edge into the graph
              val LUnDi = LUnDiEdge(edgeOuter.source, edgeOuter.target)((edgeLabel, edgeClasses))
              ret.add(LUnDi)
            } else {
              // add directed edge into the graph
              ret.add(edgeOuter)
            }
          } else {
            // add directed edge into the graph
            ret.add(edgeOuter)
          }
          
          // Keep track of edges
          processedEdges.put(edgeOuter, true)
        }
    }
    ret
  }
}