/*
 * Copyright 2018 Andrei Heidelbacher <andrei.heidelbacher@gmail.com>
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

package org.chronolens.java

import org.junit.Test
import java.net.URL

class JavaParserIntegrationTest : JavaParserTest() {
    @Test fun `test integration`() {
        val source = javaClass.getResource("/IntegrationTest.java").readText()
        println(parse(source))
    }

    @Test fun `test network`() {
        val source = URL("https://raw.githubusercontent.com/spring-projects/spring-framework/master/spring-core/src/main/java/org/springframework/core/GenericTypeResolver.java")
            .readText()
        println(parse(source))
    }
}
