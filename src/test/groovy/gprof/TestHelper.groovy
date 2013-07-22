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

class TestHelper {

     CallTree tree(Object... nodes) {
        def tree = new CallTree(Thread.currentThread());
        nodes.each { node ->
            tree.root.addChild(node)
            tree.root.data.time += node.data.time
        }
        tree
    }

    def node(element, Object... children) {
        def node = new CallTree.Node(element)
        children.each { child ->
            node.addChild(child)
        }
        node
    }

    def thread(name = "main", long id = 1) {
        new ThreadInfo(name, id)
    }
    
    def threadRun(name = "main", long id = 1) {
        new ThreadRunInfo(thread(name, id))
    }
    
    def threadRun(ThreadInfo th) {
        new ThreadRunInfo(th)        
    }

    def threadRunNode(name = "main", long id = 1, Object... children) {
        node(threadRun(thread(name, id)), children)
    }

    def method(className, methodName) {
        new MethodInfo(className, methodName)
    }

    def methodCall(className, methodName, long ms) {
        def call = new MethodCallInfo(method(className, methodName))
        call.time = new CallTime(ms * 1000000)
        call
    }

    def methodCallNode(className, methodName, long time, Object... children) {
        node(methodCall(className, methodName, time), children)
    }

}
