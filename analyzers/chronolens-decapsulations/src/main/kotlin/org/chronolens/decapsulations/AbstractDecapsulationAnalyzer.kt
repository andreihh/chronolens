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

package org.chronolens.decapsulations

import java.util.ServiceLoader
import org.chronolens.core.model.QualifiedSourceNodeId
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.SourceTree
import org.chronolens.core.model.Variable

internal abstract class AbstractDecapsulationAnalyzer {
    protected abstract fun canProcess(sourcePath: SourcePath): Boolean

    protected abstract fun getField(
        sourceTree: SourceTree,
        nodeId: QualifiedSourceNodeId<*>
    ): QualifiedSourceNodeId<Variable>?

    protected abstract fun getVisibility(
        sourceTree: SourceTree,
        nodeId: QualifiedSourceNodeId<*>
    ): Int

    protected abstract fun isConstant(
        sourceTree: SourceTree,
        nodeId: QualifiedSourceNodeId<*>
    ): Boolean

    companion object {
        private val analyzers = ServiceLoader.load(AbstractDecapsulationAnalyzer::class.java)

        private fun findAnalyzer(
            sourceTree: SourceTree,
            id: QualifiedSourceNodeId<*>,
        ): AbstractDecapsulationAnalyzer? {
            if (id !in sourceTree) return null
            val sourcePath = id.sourcePath
            return analyzers.find { it.canProcess(sourcePath) }
        }

        fun getField(
            sourceTree: SourceTree,
            nodeId: QualifiedSourceNodeId<*>
        ): QualifiedSourceNodeId<Variable>? =
            findAnalyzer(sourceTree, nodeId)?.getField(sourceTree, nodeId)

        fun getVisibility(sourceTree: SourceTree, nodeId: QualifiedSourceNodeId<*>): Int? =
            findAnalyzer(sourceTree, nodeId)?.getVisibility(sourceTree, nodeId)

        fun isConstant(sourceTree: SourceTree, nodeId: QualifiedSourceNodeId<*>): Boolean =
            findAnalyzer(sourceTree, nodeId)?.isConstant(sourceTree, nodeId) == true
    }
}
