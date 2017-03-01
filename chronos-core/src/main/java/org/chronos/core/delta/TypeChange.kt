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
import org.chronos.core.delta.NodeChange.Companion.apply

data class TypeChange(
        val supertypeChanges: List<SupertypeChange>,
        val memberChanges: List<NodeChange>
) : Change<Type> {
    sealed class SupertypeChange {
        data class Add(val name: String) : SupertypeChange()

        data class Remove(val name: String) : SupertypeChange()
    }

    override fun applyOn(subject: Type): Type {
        val supertypes = subject.supertypes.toMutableSet()
        supertypeChanges.forEach { change ->
            when (change) {
                is SupertypeChange.Add -> supertypes.add(change.name)
                is SupertypeChange.Remove -> supertypes.remove(change.name)
            }
        }
        val members = subject.members.apply(memberChanges)
        return subject.copy(supertypes = supertypes, members = members)
    }
}
