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

package org.chronos.core.delta

// Anticipate the addition of `Block` node.
sealed class BlockEdit {
    companion object {
        @JvmStatic fun String?.apply(edits: List<BlockEdit>): String? =
                edits.fold(this) { block, edit -> edit.applyOn(block) }

        // TODO: clean this up
        @JvmStatic fun String?.apply(edit: BlockEdit?): String? =
                if (edit == null) this else edit.applyOn(this)

        @JvmStatic fun String?.diff(other: String?): BlockEdit? =
                if (this != other) BlockEdit.Set(other) else null
    }

    protected abstract fun applyOn(subject: String?): String?

    // To be replaced with `Add` and `Remove`.
    data class Set(val statements: String?) : BlockEdit() {
        override fun applyOn(subject: String?): String? =
                statements
    }
}
