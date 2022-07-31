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
import org.chronolens.core.model.QualifiedId.Companion.CONTAINER_SEPARATOR
import org.chronolens.core.model.Signature
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourceTree
import org.chronolens.core.model.Type
import org.chronolens.core.model.Variable
import org.chronolens.test.core.BuilderMarker
import org.chronolens.test.core.Init
import org.chronolens.test.core.apply

public fun sourceTree(init: Init<SourceTreeBuilder>): SourceTree =
    SourceTreeBuilder().apply(init).build()

@BuilderMarker
public class SourceTreeBuilder {
    private val sources = mutableListOf<SourceFileBuilder>()

    public fun sourceFile(path: String, init: Init<SourceFileBuilder>): SourceTreeBuilder {
        sources += SourceFileBuilder(path).apply(init)
        return this
    }

    public fun build(): SourceTree = SourceTree.of(sources.map(SourceFileBuilder::build))
}

@BuilderMarker
public class SourceFileBuilder(private val path: String) {
    private val entities = mutableListOf<EntityBuilder<*>>()

    private inline fun <reified T : EntityBuilder<*>> addEntity(
        simpleId: String,
        init: Init<T>
    ): SourceFileBuilder {
        entities += newBuilder<T>(simpleId).apply(init)
        return this
    }

    public fun type(name: String, init: Init<TypeBuilder>): SourceFileBuilder =
        addEntity(name, init)

    public fun function(signature: String, init: Init<FunctionBuilder>): SourceFileBuilder =
        addEntity(signature, init)

    public fun variable(name: String, init: Init<VariableBuilder>): SourceFileBuilder =
        addEntity(name, init)

    public fun build(): SourceFile =
        SourceFile(path = path, entities = entities.map { it.build(path) }.toSet())
}

public class TypeBuilder(private val name: String) : EntityBuilder<Type> {
    private var supertypes = emptySet<String>()
    private var modifiers = emptySet<String>()
    private val members = mutableListOf<EntityBuilder<*>>()

    private inline fun <reified T : EntityBuilder<*>> addMember(
        simpleId: String,
        init: Init<T>
    ): TypeBuilder {
        members += newBuilder<T>(simpleId).apply(init)
        return this
    }

    public fun supertypes(vararg supertypes: String): TypeBuilder {
        this.supertypes = supertypes.requireDistinct()
        return this
    }

    public fun modifiers(vararg modifiers: String): TypeBuilder {
        this.modifiers = modifiers.requireDistinct()
        return this
    }

    public fun type(name: String, init: Init<TypeBuilder>): TypeBuilder = addMember(name, init)

    public fun function(signature: String, init: Init<FunctionBuilder>): TypeBuilder =
        addMember(signature, init)

    public fun variable(name: String, init: Init<VariableBuilder>): TypeBuilder =
        addMember(name, init)

    override fun build(parentId: String): Type {
        val id = "$parentId$CONTAINER_SEPARATOR$name"
        return Type(Identifier(name), supertypes, modifiers, members.map { it.build(id) }.toSet())
    }
}

public class FunctionBuilder(private val signature: String) : EntityBuilder<Function> {
    private var modifiers = emptySet<String>()
    private var parameters = emptyList<String>()
    private val body = mutableListOf<String>()

    public fun parameters(vararg parameters: String): FunctionBuilder {
        this.parameters = parameters.requireDistinct().toList()
        return this
    }

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

    override fun build(parentId: String): Function =
        Function(Signature(signature), parameters, modifiers, body)
}

public class VariableBuilder(private val name: String) : EntityBuilder<Variable> {
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

    override fun build(parentId: String): Variable =
        Variable(name = Identifier(name), modifiers = modifiers, initializer = initializer)
}

private inline fun <reified T> newBuilder(simpleId: String): T =
    T::class.java.getConstructor(String::class.java).newInstance(simpleId)

private fun <T> Array<T>.requireDistinct(): Set<T> {
    val set = LinkedHashSet<T>(size)
    for (element in this) {
        require(element !in set) { "Duplicated element '$element'!" }
        set += element
    }
    return set
}
