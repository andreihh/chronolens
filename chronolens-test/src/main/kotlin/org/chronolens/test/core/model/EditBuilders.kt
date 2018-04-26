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

import org.chronolens.core.model.EditFunction
import org.chronolens.core.model.EditType
import org.chronolens.core.model.EditVariable
import org.chronolens.core.model.ListEdit
import org.chronolens.core.model.SetEdit
import org.chronolens.test.core.BuilderMarker
import org.chronolens.test.core.Init
import org.chronolens.test.core.apply

class SetEditsBuilder<T> {
    private val setEdits = mutableListOf<SetEdit<T>>()

    fun add(value: T): SetEditsBuilder<T> {
        setEdits += SetEdit.Add(value)
        return this
    }

    operator fun T.unaryPlus() {
        setEdits += SetEdit.Add(this)
    }

    fun remove(value: T): SetEditsBuilder<T> {
        setEdits += SetEdit.Remove(value)
        return this
    }

    operator fun T.unaryMinus() {
        setEdits += SetEdit.Remove(this)
    }

    fun build(): List<SetEdit<T>> = setEdits
}

@BuilderMarker
class ListEditsBuilder<T> {
    private val listEdits = mutableListOf<ListEdit<T>>()

    fun add(index: Int, value: T): ListEditsBuilder<T> {
        listEdits += ListEdit.Add(index, value)
        return this
    }

    fun remove(index: Int): ListEditsBuilder<T> {
        listEdits += ListEdit.Remove(index)
        return this
    }

    fun build(): List<ListEdit<T>> = listEdits
}

@BuilderMarker
class EditTypeBuilder(private val id: String) {
    private val supertypeEdits = mutableListOf<SetEdit<String>>()
    private val modifierEdits = mutableListOf<SetEdit<String>>()

    fun supertypes(init: Init<SetEditsBuilder<String>>): EditTypeBuilder {
        supertypeEdits += SetEditsBuilder<String>().apply(init).build()
        return this
    }

    fun modifiers(init: Init<SetEditsBuilder<String>>): EditTypeBuilder {
        modifierEdits += SetEditsBuilder<String>().apply(init).build()
        return this
    }

    fun build(): EditType = EditType(id, supertypeEdits, modifierEdits)
}

@BuilderMarker
class EditFunctionBuilder(private val id: String) {
    private val modifierEdits = mutableListOf<SetEdit<String>>()
    private val parameterEdits = mutableListOf<ListEdit<String>>()
    private val bodyEdits = mutableListOf<ListEdit<String>>()

    fun parameters(init: Init<ListEditsBuilder<String>>): EditFunctionBuilder {
        parameterEdits += ListEditsBuilder<String>().apply(init).build()
        return this
    }

    fun modifiers(init: Init<SetEditsBuilder<String>>): EditFunctionBuilder {
        modifierEdits += SetEditsBuilder<String>().apply(init).build()
        return this
    }

    fun body(init: Init<ListEditsBuilder<String>>): EditFunctionBuilder {
        bodyEdits += ListEditsBuilder<String>().apply(init).build()
        return this
    }

    fun build(): EditFunction =
        EditFunction(id, parameterEdits, modifierEdits, bodyEdits)
}

@BuilderMarker
class EditVariableBuilder(private val id: String) {
    private val modifierEdits = mutableListOf<SetEdit<String>>()
    private val initializerEdits = mutableListOf<ListEdit<String>>()

    fun modifiers(init: Init<SetEditsBuilder<String>>): EditVariableBuilder {
        modifierEdits += SetEditsBuilder<String>().apply(init).build()
        return this
    }

    fun initializer(init: Init<ListEditsBuilder<String>>): EditVariableBuilder {
        initializerEdits += ListEditsBuilder<String>().apply(init).build()
        return this
    }

    fun build(): EditVariable =
        EditVariable(id, modifierEdits, initializerEdits)
}
