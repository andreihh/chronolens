/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.test.core.model

import org.chronolens.core.model.Function
import org.chronolens.core.model.Identifier
import org.chronolens.core.model.Signature
import org.chronolens.core.model.SourceEntity
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.Type
import org.chronolens.core.model.Variable
import org.chronolens.test.core.BuilderMarker
import org.chronolens.test.core.Init
import org.chronolens.test.core.apply

@BuilderMarker
public class SourceFileBuilder(private val path: String) {
    private val entities = mutableSetOf<SourceEntity>()

    public fun sourceEntity(sourceEntity: SourceEntity): SourceFileBuilder {
        +sourceEntity
        return this
    }

    public operator fun SourceEntity.unaryPlus() {
        require(this !in entities) { "Duplicate entity '$this'!" }
        entities += this
    }

    public fun build(): SourceFile = SourceFile(SourcePath(path), entities)
}

public class TypeBuilder(private val name: String) {
    private var supertypes = emptySet<Identifier>()
    private var modifiers = emptySet<String>()
    private val members = mutableSetOf<SourceEntity>()

    public fun supertypes(vararg supertypes: Identifier): TypeBuilder {
        this.supertypes = supertypes.requireDistinct()
        return this
    }

    public fun supertypes(vararg supertypes: String): TypeBuilder =
        supertypes(*supertypes.map(::Identifier).toTypedArray())

    public fun modifiers(vararg modifiers: String): TypeBuilder {
        this.modifiers = modifiers.requireDistinct()
        return this
    }

    public fun member(member: SourceEntity): TypeBuilder {
        +member
        return this
    }

    public operator fun SourceEntity.unaryPlus() {
        require(this !in members) { "Duplicate member '$this'!" }
        members += this
    }

    public fun build(): Type = Type(Identifier(name), supertypes, modifiers, members)
}

public class FunctionBuilder(private val signature: String) {
    private var modifiers = emptySet<String>()
    private var parameters = emptyList<Identifier>()
    private val body = mutableListOf<String>()

    public fun parameters(vararg parameters: Identifier): FunctionBuilder {
        this.parameters = parameters.requireDistinct().toList()
        return this
    }

    public fun parameters(vararg parameters: String): FunctionBuilder =
        parameters(*parameters.map(::Identifier).toTypedArray())

    public fun modifiers(vararg modifiers: String): FunctionBuilder {
        this.modifiers = modifiers.requireDistinct()
        return this
    }

    public fun body(vararg bodyLines: String): FunctionBuilder {
        body += bodyLines
        return this
    }

    public operator fun String.unaryPlus() {
        body += this
    }

    public fun build(): Function = Function(Signature(signature), parameters, modifiers, body)
}

public class VariableBuilder(private val name: String) {
    private var modifiers = emptySet<String>()
    private val initializer = mutableListOf<String>()

    public fun modifiers(vararg modifiers: String): VariableBuilder {
        this.modifiers = modifiers.requireDistinct()
        return this
    }

    public fun initializer(vararg initializerLines: String): VariableBuilder {
        initializer += initializerLines
        return this
    }

    public operator fun String.unaryPlus() {
        initializer += this
    }

    public fun build(): Variable = Variable(Identifier(name), modifiers, initializer)
}

public fun sourceFile(path: String, init: Init<SourceFileBuilder>): SourceFile =
    SourceFileBuilder(path).apply(init).build()

public fun type(name: String, init: Init<TypeBuilder>): Type = TypeBuilder(name).apply(init).build()

public fun function(signature: String, init: Init<FunctionBuilder>): Function =
    FunctionBuilder(signature).apply(init).build()

public fun variable(name: String, init: Init<VariableBuilder>): Variable =
    VariableBuilder(name).apply(init).build()

private fun <T> Array<T>.requireDistinct(): Set<T> {
    val set = LinkedHashSet<T>(size)
    for (element in this) {
        require(element !in set) { "Duplicated element '$element'!" }
        set += element
    }
    return set
}
