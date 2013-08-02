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

import groovyx.gprof.MethodInfo;
import groovyx.gprof.ReportElement;

public class FlatReportMethodElement implements ReportElement {

    private MethodInfo method;
    private long calls;
    private float timePercent;
    private long cumulativeTime;
    private long time;
    private long selfTime;

    public FlatReportMethodElement(MethodInfo method) {
        this.method = method;
    }

    public MethodInfo getMethod() {
        return method;
    }

    public long getCalls() {
        return calls;
    }

    public void setCalls(long calls) {
        this.calls = calls;
    }

    public float getTimePerCall() {
        return time / calls;
    }
    
    public long getCumulativeTime() {
        return cumulativeTime;
    }
    
    public void setCumulativeTime(long cumulativeTime) {
        this.cumulativeTime = cumulativeTime;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }
    
    public float getSelfTimePerCall() {
        return selfTime / calls;
    }
    
    public long getSelfTime() {
        return selfTime;
    }
    
    public void setSelfTime(long selfTime) {
        this.selfTime = selfTime;
    }

    public float getTimePercent() {
        return timePercent;
    }

    public void setTimePercent(float timePercent) {
        this.timePercent = timePercent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlatReportMethodElement that = (FlatReportMethodElement) o;

        if (calls != that.calls) return false;
        if (cumulativeTime != that.cumulativeTime) return false;
        if (selfTime != that.selfTime) return false;
        if (time != that.time) return false;
        if (Float.compare(that.timePercent, timePercent) != 0) return false;
        if (method != null ? !method.equals(that.method) : that.method != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = method != null ? method.hashCode() : 0;
        result = 31 * result + (int) (calls ^ (calls >>> 32));
        result = 31 * result + (timePercent != +0.0f ? Float.floatToIntBits(timePercent) : 0);
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (int) (cumulativeTime ^ (cumulativeTime >>> 32));
        result = 31 * result + (int) (selfTime ^ (selfTime >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "FlatReportMethodElement{" +
                "method=" + method +
                ", calls=" + calls +
                ", timePercent=" + timePercent +
                ", time=" + time +
                ", cumulativeTime=" + cumulativeTime +
                ", selfTime=" + selfTime +
                '}';
    }
}
