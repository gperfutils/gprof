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

class Mock {

     def treeMock(Object... nodes) {
        def tree = new ProfileCallTree(Thread.currentThread());
        nodes.each { node ->
            tree.root.addChild(node)
        }
        tree
    }

    def nodeMock(entry, Object... children) {
        def node = new ProfileCallTree.Node(entry)
        children.each { child ->
            node.addChild(child)
        }
        node
    }

    def threadNodeMock(threadName, Object... children) {
        def thread = new ProfileThreadEntry(new Thread(threadName))
        nodeMock(thread, children)
    }

    def methodNodeMock(className, methodName, long time, Object... children) {
        nodeMock(methodCall(className, methodName, time), children)
    }

    def methodCall(className, methodName, long time) {
        def call = new ProfileCallEntry(className, methodName)
        call.time = new ProfileTime(time)
        call
    }

    def threadEntry(thread = Thread.currentThread()) {
        new ProfileThreadEntry(thread)
    }

    def methodEntry(className, methodName) {
        def me = new ProfileMethodEntry(className, methodName)
        me
    }


}
