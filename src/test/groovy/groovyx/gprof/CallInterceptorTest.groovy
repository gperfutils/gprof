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
    
    def "excludes and includes threads"() {
        when:
        def filter = new ThreadRunFilter();
        filter.addInclude("thread-*")
        filter.addExclude("thread-2")
        def interceptor = new CallInterceptor(new MethodCallFilter(), filter)

        Thread.start("thread-1") {
            def o = new ArrayList()
            interceptor.beforeInvoke(methodCall(o.class.name, "ctor", 0))
            interceptor.afterInvoke(methodCall(o.class.name, "ctor", 0))
        }.join()
        Thread.start("thread-2") {
            def o = new LinkedList()
            interceptor.beforeInvoke(methodCall(o.class.name, "ctor", 0))
            interceptor.afterInvoke(methodCall(o.class.name, "ctor", 0))
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
        interceptor.beforeInvoke(methodCall(s.class.name, "ctor", 0))
        interceptor.afterInvoke(methodCall(s.class.name, "ctor", 0))
        Integer i = new Integer(0)
        interceptor.beforeInvoke(methodCall(i.class.name, "ctor", 0))
        interceptor.afterInvoke(methodCall(i.class.name, "ctor", 0))
        List l = new ArrayList();
        interceptor.beforeInvoke(methodCall(l.class.name, "ctor", 0))
        interceptor.afterInvoke(methodCall(l.class.name, "ctor", 0))
        ArrayList arrayList = new ArrayList();
        interceptor.beforeInvoke(methodCall(arrayList.class.name, "ctor", 0))
        interceptor.afterInvoke(methodCall(arrayList.class.name, "ctor", 0))

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
    
    def "subtracts overheads"() {
        setup:
            def interceptor = new CallInterceptor(new MethodCallFilter(), new ThreadRunFilter())
            def calls = [
                methodCall("A", "a", 140, 20),
                methodCall("A", "b", 100, 20),
                methodCall("A", "c", 60, 20),
                methodCall("A", "d", 20, 20) ]
            calls.each {
                interceptor.beforeInvoke(it)
            } 
            calls.reverse().each {
                interceptor.afterInvoke(it)
            }
            def flatten = new groovyx.gprof.flat.FlatReportNormalizer().normalize(interceptor.tree)
            def flattenElem = flatten.find { true }.methodElements
        
        expect:
            def r = flattenElem.find { it.method.name == name }
            nano2Milli(r.time) == totalTime && nano2Milli(r.selfTime) == selfTime
            
        where:
            name | totalTime | selfTime
            "A.a"|        80 |       20
            "A.b"|        60 |       20
            "A.c"|        40 |       20
            "A.d"|        20 |       20
    }

}
