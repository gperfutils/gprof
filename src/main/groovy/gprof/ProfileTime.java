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
    private double ns;
    public ProfileTime(double ns) {
        this.ns = ns;
    }
    public double ns() {
        return ns;
    }
    public double us() {
        return ns / 1000;
    }
    public double ms() {
        return us() / 1000;
    }
    public double s() {
        return ms() / 1000;
    }
    public ProfileTime plus(ProfileTime other) {
        return new ProfileTime(ns() + other.ns());
    }
    public ProfileTime div(ProfileTime other) {
        return new ProfileTime(ns() / other.ns());
    }
    public ProfileTime div(int i) {
        return new ProfileTime(ns() / i);
    }
    public ProfileTime multiply(int i) {
        return new ProfileTime(ns() * i);
    }
    public int compareTo(ProfileTime another) {
        return ((Double) ns()).compareTo(another.ns());
    }
}
