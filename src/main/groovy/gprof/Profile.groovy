package gprof

class Profile {

    static String TIME = "ms"
    static String PCT = "%"
    static String CALLS = "calls"
    static String TIME_PER_CALL = "us/call"
    static String METHOD = "method"
    static String KLASS = "class"
    static String SP = "  "

    ProfileTime time = new ProfileTime(0L)
    List<ProfileEntry> entries = []

    public Profile(List<ProfileCallEntry> callEntries) {
        Map<String, ProfileEntry> entryMap = [:]
        for (int i = 0; i < callEntries.size(); i++) {
            def callEntry = callEntries[i]
            def key = sprintf("%s.%s", callEntry.className, callEntry.methodName);
            def entry = entryMap[key]
            if (entry == null) {
                entry = new ProfileEntry(callEntry.className, callEntry.methodName)
                entryMap[key] = entry
                entries << entry
            }
            entry.callEntries << callEntry
            entry.time += callEntry.time
            time += callEntry.time
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
            columnWidth.timePerCall = Math.max(columnWidth.timePerCall, sprintf("%.2f", entry.timePerCall.us()).size())
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
                "%%%d.2f" + // timePerCall
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
                    entry.timePerCall.us(),
                    entry.methodName,
                    entry.className
            )
        }
    }
}
