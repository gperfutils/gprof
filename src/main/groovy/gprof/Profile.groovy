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
    List<ProfileEntry> entries = []

    public Profile(ProfileTree<ProfileCallEntry> callTree) {
        Map<String, ProfileEntry> entryMap = [:]
        callTree.root.walk { node ->
            def callEntry = node.data
            def key = sprintf("%s.%s", callEntry.className, callEntry.methodName);
            def entry = entryMap[key]
            if (entry == null) {
                entry = new ProfileEntry(callEntry.className, callEntry.methodName)
                entryMap[key] = entry
                entries << entry
            }
            entry.callEntries << callEntry
            def theTime = callEntry.endTime - callEntry.startTime
            entry.time += theTime
            if (node.parent) {
                ProfileCallEntry parent = node.parent.data
                String parentId = sprintf("%s.%s", parent.className, parent.methodName);
                ProfileEntry parentEntry = entryMap[parentId]
                if (parentEntry != null) {
                    def ot = parentEntry.time
                    parentEntry.time -= theTime
                }
            }
            time += theTime
        }
        entries.each { entry ->
            entry.timePerCall = entry.time / entry.callEntries.size()
        }
    }

    void prettyPrint() {
        def columnWidth = [
                time: TIME.size(), pct: PCT.size(), calls: CALLS.size(),
                timePerCall: TIME_PER_CALL.size(), method: METHOD.size(), klass: KLASS.size()
        ]
        entries.each { entry ->
            columnWidth.time = Math.max(columnWidth.time, sprintf("%.2f", entry.time.ms()).size())
            columnWidth.pct = Math.max(columnWidth.pct, sprintf("%.2f", entry.time.ns() / time.ns() * 100).size())
            columnWidth.calls = Math.max(columnWidth.calls, entry.callEntries.size().toString().size())
            columnWidth.timePerCall = Math.max(columnWidth.timePerCall, sprintf("%d", entry.timePerCall.ns()).size())
            columnWidth.method = Math.max(columnWidth.method, entry.methodName.size())
            columnWidth.klass = Math.max(columnWidth.klass, entry.className.size())
        }
        // header
        def headerFormat =
            sprintf(
                "%%-%ds" + // pct
                "%s" +
                "%%-%ds" + // time
                "%s" +
                "%%-%ds" + // calls
                "%s" +
                "%%-%ds" + // timePerCall
                "%s" +
                "%%-%ds" + // method
                "%s" +
                "%%-%ds" + // klass
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
            sprintf(
                "%%%d.2f" + // pct
                "%s" +
                "%%%d.2f" + // time
                "%s" +
                "%%%dd" +   // calls
                "%s" +
                "%%%dd" + // timePerCall
                "%s" +
                "%%-%ds" +  // method
                "%s" +
                "%%-%ds" +  // klass
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
                columnWidth.klass
        )
        entries.sort{ a, b ->
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
                    entry.time.ns() / time.ns() * 100,
                    entry.time.ms(),
                    entry.callEntries.size(),
                    entry.timePerCall.ns(),
                    entry.methodName,
                    entry.className
            )
        }
    }
}
