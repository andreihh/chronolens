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

package org.chronolens.core.repository

import org.chronolens.core.model.Revision
import org.chronolens.core.repository.Repository.HistoryProgressListener
import java.io.IOException

public interface RepositoryStorage {
    @Throws(IOException::class)
    public fun readHistoryIds(): List<String>

    @Throws(IOException::class)
    public fun readHistory(): Sequence<Revision>

    @Throws(IOException::class)
    public fun writeHistory(revisions: Sequence<Revision>)
}

/**
 * Writes the history of [this] repository to the given [storage] and reports the progress to
 * the given [listener].
 *
 * @throws CorruptedRepositoryException if the repository is corrupted
 * @throws IOException if any I/O errors occur
 */
@Throws(IOException::class)
public fun Repository.persist(
    storage: RepositoryStorage,
    listener: HistoryProgressListener? = null
): Repository {
    storage.writeHistory(getHistory(listener))
    return PersistentRepository(storage)
}
