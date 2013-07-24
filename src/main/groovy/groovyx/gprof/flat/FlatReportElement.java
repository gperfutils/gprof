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

public class FlatReportElement implements ReportElement {

    private MethodInfo method;
    private String name;
    private long calls;
    private double timePercent;
    private long time;
    private long minTime;
    private long maxTime;
    private long timePerCall;

    public FlatReportElement(MethodInfo method) {
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

    public long getTimePerCall() {
        return timePerCall;
    }

    public long getTime() {
        return time;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getMinTime() {
        return minTime;
    }

    public void setMinTime(long minTime) {
        this.minTime = minTime;
    }

    public long getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(long maxTime) {
        this.maxTime = maxTime;
    }

    public void setTimePerCall(long timePerCall) {
        this.timePerCall = timePerCall;
    }

    public double getTimePercent() {
        return timePercent;
    }

    public void setTimePercent(double timePercent) {
        this.timePercent = timePercent;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FlatReportElement that = (FlatReportElement) o;

        if (calls != that.calls) return false;
        if (maxTime != that.maxTime) return false;
        if (minTime != that.minTime) return false;
        if (time != that.time) return false;
        if (timePerCall != that.timePerCall) return false;
        if (Double.compare(that.timePercent, timePercent) != 0) return false;
        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        long temp;
        result = method != null ? method.hashCode() : 0;
        result = 31 * result + (name != null ? name.hashCode() : 0);
        result = 31 * result + (int) (calls ^ (calls >>> 32));
        temp = Double.doubleToLongBits(timePercent);
        result = 31 * result + (int) (temp ^ (temp >>> 32));
        result = 31 * result + (int) (time ^ (time >>> 32));
        result = 31 * result + (int) (minTime ^ (minTime >>> 32));
        result = 31 * result + (int) (maxTime ^ (maxTime >>> 32));
        result = 31 * result + (int) (timePerCall ^ (timePerCall >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "FlatReportElement{" +
                "method=" + method +
                ", name='" + name + '\'' +
                ", calls=" + calls +
                ", timePercent=" + timePercent +
                ", time=" + time +
                ", minTime=" + minTime +
                ", maxTime=" + maxTime +
                ", timePerCall=" + timePerCall +
                '}';
    }
}
