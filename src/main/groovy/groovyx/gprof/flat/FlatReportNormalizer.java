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
package groovyx.gprof.flat;

import groovyx.gprof.*;

import java.util.*;

public class FlatReportNormalizer implements ReportNormalizer {

    private boolean separateThread = false;

    public void setSeparateThread(boolean separateThread) {
        this.separateThread = separateThread;
    }

    public List<FlatReportThreadElement> normalize(final CallTree callTree) {
        final List<FlatReportThreadElement> elements = new ArrayList();

        callTree.visit(new CallTree.NodeVisitor() {
            Stack<Map<MethodInfo, FlatReportMethodElement>> methodElementMapStack = new Stack();
            
            @Override
            public void exit(CallTree.Node node) {
                CallInfo call = node.getData();
                if (call instanceof ThreadRunInfo) {
                    ThreadRunInfo threadRun = (ThreadRunInfo) call;
                    if (separateThread || threadRun.equals(callTree.getRoot().getData())) {
                        Map<MethodInfo, FlatReportMethodElement> methodElementMap = methodElementMapStack.pop();
                        List<FlatReportMethodElement> methodElements = new ArrayList(methodElementMap.values());

                        // sort first for calculating the cumulative time
                        Collections.sort(methodElements, new MethodElementComparator());

                        FlatReportThreadElement threadElement = new FlatReportThreadElement(threadRun.getThread());
                        
                        long totalTime = 0;
                        for (FlatReportMethodElement methodElement : methodElements) {
                            totalTime += methodElement.getSelfTime();    
                        }
                        
                        long cumulativeTime = 0;
                        for (FlatReportMethodElement methodElement : methodElements) {
                            cumulativeTime += methodElement.getSelfTime();
                            methodElement.setTimePercent((float) methodElement.getSelfTime() / totalTime * 100);
                            methodElement.setCumulativeTime(cumulativeTime);
                            threadElement.addMethodElement(methodElement);
                        }
                        
                        elements.add(threadElement);
                    }
                }
            }

            public void visit(CallTree.Node node) {
                CallInfo call = node.getData();
                if (call instanceof  ThreadRunInfo) {
                    ThreadRunInfo threadRun = (ThreadRunInfo) call;
                    if (separateThread || threadRun.equals(callTree.getRoot().getData())) {
                        methodElementMapStack.push(new HashMap());
                    }
                } else if (call instanceof MethodCallInfo) {
                    final MethodCallInfo methodCall = (MethodCallInfo) call;
                    Map<MethodInfo, FlatReportMethodElement> methodElementMap = methodElementMapStack.peek();
                    FlatReportMethodElement element = methodElementMap.get(methodCall.getMethod());
                    if (element == null) {
                        element = new FlatReportMethodElement(methodCall.getMethod());
                        methodElementMap.put(methodCall.getMethod(), element);
                    }
                    boolean recursive = node.getParent().getData() instanceof MethodCallInfo &&
                            ((MethodCallInfo) node.getParent().getData()).getMethod().equals(methodCall.getMethod());
                    if (!recursive) {
                        element.setCalls(element.getCalls() + 1);

                        final long[] recursiveTime = {0};
                        node.visit(new CallTree.NodeVisitor() {
                            @Override
                            public void visit(CallTree.Node child) {
                                MethodCallInfo childMethodCall = (MethodCallInfo) child.getData();
                                if (childMethodCall.getMethod().equals(methodCall.getMethod())) {
                                    recursiveTime[0] += childMethodCall.getSelfTime();
                                }
                            }
                        });
                        long selfTime = methodCall.getSelfTime() + recursiveTime[0];
                        element.setSelfTime(element.getSelfTime() + selfTime);
                        element.setMaxSelfTime(Math.max(element.getMaxSelfTime(), selfTime));
                        element.setMinSelfTime(element.getMinSelfTime() == 0 ? selfTime : Math.min(element.getMinSelfTime(), selfTime));

                        long totalTime = methodCall.getTime();
                        element.setTime(element.getTime() + totalTime);
                        element.setMaxTime(Math.max(element.getMaxTime(), totalTime));
                        element.setMinTime(element.getMinTime() == 0 ? methodCall.getTime() : Math.min(element.getMinTime(), totalTime));
                    }
                }
            }
        });
        
        Collections.sort(elements, new ThreadElementComparator());
        
        return elements;
    }
    
    static class ThreadElementComparator implements Comparator<FlatReportThreadElement> {

        @Override
        public int compare(FlatReportThreadElement o1, FlatReportThreadElement o2) {
            return Long.compare(o1.getThread().getThreadId(), o2.getThread().getThreadId());
        }
        
    }
    
    static class MethodElementComparator implements Comparator<FlatReportMethodElement> {

        @Override
        public int compare(FlatReportMethodElement o1, FlatReportMethodElement o2) {
            int r = -Long.compare(o1.getSelfTime(), o2.getSelfTime());
            if (r == 0) {
                r = -(((Long) o1.getCalls()).compareTo(o2.getCalls()));
                if (r == 0) {
                    r = o1.getMethod().getName().compareTo(o2.getMethod().getName());
                }
            }
            return r;
        }
    }

}
