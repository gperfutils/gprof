package gprof

class Profile {

    private Map prof

    Profile(Map prof) {
        this.prof = prof
    }

    static String TIME = "ms"
    static String PCT = "%"
    static String CALLS = "calls"
    static String TIME_PER_CALL = "ms/call"
    static String METHOD = "method"
    static String KLASS = "class"
    static String SP = "  "

    void prettyPrint() {
        def columnWidth = [
                time: TIME.size(), pct: PCT.size(), calls: CALLS.size(),
                timePerCall: TIME_PER_CALL.size(), method: METHOD.size(), klass: KLASS.size()
        ]
        prof.classes.each { String className, Map cprof ->
            cprof.methods.each { String methodName, Map mprof ->
                columnWidth.time = Math.max(columnWidth.time, sprintf("%.2f", mprof.time / 1000 / 1000).size())
                columnWidth.pct = Math.max(columnWidth.pct, sprintf("%.2f", mprof.time / prof.time * 100).size())
                columnWidth.calls = Math.max(columnWidth.calls, mprof.calls.size().toString().size())
                columnWidth.timePerCall = Math.max(columnWidth.timePerCall, sprintf("%.2f", mprof.timePerCall / 1000 / 1000).size())
                columnWidth.method = Math.max(columnWidth.method, methodName.size())
                columnWidth.klass = Math.max(columnWidth.klass, className.size())
            }
        }
        // header
        def headerFormat =
            sprintf(
                "%%-%ds" + // time
                "%s" +
                "%%-%ds" + // pct
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
                columnWidth.time,
                SP,
                columnWidth.pct,
                SP,
                columnWidth.calls,
                SP,
                columnWidth.timePerCall,
                SP,
                columnWidth.method,
                SP,
                columnWidth.klass)
        printf(headerFormat, TIME, PCT, CALLS, TIME_PER_CALL, METHOD, KLASS)

        // rows
        def rowFormat =
            sprintf(
                "%%%d.2f" + // time
                "%s" +
                "%%%d.2f" + // pct
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
                columnWidth.time,
                SP,
                columnWidth.pct,
                SP,
                columnWidth.calls,
                SP,
                columnWidth.timePerCall,
                SP,
                columnWidth.method,
                SP,
                columnWidth.klass
        )
        prof.classes.each { String className, Map cprof ->
            cprof.methods.each { String methodName, Map mprof ->
                printf(rowFormat,
                        mprof.time / 1000 / 1000,
                        mprof.time / prof.time * 100,
                        mprof.calls.size(),
                        mprof.timePerCall / 1000 / 1000,
                        methodName,
                        className
                )
            }
        }
    }
}
