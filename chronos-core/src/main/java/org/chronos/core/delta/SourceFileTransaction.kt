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

package org.chronos.core.delta

import org.chronos.core.SourceFile
import org.chronos.core.delta.NodeSetEdit.Companion.apply
import org.chronos.core.delta.NodeSetEdit.Companion.diff

/**
 * A transaction which should be applied to a [SourceFile].
 *
 * @property nodeEdits the list of edits which should be applied to the set of
 * `nodes`
 */
data class SourceFileTransaction(
        val nodeEdits: List<NodeSetEdit>
) : Transaction<SourceFile> {
    companion object {
        /**
         * Returns the transaction which should be applied on this source file
         * to obtain the `other` source file, or `null` if they are identical.
         *
         * @param other the source file which should be obtained
         * @return the transaction which should be applied on this source file
         */
        fun SourceFile.diff(other: SourceFile): SourceFileTransaction? {
            val nodeEdits = nodes.diff(other.nodes)
            return if (nodeEdits.isNotEmpty()) SourceFileTransaction(nodeEdits)
            else null
        }
    }

    override fun applyOn(subject: SourceFile): SourceFile =
            subject.copy(nodes = subject.nodes.apply(nodeEdits))
}
