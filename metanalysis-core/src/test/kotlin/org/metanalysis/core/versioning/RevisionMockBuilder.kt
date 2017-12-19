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

package org.metanalysis.core.versioning

class RevisionMockBuilder(private val id: String) {
    var date: Long = System.currentTimeMillis()
    var author: String = "<unknown-author>"
    private val changeSet = hashMapOf<String, String?>()

    fun change(entry: Pair<String, String?>) {
        changeSet[entry.first] = entry.second
    }

    fun build(): RevisionMock = RevisionMock(id, date, author, changeSet)
}
