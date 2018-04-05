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

package org.chronolens

import org.chronolens.core.cli.run
import picocli.CommandLine.Command

@Command(
    name = "chronolens",
    version = ["0.2"],
    mixinStandardHelpOptions = true,
    description = [
        "ChronoLens is a software evolution analysis tool that inspects the "
            + "repository detected in the current working directory."
    ]
)
class Main : Runnable {
    override fun run() {}

    companion object {
        @JvmStatic
        fun main(vararg args: String) {
            run(Main(), *args)
        }
    }
}
