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
import java.util.List;

public class CallGraphReportElement implements ReportElement {

    public static class Child {

        private long index;

        public Child(long index) {
            this.index = index;
        }

        public long getIndex() {
            return index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Child child = (Child) o;

            if (index != child.index) return false;

            return true;
        }

        @Override
        public int hashCode() {
            return (int) (index ^ (index >>> 32));
        }

        @Override
        public String toString() {
            return "Child{" +
                    "index=" + index +
                    '}';
        }
    }

    private long index;
    private ThreadInfo thread;
    private MethodInfo method;
    private int depth;
    private float timePercent;
    private CallTime time = new CallTime(0);
    private CallTime selfTime = new CallTime(0);
    private CallTime childrenTime = new CallTime(0);
    private long calls = 0;
    private long recursiveCalls = 0;
    private List<Child> children = new ArrayList(0);

    public CallGraphReportElement(long index, ThreadInfo thread, MethodInfo method, int depth) {
        this.index = index;
        this.thread = thread;
        this.method = method;
        this.depth = depth;
    }

    public long getIndex() {
        return index;
    }

    public void setTimePercent(float timePercent) {
        this.timePercent = timePercent;
    }

    public float getTimePercent() {
        return timePercent;
    }

    public void setTime(CallTime time) {
        this.time = time;
    }

    public CallTime getTime() {
        return time;
    }

    public CallTime getSelfTime() {
        return selfTime;
    }

    public void setSelfTime(CallTime selfTime) {
        this.selfTime = selfTime;
    }

    public CallTime getChildrenTime() {
        return childrenTime;
    }

    public void setChildrenTime(CallTime childrenTime) {
        this.childrenTime = childrenTime;
    }

    public ThreadInfo getThread() {
        return thread;
    }

    public MethodInfo getMethod() {
        return method;
    }

    public void addChild(Child child) {
        children.add(child);
    }

    public List<Child> getChildren() {
        return children;
    }

    public void setCalls(long calls) {
        this.calls = calls;
    }

    public long getCalls() {
        return calls;
    }

    public void setRecursiveCalls(long recursiveCalls) {
        this.recursiveCalls = recursiveCalls;
    }

    public long getRecursiveCalls() {
        return recursiveCalls;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public String toString() {
        return "CallGraphReportElement{" +
                "index=" + index +
                ", timePercent=" + timePercent +
                ", selfTime=" + selfTime +
                ", childrenTime=" + childrenTime +
                ", thread=" + thread +
                ", method=" + method +
                ", calls=" + getCalls() +
                ", recursiveCalls=" + recursiveCalls +
                ", children=" + children +
                ", depth=" + depth +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallGraphReportElement that = (CallGraphReportElement) o;

        if (getCalls() != that.getCalls()) return false;
        if (recursiveCalls != that.recursiveCalls) return false;
        if (depth != that.depth) return false;
        if (index != that.index) return false;
        if (Float.compare(that.timePercent, timePercent) != 0) return false;
        if (children != null ? !children.equals(that.children) : that.children != null) return false;
        if (childrenTime != null ? !childrenTime.equals(that.childrenTime) : that.childrenTime != null) return false;
        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        if (selfTime != null ? !selfTime.equals(that.selfTime) : that.selfTime != null) return false;
        if (thread != null ? !thread.equals(that.thread) : that.thread != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (index ^ (index >>> 32));
        result = 31 * result + (timePercent != +0.0f ? Float.floatToIntBits(timePercent) : 0);
        result = 31 * result + (selfTime != null ? selfTime.hashCode() : 0);
        result = 31 * result + (childrenTime != null ? childrenTime.hashCode() : 0);
        result = 31 * result + (thread != null ? thread.hashCode() : 0);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (int) (getCalls() ^ (getCalls() >>> 32));
        result = 31 * result + (int) (recursiveCalls ^ (recursiveCalls >>> 32));
        result = 31 * result + (children != null ? children.hashCode() : 0);
        result = 31 * result + depth;
        return result;
    }


}
