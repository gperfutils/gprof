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

import java.util.*;

public class CallGraphReportElement implements ReportElement {
    
    private ThreadInfo thread;
    
    private TreeMap<Long, CallGraphReportSubElement> subElements = new TreeMap();
    
    public CallGraphReportElement(ThreadInfo thread) {
        this.thread = thread;
    }

    public ThreadInfo getThread() {
        return thread;
    }
    
    public void addSubElement(CallGraphReportSubElement subElement) {
        this.subElements.put(subElement.getIndex(), subElement);
    }
    
    public CallGraphReportSubElement getSubElement(long index) {
        return this.subElements.get(index);
    }

    public Collection<CallGraphReportSubElement> getSubElements() {
        return subElements.values();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallGraphReportElement that = (CallGraphReportElement) o;

        if (subElements != null ? !subElements.equals(that.subElements) : that.subElements != null) return false;
        if (thread != null ? !thread.equals(that.thread) : that.thread != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = thread != null ? thread.hashCode() : 0;
        result = 31 * result + (subElements != null ? subElements.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CallGraphReportElement{" +
                "thread=" + thread +
                ", subElements=" + subElements +
                '}';
    }
}
