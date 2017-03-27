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

package org.metanalysis.core.model

import kotlin.reflect.KClass

/**
 * The code metadata of a source file.
 *
 * @property nodes the nodes contained by this source file
 */
data class SourceFile(val nodes: Set<Node> = emptySet()) {
    @Transient private val nodeMap =
            nodes.associateBy { it::class to it.identifier }

    /**
     * Returns the node with the specified `nodeType` and `identifier`.
     *
     * @param nodeType the class object of the requested node
     * @param identifier the identifier of the requested node
     * @return the requested node, or `null` if this source file doesn't contain
     * a node with the specified `nodeType` and `identifier`
     */
    fun find(nodeType: KClass<out Node>, identifier: String): Node? =
            nodeMap[nodeType to identifier]

    /**
     * Returns the node with the specified type and `identifier`.
     *
     * @param T the type of the requested node
     * @param identifier the identifier of the requested node
     * @return the requested node, or `null` if this source file doesn't contain
     * a node with the specified type and `identifier`
     */
    inline fun <reified T : Node> find(identifier: String): T? =
            find(T::class, identifier) as T?
}
