/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.test.core.model

import org.chronolens.core.model.SourceFile
import org.chronolens.test.core.BuilderMarker
import org.chronolens.test.core.Init
import org.chronolens.test.core.apply

@BuilderMarker
class SourceFileBuilder(private val path: String) {
    private val entities = mutableListOf<EntityBuilder<*>>()

    private inline fun <reified T : EntityBuilder<*>> addEntity(
        simpleId: String,
        init: Init<T>
    ): SourceFileBuilder {
        entities += newBuilder<T>(simpleId).apply(init)
        return this
    }

    fun type(name: String, init: Init<TypeBuilder>): SourceFileBuilder =
        addEntity(name, init)

    fun function(
        signature: String,
        init: Init<FunctionBuilder>
    ): SourceFileBuilder = addEntity(signature, init)

    fun variable(
        name: String,
        init: Init<VariableBuilder>
    ): SourceFileBuilder = addEntity(name, init)

    fun build(): SourceFile = SourceFile(
        id = path,
        entities = entities.map { it.build(path) }.toSet()
    )
}
