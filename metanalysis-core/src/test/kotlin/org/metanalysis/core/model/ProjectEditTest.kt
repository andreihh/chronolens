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

import org.junit.Test
import org.metanalysis.core.model.ProjectEdit.Companion.diff
import org.metanalysis.test.core.model.assertEquals
import org.metanalysis.test.core.model.project

class ProjectEditTest {
    private fun assertDiff(before: Project, after: Project) {
        val edits = before.diff(after)
        val actualAfter = Project.of(before.sources)
        actualAfter.apply(edits)
        assertEquals(after, actualAfter)
    }

    @Test
    fun `test diff equal projects`() {
        val before = project {
            sourceUnit("src/Main.java") {
                type("Main") {
                    function("getVersion(String)") {
                        parameters("name")
                    }
                }
            }
        }

        val after = project {
            sourceUnit("src/Main.java") {
                type("Main") {
                    function("getVersion(String)") {
                        parameters("name")
                    }
                }
            }
        }

        assertDiff(before, after)
    }


    @Test fun `test diff change type modifiers`() {
        val before = project {
            sourceUnit("src/Main.java") {
                type("Main") {}
            }
        }

        val after = project {
            sourceUnit("src/Main.java") {
                type("Main") {
                    modifiers("interface")
                }
            }
        }

        assertDiff(before, after)
    }

    @Test fun `test diff change type supertypes`() {
        val before = project {
            sourceUnit("src/Main.java") {
                type("Main") {}
            }
        }

        val after = project {
            sourceUnit("src/Main.java") {
                type("Main") {
                    supertypes("Object")
                }
            }
        }

        assertDiff(before, after)
    }

    @Test fun `test diff change function modifiers`() {
        val before = project {
            sourceUnit("src/Test.java") {
                function("getVersion()") {}
            }
        }

        val after = project {
            sourceUnit("src/Test.java") {
                function("getVersion()") {
                    modifiers("abstract")
                }
            }
        }

        assertDiff(before, after)
    }

    @Test fun `test diff change function parameters`() {
        val before = project {
            sourceUnit("src/Test.java") {
                function("getVersion(String, int)") {
                    parameters("name", "revision")
                }
            }
        }

        val after = project {
            sourceUnit("src/Test.java") {
                function("getVersion(String, int)") {
                    parameters("className", "revision")
                }
            }
        }

        assertDiff(before, after)
    }

    @Test fun `test diff change function body`() {
        val before = project {
            sourceUnit("src/Test.java") {
                function("getVersion()") { +"DEBUG" }
            }
        }

        val after = project {
            sourceUnit("src/Test.java") {
                function("getVersion()") { +"RELEASE" }
            }
        }

        assertDiff(before, after)
    }

    @Test fun `test diff change variable modifiers`() {
        val before = project {
            sourceUnit("src/Test.java") {
                variable("version") {
                    modifiers("public")
                }
            }
        }

        val after = project {
            sourceUnit("src/Test.java") {
                variable("version") {}
            }
        }

        assertDiff(before, after)
    }

    @Test fun `test diff change variable initializer`() {
        val before = project {
            sourceUnit("src/Test.java") {
                variable("version") { +"DEBUG" }
            }
        }

        val after = project {
            sourceUnit("src/Test.java") {
                variable("version") { +"RELEASE" }
            }
        }

        assertDiff(before, after)
    }
}
