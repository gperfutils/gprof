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

    def thread(Thread th = Thread.currentThread()) {
        new ThreadInfo(th.name, th.id)
    }

    def threadRun(Thread th = Thread.currentThread()) {
        new ThreadRunInfo(thread(th))
    }

    def threadRunNode(Thread th = Thread.currentThread(), Object... children) {
        node(threadRun(th), children)
    }

    def method(className, methodName) {
        new MethodInfo(className, methodName)
    }

    def methodCall(className, methodName, long us) {
        def call = new MethodCallInfo(method(className, methodName))
        call.time = new CallTime(us * 1000)
        call
    }

    def methodCallNode(className, methodName, long time, Object... children) {
        node(methodCall(className, methodName, time), children)
    }

}
