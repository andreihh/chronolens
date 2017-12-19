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

package org.metanalysis.test.core.repository

import org.metanalysis.core.model.Project
import org.metanalysis.core.model.SourceNode.SourceUnit
import org.metanalysis.core.repository.Repository
import org.metanalysis.core.repository.Transaction

class RepositoryBuilder {
    private val history = arrayListOf<Transaction>()
    private val snapshot = Project.empty()

    fun transaction(id: String, init: TransactionBuilder.() -> Unit) {
        val transaction = TransactionBuilder(id).apply(init).build()
        history += transaction
        snapshot.apply(transaction.edits)
    }

    fun build(): Repository = object : Repository {
        init {
            check(history.isNotEmpty())
        }

        override fun getHeadId(): String = history.last().id

        override fun getSourceUnit(path: String): SourceUnit? =
            snapshot.find<SourceUnit>(path)

        override fun listSources(): Set<String> =
                snapshot.units.map(SourceUnit::path).toSet()

        override fun getHistory(): Iterable<Transaction> = history
    }
}
