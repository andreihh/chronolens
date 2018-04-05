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

import org.chronolens.core.model.SetEdit

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
