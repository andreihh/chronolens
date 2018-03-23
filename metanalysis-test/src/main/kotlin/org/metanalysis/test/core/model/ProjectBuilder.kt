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

import org.metanalysis.core.model.Project
import org.metanalysis.test.core.BuilderMarker
import org.metanalysis.test.core.Init
import org.metanalysis.test.core.apply

@BuilderMarker
class ProjectBuilder {
    private val sources = mutableListOf<SourceFileBuilder>()

    fun sourceFile(
        path: String,
        init: Init<SourceFileBuilder>
    ): ProjectBuilder {
        sources += SourceFileBuilder(path).apply(init)
        return this
    }

    fun build(): Project = Project.of(sources.map(SourceFileBuilder::build))
}
