/*
 * Copyright 2018-2021 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.git

import org.chronolens.core.subprocess.Subprocess.execute
import java.io.File

fun init(directory: File) {
    execute(directory, "git", "init")
    execute(directory, "git", "config", "user.email", "test@test.com")
    execute(directory, "git", "config", "user.name", "test")
}

fun clone(directory: File, url: String) {
    execute(directory, "git", "clone", url, "./")
}

fun commit(directory: File, changeSet: Map<String, String>) {
    for ((path, content) in changeSet) {
        File(directory, path).writeText(content)
    }
    execute(directory, "git", "add", "-A")
    execute(directory, "git", "commit", "-m", "Test commit.")
}
