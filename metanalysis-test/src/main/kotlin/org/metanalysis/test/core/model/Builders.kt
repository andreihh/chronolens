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

@file:JvmName("Builders")

package org.metanalysis.test.core.model

import org.metanalysis.core.model.Project
import org.metanalysis.core.model.SourceNode.Companion.ENTITY_SEPARATOR
import org.metanalysis.core.model.SourceNode.SourceEntity.Function
import org.metanalysis.core.model.SourceNode.SourceEntity.Type
import org.metanalysis.core.model.SourceNode.SourceEntity.Variable
import org.metanalysis.core.model.SourceNode.SourceUnit

inline fun project(init: ProjectBuilder.() -> Unit): Project =
        ProjectBuilder().apply(init).build()

inline fun sourceUnit(path: String, init: UnitBuilder.() -> Unit): SourceUnit =
        UnitBuilder(path).apply(init).build()

inline fun type(id: String, init: TypeBuilder.() -> Unit): Type {
    val parentId = id.substringBeforeLast(ENTITY_SEPARATOR)
    val name = id.substringAfterLast(ENTITY_SEPARATOR)
    val builder = TypeBuilder(name)
    builder.init()
    return builder.build(parentId)
}

inline fun function(id: String, init: FunctionBuilder.() -> Unit): Function {
    val parentId = id.substringBeforeLast(ENTITY_SEPARATOR)
    val signature = id.substringAfterLast(ENTITY_SEPARATOR)
    val builder = FunctionBuilder(signature)
    builder.init()
    return builder.build(parentId)
}

inline fun variable(id: String, init: VariableBuilder.() -> Unit): Variable {
    val parentId = id.substringBeforeLast(ENTITY_SEPARATOR)
    val name = id.substringAfterLast(ENTITY_SEPARATOR)
    val builder = VariableBuilder(name)
    builder.init()
    return builder.build(parentId)
}
