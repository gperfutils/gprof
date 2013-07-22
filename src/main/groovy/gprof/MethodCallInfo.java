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

public class MethodCallInfo extends CallInfo {

    private MethodInfo method;
    private long calls;

    public MethodCallInfo(String className, String methodName) {
        this(new MethodInfo(className, methodName));
    }

    public MethodCallInfo(MethodInfo method) {
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

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MethodCallInfo that = (MethodCallInfo) o;

        if (calls != that.calls) return false;
        if (method != null ? !method.equals(that.method) : that.method != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result = method != null ? method.hashCode() : 0;
        result = 31 * result + (int) (calls ^ (calls >>> 32));
        return result;
    }

    @Override
    public String toString() {
        return "MethodCallInfo{" +
                "method=" + method +
                ", calls=" + calls +
                '}';
    }
}
