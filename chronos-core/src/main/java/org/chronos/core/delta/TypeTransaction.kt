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

import org.chronos.core.Node.Type
import org.chronos.core.delta.NodeSetEdit.Companion.apply
import org.chronos.core.delta.NodeSetEdit.Companion.diff
import org.chronos.core.delta.SetEdit.Companion.apply
import org.chronos.core.delta.SetEdit.Companion.diff

/**
 * A transaction which should be applied to a [Type].
 *
 * @property supertypeEdits the list of edits which should be applied to the
 * `supertypes`
 * @property memberEdits the list of edits which should be applied to the
 * `members`
 */
data class TypeTransaction(
        val supertypeEdits: List<SetEdit<String>> = emptyList(),
        val memberEdits: List<NodeSetEdit> = emptyList()
) : Transaction<Type> {
    companion object {
        /**
         * Returns the transaction which should be applied on this type to
         * obtain the `other` type, or `null` if they are identical.
         *
         * @param other the type which should be obtained
         * @return the transaction which should be applied on this type
         * @throws IllegalArgumentException if the given types have different
         * identifiers
         */
        fun Type.diff(other: Type): TypeTransaction? {
            require(identifier == other.identifier)
            val supertypeEdits = supertypes.diff(other.supertypes)
            val memberEdits = members.diff(other.members)
            return if (supertypeEdits.isNotEmpty() || memberEdits.isNotEmpty())
                TypeTransaction(supertypeEdits, memberEdits)
            else null
        }
    }

    override fun applyOn(subject: Type): Type = subject.copy(
            supertypes = subject.supertypes.apply(supertypeEdits),
            members = subject.members.apply(memberEdits)
    )
}
