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

package org.chronolens.test.api.analysis

import org.chronolens.api.analysis.OptionsProvider
import org.chronolens.test.BuilderMarker

@BuilderMarker
public class OptionsProviderBuilder {
  private val options = mutableMapOf<String, Any>()

  public fun <T : Any> setOption(name: String, value: T): OptionsProviderBuilder {
    options[name] = value
    return this
  }

  public fun <T : Any> setOption(name: String, value: List<T>): OptionsProviderBuilder {
    options[name] = value
    return this
  }

  public fun build(): OptionsProvider = FakeOptionsProvider(options)
}
