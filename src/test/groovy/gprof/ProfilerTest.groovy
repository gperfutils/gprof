package gprof

import org.junit.Test

import java.util.concurrent.Callable

import static org.junit.Assert.*

class ProfilerTest {

    @Test void startAndStop() {
        def p = new Profiler()
        p.start()
        Thread.sleep(1)
        p.stop()
        boolean b = false
        p.result.methodEntries.find { e ->
            if (e.className == Thread.class.name &&
                e.methodName == "sleep" &&
                e.callEntries.size() == 1) {
                b = true
                return false
            }
            return true
        }
        assert b
    }

    @Test void restart() {
        def p = new Profiler()
        p.start()
        Thread.sleep(1)
        p.stop()
        p.start()
        Thread.sleep(1)
        p.stop()
        boolean b = false
        p.result.methodEntries.find { e ->
            if (e.className == Thread.class.name &&
                e.methodName == "sleep" &&
                e.callEntries.size() == 2) {
                b = true
                return false
            }
            return true
        }
        assert b
    }

    @Test void reset() {
        def p = new Profiler()
        p.start()
        Thread.sleep(1)
        p.stop()
        p.reset()
        p.start()
        Thread.sleep(1)
        p.stop()
        boolean b = false
        p.result.methodEntries.find { e ->
            if (e.className == Thread.class.name &&
                e.methodName == "sleep" &&
                e.callEntries.size() == 1) {
                b = true
                return false
            }
            return true
        }
        assert b
    }

}
