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

public class ProfileGraphEntry extends ProfileEntry {

    public static class Child {

        private long index;
        private float timePercent;

        public Child(long index, float timePercent) {
            this.index = index;
            this.timePercent = timePercent;
        }

        public long getIndex() {
            return index;
        }

        public float getTimePercent() {
            return timePercent;
        }
    }

    private long index;
    private float timePercent;
    private ProfileTime selfTime = new ProfileTime(0);
    private ProfileTime childrenTime = new ProfileTime(0);
    private ProfileThreadEntry thread;
    private ProfileMethodEntry method;
    private List<Child> children = new ArrayList(0);
    private int depth;

    public ProfileGraphEntry(long index, ProfileThreadEntry thread, ProfileMethodEntry method, int depth) {
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

    public ProfileTime getSelfTime() {
        return selfTime;
    }

    public void setSelfTime(ProfileTime selfTime) {
        this.selfTime = selfTime;
    }

    public ProfileTime getChildrenTime() {
        return childrenTime;
    }

    public void setChildrenTime(ProfileTime childrenTime) {
        this.childrenTime = childrenTime;
    }

    public ProfileThreadEntry getThread() {
        return thread;
    }

    public ProfileMethodEntry getMethod() {
        return method;
    }

    public void addChild(Child child) {
        children.add(child);
    }

    public List<Child> getChildren() {
        return children;
    }

    public int getDepth() {
        return depth;
    }

    @Override
    public String toString() {
        return String.format("[%d] %s", index, method);
    }
}
