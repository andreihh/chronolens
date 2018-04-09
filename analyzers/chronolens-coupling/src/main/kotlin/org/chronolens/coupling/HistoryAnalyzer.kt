/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.chronolens.coupling

import org.chronolens.core.model.AddNode
import org.chronolens.core.model.EditFunction
import org.chronolens.core.model.Function
import org.chronolens.core.model.Project
import org.chronolens.core.model.RemoveNode
import org.chronolens.core.model.SourceEntity
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourceNode
import org.chronolens.core.model.sourcePath
import org.chronolens.core.model.walkSourceTree
import org.chronolens.core.repository.Transaction
import org.chronolens.coupling.Graph.Edge
import org.chronolens.coupling.Graph.Node

class HistoryAnalyzer(
    private val maxChangeSet: Int,
    private val minRevisions: Int,
    private val minCoupling: Double
) {

    init {
        require(maxChangeSet > 0) { "Invalid change set '$maxChangeSet'!" }
        require(minRevisions > 0) { "Invalid revisions '$minRevisions'!" }
        require(minCoupling >= 0.0) { "Invalid coupling '$minCoupling'!" }
    }

    private val project = Project.empty()
    private val changes = hashMapOf<String, Int>()
    private val jointChanges = hashMapOf<String, HashMap<String, Int>>()

    private fun getSourcePath(id: String): String =
        project.get<SourceEntity>(id).sourcePath

    private fun visit(edit: AddNode): Set<String> {
        val addedNodes = edit.node.walkSourceTree()
        val addedFunctions = addedNodes.filterIsInstance<Function>()
        return addedFunctions.map(Function::id).toSet()
    }

    private fun visit(edit: RemoveNode): Set<String> {
        val removedNode = project.get<SourceNode>(edit.id)
        val removedIds = removedNode
            .walkSourceTree()
            .filterIsInstance<Function>()
            .map(Function::id)
            .toSet()

        changes -= removedIds
        for (id in removedIds) {
            for (otherId in jointChanges[id].orEmpty().keys) {
                jointChanges[otherId]?.remove(id)
            }
            jointChanges -= id
        }

        return removedIds
    }

    private fun visit(transaction: Transaction): Set<String> {
        val editedIds = hashSetOf<String>()
        for (edit in transaction.edits) {
            when (edit) {
                is AddNode -> editedIds += visit(edit)
                is RemoveNode -> editedIds -= visit(edit)
                is EditFunction -> editedIds += edit.id
            }
            project.apply(edit)
        }
        return editedIds
    }

    private fun analyze(transaction: Transaction) {
        val editedIds = visit(transaction)
        for (id in editedIds) {
            changes[id] = (changes[id] ?: 0) + 1
        }
        if (transaction.changeSet.size > maxChangeSet) return
        for (id1 in editedIds) {
            for (id2 in editedIds) {
                if (id1 == id2) continue
                val jointChangesWithId1 = jointChanges.getOrPut(id1, ::HashMap)
                jointChangesWithId1[id2] = (jointChangesWithId1[id2] ?: 0) + 1
            }
        }
    }

    private fun takeNode(node: Graph.Node): Boolean =
        node.revisions >= minRevisions

    private fun takeEdge(edge: Graph.Edge): Boolean =
        edge.revisions >= minRevisions && edge.coupling >= minCoupling

    private fun makeEdge(id1: String, id2: String, revisions: Int): Graph.Edge {
        val countId1 = changes.getValue(id1)
        val countId2 = changes.getValue(id2)
        val totalCount = countId1 + countId2 - revisions
        val coupling = 1.0 * revisions / totalCount
        return Edge(id1, id2, revisions, coupling)
    }

    private fun aggregate(): List<Graph> {
        val idsByFile = changes.keys.groupBy(::getSourcePath)
        val sourcePaths = project.sources.map(SourceFile::path)
        return sourcePaths.map { path ->
            val ids = idsByFile[path].orEmpty()
            val edges = ids.flatMap { id1 ->
                jointChanges[id1].orEmpty()
                    .filter { (id2, _) -> id1 < id2 }
                    .map { (id2, revisions) -> makeEdge(id1, id2, revisions) }
            }
            val nodes = (ids + edges.getEndpoints())
                .map { id -> Node(id, changes.getValue(id)) }
                .toSet()
            Graph(path, nodes, edges)
                .filterNodes(::takeNode)
                .filterEdges(::takeEdge)
        }
    }

    fun analyze(history: Iterable<Transaction>): Report {
        history.forEach(::analyze)
        return Report(aggregate())
    }

    data class Report(val graphs: List<Graph>)
}
