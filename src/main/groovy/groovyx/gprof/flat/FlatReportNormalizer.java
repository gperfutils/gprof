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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FlatReportNormalizer implements ReportNormalizer {

    public List<FlatReportElement> normalize(CallTree callTree) {
        final List<FlatReportElement> elements = new ArrayList();
        final long[] time = { 0L };

        callTree.visit(new CallTree.NodeVisitor() {

            Map<String, FlatReportElement> entryMap = new HashMap();

            public void visit(CallTree.Node node) {
                CallInfo call = node.getData();
                if (call instanceof MethodCallInfo) {
                    MethodCallInfo methodCall = (MethodCallInfo) call;
                    String key = methodCall.getMethod().getName();
                    FlatReportElement element = entryMap.get(key);
                    if (element == null) {
                        element = new FlatReportElement(methodCall.getMethod());
                        entryMap.put(key, element);
                        elements.add(element);
                    }
                    element.setCalls(element.getCalls() + 1);
                    long theTime = methodCall.getSelfTime();
                    element.setTime(element.getTime() + theTime);
                    element.setMinTime(element.getMinTime() > 0 ? Math.min(element.getMinTime(), theTime) : theTime);
                    element.setMaxTime(Math.max(element.getMaxTime(), theTime));
                    time[0] += theTime;
                }
            }
        });
        for (FlatReportElement e : elements) {
            e.setTimePercent((float) e.getTime() / time[0] * 100);
            e.setTimePerCall(e.getTime() / e.getCalls());
        }
        return elements;
    }

}
