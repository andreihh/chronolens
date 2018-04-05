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

import org.chronolens.core.model.EditVariable
import org.chronolens.core.model.ListEdit
import org.chronolens.core.model.SetEdit
import org.chronolens.test.core.BuilderMarker
import org.chronolens.test.core.Init
import org.chronolens.test.core.apply

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
