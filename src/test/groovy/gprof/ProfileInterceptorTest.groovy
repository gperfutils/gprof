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

    @Test void "excludes and includes threads"() {
        def filter = new ProfileThreadFilter();
        filter.addInclude("thread-")
        filter.addExclude("thread-2")
        def interceptor = new ProfileInterceptor(new ProfileMethodFilter(), filter)

        def excludes = []
        def includes = []
        Thread.start("thread-1") {
            def o = new ArrayList()
            interceptor.beforeInvoke(o, "ctor", null)
            interceptor.afterInvoke(o, "ctor", null, o)
            includes << "java.util.ArrayList.ctor"
        }
        Thread.start("thread-2") {
            def o = new LinkedList()
            interceptor.beforeInvoke(o, "ctor", null)
            interceptor.afterInvoke(o, "ctor", null, o)
            excludes << "java.util.LinkedList.ctor"
        }

        interceptor.tree.visit(new ProfileCallTree.NodeVisitor() {
            void visit(ProfileCallTree.Node node) {
                def name = node.data.className + "." + node.data.methodName
                assert includes.contains(name)
                assert !excludes.contains(name)
            }
        })
    }

    @Test void "excludes and includes methods"() {
        def filter = new ProfileMethodFilter()
        filter.addIncludes([ "java.lang.*", "java.util.*" ])
        filter.addExcludes([ "java.lang.String.*", "java.util.List.*" ])
        def interceptor = new ProfileInterceptor(filter, new ProfileThreadFilter())

        def excludes = []
        def includes = []

        String s = new String("a")
        interceptor.beforeInvoke(s, "ctor", null)
        interceptor.afterInvoke(s, "ctor", null, null)
        excludes << "java.lang.String.ctor"
        Integer i = new Integer(0)
        interceptor.beforeInvoke(i, "ctor", null)
        interceptor.afterInvoke(i, "ctor", null, null)
        includes << "java.lang.Integer.ctor"
        List l = new ArrayList();
        interceptor.beforeInvoke(l, "ctor", null)
        interceptor.afterInvoke(l, "ctor", null, null)
        excludes << "java.util.List.ctor"
        ArrayList arrayList = new ArrayList();
        interceptor.beforeInvoke(arrayList, "ctor", null)
        interceptor.afterInvoke(arrayList, "ctor", null, null)
        includes << "java.util.ArrayList.ctor"

        interceptor.tree.visit(new ProfileCallTree.NodeVisitor() {
            void visit(ProfileCallTree.Node node) {
                def name = node.data.className + "." + node.data.methodName
                assert includes.contains(name)
                assert !excludes.contains(name)
            }
        })

    }

    @Test void "parent calls does not contain their child calls"() {
        def depth0 = { Thread.sleep 100 }
        def depth1 = { Thread.sleep 200 }
        def depth2 = { Thread.sleep 300 }

        def interceptor = new ProfileInterceptor(new ProfileMethodFilter(), new ProfileThreadFilter());
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
