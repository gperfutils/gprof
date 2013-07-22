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

public class CallTime implements Comparable<CallTime> {

    private long ns;

    public CallTime(long ns) {
        this.ns = ns;
    }

    public long nanoseconds() {
        return ns;
    }

    public double microseconds() {
        return ns * 0.001;
    }

    public double milliseconds() {
        return microseconds() * 0.001;
    }

    public double seconds() {
        return milliseconds() * 0.001;
    }

    public CallTime plus(CallTime other) {
        return new CallTime(nanoseconds() + other.nanoseconds());
    }

    public CallTime minus(CallTime other) {
        return new CallTime(nanoseconds() - other.nanoseconds());
    }

    public CallTime div(CallTime other) {
        return new CallTime(nanoseconds() / other.nanoseconds());
    }

    public CallTime div(long n) {
        return new CallTime(nanoseconds() / n);
    }

    public CallTime multiply(long n) {
        return new CallTime(nanoseconds() * n);
    }

    public int compareTo(CallTime another) {
        return ((Long) nanoseconds()).compareTo(another.nanoseconds());
    }

    @Override
    public String toString() {
        return "CallTime{" +
                "nanoseconds=" + ns +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        CallTime that = (CallTime) o;

        if (ns != that.ns) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (ns ^ (ns >>> 32));
    }
}
