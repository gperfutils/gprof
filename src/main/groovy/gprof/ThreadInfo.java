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

public class ThreadInfo {

    private String threadName;
    private long threadId;

    public ThreadInfo(String threadName, long threadId) {
        this.threadName = threadName;
        this.threadId = threadId;
    }

    public String getThreadName() {
        return threadName;
    }

    public long getThreadId() {
        return threadId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        ThreadInfo that = (ThreadInfo) o;

        if (threadId != that.threadId) return false;
        if (threadName != null ? !threadName.equals(that.threadName) : that.threadName != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = threadName != null ? threadName.hashCode() : 0;
        result = 31 * result + (int) (threadId ^ (threadId >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "ThreadInfo{" +
                "threadName='" + threadName + '\'' +
                ", threadId=" + threadId +
                '}';
    }
}
