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
package groovyx.gprof

import spock.lang.Specification

import java.util.concurrent.atomic.AtomicBoolean

@Mixin(TestHelper)
class CallInterceptorTest extends Specification {
    
    def interceptedMethods(interceptor) {
        def intercepted = []
        interceptor.getTree().visit(new CallTree.NodeVisitor() {
            void visit(CallTree.Node node) {
                def call = node.data
                if (call instanceof MethodCallInfo) {
                    intercepted << call.method
                }
            }
        })
        intercepted
    }
    
    def "waits until running calls are finished"() {
        when:
        def interceptor = new CallInterceptor(new MethodCallFilter(), new ThreadRunFilter()) 
        
        AtomicBoolean invoked = new AtomicBoolean()
        Thread.start("thread-1") {
            def o1 = new ArrayList()    
            interceptor.beforeInvoke(o1, "ctor", null)
            def o2 = new String()
            interceptor.beforeInvoke(o2, "ctor", null)
            invoked.set(true) // make the main thread to get interceptor.tree
            Thread.sleep(300) // make sure the method calls are not finished when the main thread tries to get interceptor.tree
            interceptor.afterInvoke(o2, "ctor", null, o2)
            interceptor.afterInvoke(o1, "ctor", null, o1)
        }
        while (!invoked.get()) { } // waits until the method calls are started
        def intercepted = interceptedMethods(interceptor)
        
        then:
        intercepted == [
            method("java.util.ArrayList", "ctor"),
            method("java.lang.String", "ctor") ]
    }


    def "excludes and includes threads"() {
        when:
        def filter = new ThreadRunFilter();
        filter.addInclude("thread-*")
        filter.addExclude("thread-2")
        def interceptor = new CallInterceptor(new MethodCallFilter(), filter)

        Thread.start("thread-1") {
            def o = new ArrayList()
            interceptor.beforeInvoke(o, "ctor", null)
            interceptor.afterInvoke(o, "ctor", null, o)
        }.join()
        Thread.start("thread-2") {
            def o = new LinkedList()
            interceptor.beforeInvoke(o, "ctor", null)
            interceptor.afterInvoke(o, "ctor", null, o)
        }.join()

        def intercepted = interceptedMethods(interceptor)
        
        then:
        def includes = [
            method("java.util.ArrayList", "ctor") ]
        def excludes = [
            method("java.util.LinkedList", "ctor") ]
        
        intercepted.containsAll(includes) && !(intercepted.find { excludes.contains(it) })
    }

    def "excludes and includes methods"() {
        when:
        def filter = new MethodCallFilter()
        filter.addIncludes([ "java.lang.*", "java.util.*" ])
        filter.addExcludes([ "java.lang.String.*", "java.util.List.*" ])
        def interceptor = new CallInterceptor(filter, new ThreadRunFilter())

        String s = new String("a")
        interceptor.beforeInvoke(s, "ctor", null)
        interceptor.afterInvoke(s, "ctor", null, null)
        Integer i = new Integer(0)
        interceptor.beforeInvoke(i, "ctor", null)
        interceptor.afterInvoke(i, "ctor", null, null)
        List l = new ArrayList();
        interceptor.beforeInvoke(l, "ctor", null)
        interceptor.afterInvoke(l, "ctor", null, null)
        ArrayList arrayList = new ArrayList();
        interceptor.beforeInvoke(arrayList, "ctor", null)
        interceptor.afterInvoke(arrayList, "ctor", null, null)

        def intercepted = interceptedMethods(interceptor)
        
        then:
        def includes = [ 
            method("java.lang.Integer", "ctor"), 
            method("java.util.ArrayList", "ctor") ]
        def excludes = [
            method("java.lang.String", "ctor"),
            method("java.util.List", "ctor") ]
        
        intercepted.containsAll(includes) && !(intercepted.find { excludes.contains(it) })
    }

}
