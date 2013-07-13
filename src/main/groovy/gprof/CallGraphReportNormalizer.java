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
        final List<CallGraphReportElement> elements = new ArrayList();

        callTree.visit(new CallTree.NodeVisitor() {
            Map<MethodInfo, Long> indexTable = new HashMap();
            Map<Long, CallGraphReportWholeCycleElement> cycleTable = new HashMap();
            Stack<Long> parentStack = new Stack();
            long lastIndex = 0;
            long lastCycleIndex = 0;
            CallGraphReportElement element;
            CallGraphReportSubElement spontaneousElement;
            
            
            long searchCycleParent(long index, long parentIndex) {
                CallGraphReportSubElement parentElement = element.getSubElement(parentIndex);
                if (parentElement != null && parentElement.getCycleIndex() > 0) {
                    for (int i = 0, n = parentStack.size() - 1; i < n; i++) {
                        CallGraphReportSubElement nextParentElement = element.getSubElement(parentStack.get(i + 1));
                        if (nextParentElement.getCycleIndex() == parentElement.getCycleIndex()) {
                            return i;
                        }
                    }
                }
                return parentIndex;
            }
            
            @Override
            public void visit(CallTree.Node node) {
                CallInfo call = node.getData();
                if (call instanceof ThreadRunInfo) {
                    ThreadRunInfo threadRun = (ThreadRunInfo) call;
                    element = new CallGraphReportElement(threadRun.getThread());
                    spontaneousElement = new CallGraphReportSpontaneousElement();
                    element.addSubElement(spontaneousElement);
                    handleSubElement(node, 0, false, spontaneousElement, new CallGraphReportSubElement.Parent(-1));
                    elements.add(element);
                    for (CallTree.Node child : node.getChildren()) {
                        if (child.getData() instanceof MethodCallInfo) {
                            MethodCallInfo childMethodCall = (MethodCallInfo) child.getData();
                            long index;
                            if (indexTable.containsKey(childMethodCall.getMethod())) {
                                index = indexTable.get(childMethodCall.getMethod());
                            } else {
                                index = ++lastIndex;
                            }
                            indexTable.put(childMethodCall.getMethod(), index);
                        }
                    }
                    parentStack.push(0L);
                } else if (call instanceof MethodCallInfo) {
                    final MethodCallInfo methodCall = (MethodCallInfo) call;
                    long index = indexTable.get(methodCall.getMethod());
                    long parentIndex = parentStack.peek();
                    boolean recursive = index == parentIndex;
                    if (recursive && parentStack.size() > 1) {
                        for (int i = parentStack.size() - 1; i >= 0 && parentIndex == index; i--) {
                            parentIndex = parentStack.get(i);    
                        }
                    } else if (parentStack.size() > 2) {
                        List<Long> inCycleIndices = new ArrayList();
                        inCycleIndices.add(index);
                        inCycleIndices.add(parentIndex);
                        for (int i = parentStack.size() - 2; i > 0; i--) {
                            long pi = parentStack.get(i);
                            inCycleIndices.add(pi);
                            if (pi == index) {
                                CallGraphReportSubElement cycleEntryElement = element.getSubElement(pi);
                                boolean newCycle = cycleEntryElement.getCycleIndex() == 0;
                                long cycleIndex = newCycle ? ++lastCycleIndex : cycleEntryElement.getCycleIndex();
                                if (newCycle) {
                                    CallGraphReportWholeCycleElement cycleElement = 
                                            new CallGraphReportWholeCycleElement(++lastIndex, cycleIndex);
                                    cycleTable.put(cycleIndex, cycleElement);
                                    element.addSubElement(cycleElement);
                                }
                                for (int j = inCycleIndices.size() - 1; j > 0; j--) {
                                    CallGraphReportSubElement inCycleElement = element.getSubElement(inCycleIndices.get(j));
                                    inCycleElement.setCycleIndex(cycleIndex);
                                }
                                break;
                            }
                        }
                    }
                    CallGraphReportSubElement subElement = element.getSubElement(index);
                    if (subElement == null) {
                        subElement = new CallGraphReportSubElement(index, methodCall.getMethod());
                        element.addSubElement(subElement);
                    }
                    CallGraphReportSubElement.Parent parent;
                    parent = subElement.getParents().get(parentIndex);
                    if (parent == null) {
                        parent = new CallGraphReportSubElement.Parent(parentIndex);
                        subElement.addParent(parent);
                    }
                    handleSubElement(node, index, recursive, subElement, parent);
                    parentStack.push(index);
                }
            }

            private void handleSubElement(
                    CallTree.Node node, long index, boolean recursive,
                    CallGraphReportSubElement subElement, CallGraphReportSubElement.Parent parent) {
                CallInfo call = (CallInfo) node.getData();
                subElement.setCalls(subElement.getCalls() + 1);
                parent.setCalls(parent.getCalls() + 1);
                if (subElement.getCycleIndex() > 0) {
                    subElement.setCycleCalls(subElement.getCycleCalls() + 1);
                }

                if (!recursive) {
                    subElement.setTime(subElement.getTime().plus(call.getTime()));
                    parent.setTime(parent.getTime().plus(call.getTime()));
                }

                for (CallTree.Node child : node.getChildren()) {
                    final MethodCallInfo childMethodCall = (MethodCallInfo) child.getData();
                    if (childMethodCall.getMethod().equals(subElement.getMethod())) {
                        subElement.setRecursiveCalls(subElement.getRecursiveCalls() + 1);
                        parent.setRecursiveCalls(parent.getRecursiveCalls() + 1);
                        indexTable.put(childMethodCall.getMethod(), index);
                    } else {
                        long childIndex;
                        if (indexTable.containsKey(childMethodCall.getMethod())) {
                            childIndex = indexTable.get(childMethodCall.getMethod());
                        } else {
                            childIndex = ++lastIndex;
                            indexTable.put(childMethodCall.getMethod(), childIndex);
                        }
                        if (!subElement.getChildren().containsKey(childIndex)) {
                            subElement.addChild(new CallGraphReportSubElement.Child(childIndex));
                        }
                        subElement.setChildrenTime(subElement.getChildrenTime().plus(childMethodCall.getTime()));
                        parent.setChildrenTime(parent.getChildrenTime().plus(childMethodCall.getTime()));
                    }
                }
            }

            @Override
            public void exit(CallTree.Node node) {
                if (node.getData() instanceof ThreadRunInfo) {
                    ThreadRunInfo threadRun = (ThreadRunInfo) node.getData();
                    long totalTime = 0;
                    element.getSubElements().remove(spontaneousElement);
                    for (CallGraphReportSubElement subElement : element.getSubElements()) {
                        if (!(subElement instanceof CallGraphReportWholeCycleElement)) {
                            totalTime += subElement.getSelfTime().nanoseconds();
                        }
                    }
                    threadRun.setTime(new CallTime(totalTime));
                    threadRun.setTime(threadRun.getTime());
                    for (CallGraphReportSubElement subElement : element.getSubElements()) {
                        subElement.setTimePercent(((float) subElement.getTime().nanoseconds()) / threadRun.getTime().nanoseconds() * 100f);
                    }
                } else if (node.getData() instanceof MethodCallInfo) {
                    MethodCallInfo methodCall = (MethodCallInfo) node.getData();
                    long index = indexTable.get(methodCall.getMethod());
                    long parentIndex = parentStack.get(parentStack.size() - 2);
                    boolean recursiveCall = index == parentIndex;
                    if (recursiveCall) {
                        for (int i = parentStack.size() - 1; i >= 0 && parentIndex == index; i--) {
                            parentIndex = parentStack.get(i);
                        }
                    }
                    CallGraphReportSubElement subElement = element.getSubElement(index);
                    if (subElement.getCycleIndex() > 0) {
                        // the last element is not the parent call but the call.
                        CallGraphReportSubElement.Parent parent = subElement.getParents().get(parentIndex);
                        CallGraphReportSubElement parentElement = element.getSubElement(parentIndex);
                        for (CallTree.Node childNode : node.getChildren()) {
                            MethodCallInfo childMethodCall = (MethodCallInfo) childNode.getData();
                            long childIndex = indexTable.get(childMethodCall.getMethod());
                            boolean recursiveChildCall = childIndex == index;
                            if (!recursiveChildCall) {
                                CallGraphReportSubElement childElement = element.getSubElement(childIndex); 
                                if (childElement.getCycleIndex() == subElement.getCycleIndex()) {
                                    if (parentElement != null && parentElement.getCycleIndex() == subElement.getCycleIndex()) {
                                        parent.setTime(parent.getTime().minus(childMethodCall.getTime()));
                                        parent.setChildrenTime(parent.getChildrenTime().minus(childMethodCall.getTime()));
                                    }
                                    subElement.setTime(subElement.getTime().minus(childMethodCall.getTime()));
                                    subElement.setChildrenTime(subElement.getChildrenTime().minus(childMethodCall.getTime()));
                                }
                            }
                        }
                        long cycleParentIndex = searchCycleParent(index, parentIndex);
                        CallGraphReportWholeCycleElement cycleElement = cycleTable.get(subElement.getCycleIndex());
                        cycleElement.setCalls(cycleElement.getCalls() + 1);
                        if (parentIndex != cycleParentIndex) {
                            cycleElement.setRecursiveCalls(cycleElement.getRecursiveCalls() + 1);
                        } else {
                            cycleElement.setTime(parent.getTime());
                            cycleElement.setChildrenTime(parent.getChildrenTime());
                            cycleElement.addChild(new CallGraphReportSubElement.Child(subElement.getIndex()));
                        }
                        // when the parent of the method call isn't the parent of the cycle, 
                        // the self time of the method call has to be subtracted from the children time of the parent of the cycle.
                        if (parentIndex != cycleParentIndex) {
                            CallGraphReportSubElement cycleParentElement = element.getSubElement(cycleParentIndex);
                            CallGraphReportSubElement cycleEntryElement = null;
                            for (CallGraphReportSubElement.Child child : cycleParentElement.getChildren().values()) {
                                CallGraphReportSubElement childElement = element.getSubElement(child.getIndex());
                                if (childElement.getCycleIndex() == subElement.getCycleIndex()) {
                                    cycleEntryElement = childElement;
                                }
                            }
                            CallGraphReportSubElement.Parent cycleParent = 
                                    cycleEntryElement.getParents().get(cycleParentIndex);
                            CallTime childrenTime = new CallTime(0);
                            for (CallTree.Node childNode : node.getChildren()) {
                                MethodCallInfo childCall = (MethodCallInfo) childNode.getData();
                                // if the child call is recursion, doesn't count the time of the child call
                                // as the children time but the self time of the method call.
                                if (!childCall.getMethod().equals(methodCall.getMethod())) {
                                    childrenTime = childrenTime.plus(childCall.getTime());
                                }
                            }
                            CallTime selfTime = methodCall.getTime().minus(childrenTime);
                            cycleParent.setChildrenTime(cycleParent.getChildrenTime().minus(selfTime));
                        }
                        
                    }
                }
                parentStack.pop();
            }
        });
        return elements;
    }

}
