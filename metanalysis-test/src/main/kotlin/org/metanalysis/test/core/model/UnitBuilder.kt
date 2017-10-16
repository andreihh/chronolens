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

import org.metanalysis.core.model.SourceNode.SourceUnit

@SourceBuilderMarker
class UnitBuilder(private val path: String) {
    private val entities = arrayListOf<EntityBuilder<*>>()

    fun type(name: String, init: TypeBuilder.() -> Unit) {
        entities += TypeBuilder(name).apply(init)
    }

    fun function(signature: String, init: FunctionBuilder.() -> Unit) {
        entities += FunctionBuilder(signature).apply(init)
    }

    fun variable(name: String, init: VariableBuilder.() -> Unit) {
        entities += VariableBuilder(name).apply(init)
    }

    fun type(builder: TypeBuilder): UnitBuilder {
        entities += builder
        return this
    }

    fun function(builder: FunctionBuilder): UnitBuilder {
        entities += builder
        return this
    }

    fun variable(builder: VariableBuilder): UnitBuilder {
        entities += builder
        return this
    }

    fun build(): SourceUnit =
            SourceUnit(id = path, entities = entities.map { it.build(path) })
}
