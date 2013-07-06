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
    
    def time(long nanotime) {
        new CallTime(nanotime)
    }
    
    def parent(args) {
        def parent = new CallGraphReportSubElement.Parent(args.index)
        parent.time = time(args.time)
        parent.childrenTime = time(args.childrenTime)
        parent.calls = args.calls
        parent.recursiveCalls = args.recursiveCalls
        parent
    }
    
    def spontaneous(args) {
        parent(args + [index: 0])
    }
    
    def child(args) {
        def child = new CallGraphReportSubElement.Child(args.index)
        child
    }
    
    def element(args) {
        def e = new CallGraphReportElement(args.thread);
        args.subElements.each {
            e.addSubElement(it)
        }
        e
    }

    def subElement(args) {
        def ge = new CallGraphReportSubElement(args.index, args.method)
        args.children.each { ge.addChild(it) }
        args.parents.each { ge.addParent(it) }
        ge.timePercent = args.timePercent
        ge.time = ge.parents.inject(time(0L)) { sum, i, p -> sum + p.time }
        ge.childrenTime = ge.parents.inject(time(0L)) { sum, i, p -> sum + p.childrenTime }
        ge.calls = ge.parents.inject(0L) { sum, i, p -> sum + p.calls }
        ge.recursiveCalls = ge.parents.inject(0L) { sum, i, p -> sum + p.recursiveCalls }
        ge
    }

    def "Recursive calls are counted apart from non-recursive calls"() {
        when:
        def elements = norm(tree(
            methodCallNode("A", "a", 1500,
                methodCallNode("A", "a", 1000,
                    methodCallNode("A", "a", 500)
                )
            ),
            methodCallNode("A", "a", 1500,
                methodCallNode("A", "a", 1000,
                    methodCallNode("A", "a", 500)
                )
            )
        ))
        then:
        def expected = [
            element(
                thread: thread(),
                subElements: [
                    subElement(
                        index: 1,
                        method: method("A", "a"),
                        timePercent: 100,
                        parents: [
                            spontaneous(time: 3000, childrenTime: 0, calls: 6, recursiveCalls: 4),
                        ],
                        children: [],
                    )
                ]
            )
        ]
        elements == expected
    }

    def "Method calls have the same caller are unified"() {
        when:
        def elements = norm(tree(
            methodCallNode("A", "a", 2000,
                methodCallNode("A", "b", 500),
                methodCallNode("A", "b", 500),
            ),
            methodCallNode("A", "a", 1500,
                methodCallNode("A", "b", 500),
            ),
        ))

        then:
        def expected = [
            element(
                thread: thread(),
                subElements: [
                    subElement(
                        index: 1,
                        method: method("A", "a"),
                        timePercent: 100,
                        parents: [spontaneous(time: 3500, childrenTime: 1500, calls:2, recursiveCalls: 0)],
                        children: [child(index: 2)]
                    ),
                    subElement(
                        index: 2,
                        method: method("A", "b"),
                        timePercent: 1500 / 3500 * 100,
                        parents: [parent(index: 1, time: 1500, childrenTime: 0, calls: 3, recursiveCalls: 0)],
                        children: []
                    ),
                ]
            )
        ]
        elements == expected
    }

}
