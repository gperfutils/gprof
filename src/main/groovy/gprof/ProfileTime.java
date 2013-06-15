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

public class ProfileTime implements Comparable<ProfileTime> {

    private long ns;

    public ProfileTime(long ns) {
        this.ns = ns;
    }

    public long nanoseconds() {
        return ns;
    }

    public double microseconds() {
        return ns / 1000;
    }

    public double milliseconds() {
        return microseconds() / 1000;
    }

    public double seconds() {
        return milliseconds() / 1000;
    }

    public ProfileTime plus(ProfileTime other) {
        return new ProfileTime(nanoseconds() + other.nanoseconds());
    }

    public ProfileTime minus(ProfileTime other) {
        return new ProfileTime(nanoseconds() - other.nanoseconds());
    }

    public ProfileTime div(ProfileTime other) {
        return new ProfileTime(nanoseconds() / other.nanoseconds());
    }

    public ProfileTime div(int i) {
        return new ProfileTime(nanoseconds() / i);
    }

    public ProfileTime multiply(int i) {
        return new ProfileTime(nanoseconds() * i);
    }

    public int compareTo(ProfileTime another) {
        return ((Long) nanoseconds()).compareTo(another.nanoseconds());
    }

    @Override
    public String toString() {
        return "ProfileTime{" +
                "nanoseconds=" + ns +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProfileTime that = (ProfileTime) o;

        if (ns != that.ns) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return (int) (ns ^ (ns >>> 32));
    }
}
