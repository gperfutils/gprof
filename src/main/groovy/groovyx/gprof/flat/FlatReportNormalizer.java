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

    public List<FlatReportMethodElement> normalize(CallTree callTree) {
        final List<FlatReportMethodElement> elements = new ArrayList();
        final long[] time = { 0L };

        callTree.visit(new CallTree.NodeVisitor() {

            Map<String, FlatReportMethodElement> entryMap = new HashMap();

            public void visit(CallTree.Node node) {
                CallInfo call = node.getData();
                if (call instanceof MethodCallInfo) {
                    MethodCallInfo methodCall = (MethodCallInfo) call;
                    String key = methodCall.getMethod().getName();
                    FlatReportMethodElement element = entryMap.get(key);
                    if (element == null) {
                        element = new FlatReportMethodElement(methodCall.getMethod());
                        entryMap.put(key, element);
                        elements.add(element);
                    }
                    element.setCalls(element.getCalls() + 1);
                    long theTime = methodCall.getSelfTime();
                    element.setSelfTime(element.getSelfTime() + theTime);
                    element.setTime(element.getTime() + methodCall.getTime());
                    time[0] += theTime;
                }
            }
        });
        
        Collections.sort(elements, new Comparator());
        
        long cumulativeTime = 0;
        for (FlatReportMethodElement e : elements) {
            cumulativeTime += e.getSelfTime();  
            e.setTimePercent((float) e.getSelfTime() / time[0] * 100);
            e.setCumulativeTime(cumulativeTime);
        }
        return elements;
    }
    
    static class Comparator implements java.util.Comparator<FlatReportMethodElement> {

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
