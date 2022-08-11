/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.test.core.model

import java.time.Instant
import org.chronolens.core.model.SourceTreeEdit
import org.chronolens.core.model.Transaction
import org.chronolens.core.model.TransactionId
import org.chronolens.test.core.BuilderMarker
import org.chronolens.test.core.Init
import org.chronolens.test.core.apply

@BuilderMarker
public class TransactionBuilder(private val id: String) {
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
        +edit
        return this
    }

    public operator fun SourceTreeEdit.unaryPlus() {
        edits += this
    }

    public fun build(): Transaction = Transaction(TransactionId(id), date, author, edits)
}

public fun transaction(id: String, init: Init<TransactionBuilder>): Transaction =
    TransactionBuilder(id).apply(init).build()
