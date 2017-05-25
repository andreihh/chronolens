/*
 * Copyright 2017 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.metanalysis.test.core.project

import org.metanalysis.core.delta.Transaction.Companion.apply
import org.metanalysis.core.model.SourceFile
import org.metanalysis.core.project.Project
import org.metanalysis.test.core.model.assertEquals

import java.io.FileNotFoundException

import kotlin.test.assertEquals as assertEqualsKt

class ProjectMock(
        private val files: Set<String>,
        private val models: Map<String, SourceFile>,
        private val histories: Map<String, List<HistoryEntry>>
) : Project() {
    init {
        assertEqualsKt(files, models.keys)
        assertEqualsKt(files, histories.keys)
        histories.forEach { path, history ->
            val transactions = history.map(HistoryEntry::transaction)
                    .filterNotNull()
            val sourceFile = SourceFile().apply(transactions)
            assertEquals(models[path], sourceFile)
        }
    }

    override fun listFiles(): Set<String> = files

    override fun getFileModel(path: String): SourceFile =
            models[path] ?: throw FileNotFoundException("'$path' not found!")

    override fun getFileHistory(path: String): List<HistoryEntry> =
            histories[path] ?: throw FileNotFoundException("'$path' not found!")
}
