/*
 * Copyright 2023 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.api.analysis

import java.util.ServiceLoader

/**
 * An [Analyzer] specification.
 *
 * Analyzer specs must have a public no-arg constructor and must supply an entry in the
 * `META-INF/services/org.chronolens.core.analysis.AnalyzerSpec` configuration file.
 */
public interface AnalyzerSpec {
  /** The name of this analyzer. */
  public val name: String

  /** The description of the analysis performed by this analyzer. */
  public val description: String

  /** Creates an [Analyzer] with the given [optionsProvider]. */
  public fun create(optionsProvider: OptionsProvider): Analyzer

  public companion object {
    /** Returns all registered analyzer specs. */
    @JvmStatic
    public fun loadAnalyzerSpecs(): Iterable<AnalyzerSpec> =
      ServiceLoader.load(AnalyzerSpec::class.java)
  }
}
