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

/**
 * A change which should be applied to a [SourceFile].
 *
 * @property nodeChanges the list of changes which should be applied to the set
 * of `nodes`
 */
data class SourceFileChange(
        val nodeChanges: List<NodeChange>
) : Change<SourceFile> {
    override fun applyOn(subject: SourceFile): SourceFile =
            subject.copy(nodes = subject.nodes.apply(nodeChanges))
}
