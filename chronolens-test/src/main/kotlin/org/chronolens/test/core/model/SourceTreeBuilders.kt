/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

import org.chronolens.core.model.Function
import org.chronolens.core.model.QualifiedSourceNodeId
import org.chronolens.core.model.SourceFile
import org.chronolens.core.model.SourceTree
import org.chronolens.core.model.SourceTreeNode
import org.chronolens.core.model.Type
import org.chronolens.core.model.Variable
import org.chronolens.test.core.BuilderMarker
import org.chronolens.test.core.Init
import org.chronolens.test.core.apply

@BuilderMarker
public class SourceTreeBuilder {
    private val sources = mutableSetOf<SourceFile>()

    public fun sourceFile(sourceFile: SourceFile): SourceTreeBuilder {
        +sourceFile
        return this
    }

    public operator fun SourceFile.unaryPlus() {
        require(this !in sources) { "Duplicate source file '$this'!" }
        sources += this
    }

    public fun build(): SourceTree = SourceTree.of(sources)
}

public fun sourceTree(init: Init<SourceTreeBuilder>): SourceTree =
    SourceTreeBuilder().apply(init).build()

@JvmName("buildSourceFile")
public fun QualifiedSourceNodeId<SourceFile>.build(
    init: Init<SourceFileBuilder>
): SourceTreeNode<SourceFile> =
    SourceTreeNode.of(SourceFileBuilder(this.id.toString()).apply(init).build())

@JvmName("buildType")
public fun QualifiedSourceNodeId<Type>.build(init: Init<TypeBuilder>): SourceTreeNode<Type> =
    SourceTreeNode(this, TypeBuilder(this.id.toString()).apply(init).build())

@JvmName("buildFunction")
public fun QualifiedSourceNodeId<Function>.build(
    init: Init<FunctionBuilder>
): SourceTreeNode<Function> =
    SourceTreeNode(this, FunctionBuilder(this.id.toString()).apply(init).build())

@JvmName("buildVariable")
public fun QualifiedSourceNodeId<Variable>.build(
    init: Init<VariableBuilder>
): SourceTreeNode<Variable> =
    SourceTreeNode(this, VariableBuilder(this.id.toString()).apply(init).build())
