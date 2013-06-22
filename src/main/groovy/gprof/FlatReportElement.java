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

public class FlatReportElement implements ReportElement {

    private MethodInfo method;
    private String name;
    private long calls;
    private double timePercent;
    private CallTime time;
    private CallTime minTime;
    private CallTime maxTime;
    private CallTime timePerCall;

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

    public CallTime getTimePerCall() {
        return timePerCall;
    }

    public CallTime getTime() {
        return time;
    }

    public void setTime(CallTime time) {
        this.time = time;
    }

    public CallTime getMinTime() {
        return minTime;
    }

    public void setMinTime(CallTime minTime) {
        this.minTime = minTime;
    }

    public CallTime getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(CallTime maxTime) {
        this.maxTime = maxTime;
    }

    public void setTimePerCall(CallTime timePerCall) {
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
        if (Double.compare(that.timePercent, timePercent) != 0) return false;
        if (maxTime != null ? !maxTime.equals(that.maxTime) : that.maxTime != null) return false;
        if (method != null ? !method.equals(that.method) : that.method != null) return false;
        if (minTime != null ? !minTime.equals(that.minTime) : that.minTime != null) return false;
        if (name != null ? !name.equals(that.name) : that.name != null) return false;
        if (time != null ? !time.equals(that.time) : that.time != null) return false;
        if (timePerCall != null ? !timePerCall.equals(that.timePerCall) : that.timePerCall != null) return false;

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
        result = 31 * result + (time != null ? time.hashCode() : 0);
        result = 31 * result + (minTime != null ? minTime.hashCode() : 0);
        result = 31 * result + (maxTime != null ? maxTime.hashCode() : 0);
        result = 31 * result + (timePerCall != null ? timePerCall.hashCode() : 0);
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
