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

package org.chronolens.api.process

import java.io.File
import java.util.ServiceLoader

/** A [ProcessExecutor] provider. */
public fun interface ProcessExecutorProvider {
  /** Returns a [ProcessExecutor] that will run processes in the given working [directory]. */
  public fun provide(directory: File): ProcessExecutor

  // TODO: think if we can get rid of this.
  public companion object {
    /**
     * The singleton [ProcessExecutorProvider] instance.
     *
     * Only other singleton instances should depend on this.
     */
    public val INSTANCE: ProcessExecutorProvider =
      ServiceLoader.load(ProcessExecutorProvider::class.java).single()
  }
}
