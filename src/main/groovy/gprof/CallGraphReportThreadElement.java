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

public class CallGraphReportThreadElement implements CallGraphReportElement {
    
    private ThreadInfo thread;
    
    private TreeMap<Long, CallGraphReportMethodElement> subElements = new TreeMap();
    
    public CallGraphReportThreadElement(ThreadInfo thread) {
        this.thread = thread;
    }

    public ThreadInfo getThread() {
        return thread;
    }
    
    public CallGraphReportMethodElement removeSubElement(long index) {
        return this.subElements.remove(index);
    }
    
    public void addSubElement(CallGraphReportMethodElement subElement) {
        this.subElements.put(subElement.getIndex(), subElement);
    }
    
    public CallGraphReportMethodElement getSubElement(long index) {
        return this.subElements.get(index);
    }

    public Collection<CallGraphReportMethodElement> getSubElements() {
        return subElements.values();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallGraphReportThreadElement that = (CallGraphReportThreadElement) o;

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
        return "CallGraphReportThreadElement{" +
                "thread=" + thread +
                ", subElements=" + subElements +
                '}';
    }
}
