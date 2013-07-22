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
        final CallTime[] time = { new CallTime(0) };

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
                    CallTime theTime = methodCall.getTime();
                    if (element.getTime() == null) {
                        element.setTime(theTime);
                    } else {
                        element.setTime(element.getTime().plus(theTime));
                    }
                    if (element.getMinTime() == null || element.getMinTime().compareTo(theTime) > 0) {
                        element.setMinTime(theTime);
                    }
                    if (element.getMaxTime() == null || element.getMaxTime().compareTo(theTime) < 0) {
                        element.setMaxTime(theTime);
                    }
                    time[0] = time[0].plus(theTime);
                }
            }
        });
        for (FlatReportElement entry : elements) {
            entry.setTimePercent(entry.getTime().milliseconds() / time[0].milliseconds() * 100);
            entry.setTimePerCall(entry.getTime().div(entry.getCalls()));
        }
        return elements;
    }

}
