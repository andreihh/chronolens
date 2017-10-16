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
import org.metanalysis.core.model.SourceNode.SourceEntity.Variable

class VariableBuilder(private val name: String) : EntityBuilder<Variable> {
    private var modifiers = setOf<String>()
    private val initializer = arrayListOf<String>()

    fun modifiers(vararg modifiers: String): VariableBuilder {
        modifiers.groupBy { it }.forEach { (modifier, occurrences) ->
            require(occurrences.size == 1) {
                "Duplicated modifier '$modifier'!"
            }
        }
        this.modifiers = modifiers.toSet()
        return this
    }

    operator fun String.unaryPlus() {
        initializer += this
    }

    fun initializer(lines: String): VariableBuilder {
        initializer += lines
        return this
    }

    override fun build(parentId: String): Variable = Variable(
            id = "$parentId$ENTITY_SEPARATOR$name",
            modifiers = modifiers,
            initializer = initializer
    )
}
