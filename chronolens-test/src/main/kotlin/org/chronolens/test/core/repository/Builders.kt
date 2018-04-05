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

import org.chronolens.core.repository.Repository
import org.chronolens.core.repository.Transaction
import org.chronolens.test.core.Init
import org.chronolens.test.core.apply

fun transaction(
    revisionId: String,
    init: Init<TransactionBuilder>
): Transaction = TransactionBuilder(revisionId).apply(init).build()

fun repository(init: Init<RepositoryBuilder>): Repository =
    RepositoryBuilder().apply(init).build()
