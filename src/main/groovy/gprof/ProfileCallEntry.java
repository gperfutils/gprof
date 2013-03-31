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

public class ProfileCallEntry {

    private String className;
    private String methodName;
    private ProfileTime startTime;
    private ProfileTime endTime;

    public ProfileCallEntry(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
    }

    public void setStartTime(ProfileTime startTime) {
        this.startTime = startTime;
    }

    public void setEndTime(ProfileTime endTime) {
        this.endTime = endTime;
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public ProfileTime getStartTime() {
        return startTime;
    }

    public ProfileTime getEndTime() {
        return endTime;
    }

    @Override
    public String toString() {
        return "ProfileCallEntry{" +
                "className='" + className + '\'' +
                ", methodName='" + methodName + '\'' +
                ", startTime=" + startTime +
                ", endTime=" + endTime +
                '}';
    }
}
