/*
 * Copyright 2018-2022 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.decapsulations.java

import org.chronolens.core.model.Function
import org.chronolens.core.model.Identifier
import org.chronolens.core.model.QualifiedSourceNodeId
import org.chronolens.core.model.QualifiedSourceNodeId.Companion.parentId
import org.chronolens.core.model.Signature
import org.chronolens.core.model.SourceNode
import org.chronolens.core.model.SourcePath
import org.chronolens.core.model.SourceTree
import org.chronolens.core.model.Type
import org.chronolens.core.model.Variable
import org.chronolens.core.model.variable
import org.chronolens.decapsulations.DecapsulationAnalyzer

internal class JavaAnalyzer : DecapsulationAnalyzer() {
    override fun canProcess(sourcePath: SourcePath): Boolean =
        sourcePath.toString().endsWith(".java")

    private fun getFieldName(signature: String): String? =
        listOf("is", "get", "set")
            .filter { signature.startsWith(it) }
            .map { signature.removePrefix(it).substringBefore('(') }
            .firstOrNull { it.firstOrNull()?.isUpperCase() == true }
            ?.let { "${it[0].lowercaseChar()}${it.substring(1)}" }

    private fun getFieldName(signature: Signature): Identifier? =
        getFieldName(signature.toString())?.let(::Identifier)

    override fun getField(
        sourceTree: SourceTree,
        nodeId: QualifiedSourceNodeId<*>
    ): QualifiedSourceNodeId<*>? {
        val node = sourceTree[nodeId] ?: return null
        return when (node) {
            is Variable -> nodeId
            is Function -> {
                val fieldName = getFieldName(node.signature) ?: return null
                val fieldId = nodeId.parentId?.variable(fieldName) ?: return null
                // TODO: figure out if should check only for non-nullable type.
                if (sourceTree[fieldId] is Variable?) fieldId else null
            }
            else -> null
        }
    }

    private fun getVisibility(modifiers: Set<String>): Int =
        when {
            PRIVATE_MODIFIER in modifiers -> PRIVATE_LEVEL
            PROTECTED_MODIFIER in modifiers -> PROTECTED_LEVEL
            PUBLIC_MODIFIER in modifiers -> PUBLIC_LEVEL
            else -> PACKAGE_LEVEL
        }

    private val SourceNode?.isInterface: Boolean
        get() = this is Type && INTERFACE_MODIFIER in modifiers

    private fun SourceTree.parentNodeIsInterface(nodeId: QualifiedSourceNodeId<*>): Boolean {
        val parentId = nodeId.parentId ?: return false
        return get(parentId)?.isInterface ?: false
    }

    override fun getVisibility(sourceTree: SourceTree, nodeId: QualifiedSourceNodeId<*>): Int {
        val node = sourceTree[nodeId]
        return when (node) {
            is Type -> getVisibility(node.modifiers)
            is Function ->
                if (sourceTree.parentNodeIsInterface(nodeId)) PUBLIC_LEVEL
                else getVisibility(node.modifiers)
            is Variable -> getVisibility(node.modifiers)
            else -> throw AssertionError("'$nodeId' has no visibility!")
        }
    }

    override fun isConstant(sourceTree: SourceTree, nodeId: QualifiedSourceNodeId<*>): Boolean {
        val node = sourceTree[nodeId] as? Variable? ?: return false
        val modifiers = node.modifiers
        return (STATIC_MODIFIER in modifiers && FINAL_MODIFIER in modifiers) ||
            sourceTree.parentNodeIsInterface(nodeId)
    }

    companion object {
        internal const val PRIVATE_MODIFIER: String = "private"
        internal const val PROTECTED_MODIFIER: String = "protected"
        internal const val PUBLIC_MODIFIER: String = "public"
        internal const val INTERFACE_MODIFIER: String = "interface"
        internal const val STATIC_MODIFIER: String = "static"
        internal const val FINAL_MODIFIER: String = "final"

        internal const val PRIVATE_LEVEL: Int = 1
        internal const val PACKAGE_LEVEL: Int = 2
        internal const val PROTECTED_LEVEL: Int = 3
        internal const val PUBLIC_LEVEL: Int = 4
    }
}
