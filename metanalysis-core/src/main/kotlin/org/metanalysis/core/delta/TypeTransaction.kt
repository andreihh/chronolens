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

package org.metanalysis.core.delta

import org.metanalysis.core.delta.NodeSetEdit.Companion.apply
import org.metanalysis.core.delta.NodeSetEdit.Companion.diff
import org.metanalysis.core.delta.SetEdit.Companion.apply
import org.metanalysis.core.delta.SetEdit.Companion.diff
import org.metanalysis.core.model.Node.Type

/**
 * A transaction which should be applied to a [Type].
 *
 * @property supertypeEdits the edits which should be applied to the
 * `supertypes`
 * @property modifierEdits the edits which should be applied to the `modifiers`
 * @property memberEdits the edits which should be applied to the `members`
 */
data class TypeTransaction(
        val supertypeEdits: List<SetEdit<String>> = emptyList(),
        val modifierEdits: List<SetEdit<String>> = emptyList(),
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
        @JvmStatic fun Type.diff(other: Type): TypeTransaction? {
            require(identifier == other.identifier)
            val supertypeEdits = supertypes.diff(other.supertypes)
            val modifierEdits = modifiers.diff(other.modifiers)
            val memberEdits = members.diff(other.members)
            val isChanged = supertypeEdits.isNotEmpty()
                    || modifierEdits.isNotEmpty()
                    || memberEdits.isNotEmpty()
            return TypeTransaction(supertypeEdits, modifierEdits, memberEdits)
                    .takeIf { isChanged }
        }
    }

    override fun applyOn(subject: Type): Type = subject.copy(
            supertypes = subject.supertypes.apply(supertypeEdits),
            modifiers = subject.modifiers.apply(modifierEdits),
            members = subject.members.apply(memberEdits)
    )
}
