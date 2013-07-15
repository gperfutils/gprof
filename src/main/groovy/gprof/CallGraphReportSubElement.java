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

public class CallGraphReportSubElement implements ReportElement {
    
    public static class Parent {
        
        private long index;
        private CallTime time = new CallTime(0);
        private CallTime childrenTime = new CallTime(0);
        private long calls;
        private long recursiveCalls;
        private long cycleCalls;
        
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

        public long getRecursiveCalls() {
            return recursiveCalls;
        }

        public void setRecursiveCalls(long recursiveCalls) {
            this.recursiveCalls = recursiveCalls;
        }

        public long getCycleCalls() {
            return cycleCalls;
        }

        public void setCycleCalls(long cycleCalls) {
            this.cycleCalls = cycleCalls;
        }
        
        public void setIndex(long index) {
            this.index = index;
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
            if (recursiveCalls != parent.recursiveCalls) return false;
            if (cycleCalls != parent.cycleCalls) return false;
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
            result = 31 * result + (int) (recursiveCalls ^ (recursiveCalls >>> 32));
            result = 31 * result + (int) (cycleCalls ^ (cycleCalls >>> 32));
            return result;
        }

        @Override
        public String toString() {
            return "Parent{" +
                    "index=" + index +
                    ", time=" + time +
                    ", childrenTime=" + childrenTime +
                    ", calls=" + calls +
                    ", recursiveCalls=" + recursiveCalls +
                    ", cycleCalls=" + cycleCalls +
                    '}';
        }
    }
    
    public static class Child {
        
        private long index;
        
        public Child(long index) {
            this.index = index;
        }
        
        public void setIndex(long index) {
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
    private long cycleIndex;
    private MethodInfo method;
    private float timePercent;
    private CallTime time = new CallTime(0);
    private CallTime childrenTime = new CallTime(0);
    private long calls = 0;
    private long recursiveCalls = 0;
    private long cycleCalls = 0;
    private SortedMap<Long, Parent> parents = new TreeMap();
    private SortedMap<Long, Child> children = new TreeMap();

    public CallGraphReportSubElement(long index, MethodInfo method) {
        this.index = index;
        this.method = method;
    }
    
    public void setIndex(long index) {
        this.index = index;    
    }

    public long getIndex() {
        return index;
    }

    public long getCycleIndex() {
        return cycleIndex;
    }

    public void setCycleIndex(long cycleIndex) {
        this.cycleIndex = cycleIndex;
    }

    public long getCycleCalls() {
        return cycleCalls;
    }

    public void setCycleCalls(long cycleCalls) {
        this.cycleCalls = cycleCalls;
    }

    public void setTimePercent(float timePercent) {
        this.timePercent = timePercent;
    }

    public float getTimePercent() {
        return timePercent;
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallGraphReportSubElement that = (CallGraphReportSubElement) o;

        if (calls != that.calls) return false;
        if (index != that.index) return false;
        if (recursiveCalls != that.recursiveCalls) return false;
        if (cycleCalls != that.cycleCalls) return false;
        if (Float.compare(that.timePercent, timePercent) != 0) return false;
        if (children != null ? !children.equals(that.children) : that.children != null) return false;
        if (childrenTime != null ? !childrenTime.equals(that.childrenTime) : that.childrenTime != null) return false;
        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        if (parents != null ? !parents.equals(that.parents) : that.parents != null) return false;
        if (time != null ? !time.equals(that.time) : that.time != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (index ^ (index >>> 32));
        result = 31 * result + (method != null ? method.hashCode() : 0);
        result = 31 * result + (timePercent != +0.0f ? Float.floatToIntBits(timePercent) : 0);
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (childrenTime != null ? childrenTime.hashCode() : 0);
        result = 31 * result + (int) (calls ^ (calls >>> 32));
        result = 31 * result + (int) (recursiveCalls ^ (recursiveCalls >>> 32));
        result = 31 * result + (int) (cycleCalls ^ (cycleCalls >>> 32));
        result = 31 * result + (parents != null ? parents.hashCode() : 0);
        result = 31 * result + (children != null ? children.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        return "CallGraphReportSubElement{" +
                "index=" + index +
                ", method=" + method +
                ", timePercent=" + timePercent +
                ", time=" + time +
                ", childrenTime=" + childrenTime +
                ", calls=" + calls +
                ", recursiveCalls=" + recursiveCalls +
                ", cycleCalls=" + cycleCalls +
                ", parents=" + parents +
                ", children=" + children +
                '}';
    }
}
