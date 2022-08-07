/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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
import org.chronolens.core.model.RemoveNode
import org.chronolens.core.model.SourceTree
import org.chronolens.core.model.SourceTreeEdit.Companion.apply
import org.chronolens.core.model.SourceTreeNode
import org.chronolens.core.model.walkSourceTree
import org.chronolens.core.repository.Transaction

internal class HistoryAnalyzer(
    private val maxChangeSet: Int,
    private val minRevisions: Int,
    private val minCoupling: Double
) {
    init {
        require(maxChangeSet > 0) { "Invalid change set size '$maxChangeSet'!" }
        require(minRevisions > 0) { "Invalid revisions '$minRevisions'!" }
        require(minCoupling >= 0.0) { "Invalid coupling '$minCoupling'!" }
    }

    private val sourceTree = SourceTree.empty()
    private val changes = hashMapOf<String, Int>()
    private val jointChanges = emptySparseHashMatrix<String, Int>()
    private val temporalCoupling = emptySparseHashMatrix<String, Double>()

    private fun visit(edit: AddNode): Set<String> {
        val addedNodes = edit.sourceTreeNode.walkSourceTree()
        val addedFunctions = addedNodes.filter { (_, node) -> node is Function }
        return addedFunctions.map(SourceTreeNode<*>::qualifiedId).toSet()
    }

    private fun visit(edit: RemoveNode): Set<String> {
        val removedIds =
            sourceTree
                .walk(edit.id)
                .filter { (_, node) -> node is Function }
                .map(SourceTreeNode<*>::qualifiedId)
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
                else -> {}
            }
            sourceTree.apply(edit)
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
                jointChanges[id1, id2] = (jointChanges[id1, id2] ?: 0) + 1
            }
        }
    }

    private fun computeTemporalCoupling(id1: String, id2: String): Double {
        val countId1 = changes[id1] ?: return 0.0
        val countId2 = changes[id2] ?: return 0.0
        val countId1AndId2 = jointChanges[id1, id2] ?: return 0.0
        val countId1OrId2 = countId1 + countId2 - countId1AndId2
        return 1.0 * countId1AndId2 / countId1OrId2
    }

    private fun computeTemporalCoupling() {
        for ((id1, row) in jointChanges.entries) {
            for (id2 in row.keys) {
                temporalCoupling[id1, id2] = computeTemporalCoupling(id1, id2)
            }
        }
    }

    fun analyze(history: Sequence<Transaction>): TemporalContext {
        history.forEach(::analyze)
        computeTemporalCoupling()
        return TemporalContext(changes, jointChanges, temporalCoupling)
            .filter(minRevisions, minCoupling)
    }
}
