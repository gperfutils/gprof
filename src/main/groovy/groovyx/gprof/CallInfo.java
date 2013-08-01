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
package groovyx.gprof;

public abstract class CallInfo {

    private long time;
    private long childrenTime;

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }
    
    public long getSelfTime() {
        return time - childrenTime;
    }

    public long getChildrenTime() {
        return childrenTime;
    }

    public void setChildrenTime(long childrenTime) {
        this.childrenTime = childrenTime;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallInfo callInfo = (CallInfo) o;

        if (childrenTime != callInfo.childrenTime) return false;
        if (time != callInfo.time) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = (int) (time ^ (time >>> 32));
        result = 31 * result + (int) (childrenTime ^ (childrenTime >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "CallInfo{" +
                "time=" + time +
                ", childrenTime=" + childrenTime +
                '}';
    }
}
