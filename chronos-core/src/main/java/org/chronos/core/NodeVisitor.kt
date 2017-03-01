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

package org.chronos.core

import org.chronos.core.Node.Function
import org.chronos.core.Node.Type
import org.chronos.core.Node.Variable

/** An abstract node and source file metadata processor. */
abstract class NodeVisitor {
    protected open fun visit(type: Type) {}
    protected open fun visit(variable: Variable) {}
    protected open fun visit(function: Function) {}
    protected open fun endVisit(type: Type) {}
    protected open fun endVisit(variable: Variable) {}
    protected open fun endVisit(function: Function) {}

    private fun visitType(type: Type) {
        visit(type)
        type.members.forEach(this::visit)
        endVisit(type)
    }

    private fun visitVariable(variable: Variable) {
        visit(variable)
        endVisit(variable)
    }

    private fun visitFunction(function: Function) {
        visit(function)
        endVisit(function)
    }

    /**
     * Processes the code metadata in the given `node`.
     *
     * @param node the processed node
     */
    fun visit(node: Node) {
        when (node) {
            is Type -> visitType(node)
            is Variable -> visitVariable(node)
            is Function -> visitFunction(node)
        }
    }

    /**
     * Processes the code metadata in the given source file.
     *
     * @param sourceFile the processed source file
     */
    fun visit(sourceFile: SourceFile) {
        sourceFile.nodes.forEach { visit(it) }
    }
}
