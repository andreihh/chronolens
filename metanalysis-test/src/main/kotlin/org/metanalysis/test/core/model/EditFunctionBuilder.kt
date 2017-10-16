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

package org.metanalysis.test.core.model

import org.metanalysis.core.model.ListEdit
import org.metanalysis.core.model.ProjectEdit.EditFunction
import org.metanalysis.core.model.SetEdit

@ModelBuilderMarker
class EditFunctionBuilder(private val id: String) {
    private val modifierEdits = arrayListOf<SetEdit<String>>()
    private val parameterEdits = arrayListOf<ListEdit<String>>()
    private val bodyEdits = arrayListOf<ListEdit<String>>()

    fun modifiers(init: SetEditsBuilder<String>.() -> Unit) {
        val setEditsBuilder = SetEditsBuilder<String>()
        setEditsBuilder.init()
        modifierEdits += setEditsBuilder.build()
    }

    fun parameters(init: ListEditsBuilder<String>.() -> Unit) {
        val listEditsBuilder = ListEditsBuilder<String>()
        listEditsBuilder.init()
        parameterEdits += listEditsBuilder.build()
    }

    fun body(init: ListEditsBuilder<String>.() -> Unit) {
        val listEditsBuilder = ListEditsBuilder<String>()
        listEditsBuilder.init()
        bodyEdits += listEditsBuilder.build()
    }

    fun build(): EditFunction =
            EditFunction(id, modifierEdits, parameterEdits, bodyEdits)
}
