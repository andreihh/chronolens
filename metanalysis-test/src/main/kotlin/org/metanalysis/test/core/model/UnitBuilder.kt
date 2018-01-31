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

import org.metanalysis.core.model.SourceUnit
import org.metanalysis.test.core.BuilderMarker
import org.metanalysis.test.core.Init
import org.metanalysis.test.core.apply

@BuilderMarker
class UnitBuilder(private val path: String) {
    private val entities = arrayListOf<EntityBuilder<*>>()

    private inline fun <reified T : EntityBuilder<*>> addEntity(
        simpleId: String,
        init: Init<T>
    ): UnitBuilder {
        entities += newBuilder<T>(simpleId).apply(init)
        return this
    }

    fun type(name: String, init: Init<TypeBuilder>): UnitBuilder =
        addEntity(name, init)

    fun function(signature: String, init: Init<FunctionBuilder>): UnitBuilder =
        addEntity(signature, init)

    fun variable(name: String, init: Init<VariableBuilder>): UnitBuilder =
        addEntity(name, init)

    fun build(): SourceUnit =
        SourceUnit(id = path, entities = entities.map { it.build(path) })
}
