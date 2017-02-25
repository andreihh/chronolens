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

package org.chronos.java

import org.chronos.core.Node
import org.chronos.core.NodeVisitor

class PrettyPrinterVisitor : NodeVisitor() {
    var indent = ""

    override fun visit(type: Node.Type) {
        println("$indent${type.signature}")
        indent += "  "
    }

    override fun endVisit(type: Node.Type) {
        indent = indent.dropLast(2)
    }

    override fun visit(variable: Node.Variable) {
        println("$indent${variable.signature} = ${variable.initializer}")
        indent += "  "
    }

    override fun endVisit(variable: Node.Variable) {
        indent = indent.dropLast(2)
    }

    override fun visit(function: Node.Function) {
        println("$indent${function.signature}:\n${function.body}\n")
        indent += "  "
    }

    override fun endVisit(function: Node.Function) {
        indent = indent.dropLast(2)
    }
}
