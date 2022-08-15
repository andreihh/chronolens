/*
 * Copyright 2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.core.analysis

import org.chronolens.core.analysis.Analyzer.Mode.FAST_HISTORY
import org.chronolens.core.analysis.Analyzer.Mode.RANDOM_ACCESS
import org.chronolens.core.repository.CorruptedRepositoryException
import org.chronolens.core.repository.PersistentRepository
import org.chronolens.core.repository.Repository
import org.chronolens.core.repository.InteractiveRepository
import java.io.File
import java.util.ServiceLoader

/**
 * A [Repository] analyzer.
 *
 * Implementations must have a public single-arg constructor that receives the [OptionsProvider].
 *
 * @param optionsProvider the provider of this analyzer's options
 */
public abstract class Analyzer(optionsProvider: OptionsProvider)
    : OptionsProvider by optionsProvider {

    /** Specifies the mode in which the analyzed repository is accessed. */
    public enum class Mode {
        /** Requires fast access to [Repository.getSource]. */
        RANDOM_ACCESS,

        /** Requires fast access to [Repository.getHistory]. */
        FAST_HISTORY,
    }

    /** The mode in which the analyzed repository is accessed. */
    public abstract val mode: Mode

    /**
     * Performs the analysis on the given [repository].
     *
     * @throws InvalidOptionException if one of the provided options are invalid
     * @throws CorruptedRepositoryException if the repository is corrupted
     */
    public abstract fun analyze(repository: Repository): Report
}

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

/**
 * Runs [this] analyzer on the repository found in the [repositoryRoot].
 *
 * @throws CorruptedRepositoryException if the repository is corrupted or if no repository could be
 * unambiguously found
 */
public fun Analyzer.analyze(repositoryRoot: File): Report =
    when (mode) {
        RANDOM_ACCESS -> analyze(InteractiveRepository.connect(repositoryRoot))
        FAST_HISTORY -> analyze(PersistentRepository.load(repositoryRoot))
    }
