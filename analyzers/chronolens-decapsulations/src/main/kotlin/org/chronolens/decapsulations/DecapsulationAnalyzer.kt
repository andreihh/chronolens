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

package org.chronolens.decapsulations

import org.chronolens.core.model.Project
import org.chronolens.core.model.sourcePath
import java.util.ServiceLoader

internal abstract class DecapsulationAnalyzer {
    protected abstract fun canProcess(sourcePath: String): Boolean

    protected abstract fun getField(project: Project, nodeId: String): String?

    protected abstract fun getVisibility(project: Project, nodeId: String): Int

    protected abstract fun isConstant(project: Project, nodeId: String): Boolean

    companion object {
        private val analyzers =
            ServiceLoader.load(DecapsulationAnalyzer::class.java)

        private fun findAnalyzer(
            project: Project,
            id: String,
        ): DecapsulationAnalyzer? {
            val sourcePath = project[id]?.sourcePath ?: return null
            return analyzers.find { it.canProcess(sourcePath) }
        }

        fun getField(project: Project, nodeId: String): String? =
            findAnalyzer(project, nodeId)?.getField(project, nodeId)

        fun getVisibility(project: Project, nodeId: String): Int? =
            findAnalyzer(project, nodeId)?.getVisibility(project, nodeId)

        fun isConstant(project: Project, nodeId: String): Boolean =
            findAnalyzer(project, nodeId)?.isConstant(project, nodeId) == true
    }
}
