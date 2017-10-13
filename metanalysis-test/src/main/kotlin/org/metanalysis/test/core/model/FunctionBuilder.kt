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
import org.metanalysis.core.model.SourceNode.SourceEntity.Function

class FunctionBuilder(private val signature: String) : EntityBuilder<Function> {
    private var modifiers = setOf<String>()
    private val parameters = arrayListOf<VariableBuilder>()
    private val body = arrayListOf<String>()

    fun modifiers(vararg modifiers: String) {
        modifiers.groupBy { it }.forEach { (modifier, occurrences) ->
            require(occurrences.size == 1) {
                "Duplicated modifier '$modifier'!"
            }
        }
        this.modifiers = modifiers.toSet()
    }

    fun parameter(name: String, init: VariableBuilder.() -> Unit) {
        parameters += VariableBuilder(name).apply(init)
    }

    operator fun String.unaryPlus() {
        body += this
    }

    override fun build(parentId: String): Function {
        val id = "$parentId$ENTITY_SEPARATOR$signature"
        return Function(
            id = id,
            modifiers = modifiers,
            parameters = parameters.map { it.build(id) },
            body = body
        )
    }
}
