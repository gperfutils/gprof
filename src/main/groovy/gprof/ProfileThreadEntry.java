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

public class ProfileThreadEntry extends ProfileEntry {

    private Thread thread;

    public ProfileThreadEntry(Thread thread) {
        this.thread = thread;
    }

    public Thread getThread() {
        return thread;
    }

    @Override
    public String toString() {
        return "ProfileThreadEntry{" +
                "thread=" + thread +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ProfileThreadEntry that = (ProfileThreadEntry) o;

        if (thread != null ? !thread.equals(that.thread) : that.thread != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        return thread != null ? thread.hashCode() : 0;
    }
}
