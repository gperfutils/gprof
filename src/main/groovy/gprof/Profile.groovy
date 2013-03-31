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
package gprof

class Profile {

    static String TIME = "ms"
    static String PCT = "%"
    static String CALLS = "calls"
    static String TIME_PER_CALL = "ns/call"
    static String METHOD = "method"
    static String KLASS = "class"
    static String SP = "  "

    ProfileTime time = new ProfileTime(0L)
    List<ProfileMethodEntry> entries = []

    public Profile(ProfileCallTree callTree) {
        Map<String, ProfileMethodEntry> entryMap = [:]
        callTree.root.walk { node ->
            def callEntry = node.data
            def key = sprintf("%s.%s", callEntry.className, callEntry.methodName);
            def entry = entryMap[key]
            if (entry == null) {
                entry = new ProfileMethodEntry(callEntry.className, callEntry.methodName)
                entryMap[key] = entry
                entries << entry
            }
            entry.callEntries << callEntry
            def theTime = callEntry.endTime - callEntry.startTime
            entry.time += theTime
            if (node.parent) {
                ProfileCallEntry parent = node.parent.data
                if (parent != null) {
                    String parentId = sprintf("%s.%s", parent.className, parent.methodName);
                    ProfileMethodEntry parentEntry = entryMap[parentId]
                    def ot = parentEntry.time
                    parentEntry.time -= theTime
                }
            }
            time += theTime
        }
        entries.each { entry -> entry.timePerCall = entry.time / entry.callEntries.size() }
    }

    void prettyPrint() {
        def columnWidth = [time: TIME.size(), pct: PCT.size(), calls: CALLS.size(),
            timePerCall: TIME_PER_CALL.size(), method: METHOD.size(), klass: KLASS.size()]
        entries.each { entry ->
            columnWidth.time = Math.max(columnWidth.time, sprintf("%.2f", entry.time.milliseconds()).size())
            columnWidth.pct = Math.max(columnWidth.pct, sprintf("%.2f", entry.time.nanoseconds() / time.nanoseconds() * 100).size())
            columnWidth.calls = Math.max(columnWidth.calls, entry.callEntries.size().toString().size())
            columnWidth.timePerCall = Math.max(columnWidth.timePerCall, sprintf("%d", entry.timePerCall.nanoseconds()).size())
            columnWidth.method = Math.max(columnWidth.method, entry.methodName.size())
            columnWidth.klass = Math.max(columnWidth.klass, entry.className.size())
        }
        // header
        def headerFormat =
            sprintf("%%-%ds" + // pct
                "%s" + "%%-%ds" + // time
                "%s" + "%%-%ds" + // calls
                "%s" + "%%-%ds" + // timePerCall
                "%s" + "%%-%ds" + // method
                "%s" + "%%-%ds" + // klass
                "%n"
                ,
                columnWidth.pct,
                SP,
                columnWidth.time,
                SP,
                columnWidth.calls,
                SP,
                columnWidth.timePerCall,
                SP,
                columnWidth.method,
                SP,
                columnWidth.klass)
        printf(headerFormat, PCT, TIME, CALLS, TIME_PER_CALL, METHOD, KLASS)

        // rows
        def rowFormat =
            sprintf("%%%d.2f" + // pct
                "%s" + "%%%d.2f" + // time
                "%s" + "%%%dd" + // calls
                "%s" + "%%%dd" + // timePerCall
                "%s" + "%%-%ds" + // method
                "%s" + "%%-%ds" + // klass
                "%n"
                ,
                columnWidth.pct,
                SP,
                columnWidth.time,
                SP,
                columnWidth.calls,
                SP,
                columnWidth.timePerCall,
                SP,
                columnWidth.method,
                SP,
                columnWidth.klass)
        entries.sort { a, b ->
            def r
            r = a.time <=> b.time
            if (r != 0) return r
            r = a.callEntries.size() <=> b.callEntries.size()
            if (r != 0) return r
            r = -(a.className <=> b.className)
            if (r != 0) return r
            r = -(a.methodName <=> b.methodName)
            return r
        }.reverse().each { entry ->
            // todo better sort  (time -> calls -> name)
            printf(rowFormat,
                entry.time.nanoseconds() / time.nanoseconds() * 100,
                entry.time.milliseconds(),
                entry.callEntries.size(),
                entry.timePerCall.nanoseconds(),
                entry.methodName,
                entry.className)
        }
    }
}
