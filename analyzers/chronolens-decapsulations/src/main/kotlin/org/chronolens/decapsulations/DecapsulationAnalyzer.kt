/*
 * Copyright 2018-2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.model.SourceTree
import org.chronolens.core.model.sourcePath
import java.util.ServiceLoader

internal abstract class DecapsulationAnalyzer {
    protected abstract fun canProcess(sourcePath: String): Boolean

    protected abstract fun getField(sourceTree: SourceTree, nodeId: String): String?

    protected abstract fun getVisibility(sourceTree: SourceTree, nodeId: String): Int

    protected abstract fun isConstant(sourceTree: SourceTree, nodeId: String): Boolean

    companion object {
        private val analyzers =
            ServiceLoader.load(DecapsulationAnalyzer::class.java)

        private fun findAnalyzer(
            sourceTree: SourceTree,
            id: String,
        ): DecapsulationAnalyzer? {
            if (id !in sourceTree) return null
            val sourcePath = id.sourcePath
            return analyzers.find { it.canProcess(sourcePath) }
        }

        fun getField(sourceTree: SourceTree, nodeId: String): String? =
            findAnalyzer(sourceTree, nodeId)?.getField(sourceTree, nodeId)

        fun getVisibility(sourceTree: SourceTree, nodeId: String): Int? =
            findAnalyzer(sourceTree, nodeId)?.getVisibility(sourceTree, nodeId)

        fun isConstant(sourceTree: SourceTree, nodeId: String): Boolean =
            findAnalyzer(sourceTree, nodeId)?.isConstant(sourceTree, nodeId) == true
    }
}
