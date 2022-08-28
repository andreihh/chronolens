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

package org.chronolens.test.core.versioning

import java.time.Instant
import org.chronolens.core.versioning.VcsProxy
import org.chronolens.core.versioning.VcsRevision

internal class FakeVcsProxy(revisions: List<VcsChangeSet>) : VcsProxy {
    private val history = mutableListOf<VcsRevision>()
    private val changeSets = mutableMapOf<String, VcsChangeSet>()
    private val snapshots = mutableMapOf<String, Map<String, String>>()

    init {
        val snapshot = mutableMapOf<String, String>()
        for ((index, changeSet) in revisions.withIndex()) {
            val revisionId = index.toString()
            val date = Instant.ofEpochMilli(1000L * index)
            history += VcsRevision(revisionId, date, author = "t@test.com")
            for ((path, content) in changeSet) {
                if (content == null) {
                    snapshot -= path
                } else {
                    snapshot[path] = content
                }
            }
            changeSets[revisionId] = changeSet
            snapshots[revisionId] = snapshot.toMap()
        }
    }

    override fun getHead(): VcsRevision =
        history.lastOrNull() ?: error("Repository must not be empty!")

    override fun getRevision(revisionId: String): VcsRevision? =
        history.find { it.id == revisionId }

    override fun listFiles(revisionId: String): Set<String> =
        requireNotNull(snapshots[revisionId]).keys

    override fun getFile(revisionId: String, path: String): String? =
        requireNotNull(snapshots[revisionId])[path]

    override fun getChangeSet(revisionId: String): Set<String> =
        requireNotNull(changeSets[revisionId]).keys

    override fun getHistory(revisionId: String, path: String): List<VcsRevision> {
        require(revisionId in changeSets)
        return history
            .dropLastWhile { it.id != revisionId }
            .filter { changeSets.getValue(it.id).touches(path) }
    }
}
