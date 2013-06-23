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
    
    public static class Parent {
        
        private long index;
        private CallTime time = new CallTime(0);
        private CallTime childrenTime = new CallTime(0);
        private long calls;
        
        public Parent(long index) {
            this.index = index;
        }

        public CallTime getTime() {
            return time;
        }

        public void setTime(CallTime time) {
            this.time = time;
        }

        public CallTime getSelfTime() {
            return time.minus(childrenTime);
        }

        public CallTime getChildrenTime() {
            return childrenTime;
        }

        public void setChildrenTime(CallTime childrenTime) {
            this.childrenTime = childrenTime;
        }

        public long getCalls() {
            return calls;
        }

        public void setCalls(long calls) {
            this.calls = calls;
        }

        public long getIndex() {
            return index;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            Parent parent = (Parent) o;

            if (calls != parent.calls) return false;
            if (index != parent.index) return false;
            if (childrenTime != null ? !childrenTime.equals(parent.childrenTime) : parent.childrenTime != null)
                return false;
            if (time != null ? !time.equals(parent.time) : parent.time != null) return false;
            

            return true;
        }

        @Override
        public int hashCode() {
            int result = (int) (index ^ (index >>> 32));
            result = 31 * result + (time != null ? time.hashCode() : 0);
            result = 31 * result + (childrenTime != null ? childrenTime.hashCode() : 0);
            result = 31 * result + (int) (calls ^ (calls >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "Parent{" +
                    "index=" + index +
                    ", time=" + time +
                    ", childrenTime=" + childrenTime +
                    ", calls=" + calls +
                    '}';
        }
    }
    
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
    private CallTime childrenTime = new CallTime(0);
    private long calls = 0;
    private long recursiveCalls = 0;
    private Map<Long, Parent> parents = new HashMap(0);
    private Map<Long, Child> children = new HashMap(0);

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

    public ThreadInfo getThread() {
        return thread;
    }

    public MethodInfo getMethod() {
        return method;
    }
    
    public void addParent(Parent parent) {
        parents.put(parent.getIndex(), parent);
    }
    
    public Map<Long, Parent> getParents() {
        return parents;
    }

    public void addChild(Child child) {
        children.put(child.getIndex(), child);
    }
    
    public Map<Long, Child> getChildren() {
        return children;
    }

    public CallTime getTime() {
        return time;
    }

    public void setTime(CallTime time) {
        this.time = time;
    }

    public CallTime getSelfTime() {
        return time.minus(childrenTime);
    }

    public CallTime getChildrenTime() {
        return childrenTime;
    }

    public void setChildrenTime(CallTime childrenTime) {
        this.childrenTime = childrenTime;
    }

    public long getCalls() {
        return calls;
    }

    public void setCalls(long calls) {
        this.calls = calls;
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
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallGraphReportElement that = (CallGraphReportElement) o;

        if (calls != that.calls) return false;
        if (depth != that.depth) return false;
        if (index != that.index) return false;
        if (recursiveCalls != that.recursiveCalls) return false;
        if (Float.compare(that.timePercent, timePercent) != 0) return false;
        if (children != null ? !children.equals(that.children) : that.children != null) return false;
        if (childrenTime != null ? !childrenTime.equals(that.childrenTime) : that.childrenTime != null) return false;
        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        if (parents != null ? !parents.equals(that.parents) : that.parents != null) return false;
        if (thread != null ? !thread.equals(that.thread) : that.thread != null) return false;
        if (time != null ? !time.equals(that.time) : that.time != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (index ^ (index >>> 32));
        result = 31 * result + (thread != null ? thread.hashCode() : 0);
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + depth;
        result = 31 * result + (timePercent != +0.0f ? Float.floatToIntBits(timePercent) : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (childrenTime != null ? childrenTime.hashCode() : 0);
        result = 31 * result + (int) (calls ^ (calls >>> 32));
        result = 31 * result + (int) (recursiveCalls ^ (recursiveCalls >>> 32));
        result = 31 * result + (parents != null ? parents.hashCode() : 0);
        result = 31 * result + (children != null ? children.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CallGraphReportElement{" +
                "index=" + index +
                ", thread=" + thread +
                ", method=" + method +
                ", depth=" + depth +
                ", timePercent=" + timePercent +
                ", time=" + time +
                ", childrenTime=" + childrenTime +
                ", calls=" + calls +
                ", recursiveCalls=" + recursiveCalls +
                ", parents=" + parents +
                ", children=" + children +
                '}';
    }
}
