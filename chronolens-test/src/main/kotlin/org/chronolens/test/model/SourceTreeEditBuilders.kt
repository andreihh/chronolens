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

import org.chronolens.model.AddNode
import org.chronolens.model.EditFunction
import org.chronolens.model.EditType
import org.chronolens.model.EditVariable
import org.chronolens.model.Function
import org.chronolens.model.Identifier
import org.chronolens.model.ListEdit
import org.chronolens.model.QualifiedSourceNodeId
import org.chronolens.model.RemoveNode
import org.chronolens.model.SetEdit
import org.chronolens.model.SourceFile
import org.chronolens.model.Type
import org.chronolens.model.Variable
import org.chronolens.model.name
import org.chronolens.model.signature
import org.chronolens.test.BuilderMarker
import org.chronolens.test.Init
import org.chronolens.test.apply

@BuilderMarker
public class EditTypeBuilder(private val qualifiedId: QualifiedSourceNodeId<Type>) {
  private val supertypeEdits = mutableListOf<SetEdit<Identifier>>()
  private val modifierEdits = mutableListOf<SetEdit<String>>()

  public fun supertypes(init: Init<SetEditBuilder<String>>): EditTypeBuilder {
    supertypeEdits +=
      SetEditBuilder<String>().apply(init).build().map { edit ->
        when (edit) {
          is SetEdit.Add -> SetEdit.Add(Identifier(edit.value))
          is SetEdit.Remove -> SetEdit.Remove(Identifier(edit.value))
        }
      }
    return this
  }

  public fun modifiers(init: Init<SetEditBuilder<String>>): EditTypeBuilder {
    modifierEdits += SetEditBuilder<String>().apply(init).build()
    return this
  }

  public fun build(): EditType = EditType(qualifiedId, supertypeEdits, modifierEdits)
}

@BuilderMarker
public class EditFunctionBuilder(private val qualifiedId: QualifiedSourceNodeId<Function>) {
  private val modifierEdits = mutableListOf<SetEdit<String>>()
  private val parameterEdits = mutableListOf<ListEdit<Identifier>>()
  private val bodyEdits = mutableListOf<ListEdit<String>>()

  public fun parameters(init: Init<ListEditBuilder<String>>): EditFunctionBuilder {
    parameterEdits +=
      ListEditBuilder<String>().apply(init).build().map { edit ->
        when (edit) {
          is ListEdit.Add -> ListEdit.Add(edit.index, Identifier(edit.value))
          is ListEdit.Remove -> ListEdit.Remove(edit.index)
        }
      }
    return this
  }

  public fun modifiers(init: Init<SetEditBuilder<String>>): EditFunctionBuilder {
    modifierEdits += SetEditBuilder<String>().apply(init).build()
    return this
  }

  public fun body(init: Init<ListEditBuilder<String>>): EditFunctionBuilder {
    bodyEdits += ListEditBuilder<String>().apply(init).build()
    return this
  }

  public fun build(): EditFunction =
    EditFunction(qualifiedId, parameterEdits, modifierEdits, bodyEdits)
}

@BuilderMarker
public class EditVariableBuilder(private val qualifiedId: QualifiedSourceNodeId<Variable>) {
  private val modifierEdits = mutableListOf<SetEdit<String>>()
  private val initializerEdits = mutableListOf<ListEdit<String>>()

  public fun modifiers(init: Init<SetEditBuilder<String>>): EditVariableBuilder {
    modifierEdits += SetEditBuilder<String>().apply(init).build()
    return this
  }

  public fun initializer(init: Init<ListEditBuilder<String>>): EditVariableBuilder {
    initializerEdits += ListEditBuilder<String>().apply(init).build()
    return this
  }

  public fun build(): EditVariable = EditVariable(qualifiedId, modifierEdits, initializerEdits)
}

@JvmName("addSourceFile")
public fun QualifiedSourceNodeId<SourceFile>.add(
  init: Init<SourceFileBuilder>
): AddNode<SourceFile> = AddNode(this, SourceFileBuilder(sourcePath).apply(init).build())

@JvmName("addType")
public fun QualifiedSourceNodeId<Type>.add(init: Init<TypeBuilder>): AddNode<Type> =
  AddNode(this, TypeBuilder(name).apply(init).build())

@JvmName("addFunction")
public fun QualifiedSourceNodeId<Function>.add(init: Init<FunctionBuilder>): AddNode<Function> =
  AddNode(this, FunctionBuilder(signature).apply(init).build())

@JvmName("addVariable")
public fun QualifiedSourceNodeId<Variable>.add(init: Init<VariableBuilder>): AddNode<Variable> =
  AddNode(this, VariableBuilder(name).apply(init).build())

public fun QualifiedSourceNodeId<*>.remove(): RemoveNode = RemoveNode(this)

public fun QualifiedSourceNodeId<Type>.edit(init: Init<EditTypeBuilder>): EditType =
  EditTypeBuilder(this).apply(init).build()

public fun QualifiedSourceNodeId<Function>.edit(init: Init<EditFunctionBuilder>): EditFunction =
  EditFunctionBuilder(this).apply(init).build()

public fun QualifiedSourceNodeId<Variable>.edit(init: Init<EditVariableBuilder>): EditVariable =
  EditVariableBuilder(this).apply(init).build()
