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
package gprof;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Stack;

public class CallGraphReportNormalizer implements ReportNormalizer {

    public List<CallGraphReportElement> normalize(CallTree callTree) {
        final Map<Long, CallGraphReportElement> elementTable = new HashMap();

        callTree.visit(new CallTree.NodeVisitor() {
            Stack<Map<MethodInfo, Long>> indexTableStack = new Stack();
            long lastIndex = 0;
            ThreadInfo thread;
            Stack<Long> indexStack = new Stack();
            @Override
            public void visit(CallTree.Node node) {
                CallInfo call = node.getData();
                if (call instanceof ThreadRunInfo) {
                    ThreadRunInfo threadRun = (ThreadRunInfo) call;
                    thread = threadRun.getThread();
                    Map<MethodInfo, Long> indexTable = new HashMap();
                    for (CallTree.Node child : node.getChildren()) {
                        if (child.getData() instanceof MethodCallInfo) {
                            MethodCallInfo childMethodCall = (MethodCallInfo) child.getData();
                            long index;
                            if (indexTable.containsKey(childMethodCall.getMethod())) {
                               index = indexTable.get(childMethodCall.getMethod());
                            } else {
                                index = ++lastIndex;
                                indexTable.put(childMethodCall.getMethod(), index);
                            }
                        }
                    }
                    indexTableStack.push(indexTable);
                } else if (call instanceof MethodCallInfo) {
                    MethodCallInfo methodCall = (MethodCallInfo) call;
                    long index = indexTableStack.peek().get(methodCall.getMethod());

                    CallGraphReportElement element = elementTable.get(index);
                    if (element == null) {
                        element = new CallGraphReportElement(index, thread, methodCall.getMethod(), indexTableStack.size() - 1);
                        elementTable.put(index, element);
                    }
                    MethodInfo method = element.getMethod();
                    element.setCalls(element.getCalls() + 1);
                    element.setTime(element.getTime().plus(methodCall.getTime()));

                    Map<MethodInfo, Long> indexTable = new HashMap();
                    List childNames = new ArrayList();
                    for (CallGraphReportElement.Child child : element.getChildren()) {
                        indexTable.put(elementTable.get(child.getIndex()).getMethod(), child.getIndex());
                    }
                    for (CallTree.Node child : node.getChildren()) {
                        MethodCallInfo childMethodCall = (MethodCallInfo) child.getData();
                        if (childMethodCall.getMethod().equals(methodCall.getMethod())) {
                            element.setRecursiveCalls(element.getRecursiveCalls() + 1);
                            indexTable.put(childMethodCall.getMethod(), index);
                        } else {
                            if (!indexTable.containsKey(childMethodCall.getMethod())) {
                                long childIndex = ++lastIndex;
                                float childTimePercent = childMethodCall.getTime().nanoseconds() / methodCall.getTime().nanoseconds();
                                element.addChild(new CallGraphReportElement.Child(childIndex));
                                indexTable.put(childMethodCall.getMethod(), childIndex);
                            }
                            element.setChildrenTime(element.getChildrenTime().plus(childMethodCall.getTime()));
                        }
                    }
                    indexTableStack.push(indexTable);
                }
            }
            @Override
            public void exit(CallTree.Node node) {
                if (!indexTableStack.isEmpty()) {
                    indexTableStack.pop();
                }
                if (node.getData() instanceof ThreadRunInfo) {
                    ThreadRunInfo threadRun = (ThreadRunInfo) node.getData();
                    long totalTime = 0;
                    for (CallGraphReportElement graph : elementTable.values()) {
                        if (graph.getThread().equals(threadRun.getThread())) {
                            totalTime += graph.getSelfTime().nanoseconds();
                        }
                    }
                    threadRun.setTime(new CallTime(totalTime));
                    for (CallGraphReportElement graph : elementTable.values()) {
                        if (graph.getThread().equals(threadRun.getThread())) {
                            graph.setTimePercent(((float) graph.getTime().nanoseconds()) / threadRun.getTime().nanoseconds() * 100f);
                        }
                    }
                } else if (node.getData() instanceof MethodCallInfo) {
                    MethodCallInfo call = (MethodCallInfo) node.getData();
                    long index = indexTableStack.peek().get(call.getMethod());
                    CallGraphReportElement graph = elementTable.get(index);
                    graph.setSelfTime(graph.getTime().minus(graph.getChildrenTime()));
                }
            }
        });
        return new ArrayList(elementTable.values());
    }

}
