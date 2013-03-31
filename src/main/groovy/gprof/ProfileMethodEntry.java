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

import java.util.ArrayList;
import java.util.List;

public class ProfileMethodEntry {

    private String className;
    private String methodName;
    private List<ProfileCallEntry> callEntries;
    private ProfileTime time;
    private ProfileTime minTime;
    private ProfileTime maxTime;
    private ProfileTime timePerCall;

    public ProfileMethodEntry(String className, String methodName) {
        this.className = className;
        this.methodName = methodName;
        this.callEntries = new ArrayList();
    }

    public String getClassName() {
        return className;
    }

    public String getMethodName() {
        return methodName;
    }

    public List<ProfileCallEntry> getCallEntries() {
        return callEntries;
    }

    public ProfileTime getTime() {
        return time;
    }

    public ProfileTime getTimePerCall() {
        return timePerCall;
    }

    public ProfileTime getMinTime() {
        return minTime;
    }

    public void setMinTime(ProfileTime minTime) {
        this.minTime = minTime;
    }

    public ProfileTime getMaxTime() {
        return maxTime;
    }

    public void setMaxTime(ProfileTime maxTime) {
        this.maxTime = maxTime;
    }

    public void setTime(ProfileTime time) {
        this.time = time;
    }

    public void setTimePerCall(ProfileTime timePerCall) {
        this.timePerCall = timePerCall;
    }
}
