/*
 * Copyright 2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.test.model

import org.chronolens.model.Function
import org.chronolens.model.QualifiedSourceNodeId
import org.chronolens.model.SourceFile
import org.chronolens.model.SourceTree
import org.chronolens.model.SourceTreeNode
import org.chronolens.model.Type
import org.chronolens.model.Variable
import org.chronolens.model.name
import org.chronolens.model.signature
import org.chronolens.test.BuilderMarker
import org.chronolens.test.Init
import org.chronolens.test.apply

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
): SourceTreeNode<SourceFile> = SourceTreeNode.of(SourceFileBuilder(sourcePath).apply(init).build())

@JvmName("buildType")
public fun QualifiedSourceNodeId<Type>.build(init: Init<TypeBuilder>): SourceTreeNode<Type> =
  SourceTreeNode(this, TypeBuilder(name).apply(init).build())

@JvmName("buildVariable")
public fun QualifiedSourceNodeId<Variable>.build(
  init: Init<VariableBuilder>
): SourceTreeNode<Variable> = SourceTreeNode(this, VariableBuilder(name).apply(init).build())

@JvmName("buildFunction")
public fun QualifiedSourceNodeId<Function>.build(
  init: Init<FunctionBuilder>
): SourceTreeNode<Function> = SourceTreeNode(this, FunctionBuilder(signature).apply(init).build())
