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

import spock.lang.Specification

@Mixin(TestHelper)
class CallGraphReportNormalizerTest extends Specification {

    def norm(CallTree tree) {
        new CallGraphReportNormalizer().normalize(tree)
    }

    def element(args) {
        def ge = new CallGraphReportElement(args.index, args.thread, args.method, args.depth)
        ge.timePercent = args.timePercent
        ge.selfTime = new CallTime(args.selfTime)
        ge.childrenTime = new CallTime(args.childrenTime)
        ge.time = ge.selfTime + ge.childrenTime
        ge.calls = args.calls
        ge.recursiveCalls = args.recursiveCalls
        args.children.each {
            ge.addChild(new CallGraphReportElement.Child(it))
        }
        ge
    }

    def "Recursive calls are counted apart from non-recursive calls"() {
        when:
        def graphList = norm(tree(
            methodCallNode("A", "a", 100,
                methodCallNode("A", "a", 100,
                    methodCallNode("A", "a", 100)
                )
            ),
            methodCallNode("A", "a", 100,
                methodCallNode("A", "a", 100,
                    methodCallNode("A", "a", 100)
                )
            )
        ))
        then:
        graphList == [
            element(
                index: 1,
                thread: thread(),
                method: method("A", "a"),
                depth: 0,
                timePercent: 100,
                selfTime: 100 * 6,
                childrenTime: 0,
                calls: 6,
                recursiveCalls: 4,
                children: [],
            )
        ]
    }

    def "Method calls have the same caller are unified"() {
        when:
        def elements = norm(tree(
            methodCallNode("A", "a", 50 + 100 * 2,
                methodCallNode("A", "b", 100),
                methodCallNode("A", "b", 100),
            ),
            methodCallNode("A", "a", 50 + 100 * 1,
                methodCallNode("A", "b", 100),
            ),
        ))

        then:
        def expected = [
            element(
                index: 1,
                thread: thread(),
                method: method("A", "a"),
                depth: 0,
                timePercent: 100,
                selfTime: 50 * 2,
                childrenTime: 100 * 3,
                calls: 2,
                recursiveCalls: 0,
                children: [2]
            ),
            element(
                index: 2,
                thread: thread(),
                method: method("A", "b"),
                depth: 1,
                timePercent: 100 * 3 / (50 * 2 + 100 * 3) * 100,
                selfTime: 100 * 3,
                childrenTime: 0,
                calls: 3,
                recursiveCalls: 0,
                children: []
            ),
        ]
        elements == expected
    }

}
