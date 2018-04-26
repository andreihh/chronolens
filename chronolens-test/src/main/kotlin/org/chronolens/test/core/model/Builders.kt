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

@file:JvmName("Builders")

package org.chronolens.test.core.model

import org.chronolens.core.model.Function
import org.chronolens.core.model.Project
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourceNode.Companion.CONTAINER_SEPARATOR
import org.chronolens.core.model.SourceNode.Companion.MEMBER_SEPARATOR
import org.chronolens.core.model.Type
import org.chronolens.core.model.Variable
import org.chronolens.test.core.BuilderMarker
import org.chronolens.test.core.Init
import org.chronolens.test.core.apply

fun project(init: Init<ProjectBuilder>): Project =
    ProjectBuilder().apply(init).build()

@BuilderMarker
class ProjectBuilder {
    private val sources = mutableListOf<SourceFileBuilder>()

    fun sourceFile(
        path: String,
        init: Init<SourceFileBuilder>
    ): ProjectBuilder {
        sources += SourceFileBuilder(path).apply(init)
        return this
    }

    fun build(): Project = Project.of(sources.map(SourceFileBuilder::build))
}

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

class TypeBuilder(private val name: String) : EntityBuilder<Type> {
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

    fun supertypes(vararg supertypes: String): TypeBuilder {
        this.supertypes = supertypes.requireDistinct()
        return this
    }

    fun modifiers(vararg modifiers: String): TypeBuilder {
        this.modifiers = modifiers.requireDistinct()
        return this
    }

    fun type(name: String, init: Init<TypeBuilder>): TypeBuilder =
        addMember(name, init)

    fun function(signature: String, init: Init<FunctionBuilder>): TypeBuilder =
        addMember(signature, init)

    fun variable(name: String, init: Init<VariableBuilder>): TypeBuilder =
        addMember(name, init)

    override fun build(parentId: String): Type {
        val id = "$parentId$CONTAINER_SEPARATOR$name"
        return Type(
            id = id,
            supertypes = supertypes,
            modifiers = modifiers,
            members = members.map { it.build(id) }.toSet()
        )
    }
}

class FunctionBuilder(private val signature: String) : EntityBuilder<Function> {
    private var modifiers = emptySet<String>()
    private var parameters = emptyList<String>()
    private val body = mutableListOf<String>()

    fun parameters(vararg parameters: String): FunctionBuilder {
        this.parameters = parameters.requireDistinct().toList()
        return this
    }

    fun modifiers(vararg modifiers: String): FunctionBuilder {
        this.modifiers = modifiers.requireDistinct()
        return this
    }

    fun body(vararg bodyLines: String): FunctionBuilder {
        body += bodyLines
        return this
    }

    operator fun String.unaryPlus() {
        body += this
    }

    override fun build(parentId: String): Function {
        val id = "$parentId$MEMBER_SEPARATOR$signature"
        return Function(id, parameters, modifiers, body)
    }
}

class VariableBuilder(private val name: String) : EntityBuilder<Variable> {
    private var modifiers = emptySet<String>()
    private val initializer = mutableListOf<String>()

    fun modifiers(vararg modifiers: String): VariableBuilder {
        this.modifiers = modifiers.requireDistinct()
        return this
    }

    fun initializer(vararg initializerLines: String): VariableBuilder {
        initializer += initializerLines
        return this
    }

    operator fun String.unaryPlus() {
        initializer += this
    }

    override fun build(parentId: String): Variable = Variable(
        id = "$parentId$MEMBER_SEPARATOR$name",
        modifiers = modifiers,
        initializer = initializer
    )
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
