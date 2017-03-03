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
import org.chronos.core.delta.TypeChange.SupertypeChange.AddSupertype
import org.chronos.core.delta.TypeChange.SupertypeChange.RemoveSupertype

/**
 * A changed which should be applied to a [Type].
 *
 * @property supertypeChanges the list of changes which should be applied to the
 * `supertypes`
 * @property memberChanges the list of changes which should be applied to the
 * `members`
 */
data class TypeChange(
        val supertypeChanges: List<SupertypeChange> = emptyList(),
        val memberChanges: List<NodeChange> = emptyList()
) : Change<Type> {
    /** A change which should be applied to a set of supertypes. */
    sealed class SupertypeChange {
        /**
         * Indicates that a supertype should be added to the set of supertypes.
         *
         * @property name the name of the supertype which should be added
         */
        data class AddSupertype(val name: String) : SupertypeChange()

        /**
         * Indicates that a supertype should be removed from the set of
         * supertypes.
         *
         * @property name the name of the supertype which should be removed
         */
        data class RemoveSupertype(val name: String) : SupertypeChange()
    }

    override fun applyOn(subject: Type): Type {
        val supertypes = subject.supertypes.toMutableSet()
        supertypeChanges.forEach { change ->
            when (change) {
                is AddSupertype -> supertypes.add(change.name)
                is RemoveSupertype -> supertypes.remove(change.name)
            }
        }
        val members = subject.members.apply(memberChanges)
        return subject.copy(supertypes = supertypes, members = members)
    }
}
