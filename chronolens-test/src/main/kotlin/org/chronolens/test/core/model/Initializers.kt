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

import org.chronolens.core.model.AddNode
import org.chronolens.core.model.EditFunction
import org.chronolens.core.model.EditType
import org.chronolens.core.model.EditVariable
import org.chronolens.core.model.Function
import org.chronolens.core.model.RemoveNode
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourceNode.Companion.CONTAINER_SEPARATOR
import org.chronolens.core.model.SourceNode.Companion.MEMBER_SEPARATOR
import org.chronolens.core.model.Type
import org.chronolens.core.model.Variable
import org.chronolens.test.core.Init
import org.chronolens.test.core.apply

fun sourceFile(path: String): SourceFileInitializer =
    SourceFileInitializer(path)

class SourceFileInitializer(private val path: String) {
    fun id(): String = path

    fun build(init: Init<SourceFileBuilder>): SourceFile =
        SourceFileBuilder(path).apply(init).build()

    fun type(name: String): TypeInitializer =
        TypeInitializer(path, name)

    fun function(signature: String): FunctionInitializer =
        FunctionInitializer(path, signature)

    fun variable(name: String): VariableInitializer =
        VariableInitializer(path, name)

    fun add(init: Init<SourceFileBuilder>): AddNode =
        AddNode(SourceFileBuilder(path).apply(init).build())

    fun remove(): RemoveNode = RemoveNode(path)
}

class TypeInitializer(private val parentId: String, private val name: String) {
    private val id: String = "$parentId$CONTAINER_SEPARATOR$name"

    fun id(): String = id

    fun build(init: Init<TypeBuilder>): Type =
        TypeBuilder(name).apply(init).build(parentId)

    fun type(name: String): TypeInitializer =
        TypeInitializer(id, name)

    fun function(signature: String): FunctionInitializer =
        FunctionInitializer(id, signature)

    fun variable(name: String): VariableInitializer =
        VariableInitializer(id, name)

    fun add(init: Init<TypeBuilder>): AddNode =
        AddNode(TypeBuilder(name).apply(init).build(parentId))

    fun remove(): RemoveNode = RemoveNode(id)

    fun edit(init: Init<EditTypeBuilder>): EditType =
        EditTypeBuilder(id).apply(init).build()
}

class FunctionInitializer(
    private val parentId: String,
    private val signature: String
) {

    private val id: String = "$parentId$MEMBER_SEPARATOR$signature"

    fun id(): String = id

    fun build(init: Init<FunctionBuilder>): Function =
        FunctionBuilder(signature).apply(init).build(parentId)

    fun add(init: Init<FunctionBuilder>): AddNode =
        AddNode(FunctionBuilder(signature).apply(init).build(parentId))

    fun remove(): RemoveNode = RemoveNode(id)

    fun edit(init: Init<EditFunctionBuilder>): EditFunction =
        EditFunctionBuilder(id).apply(init).build()
}

class VariableInitializer(
    private val parentId: String,
    private val name: String
) {

    private val id: String = "$parentId$MEMBER_SEPARATOR$name"

    fun id(): String = id

    fun build(init: Init<VariableBuilder>): Variable =
        VariableBuilder(name).apply(init).build(parentId)

    fun add(init: Init<VariableBuilder>): AddNode =
        AddNode(VariableBuilder(name).apply(init).build(parentId))

    fun remove(): RemoveNode = RemoveNode(id)

    fun edit(init: Init<EditVariableBuilder>): EditVariable =
        EditVariableBuilder(id).apply(init).build()
}
