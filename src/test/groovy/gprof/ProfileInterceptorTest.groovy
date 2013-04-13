/*
 * Copyright 2013 Masato Nagai
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package gprof

import org.junit.Test


class ProfileInterceptorTest {

    @Test void "parent calls does not contain their child calls"() {
        def depth0 = { Thread.sleep 100 }
        def depth1 = { Thread.sleep 200 }
        def depth2 = { Thread.sleep 300 }

        def interceptor = new ProfileInterceptor.LocalInterceptor();
        interceptor.beforeInvoke(depth0, "call", null)
        depth0()
        interceptor.beforeInvoke(depth1, "call", null)
        depth1()
        interceptor.beforeInvoke(depth2, "call", null)
        depth2()
        interceptor.afterInvoke(depth2, "call", null, null)
        interceptor.afterInvoke(depth1, "call", null, null)
        interceptor.afterInvoke(depth0, "call", null, null)

        interceptor.tree.visit(new ProfileCallTree.NodeVisitor() {
            def lastNode = null
            void visit(ProfileCallTree.Node node) {
                if (lastNode != null) {
                    assert node.parent == lastNode
                    assert node.data.time > lastNode.data.time
                }
                lastNode = node
            }
        })
    }
}
