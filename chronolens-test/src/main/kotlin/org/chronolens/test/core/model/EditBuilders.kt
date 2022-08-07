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

package org.chronolens.test.core.model

import org.chronolens.core.model.ListEdit
import org.chronolens.core.model.SetEdit
import org.chronolens.test.core.BuilderMarker

public class SetEditsBuilder<T> {
    private val setEdits = mutableListOf<SetEdit<T>>()

    public fun add(value: T): SetEditsBuilder<T> {
        +value
        return this
    }

    public operator fun T.unaryPlus() {
        setEdits += SetEdit.Add(this)
    }

    public fun remove(value: T): SetEditsBuilder<T> {
        -value
        return this
    }

    public operator fun T.unaryMinus() {
        setEdits += SetEdit.Remove(this)
    }

    public fun build(): List<SetEdit<T>> = setEdits
}

@BuilderMarker
public class ListEditsBuilder<T> {
    private val listEdits = mutableListOf<ListEdit<T>>()

    public fun add(index: Int, value: T): ListEditsBuilder<T> {
        listEdits += ListEdit.Add(index, value)
        return this
    }

    public fun remove(index: Int): ListEditsBuilder<T> {
        listEdits += ListEdit.Remove(index)
        return this
    }

    public fun build(): List<ListEdit<T>> = listEdits
}
