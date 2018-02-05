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
import org.metanalysis.core.model.SourceUnit
import org.metanalysis.core.repository.Repository
import org.metanalysis.core.repository.Transaction
import org.metanalysis.test.core.Init
import org.metanalysis.test.core.apply

class RepositoryBuilder {
    private val history = arrayListOf<Transaction>()
    private val snapshot = Project.empty()

    fun transaction(
        revisionId: String,
        init: Init<TransactionBuilder>
    ): RepositoryBuilder {
        val transaction = TransactionBuilder(revisionId).apply(init).build()
        history += transaction
        snapshot.apply(transaction.edits)
        return this
    }

    fun build(): Repository = object : Repository {
        init {
            check(history.isNotEmpty())
        }

        override fun getHeadId(): String = history.last().revisionId

        override fun listSources(): Set<String> =
            snapshot.sources.map(SourceUnit::path).toSet()

        override fun listRevisions(): List<String> =
            history.map(Transaction::revisionId)

        override fun getSource(path: String): SourceUnit? =
            snapshot.get<SourceUnit?>(path)

        override fun getHistory(): Iterable<Transaction> = history
    }
}
