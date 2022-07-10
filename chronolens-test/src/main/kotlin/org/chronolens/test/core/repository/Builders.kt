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

@file:JvmName("Builders")

package org.chronolens.test.core.repository

import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourceTree
import org.chronolens.core.model.SourceTreeEdit
import org.chronolens.core.model.apply
import org.chronolens.core.repository.Repository
import org.chronolens.core.repository.Transaction
import org.chronolens.test.core.BuilderMarker
import org.chronolens.test.core.Init
import org.chronolens.test.core.apply
import java.time.Instant

public fun transaction(
    revisionId: String,
    init: Init<TransactionBuilder>
): Transaction = TransactionBuilder(revisionId).apply(init).build()

public fun repository(init: Init<RepositoryBuilder>): Repository =
    RepositoryBuilder().apply(init).build()

@BuilderMarker
public class TransactionBuilder(private val revisionId: String) {
    public var date: Instant = Instant.now()
    public var author: String = "<unknown-author>"
    private val edits = mutableListOf<SourceTreeEdit>()

    public fun date(value: Instant): TransactionBuilder {
        date = value
        return this
    }

    public fun author(value: String): TransactionBuilder {
        author = value
        return this
    }

    public fun edit(edit: SourceTreeEdit): TransactionBuilder {
        edits += edit
        return this
    }

    public operator fun SourceTreeEdit.unaryPlus() {
        edits += this
    }

    public fun build(): Transaction =
        Transaction(revisionId, date, author, edits)
}

@BuilderMarker
public class RepositoryBuilder {
    private val history = mutableListOf<Transaction>()
    private val snapshot = SourceTree.empty()

    public fun transaction(
        revisionId: String,
        init: Init<TransactionBuilder>
    ): RepositoryBuilder {
        val transaction = TransactionBuilder(revisionId).apply(init).build()
        history += transaction
        snapshot.apply(transaction.edits)
        return this
    }

    public fun build(): Repository = object : Repository {
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

        override fun getHistory(): Sequence<Transaction> = history.asSequence()
    }
}
