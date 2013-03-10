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
    static String NAME = "method"

    void prettyPrint() {
        def columnWidth = [
                time: TIME.size(), pct: PCT.size(), calls: CALLS.size(),
                timePerCall: TIME_PER_CALL.size(), name: NAME.size()
        ]
        prof.classes.each { String className, Map cprof ->
            cprof.methods.each { String methodName, Map mprof ->
                columnWidth.time = Math.max(columnWidth.time, sprintf("%.2f", mprof.time / 1000 / 1000).size())
                columnWidth.pct = Math.max(columnWidth.pct, sprintf("%.2f", mprof.time / prof.time * 100).size())
                columnWidth.calls = Math.max(columnWidth.calls, mprof.calls.size().toString().size())
                columnWidth.timePerCall = Math.max(columnWidth.timePerCall, sprintf("%.2f", mprof.timePerCall / 1000 / 1000).size())
                columnWidth.name = Math.max(columnWidth.name, "$className.$methodName".size())
            }
        }
        // header
        def headerFormat =
            sprintf("%%-%ds %%-%ds %%-%ds %%-%ds %%-%ds%n",
                    columnWidth.time, columnWidth.pct, columnWidth.calls, columnWidth.timePerCall, columnWidth.name)
        printf(headerFormat, TIME, PCT, CALLS, TIME_PER_CALL, NAME)

        // rows
        def rowFormat = sprintf("%%%d.2f %%%d.2f %%%dd %%%d.2f %%-%ds%n",
                columnWidth.time - ".00".size(), columnWidth.pct - ".00".size(),
                columnWidth.calls, columnWidth.timePerCall - ".00".size(), columnWidth.name)
        prof.classes.each { String className, Map cprof ->
            cprof.methods.each { String methodName, Map mprof ->
                printf(rowFormat,
                        mprof.time / 1000 / 1000,
                        mprof.time / prof.time * 100,
                        mprof.calls.size(),
                        mprof.timePerCall / 1000 / 1000,
                        "$className.$methodName"
                )
            }
        }
    }
}
