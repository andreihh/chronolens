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

@file:JvmName("Builders")

package org.chronolens.test.core.repository

import org.chronolens.core.model.Project
import org.chronolens.core.model.ProjectEdit
import org.chronolens.core.model.SourceFile
import org.chronolens.core.repository.Repository
import org.chronolens.core.repository.Transaction
import org.chronolens.test.core.BuilderMarker
import org.chronolens.test.core.Init
import org.chronolens.test.core.apply
import java.time.Instant

fun transaction(
    revisionId: String,
    init: Init<TransactionBuilder>
): Transaction = TransactionBuilder(revisionId).apply(init).build()

fun repository(init: Init<RepositoryBuilder>): Repository =
    RepositoryBuilder().apply(init).build()

@BuilderMarker
class TransactionBuilder(private val revisionId: String) {
    var date: Instant = Instant.now()
    var author: String = "<unknown-author>"
    private val edits = mutableListOf<ProjectEdit>()

    fun date(value: Instant): TransactionBuilder {
        date = value
        return this
    }

    fun author(value: String): TransactionBuilder {
        author = value
        return this
    }

    fun edit(edit: ProjectEdit): TransactionBuilder {
        edits += edit
        return this
    }

    operator fun ProjectEdit.unaryPlus() {
        edits += this
    }

    fun build(): Transaction = Transaction(revisionId, date, author, edits)
}

@BuilderMarker
class RepositoryBuilder {
    private val history = mutableListOf<Transaction>()
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
            snapshot.sources.map(SourceFile::path).toSet()

        override fun listRevisions(): List<String> =
            history.map(Transaction::revisionId)

        override fun getSource(path: String): SourceFile? =
            snapshot.get<SourceFile?>(path)

        override fun getHistory(): Iterable<Transaction> = history
    }
}
