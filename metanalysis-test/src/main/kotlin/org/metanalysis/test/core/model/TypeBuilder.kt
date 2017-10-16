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

package org.metanalysis.test.core.model

import org.metanalysis.core.model.SourceNode.Companion.ENTITY_SEPARATOR
import org.metanalysis.core.model.SourceNode.SourceEntity.Type

class TypeBuilder(private val name: String) : EntityBuilder<Type> {
    private var modifiers = setOf<String>()
    private var supertypes = setOf<String>()
    private val members = arrayListOf<EntityBuilder<*>>()

    fun modifiers(vararg modifiers: String): TypeBuilder {
        modifiers.groupBy { it }.forEach { (modifier, occurrences) ->
            require(occurrences.size == 1) {
                "Duplicated modifier '$modifier'!"
            }
        }
        this.modifiers = modifiers.toSet()
        return this
    }

    fun supertypes(vararg supertypes: String): TypeBuilder {
        supertypes.groupBy { it }.forEach { (supertype, occurrences) ->
            require(occurrences.size == 1) {
                "Duplicated supertype '$supertype'!"
            }
        }
        this.supertypes = supertypes.toSet()
        return this
    }

    fun type(name: String, init: TypeBuilder.() -> Unit) {
        members += TypeBuilder(name).apply(init)
    }

    fun function(signature: String, init: FunctionBuilder.() -> Unit) {
        members += FunctionBuilder(signature).apply(init)
    }

    fun variable(name: String, init: VariableBuilder.() -> Unit) {
        members += VariableBuilder(name).apply(init)
    }

    fun type(builder: TypeBuilder): TypeBuilder {
        members += builder
        return this
    }

    fun function(builder: FunctionBuilder): TypeBuilder {
        members += builder
        return this
    }

    fun variable(builder: VariableBuilder): TypeBuilder {
        members += builder
        return this
    }

    override fun build(parentId: String): Type {
        val id = "$parentId$ENTITY_SEPARATOR$name"
        return Type(
                id = id,
                modifiers = modifiers,
                supertypes = supertypes,
                members = members.map { it.build(id) }
        )
    }
}
